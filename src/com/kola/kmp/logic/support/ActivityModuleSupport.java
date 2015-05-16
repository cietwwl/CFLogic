package com.kola.kmp.logic.support;

import com.kola.kmp.logic.combat.ICombatCommonResult;
import com.kola.kmp.logic.combat.ICombatGlobalCommonResult;
import com.kola.kmp.logic.role.KRole;

public interface ActivityModuleSupport {

	/**
	 * 通知世界boss战斗结束
	 * 
	 * @param role
	 * @param roleResult
	 * @param globalResult
	 */
	public void processWorldBossCombatFinish(long roleId, ICombatCommonResult roleResult, ICombatGlobalCommonResult globalResult);

	/**
	 * 
	 * @param roleId
	 */
	public void processRoleExitCombatFinish(long roleId);
	
	/**
	 * 
	 * @param roleId
	 */
	public void processWorldBossEscape(KRole role);
}
