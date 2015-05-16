package com.kola.kmp.logic.mission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.logging.KGameLogger;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.level.KGameLevelManager;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.level.KGameLevelSet;
import com.kola.kmp.logic.map.KGameNormalMap;
import com.kola.kmp.logic.map.KMapModule;
import com.kola.kmp.logic.mission.IMissionMenuImpl.IMissionConversationImpl;
import com.kola.kmp.logic.mission.MissionCompleteCondition.CollectItemTask;
import com.kola.kmp.logic.mission.MissionDialog.Dialogue;
import com.kola.kmp.logic.mission.guide.KGuideManager;
import com.kola.kmp.logic.mission.guide.MainMenuFunction;
import com.kola.kmp.logic.npc.KNPCDataStructs.KNPCTemplate;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.npc.menu.IMissionMenu;
import com.kola.kmp.logic.npc.menu.KMenuService;
import com.kola.kmp.logic.other.KEquipmentTypeEnum;
import com.kola.kmp.logic.other.KFunctionTypeEnum;
import com.kola.kmp.logic.other.KGameMissionFunTypeEnum;
import com.kola.kmp.logic.other.KGameMissionStatusEnum;
import com.kola.kmp.logic.other.KGameMissionTemplateTypeEnum;
import com.kola.kmp.logic.other.KNoviceGuideStepEnum;
import com.kola.kmp.logic.other.KUseFunctionTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.MissionModuleSupport;
import com.kola.kmp.logic.util.tips.MissionTips;
import com.kola.kmp.protocol.mission.KMissionProtocol;

public class KMissionModuleSupportImpl implements MissionModuleSupport {
	private static final KGameLogger _LOGGER = KGameLogger
			.getLogger(KMissionModuleSupportImpl.class);

	@Override
	public List<IMissionMenu> getNpcMissionsCopy(KRole role,
			KNPCTemplate npcTemplate) {
		List<IMissionMenu> missionMenuList = new ArrayList<IMissionMenu>();

		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role
				.getId());

		// 处理可接任务菜单列表
		List<IMissionMenu> acceptableMissionMenuList = getAcceptableMissionMenu(
				role, npcTemplate, missionSet);

		if (acceptableMissionMenuList.size() > 0) {
			for (IMissionMenu menu : acceptableMissionMenuList) {
				missionMenuList.add(menu);
			}
		}

		// 处理已接任务菜单列表
		List<IMissionMenu> acceptedMissionMenuList = getAcceptedMissionMenu(
				role, npcTemplate, missionSet);

		if (acceptedMissionMenuList.size() > 0) {
			for (IMissionMenu menu : acceptedMissionMenuList) {
				missionMenuList.add(menu);
			}
		}

		// 修行任务对话菜单列表
		List<IMissionMenu> dailyMissionMenuList = getDailyMissionNpcMenu(role,
				npcTemplate, missionSet);
		if (dailyMissionMenuList.size() > 0) {
			for (IMissionMenu menu : dailyMissionMenuList) {
				missionMenuList.add(menu);
			}
		}

		return missionMenuList;
	}

	private List<IMissionMenu> getAcceptableMissionMenu(KRole role,
			KNPCTemplate npcTemplate, KMissionSet missionSet) {
		List<IMissionMenu> missionMenuList = new ArrayList<IMissionMenu>();
		// 处理可接任务菜单列表
		for (KMissionTemplate missionTemplate : missionSet
				.getAcceptableMissionTemplateMap().values()) {
			if (!missionTemplate.isMissionCanAccept(role)) {
				continue;
			}
			if (missionTemplate.acceptMissionNPCTemplate.templateId == npcTemplate.templateId) {
				IMissionMenuImpl menu = constructIMissionMenu(
						role,
						missionTemplate,
						missionTemplate
								.getMissionNameByStatusType(
										KGameMissionStatusEnum.MISSION_STATUS_TRYRECEIVE,
										role),
						KGameMissionStatusEnum.MISSION_STATUS_TRYRECEIVE.statusType,
						IMissionMenu.ACTION_AFTER_TALK_SUBMIT,
						missionTemplate.getPrologueMissionDialog(),
						missionTemplate.getAcceptMissionDialog(role));

				// Dialogue dialogue = missionTemplate.acceptMissionTipsDialog
				// .getDialogueList().get(0);
				// menu.addMissionConversation(new IMissionConversationImpl(
				// dialogue.getContent(role.getJob()), dialogue
				// .getQuestion()));
				missionMenuList.add(menu);
				// _LOGGER.debug("$$$$$$$$$$$$  add missionMenu:"
				// + menu.getMissionName());
			}
		}

		return missionMenuList;
	}

	private List<IMissionMenu> getAcceptedMissionMenu(KRole role,
			KNPCTemplate npcTemplate, KMissionSet missionSet) {
		List<IMissionMenu> missionMenuList = new ArrayList<IMissionMenu>();
		for (KMission mission : missionSet.getAllUnclosedMission()) {
			KMissionTemplate missionTemplate = mission.getMissionTemplate();

			if (mission.getMissionStatus() == KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH) {
				if ((npcTemplate.templateId == missionTemplate.submitMissionNPCTemplate.templateId)
						&& mission.getMissionTemplate().missionFunType != KGameMissionFunTypeEnum.MISSION_FUN_TYPE_DIALOG) {
					byte clickEvent = IMissionMenu.ACTION_AFTER_TALK_CLOSE;
					// 为直接战斗任务修改，未完成任务的对话要进入战斗
					if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_BATTLEFIELD) {
						clickEvent = IMissionMenu.ACTION_AFTER_TALK_SUBMIT;
					}
					IMissionMenuImpl menu = constructIMissionMenu(
							role,
							missionTemplate,
							missionTemplate.getMissionNameByStatusType(
									mission.getMissionStatus(), role),
							mission.getMissionStatus().statusType, clickEvent,
							missionTemplate.getUncompletedMissionDialog());

					missionMenuList.add(menu);
				}
			} else if (mission.getMissionStatus() == KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT) {
				// 如果为对话类型任务，则判断是否有未完成对话在接受任务NPC身上
				if (npcTemplate.templateId == missionTemplate.acceptMissionNPCTemplate.templateId
						&& missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_DIALOG
						&& missionTemplate.getUncompletedMissionDialog() != null) {
					// 对话类型的未完成对话菜单，不用显示NPC头顶图标，设为0
					byte missionStatus;
					if (missionTemplate.acceptMissionNPCTemplate.templateId == missionTemplate.submitMissionNPCTemplate.templateId) {
						missionStatus = mission.getMissionStatus().statusType;
					} else {
						missionStatus = 0;
					}
					byte clickEvent = IMissionMenu.ACTION_AFTER_TALK_CLOSE;
					if (missionTemplate.acceptMissionNPCTemplate.templateId == missionTemplate.submitMissionNPCTemplate.templateId) {
						clickEvent = IMissionMenu.ACTION_AFTER_TALK_SUBMIT;
					}
					IMissionMenuImpl menu = constructIMissionMenu(
							role,
							missionTemplate,
							missionTemplate.getMissionNameByStatusType(
									mission.getMissionStatus(), role),
							missionStatus, clickEvent,
							missionTemplate.getUncompletedMissionDialog());
					missionMenuList.add(menu);

				} else if ((npcTemplate.templateId == missionTemplate.submitMissionNPCTemplate.templateId)) {

					IMissionMenuImpl menu = constructIMissionMenu(
							role,
							missionTemplate,
							missionTemplate.getMissionNameByStatusType(
									mission.getMissionStatus(), role),
							mission.getMissionStatus().statusType,
							IMissionMenu.ACTION_AFTER_TALK_SUBMIT,
							missionTemplate.getCompletedMissionDialog());

					missionMenuList.add(menu);
				}
			}

		}

		return missionMenuList;
	}

	private List<IMissionMenu> getDailyMissionNpcMenu(KRole role,
			KNPCTemplate npcTemplate, KMissionSet missionSet) {
		List<IMissionMenu> missionMenuList = new ArrayList<IMissionMenu>();

		if (missionSet != null && missionSet.getDailyMissionMap() != null) {
			for (KMission mission : missionSet.getDailyMissionMap().values()) {
				if (mission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_DIALOG
						&& mission.getMissionStatus() == KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH
						&& mission.getMissionTemplate().submitMissionNPCTemplate.templateId == npcTemplate.templateId) {

					IMissionMenuImpl menu = constructIMissionMenu(
							role,
							mission.getMissionTemplate(),
							mission.getMissionTemplate()
									.getMissionTypeNameText(
											mission.getMissionTemplate().missionType)
									+ mission.getMissionTemplate().missionExtName,
							KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT.statusType,
							IMissionMenu.ACTION_AFTER_TALK_SUBMIT,
							mission.getMissionTemplate().getCompletedMissionDialog());

					missionMenuList.add(menu);
				}
			}
		}

		return missionMenuList;
	}

	public static IMissionMenuImpl constructIMissionMenu(KRole role,
			KMissionTemplate missionTemplate, String missionName,
			byte missionStatus, byte clickEvent, MissionDialog... dialog) {
		IMissionMenuImpl menu = new IMissionMenuImpl();
		menu.setMainMission(missionTemplate.isMainLineMission);
		menu.setMissionName(missionName);
		menu.setMissionStatus(missionStatus);
		menu.setMissionTemplateId(missionTemplate.missionTemplateId);
		menu.setActionAfterTalk(clickEvent);
		// 任务开场对话菜单处理
		if (dialog != null && dialog.length > 0) {
			for (int i = 0; i < dialog.length; i++) {
				if (dialog[i] != null) {
					for (Dialogue dialogue : dialog[i].getDialogueList()) {
						menu.addMissionConversation(new IMissionConversationImpl(
								dialogue.getContent(role.getJob()), dialogue
										.getQuestion()));
					}
				}
			}
		}
		boolean isShowMissionReward = false;
		if (clickEvent == IMissionMenu.ACTION_AFTER_TALK_SUBMIT
				&& missionTemplate.missionType != KGameMissionTemplateTypeEnum.MISSION_TYPE_DAILY
				&& missionTemplate.missionReward != null) {
			isShowMissionReward = true;
		}
		menu.setShowMissionReward(isShowMissionReward);
		if (isShowMissionReward) {
			menu.setMissionReward(missionTemplate.missionReward
					.getBaseRewardData(role.getJob()));
		}
		return menu;
	}

	@Override
	public void nofityForRoleSelectedMission(KRole role, int missionTempId) {
		if (KMissionModuleExtension.getManager().isMissionTemplateExist(
				missionTempId)) {
			processNormalMission(role, missionTempId);
			return;
		}
		if (KMissionModuleExtension.getManager().getDailyMissionManager()
				.isMissionTemplateExist(missionTempId)) {
			processDailyMission(role, missionTempId);
		}
	}

	private void processNormalMission(KRole role, int missionTemplateId) {
		// System.out.println("####### 角色：" + role.getName() + "提交任务Id："
		// + missionTemplateId);

		KMissionTemplate missionTemplate = KMissionModuleExtension.getManager()
				.getMissionTemplate(missionTemplateId);
		if (missionTemplate == null) {
			return;
		}
		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role
				.getId());
		if (missionSet == null) {
			return;
		}
		// 判断是否任务模版Id存在角色任务容器的可接任务表中
		if (missionSet.checkMissionCanAccepted(role, missionTemplateId)) {
			// 如果该任务可接，则执行任务管理器的playerRoleAcceptMission()方法接受该任务
			KMission mission = KMissionModuleExtension.getManager()
					.playerRoleAcceptMission(role, missionTemplateId);

			KGameNormalMap currentMap = KMapModule.getGameMapManager()
					.getGameMap(role.getRoleMapData().getCurrentMapId());

			if (mission != null) {

				// 判断当前任务的新状态，更新NPC菜单
				if (mission.getMissionStatus() == KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH) {
					// 通知客户端显示接受任务特效
					KMissionModuleExtension
							.getManager()
							.processMissionStatusChangeEffect(role,
									missionTemplate,
									KMissionTemplate.MISSION_EFFECT_TYPE_ACCEPT);

					// /////////////////处理提交任务的NPC菜单///////////////////////

					if (currentMap != null
							|| currentMap
									.isNpcEntityInMap(missionTemplate.submitMissionNPCTemplate.templateId)) {

						KNPCTemplate submitNPCTemplate = mission
								.getMissionTemplate().submitMissionNPCTemplate;
						byte clickEvent = IMissionMenu.ACTION_AFTER_TALK_CLOSE;
						// 为直接战斗任务修改，未完成任务的对话要进入战斗
						if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_BATTLEFIELD
								|| missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_QUESTION) {
							clickEvent = IMissionMenu.ACTION_AFTER_TALK_SUBMIT;
						}

						IMissionMenuImpl menu = constructIMissionMenu(role,
								missionTemplate,
								missionTemplate.getMissionNameByStatusType(
										mission.getMissionStatus(), role),
								mission.getMissionStatus().statusType,
								clickEvent,
								missionTemplate.getUncompletedMissionDialog());
						List<IMissionMenu> menuList = new ArrayList<IMissionMenu>();
						menuList.add(menu);
						KMenuService.synNpcAddOrUpdateMenus(role,
								submitNPCTemplate.templateId, menuList);

					}

				} else if (mission.getMissionStatus() == KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT) {
					// 通知客户端显示任务条件达成特效
					KMissionModuleExtension
							.getManager()
							.processMissionStatusChangeEffect(
									role,
									mission.getMissionTemplate(),
									KMissionTemplate.MISSION_EFFECT_TYPE_CONDITION_FINISHED);

					// 接受任务成功，先删除原来的菜单项
					if (currentMap != null
							|| currentMap
									.isNpcEntityInMap(missionTemplate.acceptMissionNPCTemplate.templateId)) {
						int acceptMissionNPCTemplateId = missionTemplate.acceptMissionNPCTemplate.templateId;

						KMenuService.synNpcDeleteMenus(role,
								acceptMissionNPCTemplateId, missionTemplateId);

						// /////////////////处理未完成任务的NPC菜单///////////////////////
						// 如果为对话类型任务，则判断是否有未完成对话在接受任务NPC身上
						if (mission.getMissionTemplate().getUncompletedMissionDialog() != null
								&& mission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_DIALOG
								&& mission.getMissionTemplate().acceptMissionNPCTemplate.templateId != mission
										.getMissionTemplate().submitMissionNPCTemplate.templateId) {
							KNPCTemplate acceptNPCTemplate = mission
									.getMissionTemplate().acceptMissionNPCTemplate;

							IMissionMenuImpl menu = constructIMissionMenu(role,
									missionTemplate,
									missionTemplate.getMissionNameByStatusType(
											mission.getMissionStatus(), role),
									(byte) 0,
									IMissionMenu.ACTION_AFTER_TALK_CLOSE,
									missionTemplate.getUncompletedMissionDialog());
							List<IMissionMenu> menuList = new ArrayList<IMissionMenu>();
							menuList.add(menu);
							KMenuService.synNpcAddOrUpdateMenus(role,
									acceptNPCTemplate.templateId, menuList);
						}
					}

					// /////////////////处理提交任务的NPC菜单///////////////////////
					KNPCTemplate submitNPCTemplate = mission
							.getMissionTemplate().submitMissionNPCTemplate;

					if (currentMap != null
							|| currentMap
									.isNpcEntityInMap(missionTemplate.submitMissionNPCTemplate.templateId)) {

						IMissionMenuImpl menu = constructIMissionMenu(role,
								missionTemplate,
								missionTemplate.getMissionNameByStatusType(
										mission.getMissionStatus(), role),
								mission.getMissionStatus().statusType,
								IMissionMenu.ACTION_AFTER_TALK_SUBMIT,
								missionTemplate.getCompletedMissionDialog());
						List<IMissionMenu> menuList = new ArrayList<IMissionMenu>();
						menuList.add(menu);

						KMenuService.synNpcAddOrUpdateMenus(role,
								submitNPCTemplate.templateId, menuList);
					}
				}

				// 更新任务列表
				KMissionModuleExtension.getManager()
						.processUpdateMissionListWhileAccepteNewMission(role,
								mission);

				// 通知所有任务事件监听器，任务完成并接受成功
				List<KMissionEventListener> listenerList = KMissionModuleExtension
						.getManager().getAllMissionEventListener();
				for (KMissionEventListener listener : listenerList) {
					listener.notifyMissionAccepted(role, missionTemplate);
				}

				if (mission.getMissionStatus() == KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH) {
					// TODO 如果任务功能类型为直接进入战场类型，则通知战斗监听器进入任务对应的目标战场
					// if (missionTemplate.getMissionFunType() ==
					// KGameMissionFunTypeEnum.MISSION_FUN_BATTLEFIELD) {
					// processMissionJoinBattlefield(role, missionTemplate);
					// } else if (missionTemplate.getMissionFunType() ==
					// KGameMissionFunTypeEnum.MISSION_FUN_TYPE_QUESTION) {
					// KGameMissionModule.getManager()
					// .processQuestionMissionDialog(role,
					// missionTemplateId);
					// }
				}

				// if (missionTemplateId == 1) {
				// KMissionCompleteRecordSet set = KMissionModuleExtension
				// .getMissionCompleteRecordSet(role.getId());
				// if (set != null) {
				// set.completeNoviceGuideStep(KNoviceGuideStepEnum.接受新手任务引导.type);
				// }
				// }
			}

			KDialogService.sendNullDialog(role);

			return;
		} else if (missionSet.checkMissionIsUncompleted(missionTemplateId)) {
			/**
			 * 特殊处理
			 */
			// if (missionTemplateId == 1) {
			// // 如果是新手引导任务，记录步骤
			// KMissionCompleteRecordSet missionCompleteSet =
			// KMissionModuleExtension
			// .getMissionCompleteRecordSet(role.getId());
			// if (missionCompleteSet != null) {
			// missionCompleteSet
			// .completeNoviceGuideStep(KNoviceGuideStepEnum.完成新手任务引导.type);
			// }
			//
			// KGameLevelSet set = KGameLevelModuleExtension
			// .getGameLevelSet(role.getId());
			// int levelId = KGameLevelModuleExtension.getManager()
			// .getNoviceGuideGameLevel().getLevelId();
			// if (set != null) {
			// if (!set.checkGameLevelIsCompleted(levelId)) {
			// sendNotifyNoviceGuideBattleMsg(role);
			// KDialogService.sendNullDialog(role);
			// return;
			// }
			// }
			// }

			// 执行任务模块管理器中的提交任务方法
			int submitResult = KMissionModuleExtension.getManager()
					.playerRoleSubmitMission(role, missionTemplateId);

			KGameNormalMap currentMap = KMapModule.getGameMapManager()
					.getGameMap(role.getRoleMapData().getCurrentMapId());

			if (submitResult == KMissionManager.SUBMIT_MISSION_RESULT_SUCCESS) {
				// 发送任务奖励提示
				KMissionModuleExtension
						.getManager()
						.processMissionStatusChangeEffect(
								role,
								missionTemplate,
								missionTemplate.MISSION_EFFECT_TYPE_SUBMIT_SUCCEED);

				// TODO 处理完成任务奖励宠物
				// KMissionModuleExtension.getManager().addPetWhileMissionCompleted(
				// role, missionTemplateId);

				// 提交任务成功，先删除原来的菜单项
				int submitMissionNPCTemplateId = missionTemplate.submitMissionNPCTemplate.templateId;
				KMenuService.synNpcDeleteMenus(role,
						submitMissionNPCTemplateId, missionTemplateId);
				if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_DIALOG
						&& missionTemplate.getUncompletedMissionDialog() != null) {
					if (currentMap != null
							|| currentMap
									.isNpcEntityInMap(missionTemplate.acceptMissionNPCTemplate.templateId)) {
						KMenuService
								.synNpcDeleteMenus(
										role,
										missionTemplate.acceptMissionNPCTemplate.templateId,
										missionTemplateId);
					}
				}

				// 查找是否有新的可以接任务，并通知更新NPC菜单
				List<KMissionTemplate> newAcceptableMissionTemplateList = KMissionModuleExtension
						.getManager()
						.getNewAcceptableMissionTemplateWhileMissionCompleted(
								role, missionTemplateId);
				if (newAcceptableMissionTemplateList != null
						&& newAcceptableMissionTemplateList.size() > 0) {
					Map<Integer, List<IMissionMenu>> menuListMap = new HashMap<Integer, List<IMissionMenu>>();
					for (KMissionTemplate newAcceptableMissionTemplate : newAcceptableMissionTemplateList) {
						// 如果角色等级未达到开放当前可接任务的等级限制，跳过
						if (!newAcceptableMissionTemplate
								.isMissionCanAccept(role)) {
							continue;
						}

						int acceptMissionNpcId = newAcceptableMissionTemplate.acceptMissionNPCTemplate.templateId;
						if (currentMap == null
								|| !currentMap
										.isNpcEntityInMap(acceptMissionNpcId)) {
							continue;
						}
						if (!menuListMap.containsKey(acceptMissionNpcId)) {
							menuListMap.put(acceptMissionNpcId,
									new ArrayList<IMissionMenu>());
						}
						IMissionMenuImpl menu = constructIMissionMenu(
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
						// menu.addMissionConversation(new
						// IMissionConversationImpl(
						// dialogue.getContent(role.getJob()), dialogue
						// .getQuestion()));
						menuListMap.get(acceptMissionNpcId).add(menu);
					}
					_LOGGER.debug("&&&&&%%%%%%%%%%&&&&  角色完成任务：missionTemplateId："
							+ missionTemplateId
							+ ",更新任务数量:"
							+ newAcceptableMissionTemplateList.size()
							+ ","
							+ "更新NPC数量：" + menuListMap.size());
					if (menuListMap.size() > 0) {
						for (Integer addMenuNpcId : menuListMap.keySet()) {
							List<IMissionMenu> missionMenuList = menuListMap
									.get(addMenuNpcId);
							if (missionMenuList != null
									&& missionMenuList.size() > 0) {

								KMenuService.synNpcAddOrUpdateMenus(role,
										addMenuNpcId, missionMenuList);
							}
						}

					}
				}
				// 如果是收集道具任务，扣除道具
				if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_COLLECT_ITEMS) {
					CollectItemTask task = missionTemplate.missionCompleteCondition
							.getCollectItemTask();
					int decreseItemCount = task.collectCount;
					KItemTempAbs itemTemplate;
					if (task.isLimitJob) {
						itemTemplate = task.itemTemplateMap.get(role.getJob());
					} else {
						itemTemplate = task.itemTemplate;
					}
					boolean removeResult = KSupportFactory
							.getItemModuleSupport().removeItemFromBag(
									role.getId(), itemTemplate.itemCode,
									decreseItemCount);
				}

				// 更新任务列表
				KMissionModuleExtension.getManager()
						.processUpdateMissionListWhileCompleteMission(role,
								missionTemplate,
								newAcceptableMissionTemplateList);
				// 通知所有任务事件监听器，任务完成并提交成功
				List<KMissionEventListener> listenerList = KMissionModuleExtension
						.getManager().getAllMissionEventListener();
				for (KMissionEventListener listener : listenerList) {
					listener.notifyMissionCompleted(role, missionTemplate);
				}

				// 判断任务完成时是否有功能开放
				KGuideManager.processOpenNewFunctionByMissionCompleted(role,
						missionTemplateId);

				// 检测是否触发功能二次引导
				KGuideManager.checkAndSendSecondGuideFunctionByMission(role,
						missionTemplateId);

				// TODO 检测世界地图主城开放是否需要更新
				// KMapModule.getGameMapManager()
				// .checkAndUpdateWorldMapDataByMissionTemplateId(role,
				// missionTemplateId);

				// TODO 处理检测任务完成时是否有礼包引导
				// KGameMissionModule
				// .getManager()
				// .getNoviceGuideManager()
				// .checkAndSendGiftsGuideTipsByMissionComplete(role,
				// missionTemplateId);

			} else if (submitResult == KMissionManager.SUBMIT_MISSION_RESULT_FAILD) {
				// TODO 提交任务失败()，判断是否为直接战斗任务，如果是则重新进入对应战场
				// if (missionTemplate.getMissionFunType() ==
				// KGameMissionFunTypeEnum.MISSION_FUN_BATTLEFIELD) {
				// processMissionJoinBattlefield(role, missionTemplate);
				// } else if (missionTemplate.getMissionFunType() ==
				// KGameMissionFunTypeEnum.MISSION_FUN_TYPE_QUESTION) {
				// KGameMissionModule.getManager()
				// .processQuestionMissionDialog(role,
				// missionTemplateId);
				// }
			}

			KDialogService.sendNullDialog(role);
			_LOGGER.debug("%%%%%%%%%%%%%###### 角色：" + role.getName() + " 提交任务："
					+ missionTemplateId);
			return;
		}
	}

	private void processDailyMission(KRole role, int missionTemplateId) {
		// 处理任务
		KMissionSet container = KMissionModuleExtension.getMissionSet(role
				.getId());

		KMission mission = container.getDailyMissionMap()
				.get(missionTemplateId);

		// 检测该任务是否为已完成并可提交
		if (mission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_DIALOG) {
			if (mission.checkMissionCanSubmitAndChangeMissionStatus(role
					.getJob())) {
				int submitMissionNPCTemplateId = mission.getMissionTemplate().submitMissionNPCTemplate.templateId;
				// 删除对话NPC菜单
				KMenuService.synNpcDeleteMenus(role,
						submitMissionNPCTemplateId, missionTemplateId);
				// KDialogService.sendUprisingDialog(role,
				// "修行任务【" + mission.getMissionTemplate().missionName
				// + "】达成！");
				KDialogService.sendUprisingDialog(role, MissionTips
						.getTipsDailyMissionCompleteTips1(mission
								.getMissionTemplate().missionName));
				KMissionModuleExtension.getManager().getDailyMissionManager()
						.dailyMissionConditionReached(role, mission);
			}
		}

	}

	@Override
	public boolean checkMissionIsAcceptedOrCompleted(KRole role,
			int missionTemplateId) {
		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role
				.getId());
		KMissionCompleteRecordSet completeRecordSet = KMissionModuleExtension
				.getMissionCompleteRecordSet(role.getId());

		if (completeRecordSet != null
				&& completeRecordSet.checkMissionIsCompleted(missionTemplateId)) {
			return true;
		}

		if (missionSet != null
				&& missionSet.checkMissionIsUncompleted(missionTemplateId)) {
			return true;
		}

		return false;
	}

	@Override
	public void notifyUseFunction(KRole role, KUseFunctionTypeEnum funType) {
		notifyUseFunctionByCounts(role, funType, 1);
	}

	@Override
	public void notifyUseFunction(long roleId, KUseFunctionTypeEnum funType) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);

		notifyUseFunctionByCounts(role, funType, 1);
	}

	@Override
	public void notifyUseFunctionByCounts(KRole role,
			KUseFunctionTypeEnum funType, int count) {

		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role
				.getId());

		List<KMission> allUnclosedMission = missionSet.getAllUnclosedMission();
		KGameNormalMap currentMap = KMapModule.getGameMapManager().getGameMap(
				role.getRoleMapData().getCurrentMapId());
		for (KMission unclosedMission : allUnclosedMission) {
			if (unclosedMission.getMissionTemplate() != null
					&& unclosedMission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_FUNCTION
					&& unclosedMission.getMissionTemplate().missionCompleteCondition
							.getUseFunctionTask().functionId == funType.functionId
					&& !unclosedMission.getMissionTemplate().isNewPlayerGuildMission) {
				KMissionTemplate missionTemplate = unclosedMission
						.getMissionTemplate();
				// if (!KGameMissionModule.getManager().getNoviceGuideManager()
				// .getNoviceGuideStepMapByMissionTemplateId()
				// .containsKey(missionTemplate.missionTemplateId)) {
				boolean checkCanSubmit = false;
				// 检测该任务是否为已完成并可提交

				checkCanSubmit = unclosedMission
						.notifyUseFunctionMissionAndChangeMissionStatus(
								funType.functionId, count);

				if (checkCanSubmit) {

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
											unclosedMission.getMissionStatus().statusType,
											IMissionMenu.ACTION_AFTER_TALK_SUBMIT,
											missionTemplate.getCompletedMissionDialog());
							List<IMissionMenu> menuList = new ArrayList<IMissionMenu>();
							menuList.add(menu);
							KMenuService.synNpcAddOrUpdateMenus(role,
									submitMissionNPCTemplateId, menuList);
						}
					}

				} else {
					// 更新任务列表
					KMissionModuleExtension
							.getManager()
							.processUpdateMissionListWhileAcceptedMissionStatusChanged(
									role, unclosedMission);
				}
				// }
			}
		}
		// 检测日常任务
		if (missionSet.getDailyMissionInfo() != null
				&& missionSet.getDailyMissionInfo()
						.getRestFreeCompletedMissionCount() > 0) {
			for (KMission mission : missionSet.getDailyMissionMap().values()) {
				if (mission.getMissionTemplate() != null
						&& mission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_FUNCTION
						&& mission.getMissionTemplate().missionCompleteCondition
								.getUseFunctionTask().functionId == funType.functionId
						&& mission.getMissionStatus() == KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH) {
					boolean checkCanSubmit = false;
					// 检测该任务是否为已完成并可提交

					checkCanSubmit = mission
							.notifyUseFunctionMissionAndChangeMissionStatus(
									funType.functionId, count);

					if (checkCanSubmit) {
						KDialogService.sendUprisingDialog(role, MissionTips
								.getTipsDailyMissionCompleteTips(mission
										.getMissionTemplate().missionName));
					} else {

						// 提示任务进度
						KDialogService
								.sendUprisingDialog(
										role,
										KMissionModuleExtension
												.getManager()
												.getDailyMissionManager()
												.getMissionListTips(role,
														mission, true));
					}
					KMissionModuleExtension.getManager()
							.getDailyMissionManager()
							.dailyMissionConditionReached(role, mission);
				}
			}
		}
	}

	@Override
	public void notifyUseFunctionByCounts(long roleId,
			KUseFunctionTypeEnum funType, int count) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);

		notifyUseFunctionByCounts(role, funType, count);
	}

	@Override
	public void notifyUseItemFunction(long roleId, KItemTempAbs template,
			int count, KUseFunctionTypeEnum funType) {

	}

	@Override
	public void notifyRoleSkillLv(long roleId, int newLv) {

	}

	@Override
	public void notifyStrongEquip(long roleId, KEquipmentTypeEnum equipType,
			int newLv) {

	}

	@Override
	public void notifyUpgrageEquipLv(long roleId, KEquipmentTypeEnum equipType,
			int newLv) {

	}

	@Override
	public void notifyVipLevelUp(long roleId, int preLv, int newLv) {

	}

	private void sendNotifyNoviceGuideBattleMsg(KRole role) {
		KGameMessage sendMsg = KGame
				.newLogicMessage(KMissionProtocol.SM_NOTIFY_NOVICE_GUIDE_BATTLE);
		role.sendMsg(sendMsg);
	}

	@Override
	public void notifyCompleteNoviceGuideBattlefield(KRole role) {

	}

	@Override
	public List<Short> getOpenFuncIds(int preLv, int nowLv) {
		List<Short> funcIdList = new ArrayList<Short>();
		for (MainMenuFunction info : KGuideManager.getMainMenuFunctionInfoMap()
				.values()) {
			if (info.getFunctionOpenType() == MainMenuFunction.FUNCTION_OPEN_TYPE_ROLE_LEVEL) {
				if (info.getOpenRoleLevelLimit() > preLv
						&& info.getOpenRoleLevelLimit() <= nowLv) {
					funcIdList.add(info.getFunctionId());
				}
			}
		}
		return funcIdList;
	}

	@Override
	public boolean checkFunctionIsOpen(KRole role, KFunctionTypeEnum funType) {
		return KGuideManager.checkFunctionIsOpen(role, funType.functionId);
	}

}
