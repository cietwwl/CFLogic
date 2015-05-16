package com.kola.kmp.logic.pet.message;

import static com.kola.kmp.protocol.pet.KPetProtocol.CM_REQUEST_CANCEL_PET_FLOW;
import static com.kola.kmp.protocol.pet.KPetProtocol.SM_CANCEL_PET_FLOW_RESULT;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.pet.Pet;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.pet.KPet;
import com.kola.kmp.logic.pet.KPetSet;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.PetTips;

/**
 * 
 * @author PERRY CHAN
 */
public class KCancelPetFlowMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KCancelPetFlowMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_REQUEST_CANCEL_PET_FLOW;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		long petId = msgEvent.getMessage().readLong();
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(msgEvent.getPlayerSession());
		String resultTips = null;
		boolean success = false;
		if(role != null) {
			KPetSet petSet = KSupportFactory.getPetModuleSupport().getPetSet(role.getId());
			if (petSet != null) {
				Pet pet = petSet.getPet(petId);
				if(pet != null) {
					KPet kpet = (KPet) pet;
					if (kpet.isFighting()) {
						kpet.setFightingStatus(false);
					}
					if (petSet.getFightingPetId() == kpet.getId()) {
						petSet.updateFightingPet(0);
					}
					success = true;
				} else {
					resultTips = PetTips.getTipsNoSuchPet();
				}
			} else {
				resultTips = GlobalTips.getTipsServerBusy();
			}
		} else {
			resultTips = GlobalTips.getTipsServerBusy();
		}
		if(success) {
			KGameMessage msg = KGame.newLogicMessage(SM_CANCEL_PET_FLOW_RESULT);
			msg.writeLong(petId);
			msgEvent.getPlayerSession().send(msg);
			KDialogService.sendUprisingDialog(msgEvent.getPlayerSession(), PetTips.getTipsPetCancelFlowSuccess());
		} else {
			KDialogService.sendUprisingDialog(msgEvent.getPlayerSession(), resultTips);
		}
	}

}
