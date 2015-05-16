package com.kola.kmp.logic.combat.impl;

import java.util.ArrayList;
import java.util.List;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.ICombatPlugin;
import com.kola.kmp.logic.combat.KCombatType;
import com.kola.kmp.logic.combat.api.ICombatObjectBase;
import com.kola.kmp.logic.combat.api.ICombatSkillActor;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 
 * @author PERRY CHAN
 */
public class KTeamPVPCombatPlugin implements ICombatPlugin {

	@Override
	public void beforeStart(ICombat combat) {
		if (combat.getCombatType() == KCombatType.TEAM_PVP) {
			List<ICombatMember> list = new ArrayList<ICombatMember>();
			list.addAll(combat.getCombatMemberByType(ICombatMember.MEMBER_TYPE_ROLE));
			List<ICombatMember> teammates = combat.getCombatMemberByType(ICombatMember.MEMBER_TYPE_TEAM_MATE_ROLE);
			if (teammates != null) {
				list.addAll(teammates);
			}
			List<ICombatMember> enemies = combat.getCombatMemberByType(ICombatMember.MEMBER_TYPE_ROLE_MONSTER);
			if (enemies.get(0).getSrcObjType() == ICombatMember.MEMBER_TYPE_ROLE) {
				int cTotalBattlePower = 0;
				int dTotalBattlePower = 0;
				for(int i = 0; i < list.size(); i++) {
					cTotalBattlePower += KCombatManager.getBattlePower(list.get(i).getSrcObjId());
				}
				for (int i = 0; i < enemies.size(); i++) {
					dTotalBattlePower += KCombatManager.getBattlePower(enemies.get(i).getSrcObjId());
				}
				int para = Math.min(KCombatConfig.getMaxMultipleOfCompetition(), (int) Math.round(Math.pow(dTotalBattlePower / cTotalBattlePower, KCombatConfig.getCalParaOfTeamPVP())));
				if (para > 1) {
					ICombatMember member;
					ICombatSkillActor actor;
					for (int i = 0; i < enemies.size(); i++) {
						member = enemies.get(i);
						actor = member.getSkillActor();
						actor.changeCombatAttr(KGameAttrType.ATK, member.getAtk() * para - member.getAtk(), true);
						actor.changeCombatAttr(KGameAttrType.MAX_HP, (int) (member.getMaxHp() * para - member.getMaxHp()), true);
						actor.changeCombatAttr(KGameAttrType.DEF, member.getDef() * para - member.getDef(), true);
					}
				}
			}
			list.addAll(enemies);
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
			petList = combat.getCombatMemberByType(ICombatMember.MEMBER_TYPE_TEAM_MATE_PET);
			if (petList != null) {
				list.addAll(petList);
			}
			ICombatMember member;
			for (int i = 0; i < list.size(); i++) {
				member = list.get(i);
				switch (member.getMemberType()) {
				case ICombatMember.MEMBER_TYPE_ROLE:
				case ICombatMember.MEMBER_TYPE_TEAM_MATE_ROLE:
				case ICombatMember.MEMBER_TYPE_ROLE_MONSTER:
				case ICombatMember.MEMBER_TYPE_PET:
				case ICombatMember.MEMBER_TYPE_PET_MONSTER:
				case ICombatMember.MEMBER_TYPE_TEAM_MATE_PET:
					if (member.getSrcObjType() == ICombatObjectBase.OBJECT_TYPE_ROLE || member.getSrcObjType() == ICombatObjectBase.OBJECT_TYPE_PET) {
						int maxHpInc = (int) (maxHpMultiple * member.getMaxHp() - member.getMaxHp());
						member.getSkillActor().changeCombatAttr(KGameAttrType.MAX_HP, maxHpInc, true);
						member.increaseHp(maxHpInc);
//						member.blockMount();
//						member.blockSuperSkill();
					}
					break;
				}
			}
		}
	}

}
