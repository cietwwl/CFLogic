package com.kola.kmp.logic.relationship.message;

import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.relationship.KRelationShipLogic;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.skill.message.KSkillUpdateSlotsMsg;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.relationship.KRelationShipProtocol;

public class KGetAroundListMsg implements GameMessageProcesser, KRelationShipProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KGetAroundListMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GET_AROUND_LIST;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			KDialogService.sendUprisingDialog(session, GlobalTips.服务器繁忙请稍候再试);
			return;
		}

		List<Long> result = KRelationShipLogic.dealMsg_getAroundList(role);
		// -------------
		RoleModuleSupport roleSupport = KSupportFactory.getRoleModuleSupport();
		KGameMessage backmsg = KGame.newLogicMessage(SM_GET_AROUND_LIST_RESULT);
		int writeIndex = backmsg.writerIndex();
		backmsg.writeShort(result.size());
		int count = 0;
		for (Long roleId : result) {
			KRole oppRole = roleSupport.getRole(roleId);
			if (oppRole != null) {
				KPushRSsMsg.packRelationShip(backmsg, oppRole);
				count++;
			}
		}
		backmsg.setShort(writeIndex, count);
		session.send(backmsg);
	}
}
