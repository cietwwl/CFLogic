package com.kola.kmp.logic.combat.skill;

import java.util.ArrayList;
import java.util.List;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.support.IOperationRecorder;

/**
 * 
 * @author PERRY CHAN
 */
public class KCureAndAddStateSkillParser implements ICombatSkillExecutionParser {

	@Override
	public ICombatSkillExecution parse(int pSkillTemplateId, int skillLv, List<Integer> paras) {
		KCureAndAddStateSkillExecution instance = new KCureAndAddStateSkillExecution();
		instance.baseParse(pSkillTemplateId, paras);
		instance._stateAssistant = new KStateAssistant(paras.get(KCureAndAddStateSkillExecution.INDEX_STATE_ID));
		return instance;
	}

	private static class KCureAndAddStateSkillExecution extends KCureSkillExecutionBaseImpl implements ICombatSkillExecution {
		private static final int INDEX_STATE_ID = INDEX_CD_MILLIS + 1;
		private KStateAssistant _stateAssistant;

		@Override
		public List<IOperationRecorder> execute(ICombat combat, ICombatMember operator, List<ICombatMember> targets, String useCode, long happenTime) {
//			boolean isFirst = operator.getSkillActor().getSkillSettleCount(skillTemplateId, useCode) == 0;
			List<ICombatMember> stateTargets = new ArrayList<ICombatMember>();
			ICombatMember temp;
			for(int i = 0; i < targets.size(); i++) {
				temp = targets.get(i);
				if(operator.getSkillActor().getTargetSettleCount(skillTemplateId, useCode, temp.getShadowId(), happenTime) == 1) {
					stateTargets.add(temp);
//					ICombat.LOGGER.info("operator:[{},{}], skillTemplateId:{}, useCode:{}, targetId:{}, 首次结算，添加buff", operator.getName(), operator.getShadowId(), skillTemplateId, useCode, temp.getShadowId());
				}
			}
			List<IOperationRecorder> list = super.baseExecute(combat, operator, targets, happenTime, useCode);
//			if (isFirst) {
//				_stateAssistant.executeState(combat, operator, targets, happenTime);
//			}
			if (stateTargets.size() > 0) {
				_stateAssistant.executeState(combat, operator, stateTargets, happenTime);
			}
			return list;
		}
	}

}
