package com.kola.kmp.logic.combat.operation;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.IOperationResult;

/**
 * 
 * @author PERRY CHAN
 */
public class KAIBuffOperation extends KOperationBaseImpl {

	
	private short _targetId;
	private int _buffId;
	
	public KAIBuffOperation(long pHappenTime, short pTargetId, int pBuffId) {
		super(pHappenTime);
		this._targetId = pTargetId;
		this._buffId = pBuffId;
	}
	
	@Override
	public IOperationResult executeOperation(ICombat combat) {
		ICombatMember member = combat.getCombatMember(_targetId);
		if(member != null) {
			// AI的buff指令，都是自己加给自己
			member.getSkillActor().addState(member, _buffId, opTime);
		}
		return null;
	}
	
	@Override
	public int getPriority() {
		return PRIORITY_URGENT;
	}
}
