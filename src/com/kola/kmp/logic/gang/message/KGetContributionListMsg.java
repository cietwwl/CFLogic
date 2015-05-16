package com.kola.kmp.logic.gang.message;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gang.KGangMsgPackCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.gang.KGangProtocol;

public class KGetContributionListMsg implements GameMessageProcesser, KGangProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KGetContributionListMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GANG_GET_CONTRIBUTION_LIST;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		//
		KGameMessage backmsg = KGangMsgPackCenter.genContributionListMsg(SM_GANG_GET_CONTRIBUTION_LIST_RESULT, role);
		if (backmsg != null) {
			session.send(backmsg);
		}
	}
}
