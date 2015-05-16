package com.kola.kmp.logic.competition;

import com.koala.game.exception.KGameServerException;
import com.kola.kmp.logic.util.DayClearTask;

public class KCompetitionSettleTodayRewardTask extends DayClearTask{

	
	
	public KCompetitionSettleTodayRewardTask(long delay) {
		super(delay);
	}

	@Override
	public String getNameCN() {
		return KCompetitionSettleTodayRewardTask.class.getName();
	}

	@Override
	public void doWork() throws KGameServerException {
		KCompetitionModule.getCompetitionManager().settleCompetitionTodayReward();
	}

}
