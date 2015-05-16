package com.kola.kmp.logic.combat.impl;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatEvent;
import com.kola.kmp.logic.combat.ICombatEventListener;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.support.KCombatCalculateSupport;

public class KIncEnergyEventListener implements ICombatEventListener {

	@Override
	public int getEventId() {
		return ICombatEvent.EVENT_AFTER_ATTACK;
	}

	@Override
	public void run(ICombat combat, ICombatMember operator, long happenTime) {
		if (operator.getMemberType() == ICombatMember.MEMBER_TYPE_VEHICLE) {
			operator = combat.getMasterOfMount(operator);
		}
		if (operator != null && operator.getMemberType() == ICombatMember.MEMBER_TYPE_ROLE) {
			int currentCtnAtkCount = operator.getCombatRecorder().getCurrentComboAttackCount();
//			if (KCombatConfig.isProduceEnergy(currentCtnAtkCount)) {
			if(currentCtnAtkCount % KCombatConfig.getEnergyCalPara() == 0) {
				int energy = KCombatConfig.getEnergyIncCountEachTime();
				operator.increaseEnergy(energy);
//				System.out.println(StringUtil.format("当前连击数：{}，增加怒气：{}", currentCtnAtkCount, energy));
			}
		}
	}

	@Override
	public boolean isEffective(long happenTime) {
		return true;
	}

}
