package com.kola.kmp.logic.reward.exciting;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * 关于精彩活动奖项状态的枚举
 * （0未达成，1可领取，2邮件已发送奖励，3自动发奖，4发放完毕）
 * 
 * @author CamusHuang
 * @creation 2013-7-6 下午12:24:43
 * </pre>
 */
public enum ExcitiongStatusEnum {
	NOT_FINISHED(0, "未达成", false), //
	FINISHED(1, "可领取", true), //
	COLLECTED(2, "已发奖", false), //
	AUTOSENT(3, "自动发奖", false),//
	EMPTY(4, "发放完毕", false),// 全服限量且已发完
	;//
	// 标识数值
	public final byte sign;
	// 名称
	public final String name;
	// 是否UI按钮可操作
	public final boolean isCanPress;

	private ExcitiongStatusEnum(int sign, String name, boolean isCanPress) {
		this.sign = (byte) sign;
		this.name = name;
		this.isCanPress = isCanPress;
	}

	// 所有枚举
	private static final Map<Byte, ExcitiongStatusEnum> dataMap = new HashMap<Byte, ExcitiongStatusEnum>();
	static {
		ExcitiongStatusEnum[] enums = ExcitiongStatusEnum.values();
		ExcitiongStatusEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			dataMap.put(type.sign, type);
		}
	}

	// //////////////////
	/**
	 * <pre>
	 * 通过标识数值获取类型枚举对象
	 * 
	 * @param sign
	 * @return
	 * @author CamusHuang
	 * @creation 2012-11-5 上午10:53:13
	 * </pre>
	 */
	public static ExcitiongStatusEnum getEnum(byte sign) {
		return dataMap.get(sign);
	}
}
