package com.kola.kmp.logic.gang.reswar.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gang.reswar.KResWarMsgPackCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.gang.reswar.KGangResWarProtocol;

public class KGrwGetTempsMsg implements GameMessageProcesser, KGangResWarProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KGrwGetTempsMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GANGRW_GET_TEMPS;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			return;
		}
		
		KGameMessage backMsg = KGame.newLogicMessage(SM_GANGRW_GET_TEMPS_RESULT);
		KResWarMsgPackCenter.packTempDatas(backMsg);
		session.send(backMsg);
		//
		KGrwGetCityListStatusMsg.pushMsg(session, role);
	}
}
