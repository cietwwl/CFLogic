package com.kola.kmp.logic.mission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.activity.goldact.KBarrelBattlefield;
import com.kola.kmp.logic.activity.goldact.KBarrelBattlefield.KBarrelBattleData;
import com.kola.kmp.logic.level.FightEventListener;
import com.kola.kmp.logic.level.FightResult;
import com.kola.kmp.logic.level.ICombatAdditionalReward;
import com.kola.kmp.logic.level.KGameBattlefield;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.level.KLevelTemplate;
import com.kola.kmp.logic.level.KBattleObjectDataStruct.BornPointData;
import com.kola.kmp.logic.level.KBattleObjectDataStruct.MonsterData;
import com.kola.kmp.logic.level.gamestory.Animation;
import com.kola.kmp.logic.level.petcopy.KPetCopyBattlefield;
import com.kola.kmp.logic.level.tower.KTowerBattlefield;
import com.kola.kmp.logic.map.KGameNormalMap;
import com.kola.kmp.logic.map.KMapModule;
import com.kola.kmp.logic.npc.KNPCDataStructs.KMonstTemplate;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.npc.menu.IMissionMenu;
import com.kola.kmp.logic.npc.menu.KMenuService;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KGameBattlefieldTypeEnum;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.other.KGameMissionFunTypeEnum;
import com.kola.kmp.logic.other.KGameMissionStatusEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.MissionTips;

public class KMissionFightEventListenerImpl implements FightEventListener {

	@Override
	public void notifyBattle(KRole role,
			List<KGameBattlefield> battlefieldList,
			List<Animation> animationList) {
		// if (!battlefieldList.isEmpty()) {
		// KGameBattlefield battle = battlefieldList.get(battlefieldList
		// .size() - 1);
		// FightResult result = new FightResult();
		// boolean isWin = true;//UtilTool.randomNextBoolean();
		// result.setWin(isWin);
		// result.setBattlefieldId(battle.getBattlefieldId());
		// result.setBattlefieldType(battle.getBattlefieldType());
		// if (isWin) {
		// result.setBattleTime(UtilTool.random(50000, 140000));
		// result.setMaxDoubleHitCount(UtilTool.random(2, 50));
		// result.setMaxBeHitCount(UtilTool.random(0, 100));
		// result.setEndType(FightResult.FIGHT_END_TYPE_NORMAL);
		// result.setKillMonsterCount(new HashMap<Integer, Short>());
		// HashMap<Integer, AtomicInteger> killMap = new HashMap<Integer,
		// AtomicInteger>();
		//
		// for (MonsterData data:battle.monsterMap.values()) {
		// int monTemplateId = data._monsterTemplate.id;
		// if(!killMap.containsKey(monTemplateId)){
		// killMap.put(monTemplateId, new AtomicInteger(1));
		// }else{
		// killMap.get(monTemplateId).incrementAndGet();
		// }
		// }
		// for (Integer monTempId:killMap.keySet()) {
		// short count = (short)(killMap.get(monTempId).get());
		// result.getKillMonsterCount().put(monTempId, count);
		// }
		// result.setTotalDamage(UtilTool.random(15000, 30000));
		//
		// IBattleAdditionalRewardImpl reward = new
		// IBattleAdditionalRewardImpl();
		// reward.setRoleId(role.getId());
		// reward.getAdditionalCurrencyReward().put(KCurrencyTypeEnum.GOLD,
		// UtilTool.random(1000, 3000));
		//
		// reward.getAdditionalItemReward().put("310004", 2);
		// reward.getAdditionalItemReward().put("310005", 1);
		//
		// result.setBattleReward(reward);
		// }
		//
		// KGameLevelModuleExtension.getManager()
		// .processPlayerRoleCompleteBattlefield(role, result);
		// }
	}

	@Override
	public void notifyBattleFinished(KRole role, FightResult result) {
		// 处理任务
		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role
				.getId());

		List<KMission> allUnclosedMission = missionSet.getAllUnclosedMission();

		for (KMission unclosedMission : allUnclosedMission) {
			if (unclosedMission.getMissionTemplate() != null) {
				// 定义该未完成任务的数据状态是否发生改变的标志位
				boolean isMissionDataChange = false;
				// 如果任务类型为杀怪任务，则判断普通关卡战场类型的杀怪数量是否满足条件
				if (unclosedMission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_KILL_MONSTER
						&& result.getKillMonsterCount() != null
						&& result.getBattlefieldType() == KGameBattlefieldTypeEnum.普通关卡战场) {

					for (Integer monsterTemplateId : result
							.getKillMonsterCount().keySet()) {
						KMonstTemplate monsterTemplate = KSupportFactory
								.getNpcModuleSupport().getMonstTemplate(
										monsterTemplateId);
						if (monsterTemplate != null) {
							int killCount = result.getKillMonsterCount().get(
									monsterTemplateId);
							// 通知任务杀怪数量，如果杀怪数量发生改变，则设置isMissionDataChange为true
							if (unclosedMission.notifyKillMonsterMission(
									monsterTemplate, killCount)) {
								isMissionDataChange = true;
							}
						}
					}
				}// 判断是否直接战斗类型任务：（包括：护送、QTE、塔防战斗）
				else if (result.isWin()
						&& unclosedMission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_BATTLEFIELD
						&& unclosedMission.getMissionStatus() == KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH) {

				}

				// 如果任务数据发生改变，则进行如下处理
				if (isMissionDataChange) {
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
						// 先删除原来任务对应的NPC的菜单项
						int acceptMissionNPCTemplateId = missionTemplate.acceptMissionNPCTemplate.templateId;
						int submitMissionNPCTemplateId = missionTemplate.submitMissionNPCTemplate.templateId;
						if (acceptMissionNPCTemplateId == submitMissionNPCTemplateId) {
							KGameNormalMap currentMap = KMapModule
									.getGameMapManager().getGameMap(
											role.getRoleMapData()
													.getCurrentMapId());
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
						KDialogService.sendUprisingDialog(
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
				}
			}
		}
		// 检测日常任务
		if (missionSet.getDailyMissionInfo() != null
				&& missionSet.getDailyMissionInfo()
						.getRestFreeCompletedMissionCount() > 0) {
			for (KMission mission : missionSet.getDailyMissionMap().values()) {
				if (mission.getMissionTemplate() != null) {
					// 定义该未完成任务的数据状态是否发生改变的标志位
					boolean isMissionDataChange = false;
					// 如果任务类型为杀怪任务，则判断普通关卡战场类型的杀怪数量是否满足条件
					if (mission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_KILL_MONSTER
							&& result.getKillMonsterCount() != null
							&& result.getBattlefieldType() == KGameBattlefieldTypeEnum.普通关卡战场) {

						for (Integer monsterTemplateId : result
								.getKillMonsterCount().keySet()) {
							KMonstTemplate monsterTemplate = KSupportFactory
									.getNpcModuleSupport().getMonstTemplate(
											monsterTemplateId);
							if (monsterTemplate != null) {
								int killCount = result.getKillMonsterCount()
										.get(monsterTemplateId);
								// 通知任务杀怪数量，如果杀怪数量发生改变，则设置isMissionDataChange为true
								if (mission.notifyKillMonsterMission(
										monsterTemplate, killCount)) {
									isMissionDataChange = true;
								}
							}
						}
					}
					if (isMissionDataChange) {
						// 检测该任务是否为已完成并可提交
						if (mission
								.checkMissionCanSubmitAndChangeMissionStatus(role.getJob())) {
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
						KMissionModuleExtension.getManager()
								.getDailyMissionManager()
								.dailyMissionConditionReached(role, mission);
						break;
					}
				}
			}
		}
	}

	@Override
	public void notifyGameLevelCompleted(KRole role, KLevelTemplate gamelevel,
			FightResult result) {
		// 处理关卡任务
		if (gamelevel.getLevelType() != KGameLevelTypeEnum.普通关卡) {
			return;
		}
		if (!result.isWin()) {
			return;
		}

		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role
				.getId());

		List<KMission> allUnclosedMission = missionSet.getAllUnclosedMission();
		KGameNormalMap currentMap = KMapModule.getGameMapManager().getGameMap(
				role.getRoleMapData().getCurrentMapId());
		for (KMission unclosedMission : allUnclosedMission) {
			if (unclosedMission.getMissionTemplate() != null
					&& unclosedMission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_GAME_LEVEL
					&& unclosedMission.getMissionStatus() != KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT) {
				if (unclosedMission.notifyGameLevelMission(gamelevel
						.getLevelId())) {
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
						// 先删除原来任务对应的NPC的菜单项
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
						KDialogService.sendUprisingDialog(
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
				}
			}
		}
		// 检测日常任务
		if (missionSet.getDailyMissionInfo() != null
				&& missionSet.getDailyMissionInfo()
						.getRestFreeCompletedMissionCount() > 0) {
			for (KMission mission : missionSet.getDailyMissionMap().values()) {
				if (mission.getMissionTemplate() != null
						&& mission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_GAME_LEVEL
						&& mission.getMissionStatus() != KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT) {
					// boolean isCheckSuccess = false;
					if (mission.notifyGameLevelMission(gamelevel.getLevelId())) {
						// isCheckSuccess = true;
						if (mission
								.checkMissionCanSubmitAndChangeMissionStatus(role.getJob())) {
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
						KMissionModuleExtension.getManager()
								.getDailyMissionManager()
								.dailyMissionConditionReached(role, mission);
						// if (isCheckSuccess) {
						// break;
						// }
					}
				}
			}
		}
	}

	@Override
	public void notifyBattleRewardFinished(KRole role) {

	}

	public static class IBattleAdditionalRewardImpl implements
			ICombatAdditionalReward {

		private long _roleId;
		private Map<KCurrencyTypeEnum, Integer> moneyMap = new HashMap<KCurrencyTypeEnum, Integer>();
		private Map<String, Integer> itemMap = new HashMap<String, Integer>();

		@Override
		public long getRoleId() {
			return _roleId;
		}

		public void setRoleId(long roleId) {
			this._roleId = roleId;
		}

		@Override
		public Map<KCurrencyTypeEnum, Integer> getAdditionalCurrencyReward() {
			return moneyMap;
		}

		@Override
		public Map<String, Integer> getAdditionalItemReward() {
			return itemMap;
		}

		@Override
		public void addCurrencyReward(KCurrencyTypeEnum type, int value) {

		}

		@Override
		public void addItemReward(String itemCode, int value) {

		}

		@Override
		public Map<Integer, Integer> getAdditionalPetReward() {
			return null;
		}

		@Override
		public void addAdditionalPetReward(int templateId, int count) {

		}

		@Override
		public void executeReward(KRole role) {

		}

	}

	@Override
	public void notifyFriendTowerBattle(KRole role, long friendRoleId,
			KTowerBattlefield battlefield) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyGoldActivityBattle(KRole role,
			KBarrelBattlefield battlefield) {
		// FightResult result = new FightResult();
		// boolean isWin = true;// UtilTool.randomNextBoolean();
		// result.setWin(isWin);
		// result.setBattlefieldId(battlefield.getBattlefieldId());
		// result.setBattlefieldType(battlefield.getBattlefieldType());
		// if (isWin) {
		// result.setBattleTime(UtilTool.random(50000, 140000));
		// result.setMaxDoubleHitCount(UtilTool.random(2, 50));
		// result.setMaxBeHitCount(0);
		// result.setEndType(FightResult.FIGHT_END_TYPE_NORMAL);
		// result.setKillMonsterCount(new HashMap<Integer, Short>());
		// HashMap<Integer, AtomicInteger> killMap = new HashMap<Integer,
		// AtomicInteger>();
		//
		// Map<BornPointData, List<KBarrelBattleData>> map =
		// battlefield.getBarrelBattleDatas(role.getLevel());
		// for (List<KBarrelBattleData> dataList : map.values()) {
		// for (KBarrelBattleData data:dataList) {
		// int monTemplateId = data.monstTemplate.id;
		// if (!killMap.containsKey(monTemplateId)) {
		// killMap.put(monTemplateId, new AtomicInteger(1));
		// } else {
		// killMap.get(monTemplateId).incrementAndGet();
		// }
		// }
		//
		// }
		// for (Integer monTempId : killMap.keySet()) {
		// short count = (short) (killMap.get(monTempId).get());
		// result.getKillMonsterCount().put(monTempId, count);
		// }
		// result.setTotalDamage(UtilTool.random(15000, 30000));
		//
		// IBattleAdditionalRewardImpl reward = new
		// IBattleAdditionalRewardImpl();
		// reward.setRoleId(role.getId());
		// reward.getAdditionalCurrencyReward().put(KCurrencyTypeEnum.GOLD,
		// UtilTool.random(1000, 3000));
		//
		//
		// reward.getAdditionalItemReward().put("370001", 2);
		// reward.getAdditionalItemReward().put("370002", 1);
		//
		// result.setBattleReward(reward);
		// }
		//
		// KGameLevelModuleExtension.getManager()
		// .processPlayerRoleCompleteBattlefield(role, result);

	}

	@Override
	public void notifyNewGoldActivityBattle(KRole role,
			KGameBattlefield battlefield, int glodBaseValue, long battleTime) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyPetCopyBattle(KRole role, KPetCopyBattlefield battlefield) {
		// TODO Auto-generated method stub
		
	}

}
