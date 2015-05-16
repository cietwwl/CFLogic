package com.kola.kmp.logic.gang.reswar;

import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.role.IRoleEventListener;
import com.kola.kmp.logic.role.KRole;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2013-1-29 下午5:50:54
 * </pre>
 */
public class KResWarRoleEventListener implements IRoleEventListener {

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {
		// CTODO
	}

	@Override
	public void notifyRoleLeavedGame(/*KGamePlayerSession session,*/KRole role) {
		// CTODO 未测试，暂时屏蔽		
		ResWarLogic.notifyRoleLeave(role.getId());
	}

	@Override
	public void notifyRoleCreated(KGamePlayerSession session, KRole role) {
		// CTODO
	}

	@Override
	public void notifyRoleDeleted(long roleId) {
		// CTODO 
	}

	@Override
	public void notifyRoleLevelUp(KRole role, int preLv) {
		// CTODO
	}
	
	@Override
	public void notifyRoleDataPutToCache(KRole role) {
		
	}

}
