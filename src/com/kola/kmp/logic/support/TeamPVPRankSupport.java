package com.kola.kmp.logic.support;

import com.kola.kmp.logic.rank.teampvp.KTeamPVPRankTypeEnum;
import com.kola.kmp.logic.rank.teampvp.TeamPVPRankElement;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-9-1 下午2:58:47
 * </pre>
 */
public interface TeamPVPRankSupport {

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
			long memRoleId, String memRoleName, int memRoleVip);

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
	public void resetTeamMemChange(long tempId, KTeamPVPRankTypeEnum type, long leaderRoleId, String leaderRoleName, int leaderRoleVip, long memRoleId, String memRoleName, int memRoleVip);

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
	public void resetTeamBattlePower(long tempId, KTeamPVPRankTypeEnum type, int battlePower);

	/**
	 * <pre>
	 * 队伍解散
	 * 
	 * @param gangId
	 * @author CamusHuang
	 * @creation 2013-8-28 下午3:46:23
	 * </pre>
	 */
	public void notifyTeampDelete(long tempId);
	
	
	/**
	 * <pre>
	 * 查询队伍在排行榜中的排名
	 * 
	 * @param ranType
	 * @param tempId
	 * @return 0表示未上榜
	 * @author CamusHuang
	 * @creation 2014-2-21 上午11:11:44
	 * </pre>
	 */
	public int checkRank(KTeamPVPRankTypeEnum ranType, long tempId);
	
	/**
	 * <pre>
	 * 查询队伍在排行榜中的排名
	 * 默认从低傍到高榜进行获取
	 * 
	 * @param ranType
	 * @param tempId
	 * @return 0表示未上榜
	 * @author CamusHuang
	 * @creation 2014-2-21 上午11:11:44
	 * </pre>
	 */
	public int checkRank(long tempId);
	
	/**
	 * <pre>
	 * 根据排行榜类型和队伍ID获取榜中元素
	 * 
	 * @param ranType
	 * @param tempId
	 * @return 可能返回null
	 * @author CamusHuang
	 * @creation 2014-9-9 下午12:35:41
	 * </pre>
	 */
	public TeamPVPRankElement getRankElement(KTeamPVPRankTypeEnum ranType, long tempId);
	
	
	/**
	 * <pre>
	 * 根据队伍ID获取榜中元素
	 * 默认从低傍到高榜进行获取
	 * 
	 * @param tempId
	 * @return 可能返回null
	 * @author CamusHuang
	 * @creation 2014-9-9 下午12:35:41
	 * </pre>
	 */
	public TeamPVPRankElement getRankElement(long tempId);
	
	/**
	 * <pre>
	 * 根据类型和排名获取榜中元素
	 * </pre>
	 * @param ranType
	 * @param rank
	 * @return
	 */
	public TeamPVPRankElement getRankElement(KTeamPVPRankTypeEnum ranType, int rank);
}
