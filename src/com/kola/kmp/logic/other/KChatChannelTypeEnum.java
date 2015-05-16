package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * 系统(0), 世界(1), 附近(2), 军团(3), 组队(4), 私聊(5);
 * 
 * @author camus
 * @creation 2012-12-31 下午5:08:51
 * </pre>
 */

public enum KChatChannelTypeEnum {
	系统(0), //
	世界(1), //
	附近(2), //
	军团(3), //
	组队(4), //
	私聊(5);

	// 标识数值
	public final int sign;

	private KChatChannelTypeEnum(int sign) {
		this.sign = sign;
	}

	// 所有枚举
	private static final Map<Integer, KChatChannelTypeEnum> typeMap = new HashMap<Integer, KChatChannelTypeEnum>();
	static {
		KChatChannelTypeEnum[] enums = KChatChannelTypeEnum.values();
		KChatChannelTypeEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			typeMap.put(type.sign, type);
		}
	}

	// //////////////////
	/**
	 * <pre>
	 * 通过标识数值获取枚举对象
	 * 
	 * @param sign
	 * @return
	 * @author CamusHuang
	 * @creation 2012-11-5 上午10:53:13
	 * </pre>
	 */
	public static KChatChannelTypeEnum getEnum(int sign) {
		return typeMap.get(sign);
	}
}