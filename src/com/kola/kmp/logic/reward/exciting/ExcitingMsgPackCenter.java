package com.kola.kmp.logic.reward.exciting;

import java.util.List;

import com.koala.game.KGameMessage;
import com.koala.game.util.StringUtil;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.ExcitingDataManager.ExcitionActivity;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.ExcitingRuleManager.RewardRule;
import com.kola.kmp.logic.util.tips.RewardTips;

/**
 * <pre>
 * 由于在打包模块消息的过程中，涉及到访问及复杂的逻辑结构
 * 因此设立此类，用于打包消息
 * 
 * @author CamusHuang
 * @creation 2013-1-5 下午12:03:37
 * </pre>
 */
public class ExcitingMsgPackCenter {

	/**
	 * <pre>
	 * 角色上线时时，服务器直接发送给客户端
	 * 精彩活动
	 * 
	 * byte 活动数量
	 * {
	 * 	int 活动ID
	 * 	String 标签名称
	 *  String 活动名称
	 *  String 开始时间
	 *  String 结束时间
	 *  String 活动内容
	 *  int 界面链接ID
	 *  ------以下是奖励项
	 *  byte 奖励项数量
	 *  {
	 *  	int 奖励项ID
	 *  	byte 状态（0未达成，1可领取，2邮件已发送奖励）
	 *  	String 奖励项说明
	 *  	------以下是奖励内容
	 *  	byte 货币种类
	 *  	{
	 *  		byte 货币类型
	 *  		int 货币数量
	 *  	}
	 *  	int 修为值
	 *  	int 体力值
	 * 		byte 道具数量
	 *  	for (道具数量) {
	 * 			道具（参考{@link com.koala.kgame.protocol.item.KItemProtocol#MSG_STRUCT_ITEM_DETAILS}）
	 *  	}
	 *  	----------
	 *  	int 封神录抽奖次数
	 * 	byte 返现元宝百分比
	 *  }
	 * }
	 * String 统一显示网址（""表示无需显示）
	 * String 统一显示邮箱（""表示无需显示）
	 * </pre>
	 */
	public static KGameMessage packExcitingActivity(KGameMessage msg, long roleId) {
		List<ExcitionActivity> list = KExcitingDataManager.mExcitingDataManager.getDatas();

		KRoleExciting excitingReward = KExcitingExtCACreator.getRoleExciting(roleId);
		if (excitingReward == null) {
			return null;
		}
		long nowTime = System.currentTimeMillis();

		int writeIndex = msg.writerIndex();
		msg.writeByte(list.size());
		int count = 0;
		for (ExcitionActivity activity : list) {
			if (!activity.caTime.isInEffectTime(nowTime)) {
				continue;
			}

			if (activity.buyPrice != null) {
				if (excitingReward.hasCollectedAllReward(activity)) {
					// 投资计划：已领取完全部奖励
					continue;
				}
			}

			packExcitingActivity(msg, excitingReward, activity);
			count++;
		}
		msg.setByte(writeIndex, count);
		//
		msg.writeUtf8String(KExcitingDataManager.mExcitingDataManager.统一显示的网址标题);
		msg.writeUtf8String(KExcitingDataManager.mExcitingDataManager.统一显示的网址);
		msg.writeUtf8String(KExcitingDataManager.mExcitingDataManager.统一显示的邮箱标题);
		msg.writeUtf8String(KExcitingDataManager.mExcitingDataManager.统一显示的邮箱);

		return msg;
	}

	private static void packExcitingActivity(KGameMessage msg, KRoleExciting excitingReward, ExcitionActivity activity) {
		msg.writeInt(activity.id);
		msg.writeUtf8String(activity.label);
		msg.writeUtf8String(activity.title);
		msg.writeUtf8String(activity.content);
		msg.writeInt(activity.uiLink);
		{
			// 支付
			msg.writeBoolean(activity.buyPrice != null);
			if (activity.buyPrice != null) {
				msg.writeByte(activity.buyPrice.currencyType.sign);
				msg.writeLong(activity.buyPrice.currencyCount);
				msg.writeBoolean(excitingReward.isPayed(activity));
			}
		}

		msg.writeByte(activity.ruleMap.size());
		for (RewardRule rule : activity.ruleMap.values()) {
			msg.writeInt(rule.ruleId);
			
			packRule(msg, excitingReward, activity, rule);
			//
			rule.rewardMail.packMsg(msg);
			//
			msg.writeShort(rule.presentIngotRate);
		}
	}
	
	private static void packRule(KGameMessage msg, KRoleExciting excitingReward, ExcitionActivity activity, RewardRule rule) {
		// 如果是全区限量 ，则判断全服数量
		ExcitiongStatusEnum status = null;
		StringBuffer descBuf = null;
		if (rule.maxTimeForWorld > 0 || rule.chargeInOneTimeMaxTimeForRole > 0) {
			descBuf = new StringBuffer();
			descBuf.append(rule.desc);

			// 如果是全区限量 ，则判断全服数量
			int worldReleaseTime = -1;
			if (rule.maxTimeForWorld > 0) {
				worldReleaseTime = Math.max(0, rule.maxTimeForWorld - ExcitingGlobalDataImpl.instance.getCount(activity.id, rule.ruleId));
			}

			// 如果个人次数限量，则判断个人次数
			int roleRleaseTime = excitingReward.getRoleReleaseTime(activity, rule);

			if (worldReleaseTime == -1) {
				if (roleRleaseTime < 1) {
					// 个人发完了
					status = ExcitiongStatusEnum.EMPTY;
				}
				descBuf.append(StringUtil.format(RewardTips.可领奖次数x分之x, (rule.chargeInOneTimeMaxTimeForRole - roleRleaseTime), rule.chargeInOneTimeMaxTimeForRole));
			} else if (roleRleaseTime == -1) {
				if (worldReleaseTime < 1) {
					// 全服发完了
					status = ExcitiongStatusEnum.EMPTY;
				}
				descBuf.append(StringUtil.format(RewardTips.全服剩余x份, worldReleaseTime));
			} else if (roleRleaseTime <= worldReleaseTime) {
				if (roleRleaseTime < 1) {
					// 个人发完了
					status = ExcitiongStatusEnum.EMPTY;
				}
				descBuf.append(StringUtil.format(RewardTips.可领奖次数x分之x, (rule.chargeInOneTimeMaxTimeForRole - roleRleaseTime), rule.chargeInOneTimeMaxTimeForRole));
			} else {
				if (worldReleaseTime < 1) {
					// 全服发完了
					status = ExcitiongStatusEnum.EMPTY;
				}
				descBuf.append(StringUtil.format(RewardTips.全服剩余x份, worldReleaseTime));
			}
		}

		if (status == null) {
			status = KExcitingCenter.checkRewardStatus(excitingReward, activity, rule);
		}

		msg.writeBoolean(status.isCanPress);
		msg.writeUtf8String(status.name);

		msg.writeUtf8String(descBuf == null ? rule.desc : descBuf.toString());
	}

	public static KGameMessage packExcitingActivity(KGameMessage msg, long roleId, ExcitionActivity activity) {
		KRoleExciting excitingReward = KExcitingExtCACreator.getRoleExciting(roleId);
		if (excitingReward == null) {
			return null;
		}
		packExcitingActivity(msg, excitingReward, activity);
		return msg;
	}

	public static KGameMessage packExcitingActivityStatus(KGameMessage msg, long roleId) {
		List<ExcitionActivity> list = KExcitingDataManager.mExcitingDataManager.getDatas();

		KRoleExciting roleExcitingData = KExcitingExtCACreator.getRoleExciting(roleId);
		if (roleExcitingData == null) {
			return null;
		}
		long nowTime = System.currentTimeMillis();

		int writeIndex = msg.writerIndex();
		msg.writeByte(list.size());
		int count = 0;
		for (ExcitionActivity activity : list) {
			if (!activity.caTime.isInEffectTime(nowTime)) {
				continue;
			}

			if (activity.buyPrice != null) {
				if (roleExcitingData.hasCollectedAllReward(activity)) {
					// 投资计划：已领取完全部奖励
					continue;
				}
			}

			msg.writeInt(activity.id);
			// 支付
			msg.writeBoolean(roleExcitingData.isPayed(activity));

			msg.writeByte(activity.ruleMap.size());
			for (RewardRule rule : activity.ruleMap.values()) {
				msg.writeInt(rule.ruleId);
				packRule(msg, roleExcitingData, activity, rule);
			}
			count++;
		}
		msg.setByte(writeIndex, count);

		return msg;
	}

	// /**
	// * <pre>
	// * 角色上线时、跨天时、升级时，服务器直接发送给客户端
	// * 功能奖励数据
	// *
	// * byte 功能类型数量
	// * {
	// * byte funType 功能类型
	// * String funName 功能名称
	// * int icon;// 功能图标
	// * byte 货币种类数量
	// * for(货币种类数量){
	// * byte 货币类型
	// * int 货币数量
	// * }
	// * byte 道具数量
	// * for (道具数量) {
	// * String itemName ;// 物品名称
	// * int itemCount ;// 物品数量
	// * }
	// * int vitalityValue ;// 获得活跃度---客户端需要据此增加活跃度值并判断是否开放活跃度奖励
	// * short n已完成次数
	// * short m总次数
	// * boolean 是否已领奖（当n==m时，此值有效）
	// * }
	// * </pre>
	// */
	// public static void packPushFunRewardData(long roleId, KGameMessage msg) {
	// ExcitingData2 vdata = ExcitingManager.getVitalityData(roleId);
	//
	// List<FunTypeData_Type> dataList =
	// ExcitingDataManager.mFunTypeRewardDataManager.getAllDatas();
	//
	// vdata.rwLock.lock();
	// try {
	// int msgindex = msg.writerIndex();
	// msg.writeByte(0);
	// int count = 0;
	// RecordData recordData;
	// for (FunTypeData_Type dataType : dataList) {
	// recordData = vdata.getRecordData(dataType.funType);
	// if (recordData == null) {
	// continue;
	// }
	// msg.writeByte(dataType.funType.sign);
	// msg.writeUtf8String(dataType.funType.getName());
	// msg.writeInt(dataType.funType.getIcon());
	//
	// if (recordData.funTypeData.money != null) {
	// msg.writeByte(1);
	// msg.writeByte(recordData.funTypeData.money.currencyType.sign);
	// msg.writeInt(recordData.funTypeData.money.currencyCountForShow);
	// } else {
	// msg.writeByte(0);
	// }
	//
	// if (recordData.funTypeData.itemStruct3 != null) {
	// msg.writeByte(1);
	// msg.writeUtf8String(recordData.funTypeData.itemStruct3.getItemTemplate().ItemName);
	// msg.writeInt(recordData.funTypeData.itemStruct3.itemCount);
	// } else {
	// msg.writeByte(0);
	// }
	//
	// msg.writeInt(recordData.funTypeData.vitalityValue);
	//
	// msg.writeShort(recordData.getTime());
	// msg.writeShort(recordData.funTypeData.time);
	//
	// msg.writeBoolean(recordData.isAccepted());
	//
	// count++;
	// }
	// msg.setByte(msgindex, count);
	// } finally {
	// vdata.rwLock.unlock();
	// }
	// }
	//
	// /**
	// * <pre>
	// * 角色上线时、跨天时、奖励开放时，服务器直接发送给客户端
	// * 活跃度奖励数据
	// *
	// * int vitality 今日活跃度
	// * boolean 是否有活跃度奖励
	// * if(true){
	// * int vitalityValue 领取此奖励所需要达到的最小活跃度
	// * int 修为值
	// * byte 货币种类数量
	// * for(货币种类数量){
	// * byte 货币类型
	// * int 货币数量
	// * }
	// * byte 道具数量
	// * for (道具数量) {
	// * 道具（参考{@link
	// com.koala.kgame.protocol.item.KItemProtocol#MSG_STRUCT_ITEM_DETAILS}）
	// * }
	// * }
	// * </pre>
	// */
	// public static void packPushVitalityRewardData(long roleId, KGameMessage
	// msg) {
	// ExcitingData2 vdata = ExcitingManager.getVitalityData(roleId);
	//
	// int vita = vdata.getVitalityValue();
	// int minVita = vdata.getReceiveAtMinVitality();
	//
	// msg.writeInt(vita);
	// VitalityRewardData data =
	// ExcitingDataManager.mVitalityRewardDataManager.getDatas(minVita);
	// msg.writeBoolean(data != null);
	// if (data != null) {
	// msg.writeInt(data.minVitality);
	// msg.writeInt(data.xiuwei);
	// msg.writeByte(data.moneyList.size());
	// for (CurrencyCountStruct struct : data.moneyList) {
	// msg.writeByte(struct.currencyType.sign);
	// msg.writeInt(struct.currencyCountForShow);
	// }
	// msg.writeByte(data.itemStructs3.size());
	// for (ItemCountStruct3 struct : data.itemStructs3) {
	// KItemMsgPackCenter.packItem(msg, struct.getItemTemplate(),
	// struct.itemCount);
	// }
	// }
	// }
}
