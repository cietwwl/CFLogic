package com.kola.kmp.logic.shop.random;

import com.koala.game.exception.KGameServerException;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.activity.KActivity;
import com.kola.kmp.logic.npc.message.KNPCOrderMsg;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KNPCOrderEnum;
import com.kola.kmp.logic.role.KRole;

public class RandomShopActivity extends KActivity{
	static RandomShopActivity instance;

	public RandomShopActivity() {
		super();
		instance = this;
		isOpened = true;
	}
	
	@Override
	public KActionResult playerRoleJoinActivity(KRole role) {
		KActionResult result = new KActionResult();
		result.success = true;
		result.tips = UtilTool.getNotNullString(null);
		
		KNPCOrderMsg.sendNPCMenuOrder(role,
				KNPCOrderEnum.ORDER_OPEN_RANDOMSHOP,
				UtilTool.getNotNullString(null));
		
		return result;
	}

	@Override
	public void init(String activityConfigPath) throws KGameServerException {
		System.err.println("init "+getClass().getSimpleName()+".......................#########$$$$$$$$$$$$$$$$$$,id:" + this.activityId + "," + this.activityName + ",activityConfigPath:" + activityConfigPath);
		// 忽略
	}

	@Override
	public void onGameWorldInitComplete() throws KGameServerException {
		// 忽略
	}

	@Override
	public void notifyCacheLoadComplete() throws KGameServerException {
		// 忽略
	}

	@Override
	public void serverShutdown() throws KGameServerException {
		// 忽略
	}

	@Override
	public int getRestJoinActivityCount(KRole role) {
		// TODO Auto-generated method stub
		return 0;
	}
}
