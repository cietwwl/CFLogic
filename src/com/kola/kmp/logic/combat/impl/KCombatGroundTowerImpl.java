package com.kola.kmp.logic.combat.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.koala.game.KGameMessage;
import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatGround;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.resulthandler.ICombatResult;
import com.kola.kmp.logic.combat.resulthandler.ICombatRoleResult;
import com.kola.kmp.logic.level.tower.KTowerData;
import com.kola.kmp.protocol.fight.KFightProtocol;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatGroundTowerImpl extends KCombatGroundBaseImpl implements ICombatGround {

	private static final float SEND_MORE_BOTTOM_LINE = 0.9f;
	private static final int _BEGIN_INSTANCE_ID = 100;
	
//	private ICombatForce _monsterForce; // 怪物队伍，以玩家未目标的障碍都会被当成怪物队伍
//	private ICombatForce _neutralForce; // 中立队伍，不能攻击的障碍物，以及以玩家和怪物为目标的障碍物会被当成中立队伍
	private List<ICombatMember> _monsterMembers;
	private List<ICombatMember> _nextNeutral;
	private int _preTowerId; // 当前上一个的波数id
	private int _currentTowerId; // 当前的波数id
	private int _nextTowerId; // 下一波的波数id
	private AtomicInteger _instancingIdGenerator = new AtomicInteger(_BEGIN_INSTANCE_ID);
	private Map<Integer, KTowerData> _towerDatas;
	private AtomicInteger _shadowIdGenerator;
	private KCombatSpecialEffect _addEnergyEffect;
	private Set<Short> _deadMemberShadowIds; // 已经死亡的怪物集合
	private int _moreMonsterSerialId = 0;
//	private KGameMessage _nextTowerMonsterMsg;
	private Map<Integer, KGameMessage> _nextTowerMsgMap = new HashMap<Integer, KGameMessage>();
	private Map<Integer, List<Short>> _monsterIdsOfTower; // 每一波数的怪物生成之后的shadowId，只要是用来在结算的时候判断，这一波怪物是否全部死亡
	
	private float _leftCorX;
	private float _leftCorY;
	private float _rightCorX;
	private float _rightCorY;
	
	KCombatGroundTowerImpl() {
		this._nextNeutral = new ArrayList<ICombatMember>();
		this._deadMemberShadowIds = new HashSet<Short>();
		this._monsterIdsOfTower = new LinkedHashMap<Integer, List<Short>>();
		this._shadowIdGenerator = new AtomicInteger();
		this._towerDatas = new HashMap<Integer, KTowerData>();
		this._monsterMembers = new ArrayList<ICombatMember>();
	}
	
//	private void genInstanceIdToEntrances(List<KCombatEntrance> pEntranceList) {
//		for (int i = 0; i < pEntranceList.size(); i++) {
//			pEntranceList.get(i).setClientInstancingId(_instancingIdGenerator.incrementAndGet());
//		}
//	}
	
	private void init(AtomicInteger beginShadowId, List<KCombatEntrance> pEntranceList, KCombatSpecialEffect pAddEnergyEffect, ICombat pCombat) {
//		this.genInstanceIdToEntrances(pEntranceList);
		this.fillCombatMembers(beginShadowId, pEntranceList, pAddEnergyEffect, pCombat, _monsterMembers, _nextNeutral);
		this.monsterForce.addAllMembersToForce(_monsterMembers);
		this.neutralForce.addAllMembersToForce(_nextNeutral);	
		_shadowIdGenerator.set(beginShadowId.intValue());
		beginShadowId.addAndGet(6000); // 留6000个空位给这里生成对象使用
		_addEnergyEffect = pAddEnergyEffect;
		this.recordMonsterIds();
	}
	
	private void recordMonsterIds() {
		List<Short> list = this._monsterIdsOfTower.get(_currentTowerId);
		if (list == null) {
			list = new ArrayList<Short>();
			this._monsterIdsOfTower.put(_currentTowerId, list);
		}
		for (int i = 0; i < _monsterMembers.size(); i++) {
			list.add(_monsterMembers.get(i).getShadowId());
		}
	}
	
	private boolean prepareNextTowerMonsters(ICombat combat) {
		KTowerData nextTowerData = _towerDatas.get(this._nextTowerId);
		if(nextTowerData != null) {
			this._preTowerId = this._currentTowerId;
			this._currentTowerId = nextTowerData.getTowerId();
			this._nextTowerId = nextTowerData.getNextTowerId();
			this._monsterMembers.clear();
			this._nextNeutral.clear();
			List<KCombatEntrance> nextEntranceList = KCombatManager.getTowerMonsters(nextTowerData.getLeftMonsterMap(), _leftCorX, _leftCorY);
			nextEntranceList.addAll(KCombatManager.getTowerMonsters(nextTowerData.getRightMonsterMap(), _rightCorX, _rightCorY));
//			this.genInstanceIdToEntrances(nextEntranceList);
			this.fillCombatMembers(_shadowIdGenerator, nextEntranceList, _addEnergyEffect, combat, _monsterMembers, _nextNeutral);
			this.monsterForce.addAllMembersToForce(_monsterMembers);
			combat.notifyMemberAddToCombatGround(_monsterMembers);
			combat.notifyMemberAddToCombatGround(_nextNeutral);
			this.recordMonsterIds();
			this._moreMonsterSerialId++;
			ICombat.LOGGER.info("生成更多怪物，战场id：{}，当前id：{}，当前怪物id：{}", combat.getSerialId(), this._moreMonsterSerialId, _monsterIdsOfTower.get(_currentTowerId));
			return true;
		}
		return false;
	}
	
	private void processClearTower(ICombat combat, ICombatResult result, int currentTowerId, int preTowerId) {
		int clearTowerId = 0;
		if (currentTowerId > 0) {
			List<Short> shadowIdList = this._monsterIdsOfTower.get(currentTowerId);
			boolean allClear = true;
			if (shadowIdList != null) {
				for (int i = 0; i < shadowIdList.size(); i++) {
					if (!_deadMemberShadowIds.contains(shadowIdList.get(i))) {
						allClear = false;
						break;
					}
				}
			}
			if (allClear) {
				clearTowerId = currentTowerId;
			} else {
				if (preTowerId > 0) {
					currentTowerId = preTowerId;
					KTowerData tempTower;
					for (Iterator<KTowerData> itr = this._towerDatas.values().iterator(); itr.hasNext();) {
						tempTower = itr.next();
						if (tempTower.getNextTowerId() == preTowerId) {
							preTowerId = tempTower.getTowerId();
						}
					}
					if (preTowerId == currentTowerId) {
						preTowerId = 0;
					}
					this.processClearTower(combat, result, currentTowerId, preTowerId);
					return;
				}
			}
		}
		result.putAttributeToResult(ICombatResult.KEY_CLEAR_TOWER_ID, clearTowerId);
		if(clearTowerId > 0) {
			KTowerData towerData = this._towerDatas.get(clearTowerId);
			result.putAttributeToResult(ICombatResult.KEY_CLEAR_TOWER_WAVE_NUM, towerData.getWaveNum());
		} else {
			result.putAttributeToResult(ICombatResult.KEY_CLEAR_TOWER_WAVE_NUM, 0);
		}
	}
	
//	private void sendMoreMonsters(int serialNum, ICombat combat, boolean first) {
//		KGameMessage msg = this.createMoreMonsterMsg(_moreMonsterSerialId, _monsterMembers);
//		ICombat.LOGGER.info("发送更多的怪物，消息长度：{}，当前波数：{}，怪物的所有id：{}，{}", msg.getPayloadLength(), this._currentTowerId);
//		combat.sendMsgToAll(msg);
//		KGameMessage msg = _nextTowerMsgMap.get(serialNum);
//		if(msg != null) {
//			ICombat.LOGGER.info("发送更多的怪物，消息长度：{}，当前波数：{}", msg.getPayloadLength(), this._currentTowerId);
//			combat.sendMsgToAll(first ? msg : msg.duplicate());
//		}
//	}
	
	@Override
	protected void notifyDispose() {
		this._monsterMembers.clear();
		this._nextNeutral.clear();
		this._preTowerId = 0;
		this._nextTowerId = 0;
		this._currentTowerId = 0;
		this._moreMonsterSerialId = 0;
		this._nextTowerMsgMap.clear();
		this._instancingIdGenerator.set(_BEGIN_INSTANCE_ID);
		this._towerDatas.clear();
		this._shadowIdGenerator.set(0);
		this._addEnergyEffect = null;
		this._deadMemberShadowIds.clear();
		this._monsterIdsOfTower.clear();
	}
	
	@Override
	public boolean isFirst() {
		return true;
	}
	
	@Override
	public void notifyMemberDead(ICombat combat, ICombatMember member) {
		if (!_deadMemberShadowIds.contains(member.getShadowId())) {
			_deadMemberShadowIds.add(member.getShadowId());
//			ICombat.LOGGER.info("塔防战斗，通知怪物死亡，战场id:{}，怪物id:{}", combat.getSerialId(), member.getShadowId());
			float pct = (float) _deadMemberShadowIds.size() / this.monsterForce.getAllMembers().size();
			if (pct >= SEND_MORE_BOTTOM_LINE) {
//				if (this.prepareNextTowerMonsters(combat)) {
//					this.sendMoreMonsters(combat);
//				}
				if (this.prepareNextTowerMonsters(combat)) {
					KGameMessage msg = createMoreMonsterMsg(_moreMonsterSerialId, _monsterMembers);
					this._nextTowerMsgMap.put(_moreMonsterSerialId, msg);
					combat.sendMsgToAll(msg);
					ICombat.LOGGER.info("发送更多的怪物，消息长度：{}，当前波数：{}", msg.getPayloadLength(), this._currentTowerId);
				}
			}
		}
	}
	
	@Override
	public void processCombatFinish(ICombat combat, ICombatResult result) {
		this.recordMonsterHpInfo(monsterForce.getAllMembers(), result);
		this.processClearTower(combat, result, _currentTowerId, _preTowerId);
	}
	
	@Override
	public void processRoleResult(ICombat combat, ICombatResult result, ICombatRoleResult roleResult) {
		Long friendId = (Long)result.getAttachment();
		ICombatMember friendMember = combat.getRoleTypeMemberBySrcId(friendId);
		roleResult.setAttachment(friendMember);
	}
	
	@Override
	public void messageReceived(ICombat combat, KGameMessage msg) {
		super.messageReceived(combat, msg);
		switch (msg.getMsgID()) {
		case KFightProtocol.CM_REQUEST_MORE_MONSTER:
			int serialNum = msg.readByte();
			ICombat.LOGGER.info("客户端请求更多怪物！战场id：{}，客户端当前的id：{}", combat.getSerialId(), serialNum);
			KGameMessage sendMsg = _nextTowerMsgMap.get((serialNum + 1));
			if (sendMsg != null) {
				ICombat.LOGGER.info("发送更多的怪物，消息长度：{}，当前波数：{}", sendMsg.getPayloadLength(), this._currentTowerId);
				combat.sendMsgToAll(sendMsg.duplicate());
			}
			break;
		}
	}
	
	public static class KCombatGroundTowerBuilder extends KCombatGroundBuilderBaseImpl implements ICombatGroundBuilder {

		private Map<Integer, KTowerData> _towerDatas;
		private int _currentTowerId;
		private int _nextTowerId;
		private float _leftCorX;
		private float _leftCorY;
		private float _rightCorX;
		private float _rightCorY;

		public KCombatGroundTowerBuilder towerDatas(Map<Integer, KTowerData> pTowerDatas) {
			this._towerDatas = new HashMap<Integer, KTowerData>(pTowerDatas);
			return this;
		}
		
		public KCombatGroundTowerBuilder currentTowerId(int pCurrentTowerId) {
			this._currentTowerId = pCurrentTowerId;
			return this;
		}
		
		public KCombatGroundTowerBuilder nextTowerId(int pNextTowerId) {
			this._nextTowerId = pNextTowerId;
			return this;
		}
		
		public KCombatGroundTowerBuilder leftBornPointInfo(float x, float y) {
			this._leftCorX = x;
			this._leftCorY = y;
			return this;
		}
		
		public KCombatGroundTowerBuilder rightBornPointInfo(float x, float y) {
			this._rightCorX = x;
			this._rightCorY = y;
			return this;
		}
		
		@Override
		public ICombatGround build(ICombat combat, AtomicInteger pShadowId) {
			KCombatGroundTowerImpl towerCombat = KCombatGroundPool.borrowTower();
			this.baseBuild(towerCombat);
			towerCombat._towerDatas = this._towerDatas;
			towerCombat._currentTowerId = this._currentTowerId;
			towerCombat._nextTowerId = this._nextTowerId;
			towerCombat._leftCorX = this._leftCorX;
			towerCombat._leftCorY = this._leftCorY;
			towerCombat._rightCorX = this._rightCorX;
			towerCombat._rightCorY = this._rightCorY;
			towerCombat.init(pShadowId, entranceList, addEnergyEffect, combat);
			return towerCombat;
		}
		
	}

}
