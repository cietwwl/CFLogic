package com.kola.kmp.logic.competition;

import com.koala.game.exception.KGameServerException;
import com.kola.kmp.logic.util.DayClearTask;

/**
 *
 * @author PERRY CHAN
 */
//public class KCompetitionTask implements KGameTimerTask{
public class KCompetitionTask extends DayClearTask {

	public static void submit() {
		KCompetitionTask task = new KCompetitionTask();
		task.start();
	}
	
	public KCompetitionTask() {
		super();
	}
	
	@Override
	public void doWork() throws KGameServerException {
		KCompetitionModule.getCompetitionManager().resetChallengeTime();
		KCompetitionModule._LOGGER.info("清除挑战次数完成！！！");
	}

	@Override
	public String getNameCN() {
		return "竞技场重置挑战次数任务";
	}
	
	

}
