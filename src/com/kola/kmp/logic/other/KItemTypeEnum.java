package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * 关于道具类型的枚举
 * 
 * @author CamusHuang
 * @creation 2012-11-26 下午8:53:15
 * </pre>
 */
public enum KItemTypeEnum {
	装备(1, "装备"), //
	/*时装(2, "时装"),*/ //
	消耗品(3, "消耗品"), //
	改造材料(4, "改造材料"), //
	固定宝箱(5, "固定宝箱"), //
	宝石(6, "宝石"), //
	随机宝箱(8, "随机宝箱"), //
	装备包(9, "装备包"); //
	
	// 标识数值
	public final int sign;
	// 名称
	public final String name;

	private KItemTypeEnum(int sign, String name) {
		this.sign = sign;
		this.name = name;
	}

	// 所有枚举
	private static final Map<Integer, KItemTypeEnum> ItemTypeMap = new HashMap<Integer, KItemTypeEnum>();
	static {
		KItemTypeEnum[] enums = KItemTypeEnum.values();
		KItemTypeEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			ItemTypeMap.put(type.sign, type);
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
	public static KItemTypeEnum getEnum(int sign) {
		return ItemTypeMap.get(sign);
	}
}
