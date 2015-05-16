package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * 关系类型定义
 * 
 * @author CamusHuang
 * @creation 2014-3-11 下午3:01:00
 * </pre>
 */
public enum KRelationShipTypeEnum {
	好友(1, "好友"), //
	好友申请(2, "好友申请"), //
	最近联系人(3, "最近联系人"), //
	黑名单(4, "黑名单"), //
	附近的人(5, "附近的人"), //
	;
	// 标识数值
	public final int sign;
	// 名称
	public final String name;
	private int maxNum;// 最大人数限制

	private KRelationShipTypeEnum(int sign, String name) {
		this.sign = sign;
		this.name = name;
	}

	public void setMaxNum(int num) {
		maxNum = num;
	}

	public int getMaxNum() {
		return maxNum;
	}

	// 所有枚举
	private static final Map<Integer, KRelationShipTypeEnum> typeMap = new HashMap<Integer, KRelationShipTypeEnum>();
	static {
		KRelationShipTypeEnum[] enums = KRelationShipTypeEnum.values();
		KRelationShipTypeEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			typeMap.put(type.sign, type);
		}
	}

	// //////////////////
	/**
	 * <pre>
	 * 通过标识数值获取枚举对象
	 * 
	 * @param sign
	 * @return
	 * @author CamusHuang
	 * @creation 2012-11-5 上午10:53:13
	 * </pre>
	 */
	public static KRelationShipTypeEnum getEnum(byte sign) {
		return getEnum((int)sign);

	}

	/**
	 * <pre>
	 * 通过标识数值获取枚举对象
	 * 
	 * @param sign
	 * @return
	 * @author CamusHuang
	 * @creation 2012-11-5 上午10:53:13
	 * </pre>
	 */
	public static KRelationShipTypeEnum getEnum(int sign) {
		return typeMap.get(sign);
	}
}
