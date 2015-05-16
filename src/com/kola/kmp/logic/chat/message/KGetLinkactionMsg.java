package com.kola.kmp.logic.chat.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.chat.KChatLogic;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.chat.KChatProtocol;

public class KGetLinkactionMsg implements GameMessageProcesser, KChatProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KGetLinkactionMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GET_LINKACTION;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		byte actiontype = msg.readByte();
		String actionscript = msg.readUtf8String();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			doFinally(session, actiontype, actionscript, null);
			return;
		}
		// -------------
		byte[] attData = KChatLogic.dealMsg_getLinkedAction(actiontype, actionscript);
		doFinally(session, actiontype, actionscript, attData);
	}

	private void doFinally(KGamePlayerSession session, byte actiontype, String actionscript, byte[] attData) {
		// 处理消息的过程
		KGameMessage backmsg = KGame.newLogicMessage(SM_GET_LINKACTION_RESULT);
		backmsg.writeByte(actiontype);
		backmsg.writeUtf8String(actionscript);
		if (attData == null || attData.length < 1) {
			backmsg.writeInt(0);
		} else {
			backmsg.writeInt(attData.length);
			backmsg.writeBytes(attData);
		}
		session.send(backmsg);
	}
}
