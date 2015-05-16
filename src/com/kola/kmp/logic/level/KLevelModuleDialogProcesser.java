package com.kola.kmp.logic.level;

import java.util.List;

import com.koala.game.logging.KGameLogger;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kmp.logic.level.KGameLevelRecord.PlayerRoleGamelevelData;
import com.kola.kmp.logic.level.copys.KFriendCopyManager;
import com.kola.kmp.logic.mission.daily.KDailyMissionManager;
import com.kola.kmp.logic.npc.dialog.IDialogProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.LevelTips;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;

public class KLevelModuleDialogProcesser extends IDialogProcesser {

	private static final KGameLogger _LOGGER = KGameLogger
			.getLogger(KLevelModuleDialogProcesser.class);

	public static final short KEY_ENTER_NORMAL_LEVEL = 400;

	public static final short KEY_GET_LOTTORY = 401;

	public static final short KEY_RESET_COPY = 402;

	public static final short KEY_BUY_FRIEND_COPY = 403;

	public static final short KEY_COPY_LEVEL_SAODANG = 404;
	
	public static final short KEY_BUY_PET_COPY = 405;
	
	public static final short KEY_BUY_ELITE_COPY = 406;

	public KLevelModuleDialogProcesser(short minFunId, short maxFunId) {
		super(minFunId, maxFunId);
	}

	@Override
	public void processFun(short funId, String script,
			KGamePlayerSession session) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		switch (funId) {
		case KEY_ENTER_NORMAL_LEVEL:
			break;
		case KEY_GET_LOTTORY:
			processGetLottery(role);
			break;
		case KEY_RESET_COPY:
			String[] scriptData = script.split(",");
			int levelId = Integer.parseInt(scriptData[0]);
			byte levelType = Byte.parseByte(scriptData[1]);
			processResetCopy(role, levelId, levelType);
			break;
		case KEY_BUY_FRIEND_COPY:
			processBuyFriendCopy(role);
			break;
		case KEY_COPY_LEVEL_SAODANG:
//			scriptData = script.split(",");
//			levelId = Integer.parseInt(scriptData[0]);
//			levelType = Byte.parseByte(scriptData[1]);
//			short saodangCount = Short.parseShort(scriptData[2]);
//			processCopySaodang(role, levelId, levelType);
			break;
		case KEY_BUY_PET_COPY:
			processBuyPetCopy(role);
			break;	
		case KEY_BUY_ELITE_COPY:
//			processBuyEliteCopy(role);
			break;
		}
	}

	private void processGetLottery(KRole role) {
		int point = KGameLevelModuleExtension.getManager().getLotteryUsePoint(
				role);

		if (point < 0) {
			KDialogService.sendUprisingDialog(role,
					GlobalTips.getTipsServerBusy());
			return;
		}

		long result = KSupportFactory.getCurrencySupport().decreaseMoney(
				role.getId(), KCurrencyTypeEnum.DIAMOND, point,
				UsePointFunctionTypeEnum.关卡宝箱抽奖, true);
		// 元宝不足，不能进入关卡，发送提示
		if (result == -1) {
			// KDefaultDialogProcesser.showChargeDialog(role.getRoleId(),
			// "重置副本需要消耗" + usePoint
			// + "个元宝，您的元宝数量不足，是否马上充值？充值可获得VIP等级优惠权限哦！");
			// KDefaultDialogProcesser.showChargeDialog(role.getRoleId(),
			// "购买修行任务需要消耗" + usePoint
			// + "个元宝，您的元宝数量不足，是否马上充值？充值可获得VIP等级优惠权限哦！");
			// KDialogService
			// .showChargeDialog(
			// role.getId(),
			// MissionTips
			// .getTipsBuyDailyMissionNotEnoughIgot(KDailyMissionManager.reflash_use_point));
			KDialogService.sendUprisingDialog(role,
					LevelTips.getTipsGetLotteryNotEnoughIgot(point));
		} else {
			KGameLevelModuleExtension.getManager()
					.processGetAndSendLotteryInfo(role, false);
			KDialogService.sendNullDialog(role);
		}
	}

	private void processResetCopy(KRole role, int levelId, byte levelType) {
		KGameLevelTypeEnum levelTypeEnum = KGameLevelTypeEnum
				.getEnum(levelType);
		KLevelTemplate level = KGameLevelModuleExtension.getManager()
				.getCopyManager().getCopyLevelTemplate(levelId, levelTypeEnum);

		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance()
				.getGameLevelSet(role.getId());
		PlayerRoleGamelevelData levelData = levelSet.getCopyLevelData(
				level.getLevelId(), level.getLevelType());

		int point = KGameLevelModuleExtension.getManager().getCopyManager()
				.getRestCopyLevelUsePoint(role, level, levelData);

		if (point < 0) {
			KDialogService.sendUprisingDialog(role,
					GlobalTips.getTipsServerBusy());
			return;
		}
		UsePointFunctionTypeEnum usePointType;
		if(levelTypeEnum == KGameLevelTypeEnum.精英副本关卡){
			usePointType = UsePointFunctionTypeEnum.精英副本重置;
		}else{
			usePointType = UsePointFunctionTypeEnum.技术副本重置;
		}
		long result = KSupportFactory.getCurrencySupport().decreaseMoney(
				role.getId(), KCurrencyTypeEnum.DIAMOND, point,
				usePointType, true);
		// 元宝不足，不能进入关卡，发送提示
		if (result == -1) {
			// KDefaultDialogProcesser.showChargeDialog(role.getRoleId(),
			// "重置副本需要消耗" + usePoint
			// + "个元宝，您的元宝数量不足，是否马上充值？充值可获得VIP等级优惠权限哦！");
			// KDefaultDialogProcesser.showChargeDialog(role.getRoleId(),
			// "购买修行任务需要消耗" + usePoint
			// + "个元宝，您的元宝数量不足，是否马上充值？充值可获得VIP等级优惠权限哦！");
			KDialogService.showChargeDialog(role.getId(),
					LevelTips.getTipsRestCopyNotEnoughIgot(point));
		} else {
			KGameLevelModuleExtension
					.getManager()
					.getCopyManager()
					.processResetCopyGameLevel(role, levelId, levelTypeEnum,
							false);
			KDialogService.sendNullDialog(role);
		}
	}

	private void processBuyFriendCopy(KRole role) {
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance()
				.getGameLevelSet(role.getId());
		KGameLevelRecord fRecord = levelSet
				.getCopyRecord(KGameLevelTypeEnum.好友副本关卡);
		
		int buyCount = fRecord.friendCopyData.todayBuyCount;
		VIPLevelData vipData = KSupportFactory.getVIPModuleSupport()
				.getVIPLevelData(role.getId());
		
		int usePoint = vipData.friendbuyrmb[buyCount];		

		if (usePoint > 0) {
			long result = KSupportFactory.getCurrencySupport().decreaseMoney(
					role.getId(), KCurrencyTypeEnum.DIAMOND, usePoint,
					UsePointFunctionTypeEnum.购买好友副本次数, true);
			// 元宝不足，不能进入关卡，发送提示
			if (result == -1) {
				KDialogService.showChargeDialog(role.getId(), LevelTips
						.getTipsBuyFriendCopyCountNotEnoughIgot(usePoint));

			} else {
				KGameLevelModuleExtension.getManager().getFriendCopyManager()
						.processPlayerRoleBuyFriendCopyCount(role, false);
				KDialogService.sendNullDialog(role);
			}
		} else {
			KDialogService.showChargeDialog(role.getId(),
					GlobalTips.getTipsServerBusy());
		}
	}
	
	private void processBuyPetCopy(KRole role) {
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance()
				.getGameLevelSet(role.getId());
		KGameLevelRecord pcRecord = levelSet
				.getCopyRecord(KGameLevelTypeEnum.随从副本关卡);
		
		int buyCount = pcRecord.petCopyData.todayBuyCount;
		VIPLevelData vipData = KSupportFactory.getVIPModuleSupport()
				.getVIPLevelData(role.getId());
		
		int usePoint = vipData.rescueHostages[buyCount];		

		if (usePoint > 0) {
			long result = KSupportFactory.getCurrencySupport().decreaseMoney(
					role.getId(), KCurrencyTypeEnum.DIAMOND, usePoint,
					UsePointFunctionTypeEnum.购买随从副本次数, true);
			// 元宝不足，不能进入关卡，发送提示
			if (result == -1) {
				KDialogService.showChargeDialog(role.getId(), LevelTips
						.getTipsBuyFriendCopyCountNotEnoughIgot(usePoint));

			} else {
				KGameLevelModuleExtension.getManager().getPetCopyManager()
						.processPlayerRoleBuyPetCopyCount(role, false);
				KDialogService.sendNullDialog(role);
			}
		} else {
			KDialogService.sendUprisingDialog(role.getId(),
					GlobalTips.getTipsServerBusy());
		}
	}
	
	/****** 以下内容 20141129注释，精英副本不需要计算次数 ********/
//	private void processBuyEliteCopy(KRole role) {
//		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance()
//				.getGameLevelSet(role.getId());
//		KGameLevelRecord pcRecord = levelSet
//				.getCopyRecord(KGameLevelTypeEnum.精英副本关卡);
//		
//		int buyCount = pcRecord.eliteCopyData.todayBuyCount;
//		
//		
//		int usePoint = KGameLevelModuleExtension.getManager().getCopyManager().getBuyChallenCountUsePoint(buyCount);		
//
//		if (usePoint > 0) {
//			long result = KSupportFactory.getCurrencySupport().decreaseMoney(
//					role.getId(), KCurrencyTypeEnum.DIAMOND, usePoint,
//					UsePointFunctionTypeEnum.精英副本次数购买, true);
//			// 元宝不足，不能进入关卡，发送提示
//			if (result == -1) {
//				KDialogService.showChargeDialog(role.getId(), LevelTips
//						.getTipsBuyFriendCopyCountNotEnoughIgot(usePoint));
//
//			} else {
//				KGameLevelModuleExtension.getManager().getCopyManager()
//						.processPlayerRoleBuyEliteCopyCount(role, false);
//				KDialogService.sendNullDialog(role);
//			}
//		} else {
//			KDialogService.sendUprisingDialog(role.getId(),
//					GlobalTips.getTipsServerBusy());
//		}
//	}

	/****** 以下内容 20141129注释，精英副本扫荡不需要消耗钻石 ********/
//	private void processCopySaodang(KRole role, int levelId, byte levelType) {
//		KGameLevelTypeEnum levelTypeEnum = KGameLevelTypeEnum
//				.getEnum(levelType);
//		KLevelTemplate level = KGameLevelModuleExtension.getManager()
//				.getCopyManager().getCopyLevelTemplate(levelId, levelTypeEnum);
//
//		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance()
//				.getGameLevelSet(role.getId());
//		KGameLevelRecord record = levelSet
//				.getCopyRecord(KGameLevelTypeEnum.精英副本关卡);
//
//		List<Integer> usePointList = KGameLevelModuleExtension.getManager()
//				.getCopyManager()
//				.getSaodangUsePointList(role, level, record);
//		int totalPoint = 0;
//		if (usePointList.size() > 0) {
//			totalPoint = usePointList.get(0);
//		}
//
//		if (totalPoint < 0) {
//			KDialogService.sendUprisingDialog(role,
//					GlobalTips.getTipsServerBusy());
//			return;
//		}
//
//		UsePointFunctionTypeEnum usePointType = null;
//		if (levelTypeEnum == KGameLevelTypeEnum.精英副本关卡) {
//			usePointType = UsePointFunctionTypeEnum.精英副本扫荡;
//		} else {
//			usePointType = UsePointFunctionTypeEnum.技术副本扫荡;
//		}
//		long result = KSupportFactory.getCurrencySupport().decreaseMoney(
//				role.getId(), KCurrencyTypeEnum.DIAMOND, totalPoint,
//				usePointType, true);
//		// 元宝不足，不能进入关卡，发送提示
//		if (result == -1) {
//			// KDefaultDialogProcesser.showChargeDialog(role.getRoleId(),
//			// "重置副本需要消耗" + usePoint
//			// + "个元宝，您的元宝数量不足，是否马上充值？充值可获得VIP等级优惠权限哦！");
//			// KDefaultDialogProcesser.showChargeDialog(role.getRoleId(),
//			// "购买修行任务需要消耗" + usePoint
//			// + "个元宝，您的元宝数量不足，是否马上充值？充值可获得VIP等级优惠权限哦！");
//			KDialogService.showChargeDialog(role.getId(),
//					LevelTips.getTipsSaodangPointNotEnough(1, totalPoint));
//		} else {
//			KGameLevelModuleExtension
//					.getManager()
//					.getCopyManager()
//					.processCopyLevelSaodang(role, levelId, levelTypeEnum, false);
//			KDialogService.sendNullDialog(role);
//		}
//	}

}
