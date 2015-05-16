package com.kola.kmp.logic.gamble;

import com.kola.kgame.cache.DataCacheAccesserFactory;
import com.kola.kgame.cache.role.RoleExtCABaseImpl;
import com.kola.kmp.logic.other.KRoleExtTypeEnum;
import com.kola.kmp.logic.role.IRoleExtCACreator;

public class KGambleRoleExtCACreator implements IRoleExtCACreator {

	@Override
	public RoleExtCABaseImpl createRoleExtCAForDB(long roleId, int type) {
		return new KGambleRoleExtData(roleId, type);
	}

	@Override
	public RoleExtCABaseImpl newRoleExtCAInstance(long roleId, int type) {
		return new KGambleRoleExtData(roleId, type);
	}

	public static KGambleRoleExtData getGambleRoleExtData(long roleId) {
		KGambleRoleExtData result = (KGambleRoleExtData) DataCacheAccesserFactory
				.getRoleEntireDataCacheAccesser().getRoleExtCASet(roleId)
				.getExtCAByType(KRoleExtTypeEnum.GAMBLE.sign);
		if (result == null) {
			result = (KGambleRoleExtData) DataCacheAccesserFactory
					.getRoleEntireDataCacheAccesser().getRoleExtCASet(roleId)
					.addExtCA(KRoleExtTypeEnum.GAMBLE.sign);
		}
		return result;
	}

}
