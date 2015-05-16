package com.kola.kmp.logic.combat.impl;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatEventListener;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.function.IFunctionExecution;
import com.kola.kmp.logic.combat.function.event.ICondition;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatSpecialEffect implements ICombatEventListener {

	private int _eventId;
	private boolean _effective;
	private IFunctionExecution _function;
	private ICondition _condition;
	
	public KCombatSpecialEffect(int pEventId, boolean pEffective, IFunctionExecution pExecution, ICondition pCondition) {
		this._eventId = pEventId;
		this._effective = pEffective;
		this._function = pExecution;
		this._condition = pCondition;
	}
	
	@Override
	public int getEventId() {
		return _eventId;
	}

	@Override
	public void run(ICombat combat, ICombatMember operator, long happenTime) {
		if (_condition.isMatchCondition(operator)) {
			_function.execute(combat, operator, happenTime);
		}
	}

	@Override
	public boolean isEffective(long happenTime) {
		return _effective;
	}

}
