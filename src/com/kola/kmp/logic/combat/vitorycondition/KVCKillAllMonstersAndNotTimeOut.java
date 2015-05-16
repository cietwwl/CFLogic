package com.kola.kmp.logic.combat.vitorycondition;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatForce;

/**
 * 
 * @author PERRY CHAN
 */
public class KVCKillAllMonstersAndNotTimeOut extends KVitoryConditionBase implements IVitoryCondition {
	
	@Override
	public boolean validateFinish(ICombat combat) {
		if (combat.getTimeOutMillis() < combat.getCurrentUseTime()) {
			return true;
		} else if (this.isRoleSideAllDie(combat.getForce(ICombatForce.FORCE_TYPE_ROLE_SIDE))) {
			return true;
		}
		return isMonsterSideAllDie(combat);
	}

	@Override
	public boolean isRoleWin(ICombat combat) {
		boolean isTimeOut = combat.getTimeOutMillis() < combat.getCurrentUseTime();
		if (isTimeOut) {
			return false;
		} else {
			boolean allMonsterDie = this.isMonsterSideAllDie(combat);
			if (allMonsterDie) {
				boolean isRoleSideAllDie = this.isRoleSideAllDie(combat.getForce(ICombatForce.FORCE_TYPE_ROLE_SIDE));
				return !isRoleSideAllDie;
			} else {
				return false;
			}
		}
	}

}
