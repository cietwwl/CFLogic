package com.kola.kmp.logic.competition.teampvp;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPTeam.KTeamPVPTeamMember;
import com.kola.kmp.logic.other.KEquipmentTypeEnum;
import com.kola.kmp.logic.other.KItemQualityEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.IRoleEquipShowData;
import com.kola.kmp.logic.util.tips.CompetitionTips;
import com.kola.kmp.protocol.competition.KCompetitionProtocol;

/**
 * 
 * @author PERRY CHAN
 */
public class KTeamPVPMsgCenter {
	
	private static void sendMsgToAllTeamMember(KTeamPVPTeam team, KGameMessage msg) {
		List<ITeamPVPTeamMember> memberList = team.getTeamMembers();
		int lastIndex = memberList.size() - 1;
		KTeamPVPTeamMember member;
		for(int i = 0; i < memberList.size(); i++) {
			member = (KTeamPVPTeamMember)memberList.get(i);
			if (member.hasOpen()) {
				KSupportFactory.getRoleModuleSupport().sendMsg(member.getId(), msg);
				if (i < lastIndex) {
					msg = msg.duplicate();
				}
			}
		}
	}
	
	static void notifyRewardRefurbish(KTeamPVPTeam team) {
		KGameMessage msg = KGame.newLogicMessage(KCompetitionProtocol.SM_NOTIFY_UPDADE_TEAM_PVP_REWARD);
		msg.writeBoolean(true);
		int index = msg.writerIndex();
		msg.writeInt(0);
		List<ITeamPVPTeamMember> list = team.getTeamMembers();
		KTeamPVPTeamMember member;
		int size = list.size();
		for (int i = 0; i < size; i++) {
			member = (KTeamPVPTeamMember) list.get(i);
			if (member.hasOpen()) {
				msg.setInt(index, member.getExpReward());
				KSupportFactory.getRoleModuleSupport().sendMsg(member.getId(), msg);
				if (i + 1 < size) {
//					msg = msg.duplicate();
					msg = msg.copy();
				}
			}
		}
	}

	static void sendCombatResultMsg(KRole role, boolean isWin, long combatTime, int honor, int score) {
		KGameMessage msg = KGame.newLogicMessage(KCompetitionProtocol.SM_TEAM_PVP_BATTLE_RESULT);
		msg.writeBoolean(isWin);
		msg.writeInt((int)TimeUnit.SECONDS.convert(combatTime, TimeUnit.MILLISECONDS));
		msg.writeInt(honor);
		msg.writeShort(score);
		role.sendMsg(msg);
	}
	
	static void sendUpdateTeamInfo(KTeamPVPTeam team) {
		List<ITeamPVPTeamMember> members = team.getTeamMembers();
		KTeamPVPTeamMember tempMember;
		for (int i = 0; i < members.size(); i++) {
			tempMember = (KTeamPVPTeamMember) members.get(i);
			if (tempMember.hasOpen()) {
				KGameMessage msg = KGame.newLogicMessage(KCompetitionProtocol.SM_REFURBISH_TEAM_PVP_INFO);
				team.packUpdateInfoToMessage(msg, tempMember.getId());
				KSupportFactory.getRoleModuleSupport().sendMsg(tempMember.getId(), msg);
			}
		}
	}
	
	static void notifyRankRefurbish(KTeamPVPTeam team, boolean danLvUp) {
		KGameMessage msg;
		if(danLvUp) {
			msg = KGame.newLogicMessage(KCompetitionProtocol.SM_REFURBISH_TEAM_PVP_RANGE_INFO);
			msg.writeUtf8String(team.getDanStageInfo().danStageName);
			msg.writeInt(team.getDanData().iconId);
			msg.writeInt(team.getDanStageInfo().iconId);
			msg.writeShort(team.getCurrentScore());
			msg.writeShort(team.getDanStageInfo().promoteScore);
		} else {
			msg = KGame.newLogicMessage(KCompetitionProtocol.SM_NOTIFY_RANGE_CHANGE);
			msg.writeUtf8String(team.getDanStageInfo().danStageName);
			msg.writeInt(team.getDanStageInfo().iconId);
			msg.writeShort(team.getCurrentScore());
			msg.writeShort(team.getDanStageInfo().promoteScore);
		}
//		sendMsgToAllTeamMember(team, msg);
		KGameMessage cloneMsg;
		List<ITeamPVPTeamMember> list = team.getTeamMembers();
		KTeamPVPTeamMember member;
		KTeamPVPDanStageInfo nextStage;
		for(int i = list.size(); i-- > 0;) {
			if(i == 0) {
				cloneMsg = msg;
			} else {
				cloneMsg = msg.copy();
			}
			nextStage = null;
			member = (KTeamPVPTeamMember)list.get(i);
			if(member.getHighestStage().danStageType.canPromote) {
				nextStage = KTeamPVPManager.getNextDanRank(member.getHighestStage().danStageId);
			}
			if(nextStage != null) {
				cloneMsg.writeBoolean(true);
				cloneMsg.writeUtf8String(nextStage.danStageName);
				cloneMsg.writeInt(nextStage.promoDiamondReward);
			} else {
				cloneMsg.writeBoolean(false);
			}
			KSupportFactory.getRoleModuleSupport().sendMsg(member.getId(), cloneMsg);
		}
	}
	
	static void syncTeamBattlePowerToClient(KTeamPVPTeam team) {
		KGameMessage msg = createBattlePowerSyncMsg(team);
		sendMsgToAllTeamMember(team, msg);
	}
	
	static void syncTeamBattlePowerToClient(KTeamPVPTeam team, long roleId) {
		KGameMessage msg = createBattlePowerSyncMsg(team);
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}
	
	static void syncChallengeTime(KTeamPVPTeamMember member) {
		KGameMessage msg = createSyncChallengeTimeMsg(member);
		KSupportFactory.getRoleModuleSupport().sendMsg(member.getId(), msg);
	}
	
	static void syncMyHighestDanInfo(KTeamPVPTeamMember member) {
		KGameMessage msg = KGame.newLogicMessage(KCompetitionProtocol.SM_REFURBISH_MY_HIGHEST_STAGE);
		msg.writeInt(member.getHighestDan().iconId);
		msg.writeInt(member.getHighestStage().iconId);
		msg.writeUtf8String(member.getHighestStage().danStageName);
		KSupportFactory.getRoleModuleSupport().sendMsg(member.getId(), msg);
	}
	
	static void sendUpdateNotice(long roleId, String notice) {
		KGameMessage msg = createUpdateNoticeMsg(notice);
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}
	
	public static void syncFriendId(long roleId, long friendId, boolean add) {
		KGameMessage msg = KGame.newLogicMessage(KCompetitionProtocol.SM_NOTIFY_FRIEND_ADD);
		msg.writeBoolean(add);
		msg.writeLong(friendId);
		if(add) {
			boolean canInvite = true;
			String tips = null;
			if(KTeamPVPManager.isInTeam(friendId)) {
				canInvite = false;
				tips = CompetitionTips.getTipsAlreadyJoinOtherTeamLabel();
			} else {
				KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
				if(role == null || role.getLevel() < KTeamPVPConfig.getJoinMinLevel()) {
					canInvite = false;
					tips = CompetitionTips.getTipsLevelNotReachLabel();
				}
			}
			msg.writeBoolean(canInvite);
			if(!canInvite) {
				msg.writeUtf8String(tips);
			}
		}
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}
	
	public static KGameMessage createUpdateFriendStatusMsg(KRole friend, boolean canInvite, String reason) {
		KGameMessage msg = KGame.newLogicMessage(KCompetitionProtocol.SM_NOTIFY_FRIEND_ADD);
		msg.writeBoolean(true);
		msg.writeLong(friend.getId());
		msg.writeBoolean(canInvite);
		if(!canInvite) {
			msg.writeUtf8String(reason);
		}
		return msg;
	}
	
	public static KGameMessage createUpdateNoticeMsg(String notice) {
		KGameMessage msg = KGame.newLogicMessage(KCompetitionProtocol.SM_NOTIFY_TEAM_STATUS_CHANGE);
		msg.writeUtf8String(notice);
		return msg;
	}
	
	static void notifyMemberQuited(long receiverId, long memberId) {
		KGameMessage msg = KGame.newLogicMessage(KCompetitionProtocol.SM_NOTIFY_TEAM_MEMBER_CHANGE);
		msg.writeBoolean(false);
		msg.writeLong(memberId);
		KSupportFactory.getRoleModuleSupport().sendMsg(receiverId, msg);
	}
	
	public static KGameMessage createBattlePowerSyncMsg(KTeamPVPTeam team) {
		KGameMessage msg = KGame.newLogicMessage(KCompetitionProtocol.SM_REFURBISH_TEAM_BATTLE_POWER);
		msg.writeInt(team.getTeamBattlePower());
		return msg;
	}
	
	public static KGameMessage createSyncChallengeTimeMsg(KTeamPVPTeamMember member) {
		KGameMessage msg = KGame.newLogicMessage(KCompetitionProtocol.SM_REFURBISH_CHALLENGE_TIME);
		msg.writeByte(member.getRewardLeftCount());
		msg.writeByte(member.getMaxChallengeRewardCount());
		return msg;
	}
	
	public static KGameMessage createTeamInfoMessage(KTeamPVPTeam team, long roleId) {
		KGameMessage msg = KGame.newLogicMessage(KCompetitionProtocol.SM_RESPOND_TEAM_PVP_INFO);
		boolean hasTeam = team != null;
		msg.writeInt(KTeamPVPConfig.getPriceForCreateTeam());
		msg.writeBoolean(hasTeam);
		if (hasTeam) {
			KTeamPVPTeamMember member = team.getMember(roleId);
			msg.writeInt(member.getHighestDan().iconId);
			msg.writeInt(member.getHighestStage().iconId);
			msg.writeUtf8String(member.getHighestStage().danStageName);
			if (member.getHighestStage().danStageType.canPromote) {
				KTeamPVPDanStageInfo nextStage = KTeamPVPManager.getNextDanRank(member.getHighestStage().danStageId);
				msg.writeBoolean(true);
				msg.writeUtf8String(nextStage.danStageName);
				msg.writeInt(nextStage.promoDiamondReward);
			} else {
				msg.writeBoolean(false);
			}
			team.packDataToMsg(msg, roleId);
		} else {
			KTeamPVPRoleRecordData extData = KTeamPVPManager.getTeamPVPRecordData(roleId);
			int danIconId;
			int danStageIconId;
			String name;
			KTeamPVPDanStageInfo nextStage = null;
			if(extData.getMyHighestStage() > 0) {
				KTeamPVPDanStageInfo stageData = KTeamPVPManager.getDanStageInfo(extData.getMyHighestStage());
				KTeamPVPDanData danData = KTeamPVPManager.getDanData(stageData.danGradeId);
				danIconId = danData.iconId;
				danStageIconId = stageData.iconId;
				name = stageData.danStageName;
				if (stageData.danStageType.canPromote) {
					nextStage = KTeamPVPManager.getNextDanRank(extData.getMyHighestStage());
				}
			} else {
				danIconId = 0;
				danStageIconId = 0;
				name = "";
				nextStage = KTeamPVPManager.getNextDanRank(KTeamPVPConfig.getFirstDanRankId());
			}
			msg.writeInt(danIconId);
			msg.writeInt(danStageIconId);
			msg.writeUtf8String(name);
			if(nextStage != null) {
				msg.writeBoolean(true);
				msg.writeUtf8String(nextStage.danStageName);
				msg.writeInt(nextStage.promoDiamondReward);
			} else {
				msg.writeBoolean(false);
			}
		}
		return msg;
	}
	
	public static void packMemberDataToMsg(KGameMessage msg, ITeamPVPTeamMember member) {
		msg.writeLong(member.getId());
		msg.writeUtf8String(member.getName());
		msg.writeByte(member.getJob());
		msg.writeShort(member.getLevel());
		msg.writeByte(member.getVipLevel());
		msg.writeInt(member.getHeadResId());
		msg.writeInt(member.getInMapResId());
		msg.writeByte(member.getEquipmentRes().size());
//		Map.Entry<Byte, String> entry;
		IRoleEquipShowData temp;
		List<IRoleEquipShowData> list = member.getEquipmentRes();
		boolean isWeaponRed = false;
		for (int i = 0; i < list.size(); i++) {
			temp = list.get(i);
			msg.writeByte(temp.getPart());
			msg.writeUtf8String(temp.getRes());
			if(temp.getPart() == KEquipmentTypeEnum.主武器.sign) {
				isWeaponRed = temp.getQuality() == KItemQualityEnum.无敌的;
			}
		}
		msg.writeUtf8String(member.getFashionResId());
		msg.writeInt(member.getEquipSetRes()[0]);
		msg.writeInt(member.getEquipSetRes()[1]);
		msg.writeBoolean(isWeaponRed);
	}
	
	public static void packInvitation(KTeamPVPInvitation invitation, KGameMessage msg) {
		msg.writeLong(invitation.teamId);
		msg.writeUtf8String(invitation.teamName);
		msg.writeUtf8String(invitation.captainName);
		msg.writeShort(invitation.captainLevel);
		msg.writeInt(invitation.captainHeadResId);
		msg.writeInt(invitation.teamBattlePower);
		msg.writeUtf8String(invitation.danRankName);
	}
	
	public static void pushInvitation(Collection<KTeamPVPInvitation> invitations, KRole role) {
		if(invitations.isEmpty()) {
			return;
		}
		KGameMessage msg = KGame.newLogicMessage(KCompetitionProtocol.SM_PUSH_INVITATION);
		msg.writeByte(invitations.size());
		for(KTeamPVPInvitation invitation : invitations) {
			packInvitation(invitation, msg);
		}
		role.sendMsg(msg);
	}
	
	public static void notifyMemberResChange(KTeamPVPTeam team, KTeamPVPTeamMember member, int type) {
		KGameMessage msg = KGame.newLogicMessage(KCompetitionProtocol.SM_NOTIFY_TEAM_MEMBER_RES_CHANGE);
		msg.writeLong(member.getId());
		msg.writeByte(type);
		switch (type) {
		case KTeamPVPTeamMember.RES_TYPE_EQUIPMENT:
			IRoleEquipShowData temp;
			List<IRoleEquipShowData> list = member.getEquipmentRes();
			boolean isWeaponRed = false;
			for (int i = 0; i < list.size(); i++) {
				temp = list.get(i);
				msg.writeByte(temp.getPart());
				msg.writeUtf8String(temp.getRes());
				if(temp.getPart() == KEquipmentTypeEnum.主武器.sign) {
					isWeaponRed = temp.getQuality() == KItemQualityEnum.无敌的;
				}
			}
			msg.writeBoolean(isWeaponRed);
			break;
		case KTeamPVPTeamMember.RES_TYPE_EQUIPMENT_SET:
			int[] res = member.getEquipSetRes();
			msg.writeInt(res[0]);
			msg.writeInt(res[1]);
			break;
		case KTeamPVPTeamMember.RES_TYPE_FASHION:
			msg.writeUtf8String(member.getFashionResId());
			break;
		}
		sendMsgToAllTeamMember(team, msg);
	}
}
