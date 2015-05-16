package com.kola.kmp.logic.support;

import com.kola.kmp.logic.other.KDataStructs.EquiSetStruct;
import com.kola.kmp.logic.other.KLimitTimeProduceActivityTypeEnum;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.TimeLimitActivityDataManager.TimeLimieProduceActivity;
import com.kola.kmp.logic.role.KRole;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-9-1 下午2:58:47
 * </pre>
 */
public interface ExcitingRewardSupport {

	/**
	 * <pre>
	 * 充值通知
	 * 
	 * @param roleId
	 * @param chargeIngot
	 * @param isFirstCharge
	 * @author CamusHuang
	 * @creation 2014-11-17 上午10:53:37
	 * </pre>
	 */
	public void notifyCharge(long roleId, int chargeIngot, boolean isFirstCharge);
	
	/**
	 * <pre>
	 * 角色领取经验任务中的星星奖励时通知
	 * 
	 * @param roleId
	 * @param expTaskRewardLv 星星奖励的档次（从1开始计算）
	 * @author CamusHuang
	 * @creation 2014-12-13 下午4:08:52
	 * </pre>
	 */
	public void notifyExpTaskLvRewardCollected(long roleId, int expTaskRewardLv);
	
	/**
	 * <pre>
	 * 角色领取活跃度任务中的星星奖励时通知
	 * 
	 * @param roleId
	 * @param vitalityTaskRewardLv 活跃度奖励的档次（从1开始计算）
	 * @author CamusHuang
	 * @creation 2014-12-13 下午4:08:55
	 * </pre>
	 */
	public void notifyVitalityTaskLvRewardCollected(long roleId, int vitalityTaskRewardLv);

	/**
	 * <pre>
	 * 消费通知
	 * 
	 * @param roleId
	 * @param payIngot
	 * @author CamusHuang
	 * @creation 2013-7-6 下午5:16:47
	 * </pre>
	 */
	public void notifyPayDiamond(long roleId, int payIngot);

	/**
	 * <pre>
	 * 角色消耗体力后通知
	 * 
	 * @param roleId
	 * @param usePhyPow
	 * @author CamusHuang
	 * @creation 2014-9-4 下午4:24:29
	 * </pre>
	 */
	public void notifyUsePhyPow(long roleId, int usePhyPow);

	/**
	 * <pre>
	 * 
	 * 
	 * @param roleId
	 * @param setLv
	 * @author CamusHuang
	 * @creation 2014-9-2 下午5:12:47
	 * </pre>
	 */
	public void notifyEquiSetChange(long roleId, EquiSetStruct setLv);
	
	/**
	 * <pre>
	 * 角色战力变化时通知
	 * 
	 * @param role
	 * @author CamusHuang
	 * @creation 2014-9-4 下午4:24:15
	 * </pre>
	 */
	public void notifyRoleBattlePowChange(KRole role);
	
	/**
	 * <pre>
	 * 角色机甲进阶
	 * 
	 * @param role
	 * @param nowLv
	 * @author CamusHuang
	 * @creation 2014-10-16 下午12:30:00
	 * </pre>
	 */
	public void notifyMountLevelUp(KRole role, int oldLv, int nowLv);


	/**
	 * <pre>
	 * 重新加载精彩活动数据
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2013-7-6 下午6:21:40
	 * </pre>
	 */
	public String reloadExcitionData(boolean isPushExciting);
	
	/**
	 * 根据类型获取限时产出（限时商店打折）的活动数据
	 * @param activityType
	 * @return
	 */
	public TimeLimieProduceActivity getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum activityType);
}
