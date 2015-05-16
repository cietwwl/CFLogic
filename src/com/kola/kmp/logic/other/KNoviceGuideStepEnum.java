package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;

/**
 * 新手引导流程步骤枚举
 * @author Administrator
 *
 */
public enum KNoviceGuideStepEnum {
	插画开始(1),
	插画结束(2),//
	进入战场(3),//
	操作引导结束(4),//
	第一波战斗结束(5),//
	第二波战斗结束(6),//
	坐骑剧情(7),//*
	第三波战斗结束(8),//*
	结束剧情(9),//
	结算界面(10),//(服务器处理)*
	进入第一个主城(11),//(服务器处理)*
	;

	

	public final int type; // 数据类型的标识

	private KNoviceGuideStepEnum(int pDataType) {
		this.type = pDataType;
	}

	// 所有枚举
	private static final Map<Integer, KNoviceGuideStepEnum> enumMap = new HashMap<Integer, KNoviceGuideStepEnum>();
	static {
		KNoviceGuideStepEnum[] enums = KNoviceGuideStepEnum.values();
		KNoviceGuideStepEnum type;
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
	public static KNoviceGuideStepEnum getEnum(int type) {
		return enumMap.get(type);
	}
}
