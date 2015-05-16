package com.kola.kmp.logic.combat.resulthandler;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 
 * @author PERRY CHAN
 */
public class KWorldBossCombatResultHandler implements ICombatResultHandler {

	@Override
	public void processCombatResultToRole(long roleId, ICombat combat, ICombatResult combatResult) {
		ICombatRoleResult roleResult = combatResult.getRoleResult(roleId);
		if (roleResult.isEscape()) {
			KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (role != null) {
				KDialogService.sendNullDialog(role);
//				KSupportFactory.getMapSupport().processRoleFinishBattleReturnToMap(role);
				KSupportFactory.getActivityModuleSupport().processWorldBossEscape(role);
			}
		} else {
			KSupportFactory.getActivityModuleSupport().processWorldBossCombatFinish(roleId, roleResult, combatResult);
		}
	}

	@Override
	public void processCombatFinish(ICombat combat, ICombatResult combatResult) {
		
	}
	
	@Override
	public void processRoleExitCombatFinish(long roleId, ICombatResult result) {
		KSupportFactory.getActivityModuleSupport().processRoleExitCombatFinish(roleId);
	}

}
