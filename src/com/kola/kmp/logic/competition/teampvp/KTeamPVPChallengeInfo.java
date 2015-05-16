package com.kola.kmp.logic.competition.teampvp;

import com.koala.game.util.KGameExcelTable.KGameExcelRow;

/**
 * 
 * 挑战的一些信息集合，包括胜利所能获得的胜点，失败扣减的胜点。。。等等
 * 
 * @author PERRY CHAN
 */
public class KTeamPVPChallengeInfo {

	/**
	 * 挑战的次数
	 */
	public final int challengeTime;
	/**
	 * 胜利获得的胜点
	 */
	public final int winIncScore;
	/**
	 * 失败扣减的胜点
	 */
	public final int loseDecScore;
	/**
	 * 每场战斗的荣誉奖励比重
	 */
	public final float honorWeight;
	/**
	 * 匹配的最小排名
	 */
	public final int matchMinRanking;
	/**
	 * 匹配的最大排名
	 */
	public final int matchMaxRanking;
	
	public KTeamPVPChallengeInfo(KGameExcelRow resultRow, KGameExcelRow matchRuleRow) {
		this.challengeTime = resultRow.getInt("challengeTimes");
		this.winIncScore = resultRow.getInt("winIncScore");
		this.loseDecScore = resultRow.getInt("loseDecScore");
		this.honorWeight = resultRow.getFloat("powerweight");
		this.matchMinRanking = matchRuleRow.getInt("minRanking");
		this.matchMaxRanking = matchRuleRow.getInt("maxRanking");
	}
}
