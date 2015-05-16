package com.kola.kmp.logic.role;

import com.kola.kgame.cache.role.RoleBaseImpl;

/**
 * 
 * @author PERRY CHAN
 */
public class KSystemRole extends RoleBaseImpl {

	@Override
	protected String saveLogicAttribute() {
		return "";
	}

	@Override
	protected void parseLogicAttribute(String attribute) {
		
	}
	
	@Override
	protected void initFromDBComplete() {
		
	}
	
	@Override
	protected int getVipLevel() {
		return 0;
	}

	@Override
	protected void onEntireDataLoadComplete() {
		
	}
}
