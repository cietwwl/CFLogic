package com.kola.kmp.logic.role.message;

import static com.kola.kmp.protocol.role.KRoleProtocol.CM_SYNC_DELETE_PLAYERROLE;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.role.KRoleModuleManager;

/**
 * 
 * @author PERRY CHAN
 */
public class KDeleteRoleMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KDeleteRoleMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_SYNC_DELETE_PLAYERROLE;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		long roleId = msgEvent.getMessage().readLong();
		String result = KRoleModuleManager.deletePlayerRole(msgEvent.getPlayerSession(), roleId);
		boolean success = (result == null);
		KGameMessage msg = KGame.newLogicMessage(CM_SYNC_DELETE_PLAYERROLE);
		msg.writeLong(roleId);
		msg.writeBoolean(success);
		if(!success) {
			msg.writeUtf8String(result);
		}
		msgEvent.getPlayerSession().send(msg);
	}

}
