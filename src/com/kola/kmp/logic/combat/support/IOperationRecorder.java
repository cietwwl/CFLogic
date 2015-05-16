package com.kola.kmp.logic.combat.support;

/**
 * 
 * @author PERRY CHAN
 */
public interface IOperationRecorder {
	
	/**
	 * 
	 * 获取目标的id
	 * 
	 * @return
	 */
	public short getTargetId();
	
	/**
	 * 
	 * 是否伤害
	 * 
	 * @return
	 */
	public boolean isDamage();

	/**
	 * 
	 * 是否命中
	 * 
	 * @return
	 */
	public boolean isHit();
	
	/**
	 * 
	 * 是否暴击
	 * 
	 * @return
	 */
	public boolean isCrit();
	
	/**
	 * 
	 * 获取伤害数量
	 * 
	 * @return
	 */
	public int getDm();
}
