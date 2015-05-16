package com.kola.kmp.logic.competition.message;

import java.util.concurrent.TimeUnit;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPConfig;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPManager;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPTeam;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPTeam.KTeamPVPTeamMember;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.CompetitionTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.competition.KCompetitionProtocol;

public class KRequestKickTeamMemberMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KRequestKickTeamMemberMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return KCompetitionProtocol.CM_REQUEST_KICK_TEAM_MEMBER;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(msgEvent.getPlayerSession());
		String tips;
		boolean sendDialog = false;
		if(role != null) {
			KTeamPVPTeam team = KTeamPVPManager.getTeamByRoleId(role.getId());
			if(team != null) {
				KTeamPVPTeamMember member = team.getMember(role.getId());
				if (member.isTeamLeader()) {
					// 处理踢除队友
					KActionResult<Long> result = team.processKickTeamMember(role.getId());
					if (result.success) {
						tips = CompetitionTips.getTipsKickMemberSuccess();
						KGameMessage msg = KGame.newLogicMessage(KCompetitionProtocol.SM_RESPOND_QUIT_TEAM_SUCCESS);
						KSupportFactory.getRoleModuleSupport().sendMsg(result.attachment, msg);
						KDialogService.sendSimpleDialog(result.attachment, "", CompetitionTips.getTipsYouAreKickedOut());
					} else {
						tips = result.tips;
					}
				} else {
					// 处理弹劾
					long[] roleIds = team.getAllMemberIds();
					KTeamPVPTeamMember captain = null;
					for(int i = 0; i < roleIds.length; i++) {
						if(roleIds[i] != role.getId()) {
							captain = team.getMember(roleIds[i]);
						}
					}
					if (captain != null) {
						KRole captainRole = KSupportFactory.getRoleModuleSupport().getRole(captain.getId());
						boolean kickCaptainSuccess = false;
						if(captainRole == null) {
							team.processQuitTeam(captain.getId(), false);
							tips = CompetitionTips.getTipsKickCaptainSuccess();
						} else {
							if (captainRole.isOnline()) {
								tips = CompetitionTips.getTipsKickCaptainFailWithOnline();
							} else {
								long leaveGameTime = System.currentTimeMillis() - captainRole.getLastLeaveGameTime();
								if (leaveGameTime < KTeamPVPConfig.getMaxOfflineMillis()) {
									float hour = (int) TimeUnit.HOURS.convert(leaveGameTime, TimeUnit.MILLISECONDS);
									if (hour == 0) {
										hour = 0.1f;
									}
									float leftHour = KTeamPVPConfig.getMaxOfflineHours() - hour;
									tips = CompetitionTips.getTipsKickCaptainProcessing(hour, leftHour);
									sendDialog = true;
								} else {
									team.processQuitTeam(captain.getId(), false);
									tips = CompetitionTips.getTipsKickCaptainSuccess();
									kickCaptainSuccess = true;
								}
							}
						}
						if(kickCaptainSuccess) {
							KSupportFactory.getMailModuleSupport().sendSimpleMailBySystem(captain.getId(), GlobalTips.getTipsDefaultTitle(), CompetitionTips.getTipsCaptainBeKickedNotice(KTeamPVPConfig.getMaxOfflineHours(), role.getName(), team.getTeamName()));
						}
					} else {
						tips = GlobalTips.getTipsServerBusy();
					}
				}
			} else {
				tips = CompetitionTips.getTipsYouAreNotInTeam();
			}
		} else {
			tips = GlobalTips.getTipsServerBusy();
		}
		if (sendDialog) {
			KDialogService.sendSimpleDialog(msgEvent.getPlayerSession(), "", tips);
		} else {
			KDialogService.sendUprisingDialog(msgEvent.getPlayerSession(), tips);
		}
	}

}
