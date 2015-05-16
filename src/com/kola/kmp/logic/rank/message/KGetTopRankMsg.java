package com.kola.kmp.logic.rank.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.rank.KRankMsgPackCenter;
import com.kola.kmp.logic.rank.gang.KGangRankMsgPackCenter;
import com.kola.kmp.logic.rank.teampvp.KTeamPVPMsgPackCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.rank.KRankProtocol;

public class KGetTopRankMsg implements GameMessageProcesser, KRankProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KGetTopRankMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GET_TOPRANK;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			KGameMessage backmsg = KGame.newLogicMessage(SM_GET_TOPRANK_RESULT);
			backmsg.writeInt(10);
			backmsg.writeBoolean(false);
			backmsg.writeBoolean(false);
			backmsg.writeBoolean(false);
			backmsg.writeBoolean(false);
			backmsg.writeBoolean(false);
			session.send(backmsg);
			return;
		}
		// -------------
		pushTopRank(session);
	}

	public static void pushTopRank(KGamePlayerSession session) {
		KGameMessage backmsg = KGame.newLogicMessage(SM_GET_TOPRANK_RESULT);
		KRankMsgPackCenter.packTopRank(backmsg);
		//天梯最强
		KTeamPVPMsgPackCenter.packTopElement(backmsg);
		//军团战力
		KGangRankMsgPackCenter.packTopElement(backmsg);
		session.send(backmsg);
	}
}