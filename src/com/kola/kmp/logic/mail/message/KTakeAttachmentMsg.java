package com.kola.kmp.logic.mail.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.mail.KMailLogic;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.MailResult_Sync;
import com.kola.kmp.logic.util.ResultStructs.MailResult_TakeAtt;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.mail.KMailProtocol;

public class KTakeAttachmentMsg implements GameMessageProcesser, KMailProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KTakeAttachmentMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_TAKE_MAILATTACHMENT;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		long mailId = msg.readLong();
		// -------------
		MailResult_Sync result = null;
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			result = new MailResult_Sync();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, mailId, result);
			return;
		}

		result = KMailLogic.dealMsg_takeAttachment(role, mailId);
		doFinally(session, role, mailId, result);
	}

	private void doFinally(KGamePlayerSession session, KRole role, long mailId, MailResult_Sync result) {
		KGameMessage backMsg = KGame.newLogicMessage(SM_TAKE_MAILATTACHMENT_RESULT);
		backMsg.writeLong(mailId);
		backMsg.writeBoolean(result.isSucess);
		backMsg.writeUtf8String(result.tips);
		session.send(backMsg);

		result.doFinally(role);
	}
}
