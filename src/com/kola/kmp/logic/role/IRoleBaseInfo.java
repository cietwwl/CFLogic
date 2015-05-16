package com.kola.kmp.logic.role;

import com.kola.kgame.cache.role.RoleBaseInfo;
import com.kola.kmp.logic.util.IRoleMapResInfo;

/**
 * 
 * @author PERRY CHAN
 */
public interface IRoleBaseInfo extends RoleBaseInfo, IRoleMapResInfo {
	
	/**
	 * 
	 * @return
	 */
	public boolean isOnline();
}
