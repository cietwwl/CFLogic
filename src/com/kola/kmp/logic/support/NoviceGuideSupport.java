package com.kola.kmp.logic.support;

import com.kola.kmp.logic.level.KGameLevelSet;
import com.kola.kmp.logic.role.KRole;

public interface NoviceGuideSupport {

	public void processNoviceGuide(KRole role);

	/**
	 * 检测角色是否完成新手引导第一场战斗。
	 * 如果返回true，表示已经完成，则响应各模块的IRoleEventListener接口实例方法notifyRoleJoinedGame();
	 * 如果返回false
	 * ，表示未完成，则调用方notifyRoleEnterFirstNoviceGuideBattle(),响应角色进入新手引导第一场战斗。
	 * 
	 * @param role
	 * @return
	 */
	public boolean checkRoleCompleteFirstNoviceGuideBattle(KRole role);

	/**
	 * 处理角色进入新手引导第一场战斗。
	 * 
	 * @param role
	 */
	public void notifyRoleEnterFirstNoviceGuideBattle(KRole role);

	/**
	 * 处理角色完成新手引导第一场战斗。
	 * 
	 * @param role
	 */
	public void notifyRoleCompleteFirstNoviceGuideBattle(KRole role);

	/**
	 * 通知角色播放新手引导动画
	 * 
	 * @param role
	 */
	public void notifyPlayNoviceGuideAnimation(KRole role);

	/**
	 * 检测是否要进行副武器或机甲的战斗引导
	 * 
	 * @param role
	 */
	public void checkAndNotifyWeaponGuideBattle(KRole role,KGameLevelSet levelSet, int levelId);
	
	/**
	 * 检测是否要关闭机甲的战斗引导
	 * 
	 * @param role
	 */
	public void checkAndCloseMountGuideBattle(KRole role, int levelId);
	
	/**
	 * 检测并发送角色是否开放怒气槽状态
	 * @param role
	 */
	public void checkAndSendIsOpenBattlePowerSlot(KRole role);

}
