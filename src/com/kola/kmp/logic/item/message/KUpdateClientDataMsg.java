package com.kola.kmp.logic.item.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult_Ext;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.item.KItemProtocol;

public class KUpdateClientDataMsg implements GameMessageProcesser, KItemProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KUpdateClientDataMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_UPDATE_CLIENTDATA;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		String data = msg.readUtf8String();
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			CommonResult_Ext result = new CommonResult_Ext();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, result);
			return;
		}

		CommonResult_Ext result = KItemLogic.dealMsg_updateClientData(role.getId(), data);
		doFinally(session, role, result);
	}

	private void doFinally(KGamePlayerSession session, KRole role, CommonResult_Ext result) {
		KGameMessage backmsg = KGame.newLogicMessage(SM_UPDATE_CLIENTDATA_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		session.send(backmsg);
		// 处理各种提示、弹开界面二次确认
		result.doFinally(role);
	}
}