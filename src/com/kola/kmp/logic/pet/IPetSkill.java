package com.kola.kmp.logic.pet;

import com.kola.kmp.logic.combat.api.ICombatSkillData;

/**
 * 
 * @author PERRY CHAN
 */
public interface IPetSkill extends ICombatSkillData {

	/** 技能类型：主动技能 */
	byte SKILL_TYPE_ACTIVE = 1;
	/** 技能类型：被动技能 */
	byte SKILL_TYPE_PASSIVE = 2;
	
	/**
	 * 
	 * <pre>
	 * 获取技能的类型，返回：{@link #SKILL_TYPE_ACTIVE}或{@link #SKILL_TYPE_PASSIVE}
	 * </pre>
	 * 
	 * @return
	 */
	public byte getType();
	
	/**
	 * 
	 * <pre>
	 * 是否主动技能
	 * 当{@link #getSkillType() == #SKILL_TYPE_ACTIVE}时，返回true
	 * <pre>
	 * 
	 * @return
	 */
	public boolean isActiveSkill();
	
	/**
	 * 
	 * 技能升级的概率
	 * 
	 * @return
	 */
	public int getLvUpRate();
	
	/**
	 * 
	 * @return
	 */
	public boolean isMaxLv();
	
	/**
	 * 
	 * 获取获得概率
	 * 
	 * @return
	 */
	public int getRate();
	
}
