package com.kola.kmp.logic.combat.state;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.api.ICombatSkillActor;

/**
 * 
 * 霸体效果（免疫击倒、击飞、击退、眩晕、定身）
 * 
 * @author PERRY CHAN
 */
public class KImmunityControlStateParser implements ICombatStateParser {

	@Override
	public ICombatState newInstance(ICombatMember operator, ICombatStateTemplate template) {
		KImmunityControlState instance = new KImmunityControlState();
		instance.init(template);
		return instance;
	}

	private static class KImmunityControlState extends KCombatStateBaseImpl {
		
		@Override
		protected void parsePara(int[] paras) {
			super.setLastTimeMillis(paras[INDEX_LAST_TIME_MILLIS]);
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
