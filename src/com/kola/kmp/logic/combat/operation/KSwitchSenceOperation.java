package com.kola.kmp.logic.combat.operation;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.IOperationResult;

/**
 * 
 * @author PERRY CHAN
 */
public class KSwitchSenceOperation extends KOperationBaseImpl {
	
//	private short _operatorId;
	private int _currentSenceId;
	private int _targetSenceId;
	
	/**
	 * 
	 */
	public KSwitchSenceOperation(long pHappenTime, short pOperatorId, int pCurrentSenceId, int pTargetSenceId) {
		super(pHappenTime);
		this._currentSenceId = pCurrentSenceId;
		this._targetSenceId = pTargetSenceId;
	}
	

	@Override
	public IOperationResult executeOperation(ICombat combat) {
		combat.switchSence(_currentSenceId, _targetSenceId);
		return null;
	}

	@Override
	public int getPriority() {
		return PRIORITY_URGENT;
	}
}
