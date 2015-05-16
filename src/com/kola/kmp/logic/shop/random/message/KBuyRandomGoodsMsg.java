package com.kola.kmp.logic.shop.random.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.other.KUseFunctionTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.shop.random.KRandomShopCenter;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult_Ext;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.shop.KShopProtocol;

public class KBuyRandomGoodsMsg implements GameMessageProcesser, KShopProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KBuyRandomGoodsMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_BUY_RANDOMGOODS;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		byte nowGoodsType = msg.readByte();
		int goodsId = msg.readInt();
		// -------------
		CommonResult_Ext result = null;
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			result = new CommonResult_Ext();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
		} else {
			result = KRandomShopCenter.dealMsg_buyRandomGoods(role, nowGoodsType, goodsId);
		}
		// -------------
		// 处理消息的过程
		KGameMessage backMsg = KGame.newLogicMessage(SM_BUY_RANDOMGOODS_RESULT);
		backMsg.writeBoolean(result.isSucess);
		backMsg.writeUtf8String(result.tips);
		backMsg.writeByte(nowGoodsType);
		backMsg.writeInt(goodsId);
		session.send(backMsg);
		
		// 处理各种提示、弹开界面二次确认
		result.doFinally(role);
		
		if(result.isSucess){
			// 通知日常任务
			KSupportFactory.getMissionSupport().notifyUseFunction(role, KUseFunctionTypeEnum.神秘商店购买道具);
		}
	}
}
