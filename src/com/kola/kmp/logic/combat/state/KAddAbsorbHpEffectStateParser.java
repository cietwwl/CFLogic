package com.kola.kmp.logic.combat.state;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.api.ICombatSkillActor;

/**
 * 
 * @author PERRY CHAN
 */
public class KAddAbsorbHpEffectStateParser implements ICombatStateParser {

	@Override
	public ICombatState newInstance(ICombatMember operator, ICombatStateTemplate template) {
		KAddAbsorbHpEffectState instance = new KAddAbsorbHpEffectState();
		instance.init(template);
		return instance;
	}
	
	private static class KAddAbsorbHpEffectState extends KCombatStateBaseImpl {
		@Override
		protected void parsePara(int[] paras) {
			super.setLastTimeMillis(paras[INDEX_LAST_TIME_MILLIS]);
		}

		@Override
		public void onAdded(ICombat combat, ICombatSkillActor actor, long happenTime) {
			actor.addTemporaryEffec(stateTemplateId);
		}

		@Override
		public void durationEffect(ICombat combat, ICombatSkillActor actor, long happenTime) {

		}

		@Override
		public void onRemoved(ICombat combat, ICombatSkillActor actor) {

		}
	}

}
