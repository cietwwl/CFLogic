package com.kola.kmp.logic.reward.vitality.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.reward.vitality.KVitalityCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.VatalityRewardResult;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.reward.KRewardProtocol;

public class KVitalityGetRewardMsg implements GameMessageProcesser, KRewardProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KVitalityGetRewardMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_VITALITY_GET_REWARD;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		int rewardScore = msg.readInt();
		// -------------
		VatalityRewardResult result = null;
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			result = new VatalityRewardResult();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
		} else {
			result = KVitalityCenter.dealMsg_getReward(role, rewardScore);
		}
		// -------------
		KGameMessage backmsg = KGame.newLogicMessage(SM_VITALITY_GET_REWARD_RESULT);
		backmsg.writeInt(rewardScore);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		session.send(backmsg);
		//
		result.doFinally(role);
		
		if(result.isSucess){
			KSupportFactory.getExcitingRewardSupport().notifyVitalityTaskLvRewardCollected(role.getId(), result.rewardLv);
		}
	}
}
