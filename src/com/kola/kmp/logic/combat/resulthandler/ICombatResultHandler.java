package com.kola.kmp.logic.combat.resulthandler;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.kola.kmp.logic.combat.ICombat;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICombatResultHandler {
	
	/**
	 * 
	 * <pre>
	 * 处理指定角色的战斗结果，每场战斗的每个角色都会通知一次
	 * </pre>
	 * 
	 * @param roleId
	 * @param combat
	 */
	public void processCombatResultToRole(long roleId, ICombat combat, ICombatResult combatResult);
	
	/**
	 * 
	 * <pre>
	 * 通知战斗结束（全局的通知），每场战斗的每个战斗记过处理器只通知一次
	 * <pre>
	 * 
	 * @param combat
	 */
	public void processCombatFinish(ICombat combat, ICombatResult combatResult);
	
	/**
	 * 
	 * @param roleId
	 */
	public void processRoleExitCombatFinish(long roleId, ICombatResult result);
}
