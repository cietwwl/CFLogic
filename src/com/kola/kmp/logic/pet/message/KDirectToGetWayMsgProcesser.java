package com.kola.kmp.logic.pet.message;

import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.npc.message.KNPCOrderMsg;
import com.kola.kmp.logic.other.KFunctionTypeEnum;
import com.kola.kmp.logic.other.KNPCOrderEnum;
import com.kola.kmp.logic.other.KPetGetWay;
import com.kola.kmp.logic.pet.KPetTemplate;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.GangTips;
import com.kola.kmp.logic.util.tips.PetTips;
import com.kola.kmp.protocol.pet.KPetProtocol;

public class KDirectToGetWayMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KDirectToGetWayMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return KPetProtocol.CM_GOTO_GET_PET;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(msgEvent.getPlayerSession());
		int templateId = msgEvent.getMessage().readInt();
		int way = msgEvent.getMessage().readByte();
		KPetGetWay wayType = KPetGetWay.getEnum(way);
		String tips = null;
		if(wayType != null) {
			KFunctionTypeEnum func = KFunctionTypeEnum.getEnum((short)wayType.npcOrderEnum.sign);
			boolean isOpen = true;
			if(func != null) {
				isOpen = KSupportFactory.getMissionSupport().checkFunctionIsOpen(role, func);
			}
			if (isOpen) {
				KPetTemplate template = KSupportFactory.getPetModuleSupport().getPetTemplate(templateId);
				if (template != null) {
					String script = template.getWayMap.get(wayType);
					switch (wayType.npcOrderEnum) {
					case ORDER_OPEN_GANG_SHOP:
						if (KSupportFactory.getGangSupport().getGangByRoleId(role.getId()) == null) {
							KDialogService.sendUprisingDialog(msgEvent.getPlayerSession(), GangTips.对不起您不属于任何军团);
							return;
						}
						break;
					case ORDER_OPEN_SCENARIO_UI:
						KSupportFactory.getLevelSupport().notifyClientLevelSearchRoad(role, Integer.parseInt(script));
						KDialogService.sendNullDialog(msgEvent.getPlayerSession());
						return;
					default:
						break;
					}
					KNPCOrderMsg.sendNPCMenuOrder(msgEvent.getPlayerSession(), wayType.npcOrderEnum, script == null ? "" : script);
				} else {
					tips = PetTips.getTipsNoSuchPetTemplate();
				}
			} else {
				tips = PetTips.getTipsFuncNotOpen();
			}
		} else {
			tips = PetTips.getTipsNoSuchGetWay();
		}
		if (tips == null) {
			KDialogService.sendNullDialog(msgEvent.getPlayerSession());
		} else {
			KDialogService.sendUprisingDialog(msgEvent.getPlayerSession(), tips);
		}
	}

}
