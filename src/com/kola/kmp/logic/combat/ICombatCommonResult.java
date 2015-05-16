package com.kola.kmp.logic.combat;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICombatCommonResult {

	/**
	 * 
	 * 获取战斗结果的角色id
	 * 
	 * @return
	 */
	public long getRoleId();
	
	/**
	 * 
	 * 获取战斗一共耗时
	 * 
	 * @return
	 */
	public long getCombatTime();
	
	/**
	 * 
	 * 获取本场战斗所造成的总伤害
	 * 
	 * @return
	 */
	public long getTotalDamage();
	
	/**
	 * 
	 * 获取是否战斗胜利
	 * 
	 * @return
	 */
	public boolean isWin();
	
	/**
	 * 
	 * 角色是否逃跑
	 * 
	 * @return
	 */
	public boolean isEscape();
	
	/**
	 * 
	 * 获取附加属性
	 * 
	 * @return
	 */
	public Object getAttachment();
	
	/**
	 * 获取战斗类型
	 * @return
	 */
	public KCombatType getCombatType();
	
	/**
	 * 
	 */
	public void setAttachment(Object pAttachment);
	
	/**
	 * 
	 * 获取战斗结束后，剩余的HP
	 * 
	 * @return
	 */
	public long getRoleCurrentHp();
	
	/**
	 * 获取战斗结束后，随从剩余的HP
	 * 如果为-1，则表示本次战斗中没有携带随从
	 * 
	 * @return
	 */
	public long getPetCurrentHp();
}
