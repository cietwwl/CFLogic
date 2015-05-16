package com.kola.kmp.logic.activity.message;
import static com.kola.kmp.protocol.activity.KActivityProtocol.CM_REQUEST_ACTIVITY_LIST;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.activity.KActivityManager;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KRequestActivityListMsgProcesser implements GameMessageProcesser{

	@Override
	public GameMessageProcesser newInstance() {
		return new KRequestActivityListMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		
		return CM_REQUEST_ACTIVITY_LIST;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		
		KActivityManager.getInstance().sendAllActivityData(role);
	}

}
