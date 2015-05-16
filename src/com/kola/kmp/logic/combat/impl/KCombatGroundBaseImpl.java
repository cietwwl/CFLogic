package com.kola.kmp.logic.combat.impl;

import static com.kola.kmp.protocol.fight.KFightProtocol.SM_TOWER_ADD_MONSTERS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatDropInfo;
import com.kola.kmp.logic.combat.ICombatForce;
import com.kola.kmp.logic.combat.ICombatGround;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.ICombatMinion;
import com.kola.kmp.logic.combat.resulthandler.ICombatResult;
import com.kola.kmp.logic.combat.resulthandler.ICombatRoleResult;

/**
 * 
 * @author PERRY CHAN
 */
public abstract class KCombatGroundBaseImpl implements ICombatGround {

	private int _serialId;
	private int _battleFieldTemplateId;
	private String _mapResPath;
	private int _bgAudioResId;
	private int _nextBattleFieldTemplateId;
	protected final List<ICombatDropInfo> dropInfos;
	protected final Map<Integer, Short> dropIdToShadowIds;
	protected final Map<Short, List<Integer>> shadowIdToDropId;
	protected final Map<Short, Integer> shadowIdToInstanceId;
	protected final ICombatForce monsterForce; // 怪物队伍，以玩家未目标的障碍都会被当成怪物队伍
	protected final ICombatForce neutralForce; // 中立队伍，不能攻击的障碍物，以及以玩家和怪物为目标的障碍物会被当成中立队伍
	
	protected KCombatGroundBaseImpl() {
		this._serialId = ICombatGround.combatGroundIdGenerator.incrementAndGet();
		this.dropInfos = new ArrayList<ICombatDropInfo>();
		this.dropIdToShadowIds = new HashMap<Integer, Short>();
		this.shadowIdToDropId = new HashMap<Short, List<Integer>>();
		this.shadowIdToInstanceId = new HashMap<Short, Integer>();
		this.monsterForce = new KCombatForceImpl();
		this.neutralForce = new KCombatForceImpl();
	}
	
	protected abstract void notifyDispose();
	
	private List<ICombatDropInfo> getMemberDropInfo(short shadowId) {
		List<Integer> dropIds = this.shadowIdToDropId.get(shadowId);
		if (dropIds != null) {
			List<ICombatDropInfo> list = new ArrayList<ICombatDropInfo>();
			ICombatDropInfo dropInfo;
			for (int i = 0; i < dropIds.size(); i++) {
				dropInfo = this.getDropInfo(dropIds.get(i));
				if (dropInfo != null) {
					list.add(dropInfo);
				}
			}
			return list;
		}
		return null;
	}
	
	protected void handleDropInfo(List<ICombatDropInfo> pDropInfos, short memberShadowId) {
		if (pDropInfos != null && pDropInfos.size() > 0) {
			List<Integer> list = new ArrayList<Integer>();
			this.shadowIdToDropId.put(memberShadowId, list);
			ICombatDropInfo dropInfo;
			for (int i = 0; i < pDropInfos.size(); i++) {
				dropInfo = pDropInfos.get(i);
				this.dropInfos.add(dropInfo);
				this.dropIdToShadowIds.put(dropInfo.getSerialId(), memberShadowId);
				list.add(dropInfo.getSerialId());
			}
		}
	}
	
	protected void recordMonsterHpInfo(List<ICombatMember> memberList, ICombatResult result) {
		ICombatMember member;
		for (int i = 0; i < memberList.size(); i++) {
			member = memberList.get(i);
			result.recordMonsterHpInfo(this.shadowIdToInstanceId.get(member.getShadowId()), member);
		}
	}
	
	protected void packMemberInternal(List<ICombatMember> memberList, KGameMessage msg) {
		ICombatMember member;
		List<ICombatDropInfo> allDropInfos;
		ICombatDropInfo dropInfo;
		boolean hasDrop;
		msg.writeByte(memberList.size());
		for(int i = 0; i < memberList.size(); i++) {
			member = memberList.get(i);
			allDropInfos = this.getMemberDropInfo(member.getShadowId());
			hasDrop = allDropInfos != null;
			msg.writeInt(shadowIdToInstanceId.get(member.getShadowId()));
//			LOGGER.info("member.getShadowId()={}, member.instanceId={}", member.getShadowId(), shadowIdToInstanceId.get(member.getShadowId()));
			member.packDataToMsg(msg);
			msg.writeBoolean(hasDrop);
			if(hasDrop) {
				msg.writeByte(allDropInfos.size());
				for (int k = 0; k < allDropInfos.size(); k++) {
					dropInfo = allDropInfos.get(k);
					msg.writeInt(dropInfo.getSerialId());
					msg.writeInt(dropInfo.getResId());
					msg.writeByte(dropInfo.getType());
					msg.writeUtf8String(dropInfo.getDescr());
					msg.writeUtf8String(dropInfo.getDetail());
					dropInfo.packAdditionalInfoToMsg(msg);
				}
			}
		}
	}
	
	protected void fillCombatMembers(AtomicInteger beginShadowId, List<KCombatEntrance> srcList, KCombatSpecialEffect pAddEnergyEffect, ICombat pCombat, List<ICombatMember> outMonsters, List<ICombatMember> outNeutrals) {
		KCombatEntrance entrance;
		ICombatMember member;
		for (int i = 0; i < srcList.size(); i++) {
			entrance = srcList.get(i);
			member = KCombatMemberFactory.getCombatMemberInstance(entrance.getMemberType());
			member.init(entrance.getForceType(), (short)beginShadowId.incrementAndGet(), System.currentTimeMillis(), entrance, pCombat);
			switch (entrance.getForceType()) {
			case ICombatForce.FORCE_TYPE_MONSTER_SIDE:
				outMonsters.add(member);
				member.registPermanentEffect(pAddEnergyEffect);
				break;
			case ICombatForce.FORCE_TYPE_NEUTRAL:
				outNeutrals.add(member);
				break;
			}
//			switch(entrance.getMemberType()) {
//			case ICombatMember.MEMBER_TYPE_BLOCK:
//			case ICombatMember.MEMBER_TYPE_BOSS_MONSTER:
//			case ICombatMember.MEMBER_TYPE_ELITIST_MONSTER:
//			case ICombatMember.MEMBER_TYPE_MONSTER:
//			case ICombatMember.MEMBER_TYPE_BARREL_MONSTER:
//				handleDropInfo(entrance.getDropId(), member.getShadowId());
//				break;
//			}
			if (member.getMemberType() == ICombatMember.MEMBER_TYPE_PET_MONSTER) {
				pCombat.recordMasterIdOfPet(entrance.getOwnerId(), member.getShadowId());
			}
			if (member.isNarrowMonster() || member.getMemberType() == ICombatMember.MEMBER_TYPE_BLOCK) {
				handleDropInfo(entrance.getDropInfos(), member.getShadowId());
			}
			shadowIdToInstanceId.put(member.getShadowId(), entrance.getClientInstancingId());
			KCombatEntrancePool.returnEntrance(entrance);
		}
	}
	
	protected KGameMessage createMoreMonsterMsg(int serialNum, List<ICombatMember> list) {
		KGameMessage msg = KGame.newLogicMessage(SM_TOWER_ADD_MONSTERS);
		msg.writeByte(serialNum);
		this.packMemberInternal(list, msg);
		int size = 0;
		Map<Integer, ICombatMinion> minions;
		int writerIndex = msg.writerIndex();
		msg.writeByte(size);
		for (int i = 0; i < list.size(); i++) {
			minions = list.get(i).getCombatMinions();
			if (minions != null && minions.size() > 0) {
				for (Iterator<ICombatMinion> itr = minions.values().iterator(); itr.hasNext();) {
					itr.next().packDataToMsg(msg);
					size++;
				}
			}
		}
		msg.setByte(writerIndex, size);
		return msg;
	}
		
	@Override
	public final int getSerialId() {
		return _serialId;
	}
	
	@Override
	public final int getBattleFieldTemplateId() {
		return _battleFieldTemplateId;
	}
	
	@Override
	public final String getMapResPath() {
		return _mapResPath;
	}
	
	@Override
	public final int getBgAudioResId() {
		return _bgAudioResId;
	}
	
	@Override
	public final int getNextBattleFieldTemplateId() {
		return _nextBattleFieldTemplateId;
	}
	
	@Override
	public ICombatForce getMonsterForce() {
		return monsterForce;
	}
	
	@Override
	public ICombatForce getNeutralForce() {
		return neutralForce;
	}
	
	@Override
	public void packMemberDataToMsg(KGameMessage msg) {
		List<ICombatMember> allEntities = new ArrayList<ICombatMember>(monsterForce.getAllMembers().size() + neutralForce.getAllMembers().size());
		allEntities.addAll(monsterForce.getAllMembers());
		allEntities.addAll(neutralForce.getAllMembers());
		this.packMemberInternal(allEntities, msg);
	}
	
	@Override
	public void notifyStart(ICombat combat) {
		// 空实现
	}
	
	@Override
	public void notifyMemberDead(ICombat combat, ICombatMember member) {
		// 空实现
	}
	
	@Override
	public void notifyTime(ICombat combat, long currentTime) {
		// 空实现
	}
	
	@Override
	public void processRoleResult(ICombat combat, ICombatResult result, ICombatRoleResult roleResult) {
		
	}
	
	@Override
	public final void dispose() {
		this._battleFieldTemplateId = 0;
		this._bgAudioResId = 0;
		this._mapResPath = null;
		this._nextBattleFieldTemplateId = 0;
		this.dropInfos.clear();
		this.dropIdToShadowIds.clear();
		this.shadowIdToDropId.clear();
		this.shadowIdToInstanceId.clear();
		this.monsterForce.dispose();
		this.neutralForce.dispose();
		this.notifyDispose();
	}
	
	@Override
	public final short getDropOwner(int dropId) {
		Short shadowId = this.dropIdToShadowIds.get(dropId);
		if(shadowId != null) {
			return shadowId;
		}
		return 0;
	}
	
	@Override
	public final ICombatDropInfo getDropInfo(int dropId) {
		if (dropInfos.size() > 0) {
			ICombatDropInfo info;
			for (int i = 0; i < dropInfos.size(); i++) {
				info = dropInfos.get(i);
				if (info.getSerialId() == dropId) {
					return info;
				}
			}
		}
		return null;
	}
	
	@Override
	public void messageReceived(ICombat combat, KGameMessage msg) {
		
	}
	
	protected static abstract class KCombatGroundBuilderBaseImpl implements ICombatGroundBuilder {
	
		private int _battleFieldTemplateId;
		private String _mapResPath;
		private int _bgAudioResId;
		private int _nextBattleFieldTemplateId;
		protected KCombatSpecialEffect addEnergyEffect;
		protected List<KCombatEntrance> entranceList;
		
		public KCombatGroundBuilderBaseImpl battleFieldTemplateId(int pBattleFieldTemplateId) {
			this._battleFieldTemplateId = pBattleFieldTemplateId;
			return this;
		}
		
		public KCombatGroundBuilderBaseImpl mapResPath(String pMapResPath) {
			this._mapResPath = pMapResPath;
			return this;
		}
		
		public KCombatGroundBuilderBaseImpl bgAudioResId(int pBgAudioResId) {
			this._bgAudioResId = pBgAudioResId;
			return this;
		}
		
		public KCombatGroundBuilderBaseImpl nextBattleFieldTemplateId(int pNextBattleFieldTemplateId) {
			this._nextBattleFieldTemplateId = pNextBattleFieldTemplateId;
			return this;
		}
		
		public KCombatGroundBuilderBaseImpl addEnergyEffect(KCombatSpecialEffect pEffect) {
			addEnergyEffect = pEffect;
			return this;
		}
		
		public KCombatGroundBuilderBaseImpl entranceList(List<KCombatEntrance> pEntranceList) {
			this.entranceList = new ArrayList<KCombatEntrance>(pEntranceList);
			return this;
		}
		
		@Override
		public void onStartCombatFail() {
			if (this.entranceList != null && this.entranceList.size() > 0) {
				KCombatManager.releaseEntrance(entranceList);
			}
		}
		
		protected void baseBuild(KCombatGroundBaseImpl impl) {
			impl._battleFieldTemplateId = this._battleFieldTemplateId;
			impl._mapResPath = this._mapResPath;
			impl._bgAudioResId = this._bgAudioResId;
			impl._nextBattleFieldTemplateId = this._nextBattleFieldTemplateId;
		}
	}
}
