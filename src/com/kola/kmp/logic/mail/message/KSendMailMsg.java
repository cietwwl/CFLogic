package com.kola.kmp.logic.mail.message;

import java.util.HashSet;
import java.util.Set;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.mail.KMailLogic;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.mail.KMailProtocol;

public class KSendMailMsg implements GameMessageProcesser, KMailProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KSendMailMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_SEND_MAIL;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		byte mode = msg.readByte();//模式0表示好友，1表示军团，不支持混发
		int count = msg.readShort();
		Set<Long> roleIds = new HashSet<Long>();
		for(int i=0;i<count;i++){
			roleIds.add(msg.readLong());
		}
		String mailTitle = msg.readUtf8String();
		String mailContent = msg.readUtf8String();
		// -------------
		CommonResult result = null;
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			result = new CommonResult();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, result);
			return;
		}

		result = KMailLogic.dealMsg_sendMail(role, mode, roleIds, mailTitle, mailContent);
		doFinally(session, role, result);
	}

	private void doFinally(KGamePlayerSession session, KRole role, CommonResult result) {
		KGameMessage backMsg = KGame.newLogicMessage(SM_SEND_MAIL_RESULT);
		backMsg.writeBoolean(result.isSucess);
		backMsg.writeUtf8String(result.tips);
		session.send(backMsg);
	}
}
