package com.kola.kmp.logic.combat.resulthandler;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.level.TowerFightResult;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 
 * @author PERRY CHAN
 */
public class KTowerCombatResultHandler implements ICombatResultHandler {

	@Override
	public void processCombatResultToRole(long roleId, ICombat combat, ICombatResult combatResult) {
		TowerFightResult result = new TowerFightResult();
		ICombatRoleResult roleResult = combatResult.getRoleResult(roleId);
		ICombatGameLevelInfo levelInfo = combatResult.getGameLevelInfo();
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		if (role != null) {
			ICombatMember friend;
			if (roleResult.getAttachment() != null && roleResult.getAttachment() instanceof ICombatMember) {
				friend = (ICombatMember) roleResult.getAttachment();
			} else {
				friend = null;
			}
			result.setBattlefieldId(levelInfo.getLastBattleFieldId());
			result.setBattlefieldType(levelInfo.getLastBattleFieldType());
			result.setBattleTime(combatResult.getTotalCombatTime());
			result.setFinishWave((Integer) combatResult.getAttributeFromResult(ICombatResult.KEY_CLEAR_TOWER_WAVE_NUM));
			result.setFriendId((Long) combatResult.getAttachment());
			result.setLastTowerId((Integer)combatResult.getAttributeFromResult((ICombatResult.KEY_CLEAR_TOWER_ID)));
			result.setMaxBeHitCount(roleResult.getMaxBeHitCount());
			result.setMaxDoubleHitCount(roleResult.getMaxComboCount());
			result.setTotalDamage((int)roleResult.getTotalDamage());
			result.setWin(roleResult.isWin());
			result.setEndType(roleResult.isEscape() ? TowerFightResult.FIGHT_END_TYPE_ESCAPE : TowerFightResult.FIGHT_END_TYPE_NORMAL);
			if(friend != null) {
				result.setMaxFriendBeHitCount(friend.getCombatRecorder().getBeHitCount());
				result.setMaxFriendDoubleHitCount(friend.getCombatRecorder().getMaxComboAttackCount());
				result.setFriendTotalDamage((int)friend.getCombatRecorder().getTotalDm());
				System.out.println("！！！！好友的总伤害：" + friend.getCombatRecorder().getTotalDm() + "！！！！");
			}
			KSupportFactory.getLevelSupport().notifyCompleteTowerFight(role, result);
		}
	}

	@Override
	public void processCombatFinish(ICombat combat, ICombatResult combatResult) {
		
	}
	
	@Override
	public void processRoleExitCombatFinish(long roleId, ICombatResult result) {
		
	}

}
