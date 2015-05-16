package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;

public enum KGameBattlefieldTypeEnum {
	
	/**
	 * 战斗场景类型为普通关卡类型
	 */
	普通关卡战场(1),

	/**
	 * 战斗场景类型为精英副本战场
	 */
	精英副本战场(2),
	/**
	 * 战斗场景类型为技术副本战场
	 */
	技术副本战场(3),
	/**
	 * 战斗场景类型为好友副本战场
	 */
	好友副本战场(4),	
	/**
	 * 战斗场景类型为世界BOSS战场
	 */
	世界BOSS战场(5),
	
	/**
	 * 战斗场景类型为军团战BOSS战场
	 */
	军团战BOSS战场(6),
	
	/**
	 * 战斗场景类型为新手引导战场
	 */
	新手引导战场(7),
	
	/**
	 * 战斗场景类型为产金活动战场
	 */
	产金活动战场(8),
	
	/**
	 * 战斗场景类型为新产金活动战场
	 */
	新产金活动战场(9),
	
	/**
	 * 战斗场景类型为随从副本战场
	 */
	随从副本战场(10),
	
	/**
	 * 战斗场景类型为爬塔副本战场
	 */
	爬塔副本战场(11),
	/**
	 * 战斗场景类型为随从挑战副本战场
	 */
	随从挑战副本战场(12),
	/**
	 * 战斗场景类型为高级随从挑战副本战场
	 */
	高级随从挑战副本战场(13),
	;
	
	public final byte battlefieldType; // 数据类型的标识
	

	private KGameBattlefieldTypeEnum(int pDataType) {
		this.battlefieldType = (byte) pDataType;
	}

	// 所有枚举
	private static final Map<Byte, KGameBattlefieldTypeEnum> enumMap = new HashMap<Byte, KGameBattlefieldTypeEnum>();
	static {
		KGameBattlefieldTypeEnum[] enums = KGameBattlefieldTypeEnum.values();
		KGameBattlefieldTypeEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			enumMap.put(type.battlefieldType, type);
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
	public static KGameBattlefieldTypeEnum getEnum(byte battlefieldType) {
		return enumMap.get(battlefieldType);
	}

}
