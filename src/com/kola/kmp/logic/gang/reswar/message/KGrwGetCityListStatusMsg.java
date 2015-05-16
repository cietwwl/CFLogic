package com.kola.kmp.logic.gang.reswar.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gang.reswar.KResWarMsgPackCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.GangResultExt;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.gang.reswar.KGangResWarProtocol;

public class KGrwGetCityListStatusMsg implements GameMessageProcesser, KGangResWarProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KGrwGetCityListStatusMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GANGRW_GET_CITYLIST_STATUS;
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
		pushMsg(session, role);
	}
	
	public static void pushMsg(KGamePlayerSession session, KRole role){
		KGameMessage backMsg = KGame.newLogicMessage(SM_GANGRW_GET_CITYLIST_STATUS_RESULT);
		KResWarMsgPackCenter.packCityListStatus(backMsg, role);
		session.send(backMsg);
	}
}
