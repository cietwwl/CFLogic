package com.kola.kmp.logic.combat.operation;

import com.kola.kmp.logic.combat.ICombatMember;

/**
 * 
 * @author PERRY CHAN
 */
public abstract class KOperationBaseImpl implements IOperation {

	protected final long opTime;

	protected KOperationBaseImpl(long pOpTime) {
		this.opTime = pOpTime;
	}

	@Override
	public long getOperationTime() {
		return opTime;
	}
	
	@Override
	public void notifyMountAdded(ICombatMember master, ICombatMember mount) {
		// 空实现
	}
	
	@Override
	public void notifyMountReleased(ICombatMember master, ICombatMember mount) {
	}
}
