package com.kola.kmp.logic.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.management.timer.Timer;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.activity.KActivityTimeStruct.TimeIntervalStruct;

/**
 * 通用活动时间
 * 
 * 时间模式 开服N天后开启活动 活动开始时间点 开服N天后结束活动 活动结束时间点 至少开服N天后开启活动 活动开始时间 活动结束时间 int int
 * string int string int string string timeType1 relativeStartTimeStr1
 * startTimeStr1 relativeEndTimeStr1 endTimeStr1 allowStartTimeStr1
 * startTimeStr1 endTimeStr1
 * 
 * @author Administrator
 * 
 */
public class CommonActivityTime {

	// 加载时的有效时间，保证时间从小到大排序，且互不重叠
	private List<CATime> allTimes = new ArrayList<CATime>();
	private List<CATime> outTimes = new ArrayList<CATime>();
	//
	public List<CATime> effectTimes = new ArrayList<CATime>();

	public boolean isInEffectTime(long nowTime) {
		for (CATime time : effectTimes) {
			if (time.isInEffectTime(nowTime)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <pre>
	 * 
	 * 
	 * @param nowTime
	 * @param tryNext 如果当前时间没有匹配CATime,是否找下一个最接近的CATime？
	 * @return
	 * @author CamusHuang
	 * @creation 2015-1-5 上午11:11:00
	 * </pre>
	 */
	public CATime getEffectCATime(long nowTime, boolean tryNext) {

		if (tryNext) {
			for (CATime time : effectTimes) {
				int re = time.compareEffectDate(nowTime);
				if (re < 1) {
					// 按顺序遍历，找到一个进行中或者未开始的，返回
					return time;
				}
			}
			return null;
		} else {
			for (CATime time : effectTimes) {
				int re = time.compareEffectDate(nowTime);
				if (re == -1) {
					// 按顺序遍历，找到一个未开始的，返回
					return null;
				}
				if (re == 0) {
					return time;
				}
			}
			return null;
		}
	}

	public long getReleaseEffectTime(long nowTime) {

		CATime time = getEffectCATime(nowTime, false);
		if (time == null) {
			return -1;
		}

		return time.getReleaseEffectTime(nowTime);
	}
	
	public List<CATime> getAllTime(){
		return allTimes;
	}

	public static class CATime implements Comparable<CATime> {
		public static final int CATIME_TYPE_OPPOSITE=1;//相对开服时间
		public static final int CATIME_TYPE_ABSOLUTE=2;//自然时间
		public final int type;//1相对开服时间，2自然时间
		//
		public long startTime;
		public long endTime;
		//
		public String startTimeStr;// 活动开始时间
		public String endTimeStr;// 活动结束时间
		//
		/**
		 * 指定的当天时间段列表，根据timeIntervalStr转化后得来
		 */
		public final List<TimeIntervalStruct> timeIntervalList;
		public final boolean isFullTime;// 是否全天24小时开放

		public CATime(int type, long startTime, long endTime, List<TimeIntervalStruct> timeIntervalList) {
			this.type = type;
			this.startTime = startTime;
			this.endTime = endTime;
			this.timeIntervalList = timeIntervalList;
			this.isFullTime = timeIntervalList.isEmpty();
			//
			startTimeStr = UtilTool.DATE_FORMAT.format(new Date(startTime));
			endTimeStr = UtilTool.DATE_FORMAT.format(new Date(endTime));
		}

		public long getReleaseEffectTime(long nowTime) {
			return getReleaseEffectTime(nowTime, -1);
		}

		/**
		 * <pre>
		 * 离结束剩余时间（毫秒）
		 * 
		 * @param nowTime
		 * @return
		 * @author CamusHuang
		 * @creation 2015-1-5 上午10:46:41
		 * </pre>
		 */
		public long getReleaseEffectTime(long nowTime, long todayTime) {
			if (nowTime < startTime || endTime < nowTime) {
				return -1;
			}

			if (isFullTime) {
				return endTime - nowTime;
			}

			if (todayTime < 1) {
				todayTime = UtilTool.getNowDayInMilliseconds();
			}
			TimeIntervalStruct bingo = null;

			for (TimeIntervalStruct time : timeIntervalList) {
				long startTime = todayTime + time.getBeginTime();
				long endTime = todayTime + time.getEndTime();
				if (startTime <= nowTime && endTime >= nowTime) {
					bingo = time;
					break;
				}
			}
			if (bingo == null) {
				return -1;
			}
			return todayTime + bingo.getEndTime() - nowTime;
		}

		/**
		 * <pre>
		 * 比开始时间小，返回-1
		 * 在日期范围内，返回0
		 * 比结束时间大，返回1
		 * 
		 * @param nowTime
		 * @return
		 * @author CamusHuang
		 * @creation 2015-1-5 上午11:25:33
		 * </pre>
		 */
		public int compareEffectDate(long nowTime) {
			if (nowTime < startTime) {
				return -1;
			}
			if (endTime < nowTime) {
				return 1;
			}
			return 0;
		}

		/**
		 * <pre>
		 * 在日期范围内
		 * 
		 * @param nowTime
		 * @return
		 * @author CamusHuang
		 * @creation 2015-1-5 上午11:23:26
		 * </pre>
		 */
		public boolean isInEffectDate(long nowTime) {
			if (startTime < nowTime && nowTime < endTime) {
				return true;
			} else {
				return false;
			}
		}

		/**
		 * <pre>
		 * 是否在CATime日期范围内,且在有效时段内
		 * 
		 * @param nowTime
		 * @return 
		 * @author CamusHuang
		 * @creation 2015-1-5 上午10:47:11
		 * </pre>
		 */
		public boolean isInEffectTime(long nowTime) {
			return isInEffectTime(nowTime, -1);
		}

		public boolean isInEffectTime(long nowTime, long todayTime) {
			if (nowTime < startTime || endTime < nowTime) {
				return false;
			}

			if (isFullTime) {
				return true;
			}
			if (todayTime < 1) {
				todayTime = UtilTool.getNowDayInMilliseconds();
			}
			for (TimeIntervalStruct time : timeIntervalList) {
				if ((todayTime + time.getBeginTime()) <= nowTime && (todayTime + time.getEndTime()) >= nowTime) {
					return true;
				}
			}

			return false;
		}

		/**
		 * <pre>
		 * 检查是否生效
		 * 
		 * @param nowTime
		 * @return []{是否在此CATime日期范围内,是否在有效时段内}
		 * @author CamusHuang
		 * @creation 2015-1-5 上午10:47:11
		 * </pre>
		 */
		public boolean[] checkIsInEffectTime(long nowTime) {
			if (startTime < nowTime && nowTime < endTime) {
				if (isFullTime) {
					return new boolean[] { true, true };
				}
				long todayTime = UtilTool.getNowDayInMilliseconds();
				for (TimeIntervalStruct time : timeIntervalList) {
					if ((todayTime + time.getBeginTime()) <= nowTime && (todayTime + time.getEndTime()) >= nowTime) {
						return new boolean[] { true, true };
					}
				}

				return new boolean[] { true, false };
			} else {
				return new boolean[] { false, false };
			}
		}

		@Override
		public int compareTo(CATime o) {
			if (startTime < o.startTime) {
				return -1;
			}
			if (startTime > o.startTime) {
				return 1;
			}
			return 0;
		}
	}

	public static CommonActivityTime load(long ServerStartDay, long nowTime, KGameExcelRow row) throws Exception {
		final int MaxColCount = 20;
		List<CATime> effectTimeList = new ArrayList<CATime>();
		List<CATime> outTimeList = new ArrayList<CATime>();
		List<CATime> allTimeList = new ArrayList<CATime>();
		{
			//
			for (int i = 0; i < MaxColCount; i++) {
				int index = i + 1;
				String colName = "timeType" + index;
				if (!row.containsCol(colName)) {
					break;
				}
				String colData = row.getData(colName);
				if (colData == null || colData.isEmpty()) {
					continue;
				}
				//

				int timeType = row.getInt(colName);
				if (timeType != 1 && timeType != 2) {
					throw new KGameServerException(colName + " 错误=" + timeType);
				}

				List<TimeIntervalStruct> timeIntervalList = loadTimeInterval(row, index);

				CATime caTime = null;
				if (timeType == CATime.CATIME_TYPE_OPPOSITE) {
					// 相对开服时间
					caTime = loadType1(row, index, ServerStartDay, timeIntervalList);

					if (caTime.endTime <= nowTime) {
						// 时间过期
						outTimeList.add(caTime);
					} else {
						// 未过期
						effectTimeList.add(caTime);
					}
				} else {
					// 自然时间
					caTime = loadType2(row, index, ServerStartDay, timeIntervalList);

					if (caTime.endTime <= caTime.startTime || caTime.endTime <= nowTime) {
						// 时间过期
						outTimeList.add(caTime);
					} else {
						// 未过期
						effectTimeList.add(caTime);
					}
				}
				//
				allTimeList.add(caTime);
			}
		}

		if (allTimeList.isEmpty()) {
			throw new KGameServerException("未配置任何时间参数");
		}

		Collections.sort(allTimeList);
		Collections.sort(outTimeList);
		Collections.sort(effectTimeList);

		// 检查时间是否重叠
		{
			boolean isChange = false;
			//
			{
				CATime frontCA = null;
				for (CATime nowCA : effectTimeList) {
					if (frontCA == null) {
						frontCA = nowCA;
						continue;
					}
	
					if (nowCA.startTime <= frontCA.endTime) {
						if(nowCA.type == frontCA.type){
							throw new KGameServerException("时间重叠");
						}
						if(frontCA.type==CATime.CATIME_TYPE_OPPOSITE){
							//frontCA时间段优先
							nowCA.startTime = frontCA.endTime+Timer.ONE_SECOND;
							nowCA.startTimeStr = frontCA.endTimeStr;
							isChange = true;
						} else {
							//nowCA时间段优先
							frontCA.endTime = nowCA.startTime-Timer.ONE_SECOND;
							frontCA.endTimeStr = nowCA.startTimeStr;
							isChange = true;
						}
					}
				}
			}
			
			if(isChange){
				//将effectTimeList里失效的CATime移到outTimeList
				for(Iterator<CATime> it=effectTimeList.iterator();it.hasNext();){
					CATime caTime = it.next();
					if (caTime.endTime <= caTime.startTime || caTime.endTime <= nowTime) {
						// 时间过期
						outTimeList.add(caTime);
						it.remove();
					}
				}
				
				Collections.sort(allTimeList);
				Collections.sort(outTimeList);
				Collections.sort(effectTimeList);
			}
		}

		CommonActivityTime result = new CommonActivityTime();
		result.allTimes.addAll(allTimeList);
		result.outTimes.addAll(outTimeList);
		result.effectTimes.addAll(effectTimeList);
		return result;
	}

	/**
	 * <pre>
	 * 分析时间段
	 * 
	 * @param row
	 * @param index
	 * @return
	 * @throws Exception
	 * @author CamusHuang
	 * @creation 2015-1-5 上午9:59:06
	 * </pre>
	 */
	private static List<TimeIntervalStruct> loadTimeInterval(KGameExcelRow row, int index) throws Exception {

		String colName = "timeLimit" + index;
		if (!row.containsCol(colName)) {
			return Collections.emptyList();
		}
		String timeLimit = row.getData(colName);
		if (timeLimit == null || timeLimit.isEmpty()) {
			return Collections.emptyList();
		}

		String[] timeStr = timeLimit.split(",");
		if (timeStr.length < 1) {
			return Collections.emptyList();
		}

		{
			List<TimeIntervalStruct> timeIntervalList = new ArrayList<TimeIntervalStruct>();
			for (int i = 0; i < timeStr.length; i++) {
				String[] temp = (timeStr[i].substring(timeStr[i].indexOf("(") + 1, timeStr[i].indexOf(")"))).split("-");
				if (temp == null || temp.length != 2) {
					throw new KGameServerException("格式错误，str=" + timeStr[i]);
				}
				long beginTime = UtilTool.parseHHmmToMillis(temp[0]);
				long endTime = UtilTool.parseHHmmToMillis(temp[1]);

				if (beginTime >= endTime) {
					throw new KGameServerException("beginTime >= endTime，str=" + timeLimit);
				}
				timeIntervalList.add(new TimeIntervalStruct(temp[0], temp[1], beginTime, endTime));
			}

			if (timeIntervalList.isEmpty()) {
				return Collections.emptyList();
			}

			Collections.sort(timeIntervalList);

			// camus：所有时间段必须互不重叠
			if (timeIntervalList.size() > 1) {
				TimeIntervalStruct min = null;
				for (TimeIntervalStruct temp : timeIntervalList) {
					if (min == null) {
						min = temp;
						continue;
					}
					if (temp.getBeginTime() <= min.getEndTime()) {
						throw new KGameServerException("时间段重叠，str=" + timeLimit);
					}
					min = temp;
				}
			}

			return timeIntervalList;
		}
	}

	private static CATime loadType1(KGameExcelRow row, int index, long ServerStartDay, List<TimeIntervalStruct> timeIntervalList) throws Exception {

		// 相对开服时间
		String[] startAndEndDayStr = row.getData("startAndEndDayStr" + index).split("-");// 开服第N天后开始活动-第N天后结束
		int startNDay = Integer.parseInt(startAndEndDayStr[0]);
		int endNDay = Integer.parseInt(startAndEndDayStr[1]);
		//
		String[] startAndEndTimeStr = row.getData("startAndEndTimeStr" + index).split("-");// 活动开始与结束时间点
		String startTimeStr = startAndEndTimeStr[0]; // 活动开始时间点
		String endTimeStr = startAndEndTimeStr[1]; // 活动结束时间点
		//
		if (startNDay > endNDay) {
			throw new KGameServerException("时间错误 " + startNDay + ">" + endNDay);
		}

		long startTime = ServerStartDay + (startNDay - 1) * Timer.ONE_DAY + UtilTool.parseHHmmToMillis(startTimeStr);
		long endTime = ServerStartDay + (endNDay - 1) * Timer.ONE_DAY + UtilTool.parseHHmmToMillis(endTimeStr);

		if (startTime > endTime) {
			throw new KGameServerException("时间错误 " + startTime + ">" + endTime);
		}

		return new CATime(CATime.CATIME_TYPE_OPPOSITE, startTime, endTime, timeIntervalList);
	}

	private static CATime loadType2(KGameExcelRow row, int index, long ServerStartDay, List<TimeIntervalStruct> timeIntervalList) throws Exception {
		// 自然时间
		int N = row.getInt("atLeastStartDayStr" + index);// 至少开服N天后开启活动
		String startTimeStr = row.getData("startTimeStr" + index);// 活动开始时间
		String endTimeStr = row.getData("endTimeStr" + index);// 活动结束时间
		//
		long minStartTime = ServerStartDay + N * Timer.ONE_DAY;
		long startTime = UtilTool.DATE_FORMAT.parse(startTimeStr).getTime();
		long endTime = UtilTool.DATE_FORMAT.parse(endTimeStr).getTime();
		if (endTime <= startTime) {
			throw new KGameServerException("时间错误 " + endTimeStr + "<=" + startTimeStr);
		}

		startTime = Math.max(minStartTime, startTime);
		// startTime 有可能> endTime

		return new CATime(CATime.CATIME_TYPE_ABSOLUTE, startTime, endTime, timeIntervalList);
	}
}
