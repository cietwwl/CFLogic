package com.kola.kmp.logic.competition;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.mission.daily.KDailyMissionManager;
import com.kola.kmp.logic.npc.dialog.IDialogProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.CompetitionTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.LevelTips;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;

/**
 * 
 * @author PERRY CHAN
 */
public class KCompetitionDialogProcesser extends IDialogProcesser {

	public KCompetitionDialogProcesser(short minFunId, short maxFunId) {
		super(minFunId, maxFunId);
	}

	public static final short KEY_CONFIRM_ADD_CHALLENGE_TIME = 700;

	public static final short KEY_CONFIRM_CLEAR_CHALLENGE_CD_TIME = 701;

	@Override
	public void processFun(short funId, String script,
			KGamePlayerSession session) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		switch (funId) {
		case KEY_CONFIRM_ADD_CHALLENGE_TIME:
			processAddChallengeTime(role);
			break;
		case KEY_CONFIRM_CLEAR_CHALLENGE_CD_TIME:
			String[] data = script.split(",");
			if (data != null && data.length == 2) {
				int ranking = Integer.parseInt(data[0]);
				long defenderRoleId = Long.parseLong(data[1]);
				processClearCDTimeAndChallenge(role, ranking, defenderRoleId);
			} else {
				KDialogService.sendUprisingDialog(role,
						GlobalTips.getTipsServerBusy());
			}
			break;
		default:
			return;
		}
	}

	public void processAddChallengeTime(KRole role) {
		VIPLevelData vipData = KSupportFactory.getVIPModuleSupport()
				.getVIPLevelData(role.getId());
		int vip_canChallengeCount = vipData.pvpbuyrmb.length;
		KCompetitor roleC = KCompetitionModule.getCompetitionManager()
				.getCompetitorByRoleId(role.getId());
		int buyCount = roleC.getTodayBuyCount().get();
		if (buyCount < 0 || buyCount >= vip_canChallengeCount) {
			KDialogService.sendUprisingDialog(role, CompetitionTips
					.getTipsCannotBuyChallengeTime(vipData.lvl,
							vip_canChallengeCount));
			return;
		}
		int point = vipData.pvpbuyrmb[buyCount];

		if (point < 0) {
			KDialogService.sendUprisingDialog(role,
					GlobalTips.getTipsServerBusy());
			return;
		}

		long result = KSupportFactory.getCurrencySupport().decreaseMoney(
				role.getId(), KCurrencyTypeEnum.DIAMOND, point,
				UsePointFunctionTypeEnum.竞技场次数购买, true);
		// 元宝不足，发送提示
		if (result == -1) {
			KDialogService
					.sendUprisingDialog(role, CompetitionTips
							.getTipsBuyChallengeTimeNotEnoughIgot(point));
		} else {
			KCompetitionModule.getCompetitionManager().processAddChallengeTime(
					role, false);
			KDialogService.sendNullDialog(role);
		}
	}

	public void processClearCDTimeAndChallenge(KRole role, int ranking,
			long defenderRoleId) {
		KCompetitor roleC = KCompetitionModule.getCompetitionManager()
				.getCompetitorByRoleId(role.getId());

		int restTimeSeconds = roleC.getRestCDTimeSeconds();
		int point = KCompetitionManager.caculateCDTimeUsePoint(restTimeSeconds);
		long result = KSupportFactory.getCurrencySupport().decreaseMoney(
				role.getId(), KCurrencyTypeEnum.DIAMOND, point,
				UsePointFunctionTypeEnum.竞技场CD清除, true);
		// 元宝不足，发送提示
		if (result == -1) {
			KDialogService.sendUprisingDialog(role,
					CompetitionTips.getTipsClearCdTimeNotEnoughIgot(point));
		} else {
			KDialogService.sendNullDialog(role);
			KCompetitionModule.getCompetitionManager().challenge(role, ranking,
					defenderRoleId, false);
		}
	}
}
