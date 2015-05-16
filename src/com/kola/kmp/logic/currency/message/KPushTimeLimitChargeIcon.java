package com.kola.kmp.logic.currency.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.currency.KCurrencyProtocol;

public class KPushTimeLimitChargeIcon implements KCurrencyProtocol {

	public static void sendMsg(long roleId, int backRate, int releaseTime) {
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_TIMELIMIT_CHARGE_ICON);
		msg.writeInt(releaseTime);
		msg.writeInt(backRate);
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}

}
