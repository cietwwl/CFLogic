package com.kola.kmp.logic.reward.daylucky.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.reward.daylucky.KDayluckyCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.reward.KRewardProtocol;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-12-12 上午10:11:40
 * </pre>
 */
public class KDayluckySyncMsg implements KRewardProtocol {
	/**
	 * <pre>
	 * 发送每日幸运数据给客户端
	 * 
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2013-1-12 下午3:59:45
	 * </pre>
	 */
	public static void sendDayluckRoleData(long roleId) {
		KGameMessage msg = KGame.newLogicMessage(SM_DAYLUCK_SYNCDATA);
		KDayluckyCenter.packDayluckyData(msg, roleId);
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}

	/**
	 * <pre>
	 * 发送每日幸运奖励数据
	 * 
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2014-4-25 上午10:26:37
	 * </pre>
	 */
	public static void sendDayluckRewardData(KRole role) {
		KGameMessage msg = KGame.newLogicMessage(SM_DAYLUCK_PUSHDATA);
		KDayluckyCenter.packDayluckyRewards(msg);
		role.sendMsg(msg);
	}
}
