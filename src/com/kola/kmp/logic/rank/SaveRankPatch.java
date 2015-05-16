package com.kola.kmp.logic.rank;

import java.text.ParseException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.RunTimeTask;
import com.kola.kmp.logic.rank.gang.KGangRankLogic;
import com.kola.kmp.logic.rank.teampvp.KTeamPVPRankLogic;

/**
 * <pre>
 * 这是一个即时保存排行榜的补丁
 * 
 * @author CamusHuang
 * @creation 2013-7-24 上午2:58:19
 * </pre>
 */
public class SaveRankPatch implements RunTimeTask {
	private static Logger _LOGGER = KGameLogger.getLogger(SaveRankPatch.class);

	public String run(String date) {
		long delayTime;
		try {
			delayTime = UtilTool.DATE_FORMAT3.parse(date).getTime() - System.currentTimeMillis();
		} catch (ParseException e) {
			return "异常="+e.getMessage();
		}
		KGame.newTimeSignal(RankSaveTask.instance, delayTime, TimeUnit.MILLISECONDS);
		return "执行完毕";
	}

	/**
	 * <pre>
	 * 保存排行榜数据
	 * 
	 * @author CamusHuang
	 * @creation 2013-1-24 上午11:52:58
	 * </pre>
	 */
	private static class RankSaveTask implements KGameTimerTask {

		private static final RankSaveTask instance = new RankSaveTask();

		private RankSaveTask() {
		}

		@Override
		public void done(KGameTimeSignal arg0) {
		}

		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal arg0) throws KGameServerException {
			try {
				KRankLogic.saveRankByGM(true, true, true);
			} catch (Exception ex) {
				_LOGGER.error(ex.getMessage(), ex);
				throw new KGameServerException(ex);
			} finally {
			}
			
			try {
				KTeamPVPRankLogic.saveRankByGM(true, true, true);
			} catch (Exception ex) {
				_LOGGER.error(ex.getMessage(), ex);
				throw new KGameServerException(ex);
			} finally {
			}
			
			try {
				KGangRankLogic.saveRankByGM(true, true, true);
			} catch (Exception ex) {
				_LOGGER.error(ex.getMessage(), ex);
				throw new KGameServerException(ex);
			} finally {
			}
			return null;
		}

		@Override
		public void rejected(RejectedExecutionException ex) {
			_LOGGER.error(ex.getMessage(), ex);
		}
	}
}
