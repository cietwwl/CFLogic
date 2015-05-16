package com.kola.kmp.logic.gang;

import com.kola.kgame.cache.DataCacheAccesserFactory;
import com.kola.kgame.cache.role.RoleExtCABaseImpl;
import com.kola.kmp.logic.other.KRoleExtTypeEnum;
import com.kola.kmp.logic.role.IRoleExtCACreator;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.vip.KRoleVIP;

public class KGangRoleExtCACreator implements IRoleExtCACreator {

	@Override
	public RoleExtCABaseImpl createRoleExtCAForDB(long roleId, int type) {
		return new KRoleGangData(roleId, type);
	}

	@Override
	public RoleExtCABaseImpl newRoleExtCAInstance(long roleId, int type) {
		return new KRoleGangData(roleId, type);
	}

	public static KRoleGangData getData(long roleId, boolean createIfNotExist) {
		return (KRoleGangData)KSupportFactory.getRoleModuleSupport().getRoleExtCA(roleId, KRoleExtTypeEnum.GANG, true);
	}

}
