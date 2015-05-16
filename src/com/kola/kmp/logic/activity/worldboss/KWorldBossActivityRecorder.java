package com.kola.kmp.logic.activity.worldboss;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.flowdata.FlowDataModuleFactory;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.activity.KActivity;
import com.kola.kmp.logic.activity.KActivityModule;
import com.kola.kmp.logic.activity.KActivityTimeStruct.TimeIntervalStruct;

/**
 * 
 * @author PERRY CHAN
 */
public class KWorldBossActivityRecorder implements KGameTimerTask {
	
	private static final Queue<Long> _todayJoinIds = new ConcurrentLinkedQueue<Long>();
	private static final Map<Integer, Queue<Long>> _joinMap = new HashMap<Integer, Queue<Long>>();
	private static final Map<Integer, Map<Integer,Boolean>> _killMap  = new HashMap<Integer, Map<Integer,Boolean>>();
	private static final String _TEMP_SAVE_PATH = "./res/output/worldboss/worldBossRecorder_{}.txt";
	private static final String _META_PATH = "./res/output/worldboss/worldBossRecorder.meta";
	
	private static int _lastTimeIndex = 0;
	private static int _currentTimeIndex = 0;
	private static boolean _hadStartBefore = false;
	private static boolean _isOdd = true;
	
	private static void record() {
		FlowDataModuleFactory.getModule().recordWorldBossByDay(_todayJoinIds.size(), _joinMap.get(0).size(), _joinMap.get(1).size(), new ArrayList<Boolean>(_killMap.get(0).values()),
				new ArrayList<Boolean>(_killMap.get(1).values()));
	}
	
	private static void readLastRecord() {
		try {
			File file = new File(_META_PATH);
			if (file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String lastPath = null;
				try {
					lastPath = br.readLine();
				} finally {
					br.close();
				}
				if (lastPath != null) {
					file = new File(lastPath);
					if (file.exists()) {
						try {
							Calendar current = Calendar.getInstance();
							int currentDayOfYear = current.get(Calendar.DAY_OF_YEAR);
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMdd-HHmm");
							br = new BufferedReader(new FileReader(file));
							String dateTime = br.readLine();
							Date d = sdf.parse(dateTime);
							current.setTime(d);
							if (currentDayOfYear == current.get(Calendar.DAY_OF_YEAR)) {
								br.readLine(); // _todayJoinIds的长度
								String[] allJoinIds = br.readLine().replace("]", "").replace("[", "").split(",");
								for (int i = allJoinIds.length; i-- > 0;) {
									_todayJoinIds.add(Long.parseLong(allJoinIds[i].trim()));
								}
								int joidMapSize = Integer.parseInt(br.readLine());
								for (int i = 0; i < joidMapSize; i++) {
									String[] joinInfo = br.readLine().split(":");
									Queue<Long> tempQueue = _joinMap.get(Integer.parseInt(joinInfo[0]));
									for (int k = Integer.parseInt(joinInfo[1]); k-- > 0;) {
										tempQueue.add(0l);
									}
								}
								int killMapSize = Integer.parseInt(br.readLine());
								for(int i = 0; i < killMapSize; i++) {
									String[] killInfo = br.readLine().split(":");
									Map<Integer, Boolean> tempMap = _killMap.get(Integer.parseInt(killInfo[0]));
									String[] killMap = killInfo[1].replace("{", "").replace("}", "").split(",");
									for (int k = 0; k < killMap.length; k++) {
										String[] tempKillInfo = killMap[k].trim().split("=");
										tempMap.put(Integer.parseInt(tempKillInfo[0]), Boolean.parseBoolean(tempKillInfo[1]));
									}
								}
							}
						} finally {
							br.close();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static void submit() {
		KActivity activity = KActivityModule.getActivityManager().getActivity(KWorldBossActivityMonitor.getWorldBossActivityId());
		List<TimeIntervalStruct> list = activity.getKActivityTimeStruct().getTimeIntervalList();
		long tomorrowTimeMillis = UtilTool.getTommorowStart().getTimeInMillis();
		long sub = tomorrowTimeMillis - System.currentTimeMillis();
		int minute = (int)TimeUnit.MINUTES.convert(sub, TimeUnit.MILLISECONDS) - 2; // 减两分钟，以防是在刚好0点的时刻保存
		KWorldBossActivityRecorder task = new KWorldBossActivityRecorder();
		/*Map<Integer, Boolean> killMapTemplate = new LinkedHashMap<Integer, Boolean>();
		List<KWorldBossFieldData> allFieldDatas = KWorldBossManager.getAllWorldBossFieldDatas();
		for(int i = 0; i < allFieldDatas.size(); i++) {
			killMapTemplate.put(allFieldDatas.get(i).templateId, false);
		}*/
		Map<Integer, Boolean> killMapTemplate = new LinkedHashMap<Integer, Boolean>();
		killMapTemplate.put(KWorldBossManager.getWorldBossFieldData().templateId, false);
		for(int i = 0; i < list.size(); i++) {
			_joinMap.put(i, new ConcurrentLinkedQueue<Long>());
			_killMap.put(i, new LinkedHashMap<Integer, Boolean>(killMapTemplate));
		}
		_lastTimeIndex = list.size() - 1;
		readLastRecord();
		KGame.getTimer().newTimeSignal(task, minute, TimeUnit.MINUTES);
	}
	
	static void recordJoin(long roleId, int timeIndex) {
		Queue<Long> temp = _joinMap.get(timeIndex);
		if (temp != null) {
			temp.add(roleId);
		}
		if (!_todayJoinIds.contains(roleId)) {
			_todayJoinIds.add(roleId);
		}
	}
	
	static void recordBossKillStatus(int fieldId, boolean isKill, int timeIndex) {
		Map<Integer, Boolean> map = _killMap.get(timeIndex);
		if (map != null) {
			Map.Entry<Integer, Boolean> entry;
			for (Iterator<Map.Entry<Integer, Boolean>> itr = map.entrySet().iterator(); itr.hasNext();) {
				entry = itr.next();
				if (entry.getKey() == fieldId) {
					entry.setValue(isKill);
					break;
				}
			}
		}
	}
	
	static void notifyStart(int timeIndex) {
		_currentTimeIndex = timeIndex;
		_hadStartBefore = true;
	}
	
	static void shutdown() throws Exception {
		if(_currentTimeIndex == _lastTimeIndex) {
			try {
				record();
				// 保存之后删除meta文件，以防下次开机的时候又读取同一天的数据
				File file = new File(_META_PATH);
				if(file.exists()) {
					file.delete();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			if (_todayJoinIds.size() > 0) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMdd-HHmm");
				String dateTime = sdf.format(Calendar.getInstance().getTime());
				String filePath = StringUtil.format(_TEMP_SAVE_PATH, dateTime.replace("-", "_"));

				BufferedWriter bw = new BufferedWriter(new FileWriter(_META_PATH));

				bw.write(filePath);
				bw.write("\n");
				bw.flush();
				bw.close();

				bw = new BufferedWriter(new FileWriter(filePath));
				bw.write(dateTime);
				bw.write("\n");
				bw.write(String.valueOf(_todayJoinIds.size()));
				bw.write("\n");
				bw.write(_todayJoinIds.toString());
				bw.write("\n");
				bw.write(String.valueOf(_joinMap.size()));
				bw.write("\n");
				Map.Entry<Integer, Queue<Long>> entry;
				for (Iterator<Map.Entry<Integer, Queue<Long>>> itr = _joinMap.entrySet().iterator(); itr.hasNext();) {
					entry = itr.next();
					bw.write(entry.getKey() + ":" + entry.getValue().size());
					bw.write("\n");
				}

				bw.write(String.valueOf(_killMap.size()));
				bw.write("\n");
				Map.Entry<Integer, Map<Integer, Boolean>> killMapEntry;
				for (Iterator<Map.Entry<Integer, Map<Integer, Boolean>>> itr = _killMap.entrySet().iterator(); itr.hasNext();) {
					killMapEntry = itr.next();
					bw.write(killMapEntry.getKey() + ":" + killMapEntry.getValue().toString());
					bw.write("\n");
				}
				bw.flush();
				bw.close();
			}
		}
	}
	
	private void reset() {
		_hadStartBefore = false;
		_currentTimeIndex = 0; // 充值timeIndex，否则停机的时候，如果timeIndex>0会重复保存
		_todayJoinIds.clear();
		for(Iterator<Queue<Long>> itr = _joinMap.values().iterator(); itr.hasNext();) {
			itr.next().clear();
		}
		for(Iterator<Map<Integer, Boolean>> itr = _killMap.values().iterator(); itr.hasNext();) {
			for(Iterator<Map.Entry<Integer, Boolean>> itr2 = itr.next().entrySet().iterator(); itr2.hasNext();) {
				itr2.next().setValue(false);
			}
		}
	}

	@Override
	public String getName() {
		return "KWorldBossActivityRecorder";
	}

	@Override
	public Object onTimeSignal(KGameTimeSignal timeSignal) throws KGameServerException {
		try {
			if (_hadStartBefore) {
				record();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.reset();
		int delay = 23;
		if(_isOdd) {
			delay = 24;
			_isOdd = false;
		} else {
			_isOdd = true;
		}
		timeSignal.getTimer().newTimeSignal(this, delay, TimeUnit.HOURS);
		return "SUCCESS";
	}

	@Override
	public void done(KGameTimeSignal timeSignal) {

	}

	@Override
	public void rejected(RejectedExecutionException e) {
		
	}

}
