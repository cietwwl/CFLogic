package com.kola.kmp.logic.reward;

import java.util.List;

import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.PhyPowerRewardStruct;
import com.kola.kmp.logic.reward.KRewardDataStruct.DialyMailData;
import com.kola.kmp.logic.reward.KRewardDataStruct.KAJIRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.PhyPowerRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.ShutdownRewardData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.tips.RewardTips;

public class KRewardLogic {

	static void sendDialyMail(KRole role) {
		KRoleReward roleReward = KRewardRoleExtCACreator.getRoleReward(role.getId());

		roleReward.rwLock.lock();
		try {

			if (roleReward.hasSendedDayMail()) {
				// 今天已发送
				return;
			}

			// 今天未发送
			boolean isSend = false;
			
			long nowTime = System.currentTimeMillis();
			List<DialyMailData> list = KRewardDataManager.mDialyMailDataManager.getDatas(role.getLevel());
			for (DialyMailData dayMail : list) {
				if (!dayMail.time.isInEffectTime(nowTime)) {
					continue;
				}

				KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), dayMail.mail, PresentPointTypeEnum.每日邮件);
				isSend = true;
			}
			
			if(isSend){
				roleReward.notifySendedDayMail();
			}
		} finally {
			roleReward.rwLock.unlock();
		}
	}

	static void sendShutdownReward(KRole role) {
		KRoleReward roleReward = KRewardRoleExtCACreator.getRoleReward(role.getId());

		roleReward.rwLock.lock();
		try {
			if (KRewardModule.ServerStartTimeMillis < role.getCreateTime()) {
				// 开服后创建的角色
				return;
			}

			if (KRewardModule.ServerStartTimeMillis < roleReward.getSendedShutdownRewardTime()) {
				// 已领取
				return;
			}

			// 今天未发送
			List<ShutdownRewardData> list = KRewardDataManager.mShutdownRewardManager.getDatas(role.getLevel());
			for (ShutdownRewardData dayMail : list) {
				KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), dayMail.mail, PresentPointTypeEnum.维护补偿奖励);
			}
			roleReward.notifySendedShutdownReward(System.currentTimeMillis());
		} finally {
			roleReward.rwLock.unlock();
		}
	}
	
	static void sendKAJIReward(KRole role) {
		KRoleReward roleReward = KRewardRoleExtCACreator.getRoleReward(role.getId());

		roleReward.rwLock.lock();
		try {

			long nowTime = System.currentTimeMillis();
			long lastTime = roleReward.getSendedKAJIRewardTime();
			long createTime = role.getCreateTime();
			//
			boolean isGetReward = false;
			List<KAJIRewardData> list = KRewardDataManager.mKAJIRewardManager.getDatas(role.getLevel());
			for (KAJIRewardData data : list) {
				if (nowTime < data.effectStartTime || data.effectEndTime < nowTime) {
					// 奖励未生效
					continue;
				}
				if (createTime < data.createStartTime || data.createEndTime < createTime) {
					// 不是指定时间范围创建的角色
					continue;
				}

				if (data.effectStartTime < lastTime) {
					// 已经领取过
					continue;
				}

				KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), data.mail, PresentPointTypeEnum.卡机补偿);
				isGetReward = true;
			}
			if(isGetReward){
				roleReward.notifySendedKAJIReward(nowTime);
			}
		} finally {
			roleReward.rwLock.unlock();
		}
	}	

	/**
	 * <pre>
	 * 一天之内，只有最后一次体力领取后或者最后一次时间段过期后才不显示ICON，其它时间显示ICON
	 * 
	 * @param role
	 * @return
	 * @author CamusHuang
	 * @creation 2015-1-29 下午3:59:39
	 * </pre>
	 */
	public static boolean isShowPhyPowerIcon(KRole role) {
		long nowTime = System.currentTimeMillis();
		long todayStartTime = UtilTool.getTodayStart().getTimeInMillis();
		//
		return isShowPhyPowerIcon(role, nowTime, todayStartTime);
	}
	
	/**
	 * <pre>
	 * 一天之内，只有最后一次体力领取后或者最后一次时间段过期后才不显示ICON，其它时间显示ICON
	 * 
	 * @param role
	 * @param nowTime
	 * @param todayStartTime
	 * @return
	 * @author CamusHuang
	 * @creation 2015-1-29 下午3:58:20
	 * </pre>
	 */
	static boolean isShowPhyPowerIcon(KRole role, long nowTime, long todayStartTime) {
		PhyPowerRewardData lastRewardData = KRewardDataManager.mPhyPowerDataManager.getLastStruct();
		if (lastRewardData == null) {
			return false;
		}
		if(nowTime >= todayStartTime+lastRewardData.timeInterval.getEndTime()){
			return false;
		}
		
		KRoleReward roleReward = KRewardRoleExtCACreator.getRoleReward(role.getId());
		roleReward.rwLock.lock();
		try {
			long lastTime = roleReward.getLastCollectPhyPowerTime();
			if (lastTime < todayStartTime + lastRewardData.timeInterval.getBeginTime()) {
				return true;
			}
			return false;
		} finally {
			roleReward.rwLock.unlock();
		}
	}

	public static CommonResult dealMsg_getPhyPower(KRole role) {
		CommonResult result = new CommonResult();
		//
		long nowTime = System.currentTimeMillis();
		long todayStartTime = UtilTool.getNextNDaysStart(nowTime, 0).getTimeInMillis();

		PhyPowerRewardStruct rewardData = KRewardDataManager.mPhyPowerDataManager.getEffectTimeStruct(nowTime-todayStartTime);
		if(rewardData==null){
			result.tips = RewardTips.此奖励尚未开放;
			return result;
		}
		if (rewardData.nowTime == null) {
			if (rewardData.nextTime == null) {
				result.tips = RewardTips.此奖励尚未开放;
			} else {
				result.tips = StringUtil.format(RewardTips.请x至x期间领取体力, rewardData.nextTime.timeInterval.getBeginTimeStr(), rewardData.nextTime.timeInterval.getEndTimeStr());
			}
			return result;
		}

		KRoleReward roleReward = KRewardRoleExtCACreator.getRoleReward(role.getId());
		roleReward.rwLock.lock();
		try {
			long lastTime = roleReward.getLastCollectPhyPowerTime();
			if (lastTime >= todayStartTime + rewardData.nowTime.timeInterval.getBeginTime()) {
				if (rewardData.nextTime == null) {
					result.tips = RewardTips.此奖励尚未开放;
				} else {
					result.tips = StringUtil.format(RewardTips.请x至x期间领取体力, rewardData.nextTime.timeInterval.getBeginTimeStr(), rewardData.nextTime.timeInterval.getEndTimeStr());
				}
				return result;
			}
			
			//
			KSupportFactory.getRoleModuleSupport().addPhyPower(role.getId(), rewardData.nowTime.PhysicalCount, true, "定时领取");
			roleReward.setLastCollectPhyPowerTime(nowTime);
			//
			result.tips = StringUtil.format(RewardTips.恭喜您领取了x体力, rewardData.nowTime.PhysicalCount);
			return result;
		} finally {
			roleReward.rwLock.unlock();
		}
	}
}
