package com.kola.kmp.logic.combat.impl;

import java.util.List;

import com.kola.kmp.logic.activity.goldact.KBarrelBattlefield;
import com.kola.kmp.logic.combat.KCombatType;
import com.kola.kmp.logic.level.FightEventListener;
import com.kola.kmp.logic.level.FightResult;
import com.kola.kmp.logic.level.KGameBattlefield;
import com.kola.kmp.logic.level.KLevelTemplate;
import com.kola.kmp.logic.level.gamestory.Animation;
import com.kola.kmp.logic.level.petcopy.KPetCopyBattlefield;
import com.kola.kmp.logic.level.tower.KTowerBattlefield;
import com.kola.kmp.logic.other.KGameBattlefieldTypeEnum;
import com.kola.kmp.logic.role.KRole;

/**
 * 
 * @author PERRY CHAN
 */
public class KFightEventListenerImpl implements FightEventListener {

	@Override
	public void notifyBattle(KRole role, List<KGameBattlefield> battlefield, List<Animation> animationList) {
		if (battlefield.size() > 0) {
			KGameBattlefieldTypeEnum battleFieldType = battlefield.get(0).getBattlefieldType();
			KCombatType combatType;
			switch (battleFieldType) {
			case 爬塔副本战场:
				combatType = KCombatType.CLIMB_TOWER_COMBAT;
				break;
			case 随从挑战副本战场:
				combatType = KCombatType.PET_CHALLENGE_COPY;
				break;
			case 高级随从挑战副本战场:
				combatType = KCombatType.PET_CHALLENGE_SENIOR_COPY;
				break;
			default:
				combatType = KCombatType.GAME_LEVEL;
				break;
			}
			KCombatManager.startGameLevelBattle(role, battlefield, animationList, combatType);
		}
	}

	@Override
	public void notifyBattleFinished(KRole role, FightResult result) {

	}

	@Override
	public void notifyGameLevelCompleted(KRole role, KLevelTemplate gamelevel, FightResult result) {

	}

	@Override
	public void notifyBattleRewardFinished(KRole role) {

	}

	@Override
	public void notifyFriendTowerBattle(KRole role, long friendRoleId, KTowerBattlefield battlefield) {
		KCombatManager.startTowerCombat(role, friendRoleId, battlefield);
	}

	@Override
	public void notifyGoldActivityBattle(KRole role, KBarrelBattlefield battlefield) {
		KCombatManager.startBarrelCombat(role, battlefield);
	}
	
	@Override
	public void notifyNewGoldActivityBattle(KRole role, KGameBattlefield battlefield, int glodBaseValue, long battleTime) {
		KCombatManager.startNewGoldActivityCombat(role, battlefield, glodBaseValue, battleTime);
	}

	@Override
	public void notifyPetCopyBattle(KRole role, KPetCopyBattlefield battlefield) {
		KCombatManager.startPetCopyCombat(role, battlefield);
	}
}
