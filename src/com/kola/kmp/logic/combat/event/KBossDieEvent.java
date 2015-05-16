package com.kola.kmp.logic.combat.event;

import java.util.List;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatEvent;
import com.kola.kmp.logic.combat.ICombatEventListener;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.impl.KCombatManager;

public class KBossDieEvent implements ICombatEventListener {

	@Override
	public int getEventId() {
		return ICombatEvent.EVENT_SELF_DEAD;
	}

	@Override
	public void run(ICombat combat, ICombatMember operator, long happenTime) {
		if (operator.getMemberType() == ICombatMember.MEMBER_TYPE_BOSS_MONSTER) {
			List<ICombatMember> all = combat.getCombatMemberByType(ICombatMember.MEMBER_TYPE_BOSS_MONSTER);
			boolean combatFinish = true;
			if (all.size() > 1) {
				ICombatMember member;
				for (int i = 0; i < all.size(); i++) {
					member = all.get(i);
					if (member.isAlive()) {
						combatFinish = false;
						break;
					}
				}
			}
			if (combatFinish) {
				combat.submitCommand(KCombatManager.getCmdCreateorInstance().createKillAllMemberCommand(combat, true));
			}
		}
	}

	@Override
	public boolean isEffective(long happenTime) {
		return true;
	}

}
