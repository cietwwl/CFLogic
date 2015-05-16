package com.kola.kmp.logic.combat.api;


/**
 * 
 * @author PERRY CHAN
 */
public interface ICombatRole extends ICombatObjectBase, ICombatObjectFight {
	
	/**
	 * 
	 * 获取技能伤害百分比加成
	 * 
	 * @return
	 */
	public int getSkillDmPctInc();
	
	/**
	 * 
	 * 获取伤害减免比例
	 * 
	 * @return
	 */
	public int getDmReducePct();

	/**
	 * 
	 * 获取当前的怒气值
	 * 
	 * @return
	 */
	public int getCurrentEnergy();
	
	/**
	 * 
	 * 获取怒气上限
	 * 
	 * @return
	 */
	public int getMaxEnergy();
	
	/**
	 * 
	 * 获取当前怒气豆的数量
	 * 
	 * @return
	 */
	public int getEnergyBean();
	
	/**
	 * 
	 * 获取最大的怒气豆数量
	 * 
	 * @return
	 */
	public int getMaxEnergyBean();
	
	/**
	 * 
	 * 获取HP恢复速度
	 * 
	 * @return
	 */
	public int getHpRecovery();
	
	/**
	 *
	 * 获取AIId
	 * 
	 * @return
	 */
	public String getAIId();
	
	/**
	 * 获取职业
	 * @return
	 */
	public byte getJob();
	
	/**
	 * 
	 * @return
	 */
	public int getBattlePower();
	
	/**
	 * 
	 * 获取技能支持
	 * 
	 * @return
	 */
	public ICombatSkillSupport getSkillSupport();
}
