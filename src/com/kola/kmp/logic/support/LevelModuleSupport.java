package com.kola.kmp.logic.support;

import java.util.Map;

import com.kola.kmp.logic.level.FightResult;
import com.kola.kmp.logic.level.KGameBattlefield;
import com.kola.kmp.logic.level.KLevelTemplate;
import com.kola.kmp.logic.level.TowerFightResult;
import com.kola.kmp.logic.level.petcopy.KPetCopyBattlefield;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.role.KRole;

public interface LevelModuleSupport {

	/**
	 * 根据关卡ID，获取普通关卡数据模版
	 * 
	 * @param levelId
	 * @return
	 */
	public KLevelTemplate getNormalGameLevelTemplate(
			KGameLevelTypeEnum levelType, int levelId);

	/**
	 * 根据关卡ID，获取普通关卡所属章节名称
	 * 
	 * @param levelId
	 * @return
	 */
	public String getScenarioNameByLevelId(int levelId);

	/**
	 * 通知关卡剧本系统完成一次战斗
	 * 
	 * @param role
	 * @param result
	 */
	public void notifyCompleteFight(KRole role, FightResult result);

	/**
	 * 根据关卡ID判断角色是否完成对应的关卡
	 * 
	 * @param roleId
	 *            角色Id
	 * @param levelId
	 *            关卡Id
	 * @return
	 */
	public boolean checkGameLevelIsCompleted(long roleId, int levelId);

	/**
	 * 通知关卡剧本系统完成一次塔防战斗
	 * 
	 * @param role
	 * @param result
	 * @return
	 */
	public boolean notifyCompleteTowerFight(KRole role, TowerFightResult result);

	/**
	 * 
	 * 获取世界boss的关卡数据
	 * 
	 * @param levelId
	 * @return
	 */
	public KGameBattlefield getWorldBossBattlefield(int levelId);

	/**
	 * 获取军团战的PVE战场
	 * 
	 * @param levelId
	 * @return
	 */
	public KGameBattlefield getFamilyWarBattlefield(int levelId);

	/**
	 * 通知角色进入军团战PVE战场
	 * 
	 * @param role
	 * @param pveBattle
	 * @return
	 */
	public KActionResult startFamilyWarPVEBattle(KRole role,
			KGameBattlefield pveBattle);

	/**
	 * 获取新手引导战场
	 * 
	 * @param levelId
	 * @return
	 */
	public KGameBattlefield getNoviceGuideBattlefield();

	/**
	 * 次日凌晨检测并重置副本数据
	 */
	public void checkAndResetCopyData(KRole role);

	/**
	 * 次日凌晨检测并重置好友副本数据
	 */
	public void checkAndResetFriendCopyData(KRole role);

	/**
	 * 检测角色升级时的关卡状态更新
	 * 
	 * @param role
	 */
	public void checkAndUpdateGameLevelOpenStateWhileRoleLvUp(KRole role);
	
	/**
	 * 服务器主动通知客户端进行关卡寻路
	 * @param role
	 * @param levelId
	 */
	public void notifyClientLevelSearchRoad(KRole role,int levelId);
}
