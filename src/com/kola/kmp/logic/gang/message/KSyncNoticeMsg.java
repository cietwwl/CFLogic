package com.kola.kmp.logic.gang.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.gang.KGangMsgPackCenter;
import com.kola.kmp.protocol.gang.KGangProtocol;

public class KSyncNoticeMsg implements KGangProtocol {

	/**
	 * <pre>
	 * 服务器主动推送最新公告，消息体内容如下：
	 * 军团--修改时主动推送公告
	 * 
	 * String 公告内容
	 * </pre>
	 */
	public static void sendMsg(KGang gang,String newNotice) {
		KGameMessage msg = KGame.newLogicMessage(SM_GANG_SYNC_NOTICE);
		msg.writeUtf8String(newNotice);
		KGangMsgPackCenter.sendMsgToMemebers(msg, gang);
	}
}
