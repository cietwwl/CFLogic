package com.kola.kmp.logic.gang.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.gang.KGangModuleExtension;
import com.kola.kmp.logic.gang.KGangMsgPackCenter;
import com.kola.kmp.protocol.gang.KGangProtocol;

/**
 * <pre>
 * 军团--服务器推送最新的军团数据，消息体内容如下：
 * 
 * @author CamusHuang
 * @creation 2013-1-30 上午11:37:00
 * </pre>
 */
public class KSyncGangDataMsg implements KGangProtocol {

	public static void sendMsg(long gangId) {
		KGang gang = KGangModuleExtension.getGang(gangId);
		if (gang == null) {
			return;
		}
		sendMsg(gang);
	}

	public static void sendMsg(KGang gang) {
		KGameMessage msg = KGame.newLogicMessage(SM_GANG_SYNC_GANGDATA);
		KGangMsgPackCenter.packGangUpdataData(msg, gang);
		KGangMsgPackCenter.sendMsgToMemebers(msg, gang);
	}
}
