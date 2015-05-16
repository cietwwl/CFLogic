package com.kola.kmp.logic.combat.operation;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.IOperationResult;

/**
 * 
 * @author PERRY CHAN
 */
public class KAIAddStateOperation extends KOperationBaseImpl {

	private static final byte STATE_TYPE_FROZEN = 1;
	private static final byte STATE_TYPE_FAINT = 2;
	private static final byte STATE_TYPE_FULL_IMMULITY = 3;
	
	private short _targetId;
	private boolean _add;
	private byte _type;
	
	public KAIAddStateOperation(long pOpTime, short pTargetId,  boolean pAdd, byte pType) {
		super(pOpTime);
		this._targetId = pTargetId;
		this._add = pAdd;
		this._type = pType;
	}

	@Override
	public IOperationResult executeOperation(ICombat combat) {
		ICombatMember member = combat.getCombatMember(_targetId);
		if (member != null) {
			switch (_type) {
			case STATE_TYPE_FAINT:
				member.getSkillActor().handleFaint(_add);
				break;
			case STATE_TYPE_FROZEN:
				member.getSkillActor().handleFreeze(_add);
				break;
			case STATE_TYPE_FULL_IMMULITY:
				member.getSkillActor().handleFullImmunity(_add);
				break;
			}
		}
		return null;
	}
	
	@Override
	public int getPriority() {
		return PRIORITY_URGENT;
	}

}
