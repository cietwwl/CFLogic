package com.kola.kmp.logic.relationship.message;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.relationship.KRelationShipLogic;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.skill.message.KSkillUpdateSlotsMsg;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.RSResult_AddFriend;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.relationship.KRelationShipProtocol;

public class KAppFriendMsg implements GameMessageProcesser, KRelationShipProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KAppFriendMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_APP_FRIEND;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		KRole oppRole = null;
		if (msg.readBoolean()) {
			long roleId = msg.readLong();
			oppRole = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		} else {
			String roleName = msg.readUtf8String();
			oppRole = KSupportFactory.getRoleModuleSupport().getRole(roleName);
		}
		// -------------
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			KDialogService.sendUprisingDialog(session, GlobalTips.服务器繁忙请稍候再试);
			return;
		}

		if (oppRole == null) {
			KDialogService.sendUprisingDialog(session, GlobalTips.角色不存在);
			return;
		}

		RSResult_AddFriend result = KRelationShipLogic.dealMsg_appFriend(role, oppRole);
		// -------------
		KDialogService.sendUprisingDialog(session, result.tips);
		KPushRSsMsg.synRelationShips(result.rsSynDatas);
	}
}