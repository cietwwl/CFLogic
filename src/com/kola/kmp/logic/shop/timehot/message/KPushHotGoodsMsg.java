package com.kola.kmp.logic.shop.timehot.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.shop.KShopRoleExtCACreator;
import com.kola.kmp.logic.shop.timehot.KHotShopCenter;
import com.kola.kmp.logic.shop.timehot.KHotShopMsgPackCenter;
import com.kola.kmp.logic.shop.timehot.TimeHotShopData;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;
import com.kola.kmp.protocol.shop.KShopProtocol;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-11-10 下午5:35:47
 * </pre>
 */
public class KPushHotGoodsMsg implements KShopProtocol {
	public static KPushHotGoodsMsg instance = new KPushHotGoodsMsg();

	public void pushMsg(KRole role) {
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_HOTGOODS);
		KHotShopMsgPackCenter.instance.packAllGoods(msg, role.getId());
		role.sendMsg(msg);
	}

	public void pushMsg(long roleId) {
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_HOTGOODS);
		KHotShopMsgPackCenter.instance.packAllGoods(msg, roleId);
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}

	public void pushToAllOnlineRole() {
		RoleModuleSupport support = KSupportFactory.getRoleModuleSupport();
		for (long roleId : support.getAllOnLineRoleIds()) {
			try {
				KGameMessage msg = KGame.newLogicMessage(SM_PUSH_HOTGOODS);
				KHotShopMsgPackCenter.instance.packAllGoods(msg, roleId);
				support.sendMsg(roleId, msg);
			} catch (Exception e) {
				KHotShopCenter._LOGGER.error(e.getMessage(), e);
			}
		}
	}
}
