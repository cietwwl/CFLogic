package com.kola.kmp.logic.combat.state;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.api.ICombatSkillActor;

/**
 * 
 * 添加一个特殊效果的状态
 * 
 * @author PERRY CHAN
 */
public class KAddSpecialEffectStateParser implements ICombatStateParser {

	
	@Override
	public ICombatState newInstance(ICombatMember operator, ICombatStateTemplate template) {
		KAddSpecialEffectState instance = new KAddSpecialEffectState();
		instance.init(template);
		return instance;
	}
	
	private static class KAddSpecialEffectState extends KCombatStateBaseImpl {

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
