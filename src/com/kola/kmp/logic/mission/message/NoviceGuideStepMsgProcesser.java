package com.kola.kmp.logic.mission.message;

import static com.kola.kmp.protocol.mission.KMissionProtocol.CM_SEND_NOVICE_GUIDE_STEP;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.logging.KGameLogger;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.mission.IMissionMenuImpl;
import com.kola.kmp.logic.mission.KMission;
import com.kola.kmp.logic.mission.KMissionCompleteRecordSet;
import com.kola.kmp.logic.mission.KMissionModuleExtension;
import com.kola.kmp.logic.mission.KMissionModuleSupportImpl;
import com.kola.kmp.logic.mission.KMissionSet;
import com.kola.kmp.logic.mission.KMissionTemplate;
import com.kola.kmp.logic.npc.menu.IMissionMenu;
import com.kola.kmp.logic.npc.menu.KMenuService;
import com.kola.kmp.logic.other.KGameMissionFunTypeEnum;
import com.kola.kmp.logic.other.KGameMissionStatusEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class NoviceGuideStepMsgProcesser implements GameMessageProcesser {
	
	private static final KGameLogger _LOGGER = KGameLogger
			.getLogger(NoviceGuideStepMsgProcesser.class);

	@Override
	public GameMessageProcesser newInstance() {
		return new NoviceGuideStepMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_SEND_NOVICE_GUIDE_STEP;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		int stepId = msg.readByte();
		_LOGGER.debug("### CM_SEND_NOVICE_GUIDE_STEP :::"+stepId);

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);

		KMissionCompleteRecordSet missionSet = KMissionModuleExtension
				.getMissionCompleteRecordSet(role.getId());

		if (missionSet != null) {
			missionSet.completeNoviceGuideStep(stepId);
		}
	}
}
