package com.kola.kmp.logic.gm;

import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.gm.message.KGMPushMsg;
import com.kola.kmp.logic.role.IRoleEventListener;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.protocol.gm.ProtocolGs;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2013-1-29 下午5:50:54
 * </pre>
 */
public class KGMRoleEventListener implements IRoleEventListener,ProtocolGs {

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {
		// 忽略
	}

	@Override
	public void notifyRoleLeavedGame(/*KGamePlayerSession session,*/KRole role) {
		KGMPushMsg.pushRoleLeaveToGM(role);
	}

	@Override
	public void notifyRoleCreated(KGamePlayerSession session, KRole role) {
		// 忽略
	}

	@Override
	public void notifyRoleDeleted(long roleId) {
		// 忽略
	}

	@Override
	public void notifyRoleLevelUp(KRole role, int preLv) {
		// 忽略
	}
	
	@Override
	public void notifyRoleDataPutToCache(KRole role) {
		
	}

}
