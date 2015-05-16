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

/**
 * <pre>
 * 专用于合成宝石，分为金币合成和宝石合成，会根据材料表头执行扣费和成功率，并在失败时损失宝石
 * 
 * @deprecated 已作废
 * @author CamusHuang
 * @creation 2012-12-12 上午10:11:40
 * </pre>
 */
public class KComposeStoneMsg implements GameMessageProcesser, KItemProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KComposeStoneMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_COMPOSE_STONE;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		long itemId = msg.readLong();
		boolean isCommon = msg.readBoolean();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			CommonResult_Ext result = new CommonResult_Ext();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, itemId, result);
			return;
		}

		// 处理消息的过程
		CommonResult_Ext result = KItemLogic.dealMsg_composeStone(role, itemId, isCommon);

		// -------------
		dofinally(session, role, itemId, result);
	}

	private void dofinally(KGamePlayerSession session, KRole role, long itemId, CommonResult_Ext result) {
		KGameMessage backmsg = KGame.newLogicMessage(SM_USE_ITEM_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		backmsg.writeLong(itemId);
		session.send(backmsg);

		// 处理各种提示、弹开界面二次确认
		result.doFinally(role);
	}
}
