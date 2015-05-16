package com.kola.kmp.logic.gang.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.gang.KGangProtocol;

public class KSyncDismissMsg implements KGangProtocol {

	/**
	 * <pre>
	 * 服务器主动推送，此消息表示你已被开除出军团
	 * 
	 * String 提示
	 * </pre>
	 */
	public static void sendMsg(long roleId, String dismissTips) {
		KGameMessage msg = KGame.newLogicMessage(SM_GANG_SYNC_DISMISS);
		msg.writeUtf8String(dismissTips);
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}
}
