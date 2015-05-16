package com.kola.kmp.logic.relationship.message;

import java.util.HashSet;
import java.util.Set;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.relationship.KRelationShipLogic;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.RSResult_AddFriend;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.relationship.KRelationShipProtocol;

public class KAppFriendsMsg implements GameMessageProcesser, KRelationShipProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KAppFriendsMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_APP_FRIENDS;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			KDialogService.sendUprisingDialog(session,  GlobalTips.服务器繁忙请稍候再试);
			return;
		}
		// -------------
		Set<KRole> oppRoles = new HashSet<KRole>();
		byte size = msg.readByte();
		size = (byte)Math.min((int)size, 20);//不能同时向超过20名玩家发起加友请求
		for (int i = 0; i < size; i++) {
			long roleId = msg.readLong();
			if (roleId == role.getId()) {
				continue;
			}
			KRole oppRole = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (oppRole == null) {
				continue;
			}
			oppRoles.add(oppRole);
		}
		// -------------
		RSResult_AddFriend result = KRelationShipLogic.dealMsg_appFriends(role, oppRoles);
		// -------------
		KDialogService.sendUprisingDialog(session, result.tips);
		KPushRSsMsg.synRelationShips(result.rsSynDatas);
	}
}