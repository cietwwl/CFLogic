package com.kola.kmp.logic.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.koala.game.exception.KGameServerException;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.activity.KActivityTimeStruct.TimeIntervalStruct;

public class KNormalMapActivityManager {

	private List<KNormalMapActivityData> normalMapActivityDataList = new ArrayList<KNormalMapActivityData>();
	// <枚举类型,数据>
	private Map<Integer, KNormalMapActivityData> normalMapActivityDataMap = new HashMap<Integer, KNormalMapActivityData>();

	public KNormalMapActivityManager() {
	}
	
	void initDatas(List<KNormalMapActivityData> normalMapActivityDataList) throws Exception {
		for (KNormalMapActivityData data : normalMapActivityDataList) {
			if (normalMapActivityDataMap.put(data.mapId, data) != null) {
				throw new Exception("重复的地图 ID=" + data.mapId);
			}
			this.normalMapActivityDataList.add(data);
		}
	}

	public KNormalMapActivityData getNormalMapActivityData(int mapId) {
		return normalMapActivityDataMap.get(mapId);
	}

	public List<KNormalMapActivityData> getDataCache() {
		return normalMapActivityDataList;
	}
	
	public static class KNormalMapActivityData{
		public int mapId;
		public String map_xml_path;
		public int musicId;
		public String startTimeStr;// 活动开始时间
		public String endTimeStr;// 活动结束时间
		public String timeLimit;// 限时时段格式数据
		
		public long startTime;// 活动开始时间
		public long endTime;// 活动结束时间
		public boolean isFullTime = false;// 是否全天24小时开放
		/**
		 * 指定的当天时间段列表，根据timeIntervalStr转化后得来
		 */
		public List<TimeIntervalStruct> timeIntervalList = new ArrayList<TimeIntervalStruct>();
		
		/**
		 * <pre>
		 * 当前是否在活动开启过程中
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2014-11-12 下午12:32:12
		 * </pre>
		 */
		public boolean isActivityTakeEffectNow() {
			long nowTime = System.currentTimeMillis();

			boolean isInDate = false;
			if (nowTime > endTime || nowTime < startTime) {
				isInDate = false;
			} else {
				isInDate = true;
			}
			if (isInDate) {
				if (isFullTime) {
					return true;
				}
				long todayTime = UtilTool.getNowDayInMilliseconds();
				for (TimeIntervalStruct time : timeIntervalList) {
					if ((todayTime + time.getBeginTime()) <= nowTime && (todayTime + time.getEndTime()) >= nowTime) {
						return true;
					}
				}
			}
			return false;
		}
		
		void notifyCacheLoadComplete() throws Exception {
			startTime = UtilTool.DATE_FORMAT.parse(startTimeStr).getTime();
			endTime = UtilTool.DATE_FORMAT.parse(endTimeStr).getTime();
			if (startTime >= endTime) {
				throw new KGameServerException("初始化表<活动时间主城地图配置> 起始结束日期错误");
			}

			if (timeLimit != null && timeLimit.length() > 0) {
				String[] timeStr = timeLimit.split(",");
				if (timeStr != null) {
					timeIntervalList = new ArrayList<TimeIntervalStruct>();
					for (int i = 0; i < timeStr.length; i++) {
						String[] temp = (timeStr[i].substring(timeStr[i].indexOf("(") + 1, timeStr[i].indexOf(")"))).split("-");
						if (temp == null || temp.length != 2) {
							throw new KGameServerException("初始化表<活动时间主城地图配置>时间参数格式错误，解释指定当天时间段字符串参数出错，str=" + timeLimit);
						}
						long beginTime;
						long endTime;
						try {
							System.out.println(temp[0] + "----" + temp[1]);
							beginTime = UtilTool.parseHHmmToMillis(temp[0]);
							endTime = UtilTool.parseHHmmToMillis(temp[1]);
							System.out.println(beginTime + "----" + endTime);
						} catch (Exception e) {
							throw new KGameServerException("初始化表<活动时间主城地图配置>时间参数格式错误，解释指定当天时间段字符串参数出错，str=" + timeLimit);
						}

						if (beginTime >= endTime) {
							throw new KGameServerException("初始化表<活动时间主城地图配置>时间参数格式错误，解释指定当天时间段字符串参数出错，str=" + timeLimit);
						}

						TimeIntervalStruct struct = new TimeIntervalStruct(temp[0], temp[1], beginTime, endTime);
						timeIntervalList.add(struct);
					}

					if (!timeIntervalList.isEmpty()) {
						Collections.sort(timeIntervalList);
					}

					// camus：所有时间段必须互不重叠
					if (timeIntervalList.size() > 1) {
						TimeIntervalStruct min = timeIntervalList.get(0);
						for (int index = 1; index < timeIntervalList.size(); index++) {
							TimeIntervalStruct temp = timeIntervalList.get(index);
							if (temp.getBeginTime() <= min.getBeginTime()) {
								throw new KGameServerException("初始化表<活动时间主城地图配置>时间参数格式错误，当天时间段字符串参数时间段重叠，str=" + timeLimit);
							}
							min = temp;
						}
					}
				}
			} else {
				isFullTime = true;
			}
		}
	}
}
