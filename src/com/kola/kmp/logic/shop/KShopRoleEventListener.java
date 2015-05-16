package com.kola.kmp.logic.shop;

import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.role.IRoleEventListener;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.shop.random.KRandomShopCenter;
import com.kola.kmp.logic.shop.timehot.KHotShopCenter;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2013-1-29 下午5:50:54
 * </pre>
 */
public class KShopRoleEventListener implements IRoleEventListener {

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {
		
		KRoleShop shop = KShopRoleExtCACreator.getRoleShop(role.getId());
		// 尝试跨天数据重置
		shop.notifyForLogin(role.getLevel());
		
		KRandomShopCenter.notifyRoleJoinedGame(session, role);
		
		KHotShopCenter.notifyRoleJoinedGame(session, role);
	}

	@Override
	public void notifyRoleLeavedGame(/*KGamePlayerSession session,*/KRole role) {
		// 忽略
	}

	@Override
	public void notifyRoleCreated(KGamePlayerSession session, KRole role) {
		// CTODO
	}

	@Override
	public void notifyRoleDeleted(long roleId) {
		// 忽略
	}

	@Override
	public void notifyRoleLevelUp(KRole role, int preLv) {
		KRandomShopCenter.notifyRoleLevelUp(role, preLv);
		
		KHotShopCenter.notifyRoleLevelUp(role, preLv);
	}
	
	@Override
	public void notifyRoleDataPutToCache(KRole role) {
		
	}

}
