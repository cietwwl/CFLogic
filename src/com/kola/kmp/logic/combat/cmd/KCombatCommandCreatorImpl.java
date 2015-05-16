package com.kola.kmp.logic.combat.cmd;

import java.util.List;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.operation.IOperation;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatCommandCreatorImpl implements ICombatCommandCreator {

	@Override
	public ICombatCommand createStartCombatCommand(ICombat combat) {
		return new KStartCombatCommand(combat);
	}
	
	@Override
	public ICombatCommand createCombatOperationCommand(ICombat combat, List<IOperation> opList) {
		return new KOperationCommand(combat, opList);
	}
	
	@Override
	public ICombatCommand createKillAllMemberCommand(ICombat combat, boolean killAllMonsters) {
		return new KKillAllMemberCommand(combat, killAllMonsters);
	}
	
	@Override
	public ICombatCommand createForceFinishCommand(ICombat combat, boolean isRoleWin) {
		return new KForceFinishCombatCommand(combat, isRoleWin);
	}
}
