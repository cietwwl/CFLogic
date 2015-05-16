package com.kola.kmp.logic.activity.mineral;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.kola.kmp.logic.activity.mineral.message.KPushMsg;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.util.DayClearTask;

/**
 * <pre>
 * 本类管理模块定义的所有任务
 * 
 * @author CamusHuang
 * @creation 2014-12-8 下午4:48:33
 * </pre>
 */
public class KDigMineralTaskManager {

	public static final Logger _LOGGER = KGameLogger.getLogger(KDigMineralTaskManager.class);

	private KDigMineralTaskManager() {
	}

	static void notifyCacheLoadComplete() {
		// 产出任务
		KMineralMinuteTask.submitTask();
		// 跨天清0任务
		KRewardDayTask.instance.start();
	}

	/**
	 * <pre>
	 * 矿区产出任务
	 * 
	 * @author CamusHuang
	 * @creation 2014-12-5 上午10:35:19
	 * </pre>
	 */
	public static class KMineralMinuteTask implements KGameTimerTask {

		public static KMineralMinuteTask instance;

		/**
		 * <pre>
		 * 
		 * @param broadDataList
		 * @author CamusHuang
		 * @creation 2013-8-28 下午9:00:46
		 * </pre>
		 */
		static void submitTask() {
			cancel();
			instance = new KMineralMinuteTask();
			KGame.newTimeSignal(instance, 10, TimeUnit.SECONDS);
		}

		static void cancel() {
			if (instance != null) {
				instance.cancel = true;
			}
		}

		private boolean cancel;

		@Override
		public Object onTimeSignal(KGameTimeSignal timeSignal) throws KGameServerException {

			if (cancel) {
				return null;
			}

			try {
				KDigMineralActivityManager.notifyForProduceTask();
				return null;
			} catch (Exception ex) {
				_LOGGER.error(ex.getMessage(), ex);
				throw new KGameServerException(ex);
			} finally {
				KGame.newTimeSignal(instance, 10, TimeUnit.SECONDS);
			}

		}

		@Override
		public void done(KGameTimeSignal timeSignal) {

		}

		@Override
		public void rejected(RejectedExecutionException ex) {
			_LOGGER.error(ex.getMessage(), ex);
		}

		@Override
		public String getName() {
			return KMineralMinuteTask.class.getSimpleName();
		}
	}

	/**
	 * <pre>
	 * 角色进入活动后提交此任务
	 * 延时同步周围玩家状态
	 * 
	 * @author CamusHuang
	 * @creation 2014-12-5 上午10:35:19
	 * </pre>
	 */
	public static class KMineralSyncTask implements KGameTimerTask {

		static void submit(KRole role){
			KGame.newTimeSignal(new KMineralSyncTask(role.getId()), KDigMineralDataManager.初次同步周围玩家延时, TimeUnit.MILLISECONDS);
		}
		
		private long roleId;

		KMineralSyncTask(long roleId) {
			this.roleId = roleId;
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal timeSignal) throws KGameServerException {

			try {
				KPushMsg.synMineStatusToMe(roleId);
				return null;
			} catch (Exception ex) {
				_LOGGER.error(ex.getMessage(), ex);
				throw new KGameServerException(ex);
			}

		}

		@Override
		public void done(KGameTimeSignal timeSignal) {
		}

		@Override
		public void rejected(RejectedExecutionException ex) {
			_LOGGER.error(ex.getMessage(), ex);
		}

		@Override
		public String getName() {
			return KMineralSyncTask.class.getSimpleName();
		}
	}

	/**
	 * <pre>
	 * 跨天触发任务
	 * 每天凌晨00:00:03清0
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-28 下午6:35:32
	 * </pre>
	 */
	static class KRewardDayTask extends DayClearTask {

		static final KRewardDayTask instance = new KRewardDayTask(3 * Timer.ONE_SECOND);

		private KRewardDayTask(long delay) {
			super(delay);
		}

		public void doWork() throws KGameServerException {
			//
			KDigMineralActivityManager.notifyForDayChange();
		}

		@Override
		public String getNameCN() {
			return "每日挖矿数据跨天清0任务";
		}
	}
}
