package com.kola.kmp.logic.reward.exciting;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.kola.kmp.logic.other.KDataStructs.EquiSetStruct;
import com.kola.kmp.logic.other.KLimitTimeProduceActivityTypeEnum;
import com.kola.kmp.logic.reward.exciting.ExcitingTaskManager.ExcitingActivityTaskDataManager;
import com.kola.kmp.logic.reward.exciting.ExcitingTaskManager.TimeLimitActivityTaskDataManager;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.TimeLimitActivityDataManager.TimeLimieProduceActivity;
import com.kola.kmp.logic.reward.exciting.message.KPushExcitingActivityDataMsg;
import com.kola.kmp.logic.reward.exciting.message.KSynDataMsg;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.ExcitingRewardSupport;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2013-7-5 下午9:00:40
 * </pre>
 */
public class KExcitingSupportImpl implements ExcitingRewardSupport {
	private static Logger _LOGGER = KGameLogger.getLogger(KExcitingSupportImpl.class);

	public void notifyRoleBattlePowChange(KRole role) {
		KRoleExciting roleData = KExcitingExtCACreator.getRoleExciting(role.getId());
		if (roleData.notifyBattlePowChange(role.getBattlePower())) {
			KSynDataMsg.sendMsgForStatus(role.getId());
		}
	}

	public void notifyMountLevelUp(KRole role, int oldLv, int nowLv) {
		KRoleExciting roleData = KExcitingExtCACreator.getRoleExciting(role.getId());
		if (roleData.notifyMountLevelUp(oldLv, nowLv)) {
			KSynDataMsg.sendMsgForStatus(role.getId());
		}
	}

	public void notifyEquiSetChange(long roleId, EquiSetStruct setLv) {
		KRoleExciting roleData = KExcitingExtCACreator.getRoleExciting(roleId);
		if (roleData.notifyEquiSetChange(setLv)) {
			KSynDataMsg.sendMsgForStatus(roleId);
		}
	}

	@Override
	public void notifyCharge(long roleId, int charge, boolean isFirstCharge) {
		boolean isSendReward = false;

		// 先处理单次充值自动发送
		if (KExcitingCenter.dealChargeInOneTime(roleId, charge, isFirstCharge)) {
			isSendReward = true;
		}

		// 再处理累计充值
		KRoleExciting roleData = KExcitingExtCACreator.getRoleExciting(roleId);
		if (roleData.addTotalCharge(charge, isFirstCharge)) {
			isSendReward = true;
		}

		if (isSendReward) {
			KSynDataMsg.sendMsgForStatus(roleId);
		}
	}

	@Override
	public void notifyExpTaskLvRewardCollected(long roleId, int expTaskRewardLv) {
		if (KExcitingCenter.dealExpTaskRewardLv(roleId, expTaskRewardLv)) {
			KSynDataMsg.sendMsgForStatus(roleId);
		}
	}

	@Override
	public void notifyVitalityTaskLvRewardCollected(long roleId, int vitalityTaskRewardLv) {
		if (KExcitingCenter.dealVitalityTaskRewardLv(roleId, vitalityTaskRewardLv)) {
			KSynDataMsg.sendMsgForStatus(roleId);
		}
	}

	@Override
	public void notifyPayDiamond(long roleId, int payDiamond) {
		KRoleExciting roleData = KExcitingExtCACreator.getRoleExciting(roleId);
		if (roleData.addTotalPay(payDiamond)) {
			KSynDataMsg.sendMsgForStatus(roleId);
		}
	}

	@Override
	public void notifyUsePhyPow(long roleId, int usePhyPow) {
		KRoleExciting roleData = KExcitingExtCACreator.getRoleExciting(roleId);
		if (roleData.addTotalUsePhyPow(usePhyPow)) {
			KSynDataMsg.sendMsgForStatus(roleId);
		}
	}

	public String reloadExcitionData(boolean isPushExciting) {
		try {
			KExcitingDataManager.reloadData(true);
		} catch (Exception e) {
			return e.getMessage();
		}

		// 启动所有活动的起始和结束任务
		ExcitingActivityTaskDataManager.restartActivityTast();
		// 启动限时奖励活动起始和结束任务
		TimeLimitActivityTaskDataManager.restartAllActivityTast();
		// 启动排行榜排名奖励自动发奖任务
		ExcitingTaskManager.restartAutoCollectedRankRewardTask();
		//
		if (isPushExciting) {
			KPushExcitingActivityDataMsg.sendMsgToAllOnlineRole();
		}
		return null;
	}

	@Override
	public TimeLimieProduceActivity getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum activityType) {
		return KExcitingDataManager.mTimeLimitActivityDataManager.getTimeLimieProduceActivityData(activityType);
	}
}
