package com.kola.kmp.logic.talent;

import java.util.Map;

import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.role.IRoleAttributeProvider;
import com.kola.kmp.logic.role.KRole;

/**
 * 
 * @author PERRY CHAN
 */
public class KTalentAttributeProvider implements IRoleAttributeProvider {

	private static int _type;
	
	public static int getType() {
		return _type;
	}
	
	@Override
	public Map<KGameAttrType, Integer> getEffectAttr(KRole role) {
		KTalentEntireData talentData = KTalentModuleManager.getTalentEntireData(role.getId());
		if (talentData != null) {
			return talentData.getEffectAttr();
		}
		return null;
	}

	@Override
	public void notifyTypeAssigned(int type) {
		_type = type;
	}

}
