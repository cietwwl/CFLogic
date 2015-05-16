package com.kola.kmp.logic.competition.teampvp;

import com.kola.kmp.logic.role.KRole;

/**
 * 
 * @author PERRY CHAN
 */
public class KTeamPVPInvitation {

	public final long teamId;
	public final String teamName;
	public final String captainName;
	public final int teamBattlePower;
	public final int captainLevel;
	public final int captainHeadResId;
	public final int captainResId;
	public final String danRankName;
	
	public KTeamPVPInvitation(KRole captain, KTeamPVPTeam team) {
		this.teamId = team.getId();
		this.teamName = team.getTeamName();
		this.danRankName = team.getDanStageInfo().danStageName;
		this.teamBattlePower = team.getTeamBattlePower();
		this.captainName = captain.getName();
		this.captainLevel = captain.getLevel();
		this.captainHeadResId = captain.getHeadResId();
		this.captainResId = captain.getHeadResId();
	}
}
