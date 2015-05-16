package com.kola.kmp.logic.gamble.message;

import static com.kola.kmp.protocol.gamble.KGambleProtocol.CM_REQUEST_UPDATE_WISH_DATA;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gamble.KGambleModule;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KRequestUpdateWishDataMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KRequestUpdateWishDataMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_REQUEST_UPDATE_WISH_DATA;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		KGambleModule.getWishSystemManager().sendUpdateWishData(role, null);
	}

}
