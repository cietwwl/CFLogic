package com.kola.kmp.logic.combat.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.kola.kmp.logic.combat.ICombatMember;

/**
 * 
 * @author PERRY CHAN
 */
public class KRoleCombatRecorderImpl extends KCombatRecorderBaseImpl {

	private AtomicInteger _beHitCount = new AtomicInteger();
	private AtomicInteger _combatAttackCount = new AtomicInteger();
	private int _maxComboAttackCount = 0;
	private long _lastAttackTime;
	private AtomicLong _totalDm = new AtomicLong();
	private AtomicLong _totalDmIncludeAccompany = new AtomicLong();
	private Map<Integer, Short> _killMonsterMap = new HashMap<Integer, Short>();
	
	@Override
	public void recordDm(long quantity) {
		super.recordDm(quantity);
		_totalDm.addAndGet(quantity);
		_totalDmIncludeAccompany.addAndGet(quantity);
	}

	@Override
	public void recordKillMember(ICombatMember member) {
		Short value = _killMonsterMap.get(member.getSrcObjTemplateId());
		if(value == null) {
			value = 1;
		} else {
			value++;
		}
		_killMonsterMap.put(member.getSrcObjTemplateId(), value);
	}
	
	@Override
	public long getTotalDm() {
		return _totalDm.longValue();
	}
	
	@Override
	public void recordAccompanyDm(long quantity) {
//		super.recordAccompanyDm(quantity);
		_totalDmIncludeAccompany.addAndGet(quantity);
	}
	
	@Override
	public long getTotalDmIncludingAccompany() {
		return _totalDmIncludeAccompany.get();
	}
	
	@Override
	public Map<Integer, Short> getKillMemberMap() {
		return _killMonsterMap;
	}
	
	@Override
	public void recordBeHit(long happenTime) {
		_beHitCount.incrementAndGet();
	}
	
	@Override
	public int getBeHitCount() {
		return _beHitCount.intValue();
	}
	
	@Override
	public void recordAttack(long happenTime) {
		if(_lastAttackTime == 0) {
			_combatAttackCount.incrementAndGet();
		} else {
			if (KCombatConfig.getCtnAtkMillis() > happenTime - _lastAttackTime) {
				_combatAttackCount.incrementAndGet();
			} else {
				_combatAttackCount.set(1);
			}
		}
		if(_maxComboAttackCount < _combatAttackCount.intValue()) {
			_maxComboAttackCount = _combatAttackCount.intValue();
//			LOGGER.info("[{}]的最高连击数发生改变！改变后最高连击数：{}", combatMemberId, _maxComboAttackCount);
		}
		_lastAttackTime = happenTime;
	}
	
	@Override
	public int getMaxComboAttackCount() {
		return _maxComboAttackCount;
	}
	
	@Override
	public int getCurrentComboAttackCount() {
		return _combatAttackCount.get();
	}
	
	@Override
	public void release() {
		super.release();
		this._beHitCount.set(0);
		this._combatAttackCount.set(0);
		this._maxComboAttackCount = 0;
		this._lastAttackTime = 0;
		this._totalDm.set(0);
		this._killMonsterMap.clear();
		this._totalDmIncludeAccompany.set(0);
	}

}
