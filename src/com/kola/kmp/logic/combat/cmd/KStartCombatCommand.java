package com.kola.kmp.logic.combat.cmd;

import com.kola.kmp.logic.combat.ICombat;

/**
 * 
 * @author PERRY CHAN
 */
public class KStartCombatCommand implements ICombatCommand {

	private ICombat _combat;

	KStartCombatCommand(ICombat pCombat) {
		this._combat = pCombat;
	}
	@Override
	public void execute() {
		_combat.startCombat();
	}

}
