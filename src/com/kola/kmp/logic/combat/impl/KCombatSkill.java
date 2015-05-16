package com.kola.kmp.logic.combat.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kola.kmp.logic.combat.api.ICombatSkillData;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatSkill {

	private ICombatSkillData _skillData;
	private long _cooldownEndTime; // cd完成时间（毫秒）
	private String _currentUseCode; // 当前的使用代号
	private List<Long> _settleTimeRecord = new ArrayList<Long>(); // 当前使用代号的结算周期
	private Map<Short, Integer> _settleRecord = new HashMap<Short, Integer>();
	private long _currentFirstSettleTime;
	
	public KCombatSkill(ICombatSkillData pSkillData) {
		this._skillData = pSkillData;
	}
	
	void setCoolDownEndTime(long pEndTime) {
		this._cooldownEndTime = pEndTime;
	}
	
	void reduceCoolDownTime(int newCdMillis) {
//		if (_settleTimeRecord.size() > 0) {
//			long firstTime = _settleTimeRecord.get(0);
//			int lastCd = (int) (_cooldownEndTime - firstTime);
//			if (lastCd > 0) {
//				int sub = lastCd - newCdMillis;
//				_cooldownEndTime -= sub;
//			}
//		}
		if (_currentFirstSettleTime > 0) {
			int lastCd = (int) (_cooldownEndTime - _currentFirstSettleTime);
			if (lastCd > 0) {
				int sub = lastCd - newCdMillis;
				_cooldownEndTime -= sub;
			}
		}
	}
	
	long getCoolDownEndTime() {
		return this._cooldownEndTime;
	}
	
	boolean isCoolDownFinished(long compareTime) {
//		ICombat.LOGGER.info("技能id：{}，比较cd时间：{},{}", _skillData.getSkillTemplateId(), _cooldownEndTime, compareTime);
		return this._cooldownEndTime < compareTime;
	}
	
	void recordSkillUsed(String useCode, long happenTime) {
		_currentUseCode = useCode;
		_currentFirstSettleTime = happenTime;
	}
	
	void recordSkillUsed(String useCode, short targetId, long happenTime) {
//		if (!useCode.equals(_currentUseCode)) {
//			_currentUseCode = useCode;
//			_settleTimeRecord.clear();
//		}
//		_settleTimeRecord.add(happenTime);
		if (!useCode.equals(_currentUseCode)) {
			_currentUseCode = useCode;
			_currentFirstSettleTime = happenTime;
			_settleRecord.clear();
			_settleTimeRecord.clear();
		}
//		List<Short> list = _settleRecord.get(happenTime);
//		if (list == null) {
//			list = new ArrayList<Short>();
//			_settleRecord.put(happenTime, list);
//		}
//		list.add(targetId);
		if (!_settleTimeRecord.contains(happenTime)) {
			_settleTimeRecord.add(happenTime);
		}
		Integer count = _settleRecord.get(targetId);
		if (count == null) {
			count = 1;
		} else {
			count++;
		}
		_settleRecord.put(targetId, count);
	}
	
	boolean isTimeInSettleRecord(String useCode, long time) {
		if (useCode == null || useCode.length() == 0) {
			return false;
		} else if (useCode.equals(_currentUseCode)) {
			return _settleRecord.containsKey(time);
		} else {
			return false;
		}
	}
	
	int getSettleTimes(String useCode) {
		if (useCode.equals(_currentUseCode)) {
//			return _settleTimeRecord.size();
			return _settleRecord.size();
		}
		return 0;
	}
	
	int getTargetSettleTimes(String useCode, short targetId, long happenTime) {
		if (useCode == null || useCode.length() == 0) {
			return Short.MAX_VALUE;
		}
		if (useCode.equals(_currentUseCode)) {
			if (this._settleRecord.isEmpty()) {
				return 0;
			} else {
				Integer count = _settleRecord.get(targetId);
				if (count == null) {
					return 0;
				}
				return count;
			}
		} else {
			return 0;
		}
	}
	
	boolean hasTargetBeenSettleBefore(String useCode, long happenTime, short targetId) {
////		if (useCode.equals(_currentUseCode)) {
////			return _settleTimeRecord.contains(happenTime);
////		} else {
////			return false;
////		}
//		if (useCode.equals(_currentUseCode)) {
//			List<Short> settleTargets = _settleRecord.get(happenTime);
//			if (settleTargets == null) {
//				return false;
//			} else {
//				return settleTargets.contains(targetId);
//			}
//		} else {
//			return false;
//		}
		return true;
	}
	
	boolean isTimeFirstSettleOfThisCode(String useCode, long happenTime) {
//		if (useCode != null && useCode.equals(_currentUseCode) && _settleTimeRecord.size() > 0) {
//			return _settleTimeRecord.indexOf(happenTime) == 0;
//		} else {
//			return false;
//		}
		if (useCode != null && useCode.equals(_currentUseCode) && _currentFirstSettleTime > 0){
			return _currentFirstSettleTime == happenTime;
		} else {
			return false;
		}
	}
	
	String getCurrentUseCode() {
		return this._currentUseCode;
	}
	
	boolean isNewSettle(String useCode) {
//		return this._currentUseCode.equals(useCode);
		if (useCode != null && useCode.length() > 0) {
			if (this._currentUseCode == null) {
				return true;
			} else {
				return !useCode.equals(_currentUseCode);
			}
		} else {
			return false;
		}
	}
	
	public ICombatSkillData getSkillData() {
		return this._skillData;
	}
}
