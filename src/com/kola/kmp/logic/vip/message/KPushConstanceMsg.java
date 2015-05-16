package com.kola.kmp.logic.vip.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.vip.KVIPMsgPackCenter;
import com.kola.kmp.protocol.vip.KVIPProtocol;

public class KPushConstanceMsg implements KVIPProtocol {

	public static void sendMsg(KRole role) {
		KGameMessage msg = KGame.newLogicMessage(SM_VIP_PUSH_CONSTANCE);
		KVIPMsgPackCenter.packConstance(msg, role);
		role.sendMsg(msg);
	}
}
