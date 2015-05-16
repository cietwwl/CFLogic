package com.kola.kmp.logic.activity.worldboss;

import com.koala.game.util.KGameExcelTable.KGameExcelRow;

/**
 * 
 * @author PERRY CHAN
 */
public class KWorldBossRankReward {

	public final int beginRank;
	public final int endRank;
	public final KWorldBossBaseReward rewardData;


	KWorldBossRankReward(KGameExcelRow row) {
		this.beginRank = row.getInt("beginRank");
		this.endRank = row.getInt("endRank");
		this.rewardData = new KWorldBossBaseReward(row);
	}
}
