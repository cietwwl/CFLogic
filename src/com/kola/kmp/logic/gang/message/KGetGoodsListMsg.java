package com.kola.kmp.logic.gang.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gang.KGangMsgPackCenter;
import com.kola.kmp.protocol.gang.KGangProtocol;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2013-1-30 上午11:38:16
 * </pre>
 */
public class KGetGoodsListMsg implements GameMessageProcesser, KGangProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KGetGoodsListMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GANG_GET_GOODS_LIST;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		// -------------
		//
		KGameMessage backmsg = KGame.newLogicMessage(SM_GANG_GET_GOODS_LIST_RESULT);
		KGangMsgPackCenter.packGoodsList(backmsg);
		session.send(backmsg);
	}

}
