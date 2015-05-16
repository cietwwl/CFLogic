package com.kola.kmp.logic.reward.vitality.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.reward.vitality.KVitalityCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.protocol.reward.KRewardProtocol;

public class KVitalityPushRewardDataMsg implements KRewardProtocol {

	public static void sendMsg(KRole role) {
		KGameMessage msg = KGame.newLogicMessage(SM_VITALITY_PUSH_REWARDDATA);
		KVitalityCenter.packRewardDatas(role, msg);
		role.sendMsg(msg);
	}
}
