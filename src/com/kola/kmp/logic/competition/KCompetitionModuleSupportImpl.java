package com.kola.kmp.logic.competition;

import java.util.ArrayList;
import java.util.List;

import com.kola.kmp.logic.activity.mineral.KDigMineralActivityManager;
import com.kola.kmp.logic.activity.transport.KTransportManager;
import com.kola.kmp.logic.combat.ICombatCommonResult;
import com.kola.kmp.logic.combat.KCombatType;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPManager;
import com.kola.kmp.logic.gang.reswar.ResWarLogic;
import com.kola.kmp.logic.gang.war.GangWarLogic;
import com.kola.kmp.logic.relationship.IntercourseCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.CompetitionModuleSupport;
import com.kola.kmp.logic.support.KSupportFactory;

public class KCompetitionModuleSupportImpl implements CompetitionModuleSupport {

	@Override
	public KCompetitor getCompetitor(long roleId) {
		return KCompetitionModule.getCompetitionManager().getCompetitorByRoleId(roleId);
	}

	@Override
	public KCompetitor getCompetitor(int ranking) {
		return KCompetitionModule.getCompetitionManager().getCompetitorByRanking(ranking);
	}

	@Override
	public List<KCompetitor> getCurrentRanks(int frontCount) {
		if (frontCount > KCompetitionModule.getCompetitionManager().getCurrentLastRank()) {
			frontCount = KCompetitionModule.getCompetitionManager().getCurrentLastRank();
		}
		List<KCompetitor> list = new ArrayList<KCompetitor>(frontCount);
		for (int i = 1; i <= frontCount; i++) {
			KCompetitor competitor = KCompetitionModule.getCompetitionManager().getCompetitorByRanking(i);
			if (competitor != null) {
				list.add(competitor);
			}
		}
		return list;
	}

	@Override
	public int getCurrentRankOfRole(long roleId) {
		KCompetitor competitor = KCompetitionModule.getCompetitionManager().getCompetitorByRoleId(roleId);
		if (competitor != null) {
			return competitor.getRanking();
		} else {
			return 0;
		}
	}

	@Override
	public void notifyVipLevelUp(long roleId, int preVipLv, int nowVipLv) {
		// TODO Auto-generated method stub
	}

	@Override
	public int[] getCanChallengeTimesOfRole(long roleId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void notifyCompetitionFinish(KRole role, ICombatCommonResult result) {
		if (result.isEscape()) {
			
			KSupportFactory.getMapSupport().processRoleFinishBattleReturnToMap(role);
			
			if (result.getCombatType() == KCombatType.GANG_WAR_PVP) {
				GangWarLogic.notifyPVPBattleFinished(role, result);
			} else if (result.getCombatType() == KCombatType.INTERCOURSE_PVP) {
				IntercourseCenter.notifyPVPBattleFinished(role, result);
			} else if (result.getCombatType() == KCombatType.MINERAL_PVP) {
				KDigMineralActivityManager.notifyPVPBattleFinished(role, result);
			}
		} else {
			if (result.getCombatType() == KCombatType.COMPETITION) {
				KCompetitionModule.getCompetitionManager().notifyBattleFinished(role, result);
			} else if (result.getCombatType() == KCombatType.TRANSPORT_COMBAT) {
				KTransportManager.notifyBattleFinished(role, result);
			} else if (result.getCombatType() == KCombatType.GANG_RESWAR) {
				ResWarLogic.notifyBattleFinished(role, result);
			} else if (result.getCombatType() == KCombatType.GANG_WAR_PVP) {
				GangWarLogic.notifyPVPBattleFinished(role, result);
			} else if (result.getCombatType() == KCombatType.INTERCOURSE_PVP) {
				IntercourseCenter.notifyPVPBattleFinished(role, result);
			} else if (result.getCombatType() == KCombatType.MINERAL_PVP) {
				KDigMineralActivityManager.notifyPVPBattleFinished(role, result);
			}
		}
	}

	@Override
	public String joinCompetitionForGM(long roleId) {
		return KCompetitionModule.getCompetitionManager().joinCompetitionForGM(roleId);
	}

	@Override
	public void checkAndRestCompetitionData(KRole role) {
		KCompetitionModule.getCompetitionManager().checkAndRestCompetitionData(role);
	}

	@Override
	public void notifyBattlePowerChange(KRole role, int battlePower) {
		if (role == null) {
			return;
		}
		KCompetitor competitor = KCompetitionModule.getCompetitionManager().getCompetitorByRoleId(role.getId());
		if (competitor != null) {
			competitor.notifyBattlePowerChange(battlePower);
		}
		KTeamPVPManager.notifyBattlePowerChange(role, battlePower);
	}

}
