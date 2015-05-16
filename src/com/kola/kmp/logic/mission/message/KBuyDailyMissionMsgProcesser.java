package com.kola.kmp.logic.mission.message;

import static com.kola.kmp.protocol.mission.KMissionProtocol.CM_BUY_DAILY_MISSION_COUNT;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.mission.KMissionModuleDialogProcesser;
import com.kola.kmp.logic.mission.KMissionModuleExtension;
import com.kola.kmp.logic.mission.KMissionSet;
import com.kola.kmp.logic.mission.KMissionSet.UpdateDailyMissionStruct;
import com.kola.kmp.logic.mission.daily.KDailyMissionManager;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.LevelTips;
import com.kola.kmp.logic.util.tips.MissionTips;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;

public class KBuyDailyMissionMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KBuyDailyMissionMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_BUY_DAILY_MISSION_COUNT;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		processBuyMission(role);
	}

	private void processBuyMission(KRole role) {
		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role
				.getId());
		int buyCount = missionSet.getDailyMissionInfo().getBuyCount();
		VIPLevelData vipData = KSupportFactory.getVIPModuleSupport()
				.getVIPLevelData(role.getId());
		int vip_can_buy_count = vipData.daytaskrmb.length;

		// int restFreeCount =
		// missionSet.getDailyMissionInfo().getRestFreeCompletedMissionCount();
		// int nowCompleteCount = missionSet.getDailyMissionInfo()
		// .getTodayCompletedMissionCount();

		if (buyCount >= vip_can_buy_count) {
			KDialogService.sendUprisingDialog(role, MissionTips
					.getTipsCannotBuyDailyMission(vipData.lvl,
							vip_can_buy_count));
		} else {
			// int usePoint = KMissionModuleExtension.getManager()
			// .getDailyMissionManager().buyMissionUsePointMap.get(count);
			int restBuyCount = vipData.daytaskrmb.length - buyCount;
			int usePoint = vipData.daytaskrmb[buyCount];
			KMissionModuleExtension
					.getManager()
					.getDailyMissionManager()
					.sendDailyMissionTipsMessage(
							role,
							KMissionModuleDialogProcesser.KEY_BUY_DAILY_MISSION,
							MissionTips
									.getTipsBuyDailyMissionUsePoint(
											usePoint,
											KDailyMissionManager.add_complete_mission_count,
											KSupportFactory
													.getVIPModuleSupport()
													.getVipLv(role.getId()),
											restBuyCount), true, "");
		}
	}
}
