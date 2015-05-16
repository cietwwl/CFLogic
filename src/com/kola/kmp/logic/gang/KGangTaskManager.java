package com.kola.kmp.logic.gang;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.kola.kmp.logic.util.DayClearTask;
import com.kola.kmp.logic.util.HourClearTask;

/**
 * <pre>
 * 本类管理模块定义的所有任务
 * 
 * @author CamusHuang
 * @creation 2013-1-9 下午2:39:58
 * </pre>
 */
class KGangTaskManager {

	private KGangTaskManager() {
	}

	private static final Logger _LOGGER = KGameLogger.getLogger(KGangTaskManager.class);

	static void notifyCacheLoadComplete() {
		// 军团跨天触发任务
		KGangDayTask.instance.start();
		// 军团长自动禅让检查
		KGangHourTask.instance.start();
	}

	/**
	 * <pre>
	 * 军团跨天触发任务
	 * 每天凌晨00:00:03清0
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-28 下午6:35:32
	 * </pre>
	 */
	static class KGangDayTask extends DayClearTask {

		static final KGangDayTask instance = new KGangDayTask(3 * Timer.ONE_SECOND);

		private KGangDayTask(long delay) {
			super(delay);
		}

		public void doWork() throws KGameServerException {
			KGangLogic.notifyForDayTask();
		}

		@Override
		public String getNameCN() {
			return "军团数据跨天清0任务";
		}
	}

	/**
	 * <pre>
	 * 军团小时触发任务
	 * 每小时hh:00:03清0
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-28 下午6:35:32
	 * </pre>
	 */
	static class KGangHourTask extends HourClearTask {

		static final KGangHourTask instance = new KGangHourTask(3 * Timer.ONE_SECOND);

		private KGangHourTask(long delay) {
			super(delay);
		}

		public void doWork() throws KGameServerException {
			KGangLogic.notifyForHourTask();
		}

		@Override
		public String getNameCN() {
			return "军团小时任务";
		}
	}
}
