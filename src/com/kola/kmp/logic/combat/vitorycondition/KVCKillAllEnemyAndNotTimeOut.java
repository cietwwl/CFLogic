package com.kola.kmp.logic.combat.vitorycondition;

import java.util.List;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatForce;
import com.kola.kmp.logic.combat.ICombatMember;

public class KVCKillAllEnemyAndNotTimeOut extends KVitoryConditionBase implements IVitoryCondition {

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
	
	@Override
	protected boolean isMonsterSideAllDie(ICombat combat) {
		List<ICombatForce> allEnermyForce = combat.getAllEnermyForces();
		List<ICombatMember> members;
		ICombatMember currentMember;
		for (int i = 0; i < allEnermyForce.size(); i++) {
			members = allEnermyForce.get(i).getAllMembers();
			for (int k = 0; k < members.size(); k++) {
				currentMember = members.get(k);
				if (currentMember.isGeneralMonster()) {
					if (currentMember.getMemberType() == ICombatMember.MEMBER_TYPE_PET_MONSTER) {
						continue;
					}
					if (currentMember.isAlive()) {
						return false;
					}
				} else {
					switch (currentMember.getMemberType()) {
					case ICombatMember.MEMBER_TYPE_MINION:
					case ICombatMember.MEMBER_TYPE_BLOCK:
						if (currentMember.isAlive()) {
							return false;
						}
					}
				}
			}
		}
		ICombat.LOGGER.info("monster all dead...combat id ： {}", combat.getSerialId());
		return true;
	}

}
