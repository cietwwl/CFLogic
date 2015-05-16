package com.kola.kmp.logic.combat.state;

import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.api.ICombatSkillActor;
import com.kola.kmp.logic.other.KGameAttrType;

/**
 * 
 * @author PERRY CHAN
 */
public class KDecreaseFixedAttrCycStateParser implements ICombatStateParser {

	@Override
	public ICombatState newInstance(ICombatMember operator, ICombatStateTemplate template) {
		KDecreaseFixedAttrCycState instance = new KDecreaseFixedAttrCycState();
		instance.init(template);
		return instance;
	}
	
	private static class KDecreaseFixedAttrCycState extends KCombatStateBaseImpl {

		private static final int INDEX_EFFECT_HP_TYPE= INDEX_LAST_TIME_MILLIS + 1;
		private static final int INDEX_EFFECT_HP_VALUE = INDEX_EFFECT_HP_TYPE + 1;
		private KGameAttrType _attrType;
		private int _value;

		@Override
		protected void parsePara(int[] paras) {
			this.setLastTimeMillis(paras[INDEX_LAST_TIME_MILLIS]);
			this._attrType = KGameAttrType.getAttrTypeEnum(paras[INDEX_EFFECT_HP_TYPE]);
			this._value = paras[INDEX_EFFECT_HP_VALUE];
			if (this._attrType == null) {
				throw new IllegalArgumentException("属性类型为null，状态id：" + this.getStateTemplateId());
			} else if (this._attrType != KGameAttrType.HP && this._attrType != KGameAttrType.HP_PCT) {
				throw new IllegalArgumentException("属性类型只能为" + KGameAttrType.HP.sign + "或者" + KGameAttrType.HP_PCT.sign + "，状态id：" + this.getStateTemplateId());
			}
		}

		@Override
		public boolean isCycState() {
			return true;
		}
		
		@Override
		public void onAdded(ICombat combat, ICombatSkillActor actor, long happenTime) {
			switch (_attrType) {
			case HP_PCT:
				this._value = (int) UtilTool.calculateTenThousandRatioL(actor.getMaxHp(), this._value);
				break;
			default:
				break;
			}
		}

		@Override
		public void durationEffect(ICombat combat, ICombatSkillActor actor, long happenTime) {
			actor.decreaseHp(_value, happenTime);
//			LOGGER.info("bufferId：{}，周期性扣血，value={}，当前剩余血量={}，名字={}，shadowId={}", this.getStateTemplateId(), _value, actor.getCurrentHp(), ((ICombatMember) actor).getName(), ((ICombatMember) actor).getShadowId());
		}

		@Override
		public void onRemoved(ICombat combat, ICombatSkillActor actor) {

		}
	}
}
