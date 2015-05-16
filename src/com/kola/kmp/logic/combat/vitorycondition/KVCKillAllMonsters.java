package com.kola.kmp.logic.combat.vitorycondition;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatForce;

/**
 * 
 * @author PERRY CHAN
 */
public class KVCKillAllMonsters extends KVitoryConditionBase implements IVitoryCondition {
	
	@Override
	public boolean validateFinish(ICombat combat) {
		ICombatForce roleForce = combat.getForce(ICombatForce.FORCE_TYPE_ROLE_SIDE);
		boolean roleSideAllDie = this.isRoleSideAllDie(roleForce);
		if (roleSideAllDie) {
			return true;
		} else {
			boolean allMonsterDie = this.isMonsterSideAllDie(combat);
			if (allMonsterDie) {
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public boolean isRoleWin(ICombat combat) {
		boolean allMonsterDie = this.isMonsterSideAllDie(combat);
		if (allMonsterDie) {
			boolean isRoleSideAllDie = this.isRoleSideAllDie(combat.getForce(ICombatForce.FORCE_TYPE_ROLE_SIDE));
			return !isRoleSideAllDie;
		} else {
			return false;
		}
	}

}
