package com.kola.kmp.logic.util;

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
 * 每天执行的任务
 * 内部会计算好当天的执行时刻，按指定时刻执行
 * 例外：如果当天的执行时刻已过，则会进入下一天的任务。
 * 
 * 注意：新建对象后，必须调用 {@link #start()}启动任务
 * 
 * @author CamusHuang
 * @creation 2013-4-28 下午6:35:32
 * </pre>
 */
public abstract class DayClearTask implements KGameTimerTask {
	public static final Logger _LOGGER = KGameLogger.getLogger(DayClearTask.class);
	/* 默认执行时刻（以每天的零点整为基准的偏移值，单位：毫秒） */
	private static final long DefaultDayDelay = 5 * Timer.ONE_SECOND;

	//
	private final String taskName = this.getClass().getSimpleName();
	private final long dayDelay;// 执行时刻（以每天的零点整为基准的偏移值，单位：毫秒）

	/**
	 * <pre>
	 * 默认00:00:05执行任务
	 * 
	 * @author CamusHuang
	 * @creation 2013-6-19 下午6:18:58
	 * </pre>
	 */
	public DayClearTask() {
		dayDelay = DefaultDayDelay;
	}

	/**
	 * <pre>
	 * 
	 * 
	 * @param dayDelay 零点整偏移delay毫秒执行任务
	 * @author CamusHuang
	 * @creation 2013-6-19 下午6:18:06
	 * </pre>
	 */
	public DayClearTask(long dayDelay) {
		this.dayDelay = dayDelay;
	}

	/**
	 * <pre>
	 * 启动任务
	 * 首次执行将于firstTimeDelay 毫秒后进行
	 * 后续则按构造方法的 dayDelay 每天执行
	 * 
	 * @param firstTimeDelay 毫秒
	 * @author CamusHuang
	 * @creation 2014-8-20 下午10:50:34
	 * </pre>
	 */
	public void start(long firstTimeDelay) {
		KGame.newTimeSignal(this, firstTimeDelay, TimeUnit.MILLISECONDS);
		_LOGGER.warn(">>>>>>>>> 任务:{}-下次任务执行时间:{}", getNameCN(), UtilTool.DATE_FORMAT2.format(new Date(System.currentTimeMillis() + firstTimeDelay)));
	}
	
	/**
	 * <pre>
	 * 启动任务
	 * 按构造方法的 dayDelay 每天执行
	 * 
	 * @author CamusHuang
	 * @creation 2013-6-19 下午5:23:35
	 * </pre>
	 */
	public void start() {
		long nextRunDelayTime = getNextRunDealyTime(false);
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
		try {
			// 执行具体工作
			doWork();
		} catch (Exception ex) {
			_LOGGER.error(ex.getMessage(), ex);
			throw new KGameServerException(ex);
		} finally {
			long nextRunDelayTime = getNextRunDealyTime(true);
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
	 * 明天的凌晨00:00:05距离现在的时长（毫秒）
	 * 
	 * @param onTimeSignal 是否在onTimeSignal方法中调用的，如果是在onTimeSignal调用的，则传入true
	 * @return
	 * @author CamusHuang
	 * @creation 2013-6-13 下午9:54:26
	 * </pre>
	 */
	public long getNextRunDealyTime(boolean onTimeSignal) {
		// 2014-09-15 增加onTimeSignal参数，表示是否在onTimeSignal中调用
		// 因为有时候0点执行的任务，例如假设是00:00:03执行的，时效任务有可能提前几十毫秒执行，
		// 即可能在00:00:02.930执行，这时候nowTime一定会比todayRunTime小，因为todayRunTime是用
		// 今天开始的时间+dayDelay，dayDelay在例子中位3，即0点偏移3毫秒，加入这个参数
		// 如果onTimeSignal为true，则表示一定执行过一次，所以这里必然是明天执行的
		long nowTime = System.currentTimeMillis();
		long todayRunTime = UtilTool.getNextNDaysStart(nowTime, 0).getTimeInMillis() + dayDelay;
		long runDelayTime = 0;
		if (onTimeSignal || nowTime >= todayRunTime) {
			// 明天执行
			runDelayTime = todayRunTime + Timer.ONE_DAY - nowTime;
		} else {
			// 今天可以执行
			runDelayTime = todayRunTime - nowTime;
		}

		if (runDelayTime < Timer.ONE_MINUTE) {
			runDelayTime = Timer.ONE_MINUTE;
		}
		return runDelayTime;
	}
}
