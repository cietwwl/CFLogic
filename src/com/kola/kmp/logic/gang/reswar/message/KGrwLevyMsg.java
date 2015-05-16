package com.kola.kmp.logic.gang.reswar.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gang.reswar.ResWarLogic;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.GangResultExt;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.gang.reswar.KGangResWarProtocol;

public class KGrwLevyMsg implements GameMessageProcesser, KGangResWarProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KGrwLevyMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GANGRW_LEVY;
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
			GangResultExt result = new GangResultExt();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, cityId, result);
			return;
		}
		GangResultExt result = ResWarLogic.dealMsg_levyCity(role, cityId);
		dofinally(session, role, cityId, result);
	}

	private void dofinally(KGamePlayerSession session, KRole role, int cityId, GangResultExt result) {
		KGameMessage msg = KGame.newLogicMessage(SM_GANGRW_LEVY_RESULT);
		msg.writeBoolean(result.isSucess);
		msg.writeUtf8String(result.tips);
		msg.writeByte(cityId);
		session.send(msg);

		result.doFinally(role);
	}
}
