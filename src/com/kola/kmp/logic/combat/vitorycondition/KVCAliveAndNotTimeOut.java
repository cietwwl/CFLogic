package com.kola.kmp.logic.combat.vitorycondition;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatForce;

/**
 * 
 * @author PERRY CHAN
 */
public class KVCAliveAndNotTimeOut extends KVitoryConditionBase implements IVitoryCondition {
	
	@Override
	public boolean validateFinish(ICombat combat) {
		if (combat.getTimeOutMillis() < combat.getCurrentUseTime()) {
			return true;
		} else if (this.isRoleSideAllDie(combat.getForce(ICombatForce.FORCE_TYPE_ROLE_SIDE))) {
			return true;
		}
		return this.isMonsterSideAllDie(combat);
	}

	@Override
	public boolean isRoleWin(ICombat combat) {
		return true;
	}
	
}
