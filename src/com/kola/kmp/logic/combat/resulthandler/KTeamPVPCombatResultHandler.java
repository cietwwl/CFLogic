package com.kola.kmp.logic.combat.resulthandler;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPManager;

/**
 * 
 * @author PERRY CHAN
 */
public class KTeamPVPCombatResultHandler implements ICombatResultHandler {

	@Override
	public void processCombatResultToRole(long roleId, ICombat combat, ICombatResult combatResult) {
		KTeamPVPManager.processCombatFinish(roleId, combatResult);
	}

	@Override
	public void processCombatFinish(ICombat combat, ICombatResult combatResult) {
		
	}

	@Override
	public void processRoleExitCombatFinish(long roleId, ICombatResult result) {
		
	}

}
