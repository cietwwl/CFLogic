package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * 关于道具包类型的枚举
 * 
 * @author CamusHuang
 * @creation 2012-11-26 下午8:53:15
 * </pre>
 */
public enum KItemPackTypeEnum {
	BAG(1), // "背包"),
	BODYSLOT(3);// , "装备栏");
	// 标识数值
	public final int sign;

	private KItemPackTypeEnum(int sign) {
		this.sign = sign;
	}

	// 所有枚举
	private static final Map<Integer, KItemPackTypeEnum> PackTypeMap = new HashMap<Integer, KItemPackTypeEnum>();
	static {
		KItemPackTypeEnum[] enums = KItemPackTypeEnum.values();
		KItemPackTypeEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			PackTypeMap.put(type.sign, type);
		}
	}

	// //////////////////
	/**
	 * <pre>
	 * 通过标识数值获取类型枚举对象
	 * 
	 * @param sign
	 * @return
	 * @author CamusHuang
	 * @creation 2012-11-5 上午10:53:13
	 * </pre>
	 */
	public static KItemPackTypeEnum getEnum(int sign) {
		return PackTypeMap.get(sign);
	}
}
