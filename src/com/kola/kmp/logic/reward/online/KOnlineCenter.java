package com.kola.kmp.logic.reward.online;

import javax.management.timer.Timer;

import com.koala.game.KGameMessage;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.reward.online.KOnlineDataManager.KOnlineRewardDataManager.OnlineRewardData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.util.ResultStructs.RewardResult_Online;
import com.kola.kmp.logic.util.tips.ItemTips;
import com.kola.kmp.logic.util.tips.RewardTips;

public class KOnlineCenter {

	public static RewardResult_Online dealMsg_getReward(KRole role) {

		RewardResult_Online result = new RewardResult_Online();

		long nowTime = System.currentTimeMillis();
		boolean isFirstDay = !UtilTool.isBetweenDay(role.getCreateTime(), nowTime);

		KRoleRewardOnline roleData = KOnlineSonModule.instance.getRewardSon(role.getId());
		roleData.rwLock.lock();
		try {

			int lastRewardId = roleData.getLastRewardId();
			int nextRewardId = lastRewardId + 1;
			OnlineRewardData nextRewardData = null;
			if (isFirstDay) {
				nextRewardData = KOnlineDataManager.mFirstDayRewardDataManager.getData(nextRewardId);
			} else {
				nextRewardData = KOnlineDataManager.mOtherDayRewardDataManager.getData(nextRewardId);
			}
			if (nextRewardData == null) {
				result.isSync = true;
				result.tips = RewardTips.不存在此奖励;
				return result;
			}

			int onlineTime = roleData.countOnlineTime(nowTime);
			if (nextRewardData.timeMills - onlineTime > KOnlineDataManager.AllowanceOnlineTime) {
				// 在线时长不足
				result.isSync = true;
				result.tips = RewardTips.你还不能领取此礼包;
				return result;
			}

			// 发奖
			if (!nextRewardData.baseReward.sendReward(role, PresentPointTypeEnum.在线奖励)) {
				result.tips = ItemTips.背包已满;
				return result;
			}
			
			// 在线时长充足
			roleData.setLastRewardId(nextRewardData.id);

			//
			result.isSucess = true;
			result.tips = RewardTips.成功领取奖励;
			result.addDataUprisingTips(nextRewardData.baseReward.dataUprisingTips);
			{
				int nextnextRewardId = nextRewardData.id + 1;
				OnlineRewardData nextnextRewardData = null;
				if (isFirstDay) {
					nextnextRewardData = KOnlineDataManager.mFirstDayRewardDataManager.getData(nextnextRewardId);
				} else {
					nextnextRewardData = KOnlineDataManager.mOtherDayRewardDataManager.getData(nextnextRewardId);
				}
				if(nextnextRewardData == null){
					result.addDataUprisingTips(RewardTips.今天的在线奖励全部领取完毕);
				}
			}
			
			result.isSync = true;
			return result;
		} finally {
			roleData.rwLock.unlock();
		}
	}

	public static void packLogo(KRole role, KGameMessage msg) {
		/**
		 * <pre>
		 * 在线奖励图标
		 * 登陆时或有需要时推送
		 * 
		 * boolean 是否显示在线奖励LOGO
		 * if(true){
		 * 	int 剩余多少秒可以领取(0表示立即可以领取)
		 * }
		 * </pre>
		 */

		long nowTime = System.currentTimeMillis();
		boolean isFirstDay = !UtilTool.isBetweenDay(role.getCreateTime(), nowTime);

		KRoleRewardOnline roleData = KOnlineSonModule.instance.getRewardSon(role.getId());
		roleData.rwLock.lock();
		try {

			int lastRewardId = roleData.getLastRewardId();
			int nextRewardId = lastRewardId + 1;
			OnlineRewardData nextRewardData = null;
			if (isFirstDay) {
				nextRewardData = KOnlineDataManager.mFirstDayRewardDataManager.getData(nextRewardId);
			} else {
				nextRewardData = KOnlineDataManager.mOtherDayRewardDataManager.getData(nextRewardId);
			}
			if (nextRewardData == null) {
				msg.writeBoolean(false);
			} else {
				msg.writeBoolean(true);
				long releaseTime = nextRewardData.timeMills - roleData.countOnlineTime(nowTime);
				if (releaseTime > Timer.ONE_SECOND) {
					// 在线时长不足
					msg.writeInt((int)(releaseTime/Timer.ONE_SECOND));
				} else {
					// 在线时长充足
					msg.writeInt(0);
				}
				//打包通用奖励
				nextRewardData.baseReward.packMsg(msg);
			}

		} finally {
			roleData.rwLock.unlock();
		}
	}
}
