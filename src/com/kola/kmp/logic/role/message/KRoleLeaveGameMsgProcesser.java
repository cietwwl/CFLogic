package com.kola.kmp.logic.role.message;

import static com.kola.kmp.protocol.role.KRoleProtocol.CM_PLAYERROLE_LEAVE_GAME;

import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.role.RoleModuleFactory;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 
 * @author PERRY CHAN
 */
public class KRoleLeaveGameMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KRoleLeaveGameMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_PLAYERROLE_LEAVE_GAME;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(msgEvent.getPlayerSession());
		if(role != null) {
			RoleModuleFactory.getRoleModule().roleLeaveGame(role.getId());
		}
	}

}
