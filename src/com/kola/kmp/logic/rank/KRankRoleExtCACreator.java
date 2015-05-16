package com.kola.kmp.logic.rank;

import com.kola.kgame.cache.DataCacheAccesserFactory;
import com.kola.kgame.cache.role.RoleExtCABaseImpl;
import com.kola.kmp.logic.other.KRoleExtTypeEnum;
import com.kola.kmp.logic.reward.KRoleReward;
import com.kola.kmp.logic.role.IRoleExtCACreator;
import com.kola.kmp.logic.support.KSupportFactory;

public class KRankRoleExtCACreator implements IRoleExtCACreator {

	@Override
	public RoleExtCABaseImpl createRoleExtCAForDB(long roleId, int type) {
		return new KRoleRankData(roleId, type, false);
	}

	@Override
	public RoleExtCABaseImpl newRoleExtCAInstance(long roleId, int type) {
		return new KRoleRankData(roleId, type, true);
	}

	public static KRoleRankData getRoleRankData(long roleId) {
		return (KRoleRankData)KSupportFactory.getRoleModuleSupport().getRoleExtCA(roleId, KRoleExtTypeEnum.RANK, true);
	}

}
