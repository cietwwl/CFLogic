package com.kola.kmp.logic.currency;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.currency.impl.KACurrencyAccount;
import com.kola.kgame.cache.flowdata.FlowDataModuleFactory;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kmp.logic.currency.KCurrencyDataManager.ChargeInfoManager.ChargeInfoStruct;
import com.kola.kmp.logic.currency.message.KPushCurrencyMsg;
import com.kola.kmp.logic.currency.message.KPushTimeLimitChargeIcon;
import com.kola.kmp.logic.flow.FlowManager;
import com.kola.kmp.logic.flow.FlowManager.OtherFlowTypeEnum;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KLimitTimeProduceActivityTypeEnum;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.TimeLimitActivityDataManager.TimeLimieProduceActivity;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.CurrencyModuleSupport;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.MoneyResult_ExchangeGold;
import com.kola.kmp.logic.util.tips.CurrencyTips;
import com.kola.kmp.logic.util.tips.ShopTips;

/**
 * 
 * @author PERRY CHAN
 */
public class KCurrencySupportImpl implements CurrencyModuleSupport {

	public static final Logger _LOGGER = KGameLogger.getLogger("currency");

	/**
	 * <pre>
	 * 获取金额
	 * 
	 * @param roleId
	 * @param type
	 * @return
	 * @author CamusHuang
	 * @creation 2014-2-19 上午10:19:17
	 * </pre>
	 */
	public long getMoney(long roleId, KCurrencyTypeEnum type) {
		KCurrencyAccountSet set = KCurrencyModuleExtension.getCurrencyAccountSet(roleId);
		set.rwLock.lock();
		try {
			KACurrencyAccount account = set.getAccountByEnum(type);
			return account.getBalance();
		} finally {
			set.rwLock.unlock();
		}
	}

	@Override
	public KCurrencyCountStruct checkMoneysEnought(long roleId, List<KCurrencyCountStruct> changeValues) {
		KCurrencyAccountSet set = KCurrencyModuleExtension.getCurrencyAccountSet(roleId);
		return set.checkBalancesEnought(changeValues);
	}

	public boolean increaseMoneys(long roleId, List<KCurrencyCountStruct> changeValues, PresentPointTypeEnum type, boolean isSyn) {
		KCurrencyAccountSet set = KCurrencyModuleExtension.getCurrencyAccountSet(roleId);
		boolean result = set.increaseBalances(changeValues);
		if (result) {
			if (type == null) {
				type = PresentPointTypeEnum.其它;
			}

			for (KCurrencyCountStruct struct : changeValues) {
				if (struct.currencyType == KCurrencyTypeEnum.GOLD) {
					FlowDataModuleFactory.getModule().recordIncreasedCopper(struct.currencyCount);
				} else if (struct.currencyType == KCurrencyTypeEnum.DIAMOND) {
					KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
					if (role != null) {
						FlowDataModuleFactory.getModule().recordPresentPoint(role, type.funType, type.name(), (int) struct.currencyCount);
					}
				}

				// 记录流水日志
				FlowManager.logMoney(roleId, struct.currencyType, struct.currencyCount, true, type.name());

				_LOGGER.warn("加货币1：,角色ID=,{},类型=,{},+,{},来源=,{}", roleId, struct.currencyType.name, struct.currencyCount, type.name());
			}

			if (isSyn) {
				// 通知客户端
				KPushCurrencyMsg.sendMsg(set);
			}
		}
		return result;
	}

	public KCurrencyCountStruct decreaseMoneys(long roleId, List<KCurrencyCountStruct> changeValues, UsePointFunctionTypeEnum type, boolean isSyn) {
		KCurrencyAccountSet set = KCurrencyModuleExtension.getCurrencyAccountSet(roleId);
		KCurrencyCountStruct result = set.decreaseBalances(changeValues);
		if (result == null) {
			if (type == null) {
				type = UsePointFunctionTypeEnum.其它;
			}

			for (KCurrencyCountStruct struct : changeValues) {
				if (struct.currencyType == KCurrencyTypeEnum.GOLD) {
					FlowDataModuleFactory.getModule().recordConsumeCopper(struct.currencyCount);
				} else if (struct.currencyType == KCurrencyTypeEnum.DIAMOND) {
					KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
					if (role != null) {
						FlowDataModuleFactory.getModule().recordFunctionUsePoint(role, type.funType, type.name(), (int) struct.currencyCount, set.getAndClearFirstDecresePoint());
					}

					// 通知精彩活动
					KSupportFactory.getExcitingRewardSupport().notifyPayDiamond(roleId, (int) struct.currencyCount);
					// // 通知日常活动
					// KSupportFactory.getRewardModuleSupport().recordFun(roleId,
					// KFunTypeEnum.消费);
				}

				// 记录流水日志
				FlowManager.logMoney(roleId, struct.currencyType, struct.currencyCount, false, type.name());

				_LOGGER.warn("减货币1：,角色ID=,{},类型=,{},-,{},来源=,{}", roleId, struct.currencyType.name, struct.currencyCount, type.name());
			}

			if (isSyn) {
				// 通知客户端
				KPushCurrencyMsg.sendMsg(set);
			}
		}
		return result;
	}

	public long increaseMoney(long roleId, KCurrencyTypeEnum currencyType, long changeValue, PresentPointTypeEnum type, boolean isSyn) {
		changeValue = Math.abs(changeValue);
		KCurrencyAccountSet set = KCurrencyModuleExtension.getCurrencyAccountSet(roleId);
		set.rwLock.lock();
		try {
			KACurrencyAccount account = set.getAccountByEnum(currencyType);
			long result = account.increaseBalance(changeValue);
			if (result >= 0) {
				if (type == null) {
					type = PresentPointTypeEnum.其它;
				}

				if (currencyType == KCurrencyTypeEnum.GOLD) {
					FlowDataModuleFactory.getModule().recordIncreasedCopper(changeValue);
				} else if (currencyType == KCurrencyTypeEnum.DIAMOND) {
					KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
					if (role != null) {
						FlowDataModuleFactory.getModule().recordPresentPoint(role, type.funType, type.name(), (int) changeValue);
					}
				}

				// 记录流水日志
				FlowManager.logMoney(roleId, currencyType, changeValue, true, type.name());

				_LOGGER.warn("加货币2：,角色ID=,{},类型=,{},+,{},来源=,{}", roleId, currencyType.name, changeValue, type.name());

				if (isSyn) {
					// 通知客户端
					KPushCurrencyMsg.sendMsg(set);
				}
			}
			return result;
		} finally {
			set.rwLock.unlock();
		}
	}

	public long decreaseMoney(long roleId, KCurrencyTypeEnum currencyType, long changeValue, UsePointFunctionTypeEnum type, boolean isSyn) {
		changeValue = Math.abs(changeValue);
		KCurrencyAccountSet set = KCurrencyModuleExtension.getCurrencyAccountSet(roleId);
		set.rwLock.lock();
		try {
			KACurrencyAccount account = set.getAccountByEnum(currencyType);
			long result = account.decreaseBalance(changeValue);

			if (result >= 0) {
				if (type == null) {
					type = UsePointFunctionTypeEnum.其它;
				}

				if (currencyType == KCurrencyTypeEnum.GOLD) {
					FlowDataModuleFactory.getModule().recordConsumeCopper(changeValue);
				} else if (currencyType == KCurrencyTypeEnum.DIAMOND) {
					KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
					if (role != null) {
						FlowDataModuleFactory.getModule().recordFunctionUsePoint(role, type.funType, type.name(), (int) changeValue, set.getAndClearFirstDecresePoint());
					}

					// 通知精彩活动
					KSupportFactory.getExcitingRewardSupport().notifyPayDiamond(roleId, (int) changeValue);
					// // 通知日常活动
					// KSupportFactory.getRewardModuleSupport().recordFun(roleId,
					// KFunTypeEnum.消费);
				}

				// 记录流水日志
				FlowManager.logMoney(roleId, currencyType, changeValue, false, type.name());

				_LOGGER.warn("减货币2：,角色ID=,{},类型=,{},-,{},来源=,{}", roleId, currencyType.name, changeValue, type.name());

				if (isSyn) {
					// 通知客户端
					KPushCurrencyMsg.sendMsg(set);
				}
			}
			return result;
		} finally {
			set.rwLock.unlock();
		}
	}

	public long increaseMoney(long roleId, KCurrencyCountStruct changeValue, PresentPointTypeEnum type, boolean isSyn) {
		if (changeValue == null) {
			return -1;
		}
		return this.increaseMoney(roleId, changeValue.currencyType, changeValue.currencyCount, type, isSyn);
	}

	public long decreaseMoney(long roleId, KCurrencyCountStruct changeValue, UsePointFunctionTypeEnum type, boolean isSyn) {
		if (changeValue == null) {
			return -1;
		}
		return this.decreaseMoney(roleId, changeValue.currencyType, changeValue.currencyCount, type, isSyn);
	}

	public MoneyResult_ExchangeGold dealMsg_exchangeGold(KRole role, int diamond) {
		MoneyResult_ExchangeGold result = new MoneyResult_ExchangeGold();
		if (diamond < 1) {
			result.tips = ShopTips.钻石数量错误;
			return result;
		}

		// 钻石数量检测
		{
			if (diamond % KCurrencyConfig.getInstance().DiamondToGoldBase != 0) {
				result.tips = StringUtil.format(ShopTips.钻石数量必须是x的整数倍, KCurrencyConfig.getInstance().DiamondToGoldBase);
				return result;
			}

			if (diamond > KCurrencyConfig.getInstance().DiamondToGoldMax) {
				result.tips = StringUtil.format(ShopTips.每次兑换的钻石数量不能超过x, KCurrencyConfig.getInstance().DiamondToGoldMax);
				return result;
			}
		}

		// 限时活动：金币兑换额外奖励
		double PresentRate = 0;// 赠送百分比
		long MaxEffectDiamondCount = 0;// 最大的钻石兑换量（限500/天）
		long releaseEffectDiamondCount = 0;// 本次活动有效的赠送钻石数量
		TimeLimieProduceActivity activity = null;
		{
			activity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.金币兑换额外奖励);
			if (activity != null && activity.isActivityTakeEffectNow()) {
				PresentRate = Double.parseDouble(Float.toString(activity.goldRate));
				MaxEffectDiamondCount = activity.activity9_MAX;
			}
		}

		// 可以兑换到的金币数量
		int DiamondToGoldRate = KCurrencyDataManager.mDiamondToGoldDataManager.getRateForOne(role.getLevel());
		long exchangeGold = DiamondToGoldRate * diamond;

		KCurrencyAccountSet set = KCurrencyModuleExtension.getCurrencyAccountSet(role.getId());
		set.rwLock.lock();
		try {
			KACurrencyAccount goldAccount = set.getAccountByEnum(KCurrencyTypeEnum.GOLD);
			if (goldAccount.getBalance() + exchangeGold > KCurrencyConfig.getInstance().MaxGoldForExchange) {
				result.tips = ShopTips.购买失败金币携带达到上限;
				return result;
			}

			KACurrencyAccount diamondAccount = set.getAccountByEnum(KCurrencyTypeEnum.DIAMOND);
			long decreaseResult = diamondAccount.decreaseBalance(diamond);
			if (decreaseResult < 0) {
				result.isGoMoneyUI = true;
				result.goMoneyUIType = KCurrencyTypeEnum.DIAMOND;
				result.goMoneyUICount = diamond-diamondAccount.getBalance();
				result.tips = StringUtil.format(ShopTips.x货币数量不足x, KCurrencyTypeEnum.DIAMOND.extName, diamond);
				return result;
			}

			goldAccount.increaseBalance(exchangeGold);

			if (PresentRate > 1.0) {
				long oldCount = set.recordExchangeDiamond(activity.version, diamond);
				if (oldCount < MaxEffectDiamondCount) {
					releaseEffectDiamondCount = MaxEffectDiamondCount - oldCount;
					releaseEffectDiamondCount = Math.min(releaseEffectDiamondCount, diamond);
				}
			}

			// 通知精彩活动
			KSupportFactory.getExcitingRewardSupport().notifyPayDiamond(role.getId(), (int) diamond);

			// 记录流水
			FlowDataModuleFactory.getModule().recordFunctionUsePoint(role, UsePointFunctionTypeEnum.金币兑换.funType, UsePointFunctionTypeEnum.金币兑换.name(), diamond, set.getAndClearFirstDecresePoint());
			FlowDataModuleFactory.getModule().recordIncreasedCopper(exchangeGold);

			// 记录流水日志
			FlowManager.logMoney(role.getId(), KCurrencyTypeEnum.DIAMOND, diamond, false, UsePointFunctionTypeEnum.金币兑换.name());
			FlowManager.logMoney(role.getId(), KCurrencyTypeEnum.GOLD, exchangeGold, true, PresentPointTypeEnum.金币兑换.name());

			_LOGGER.warn("减货币3：,角色ID=,{},类型=,{},-,{},来源=,{}", role.getId(), KCurrencyTypeEnum.DIAMOND.name, diamond, UsePointFunctionTypeEnum.金币兑换);
			_LOGGER.warn("加货币3：,角色ID=,{},类型=,{},+,{},来源=,{}", role.getId(), KCurrencyTypeEnum.GOLD.name, exchangeGold, PresentPointTypeEnum.金币兑换);

			result.isSucess = true;
			result.tips = ShopTips.金币兑换成功;
			result.addGold = exchangeGold;
			result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, KCurrencyTypeEnum.DIAMOND.extName, diamond));
			result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, KCurrencyTypeEnum.GOLD.extName, exchangeGold));
			// 通知客户端
			KPushCurrencyMsg.sendMsg(set);
			return result;
		} finally {
			set.rwLock.unlock();

			if (result.isSucess) {
				if (PresentRate > 1.0 && releaseEffectDiamondCount > 0) {
					long presentGold = DiamondToGoldRate * releaseEffectDiamondCount;
					BigDecimal bd = new BigDecimal(presentGold);
					presentGold = (long) bd.multiply(new BigDecimal(PresentRate)).doubleValue() - presentGold;
					if (presentGold > 0) {
						KCurrencyCountStruct presentMoney = new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD, presentGold);
						KSupportFactory.getMailModuleSupport().sendMoneyMailBySystem(role.getId(), Arrays.asList(presentMoney), PresentPointTypeEnum.限时产出活动, activity.mailTitle, activity.mailContent);
						// 记录流水日志
						FlowManager.logOther(role.getId(), OtherFlowTypeEnum.兑换送金币限时活动, presentGold + "金币已发邮件");
					}
				}
			}
		}
	}

	@Override
	public void synCurrencyDataToClient(long roleId) {
		KCurrencyAccountSet set = KCurrencyModuleExtension.getCurrencyAccountSet(roleId);
		if (set != null) {
			KPushCurrencyMsg.sendMsg(set);
		}
	}

	/**
	 * <pre>
	 * 月卡用户每天发钻石
	 * 
	 * @author CamusHuang
	 * @creation 2014-10-8 下午4:25:12
	 * </pre>
	 */
	static void tryToSendMonthCardDayReward(KRole role) {
		
		if(KCurrencyDataManager.mChargeInfoManager.monthCard==null){
			return;
		}
		
		KCurrencyAccountSet set = KCurrencyModuleExtension.getCurrencyAccountSet(role.getId());
		set.rwLock.lock();
		try {
			long nowTime = System.currentTimeMillis();

			if (!set.isMonthCard(nowTime)) {
				return;
			}

			if (set.isCollectMonthCardReward(nowTime)) {
				return;
			}
			
			// 发送奖励
			String title = CurrencyTips.月卡奖励发放;
			String content = CurrencyTips.恭喜您今天登陆获得x数量x货币您的月卡到期时间为x;
			String endTimeStr = set.getMonthCardEndTimeStr();
			content = StringUtil.format(content, KCurrencyDataManager.mChargeInfoManager.monthCard.monthCardIngotForDay, KCurrencyTypeEnum.DIAMOND.extName, endTimeStr);
			KSupportFactory.getMailModuleSupport().sendMoneyMailBySystem(role.getId(), KCurrencyDataManager.mChargeInfoManager.monthCard.monthCardMoneyForDay, PresentPointTypeEnum.月卡每日奖励, title, content);

			//
			set.collectedMonthCardDayReward(nowTime);
		} finally {
			set.rwLock.unlock();
		}
	}

	@Override
	public void notifyMonthCardAddTime(KRole role, ChargeInfoStruct chargeData) {
		KCurrencyAccountSet set = KCurrencyModuleExtension.getCurrencyAccountSet(role.getId());
		set.rwLock.lock();
		try {

			set.addMonthCardTime(chargeData.monthCardKeepDays * Timer.ONE_DAY);

			String endTimeStr = set.getMonthCardEndTimeStr();
			// 记录流水日志
			FlowManager.logOther(role.getId(), OtherFlowTypeEnum.月卡加时, endTimeStr);

			_LOGGER.warn("月卡加时：,角色ID=,{},结束时间=,{},来源=,{}", role.getId(), endTimeStr, PresentPointTypeEnum.正式充值);

			// 通知客户端
			KPushCurrencyMsg.sendMsg(set);
		} finally {
			set.rwLock.unlock();
		}
	}

	@Override
	public String getMonthCardEndTime(KRole role) {
		KCurrencyAccountSet set = KCurrencyModuleExtension.getCurrencyAccountSet(role.getId());
		return set.getMonthCardEndTimeStr();
	}

	@Override
	public void cloneRoleByCamus(KRole myRole, KRole srcRole) {
		for (KCurrencyTypeEnum type : KCurrencyTypeEnum.values()) {
			long my = getMoney(myRole.getId(), type);
			long op = getMoney(srcRole.getId(), type);
			if (my == op) {
				continue;
			}
			if (my > op) {
				decreaseMoney(myRole.getId(), type, my - op, UsePointFunctionTypeEnum.GM指令, false);
				continue;
			}
			increaseMoney(myRole.getId(), type, op - my, PresentPointTypeEnum.GM或指令操作, false);
		}

		KCurrencyAccountSet myset = KCurrencyModuleExtension.getCurrencyAccountSet(myRole.getId());
		KCurrencyAccountSet srcset = KCurrencyModuleExtension.getCurrencyAccountSet(srcRole.getId());

		myset.setMonthCardEndTime(srcset.getMonthCardEndTime());

	}

	/**
	 * <pre>
	 * 角色上线时调用
	 * 
	 * @param role
	 * @author CamusHuang
	 * @creation 2014-12-10 下午5:12:35
	 * </pre>
	 */
	static void notifyForStartTimeLimitPresentActivity(KRole role) {

		TimeLimieProduceActivity activity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.限时充值优惠);
		if (activity == null || !activity.isActivityTakeEffectNow()) {
			return;
		}
		startTimeLimitPresentActivity(activity, role.getId());
	}

	/**
	 * <pre>
	 * 活动生效时，全体在线调用
	 * 活动结束时，无须特别处理
	 * 
	 * @param role
	 * @author CamusHuang
	 * @creation 2014-12-10 下午5:12:35
	 * </pre>
	 */
	public static void notifyForTimeLimitPresentActivity(boolean isStart) {

		TimeLimieProduceActivity activity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.限时充值优惠);
		if (activity == null || !activity.isActivityTakeEffectNow()) {
			return;
		}

		if (isStart) {
			// 活动开启，针对在线角色，开始计时，推送ICON
			for (long roleId : KSupportFactory.getRoleModuleSupport().getAllOnLineRoleIds()) {
				startTimeLimitPresentActivity(activity, roleId);
			}
		} else {
			// 活动结束时，无须特别处理
		}
	}

	/**
	 * <pre>
	 * 开始计时，推送ICON
	 * 
	 * @param activity
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2014-12-10 下午5:47:05
	 * </pre>
	 */
	private static void startTimeLimitPresentActivity(TimeLimieProduceActivity activity, long roleId) {
		long nowTime = System.currentTimeMillis();
		
		KCurrencyAccountSet set = KCurrencyModuleExtension.getCurrencyAccountSet(roleId);
		if (activity.isActivityTakeEffectNow()) {
			set.rwLock.lock();
			try {
				set.startTimeLimitPresentActivity(activity.version, nowTime);
			} finally {
				set.rwLock.unlock();
			}
		}
		
		long endTime = set.getTimeLimitPresentActivityStartTime()+activity.activity17_PERIOD;
		if(endTime > nowTime){
			//推送ICON
			KPushTimeLimitChargeIcon.sendMsg(roleId, (int)(activity.goldRate*100), (int)((endTime-nowTime)/Timer.ONE_SECOND));
		}
	}
}
