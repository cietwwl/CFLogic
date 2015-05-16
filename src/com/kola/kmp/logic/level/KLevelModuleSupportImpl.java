package com.kola.kmp.logic.level;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.level.petcopy.KPetCopyBattlefield;
import com.kola.kmp.logic.mission.KMissionModuleExtension;
import com.kola.kmp.logic.mission.MissionCompleteCondition.GameLevelTask;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.other.KMissionSearchRoadTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.LevelModuleSupport;
import com.kola.kmp.protocol.level.KLevelProtocol;

public class KLevelModuleSupportImpl implements LevelModuleSupport {

	private static KGameMessage resetCopyDataMsg;
	private static KGameMessage resetFriendCopyDataMsg;

	static {
		resetCopyDataMsg = KGame.newLogicMessage(KLevelProtocol.SM_REST_COPY_DATA);
		resetFriendCopyDataMsg = KGame.newLogicMessage(KLevelProtocol.SM_REST_FRIEND_COPY_DATA);
	}

	@Override
	public void notifyCompleteFight(KRole role, FightResult result) {
		KGameLevelModuleExtension.getManager().processPlayerRoleCompleteBattlefield(role, result);
	}

	@Override
	public boolean checkGameLevelIsCompleted(long roleId, int levelId) {
		// 获取关卡记录
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(roleId);
		if (levelSet != null) {
			return levelSet.checkGameLevelIsCompleted(levelId);
		}
		return false;
	}

	@Override
	public boolean notifyCompleteTowerFight(KRole role, TowerFightResult result) {
		return KGameLevelModuleExtension.getManager().getFriendCopyManager().processPlayerRoleCompleteCopyLevel(role, result);
	}

	@Override
	public KGameBattlefield getWorldBossBattlefield(int levelId) {
		return KGameLevelModuleExtension.getManager().getWorldBossBattlefield(levelId);
	}

	@Override
	public KGameBattlefield getFamilyWarBattlefield(int levelId) {
		return KGameLevelModuleExtension.getManager().getFamilyWarBattlefield(levelId);
	}

	@Override
	public KActionResult startFamilyWarPVEBattle(KRole role, KGameBattlefield pveBattle) {
		return null;
	}

	@Override
	public KGameBattlefield getNoviceGuideBattlefield() {
		return KGameLevelModuleExtension.getManager().getNoviceGuideBattlefield();
	}

	@Override
	public void checkAndResetCopyData(KRole role) {
		boolean result = KGameLevelModuleExtension.getManager().getCopyManager().checkAndResetEliteCopyDatas(role, true);
		if (result) {
			role.sendMsg(resetCopyDataMsg.duplicate());
		}
	}

	@Override
	public void checkAndResetFriendCopyData(KRole role) {
		boolean result = KGameLevelModuleExtension.getManager().getFriendCopyManager().checkAndResetFriendCopyDatas(role, true);
		if (result) {
			role.sendMsg(resetFriendCopyDataMsg.duplicate());
		}
	}

	@Override
	public void checkAndUpdateGameLevelOpenStateWhileRoleLvUp(KRole role) {
		KGameLevelModuleExtension.getManager().checkAndUpdateGameLevelOpenState(role);
		KGameLevelModuleExtension.getManager().getCopyManager().checkAndUpdateGameLevelOpenState(role);
	}

	@Override
	public KLevelTemplate getNormalGameLevelTemplate(KGameLevelTypeEnum levelType, int levelId) {

		if (levelType == KGameLevelTypeEnum.普通关卡) {
			return KGameLevelModuleExtension.getManager().getKGameLevel(levelId);
		} else if (levelType == KGameLevelTypeEnum.精英副本关卡 || levelType == KGameLevelTypeEnum.技术副本关卡) {
			return KGameLevelModuleExtension.getManager().getCopyManager().getCopyLevelTemplate(levelId, levelType);
		}
		// else if (levelType == KGameLevelTypeEnum.新手引导关卡) {
		// return KGameLevelModuleExtension.getManager().noviceGuideGameLevel;
		// }
		return null;
	}

	@Override
	public String getScenarioNameByLevelId(int levelId) {
		KLevelTemplate level = KGameLevelModuleExtension.getManager().getKGameLevel(levelId);
		if (level != null) {
			KGameScenario scenario = KGameLevelModuleExtension.getManager().allKGameScenario.get(level.getScenarioId());
			if (scenario != null) {
				return scenario.getScenarioName();
			}
		}
		return null;
	}

	@Override
	public void notifyClientLevelSearchRoad(KRole role, int levelId) {
		if (role == null) {
			return;
		}
		if (KGameLevelModuleExtension.getManager().allKGameLevel.containsKey(levelId) || levelId == GameLevelTask.ANY_TYPE_LEVEL) {
			KMissionModuleExtension.getManager().processPlayerRoleSearchRoad(role, KMissionSearchRoadTypeEnum.SEARCH_ROAD_TYPE_LEVEL.getSearchRoadType(), levelId + "", -1);
		}
	}
}
