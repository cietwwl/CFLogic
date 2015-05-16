package com.kola.kmp.logic.level.message;

import static com.kola.kmp.protocol.level.KLevelProtocol.CM_REQUEST_SCENARIO_DATA;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.level.KGameScenario;
import com.kola.kmp.logic.map.GameMapExitsEventData;
import com.kola.kmp.logic.map.KGameNormalMap;
import com.kola.kmp.logic.map.KMapModule;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KGetScenarioDataMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KGetScenarioDataMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_REQUEST_SCENARIO_DATA;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		int srcMapId = msg.readInt();
		int exitId = msg.readInt();
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);

		KGameNormalMap map = KMapModule.getGameMapManager().getGameMap(srcMapId);
		if (map == null
				|| map.getGameMapData() == null
				|| map.getGameMapData().getGameMapExitsEventData(exitId) == null
				|| map.getGameMapData().getGameMapExitsEventData(exitId)
						.getExitType() != GameMapExitsEventData.EXIT_EVENT_TYPE_GAMELEVELS) {
			KDialogService.sendUprisingDialog(role, "地图错误，找不到对应的副本。");
			return;
		}
		int scenarioId = map.getGameMapData().getGameMapExitsEventData(exitId).targetId;
		KGameScenario scenario = KGameLevelModuleExtension.getManager()
				.getKGameScenario(scenarioId);
		if (scenario == null) {
			KDialogService.sendUprisingDialog(role, "地图错误，找不到对应的副本。");
			return;
		}

		KGameLevelModuleExtension.getManager().sendScenarioData(role, scenario,
				srcMapId, exitId);

	}
}
