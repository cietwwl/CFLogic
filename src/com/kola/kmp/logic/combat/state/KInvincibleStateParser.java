package com.kola.kmp.logic.combat.state;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.api.ICombatSkillActor;

public class KInvincibleStateParser implements ICombatStateParser {

	@Override
	public ICombatState newInstance(ICombatMember operator, ICombatStateTemplate template) {
		KInvincibleState instance = new KInvincibleState();
		instance.init(template);
		return instance;
	}

	private static class KInvincibleState extends KCombatStateBaseImpl {
		
		@Override
		protected void parsePara(int[] paras) {
			super.setLastTimeMillis(paras[INDEX_LAST_TIME_MILLIS]);
		}

		@Override
		public void onAdded(ICombat combat, ICombatSkillActor actor, long happenTime) {
			ICombat.LOGGER.info("处理无敌，actorId:{}", actor.getShadowId());
			actor.handleInvincible(true);
		}

		@Override
		public void durationEffect(ICombat combat, ICombatSkillActor actor, long happenTime) {

		}

		@Override
		public void onRemoved(ICombat combat, ICombatSkillActor actor) {
			ICombat.LOGGER.info("解除无敌，actorId:{}", actor.getShadowId());
			actor.handleInvincible(false);
		}
	}
}
