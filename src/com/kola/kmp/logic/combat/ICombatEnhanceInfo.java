package com.kola.kmp.logic.combat;

/**
 * 
 * 战斗里面的属性增强信息<p>
 * 这里包含了攻击力（含万分比）和防御力（含万分比）的加成
 * 
 * @author PERRY CHAN
 */
public interface ICombatEnhanceInfo {

	/**
	 * 
	 * 获取攻击的加成数值
	 * 
	 * @return
	 */
	public int getAtkInc();
	
	/**
	 * 
	 * 获取防御的加成数值
	 * 
	 * @return
	 */
	public int getDefInc();
	
	/**
	 * 
	 * 获取攻击的万分比加成数值
	 * 
	 * @return
	 */
	public int getAtkPctInc();
	
	/**
	 * 
	 * 获取防御的万分比加成数值
	 * 
	 * @return
	 */
	public int getDefPctInc();
}
