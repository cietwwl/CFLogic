package com.kola.kmp.logic.reward.login;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.koala.game.KGameMessage;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.flow.FlowManager;
import com.kola.kmp.logic.flow.FlowManager.OtherFlowTypeEnum;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailRewardDataForJobs;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.reward.KRoleReward;
import com.kola.kmp.logic.reward.login.KLoginDataManager.KAddCheckDataManager.AddCheckData;
import com.kola.kmp.logic.reward.login.KLoginDataManager.KCheckUpRewardDataManager.CheckUpRewardData;
import com.kola.kmp.logic.reward.login.KLoginDataManager.KSevenRewardDataManager.SevenRewardData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.AddCheckResult;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.ResultStructs.CommonResult_Ext;
import com.kola.kmp.logic.util.ResultStructs.RewardResult_SendMail;
import com.kola.kmp.logic.util.tips.ItemTips;
import com.kola.kmp.logic.util.tips.RewardTips;
import com.kola.kmp.logic.util.tips.ShopTips;
import com.kola.kmp.logic.util.tips.VIPTips;

public class KLoginCenter {

	public static CommonResult dealMsg_checkUp(KRole role) {
		CommonResult result = new CommonResult();

		long nowTime = System.currentTimeMillis();

		KRoleRewardLogin roleData = KLoginSonModule.instance.getRewardSon(role.getId());
		roleData.rwLock.lock();
		try {

			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date(nowTime));
			int TODAY = cal.get(Calendar.DAY_OF_MONTH);

			Set<Integer> checkUpData = roleData.getCheckUpData();
			if (checkUpData.contains(TODAY)) {
				result.tips = RewardTips.你今天已签到;
				return result;
			}
			roleData.checkUp(TODAY);
			//
			result.isSucess = true;
			result.tips = RewardTips.成功签到;
			return result;
		} finally {
			roleData.rwLock.unlock();
		}
	}

	public static AddCheckResult dealMsg_addCheckUp(KRole role, boolean isConfirm) {
		AddCheckResult result = new AddCheckResult();

		long nowTime = System.currentTimeMillis();

		KRoleRewardLogin roleData = KLoginSonModule.instance.getRewardSon(role.getId());
		roleData.rwLock.lock();
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date(nowTime));
			int MaxDay = cal.getMaximum(Calendar.DAY_OF_MONTH);
			int TODAY = cal.get(Calendar.DAY_OF_MONTH);

			Set<Integer> checkUpData = roleData.getCheckUpData();
			result.day = searchAddCheckUpDay(checkUpData, TODAY, MaxDay);
			if (result.day < 1) {
				result.tips = RewardTips.没有可以补签的日期;
				return result;
			}

			AddCheckData addCheckData = KLoginDataManager.mAddCheckDataManager.getData(result.day);

			if (!isConfirm) {
				result.isGoConfirm = true;
				result.tips = StringUtil.format(RewardTips.本次补签需要消耗x数量x货币确定要补签吗, addCheckData.price.currencyCount, addCheckData.price.currencyType.extName);
				return result;
			}

			if (KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), addCheckData.price, UsePointFunctionTypeEnum.补签到, true) < 0) {
				result.isGoMoneyUI = true;
				result.goMoneyUIType = addCheckData.price.currencyType;
				result.goMoneyUICount = addCheckData.price.currencyCount - KSupportFactory.getCurrencySupport().getMoney(role.getId(), addCheckData.price.currencyType);
				result.tips = StringUtil.format(ShopTips.x货币数量不足x, addCheckData.price.currencyType.extName, addCheckData.price.currencyCount);
				return result;
			}
			roleData.checkUp(result.day);
			//

			result.isSucess = true;
			result.tips = RewardTips.成功签到;
			return result;
		} finally {
			roleData.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 找出可以补签的日期
	 * 
	 * @param checkUpData
	 * @param TODAY
	 * @return
	 * @author CamusHuang
	 * @creation 2014-11-17 上午12:20:37
	 * </pre>
	 */
	private static int searchAddCheckUpDay(Set<Integer> checkUpData, int TODAY, int MaxDay) {
		for (int day = 1; day <= MaxDay; day++) {
			if (day >= TODAY) {
				return -1;
			}
			if (!checkUpData.contains(day)) {
				return day;
			}
		}
		return -1;
	}

	public static CommonResult_Ext dealMsg_getCheckUpReward(KRole role, int day) {

		CommonResult_Ext result = new CommonResult_Ext();

		KRoleRewardLogin roleData = KLoginSonModule.instance.getRewardSon(role.getId());
		roleData.rwLock.lock();
		try {
			
			int LastCollectDay = roleData.getLastCheckUpRewardCollectDay();
			Set<Integer> checkUpData = roleData.getCheckUpData();
			int TotalCheckUpDay = checkUpData.size();

			if (day <= LastCollectDay) {
				result.tips = RewardTips.你已领取此礼包;
				return result;
			}

			if (day > TotalCheckUpDay) {
				result.tips = RewardTips.你还不能领取此奖励;
				return result;
			}
			
			CheckUpRewardData nextData = KLoginDataManager.mCheckUpRewardDataManager.getNextData(LastCollectDay);
			if (nextData == null) {
				result.tips = RewardTips.不存在此奖励;
				return result;
			}
			
			if(nextData.ID!=day){
				result.tips = StringUtil.format(RewardTips.请先领取第x天奖励, nextData.ID);
				return result;
			}
			
			// 发奖
			if(!nextData.baseReward.sendReward(role, PresentPointTypeEnum.登录奖励).isSucess){
				result.tips = ItemTips.背包已满;
				return result;
			}
			
			BaseRewardData baseReward = nextData.baseReward.getBaseRewardData(role.getJob());
			result.addDataUprisingTips(baseReward.dataUprisingTips);
			roleData.setLastChuckUpRewardCollectDay(day);

			// 财产日志
			FlowManager.logOther(role.getId(), OtherFlowTypeEnum.登陆奖励小奖, "天数:" + day);
			//
			result.isSucess = true;
			result.tips = RewardTips.成功领取奖励;
			return result;
		} finally {
			roleData.rwLock.unlock();
		}
	}

	public static CommonResult_Ext dealMsg_getSevenReward(KRole role, int day, boolean isVip) {

		CommonResult_Ext result = new CommonResult_Ext();

		SevenRewardData data = KLoginDataManager.mSevenRewardDataManager.getData(day);
		if (data == null) {
			result.tips = RewardTips.不存在此奖励;
			return result;
		}

		int vipLv = KSupportFactory.getVIPModuleSupport().getVipLv(role.getId());

		if (isVip && data.vip > 0 && vipLv < data.vip) {
			result.isGoMoneyUI = true;
			result.goMoneyUIType = KCurrencyTypeEnum.DIAMOND;
			result.showMoneyTips = StringUtil.format(VIPTips.您没有达到VIPx是否前往充值, data.vip);
			return result;
		}

		KRoleRewardLogin roleData = KLoginSonModule.instance.getRewardSon(role.getId());
		roleData.rwLock.lock();
		try {
			int LastSevenCollectDay = roleData.getLastSevenCollectDay();
			if (day <= LastSevenCollectDay) {
				result.tips = RewardTips.你已领取此礼包;
				return result;
			}

			int totalDay = roleData.getTotalLoginDays();
			if (day > totalDay) {
				result.tips = RewardTips.你还不能领取此奖励;
				return result;
			}

			int nextDay = LastSevenCollectDay + 1;
			if (day != nextDay) {
				result.tips = StringUtil.format(RewardTips.请先领取第x天奖励, nextDay);
				return result;
			}

			roleData.setLastSevenCollectDay(nextDay);

			// 发奖
			BaseMailRewardDataForJobs mailReward = null;
			if (data.vip > 0 && vipLv >= data.vip) {
				mailReward = data.vipBaseMailReward;
			} else {
				mailReward = data.baseMailReward;
			}

			RewardResult_SendMail tempResult = mailReward.sendReward(role, PresentPointTypeEnum.登录奖励, true);
			if (!tempResult.isSucess) {
				result.tips = RewardTips.领取失败;
				return result;
			}

			if (tempResult.isSendByMail) {
				result.addUprisingTips(RewardTips.背包已满奖励通过邮件发送);
			} else {
				result.addDataUprisingTips(tempResult.getDataUprisingTips());
			}

			// 财产日志
			FlowManager.logOther(role.getId(), OtherFlowTypeEnum.登陆奖励大奖, "天数:" + day);
			//
			result.isSucess = true;
			result.tips = RewardTips.成功领取奖励;
			return result;
		} finally {
			roleData.rwLock.unlock();
		}
	}

	public static void packCheckUpRewards(KRole role, KGameMessage msg) {
		/**
		 * <pre>
		 * 签到奖励数据
		 * 角色上线时，服务器直接发送给客户端
		 * 
		 * String 日期
		 * byte 签到天数
		 * 
		 * byte　签到奖励天数档次
		 * for(０～Ｎ){
		 * 	byte 第几天
		 * 	通用奖励协议结构（参考{@link #MSG_STRUCT_COMMON_REWARD}）
		 *  byte 状态（0：已领取，1：可领取，2：未可领取）
		 * }
		 * 
		 * byte	签到记录
		 * for(０～Ｎ){
		 * 	boolean 是否已签
		 * }
		 * byte 今天是哪天
		 * </pre>
		 */

		long nowTime = System.currentTimeMillis();

		KRoleRewardLogin roleData = KLoginSonModule.instance.getRewardSon(role.getId());
		roleData.rwLock.lock();
		try {

			msg.writeUtf8String(UtilTool.DATE_FORMAT6.format(new Date(nowTime)));

			int LastCollectDay = roleData.getLastCheckUpRewardCollectDay();
			Set<Integer> checkUpData = roleData.getCheckUpData();
			int TotalCheckUpDay = checkUpData.size();
			msg.writeByte(TotalCheckUpDay);

			//
			List<CheckUpRewardData> dataList = KLoginDataManager.mCheckUpRewardDataManager.getDataCache();
			msg.writeByte(dataList.size());
			for (CheckUpRewardData data : dataList) {
				msg.writeByte(data.ID);
				data.baseReward.packMsg(role.getJob(), msg);

				byte state = 0;
				if (data.ID <= LastCollectDay) {
					state = KRoleReward.REWARD_STATUS_已领取;
				} else if (TotalCheckUpDay < data.ID) {
					state = KRoleReward.REWARD_STATUS_未可领取;
				} else {
					state = KRoleReward.REWARD_STATUS_可领取;
				}
				msg.writeByte(state);
			}

			// * byte 签到记录
			// * for(０～Ｎ){
			// * byte 第几天
			// * boolean 是否已签
			// * }
			// * byte 今天是哪天
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date(nowTime));
			int MaxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
			int TODAY = cal.get(Calendar.DAY_OF_MONTH);
			msg.writeByte(MaxDay);
			for (int day = 1; day <= MaxDay; day++) {
				msg.writeBoolean(checkUpData.contains(day));
			}
			msg.writeByte(TODAY);
		} finally {
			roleData.rwLock.unlock();
		}
	}

	public static void packSevenRewards(KRole role, KGameMessage msg) {
		/**
		 * <pre>
		 * byte　七天奖励天数档次
		 * for(０～Ｎ){
		 * 	byte 第几天
		 * 	byte vip等级双倍门槛（>0时表示存在VIP双倍，需要显示双倍按钮）
		 * 	通用奖励协议结构（参考{@link #MSG_STRUCT_COMMON_REWARD}）
		 *  byte 状态（0：已领取，1：可领取，2：未可领取）
		 * }
		 * </pre>
		 */
		KRoleRewardLogin roleData = KLoginSonModule.instance.getRewardSon(role.getId());
		roleData.rwLock.lock();
		try {
			int totalDay = roleData.getTotalLoginDays();
			{
				int LastCollectDay = roleData.getLastSevenCollectDay();

				Map<Integer, SevenRewardData> dataMap = KLoginDataManager.mSevenRewardDataManager.getDataCache();
				msg.writeByte(dataMap.size());

				for (SevenRewardData data : dataMap.values()) {
					msg.writeByte(data.ID);
					msg.writeByte(data.vip);

					data.baseMailReward.packMsg(role.getJob(), msg);

					byte state = 0;
					if (data.ID <= LastCollectDay) {
						state = KRoleReward.REWARD_STATUS_已领取;
					} else if (totalDay < data.ID) {
						state = KRoleReward.REWARD_STATUS_未可领取;
					} else {
						state = KRoleReward.REWARD_STATUS_可领取;
					}
					msg.writeByte(state);
				}
			}

		} finally {
			roleData.rwLock.unlock();
		}
	}

	public static void packCheckUpRewardStates(KRole role, KGameMessage msg) {
		/**
		 * <pre>
		 * 签到奖励数据
		 * 角色上线时，服务器直接发送给客户端
		 * 
		 * byte 签到天数
		 * 
		 * byte　签到奖励天数档次
		 * for(０～Ｎ){
		 * 	byte 第几天
		 *  byte 状态（0：已领取，1：可领取，2：未可领取）
		 * }
		 * </pre>
		 */
		KRoleRewardLogin roleData = KLoginSonModule.instance.getRewardSon(role.getId());
		roleData.rwLock.lock();
		try {

			int LastCollectDay = roleData.getLastCheckUpRewardCollectDay();
			Set<Integer> checkUpData = roleData.getCheckUpData();
			int TotalCheckUpDay = checkUpData.size();
			msg.writeByte(TotalCheckUpDay);

			//
			List<CheckUpRewardData> dataList = KLoginDataManager.mCheckUpRewardDataManager.getDataCache();
			msg.writeByte(dataList.size());
			for (CheckUpRewardData data : dataList) {
				msg.writeByte(data.ID);

				byte state = 0;
				if (data.ID <= LastCollectDay) {
					state = KRoleReward.REWARD_STATUS_已领取;
				} else if (TotalCheckUpDay < data.ID) {
					state = KRoleReward.REWARD_STATUS_未可领取;
				} else {
					state = KRoleReward.REWARD_STATUS_可领取;
				}
				msg.writeByte(state);
			}
		} finally {
			roleData.rwLock.unlock();
		}
	}
}
