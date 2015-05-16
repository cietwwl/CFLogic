package com.kola.kmp.logic.activity;

import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.activity.mineral.KDigMineralActivityManager;
import com.kola.kmp.logic.activity.worldboss.KWorldBossManager;
import com.kola.kmp.logic.role.IRoleEventListener;
import com.kola.kmp.logic.role.KRole;

/**
 * 
 * @author PERRY CHAN
 */
public class KActivityModuleRoleEventListener implements IRoleEventListener {

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {
		KActivityModule.getActivityManager().notifyRoleJoinedGame(role);
	}

	@Override
	public void notifyRoleLeavedGame(/*KGamePlayerSession session,*/KRole role) {
		KActivityModule.getActivityManager().notifyRoleLeavedGame(role);
	}

	@Override
	public void notifyRoleCreated(KGamePlayerSession session, KRole role) {
		
	}

	@Override
	public void notifyRoleDeleted(long roleId) {
		KDigMineralActivityManager.notifyRoleDeleted(roleId);
		KWorldBossManager.notifyRoleDeleted(roleId);
	}

	@Override
	public void notifyRoleLevelUp(KRole role, int preLv) {
		
	}
	
	@Override
	public void notifyRoleDataPutToCache(KRole role) {
		KWorldBossManager.notifyRoleDataPutToCache(role);
	}

}
