package com.kola.kmp.logic.gang.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.gang.KGangMsgPackCenter;
import com.kola.kmp.protocol.gang.KGangProtocol;

public class KPushGangConstanceMsg implements KGangProtocol {

	public static void sendMsg(KGamePlayerSession session) {
		KGameMessage msg = KGame.newLogicMessage(SM_GANG_PUSH_CONSTANCE);
		KGangMsgPackCenter.packConstance(msg);
		session.send(msg);
	}
}
