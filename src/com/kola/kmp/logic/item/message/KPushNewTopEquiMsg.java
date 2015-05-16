package com.kola.kmp.logic.item.message;

import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.item.KItem;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.item.KItemProtocol;

/**
 * <pre>
 * 将最新的顶级装备通知客户端快捷穿戴
 * 
 * @author CamusHuang
 * @creation 2013-4-2 下午5:04:33
 * </pre>
 */
public class KPushNewTopEquiMsg implements KItemProtocol {


	public static void sendMsg(long roleId, KItem item) {
		if (item == null) {
			return;
		}
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_NEW_TOP_EQUI);
		msg.writeByte(1);
		msg.writeLong(item.getId());
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}

	public static void sendMsg(long roleId, List<KItem> items) {
		if (items == null || items.isEmpty()) {
			return;
		}
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_NEW_TOP_EQUI);
		msg.writeByte(items.size());
		for (KItem item : items) {
			msg.writeLong(item.getId());
		}
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}
}
