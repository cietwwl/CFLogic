package com.kola.kmp.logic.shop.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.item.KItem;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult_Ext;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_Item;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.shop.KShopProtocol;

public class KBuyItemMsg implements GameMessageProcesser, KShopProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KBuyItemMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_BUY_ITEM;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		String itemCode = msg.readUtf8String();
		int buyCount = msg.readInt();
		// -------------
		CommonResult_Ext result = null;
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			result = new ItemResult_Item();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
		} else {
			result = KItemLogic.dealMsg_buyItem(role, itemCode, buyCount);
		}
		// -------------
		// 处理消息的过程
		KGameMessage backMsg = KGame.newLogicMessage(SM_BUY_ITEM_RESULT);
		backMsg.writeBoolean(result.isSucess);
		backMsg.writeUtf8String(result.tips);
		backMsg.writeUtf8String(itemCode);
		backMsg.writeInt(buyCount);
		session.send(backMsg);
		
		// 处理各种提示、弹开界面二次确认
		result.doFinally(role);
	}
}
