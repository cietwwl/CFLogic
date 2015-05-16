package com.kola.kmp.logic.reward.garden;

import com.kola.kgame.cache.role.RoleExtCABaseImpl;
import com.kola.kmp.logic.other.KRoleExtTypeEnum;
import com.kola.kmp.logic.role.IRoleExtCACreator;
import com.kola.kmp.logic.support.KSupportFactory;

public class KGardenRoleExtCACreator implements IRoleExtCACreator {

	@Override
	public RoleExtCABaseImpl createRoleExtCAForDB(long roleId, int type) {
		return new KRoleGarden(roleId, type, false);
	}

	@Override
	public RoleExtCABaseImpl newRoleExtCAInstance(long roleId, int type) {
		return new KRoleGarden(roleId, type, true);
	}

	public static KRoleGarden getRoleGarden(long roleId) {
		return (KRoleGarden)KSupportFactory.getRoleModuleSupport().getRoleExtCA(roleId, KRoleExtTypeEnum.GARDEN, true);
	}

}
