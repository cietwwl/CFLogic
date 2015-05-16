package com.kola.kmp.logic.gang.reswar.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gang.reswar.KResWarMsgPackCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.gang.reswar.KGangResWarProtocol;

public class KGrwGetCityDialogDataMsg implements GameMessageProcesser, KGangResWarProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KGrwGetCityDialogDataMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GANGRW_GET_CITYDIALOG_DATA;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		int cityId = msg.readByte();
		// -------------
		KGameMessage backMsg = KGame.newLogicMessage(SM_GANGRW_GET_CITYDIALOG_DATA_RESULT);
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			backMsg.writeBoolean(false);
			backMsg.writeUtf8String(GlobalTips.服务器繁忙请稍候再试);
			backMsg.writeByte(cityId);
			session.send(backMsg);
			return;
		}

		KResWarMsgPackCenter.packCityDialogDatas(backMsg, role, cityId);
		session.send(backMsg);
	}
}
