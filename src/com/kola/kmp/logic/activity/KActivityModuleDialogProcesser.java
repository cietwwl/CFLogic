package com.kola.kmp.logic.activity;

import com.koala.game.logging.KGameLogger;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kmp.logic.activity.transport.KTransportData;
import com.kola.kmp.logic.activity.transport.KTransportManager;
import com.kola.kmp.logic.activity.worldboss.KWorldBossManager;
import com.kola.kmp.logic.activity.worldboss.KWorldBossMessageHandler;
import com.kola.kmp.logic.npc.dialog.IDialogProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.TransportTips;

/**
 * 
 * NPC对话框功能id段<font color="ff0000"><b>900-1000</b></font>的处理器
 * 
 * @author PERRY CHAN
 */
public class KActivityModuleDialogProcesser extends IDialogProcesser {
	
	private static final KGameLogger _LOGGER = KGameLogger.getLogger(KActivityModuleDialogProcesser.class);

	public static final short KEY_REFLASH_TRANSPORT_CARRIER = 900;
	
	public static final short KEY_CLEAR_TRANSPORT_INTERCEPT_COOL_TIME = 901;
	
	public static final short FUN_WORLD_BOSS_RESULT_CONFIRM = 902;
	
	public static final short FUN_CONFIRM_JOIN_WORLD_BOSS = 903;
	
	public static final short FUN_CONFIRM_AUTO_JOIN = 904;

	public static final short FUN_CANCLE_AUTO_JOIN = 905;
	
	/**
	 * @param minFunId
	 * @param maxFunId
	 */
	public KActivityModuleDialogProcesser(short minFunId, short maxFunId) {
		super(minFunId, maxFunId);
	}

	@Override
	public void processFun(short funId, String script, KGamePlayerSession session) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		switch (funId) {
		case KEY_REFLASH_TRANSPORT_CARRIER:
			processReflashTransportCarrier(role);
			break;
		case KEY_CLEAR_TRANSPORT_INTERCEPT_COOL_TIME:
			processClearTransportCoolTime(role);
			break;
		case FUN_WORLD_BOSS_RESULT_CONFIRM:
			if (role != null) {
				KSupportFactory.getMapSupport().processRoleFinishBattleReturnToMap(role);
			}
			break;
		case FUN_CONFIRM_JOIN_WORLD_BOSS:
			if(role != null) {
				CommonResult result = KWorldBossManager.getWorldBossActivity().joinActivity(role);
				if(result.isSucess) {
					KDialogService.sendNullDialog(session);
				} else {
					KDialogService.sendUprisingDialog(session, result.tips);
				}
			}
		case FUN_CONFIRM_AUTO_JOIN:
			if(role != null) {
				KWorldBossManager.processRequestAutoJoin(role, true, true);
			}
		case FUN_CANCLE_AUTO_JOIN:
			if(role != null) {
				KWorldBossMessageHandler.syncAutoJoinFlagToClient(role);
				KDialogService.sendNullDialog(session);
			}
			break;
		}
	}
	
	private void processReflashTransportCarrier(KRole role) {
		KTransportData data = KActivityRoleExtCaCreator.getActivityRoleExtData(
				role.getId()).getTransportData();
		long point = KTransportManager.getReflashUseCount(data).currencyCount;

		if (point < 0) {
			KDialogService.sendUprisingDialog(role,
					GlobalTips.getTipsServerBusy());
			return;
		}

		long result = KSupportFactory.getCurrencySupport().decreaseMoney(
				role.getId(), KCurrencyTypeEnum.DIAMOND,
				point,
				UsePointFunctionTypeEnum.刷新运输载具, true);
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
					TransportTips.getTipsReflashCarrierNotEnoughIgot(point,""));
		} else {
			KTransportManager.reflashCarrier(role, false);
			KDialogService.sendNullDialog(role);
		}
	}
	
	private void processClearTransportCoolTime(KRole role) {
		int point = KTransportManager.clear_cooltime_use_point;

		if (point < 0) {
			KDialogService.sendUprisingDialog(role,
					GlobalTips.getTipsServerBusy());
			return;
		}

		long result = KSupportFactory.getCurrencySupport().decreaseMoney(
				role.getId(), KCurrencyTypeEnum.DIAMOND,
				point,
				UsePointFunctionTypeEnum.清除拦截冷却时间, true);
		// 元宝不足，发送提示
		if (result == -1) {
			KDialogService.sendUprisingDialog(role,
					TransportTips.getTipsClearCoolTimeNotEnoughIgot(point));
		} else {
			KTransportManager.clearCoolTime(role, false);
			KDialogService.sendNullDialog(role);
		}
	}

}
