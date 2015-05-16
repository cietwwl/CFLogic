package com.kola.kmp.logic.currency.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.currency.KCurrencyMsgPackCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.protocol.currency.KCurrencyProtocol;

public class KPushFirstChargeRewardMsg implements KCurrencyProtocol {

	public static void sendMsg(KGamePlayerSession session, KRole role) {
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_FIRST_CHARGE_REWARD);
		KCurrencyMsgPackCenter.packFirstChargeRewardData(role, msg);
		session.send(msg);
	}

	public static void sendMsg(KRole role) {
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_FIRST_CHARGE_REWARD);
		KCurrencyMsgPackCenter.packFirstChargeRewardData(role, msg);
		role.sendMsg(msg);
	}

}
