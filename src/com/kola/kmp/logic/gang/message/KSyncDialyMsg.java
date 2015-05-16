package com.kola.kmp.logic.gang.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.gang.KGangExtCASet.GangDialyCache.GangDialy;
import com.kola.kmp.logic.gang.KGangMsgPackCenter;
import com.kola.kmp.protocol.gang.KGangProtocol;

public class KSyncDialyMsg implements KGangProtocol {

	/**
	 * <pre>
	 * 军团--服务器主动推送最新日志
	 * 
	 * @param gang
	 * @param dialy
	 * @author CamusHuang
	 * @creation 2013-1-25 下午8:01:59
	 * </pre>
	 */
	public static void sendMsg(KGang gang, GangDialy dialy) {
		KGameMessage msg = KGame.newLogicMessage(SM_GANG_SYNC_DIALY);
		KGangMsgPackCenter.packDialySync(msg, gang, dialy);
		KGangMsgPackCenter.sendMsgToMemebers(msg, gang);
	}

	/**
	 * <pre>
	 * 军团--服务器主动推送最新日志
	 * 
	 * @param gang
	 * @param dialy
	 * @param exceptRoleId 例外的角色ID
	 * @author CamusHuang
	 * @creation 2013-1-25 下午8:01:59
	 * </pre>
	 */
	public static void sendMsg(KGang gang, GangDialy dialy, long exceptRoleId) {
		KGameMessage msg = KGame.newLogicMessage(SM_GANG_SYNC_DIALY);
		KGangMsgPackCenter.packDialySync(msg, gang, dialy);
		KGangMsgPackCenter.sendMsgToMemebers(msg, gang, exceptRoleId);
	}

}
