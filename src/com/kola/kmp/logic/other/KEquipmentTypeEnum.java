package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * 装备类型枚举，即身体部位枚举
 * 
 * @author CamusHuang
 * @creation 2012-11-26 下午8:22:23
 * </pre>
 */
public enum KEquipmentTypeEnum {
	主武器	(	1, "主武器"	),//
	副武器	(	2, "副武器"	),//
	头盔	(	3, "头盔"	),//	
	胸甲	(	4, "胸甲"	),//	
	护手	(	5, "护手"	),//	
	腿甲	(	6, "腿甲"	),//	
	腰带	(	7, "腰带"	),//	
	战靴	(	8, "战靴"	),//	
	项链	(	9, "项链"	),//	
	指环	(	10, "指环"	);//	

	/** 装备类型值，即身体部位标识 */
	public final int sign;
	// 名称
	public final String name;	

	private KEquipmentTypeEnum(int sign, String name) {
		this.sign = sign;
		this.name = name;
	}

	/**
	 * <pre>
	 * 所有的装备类型,MAP形式
	 * KEY=装备类型值，即身体部位值
	 * </pre>
	 */
	private static final Map<Integer, KEquipmentTypeEnum> enumMap = new HashMap<Integer, KEquipmentTypeEnum>();
	static {
		KEquipmentTypeEnum[] enums = KEquipmentTypeEnum.values();
		KEquipmentTypeEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			enumMap.put(type.sign, type);
		}
	}

	/**
	 * <pre>
	 * 通过标识数值获取类型枚举对象
	 * 
	 * @param type
	 * @return
	 * @author CamusHuang
	 * @creation 2012-11-28 上午11:43:27
	 * </pre>
	 */
	public static KEquipmentTypeEnum getEnum(int type) {
		return enumMap.get(type);
	}

	/**
	 * <pre>
	 * 是否存在指定类型的枚举
	 * 
	 * @param equiType
	 * @return
	 * @author CamusHuang
	 * @creation 2012-11-28 上午11:43:31
	 * </pre>
	 */
	public static boolean containEquipmentType(int type) {
		return enumMap.containsKey(type);
	}

}
