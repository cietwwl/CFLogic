package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * 道具(1), 图片(2), 声音(3), 菜单(4);
 * 
 * @author camus
 * @creation 2012-12-31 下午5:08:51
 * </pre>
 */
public enum KChatLinkedTypeEnum {
	 道具(1), //
	 图片(2), //
	 声音(3), //
	 菜单(4);

	// 标识数值
	public final int sign;

	private KChatLinkedTypeEnum(int sign) {
		this.sign = sign;
	}

	// 所有枚举
	private static final Map<Integer, KChatLinkedTypeEnum> typeMap = new HashMap<Integer, KChatLinkedTypeEnum>();
	static {
		KChatLinkedTypeEnum[] enums = KChatLinkedTypeEnum.values();
		KChatLinkedTypeEnum type;
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
	public static KChatLinkedTypeEnum getEnum(int sign) {
		return typeMap.get(sign);
	}
}