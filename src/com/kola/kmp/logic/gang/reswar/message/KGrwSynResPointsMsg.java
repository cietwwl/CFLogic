package com.kola.kmp.logic.gang.reswar.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.gang.reswar.KResWarMsgPackCenter;
import com.kola.kmp.logic.gang.reswar.ResWarCity;
import com.kola.kmp.protocol.gang.reswar.KGangResWarProtocol;

public class KGrwSynResPointsMsg implements KGangResWarProtocol {

	public static void pushMsg(ResWarCity city) {
		KGameMessage backMsg = KGame.newLogicMessage(SM_GANGRW_SYN_RESPOINTS);
		KResWarMsgPackCenter.packResPoints(backMsg, city);
		// 向城市对战军团成员发送
		KResWarMsgPackCenter.sendMsgToRoleInCity(backMsg, city);
	}
}
