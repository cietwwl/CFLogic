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
public class KDmAndAddStateSkillParser implements ICombatSkillExecutionParser {

	@Override
	public ICombatSkillExecution parse(int pSkillTemplateId, int skillLv, List<Integer> paras) {
		KDmAndAddStateSkillExecution instance = new KDmAndAddStateSkillExecution();
		instance.baseParse(pSkillTemplateId, paras);
//		instance._assistant = new KStateAssistant(paras.get(KDmAndAddStateSkillExecution.INDEX_STATE_ID));
		instance._assistants = new ArrayList<KStateAssistant>();
		int stateId;
		for (int i = KDmAndAddStateSkillExecution.INDEX_STATE_ID; i < paras.size(); i++) {
			stateId = paras.get(i);
			if (stateId > 0) {
				instance._assistants.add(new KStateAssistant(paras.get(i)));
			} else {
				break;
			}
		}
		return instance;
	}
	
	private static class KDmAndAddStateSkillExecution extends KDmSkillExecutionBaseImpl implements ICombatSkillExecution {
		
		private static final int INDEX_STATE_ID = INDEX_CD_MILLIS + 1;
//		private KStateAssistant _assistant; // 状态协助
		private List<KStateAssistant> _assistants;
		
		@Override
		public List<IOperationRecorder> execute(ICombat combat, ICombatMember operator, List<ICombatMember> targets, String useCode, long happenTime) {
//			if(this.skillTemplateId == 410533 || this.skillTemplateId == 410534) {
//				System.out.println();
//			}
//			boolean isFirst = operator.getSkillActor().isNewSettle(skillTemplateId, useCode);
//			if(!isFirst) {
//				isFirst = operator.getSkillActor().isTimeFirstSettle(skillTemplateId, useCode, happenTime);
//			}
			List<ICombatMember> stateTargets = new ArrayList<ICombatMember>();
			ICombatMember temp;
			for(int i = 0; i < targets.size(); i++) {
				temp = targets.get(i);
				if(operator.getSkillActor().getTargetSettleCount(skillTemplateId, useCode, temp.getShadowId(), happenTime) == 0) {
					stateTargets.add(temp);
//					ICombat.LOGGER.info("operator:[{},{}], skillTemplateId:{}, useCode:{}, targetId:{}, 首次结算，添加buff", operator.getName(), operator.getShadowId(), skillTemplateId, useCode, temp.getShadowId());
				}
			}
			List<IOperationRecorder> list = super.baseExecute(combat, operator, targets, useCode, happenTime);
//			if (isFirst) {
////				if(this.skillTemplateId == 410533 || this.skillTemplateId == 410534) {
////					System.out.println();
////				}
//				// 第一次执行技能才需要加状态
////				_assistant.executeState(combat, operator, targets, happenTime);
//				for (int i = 0; i < _assistants.size(); i++) {
//					_assistants.get(i).executeState(combat, operator, targets, happenTime);
//				}
//			}List<ICombatMember> stateTargets = new ArrayList<ICombatMember>();
			if (stateTargets.size() > 0) {
				for (int i = 0; i < _assistants.size(); i++) {
					_assistants.get(i).executeState(combat, operator, stateTargets, happenTime);
				}
			}
			return list;
		}
	}

}
