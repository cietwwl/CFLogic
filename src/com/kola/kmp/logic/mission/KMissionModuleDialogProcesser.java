package com.kola.kmp.logic.mission;

import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kmp.logic.mission.KMissionSet.UpdateDailyMissionStruct;
import com.kola.kmp.logic.mission.daily.KDailyMissionManager;
import com.kola.kmp.logic.npc.dialog.IDialogProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.LevelTips;
import com.kola.kmp.logic.util.tips.MissionTips;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;

public class KMissionModuleDialogProcesser extends IDialogProcesser {

	public static final short KEY_MANUAL_REFLASH_DAILY_MISSION = 600;

	public static final short KEY_BUY_DAILY_MISSION = 601;

	public static final short KEY_AUTO_SUBMIT_DAILY_MISSION = 602;

	public KMissionModuleDialogProcesser(short minFunId, short maxFunId) {
		super(minFunId, maxFunId);
	}

	@Override
	public void processFun(short funId, String script,
			KGamePlayerSession session) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		switch (funId) {
		case KEY_MANUAL_REFLASH_DAILY_MISSION:
			processReflashDailyMission(role);
			break;
		case KEY_BUY_DAILY_MISSION:
			processBuyDailyMission(role);
			break;

		case KEY_AUTO_SUBMIT_DAILY_MISSION:
			int missionTemplateId = Integer.parseInt(script);
			processAutoSubmitDailyMission(role, missionTemplateId);
			break;
		}
	}

	public void processBuyDailyMission(KRole role) {

		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role
				.getId());
		int buyCount = missionSet.getDailyMissionInfo().getBuyCount();
		VIPLevelData vipData = KSupportFactory.getVIPModuleSupport()
				.getVIPLevelData(role.getId());
		int vip_can_buy_count = vipData.daytaskrmb.length;
		int usePoint = vipData.daytaskrmb[buyCount];

		if (usePoint <= 0) {
			// KDialogService.sendUprisingDialog(role, "数据不合法，请稍后再试！");
			KDialogService.sendUprisingDialog(role,
					LevelTips.getTipsProcessDataError());
			return;
		}
		long result = KSupportFactory.getCurrencySupport().decreaseMoney(
				role.getId(), KCurrencyTypeEnum.DIAMOND, usePoint,
				UsePointFunctionTypeEnum.购买日常任务, true);
		// 元宝不足，不能进入关卡，发送提示
		if (result == -1) {
			// KDefaultDialogProcesser.showChargeDialog(role.getRoleId(),
			// "重置副本需要消耗" + usePoint
			// + "个元宝，您的元宝数量不足，是否马上充值？充值可获得VIP等级优惠权限哦！");
			// KDefaultDialogProcesser.showChargeDialog(role.getRoleId(),
			// "购买修行任务需要消耗" + usePoint
			// + "个元宝，您的元宝数量不足，是否马上充值？充值可获得VIP等级优惠权限哦！");
			KDialogService.showChargeDialog(role.getId(),
					MissionTips.getTipsBuyDailyMissionNotEnoughIgot(usePoint));
		} else {
			missionSet.buyDailyMission();
			KMissionModuleExtension
					.getManager()
					.getDailyMissionManager()
					.completeOrDropMissionReflashNewDailyMission(role,
							new UpdateDailyMissionStruct());
			KDialogService.sendNullDialog(role);
		}
	}

	public void processReflashDailyMission(KRole role) {
		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role
				.getId());

		long result = KSupportFactory.getCurrencySupport().decreaseMoney(
				role.getId(), KCurrencyTypeEnum.DIAMOND,
				KDailyMissionManager.reflash_use_point,
				UsePointFunctionTypeEnum.日常任务刷新, true);
		// 元宝不足，不能进入关卡，发送提示
		if (result == -1) {
			// KDefaultDialogProcesser.showChargeDialog(role.getRoleId(),
			// "重置副本需要消耗" + usePoint
			// + "个元宝，您的元宝数量不足，是否马上充值？充值可获得VIP等级优惠权限哦！");
			// KDefaultDialogProcesser.showChargeDialog(role.getRoleId(),
			// "购买修行任务需要消耗" + usePoint
			// + "个元宝，您的元宝数量不足，是否马上充值？充值可获得VIP等级优惠权限哦！");
			KDialogService
					.showChargeDialog(
							role.getId(),
							MissionTips
									.getTipsManualReflashDailyMissionNotEnoughIgot(KDailyMissionManager.reflash_use_point));
		} else {
			KMissionModuleExtension.getManager().getDailyMissionManager()
					.manualReflashDailyMission(role, false, true);
			KDialogService.sendNullDialog(role);
		}
	}

	public void processAutoSubmitDailyMission(KRole role, int missionTemplateId) {
		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role
				.getId());

		long result = KSupportFactory.getCurrencySupport().decreaseMoney(
				role.getId(), KCurrencyTypeEnum.DIAMOND,
				KDailyMissionManager.auto_complete_mission_use_point,
				UsePointFunctionTypeEnum.日常任务自动完成, true);
		// 元宝不足，不能进入关卡，发送提示
		if (result == -1) {
			// KDefaultDialogProcesser.showChargeDialog(role.getRoleId(),
			// "重置副本需要消耗" + usePoint
			// + "个元宝，您的元宝数量不足，是否马上充值？充值可获得VIP等级优惠权限哦！");
			// KDefaultDialogProcesser.showChargeDialog(role.getRoleId(),
			// "购买修行任务需要消耗" + usePoint
			// + "个元宝，您的元宝数量不足，是否马上充值？充值可获得VIP等级优惠权限哦！");
			KDialogService
					.showChargeDialog(
							role.getId(),
							MissionTips
									.getTipsAutoSubmitDailyMissionNotEnoughIgot(KDailyMissionManager.auto_complete_mission_use_point));
		} else {
			KMissionModuleExtension
					.getManager()
					.getDailyMissionManager()
					.playerRoleAutoSubmitDailyMission(role, missionTemplateId,
							false);
			KDialogService.sendNullDialog(role);
		}
	}

}
