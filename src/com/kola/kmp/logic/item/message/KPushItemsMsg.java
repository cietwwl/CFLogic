package com.kola.kmp.logic.item.message;

import java.util.Collection;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.item.KItem;
import com.kola.kmp.logic.item.KItemMsgPackCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.item.KItemProtocol;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-11-22 下午4:49:56
 * </pre>
 */
public class KPushItemsMsg implements KItemProtocol {
	
	public static void pushItemQualitySetConstance(KRole role) {
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_QUALITY_SETS_CONSTANCE);
		KItemMsgPackCenter.packItemQualitySetConstance(msg);
		role.sendMsg(msg);
	}

	public static void pushAllItems(KRole role) {
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_ALLITEM_LIST);
		KItemMsgPackCenter.packAllItems(msg, role.getId(), role.getLevel());
		role.sendMsg(msg);
	}

	public static void pushItem(KRole role, KItem item) {
		if (item == null) {
			return;
		}
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_ITEM);
		msg.writeShort(1);
		KItemMsgPackCenter.packItemAndSlotId(msg, role.getId(), role.getLevel(), item);
		role.sendMsg(msg);
	}

	public static void pushItems(KRole role, Collection<KItem> items) {
		if (items == null || items.isEmpty()) {
			return;
		}
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_ITEM);
		msg.writeShort(items.size());
		for (KItem item : items) {
			KItemMsgPackCenter.packItemAndSlotId(msg, role.getId(), role.getLevel(), item);
		}
		role.sendMsg(msg);
	}

	public static void pushNull(long roleId) {
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_ITEM);
		msg.writeShort(0);
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}

	public static void pushItemCount(long roleId, long itemId, long count) {
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_ITEMCOUNT);
		msg.writeShort(1);
		msg.writeLong(itemId);
		msg.writeLong(count);
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}

	public static void pushItemCounts(long roleId, Collection<KItem> items) {
		if (items == null || items.isEmpty()) {
			return;
		}
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_ITEMCOUNT);
		msg.writeShort(items.size());
		for (KItem item : items) {
			msg.writeLong(item.getId());
			msg.writeLong(item.getCount());
		}

		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}

	public static void pushEquiSetData(KRole role) {
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_EQUISET_DATA);
		KItemMsgPackCenter.packEquiSetData(role, msg);
		role.sendMsg(msg);
	}
	
	public static void pushAllItemsPower(KRole role) {
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_ALLITEM_POWER);
		KItemMsgPackCenter.packAllItemsPower(msg, role.getId());
		role.sendMsg(msg);
	}
}
