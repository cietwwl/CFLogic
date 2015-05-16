package com.kola.kmp.logic.role;

import com.kola.kgame.cache.role.RoleExtCABaseImpl;

/**
 * 
 * @author PERRY CHAN
 */
public interface IRoleExtCACreator {

	/**
	 * 
	 * @return
	 */
	public RoleExtCABaseImpl createRoleExtCAForDB(long roleId, int type);
	
	/**
	 * 
	 * @param roleId
	 * @param type
	 * @return
	 */
	public RoleExtCABaseImpl newRoleExtCAInstance(long roleId, int type);
}
