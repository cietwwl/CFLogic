package com.kola.kmp.logic.util;

import java.text.ParseException;
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
 * 每隔N小时执行的任务
 * 内部会计算好下次执行的时刻
 * 
 * 注意：新建对象后，必须调用 {@link #start()}启动任务
 * 
 * @author CamusHuang
 * @creation 2013-4-28 下午6:35:32
 * </pre>
 */
public abstract class HourClearTask implements KGameTimerTask {
	public static final Logger _LOGGER = KGameLogger.getLogger(HourClearTask.class);
	/* 默认延迟时间（毫秒） */
	private static final long DefaultDelay = 5 * Timer.ONE_SECOND;

	//
	private final String taskName = this.getClass().getSimpleName();
	private final long delay;// hh:mm:00 再往后偏移N毫秒
	private int hours = 0;
	//
	private long nextRunTime;// 下次执行的时刻

	/**
	 * <pre>
	 * 默认延时05秒即hh:mm:05执行任务
	 * 每小时执行
	 * 
	 * @author CamusHuang
	 * @creation 2013-6-19 下午6:18:58
	 * </pre>
	 */
	public HourClearTask() {
		this(0, DefaultDelay);
	}

	/**
	 * <pre>
	 * 每小时执行
	 * 
	 * @param delay hh:mm:00再往后偏移的时间值 例如偏移05秒即为hh:mm:05执行任务
	 * @author CamusHuang
	 * @creation 2013-6-19 下午6:18:06
	 * </pre>
	 */
	public HourClearTask(long delay) {
		this(0, delay);
	}

	/**
	 * <pre>
	 * hours必须是1，2，3，4，6，8，12即 24%hours必须等于0
	 * 另外hours可以为0表示按天执行
	 * 
	 * @param hours hh%hours==0时执行，例如hours=3,则0,3,6,9,12,15,18,21点执行
	 * @param delay hh:mm:00再往后偏移的时间值 例如偏移05秒即为hh:mm:05执行任务
	 * @author CamusHuang
	 * @creation 2014-4-18 下午7:59:18
	 * </pre>
	 */
	public HourClearTask(int hours, long delay) {
		this.hours = hours;
		this.delay = delay;
		if (hours != 0) {
			long DayHours = Timer.ONE_DAY/Timer.ONE_HOUR;//24小时
			if (hours < 1 || hours >= DayHours || DayHours % hours != 0) {
				throw new RuntimeException("hours 参数错误");
			}
		}
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
		_LOGGER.warn(">>>>>>>>> 任务:{}-下次任务执行时间:{}", getNameCN(), UtilTool.DATE_FORMAT2.format(new Date(System.currentTimeMillis() + nextRunDelayTime)));
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
	public Object onTimeSignal(KGameTimeSignal arg0) throws KGameServerException {
		// 预先产生下次执行时刻
		long nextRunDelayTime = getNextRunDealyTime();
		try {
			// 执行具体工作
			doWork();
		} catch (Exception ex) {
			_LOGGER.error(ex.getMessage(), ex);
			throw new KGameServerException(ex);
		} finally {
			arg0.getTimer().newTimeSignal(this, nextRunDelayTime, TimeUnit.MILLISECONDS);
			_LOGGER.warn(">>>>>>>>> 任务:{}-下次任务执行时间:{}", getNameCN(), UtilTool.DATE_FORMAT2.format(new Date(System.currentTimeMillis() + nextRunDelayTime)));
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
	 * 下次执行的时刻
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-19 上午11:01:11
	 * </pre>
	 */
	public long getNextRunTime() {
		return nextRunTime;
	}

	/**
	 * <pre>
	 * 下一次执行hh:mm:05距离现在的时长（毫秒）
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2013-6-13 下午9:54:26
	 * </pre>
	 */
	private long getNextRunDealyTime() {
		if (hours == 0) {
			long nowTime = System.currentTimeMillis();
			nextRunTime = UtilTool.getNextHourStart(nowTime).getTimeInMillis() + delay;
			return nextRunTime - nowTime;
		} else {
			Calendar cal = Calendar.getInstance();
			long nowTime = cal.getTimeInMillis();
			int nowHour = cal.get(Calendar.HOUR_OF_DAY);
			int addHour = (nowHour / hours + 1) * hours - nowHour;
			cal.add(Calendar.HOUR_OF_DAY, addHour);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			nextRunTime = cal.getTimeInMillis() + delay;
			return nextRunTime - nowTime;
		}
	}

	/**
	 * <pre>
	 * 周期
	 * 1小时或N小时
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-8-22 上午4:03:22
	 * </pre>
	 */
	public long getPeriodInMills() {
		if (hours == 0) {
			return Timer.ONE_HOUR;
		} else {
			return Timer.ONE_HOUR * hours;
		}
	}
}
