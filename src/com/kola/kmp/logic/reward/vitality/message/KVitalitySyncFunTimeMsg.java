package com.kola.kmp.logic.reward.vitality.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.reward.KRewardProtocol;

public class KVitalitySyncFunTimeMsg implements KRewardProtocol {

	public static void sendMsg(long roleId, KVitalityTypeEnum type, int time, int totalVitalityValue) {
		KGameMessage msg = KGame.newLogicMessage(SM_VITALITY_SYNC_FUNTIME);
		msg.writeByte(type.sign);
		msg.writeShort(time);
		msg.writeInt(totalVitalityValue);
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}
}
