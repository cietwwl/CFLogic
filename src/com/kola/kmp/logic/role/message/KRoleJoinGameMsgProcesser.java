package com.kola.kmp.logic.role.message;

import static com.kola.kmp.protocol.role.KRoleProtocol.CM_SYNC_PLAYERROLE_JOIN_GAME;

import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.role.RoleModuleFactory;
import com.kola.kgame.cache.util.GameMessageProcesser;

/**
 * 
 * @author PERRY CHAN
 */
public class KRoleJoinGameMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KRoleJoinGameMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_SYNC_PLAYERROLE_JOIN_GAME;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		long roleId = msgEvent.getMessage().readLong();
		RoleModuleFactory.getRoleModule().roleJoinGame(roleId);
	}
	
	public static void main(String[] args) {
		byte[] b = new byte[]{0, 0, 0, 51, -80, 59, 44, 6};
		int temp = 64;
		int result = 0;
		for(int i = 0; i < b.length; i++) {
			int tt = temp - i * 8;
			result += (b[i] << tt);
		}
		System.out.println(result);
	}

}
