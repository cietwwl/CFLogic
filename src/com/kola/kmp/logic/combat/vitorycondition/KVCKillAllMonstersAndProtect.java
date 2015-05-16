package com.kola.kmp.logic.combat.vitorycondition;

import java.util.List;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatForce;
import com.kola.kmp.logic.combat.ICombatMember;

/**
 * 
 * @author PERRY CHAN
 */
public class KVCKillAllMonstersAndProtect extends KVitoryConditionBase implements IVitoryCondition {

	private boolean isAllDead(List<ICombatMember> list) {
		for(int i = 0; i < list.size(); i++) {
			if(list.get(i).isAlive()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean validateFinish(ICombat combat) {
		boolean isProtectionAllDead = this.isAllDead(combat.getCombatMemberByType(ICombatMember.MEMBER_TYPE_ASSISTANT));
		if (!isProtectionAllDead) {
			isProtectionAllDead = this.isRoleSideAllDie(combat.getForce(ICombatForce.FORCE_TYPE_ROLE_SIDE));
			if (!isProtectionAllDead) {
				isProtectionAllDead = this.isMonsterSideAllDie(combat);
			}
		}
		return isProtectionAllDead;
	}

	@Override
	public boolean isRoleWin(ICombat combat) {
		if (this.isAllDead(combat.getCombatMemberByType(ICombatMember.MEMBER_TYPE_ASSISTANT))) {
			return false;
		}
		boolean allMonsterDie = this.isMonsterSideAllDie(combat);
		if (allMonsterDie) {
			boolean isRoleSideAllDie = this.isRoleSideAllDie(combat.getForce(ICombatForce.FORCE_TYPE_ROLE_SIDE));
			return !isRoleSideAllDie;
		} else {
			return false;
		}
	}

}
