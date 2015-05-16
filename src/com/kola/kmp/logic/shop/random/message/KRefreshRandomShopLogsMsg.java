package com.kola.kmp.logic.shop.random.message;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.shop.random.KRandomShopMsgPackCenter;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.shop.KShopProtocol;

public class KRefreshRandomShopLogsMsg implements GameMessageProcesser, KShopProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KRefreshRandomShopLogsMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_REFRESH_RANDOMSHOP_LOGS;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		int maxLogsId = msg.readInt();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			return;
		}
		//
		pushMsg(role, maxLogsId);
	}

	public static void pushMsg(KRole role, int maxLogsId) {
		KGameMessage backMsg = KRandomShopMsgPackCenter.packRandomShopLogs(SM_PUSH_RANDOMSHOP_LOGS, maxLogsId);
		if (backMsg != null) {
			role.sendMsg(backMsg);
		}
	}
}
