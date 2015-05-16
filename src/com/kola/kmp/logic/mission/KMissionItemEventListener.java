package com.kola.kmp.logic.mission;

import java.util.ArrayList;
import java.util.List;

import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.item.listener.KItemEventListener;
import com.kola.kmp.logic.map.KGameNormalMap;
import com.kola.kmp.logic.map.KMapModule;
import com.kola.kmp.logic.mission.MissionCompleteCondition.CollectItemTask;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.npc.menu.IMissionMenu;
import com.kola.kmp.logic.npc.menu.KMenuService;
import com.kola.kmp.logic.other.KGameMissionFunTypeEnum;
import com.kola.kmp.logic.other.KGameMissionStatusEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.MissionTips;

public class KMissionItemEventListener implements KItemEventListener {

	@Override
	public void notifyItemCountChangeInBag(long roleId,
			KItemTempAbs itemTemplate, long nowCount) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);

		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(roleId);

		List<KMission> allUnclosedMission = missionSet.getAllUnclosedMission();
		KGameNormalMap currentMap = KMapModule.getGameMapManager().getGameMap(
				role.getRoleMapData().getCurrentMapId());
		for (KMission unclosedMission : allUnclosedMission) {
			if (unclosedMission.getMissionTemplate() != null
					&& unclosedMission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_COLLECT_ITEMS
					&& unclosedMission.getMissionTemplate().missionCompleteCondition
							.getCollectItemTask().itemCodeSet.contains(
									itemTemplate.itemCode)) {
				if (unclosedMission.getMissionStatus() == KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH) {
					// 检测该任务是否为已完成并可提交
					if (unclosedMission
							.checkMissionCanSubmitAndChangeMissionStatus(role.getJob())) {
						KMissionTemplate missionTemplate = unclosedMission
								.getMissionTemplate();
						// 通知客户端显示任务条件达成特效
						KMissionModuleExtension
								.getManager()
								.processMissionStatusChangeEffect(
										role,
										missionTemplate,
										KMissionTemplate.MISSION_EFFECT_TYPE_CONDITION_FINISHED);

						// 更新任务列表
						KMissionModuleExtension
								.getManager()
								.processUpdateMissionListWhileAcceptedMissionStatusChanged(
										role, unclosedMission);

						// /////////更新NPC菜单//////////////////

						int acceptMissionNPCTemplateId = missionTemplate.acceptMissionNPCTemplate.templateId;
						int submitMissionNPCTemplateId = missionTemplate.submitMissionNPCTemplate.templateId;
						if (acceptMissionNPCTemplateId == submitMissionNPCTemplateId) {
							if (currentMap != null
									|| currentMap
											.isNpcEntityInMap(acceptMissionNPCTemplateId)) {
								KMenuService.synNpcDeleteMenus(role,
										submitMissionNPCTemplateId,
										missionTemplate.missionTemplateId);
								IMissionMenuImpl menu = KMissionModuleSupportImpl
										.constructIMissionMenu(
												role,
												missionTemplate,
												missionTemplate
														.getMissionNameByStatusType(
																unclosedMission
																		.getMissionStatus(),
																role),
												unclosedMission
														.getMissionStatus().statusType,
												IMissionMenu.ACTION_AFTER_TALK_SUBMIT,
												missionTemplate.getCompletedMissionDialog());
								List<IMissionMenu> menuList = new ArrayList<IMissionMenu>();
								menuList.add(menu);
								KMenuService.synNpcAddOrUpdateMenus(role,
										submitMissionNPCTemplateId, menuList);
							}
						}

					} else {
						// 提示任务进度
						KDialogService
								.sendUprisingDialog(
										role,
										KMissionModuleExtension.getManager()
												.getMissionProgressTips(role,
														unclosedMission));

						// 更新任务列表
						KMissionModuleExtension
								.getManager()
								.processUpdateMissionListWhileAcceptedMissionStatusChanged(
										role, unclosedMission);
					}
				} else if (unclosedMission.getMissionStatus() == KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT) {
					// 当任务处于可提交状态时，再次检查背包发现目标道具数量不足（可能玩家将道具从背包移除），
					// 则在检测方法checkMissionCanSubmitAndChangeMissionStatus()中会将任务自动设回MISSION_STATUS_TRYFINISH状态
					// 并且要将之前已更新的NPC菜单和任务列表设为未完成状态并通知客户端更新
					if (!unclosedMission
							.checkMissionCanSubmitAndChangeMissionStatus(role.getJob())) {
						KMissionTemplate missionTemplate = unclosedMission
								.getMissionTemplate();
						// 更新任务列表
						KMissionModuleExtension
								.getManager()
								.processUpdateMissionListWhileAcceptedMissionStatusChanged(
										role, unclosedMission);

						// /////////更新NPC菜单//////////////////

						int acceptMissionNPCTemplateId = missionTemplate.acceptMissionNPCTemplate.templateId;
						int submitMissionNPCTemplateId = missionTemplate.submitMissionNPCTemplate.templateId;
						if (acceptMissionNPCTemplateId == submitMissionNPCTemplateId) {
							if (currentMap != null
									|| currentMap
											.isNpcEntityInMap(acceptMissionNPCTemplateId)) {
								KMenuService.synNpcDeleteMenus(role,
										submitMissionNPCTemplateId,
										missionTemplate.missionTemplateId);
								IMissionMenuImpl menu = KMissionModuleSupportImpl
										.constructIMissionMenu(
												role,
												missionTemplate,
												missionTemplate
														.getMissionNameByStatusType(
																unclosedMission
																		.getMissionStatus(),
																role),
												unclosedMission
														.getMissionStatus().statusType,
												IMissionMenu.ACTION_AFTER_TALK_CLOSE,
												missionTemplate.getUncompletedMissionDialog());
								List<IMissionMenu> menuList = new ArrayList<IMissionMenu>();
								menuList.add(menu);
								KMenuService.synNpcAddOrUpdateMenus(role,
										submitMissionNPCTemplateId, menuList);
							}
						}
					}
				}
			}
		}

		// 检测日常任务
		for (KMission mission : missionSet.getDailyMissionMap().values()) {
			if (mission.getMissionTemplate() != null
					&& mission.getMissionStatus() != KGameMissionStatusEnum.MISSION_STATUS_TRYRECEIVE
					&& mission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_COLLECT_ITEMS
					&& mission.getMissionTemplate().missionCompleteCondition
							.getCollectItemTask().itemCodeSet.contains(
									itemTemplate.itemCode)) {
				// 检测该任务是否为已完成并可提交
				if (mission.checkMissionCanSubmitAndChangeMissionStatus(role.getJob())) {
					// KDialogService.sendUprisingDialog(role, "修行任务【"
					// + mission.getMissionTemplate().missionName
					// + "】达成！");
					KDialogService.sendUprisingDialog(role, MissionTips
							.getTipsDailyMissionCompleteTips(mission
									.getMissionTemplate().missionName));
				} else {
					// 提示任务进度
					KDialogService.sendUprisingDialog(role,
							KMissionModuleExtension.getManager()
									.getDailyMissionManager()
									.getMissionListTips(role,mission, true));
				}
				KMissionModuleExtension.getManager().getDailyMissionManager()
						.dailyMissionConditionReached(role, mission);
			}
		}
	}

}
