package com.kola.kmp.logic.gamble.message;
import static com.kola.kmp.protocol.gamble.KGambleProtocol.CM_THROW_DICE;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gamble.KGambleModule;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KThrowDiceMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KThrowDiceMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_THROW_DICE;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		boolean isUseAllCount = msg.readBoolean();
		
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		KGambleModule.getWishSystemManager().processThrowDice(role, isUseAllCount);
	}

}
