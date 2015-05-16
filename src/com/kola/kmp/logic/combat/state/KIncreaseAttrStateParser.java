package com.kola.kmp.logic.combat.state;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.api.ICombatSkillActor;
import com.kola.kmp.logic.other.KGameAttrType;

/**
 * 
 * @author PERRY CHAN
 */
public class KIncreaseAttrStateParser implements ICombatStateParser {

	@Override
	public ICombatState newInstance(ICombatMember operator, ICombatStateTemplate template) {
		KIncreaseAttrState instance = new KIncreaseAttrState();
		instance.init(template);
		return instance;
	}

	private static class KIncreaseAttrState extends KCombatStateBaseImpl {

		private static final int INDEX_ATTR_TYPE = INDEX_LAST_TIME_MILLIS + 1;
		private static final int INDEX_EFFECT_VALUE = INDEX_ATTR_TYPE + 1;
		private static final int INDEX_ATTR_TYPE_PCT = INDEX_EFFECT_VALUE + 1;
		private static final int INDEX_EFFECT_VALUE_PCT = INDEX_ATTR_TYPE_PCT + 1;
		
		private KGameAttrType _attrType; // 属性类型
		private int _value; // 值
		private KGameAttrType _attrTypePct; // 属性类型
		private int _valuePct; // 值

		@Override
		protected void parsePara(int[] paras) {
			super.setLastTimeMillis(paras[INDEX_LAST_TIME_MILLIS]);
			this._attrType = KGameAttrType.getAttrTypeEnum(paras[INDEX_ATTR_TYPE]);
			this._value = paras[INDEX_EFFECT_VALUE];
			if (paras.length > INDEX_ATTR_TYPE_PCT) {
				_attrTypePct = KGameAttrType.getAttrTypeEnum(paras[INDEX_ATTR_TYPE_PCT]);
				_valuePct = paras[INDEX_EFFECT_VALUE_PCT];
			}
		}

		@Override
		public void onAdded(ICombat combat, ICombatSkillActor actor, long happenTime) {
			LOGGER.info("修改角色属性：{}，修改值：{}", _attrType, _value);
			actor.changeCombatAttr(_attrType, _value, true);
			if (_attrTypePct != null) {
				actor.changeCombatAttr(_attrTypePct, _valuePct, true);
			}
		}

		@Override
		public void durationEffect(ICombat combat, ICombatSkillActor actor, long happenTime) {

		}

		@Override
		public void onRemoved(ICombat combat, ICombatSkillActor actor) {
			LOGGER.info("扣减角色属性：{}，修改值：{}", _attrType, _value);
			actor.changeCombatAttr(_attrType, _value, false);
			if (_attrTypePct != null) {
				actor.changeCombatAttr(_attrTypePct, _valuePct, false);
			}
		}
	}

}
