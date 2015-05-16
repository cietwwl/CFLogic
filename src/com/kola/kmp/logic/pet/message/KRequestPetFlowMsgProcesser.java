package com.kola.kmp.logic.pet.message;

import static com.kola.kmp.protocol.pet.KPetProtocol.CM_REQUEST_PET_FLOW;
import static com.kola.kmp.protocol.pet.KPetProtocol.SM_NOTIFY_PET_FLOW_SUCCESS;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.pet.Pet;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.pet.KPet;
import com.kola.kmp.logic.pet.KPetModuleConfig;
import com.kola.kmp.logic.pet.KPetSet;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.PetTips;

/**
 * 
 * @author PERRY CHAN
 */
public class KRequestPetFlowMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KRequestPetFlowMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_REQUEST_PET_FLOW;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(msgEvent.getPlayerSession());
		KPet targetPet = null;
		if (role != null) {
			String result = null;
			long lastFightingPetId = 0;
			long petId = msgEvent.getMessage().readLong();
			KPetSet petSet = KSupportFactory.getPetModuleSupport().getPetSet(role.getId());
			Pet pet = petSet.getPet(petId);
			if (pet != null) {
				targetPet = (KPet) pet;
				int subLv = targetPet.getLevel() - role.getLevel();
				if (subLv > KPetModuleConfig.getPetMaxLvGreatThanRole()) {
					result = PetTips.getTipsPetIsStrongerThanRole();
				} else if (!targetPet.isFighting()) {
					KPet currentPet = petSet.getFightingPet();
					if (currentPet != null) {
						lastFightingPetId = currentPet.getId();
						currentPet.setFightingStatus(false);
					}
					targetPet.setFightingStatus(true);
					petSet.updateFightingPet(targetPet.getId());
					
				}
			} else {
				result = PetTips.getTipsNoSuchPet();
			}
			if (result != null) {
				KDialogService.sendDataUprisingDialog(msgEvent.getPlayerSession(), result);
			} else {
				KGameMessage msg = KGame.newLogicMessage(SM_NOTIFY_PET_FLOW_SUCCESS);
				msg.writeLong(petId);
				msg.writeLong(lastFightingPetId);
				msgEvent.getPlayerSession().send(msg);
				KDialogService.sendUprisingDialog(msgEvent.getPlayerSession(), PetTips.getTipsPetFlowSuccess());
				KSupportFactory.getRankModuleSupport().notifyPetInfoChange(role, targetPet.getName(), targetPet.getLevel(), targetPet.getAttributeByType(KGameAttrType.BATTLE_POWER));
			}
		}
	}

}
