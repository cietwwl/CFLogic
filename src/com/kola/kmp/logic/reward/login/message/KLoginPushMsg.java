package com.kola.kmp.logic.reward.login.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.reward.login.KLoginCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.protocol.reward.KRewardProtocol;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-12-12 上午10:11:40
 * </pre>
 */
public class KLoginPushMsg implements KRewardProtocol {

	/**
	 * <pre>
	 * 推送签到奖励数据
	 * 
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2014-4-25 上午10:26:37
	 * </pre>
	 */
	public static void sendCheckUpDataMsg(KRole role) {
		if (role == null) {
			return;
		}
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_LOGINREWARD);
		KLoginCenter.packCheckUpRewards(role, msg);
		role.sendMsg(msg);
	}
	
	public static void syncCheckUpDataMsg(KRole role) {
		if (role == null) {
			return;
		}
		KGameMessage msg = KGame.newLogicMessage(SM_SYNC_LOGINREWARD);
		KLoginCenter.packCheckUpRewardStates(role, msg);
		role.sendMsg(msg);
	}

	/**
	 * <pre>
	 * 推送七天奖励数据
	 * 
	 * @param role
	 * @author CamusHuang
	 * @creation 2014-11-16 下午10:30:26
	 * </pre>
	 */
	public static void sendSevenDataMsg(KRole role) {
		if (role == null) {
			return;
		}
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_SEVENREWARD);
		KLoginCenter.packSevenRewards(role, msg);
		role.sendMsg(msg);
	}
}
