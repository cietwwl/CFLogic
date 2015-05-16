package com.kola.kmp.logic.pet;

import java.util.List;
import java.util.Map;

import com.koala.game.util.StringUtil;
import com.kola.kmp.logic.flow.FlowManager;
import com.kola.kmp.logic.flow.FlowManager.PropertyTypeEnum;
import com.kola.kmp.logic.util.tips.PetTips;

/**
 * 
 * @author PERRY CHAN
 */
public class KPetFlowLogger {
	
	public static void logSwallowPets(KPet swallower, List<KPet> targets, int exp, int preExp, int preLevel, Map<Integer, Integer> skillChgMap) {
		StringBuilder strBld = new StringBuilder("（");
		KPet pet;
		for (int i = 0; i < targets.size(); i++) {
			pet = targets.get(i);
			strBld.append(pet.getName()).append("，").append(pet.getUUID()).append("；");
		}
		strBld.append("）");
		String tips = StringUtil.format(PetTips.getTipsSwallowPets(), strBld.toString(), exp, preExp, swallower.getCurrentExp(), preLevel, swallower.getLevel(), skillChgMap.toString().replace(",", "，").replace("[", "（").replace("]", "）"));
		FlowManager.logPropertyModify(swallower.getId(), PropertyTypeEnum.宠物, swallower.getUUID(), swallower.getTemplateId(), swallower.getName(), tips);
	}
}
