package com.kola.kmp.logic.gang.reswar;

import com.koala.game.exception.KGameServerException;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.activity.KActivity;
import com.kola.kmp.logic.gang.KGangLogic;
import com.kola.kmp.logic.npc.message.KNPCOrderMsg;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KNPCOrderEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.GangTips;

public class KResWarActivity extends KActivity {

	static KResWarActivity instance;

	public KResWarActivity() {
		super();
		instance = this;
		isOpened = true;
	}

	@Override
	public KActionResult playerRoleJoinActivity(KRole role) {

		KActionResult result = new KActionResult();

		// 是否有军团
		long gangId = KSupportFactory.getGangSupport().getGangIdByRoleId(role.getId());
		if (gangId < 1) {
			result.tips = GangTips.对不起您不属于任何军团;
			return result;
		}

		// 通知跳转到军团资源战活动面板
		KNPCOrderMsg.sendNPCMenuOrder(role, KNPCOrderEnum.ORDER_OPEN_GANG_REWWAR, UtilTool.getNotNullString(null));

		result.success = true;
		result.tips = UtilTool.getNotNullString(null);
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
