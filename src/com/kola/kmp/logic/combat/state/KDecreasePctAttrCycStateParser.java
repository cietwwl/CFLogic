package com.kola.kmp.logic.combat.state;

import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.api.ICombatSkillActor;

/**
 * 
 * 周期性伤害状态
 * 按照攻击者的攻击力百分比所产生的伤害
 * 
 * @author PERRY CHAN
 */
public class KDecreasePctAttrCycStateParser implements ICombatStateParser {

	@Override
	public ICombatState newInstance(ICombatMember operator, ICombatStateTemplate template) {
		KDecreasePctAttrCycState instance = new KDecreasePctAttrCycState();
		instance.init(template);
		instance._effectValue = UtilTool.calculateTenThousandRatio(operator.getAtk(), instance._pct);
		return instance;
	}

	private static class KDecreasePctAttrCycState extends KCombatStateBaseImpl {

		private static final int INDEX_EFFECT_HP_PCT = INDEX_LAST_TIME_MILLIS + 2; // +1是属性类型
		private int _pct;
		private int _effectValue;

		@Override
		protected void parsePara(int[] paras) {
			this.setLastTimeMillis(paras[INDEX_LAST_TIME_MILLIS]);
			this._pct = paras[INDEX_EFFECT_HP_PCT];
		}

		@Override
		public boolean isCycState() {
			return true;
		}

		@Override
		public void onAdded(ICombat combat, ICombatSkillActor actor, long happenTime) {

		}

		@Override
		public void durationEffect(ICombat combat, ICombatSkillActor actor, long happenTime) {
			actor.decreaseHp(_effectValue, happenTime);
//			LOGGER.info("bufferId：{}，周期性扣血，value={}，当前剩余血量={}，名字={}，shadowId={}", this.getStateTemplateId(), _effectValue, actor.getCurrentHp(), ((ICombatMember) actor).getName(), ((ICombatMember) actor).getShadowId());
		}

		@Override
		public void onRemoved(ICombat combat, ICombatSkillActor actor) {

		}
	}
}
