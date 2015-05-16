package com.kola.kmp.logic.combat.resulthandler;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.gang.war.GangWarLogic;

/**
 * 
 * @author PERRY CHAN
 */
public class KGangWarPVECombatResultHandler implements ICombatResultHandler {

	@Override
	public void processCombatResultToRole(long roleId, ICombat combat, ICombatResult combatResult) {
		ICombatRoleResult roleResult = combatResult.getRoleResult(roleId);
		roleResult.setAttachment(combatResult.getAttachment());
		GangWarLogic.notifyPVEBattleFinished(roleId, roleResult, combatResult);
	}

	@Override
	public void processCombatFinish(ICombat combat, ICombatResult combatResult) {
		
	}

	@Override
	public void processRoleExitCombatFinish(long roleId, ICombatResult result) {
		
	}

}
