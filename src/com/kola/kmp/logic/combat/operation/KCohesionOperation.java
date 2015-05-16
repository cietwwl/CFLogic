package com.kola.kmp.logic.combat.operation;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.IOperationResult;

/**
 * 
 * @author PERRY CHAN
 */
public class KCohesionOperation extends KOperationBaseImpl {

	private short _shadowId;
	private int _cohesionTime;
	private int _clientResult;
	
	public KCohesionOperation(long pOpTime, short pShadowId, int pCohesionTime, int pClientResult) {
		super(pOpTime);
		this._shadowId = pShadowId;
		this._cohesionTime = pCohesionTime;
		this._clientResult = pClientResult;
	}
	
	@Override
	public IOperationResult executeOperation(ICombat combat) {
		ICombatMember member = combat.getCombatMember(_shadowId);
		member.processCohension(_cohesionTime, _clientResult);
		return null;
	}
	
	@Override
	public int getPriority() {
		return PRIORITY_URGENT;
	}

}
