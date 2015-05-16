package com.kola.kmp.logic.support;

import java.util.List;

import com.kola.kmp.logic.combat.ICombatCommonResult;
import com.kola.kmp.logic.competition.KCompetitor;
import com.kola.kmp.logic.role.KRole;

public interface CompetitionModuleSupport {

	/**
	 * 通知竞技场模块战斗结束
	 * @param role
	 * @param defenderId
	 * @param win
	 * @param ranking
	 */
	public void notifyCompetitionFinish(KRole role, ICombatCommonResult result);

	/**
	 * 
	 * @param roleId
	 * @return
	 */
	public KCompetitor getCompetitor(long roleId);

	/**
	 * 
	 * @param ranking
	 * @return
	 */
	public KCompetitor getCompetitor(int ranking);

	/**
	 * <pre>
	 * 获取前frontCount名的即时排名
	 * 不足则返回现有数量
	 * 
	 * 队首为第一名
	 * 
	 * @param frontCount
	 * @return 不可为NULL
	 * @author CamusHuang
	 * @creation 2013-6-11 上午11:04:04
	 * </pre>
	 */
	public List<KCompetitor> getCurrentRanks(int frontCount);

	/**
	 * 
	 * @param roleId
	 * @return
	 */
	public int getCurrentRankOfRole(long roleId);

	/**
	 * 
	 * @param roleId
	 * @param preVipLv
	 * @param nowVipLv
	 */
	public void notifyVipLevelUp(long roleId, int preVipLv, int nowVipLv);

	/**
	 * 
	 * 获取角色今天的可挑战次数以及剩余挑战次数
	 * 
	 * @param roleId
	 * @return int[], int[0]表示总共的可挑战次数, int[1]表示剩余的可挑战次数。
	 *         如果该角色仍未可以参加竞技场，则默认两个都是0
	 */
	public int[] getCanChallengeTimesOfRole(long roleId);

	/**
	 * <pre>
	 * 如果指定角色不存在于竞技场，则将角色加入竞技场
	 * 
	 * @deprecated GM需求
	 * @param id
	 * @return
	 * @author CamusHuang
	 * @creation 2014-6-9 下午4:42:34
	 * </pre>
	 */
	public String joinCompetitionForGM(long roleId);
	
	/**
	 * 检测并重置竞技场数据
	 * @param role
	 */
	public void checkAndRestCompetitionData(KRole role);
	
	/**
	 * 通知竞技场排行榜战力变化
	 * @param role
	 * @param battlePower
	 */
	public void notifyBattlePowerChange(KRole role, int battlePower);
}
