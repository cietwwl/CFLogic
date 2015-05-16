package com.kola.kmp.logic.competition.message;
import static com.kola.kmp.protocol.competition.KCompetitionProtocol.CM_COMFIRM_COMPLETE_COMPETITION_BATTLE_RESULT;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.competition.KCompetitionModule;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KConfirmCompetitionBattleResultMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KConfirmCompetitionBattleResultMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_COMFIRM_COMPLETE_COMPETITION_BATTLE_RESULT;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		KCompetitionModule.getCompetitionManager().confirmCompleteBattle(role);
		KDialogService.sendNullDialog(session);
	}

}
