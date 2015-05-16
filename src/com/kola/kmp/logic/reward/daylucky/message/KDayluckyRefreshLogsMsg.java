package com.kola.kmp.logic.reward.daylucky.message;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.reward.daylucky.KDayluckyCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.reward.KRewardProtocol;

public class KDayluckyRefreshLogsMsg implements GameMessageProcesser, KRewardProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KDayluckyRefreshLogsMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_DAYLUCK_REFRESH_LOGS;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		int maxLogsId = msg.readInt();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			return;
		}
		//
		KGameMessage backMsg = KDayluckyCenter.packLogs(SM_DAYLUCK_REFRESH_LOGS_RESULT, maxLogsId);
		if (backMsg != null) {
			role.sendMsg(backMsg);
		}
	}
}
