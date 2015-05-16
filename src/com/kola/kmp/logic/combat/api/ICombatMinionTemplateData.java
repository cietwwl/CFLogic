package com.kola.kmp.logic.combat.api;

import java.util.List;

import com.kola.kmp.logic.other.KObstructionTargetType;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICombatMinionTemplateData extends ICombatObjectBase {
	
	/**
	 * 
	 * @return
	 */
	public int getAtkCountPerTime();

	/**
	 * 
	 * 获取攻击力
	 * 
	 * @return
	 */
	public int getAtk();
	
	/**
	 * 
	 * 获取防御力
	 * 
	 * @return
	 */
	public int getDef();
	
	/**
	 * 
	 * 获取命中等级
	 * 
	 * @return
	 */
	public int getHitRating();
	
	/**
	 * 
	 * 获取防御等级
	 * 
	 * @return
	 */
	public int getDodgeRating();
	
	/**
	 * 
	 * 获取暴击等级
	 * 
	 * @return
	 */
	public int getCritRating();
	
	/**
	 * 
	 * 获取抗爆等级
	 * 
	 * @return
	 */
	public int getResilienceRating();
	
	/**
	 * 
	 * @return
	 */
	public int getCritMultiple();
	
	/**
	 * 
	 * 获取攻击距离
	 * 
	 * @return
	 */
	public int getAtkRange();
	
	/**
	 * 
	 * 获取攻击间隔（毫秒）
	 * 
	 * @return
	 */
	public int getAtkPeriod();
	
	/**
	 * 
	 * @return
	 */
	public boolean isGenerateByOwner();
	
	/**
	 * 
	 * @return
	 */
	public int getDuration();
	
	/**
	 * 
	 * @return
	 */
	public KObstructionTargetType getTargetType();
	
	/**
	 * 
	 * 是否霸体
	 * 
	 * @return
	 */
	public boolean isFullImmunity();
	
	/**
	 * 
	 * 霸体持续时间（单位：毫秒）
	 * 
	 * @return
	 */
	public int getFullImmunityDuration();
	
	/**
	 * 
	 * 霸体间隔（单位：毫秒）
	 * 
	 * @return
	 */
	public int getFullImmunityIteration();
	
	/**
	 * 
	 * 获取所有技能
	 * 
	 * @return
	 */
	public List<ICombatSkillData> getAllSkills();
	
	/**
	 * 
	 * 获取AI的id
	 * 
	 * @return
	 */
	public String getAIId();
}
