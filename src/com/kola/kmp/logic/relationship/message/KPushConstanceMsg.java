package com.kola.kmp.logic.relationship.message;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.other.KRelationShipTypeEnum;
import com.kola.kmp.logic.relationship.KRelationShip;
import com.kola.kmp.logic.relationship.KRelationShipConfig;
import com.kola.kmp.logic.relationship.KRelationShipLogic;
import com.kola.kmp.logic.relationship.KRelationShipModuleExtension;
import com.kola.kmp.logic.relationship.KRelationShipSet;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;
import com.kola.kmp.logic.util.ResultStructs.RSResult_AddFriend.RSSynStruct;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;
import com.kola.kmp.protocol.relationship.KRelationShipProtocol;

public class KPushConstanceMsg implements KRelationShipProtocol {

	/**
	 * <pre>
	 * 推送关系数量上限
	 * 
	 * @param role
	 * @author CamusHuang
	 * @creation 2014-3-13 上午11:11:21
	 * </pre>
	 */
	public static void pushMsg(long roleId) {
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_CONSTANCE);
		//
		msg.writeInt(KRelationShipLogic.getMaxFriendCount(roleId));
		msg.writeInt(KRelationShipTypeEnum.黑名单.getMaxNum());
		//
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}
}
