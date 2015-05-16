package com.kola.kmp.logic.reward.activatecode.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayer;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.reward.activatecode.KActivateCodeCenter;
import com.kola.kmp.logic.reward.daylucky.KDayluckyCenter;
import com.kola.kmp.logic.reward.daylucky.message.KDayluckyGetNumMsg;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.ResultStructs.CommonResult_Ext;
import com.kola.kmp.logic.util.ResultStructs.RewardResult_GoodLuck;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.RewardTips;
import com.kola.kmp.protocol.reward.KRewardProtocol;

public class KGetActivationRewardMsg implements GameMessageProcesser, KRewardProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KGetActivationRewardMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GET_ACTIVATION_REWARD;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		String activationCode = msg.readUtf8String();
		// -------------
		CommonResult_Ext result = null;
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			result = new CommonResult_Ext();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, result);
			return;
		}
		try {
			long code = Long.parseLong(activationCode);
			result = KActivateCodeCenter.collectActivationReward(session, role, activationCode);

			doFinally(session, role, result);
		} catch (Exception e) {
			result = new CommonResult_Ext();
			result.tips = RewardTips.此激活码不存在;
			doFinally(session, role, result);
		}
	}

	private void doFinally(KGamePlayerSession session, KRole role, CommonResult_Ext result) {
		KGameMessage backmsg = KGame.newLogicMessage(SM_GET_ACTIVATION_REWARD);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		session.send(backmsg);

		result.doFinally(role);
	}

}
