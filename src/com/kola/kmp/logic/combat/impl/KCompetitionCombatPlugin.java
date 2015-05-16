package com.kola.kmp.logic.combat.impl;

import java.util.ArrayList;
import java.util.List;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.ICombatPlugin;
import com.kola.kmp.logic.combat.KCombatType;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 
 * @author PERRY CHAN
 */
public class KCompetitionCombatPlugin implements ICombatPlugin {

	@Override
	public void beforeStart(ICombat combat) {
		if (combat.getCombatType().isPVP) {
			List<ICombatMember> list = new ArrayList<ICombatMember>();
			list.addAll(combat.getCombatMemberByType(ICombatMember.MEMBER_TYPE_ROLE));
			list.addAll(combat.getCombatMemberByType(ICombatMember.MEMBER_TYPE_ROLE_MONSTER));
			if(combat.getCombatType() == KCombatType.COMPETITION) {
				ICombatMember challenger = list.get(0);
				ICombatMember defender = list.get(1);
				float cBattlePower = KCombatManager.getBattlePower(challenger.getSrcObjId());
				float dBattlePower = KCombatManager.getBattlePower(defender.getSrcObjId());
				int para = Math.min(KCombatConfig.getMaxMultipleOfCompetition(), (int) Math.round(Math.pow(dBattlePower / cBattlePower, KCombatConfig.getCalParaOfCompetition())));
				if (para > 1) {
					defender.getSkillActor().changeCombatAttr(KGameAttrType.ATK, defender.getAtk() * para - defender.getAtk(), true);
					defender.getSkillActor().changeCombatAttr(KGameAttrType.MAX_HP, (int) (defender.getMaxHp() * para - defender.getMaxHp()), true);
					defender.getSkillActor().changeCombatAttr(KGameAttrType.DEF, defender.getDef() * para - defender.getDef(), true);
					defender.increaseHp(defender.getMaxHp() - defender.getCurrentHp());
				}
			}
			int maxLv = 0;
			for (int i = 0; i < list.size(); i++) {
				ICombatMember member = list.get(i);
				if (maxLv < member.getLv()) {
					maxLv = member.getLv();
				}
			}
			int maxHpMultiple = KSupportFactory.getRoleModuleSupport().getHpMultiple(maxLv);
			List<ICombatMember> petList = combat.getCombatMemberByType(ICombatMember.MEMBER_TYPE_PET);
			if (petList != null) {
				list.addAll(petList);
			}
			petList = combat.getCombatMemberByType(ICombatMember.MEMBER_TYPE_PET_MONSTER);
			if (petList != null) {
				list.addAll(petList);
			}
			ICombatMember member;
			for (int i = 0; i < list.size(); i++) {
				member = list.get(i);
				switch (member.getMemberType()) {
				case ICombatMember.MEMBER_TYPE_ROLE:
				case ICombatMember.MEMBER_TYPE_ROLE_MONSTER:
				case ICombatMember.MEMBER_TYPE_PET:
				case ICombatMember.MEMBER_TYPE_PET_MONSTER:
					long preMaxHp = member.getMaxHp();
					int maxHpInc = (int) (maxHpMultiple * member.getMaxHp() - member.getMaxHp());
					member.getSkillActor().changeCombatAttr(KGameAttrType.MAX_HP, maxHpInc, true);
					if (preMaxHp == member.getCurrentHp()) {
						// 有些战斗不是满血进入战斗的，所以如果之前的maxHp和currentHp不一样，则不需要改变血量，用回已经设定的血量
						member.increaseHp(maxHpInc);
					} else {
						member.increaseHp(KCombatConfig.getCompetitionHpMultiple() * member.getCurrentHp() - member.getCurrentHp());
					}
//					member.blockMount();
//					member.blockSuperSkill();
					break;
				}
			}
		}
	}

}
