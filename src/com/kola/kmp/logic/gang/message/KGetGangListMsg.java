package com.kola.kmp.logic.gang.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gang.KGangMsgPackCenter;
//import com.kola.kmp.logic.other.FunctionTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.gang.KGangProtocol;

public class KGetGangListMsg implements GameMessageProcesser, KGangProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KGetGangListMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GET_GANG_LIST;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		byte numPerPage = msg.readByte();
		byte pageNum = msg.readByte();
		short startPage = msg.readShort();
		if (numPerPage > 100) {
			numPerPage = 100;
		}
		if (pageNum > 10) {
			pageNum = 10;
		}
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		// -------------
		KGameMessage backmsg = KGame.newLogicMessage(SM_GET_GANG_LIST_RESULT);
		backmsg.writeByte(numPerPage);
		backmsg.writeByte(pageNum);
		backmsg.writeShort(startPage);
		KGangMsgPackCenter.packGangList(backmsg, role, numPerPage, startPage, pageNum);
		session.send(backmsg);
		
		//
//		KSupportFactory.getMissionSupport().notifyUseFunction(role, FunctionTypeEnum.军团列表);
	}
}
