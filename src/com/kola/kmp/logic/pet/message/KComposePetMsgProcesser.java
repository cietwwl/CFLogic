package com.kola.kmp.logic.pet.message;

import static com.kola.kmp.protocol.pet.KPetProtocol.CM_REQUEST_SWALLOW_PETS;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 
 * @author PERRY CHAN
 */
public class KComposePetMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KComposePetMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_REQUEST_SWALLOW_PETS;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		long swallowerId = msgEvent.getMessage().readLong();
		int count = msgEvent.getMessage().readByte();
		List<Long> beComposedIds = new ArrayList<Long>();
		long tempId;
		for (int i = 0; i < count; i++) {
			tempId = msgEvent.getMessage().readLong();
			if (tempId != swallowerId) {
				beComposedIds.add(tempId);
			}
		}
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(msgEvent.getPlayerSession());
		KSupportFactory.getPetModuleSupport().processComposePet(role, swallowerId, beComposedIds, false, false);
	}

}
