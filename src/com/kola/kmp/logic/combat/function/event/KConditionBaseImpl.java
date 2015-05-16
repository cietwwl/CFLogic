package com.kola.kmp.logic.combat.function.event;

import com.kola.kmp.logic.combat.ICombatMember;

/**
 * 
 * @author PERRY CHAN
 */
public abstract class KConditionBaseImpl implements ICondition {

	KConditionBaseImpl nextCondition;
	/**
	 * 
	 * @param member
	 * @return
	 */
	protected abstract boolean conditionPass(ICombatMember member);
	
	@Override
	public boolean isMatchCondition(ICombatMember member) {
		KConditionBaseImpl temp = this;
		do {
			if (!temp.conditionPass(member)) {
				return false;
			}
		} while ((temp = temp.nextCondition) != null);
		return true;
	}

}
