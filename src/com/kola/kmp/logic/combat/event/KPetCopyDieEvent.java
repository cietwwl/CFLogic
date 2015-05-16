package com.kola.kmp.logic.combat.event;

import java.util.ArrayList;
import java.util.List;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatEvent;
import com.kola.kmp.logic.combat.ICombatEventListener;
import com.kola.kmp.logic.combat.ICombatMember;

/**
 * 
 * @author PERRY CHAN
 *
 */
public class KPetCopyDieEvent implements ICombatEventListener {

	private int _callerInstanceId;
	private List<Short> _monshterShadowIds;
	
	public KPetCopyDieEvent(int pCallerInstanceId, List<Short> monsterIdList) {
		_callerInstanceId = pCallerInstanceId;
		_monshterShadowIds = new ArrayList<Short>(monsterIdList);
	}

	@Override
	public int getEventId() {
		return ICombatEvent.EVENT_SELF_DEAD;
	}

	@Override
	public void run(ICombat combat, ICombatMember operator, long happenTime) {
		short tempId;
		ICombatMember member;
		boolean allDie = true;
		for (int i = 0; i < _monshterShadowIds.size(); i++) {
			tempId = _monshterShadowIds.get(i);
			if (tempId != operator.getShadowId()) {
				member = combat.getCombatMember(tempId);
				if (member.isAlive()) {
					allDie = false;
					break;
				}
			}
		}
		if(allDie) {
			combat.recordKillInstanceId(_callerInstanceId);
		}
	}

	@Override
	public boolean isEffective(long happenTime) {
		return true;
	}

}
