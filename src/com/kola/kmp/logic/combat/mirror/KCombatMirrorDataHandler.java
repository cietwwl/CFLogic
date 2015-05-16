package com.kola.kmp.logic.combat.mirror;

import com.kola.kmp.logic.combat.ICombatMirrorDataGroup;
import com.kola.kmp.logic.combat.ICombatMirrorDataHandler;
import com.kola.kmp.logic.combat.api.ICombatMount;
import com.kola.kmp.logic.combat.api.ICombatPet;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatMirrorDataHandler implements ICombatMirrorDataHandler {

	@Override
	public ICombatMirrorDataGroup getMirrorDataGroup(KRole role) {
		ICombatPet fightingPet = KSupportFactory.getPetModuleSupport().getFightingPetForBattle(role.getId());
		ICombatMount mount = KSupportFactory.getMountModuleSupport().getMountCanWarOfRole(role.getId());
		return new KCombatMirrorDataGroupImpl(role, fightingPet, mount);
	}
	
}
