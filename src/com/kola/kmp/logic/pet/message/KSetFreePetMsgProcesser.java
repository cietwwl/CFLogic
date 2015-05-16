package com.kola.kmp.logic.pet.message;

import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.pet.KPetProtocol;

public class KSetFreePetMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KSetFreePetMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return KPetProtocol.CM_REQUEST_PET_LET_GO;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		long petId = msgEvent.getMessage().readLong();
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(msgEvent.getPlayerSession());
		KSupportFactory.getPetModuleSupport().processSetFreePet(role, petId, false);
	}

}
