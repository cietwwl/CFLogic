package com.kola.kmp.logic.reward.exciting.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.reward.exciting.ExcitingMsgPackCenter;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;
import com.kola.kmp.protocol.reward.KRewardProtocol;

public class KPushExcitingActivityDataMsg implements KRewardProtocol {
	/**
	 * <pre>
	 * 
	 * @param session
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2013-7-6 下午3:43:56
	 * </pre>
	 */
	public static void sendMsg(KGamePlayerSession session, long roleId) {
		KGameMessage msg = KGame.newLogicMessage(SM_EXCITING_PUSH_ACTIVITY);
		msg = ExcitingMsgPackCenter.packExcitingActivity(msg, roleId);
		if(msg != null){
			session.send(msg);
		}
	}

	public static void sendMsg(long roleId) {
		KGameMessage msg = KGame.newLogicMessage(SM_EXCITING_PUSH_ACTIVITY);
		msg = ExcitingMsgPackCenter.packExcitingActivity(msg, roleId);
		if(msg != null){
			KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
		}
	}

	/**
	 * <pre>
	 * 
	 * @author CamusHuang
	 * @creation 2013-7-6 下午3:43:56
	 * </pre>
	 */
	public static void sendMsgToAllOnlineRole() {
		RoleModuleSupport roleSupport = KSupportFactory.getRoleModuleSupport();
		for (Long roleId : roleSupport.getAllOnLineRoleIds()) {
			KGameMessage msg = KGame.newLogicMessage(SM_EXCITING_PUSH_ACTIVITY);
			msg = ExcitingMsgPackCenter.packExcitingActivity(msg, roleId);
			if(msg != null){
				roleSupport.sendMsg(roleId, msg);
			}
		}
	}
}
