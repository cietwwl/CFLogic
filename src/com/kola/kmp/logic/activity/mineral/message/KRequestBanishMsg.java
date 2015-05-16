package com.kola.kmp.logic.activity.mineral.message;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.activity.mineral.KDigMineralActivityManager;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.DigRequestResult;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.activity.KActivityProtocol;

public class KRequestBanishMsg implements GameMessageProcesser, KActivityProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KRequestBanishMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_REQUEST_BANISH;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		int mineId = msg.readInt();
		long oppRoleId = msg.readLong();
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			DigRequestResult result = new DigRequestResult();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, mineId, result);
			return;
		}
		
		DigRequestResult result = KDigMineralActivityManager.dealMsg_requestBanish(role, mineId, oppRoleId);
		
		doFinally(session, role, mineId, result);
	}

	private void doFinally(KGamePlayerSession session, KRole role, int mineId, DigRequestResult result) {
		if (result.tips != null && !result.tips.isEmpty()) {
			KDialogService.sendSimpleDialog(session, "", result.tips);
		} else {
			KDialogService.sendNullDialog(session);
		}
		
		if (result.isShowNameList) {
			KPushMsg.pushRolesInMineral(role.getId(), mineId);
		}
		
		if (result.isSucess) {
			KPushMsg.synMineJob(role.getId(), -1);
		}
	}
}