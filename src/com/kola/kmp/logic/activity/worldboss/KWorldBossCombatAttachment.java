package com.kola.kmp.logic.activity.worldboss;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.kola.kmp.logic.activity.worldboss.KWorldBossActivityField.KWorldBossMonsterInfo;

/**
 * 
 * @author PERRY CHAN
 */
public class KWorldBossCombatAttachment {

	private int _genRuleInstanceId; // 生成规则id
	private final Map<Integer, Long> _currentHpInfo = new HashMap<Integer, Long>();
	private final Map<Integer, Long> _currentHpInfoRO = Collections.unmodifiableMap(_currentHpInfo); // 当前的血量状态

	void fillData(int genRuleInstanceId, Map<Integer, KWorldBossMonsterInfo> monsterInfo) {
		this._genRuleInstanceId = genRuleInstanceId;
		if (_currentHpInfo.size() > 0) {
			this._currentHpInfo.clear();
		}
		synchronized (monsterInfo) {
			KWorldBossMonsterInfo info;
			for (Iterator<KWorldBossMonsterInfo> itr = monsterInfo.values().iterator(); itr.hasNext();) {
				info = itr.next();
				_currentHpInfo.put(info.getInstanceId(), info.getCurrentHp());
			}
		}
	}
	
	public int getGenRuleInstanceId() {
		return _genRuleInstanceId;
	}
	
	public Map<Integer, Long> getCurrentHpInfo() {
		return _currentHpInfoRO;
	}
}
