package com.kola.kmp.logic.combat.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.koala.game.KGameMessage;
import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatEventListener;
import com.kola.kmp.logic.combat.ICombatGround;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.KCombatType;
import com.kola.kmp.logic.combat.api.ICombatMonster;
import com.kola.kmp.logic.combat.event.KPetCopyDieEvent;
import com.kola.kmp.logic.combat.resulthandler.ICombatResult;
import com.kola.kmp.logic.npc.KNPCDataStructs.KMonstTemplate;
import com.kola.kmp.logic.support.CombatModuleSupport.ICombatMonsterUpdateInfo;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 
 * 表示战场里面其中给一个地图层的数据
 * 
 * @author PERRY CHAN
 */
public class KCombatGroundImpl extends KCombatGroundBaseImpl implements ICombatGround {

	private boolean _first;
	private Map<Integer, Map<KMonstTemplate, Integer>> _genMonstersWhenDead;
	private AtomicInteger _shadowIdGenerator = new AtomicInteger();
	private KCombatSpecialEffect _addEnergyEffect;
	
	
	
	private void init(AtomicInteger beginShadowId, List<KCombatEntrance> pEntraceList, KCombatSpecialEffect pAddEnergyEffect, ICombat pCombat) {
		List<ICombatMember> monsters = new ArrayList<ICombatMember>();
		List<ICombatMember> neutrals = new ArrayList<ICombatMember>();
		this.fillCombatMembers(beginShadowId, pEntraceList, pAddEnergyEffect, pCombat, monsters, neutrals);
		this.monsterForce.addAllMembersToForce(monsters);
		this.neutralForce.addAllMembersToForce(neutrals);
		_addEnergyEffect = pAddEnergyEffect;
		_shadowIdGenerator.set(beginShadowId.getAndAdd(1000));
	}
	
	
	@Override
	protected void notifyDispose() {
		this._first = false;
		this._shadowIdGenerator.set(0);
		if(this._genMonstersWhenDead != null) {
			this._genMonstersWhenDead.clear();
			this._genMonstersWhenDead = null;
		}
		this._addEnergyEffect = null;
	}
	
	@Override
	public boolean isFirst() {
		return _first;
	}
	
	@Override
	public void processCombatFinish(ICombat combat, ICombatResult result) {
		this.recordMonsterHpInfo(monsterForce.getAllMembers(), result);
	}
	
	@Override
	public void notifyMemberDead(ICombat combat, ICombatMember member) {
		if (_genMonstersWhenDead != null) {
			Integer instanceId = shadowIdToInstanceId.get(member.getShadowId());
			if (instanceId != null) {
				Map<KMonstTemplate, Integer> map = _genMonstersWhenDead.get(instanceId);
				if (map != null) {
					List<ICombatMember> monsterList = new ArrayList<ICombatMember>();
					List<ICombatMember> neutralList = new ArrayList<ICombatMember>();
					List<KCombatEntrance> entranceList = new ArrayList<KCombatEntrance>();
					KCombatEntrance entrance;
					ICombatMonster monster;
					Map.Entry<KMonstTemplate, Integer> entry;
					for (Iterator<Map.Entry<KMonstTemplate, Integer>> itr = map.entrySet().iterator(); itr.hasNext();) {
						entry = itr.next();
						monster = KSupportFactory.getNpcModuleSupport().getCombatMonster(entry.getKey());
						entrance = KCombatEntrancePool.borrowEntrance(monster, null, member.getX(), member.getY(), 0);
						entranceList.add(entrance);
					}
					this.fillCombatMembers(_shadowIdGenerator, entranceList, _addEnergyEffect, combat, monsterList, neutralList);
					this.monsterForce.addAllMembersToForce(monsterList);
					combat.notifyMemberAddToCombatGround(monsterList);
					KGameMessage msg = this.createMoreMonsterMsg(0, monsterList);
					combat.sendMsgToAll(msg);
					if(combat.getCombatType() == KCombatType.PET_COPY) {
						List<Short> monsterIds = new ArrayList<Short>();
						for(int i = 0; i < monsterList.size(); i++) {
							monsterIds.add(monsterList.get(i).getShadowId());
						}
						ICombatEventListener event = new KPetCopyDieEvent(shadowIdToInstanceId.get(member.getShadowId()), monsterIds);
						for(int i = 0; i < monsterList.size(); i++) {
							monsterList.get(i).registPermanentEffect(event);
						}
					}
				} else if (combat.getCombatType() == KCombatType.PET_COPY) {
					if (member.getMemberType() == ICombatMember.MEMBER_TYPE_BLOCK) {
						combat.recordKillInstanceId(shadowIdToInstanceId.get(member.getShadowId()));
					}
				}
			}
		}
	}
	
	public static class KCombatGroundBuilder  extends KCombatGroundBuilderBaseImpl implements ICombatGroundBuilder {
		
		private boolean _first;
		private Map<Integer, ICombatMonsterUpdateInfo> _updateInfoMap;
		private Map<Integer, Map<KMonstTemplate, Integer>> _genMonstersWhenDead;
		
		public KCombatGroundBuilder first(boolean pFirst) {
			this._first = pFirst;
			return this;
		}
		
		public KCombatGroundBuilder updateInfo(Map<Integer, ICombatMonsterUpdateInfo> map) {
			_updateInfoMap = new HashMap<Integer, ICombatMonsterUpdateInfo>(map);
			return this;
		}
		
		public KCombatGroundBuilder genMonstersWhenDead(Map<Integer, Map<KMonstTemplate, Integer>> pGenMonstersWhenDead) {
			this._genMonstersWhenDead = pGenMonstersWhenDead;
			return this;
		}
		
		public ICombatGround build(ICombat combat, AtomicInteger pShadowId) {
			KCombatGroundImpl impl = KCombatGroundPool.borrowCommon();
			this.baseBuild(impl);
			impl._first = this._first;
			if (_updateInfoMap != null) {
				Map.Entry<Integer, ICombatMonsterUpdateInfo> entry;
				KCombatEntrance entrance;
				for (Iterator<Map.Entry<Integer, ICombatMonsterUpdateInfo>> itr = _updateInfoMap.entrySet().iterator(); itr.hasNext();) {
					entry = itr.next();
					for (int i = 0; i < entranceList.size(); i++) {
						entrance = entranceList.get(i);
						if (entrance.getClientInstancingId() == entry.getKey()) {
							if (entry.getValue().getCurrentHp() > 0) {
								entrance.updateHp(entry.getValue().getCurrentHp());
							} else {
								entranceList.remove(i);
								KCombatEntrancePool.returnEntrance(entrance);
							}
							break;
						}
					}
				}
			}
			impl._genMonstersWhenDead = this._genMonstersWhenDead;
			impl.init(pShadowId, entranceList, addEnergyEffect, combat);
			return impl;
		}
	}

}
