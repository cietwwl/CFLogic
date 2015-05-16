package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;

public enum KGameLevelRecordDBTypeEnum {

	/**
	 * <pre>
	 * 表示普通关卡的数据记录类型
	 * </pre>
	 */
	普通关卡数据记录(0),

	/**
	 * <pre>
	 * 表示精英副本关卡的数据记录类型
	 * </pre>
	 */
	精英副本关卡数据记录(1),

	/**
	 * <pre>
	 * 表示普通关卡剧本数据记录
	 * </pre>
	 */
	普通关卡剧本数据记录(2),

	/**
	 * <pre>
	 * 表示技术副本关卡的数据记录类型
	 * </pre>
	 */
	技术副本关卡数据记录(3),
	/**
	 * <pre>
	 * 表示好友副本关卡的数据记录类型
	 * </pre>
	 */
	好友副本关卡数据记录(4),
	/**
	 * <pre>
	 * 表示随从副本关卡的数据记录类型
	 * </pre>
	 */
	随从副本关卡数据记录(5),
	/**
	 * <pre>
	 * 表示爬塔副本关卡的数据记录类型
	 * </pre>
	 */
	爬塔副本关卡数据记录(6),
	/**
	 * <pre>
	 * 表示随从挑战副本关卡的数据记录类型
	 * </pre>
	 */
	随从挑战副本关卡数据记录(7),
	/**
	 * <pre>
	 * 表示高级随从挑战副本关卡的数据记录类型
	 * </pre>
	 */
	高级随从挑战副本关卡数据记录(8);

	private final int levelRecordDbType; // 数据类型的标识

	private KGameLevelRecordDBTypeEnum(int pDataType) {
		this.levelRecordDbType = pDataType;
	}

	// 所有枚举
	private static final Map<Integer, KGameLevelRecordDBTypeEnum> enumMap = new HashMap<Integer, KGameLevelRecordDBTypeEnum>();
	static {
		KGameLevelRecordDBTypeEnum[] enums = KGameLevelRecordDBTypeEnum
				.values();
		KGameLevelRecordDBTypeEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			enumMap.put(type.levelRecordDbType, type);
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
	public static KGameLevelRecordDBTypeEnum getEnum(int levelRecordDbType) {
		return enumMap.get(levelRecordDbType);
	}

	public int getLevelRecordDbType() {
		return levelRecordDbType;
	}

}
