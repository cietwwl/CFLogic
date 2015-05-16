package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * 任务的状态分类
 * 1完全不可接，2可接但未接，3已接但未完成，4已完成且可提交
 * 
 * @author CamusHuang
 * @creation 2012-12-3 下午3:53:28
 * </pre>
 */
public enum KGameMissionStatusEnum {
	/**
	 * 任务状态：已接但未完成
	 */
	MISSION_STATUS_TRYFINISH(1, 2," （未完成）"),
	/**
	 * 任务状态：可接但未接
	 */
	MISSION_STATUS_TRYRECEIVE(2, 1," （可接受）"),	 
	/**
	 * 任务状态：已完成且可提交
	 */
	MISSION_STATUS_TRYSUBMIT(3,3, " （可交付）") ;
	
	public final byte statusType; // 数据类型的标识
	public final byte statusTrackingSerial;//任务跟踪时的状态排序
	public final String statusName; // 数据类型的描述
	

	private KGameMissionStatusEnum(int pDataType,int statusTrackingSerial, String pDataDesc) {
		this.statusType = (byte) pDataType;
		this.statusTrackingSerial = (byte) statusTrackingSerial;
		this.statusName = pDataDesc;
	}

	// 所有枚举
	private static final Map<Byte, KGameMissionStatusEnum> enumMap = new HashMap<Byte, KGameMissionStatusEnum>();
	static {
		KGameMissionStatusEnum[] enums = KGameMissionStatusEnum.values();
		KGameMissionStatusEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			enumMap.put(type.statusType, type);
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
	public static KGameMissionStatusEnum getEnum(byte statusType) {
		return enumMap.get(statusType);
	}
}
