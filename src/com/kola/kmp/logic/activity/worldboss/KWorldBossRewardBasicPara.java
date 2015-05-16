package com.kola.kmp.logic.activity.worldboss;

import com.koala.game.util.KGameExcelTable.KGameExcelRow;

/**
 * 
 * @author PERRY CHAN
 */
public class KWorldBossRewardBasicPara {

	public final int lv;
	public final int expBasicPara;
	public final int potentialBasicPara;
	public final int goldBasicPara;
//	public final int rankExpBasicPara;

	public KWorldBossRewardBasicPara(KGameExcelRow row) {
		this.lv = row.getInt("lv");
		this.expBasicPara = row.getInt("expBasicPara");
		this.potentialBasicPara = row.getInt("potentialBasicPara");
		this.goldBasicPara = row.getInt("goldBasicPara");
//		this.rankExpBasicPara = row.getInt("rankExpBasicPara");
	}
}
