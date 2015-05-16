package com.kola.kmp.logic.reward.exciting.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.reward.exciting.ExcitingMsgPackCenter;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.ExcitingDataManager.ExcitionActivity;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;
import com.kola.kmp.protocol.reward.KRewardProtocol;

public class KSynDataMsg implements KRewardProtocol {
	/**
	 * <pre>
	 * 通知所有在线玩家活动关闭
	 * 
	 * @param activityId
	 * @author CamusHuang
	 * @creation 2013-7-6 下午3:43:56
	 * </pre>
	 */
	public static void sendMsgForDelete(int activityId) {
		KGameMessage msg = KGame.newLogicMessage(SM_EXCITING_SYNC_ACTIVITY_DELETE);
		msg.writeInt(activityId);
		UtilTool.sendMsgToAllOnlineSession(msg);
	}

	/**
	 * <pre>
	 * 通知所有在线玩家有新活动
	 * 
	 * @param activityId
	 * @author CamusHuang
	 * @creation 2013-7-6 下午3:57:18
	 * </pre>
	 */
	public static void sendMsgForAdd(ExcitionActivity activity) {
		// 有效期内
		RoleModuleSupport roleSupport = KSupportFactory.getRoleModuleSupport();
		for (Long roleId : roleSupport.getAllOnLineRoleIds()) {
			KGameMessage msg = KGame.newLogicMessage(SM_EXCITING_SYNC_ACTIVITY_ADD);
			msg = ExcitingMsgPackCenter.packExcitingActivity(msg, roleId, activity);
			if (msg != null) {
				roleSupport.sendMsg(roleId, msg);
			}
		}
	}

	/**
	 * <pre>
	 * 更新精彩活动状态
	 * 
	 * @param activityId
	 * @author CamusHuang
	 * @creation 2013-7-6 下午4:03:28
	 * </pre>
	 */
	public static void sendMsgForStatus(long roleId) {
		KGameMessage msg = KGame.newLogicMessage(SM_EXCITING_SYNC_ACTIVITY_REFRESH);
		msg = ExcitingMsgPackCenter.packExcitingActivityStatus(msg, roleId);
		if (msg != null) {
			KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
		}
	}
}
