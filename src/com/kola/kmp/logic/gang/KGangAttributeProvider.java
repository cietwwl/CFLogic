package com.kola.kmp.logic.gang;

import java.util.Collections;
import java.util.Map;

import com.kola.kmp.logic.gang.war.KGangWarDataManager;
import com.kola.kmp.logic.gang.war.KGangWarDataManager.KGangMedalDataManager.GangMedalData;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.role.IRoleAttributeProvider;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.ItemModuleSupport.ISecondWeapon;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-11-7 下午4:22:19
 * </pre>
 */
public class KGangAttributeProvider implements IRoleAttributeProvider {

	private static int _type;

	public static int getType() {
		return _type;
	}

	@Override
	public Map<KGameAttrType, Integer> getEffectAttr(KRole role) {
		long roleId = role.getId();
		KGang gang = KGangLogic.getGangByRoleId(roleId);
		if (gang == null) {
			return Collections.emptyMap();
		}
		gang.rwLock.lock();
		try {
			KGangMember mem = gang.getMember(roleId);
			if (mem == null) {
				return Collections.emptyMap();
			}
			GangMedalData medalData = KGangWarDataManager.mGangMedalDataManager.getDataByRank(mem.getMedal());
			if(medalData==null){
				return Collections.emptyMap();
			}
			return medalData.addAttsMap;
		} finally {
			gang.rwLock.unlock();
		}
	}

	@Override
	public void notifyTypeAssigned(int type) {
		_type = type;
	}

	public static void notifyEffectAttrChange(long roleId) {
		// 刷新角色属性
		KSupportFactory.getRoleModuleSupport().notifyEffectAttrChange(roleId, getType());
		
		// 刷新UI
//		KSupportFactory.getMapSupport().no
	}
}
