package com.kola.kmp.logic.combat.state;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.api.ICombatSkillActor;
import com.kola.kmp.logic.other.KGameAttrType;

/**
 * 
 * @author PERRY CHAN
 */
public class KDecreaseAttrStateParser implements ICombatStateParser {

	@Override
	public ICombatState newInstance(ICombatMember operator, ICombatStateTemplate template) {
		KDecreaseAttrState instance = new KDecreaseAttrState();
		instance.init(template);
		return instance;
	}
	
	private static class KDecreaseAttrState extends KCombatStateBaseImpl {
		
		private static final int INDEX_ATTR_TYPE = INDEX_LAST_TIME_MILLIS + 1;
		private static final int INDEX_EFFECT_VALUE = INDEX_ATTR_TYPE + 1;
		private static final int INDEX_ATTR_TYPE_PCT = INDEX_EFFECT_VALUE + 1;
		private static final int INDEX_EFFECT_VALUE_PCT = INDEX_ATTR_TYPE_PCT + 1;
		
		private KGameAttrType _attrType;
		private int _value;
		private KGameAttrType _attrTypePct;
		private int _valuePct;

		@Override
		protected void parsePara(int[] paras) {
			super.setLastTimeMillis(paras[INDEX_LAST_TIME_MILLIS]);
			this._attrType = KGameAttrType.getAttrTypeEnum(paras[INDEX_ATTR_TYPE]);
			this._value = paras[INDEX_EFFECT_VALUE];
			if (paras.length > INDEX_ATTR_TYPE_PCT) {
				this._attrTypePct = KGameAttrType.getAttrTypeEnum(paras[INDEX_ATTR_TYPE_PCT]);
				this._valuePct = paras[INDEX_EFFECT_VALUE_PCT];
			}
		}

		@Override
		public void onAdded(ICombat combat, ICombatSkillActor actor, long happenTime) {
			if (_attrType != null) {
				actor.changeCombatAttr(_attrType, _value, false);
			}
			if (_attrTypePct != null) {
				actor.changeCombatAttr(_attrTypePct, _valuePct, false);
			}
		}

		@Override
		public void durationEffect(ICombat combat, ICombatSkillActor actor, long happenTime) {

		}

		@Override
		public void onRemoved(ICombat combat, ICombatSkillActor actor) {
			if (_attrType != null) {
				actor.changeCombatAttr(_attrType, _value, true);
			}
			if (_attrTypePct != null) {
				actor.changeCombatAttr(_attrTypePct, _valuePct, true);
			}
		}
	}
}
