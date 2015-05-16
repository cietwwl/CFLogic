package com.kola.kmp.logic.combat.resulthandler;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.level.FightResult;
import com.kola.kmp.logic.level.ICombatAdditionalReward;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 
 * @author PERRY CHAN
 */
public class KGameLevelCombatResultHandler implements ICombatResultHandler {

//	private static final Logger _LOGGER = KGameLogger.getLogger(KGameLevelCombatResultHandler.class);
	
	@Override
	public void processCombatResultToRole(long roleId, ICombat combat, ICombatResult combatResult) {
		ICombatRoleResult roleResult = combatResult.getRoleResult(roleId);
		if (roleResult != null) {
			KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			ICombatAdditionalReward reward = combatResult.getCombatReward(roleId);
			ICombatGameLevelInfo attachment = combatResult.getGameLevelInfo();
			FightResult result = new FightResult();
			result.setWin(combatResult.isRoleWin());
			result.setBattlefieldId(attachment.getLastBattleFieldId());
			result.setBattlefieldType(attachment.getLastBattleFieldType());
			if (combatResult.isRoleWin()) {
				result.setBattleTime(combatResult.getTotalCombatTime());
				result.setMaxDoubleHitCount(roleResult.getMaxComboCount());
				result.setMaxBeHitCount(roleResult.getMaxBeHitCount());
				result.setKillMonsterCount(roleResult.getKillMonsterCount());
				result.setTotalDamage((int)roleResult.getTotalDamage());
				result.setBattleReward(reward);
				result.setRoleCurrentHp(roleResult.getRoleCurrentHp());
				result.setPetCurrentHp(roleResult.getPetCurrentHp());
//				reward.executeReward(role);
			}
			if (roleResult.isEscape()) {
				result.setEndType(FightResult.FIGHT_END_TYPE_ESCAPE);
			} else {
				result.setEndType(FightResult.FIGHT_END_TYPE_NORMAL);
			}
			KGameLevelModuleExtension.getManager().processPlayerRoleCompleteBattlefield(role, result);
		}
	}

	@Override
	public void processCombatFinish(ICombat combat, ICombatResult combatResult) {
		
	}
	
	@Override
	public void processRoleExitCombatFinish(long roleId, ICombatResult result) {
		ICombatRoleResult roleResult = result.getRoleResult(roleId);
		if (roleResult.isWin()) {
			KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			ICombatAdditionalReward reward = result.getCombatReward(roleId);
			reward.executeReward(role);
		}
	}

}
