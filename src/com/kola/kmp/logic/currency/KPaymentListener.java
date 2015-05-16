package com.kola.kmp.logic.currency;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;

import com.koala.game.dataaccess.KGameDataAccessFactory;
import com.koala.game.gameserver.KGameServer;
import com.koala.game.gameserver.paysupport.KGamePaymentListener;
import com.koala.game.gameserver.paysupport.PayOrderDealResult;
import com.koala.game.logging.KGameLogger;
import com.koala.game.player.KGamePlayer;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.koala.paymentserver.PayOrder;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.chat.KWordBroadcastType;
import com.kola.kmp.logic.currency.KCurrencyDataManager.ChargeInfoManager.ChargeInfoStruct;
import com.kola.kmp.logic.currency.KCurrencyDataManager.FirstChargeRewardDataManager;
import com.kola.kmp.logic.currency.message.KPushChargeResultMsg;
import com.kola.kmp.logic.currency.message.KPushFirstChargeRewardMsg;
import com.kola.kmp.logic.currency.message.KPushPayInfoMsg;
import com.kola.kmp.logic.flow.FlowManager;
import com.kola.kmp.logic.flow.FlowManager.OtherFlowTypeEnum;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KLimitTimeProduceActivityTypeEnum;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.TimeLimitActivityDataManager.TimeLimieProduceActivity;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.CurrencyTips;
import com.kola.kmp.logic.util.tips.ShopTips;

public class KPaymentListener implements KGamePaymentListener {

	public static final int ChargeRate = 10;// 充值倍率，1元=10元宝
	private static final String ChargeRateStr = "0.10";// 充值倍率，1元=10元宝
	public static final KPaymentListener instance = new KPaymentListener();

	private static Logger _LOGGER = KGameLogger.getLogger("chargeRecord");

	@Override
	public PayOrderDealResult dealPayOrder(PayOrder payOrder) {
		return doPayOrder(payOrder, true);
	}

	/**
	 * <pre>
	 * 测试充值
	 * 与{@link #dealPayOrder(PayOrder payOrder)}的区别在于不记录日志到数据库
	 * 
	 * @deprecated 本方法仅用于游戏内指令充值
	 * @param payOrder
	 * @return
	 * @author CamusHuang
	 * @creation 2013-7-12 上午11:04:04
	 * </pre>
	 */
	public PayOrderDealResult dealPayOrderForTest(PayOrder payOrder) {
		return doPayOrder(payOrder, false);
	}

	/**
	 * <pre>
	 * 高仿真充值
	 * 与{@link #dealPayOrder(PayOrder payOrder)}的基本上无区别，一样写入DB日志和通知客户端
	 * 
	 * @deprecated 本方法仅用于游戏内指令充值
	 * @param payOrder
	 * @return
	 * @author CamusHuang
	 * @creation 2013-12-9 上午11:20:05
	 * </pre>
	 */
	public PayOrderDealResult dealPayOrderForTestForHighSimulation(PayOrder payOrder) {
		return doPayOrder(payOrder, true);
	}

	public static class ChargeOrderStruct {
		public final PayOrder payOrder;
		final boolean isRealCharge;
		//
		// 充值的货币名称
		final int ISOCurrencyDBType;
		// 充值多少元
		public final float RMB;
		// 充值货币对应的钻石数量(充10元即100钻)
		final int rmbIngot;
		// 档位数据
		final ChargeInfoStruct mChargeInfoStruct;
		// 档位充值获得钻石数量(充10元不一定获得100钻)
		final int baseIngot;
		// 档位首充赚点
		final int presentIngotForFirstCharge;
		// 档位固定赠点
		final int presentIngotForFixed;
		// 是否档位首充
		boolean isFirstChargeForReturn;
		// 首充礼包
		List<ItemCountStruct> firstChargeGift;

		ChargeOrderStruct(PayOrder payOrder, boolean isRealCharge) {
			this.payOrder = payOrder;
			this.isRealCharge = isRealCharge;

			ISOCurrencyDBType = 1;
			{
				// RMB分
				String chargeFenStr = payOrder.getMoney();
				// RMB元 DX修改money单位为分所以要除以100
				RMB = Float.parseFloat(chargeFenStr) / 100;
				rmbIngot = (int) (RMB * ChargeRate);
				//
				mChargeInfoStruct = KCurrencyDataManager.mChargeInfoManager.getInfo(rmbIngot);
				// ==============================================
				// 优先根据平台要求加元宝，若平台无指示则根据规则计算元宝
				if (payOrder.getCoins() < 1) {
					// 按充值比率得到的元宝数量
					if (mChargeInfoStruct == null) {
						baseIngot = rmbIngot;
					} else {
						baseIngot = mChargeInfoStruct.baseIngot;
					}
				} else {
					baseIngot = payOrder.getCoins();
				}
				// ==============================================
				if (mChargeInfoStruct != null) {
					int firstChargePresentIngot = baseIngot * mChargeInfoStruct.returnRateForFirst / 100;
					if (firstChargePresentIngot < 0) {
						firstChargePresentIngot = 0;
					}
					this.presentIngotForFirstCharge = firstChargePresentIngot;
					presentIngotForFixed = mChargeInfoStruct.presentIngot;
				} else {
					this.presentIngotForFirstCharge = 0;
					presentIngotForFixed = 0;
				}
			}
		}

		boolean isMonthCard() {
			return mChargeInfoStruct != null && mChargeInfoStruct.isMonthCard();
		}
	}

	private PayOrderDealResult doPayOrder(PayOrder payOrder, boolean isRealCharge) {
		_LOGGER.warn("充值通知,：payOrder=,{},isChargeByRMB={}", payOrder.toString(), isRealCharge);
		// ==============================================

		long roleId = payOrder.getExt().getRoleID();

		ChargeOrderStruct chargeData = new ChargeOrderStruct(payOrder, isRealCharge);

		// ==============================================

		PayOrderDealResultImpl payResult = new PayOrderDealResultImpl(payOrder);
		payResult.result = 1;// 默认不成功

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		if (role == null) {
			payResult.result = 1;// 帐户不存在（例如角色已删）
			_LOGGER.warn("充值加点失败,:roleId=,{},roleName=,{},充点数=,{},得点数=,{},payOrder=,{}", roleId, "", chargeData.rmbIngot, chargeData.baseIngot, payOrder.toString());
			return payResult;
		}

		// ==============================================
		try {
			// 记录充值点数（不含各种赠点）
			if (chargeData.baseIngot > 0) {
				long lastIngot = KSupportFactory.getCurrencySupport().increaseMoney(roleId, KCurrencyTypeEnum.DIAMOND, chargeData.baseIngot,
						(isRealCharge ? PresentPointTypeEnum.正式充值 : PresentPointTypeEnum.后台直接发放), true);
				if (lastIngot < 0) {
					payResult.result = 1;// 帐户不存在（例如角色已删）
					_LOGGER.warn("充值加点失败,:roleId=,{},roleName=,{},充点数=,{},得点数=,{},payOrder=,{}", roleId, "", chargeData.rmbIngot, chargeData.baseIngot, payOrder.toString());
				} else {
					payResult.result = 0;// 成功
					_LOGGER.warn("充值加点成功,:roleId=,{},roleName=,{},充点数=,{},得点数=,{},余额=,{},payOrder=,{}", roleId, "", chargeData.rmbIngot, chargeData.baseIngot, lastIngot, payOrder.toString());
				}
			} else {
				payResult.result = 0;// 成功
				_LOGGER.warn("充值加点成功,:roleId=,{},roleName=,{},充点数=,{},得点数=,{},payOrder=,{}", roleId, "", chargeData.rmbIngot, chargeData.baseIngot, payOrder.toString());
			}

			if (payResult.result == 0 && chargeData.isMonthCard()) {
				// 购买月卡
				KSupportFactory.getCurrencySupport().notifyMonthCardAddTime(role, chargeData.mChargeInfoStruct);
			}
			return payResult;
		} catch (Exception e) {
			// 以防万一捕捉异常
			_LOGGER.warn("充值异常,:roleId=,{},roleName=,{},充点数=,{},得点数=,{},payOrder=,{}", roleId, role.getName(), chargeData.rmbIngot, chargeData.baseIngot, payOrder.toString());
			_LOGGER.error(e.getMessage(), e);
			return payResult;
		} finally {
			if (payResult.result == 0) {
				try {

					// 处理首充礼包
					dealFirstChargeGiftReward(role, chargeData);

					// 处理档位首充赠点
					dealPresentIngotForFirstCharge(role, chargeData);

					// 处理档位固定赠点
					dealPresentIngotForFix(role, chargeData);

					// 处理真实充值
					dealForRealCharge(role, chargeData);

					// 处理邮件和其它通知
					dealForChargeMailAndNotifyNew(role, chargeData);

					// 处理充值限时优惠
					dealTimeLimitPresentReward(role, chargeData);

				} catch (Exception e) {
					_LOGGER.warn("充值成功结果处理异常,:roleId=,{},roleName=,{},充点数=,{},得点数=,{},payOrder=,{}", roleId, role.getName(), chargeData.rmbIngot, chargeData.baseIngot, payOrder.toString());
					_LOGGER.error(e.getMessage(), e);
				}
				
				// 同步一次档位数据
				KPushPayInfoMsg.sendMsg(role);
			}
		}
	}

	/**
	 * <pre>
	 * 处理首充礼包
	 * 
	 * @param role
	 * @param chargeData
	 * @author CamusHuang
	 * @creation 2015-1-21 上午10:03:54
	 * </pre>
	 */
	private static void dealFirstChargeGiftReward(KRole role, ChargeOrderStruct chargeData) {

		FirstChargeRewardDataManager fchargeManager = KCurrencyDataManager.mFirstChargeRewardDataManager;

		if (fchargeManager == null || fchargeManager.keepDays < 1) {
			// 无首充数据
			return;
		}

		long nowTime = System.currentTimeMillis();
		int N = UtilTool.countDays(role.getCreateTime(), nowTime);// 角色创建了多少天
		if (N > fchargeManager.keepDays) {
			// 首充过时失效
			return;
		}

		if (chargeData.rmbIngot < fchargeManager.minChargeForGift) {
			// 充值金额不足
			return;
		}

		KCurrencyAccountSet set = KCurrencyModuleExtension.getCurrencyAccountSet(role.getId());

		if (!set.tryToRecordCatchFirstChargeGiftReward()) {
			// 已经领取过首充礼包
			return;
		}

		// 关闭客户端首充入口
		KPushFirstChargeRewardMsg.sendMsg(role);

		// 首充礼包
		chargeData.firstChargeGift = fchargeManager.getReward(role.getJob());
	}

	/**
	 * <pre>
	 * 处理档位首充赠点
	 * 
	 * @param role
	 * @param chargeData
	 * @return
	 * @author CamusHuang
	 * @creation 2014-9-25 下午6:03:17
	 * </pre>
	 */
	private static void dealPresentIngotForFirstCharge(KRole role, ChargeOrderStruct chargeData) {

		KCurrencyAccountSet set = KCurrencyModuleExtension.getCurrencyAccountSet(role.getId());

		if (!set.tryToRecordCatchFirstChargeReturn(chargeData.rmbIngot)) {
			return;
		}

		{
			chargeData.isFirstChargeForReturn = true;
			// 首充返利
			if (chargeData.presentIngotForFirstCharge > 0) {
				KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), KCurrencyTypeEnum.DIAMOND, chargeData.presentIngotForFirstCharge, PresentPointTypeEnum.首充返利, true);
				KDialogService.sendUprisingDialog(role, StringUtil.format(CurrencyTips.首充赠送x数量x货币, chargeData.presentIngotForFirstCharge, KCurrencyTypeEnum.DIAMOND.extName));

				_LOGGER.warn("首充返利成功,:roleId=,{},roleName=,{},返点数=,{},payOrder=,{}", role.getId(), role.getName(), chargeData.presentIngotForFirstCharge, chargeData.payOrder.toString());
			}
		}
	}

	/**
	 * <pre>
	 * 处理档位固定赠点
	 * 
	 * @param role
	 * @param chargeData
	 * @author CamusHuang
	 * @creation 2014-9-25 下午6:19:16
	 * </pre>
	 */
	private static void dealPresentIngotForFix(KRole role, ChargeOrderStruct chargeData) {
		if (chargeData.presentIngotForFixed < 1) {
			return;
		}

		KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), KCurrencyTypeEnum.DIAMOND, chargeData.presentIngotForFixed, PresentPointTypeEnum.充值赠点, true);
		KDialogService.sendUprisingDialog(role, StringUtil.format(CurrencyTips.系统赠送x数量x货币, chargeData.presentIngotForFixed, KCurrencyTypeEnum.DIAMOND.extName));

		_LOGGER.warn("充值固定赠点成功,:roleId=,{},roleName=,{},赠点数=,{},payOrder=,{}", role.getId(), role.getName(), chargeData.presentIngotForFixed, chargeData.payOrder.toString());
	}

	/**
	 * <pre>
	 * 处理真实充值
	 * 
	 * @param role
	 * @param chargeData
	 * @author CamusHuang
	 * @creation 2014-9-25 下午6:19:16
	 * </pre>
	 */
	private static void dealForRealCharge(KRole role, ChargeOrderStruct chargeData) {
		if (!chargeData.isRealCharge) {
			return;
		}
	
		try {
			byte isPlayerFirstCharge = 1;// 帐号是否首充?默认本次是首充
	
			KGamePlayer player = null;
	
			// 不保证存在
			KGamePlayerSession session = KGameServer.getInstance().getPlayerManager().getPlayerSession(role.getPlayerId());
			if (session != null) {
				player = session.getBoundPlayer();
			} else {
				player = KGameServer.getInstance().getPlayerManager().loadPlayerData(role.getPlayerId());
			}
	
			if (player != null) {
				if (player.isFirstCharge()) {
					// 帐号已首充
					isPlayerFirstCharge = 0;
				} else {
					// 帐号未首充
					isPlayerFirstCharge = 1;
					player.setFirstCharge(true);
				}
			}
	
			if (!KSupportFactory.getRoleModuleSupport().isPlayerFirstCharge(role.getId())) {
				KSupportFactory.getRoleModuleSupport().setPlayerFirstCharge(role.getId(), true);
			}
	
			// 记录流水
			KGameDataAccessFactory
					.getInstance()
					.getPlayerManagerDataAccess()
					.addChargeReocrd(role.getPlayerId(), role.getId(), role.getName(), role.getLevel(), isPlayerFirstCharge, Float.parseFloat(chargeData.payOrder.getMoney()), chargeData.baseIngot,
							chargeData.payOrder.getOrderId(), chargeData.payOrder.getPayWay(), System.currentTimeMillis(), chargeData.ISOCurrencyDBType, role.getPromoId(), role.getParentPromoId(), 0,
							chargeData.payOrder.getOtherinfo() + ":" + (chargeData.isMonthCard() ? "月卡" : "普通充值"));
		} catch (Exception e) {
			_LOGGER.warn("充值加点流水异常,:roleId=,{},roleName=,{},加点数=,{},payOrder=,{}", role.getId(), role.getName(), chargeData.baseIngot, chargeData.payOrder.toString());
			_LOGGER.error(e.getMessage(), e);
		}
	
		// 通知客户端
		KPushChargeResultMsg.sendMsg(role, chargeData);
	}

	/**
	 * <pre>
	 * 处理邮件和其它通知
	 * 
	 * @param role
	 * @param chargeData
	 * @author CamusHuang
	 * @creation 2014-9-25 下午6:19:16
	 * </pre>
	 */
	private static void dealForChargeMailAndNotifyNew(KRole role, ChargeOrderStruct chargeData) {
	
		long lastIngot = KSupportFactory.getCurrencySupport().getMoney(role.getId(), KCurrencyTypeEnum.DIAMOND);
		String monthCardEndTime = KSupportFactory.getCurrencySupport().getMonthCardEndTime(role);
	
		// 邮件详细格式
		String tipsToPlayer = null;
		String mailContent = null;
		{
			String baseInfo = chargeData.baseIngot > 0 ? StringUtil.format(CurrencyTips.Tips获得x数量x货币, chargeData.baseIngot, KCurrencyTypeEnum.DIAMOND.name) : "";
			String presentInfo = chargeData.presentIngotForFixed > 0 ? StringUtil.format(CurrencyTips.Tips系统赠送x数量x货币, chargeData.presentIngotForFixed, KCurrencyTypeEnum.DIAMOND.name) : "";
			String firstPresentInfo = (chargeData.isFirstChargeForReturn && chargeData.presentIngotForFirstCharge > 0) ? StringUtil.format(CurrencyTips.Tips首充赠送x数量x货币,
					chargeData.presentIngotForFirstCharge, KCurrencyTypeEnum.DIAMOND.name) : "";
			//
			if (chargeData.isMonthCard()) {
				tipsToPlayer = CurrencyTips.购买月卡成功获得信息x赠送信息x首充信息x当前余额x数量x货币月卡到期时间为x;
				mailContent = CurrencyTips.x时间购买月卡成功获得信息x赠送信息x首充信息x当前余额x数量x货币月卡到期时间为x;
			} else {
				tipsToPlayer = CurrencyTips.充值成功获得信息x赠送信息x首充信息x当前余额x数量x货币;
				mailContent = CurrencyTips.x时间充值成功获得信息x赠送信息x首充信息x当前余额x数量x货币;
			}
			tipsToPlayer = StringUtil.format(tipsToPlayer, baseInfo, presentInfo, firstPresentInfo, lastIngot, KCurrencyTypeEnum.DIAMOND.name);
			mailContent = StringUtil.format(mailContent, chargeData.payOrder.getPaytime(), baseInfo, presentInfo, firstPresentInfo, lastIngot, KCurrencyTypeEnum.DIAMOND.name);
			if (chargeData.isMonthCard()) {
				tipsToPlayer = StringUtil.format(tipsToPlayer, monthCardEndTime);
				mailContent = StringUtil.format(mailContent, monthCardEndTime);
			}
	
			// 通知游戏内玩家
			if (role.isOnline()) {
				KDialogService.sendSimpleDialog(role, CurrencyTips.充值到帐, tipsToPlayer);
				if (chargeData.baseIngot > 0) {
					KDialogService.sendDataUprisingDialog(role, StringUtil.format(ShopTips.x加x, KCurrencyTypeEnum.DIAMOND.extName, chargeData.baseIngot));
				}
				if (chargeData.presentIngotForFixed > 0) {
					KDialogService.sendDataUprisingDialog(role, StringUtil.format(ShopTips.x加x, KCurrencyTypeEnum.DIAMOND.extName, chargeData.presentIngotForFixed));
				}
				if (chargeData.isFirstChargeForReturn && chargeData.presentIngotForFirstCharge > 0) {
					KDialogService.sendDataUprisingDialog(role, StringUtil.format(ShopTips.x加x, KCurrencyTypeEnum.DIAMOND.extName, chargeData.presentIngotForFirstCharge));
				}
			}
	
			// 发送邮件
			KSupportFactory.getMailModuleSupport().sendSimpleMailBySystem(role.getId(), CurrencyTips.充值到帐, mailContent);
	
			// 发送首充附件邮件
			if (chargeData.firstChargeGift != null) {
				mailContent = StringUtil.format(CurrencyTips.x时间首充成功获得以下礼包, chargeData.payOrder.getPaytime());
				KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), chargeData.firstChargeGift, CurrencyTips.首充礼包, mailContent);
	
				KWordBroadcastType _boradcastType = KWordBroadcastType.首冲礼包_XX领取了首冲礼包;
				KSupportFactory.getChatSupport().sendSystemChat(StringUtil.format(_boradcastType.content, role.getExName()), _boradcastType);
			}
		}
	
		// 月卡用户首次发钻石（不用等下次登陆驱动）
		if (chargeData.isMonthCard()) {
			KCurrencySupportImpl.tryToSendMonthCardDayReward(role);
		}
	
		// 通知VIP
		KSupportFactory.getVIPModuleSupport().notifyCharge(role, chargeData.baseIngot);
	
		// 通知精彩活动
		KSupportFactory.getExcitingRewardSupport().notifyCharge(role.getId(), chargeData.baseIngot, chargeData.firstChargeGift!=null);
	}

	/**
	 * <pre>
	 * 处理限时返现活动
	 * 
	 * @param role
	 * @param chargeData
	 * @author CamusHuang
	 * @creation 2014-9-25 下午6:19:16
	 * </pre>
	 */
	private static void dealTimeLimitPresentReward(KRole role, ChargeOrderStruct chargeData) {
		if (chargeData.baseIngot < 1) {
			return;
		}

		TimeLimieProduceActivity activity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.限时充值优惠);
		if (activity == null || activity.goldRate <= 0 || !activity.isActivityTakeEffectNow()) {
			return;
		}

		KCurrencyAccountSet set = KCurrencyModuleExtension.getCurrencyAccountSet(role.getId());

		long nowTime = System.currentTimeMillis();
		long endTime = set.getTimeLimitPresentActivityStartTime() + activity.activity17_PERIOD;
		if (endTime <= nowTime) {
			return;
		}

		BigDecimal bd = new BigDecimal(chargeData.baseIngot);
		long returnValue = (int) bd.multiply(new BigDecimal(activity.goldRate)).doubleValue();
		if (returnValue < 1) {
			return;
		}

		// //
		KCurrencyCountStruct presentMoney = new KCurrencyCountStruct(KCurrencyTypeEnum.DIAMOND, returnValue);
		KSupportFactory.getMailModuleSupport().sendMoneyMailBySystem(role.getId(), Arrays.asList(presentMoney), PresentPointTypeEnum.限时产出活动, activity.mailTitle, activity.mailContent);
		// 记录流水日志
		FlowManager.logOther(role.getId(), OtherFlowTypeEnum.充值优惠限时活动, returnValue + "钻石已发邮件");

		_LOGGER.warn("限时充值优惠,:roleId=,{},roleName=,{},返点数=,{},payOrder=,{}", role.getId(), role.getName(), returnValue, chargeData.payOrder.toString());
	}

	class PayOrderDealResultImpl implements PayOrderDealResult {

		private PayOrder payOrder;
		private int result;// 处理结果，0表示成功，1表示失败原因1... TODO 待定义

		PayOrderDealResultImpl(PayOrder payOrder) {
			this.payOrder = payOrder;
		}

		@Override
		public int getResult() {
			return result;
		}

		@Override
		public PayOrder getPayOrder() {
			return payOrder;
		}
	}
}
