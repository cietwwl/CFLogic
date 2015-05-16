package com.kola.kmp.logic.competition.message;
import static com.kola.kmp.protocol.competition.KCompetitionProtocol.CM_VISIT_HALL;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.competition.KCompetitionModule;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KVisitHallMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KVisitHallMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_VISIT_HALL;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		int position = (int)msg.readByte();

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		KCompetitionModule.getCompetitionManager().processVisitHall(role, position);
	}

}
