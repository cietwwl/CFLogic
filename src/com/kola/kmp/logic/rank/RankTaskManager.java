package com.kola.kmp.logic.rank;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.kola.kmp.logic.other.KRankTypeEnum;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.DayClearTask;

class RankTaskManager {

	private RankTaskManager() {
	}

	final static Logger _LOGGER = KGameLogger.getLogger(RankTaskManager.class);

	static void notifyCacheLoadComplete() {
		RankPublishTask.getInstance().start(KRankConfig.getInstance().RankResortPeriod);
		RankChangeNotifyTask.delayTime = KRankConfig.getInstance().RankChangeTaskDelayTime;

		// 跨天触发任务
		KGoodTimeClearDayTask.instance.start();
	}

	/**
	 * <pre>
	 * 排行榜定时发布任务
	 * 
	 * @deprecated 内部唯一实例
	 * @author CamusHuang
	 * @creation 2014-2-21 上午9:44:51
	 * </pre>
	 */
	static class RankPublishTask implements KGameTimerTask {

		private final static RankPublishTask instance = new RankPublishTask();
		private long periodTime;
		private long nextTime;

		private RankPublishTask() {
		}

		public long getNextTime() {
			return nextTime;
		}

		static RankPublishTask getInstance() {
			return instance;
		}

		/**
		 * <pre>
		 * 启动入口
		 * 
		 * @author CamusHuang
		 * @creation 2014-2-21 上午10:35:34
		 * </pre>
		 */
		void start(long periodTime) {
			this.periodTime = periodTime;
			KGame.newTimeSignal(RankPublishTask.instance, periodTime, TimeUnit.MILLISECONDS);
			nextTime = System.currentTimeMillis() + periodTime;
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
				KRankLogic.onTimeSignalForPublish(true, false, false);
			} catch (Exception ex) {
				_LOGGER.error(ex.getMessage(), ex);
				throw new KGameServerException(ex);
			} finally {
				arg0.getTimer().newTimeSignal(this, periodTime, TimeUnit.MILLISECONDS);
				nextTime = System.currentTimeMillis() + periodTime;
			}
			return null;
		}

		@Override
		public void rejected(RejectedExecutionException ex) {
			_LOGGER.error(ex.getMessage(), ex);
		}
	}

	/**
	 * <pre>
	 * 排行榜排名变化异步通知任务
	 * 
	 * @deprecated 内部唯一实例
	 * @author CamusHuang
	 * @creation 2013-7-6 下午9:40:13
	 * </pre>
	 */
	static class RankChangeNotifyTask implements KGameTimerTask {

		private static long delayTime;
		final static RankChangeNotifyTask instance = new RankChangeNotifyTask();
		//
		private final ReentrantLock lock = new ReentrantLock();
		//
		// 缓存的排行榜变化名单
		private final Map<KRankTypeEnum, Set<Long>> cachedMap = new HashMap<KRankTypeEnum, Set<Long>>(8);//
		private KGameTimeSignal timeSignal;

		private RankChangeNotifyTask() {
		}

		void notifyData(KRankTypeEnum rankType, Set<Long> changeRoles) {
			lock.lock();
			try {
				boolean isShouldSubmit = cachedMap.isEmpty();// 如果缓存为空，则需要提交时效

				Set<Long> roleSet = cachedMap.get(rankType);
				if (roleSet == null) {
					roleSet = new HashSet<Long>();
					cachedMap.put(rankType, roleSet);
				}
				roleSet.addAll(changeRoles);

				if (isShouldSubmit) {
					if (timeSignal == null) {
						timeSignal = KGame.newTimeSignal(this, delayTime, TimeUnit.MILLISECONDS);
					} else {
						timeSignal.getTimer().newTimeSignal(this, delayTime, TimeUnit.MILLISECONDS);
					}
				}
			} finally {
				lock.unlock();
			}
		}

		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal arg0) throws KGameServerException {
			// 由于涉及到遍历及调用未知代码，因此本方法先复制缓存的数据并清空缓存，释放锁后对临时数据进行遍历
			Map<KRankTypeEnum, Set<Long>> tempCachedMap = new HashMap<KRankTypeEnum, Set<Long>>(8);

			lock.lock();
			try {
				// 复制缓存
				tempCachedMap.putAll(cachedMap);
				cachedMap.clear();
			} catch (Exception ex) {
				_LOGGER.error(ex.getMessage(), ex);
				throw new KGameServerException(ex);
			} finally {
				lock.unlock();
			}

			// 对临时数据进行遍历，调用未知代码
			for (Entry<KRankTypeEnum, Set<Long>> entry : tempCachedMap.entrySet()) {
				try {
					KRankLogic.synNotifyForRankChange(entry.getKey(), entry.getValue());
				} catch (Exception ex) {
					_LOGGER.error(ex.getMessage(), ex);
				}
			}

			return null;
		}

		@Override
		public void rejected(RejectedExecutionException ex) {
			_LOGGER.error(ex.getMessage(), ex);
		}

		@Override
		public void done(KGameTimeSignal arg0) {
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
	static class KGoodTimeClearDayTask extends DayClearTask {

		static final KGoodTimeClearDayTask instance = new KGoodTimeClearDayTask(5 * Timer.ONE_SECOND);

		private KGoodTimeClearDayTask(long delay) {
			super(delay);
		}

		public void doWork() throws KGameServerException {
			long nowTime = System.currentTimeMillis();

			for (long roleId : KSupportFactory.getRoleModuleSupport().getAllOnLineRoleIds()) {
				KRoleRankData doRoleData = KRankRoleExtCACreator.getRoleRankData(roleId);
				
				if (doRoleData == null) {
					continue;
				}
				
				doRoleData.notifyForDayChange(nowTime);
			}
		}

		@Override
		public String getNameCN() {
			return "点赞次数跨天清0任务";
		}
	}
}
