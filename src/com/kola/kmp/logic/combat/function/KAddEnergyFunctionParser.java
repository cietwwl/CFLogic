package com.kola.kmp.logic.combat.function;

import java.util.List;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 
 * @author PERRY CHAN
 */
public class KAddEnergyFunctionParser implements IFunctionParser {

	@Override
	public IFunctionExecution parse(List<Integer> args) {
		KAddEnergyFunction instance = new KAddEnergyFunction();
		return instance;
	}

	private static class KAddEnergyFunction implements IFunctionExecution {
		@Override
		public void execute(ICombat combat, ICombatMember operator, long happenTime) {
			ICombatMember attacker = operator.getCombatRecorder().getLastAttacker();
			if (attacker != null && attacker.isAlive()) {
				if (operator.isNarrowMonster()) {
					switch (attacker.getMemberType()) {
					case ICombatMember.MEMBER_TYPE_ROLE:
					case ICombatMember.MEMBER_TYPE_ROLE_MONSTER:
					case ICombatMember.MEMBER_TYPE_TEAM_MATE_ROLE:
						attacker.increaseEnergy(KSupportFactory.getNpcModuleSupport().getKillEnergy(operator.getSrcObjTemplateId()));
						break;
					case ICombatMember.MEMBER_TYPE_VEHICLE:
					{
						ICombatMember master = combat.getMasterOfMount(attacker);
						master.increaseEnergy(KSupportFactory.getNpcModuleSupport().getKillEnergy(operator.getSrcObjTemplateId()));
						break;
					}
					case ICombatMember.MEMBER_TYPE_PET:
					{
						ICombatMember master = combat.getCombatMember(combat.getMasterShadowIdOfPet(attacker.getShadowId()));
						if (master != null) {
							master.increaseEnergy(KSupportFactory.getNpcModuleSupport().getKillEnergy(operator.getSrcObjTemplateId()));
						}
						break;
					}
					case ICombatMember.MEMBER_TYPE_MINION:
					{
						ICombatMember master = combat.getMasterOfMinion(attacker);
						switch (master.getMemberType()) {
						case ICombatMember.MEMBER_TYPE_ROLE:
						case ICombatMember.MEMBER_TYPE_ROLE_MONSTER:
						case ICombatMember.MEMBER_TYPE_TEAM_MATE_ROLE:
							attacker.increaseEnergy(KSupportFactory.getNpcModuleSupport().getKillEnergy(operator.getSrcObjTemplateId()));
						}
						break;
					}
					
					}
				}
			}
		}
	}

}
