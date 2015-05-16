package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;

public enum KActivityTimeTypeEnum {
	ACTIVITY_TYPE_SPECIAL_TIME(0),//特殊时间开放的活动（不由活动配置表定义）
	
	ACTIVITY_TYPE_ANY_TIME(1),//每天24小时都开放的活动
	
	ACTIVITY_TYPE_EVERYDAY_TIMED(2),//每天某个时段开放的活动
	
	ACTIVITY_TYPE_WEEKDAY_TIMED(3),//每周的某几天的某个时段开放的活动（例如：周一、三、五的11点和23点）
	
	ACTIVITY_TYPE_APPOINTED_DAY(4),//指定一年的特定一天或连续特定几天的某些时段进行的活动
	;
	
	
	public final int activityType; // 数据类型的标识

	private KActivityTimeTypeEnum(int pDataType) {
		this.activityType =  pDataType;
	}

	// 所有枚举
	private static final Map<Integer, KActivityTimeTypeEnum> enumMap = new HashMap<Integer, KActivityTimeTypeEnum>();
	static {
		KActivityTimeTypeEnum[] enums = KActivityTimeTypeEnum.values();
		KActivityTimeTypeEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			enumMap.put(type.activityType, type);
		}
	}

	/**
	 * <pre>
	 * 通过标识数值获取枚举对象
	 * 
	 * @param type
	 * @return
	 * @creation 2012-12-3 下午3:53:28
	 * </pre>
	 */
	public static KActivityTimeTypeEnum getEnum(int activityType) {
		return enumMap.get(activityType);
	}

}
