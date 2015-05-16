package com.kola.kmp.logic.activity.message;

import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.activity.worldboss.KWorldBossManager;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.activity.KActivityProtocol;

public class KRequestAutoJoinMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KRequestAutoJoinMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return KActivityProtocol.CM_REQUEST_AUTO_JOIN_WORLD_BOSS;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		boolean flag = msgEvent.getMessage().readBoolean();
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(msgEvent.getPlayerSession());
		KWorldBossManager.processRequestAutoJoin(role, flag, false);
	}

}
