package com.kola.kmp.logic.util.text;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * 富文本协议：	
 * 标签	效果
 * [b]文本[/b]	加粗
 * [i]文本[/i]	斜体
 * [u]文本[/u]	下划线：
 * [s]文本[/s]	删除线
 * [sub]文本[/sub]	向下缩小
 * [sup]文本[/sup]	向上缩小
 * [emo]xx[/emo]	表情，xx是表情索引，从01开始
 * [url=http://www.kl321.com/]链接[/url]	链接
 * [c?]文本[-]	变色，c?是颜色符号，参看【颜色表】
 * [ffffff]文本[-]	变色，ffffff是颜色值
 * 
 * @author CamusHuang
 * @creation 2014-4-7 下午12:57:30
 * </pre>
 */
public enum HyperTextTypeEnum {
	/** 粗体字 */
	b("[b]", "[/b]"),
	/** 斜体 */
	i("[i]", "[/i]"),
	/** 下划线 */
	u("[u]", "[/u]"),
	/** 删除线 */
	s("[s]", "[/s]"),
	/** 向下缩小 */
	sub("[sub]", "[/sub]"),
	/** 向上缩小 */
	sup("[sup]", "[/sup]"),
	/** 表情 */
	emo("[emo]", "[/emo]"),
	/** 链接 */
	url("[url=]", "[/url]"),
	/** 颜色 */
	col("[]", "[-]");

	public final String startSign;
	public final String endSign;

	private HyperTextTypeEnum(String startSign, String endSign) {
		this.startSign = startSign;
		this.endSign = endSign;
	}

	// 所有枚举
	private static final Map<String, HyperTextTypeEnum> typeMap = new HashMap<String, HyperTextTypeEnum>();
	static {
		for (HyperTextTypeEnum type : HyperTextTypeEnum.values()) {
			typeMap.put(type.startSign, type);
			typeMap.put(type.endSign, type);
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
	public static HyperTextTypeEnum getEnum(String sign) {
		return typeMap.get(sign);
	}

}
