package com.kola.kmp.logic.activity.message;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.activity.worldboss.KWorldBossManager;

import static com.kola.kmp.protocol.activity.KActivityProtocol.CM_REQUEST_LAST_RANKING;

/**
 * 
 * @author PERRY CHAN
 */
public class KWorldBossGetLastRankingMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KWorldBossGetLastRankingMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_REQUEST_LAST_RANKING;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGameMessage msg = KWorldBossManager.getWorldBossActivity().getLastRankingMsg();
		msgEvent.getPlayerSession().send(msg);
	}

}
