package com.kola.kmp.logic.shop.timehot.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.other.KUseFunctionTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.shop.timehot.KHotShopCenter;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.HotShopResultExt;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.shop.KShopProtocol;

public class KBuyHotGoodsMsg implements GameMessageProcesser, KShopProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KBuyHotGoodsMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_BUY_HOTGOODS;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		byte type = msg.readByte();
		int goodsId = msg.readInt();
		// -------------
		HotShopResultExt result = null;
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			result = new HotShopResultExt();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
		} else {
			result = KHotShopCenter.dealMsg_buyHotGoods(role, type, goodsId);
		}
		// -------------
		// 处理消息的过程
		KGameMessage backMsg = KGame.newLogicMessage(SM_BUY_HOTOODS_RESULT);
		backMsg.writeBoolean(result.isSucess);
		backMsg.writeUtf8String(result.tips);
		backMsg.writeByte(type);
		backMsg.writeInt(goodsId);
		backMsg.writeBoolean(result.isFind);
		if(result.isFind){
			backMsg.writeShort(result.releaseTime);
			backMsg.writeInt(result.releaseWorldTime);
		}
		session.send(backMsg);
		
		// 处理各种提示、弹开界面二次确认
		result.doFinally(role);
		
//		if(result.isSucess){
//			// 通知日常任务
//			KSupportFactory.getMissionSupport().notifyUseFunction(role, KUseFunctionTypeEnum.神秘商店购买道具);
//		}
	}
}
