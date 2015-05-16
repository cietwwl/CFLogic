package com.kola.kmp.logic.combat.skill;

import java.util.List;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICombatSkillExecutionParser {

	/**
	 * 
	 * @return
	 */
	public ICombatSkillExecution parse(int pSkillTemplateId, int skillLv, List<Integer> paras);
}
