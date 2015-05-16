package com.kola.kmp.logic.reward.vitality.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.reward.vitality.KVitalityCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.reward.KRewardProtocol;

public class KVitalityPushFunDataMsg implements KRewardProtocol {

	public static void sendMsg(KRole role) {
		KGameMessage msg = KGame.newLogicMessage(SM_VITALITY_PUSH_FUNDATA);
		KVitalityCenter.packFunDatas(role.getId(), msg);
		role.sendMsg(msg);
	}
	
	public static void sendMsg(long roleId) {
		KGameMessage msg = KGame.newLogicMessage(SM_VITALITY_PUSH_FUNDATA);
		KVitalityCenter.packFunDatas(roleId, msg);
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}
}
