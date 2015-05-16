package com.kola.kmp.logic.combat.impl;

import java.util.Collections;
import java.util.Map;

import com.kola.kmp.logic.combat.ICombatMember;

/**
 * 
 * @author PERRY CHAN
 */
public class KEmptyCombatRecorderImpl extends KCombatRecorderBaseImpl{

	@Override
	public void recordKillMember(ICombatMember member) {
		// 不做任何记录
	}
	
	@Override
	public long getTotalDm() {
		return 0;
	}
	
	@Override
	public Map<Integer, Short> getKillMemberMap() {
		return Collections.emptyMap();
	}

}
