package com.kola.kmp.logic.util.tips;

import com.koala.game.util.StringUtil;
import com.kola.kmp.logic.other.KColorFunEnum.KColorManager;
import com.kola.kmp.logic.util.text.HyperTextTool;

public class MissionTips {
	public static String tipsAcceptMissionButton;
	public static String tipsCanNotAcceptMissionByLowLv;
	public static String tipsCanNotSubmitMissionByBagFull;
	public static String tipsSearchRoadAnyGameLevelDesc;
	public static String tipsSearchRoadUseItemDesc;
	public static String tipsSearchRoadTargetNameDesc;
	public static String tipsAnswerMissionQuestionError;
	public static String tipsMissionTitle;
	public static String tipsMissionPriceTitle;
	public static String tipsMissionPriceTitle1;
	public static String tipsMissionTipsTitle;
	public static String tipsMissionTargetTitle;
	public static String tipsMissionTargetTitle1;
	public static String tipsMissionTipsCompleteStatus;
	public static String tipsKillMonsterTypeTips;
	public static String tipsKillMonsterTypeMonsterName;
	public static String tipsKillMonsterTypeMonsterName1;
	public static String tipsKillMonsterTypeMonsterInfo;
	public static String tipsKillMonsterTypeMonsterInfo1;
	public static String tipsCollectItemTypeTips;
	public static String tipsCollectItemTypeItemInfo;
	public static String tipsCollectItemTypeItemInfo1;
	public static String tipsCompleteLevelTypeInfo;
	public static String tipsCompleteLevelTypeInfo1;
	public static String tipsCompleteLevelTypeInfo2;
	public static String tipsCompleteLevelTypeInfo3;
	public static String tipsUseFunTypeInfo;
	public static String tipsUseFunTypeTips;
	public static String tipsUseItemTypeInfo;
	public static String tipsQuestionTypeInfo;
	public static String tipsQuestionTypeInfo1;
	public static String tipsTalkToNpcTypeInfo;

	public static String tipsAcceptMissionFail;
	public static String tipsSubmitMissionFail;
	public static String tipsDropMissionFail;
	public static String tipsDropMissionSuccess;

	public static String tipsMissionAcceptStatus;
	public static String tipsItemSearchRoadNotFound;
	public static String tipsItemSearchRoadLevelNotOpen;

	public static String tipsDailyMissionTitle;
	public static String tipsDailyMissionRuleTips;
	public static String tipsDailyMissionRuleTips1;
	public static String tipsDailyMissionCompleteTips;
	public static String tipsDailyMissionCompleteTips1;
	public static String tipsDailyMissionCompleteTips2;

	public static String tipsBuyDailyMissionUsePoint;
	public static String tipsBuyDailyMissionNotEnoughIgot;
	public static String tipsAcceptDailyMissionSuccess;
	public static String tipsAcceptDailyMissionFailMoreThan2;
	public static String tipsCannotAcceptNewDailyMission;
	public static String tipsCannotBuyDailyMission;
	public static String tipsDailyMissionMaxCompleteCount;
	public static String tipsDailyMissionMaxCompleteCountTips;
	public static String tipsDailyMissionMaxCompleteCountTips1;
	public static String tipsAutoCompleteDailyMissionNotEnoughIgot;
	public static String tipsCannotManualReflashDailyMission;
	public static String tipsManualReflashDailyMissionMaxCount;
	public static String tipsManualReflashDailyMissionNotEnoughIgot;
	public static String tipsSeniorReflashDailyMissionVipLimit;
	public static String tipsSeniorReflashDailyMissionNotEnoughIgot;
	public static String tipsAutoSubmitDailyMission;
	public static String tipsAutoSubmitDailyMissionNotEnoughIgot;
	public static String tipsCanGetPriceBox;
	public static String tipsAlreadyPriceBox;
	public static String tipsGetPriceBoxNotEnoughScore;

	public static String tipsPriceBoxRestCompleteCount;

	public static String tipsExp;

	public static String tipsMainLineMissionConditionComplete;
	public static String tipsBranchLineMissionConditionComplete;
	public static String tipsCollectItemMissionAcceptDiaLogTips;

	// public static String tipsDailyMissionNotOpenTips;

	// public static void init(Element elm) {
	// @SuppressWarnings("unchecked")
	// List<Element> list = elm.getChildren("tips");
	// KGameReflectPaser.parse(KMissionTipsManager.class, list, true);
	// }

	public static String getTipsCanNotAcceptMissionByLowLv(int roleLv) {
		return StringUtil.format(tipsCanNotAcceptMissionByLowLv, roleLv);
	}

	public static String getTipsAcceptMissionButton() {
		return tipsAcceptMissionButton;
	}

	public static String getTipsCanNotSubmitMissionByBagFull() {
		return tipsCanNotSubmitMissionByBagFull;
	}

	public static String getTipsSearchRoadAnyGameLevelDesc() {
		return tipsSearchRoadAnyGameLevelDesc;
	}

	public static String getTipsSearchRoadUseItemDesc(String itemName) {
		return StringUtil.format(tipsSearchRoadUseItemDesc, itemName);
	}

	public static String getTipsSearchRoadTargetNameDesc(String missionType,
			String missionName, String targetName) {
		return StringUtil.format(tipsSearchRoadTargetNameDesc, missionType,
				missionName, targetName);
	}

	public static String getTipsAnswerMissionQuestionError() {
		return tipsAnswerMissionQuestionError;
	}

	public static String getTipsMissionTitle(String missionName) {
		return StringUtil.format(tipsMissionTitle, missionName);
	}

	public static String getTipsMissionPriceTitle() {
		return tipsMissionPriceTitle;
	}

	public static String getTipsMissionPriceTitle1() {
		return tipsMissionPriceTitle1;
	}

	public static String getTipsMissionTipsTitle(String missionTips) {
		return StringUtil.format(tipsMissionTipsTitle, missionTips);
	}

	public static String getTipsMissionTargetTitle() {
		return tipsMissionTargetTitle;
	}

	public static String getTipsMissionTargetTitle1() {
		return tipsMissionTargetTitle1;
	}

	public static String getTipsMissionTipsCompleteStatus(int nowCount,
			int targetCount, int color) {
		return HyperTextTool.extColor(StringUtil.format(
				tipsMissionTipsCompleteStatus, nowCount, targetCount), color);
	}

	public static String getTipsKillMonsterTypeTips() {
		return tipsKillMonsterTypeTips;
	}

	public static String getTipsKillMonsterTypeMonsterName() {
		return tipsKillMonsterTypeMonsterName;
	}

	public static String getTipsKillMonsterTypeMonsterName1(int lv) {
		return StringUtil.format(tipsKillMonsterTypeMonsterName1, lv);
	}

	public static String getTipsKillMonsterTypeMonsterInfo(String monsterName,
			int targetCount) {
		return StringUtil.format(tipsKillMonsterTypeMonsterInfo, monsterName,
				targetCount);
	}

	public static String getTipsKillMonsterTypeMonsterInfo1(String monsterName,
			int targetCount) {
		return StringUtil.format(tipsKillMonsterTypeMonsterInfo1, monsterName,
				targetCount);
	}

	public static String getTipsCollectItemTypeTips() {
		return tipsCollectItemTypeTips;
	}

	public static String getTipsCollectItemTypeItemInfo(String itemName,
			int targetCount) {
		return StringUtil.format(tipsCollectItemTypeItemInfo, itemName,
				targetCount);
	}

	public static String getTipsCollectItemTypeItemInfo1(String itemName,
			int targetCount) {
		return StringUtil.format(tipsCollectItemTypeItemInfo1, itemName,
				targetCount);
	}

	public static String getTipsCompleteLevelTypeInfo(int targetCount) {
		return StringUtil.format(tipsCompleteLevelTypeInfo, targetCount);
	}

	public static String getTipsCompleteLevelTypeInfo1(String levelName,
			int targetCount) {
		return StringUtil.format(tipsCompleteLevelTypeInfo1, levelName,
				targetCount);
	}

	public static String getTipsCompleteLevelTypeInfo2(int targetCount) {
		return StringUtil.format(tipsCompleteLevelTypeInfo2, targetCount);
	}

	public static String getTipsCompleteLevelTypeInfo3(String levelName,
			int targetCount) {
		return StringUtil.format(tipsCompleteLevelTypeInfo3, levelName,
				targetCount);
	}

	public static String getTipsUseFunTypeInfo(String tips, int targetCount) {
		return StringUtil.format(tipsUseFunTypeInfo, tips, targetCount);
	}

	public static String getTipsUseFunTypeTips(String funName) {
		return StringUtil.format(tipsUseFunTypeTips, funName);
	}

	public static String getTipsUseItemTypeInfo(String itemName) {
		return StringUtil.format(tipsUseItemTypeInfo, itemName);
	}

	public static String getTipsQuestionTypeInfo(int targetCount) {
		return StringUtil.format(tipsQuestionTypeInfo, targetCount);
	}

	public static String getTipsQuestionTypeInfo1(int targetCount) {
		return StringUtil.format(tipsQuestionTypeInfo1, targetCount);
	}

	public static String getTipsTalkToNpcTypeInfo(String npcName) {
		return StringUtil.format(tipsTalkToNpcTypeInfo, npcName);
	}

	public static String getTipsDailyMissionCompleteTips(String missionName) {
		return StringUtil.format(tipsDailyMissionCompleteTips, missionName);
	}

	public static String getTipsDailyMissionCompleteTips1(String missionName) {
		return StringUtil.format(tipsDailyMissionCompleteTips1, missionName);
	}

	public static String getTipsDailyMissionCompleteTips2(String missionName) {
		return StringUtil.format(tipsDailyMissionCompleteTips2, missionName);
	}

	public static String getTipsBuyDailyMissionUsePoint(int igot, int addCount,
			int vipLv, int buyCount) {
		return StringUtil.format(tipsBuyDailyMissionUsePoint, igot, addCount,
				vipLv, buyCount);
	}

	public static String getTipsBuyDailyMissionNotEnoughIgot(int igot) {
		return StringUtil.format(tipsBuyDailyMissionNotEnoughIgot, igot);
	}

	public static String getTipsMissionAcceptStatus(int roleLv) {
		return StringUtil.format(tipsMissionAcceptStatus, roleLv);
	}

	public static String getTipsItemSearchRoadNotFound() {
		return tipsItemSearchRoadNotFound;
	}

	public static String getTipsItemSearchRoadLevelNotOpen(String levelName) {
		return StringUtil.format(tipsItemSearchRoadLevelNotOpen, levelName);
	}

	public static String getTipsDailyMissionTitle(String missionName) {
		return StringUtil.format(tipsDailyMissionTitle, missionName);
	}

	public static String getTipsDailyMissionRuleTips() {
		return StringUtil.format(tipsDailyMissionRuleTips);
	}

	public static String getTipsDailyMissionRuleTips1() {
		return tipsDailyMissionRuleTips1;
	}

	public static String getTipsAcceptMissionFail() {
		return tipsAcceptMissionFail;
	}

	public static String getTipsSubmitMissionFail() {
		return tipsSubmitMissionFail;
	}

	public static String getTipsDropMissionFail() {
		return tipsDropMissionFail;
	}

	public static String getTipsDropMissionSuccess() {
		return tipsDropMissionSuccess;
	}

	public static String getTipsAcceptDailyMissionFailMoreThan2() {
		return tipsAcceptDailyMissionFailMoreThan2;
	}

	public static String getTipsCannotAcceptNewDailyMission() {
		return tipsCannotAcceptNewDailyMission;
	}

	public static String getTipsCannotBuyDailyMission(int vipLv, int buyCount) {
		return StringUtil.format(tipsCannotBuyDailyMission, vipLv, buyCount);
	}

	public static String getTipsDailyMissionMaxCompleteCount() {
		return tipsDailyMissionMaxCompleteCount;
	}

	public static String getTipsDailyMissionMaxCompleteCountTips(int freeCount) {
		return StringUtil.format(tipsDailyMissionMaxCompleteCountTips,
				freeCount);
	}

	public static String getTipsDailyMissionMaxCompleteCountTips1(
			int freeCount, int vipLv, int buyCount) {
		return StringUtil.format(tipsDailyMissionMaxCompleteCountTips1,
				freeCount, vipLv, buyCount);
	}

	public static String getTipsAcceptDailyMissionSuccess(String missionName) {
		return StringUtil.format(tipsAcceptDailyMissionSuccess, missionName);
	}

	public static String getTipsAutoCompleteDailyMissionNotEnoughIgot(int igot) {
		return StringUtil.format(tipsAutoCompleteDailyMissionNotEnoughIgot,
				igot);
	}

	public static String getTipsManualReflashDailyMissionMaxCount(int point) {
		return StringUtil.format(tipsManualReflashDailyMissionMaxCount, point);
	}

	public static String getTipsManualReflashDailyMissionNotEnoughIgot(int igot) {
		return StringUtil.format(tipsManualReflashDailyMissionNotEnoughIgot,
				igot);
	}

	public static String getTipsCannotManualReflashDailyMission() {
		return tipsCannotManualReflashDailyMission;
	}

	public static String getTipsSeniorReflashDailyMissionVipLimit(int vipLv) {
		return StringUtil.format(tipsSeniorReflashDailyMissionVipLimit, vipLv);
	}

	public static String getTipsSeniorReflashDailyMissionNotEnoughIgot(int igot) {
		return StringUtil.format(tipsSeniorReflashDailyMissionNotEnoughIgot,
				igot);
	}

	public static String getTipsCanGetPriceBox() {
		return tipsCanGetPriceBox;
	}

	public static String getTipsAlreadyPriceBox() {
		return tipsAlreadyPriceBox;
	}

	public static String getTipsPriceBoxRestCompleteCount(int count) {
		return StringUtil.format(tipsPriceBoxRestCompleteCount, count);
	}

	public static String getTipsExp() {
		return tipsExp;
	}

	public static String getTipsAutoSubmitDailyMission(int usePoint) {
		return StringUtil.format(tipsAutoSubmitDailyMission, usePoint);
	}

	public static String getTipsAutoSubmitDailyMissionNotEnoughIgot(int usePoint) {
		return StringUtil.format(tipsAutoSubmitDailyMissionNotEnoughIgot,
				usePoint);
	}

	public static String getTipsGetPriceBoxNotEnoughScore(int score) {
		return StringUtil.format(tipsGetPriceBoxNotEnoughScore, score);
	}

	public static String getTipsMainLineMissionConditionComplete(
			String missionName) {
		return StringUtil.format(tipsMainLineMissionConditionComplete,
				missionName);
	}

	public static String getTipsBranchLineMissionConditionComplete(
			String missionName) {
		return StringUtil.format(tipsBranchLineMissionConditionComplete,
				missionName);
	}

	public static String getTipsCollectItemMissionAcceptDiaLogTips(
			String levelName, String itemName, int count) {
		return StringUtil.format(tipsCollectItemMissionAcceptDiaLogTips,
				levelName, itemName, count);
	}
}
