package com.kola.kmp.logic.competition.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPTeam;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPConfig;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPManager;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPMsgCenter;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPTeam.KTeamPVPTeamMember;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.competition.KCompetitionProtocol;

/**
 * 
 * @author PERRY CHAN
 */
public class KGetTeamPVPInfoMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KGetTeamPVPInfoMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return KCompetitionProtocol.CM_REQUEST_TEAM_PVP_INFO;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(msgEvent.getPlayerSession());
		if(role != null) {
			KTeamPVPTeam team = KTeamPVPManager.getTeamByRoleId(role.getId());
//			KGameMessage msg = KGame.newLogicMessage(KCompetitionProtocol.SM_RESPOND_TEAM_PVP_INFO);
//			boolean hasTeam = team != null;
//			msg.writeInt(KCompetitionTeamPVPConfig.getPriceForCreateTeam());
//			msg.writeBoolean(hasTeam);
//			if(hasTeam) {
//				team.packDataToMsg(msg, role.getId());
//			}
			KGameMessage msg = KTeamPVPMsgCenter.createTeamInfoMessage(team, role.getId());
			msgEvent.getPlayerSession().send(msg);
			
			if (team != null) {
				KTeamPVPTeamMember member = team.getMember(role.getId());
				if (member.getNotice() != null) {
					KGameMessage noticeMsg = KTeamPVPMsgCenter.createUpdateNoticeMsg(member.getNotice());
					msgEvent.getPlayerSession().send(noticeMsg);
					member.setNotice(null);
				}
			}
		}
	}

}
