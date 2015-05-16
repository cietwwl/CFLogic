package com.kola.kmp.logic.level.message;

import static com.kola.kmp.protocol.level.KLevelProtocol.CM_GET_SCENARIO_S_ITEM;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KGetScenarioSLevelItemMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KGetScenarioSLevelItemMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GET_SCENARIO_S_ITEM;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		int scenarioId = msg.readInt();
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);

		KGameLevelModuleExtension.getManager().processGetScenarioSLevelItem(role,
				scenarioId);
	}

}
