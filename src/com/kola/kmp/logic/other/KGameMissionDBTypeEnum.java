package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;

/**
 * 任务数据的数据库保存的数据记录类型枚举
 * @author zhaizl
 *
 */
public enum KGameMissionDBTypeEnum {
	/**
	 * <pre>
	 * 表示空数据任务的数据记录类型，该类型在缓存和DB中表现为一笔没有任何数据的空闲记录，可被系统重用。
	 * 当角色完成一个任务时，该笔任务数据对象并不会删除，而是将其数据库保存类型设置为空数据状态，
	 * 等待被下一个接受的新任务使用。
	 * </pre>
	 */
	MISSION_DB_TYPE_EMPTY(0),
	/**
	 * <pre>
	 * 未关闭的任务数据记录。
	 * 该类型表示一个角色接受一个新任务后，产生的任务数据记录就会设为未关闭状态。当角色完成这个任务后，
	 * 该记录会被设为空数据状态（即MISSION_DB_TYPE_EMPTY），而这个任务对应的任务模版ID会被记录在一个已完成任务
	 * 数据记录类型的任务数据记录中（即类型为MISSION_DB_TYPE_CLOSED的KMission任务记录）。 
	 * </pre>
	 */
	MISSION_DB_TYPE_UNCLOSED(1),
	
	/**
	 * <pre>
	 * 日常任务的数据记录类型。
	 * </pre>
	 */
	MISSION_DB_TYPE_DAILY(2);
	
	
	private final int missionDbType; // 数据类型的标识
	
	private KGameMissionDBTypeEnum(int pDataType) {
		this.missionDbType =  pDataType;
	}

	// 所有枚举
	private static final Map<Integer, KGameMissionDBTypeEnum> enumMap = new HashMap<Integer, KGameMissionDBTypeEnum>();
	static {
		KGameMissionDBTypeEnum[] enums = KGameMissionDBTypeEnum.values();
		KGameMissionDBTypeEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			enumMap.put(type.missionDbType, type);
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
	public static KGameMissionDBTypeEnum getEnum(int missionDbType) {
		return enumMap.get(missionDbType);
	}

	public int getMissionDbType() {
		return missionDbType;
	}
	
	

}
