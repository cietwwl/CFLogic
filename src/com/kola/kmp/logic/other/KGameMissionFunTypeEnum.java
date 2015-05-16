package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;
/**
 * 任务功能目标类型枚举，分类例如对话、杀怪、收集道具、关卡任务等等
 * @author zhaizl
 *
 */
public enum KGameMissionFunTypeEnum {
    //"对话类型"
	MISSION_FUN_TYPE_DIALOG(1),
	//杀怪类型
	MISSION_FUN_TYPE_KILL_MONSTER(2), 
	//"收集道具类型"
	MISSION_FUN_TYPE_COLLECT_ITEMS(3), 
	//"关卡类型"
	MISSION_FUN_TYPE_GAME_LEVEL(4),
	//数值类型
	MISSION_FUN_TYPE_ATTRIBUTE_DATA(5), 
	//使用道具类型
	MISSION_FUN_TYPE_USE_ITEM(6), 
	//使用功能类型
	MISSION_FUN_TYPE_USE_FUNCTION(7),
	//直接战斗类型
	MISSION_FUN_BATTLEFIELD(8),
	//答题类型
	MISSION_FUN_TYPE_QUESTION(9),
	//提升功能等级类型
	MISSION_FUN_TYPE_UP_FUNC_LV(10);

	public final byte missionFunType; // 数据类型的标识

	private KGameMissionFunTypeEnum(int pDataType) {
		this.missionFunType = (byte) pDataType;
	}

	// 所有枚举
	private static final Map<Byte, KGameMissionFunTypeEnum> enumMap = new HashMap<Byte, KGameMissionFunTypeEnum>();
	static {
		KGameMissionFunTypeEnum[] enums = KGameMissionFunTypeEnum.values();
		KGameMissionFunTypeEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			enumMap.put(type.missionFunType, type);
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
	public static KGameMissionFunTypeEnum getEnum(byte missionFunType) {
		return enumMap.get(missionFunType);
	}

}
