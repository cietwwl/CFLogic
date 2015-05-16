
package com.kola.kmp.logic.combat.vitorycondition;

import java.util.List;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatForce;
import com.kola.kmp.logic.combat.ICombatMember;

/**
 * 
 * @author PERRY CHAN
 */
public class KVitoryConditionBase {

	protected boolean isRoleSideAllDie(ICombatForce roleForce) {
		List<ICombatMember> members = roleForce.getAllMembers();
		ICombatMember currentMember;
		boolean roleSideAllDie = true;
		for (int i = 0; i < members.size(); i++) {
			currentMember = members.get(i);
			switch (currentMember.getMemberType()) {
			case ICombatMember.MEMBER_TYPE_ROLE:
			case ICombatMember.MEMBER_TYPE_TEAM_MATE_ROLE:
				if (currentMember.isAlive()) {
					return false;
				}
				break;
			}
		}
		return roleSideAllDie;
	}
	
	protected boolean isMonsterSideAllDie(ICombat combat) {
		List<ICombatForce> allEnermyForce = combat.getAllEnermyForces();
		List<ICombatMember> members;
		ICombatMember currentMember;
		for (int i = 0; i < allEnermyForce.size(); i++) {
			members = allEnermyForce.get(i).getAllMembers();
			for (int k = 0; k < members.size(); k++) {
				currentMember = members.get(k);
//				switch (currentMember.getMemberType()) {
//				case ICombatMember.MEMBER_TYPE_MONSTER:
//				case ICombatMember.MEMBER_TYPE_ROLE_MONSTER:
//				case ICombatMember.MEMBER_TYPE_BOSS_MONSTER:
//				case ICombatMember.MEMBER_TYPE_ELITIST_MONSTER:
//				case ICombatMember.MEMBER_TYPE_BARREL_MONSTER:
////				case ICombatMember.MEMBER_TYPE_MINION:
//					if (currentMember.isAlive()) {
//						return false;
//					}
//					break;
//				case ICombatMember.MEMBER_TYPE_MINION:
//					if (combat.getCombatType() != KCombatType.COMPETITION) {
//						// 竞技场不需要判断召唤物
//						if (currentMember.isAlive()) {
//							return false;
//						}
//					}
//					break;
//				}
				if (currentMember.isGeneralMonster()) {
					if (currentMember.getMemberType() == ICombatMember.MEMBER_TYPE_PET_MONSTER) {
						continue;
					}
					if (currentMember.isAlive()) {
						return false;
					}
				} else if (currentMember.getMemberType() == ICombatMember.MEMBER_TYPE_MINION) {
//					switch (combat.getCombatType()) {
//					case COMPETITION:
//					case TEAM_PVP:
//						// 竞技场不需要判断召唤物
//						continue;
//					default:
//						if (currentMember.isAlive()) {
//							return false;
//						}
//					}
					if(combat.getCombatType().isPVP) {
						// PVP类战斗不需要判断
						continue;
					} else {
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
