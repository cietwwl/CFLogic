package com.kola.kmp.logic.chat.bug.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.chat.bug.BugManager;
import com.kola.kmp.logic.chat.message.KGetLinkactionMsg;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.chat.KChatProtocol;

/**
 * <pre>
 * 玩家提交BUG\投诉\意见\其他
 * 
 * @author CamusHuang
 * @creation 2013-6-5 下午3:23:11
 * </pre>
 */
public class KUpBugMsg implements GameMessageProcesser, KChatProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KUpBugMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_UP_BUG;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		int type = msg.readByte();
		String content = msg.readUtf8String();
		String qq = msg.readUtf8String();
		String mobile = msg.readUtf8String();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			CommonResult result = new CommonResult();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, result);
			return;
		}
		// -------------
		CommonResult result = BugManager.recordBug(session, role, type, content, qq, mobile);
		doFinally(session, result);
	}

	private void doFinally(KGamePlayerSession session, CommonResult result) {
		KGameMessage msg = KGame.newLogicMessage(SM_UP_BUG_RESULT);
		msg.writeBoolean(result.isSucess);
		msg.writeUtf8String(result.tips);
		session.send(msg);
	}
}
