package com.kola.kmp.logic.item.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_InheritEquiPrice;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_Equi;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.item.KItemProtocol;

/**
 * <pre>
 * 合成道具
 * 
 * @author CamusHuang
 * @creation 2012-12-12 上午10:11:40
 * </pre>
 */
public class KGetEquiInhritPriceMsg implements GameMessageProcesser, KItemProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KGetEquiInhritPriceMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GET_EQUIINHERIT_PRICE;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		long srcItemId = msg.readLong();
		long tarItemId = msg.readLong();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			ItemResult_InheritEquiPrice result = new ItemResult_InheritEquiPrice();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, srcItemId, tarItemId, result);
			return;
		}

		ItemResult_InheritEquiPrice result = KItemLogic.dealMsg_getInheritEquiPrice(role, srcItemId, tarItemId);

		doFinally(session, role, srcItemId, tarItemId, result);
	}

	private void doFinally(KGamePlayerSession session, KRole role, long srcItemId, long tarItemId, ItemResult_InheritEquiPrice result) {
		KGameMessage backmsg = KGame.newLogicMessage(SM_GET_EQUIINHERIT_PRICE_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		backmsg.writeLong(srcItemId);
		backmsg.writeLong(tarItemId);
		if(result.isSucess){
			backmsg.writeInt((int)result.commonPayGold.currencyCount);
		}
		session.send(backmsg);
	}
}
