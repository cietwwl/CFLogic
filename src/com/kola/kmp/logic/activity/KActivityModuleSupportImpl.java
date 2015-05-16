package com.kola.kmp.logic.activity;

import java.util.Arrays;

import com.kola.kmp.logic.activity.worldboss.KWorldBossManager;
import com.kola.kmp.logic.combat.ICombatCommonResult;
import com.kola.kmp.logic.combat.ICombatGlobalCommonResult;
import com.kola.kmp.logic.npc.dialog.KDialogButton;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.ActivityModuleSupport;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.WorldBossTips;

/**
 * 
 * @author PERRY CHAN
 */
public class KActivityModuleSupportImpl implements ActivityModuleSupport {

	// boolean world_boss_debug = true;
	@Override
	public void processWorldBossCombatFinish(long roleId, ICombatCommonResult roleResult, ICombatGlobalCommonResult globalResult) {
		// if (!world_boss_debug) {
		// KWorldBossManager.getWorldBossActivity().processCombatFinished(roleId,
		// roleResult, globalResult);
		// } else {
		// KDialogButton button =
		// KDialogService.createButton(KActivityModuleDialogProcesser.FUN_WORLD_BOSS_RESULT_CONFIRM,
		// "", "confirm");
		// KDialogService.sendFunDialog(roleId, "",
		// WorldBossTips.getTipsCombatResult(1000), Arrays.asList(button), true,
		// (byte) -1);
		// }
		KWorldBossManager.getWorldBossActivity().processCombatFinished(roleId, roleResult, globalResult);
	}

	@Override
	public void processRoleExitCombatFinish(long roleId) {
		// if (!world_boss_debug) {
		// KWorldBossManager.getWorldBossActivity().processRoleExitCombatFinish(roleId);
		// } else {
		// System.out.println("世界boss收到战斗完成消息！角色id：" + roleId);
		// }
		KWorldBossManager.getWorldBossActivity().processRoleExitCombatFinish(roleId);
	}
	
	@Override
	public void processWorldBossEscape(KRole role) {
		KWorldBossManager.getWorldBossActivity().confirmCombatResult(role);
	}

}
