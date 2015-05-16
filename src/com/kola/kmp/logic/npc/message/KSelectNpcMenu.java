package com.kola.kmp.logic.npc.message;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.npc.KNpcProtocol;

public class KSelectNpcMenu implements GameMessageProcesser, KNpcProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KSelectNpcMenu();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_SELECT_NPC_MENU;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		int missionTempId = msg.readInt();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			KDialogService.sendUprisingDialog(session, GlobalTips.服务器繁忙请稍候再试);
			return;
		}
		//
		KSupportFactory.getMissionSupport().nofityForRoleSelectedMission(role, missionTempId);
	}
}