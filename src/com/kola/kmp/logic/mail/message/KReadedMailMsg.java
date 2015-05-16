package com.kola.kmp.logic.mail.message;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.mail.KMailLogic;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.mail.KMailProtocol;

public class KReadedMailMsg implements GameMessageProcesser, KMailProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KReadedMailMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_READED_MAIL;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		long mailId = msg.readLong();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
		} else {
			KMailLogic.dealMsg_readedMail(role.getId(), mailId);
		}
	}
}
