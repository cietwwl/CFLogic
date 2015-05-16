package com.kola.kmp.logic.gamble.message;

import static com.kola.kmp.protocol.gamble.KGambleProtocol.CM_WISH2;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gamble.KGambleModule;
import com.kola.kmp.logic.gamble.KGambleRoleExtCACreator;
import com.kola.kmp.logic.gamble.KGambleRoleExtData;
import com.kola.kmp.logic.gamble.wish.KRoleWishData;
import com.kola.kmp.logic.gamble.wish.KWishSystemManager;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KWish2MsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KWish2MsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_WISH2;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		byte poolType = msg.readByte();
		boolean isUse10Count = msg.readBoolean();
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);

		KGambleModule.getWish2Manager().processWish(role, poolType, isUse10Count);
	}

}
