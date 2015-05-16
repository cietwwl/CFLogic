package com.kola.kmp.logic.activity;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ch.qos.logback.classic.pattern.Util;

import com.koala.game.exception.KGameServerException;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.KActivityTimeTypeEnum;

public class KActivityTimeStruct {
	// 活动开启时间类型
	private KActivityTimeTypeEnum timeType;

	/**
	 * 指定的日期字符串参数，表示特定一天或连续特定几天（格式：2014/09/09,2014/09/10）
	 */
	private String appointedDayStr;

	/**
	 * 指定的每周某几天字符串参数（格式：1,2,6,7 表示每周的周一、二、五、六）
	 */
	private String weekDayStr;

	/**
	 * <pre>
	 * 指定当天时间段字符串参数
	 * （格式：(10:00-10:59),(20:00-20:59)）：表示10:00至10:59、20:00至20:59两个时间段
	 * </pre>
	 */
	private String timeIntervalStr;

	/**
	 * 指定的日期列表，根据appointedDayStr转化后得来的日期时间
	 */
	private List<Date> appointedDayList;

	/**
	 * 指定的每周某几天的列表，list的元素值为{@link Calendar#DAY_OF_WEEK}的值，并保证列表中没有重复值
	 */
	private List<Integer> weekDayList;

	/**
	 * 指定的当天时间段列表，根据timeIntervalStr转化后得来
	 */
	private List<TimeIntervalStruct> timeIntervalList;

	public KActivityTimeStruct(int timeType, String appointedDayStr,
			String weekDayStr, String timeIntervalStr) {
		this.timeType = KActivityTimeTypeEnum.getEnum(timeType);
		this.appointedDayStr = appointedDayStr;
		this.weekDayStr = weekDayStr;
		this.timeIntervalStr = timeIntervalStr;
	}

	public void initTimeStruct() throws KGameServerException {
		if (appointedDayStr != null) {
			String[] dayStr = appointedDayStr.split(",");
			if (dayStr != null) {
				appointedDayList = new ArrayList<Date>();
				for (int i = 0; i < dayStr.length; i++) {
					try {
						Date appointedDay = UtilTool.DATE_FORMAT9
								.parse(dayStr[i]);
						appointedDayList.add(appointedDay);
					} catch (ParseException e) {
						throw new KGameServerException(
								"初始化活动时间参数格式错误，解释指定的日期字符串参数出错，str="
										+ appointedDayStr, e);
					}
				}
			}
		}

		if (weekDayStr != null) {
			String[] dayStr = weekDayStr.split(",");
			if (dayStr != null) {
				weekDayList = new ArrayList<Integer>();
				for (int i = 0; i < dayStr.length; i++) {
					int dayOfWeek = Integer.parseInt(dayStr[i]);
					if (dayOfWeek < Calendar.SUNDAY
							|| dayOfWeek > Calendar.SATURDAY) {
						throw new KGameServerException(
								"初始化活动时间参数格式错误，解释指定的每周某几天字符串参数出错，超出了范围（1-7），str="
										+ weekDayList);
					}
					if (weekDayList.contains(dayOfWeek)) {
						throw new KGameServerException(
								"初始化活动时间参数格式错误，解释指定的每周某几天字符串参数出错，设置了重复的日期，str="
										+ weekDayList);
					}
					weekDayList.add(dayOfWeek);
				}
			}
		}

		if (timeIntervalStr != null) {
			String[] timeStr = timeIntervalStr.split(",");
			if (timeStr != null) {
				timeIntervalList = new ArrayList<TimeIntervalStruct>();
				for (int i = 0; i < timeStr.length; i++) {
					String[] temp = (timeStr[i].substring(
							timeStr[i].indexOf("(") + 1,
							timeStr[i].indexOf(")"))).split("-");
					if (temp == null || temp.length != 2) {
						throw new KGameServerException(
								"初始化活动时间参数格式错误，解释指定当天时间段字符串参数出错，str="
										+ timeIntervalStr);
					}
					String[] beginTimeStr = temp[0].split(":");
					String[] endTimeStr = temp[1].split(":");
					long beginTime;
					long endTime;
					try {
						System.out.println(temp[0] + "----" + temp[1]);
						beginTime = UtilTool.parseHHmmToMillis(temp[0]);
						endTime = UtilTool.parseHHmmToMillis(temp[1]);
						System.out.println(beginTime + "----" + endTime);
					} catch (Exception e) {
						throw new KGameServerException(
								"初始化活动时间参数格式错误，解释指定当天时间段字符串参数出错，str="
										+ timeIntervalStr);
					}
					TimeIntervalStruct struct = new TimeIntervalStruct(temp[0],
							temp[1], beginTime, endTime);
					timeIntervalList.add(struct);
				}
				
				if(!timeIntervalList.isEmpty()){
					Collections.sort(timeIntervalList);
				}
			}
		}
	}

	public KActivityTimeTypeEnum getTimeType() {
		return timeType;
	}

	public String getAppointedDayStr() {
		return appointedDayStr;
	}

	public String getWeekDayStr() {
		return weekDayStr;
	}

	public String getTimeIntervalStr() {
		return timeIntervalStr;
	}

	public List<Date> getAppointedDayList() {
		return appointedDayList;
	}

	public List<Integer> getWeekDayList() {
		return weekDayList;
	}

	public List<TimeIntervalStruct> getTimeIntervalList() {
		return timeIntervalList;
	}

	public static class TimeIntervalStruct implements
			Comparable<TimeIntervalStruct> {
		// 起始时间，格式：HH:MM
		private String beginTimeStr;
		// 结束时间，格式：HH:MM
		private String endTimeStr;
		// 转化为毫秒数后的起始时间
		private long beginTime;
		// 转化为毫秒数后的结束时间
		private long endTime;

		public TimeIntervalStruct(String beginTimeStr, String endTimeStr,
				long beginTime, long endTime) {
			super();
			this.beginTimeStr = beginTimeStr;
			this.endTimeStr = endTimeStr;
			this.beginTime = beginTime;
			this.endTime = endTime;
		}

		public String getBeginTimeStr() {
			return beginTimeStr;
		}

		public String getEndTimeStr() {
			return endTimeStr;
		}

		public long getBeginTime() {
			return beginTime;
		}

		public long getEndTime() {
			return endTime;
		}

		@Override
		public int compareTo(TimeIntervalStruct o) {
			if (this.beginTime < o.beginTime) {
				return -1;
			} else if (this.beginTime > o.beginTime) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	public static void main(String[] a) {
		String appointedDayStr = "2014/09/09,2014/09/10";
		String weekstr = "1,2,6,7";
		String timeIntervalStr = "(10:00-10:59),(20:00-20:59)";
		KActivityTimeStruct struct = new KActivityTimeStruct(1,
				appointedDayStr, weekstr, timeIntervalStr);
		try {
			struct.initTimeStruct();
		} catch (KGameServerException e) {
			e.printStackTrace();
		}
	}
}
