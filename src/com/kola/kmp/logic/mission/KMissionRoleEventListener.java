package com.kola.kmp.logic.mission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.map.KGameNormalMap;
import com.kola.kmp.logic.map.KMapModule;
import com.kola.kmp.logic.mission.IMissionMenuImpl.IMissionConversationImpl;
import com.kola.kmp.logic.mission.MissionDialog.Dialogue;
import com.kola.kmp.logic.mission.assistant.KAssistantManager;
import com.kola.kmp.logic.mission.guide.KGuideManager;
import com.kola.kmp.logic.npc.menu.IMissionMenu;
import com.kola.kmp.logic.npc.menu.KMenuService;
import com.kola.kmp.logic.other.KGameMissionStatusEnum;
import com.kola.kmp.logic.role.IRoleEventListener;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KMissionRoleEventListener implements IRoleEventListener {

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {

		// // 查找角色可接任务，并初始化到任务容器中
		// KMissionModuleExtension.getManager().processSearchCanAcceptedMission(
		// role);
		// // 发送角色任务列表数据
		// KMissionModuleExtension.getManager().processGetPlayerRoleMissionList(
		// role);
		// //小助手数据
		// KAssistantManager.sendAssistandData(role);
	}

	@Override
	public void notifyRoleLeavedGame(/*KGamePlayerSession session,*/KRole role) {

	}

	@Override
	public void notifyRoleCreated(KGamePlayerSession session, KRole role) {

	}

	@Override
	public void notifyRoleDeleted(long roleId) {

	}

	@Override
	public void notifyRoleLevelUp(KRole role, int preLv) {
		// 当角色升级时，判断可接任务列表中的未开放状态任务是否达到开放条件
		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role
				.getId());

		List<KMissionTemplate> openAcceptableMissionTemplateList = new ArrayList<KMissionTemplate>();
		for (KMissionTemplate acceptableMissionTemplate : missionSet
				.getAcceptableMissionTemplateMap().values()) {
			int roleLevelLimit = acceptableMissionTemplate.missionTriggerCondition.roleLevelLimit;
			if (roleLevelLimit > preLv && roleLevelLimit <= role.getLevel()
					&& acceptableMissionTemplate.isMissionCanAccept(role)) {
				openAcceptableMissionTemplateList
						.add(acceptableMissionTemplate);
			}
		}
		KGameNormalMap currentMap = KMapModule.getGameMapManager().getGameMap(
				role.getRoleMapData().getCurrentMapId());
		if (!openAcceptableMissionTemplateList.isEmpty()) {
			Map<Integer, List<IMissionMenu>> menuListMap = new HashMap<Integer, List<IMissionMenu>>();
			for (KMissionTemplate acceptableMissionTemplate : openAcceptableMissionTemplateList) {
				int acceptMissionNpcId = acceptableMissionTemplate.acceptMissionNPCTemplate.templateId;
				if (currentMap != null
						|| currentMap.isNpcEntityInMap(acceptMissionNpcId)) {
					if (!menuListMap.containsKey(acceptMissionNpcId)) {
						menuListMap.put(acceptMissionNpcId,
								new ArrayList<IMissionMenu>());
					}
					IMissionMenuImpl menu = KMissionModuleSupportImpl
							.constructIMissionMenu(
									role,
									acceptableMissionTemplate,
									acceptableMissionTemplate
											.getMissionNameByStatusType(
													KGameMissionStatusEnum.MISSION_STATUS_TRYRECEIVE,
													role),
									KGameMissionStatusEnum.MISSION_STATUS_TRYRECEIVE.statusType,
									IMissionMenu.ACTION_AFTER_TALK_SUBMIT,
									acceptableMissionTemplate.getPrologueMissionDialog(),
									acceptableMissionTemplate.getAcceptMissionDialog(role));
					// Dialogue dialogue =
					// acceptableMissionTemplate.acceptMissionTipsDialog
					// .getDialogueList().get(0);
					// menu.addMissionConversation(new IMissionConversationImpl(
					// dialogue.getContent(role.getJob()), dialogue
					// .getQuestion()));
					menuListMap.get(acceptMissionNpcId).add(menu);
				}
			}
			if (menuListMap.size() > 0) {
				for (Integer addMenuNpcId : menuListMap.keySet()) {
					List<IMissionMenu> missionMenuList = menuListMap
							.get(addMenuNpcId);
					if (missionMenuList != null && missionMenuList.size() > 0) {
						IMissionMenu[] menus = new IMissionMenu[missionMenuList
								.size()];
						for (int i = 0; i < menus.length; i++) {
							menus[i] = missionMenuList.get(i);
						}
						KMenuService.synNpcAddOrUpdateMenus(role, addMenuNpcId,
								missionMenuList);
					}
				}

			}

			KMissionModuleExtension
					.getManager()
					.processUpdateMissionListWhileTriggerOpenAcceptableMissions(
							role, openAcceptableMissionTemplateList);
		}

		// 判断角色等级提升，是否产生新的可接任务
		List<KMissionTemplate> newAcceptableMissionTemplateList = KMissionModuleExtension
				.getManager().searchNewAcceptableMissionWhileRoleLevelUp(role,
						preLv, role.getLevel());

		if (!newAcceptableMissionTemplateList.isEmpty()) {
			// //////////////////处理NPC菜单///////////////////////
			Map<Integer, List<IMissionMenu>> menuListMap = new HashMap<Integer, List<IMissionMenu>>();
			for (KMissionTemplate newAcceptableMissionTemplate : newAcceptableMissionTemplateList) {

				// 如果角色等级未达到开放当前可接任务的等级限制，跳过
				if (!newAcceptableMissionTemplate.isMissionCanAccept(role)) {
					continue;
				}

				int acceptMissionNpcId = newAcceptableMissionTemplate.acceptMissionNPCTemplate.templateId;
				if (!menuListMap.containsKey(acceptMissionNpcId)) {
					menuListMap.put(acceptMissionNpcId,
							new ArrayList<IMissionMenu>());
				}
				IMissionMenuImpl menu = KMissionModuleSupportImpl
						.constructIMissionMenu(
								role,
								newAcceptableMissionTemplate,
								newAcceptableMissionTemplate
										.getMissionNameByStatusType(
												KGameMissionStatusEnum.MISSION_STATUS_TRYRECEIVE,
												role),
								KGameMissionStatusEnum.MISSION_STATUS_TRYRECEIVE.statusType,
								IMissionMenu.ACTION_AFTER_TALK_SUBMIT,
								newAcceptableMissionTemplate.getPrologueMissionDialog(),
								newAcceptableMissionTemplate.getAcceptMissionDialog(role));
				// Dialogue dialogue =
				// newAcceptableMissionTemplate.acceptMissionTipsDialog
				// .getDialogueList().get(0);
				// menu.addMissionConversation(new IMissionConversationImpl(
				// dialogue.getContent(role.getJob()), dialogue
				// .getQuestion()));
				menuListMap.get(acceptMissionNpcId).add(menu);
			}
			if (menuListMap.size() > 0) {
				for (Integer addMenuNpcId : menuListMap.keySet()) {
					List<IMissionMenu> missionMenuList = menuListMap
							.get(addMenuNpcId);
					if (missionMenuList != null && missionMenuList.size() > 0) {
						IMissionMenu[] menus = new IMissionMenu[missionMenuList
								.size()];
						for (int i = 0; i < menus.length; i++) {
							menus[i] = missionMenuList.get(i);
						}
						KMenuService.synNpcAddOrUpdateMenus(role, addMenuNpcId,
								missionMenuList);
					}
				}

			}

			// //////////////更新任务列表////////////////////////

			KMissionModuleExtension.getManager()
					.processUpdateMissionListWhileTriggerNewMissions(role,
							newAcceptableMissionTemplateList);
		}
		// ------------------处理客户端主菜单功能开放--------------------//
		KGuideManager.processOpenNewFunctionByRoleLevel(role);
		// ------------------主菜单功能是否需要二次引导--------------------//
		KGuideManager.checkAndSendSecondGuideFunctionByRoleLv(role);

		// -------------------处理关卡开放状态更新-----------------------//
		KSupportFactory.getLevelSupport()
				.checkAndUpdateGameLevelOpenStateWhileRoleLvUp(role);
	}
	
	@Override
	public void notifyRoleDataPutToCache(KRole role) {
		
	}

}
