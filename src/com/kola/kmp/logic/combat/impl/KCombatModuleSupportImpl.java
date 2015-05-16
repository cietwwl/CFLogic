package com.kola.kmp.logic.combat.impl;

import java.util.List;
import java.util.Map;

import com.kola.kmp.logic.combat.ICombatEnhanceInfo;
import com.kola.kmp.logic.combat.ICombatMirrorDataGroup;
import com.kola.kmp.logic.combat.ICombatRoleSideHpUpdater;
import com.kola.kmp.logic.combat.KCombatType;
import com.kola.kmp.logic.level.KGameBattlefield;
import com.kola.kmp.logic.level.gamestory.Animation;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.CombatModuleSupport;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatModuleSupportImpl implements CombatModuleSupport {

	@Override
	public KActionResult<Integer> fightWithAI(KRole role, long defenderRoleId, KCombatType type, ICombatEnv env, Object attachment) {
		return KCombatManager.startFightWithAICombat(role, defenderRoleId, env, type, attachment, 0);
	}
	
	@Override
	public KActionResult<Integer> fightWithAIWithTimeLimit(KRole role, long defenderRoleId, KCombatType type, ICombatEnv env, Object attachment, int timeOutMillis) {
		return KCombatManager.startFightWithAICombat(role, defenderRoleId, env, type, attachment, timeOutMillis);
	}

	@Override
	public KActionResult<Integer> fightByUpdateInfo(KRole role, KGameBattlefield battlefield, ICombatEnhanceInfo enhanceInfo, Map<Integer, ICombatMonsterUpdateInfo> map, KCombatType type, Object attachment) {
		return KCombatManager.startFightByUpdateInfoCombat(role, battlefield, null, enhanceInfo, map, null, type, attachment, 0);
	}
	
	@Override
	public KActionResult<Integer> fightByUpdateInfo(KRole role, KGameBattlefield battlefield, ICombatEnhanceInfo enhanceInfo, Map<Integer, ICombatMonsterUpdateInfo> map, KCombatType type, Object attachment, int timeOutMillis) {
		return KCombatManager.startFightByUpdateInfoCombat(role, battlefield, null, enhanceInfo, map, null, type, attachment, timeOutMillis);
	}
	
	@Override
	public KActionResult<Integer> fightByUpdateInfoWithRobots(KRole role, KGameBattlefield battlefield, ICombatEnhanceInfo enhanceInfo, Map<Integer, ICombatMonsterUpdateInfo> map, List<Long> pRobotIds, KCombatType type, Object attachment) {
		return KCombatManager.startFightByUpdateInfoCombat(role, battlefield, null, enhanceInfo, map, pRobotIds, type, attachment, 0);
	}
	
	@Override
	public KActionResult<Integer> fightInBattlefield(KRole role, KGameBattlefield battlefield, List<Animation> animationList,  KCombatType type, Object attachment) {
		return KCombatManager.startFightByUpdateInfoCombat(role, battlefield, animationList, null, null, null, type, attachment, 0);
	}
	
	@Override
	public KActionResult<Integer> fightWithAI(KRole role, long teammateId, List<ICombatMirrorDataGroup> enemies, KCombatType type, ICombatEnvPlus env, Object attachment, long timeoutMillis) {
		return KCombatManager.startFightWithAICombatByEnemies(role, teammateId, enemies, type, env, attachment, timeoutMillis);
	}
	
	@Override
	public KActionResult<Integer> fightWithAI(KRole role, long teammateId, long[] enemyIds, KCombatType type, ICombatEnvPlus env, Object attachment, long timeoutMillis) {
		return KCombatManager.startFightWithAICombat(role, teammateId, enemyIds, type, env, attachment, timeoutMillis);
	}
	
	@Override
	public void forceFinishCombat(KRole role, KCombatType type) {
		KCombatManager.forceFinishCombat(role, type, false);
	}
	
	@Override
	public void registerCombatHpUpdater(ICombatRoleSideHpUpdater pUpdater) {
		KCombatManager.registerRoleSideHpUpdater(pUpdater);
	}
}
