package com.kola.kmp.logic.level;

import java.util.List;

import com.kola.kmp.logic.activity.goldact.KBarrelBattlefield;
import com.kola.kmp.logic.level.gamestory.Animation;
import com.kola.kmp.logic.level.petcopy.KPetCopyBattlefield;
import com.kola.kmp.logic.level.tower.KTowerBattlefield;
import com.kola.kmp.logic.role.KRole;

public interface FightEventListener {
	/**
	 * <pre>
	 * 响应触发角色进入战场(普通关卡战场和护送任务战场)
	 * @param role
	 * @param battlefield
	 * @param animation   战斗场景中触发的剧情动画数据，传入的Animation对象的变量
	 *                        {@link Animation#animationStartType}的值一定为
	 *                        {@link Animation#ANIMATION_START_TYPE_LEVEL_START}或者
	 *                        {@link Animation#ANIMATION_START_TYPE_LEVEL_END}
	 *                    如果 Animation对象为null，表示本次战斗没有触发的剧情动画数据
	 * </pre>
	 */
	public void notifyBattle(KRole role, List<KGameBattlefield> battlefield,
			List<Animation> animationList);

	/**
	 * <pre>
	 * 响应触发角色进入好友副本战场
	 * @param role
	 * @param friendRoleId 好友角色ID，当Id为-1时，表示没有好友参战
	 * @param battlefield
	 * </pre>
	 */
	public void notifyFriendTowerBattle(KRole role, long friendRoleId,
			KTowerBattlefield battlefield);

	/**
	 * 响应触发角色进入产金活动战场
	 * 
	 * @param role
	 * @param battlefield
	 *            ，产金活动战场数据
	 */
	public void notifyGoldActivityBattle(KRole role,
			KBarrelBattlefield battlefield);

	/**
	 * <pre>
	 * 响应触发角色进入新产金活动战场
	 * 
	 * @param role
	 * @param battlefield
	 *            ，产金活动战场数据
	 * @param glodBaseValue，角色金币基数，战斗中显示获得金币的公式：我的伤害 / BOSS总血量 * 金币基数 = 可获得金币
	 * @param battleTime，战斗时间
	 * </pre>
	 */
	public void notifyNewGoldActivityBattle(KRole role,
			KGameBattlefield battlefield, int glodBaseValue,long battleTime);
	
	/**
	 * 响应触发角色进入随从副本战场
	 * @param role
	 * @param battlefield，随从副本战场
	 */
	public void notifyPetCopyBattle(KRole role,KPetCopyBattlefield battlefield);

	/**
	 * 通知战场结束，以及战斗结果
	 * 
	 * @param role
	 * @param result
	 */
	public void notifyBattleFinished(KRole role, FightResult result);

	/**
	 * 通知关卡完成
	 * 
	 * @param role
	 * @param gamelevel
	 */
	public void notifyGameLevelCompleted(KRole role, KLevelTemplate gamelevel,
			FightResult result);

	/**
	 * 通知战斗结算结束，释放战场数据
	 * 
	 * @param role
	 */
	public void notifyBattleRewardFinished(KRole role);
}
