package com.kola.kmp.logic.combat.resulthandler;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 
 * @author PERRY CHAN
 */
public class KCompetitionCombatResultHandler implements ICombatResultHandler {

	@Override
	public void processCombatResultToRole(long roleId, ICombat combat, ICombatResult combatResult) {
		ICombatRoleResult result = combatResult.getRoleResult(roleId);
		result.setAttachment(combatResult.getAttachment()); // 设置竞技场的相关数据
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		KSupportFactory.getCompetitionModuleSupport().notifyCompetitionFinish(role, result);
	}

	@Override
	public void processCombatFinish(ICombat combat, ICombatResult combatResult) {
		
	}
	
	@Override
	public void processRoleExitCombatFinish(long roleId, ICombatResult result) {
		
	}

}
