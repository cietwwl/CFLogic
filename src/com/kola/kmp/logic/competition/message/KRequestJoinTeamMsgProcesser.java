package com.kola.kmp.logic.competition.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPTeam;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPManager;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPMsgCenter;
import com.kola.kmp.logic.flow.FlowManager;
import com.kola.kmp.logic.flow.FlowManager.OtherFlowTypeEnum;
import com.kola.kmp.logic.item.message.KPushItemsMsg;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.CompetitionTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.competition.KCompetitionProtocol;

/**
 * 
 * @author PERRY CHAN
 */
public class KRequestJoinTeamMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KRequestJoinTeamMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return KCompetitionProtocol.CM_REQUEST_JOIN_TEAM;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		boolean accept = msgEvent.getMessage().readBoolean();
		long teamId = msgEvent.getMessage().readLong();
		KTeamPVPTeam team = KTeamPVPManager.getTeamByTeamId(teamId);
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(msgEvent.getPlayerSession());
		KTeamPVPTeam myTeam = KTeamPVPManager.getTeamByRoleId(role.getId());
		boolean success = false;
		String tips;
		KRole captain = null;
		if(myTeam != null) {
			tips = CompetitionTips.getTipsYouAreInTeam();
		} else if (team != null) {
			captain = KSupportFactory.getRoleModuleSupport().getRole(team.getAllMemberIds()[0]);
			if (accept) {
				success = team.processAcceptInvitation(role);
				if (success) {
					tips = CompetitionTips.getTipsJoinTeamSuccess();
					KTeamPVPManager.removeInvitation(teamId, role, true);
				} else {
					tips = CompetitionTips.getTipsTargetTeamIsFull();
				}
			} else {
				team.notifyRejectInvitation(role.getId());
				KTeamPVPManager.removeInvitation(teamId, role, false);
				if (captain.isOnline()) {
					KSupportFactory.getChatSupport().sendChatToRole(CompetitionTips.getTipsRejectInvitation(role.getName()), captain.getId());
				} else {
					KSupportFactory.getMailModuleSupport().sendSimpleMailBySystem(captain.getId(), GlobalTips.getTipsDefaultTitle(), CompetitionTips.getTipsRejectInvitation(role.getName()));
				}
				return;
			}
		} else {
			tips = CompetitionTips.getTipsNoSuchTeam();
		}
		KGameMessage msg = KGame.newLogicMessage(KCompetitionProtocol.SM_RESPOND_JOIN_TEAM);
		msg.writeBoolean(success);
		msg.writeUtf8String(tips);
		msgEvent.getPlayerSession().send(msg);
		if(success) {
//			msg = KTeamPVPMsgCenter.createTeamInfoMessage(team, captain.getId());
//			captain.sendMsg(msg);
			KGameMessage newMemberMsg = KGame.newLogicMessage(KCompetitionProtocol.SM_NOTIFY_TEAM_MEMBER_CHANGE);
			newMemberMsg.writeBoolean(true);
			KTeamPVPMsgCenter.packMemberDataToMsg(newMemberMsg, team.getMember(role.getId()));
			captain.sendMsg(newMemberMsg);
			
			KGameMessage battlePowerMsg = KTeamPVPMsgCenter.createBattlePowerSyncMsg(team);
			captain.sendMsg(battlePowerMsg);
			
			KGameMessage teamInfoMsg = KTeamPVPMsgCenter.createTeamInfoMessage(team, role.getId());
			msgEvent.getPlayerSession().send(teamInfoMsg);
			
			KSupportFactory.getTeamPVPSupport().notifyRoleTeamDataChange(role);
			
			FlowManager.logOther(role.getId(), OtherFlowTypeEnum.战队竞技, CompetitionTips.getTipsFlowJoinTeam(role.getName(), team.getUUID()));
		}
	}

}
