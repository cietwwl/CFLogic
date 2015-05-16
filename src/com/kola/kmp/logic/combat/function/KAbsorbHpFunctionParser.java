package com.kola.kmp.logic.combat.function;

import java.util.List;

import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;

/**
 * 
 * @author PERRY CHAN
 */
public class KAbsorbHpFunctionParser implements IFunctionParser {

	private static final int INDEX_FIX_VALUE = 2;
	private static final int INDEX_PCT_VALUE = 1;
	
	@Override
	public IFunctionExecution parse(List<Integer> args) {
		KAbsorbHpFunction instance = new KAbsorbHpFunction();
		instance._pctValue = args.get(INDEX_PCT_VALUE);
		instance._fixValue = args.get(INDEX_FIX_VALUE);
		return instance;
	}
	
	private static class KAbsorbHpFunction implements IFunctionExecution {

		private int _fixValue;
		private int _pctValue;
		
		@Override
		public void execute(ICombat combat, ICombatMember operator, long happenTime) {
			if (!operator.getCombatRecorder().lastAttackTypeIsSkill()) {
				int total = 0;
				if (_fixValue > 0) {
					total += _fixValue;
				}
				if (_pctValue > 0) {
					total += UtilTool.calculateTenThousandRatioL(operator.getCombatRecorder().getLastDm(), _pctValue);
				}
				operator.increaseHp(total);
//				LOGGER.info("吸血特效，攻击者id：{}，最后伤害数值：{}，回血量：{}", operator.getShadowId(), operator.getCombatRecorder().getLastDm(), total);
			}
		}
		
	}

}
