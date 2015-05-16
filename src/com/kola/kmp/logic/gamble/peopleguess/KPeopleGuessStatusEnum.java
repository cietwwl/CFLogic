package com.kola.kmp.logic.gamble.peopleguess;


import java.util.HashMap;
import java.util.Map;


/**
 * 跑马状态
 * @author Alex
 * @create 2015年3月2日 下午5:19:46
 */
public enum KPeopleGuessStatusEnum {
	
	STATUS_YABAO(1, "投注状态"),
	STATUS_WAITING_RACE(2, "等待开跑状态"),
	STATUS_RACING(3, "开跑状态"),
	STATUS_SETTLE_PRICE(4, "结算状态"),
	;
	

	public final byte status; // 数据类型的标识
	public final String statusName; // 数据类型的描述

	private KPeopleGuessStatusEnum(int pDataType, String pDataDesc) {
		this.status = (byte)pDataType;
		this.statusName = pDataDesc;
	}

	// 所有枚举
	private static final Map<Byte, KPeopleGuessStatusEnum> enumMap = new HashMap<Byte, KPeopleGuessStatusEnum>();
	
	static {
		KPeopleGuessStatusEnum[] enums = KPeopleGuessStatusEnum.values();
		KPeopleGuessStatusEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			enumMap.put(type.status, type);
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
	public static KPeopleGuessStatusEnum getEnum(byte status) {
		return enumMap.get(status);
	}
	
	
}
