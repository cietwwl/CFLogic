package com.kola.kmp.logic.talent;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.kola.kmp.logic.support.TalentModuleSupport;

/**
 * 
 * @author PERRY CHAN
 */
public class KTalentModuleSupportImpl implements TalentModuleSupport {

	@Override
	public KTalentEntireData getTalentData(long roleId) {
		return KTalentModuleManager.getTalentEntireData(roleId);
	}
	
	@Override
	public Map<String, List<ITalent>> getAllTalentData(long roleId) {
		KTalentEntireData entireData = KTalentModuleManager.getTalentEntireData(roleId);
		if (entireData != null) {
			List<KTalentTreeTemplate> list = KTalentModuleManager.getAllTalentTreeTemplates();
			Map<String, List<ITalent>> map = new LinkedHashMap<String, List<ITalent>>(list.size());
			KTalentTree tree;
			for (int i = 0; i < list.size(); i++) {
				tree = entireData.getTalentTree(list.get(i).talentTreeId);
				map.put(tree.getTalentTreeName(), tree.getAllTalentDatas());
			}
			return map;
		} else {
			return Collections.emptyMap();
		}
	}

}
