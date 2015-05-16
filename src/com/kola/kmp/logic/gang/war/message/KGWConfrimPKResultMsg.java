package com.kola.kmp.logic.gang.war.message;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gang.war.GangWarLogic;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.gang.war.KGangWarProtocol;

public class KGWConfrimPKResultMsg implements GameMessageProcesser, KGangWarProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KGWConfrimPKResultMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GW_CONFRIM_PK_RESULT;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			KDialogService.sendUprisingDialog(role, GlobalTips.服务器繁忙请稍候再试);
			return;
		}

		GangWarLogic.dealMsg_confrimPKResult(role);
		
		KDialogService.sendNullDialog(session);
	}
}
