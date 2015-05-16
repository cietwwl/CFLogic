package com.kola.kmp.logic.gang.war.message;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.gang.KGangLogic;
import com.kola.kmp.logic.gang.war.KGangWarMsgPackCenter;
import com.kola.kmp.logic.gang.war.KGangWarDataManager.KGangMedalDataManager.GangMedalData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.gang.war.KGangWarProtocol;

public class KGWPushMsg implements KGangWarProtocol {

	public static void pushConstance(KGamePlayerSession session, KRole role) {
		KGameMessage msg = KGame.newLogicMessage(SM_GW_PUSH_CONSTANCE);
		KGangWarMsgPackCenter.packConstance(msg, role);
		session.send(msg);
	}

	public static void syncConstance() {
		for (long roleId : KSupportFactory.getRoleModuleSupport().getAllOnLineRoleIds()) {
			KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (role != null) {
				KGameMessage msg = KGame.newLogicMessage(SM_GW_PUSH_CONSTANCE);
				KGangWarMsgPackCenter.packConstance(msg, role);
				role.sendMsg(msg);
			}
		}
	}

	public static void syncMedal(long roleId, GangMedalData medal, boolean hasShowedDialog) {
		KGameMessage msg = KGame.newLogicMessage(SM_GW_SYNC_MEDAL);
		msg.writeByte(medal==null?0:medal.rank);
		msg.writeInt(medal==null?0:medal.icon);
		msg.writeBoolean(hasShowedDialog);
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}

	/**
	 * <pre>
	 * 将军团战状态发送给在线的已加入军团的角色
	 * 
	 * 
	 * @author CamusHuang
	 * @creation 2014-5-23 下午4:44:10
	 * </pre>
	 */
	public static void syncGangWarState() {

		Map<Long, List<Long>> map = KGangLogic.searchOnlineRoleOfGang();
		for (Entry<Long, List<Long>> e : map.entrySet()) {
			long gangId = e.getKey();
			List<Long> roleIds = e.getValue();

			KGameMessage msg = KGame.newLogicMessage(SM_GW_SYN_WAR_STATE);
			KGangWarMsgPackCenter.packGangWarStatus(msg, gangId);

			KGameMessage dupMsg = msg.duplicate();
			for (long roleId : roleIds) {
				if (KSupportFactory.getRoleModuleSupport().sendMsg(roleId, dupMsg)) {
					dupMsg = msg.duplicate();
				}
			}
		}
	}

	/**
	 * <pre>
	 * 军团战主界面ICON入口（每轮过程中上线时推送；只针对本轮参战所有军团成员）
	 * 
	 * @param isOpen
	 * @author CamusHuang
	 * @creation 2014-11-26 上午11:37:47
	 * </pre>
	 */
	public static void syncGangWarIcon(KRole role, boolean isOpen) {
		
		KGameMessage msg = KGame.newLogicMessage(SM_GW_SYN_WAR_ICON);
		msg.writeBoolean(isOpen);

		role.sendMsg(msg);
	}

	/**
	 * <pre>
	 * 军团战主界面ICON入口（每轮准备时，每轮结束时；只针对本轮参战所有军团成员）
	 * 
	 * @param isOpen
	 * @author CamusHuang
	 * @creation 2014-11-26 上午11:37:47
	 * </pre>
	 */
	public static void syncGangWarIcon(boolean isOpen) {

		KGameMessage msg = KGame.newLogicMessage(SM_GW_SYN_WAR_ICON);
		msg.writeBoolean(isOpen);

		KGangWarMsgPackCenter.sendMsgToRoleOfRound(msg);
	}
}
