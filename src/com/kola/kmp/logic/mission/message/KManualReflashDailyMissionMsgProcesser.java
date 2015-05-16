package com.kola.kmp.logic.mission.message;

import static com.kola.kmp.protocol.mission.KMissionProtocol.CM_MANUAL_REFLASH_DAILY_MISSION;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.mission.KMissionModuleExtension;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KManualReflashDailyMissionMsgProcesser implements
		GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KManualReflashDailyMissionMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_MANUAL_REFLASH_DAILY_MISSION;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		KMissionModuleExtension.getManager().getDailyMissionManager()
				.manualReflashDailyMission(role, true, true);
		KDialogService.sendNullDialog(role);
	}

}
