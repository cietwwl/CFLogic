package com.kola.kmp.logic.vip.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.vip.KRoleVIP;
import com.kola.kmp.logic.vip.KVIPMsgPackCenter;
import com.kola.kmp.logic.vip.KVIPRoleExtCACreator;
import com.kola.kmp.protocol.vip.KVIPProtocol;

public class KSyncVipDataMsg implements KVIPProtocol {
	/**
	 * <pre>
	 * VIP--角色登陆时或VIP数据变更时，应该通知本方法发送消息
	 * 
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2013-4-7 下午6:31:20
	 * </pre>
	 */
	public static void sendMsg(KRole role, KRoleVIP vip) {
		if (vip == null) {
			vip = KVIPRoleExtCACreator.getRoleVIP(role.getId());
		}
		KGameMessage msg = KGame.newLogicMessage(SM_VIP_SYNC_VIPDATA);
		KVIPMsgPackCenter.packVipData(msg, vip);
		role.sendMsg(msg);
	}

	/**
	 * <pre>
	 * 
	 * 
	 * @param roleId
	 * @param vip
	 * @author CamusHuang
	 * @creation 2014-4-18 下午8:41:24
	 * </pre>
	 */
	public static void sendMsg(long roleId, KRoleVIP vip) {
		if (vip == null) {
			vip = KVIPRoleExtCACreator.getRoleVIP(roleId);
		}
		KGameMessage msg = KGame.newLogicMessage(SM_VIP_SYNC_VIPDATA);
		KVIPMsgPackCenter.packVipData(msg, vip);
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}
}
