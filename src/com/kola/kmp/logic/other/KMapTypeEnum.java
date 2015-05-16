package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;


public enum KMapTypeEnum {
	普通主城地图(1),
	副本地图(2),
	;
	

	public final byte type; // 数据类型的标识

	private KMapTypeEnum(int pDataType) {
		this.type = (byte)pDataType;
	}

	// 所有枚举
	private static final Map<Byte, KMapTypeEnum> enumMap = new HashMap<Byte, KMapTypeEnum>();
	static {
		KMapTypeEnum[] enums = KMapTypeEnum.values();
		KMapTypeEnum type;
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
	public static KMapTypeEnum getEnum(byte type) {
		return enumMap.get(type);
	}
}
