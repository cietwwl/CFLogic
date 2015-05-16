package com.kola.kmp.logic.gang.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
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
public class KSyncContributionChangeCountMsg implements KGangProtocol {

	/**
	 * <pre>
	 * 全体成员接收
	 * 
	 * @param gang
	 * @author CamusHuang
	 * @creation 2013-1-25 下午6:17:56
	 * </pre>
	 */
	public static void sendMsg(KGang gang, int change) {
		if (change < 0) {
			// 过滤，只将增量变化通知客户端，忽略减量变化
			return;
		}
		KGameMessage msg = KGame.newLogicMessage(SM_GANG_SYN_CONTRIBUTION_CHANGE_COUNT);
		msg.writeShort(change);
		KGangMsgPackCenter.sendMsgToMemebers(msg, gang);
	}

	public static void sendMsg(long roleId, int change) {
		if (change < 0) {
			// 过滤，只将增量变化通知客户端，忽略减量变化
			return;
		}
		KGameMessage msg = KGame.newLogicMessage(SM_GANG_SYN_CONTRIBUTION_CHANGE_COUNT);
		msg.writeShort(change);
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}
}
