package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;

import com.kola.kmp.logic.util.text.HyperTextTool;

/**
 * <pre>
 * 道具品质枚举
 * 
 * 绿(优秀)、蓝（精良）、紫（史诗）、橙（传说）、红（无敌）
 * @author CamusHuang
 * @creation 2012-11-26 下午8:22:23
 * </pre>
 */
public enum KItemQualityEnum {
	优秀的	(	1, "优秀的",	KColorFunEnum.品质_绿, "优秀"),//	优秀
	精良的	(	2, "精良的",	KColorFunEnum.品质_蓝, "精良"),//	精良
	史诗的	(	3, "史诗的",	KColorFunEnum.品质_紫, "史诗"),//	史诗
	传说的	(	4, "传说的",	KColorFunEnum.品质_橙, "传说"),//	传说		
	无敌的	(	5, "无敌的",	KColorFunEnum.品质_红, "无敌");//	无敌		

	/** 装备类型值，即身体部位标识 */
	public final int sign;
	// 名称
	public final String name;	
	public final KColorFunEnum color;
	public final String descName;

	private KItemQualityEnum(int type, String name, KColorFunEnum color, String descName) {
		this.sign = type;
		this.name = name;
		this.color = color;
		this.descName = HyperTextTool.extColor(descName, color);
	}

	/**
	 * <pre>
	 * 所有的道具品质,MAP形式
	 * KEY=装备类型值，即身体部位值
	 * </pre>
	 */
	private static final Map<Integer, KItemQualityEnum> enumMap = new HashMap<Integer, KItemQualityEnum>();
	static {
		KItemQualityEnum[] enums = KItemQualityEnum.values();
		KItemQualityEnum type;
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
	public static KItemQualityEnum getEnum(int type) {
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
