package com.kola.kmp.logic.combat.resulthandler;

import java.util.Map;

import com.kola.kmp.logic.combat.ICombatCommonResult;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICombatRoleResult extends ICombatCommonResult {

	/**
	 * 
	 * 获取角色的id
	 * 
	 * @return
	 */
	public long getRoleId();
	
	/**
	 * 
	 * 角色是否生存
	 * 
	 * @return
	 */
	public boolean isAlive();
	
	/**
	 * 
	 * 获取最大的连击数
	 * 
	 * @return
	 */
	public int getMaxComboCount();
	
	/**
	 * 
	 * 获取最大的受击数量
	 * 
	 * @return
	 */
	public int getMaxBeHitCount();
	
	/**
	 * 
	 * 获取杀死的怪物信息
	 * 
	 * @return key=怪物模板id，value=数量
	 */
	public Map<Integer, Short> getKillMonsterCount();
}
