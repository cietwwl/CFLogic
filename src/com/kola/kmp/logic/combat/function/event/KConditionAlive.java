package com.kola.kmp.logic.combat.function.event;

import com.kola.kmp.logic.combat.ICombatMember;

/**
 * 
 * @author PERRY CHAN
 */
public class KConditionAlive extends KConditionBaseImpl {

	
	@Override
	public void parseArgs(String arg) {
		
	}
	
	@Override
	protected boolean conditionPass(ICombatMember member) {
		return member.isAlive();
	}

}
