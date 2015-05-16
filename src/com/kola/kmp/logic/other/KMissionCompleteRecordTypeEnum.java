package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;

public enum KMissionCompleteRecordTypeEnum {
	/**
	 * <pre>
	 * 表示普通任务完成记录的数据类型。
	 * </pre>
	 */
	DB_TYPE_NORMAL(0);
	
	
	
	private final int dbType; // 数据类型的标识
	
	private KMissionCompleteRecordTypeEnum(int pDataType) {
		this.dbType =  pDataType;
	}

	// 所有枚举
	private static final Map<Integer, KMissionCompleteRecordTypeEnum> enumMap = new HashMap<Integer, KMissionCompleteRecordTypeEnum>();
	static {
		KMissionCompleteRecordTypeEnum[] enums = KMissionCompleteRecordTypeEnum.values();
		KMissionCompleteRecordTypeEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			enumMap.put(type.dbType, type);
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
	public static KMissionCompleteRecordTypeEnum getEnum(int dbType) {
		return enumMap.get(dbType);
	}

	public int getDbType() {
		return dbType;
	}
}
