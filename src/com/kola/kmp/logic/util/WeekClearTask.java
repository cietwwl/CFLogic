package com.kola.kmp.logic.util;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.kola.kgame.cache.util.UtilTool;

/**
 * <pre>
 * 周时效任务，可指定到周几的某时刻执行时效。
 * 例如指定周二13点执行任务，可以调用构造函数WeekClearTask(int dayOfWeek,long delay)，
 * 传入参数：dayOfWeek=3，delay=(13*3600000)。
 * 如果当前时间小于指定时间，则会在本周二的13点执行。如果大于指定时间，则顺延至下周二的13点执行
 * @author zhaizl
 * </pre>
 */
public abstract class WeekClearTask implements KGameTimerTask {
	public static final Logger _LOGGER = KGameLogger
			.getLogger(WeekClearTask.class);
	/* 默认延迟时间（毫秒） */
	private static final long DefaultDelay = 5 * Timer.ONE_SECOND;

	//
	private final String taskName = this.getClass().getSimpleName();
	private final long delay;// hh:mm:00 再往后偏移N毫秒
	private final int dayOfWeek;

	/**
	 * <pre>
	 * 默认延时05秒即hh:mm:05执行任务
	 * @param dayOfWeek 指定的周几（范围：1~7，表示星期日至星期六）
	 * 
	 * @author CamusHuang
	 * @creation 2013-6-19 下午6:18:58
	 * </pre>
	 */
	public WeekClearTask(int dayOfWeek) {
		delay = DefaultDelay;
		this.dayOfWeek = dayOfWeek;
	}

	/**
	 * <pre>
	 * 
	 * @param dayOfWeek 指定的周几（范围：1~7，表示星期日至星期六）
	 * @param delay hh:mm:00再往后偏移的时间值 例如偏移05秒即为hh:mm:05执行任务
	 * @author CamusHuang
	 * @creation 2013-6-19 下午6:18:06
	 * </pre>
	 */
	public WeekClearTask(int dayOfWeek, long delay) {
		this.delay = delay;
		this.dayOfWeek = dayOfWeek;
	}

	/**
	 * <pre>
	 * 启动任务
	 * 
	 * @author CamusHuang
	 * @creation 2013-6-19 下午5:23:35
	 * </pre>
	 */
	public void start() {
		long nextRunDelayTime = getNextRunDealyTime();
		KGame.newTimeSignal(this, nextRunDelayTime, TimeUnit.MILLISECONDS);
		_LOGGER.warn(
				">>>>>>>>> 任务:{}-下次任务执行时间:{}",
				getNameCN(),
				UtilTool.DATE_FORMAT2.format(new Date(System
						.currentTimeMillis() + nextRunDelayTime)));
	}

	@Override
	public void done(KGameTimeSignal arg0) {
	}

	@Override
	public String getName() {
		return taskName;
	}

	/**
	 * <pre>
	 * 显示于日志的任务名称，最好是中文名
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2013-6-19 下午6:31:06
	 * </pre>
	 */
	public abstract String getNameCN();

	@Override
	public Object onTimeSignal(KGameTimeSignal arg0)
			throws KGameServerException {
		try {
			// 执行具体工作
			doWork();
		} catch (Exception ex) {
			_LOGGER.error(ex.getMessage(), ex);
			throw new KGameServerException(ex);
		} finally {
			long nextRunDelayTime = getNextRunDealyTime();
			arg0.getTimer().newTimeSignal(this, nextRunDelayTime,
					TimeUnit.MILLISECONDS);
			_LOGGER.warn(
					">>>>>>>>> 任务:{}-下次任务执行时间:{}",
					getNameCN(),
					UtilTool.DATE_FORMAT2.format(new Date(System
							.currentTimeMillis() + nextRunDelayTime)));
		}
		return null;
	}

	@Override
	public void rejected(RejectedExecutionException ex) {
		_LOGGER.error(ex.getMessage(), ex);
	}

	/**
	 * <pre>
	 * 任务的具体执行内容
	 * 
	 * @author CamusHuang
	 * @creation 2013-6-19 下午5:22:49
	 * </pre>
	 */
	public abstract void doWork() throws KGameServerException;

	/**
	 * <pre>
	 * 下一小时hh:mm:05距离现在的时长（毫秒）
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2013-6-13 下午9:54:26
	 * </pre>
	 */
	public long getNextRunDealyTime() {
		long nowTime = System.currentTimeMillis();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(nowTime);
		if (cal.get(Calendar.DAY_OF_WEEK) == dayOfWeek) {
			long checkTime = UtilTool.getTodayStart().getTimeInMillis() + delay;
			if (checkTime > nowTime + DefaultDelay) {
				return (checkTime - nowTime);
			}
		}

		long period = UtilTool.getNextWeekDayStart(nowTime, dayOfWeek)
				.getTimeInMillis() - nowTime + delay;
		return period;
	}
}
