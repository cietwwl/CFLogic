package com.kola.kmp.logic.activity.mineral.message;

import com.koala.game.KGame;
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

public class KRequestDigMineralMsg implements GameMessageProcesser, KActivityProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KRequestDigMineralMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_REQUEST_DIG_MINERAL_ACTION;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		int mineId = msg.readInt();
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			DigRequestResult result = new DigRequestResult();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, mineId, result);
			return;
		}

		DigRequestResult result = KDigMineralActivityManager.dealMsg_requestDigMineral(role, mineId);

		doFinally(session, role, mineId, result);
	}

	private void doFinally(KGamePlayerSession session, KRole role, int mineId, DigRequestResult result) {
		if (result.isSucess) {
			KPushMsg.synMineJob(role.getId(), mineId);
		}
		
		KGameMessage msg = KGame.newLogicMessage(SM_RESPONSE_DIG_MINERAL_ACTION);
		msg.writeBoolean(result.isSucess);
		msg.writeUtf8String(result.tips);
		msg.writeInt(mineId);
		msg.writeBoolean(result.isShowNameList);
		if (result.isShowNameList) {
			KDigMineralActivityManager.packRoleInMineral(msg, mineId);
		}
		session.send(msg);
	}
}