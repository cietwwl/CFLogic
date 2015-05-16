package com.kola.kmp.logic.combat.api;

import java.util.List;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICombatSkillTemplateData {

	/** 主动技能类型：伤害 */
	public static final int INACTIVE_SKILL_DM = 1;
	/** 主动技能类型：伤害并添加状态 */
	public static final int INACTIVE_SKILL_DM_AND_ADD_STATE = 2;
	/** 主动技能类型：治疗 */
	public static final int INACTIVE_SKILL_CURE = 3;
	/** 主动技能类型：治疗并添加状态 */
	public static final int INACTIVE_SKILL_CURE_AND_ADD_STATE = 4;
	/** 主动技能类型：添加多个状态 */
	public static final int INACTIVE_SKILL_ADD_MULTIPLE_STATE = 5;
	/** 主动技能类型：召唤类型 */
	public static final int INACTIVE_SKILL_SUMMON = 6;
	
	/** 被动技能类型：增加属性 */
	public static final int PASSIVE_SKILL_INCREASE_ATTR = 1;
	/** 被动技能类型：添加特效 */
	public static final int PASSIVE_SKILL_SPECIAL_EFFECT = 2;
	/** 被动技能类型：出战添加状态 */
	public static final int PASSIVE_SKILL_PVP_ADD_STATE = 3;
	/**
	 * 
	 * @return
	 */
	public int getSkillTemplateId();
	
	/**
	 * 
	 * @return
	 */
	public int getSkillType();
	
	/**
	 * 
	 * @return
	 */
	public int getSkillLv();
	
	/**
	 * 
	 * @return
	 */
	public List<Integer> getSkillArgs();
}
