package com.kola.kmp.logic.rank.teampvp;

import com.kola.kmp.logic.support.TeamPVPRankSupport;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2013-7-5 下午9:00:40
 * </pre>
 */
public class KTeamPVPSupportImpl implements TeamPVPRankSupport {
	
	/**
	 * <pre>
	 * 队伍所属段位、段级、胜点变化时通知
	 * 会根据数据新增入榜
	 * 
	 * @param tempId
	 * @param tempName
	 * @param type 当前段位
	 * @param lv 段级
	 * @param exp 胜点
	 * @param battlePow 队伍战力
	 * @param leaderRoleId
	 * @param leaderRoleName
	 * @param leaderRoleVip
	 * @param memRoleId
	 * @param memRoleName
	 * @param memRoleVip
	 * @author CamusHuang
	 * @creation 2014-9-3 下午3:51:17
	 * </pre>
	 */
	public void notifyTempChange(long tempId, String tempName, KTeamPVPRankTypeEnum type, int lv, int exp, int battlePow, long leaderRoleId, String leaderRoleName, int leaderRoleVip,
			long memRoleId, String memRoleName, int memRoleVip) {
		KTeamPVPRankLogic.notifyTempChange(tempId, tempName, type, lv, exp, battlePow, leaderRoleId, leaderRoleName, leaderRoleVip, memRoleId, memRoleName, memRoleVip);
	}

	/**
	 * <pre>
	 * 单纯队伍队长、队员变更时通知
	 * 只更新现存于排行榜中的队伍的信息，不新增入榜
	 * 
	 * @param tempId
	 * @param type 当前段位
	 * @param leaderRoleId
	 * @param leaderRoleName
	 * @param leaderRoleVip
	 * @param memRoleId
	 * @param memRoleName
	 * @param memRoleVip
	 * @author CamusHuang
	 * @creation 2014-9-3 下午3:52:57
	 * </pre>
	 */
	public void resetTeamMemChange(long tempId, KTeamPVPRankTypeEnum type, long leaderRoleId, String leaderRoleName, int leaderRoleVip, long memRoleId, String memRoleName, int memRoleVip) {
		KTeamPVPRankLogic.resetTeamMemChange(tempId, type, leaderRoleId, leaderRoleName, leaderRoleVip, memRoleId, memRoleName, memRoleVip);	
	}

	/**
	 * <pre>
	 * 单纯队伍战力变化时通知
	 * 只更新现存于排行榜中的队伍的信息，不新增入榜
	 * 
	 * @param tempId
	 * @param type 当前段位
	 * @param battlePower
	 * @return
	 * @author CamusHuang
	 * @creation 2013-7-7 上午11:43:58
	 * </pre>
	 */
	public void resetTeamBattlePower(long tempId, KTeamPVPRankTypeEnum type, int battlePower) {
		KTeamPVPRankLogic.resetTeamBattlePower(tempId, type, battlePower);
	}

	/**
	 * <pre>
	 * 队伍解散
	 * 
	 * @param gangId
	 * @author CamusHuang
	 * @creation 2013-8-28 下午3:46:23
	 * </pre>
	 */
	public void notifyTeampDelete(long tempId) {
		KTeamPVPRankLogic.notifyTeampDelete(tempId);
	}	
	
	@Override
	public int checkRank(KTeamPVPRankTypeEnum ranType, long tempId) {
		return KTeamPVPRankLogic.checkRank(ranType, tempId);
	}
	
	@Override
	public int checkRank(long tempId) {
		return KTeamPVPRankLogic.checkRank(tempId);
	}

	@Override
	public TeamPVPRankElement getRankElement(KTeamPVPRankTypeEnum ranType, long tempId) {
		return KTeamPVPRankLogic.getRankElement(ranType, tempId);
	}

	@Override
	public TeamPVPRankElement getRankElement(long tempId) {
		return KTeamPVPRankLogic.getRankElement(tempId);
	}
	
	@Override
	public TeamPVPRankElement getRankElement(KTeamPVPRankTypeEnum ranType, int rank) {
		return null;
	}

}
