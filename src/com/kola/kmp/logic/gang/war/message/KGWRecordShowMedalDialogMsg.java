package com.kola.kmp.logic.gang.war.message;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gang.KGangLogic;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.gang.war.KGangWarProtocol;

public class KGWRecordShowMedalDialogMsg implements GameMessageProcesser, KGangWarProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KGWRecordShowMedalDialogMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GW_RECORD_SHOWMEDAL;
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
		KGangLogic.recordShowMedalDialog(role);
	}

}
