package com.kola.kmp.logic.competition.teampvp;

import com.kola.kmp.logic.competition.teampvp.KTeamPVPTeam.KTeamPVPTeamMember;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.vip.KRoleVIP;
import com.kola.kmp.logic.vip.KVIPUpLvListener;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;

/**
 * 
 * @author PERRY CHAN
 */
public class KTeamPVPVipEventListener implements KVIPUpLvListener {

	@Override
	public void notifyVIPLevelUp(KRoleVIP vip, int preLv) {
		KTeamPVPTeam team = KTeamPVPManager.getTeamByRoleId(vip.getRoleId());
		if(team != null) {
			KTeamPVPTeamMember member = team.getMember(vip.getRoleId());
			VIPLevelData levelData = KSupportFactory.getVIPModuleSupport().getVIPLevelData(vip.getLv());
			member.notifyVipChange(vip.getLv(), levelData.ladderbuyrmb.length);
		}
	}

}
