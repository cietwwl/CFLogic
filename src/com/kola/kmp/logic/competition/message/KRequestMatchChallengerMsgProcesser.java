package com.kola.kmp.logic.competition.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.competition.teampvp.ITeamPVPTeam;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPTeam;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPManager;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPMsgCenter;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPTeam.KTeamPVPTeamMember;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.CompetitionTips;
import com.kola.kmp.protocol.competition.KCompetitionProtocol;

/**
 * 
 * @author PERRY CHAN
 */
public class KRequestMatchChallengerMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KRequestMatchChallengerMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return KCompetitionProtocol.CM_REQUEST_TEAM_PVP_MATCH_CHALLENGER;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(msgEvent.getPlayerSession());
		KTeamPVPTeam team = KTeamPVPManager.getTeamByRoleId(role.getId());
		ITeamPVPTeam selectedTeam = null;
		boolean hasTeam = false;
		if (team != null) {
//			KTeamPVPTeamMember member = team.getMember(role.getId());
//			if (member.getChallengeLeftCount() > 0) {
				selectedTeam = team.processSelectTeam(role.getId());
				hasTeam = selectedTeam != null;
//			}
		}
		KGameMessage msg = KGame.newLogicMessage(KCompetitionProtocol.SM_RESPOND_TEAM_PVP_MATCH_CHALLENGER);
		msg.writeBoolean(hasTeam);
		if (hasTeam) {
			msg.writeUtf8String(selectedTeam.getTeamName());
			msg.writeByte(selectedTeam.getTeamMembers().size());
			for (int i = 0; i < selectedTeam.getTeamMembers().size(); i++) {
				KTeamPVPMsgCenter.packMemberDataToMsg(msg, selectedTeam.getTeamMembers().get(i));
			}
		} else {
			msg.writeUtf8String(CompetitionTips.getTipsNoTeamMatch());
		}
		msgEvent.getPlayerSession().send(msg);
	}

}
