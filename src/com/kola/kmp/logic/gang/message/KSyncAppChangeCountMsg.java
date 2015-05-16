package com.kola.kmp.logic.gang.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.gang.KGangMsgPackCenter;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.gang.KGangProtocol;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2013-1-30 上午11:38:16
 * </pre>
 */
public class KSyncAppChangeCountMsg implements KGangProtocol {

	/**
	 * <pre>
	 * 只发给非普通成员
	 * 
	 * @param gang
	 * @author CamusHuang
	 * @creation 2013-1-25 下午6:17:56
	 * </pre>
	 */
	public static void sendMsg(KGang gang, int change) {
		KGameMessage msg = KGame.newLogicMessage(SM_GANG_SYN_APP_CHANGE_COUNT);
		msg.writeShort(change);
		KGangMsgPackCenter.sendMsgToNotCommonMems(msg, gang);
	}

	/**
	 * <pre>
	 * 
	 * @param gang
	 * @author CamusHuang
	 * @creation 2013-1-25 下午6:17:56
	 * </pre>
	 */
	public static void sendMsg(KGamePlayerSession session, int change) {
		KGameMessage msg = KGame.newLogicMessage(SM_GANG_SYN_APP_CHANGE_COUNT);
		msg.writeShort(change);
		session.send(msg);
	}
	
	/**
	 * <pre>
	 * 
	 * @param gang
	 * @author CamusHuang
	 * @creation 2013-1-25 下午6:17:56
	 * </pre>
	 */
	public static void sendMsg(long roleId, int change) {
		KGameMessage msg = KGame.newLogicMessage(SM_GANG_SYN_APP_CHANGE_COUNT);
		msg.writeShort(change);
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}
}
