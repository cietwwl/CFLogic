package com.kola.kmp.logic.combat.skill;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.support.IOperationRecorder;

/**
 * 
 * @author PERRY CHAN
 */
public class KAddMultipleStateSkillParser implements ICombatSkillExecutionParser {

	@Override
	public ICombatSkillExecution parse(int pSkillTemplateId, int skillLv, List<Integer> paras) {
		KAddMultipleStateSkillExecution instance = new KAddMultipleStateSkillExecution();
		instance._cdMillis = (int)TimeUnit.MILLISECONDS.convert(paras.get(0), TimeUnit.SECONDS);
		instance._stateAssistants = new ArrayList<KStateAssistant>();
		for (int i = 1; i < paras.size(); i++) {
			int stateId = paras.get(i);
			if (stateId > 0) {
				instance._stateAssistants.add(new KStateAssistant(stateId));
			}
		}
		instance._templateId = pSkillTemplateId;
		return instance;
	}

	private static class KAddMultipleStateSkillExecution implements ICombatSkillExecution {
		private int _cdMillis;
		private int _templateId;
		private List<KStateAssistant> _stateAssistants;

		@Override
		public List<IOperationRecorder> execute(ICombat combat, ICombatMember operator, List<ICombatMember> targets, String useCode, long happenTime) {
			boolean isNewSettle = operator.getSkillActor().isNewSettle(_templateId, useCode);
			if (isNewSettle) {
				for (int i = 0; i < _stateAssistants.size(); i++) {
					_stateAssistants.get(i).executeState(combat, operator, targets, happenTime);
				}
//				for (int i = 0; i < targets.size(); i++) {
//					operator.getSkillActor().recordSkillUsed(_templateId, targets.get(i).getShadowId(), useCode, happenTime);
//				}
				operator.getSkillActor().recordSkillUse(_templateId, useCode, happenTime);
				operator.getSkillActor().recordSkillCoolDown(_templateId, happenTime, _cdMillis);
			}
			return null;
		}
	}
}
