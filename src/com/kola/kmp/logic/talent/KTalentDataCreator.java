package com.kola.kmp.logic.talent;

import com.kola.kgame.cache.role.RoleExtCABaseImpl;
import com.kola.kmp.logic.role.IRoleExtCACreator;

/**
 * 
 * @author PERRY CHAN
 */
public class KTalentDataCreator implements IRoleExtCACreator {

	@Override
	public RoleExtCABaseImpl createRoleExtCAForDB(long roleId, int type) {
		return new KTalentEntireData(roleId, type);
	}

	@Override
	public RoleExtCABaseImpl newRoleExtCAInstance(long roleId, int type) {
		return new KTalentEntireData(roleId, type);
	}

}
