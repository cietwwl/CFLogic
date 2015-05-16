package com.kola.kmp.logic.combat.operation;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.IOperationResult;

/**
 * 
 * @author PERRY CHAN
 */
public class KSwitchBlockOperation extends KOperationBaseImpl {

	private short _shadowId;
	private boolean _start;
	
	public KSwitchBlockOperation(long pOpTime, short pShadowId, boolean pStart) {
		super(pOpTime);
		this._start = pStart;
		this._shadowId = pShadowId;
	}

	@Override
	public IOperationResult executeOperation(ICombat combat) {
		ICombatMember member = combat.getCombatMember(_shadowId);
		if (member != null) {
			member.switchBlockStatus(_start);
		}
		return null;
	}

	@Override
	public int getPriority() {
		return PRIORITY_URGENT;
	}
}
