package com.kola.kmp.logic.role.message;

import static com.kola.kmp.protocol.role.KRoleProtocol.CM_SYNC_GET_PLAYERROLE_LIST;

import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;

/**
 * 
 * @author PERRY CHAN
 */
public class KGetRoleListMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KGetRoleListMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_SYNC_GET_PLAYERROLE_LIST;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KRoleServerMsgPusher.sendRoleList(session);
	}

}
