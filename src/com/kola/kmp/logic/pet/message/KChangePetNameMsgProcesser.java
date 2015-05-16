package com.kola.kmp.logic.pet.message;

import static com.kola.kmp.protocol.pet.KPetProtocol.CM_REQUEST_CHANGE_NAME;
import static com.kola.kmp.protocol.pet.KPetProtocol.SM_NOTIFY_CHANGE_NAME_SUCCESS;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.pet.KPet;
import com.kola.kmp.logic.pet.KPetModuleConfig;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.PetTips;

/**
 * 
 * @author PERRY CHAN
 */
public class KChangePetNameMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KChangePetNameMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_REQUEST_CHANGE_NAME;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(msgEvent.getPlayerSession());
		long petId = msgEvent.getMessage().readLong();
		String name = msgEvent.getMessage().readUtf8String();
		String result = null;
		boolean success = false;
		KPet pet = null;
		for(int i = 0; i < name.length(); i++) {
			if(String.valueOf(name.charAt(i)).getBytes().length > 3) {
				result = PetTips.getTipsNameFormatNotIllegal();
				break;
			}
		}
		if (result == null) {
			int length = 0;
			if ((length = UtilTool.getStringLength(name)) < KPetModuleConfig.getPetNameLengthMin() || length > KPetModuleConfig.getPetNameLengthMax()) {
				result = PetTips.getTipsPetNameLengthNotPass();
			} else if (KSupportFactory.getDirtyWordSupport().containDirtyWord(name) != null) {
				result = PetTips.getTipsPetNameContainsDirtyWord();
			} else {
				pet = KSupportFactory.getPetModuleSupport().getPet(role.getId(), petId);
				if (pet == null) {
					result = PetTips.getTipsNoSuchPet();
				} else if (pet.isChgNameCoolDownFinished()) {
					pet.modifyPetName(name);
					success = true;
					result = PetTips.getTipsModifyNameSuccess();
				} else {
					result = PetTips.getTipsPetChgNameIsCoolingDown();
				}
			}
		}
		KGameMessage msg = KGame.newLogicMessage(SM_NOTIFY_CHANGE_NAME_SUCCESS);
		msg.writeBoolean(success);
		msg.writeUtf8String(result);
		if(success) {
			msg.writeLong(petId);
			msg.writeUtf8String(name);
		}
		msgEvent.getPlayerSession().send(msg);
		if(success && pet.isFighting()) {
			KSupportFactory.getRankModuleSupport().notifyPetInfoChange(role, pet.getName(), pet.getLevel(), pet.getAttributeByType(KGameAttrType.BATTLE_POWER));
		}
	}

}
