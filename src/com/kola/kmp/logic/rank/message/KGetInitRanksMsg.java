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

public class KGetInitRanksMsg implements GameMessageProcesser, KRankProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KGetInitRanksMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GET_INI_RANKS;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
//		 * byte 一页多少行
//		 * byte 获取多少页
		int numPerPage = msg.readByte();
		int pageNum = msg.readByte();
		// -------------
		KGameMessage backmsg = KGame.newLogicMessage(SM_GET_INI_RANKS_RESULT);
		backmsg.setEncryption(KGameMessage.ENCRYPTION_ZIP);
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			backmsg.writeByte(0);
			session.send(backmsg);
			return;
		}
		// -------------
		KRankMsgPackCenter.packIniRanks(backmsg, role, numPerPage, pageNum);
		KTeamPVPMsgPackCenter.packIniRanks(backmsg, role, numPerPage, pageNum);
		KGangRankMsgPackCenter.packIniRanks(backmsg, role, numPerPage, pageNum);
		session.send(backmsg);
	}
}