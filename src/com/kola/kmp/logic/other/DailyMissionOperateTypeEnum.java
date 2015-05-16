package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;

public enum DailyMissionOperateTypeEnum {

	
	/**
	 * 提交任务领取奖励
	 */
	OPERATE_TYPE_SUBMIT(1,"领取奖励"),
	/**
	 * 消耗元宝自动完成任务并领取奖励
	 */
	OPERATE_TYPE_AUTO_SUBMIT(2,"自动完成"),
	/**
	 * 消耗元宝自动完成任务并领取奖励
	 */
	OPERATE_TYPE_AUTO_SEARCH_ROAD(3,"自动寻路")
	;
	
	public final byte operateType; // 数据类型的标识
	public final String typeName; // 数据类型的描述

	private DailyMissionOperateTypeEnum(int pDataType, String pDataDesc) {
		this.operateType = (byte) pDataType;
		this.typeName = pDataDesc;
	}

	// 所有枚举
	private static final Map<Byte, DailyMissionOperateTypeEnum> enumMap = new HashMap<Byte, DailyMissionOperateTypeEnum>();
	static {
		DailyMissionOperateTypeEnum[] enums = DailyMissionOperateTypeEnum.values();
		DailyMissionOperateTypeEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			enumMap.put(type.operateType, type);
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
	public static DailyMissionOperateTypeEnum getEnum(byte operateType) {
		return enumMap.get(operateType);
	}

}
