package com.kola.kmp.logic.shop.message;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.fashion.KFashionLogic;
import com.kola.kmp.logic.fashion.message.KPushFashionsMsg;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.FashionResult_Buy;
import com.kola.kmp.logic.util.ResultStructs.FashionResult_Buys;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.shop.KShopProtocol;

public class KBuyFashionsMsg implements GameMessageProcesser, KShopProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KBuyFashionsMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_BUY_FASHIONS;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		List<Integer> fashionIds = new ArrayList<Integer>();
		int size = msg.readByte();
		for (int i = 0; i < size; i++) {
			fashionIds.add(msg.readInt());
		}
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			FashionResult_Buys result = new FashionResult_Buys();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, result, fashionIds);
			return;
		}

		FashionResult_Buys result = KFashionLogic.dealMsg_buyFashions(role, fashionIds);

		dofinally(session, role, result, fashionIds);
	}

	private static void dofinally(KGamePlayerSession session, KRole role, FashionResult_Buys result, List<Integer> fashionIds) {
		// 处理消息的过程
		KGameMessage backMsg = KGame.newLogicMessage(SM_BUY_FASHIONS_RESULT);
		backMsg.writeBoolean(result.isSucess);
		backMsg.writeUtf8String(result.tips);
		backMsg.writeByte(fashionIds.size());
		for (int fashionId : fashionIds) {
			backMsg.writeInt(fashionId);
			Long effectTime = result.effectTimes.get(fashionId);
			backMsg.writeLong(effectTime == null ? -1 : effectTime);
		}
		session.send(backMsg);

		// 处理各种提示、弹开界面二次确认
		result.doFinally(role);

		if (result.isSucess) {
			// 同步全部时装
			KPushFashionsMsg.pushAllFashions(role);
			// 自动穿戴
			KFashionLogic.autoSelectFashionForMutil(role, fashionIds, true);
		}
	}
}
