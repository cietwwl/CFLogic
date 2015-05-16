package com.kola.kmp.logic.relationship;

import javax.management.timer.Timer;

import com.koala.game.exception.KGameServerException;
import com.kola.kmp.logic.util.HourClearTask;

/**
 * <pre>
 * 本类管理模块定义的所有任务
 * 
 * @author CamusHuang
 * @creation 2014-12-5 上午10:34:09
 * </pre>
 */
public class KRelationShipTaskManager {

	private KRelationShipTaskManager() {
	}

	static void notifyCacheLoadComplete() {
		// 小时触发任务
		KIntercourseHourTask.instance.start();
	}

	/**
	 * <pre>
	 * 小时触发任务
	 * 
	 * @author CamusHuang
	 * @creation 2014-12-5 上午10:35:19
	 * </pre>
	 */
	public static class KIntercourseHourTask extends HourClearTask {

		public static final KIntercourseHourTask instance = new KIntercourseHourTask(3 * Timer.ONE_SECOND);

		private KIntercourseHourTask(long delay) {
			super(delay);
		}

		public void doWork() throws KGameServerException {
			IntercourseCenter.notifyForHourTask();
		}

		@Override
		public String getNameCN() {
			return "切磋小时战报任务";
		}
	}
}
