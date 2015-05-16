package com.kola.kmp.logic.competition.message;

import java.util.Collections;
import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPTeam;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPConfig;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPManager;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPMsgCenter;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPInvitation;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.CompetitionTips;
import com.kola.kmp.logic.util.tips.RoleTips;
import com.kola.kmp.protocol.competition.KCompetitionProtocol;

/**
 * 
 * @author PERRY CHAN
 */
public class KInviteMemberToTeamMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KInviteMemberToTeamMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return KCompetitionProtocol.CM_REQUEST_INVITE_MEMBER_TO_TEAM;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		long friendId = msgEvent.getMessage().readLong();
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(msgEvent.getPlayerSession());
		String tips;
		boolean success = false;
		KTeamPVPTeam team = KTeamPVPManager.getTeamByRoleId(role.getId());
		if (team == null) {
			tips = CompetitionTips.getTipsYouAreNotInTeam();
		} else if (team.isTeamFull()) {
			tips = CompetitionTips.getTipsTeamIsFull();
		} else if (!KSupportFactory.getRelationShipModuleSupport().isInFriendList(role.getId(), friendId)) {
			tips = CompetitionTips.getTipsYouAreNotFriend();
		} else if (KTeamPVPManager.isInTeam(friendId)) {
			tips = CompetitionTips.getTipsFriendIsInTeam();
		} else {
			KRole targetRole = KSupportFactory.getRoleModuleSupport().getRole(friendId);
			if(targetRole == null) {
				tips = RoleTips.getTipsNoSuchRole();
			} else if (targetRole.getLevel() < KTeamPVPConfig.getJoinMinLevel()) {
				tips = CompetitionTips.getTipsFriendLevelNotReach();
			} else {
				KTeamPVPInvitation invitation = KTeamPVPManager.createInvitation(role, team, targetRole);
				tips = CompetitionTips.getTipsInvitationSent();
				success = true;
				
				KGameMessage invitationMsg = KGame.newLogicMessage(KCompetitionProtocol.SM_NOTIFY_INVITATION_RECEIVE);
				KTeamPVPMsgCenter.packInvitation(invitation, invitationMsg);
				targetRole.sendMsg(invitationMsg);
			}
		}
		List<KActionResult<Long>> allFriends = null;
		if (team != null) {
			allFriends = KTeamPVPManager.getCandidateFriendIds(role.getId(), team);
			if (success) {
				allFriends.remove(friendId);
			}
		} else {
			allFriends = Collections.emptyList();
		}
		KActionResult<Long> temp;
		KGameMessage msg = KGame.newLogicMessage(KCompetitionProtocol.SM_RESPOND_INVITE_MEMBER_TO_TEAM);
		msg.writeBoolean(success);
		msg.writeUtf8String(tips);
		msg.writeByte(allFriends.size());
		for (int i = 0; i < allFriends.size(); i++) {
			temp = allFriends.get(i);
			msg.writeLong(temp.attachment);
			msg.writeBoolean(temp.success);
			if (!temp.success) {
				msg.writeUtf8String(temp.tips);
			}
		}
		msgEvent.getPlayerSession().send(msg);
	}

}
