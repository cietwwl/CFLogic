package com.kola.kmp.logic.mission.message;
import static com.kola.kmp.protocol.mission.KMissionProtocol.CM_GET_DAILY_MISSION_PRICE_BOX;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.mission.KMissionModuleExtension;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KGetDailyMissionPriceBoxMsgProcesser  implements GameMessageProcesser{

	@Override
	public GameMessageProcesser newInstance() {
		return new KGetDailyMissionPriceBoxMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GET_DAILY_MISSION_PRICE_BOX;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		int boxId = msg.readInt();
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		KMissionModuleExtension.getManager().getDailyMissionManager().processGetPriceBox(role,boxId);
	}

}
