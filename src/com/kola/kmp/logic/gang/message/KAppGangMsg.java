package com.kola.kmp.logic.gang.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gang.KGangLogic;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.GangResultExt;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.gang.KGangProtocol;

public class KAppGangMsg implements GameMessageProcesser, KGangProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KAppGangMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GANG_APP_GANG;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		long gangId = msg.readLong();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			GangResultExt result = new GangResultExt();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, gangId, result);
			return;
		}
		// 军团--申请加入军团
		GangResultExt result = KGangLogic.dealMsg_appGang(role, gangId);
		dofinally(session, role, gangId, result);
	}

	private void dofinally(KGamePlayerSession session, KRole role, long gangId, GangResultExt result) {
		KGameMessage msg = KGame.newLogicMessage(SM_GANG_APP_GANG_RESULT);
		msg.writeBoolean(result.isSucess);
		msg.writeUtf8String(result.tips);
		msg.writeLong(gangId);
		session.send(msg);
		
		result.doFinally(role);

		if (result.isSucess) {
			//提示申请列表有变化
			KSyncAppChangeCountMsg.sendMsg(result.gang, 1);
		}
	}
}
