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

public class KRefuseFriendMsg  implements GameMessageProcesser, KRelationShipProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KRefuseFriendMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_REFUSE_FRIEND;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		boolean isAll = msg.readBoolean();
		long oppRoleId = 0;
		if (!isAll) {
			oppRoleId = msg.readLong();
		}
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			KDialogService.sendUprisingDialog(session, GlobalTips.服务器繁忙请稍候再试);
			return;
		} 
		
		RSResult_AddFriend result = null;
		if (isAll) {
			result = KRelationShipLogic.dealMsg_refuseForFriends(role);
		} else {
			result = KRelationShipLogic.dealMsg_refuseForFriend(role, oppRoleId);
		}
		// -------------
		KDialogService.sendUprisingDialog(session, result.tips);
		KPushRSsMsg.synRelationShips(result.rsSynDatas);
	}
}