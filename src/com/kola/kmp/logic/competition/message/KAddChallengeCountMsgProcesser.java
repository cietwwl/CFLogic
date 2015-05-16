package com.kola.kmp.logic.competition.message;

import static com.kola.kmp.protocol.competition.KCompetitionProtocol.CM_ADD_CHALLENGE;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.competition.KCompetitionModule;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KAddChallengeCountMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KAddChallengeCountMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_ADD_CHALLENGE;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		KCompetitionModule.getCompetitionManager().processAddChallengeTime(role, true);
	}

}
