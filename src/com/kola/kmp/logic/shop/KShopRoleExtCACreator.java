package com.kola.kmp.logic.shop;

import com.kola.kgame.cache.role.RoleExtCABaseImpl;
import com.kola.kmp.logic.other.KRoleExtTypeEnum;
import com.kola.kmp.logic.role.IRoleExtCACreator;
import com.kola.kmp.logic.shop.random.KRoleRandomData;
import com.kola.kmp.logic.shop.timehot.TimeHotShopData;
import com.kola.kmp.logic.support.KSupportFactory;

public class KShopRoleExtCACreator implements IRoleExtCACreator {

	@Override
	public RoleExtCABaseImpl createRoleExtCAForDB(long roleId, int type) {
		return new KRoleShop(roleId, type);
	}

	@Override
	public RoleExtCABaseImpl newRoleExtCAInstance(long roleId, int type) {
		return new KRoleShop(roleId, type);
	}

	public static KRoleShop getRoleShop(long roleId) {
		return (KRoleShop)KSupportFactory.getRoleModuleSupport().getRoleExtCA(roleId, KRoleExtTypeEnum.SHOP, true);
	}
	
	public static KRoleRandomData getRoleRandomData(long roleId) {
		KRoleShop roleShop = getRoleShop(roleId);
		if(roleShop==null){
			return null;
		}
		return roleShop.getRandomData();
	}
	
	public static TimeHotShopData getRoleTimeHotShopData(long roleId) {
		KRoleShop roleShop = getRoleShop(roleId);
		if(roleShop==null){
			return null;
		}
		return roleShop.getTimeHotShopData();
	}
}
