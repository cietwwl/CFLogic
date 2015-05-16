package com.kola.kmp.logic.combat.function;

import java.util.List;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.state.ICombatStateTemplate;

/**
 * 
 * @author PERRY CHAN
 */
public class KAddStateFunctionParser implements IFunctionParser {

	private static final byte TARGET_TYPE_OWN = 1;
	private static final byte TARGET_TYPE_ENERMY = 2;
	
	private static final int INDEX_RATE = 1;
	private static final int INDEX_STATE_ID = 2;
	private static final int INDEX_TARGET = 3;

	@Override
	public IFunctionExecution parse(List<Integer> args) {
		KAddStateFunction function = new KAddStateFunction();
		int stateType = args.get(INDEX_TARGET);
		switch (stateType) {
		case ICombatStateTemplate.STATE_TYPE_ADD_STATE_TO_OWN:
			function._targetType = TARGET_TYPE_OWN;
			break;
		case ICombatStateTemplate.STATE_TYPE_ADD_STATE_TO_TARGET:
			function._targetType = TARGET_TYPE_ENERMY;
			break;

		}
		function._rate = args.get(INDEX_RATE);
		function._stateId = args.get(INDEX_STATE_ID);
		return function;
	}
	
	private static class KAddStateFunction implements IFunctionExecution {

		private byte _targetType; // 目标类型
		private int _rate; // 几率
		private int _stateId; // 状态id

		@Override
		public void execute(ICombat combat, ICombatMember operator, long happenTime) {
			int actualRate = combat.getCombatRandomInstance().nextInt(TEN_THOUSAND);
			if (actualRate < _rate) {
				ICombatMember target = null;
				switch (_targetType) {
				case TARGET_TYPE_OWN:
					target = operator;
					break;
				case TARGET_TYPE_ENERMY:
					target = operator.getCombatRecorder().getLastTarget();
					break;
				}
				if (target != null && target.isAlive()) {
					target.getSkillActor().addState(operator, _stateId, happenTime);
				}
			}
		}
	}

}
