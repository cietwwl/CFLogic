package com.kola.kmp.logic.map;

import com.koala.game.KGame;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.flowdata.FlowDataModuleFactory;
import com.kola.kmp.logic.activity.KActivityManager;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMapManager;
import com.kola.kmp.logic.mission.KMissionModuleExtension;
import com.kola.kmp.logic.mission.assistant.KAssistantManager;
import com.kola.kmp.logic.mission.guide.KGuideManager;
import com.kola.kmp.logic.role.IRoleEventListener;
import com.kola.kmp.logic.role.IRoleGameSettingData;
import com.kola.kmp.logic.role.IRoleMapData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KMapRoleEventListener implements IRoleEventListener {

	private static KMapRoleEventListener instance;

	public KMapRoleEventListener() {
		instance = this;
	}

	public static KMapRoleEventListener getInstance() {
		return instance;
	}

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {
		// if (KGuideManager.isCloseNoviceGuide) {
		// // 发送角色主菜单功能的开放状态信息
		// KGuideManager.sendAllOpenFunction(role);
		//
		// // 处理角色等距进入地图
		// KMapModule.getGameMapManager().playerRoleLoginJumpMap(role, false);
		//
		// KMapModule.getGameMapManager().sendRoleSettingMsg(role, true);
		// } else if (KSupportFactory.getNoviceGuideSupport()
		// .checkRoleCompleteFirstNoviceGuideBattle(role)) {
		//
		// // 发送角色主菜单功能的开放状态信息
		// KGuideManager.sendAllOpenFunction(role);
		//
		// // 处理角色等距进入地图
		// KMapModule.getGameMapManager().playerRoleLoginJumpMap(role, true);
		//
		// KMapModule.getGameMapManager().sendRoleSettingMsg(role, true);
		// } else {
		// KSupportFactory.getNoviceGuideSupport()
		// .notifyPlayNoviceGuideAnimation(role);
		// }
		
		IRoleGameSettingData data = role.getRoleGameSettingData();
		if(data!=null){
			data.checkAndSetMapShowPlayerLevel(session);
			data.notifyLogin();
		}
		
		KSupportFactory.getNoviceGuideSupport().checkAndSendIsOpenBattlePowerSlot(role);

		KSupportFactory.getNoviceGuideSupport().processNoviceGuide(role);
		
		
	}

	@Override
	public void notifyRoleLeavedGame(/*KGamePlayerSession session,*/KRole role) {
		
		IRoleGameSettingData data = role.getRoleGameSettingData();
		if(data!=null){
			data.notifyLogout();
		}
		
		IRoleMapData mapData = role.getRoleMapData();
		if (mapData != null) {
			if (mapData.isInDuplicateMap()
					&& mapData.getCurrentDuplicateMapId() > 0) {
				KDuplicateMapManager.getInstace()
						.playerRoleLogoutLeaveDuplicateMap(role,
								mapData.getCurrentDuplicateMapId());
			}
		}
		KMapModule.getGameMapManager().playerRoleLogoutGameLeaveMap(role);
	}

	@Override
	public void notifyRoleCreated(KGamePlayerSession session, KRole role) {
		FlowDataModuleFactory.getModule().recordCreatePlayerRole(session, role, role.getJob());
	}

	@Override
	public void notifyRoleDeleted(long roleId) {

	}

	@Override
	public void notifyRoleLevelUp(KRole role, int preLv) {
		KGamePlayerSession session = KGame.getPlayerSession(role.getPlayerId());
		IRoleGameSettingData data = role.getRoleGameSettingData();
		if(data!=null && session != null && session.getBoundPlayer() != null){
			int useTime = data.notifyUpgradeLv();
			FlowDataModuleFactory.getModule().recordRoleUpgradeLv(session, role, preLv, role.getLevel(), useTime);
		}
	}
	
	@Override
	public void notifyRoleDataPutToCache(KRole role) {
		
	}

	public void notifyRoleJoinMap(KRole role) {
		// 发送角色主菜单功能的开放状态信息
		KGuideManager.sendAllOpenFunction(role);

		// 处理角色等距进入地图
		KMapModule.getGameMapManager().playerRoleLoginJumpMap(role);

		KMapModule.getGameMapManager().sendRoleSettingMsg(role, true);

		// 查找角色可接任务，并初始化到任务容器中
		KMissionModuleExtension.getManager().processSearchCanAcceptedMission(
				role);
		// 发送角色任务列表数据
		KMissionModuleExtension.getManager().processGetPlayerRoleMissionList(
				role);
		//日常任务数据
		KMissionModuleExtension.getManager().getDailyMissionManager().sendDailyMissionData(role);
		// 小助手数据
		KAssistantManager.sendAssistandData(role);
		// 活动列表
		KActivityManager.getInstance().sendAllActivityData(role);
	}

}
