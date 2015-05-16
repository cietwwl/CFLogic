package com.kola.kmp.logic.reward.exciting.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.reward.exciting.KExcitingCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.RewardResult_SendMail;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.RewardTips;
import com.kola.kmp.protocol.reward.KRewardProtocol;

public class KGetExcitingRewardMsg implements GameMessageProcesser, KRewardProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KGetExcitingRewardMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_EXCITING_GET_REWARD;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		int activityId = msg.readInt();
		int ruleId = msg.readInt();
		// -------------
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			RewardResult_SendMail result = new RewardResult_SendMail();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, result);
			return;
		}

		RewardResult_SendMail result = KExcitingCenter.dealMsg_getExcitingReward(role, activityId, ruleId);

		doFinally(session, role, result);
	}

	private void doFinally(KGamePlayerSession session, KRole role, RewardResult_SendMail result) {
		KGameMessage backmsg = KGame.newLogicMessage(SM_EXCITING_GET_REWARD_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		session.send(backmsg);
		//
		result.doFinally(role);
		
		if(result.isSendByMail){
			KDialogService.sendUprisingDialog(session, UtilTool.getNotNullString(null), RewardTips.背包已满奖励通过邮件发送);
		}

		if (result.isSucess) {
			KSynDataMsg.sendMsgForStatus(role.getId());
		}
	}
}
