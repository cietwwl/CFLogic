package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;


public enum KMapDuplicateTypeEnum {
	世界BOSS副本地图类型(1),
	军团战副本地图类型(2),
	宝石副本地图类型(3),
	跑马副本地图类型(4),
	;
	

	public final byte type; // 数据类型的标识

	private KMapDuplicateTypeEnum(int pDataType) {
		this.type = (byte)pDataType;
	}

	// 所有枚举
	private static final Map<Byte, KMapDuplicateTypeEnum> enumMap = new HashMap<Byte, KMapDuplicateTypeEnum>();
	static {
		KMapDuplicateTypeEnum[] enums = KMapDuplicateTypeEnum.values();
		KMapDuplicateTypeEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			enumMap.put(type.type, type);
		}
	}
	/**
	 * <pre>
	 * 通过标识数值获取类型枚举对象
	 * 
	 * @param functionId
	 * @return
	 * @author CamusHuang
	 * @creation 2012-11-28 上午11:41:04
	 * </pre>
	 */
	public static KMapDuplicateTypeEnum getEnum(byte type) {
		return enumMap.get(type);
	}
}
