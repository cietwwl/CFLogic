package com.kola.kmp.logic.activity.worldboss;

import com.koala.game.util.KGameExcelTable.KGameExcelRow;


public class KWorldBossTemplate {

	public final int bossLv;
	public final int monsterTemplateId;
	public final KWorldBossBaseReward rewardData;
	
	public KWorldBossTemplate(KGameExcelRow row) {
		this.bossLv = row.getInt("BossLv");
		this.monsterTemplateId = row.getInt("ID");
		this.rewardData = new KWorldBossBaseReward(row);
	}
}
