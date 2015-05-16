package com.kola.kmp.logic.combat.vitorycondition;

import com.kola.kmp.logic.combat.ICombat;

/**
 * 
 * @author PERRY CHAN
 */
public interface IVitoryCondition {

	/**
	 * 
	 * @param combat
	 * @return
	 */
	public boolean validateFinish(ICombat combat);
	
	/**
	 * 
	 * @param combat
	 * @return
	 */
	public boolean isRoleWin(ICombat combat);
}
