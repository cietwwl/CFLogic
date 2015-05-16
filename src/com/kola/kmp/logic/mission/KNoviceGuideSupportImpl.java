package com.kola.kmp.logic.mission;

import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.gameserver.KGameServer;
import com.kola.kmp.logic.combat.KCombatType;
import com.kola.kmp.logic.level.KGameBattlefield;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.level.KGameLevelSet;
import com.kola.kmp.logic.level.gamestory.Animation;
import com.kola.kmp.logic.level.gamestory.AnimationManager;
import com.kola.kmp.logic.map.KMapRoleEventListener;
import com.kola.kmp.logic.mission.guide.KGuideManager;
import com.kola.kmp.logic.other.KNoviceGuideStepEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.NoviceGuideSupport;
import com.kola.kmp.protocol.mission.KMissionProtocol;

public class KNoviceGuideSupportImpl implements NoviceGuideSupport {

	public static int second_weapon_guide_mission_id;
	public static int second_weapon_guide_level_id;
	public static int mount_guide_mission_id;
	public static int mount_guide_level_id;

	@Override
	public boolean checkRoleCompleteFirstNoviceGuideBattle(KRole role) {
		KMissionCompleteRecordSet set = KMissionModuleExtension.getMissionCompleteRecordSet(role.getId());
		if (set != null) {
			if (set.getNoviceGuideRecord().isCompleteFirstGuideBattle) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void notifyRoleEnterFirstNoviceGuideBattle(KRole role) {
		// 检测是否增加辅助宠物
		KSupportFactory.getPetModuleSupport().addNoviceGuideFightingPet(role);
		// 检测是否装备坐骑
		KSupportFactory.getMountModuleSupport().notifyStartMountForNewRole(role.getId());

		KGameMessage sendMsg = KGame.newLogicMessage(KMissionProtocol.SM_NOTIFY_ABOUT_ENTER_GUIDE_BATTLE);
		sendMsg.writeByte(1);
		role.sendMsg(sendMsg);
		
		role.fullEnergy(); // 新手引导战斗需求，满能量

		// 进入战斗
		KGameBattlefield battlefield = KSupportFactory.getLevelSupport().getNoviceGuideBattlefield();

		List<Animation> animationList = AnimationManager.getInstance().getNoviceGuideBattleTypeAnimations().get(battlefield.getLevelId());

		KSupportFactory.getCombatModuleSupport().fightInBattlefield(role, battlefield, animationList, KCombatType.GAME_LEVEL, null);
	}

	@Override
	public void notifyRoleCompleteFirstNoviceGuideBattle(KRole role) {
		KMissionCompleteRecordSet completeRecordSet = KMissionModuleExtension.getMissionCompleteRecordSet(role.getId());
		if (completeRecordSet != null) {
			completeRecordSet.finishFirstNoviceGuideBattle();
		}
	}

	@Override
	public void notifyPlayNoviceGuideAnimation(KRole role) {
		KGameMessage sendMsg = KGame.newLogicMessage(KMissionProtocol.SM_SEND_FIRST_ANIMATION);
		role.sendMsg(sendMsg);
	}

	// @Override
	// public void processNoviceGuide1(KRole role) {
	// KMissionCompleteRecordSet completeSet = KMissionModuleExtension
	// .getMissionCompleteRecordSet(role.getId());
	// KMissionSet set = KMissionModuleExtension.getMissionSet(role.getId());
	// if (set != null && completeSet != null) {
	// KMissionTemplate firstMission = KMissionModuleExtension
	// .getManager().getMissionTemplate(1);
	// if (KGuideManager.isCloseNoviceGuide) {
	// if (!completeSet
	// .checkMissionIsCompleted(firstMission.missionTemplateId)) {
	// set.completeNoviceGuideMission();
	// }
	// KMapRoleEventListener.getInstance().notifyRoleJoinMap(role,
	// false);
	// } else {
	// if (!completeSet.getNoviceGuideRecord().isCompleteFirstGuideBattle) {
	// notifyPlayNoviceGuideAnimation(role);
	// } else {
	// if (completeSet.getNoviceGuideRecord().isCompleteGuide) {
	// if (!completeSet
	// .checkMissionIsCompleted(firstMission.missionTemplateId)) {
	// set.completeNoviceGuideMission();
	// }
	// KMapRoleEventListener.getInstance().notifyRoleJoinMap(
	// role, false);
	// } else {
	// if (!completeSet
	// .checkMissionIsCompleted(firstMission.missionTemplateId)) {
	// KGameLevelSet levelSet = KGameLevelModuleExtension
	// .getGameLevelSet(role.getId());
	// if (levelSet != null) {
	// if (levelSet
	// .checkGameLevelIsCompleted(KGameLevelModuleExtension
	// .getManager().noviceGuideGameLevel
	// .getLevelId())) {
	// set.completeNoviceGuideMission();
	// KMapRoleEventListener.getInstance()
	// .notifyRoleJoinMap(role, false);
	// } else {
	// KMapRoleEventListener.getInstance()
	// .notifyRoleJoinMap(role, true);
	// }
	// } else {
	// KMapRoleEventListener.getInstance()
	// .notifyRoleJoinMap(role, true);
	// }
	// } else {
	// KMapRoleEventListener.getInstance()
	// .notifyRoleJoinMap(role, false);
	// }
	// }
	// }
	// }
	// }
	// }

	public void processNoviceGuide(KRole role) {
		KMissionCompleteRecordSet completeSet = KMissionModuleExtension.getMissionCompleteRecordSet(role.getId());
		KMissionSet set = KMissionModuleExtension.getMissionSet(role.getId());
		if (set != null && completeSet != null) {
			if (KGuideManager.isCloseNoviceGuide) {
				completeSet.finishNoviceGuide();

				KMapRoleEventListener.getInstance().notifyRoleJoinMap(role);
			} else {
				if (!completeSet.getNoviceGuideRecord().isCompleteGuide) {
					if (role.getLevel() > 1 || role.getRoleMapData().getLastMapId() > 0) {
						KMapRoleEventListener.getInstance().notifyRoleJoinMap(role);
					} else if (completeSet.getNoviceGuideRecord().isCompleteFirstGuideBattle) {
						// 2014-10-13 08:00 添加
						// completeSet.getNoviceGuideRecord().isCompleteFirstGuideBattlet条件
						// 完成第一场战斗后，同样不需要再次进入新手战斗流程
						completeSet.finishFirstNoviceGuideBattle();
						KMapRoleEventListener.getInstance().notifyRoleJoinMap(role);
					} else {
						notifyPlayNoviceGuideAnimation(role);
					}
				} else {
					KMapRoleEventListener.getInstance().notifyRoleJoinMap(role);
				}
			}
		}

	}

	public void checkIfPassNoviceGuideAndCompleteGuideMission(KRole role) {
		KMissionCompleteRecordSet completeSet = KMissionModuleExtension.getMissionCompleteRecordSet(role.getId());
		KMissionSet set = KMissionModuleExtension.getMissionSet(role.getId());
		if (completeSet != null && set != null) {
			if (completeSet.getNoviceGuideRecord().isCompleteGuide) {
				KMissionTemplate template = KMissionModuleExtension.getManager().getMissionTemplate(1);
				if (!completeSet.checkMissionIsCompleted(1)) {
					set.completeNoviceGuideMission();
				}
			}
		}
	}

	@Override
	public void checkAndNotifyWeaponGuideBattle(KRole role, KGameLevelSet levelSet, int levelId) {
		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role.getId());
		if (levelId == second_weapon_guide_level_id) {
			if (missionSet.checkMissionIsUncompleted(second_weapon_guide_mission_id) && !levelSet.checkGameLevelIsCompleted(second_weapon_guide_level_id)) {
				KGameMessage sendMsg = KGame.newLogicMessage(KMissionProtocol.SM_NOTIFY_SECOND_WEAPON_GUIDE_BATTLE);
				role.sendMsg(sendMsg);
			}
		} else if (levelId == mount_guide_level_id) {
			if (!levelSet.isOpenBattlePowerSlot()) {
				levelSet.notifyOpenBattlePowerSlot();
				checkAndSendIsOpenBattlePowerSlot(role);
			}

			if (missionSet.checkMissionIsUncompleted(mount_guide_mission_id) && !levelSet.checkGameLevelIsCompleted(mount_guide_level_id)) {
				KSupportFactory.getMountModuleSupport().notifyStartMountForNewRole(role.getId());
				KGameMessage sendMsg = KGame.newLogicMessage(KMissionProtocol.SM_NOTIFY_MOUNT_GUIDE_BATTLE);
				role.sendMsg(sendMsg);
			}
		}
	}

	@Override
	public void checkAndCloseMountGuideBattle(KRole role, int levelId) {
		if (levelId == mount_guide_level_id) {
			KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role.getId());
			if (missionSet.checkMissionIsUncompleted(mount_guide_mission_id)) {
				KSupportFactory.getMountModuleSupport().notifyCancelMountFromNewRole(role.getId());
			}
		}
	}

	@Override
	public void checkAndSendIsOpenBattlePowerSlot(KRole role) {
		KGameLevelSet levelSet = KGameLevelModuleExtension.getGameLevelSet(role.getId());
		boolean isOpenSlot = false;
		if (levelSet != null) {
			isOpenSlot = levelSet.isOpenBattlePowerSlot();
			if (!isOpenSlot) {
				KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role.getId());
				KMissionCompleteRecordSet completeSet = KMissionModuleExtension.getMissionCompleteRecordSet(role.getId());
				if (completeSet != null && completeSet.checkMissionIsCompleted(mount_guide_mission_id)) {
					levelSet.notifyOpenBattlePowerSlot();
					isOpenSlot = true;
				} else {
					if (missionSet != null) {
						if (missionSet.checkMissionIsUncompleted(mount_guide_mission_id) && levelSet.checkGameLevelIsCompleted(mount_guide_level_id)) {
							levelSet.notifyOpenBattlePowerSlot();
							isOpenSlot = true;
						}
					}
				}
			}
		}

		KGameMessage sendMsg = KGame.newLogicMessage(KMissionProtocol.SM_SEND_IS_OPEN_BATTLE_POWER_SLOT);
		sendMsg.writeBoolean(isOpenSlot);
		role.sendMsg(sendMsg);
	}

}
