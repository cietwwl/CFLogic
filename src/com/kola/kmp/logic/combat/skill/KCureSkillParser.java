package com.kola.kmp.logic.combat.skill;

import java.util.List;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.support.IOperationRecorder;

/**
 * 
 * @author PERRY CHAN
 */
public class KCureSkillParser implements ICombatSkillExecutionParser {

	@Override
	public ICombatSkillExecution parse(int pSkillTemplateId, int skillLv, List<Integer> paras) {
		KCureSkillExecution instance = new KCureSkillExecution();
		instance.baseParse(pSkillTemplateId, paras);
		return instance;
	}

	private static class KCureSkillExecution extends KCureSkillExecutionBaseImpl implements ICombatSkillExecution {
		@Override
		public List<IOperationRecorder> execute(ICombat combat, ICombatMember operator, List<ICombatMember> targets, String useCode, long happenTime) {
			return this.baseExecute(combat, operator, targets, happenTime, useCode);
		}
	}
	
}
