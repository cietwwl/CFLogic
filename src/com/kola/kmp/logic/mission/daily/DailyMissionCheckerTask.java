package com.kola.kmp.logic.mission.daily;

import java.util.List;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import com.koala.game.exception.KGameServerException;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.activity.KActivityRoleExtCaCreator;
import com.kola.kmp.logic.activity.goldact.KGoldActivity;
import com.kola.kmp.logic.activity.newglodact.KNewGoldActivity;
import com.kola.kmp.logic.competition.KCompetitionTask;
import com.kola.kmp.logic.gamble.KGambleModule;
import com.kola.kmp.logic.gamble.KGambleRoleExtCACreator;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.level.KGameLevelRecord;
import com.kola.kmp.logic.level.KGameLevelSet;
import com.kola.kmp.logic.mission.KMission;
import com.kola.kmp.logic.mission.KMissionModuleExtension;
import com.kola.kmp.logic.mission.KMissionSet;
import com.kola.kmp.logic.mission.guide.KGuideManager;
import com.kola.kmp.logic.other.KFunctionTypeEnum;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.DayClearTask;

public class DailyMissionCheckerTask extends DayClearTask {

	// @Override
	// public String getName() {
	// return DailyMissionCheckerTask.class.getName();
	// }

	// @Override
	// public Object onTimeSignal(KGameTimeSignal timeSignal)
	// throws KGameServerException {
	//
	// return null;
	// }

	// @Override
	// public void done(KGameTimeSignal timeSignal) {
	// timeSignal.getTimer().newTimeSignal(this,
	// KDailyMissionManager.dailyMissionCheckerTimeSeconds,
	// TimeUnit.SECONDS);
	// }

	// @Override
	// public void rejected(RejectedExecutionException e) {
	//
	// }

	// public static int getRestCheckTimeSeconds() {
	// long nowTime = System.currentTimeMillis();
	// long tomorrowTimeMillis = UtilTool.getTommorowStart(nowTime)
	// .getTimeInMillis();
	// int result = (int) ((tomorrowTimeMillis - nowTime) / 1000) + 10;
	// System.out.println("getRestCheckTimeSeconds():::::::::" + result);
	// return result;
	//
	// }

	// public static void main(String[] a) {
	// System.out.println(getRestCheckTimeSeconds());
	// }

	@Override
	public String getNameCN() {
		return DailyMissionCheckerTask.class.getName();
	}

	@Override
	public void doWork() throws KGameServerException {
		List<Long> allOnlineRoles = KSupportFactory.getRoleModuleSupport().getAllOnLineRoleIds();

		for (Long roleId : allOnlineRoles) {
			KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);

			// 检测日常任务次日更新
			KMissionSet missionSet = KMissionModuleExtension.getMissionSet(roleId);
			if (missionSet != null && missionSet.getDailyMissionInfo() != null) {
				if (KGuideManager.checkFunctionIsOpen(role, KFunctionTypeEnum.日常任务.functionId)) {
					boolean isChange = missionSet.checkAndResetDailyMissionInfo(true);

					if (isChange) {
						KMissionModuleExtension.getManager().getDailyMissionManager().processSendUpdateMissionData(role, missionSet);
					}
				}
				// 检测精英副本和技术副本次日更新
				if (KGuideManager.checkFunctionIsOpen(role, KFunctionTypeEnum.精英副本.functionId) || KGuideManager.checkFunctionIsOpen(role, KFunctionTypeEnum.技术副本.functionId)) {
					KSupportFactory.getLevelSupport().checkAndResetCopyData(role);
				}
				// 检测好友地下城次日更新
				if (KGuideManager.checkFunctionIsOpen(role, KFunctionTypeEnum.好友地下城.functionId)) {
					KSupportFactory.getLevelSupport().checkAndResetFriendCopyData(role);
				}

				// 检测产金活动
				if (KSupportFactory.getMissionSupport().checkFunctionIsOpen(role, KFunctionTypeEnum.赚金币)) {
					if (KActivityRoleExtCaCreator.getNewGoldActivityRoleRecordData(role.getId()).checkAndRestData(true)) {
						KNewGoldActivity.getInstance().sendUpdateActivity(role);
					}
				}

				// 物资运输活动
				if (KSupportFactory.getMissionSupport().checkFunctionIsOpen(role, KFunctionTypeEnum.运送物资)) {
					KActivityRoleExtCaCreator.getActivityRoleExtData(role.getId()).getTransportData().checkAndRestData(true);
				}

//				// 许愿检测
//				if (KSupportFactory.getMissionSupport().checkFunctionIsOpen(role, KFunctionTypeEnum.许愿)) {
//					if (KGambleRoleExtCACreator.getGambleRoleExtData(role.getId()).checkAndRestWishData()) {
//						KGambleModule.getWishSystemManager().sendWishData(role, null);
//					}
//				}
				// 爬塔副本
				if (KSupportFactory.getMissionSupport().checkFunctionIsOpen(role, KFunctionTypeEnum.爬塔副本)) {
					if (KGameLevelModuleExtension.getManager().getTowerCopyManager().checkAndResetTowerCopyDatas(role, true)) {
						KGameLevelModuleExtension.getManager().getTowerCopyManager().completeOrUpdateCopyInfo(role, KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId()), null);
					}
				}
				// 随从挑战副本
				if (KSupportFactory.getMissionSupport().checkFunctionIsOpen(role, KFunctionTypeEnum.随从挑战)) {
					if (KGameLevelModuleExtension.getManager().getPetChallengeCopyManager().checkAndResetPetChallengeCopyDatas(role, true)) {
						KGameLevelModuleExtension.getManager().getPetChallengeCopyManager().updateCopyInfo(role);
					}
				}
				// 高级随从挑战副本
				if (KSupportFactory.getMissionSupport().checkFunctionIsOpen(role, KFunctionTypeEnum.高级随从试炼)) {
					// 获取关卡记录
					KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
					KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.高级随从挑战副本关卡);
					if(record!=null && record.seniorPetChallengeCopyData != null){
						record.seniorPetChallengeCopyData.checkAndResetSeniorPetChallengeCopyData(true);
					}
				}
				//检测幸运转盘
				if (KSupportFactory.getMissionSupport().checkFunctionIsOpen(role, KFunctionTypeEnum.抽奖)) {
					KGambleModule.getWish2Manager().checkAndReflashWish2Data(role);
				}
			}
		}
	}
}
