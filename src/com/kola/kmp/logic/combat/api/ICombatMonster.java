package com.kola.kmp.logic.combat.api;

import java.util.List;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICombatMonster extends ICombatObjectBase, ICombatObjectFight {
	
	/**
	 * 
	 * @return
	 */
	public int getAtkCountPerTime();

	/**
	 * 获取掉落编号
	 * 
	 * @return
	 */
	public List<Integer> getDropId();
	
	/**
	 * 
	 * 获取被击杀后所能获得的怒气
	 * 
	 * @return
	 */
	public int getKilledEnergy();
	
	/**
	 * 
	 * 获取颜色值
	 * 
	 * @return
	 */
	public int getColor();
	
	/**
	 * 
	 * 获取AI模板id
	 * 
	 * @return
	 */
	public String getAIId();
	
	/**
	 * 
	 * @return
	 */
	public ICombatSkillSupport getCombatSkillSupport();
	
	/**
	 * 
	 * @return
	 */
	public boolean isFullImmunity();
	
	/**
	 * 
	 * 获取霸体的持续时间（单位：毫秒）
	 * 
	 * @return
	 */
	public int getFullImmunityDuration();
	
	/**
	 * 
	 * 获取霸体的间隔，即一次霸体结束之后，到下一次霸体开始时的间隔（单位：毫秒）
	 * 
	 * @return
	 */
	public int getFullImmunityIteration();
}
