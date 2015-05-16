package com.kola.kmp.logic.reward.garden;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;

/**
 * <pre>
 * 本类管理模块定义的所有任务
 * 
 * @author CamusHuang
 * @creation 2013-1-9 下午2:39:58
 * </pre>
 */
public class KGardenTaskManager {

	private KGardenTaskManager() {
	}

	private static final Logger _LOGGER = KGameLogger.getLogger(KGardenTaskManager.class);

	static void notifyCacheLoadComplete() {
		KGame.newTimeSignal(KGardenZombieRfreshDayTask.instance, KGardenDataManager.ZombieRefreshPeriod, TimeUnit.MILLISECONDS);
	}

	/**
	 * <pre>
	 * 每2小时刷新僵尸
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-28 下午6:35:32
	 * </pre>
	 */
	static class KGardenZombieRfreshDayTask implements KGameTimerTask {

		static KGardenZombieRfreshDayTask instance = new KGardenZombieRfreshDayTask();

		private long startTime = System.currentTimeMillis();

		private KGardenZombieRfreshDayTask() {
		}

		@Override
		public String getName() {
			return getClass().getName();
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal arg0) throws KGameServerException {
			try {
				// 执行具体工作
				KGardenCenter.notifyForZombieRefreshTask();
			} catch (Exception ex) {
				_LOGGER.error(ex.getMessage(), ex);
				throw new KGameServerException(ex);
			} finally {
				arg0.getTimer().newTimeSignal(this, KGardenDataManager.ZombieRefreshPeriod, TimeUnit.MILLISECONDS);
			}
			return null;
		}

		@Override
		public void done(KGameTimeSignal timeSignal) {
		}

		@Override
		public void rejected(RejectedExecutionException ex) {
			_LOGGER.error(ex.getMessage(), ex);
		}

		/**
		 * <pre>
		 * 找出最后僵尸刷新时间到目前的所有僵尸刷新时间点 
		 * 
		 * @param lastTime
		 * @param nowTime
		 * @param timeList
		 * @author CamusHuang
		 * @creation 2014-5-2 下午1:42:56
		 * </pre>
		 */
		public void countRefreshTimes(long lastTime, long nowTime, List<Long> timeList) {
			long period = KGardenDataManager.ZombieRefreshPeriod;
			long tempTime = startTime + (((lastTime - startTime) / period) + 1) * period;
			for (; tempTime < nowTime; tempTime += period) {
				timeList.add(tempTime);
			}
		}

	}
}
