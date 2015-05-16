package com.kola.kmp.logic.combat.api;

import java.util.List;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICombatSkillSupport {

	/**
	 * 
	 * 获取可以使用的技能列表
	 * 
	 * @return
	 */
	public List<ICombatSkillData> getUsableSkills();
	
	/**
	 * 
	 * 获取被动技能列表
	 * 
	 * @return
	 */
	public List<ICombatSkillData> getPassiveSkills();
}
