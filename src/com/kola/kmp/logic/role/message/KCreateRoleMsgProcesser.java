package com.kola.kmp.logic.role.message;

import static com.kola.kmp.protocol.role.KRoleProtocol.CM_SYNC_CREATE_PLAYERROLE;
import static com.kola.kmp.protocol.role.KRoleProtocol.SM_SYNC_CREATE_PLAYERROLE;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.role.KRoleModuleConfig;
import com.kola.kmp.logic.role.KRoleModuleManager;

/**
 * 
 * @author PERRY CHAN
 */
public class KCreateRoleMsgProcesser implements GameMessageProcesser {
	
	@Override
	public GameMessageProcesser newInstance() {
		return new KCreateRoleMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_SYNC_CREATE_PLAYERROLE;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		String name = msgEvent.getMessage().readUtf8String();
		int templateId = msgEvent.getMessage().readInt();
		KActionResult<Long> result = KRoleModuleManager.createRole(session, name, templateId, KRoleModuleConfig.isJoinGameAfterCreate(), "");
		if (result.success) {
			result.tips = "";
		} else {
			result.attachment = -1l;
		}
		KGameMessage respMsg = KGame.newLogicMessage(SM_SYNC_CREATE_PLAYERROLE);
		respMsg.writeUtf8String(result.tips);
		respMsg.writeLong(result.attachment);
		session.send(respMsg);
		if (result.success && !KRoleModuleConfig.isJoinGameAfterCreate()) {
			KRoleServerMsgPusher.sendRoleList(session);
		}
	}

}
