package com.kola.kmp.logic.item;

import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.item.message.KPushItemsMsg;
import com.kola.kmp.logic.role.IRoleEventListener;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.util.ResultStructs.EquiSetResult;

public class KItemRoleEventListener implements IRoleEventListener {

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {
		// //CTODO ---------以下属于测试代码
		// KItemLogic.addItemToBag(role, "350000", 10);
		// KItemLogic.addItemToBag(role, "350100", 10);
		
		// ----------------
		// 发送消息给客户端
		KPushItemsMsg.pushAllItems(role);
		KPushItemsMsg.pushItemQualitySetConstance(role);
		// 重算套装数据并PUSH
		EquiSetResult isSetChange = KItemLogic.recountEquiSetData(role, KItemConfig.MAIN_BODYSLOT_ID);
		KItemLogic.synEquiSetData(role, isSetChange);
	}

	@Override
	public void notifyRoleLeavedGame(/*KGamePlayerSession session,*/KRole role) {
		// 忽略
	}

	@Override
	public void notifyRoleCreated(KGamePlayerSession session, KRole role) {
		KItemLogic.initEquipmentsForNewRole(role);
		
		// 新角色初始默认物品
		KItemLogic.addItemsToBag(role, KItemConfig.NewRoleItems, "新角色初始默认物品");
	}

	@Override
	public void notifyRoleDeleted(long roleId) {
		// 忽略
	}

	@Override
	public void notifyRoleLevelUp(KRole role, int preLv) {
//		if(KItemDataManager.mItemTemplateManager.checkSenstiveLv(preLv, role.getLevel())){
			// 同步装备强化价格、战力、获取引导等
			KPushItemsMsg.pushAllItems(role);
//		} else {
//			KPushItemsMsg.pushAllItemsPower(role);
//		}
	}
	
	@Override
	public void notifyRoleDataPutToCache(KRole role) {
		
	}
}
