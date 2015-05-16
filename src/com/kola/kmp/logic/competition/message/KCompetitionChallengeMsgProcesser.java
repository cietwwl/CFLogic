package com.kola.kmp.logic.competition.message;

import static com.kola.kmp.protocol.competition.KCompetitionProtocol.CM_REQUEST_CHALLENGE;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.competition.KCompetitionModule;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KCompetitionChallengeMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KCompetitionChallengeMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_REQUEST_CHALLENGE;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		int ranking = msg.readInt();
		long defcRoleId = msg.readLong();

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		boolean result = KCompetitionModule.getCompetitionManager().challenge(
				role, ranking, defcRoleId, true);
		if (result) {
			KDialogService.sendNullDialog(session);
		}
	}

}
