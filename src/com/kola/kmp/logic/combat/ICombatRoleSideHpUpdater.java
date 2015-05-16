package com.kola.kmp.logic.combat;

public interface ICombatRoleSideHpUpdater {

	/**
	 * 
	 * 获取关注的战斗类型
	 * 如果同一种战斗类型底下有好几种
	 * 
	 * @return
	 */
	public KCombatType getCombatTypeResponse();
	
	/**
	 * 
	 * @return
	 */
	public boolean handleRoleHpUpdate();
	
	/**
	 * 
	 * 获取主角的HP
	 * 
	 * @param roleId
	 * @return
	 */
	public long getRoleHp(long roleId);
	
	/**
	 * 
	 * @return
	 */
	public boolean handlePetHpUpdate();
	
	/**
	 * 
	 * 获取随从的HP
	 * 
	 * @param roleId
	 * @param petId
	 * @return
	 */
	public long getPetHp(long roleId, long petId);
}
