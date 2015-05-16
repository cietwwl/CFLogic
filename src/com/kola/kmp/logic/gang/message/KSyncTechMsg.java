package com.kola.kmp.logic.gang.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.gang.KGangExtCASet;
import com.kola.kmp.logic.gang.KGangMsgPackCenter;
import com.kola.kmp.protocol.gang.KGangProtocol;

public class KSyncTechMsg implements KGangProtocol {

	/**
	 * <pre>
	 * 军团--服务器主动推送军团科技更新
	 * 推送给推定成员
	 * 
	 * @param gang
	 * @param techId 要更新的科技项目
	 * @author CamusHuang
	 * @creation 2013-1-25 下午7:48:55
	 * </pre>
	 */
	public static void sendMsg(KGang gang, KGangExtCASet set, int techId) {
		KGameMessage msg = KGame.newLogicMessage(SM_GANG_SYNC_TECH);
		KGangMsgPackCenter.packTechForSyn(msg, set, techId);
		KGangMsgPackCenter.sendMsgToMemebers(msg, gang);
	}
}
