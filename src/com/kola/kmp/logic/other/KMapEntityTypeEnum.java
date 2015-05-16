package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;


public enum KMapEntityTypeEnum {
	/**
	 * 表示类型为玩家角色的地图实体
	 */
	ENTITY_TYPE_DEFAULT(1<<1),
	/**
	 * 表示类型为NPC的地图实体
	 */
	ENTITY_TYPE_NPC(1<<2),
	/**
	 * 表示类型为怪物的地图实体
	 */
	ENTITY_TYPE_MONSTER(1<<3),
	/**
	 * 表示类型为默认地图出生点的地图实体
	 */
	ENTITY_TYPE_BORN_POINT(1<<4),
	/**
	 * 表示类型为玩家角色的地图实体
	 */
	ENTITY_TYPE_PLAYERROLE(1<<6),
	/**
	 * 表示类型为玩家角色的地图实体
	 */
	ENTITY_TYPE_OBSTRUCTION(1<<5),
	/**
	 * 表示类型为出口的地图实体
	 */
	ENTITY_TYPE_MAP_EXIT(1<<8),	
	/**
	 * 表示类型为出口的地图实体
	 */
	ENTITY_TYPE_LEVEL_EXIT(1<<9),
	/**
	 * 表示类型为战场分段的地图实体
	 */
	ENTITY_TYPE_SECTION_POINT(1<<10),
	/**
	 * 表示类型为玩家角色的宠物（随从）
	 */
	ENTITY_TYPE_PET(1<<15);
	
	public final int entityType; // 数据类型的标识
	

	private KMapEntityTypeEnum(int pDataType) {
		this.entityType = pDataType;
	}

	// 所有枚举
	private static final Map<Integer, KMapEntityTypeEnum> enumMap = new HashMap<Integer, KMapEntityTypeEnum>();
	static {
		KMapEntityTypeEnum[] enums = KMapEntityTypeEnum.values();
		KMapEntityTypeEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			enumMap.put(type.entityType, type);
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
	public static KMapEntityTypeEnum getEnum(Integer entityType) {
		return enumMap.get(entityType);
	}
}
