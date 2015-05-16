package com.kola.kmp.logic.relationship.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.relationship.IntercourseCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.relationship.KRelationShipProtocol;

public class KConfirmIntercourseResultMsg implements GameMessageProcesser, KRelationShipProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KConfirmIntercourseResultMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_CONFRIM_INTERCOURSE_RESULT;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			KDialogService.sendUprisingDialog(session, GlobalTips.服务器繁忙请稍候再试);
			return;
		}
		// -------------
		IntercourseCenter.dealMsg_confrimPVPResult(role);
	}
}