package com.kola.kmp.logic.combat.api;

import java.util.Map;

import com.kola.kmp.logic.other.KGameAttrType;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICombatMount extends ICombatObjectBase, ICombatSkillSupport {
	
	/**
	 * 
	 * 获取机甲加速的倍数
	 * 
	 * @return
	 */
	public float getSpeedUpTimes();
	
	/**
	 * 
	 * 获取单次普通攻击的攻击次数
	 * 
	 * @return
	 */
	public int getAtkCountPerTime();
	
	/**
	 * 
	 * 获取每次霸体时长（单位：毫秒）
	 * 
	 * @return
	 */
	public int getFullImmunityDuration();
	
	/**
	 * 
	 * 获取每次霸体的间隔（单位：毫秒）
	 * 
	 * @return
	 */
	public int getFullImmunityIteration();
	
	/**
	 * 
	 * @return
	 */
	public String getAI();
	
	/**
	 * <pre>
	 * <怒气豆数量,有效时长（秒）>，1，2，3个怒气豆
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2015-1-14 上午9:21:56
	 * </pre>
	 */
	public Map<Integer,Integer> getBeanTime();
	
	/**
	 * 
	 * 获取机甲的基础属性
	 * 
	 * @return
	 */
	public Map<KGameAttrType, Integer> getBasicAttrs();
	
	/**
	 * 
	 * 获取机甲的装备属性
	 * 
	 * @return
	 */
	public Map<KGameAttrType, Integer> getEquipmentAttrs();
}
