package com.kola.kmp.logic.competition;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimer;
import com.koala.game.timer.KGameTimerTask;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.util.WeekClearTask;
/**
 *
 * @author PERRY CHAN
 */
public class KCompetitionSettleTask extends WeekClearTask{

    public KCompetitionSettleTask(int dayOfWeek) {
		super(dayOfWeek);
	}

    
    
	public KCompetitionSettleTask(int dayOfWeek, long delay) {
		super(dayOfWeek, delay);
	}

	@Override
	public void done(KGameTimeSignal timeSignal) {	
		
	}

	@Override
	public void rejected(RejectedExecutionException e) {		
	}

	@Override
	public String getNameCN() {
		return KCompetitionSettleTask.class.getName();
	}

	@Override
	public void doWork() throws KGameServerException {
		KCompetitionModule.getCompetitionManager().settleCompetitionWeekReward();
	}

}
