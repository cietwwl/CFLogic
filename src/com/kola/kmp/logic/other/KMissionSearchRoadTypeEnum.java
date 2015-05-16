package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;

/**
 * 任务系统自动寻路类型枚举
 * @author zhaizl
 *
 */
public enum KMissionSearchRoadTypeEnum {

	/**
	 * 自动寻路目标类型为NPC(操作类型为
	 * {@link KMissionSearchRoadTypeEnum#OPERATE_TYPE_REQUEST_SERVER})
	 */
	SEARCH_ROAD_TYPE_NPC(1, 1),

	/**
	 * 自动寻路目标类型为关卡(操作类型为
	 * {@link KMissionSearchRoadTypeEnum#OPERATE_TYPE_REQUEST_SERVER})
	 */
	SEARCH_ROAD_TYPE_LEVEL(2, 1),

	/**
	 * 自动寻路目标类型为道具(操作类型为
	 * {@link KMissionSearchRoadTypeEnum#OPERATE_TYPE_CLIENT_LOCAL})
	 */
	SEARCH_ROAD_TYPE_ITEM(3, 2),

	/**
	 * 自动寻路目标类型为功能类型(操作类型为
	 * {@link KMissionSearchRoadTypeEnum#OPERATE_TYPE_CLIENT_LOCAL})
	 */
	SEARCH_ROAD_TYPE_FUNCTION(4, 2),
	
	/**
	 * 自动寻路目标类型为打开任务面板的未接任务类型(操作类型为
	 * {@link KMissionSearchRoadTypeEnum#OPERATE_TYPE_CLIENT_LOCAL})
	 */
	SEARCH_ROAD_TYPE_MISSION_PANEL(5, 2),
	
	/**
	 * 自动寻路目标类型为打开答题界面(操作类型为
	 * {@link KMissionSearchRoadTypeEnum#OPERATE_TYPE_REQUEST_SERVER})
	 */
	SEARCH_ROAD_TYPE_QUESTION(6, 1);

	/**
	 * 表示自动寻路的客户端操作类型为请求服务器计算寻路
	 */
	public static final byte OPERATE_TYPE_REQUEST_SERVER = 1;

	/**
	 * 表示自动寻路的客户端操作类型为本地操作（即使用本地某个道具或者功能）
	 */
	public static final byte OPERATE_TYPE_CLIENT_LOCAL = 2;

	private byte searchRoadType;
	private byte operateType;

	private KMissionSearchRoadTypeEnum(int searchRoadType, int operateType) {
		this.searchRoadType = (byte) searchRoadType;
		this.operateType = (byte) operateType;
	}

	// 所有枚举
	private static final Map<Byte, KMissionSearchRoadTypeEnum> enumMap = new HashMap<Byte, KMissionSearchRoadTypeEnum>();
	static {
		KMissionSearchRoadTypeEnum[] enums = KMissionSearchRoadTypeEnum
				.values();
		KMissionSearchRoadTypeEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			enumMap.put(type.searchRoadType, type);
		}
	}

	public byte getSearchRoadType() {
		return searchRoadType;
	}

	public byte getOperateType() {
		return operateType;
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
	public static KMissionSearchRoadTypeEnum getEnum(byte searchRoadType) {
		return enumMap.get(searchRoadType);
	}

}
