package com.kola.kmp.logic.combat.function.event;

import com.kola.kmp.logic.combat.ICombatMember;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICondition {

	/**
	 * 
	 */
	public static final int CONDITION_ALIVE = 1;
	/**
	 * 
	 */
	public static final int CONDITION_DEAD = 2;
	
	/**
	 * 
	 * @param arg
	 */
	public void parseArgs(String arg);
	
	/**
	 * 
	 * 是否符合条件要求
	 * 
	 * @param member
	 * @return
	 */
	public boolean isMatchCondition(ICombatMember member);
}
