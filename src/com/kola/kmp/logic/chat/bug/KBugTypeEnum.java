package com.kola.kmp.logic.chat.bug;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * BUG\投诉\意见\其他
 * 
 * @author CamusHuang
 * @creation 2013-1-14 上午10:37:36
 * </pre>
 */
public enum KBugTypeEnum {
	BUG(1, "BUG"), //
	投诉(2, "投诉"), //
	意见(3, "意见"), //
	其他(4, "其他"), //
	;
	// 标识数值
	public final int sign;
	// 名称
	public final String name;

	private KBugTypeEnum(int sign, String name) {
		this.sign = sign;
		this.name = name;
	}

	// 所有枚举
	private static final Map<Integer, KBugTypeEnum> TypeMap = new HashMap<Integer, KBugTypeEnum>();
	static {
		KBugTypeEnum[] enums = KBugTypeEnum.values();
		KBugTypeEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			TypeMap.put(type.sign, type);
		}
	}

	// //////////////////
	/**
	 * <pre>
	 * 通过标识数值获取枚举对象
	 * 
	 * @param sign
	 * @return
	 * @author camus
	 * @creation 2012-12-30 下午11:56:06
	 * </pre>
	 */
	public static KBugTypeEnum getEnum(int sign) {
		return TypeMap.get(sign);
	}
}
