package com.kola.kmp.logic.competition.teampvp;

import java.util.Calendar;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.kola.kgame.cache.util.UtilTool;

/**
 * 
 * @author PERRY CHAN
 */
public class KTeamPVPTask implements KGameTimerTask {

	private int getDelaySecond() {
		long tomorrowStart = UtilTool.getTommorowStart().getTimeInMillis();
		int seconds = (int) TimeUnit.SECONDS.convert(tomorrowStart - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		seconds += 5;
		return seconds;
	}
	
	void start() {
		int seconds = this.getDelaySecond();
		KGame.newTimeSignal(this, seconds, TimeUnit.SECONDS);
	}
	
	@Override
	public String getName() {
		return "KCompetitionTeamPVPTask";
	}

	@Override
	public Object onTimeSignal(KGameTimeSignal timeSignal) throws KGameServerException {
		KTeamPVPManager.notifyDayChange();
		KTeamPVPManager.saveAlLTeam();
		int seconds = this.getDelaySecond();
		timeSignal.getTimer().newTimeSignal(this, seconds, TimeUnit.SECONDS);
		return Boolean.TRUE;
	}

	@Override
	public void done(KGameTimeSignal timeSignal) {
		
	}

	@Override
	public void rejected(RejectedExecutionException e) {
		
	}

}
