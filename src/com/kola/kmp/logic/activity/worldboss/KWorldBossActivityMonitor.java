package com.kola.kmp.logic.activity.worldboss;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.activity.KActivity;
import com.kola.kmp.logic.activity.KActivityModule;
import com.kola.kmp.logic.activity.KActivityTimeStruct.TimeIntervalStruct;

/**
 * 
 * @author PERRY CHAN
 */
public class KWorldBossActivityMonitor implements KGameTimerTask {

	private static int _activityId;
	private static int _defalutDelayMillis = (int) TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS);

	private int _currentTimeIntdex = 0;
	private int _nextTimeIndex = 0;
	private boolean _warnUp = true; // 是否开始预热
	private int _warnUpDelay = 0;

	static void setActivityId(int pActivityId) {
		_activityId = pActivityId;
	}
	
	static int getWorldBossActivityId() {
		return _activityId;
	}

	static void start() {
		KWorldBossActivityMonitor monitor = new KWorldBossActivityMonitor();
		int delay = monitor.getDelaySecond();
		KGame.newTimeSignal(monitor, delay, TimeUnit.SECONDS);
	}
	
	private int getDelaySecond() {
		KActivity activity = KActivityModule.getActivityManager().getActivity(_activityId);
		List<TimeIntervalStruct> list = activity.getKActivityTimeStruct().getTimeIntervalList();
		TimeIntervalStruct last = list.get(list.size() - 1);
		long todayBeginTime = UtilTool.getNowDayInMilliseconds();
		long currentTime = System.currentTimeMillis();
		long stdBeginTime = 0;
		if (currentTime <= (todayBeginTime + last.getEndTime() - KWorldBossConfig.getMinActivityTime()) && _nextTimeIndex < list.size()) {
			stdBeginTime = todayBeginTime;
		} else {
			stdBeginTime = UtilTool.getTommorowStart().getTimeInMillis();
			_nextTimeIndex = 0;
		}
		long beginTime = 0;
		long endTime = 0;
		TimeIntervalStruct temp;
		long delay = 0;
		int index = _nextTimeIndex;
		for (; index < list.size(); index++) {
			temp = list.get(index);
			beginTime = stdBeginTime + temp.getBeginTime();
			endTime = stdBeginTime + temp.getEndTime();
			if (beginTime <= currentTime) {
				if (endTime < currentTime && endTime < currentTime + KWorldBossConfig.getMinActivityTime()) {
					continue;
				} else {
					_warnUp = false;
					delay = _defalutDelayMillis;
					break;
				}
			} else {
				_warnUp = true;
				delay = beginTime - currentTime;
				break;
			}
		}
		_currentTimeIntdex = index;
		_nextTimeIndex = index + 1;
		if (delay == 0) {
			throw new RuntimeException("世界boss，找不到下次开始的时间！！");
		}
		int result = (int) TimeUnit.SECONDS.convert(delay, TimeUnit.MILLISECONDS);
		if(_warnUp) {
			if (result > KWorldBossConfig.getWarnUpDurationSeconds()) {
				_warnUpDelay = KWorldBossConfig.getWarnUpDurationSeconds();
				result -= _warnUpDelay;
			} else {
				_warnUpDelay = result;
				result = 1;
			}
		}
		System.out.println(StringUtil.format("》》》》世界boss下次开始时间距离现在差{}秒，预热延迟：{}《《《《", result, _warnUpDelay));
		return result;
	}

	@Override
	public String getName() {
		return "KWorldBossActivityStartUp";
	}

	@Override
	public Object onTimeSignal(KGameTimeSignal timeSignal) throws KGameServerException {
		if (_warnUp) {
			System.out.println("！！！！世界boss预热开始！！！！");
			try {
				_warnUp = false;
				KWorldBossManager.getWorldBossActivity().warnUp(_warnUpDelay, _currentTimeIntdex);
				timeSignal.getTimer().newTimeSignal(this, _warnUpDelay, TimeUnit.SECONDS);
				System.out.println(StringUtil.format("！！！！世界boss将于{}秒后开始！！！！", _warnUpDelay));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("！！！！世界boss正式开始！！！！");
			try {
				KWorldBossManager.getWorldBossActivity().start(_currentTimeIntdex);
				int delay = getDelaySecond();
				timeSignal.getTimer().newTimeSignal(this, delay, TimeUnit.SECONDS);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public void done(KGameTimeSignal timeSignal) {

	}

	@Override
	public void rejected(RejectedExecutionException e) {

	}

}
