package com.kola.kmp.logic.other;


/**
 * <pre>
 * 关于商店类型的枚举
 * 
 * @author CamusHuang
 * @creation 2012-11-26 下午8:53:15
 * </pre>
 */
public enum KShopTypeEnum {
	普通商店(1), //
	随机商店(2),//
	军团商店(3),//
	热购商店(4),//
	
	;//
	// 标识数值
	public final int sign;

	private KShopTypeEnum(int sign) {
		this.sign = sign;
	}
}
