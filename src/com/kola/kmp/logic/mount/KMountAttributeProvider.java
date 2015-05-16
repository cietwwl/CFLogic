package com.kola.kmp.logic.mount;

import java.util.Map;

import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.role.IRoleAttributeProvider;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-4-8 下午8:49:10
 * </pre>
 */
public class KMountAttributeProvider implements IRoleAttributeProvider {

	private static int _type;
	
	public static int getType() {
		return _type;
	}
	
	@Override
	public Map<KGameAttrType, Integer> getEffectAttr(KRole role) {
		//骑乘属性=升级属性（培养属性）
		return KMountLogic.getAllMountAttsForLv(role.getId());
	}
	
	@Override
	public void notifyTypeAssigned(int type) {
		_type = type;
	}

}
