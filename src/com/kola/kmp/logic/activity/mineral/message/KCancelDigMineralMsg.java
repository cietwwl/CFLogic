package com.kola.kmp.logic.activity.mineral.message;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.activity.mineral.KDigMineralActivityManager;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.activity.KActivityProtocol;

public class KCancelDigMineralMsg implements GameMessageProcesser, KActivityProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KCancelDigMineralMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_CANCEL_DIG_MINERAL_ACTION;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			CommonResult result = new CommonResult();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, result);
			return;
		}

		CommonResult result = KDigMineralActivityManager.dealMsg_cancelDigMine(role);

		doFinally(session, role, result);
	}

	private void doFinally(KGamePlayerSession session, KRole role, CommonResult result) {
		if (result.tips != null && !result.tips.isEmpty()) {
			KDialogService.sendUprisingDialog(session, result.tips);
		} else {
			KDialogService.sendNullDialog(session);
		}

		if (result.isSucess) {
			KPushMsg.synMineJob(role.getId(), -1);
		}
	}
}