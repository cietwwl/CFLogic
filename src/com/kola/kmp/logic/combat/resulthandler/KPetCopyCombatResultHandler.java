package com.kola.kmp.logic.combat.resulthandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.level.FightResult;
import com.kola.kmp.logic.level.ICombatAdditionalReward;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.level.petcopy.KPetCopyBattlefield.KPetCopyBattlefieldDropData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 
 * @author PERRY CHAN
 *
 */
public class KPetCopyCombatResultHandler implements ICombatResultHandler {

	@Override
	public void processCombatResultToRole(long roleId, ICombat combat, ICombatResult combatResult) {
		ICombatRoleResult roleResult = combatResult.getRoleResult(roleId);
		if (roleResult != null) {
			KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			ICombatAdditionalReward reward = combatResult.getCombatReward(roleId);
			ICombatGameLevelInfo attachment = combatResult.getGameLevelInfo();
			@SuppressWarnings("unchecked")
			Map<Integer, KPetCopyBattlefieldDropData> petCopyDropDataMap = (Map<Integer, KPetCopyBattlefieldDropData>)combatResult.getAttachment();
			Map<KPetCopyBattlefieldDropData, Boolean> resultDropDataMap = new HashMap<KPetCopyBattlefieldDropData, Boolean>();
			List<Integer> instanceIds = combatResult.getKillInstanceIds();
			KPetCopyBattlefieldDropData dropData;
			for(int i = 0; i < instanceIds.size(); i++) {
				dropData = petCopyDropDataMap.remove(instanceIds.get(i));
				if(dropData != null) {
					resultDropDataMap.put(dropData, true);
				}
			}
			for(Iterator<KPetCopyBattlefieldDropData> itr = petCopyDropDataMap.values().iterator(); itr.hasNext();) {
				resultDropDataMap.put(itr.next(), false);
			}
			FightResult result = new FightResult();
			result.setWin(combatResult.isRoleWin());
			result.setBattlefieldId(attachment.getLastBattleFieldId());
			result.setBattlefieldType(attachment.getLastBattleFieldType());
			result.setPetCopyResultMap(resultDropDataMap);
			if (combatResult.isRoleWin()) {
				result.setBattleTime(combatResult.getTotalCombatTime());
				result.setMaxDoubleHitCount(roleResult.getMaxComboCount());
				result.setMaxBeHitCount(roleResult.getMaxBeHitCount());
				result.setKillMonsterCount(roleResult.getKillMonsterCount());
				result.setTotalDamage((int)roleResult.getTotalDamage());
				result.setBattleReward(reward);
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
		
	}

}
