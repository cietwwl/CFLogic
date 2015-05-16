package com.kola.kmp.logic.gang.reswar;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.kola.kmp.logic.chat.KWordBroadcastType;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * <pre>
 * 本类管理模块定义的所有任务
 * 
 * @author CamusHuang
 * @creation 2013-1-9 下午2:39:58
 * </pre>
 */
class ResWarTaskManager {

	private ResWarTaskManager() {
	}

	private static final Logger _LOGGER = KGameLogger.getLogger(ResWarTaskManager.class);

	static void notifyCacheLoadComplete() {
		KGame.newTimeSignal(KCityBidRankSaveTask.instance, 3, TimeUnit.SECONDS);
	}

	/**
	 * <pre>
	 * 停止所有时效任务
	 * 
	 * @author CamusHuang
	 * @creation 2013-10-11 下午11:24:17
	 * </pre>
	 */
	static void stopGangResWar() {

		GangResWarStatusTask.cancel();
		JudgeInWarTask.cancel();
		ScoreProduceTask.cancel();
		WarErrorScanTask.cancel();
		CityBroadcastTask.cancel();
		WorldBroadcastTask.cancelAll();
	}

	/**
	 * <pre>
	 * 军团资源战状态定时切换任务
	 * 
	 * @author CamusHuang
	 * @creation 2013-1-24 上午11:52:58
	 * </pre>
	 */
	static class GangResWarStatusTask implements KGameTimerTask {

		/** 军团资源战状态定时切换任务 */
		private static GangResWarStatusTask instance;

		/**
		 * <pre>
		 * 军团资源战状态定时切换任务
		 * 
		 * @param status
		 * @param delayTime
		 * @author CamusHuang
		 * @creation 2013-8-28 下午9:00:53
		 * </pre>
		 */
		static void submitTast(ResWarStatusEnum status, long delayTime) {
			if (delayTime < Timer.ONE_SECOND) {
				delayTime = Timer.ONE_SECOND;
			}
			ResWarDataCenter.rwLock.writeLock().lock();
			try {
				cancel();
				instance = new GangResWarStatusTask(status);
				KGame.newTimeSignal(instance, delayTime, TimeUnit.MILLISECONDS);
			} finally {
				ResWarDataCenter.rwLock.writeLock().unlock();
			}
		}

		static ResWarStatusEnum getNextWarStatusEnum() {
			ResWarDataCenter.rwLock.readLock().lock();
			try {
				if (instance != null) {
					return instance.status;
				}
			} finally {
				ResWarDataCenter.rwLock.readLock().unlock();
			}
			return null;
		}

		static void cancel() {
			ResWarDataCenter.rwLock.writeLock().lock();
			try {
				if (instance != null) {
					instance.cancel = true;
				}
			} finally {
				ResWarDataCenter.rwLock.writeLock().unlock();
			}
		}

		/**
		 * <pre>
		 * 加速当前状态结束
		 * 
		 * @author CamusHuang
		 * @creation 2013-10-11 下午9:56:28
		 * </pre>
		 */
		static void speedToRun() {
			ResWarDataCenter.rwLock.writeLock().lock();
			try {
				if (instance == null || instance.cancel) {
					return;
				}

				// 提前执行
				try {
					instance.onTimeSignal(null);
				} catch (KGameServerException e) {
					ResWarDataCenter.RESWAR_LOGGER.error(e.getMessage(), e);
				}
			} finally {
				ResWarDataCenter.rwLock.writeLock().unlock();
			}
		}

		private final ResWarStatusEnum status;

		private boolean cancel;

		private GangResWarStatusTask(ResWarStatusEnum status) {
			this.status = status;
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal arg0) throws KGameServerException {
			ResWarDataCenter.rwLock.writeLock().lock();
			try {
				if (cancel) {
					return null;
				}

				// 保证不会被时效重复执行
				cancel = true;

				try {
					ResWarStatusManager.TaskNotify_Status(status);
				} catch (Exception ex) {
					_LOGGER.error(ex.getMessage(), ex);
					throw new KGameServerException(ex);
				}
				return null;
			} finally {
				ResWarDataCenter.rwLock.writeLock().unlock();
			}
		}

		@Override
		public void done(KGameTimeSignal arg0) {
		}

		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		@Override
		public void rejected(RejectedExecutionException ex) {
			_LOGGER.error(ex.getMessage(), ex);
		}
	}

	/**
	 * <pre>
	 * 竞价排行榜定时保存任务
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-28 下午6:35:32
	 * </pre>
	 */
	static class KCityBidRankSaveTask implements KGameTimerTask {

		static KCityBidRankSaveTask instance = new KCityBidRankSaveTask();

		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal timeSignal) throws KGameServerException {
			try {
				for (ResWarCity city : ResWarDataCenter.allCityMap.values()) {
					city.bidRank.onTimeSignalForSave();
				}
				return null;
			} catch (Exception ex) {
				_LOGGER.error(ex.getMessage(), ex);
				throw new KGameServerException(ex);
			} finally {
				KGame.newTimeSignal(instance, KResWarConfig.BID_RANK_SAVE_PERIOD, TimeUnit.MILLISECONDS);
			}
		}

		@Override
		public void done(KGameTimeSignal timeSignal) {
		}

		@Override
		public void rejected(RejectedExecutionException ex) {
			_LOGGER.error(ex.getMessage(), ex);
		}
	}

	/**
	 * <pre>
	 * 积分生产时效
	 * 对战开始时提交，对战结束时取消
	 * 
	 * @author CamusHuang
	 * @creation 2013-1-24 上午11:52:58
	 * </pre>
	 */
	static class ScoreProduceTask implements KGameTimerTask {

		static ScoreProduceTask instance;

		/**
		 * <pre>
		 * 积分生产时效
		 * 
		 * @param broadDataList
		 * @author CamusHuang
		 * @creation 2013-8-28 下午9:00:46
		 * </pre>
		 */
		static void submitTask() {
			ResWarDataCenter.rwLock.writeLock().lock();
			try {
				cancel();
				instance = new ScoreProduceTask();
				KGame.newTimeSignal(instance, KResWarConfig.AddScorePeroid, TimeUnit.SECONDS);
			} finally {
				ResWarDataCenter.rwLock.writeLock().unlock();
			}
		}

		static void cancel() {
			ResWarDataCenter.rwLock.writeLock().lock();
			try {
				if (instance != null) {
					instance.cancel = true;
				}
			} finally {
				ResWarDataCenter.rwLock.writeLock().unlock();
			}
		}

		//

		private boolean cancel;

		private ScoreProduceTask() {
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal arg0) throws KGameServerException {
			ResWarDataCenter.rwLock.writeLock().lock();
			try {
				if (cancel) {
					return null;
				}
				try {
					// 通知各军团生成积分
					ResWarLogic.onTimeSignalForProduceScore();
					return null;
				} catch (Exception ex) {
					_LOGGER.error(ex.getMessage(), ex);
					throw new KGameServerException(ex);
				} finally {
					KGame.newTimeSignal(instance, KResWarConfig.AddScorePeroid, TimeUnit.SECONDS);
				}

			} finally {
				ResWarDataCenter.rwLock.writeLock().unlock();
			}
		}

		@Override
		public void done(KGameTimeSignal arg0) {
		}

		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		@Override
		public void rejected(RejectedExecutionException ex) {
			_LOGGER.error(ex.getMessage(), ex);
		}
	}

	/**
	 * <pre>
	 * 用于对战中进行扫描，完成以下任务内容:
	 * 1.执行裁决
	 * 
	 * @author CamusHuang
	 * @creation 2013-1-24 上午11:52:58
	 * </pre>
	 */
	static class JudgeInWarTask implements KGameTimerTask {

		static JudgeInWarTask instance;

		/**
		 * <pre>
		 * 
		 * @param broadDataList
		 * @author CamusHuang
		 * @creation 2013-8-28 下午9:00:46
		 * </pre>
		 */
		static void submitTask() {
			ResWarDataCenter.rwLock.writeLock().lock();
			try {
				cancel();
				instance = new JudgeInWarTask();
				KGame.newTimeSignal(instance, KResWarConfig.WarScanStartDelay, TimeUnit.MILLISECONDS);
			} finally {
				ResWarDataCenter.rwLock.writeLock().unlock();
			}
		}

		static void cancel() {
			ResWarDataCenter.rwLock.writeLock().lock();
			try {
				if (instance != null) {
					instance.cancel = true;
				}
			} finally {
				ResWarDataCenter.rwLock.writeLock().unlock();
			}
		}

		//

		private boolean cancel;

		private JudgeInWarTask() {
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal arg0) throws KGameServerException {

			boolean isAllGroupEnd = false;// 是否所有分组均已决出胜负

			ResWarDataCenter.rwLock.writeLock().lock();
			try {
				if (cancel) {
					return null;
				}
				try {
					isAllGroupEnd = ResWarLogic.onTimeSignalForJudgeInWar();
					return null;
				} catch (Exception ex) {
					_LOGGER.error(ex.getMessage(), ex);
					throw new KGameServerException(ex);
				} finally {
					KGame.newTimeSignal(instance, KResWarConfig.WarScanPeroid, TimeUnit.MILLISECONDS);
				}

			} finally {
				ResWarDataCenter.rwLock.writeLock().unlock();

				if (isAllGroupEnd) {
					// 加速单场结束
					GangResWarStatusTask.speedToRun();
				}
			}
		}

		@Override
		public void done(KGameTimeSignal arg0) {
		}

		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		@Override
		public void rejected(RejectedExecutionException ex) {
			_LOGGER.error(ex.getMessage(), ex);
		}
	}

	/**
	 * <pre>
	 * 定时扫描处理PVP玩家非战斗、离线等错误状态
	 * 处理占领超时的资源点
	 * 不干预PVP流程，目的在于释放资源点
	 * 
	 * @author CamusHuang
	 * @creation 2013-1-24 上午11:52:58
	 * </pre>
	 */
	static class WarErrorScanTask implements KGameTimerTask {

		static WarErrorScanTask instance;

		/**
		 * <pre>
		 * 
		 * @param broadDataList
		 * @author CamusHuang
		 * @creation 2013-8-28 下午9:00:46
		 * </pre>
		 */
		static void submitTask() {
			ResWarDataCenter.rwLock.writeLock().lock();
			try {
				cancel();
				instance = new WarErrorScanTask();
				KGame.newTimeSignal(instance, KResWarConfig.AddScorePeroid, TimeUnit.SECONDS);
			} finally {
				ResWarDataCenter.rwLock.writeLock().unlock();
			}
		}

		static void cancel() {
			ResWarDataCenter.rwLock.writeLock().lock();
			try {
				if (instance != null) {
					instance.cancel = true;
				}
			} finally {
				ResWarDataCenter.rwLock.writeLock().unlock();
			}
		}

		//

		private boolean cancel;

		private WarErrorScanTask() {
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal arg0) throws KGameServerException {

			ResWarDataCenter.rwLock.writeLock().lock();
			try {
				if (cancel) {
					return null;
				}
				try {
					ResWarLogic.onTimeSignalForWarErrorScan();
					return null;
				} catch (Exception ex) {
					_LOGGER.error(ex.getMessage(), ex);
					throw new KGameServerException(ex);
				} finally {
					KGame.newTimeSignal(instance, KResWarConfig.AddScorePeroid, TimeUnit.SECONDS);
				}

			} finally {
				ResWarDataCenter.rwLock.writeLock().unlock();
			}
		}

		@Override
		public void done(KGameTimeSignal arg0) {
		}

		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		@Override
		public void rejected(RejectedExecutionException ex) {
			_LOGGER.error(ex.getMessage(), ex);
		}
	}

	/**
	 * <pre>
	 * 城市内定时世界广播
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-28 下午9:06:18
	 * </pre>
	 */
	static class CityBroadcastTask extends LoopTask {
		/** 城市内定时世界广播 */
		private static CityBroadcastTask instance;

		/**
		 * <pre>
		 * 城市内定时定时世界广播
		 * 
		 * @param broadDataList
		 * @author CamusHuang
		 * @creation 2013-8-28 下午9:00:46
		 * </pre>
		 */
		static void submitTask(List<BroadData> broadDataList) {
			cancel();
			instance = new CityBroadcastTask(broadDataList);
		}

		static void cancel() {
			ResWarDataCenter.rwLock.writeLock().lock();
			try {
				if (instance != null) {
					instance.beCancel();
				}
			} finally {
				ResWarDataCenter.rwLock.writeLock().unlock();
			}
		}

		private CityBroadcastTask(List<BroadData> broadDataList) {
			super(broadDataList);
		}

		void doWork(BroadData data) {
			if (data != null && data.tips != null) {
				// 通知所有城市内的玩家
				KResWarMsgPackCenter.sendMsgToAllRoleInCitys(KSupportFactory.getChatSupport().genSystemChatMsg(data.tips, data.type));
			}
		}
	}

	/**
	 * <pre>
	 * 全世界定时世界广播
	 * ----活动开始前10分钟会发送活动即将开启的广播公告，公告与系统消息同时进行。
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-28 下午9:06:18
	 * </pre>
	 */
	static class WorldBroadcastTask extends LoopTask {
		/** 全世界定时世界广播，最多支持两个并行实例，可以N个，具体看代码调用到底需要多少个实现 */
		private static WorldBroadcastTask[] instance = new WorldBroadcastTask[2];

		/**
		 * <pre>
		 * 全世界定时世界广播
		 * 
		 * @param broadDataList
		 * @author CamusHuang
		 * @creation 2013-8-28 下午9:00:46
		 * </pre>
		 */
		static void submitTask(int index, List<BroadData> broadDataList) {
			cancel(index);
			instance[index] = new WorldBroadcastTask(broadDataList);
		}

		static void cancel(int index) {
			ResWarDataCenter.rwLock.writeLock().lock();
			try {
				if (instance[index] != null) {
					instance[index].beCancel();
				}
			} finally {
				ResWarDataCenter.rwLock.writeLock().unlock();
			}
		}

		static void cancelAll() {
			ResWarDataCenter.rwLock.writeLock().lock();
			try {
				for (WorldBroadcastTask temp : instance) {
					if (temp != null) {
						temp.beCancel();
					}
				}
			} finally {
				ResWarDataCenter.rwLock.writeLock().unlock();
			}
		}

		//
		private WorldBroadcastTask(List<BroadData> broadDataList) {
			super(broadDataList);
		}

		void doWork(BroadData data) {
			if (data != null && data.tips != null) {
				KSupportFactory.getChatSupport().sendSystemChat(data.tips, data.type);
			}
		}
	}

	/**
	 * <pre>
	 * 循环任务，直到所有任务发送完毕
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-28 下午9:06:18
	 * </pre>
	 */
	abstract static class LoopTask implements KGameTimerTask {
		private List<BroadData> broadDataList;
		private int broadIndex = -1;

		private boolean cancel;

		private LoopTask(List<BroadData> broadDataList) {
			this.broadDataList = broadDataList;
			goForNext();
		}

		private void goForNext() {
			broadIndex++;
			long nowTime = System.currentTimeMillis();
			while (broadDataList.size() > broadIndex) {
				BroadData data = broadDataList.get(broadIndex);
				if (data.sendTime > nowTime) {
					KGame.newTimeSignal(this, data.sendTime - nowTime, TimeUnit.MILLISECONDS);
					break;
				} else {
					broadIndex++;
				}
			}
		}

		void beCancel() {
			ResWarDataCenter.rwLock.writeLock().lock();
			try {
				cancel = true;
			} finally {
				ResWarDataCenter.rwLock.writeLock().unlock();
			}
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal arg0) throws KGameServerException {
			ResWarDataCenter.rwLock.writeLock().lock();
			try {
				if (cancel) {
					return null;
				}
				try {
					BroadData data = broadDataList.get(broadIndex);
					if (data != null) {
						doWork(data);
					}
				} catch (Exception ex) {
					_LOGGER.error(ex.getMessage(), ex);
					throw new KGameServerException(ex);
				} finally {
					goForNext();
				}
				return null;
			} finally {
				ResWarDataCenter.rwLock.writeLock().unlock();
			}
		}

		abstract void doWork(BroadData data);

		@Override
		public void done(KGameTimeSignal arg0) {
		}

		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		@Override
		public void rejected(RejectedExecutionException ex) {
			_LOGGER.error(ex.getMessage(), ex);
		}

	}

	/**
	 * <pre>
	 * 广播内容
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-28 下午9:01:57
	 * </pre>
	 */
	static class BroadData {
		long sendTime;
		String tips;
		KWordBroadcastType type;

		BroadData(long sendTime, String tips, KWordBroadcastType type) {
			this.sendTime = sendTime;
			this.tips = tips;
			this.type = type;
		}
	}
}
