package com.kola.kmp.logic.gang.war.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gang.KGangMsgPackCenter;
import com.kola.kmp.logic.gang.war.KGangWarMsgPackCenter;
import com.kola.kmp.logic.npc.dialog.KDialogService;
//import com.kola.kmp.logic.other.FunctionTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.gang.war.KGangWarProtocol;

public class KGWGetSignUpListMsg implements GameMessageProcesser, KGangWarProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KGWGetSignUpListMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GW_GET_SIGNUP_LIST;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		byte numPerPage = msg.readByte();
		byte pageNum = msg.readByte();
		short startPage = msg.readShort();
		if (numPerPage > 50) {
			numPerPage = 50;
		}
		if (pageNum > 3) {
			pageNum = 3;
		}
		// -------------
		// -------------
		KGameMessage backmsg = KGame.newLogicMessage(SM_GW_GET_SIGNUP_LIST_RESULT);
		backmsg.writeByte(numPerPage);
		backmsg.writeByte(pageNum);
		backmsg.writeShort(startPage);
		KGangWarMsgPackCenter.packSignUpList(backmsg, numPerPage, startPage, pageNum);
		session.send(backmsg);
	}
}
