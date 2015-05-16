package com.kola.kmp.logic.level.message;

import static com.kola.kmp.protocol.level.KLevelProtocol.CM_MAX_LEVEL_SEARCH_ROAD;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.level.KGameLevelSet;
import com.kola.kmp.logic.level.KLevelTemplate;
import com.kola.kmp.logic.map.AutoSearchRoadTrack;
import com.kola.kmp.logic.map.AutoSearchRoadTrack.RoadPath;
import com.kola.kmp.logic.mission.KMissionModuleExtension;
import com.kola.kmp.logic.mission.KMissionSet;
import com.kola.kmp.logic.mission.MissionCompleteCondition.GameLevelTask;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.npc.message.KNPCOrderMsg;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.other.KMissionSearchRoadTypeEnum;
import com.kola.kmp.logic.other.KNPCOrderEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.LevelTips;
import com.kola.kmp.protocol.level.KLevelProtocol;
import com.kola.kmp.protocol.mission.KMissionProtocol;

public class KMaxLevelSearchRoadMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KMaxLevelSearchRoadMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_MAX_LEVEL_SEARCH_ROAD;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);

		KDialogService.sendNullDialog(role);

		processPlayerRoleMaxLevelSearchRoad(role);

	}

	/**
	 * 处理关卡自动寻路
	 * 
	 * @param role
	 */
	private void processPlayerRoleMaxLevelSearchRoad(KRole role) {

		AutoSearchRoadTrack track = null;

		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance()
				.getGameLevelSet(role.getId());
		KLevelTemplate level = KGameLevelModuleExtension.getManager()
				.getKGameLevel(levelSet.maxCompleteNormalLevelId);
		int levelId = levelSet.maxCompleteNormalLevelId;
		if (level == null) {
			sendSearchRoadResult(role, false, GlobalTips.getTipsServerBusy());
			return;
		}
		if (level.getHinderGameLevelList().size() > 0) {
			KLevelTemplate hinderLevel = level.getHinderGameLevelList().get(0);
			if (hinderLevel != null) {
				if (KGameLevelModuleExtension.getManager()
						.judgeGameLevelOpenState(role, levelSet, hinderLevel) == KLevelTemplate.GAME_LEVEL_STATE_OPEN) {
					levelId = hinderLevel.getLevelId();
				} 
//				else if (KGameLevelModuleExtension.getManager()
//						.judgeGameLevelOpenState(role, levelSet, level) != KLevelTemplate.GAME_LEVEL_STATE_OPEN) {
//					sendSearchRoadResult(role, false,
//							LevelTips.getTipsLevelNotOpen());
//					return;
//				}
			}
		}
//		if (KGameLevelModuleExtension.getManager().judgeGameLevelOpenState(
//				role, levelSet, level) != KLevelTemplate.GAME_LEVEL_STATE_OPEN) {
//			sendSearchRoadResult(role, false, LevelTips.getTipsLevelNotOpen());
//			return;
//		}

		int sccnarioId = KGameLevelModuleExtension.getManager()
				.getKGameLevel(levelId).getScenarioId();
		track = KSupportFactory.getMapSupport().autoDirectToGameLevel(role,
				sccnarioId);

		if (track != null) {
			sendSearchRoadResult(role, true, null);
			KGameMessage sendMsg = KGame
					.newLogicMessage(KMissionProtocol.SM_AUTO_SEARCH_ROAD);
			sendMsg.writeInt(track.getRoadPathStack().size());
			while (!track.getRoadPathStack().isEmpty()) {
				RoadPath path = track.getRoadPathStack().pop();
				sendMsg.writeByte(path.pathType);
				sendMsg.writeInt(path.targetId);

				if (path.pathType == RoadPath.PATH_TYPE_WALK_TO_EXITS) {
					if (track.getRoadPathStack().isEmpty()) {
						sendMsg.writeBoolean(true);
						sendMsg.writeInt(levelId);
					} else {
						sendMsg.writeBoolean(false);
					}
				}
			}
			role.sendMsg(sendMsg);
		} else {
			sendSearchRoadResult(role, false, GlobalTips.getTipsServerBusy());
		}
	}

	private void sendSearchRoadResult(KRole role, boolean isSuccess, String tips) {
		KGameMessage sendMsg = KGame
				.newLogicMessage(KLevelProtocol.SM_LEVEL_SEARCH_ROAD);
		sendMsg.writeBoolean(isSuccess);
		if (!isSuccess) {
			if (tips != null) {
				sendMsg.writeUtf8String(tips);
			} else {
				sendMsg.writeUtf8String("");
			}
		}
		role.sendMsg(sendMsg);
	}

}
