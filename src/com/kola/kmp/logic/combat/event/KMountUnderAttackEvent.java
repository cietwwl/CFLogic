package com.kola.kmp.logic.combat.event;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatEvent;
import com.kola.kmp.logic.combat.ICombatEventListener;
import com.kola.kmp.logic.combat.ICombatMember;

/**
 * 
 * @author PERRY CHAN
 */
public class KMountUnderAttackEvent implements ICombatEventListener {

	
	private long _reduceDurationPerAttack;
	
	public KMountUnderAttackEvent(long reduceTime) {
		this._reduceDurationPerAttack = reduceTime;
	}
	
	@Override
	public int getEventId() {
		return ICombatEvent.EVENT_UNDER_ATTACK;
	}

	@Override
	public void run(ICombat combat, ICombatMember operator, long happenTime) {
		operator.reduceSurviveTime(_reduceDurationPerAttack);
		if(operator.getTerminateTime() < happenTime) {
			operator.sentenceToDead(happenTime);
		}
	}

	@Override
	public boolean isEffective(long happenTime) {
		return true;
	}

	
}
