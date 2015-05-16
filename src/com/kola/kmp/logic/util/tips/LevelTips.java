package com.kola.kmp.logic.util.tips;

import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.UtilTool;

public class LevelTips {

	public static String tipsLevelNotOpen;
	public static String tipsJoinLevelFailedWhileStaminaNotEnough;
	public static String tipsJoinLevelFailedRecoverStamina;
	public static String tipsJoinLevelFailedRecoverStamina2;
	public static String tipsJoinLevelFailedWhileMaxCount;
	public static String tipsJoinLevelFailed;
	public static String tipsBagCapacityNotEnough;
	public static String tipsLotteryNotVip;
	public static String tipsGetLotteryRewardUsePoint;
	public static String tipsGetLotteryRewardCountFull;
	public static String tipsGetLotteryNotEnoughIgot;
	public static String tipsSendMailItemForBagFullTitle;
	public static String tipsSendMailItemForBagFull;
	public static String tipsProcessDataError;
	public static String tipsJoinCopyLevelFailedWhileFinished;
	public static String tipsJoinCopyLevelFailedWhileMaxCount;
	public static String tipsRestCopyLevelFailedWhileNotFinished;
	public static String tipsRestCopyLevelFailedWhileLvHigher;
	public static String tipsRestCopyLevel;
	public static String tipsRestCopyNotEnoughIgot;
	public static String tipsJoinFriendCopyFailedFriendNotFound;
	public static String tipsJoinFriendCopyFailedUsePoint;
	public static String tipsJoinFriendCopyFailedCountNotEnough;
	public static String tipsJoinFriendCopyFailedFriendIsCooling;
	public static String tipsBuyFriendCopyCount;
	public static String tipsBuyFriendCopyCountNotEnoughIgot;
	public static String tipsFriendCopyMailTitle;
	public static String tipsFriendCopyMailContent;
	public static String tipsFriendCopyMailContentFullCount;
	public static String tipsSaodangNothing;
	public static String tipsSaodangVipLvNotEnough;
	public static String tipsSaodangStaminaNotEnough;
	public static String tipsCannotSaodang;
	public static String tipsCannotSaodang1;
	public static String tipsSaodangMaxCount;
	public static String tipsSaodangUsePoint;
	public static String tipsSaodangPointNotEnough;
	public static String tipsAlreadyGetFirstDropPrice;
	public static String tipsCarnnotGetFirstDropPrice;
	public static String tipsAlreadyGetScenarioPrice;
	public static String tipsCarnnotGetScenarioPrice;
	public static String tipsAlreadyGetScenarioSLevelPrice;
	public static String tipsCarnnotGetScenarioSLevelPrice;
	public static String tipsGetScenarioPassItemBagFull;
	public static String tipsGetScenarioSLevelItemBagFull;
	public static String tipsGetScenarioSLevelItemBagFullMailTitle;
	public static String tipsGetScenarioPassItemBagFullMailTitle;
	public static String tipsSaodangStaminaNotEnoughDialogTips1;
	public static String tipsSaodangStaminaNotEnoughDialogTips2;
	public static String tipsSaodangStaminaNotEnoughDialogTips3;
	public static String tipsSaodangStaminaNotEnoughDialogTips4;
	public static String tipsSaodangStaminaNotEnoughDialogTips5;
	public static String tipsSaodangStaminaNotEnoughDialogTips6;
	
	public static String tipsBuyEliteCopyCount;
	public static String tipsJoinEliteCopyFailedCountNotEnough;
	public static String tipsTowerCopyLevelIsComplete;
	public static String tipsTowerCopyChallengeCountFull;
	public static String tipsTowerCopyEndLevel;
	public static String tipsAlreadyGetTowerCopyDayPrice;
	public static String tipsTowerCopyBattleFaild;
	public static String tipsPetChallengeCopyBattleWin;
	public static String tipsPetChallengeCopyBattleFailed;
	public static String tipsPetChallengeCopyBattleFailedReward;
	public static String tipsPetChallengeCopyLastBattleWin;
	public static String tipsSLevelbeHitCount;
	public static String tipsSLevelUseTime;
//	public static String tipsCopyActivityRewardMailTitle;
//	public static String tipsCopyActivityRewardMailContent;

	public static String getTipsLevelNotOpen() {
		return tipsLevelNotOpen;
	}

	public static String getTipsJoinLevelFailedWhileStaminaNotEnough() {
		return tipsJoinLevelFailedWhileStaminaNotEnough;
	}

	public static String getTipsJoinLevelFailedRecoverStamina(int igot) {
		return StringUtil.format(tipsJoinLevelFailedRecoverStamina, igot);
	}

	public static String getTipsJoinLevelFailedRecoverStamina2(int itemCount,
			String itemName) {
		return StringUtil.format(tipsJoinLevelFailedRecoverStamina2, itemName,
				itemCount);
	}

	public static String getTipsJoinLevelFailedWhileMaxCount() {
		return tipsJoinLevelFailedWhileMaxCount;
	}

	public static String getTipsJoinLevelFailed() {
		return tipsJoinLevelFailed;
	}

	public static String getTipsBagCapacityNotEnough() {
		return tipsBagCapacityNotEnough;
	}

	public static String getTipsGetLotteryRewardCountFull(int count) {
		return StringUtil.format(tipsGetLotteryRewardCountFull, count);
	}

	public static String getTipsSendMailItemForBagFullTitle() {
		return tipsSendMailItemForBagFullTitle;
	}

	public static String getTipsSendMailItemForBagFull() {
		return tipsSendMailItemForBagFull;
	}

	public static String getTipsProcessDataError() {
		return tipsProcessDataError;
	}

	public static String getTipsGetLotteryRewardUsePoint(int point) {
		return StringUtil.format(tipsGetLotteryRewardUsePoint, point);
	}

	public static String getTipsJoinCopyLevelFailedWhileFinished() {
		return tipsJoinCopyLevelFailedWhileFinished;
	}

	public static String getTipsJoinCopyLevelFailedWhileMaxCount() {
		return tipsJoinCopyLevelFailedWhileMaxCount;
	}

	public static String getTipsRestCopyLevelFailedWhileNotFinished() {
		return tipsRestCopyLevelFailedWhileNotFinished;
	}

	public static String getTipsRestCopyLevelFailedWhileLvHigher() {
		return tipsRestCopyLevelFailedWhileLvHigher;
	}

	public static String getTipsRestCopyLevel(int point) {
		return StringUtil.format(tipsRestCopyLevel, point);
	}

	public static String getTipsLotteryNotVip(int lv) {
		return StringUtil.format(tipsLotteryNotVip, lv);
	}

	public static String getTipsGetLotteryNotEnoughIgot(int point) {
		return StringUtil.format(tipsGetLotteryNotEnoughIgot, point);
	}

	public static String getTipsRestCopyNotEnoughIgot(int point) {
		return StringUtil.format(tipsRestCopyNotEnoughIgot, point);
	}

	public static String getTipsJoinFriendCopyFailedFriendNotFound() {
		return tipsJoinFriendCopyFailedFriendNotFound;
	}

	public static String getTipsJoinFriendCopyFailedUsePoint(int point) {
		return StringUtil.format(tipsJoinFriendCopyFailedUsePoint, point);
	}

	public static String getTipsJoinFriendCopyFailedCountNotEnough() {
		return tipsJoinFriendCopyFailedCountNotEnough;
	}

	public static String getTipsJoinFriendCopyFailedFriendIsCooling() {
		return tipsJoinFriendCopyFailedFriendIsCooling;
	}

	public static String getTipsBuyFriendCopyCountNotEnoughIgot(int point) {
		return StringUtil.format(tipsBuyFriendCopyCountNotEnoughIgot, point);
	}

	public static String getTipsBuyFriendCopyCount(int point, int vipLv,
			int buyCount) {
		return StringUtil
				.format(tipsBuyFriendCopyCount, point, vipLv, buyCount);
	}

	public static String getTipsFriendCopyMailTitle() {
		return tipsFriendCopyMailTitle;
	}

	public static String getTipsFriendCopyMailContent(String friendName,
			String levelName, int floor) {
		return StringUtil.format(tipsFriendCopyMailContent, friendName,
				levelName, floor);
	}

	public static String getTipsFriendCopyMailContentFullCount(
			String friendName, String levelName, int floor, int maxCount) {
		return StringUtil.format(tipsFriendCopyMailContentFullCount,
				friendName, levelName, floor, maxCount);
	}

	public static String getTipsSaodangNothing() {
		return tipsSaodangNothing;
	}

	public static String getTipsSaodangVipLvNotEnough(int vipLv) {
		return StringUtil.format(tipsSaodangVipLvNotEnough, vipLv);
	}

	public static String getTipsSaodangStaminaNotEnough() {
		return tipsSaodangStaminaNotEnough;
	}

	public static String getTipsCannotSaodang() {
		return tipsCannotSaodang;
	}
	
	public static String getTipsCannotSaodang1() {
		return tipsCannotSaodang1;
	}
	
	public static String getTipsSaodangMaxCount(int count) {
		return StringUtil.format(tipsSaodangMaxCount, count);
	}
	
	public static String getTipsSaodangUsePoint(int count, int point) {
		return StringUtil.format(tipsSaodangUsePoint, count, point);
	}

	public static String getTipsSaodangPointNotEnough(int count, int point) {
		return StringUtil.format(tipsSaodangPointNotEnough, count, point);
	}

	public static String getTipsAlreadyGetFirstDropPrice() {
		return tipsAlreadyGetFirstDropPrice;
	}

	public static String getTipsCarnnotGetFirstDropPrice() {
		return tipsCarnnotGetFirstDropPrice;
	}

	public static String getTipsAlreadyGetScenarioPrice() {
		return tipsAlreadyGetScenarioPrice;
	}

	public static String getTipsCarnnotGetScenarioPrice() {
		return tipsCarnnotGetScenarioPrice;
	}

	public static String getTipsAlreadyGetScenarioSLevelPrice() {
		return tipsAlreadyGetScenarioSLevelPrice;
	}

	public static String getTipsCarnnotGetScenarioSLevelPrice() {
		return tipsCarnnotGetScenarioSLevelPrice;
	}

	public static String getTipsGetScenarioPassItemBagFull() {
		return tipsGetScenarioPassItemBagFull;
	}

	public static String getTipsGetScenarioSLevelItemBagFull() {
		return tipsGetScenarioSLevelItemBagFull;
	}

	public static String getTipsGetScenarioSLevelItemBagFullMailTitle() {
		return tipsGetScenarioSLevelItemBagFullMailTitle;
	}

	public static String getTipsGetScenarioPassItemBagFullMailTitle() {
		return tipsGetScenarioPassItemBagFullMailTitle;
	}

	public static String getTipsSaodangStaminaNotEnoughDialogTips1() {
		return tipsSaodangStaminaNotEnoughDialogTips1;
	}

	public static String getTipsSaodangStaminaNotEnoughDialogTips2(int restPhyItemCount) {
		return StringUtil.format(tipsSaodangStaminaNotEnoughDialogTips2, restPhyItemCount);
	}

	public static String getTipsSaodangStaminaNotEnoughDialogTips3(String tips) {
		return StringUtil.format(tipsSaodangStaminaNotEnoughDialogTips3, tips);
	}

	public static String getTipsSaodangStaminaNotEnoughDialogTips4(int vip,int buyCount) {
		return StringUtil.format(tipsSaodangStaminaNotEnoughDialogTips4, vip,buyCount);
	}

	public static String getTipsSaodangStaminaNotEnoughDialogTips5(int vip,int buyCount) {
		return StringUtil.format(tipsSaodangStaminaNotEnoughDialogTips5, vip,buyCount);
	}

	public static String getTipsSaodangStaminaNotEnoughDialogTips6() {
		return tipsSaodangStaminaNotEnoughDialogTips6;
	}

	
	public static String getTipsBuyEliteCopyCount(int point) {
		return StringUtil
				.format(tipsBuyEliteCopyCount, point);
	}

	public static String getTipsJoinEliteCopyFailedCountNotEnough() {
		return tipsJoinEliteCopyFailedCountNotEnough;
	}

	public static String getTipsTowerCopyLevelIsComplete() {
		return tipsTowerCopyLevelIsComplete;
	}

	public static String getTipsTowerCopyChallengeCountFull() {
		return tipsTowerCopyChallengeCountFull;
	}

	public static String getTipsAlreadyGetTowerCopyDayPrice() {
		return tipsAlreadyGetTowerCopyDayPrice;
	}

	public static String getTipsTowerCopyBattleFaild(int levelNum) {
		return StringUtil.format(tipsTowerCopyBattleFaild, levelNum);
	}

	public static String getTipsPetChallengeCopyBattleWin(int levelNum) {
		return StringUtil.format(tipsPetChallengeCopyBattleWin, levelNum);
	}

	public static String getTipsPetChallengeCopyBattleFailed(String rewardTips) {
		return StringUtil.format(tipsPetChallengeCopyBattleFailed, rewardTips);
	}

	public static String getTipsPetChallengeCopyBattleFailedReward() {
		return tipsPetChallengeCopyBattleFailedReward;
	}

	public static String getTipsPetChallengeCopyLastBattleWin() {
		return tipsPetChallengeCopyLastBattleWin;
	}

	public static String getTipsSLevelbeHitCount(int count) {
		return StringUtil.format(tipsSLevelbeHitCount, count);
	}

	public static String getTipsSLevelUseTime(long time) {
		return StringUtil.format(tipsSLevelUseTime, UtilTool.genReleaseCDTimeString3(time));
	}

	public static String getTipsTowerCopyEndLevel() {
		return tipsTowerCopyEndLevel;
	}
	
	
//	public static String getTipsCopyActivityRewardMailTitle() {
//		return tipsCopyActivityRewardMailTitle;
//	}
//
//	public static String getTipsCopyActivityRewardMailContent() {
//		return tipsCopyActivityRewardMailContent;
//	}

}
