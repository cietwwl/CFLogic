package com.kola.kmp.logic.util.tips;

import java.util.List;

import com.koala.game.util.StringUtil;
import com.kola.kmp.logic.other.KColorFunEnum;
import com.kola.kmp.logic.util.text.HyperTextTool;

public class CompetitionTips {
	private static String _tipsNoInYourRanking;
	private static String _tipsNoMoreChallengeTime;
	private static String _tipsBuyChallengeTime;
	private static String _tipsChallengeSuccess;
	private static String _tipsChallengeFail;
	private static String _tipsBeChallengeSuccess;
	private static String _tipsBeChallengeFail;
	private static String _tipsChallengeFirstSuccess;
	private static String _tipsChallengeFirstTenSuccess;
	private static String _tipsChallengeLowerSuccess;
	private static String _tipsChallengeLowerFail;
	private static String _tipsBeChallengeSuccessByHigher;
	private static String _tipsBeChallengeFailByHigher;
	private static String _tipsCannotBuyChallengeTime;
	private static String _tipsBuyChallengeTimeNotEnoughIgot;
	private static String _tipsHistory1;
	private static String _tipsHistory2;
	private static String _tipsHistory3;
	private static String _tipsAlreadyVisit;
	private static String _tipsVisitSuccess;
	private static String _tipsChallengeTimeFull;
	private static String _tipsReflashTimeTooShort;
	private static String _tipsReflashCompetitionSuccess;
	private static String _tipsSettleTodayReward;
	private static String _tipsSettleWeekReward;
	private static String _tipsChallengeClearCdTime;
	private static String _tipsClearCdTimeNotEnoughIgot;
	private static String _tipsSettleFashionMailTitle;
	private static String _tipsSettleFashionMailContent;
	private static String _tipsRoleNotFound;
	
	private static String _tipsTeamNameContainsDirtyWord;
	private static String _tipsDuplicateTeamName;
	private static String _tipsYouAreInTeam;
	private static String _tipsCreateTeamSuccess;
	private static String _tipsYouAreNotFriend;
	private static String _tipsYouAreNotInTeam;
	private static String _tipsYouAreNotCaptain;
	private static String _tipsNoMembersToKick;
	private static String _tipsKickMemberSuccess;
	private static String _tipsKickCaptainSuccess;
	private static String _tipsKickCaptainFailWithOnline;
	private static String _tipsKickCaptainProcessing;
	private static String _tipsCaptainBeKickedNotice;
	private static String _tipsYouAreKickedOut;
	private static String _tipsTeamIsFull;
	private static String _tipsTargetTeamIsFull;
	private static String _tipsFriendIsInTeam;
	private static String _tipsFriendLevelNotReach;
	private static String _tipsInvitationSent;
	private static String _tipsNoSuchTeam;
	private static String _tipsJoinTeamSuccess;
	private static String _tipsRejectInvitation;
	private static String _tipsPlsSelectATarget;
	private static String _tipsCannotChallengeAnyMoreToday;
	private static String _tipsTeamLengthNotPassed;
	private static String _tipsRobotTeamNamePrefix;
	private static String _tipsTeamChallengeSuccess;
	private static String _tipsTeamChallengeFail;
	private static String _tipsPromoteChallengeSuccess;
	private static String _tipsPromoteChallengeFail;
	private static String _tipsGetDailyRewardSuccess;
	private static String _tipsAlreadyGetDailyReward;
	private static String _tipsNoDailyRewardToGet;
	private static String _tipsBuyChallengeTimeSuccess;
	private static String _tipsPromoteRewardTitle;
	private static String _tipsPromoteRewardContent;
	private static String _tipsPrepareForPromote;
	private static String _tipsPrepareForPromoteNotice;
	private static String _tipsPromoteFailNotice;
	private static String _tipsPromoteSuccessNotice;
	private static String _tipsDemoteNotice;
	private static String _tipsNoTeamMatch;
	private static String _tipsCannotBuyAnyMore;
	private static String _tipsAlreadyJoinOtherTeamLabel;
	private static String _tipsLevelNotReachLabel;
	private static String _tipsAutoRemoveFromTeam;
	private static String _tipsFlowCreateTeam;
	private static String _tipsFlowJoinTeam;
	private static String _tipsFlowQuitTeam;
	private static String _tipsFlowTeamPromote;
	private static String _tipsFlowTeamDemote;
	private static String _tipsFlowTeamResult;
	private static String _tipsFlowTeamPromoteFightResult;
	private static String _tipsFlowTeamPromoteFail;

	public static String getTipsNoInYourRanking() {
		return _tipsNoInYourRanking;
	}

	public static String getTipsNoMoreChallengeTime(int point) {
		return StringUtil.format(_tipsNoMoreChallengeTime, point);
	}
	
	public static String getTipsChallengeClearCdTime(int point) {
		return StringUtil.format(_tipsChallengeClearCdTime, point);
	}

	public static String getChallengeResult(String roleName, boolean win,
			int ranking) {
		if (win) {
			return StringUtil.format(_tipsChallengeSuccess, roleName,
					HyperTextTool.extColor(ranking + "", KColorFunEnum.品质_绿));
		} else {
			return StringUtil.format(_tipsChallengeFail, roleName);
		}
	}

	public static String getBeChallengeResult(String roleName, boolean win,
			int ranking) {
		if (win) {
			return StringUtil.format(_tipsBeChallengeSuccess, roleName);
		} else {
			return StringUtil.format(_tipsBeChallengeFail, roleName,
					HyperTextTool.extColor(ranking + "", KColorFunEnum.品质_红));
		}
	}

	public static String getTipsChallengeFirstSuccess(String name,
			String defcRoleName) {
		return StringUtil
				.format(_tipsChallengeFirstSuccess, name, defcRoleName);
	}

	public static String getTipsChallengeFirstTenSuccess(String challengeName,
			String defenderName, int rank) {
		return StringUtil.format(_tipsChallengeFirstTenSuccess, challengeName,
				defenderName, rank);
	}

	public static String getTipsChallengeLowerSuccess(String name) {
		return StringUtil.format(_tipsChallengeLowerSuccess, name);
	}

	public static String getTipsChallengeLowerFail(String name) {
		return StringUtil.format(_tipsChallengeLowerFail, name);
	}

	public static String getTipsBeChallengeSuccessByHigher(String name) {
		return StringUtil.format(_tipsBeChallengeSuccessByHigher, name);
	}

	public static String getTipsBeChallengeFailByHigher(String name) {
		return StringUtil.format(_tipsBeChallengeFailByHigher, name);
	}

	public static String getTipsCannotBuyChallengeTime(int vipLv,int count) {
		return StringUtil.format(_tipsCannotBuyChallengeTime, vipLv,count);
	}

	public static String getTipsBuyChallengeTimeNotEnoughIgot(int point) {
		return StringUtil.format(_tipsBuyChallengeTimeNotEnoughIgot, point);
	}

	public static String getTipsHistory1(String dateInfo, String roleName) {
		return StringUtil.format(_tipsHistory1, dateInfo, roleName);
	}

	public static String getTipsHistory2(String dateInfo, String winName,
			String failName) {
		return StringUtil.format(_tipsHistory2, dateInfo, failName, winName,
				winName);
	}

	public static String getTipsHistory3(String dateInfo, String roleName) {
		return StringUtil.format(_tipsHistory3, dateInfo, roleName);
	}

	public static String getTipsAlreadyVisit() {
		return _tipsAlreadyVisit;
	}
	
	public static String getTipsVisitSuccess() {
		return _tipsVisitSuccess;
	}

	public static String getTipsChallengeTimeFull() {
		return _tipsChallengeTimeFull;
	}

	public static String getTipsBuyChallengeTime(int point) {
		return StringUtil.format(_tipsBuyChallengeTime, point);
	}

	public static String getTipsReflashTimeTooShort() {
		return _tipsReflashTimeTooShort;
	}
	
	public static String getTipsReflashCompetitionSuccess() {
		return _tipsReflashCompetitionSuccess;
	}
	
	public static String getTipsSettleTodayReward() {
		return _tipsSettleTodayReward;
	}

	public static String getTipsSettleWeekReward() {
		return _tipsSettleWeekReward;
	}

	public static String getTipsClearCdTimeNotEnoughIgot(int point) {
		return StringUtil.format(_tipsClearCdTimeNotEnoughIgot, point);
	}
	
	public static String getTipsSettleFashionMailTitle() {
		return _tipsSettleFashionMailTitle;
	}

	public static String getTipsSettleFashionMailContent(String positionName) {
		return StringUtil.format(_tipsSettleFashionMailContent, positionName);
	}
	
	public static String getTipsRoleNotFound() {
		return _tipsRoleNotFound;
	}

	public static String getTipsTeamNameContainsDirtyWord() {
		return _tipsTeamNameContainsDirtyWord;
	}

	public static String getTipsDuplicateTeamName() {
		return _tipsDuplicateTeamName;
	}

	public static String getTipsYouAreInTeam() {
		return _tipsYouAreInTeam;
	}

	public static String getTipsCreateTeamSuccess() {
		return _tipsCreateTeamSuccess;
	}

	public static String getTipsYouAreNotFriend() {
		return _tipsYouAreNotFriend;
	}

	public static String getTipsYouAreNotInTeam() {
		return _tipsYouAreNotInTeam;
	}

	public static String getTipsTeamIsFull() {
		return _tipsTeamIsFull;
	}
	
	public static String getTipsTargetTeamIsFull() {
		return _tipsTargetTeamIsFull;
	}

	public static String getTipsFriendIsInTeam() {
		return _tipsFriendIsInTeam;
	}

	public static String getTipsFriendLevelNotReach() {
		return _tipsFriendLevelNotReach;
	}

	public static String getTipsInvitationSent() {
		return _tipsInvitationSent;
	}

	public static String getTipsNoSuchTeam() {
		return _tipsNoSuchTeam;
	}

	public static String getTipsJoinTeamSuccess() {
		return _tipsJoinTeamSuccess;
	}

	public static String getTipsRejectInvitation(String roleName) {
		return StringUtil.format(_tipsRejectInvitation, roleName);
	}

	public static String getTipsPlsSelectATarget() {
		return _tipsPlsSelectATarget;
	}

	public static String getTipsCannotChallengeAnyMoreToday() {
		return _tipsCannotChallengeAnyMoreToday;
	}

	public static String getTipsTeamLengthNotPassed() {
		return _tipsTeamLengthNotPassed;
	}

	public static String getTipsRobotTeamNamePrefix() {
		return _tipsRobotTeamNamePrefix;
	}

	public static String getTipsTeamChallengeSuccess(String roleName, String teamName, int score) {
		return StringUtil.format(_tipsTeamChallengeSuccess, roleName, teamName, score);
	}

	public static String getTipsTeamChallengeFail(String roleName, String teamName, int score) {
		return StringUtil.format(_tipsTeamChallengeFail, roleName, teamName, score);
	}

	public static String getTipsPromoteChallengeSuccess(String roleName, String teamName) {
		return StringUtil.format(_tipsPromoteChallengeSuccess, roleName, teamName);
	}

	public static String getTipsPromoteChallengeFail(String roleName, String teamName) {
		return StringUtil.format(_tipsPromoteChallengeFail, roleName, teamName);
	}

	public static String getTipsGetDailyRewardSuccess(int exp) {
		return StringUtil.format(_tipsGetDailyRewardSuccess, exp);
	}

	public static String getTipsAlreadyGetDailyReward() {
		return _tipsAlreadyGetDailyReward;
	}

	public static String getTipsNoDailyRewardToGet() {
		return _tipsNoDailyRewardToGet;
	}

	public static String getTipsBuyChallengeTimeSuccess() {
		return _tipsBuyChallengeTimeSuccess;
	}

	public static String getTipsPromoteRewardTitle() {
		return _tipsPromoteRewardTitle;
	}

	public static String getTipsPromoteRewardContent(String orignalDanRankName, String danRankName) {
		return StringUtil.format(_tipsPromoteRewardContent, orignalDanRankName, danRankName);
	}

	public static String getTipsPrepareForPromote(int fightCount) {
		return StringUtil.format(_tipsPrepareForPromote, fightCount);
	}

	public static String getTipsPrepareForPromoteNotice(int fightCount) {
		return StringUtil.format(_tipsPrepareForPromoteNotice, fightCount);
	}

	public static String getTipsPromoteFailNotice() {
		return _tipsPromoteFailNotice;
	}

	public static String getTipsPromoteSuccessNotice(String preName, String nowName) {
		return StringUtil.format(_tipsPromoteSuccessNotice, preName, nowName);
	}

	public static String getTipsDemoteNotice(String preName, String nowName) {
		return StringUtil.format(_tipsDemoteNotice, preName, nowName);
	}

	public static String getTipsNoTeamMatch() {
		return _tipsNoTeamMatch;
	}

	public static String getTipsCannotBuyAnyMore(int vipLv, int times) {
		return StringUtil.format(_tipsCannotBuyAnyMore, vipLv, times);
	}
	
	public static String getTipsAlreadyJoinOtherTeamLabel() {
		return _tipsAlreadyJoinOtherTeamLabel;
	}

	public static String getTipsLevelNotReachLabel() {
		return _tipsLevelNotReachLabel;
	}

	public static String getTipsFlowCreateTeam(String roleName, String teamName, String UUID) {
		return StringUtil.format(_tipsFlowCreateTeam, roleName, teamName, UUID);
	}

	public static String getTipsFlowJoinTeam(String roleName, String UUID) {
		return StringUtil.format(_tipsFlowJoinTeam, roleName, UUID);
	}

	public static String getTipsFlowQuitTeam(String roleName, String UUID) {
		return StringUtil.format(_tipsFlowQuitTeam, roleName, UUID);
	}

	public static String getTipsFlowTeamPromote(String UUID, String danStageName) {
		return StringUtil.format(_tipsFlowTeamPromote, UUID, danStageName);
	}

	public static String getTipsFlowTeamDemote(String UUID, String danStageName) {
		return StringUtil.format(_tipsFlowTeamDemote, UUID, danStageName);
	}

	public static String getTipsFlowTeamResult(String UUID, String defenderTeamName, boolean isWin, int score) {
		return StringUtil.format(_tipsFlowTeamResult, UUID, defenderTeamName, isWin, score);
	}

	public static String getTipsAutoRemoveFromTeam(int days, String teamName) {
		return StringUtil.format(_tipsAutoRemoveFromTeam, days, teamName);
	}

	public static String getTipsFlowTeamPromoteFightResult(String UUID, int nowFightCount, int total, String defenderTeamName, boolean isWin) {
		return StringUtil.format(_tipsFlowTeamPromoteFightResult, UUID, defenderTeamName, isWin, nowFightCount, total);
	}

	public static String getTipsFlowTeamPromoteFail(String UUID, String currentStageName, String challengeResult) {
		return StringUtil.format(_tipsFlowTeamPromoteFail, UUID, currentStageName, challengeResult);
	}

	public static String getTipsYouAreNotCaptain() {
		return _tipsYouAreNotCaptain;
	}

	public static String getTipsNoMembersToKick() {
		return _tipsNoMembersToKick;
	}

	public static String getTipsKickMemberSuccess() {
		return _tipsKickMemberSuccess;
	}

	public static String getTipsYouAreKickedOut() {
		return _tipsYouAreKickedOut;
	}

	public static String getTipsKickCaptainSuccess() {
		return _tipsKickCaptainSuccess;
	}

	public static String getTipsKickCaptainFailWithOnline() {
		return _tipsKickCaptainFailWithOnline;
	}

	public static String getTipsKickCaptainProcessing(float nowHour, float leftHour) {
		return StringUtil.format(_tipsKickCaptainProcessing, nowHour, leftHour);
	}

	public static String getTipsCaptainBeKickedNotice(int hour, String memberName, String teamName) {
		return StringUtil.format(_tipsCaptainBeKickedNotice, hour, memberName, teamName);
	}
	
	
}
