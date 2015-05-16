package com.kola.kmp.logic.pet;

import java.util.Collections;
import java.util.Map;

import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.role.IRoleAttributeProvider;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KPetModuleAttributeProvider implements IRoleAttributeProvider {

	private static int _type;
	
	static int getProviderType() {
		return _type;
	}
	
	@Override
	public Map<KGameAttrType, Integer> getEffectAttr(KRole role) {
		KPet pet = KSupportFactory.getPetModuleSupport().getFightingPet(role.getId());
		if(pet != null) {
			return pet.getEffectRoleAttrs();
		}
		return Collections.emptyMap();
	}

	@Override
	public void notifyTypeAssigned(int type) {
		_type = type;
	}

}
