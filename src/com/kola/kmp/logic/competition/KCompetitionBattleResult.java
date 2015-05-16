package com.kola.kmp.logic.competition;

/**
 * 竞技场战斗结果数据
 * @author zhaizl
 *
 */
public class KCompetitionBattleResult {
	/**
	 * 挑战者角色ID
	 */
	public long challengeRoleId;
	/**
	 * 被挑战者角色ID
	 */
	public long defenderRoleId;
	/**
	 * 被挑战者战斗前的排名
	 */
	public int defenderRanking;
	
	/**
	 * 挑战者是否胜利
	 */
	public boolean isWin;
	
	/**
	 * 战斗耗时
	 */
	public long battleTime;

}
