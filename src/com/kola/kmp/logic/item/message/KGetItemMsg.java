package com.kola.kmp.logic.item.message;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.item.KItem;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.item.KItemProtocol;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-11-22 下午4:49:56
 * </pre>
 */
public class KGetItemMsg implements GameMessageProcesser, KItemProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KGetItemMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GET_ITEM;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		long itemId = msg.readLong();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			KDialogService.sendSimpleDialog(session, "", GlobalTips.服务器繁忙请稍候再试, true);
			return;
		}
		//
		KItem item = KItemLogic.getItem(role.getId(), itemId);
		if (item == null) {
			KPushItemsMsg.pushNull(role.getId());
		} else {
			KPushItemsMsg.pushItem(role, item);
		}
	}
}
