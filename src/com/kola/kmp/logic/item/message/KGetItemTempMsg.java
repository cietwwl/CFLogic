package com.kola.kmp.logic.item.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.item.KItem;
import com.kola.kmp.logic.item.KItemDataManager;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.item.KItemMsgPackCenter;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_EquiUpStar;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.ItemTips;
import com.kola.kmp.protocol.item.KItemProtocol;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-11-22 下午4:49:56
 * </pre>
 */
public class KGetItemTempMsg implements GameMessageProcesser, KItemProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KGetItemTempMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GET_ITEMTEMP;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		String itemCode = msg.readUtf8String();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			doFinally(session, role, null, GlobalTips.服务器繁忙请稍候再试);
			return;
		}
		//
		KItemTempAbs temp = KItemDataManager.mItemTemplateManager.getItemTemplate(itemCode);

		doFinally(session, role, temp, ItemTips.物品不存在);
	}

	private void doFinally(KGamePlayerSession session, KRole role, KItemTempAbs temp, String errorTips) {

		KGameMessage backmsg = KGame.newLogicMessage(SM_GET_ITEMTEMP_RESULT);
		backmsg.writeBoolean(temp != null);
		if (temp == null) {
			backmsg.writeUtf8String(errorTips);
		} else {
			KItemMsgPackCenter.packItem(backmsg, temp, 1);
		}
		session.send(backmsg);
	}
}
