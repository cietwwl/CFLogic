package com.kola.kmp.logic.reward.exciting;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.activity.KActivityTimeStruct.TimeIntervalStruct;
import com.kola.kmp.logic.currency.KCurrencySupportImpl;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.other.KLimitTimeProduceActivityTypeEnum;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.ExcitingDataManager.ExcitionActivity;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.RankTimeLimitRewardDataManager.RankTimeLimitReward;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.TimeLimitActivityDataManager.TimeLimieProduceActivity;
import com.kola.kmp.logic.reward.exciting.message.KSynDataMsg;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.CommonActivityTime.CATime;
import com.kola.kmp.protocol.gm.ProtocolGs;

/**
 * <pre>
 * 本类管理模块定义的所有任务
 * 
 * @author CamusHuang
 * @creation 2013-1-9 下午2:39:58
 * </pre>
 */
class ExcitingTaskManager implements ProtocolGs {

	private ExcitingTaskManager() {
	}

	private static final Logger _LOGGER = KGameLogger.getLogger(ExcitingTaskManager.class);

	/**
	 * <pre>
	 * 启动精彩活动自动发奖任务
	 * 
	 * @author CamusHuang
	 * @creation 2014-11-20 下午9:36:47
	 * </pre>
	 */
	static void startAutoCollectedExcitingRewardTask() {
		KGame.newTimeSignal(AutoCollectedRewardTaskForExciting.instance, 5, TimeUnit.SECONDS);
	}
	
	/**
	 * <pre>
	 * 精彩活动全服状态同步
	 * 
	 * @author CamusHuang
	 * @creation 2015-1-7 下午6:25:15
	 * </pre>
	 */
	static void startAutoSynWorldTimeTaskForExciting() {
		KGame.newTimeSignal(SynAllOnlineRoleStatusTaskForExciting.instance, 5, TimeUnit.SECONDS);
	}

	/**
	 * <pre>
	 * 启动排行榜排名奖励自动发奖任务
	 * 
	 * @author CamusHuang
	 * @creation 2014-11-20 下午9:36:47
	 * </pre>
	 */
	static void restartAutoCollectedRankRewardTask() {
		AutoCollectedRewardTaskForRank.clearCache();

		long nowTime = System.currentTimeMillis();

		// 启动所有排行榜定时奖励活动
		for (RankTimeLimitReward reward : KExcitingDataManager.mRankTimeLimitRewardDataManager.getDataCache()) {
			long nextRunTime = reward.getNextRewardCollectTime(nowTime);
			if (nextRunTime > 0) {
				AutoCollectedRewardTaskForRank.submitTast(reward.id, nextRunTime);
				_LOGGER.warn("排行榜定时奖励：《" + reward.name + "》下次执行时间" + UtilTool.DATE_FORMAT2.format(new Date(nextRunTime)));
			}
		}
	}

	/**
	 * <pre>
	 * 管理器
	 * 精彩活动起始和结束任务
	 * 
	 * @author CamusHuang
	 * @creation 2014-11-11 上午9:59:21
	 * </pre>
	 */
	static class ExcitingActivityTaskDataManager {

		// 缓存所有任务
		private static List<ActivityStartOrEndTask> startCache = new ArrayList<ActivityStartOrEndTask>();
		private static List<ActivityStartOrEndTask> endCache = new ArrayList<ActivityStartOrEndTask>();

		// 监听器
		private static ExcitingActivityListener listener = new ExcitingActivityListener();

		private static void submitTast(int activityId, boolean isStart, long delayTime) {
			ActivityStartOrEndTask task = new ActivityStartOrEndTask(activityId, isStart, listener);
			if (isStart) {
				startCache.add(task);
			} else {
				endCache.add(task);
			}
			KGame.newTimeSignal(task, delayTime, TimeUnit.MILLISECONDS);
		}

		private static void clearCache(boolean isStart) {
			List<ActivityStartOrEndTask> cache = isStart ? startCache : endCache;
			for (ActivityStartOrEndTask task : cache) {
				task.cancel = true;
			}
			cache.clear();
		}

		/**
		 * <pre>
		 * 启动所有活动的起始和结束任务
		 * 
		 * @author CamusHuang
		 * @creation 2014-11-11 上午9:42:12
		 * </pre>
		 */
		static void restartActivityTast() {

			clearCache(true);
			clearCache(false);

			// 启动所有活动的起始和结束任务
			long nowTime = System.currentTimeMillis();

			for (ExcitionActivity activity : KExcitingDataManager.mExcitingDataManager.getDatas()) {
				if (activity.caTime.startTime > nowTime) {
					_LOGGER.warn("精彩活动【{}】将于{}开启", activity.title, activity.caTime.startTimeStr);
					submitTast(activity.id, true, activity.caTime.startTime - nowTime);
				}

				if (activity.caTime.endTime > nowTime) {
					_LOGGER.warn("精彩活动【{}】将于{}关闭", activity.title, activity.caTime.endTimeStr);
					submitTast(activity.id, false, activity.caTime.endTime - nowTime);
				}
			}
		}

		/**
		 * <pre>
		 * 接受时效任务回调，处理精彩活动的开始和结束
		 * 
		 * @author CamusHuang
		 * @creation 2014-11-11 上午9:58:26
		 * </pre>
		 */
		static class ExcitingActivityListener implements ActivityListener {

			@Override
			public void nofityActivityStart(Object activityKey) {
				KExcitingCenter.activityStartNotify(((Integer) activityKey));
			}

			@Override
			public void nofityActivityEnd(Object activityKey) {
				KExcitingCenter.activityEndNotify(((Integer) activityKey));
			}
		}

	}

	/**
	 * <pre>
	 * 限时活动开始和结束任务
	 * 
	 * @author CamusHuang
	 * @creation 2014-11-11 上午10:06:57
	 * </pre>
	 */
	static class TimeLimitActivityTaskDataManager {

		// 缓存所有任务
		private static Map<Object, ActivityStartOrEndTask> startCache = new HashMap<Object, ActivityStartOrEndTask>();
		private static Map<Object, ActivityStartOrEndTask> endCache = new HashMap<Object, ActivityStartOrEndTask>();

		// 监听器
		private static TimeLimitActivityListener listener = new TimeLimitActivityListener();

		private static void submitTast(Object activityKey, boolean isStart, long delayTime) {
			ActivityStartOrEndTask old = startCache.get(activityKey);
			if (old != null) {
				old.cancel = true;
			}
			old = endCache.get(activityKey);
			if (old != null) {
				old.cancel = true;
			}

			ActivityStartOrEndTask task = new ActivityStartOrEndTask(activityKey, isStart, listener);
			if (isStart) {
				startCache.put(activityKey, task);
			} else {
				endCache.put(activityKey, task);
			}
			_LOGGER.warn("限时活动【{}】将于{}{}", ((KLimitTimeProduceActivityTypeEnum) activityKey).name(), UtilTool.DATE_FORMAT2.format(new Date(delayTime + System.currentTimeMillis())), isStart ? "开启"
					: "关闭");
			KGame.newTimeSignal(task, delayTime, TimeUnit.MILLISECONDS);
		}

		private static void clearCache(boolean isStart) {
			Map<Object, ActivityStartOrEndTask> cache = isStart ? startCache : endCache;
			for (ActivityStartOrEndTask task : cache.values()) {
				task.cancel = true;
			}
			cache.clear();
		}

		/**
		 * <pre>
		 * 启动限时奖励活动起始和结束任务
		 * 
		 * @author CamusHuang
		 * @creation 2014-11-11 上午9:42:12
		 * </pre>
		 */
		static void restartAllActivityTast() {

			clearCache(true);
			clearCache(false);

			// 启动所有活动的起始和结束任务
			long nowTime = System.currentTimeMillis();

			for (TimeLimieProduceActivity activity : KExcitingDataManager.mTimeLimitActivityDataManager.getDataCache()) {
				restartActivityTask(activity, nowTime, true);
			}
		}

		private static void notifyRestartActivityTask(KLimitTimeProduceActivityTypeEnum typeEnum) {
			long nowTime = System.currentTimeMillis();
			TimeLimieProduceActivity activity = KExcitingDataManager.mTimeLimitActivityDataManager.getTimeLimieProduceActivityData(typeEnum);

			restartActivityTask(activity, nowTime, false);
		}

		private static void restartActivityTask(TimeLimieProduceActivity activity, long nowTime, boolean isForInit) {

			CATime caTime = activity.mCommonActivityTime.getEffectCATime(nowTime, true);
			if(caTime==null){
				// 活动已失效
				return;
			}
			
			if (nowTime > caTime.endTime) {
				// 活动已失效
				return;
			}

			if (nowTime < caTime.startTime) {
				// 活动未生效
				if (caTime.isFullTime) {
					// 全天，则按活动起始时间
					submitTast(activity.acitvityType, true, caTime.startTime - nowTime);
					return;
				}

				// 分时段，则按第一个时段的开始时间
				TimeIntervalStruct time = caTime.timeIntervalList.get(0);
				long nextStartTime = UtilTool.getNextNDaysStart(caTime.startTime, 0).getTimeInMillis() + time.getBeginTime();
				submitTast(activity.acitvityType, true, nextStartTime - nowTime);
				return;
			}

			{
				// 活动已生效
				if (isForInit && caTime.isFullTime) {
					// 全天，则马上启动
					submitTast(activity.acitvityType, true, 3 * Timer.ONE_SECOND);
					return;
				}

				// 找出适合的时间段
				long todayStartTime = UtilTool.getTodayStart().getTimeInMillis();
				boolean isSubmit = tryToSubmitForDayPeriod(activity, caTime, nowTime, todayStartTime);
				if (isSubmit) {
					return;
				}

				// 尝试下一天
				isSubmit = tryToSubmitForDayPeriod(activity, caTime, nowTime, todayStartTime + Timer.ONE_DAY);
				if (isSubmit) {
					return;
				}
				// 结束
				submitTast(activity.acitvityType, false, caTime.endTime - nowTime);
			}
		}

		private static boolean tryToSubmitForDayPeriod(TimeLimieProduceActivity activity, CATime caTime, long nowTime, long todayStartTime) {
			// 活动已生效，找出适合的时间段
			for (TimeIntervalStruct time : caTime.timeIntervalList) {
				long tempTime = todayStartTime + time.getBeginTime();
				{
					if (tempTime >= caTime.endTime) {
						// 此时活动已经结束
						submitTast(activity.acitvityType, false, caTime.endTime - nowTime);
						return true;
					}
					if (nowTime < tempTime) {
						// 按此时段的开始时间
						submitTast(activity.acitvityType, true, tempTime - nowTime);
						return true;
					}
				}
				{
					tempTime = todayStartTime + time.getEndTime();
					if (tempTime >= caTime.endTime) {
						// 此时活动已经结束
						submitTast(activity.acitvityType, false, caTime.endTime - nowTime);
						return true;
					}
					if (nowTime < tempTime) {
						// 按此时段的结束时间
						submitTast(activity.acitvityType, false, tempTime - nowTime);
						return true;
					}
				}
			}

			// 指定当天找不到适合的时间段
			return false;
		}

		/**
		 * <pre>
		 * 接受时效任务回调，处理活动的开始和结束
		 * 
		 * @author CamusHuang
		 * @creation 2014-11-11 上午9:58:26
		 * </pre>
		 */
		static class TimeLimitActivityListener implements ActivityListener {

			@Override
			public void nofityActivityStart(Object activityKey) {
				KLimitTimeProduceActivityTypeEnum typeEnum = (KLimitTimeProduceActivityTypeEnum) activityKey;
				//
				switch (typeEnum) {
				case 限时充值优惠:
					KCurrencySupportImpl.notifyForTimeLimitPresentActivity(true);
					break;
				case 装备升星成功率:
					KItemLogic.synAllEquiForOnlineRoles();
					break;
				}

				// 重新提交任务
				TimeLimitActivityTaskDataManager.notifyRestartActivityTask(typeEnum);
			}

			@Override
			public void nofityActivityEnd(Object activityKey) {
				KLimitTimeProduceActivityTypeEnum typeEnum = (KLimitTimeProduceActivityTypeEnum) activityKey;
				//
				switch (typeEnum) {
				case 限时充值优惠:
					KCurrencySupportImpl.notifyForTimeLimitPresentActivity(false);
					break;
				case 装备升星成功率:
					KItemLogic.synAllEquiForOnlineRoles();
					break;
				}

				// 重新提交任务
				TimeLimitActivityTaskDataManager.notifyRestartActivityTask(typeEnum);
			}
		}

	}

	/**
	 * <pre>
	 * 需要监听
	 * 
	 * @author CamusHuang
	 * @creation 2014-11-10 下午6:32:17
	 * </pre>
	 */
	public static interface ActivityListener {
		/**
		 * <pre>
		 * 活动（时段）开启
		 * 开服时全天活动如果未到开启时间，则在开启时通知一次活动开启，不会每天通知一次活动开启
		 * 开服时全天活动如果已过开启时间，则在不会再有通知活动开启
		 * 
		 * @param type
		 * @author CamusHuang
		 * @creation 2014-11-10 下午6:30:28
		 * </pre>
		 */
		public void nofityActivityStart(Object activityKey);

		/**
		 * <pre>
		 * 活动（时段）关闭
		 * 
		 * @param type
		 * @author CamusHuang
		 * @creation 2014-11-10 下午6:30:36
		 * </pre>
		 */
		public void nofityActivityEnd(Object activityKey);
	}

	/**
	 * <pre>
	 * 活动开始或结束通知任务
	 * 
	 * @author CamusHuang
	 * @creation 2014-11-11 上午9:53:05
	 * </pre>
	 */
	static class ActivityStartOrEndTask implements KGameTimerTask {
		private final Object activityKey;
		private boolean isStart;
		private boolean cancel;
		//
		private ActivityListener listener;

		private ActivityStartOrEndTask(Object activityKey, boolean isStart, ActivityListener listener) {
			this.activityKey = activityKey;
			this.isStart = isStart;
			this.listener = listener;
		}

		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal arg0) throws KGameServerException {
			if (cancel) {
				return null;
			}
			try {
				if (isStart) {
					listener.nofityActivityStart(activityKey);
				} else {
					listener.nofityActivityEnd(activityKey);
				}
			} catch (Exception ex) {
				_LOGGER.error(ex.getMessage(), ex);
				throw new KGameServerException(ex);
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
	 * 精彩活动自动发奖任务
	 * 
	 * @author CamusHuang
	 * @creation 2014-1-11 下午5:18:35
	 * </pre>
	 */
	static class AutoCollectedRewardTaskForExciting implements KGameTimerTask {
		// 所有可以领取的奖励<角色ID,Map<活动ID,Set<规则ID>>>
		private final Map<Long, Map<Integer, Set<Integer>>> activitysMap = new HashMap<Long, Map<Integer, Set<Integer>>>();
		private final ReentrantLock lock = new ReentrantLock();

		final static AutoCollectedRewardTaskForExciting instance = new AutoCollectedRewardTaskForExciting();

		private AutoCollectedRewardTaskForExciting() {
		}

		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		public void addData(long roleId, int activityId, int ruleId) {
			lock.lock();
			try {
				Map<Integer, Set<Integer>> map = activitysMap.get(roleId);
				if (map == null) {
					map = new HashMap<Integer, Set<Integer>>();
					activitysMap.put(roleId, map);
				}

				Set<Integer> set = map.get(activityId);
				if (set == null) {
					set = new HashSet<Integer>();
					map.put(activityId, set);
				}

				set.add(ruleId);

			} finally {
				lock.unlock();
			}
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal arg0) throws KGameServerException {
			try {

				Map<Long, Map<Integer, Set<Integer>>> activitysMapCopy = null;
				lock.lock();
				try {
					if (!activitysMap.isEmpty()) {
						activitysMapCopy = new HashMap<Long, Map<Integer, Set<Integer>>>(activitysMap);
						activitysMap.clear();
					}
				} finally {
					lock.unlock();
				}

				if (activitysMapCopy != null) {
					KExcitingCenter.autoCollectExcitingReward(activitysMapCopy);
				}
			} catch (Exception ex) {
				_LOGGER.error(ex.getMessage(), ex);
				throw new KGameServerException(ex);
			} finally {
				arg0.getTimer().newTimeSignal(this, 5, TimeUnit.SECONDS);
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
	 * 精彩活动全服状态同步
	 * 
	 * @author CamusHuang
	 * @creation 2015-1-7 下午6:21:36
	 * </pre>
	 */
	static class SynAllOnlineRoleStatusTaskForExciting implements KGameTimerTask {
		// 是否需要更新全服在线人员的活动状态
		public final AtomicBoolean isDirty=new AtomicBoolean();

		final static SynAllOnlineRoleStatusTaskForExciting instance = new SynAllOnlineRoleStatusTaskForExciting();

		private SynAllOnlineRoleStatusTaskForExciting() {
		}

		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal arg0) throws KGameServerException {
			try {
				if(isDirty.compareAndSet(true, false)){
					for (long roleId : KSupportFactory.getRoleModuleSupport().getAllOnLineRoleIds()) {
						KRoleExciting data = KExcitingExtCACreator.getRoleExciting(roleId);
						if (data == null) {
							continue;
						}
						
						// 通知在线角色进行个人数据刷新
						KSynDataMsg.sendMsgForStatus(roleId);
					}
				}
			} catch (Exception ex) {
				_LOGGER.error(ex.getMessage(), ex);
				throw new KGameServerException(ex);
			} finally {
				arg0.getTimer().newTimeSignal(this, 5, TimeUnit.SECONDS);
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
	 * 排行榜定时奖励自动发奖任务
	 * 
	 * @author CamusHuang
	 * @creation 2014-11-20 下午9:39:25
	 * </pre>
	 */
	static class AutoCollectedRewardTaskForRank implements KGameTimerTask {
		// 缓存所有任务
		private static List<AutoCollectedRewardTaskForRank> cache = new ArrayList<AutoCollectedRewardTaskForRank>();

		static void submitTast(int rewardId, long nextRunTime) {
			AutoCollectedRewardTaskForRank task = new AutoCollectedRewardTaskForRank(rewardId, nextRunTime);
			cache.add(task);
			KGame.newTimeSignal(task, PERIOD, TimeUnit.MILLISECONDS);
		}

		static void clearCache() {
			for (AutoCollectedRewardTaskForRank task : cache) {
				task.cancel = true;
			}
			cache.clear();
		}

		private static final long PERIOD = 20 * Timer.ONE_SECOND;

		private int rewardId;// 活动ID
		private long nextRunTime;
		private boolean cancel;

		private AutoCollectedRewardTaskForRank(int rewardId, long nextRunTime) {
			this.rewardId = rewardId;
			this.nextRunTime = nextRunTime;
		}

		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal arg0) throws KGameServerException {
			if (cancel) {
				return null;
			}

			long nowTime = System.currentTimeMillis();
			try {
				if (nowTime > nextRunTime) {
					nextRunTime = KExcitingCenter.autoCollectRankLimitReward(rewardId, nextRunTime);
					if (nextRunTime > 0) {
						_LOGGER.warn("排行榜定时奖励：《" + rewardId + "》下次执行时间" + UtilTool.DATE_FORMAT2.format(new Date(nextRunTime)));
					}
				}
			} catch (Exception ex) {
				_LOGGER.error(ex.getMessage(), ex);
				throw new KGameServerException(ex);
			} finally {
				if (nextRunTime > 0) {
					arg0.getTimer().newTimeSignal(this, PERIOD, TimeUnit.MILLISECONDS);
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
}
