package com.kola.kmp.logic.combat.cmd;

import java.util.List;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.operation.IOperation;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICombatCommandCreator {

	/**
	 * 
	 * @param combat
	 * @return
	 */
	ICombatCommand createStartCombatCommand(ICombat combat);
	
	/**
	 * 
	 * @param combat
	 * @return
	 */
	ICombatCommand createCombatOperationCommand(ICombat combat, List<IOperation> opList);
	
	/**
	 * 
	 * @param combat
	 * @return
	 */
	ICombatCommand createKillAllMemberCommand(ICombat combat, boolean killAllMonsters);
	
	/**
	 * 
	 * @param combat
	 * @return
	 */
	ICombatCommand createForceFinishCommand(ICombat combat, boolean isRoleWin);
}
