package com.kola.kmp.logic.reward.exciting;

import com.kola.kgame.cache.role.RoleExtCABaseImpl;
import com.kola.kmp.logic.other.KRoleExtTypeEnum;
import com.kola.kmp.logic.role.IRoleExtCACreator;
import com.kola.kmp.logic.support.KSupportFactory;

public class KExcitingExtCACreator implements IRoleExtCACreator {

	@Override
	public RoleExtCABaseImpl createRoleExtCAForDB(long roleId, int type) {
		return new KRoleExciting(roleId, type, false);
	}

	@Override
	public RoleExtCABaseImpl newRoleExtCAInstance(long roleId, int type) {
		return new KRoleExciting(roleId, type, true);
	}

	public static KRoleExciting getRoleExciting(long roleId) {
		return (KRoleExciting) KSupportFactory.getRoleModuleSupport().getRoleExtCA(roleId, KRoleExtTypeEnum.EXCITING, true);
	}

}
