package com.kola.kmp.logic.competition;

import com.koala.game.exception.KGameServerException;
import com.kola.kmp.logic.util.HourClearTask;

/**
 *
 * @author PERRY CHAN
 */
public class KCompetitionSaveTask extends HourClearTask {

	

	@Override
	public String getNameCN() {
		return KCompetitionSaveTask.class.getName();
	}

	@Override
	public void doWork() throws KGameServerException {
		KCompetitionModule.getCompetitionManager().save();
	}

}
