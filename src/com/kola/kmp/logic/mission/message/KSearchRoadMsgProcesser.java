package com.kola.kmp.logic.mission.message;

import static com.kola.kmp.protocol.mission.KMissionProtocol.CM_AUTO_SEARCH_ROAD;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.mission.KMissionModuleExtension;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KSearchRoadMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KSearchRoadMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_AUTO_SEARCH_ROAD;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		byte searchRoadTargetType = msg.readByte();
		String searchRoadTargetId = msg.readUtf8String();
		int missionTemplateId = msg.readInt();

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		KMissionModuleExtension.getManager().processPlayerRoleSearchRoad(role, searchRoadTargetType, searchRoadTargetId, missionTemplateId);
	}

}
