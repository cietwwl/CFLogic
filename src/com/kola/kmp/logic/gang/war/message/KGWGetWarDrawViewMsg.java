package com.kola.kmp.logic.gang.war.message;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.logging.KGameLogger;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gang.war.KGangWarMsgPackCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.gang.war.KGangWarProtocol;

public class KGWGetWarDrawViewMsg implements GameMessageProcesser, KGangWarProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KGWGetWarDrawViewMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GW_WAR_DRAWVIEW;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		
		KGameMessage backmsg = KGame.newLogicMessage(SM_GW_WAR_DRAWVIEW_RESULT);
		KGangWarMsgPackCenter.packWarDrawView(backmsg, role);
		session.send(backmsg);
	}
}
