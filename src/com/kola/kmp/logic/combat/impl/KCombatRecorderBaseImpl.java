package com.kola.kmp.logic.combat.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.koala.game.KGameMessage;
import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.ICombatRecorder;
import com.kola.kmp.logic.combat.IOperationResult;

/**
 * 
 * @author PERRY CHAN
 */
public abstract class KCombatRecorderBaseImpl implements ICombatRecorder {

	protected short combatMemberId;
	protected ICombat combat;
	protected ICombatMember lastTarget;
	protected ICombatMember lastAttacker;
	protected long timeOfDmByDead;
	protected Map<Short, List<IRecordOfAttack>> _attackRecords = new HashMap<Short, List<ICombatRecorder.IRecordOfAttack>>();
	protected Map<Short, List<Long>> _normalAtkTimeRecord = new HashMap<Short, List<Long>>();
	protected long lastDm;
	protected boolean lastAttackIsSkill;
//	protected List<Integer> hpRecoveryRecord = new ArrayList<Integer>();
	
	@Override
	public final void setCombat(ICombat pCombat) {
		if(combat == null) {
			combat = pCombat;
		}
	}
	
	@Override
	public final void setCombetMemberId(short pMemberId) {
		combatMemberId = pMemberId;
	}
	
	@Override
	public void recordDm(long quantity) {
		this.lastDm = quantity;
	}
	
	@Override
	public void recordHpRecovery(int quantity, long currentHp) {
		this.combat.addOperationResult(new KHPRecoveryResult(combatMemberId, quantity, currentHp));
	}
	
	@Override
	public void recordEnergyRecovery(int qualtity, int currentEnergy, int currentEnergyBean) {
		this.combat.addOperationResult(new KEnergyRecoveryResult(combatMemberId, qualtity, currentEnergy, currentEnergyBean));
//		LOGGER.info("恢复怒气记录，shadowId={}，恢复量={}，当前量={}，当前怒气豆数量={}", combatMemberId, qualtity, currentEnergy, currentEnergyBean);
	}
	
	@Override
	public void recordLastTarget(ICombatMember pMember) {
		lastTarget = pMember;
	}
	
	@Override
	public void recordLastAttackType(boolean isSkillAttack) {
		this.lastAttackIsSkill = isSkillAttack;
	}
	
	@Override
	public final long getLastDm() {
		return lastDm;
	}
	
	@Override
	public ICombatMember getLastTarget() {
		return lastTarget;
	}
	
	@Override
	public boolean lastAttackTypeIsSkill() {
		return lastAttackIsSkill;
	}
	
	@Override
	public void recordLastAttacker(ICombatMember pMember) {
		this.lastAttacker = pMember;
	}
	
	@Override
	public ICombatMember getLastAttacker() {
		return this.lastAttacker;
	}
	
	@Override
	public void recordBeHit(long happenTime) {
		
	}
	
	@Override
	public int getBeHitCount() {
		return 0;
	}
	
	@Override
	public void recordAttack(long happenTime) {
		
	}
	
	@Override
	public void recordUnderAttack(short pAttackerId, int pct, int add, boolean hit, boolean crit, int dm, boolean isSkillAtk) {
		IRecordOfAttack record = new KRecordOfAttackImpl(pAttackerId, pct, add, hit, crit, dm, isSkillAtk);
		List<IRecordOfAttack> list = this._attackRecords.get(pAttackerId);
		if (list == null) {
			list = new ArrayList<ICombatRecorder.IRecordOfAttack>();
			this._attackRecords.put(pAttackerId, list);
		}
		list.add(record);
	}
	
	@Override
	public void recordBeNormalAttackTime(short pAttackerId, long time) {
		List<Long> list = _normalAtkTimeRecord.get(pAttackerId);
		if (list == null) {
			list = new ArrayList<Long>();
			_normalAtkTimeRecord.put(pAttackerId, list);
		}
		list.add(time);
	}
	
	@Override
	public boolean isNormalAttackDuplicate(short pAttackerId, long time) {
		List<Long> list = _normalAtkTimeRecord.get(pAttackerId);
		if (list != null) {
			return list.contains(time);
		}
		return false;
	}
	
	@Override
	public int getMaxComboAttackCount() {
		return 0;
	}
	
	@Override
	public int getCurrentComboAttackCount() {
		return 0;
	}
	
	@Override
	public void recordTimeOfDmByDead(long time) {
		this.timeOfDmByDead = time;
	}
	
	@Override
	public long getTimeOfDmByDead() {
		return timeOfDmByDead;
	}
	
	@Override
	public Map<Short, List<IRecordOfAttack>> getAttackRecord() {
		return _attackRecords;
	}
	
	@Override
	public long getTotalDmIncludingAccompany() {
		return 0;
	}
	
	@Override
	public void recordAccompanyDm(long quantity) {
		
	}
	
	public List<Long> getNormalAtkTimeRecord(short attackerId) {
		return this._normalAtkTimeRecord.get(attackerId);
	}
	
	@Override
	public void release() {
		this.combat = null;
		this.combatMemberId = 0;
		this.lastAttacker = null;
		this.lastAttacker = null;
		this.timeOfDmByDead = 0;
		this._attackRecords.clear();
		if(_normalAtkTimeRecord.size() > 0) {
			for(Iterator<List<Long>> itr = _normalAtkTimeRecord.values().iterator(); itr.hasNext();) {
				itr.next().clear();
			}
			_normalAtkTimeRecord.clear();
		}
	}
	
	static class KHPRecoveryResult implements IOperationResult {

		private short _shadowId;
		private int _quantity;
		private long _currentHp;
		
		KHPRecoveryResult(short pShadowId, int pQuantity, long pCurrentHp) {
			this._shadowId = pShadowId;
			this._quantity = pQuantity;
			this._currentHp = pCurrentHp;
		}
		
		@Override
		public byte getOperationType() {
			return OPERATION_TYPE_SYNC_HP;
		}

		@Override
		public void fillMsg(KGameMessage msg) {
			msg.writeShort(_shadowId);
			msg.writeInt(_quantity);
//			msg.writeInt(_currentHp);
			msg.writeLong(_currentHp);
		}
	}
	
	static class KEnergyRecoveryResult implements IOperationResult {

		private short _shadowId;
		private int _quantity;
		private int _currentEnergy;
		private int _currentEnergyBean;
		
		
		/**
		 * @param pShadowId
		 * @param pQuantity
		 * @param pCurrentEnergy
		 * @param pCurrentEnergyBean
		 */
		public KEnergyRecoveryResult(short pShadowId, int pQuantity, int pCurrentEnergy, int pCurrentEnergyBean) {
			this._shadowId = pShadowId;
			this._quantity = pQuantity;
			this._currentEnergy = pCurrentEnergy;
			this._currentEnergyBean = pCurrentEnergyBean;
		}

		@Override
		public byte getOperationType() {
			return OPERATION_TYPE_ENERGY_RECOVERY;
		}

		@Override
		public void fillMsg(KGameMessage msg) {
			msg.writeShort(_shadowId);
			msg.writeByte(_quantity);
			msg.writeShort(_currentEnergy);
			msg.writeByte(_currentEnergyBean);
//			LOGGER.info("发送怒气记录，shadowId={}，恢复量={}，当前量={}，当前怒气豆数量={}", _shadowId, _quantity, _currentEnergy, _currentEnergyBean);
		}
		
	}
	
	static class KRecordOfAttackImpl implements IRecordOfAttack {

		private short _attackerId;
		private int _pct;
		private int _add;
		private boolean _hit;
		private boolean _crit;
		private int _dm;
		private boolean _skillAtk;

		/**
		 * 
		 */
		public KRecordOfAttackImpl(short pAttackerId, int pct, int add, boolean pHit, boolean pCrit, int pDm, boolean pIsSkillAtk) {
			this._attackerId = pAttackerId;
			this._pct = pct;
			this._add = add;
			this._hit = pHit;
			this._crit = pCrit;
			this._dm = pDm;
			this._skillAtk = pIsSkillAtk;
		}
		
		@Override
		public int getPct() {
			return _pct;
		}
		
		@Override
		public int getAdd() {
			return _add;
		}

		@Override
		public short getAttackerId() {
			return _attackerId;
		}

		@Override
		public boolean isHit() {
			return _hit;
		}

		@Override
		public boolean isCrit() {
			return _crit;
		}
		
		@Override
		public boolean isSkillAtk() {
			return _skillAtk;
		}

		@Override
		public int getDm() {
			return _dm;
		}

	}
}
