package com.kola.kmp.logic.role;

import com.koala.game.player.KGamePlayerSession;

/**
 * 
 * @author PERRY CHAN
 */
public interface IRoleEventListener {

	/**
	 * 
	 * 角色登录游戏
	 * 
	 * @param role
	 */
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role);
	
	/**
	 * 
	 * 角色离开游戏
	 * 
	 * @param role
	 */
//	public void notifyRoleLeavedGame(KGamePlayerSession session, KRole role);
	public void notifyRoleLeavedGame(KRole role);
	
	/**
	 * 
	 * 角色创建
	 * 
	 * @param role
	 */
	public void notifyRoleCreated(KGamePlayerSession session, KRole role);
	
	/**
	 * 
	 * 角色删除
	 * 
	 * @param roleId
	 */
	public void notifyRoleDeleted(long roleId);
	
	/**
	 * 
	 * 角色升级的通知
	 * 
	 * @param role
	 * @param preLv
	 */
	public void notifyRoleLevelUp(KRole role, int preLv);
	
	/**
	 * 
	 * 通知角色数据被放入缓存当中
	 * 
	 * @param role
	 */
	public void notifyRoleDataPutToCache(KRole role);
}
