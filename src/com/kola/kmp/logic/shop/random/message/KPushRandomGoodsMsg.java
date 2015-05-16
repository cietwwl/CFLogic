package com.kola.kmp.logic.shop.random.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.shop.random.KRandomShopMsgPackCenter;
import com.kola.kmp.logic.shop.random.KRandomShopTypeEnum;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.shop.KShopProtocol;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-11-22 下午4:49:56
 * </pre>
 */
public class KPushRandomGoodsMsg implements KShopProtocol {

	public static void pushMsg(KRole role, KRandomShopTypeEnum nowGoodsType) {
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_RANDOMGOODS);
		if(nowGoodsType==null){
			KRandomShopMsgPackCenter.packAllRandomGoods(msg, role.getId());
		} else {
			KRandomShopMsgPackCenter.packRandomGoods(msg, role.getId(), nowGoodsType);
		}
		role.sendMsg(msg);
	}
	
	public static void pushMsg(long roleId, KRandomShopTypeEnum nowGoodsType) {
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_RANDOMGOODS);
		if(nowGoodsType==null){
			KRandomShopMsgPackCenter.packAllRandomGoods(msg, roleId);
		} else {
			KRandomShopMsgPackCenter.packRandomGoods(msg, roleId, nowGoodsType);
		}
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}
}
