package com.kola.kmp.logic.competition.message;

import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.competition.teampvp.ITeamPVPTeamMember;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPManager;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPMsgCenter;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPTeam;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPTeam.KTeamPVPTeamMember;
import com.kola.kmp.logic.flow.FlowManager;
import com.kola.kmp.logic.flow.FlowManager.OtherFlowTypeEnum;
import com.kola.kmp.logic.item.message.KPushItemsMsg;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.CompetitionTips;
import com.kola.kmp.protocol.competition.KCompetitionProtocol;

/**
 * 
 * @author PERRY CHAN
 */
public class KCompetitionQuitTeamMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KCompetitionQuitTeamMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return KCompetitionProtocol.CM_REQUEST_QUIT_TEAM;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(msgEvent.getPlayerSession());
		if (role != null) {
			KTeamPVPTeam team = KTeamPVPManager.getTeamByRoleId(role.getId());
			if (team != null) {
				team.processQuitTeam(role.getId(), false);
				FlowManager.logOther(role.getId(), OtherFlowTypeEnum.战队竞技, CompetitionTips.getTipsFlowQuitTeam(role.getName(), team.getUUID()));
				KSupportFactory.getTeamPVPSupport().notifyRoleTeamDataChange(role);
			}
		}
		KGameMessage msg = KGame.newLogicMessage(KCompetitionProtocol.SM_RESPOND_QUIT_TEAM_SUCCESS);
		msgEvent.getPlayerSession().send(msg);
		
		// 同步自己的id给好友
		List<Long> allFriendIds = KSupportFactory.getRelationShipModuleSupport().getAllFriends(role.getId());
		long currentId;
		KTeamPVPTeam tempTeam;
		KTeamPVPTeamMember member;
		KGameMessage statusMsg = KTeamPVPMsgCenter.createUpdateFriendStatusMsg(role, true, null);
		int lastIndex = allFriendIds.size() - 1;
		for (int i = 0; i < allFriendIds.size(); i++) {
			currentId = allFriendIds.get(i);
			tempTeam = KTeamPVPManager.getTeamByRoleId(currentId);
			if (tempTeam != null) {
				member = tempTeam.getMember(currentId);
				if (member.hasOpen()) {
//					KTeamPVPMsgCenter.syncFriendId(currentId, role.getId(), true);
					KSupportFactory.getRoleModuleSupport().sendMsg(currentId, statusMsg);
					if(i < lastIndex) {
						statusMsg = statusMsg.duplicate();
					}
				}
			}
		}
	}

}
