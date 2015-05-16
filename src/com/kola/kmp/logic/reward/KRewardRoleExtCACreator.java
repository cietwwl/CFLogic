package com.kola.kmp.logic.reward;

import com.kola.kgame.cache.DataCacheAccesserFactory;
import com.kola.kgame.cache.role.RoleExtCABaseImpl;
import com.kola.kmp.logic.fashion.KRoleFashion;
import com.kola.kmp.logic.other.KRoleExtTypeEnum;
import com.kola.kmp.logic.role.IRoleExtCACreator;
import com.kola.kmp.logic.support.KSupportFactory;

public class KRewardRoleExtCACreator implements IRoleExtCACreator {

	@Override
	public RoleExtCABaseImpl createRoleExtCAForDB(long roleId, int type) {
		return new KRoleReward(roleId, type, false);
	}

	@Override
	public RoleExtCABaseImpl newRoleExtCAInstance(long roleId, int type) {
		return new KRoleReward(roleId, type, true);
	}

	public static KRoleReward getRoleReward(long roleId) {
		return (KRoleReward)KSupportFactory.getRoleModuleSupport().getRoleExtCA(roleId, KRoleExtTypeEnum.REWARD, true);
	}

}
