package com.kola.kmp.logic.fashion;

import com.kola.kgame.cache.role.RoleExtCABaseImpl;
import com.kola.kmp.logic.other.KRoleExtTypeEnum;
import com.kola.kmp.logic.role.IRoleExtCACreator;
import com.kola.kmp.logic.support.KSupportFactory;

public class KFasionRoleExtCACreator implements IRoleExtCACreator {

	@Override
	public RoleExtCABaseImpl createRoleExtCAForDB(long roleId, int type) {
		return new KRoleFashion(roleId, type);
	}

	@Override
	public RoleExtCABaseImpl newRoleExtCAInstance(long roleId, int type) {
		return new KRoleFashion(roleId, type);
	}

	public static KRoleFashion getRoleFashion(long roleId) {
		return (KRoleFashion)KSupportFactory.getRoleModuleSupport().getRoleExtCA(roleId, KRoleExtTypeEnum.FASHION, true);
	}

}
