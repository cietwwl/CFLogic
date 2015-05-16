package com.kola.kmp.logic.relationship.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.relationship.IntercourseCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.relationship.KRelationShipProtocol;

public class KIntercourseMsg implements GameMessageProcesser, KRelationShipProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KIntercourseMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_INTERCOURSE;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		boolean isId = msg.readBoolean();

		KRole oppRole = null;
		if (isId) {
			long oppRoleId = msg.readLong();
			oppRole = KSupportFactory.getRoleModuleSupport().getRole(oppRoleId);
		} else {
			String oppRoleName = msg.readUtf8String();
			oppRole = KSupportFactory.getRoleModuleSupport().getRole(oppRoleName);
		}

		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			KDialogService.sendUprisingDialog(session, GlobalTips.服务器繁忙请稍候再试);
			return;
		}

		if (oppRole == null) {
			KDialogService.sendUprisingDialog(role, GlobalTips.角色不存在);
			return;
		}

		// -------------
		CommonResult result = IntercourseCenter.dealMsg_startPVP(role, oppRole);
		KDialogService.sendUprisingDialog(role, result.tips);
		
		if(result.isSucess){
			KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.切磋);
		}
		return;
	}

	public static void pushPVPResult(KRole role, boolean isWin, String tips, int totalScore) {
		KGameMessage msg = KGame.newLogicMessage(KRelationShipProtocol.SM_PUSH_INTERCOURSE_RESULT);
		msg.writeBoolean(isWin);
		msg.writeUtf8String(tips);
		msg.writeInt(totalScore);
		role.sendMsg(msg);
	}
}