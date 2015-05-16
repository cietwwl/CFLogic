package com.kola.kmp.logic.competition.message;

import static com.kola.kmp.protocol.competition.KCompetitionProtocol.CM_REFLASH_COMPETITION_DATA;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.competition.KCompetitionModule;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class ReflashCompetitionDataMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new ReflashCompetitionDataMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_REFLASH_COMPETITION_DATA;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		KDialogService.sendNullDialog(role);
		KCompetitionModule.getCompetitionManager().reflashCompetitionData(role, true, true);
	}

}
