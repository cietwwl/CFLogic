package com.kola.kmp.logic.gang.reswar.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gang.reswar.ResWarLogic;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.GangResWarResult_Bid;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.gang.reswar.KGangResWarProtocol;

public class KGrwBidMsg implements GameMessageProcesser, KGangResWarProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KGrwBidMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GANGRW_BID;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		int cityId = msg.readByte();
		int value = msg.readInt();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			GangResWarResult_Bid result = new GangResWarResult_Bid();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, cityId, result);
			return;
		}
		GangResWarResult_Bid result = ResWarLogic.dealMsg_bidCity(role, cityId, value);
		dofinally(session, role, cityId, result);
	}

	private void dofinally(KGamePlayerSession session, KRole role, int cityId, GangResWarResult_Bid result) {
		KGameMessage msg = KGame.newLogicMessage(SM_GANGRW_BID_RESULT);
		msg.writeBoolean(result.isSucess);
		msg.writeUtf8String(result.tips);
		msg.writeByte(cityId);
		msg.writeInt(result.我的竞价);
		msg.writeInt(result.追加或竞价规定输入的金额);
		session.send(msg);

		result.doFinally(role);
	}
}
