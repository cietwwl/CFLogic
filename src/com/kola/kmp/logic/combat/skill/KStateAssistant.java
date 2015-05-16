package com.kola.kmp.logic.combat.skill;

import java.util.List;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 
 * @author PERRY CHAN
 */
public class KStateAssistant {

	private int _stateId;
	
	public KStateAssistant(int pStateId) {
		this._stateId = pStateId;
		if(KSupportFactory.getSkillModuleSupport().getStateTemplate(_stateId) == null) {
			throw new NullPointerException("状态不存在！状态id：" + _stateId);
		}
	}
	
	public void executeState(ICombat combat, ICombatMember operator, List<ICombatMember> targets, long happendTime) {
		ICombatMember member;
		for (int i = 0; i < targets.size(); i++) {
			member = targets.get(i);
			if (member.isAlive()) {
				member.getSkillActor().addState(operator, this._stateId, happendTime);
			}
		}
	}
}
