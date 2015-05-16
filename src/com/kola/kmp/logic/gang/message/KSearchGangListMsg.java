package com.kola.kmp.logic.gang.message;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gang.KGangConfig;
import com.kola.kmp.logic.gang.KGangDataManager;
import com.kola.kmp.logic.gang.KGangMsgPackCenter;
//import com.kola.kmp.logic.other.FunctionTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.GangResult;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.gang.KGangProtocol;

public class KSearchGangListMsg implements GameMessageProcesser, KGangProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KSearchGangListMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_SEARCH_GANG;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		String key = msg.readUtf8String();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		// -------------
		KGameMessage backmsg = KGame.newLogicMessage(SM_SEARCH_GANG_RESULT);
		KGangMsgPackCenter.searchAndPackGangList(backmsg, role, key);
		session.send(backmsg);
	}
}
