package com.kola.kmp.logic.gang.reswar.message;

import java.util.Set;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.gang.KGangMsgPackCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.protocol.gang.reswar.KGangResWarProtocol;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-5-12 下午4:04:46
 * </pre>
 */
public class KGrwSynMsg implements KGangResWarProtocol {

	public static void sendCityListStatusChangeMsg(Set<Long> gangIds) {
		KGameMessage msg = KGame.newLogicMessage(SM_GANGRW_SYN_CITYLIST_STATUS_CHANGE);
		// 向指定军团在线成员发送
		KGangMsgPackCenter.sendMsgToMembers(msg, gangIds);
	}

	/**
	 * <pre>
	 * 通知PVP战斗结果
	 * 
	 * @param role
	 * @param isWin
	 * @author CamusHuang
	 * @creation 2014-5-14 上午9:59:49
	 * </pre>
	 */
	public static void sendBattleResult(KRole role, boolean isWin) {
		KGameMessage msg = KGame.newLogicMessage(SM_GANGRW_SHOW_PVP_RESULT);
		msg.writeBoolean(isWin);
		role.sendMsg(msg);
	}

}
