package com.kola.kmp.logic.gang.war;

import com.kola.kmp.logic.combat.ICombatRoleSideHpUpdater;
import com.kola.kmp.logic.combat.KCombatType;
import com.kola.kmp.logic.gang.war.GangWarRound.GangRace;
import com.kola.kmp.logic.gang.war.GangWarRound.GangRace.RaceGangData;
import com.kola.kmp.logic.gang.war.GangWarRound.GangRace.RaceGangData.RaceMemberData;
import com.kola.kmp.logic.pet.KPet;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class PVPRoleSideHpUpdater implements ICombatRoleSideHpUpdater {

	@Override
	public KCombatType getCombatTypeResponse() {
		return KCombatType.GANG_WAR_PVP;
	}

	@Override
	public boolean handleRoleHpUpdate() {
		return true;
	}

	@Override
	public long getRoleHp(long roleId) {

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		if (role == null) {
			return 0;
		}

		long maxHP = role.getMaxHp();

		RaceMemberData mem = getMem(role);
		if (mem == null) {
			return maxHP;
		}

		long chp = mem.getReleaseHP();
		if (chp < 1) {
			return maxHP;
		}
		return chp;

	}

	@Override
	public boolean handlePetHpUpdate() {
		return true;
	}

	@Override
	public long getPetHp(long roleId, long petId) {

		KPet pet = KSupportFactory.getPetModuleSupport().getFightingPet(roleId);
		if (pet == null) {
			return 0;
		}

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		if (role == null) {
			return 0;
		}

		RaceMemberData mem = getMem(role);
		if (mem == null) {
			return 0;
		}

		long chp = mem.getPetReleaseHP(petId);
		if (chp < 1) {
			return pet.getMaxHp();
		}
		return chp;
	}

	static RaceMemberData getMem(KRole role) {

		long gangId = KSupportFactory.getGangSupport().getGangIdByRoleId(role.getId());
		if (gangId < 1) {
			return null;
		}
		// 军团是否已报名？是否已开战？是否进入对战？
		{
			// 可入状态
			if (GangWarStatusManager.getNowStatus() == GangWarStatusEnum.WAR_WAIT_NOW) {
				// result.tips = GangWarTips.本场军团战未开始;
				return null;
			}
			if (GangWarStatusManager.getNowStatus() == GangWarStatusEnum.WAR_ROUND_READY_NOW) {
				// result.tips = GangWarTips.本场军团战准备中;
				return null;
			}
			if (GangWarStatusManager.getNowStatus() != GangWarStatusEnum.WAR_ROUND_START_NOW) {
				// result.tips = GangWarTips.本场军团战已结束;
				return null;
			}

			// 是否入围
			GangData gangData = GangWarDataCenter.getWarGang(gangId);
			if (gangData == null) {
				return null;
			}

			GangWarRound warRound = GangWarDataCenter.getNowRoundData();
			if (warRound == null) {
				return null;
			}
			GangRace gangRace = warRound.getRaceByGangId(gangId);
			// 是否胜负已分？
			if (gangRace.getWinner() != null) {
				return null;
			}

			//
			RaceGangData raceGangData = gangRace.getRaceGang(gangId);
			// 判断角色状态
			return raceGangData.getMem(role.getId());
		}
	}

}
