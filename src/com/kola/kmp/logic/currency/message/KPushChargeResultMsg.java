package com.kola.kmp.logic.currency.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.currency.KCurrencyConfig;
import com.kola.kmp.logic.currency.KPaymentListener.ChargeOrderStruct;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.protocol.currency.KCurrencyProtocol;

public class KPushChargeResultMsg implements KCurrencyProtocol {

	/**
	 * <pre>
	 * 角色充值成功时，通知客户端
	 * 
	 * @param role
	 * @param goodsName
	 * @param goodsPrice
	 * @param goodsNum
	 * @param payTypeCode
	 * @author CamusHuang
	 * @creation 2014-5-17 下午5:23:48
	 * </pre>
	 */
	public static void sendMsg(KRole role, ChargeOrderStruct chargeData) {
		
		if(!KCurrencyConfig.getInstance().IS_PUSH_CHARGERESULT_TO_CLIENT){
			return;
		}
		
		/**
		 * <pre>
		 * 服务器返回充值结果
		 * String 商品名称
		 * float 商品单价
		 * int 商品数量
		 * String 支付货币代号（比如：CNY 中国  HKD 香港 TWD 台湾 http://en.wikipedia.org/wiki/ISO_4217 可以查询）
		 * String 用户账号(add 20141125)
		 * String 订单号(add 20141125)
		 * String 支付类型(add 20141125)
		 * </pre>
		 */
		//, , , 
		KGameMessage msg = KGame.newLogicMessage(SM_ON_PAY_RESULT);
		msg.writeUtf8String(chargeData.payOrder.getGoodsName());
//		msg.writeFloat(chargeData.payOrder.getGoodsPrice());
//		msg.writeInt(chargeData.payOrder.getGoodsCount());
		msg.writeFloat(chargeData.RMB);
		msg.writeInt(1);
		
		msg.writeUtf8String(chargeData.payOrder.getPayCurrencyCode());
		
		msg.writeUtf8String(chargeData.payOrder.getPromoMask());
		msg.writeUtf8String(chargeData.payOrder.getOrderId());
		msg.writeUtf8String(chargeData.payOrder.getPayWay());
		role.sendMsg(msg);
	}

}
