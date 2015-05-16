package com.kola.kmp.logic.reward;

import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.role.KRole;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-5-7 上午10:30:15
 * </pre>
 */
public interface IRewardRoleEventListener {

	/**
	 * 
	 * 角色登录游戏
	 * 
	 * @param role
	 */
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role, long nowTime);
	
	/**
	 * 
	 * 角色离开游戏
	 * 
	 * @param role
	 */
	public void notifyRoleLeavedGame(/*KGamePlayerSession session, */KRole role, long nowTime);
	
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
}
