package com.kola.kmp.logic.competition;

import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPManager;
import com.kola.kmp.logic.role.IRoleEventListener;
import com.kola.kmp.logic.role.KRole;

public class KCompetitionRoleEventListener implements IRoleEventListener{

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {
		KCompetitionModule.getCompetitionManager().notifyRoleJoinedGame(role);
		KTeamPVPManager.notifyRoleJoinedGame(role);
	}

	@Override
	public void notifyRoleLeavedGame(/*KGamePlayerSession session,*/KRole role) {
		KCompetitionModule.getCompetitionManager().notifyRoleLeaveGame(role);
		KTeamPVPManager.notifyRoleLeavedGame(role);
	}

	@Override
	public void notifyRoleCreated(KGamePlayerSession session, KRole role) {
		
	}

	@Override
	public void notifyRoleDeleted(long roleId) {
		KCompetitionModule.getCompetitionManager().notifyRoleDelete(roleId);
		KTeamPVPManager.notifyRoleDeleted(roleId);
	}

	@Override
	public void notifyRoleLevelUp(KRole role, int preLv) {
		KCompetitionModule.getCompetitionManager().notifyRoleLevelUp(role, preLv, role.getLevel());
		KTeamPVPManager.notifyRoleUpgraded(role, preLv);
	}
	
	@Override
	public void notifyRoleDataPutToCache(KRole role) {
		
	}

}
