package com.kola.kmp.logic.combat.api;

import java.util.List;

import com.kola.kmp.logic.other.KObstructionTargetType;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICombatBlock extends ICombatObjectBase, ICombatObjectFight {
	
	/**
	 * 获取目标类型
	 * @return
	 */
	public KObstructionTargetType getTargetType();
	
	/**
	 * 
	 * 获取掉落id
	 * 
	 * @return
	 */
	public List<Integer> getDropId();
	
	/**
	 * 
	 * 获取生存状态的情况下，直接造成的状态id
	 * 
	 * @return
	 */
	public int getStateIdDuringAlive();
	
	/**
	 * 
	 * 获取被摧毁后，造成的状态id
	 * 
	 * @return
	 */
	public int getStateIdAfterDestoryed();
	
	/**
	 * 
	 * @return
	 */
	public int getAppearRate();
}
