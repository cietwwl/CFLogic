package com.kola.kmp.logic.activity.transport;

import com.koala.game.exception.KGameServerException;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.activity.KActivity;
import com.kola.kmp.logic.npc.message.KNPCOrderMsg;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KNPCOrderEnum;
import com.kola.kmp.logic.role.KRole;

public class KTransportActivity extends KActivity {

	@Override
	public KActionResult playerRoleJoinActivity(KRole role) {
		// 通知跳转到好友副本界面
		KActionResult result = new KActionResult();
		KNPCOrderMsg.sendNPCMenuOrder(role, KNPCOrderEnum.ORDER_OPEN_TRANSPORT,
				UtilTool.getNotNullString(null));

		result.success = true;
		result.tips = UtilTool.getNotNullString(null);
		return result;
	}

	@Override
	public void init(String activityConfigPath) throws KGameServerException {
		System.err
				.println("init KTransportActivity.......................#########$$$$$$$$$$$$$$$$$$,id:"
						+ this.activityId
						+ ","
						+ this.activityName
						+ ",activityConfigPath:" + activityConfigPath);
		KTransportManager.openRoleLv = this.openRoleLv;
		this.isOpened = true;
		KTransportManager.init(activityConfigPath);
	}

	@Override
	public void onGameWorldInitComplete() throws KGameServerException {

	}

	@Override
	public void notifyCacheLoadComplete() throws KGameServerException {
		KTransportManager.readTransporters();
	}

	@Override
	public void serverShutdown() throws KGameServerException {
		KTransportManager.isShutdown = true;
		KTransportManager.saveTransporters();
	}

	@Override
	public int getRestJoinActivityCount(KRole role) {
		return 0;
	}

}
