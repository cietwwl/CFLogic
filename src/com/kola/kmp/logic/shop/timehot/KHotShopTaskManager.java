package com.kola.kmp.logic.shop.timehot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.activity.KActivityTimeStruct.TimeIntervalStruct;
import com.kola.kmp.logic.shop.timehot.KHotShopDataManager.HotShopManager.HotGoods;
import com.kola.kmp.logic.shop.timehot.KHotShopDataManager.HotShopManager.HotShop;
import com.kola.kmp.logic.util.CommonActivityTime.CATime;
import com.kola.kmp.logic.util.DayClearTask;
import com.kola.kmp.protocol.gm.ProtocolGs;

/**
 * <pre>
 * 本类管理模块定义的所有任务
 * 
 * @author CamusHuang
 * @creation 2013-1-9 下午2:39:58
 * </pre>
 */
class KHotShopTaskManager implements ProtocolGs {

	private KHotShopTaskManager() {
	}

	private static final Logger _LOGGER = KGameLogger.getLogger(KHotShopTaskManager.class);

	static void notifyCacheLoadComplete() throws KGameServerException {
		// 每日任务
		(new TimeHotShopDayTask()).start();
		// 启动任务
		TimeLimitActivityTaskDataManager.restartAllActivityTast();
	}

	static class TimeHotShopDayTask extends DayClearTask {

		@Override
		public String getNameCN() {
			return "限时热购每天任务";
		}

		@Override
		public void doWork() throws KGameServerException {
			TimeLimitActivityTaskDataManager.restartAllActivityTast();
			//
			KHotShopCenter.notifyForDayChange();
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

		// 缓存当天所有任务<执行时刻,任务>
		private static Map<Long, GoodsStartOrEndTask> taskCache = new HashMap<Long, GoodsStartOrEndTask>();

		/**
		 * <pre>
		 * 启动限时热购当天任务
		 * 
		 * @author CamusHuang
		 * @creation 2014-11-11 上午9:42:12
		 * </pre>
		 */
		static void restartAllActivityTast() {

			clearCache();

			long nowTime = System.currentTimeMillis();

			for (HotShop shop : KHotShopDataManager.mHotShopManager.allShopList) {
				for (HotGoods goods : shop.dataList) {
					if (nowTime >= goods.caTime.endTime) {
						continue;
					}
					CATime caTime = goods.caTime;

					if (caTime.isFullTime) {
						restartActivity(shop, goods, nowTime, caTime.startTime, caTime.endTime);
					} else {

						long todayStartTime = UtilTool.getTodayStart().getTimeInMillis();

						// 循环所有时间段
						for (TimeIntervalStruct s : caTime.timeIntervalList) {
							long sTime = Math.max(todayStartTime + s.getBeginTime(), caTime.startTime);
							_LOGGER.warn(UtilTool.DATE_FORMAT.format(new Date(sTime)) + " =" + UtilTool.DATE_FORMAT.format(new Date((todayStartTime + s.getBeginTime()))) + " "
									+ UtilTool.DATE_FORMAT.format(new Date(caTime.startTime)));
							long eTime = Math.min(todayStartTime + s.getEndTime(), caTime.endTime);
							if (sTime >= eTime) {
								continue;
							}
							restartActivity(shop, goods, nowTime, sTime, eTime);
						}
					}
				}
			}
		}

		private static void restartActivity(HotShop shop, HotGoods goods, long nowTime, long startTime, long endTime) {
			if (endTime <= nowTime) {
				// 已经结束
				return;
			}

			// 未结束
			if (startTime > nowTime) {
				// 未开始，提交开始任务
				submitTast(shop, goods, true, startTime, startTime - nowTime);
			}

			// 提交结束任务
			submitTast(shop, goods, false, endTime, endTime - nowTime);
		}

		private static void submitTast(HotShop shop, HotGoods goods, boolean isStart, long effectTime, long delayTime) {
			_LOGGER.warn("限时热购商品【{}】【{}】将于{}{}", shop.type.name(), goods.index, UtilTool.DATE_FORMAT2.format(new Date(delayTime + System.currentTimeMillis())), isStart ? "开启" : "关闭");
			
			if(taskCache.containsKey(effectTime)){
				return;
			}
			GoodsStartOrEndTask task = new GoodsStartOrEndTask(effectTime);
			taskCache.put(task.effectTime, task);
			KGame.newTimeSignal(task, delayTime, TimeUnit.MILLISECONDS);
		}

		private static void clearCache() {
			for (GoodsStartOrEndTask task : taskCache.values()) {
				task.cancel = true;
			}
			taskCache.clear();
		}

		/**
		 * <pre>
		 * 商品开始或结束通知任务
		 * 
		 * @author CamusHuang
		 * @creation 2015-1-22 下午1:43:39
		 * </pre>
		 */
		static class GoodsStartOrEndTask implements KGameTimerTask {
			private long effectTime;
			private boolean cancel;

			//
			private GoodsStartOrEndTask(long effectTime) {
				this.effectTime =  effectTime;
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
					KHotShopCenter.nofityGoodsStartOrEnd();
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
	}
}
