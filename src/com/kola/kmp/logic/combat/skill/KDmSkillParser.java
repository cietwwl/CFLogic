package com.kola.kmp.logic.combat.skill;

import java.util.List;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.support.IOperationRecorder;

/**
 * 
 * @author PERRY CHAN
 */
public class KDmSkillParser implements ICombatSkillExecutionParser {
	
	@Override
	public ICombatSkillExecution parse(int pSkillTemplateId, int skillLv, List<Integer> paras) {
		KDmSkillExecution instance = new KDmSkillExecution();
		instance.baseParse(pSkillTemplateId, paras);
		return instance;
	}
	
	private static class KDmSkillExecution extends KDmSkillExecutionBaseImpl implements ICombatSkillExecution {
		@Override
		public List<IOperationRecorder> execute(ICombat combat, ICombatMember operator, List<ICombatMember> targets, String useCode, long happenTime) {
			return super.baseExecute(combat, operator, targets, useCode, happenTime);
		}
	}

}
