package com.kola.kmp.logic.combat.impl;

import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.role.IRoleEventListener;
import com.kola.kmp.logic.role.KRole;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatRoleEventListener implements IRoleEventListener {

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {
		KCombatManager.handleRoleJoinGame(role);
	}

	@Override
	public void notifyRoleLeavedGame(/*KGamePlayerSession session,*/KRole role) {
		KCombatManager.handleRoleLeaveGame(role);
	}

	@Override
	public void notifyRoleCreated(KGamePlayerSession session, KRole role) {
		
	}

	@Override
	public void notifyRoleDeleted(long roleId) {
		
	}

	@Override
	public void notifyRoleLevelUp(KRole role, int preLv) {
		
	}

	@Override
	public void notifyRoleDataPutToCache(KRole role) {
		
	}
}
