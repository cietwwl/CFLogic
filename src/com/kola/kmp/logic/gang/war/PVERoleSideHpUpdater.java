package com.kola.kmp.logic.gang.war;

import com.kola.kmp.logic.combat.KCombatType;

public class PVERoleSideHpUpdater extends PVPRoleSideHpUpdater {

	@Override
	public KCombatType getCombatTypeResponse() {
		return KCombatType.GANG_WAR_PVE;
	}
}
