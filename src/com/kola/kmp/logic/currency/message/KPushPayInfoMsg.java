package com.kola.kmp.logic.currency.message;

import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.currency.KCurrencyAccountSet;
import com.kola.kmp.logic.currency.KCurrencyDataManager;
import com.kola.kmp.logic.currency.KCurrencyModuleExtension;
import com.kola.kmp.logic.currency.KCurrencyDataManager.ChargeInfoManager.ChargeInfoStruct;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.protocol.currency.KCurrencyProtocol;

public class KPushPayInfoMsg implements KCurrencyProtocol {

	/**
	 * <pre>
	 * 角色充值成功时，通知客户端
	 * 
	 * 
	 * @param family
	 * @author CamusHuang
	 * @creation 2013-12-8 下午5:03:48
	 * </pre>
	 */
	public static void sendMsg(KRole role) {
		/**
		 * <pre>
		 * String 货币(元、美元...)
		 * int 档位数量
		 * for (档位数量) {
		 *   String 商品价格
		 *   String 商品名称 （如果需要赠送可填：5000+100元宝）
		 *   String 赠送提示
		 * 	 String 扩展内容
		 * 	 boolean 是否热卖
		 *	 short 首充返点比例(100表示返100%即首充双倍，0表示没有首充返点或者已经消耗掉返点机会)
		 *	 boolean 是否月卡
		 *	 if(是月卡){
		 * 		int 月卡连续发奖多少天
		 * 		int 月卡每天发奖多少钻石
		 *	 }
		 * }
		 * String 温馨提示（比如：温馨提示：非本界面指定的充值金额（如31元）不享受元宝赠送喔）
		 * </pre>
		 */
		
		KCurrencyAccountSet set = KCurrencyModuleExtension.getCurrencyAccountSet(role.getId());
		if(set==null){
			return;
		}
		
		KGameMessage msg = KGame.newLogicMessage(SM_ON_PUSH_PAY_INFO);
		msg.writeUtf8String(KCurrencyDataManager.mChargeInfoManager.getCNY_UNIT());
		List<ChargeInfoStruct> infos = KCurrencyDataManager.mChargeInfoManager.getInfos();
		
		{
			msg.writeInt(infos.size());
			for (ChargeInfoStruct info : infos) {
				msg.writeUtf8String(info.goodsPrice);
				msg.writeUtf8String(info.goodsName);
				msg.writeUtf8String(info.presentTips);
				msg.writeUtf8String(info.ext);
				msg.writeBoolean(info.isHot);
				if(info.returnRateForFirst<1){
					msg.writeShort(0);
				} else if(set.isCatchFirstChargeReturn(info.rmbIngot)){
					msg.writeShort(0);
				} else {
					msg.writeShort(info.returnRateForFirst);
				}
				if(info.isMonthCard()){
					msg.writeBoolean(true);
					msg.writeInt(KCurrencyDataManager.mChargeInfoManager.monthCard.monthCardKeepDays);
					msg.writeInt(KCurrencyDataManager.mChargeInfoManager.monthCard.monthCardIngotForDay);
				} else {
					msg.writeBoolean(false);
				}
			}
		}
		msg.writeUtf8String(KCurrencyDataManager.mChargeInfoManager.getTips());

		role.sendMsg(msg);
	}
}
