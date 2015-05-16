package com.kola.kmp.logic.combat.operation;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.IOperationResult;

/**
 * 
 * @author PERRY CHAN
 */
public class KPickUpOperation extends KOperationBaseImpl {

	private int _dropId;
	private long _roleId;
	
	
	KPickUpOperation(long pHappenTime, long pRoleId, int pDropId) {
		super(pHappenTime);
		this._dropId = pDropId;
		this._roleId = pRoleId;
	}
	
	@Override
	public IOperationResult executeOperation(ICombat combat) {
		combat.pickUp(_roleId, _dropId, opTime);
		return null;
	}
	
	@Override
	public int getPriority() {
		return PRIORITY_URGENT;
	}

}
