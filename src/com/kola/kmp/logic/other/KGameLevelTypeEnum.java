package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;


public enum KGameLevelTypeEnum {
	/**
	 * 普通关卡类型
	 */
	普通关卡(1),
	/**
	 * 精英副本关卡类型
	 */
	精英副本关卡(2),
	/**
	 * 技术副本关卡类型
	 */
	技术副本关卡(3),
	/**
	 * 好友副本关卡类型
	 */
	好友副本关卡(4),
	/**
	 * 新手引导关卡类型
	 */
	新手引导关卡(5),
	/**
	 * 产金活动关卡类型
	 */
	产金活动关卡(6),
	/**
	 * 新产金活动关卡类型
	 */
	新产金活动关卡(7),
	/**
	 * 随从副本关卡类型
	 */
	随从副本关卡(8),
	/**
	 * 爬塔副本关卡类型
	 */
	爬塔副本关卡(9),
	/**
	 * 随从挑战副本关卡类型
	 */
	随从挑战副本关卡(10),
	/**
	 * 高级随从挑战副本关卡类型
	 */
	高级随从挑战副本关卡(11),
	;
	
	public final byte levelType; // 数据类型的标识
	

	private KGameLevelTypeEnum(int pDataType) {
		this.levelType = (byte) pDataType;
	}

	// 所有枚举
	private static final Map<Byte, KGameLevelTypeEnum> enumMap = new HashMap<Byte, KGameLevelTypeEnum>();
	static {
		KGameLevelTypeEnum[] enums = KGameLevelTypeEnum.values();
		KGameLevelTypeEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			enumMap.put(type.levelType, type);
		}
	}

	/**
	 * <pre>
	 * 通过标识数值获取枚举对象
	 * 
	 * @param type
	 * @return
	 * @author CamusHuang
	 * @creation 2012-12-3 下午3:53:28
	 * </pre>
	 */
	public static KGameLevelTypeEnum getEnum(byte levelType) {
		return enumMap.get(levelType);
	}
}
