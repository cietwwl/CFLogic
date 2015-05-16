package com.kola.kmp.logic.combat.state;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.api.ICombatSkillActor;

/**
 * 
 * @author PERRY CHAN
 */
public class KFaintStateParser implements ICombatStateParser {

	@Override
	public ICombatState newInstance(ICombatMember operator, ICombatStateTemplate template) {
		KFaintState instance = new KFaintState();
		instance.init(template);
		return instance;
	}
	
	private static class KFaintState extends KCombatStateBaseImpl {
		
		@Override
		protected void parsePara(int[] paras) {
			super.setLastTimeMillis(paras[0]);
		}

		@Override
		public void onAdded(ICombat combat, ICombatSkillActor actor, long happenTime) {

		}

		@Override
		public void durationEffect(ICombat combat, ICombatSkillActor actor, long happenTime) {

		}

		@Override
		public void onRemoved(ICombat combat, ICombatSkillActor actor) {

		}
	}

}
