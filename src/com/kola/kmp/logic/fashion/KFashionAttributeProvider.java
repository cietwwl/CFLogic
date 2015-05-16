package com.kola.kmp.logic.fashion;

import java.util.Collections;
import java.util.Map;

import com.kola.kmp.logic.fashion.KFashionDataStructs.KFashionTemplate;
import com.kola.kmp.logic.fashion.KRoleFashion.FashionData;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.role.IRoleAttributeProvider;
import com.kola.kmp.logic.role.KRole;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-4-8 下午8:49:10
 * </pre>
 */
public class KFashionAttributeProvider implements IRoleAttributeProvider {

	private static int _type;
	
	public static int getType() {
		return _type;
	}
	
	@Override
	public Map<KGameAttrType, Integer> getEffectAttr(KRole role) {
		KRoleFashion set = KFasionRoleExtCACreator.getRoleFashion(role.getId());
		set.rwLock.lock();
		try {
			FashionData data = set.getFashionData(set.getSelectedFashionId());
			if (data == null) {
				return Collections.emptyMap();
			}
			KFashionTemplate temp = KFashionDataManager.mFashionTemplateManager.getFashionTemplate(data.tempId);
			if (temp == null) {
				return Collections.emptyMap();
			}
			return temp.allEffects;
		} finally {
			set.rwLock.unlock();
		}
	}

	@Override
	public void notifyTypeAssigned(int type) {
		_type = type;
	}

}
