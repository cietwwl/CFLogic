package com.kola.kmp.logic.combat.impl;

import java.util.HashMap;
import java.util.Map;

import com.kola.kmp.logic.combat.KCombatType;
import com.kola.kmp.logic.combat.resulthandler.ICombatRoleResult;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatRoleResultImpl implements ICombatRoleResult {

	private long _roleId;
	private boolean _alive;
	private long _currentHp;
	private long _petCurrentHp = -1;
	private boolean _escape;
	private long _totalDamage;
	private int _maxComboCount;
	private int _maxBeHitCount;
	private Map<Integer, Short> _killMonsterMap = new HashMap<Integer, Short>();
	private boolean _win;
	private long _totalTime;
	private KCombatType _type;
	private Object _attachment;
	
	/**
	 * 
	 */
	KCombatRoleResultImpl(KCombatType combatType) {
		this._type = combatType;
	}
	
	@Override
	public long getRoleId() {
		return _roleId;
	}
	
	void setRoleId(long pRoleId) {
		this._roleId = pRoleId;
	}

	@Override
	public boolean isAlive() {
		return _alive;
	}
	
	void setAlive(boolean pAlive) {
		this._alive = pAlive;
	}
	
	@Override
	public long getRoleCurrentHp() {
		return _currentHp;
	}
	
	void setCurrentHp(long pCurrentHp) {
		this._currentHp = pCurrentHp;
	}
	
	@Override
	public long getPetCurrentHp() {
		return _petCurrentHp;
	}
	
	void setPetCurrentHp(long pCurrentHp) {
		_petCurrentHp = pCurrentHp;
	}

	@Override
	public boolean isEscape() {
		return _escape;
	}
	
	void setEscape(boolean pEscape) {
		this._escape = pEscape;
	}

	@Override
	public long getTotalDamage() {
		return _totalDamage;
	}
	
	void setTotalDamage(long pDamage) {
		this._totalDamage = pDamage;
	}

	@Override
	public int getMaxComboCount() {
		return _maxComboCount;
	}
	
	void setMaxComboCount(int pMaxComboCount) {
		this._maxComboCount = pMaxComboCount;
	}

	@Override
	public int getMaxBeHitCount() {
		return _maxBeHitCount;
	}
	
	void setMaxBeHitCount(int pMaxBeHitCount) {
		this._maxBeHitCount = pMaxBeHitCount;
	}

	@Override
	public Map<Integer, Short> getKillMonsterCount() {
		return _killMonsterMap;
	}
	
	void addKillMonster(int templateId, int addCount) {
		Short currentCount = this._killMonsterMap.get(templateId);
		if (currentCount != null) {
			addCount += currentCount;
		}
		this._killMonsterMap.put(templateId, (short) addCount);
	}
	
	void addKillMonsterAll(Map<Integer, Short> map){
		this._killMonsterMap.putAll(map);
	}

	@Override
	public long getCombatTime() {
		return _totalTime;
	}
	
	void setCombatTime(long time) {
		this._totalTime = time;
	}

	@Override
	public boolean isWin() {
		return _win;
	}
	
	void setIsWin(boolean win) {
		_win = win;
	}

	@Override
	public Object getAttachment() {
		return _attachment;
	}
	
	@Override
	public void setAttachment(Object pAttachment) {
		_attachment = pAttachment;
	}

	@Override
	public KCombatType getCombatType() {
		return _type;
	}
}
