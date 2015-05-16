package com.kola.kmp.logic.mission.message;
import static com.kola.kmp.protocol.mission.KMissionProtocol.CM_NOTIFY_FUNCTION_FIRST_OPEN_GUIDE_COMPLETED;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.mission.KMissionModuleExtension;
import com.kola.kmp.logic.mission.KMissionSet;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.mission.KMissionProtocol;

public class FirstFunctionGuideCompleteMsgProcesser implements
GameMessageProcesser {
	

	@Override
	public GameMessageProcesser newInstance() {
		return new FirstFunctionGuideCompleteMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_NOTIFY_FUNCTION_FIRST_OPEN_GUIDE_COMPLETED;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		short functionId = msg.readShort();

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role
				.getId());
		
		if (missionSet != null && missionSet.funtionMap.containsKey(functionId)) {
			missionSet.addOrUpdateFunctionInfo(functionId, true, true);
		}
		
		KGameMessage sendMsg = KGame
				.newLogicMessage(KMissionProtocol.SM_NOTIFY_FUNCTION_FIRST_OPEN_GUIDE_COMPLETED);
		sendMsg.writeShort(functionId);
		sendMsg.writeBoolean(true);
		role.sendMsg(sendMsg);
	}

}
