package com.kola.kmp.logic.combat.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.resulthandler.ICombatGameLevelInfo;
import com.kola.kmp.logic.combat.resulthandler.ICombatResult;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatResultImpl implements ICombatResult {

	private List<Long> _allRoleIds = new ArrayList<Long>();
	private List<Long> _allRoleIdsReadOnly = Collections.unmodifiableList(_allRoleIds);
	private Map<Long, KCombatRoleResultImpl> _roleResultMap = new HashMap<Long, KCombatRoleResultImpl>();
	private Map<Long, KCombatRewardImpl> _rewardMap = new HashMap<Long, KCombatRewardImpl>();
	private Map<Integer, Long> _monsterHpInfo = new HashMap<Integer, Long>();
	private Map<Integer, Long> _monsterHpInfoRO = Collections.unmodifiableMap(_monsterHpInfo);
	private List<Integer> _killedInstanceIds = new ArrayList<Integer>();
	private boolean _roleWin;
	private long _totalTime;
	private ICombatGameLevelInfo _gameLevelInfo;
//	private int _lastBattleFieldId;
//	private KGameBattlefieldTypeEnum _lastBattleFieldType;
//	private Object _attachment;
	private Map<String, Object> _attribute = new HashMap<String, Object>();
	
	void reset() {
		this._allRoleIds.clear();
		this._roleResultMap.clear();
		this._rewardMap.clear();
		this._monsterHpInfo.clear();
		this._roleWin = false;
		this._totalTime = 0;
		this._attribute.clear();
		this._killedInstanceIds.clear();
		this._gameLevelInfo = null;
	}
	
	@Override
	public List<Long> getAllRoleIds() {
		return _allRoleIdsReadOnly;
	}
	
	void addRoleId(long pRoleId) {
		this._allRoleIds.add(pRoleId);
	}

	@Override
	public KCombatRoleResultImpl getRoleResult(long roleId) {
		return _roleResultMap.get(roleId);
	}
	
	void addCombatRoleResult(long pRoleId, KCombatRoleResultImpl pRoleResult) {
		_roleResultMap.put(pRoleId, pRoleResult);
	}

	@Override
	public boolean isRoleWin() {
		return _roleWin;
	}
	
	void setIsRoleWin(boolean win) {
		this._roleWin = win;
	}

	@Override
	public KCombatRewardImpl getCombatReward(long pRoleId) {
		return this._rewardMap.get(pRoleId);
	}
	
	void addCombatReward(long pRoleId, KCombatRewardImpl pReward) {
		this._rewardMap.put(pRoleId, pReward);
	}

	@Override
	public long getTotalCombatTime() {
		return _totalTime;
	}
	
	void setTotalCombatTime(long pTime) {
		this._totalTime = pTime;
	}

//	@Override
//	public int getLastBattleFieldId() {
//		return _lastBattleFieldId;
//	}
	
//	void setLastBattleFieldId(int pLastBattleFieldId) {
//		this._lastBattleFieldId = pLastBattleFieldId;
//	}

//	@Override
//	public KGameBattlefieldTypeEnum getLastBattleFieldType() {
//		return _lastBattleFieldType;
//	}
	
//	void setLastBattleFieldType(KGameBattlefieldTypeEnum pType) {
//		this._lastBattleFieldType = pType;
//	}
	
	@Override
	public ICombatGameLevelInfo getGameLevelInfo() {
		return _gameLevelInfo;
	}
	
	void setGameLevelInfo(ICombatGameLevelInfo pInfo) {
		_gameLevelInfo = pInfo;
	}
	
	@Override
	public Object getAttachment() {
		return _attribute.get(KEY_ATTACHMENT);
	}
	
	void setAttachment(Object attachment) {
		this._attribute.put(KEY_ATTACHMENT, attachment);
	}
	
	@Override
	public void putAttributeToResult(String key, Object obj) {
		this._attribute.put(key, obj);
	}
	
	@Override
	public Object getAttributeFromResult(String key) {
		return _attribute.get(key);
	}
	
	@Override
	public void recordMonsterHpInfo(int instanceId, ICombatMember monster){
		this._monsterHpInfo.put(instanceId, monster.getCurrentHp());
	}
	
	@Override
	public Map<Integer, Long> getMonsterHpInfo() {
		return _monsterHpInfoRO;
	}
	
	@Override
	public void recordKillInstanceId(int instanceId) {
		_killedInstanceIds.add(instanceId);
	}
	
	@Override
	public List<Integer> getKillInstanceIds() {
		return _killedInstanceIds;
	}

}
