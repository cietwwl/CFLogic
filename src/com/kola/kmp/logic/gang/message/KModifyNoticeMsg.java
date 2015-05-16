package com.kola.kmp.logic.gang.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gang.KGangLogic;
import com.kola.kmp.logic.other.KChatChannelTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.GangResult;
import com.kola.kmp.logic.util.tips.GangTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.gang.KGangProtocol;

public class KModifyNoticeMsg implements GameMessageProcesser, KGangProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KModifyNoticeMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GANG_MODIFY_NOTICE;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		String newNotice = msg.readUtf8String();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			GangResult result = new GangResult();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, newNotice, result);
			return;
		}
		// 军团--修改公告
		GangResult result = KGangLogic.dealMsg_modifyNotice(role.getId(), newNotice);
		dofinally(session, role, newNotice, result);
	}

	private void dofinally(KGamePlayerSession session, KRole role, String newNotice, GangResult result) {
		KGameMessage msg = KGame.newLogicMessage(SM_GANG_MODIFY_NOTICE_RESULT);
		msg.writeBoolean(result.isSucess);
		msg.writeUtf8String(result.tips);
		session.send(msg);

		if (result.isSucess) {
			// 通知现存成员：军团频道
			KSupportFactory.getChatSupport().sendChatToAnyChannel(KChatChannelTypeEnum.军团, GangTips.军团公告前缀 + newNotice, result.gang.getId());
			// 同步公告
			KSyncNoticeMsg.sendMsg(result.gang, newNotice);
		}
	}
}
