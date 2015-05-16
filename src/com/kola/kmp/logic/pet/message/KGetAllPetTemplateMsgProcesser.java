package com.kola.kmp.logic.pet.message;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.other.KPetGetWay;
import com.kola.kmp.logic.other.KPetQuality;
import com.kola.kmp.logic.pet.ITransferable;
import com.kola.kmp.logic.pet.KPetModuleManager;
import com.kola.kmp.logic.pet.KPetTemplateHandbookModel;
import com.kola.kmp.protocol.pet.KPetProtocol;

public class KGetAllPetTemplateMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KGetAllPetTemplateMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return KPetProtocol.CM_REQUEST_MUSEUM_PETS_INFO;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		Map<KPetQuality, List<KPetTemplateHandbookModel>> allTemplates = KPetModuleManager.getAllPetTemplatesByQuality();
		Map.Entry<KPetQuality, List<KPetTemplateHandbookModel>> entry;
		KPetQuality quality;
		List<KPetTemplateHandbookModel> list;
		KPetTemplateHandbookModel template;
		KPetGetWay getWay;
		int sizeIndex;
		int count;
		KGameMessage msg = KGame.newLogicMessage(KPetProtocol.SM_RESPONSE_MUSEUM_PETS_INFO);
		msg.writeByte(allTemplates.size());
		for(Iterator<Map.Entry<KPetQuality, List<KPetTemplateHandbookModel>>> itr = allTemplates.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			quality = entry.getKey();
			list = entry.getValue();
			count = 0;
			msg.writeUtf8String(quality.getName());
			msg.writeInt(quality.getColor());
			sizeIndex = msg.writerIndex();
			msg.writeShort(list.size());
			for(int k = 0; k < list.size(); k++) {
				template = list.get(k);
				if (template.isShowInHandbook()) {
					KPetServerMsgSender.packSinglePetData(template, msg);
					msg.writeUtf8String("");
					msg.writeByte(template.getPetGetWayMap().size());
					for (Iterator<KPetGetWay> itr2 = template.getPetGetWayMap().keySet().iterator(); itr2.hasNext();) {
						getWay = itr2.next();
						msg.writeByte(getWay.sign);
						msg.writeUtf8String(getWay.name);
					}
					count++;
				}
			}
			msg.setShort(sizeIndex, count);
		}
		msgEvent.getPlayerSession().send(msg);
	}
	
}
