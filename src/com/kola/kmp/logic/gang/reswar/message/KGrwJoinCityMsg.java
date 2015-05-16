package com.kola.kmp.logic.gang.reswar.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gang.reswar.ResWarLogic;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.GangResWarResult_Join;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.gang.reswar.KGangResWarProtocol;

public class KGrwJoinCityMsg implements GameMessageProcesser, KGangResWarProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KGrwJoinCityMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GANGRW_JOIN_CITY;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		int cityId = msg.readByte();
		// -------------

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			GangResWarResult_Join result = new GangResWarResult_Join();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, cityId, result);
			return;
		}

		GangResWarResult_Join result = ResWarLogic.dealMsg_joinCity(role, cityId);
		dofinally(session, role, cityId, result);
	}

	private void dofinally(KGamePlayerSession session, KRole role, int cityId, GangResWarResult_Join result) {
		KGameMessage backMsg = KGame.newLogicMessage(SM_GANGRW_JOIN_CITY_RESULT);
		backMsg.writeBoolean(result.isSucess);
		backMsg.writeUtf8String(result.tips);
		if (result.isSucess) {
			KGrwForceJoinMsg.packMsg(backMsg, result.city, 0);
		}
		session.send(backMsg);

		if (result.isSucess) {
			// 同步人数、积分
			KGrwSynWarInfosMsg.pushMsg(result.city);
		}
	}
}
