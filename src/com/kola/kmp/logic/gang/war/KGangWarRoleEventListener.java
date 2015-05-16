package com.kola.kmp.logic.gang.war;

import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.gang.war.GangWarRound.GangRace;
import com.kola.kmp.logic.gang.war.message.KGWPushMsg;
import com.kola.kmp.logic.role.IRoleEventListener;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2013-1-29 下午5:50:54
 * </pre>
 */
public class KGangWarRoleEventListener implements IRoleEventListener {

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {
		KGWPushMsg.pushConstance(session, role);
		//
		{
			if (GangWarStatusManager.isCanJoinMap()) {
				GangWarRound round = GangWarDataCenter.getNowRoundData();
				if (round != null) {
					long gangId = KSupportFactory.getGangSupport().getGangIdByRoleId(role.getId());
					if (gangId > 1) {
						GangRace race = round.getRaceByGangId(gangId);
						if (race != null) {
							KGWPushMsg.syncGangWarIcon(role, true);
						}
					}
				}
			}
		}
	}

	@Override
	public void notifyRoleLeavedGame(/* KGamePlayerSession session, */KRole role) {
		GangWarLogic.dealMsg_leaveRace(role, false);
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
