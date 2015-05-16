package com.kola.kmp.logic.rank.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.other.KRankTypeEnum;
import com.kola.kmp.logic.rank.KRankMsgPackCenter;
import com.kola.kmp.logic.rank.gang.KGangRankMsgPackCenter;
import com.kola.kmp.logic.rank.gang.KGangRankTypeEnum;
import com.kola.kmp.logic.rank.teampvp.KTeamPVPMsgPackCenter;
import com.kola.kmp.logic.rank.teampvp.KTeamPVPRankTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.rank.KRankProtocol;

public class KGetRankMsg implements GameMessageProcesser, KRankProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KGetRankMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GET_RANK;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		int rankType = msg.readByte();
		int numPerPage = msg.readByte();
		int pageNum = msg.readByte();
		int startPage = msg.readShort();
		if (numPerPage > 50) {
			numPerPage = 50;
		}
		if (pageNum > 3) {
			pageNum = 3;
		}
		// -------------
		KGameMessage backmsg = KGame.newLogicMessage(SM_GET_RANK_RESULT);
		backmsg.writeByte(rankType);
		backmsg.writeByte(numPerPage);
		backmsg.writeByte(pageNum);
		backmsg.writeShort(startPage);
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			backmsg.writeByte(rankType);
			backmsg.writeShort(0);
			session.send(backmsg);
			return;
		}
		// -------------
		{
			KRankTypeEnum type = KRankTypeEnum.getEnum(rankType);
			if (type != null) {
				KRankMsgPackCenter.packRank(backmsg, role, type, numPerPage, startPage, pageNum);
				session.send(backmsg);
				return;
			}
		}
		{
			KTeamPVPRankTypeEnum type = KTeamPVPRankTypeEnum.getEnum(rankType);
			if (type != null) {
				KTeamPVPMsgPackCenter.packRank(backmsg, role, type, numPerPage, startPage, pageNum);
				session.send(backmsg);
				return;
			}
		}
		{
			KGangRankTypeEnum type = KGangRankTypeEnum.getEnum(rankType);
			if (type != null) {
				KGangRankMsgPackCenter.packRank(backmsg, role, type, numPerPage, startPage, pageNum);
				session.send(backmsg);
				return;
			}
		}

		backmsg.writeByte(rankType);
		backmsg.writeShort(0);
		session.send(backmsg);
		return;
	}
}