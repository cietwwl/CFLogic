package com.kola.kmp.logic.combat.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.koala.game.KGameMessage;
import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatForce;
import com.kola.kmp.logic.combat.ICombatGround;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.resulthandler.ICombatResult;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatGroundBarrelImpl extends KCombatGroundBaseImpl {

//	private static final int _BEGIN_INSTANCE_ID = 100;
	
//	private Map<KCombatEntrance, Long> _entranceMap = new HashMap<KCombatEntrance, Long>();
	private Map<ICombatMember, Long> _remainMonsterMap = new HashMap<ICombatMember, Long>();
//	private AtomicInteger _idGenerator = new AtomicInteger();
	private KCombatSpecialEffect _addEnergyEffect;
	
//	private AtomicInteger _instancingIdGenerator = new AtomicInteger(_BEGIN_INSTANCE_ID);
	
	private <T> void getBornEntrance(Map<T, Long> entranceMap, long compareTime, List<T> list) {
		Map.Entry<T, Long> entry;
		for (Iterator<Map.Entry<T, Long>> itr = entranceMap.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			if (entry.getValue() <= compareTime) {
				itr.remove();
				list.add(entry.getKey());
			}
		}
	}
	
	private void init(AtomicInteger beginShadowId, Map<KCombatEntrance, Long> entranceMap, KCombatSpecialEffect pAddEnergyEffect, ICombat pCombat) {
//		for (Iterator<KCombatEntrance> itr = entranceMap.keySet().iterator(); itr.hasNext();) {
//			itr.next().setClientInstancingId(_instancingIdGenerator.incrementAndGet());
//		}
		List<ICombatMember> monsters = new ArrayList<ICombatMember>();
		List<ICombatMember> neutrals = new ArrayList<ICombatMember>();
		long compareTime = 1000;
		List<KCombatEntrance> list = new ArrayList<KCombatEntrance>();
		do {
			this.getBornEntrance(entranceMap, compareTime, list);
			compareTime += 1000;
			if(compareTime > 120000) {
				break;
			}
		} while (list.isEmpty());
		this.fillCombatMembers(beginShadowId, list, pAddEnergyEffect, pCombat, monsters, neutrals);
		this.monsterForce.addAllMembersToForce(monsters);
		this.neutralForce.addAllMembersToForce(neutrals);
		monsters.clear();
		neutrals.clear();
		this.fillCombatMembers(beginShadowId, new ArrayList<KCombatEntrance>(entranceMap.keySet()), _addEnergyEffect, pCombat, monsters, neutrals);
		monsters.addAll(neutrals);
		ICombatMember temp;
		Map.Entry<KCombatEntrance, Long> entry;
		int instanceId;
		for (int i = 0; i < monsters.size(); i++) {
			temp = monsters.get(i);
			instanceId = this.shadowIdToInstanceId.get(temp.getShadowId());
			for (Iterator<Map.Entry<KCombatEntrance, Long>> itr = entranceMap.entrySet().iterator(); itr.hasNext();) {
				entry = itr.next();
				if (entry.getKey().getClientInstancingId() == instanceId) {
					_remainMonsterMap.put(temp, entry.getValue());
					itr.remove();
					break;
				}
			}
		}
		pCombat.notifyMemberAddToCombatGround(monsters);
	}
	
	@Override
	public boolean isFirst() {
		return true;
	}

	@Override
	public void processCombatFinish(ICombat combat, ICombatResult result) {
		
	}

	@Override
	protected void notifyDispose() {
//		_instancingIdGenerator.set(_BEGIN_INSTANCE_ID);
		_remainMonsterMap.clear();
	}
	
	@Override
	public void notifyStart(ICombat combat) {
		ICombat.LOGGER.info("！！通知战斗开始！！");
		ICombatMember member;
		for(Iterator<ICombatMember> itr = _remainMonsterMap.keySet().iterator(); itr.hasNext();) {
			member = itr.next();
			switch(member.getForceType()) {
			case ICombatForce.FORCE_TYPE_MONSTER_SIDE:
				this.monsterForce.addMemberToForce(member);
				break;
			case ICombatForce.FORCE_TYPE_NEUTRAL:
				this.neutralForce.addMemberToForce(member);
				break;
			}
		}
	}
	
	@Override
	public void notifyTime(ICombat combat, long currentTime) {
		int sub = (int)(currentTime - combat.getCombatStartTime());
		if (this._remainMonsterMap.size() > 0) {
			List<ICombatMember> list = new ArrayList<ICombatMember>();
			this.getBornEntrance(_remainMonsterMap, sub, list);
			if(list.size() > 0) {
//				ICombatMember member;
//				for(int i = 0; i < list.size(); i++) {
//					member = list.get(i);
//					if(member.getForceType() == ICombatForce.FORCE_TYPE_MONSTER_SIDE) {
//						this.monsterForce.addMemberToForce(member);
//					} else {
//						this.neutralForce.addMemberToForce(member);
//					}
//				}
				KGameMessage msg = this.createMoreMonsterMsg(0, list);
//				LOGGER.info("发送更多的怪物，消息长度：{}", msg.getPayloadLength());
				combat.sendMsgToAll(msg);
			}
		}
	}
	
	public static class KCombatGroundBarrelBuilder extends KCombatGroundBuilderBaseImpl {

		private Map<KCombatEntrance, Long> _entranceMap;
		
		public KCombatGroundBarrelBuilder entranceMap(Map<KCombatEntrance, Long> pBornTimeMap) {
			this._entranceMap = pBornTimeMap;
			return this;
		}
		
		@Override
		public void onStartCombatFail() {
			List<KCombatEntrance> tempEntranceSet = _entranceMap == null ? null : new ArrayList<KCombatEntrance>(_entranceMap.keySet());
			if (tempEntranceSet != null) {
				KCombatManager.releaseEntrance(tempEntranceSet);
			}
		}
		
		@Override
		public ICombatGround build(ICombat combat, AtomicInteger pShadowId) {
			KCombatGroundBarrelImpl barrelGround = KCombatGroundPool.borrowBarrel();
			super.baseBuild(barrelGround);
			barrelGround.init(pShadowId, _entranceMap, addEnergyEffect, combat);
			return barrelGround;
		}

		
	}

}
