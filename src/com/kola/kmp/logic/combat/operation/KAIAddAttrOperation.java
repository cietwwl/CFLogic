package com.kola.kmp.logic.combat.operation;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.IOperationResult;
import com.kola.kmp.logic.other.KGameAttrType;

/**
 * 
 * @author PERRY CHAN
 */
public class KAIAddAttrOperation extends KOperationBaseImpl {

	private short _targetId;
	private KGameAttrType _attrType;
	private int _value;
	private boolean _add;
	
	public KAIAddAttrOperation(long pOpTime, short pTargetId, KGameAttrType pAttrType, int pValue) {
		super(pOpTime);
		this._targetId = pTargetId;
		this._attrType = pAttrType;
		if (pValue < 0) {
			_add = false;
			_value = Math.abs(pValue);
		} else {
			_add = true;
			_value = pValue;
		}
	}

	@Override
	public IOperationResult executeOperation(ICombat combat) {
		ICombatMember member = combat.getCombatMember(_targetId);
		if (member != null) {
			member.getSkillActor().changeCombatAttr(_attrType, _value, _add);
		}
		return null;
	}
	
	@Override
	public int getPriority() {
		return PRIORITY_URGENT;
	}

}
