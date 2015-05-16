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

public class KGrwGiveUpResPointMsg implements GameMessageProcesser, KGangResWarProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KGrwGiveUpResPointMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GANGRW_GIVEUP_RESPOINT;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		int cityId = msg.readByte();
		int resPointId = msg.readByte();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			GangResWarResult_Join result = new GangResWarResult_Join();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, cityId, resPointId, result);
			return;
		}
		GangResWarResult_Join result = ResWarLogic.dealMsg_giveUpResPoint(role, cityId, resPointId);
		dofinally(session, role, cityId, resPointId, result);
	}

	private void dofinally(KGamePlayerSession session, KRole role, int cityId, int resPointId, GangResWarResult_Join result) {
		KGameMessage msg = KGame.newLogicMessage(SM_GANGRW_GIVEUP_RESPOINT_RESULT);
		msg.writeBoolean(result.isSucess);
		msg.writeUtf8String(result.tips);
		msg.writeByte(cityId);
		msg.writeByte(resPointId);
		session.send(msg);

		if (result.isSucess) {
			// 同步资源点数据
			KGrwSynResPointsMsg.pushMsg(result.city);
		}
	}
}
