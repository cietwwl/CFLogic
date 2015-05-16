package com.kola.kmp.logic.competition.message;

import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.competition.KCompetitionProtocol;

/**
 * 
 * @author PERRY CHAN
 */
public class KConfirmCompetitionTeamPVPResultMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KConfirmCompetitionTeamPVPResultMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return KCompetitionProtocol.CM_COMFIRM_COMPLETE_TEAM_PVP_BATTLE_RESULT;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(msgEvent.getPlayerSession());
		if(role != null) {
			KSupportFactory.getMapSupport().processRoleFinishBattleReturnToMap(role);
		}
		KDialogService.sendNullDialog(msgEvent.getPlayerSession());
	}
	
}
