package com.kola.kmp.logic.shop.timehot;

import java.util.HashMap;
import java.util.Map;

import com.koala.game.exception.KGameServerException;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;


/**
 * <pre>
 * 关于随机商店类型的枚举
 * 
 * @author CamusHuang
 * @creation 2014-12-29 下午2:49:15
 * </pre>
 */
public enum KHotShopTypeEnum {
	时装(1, "时装"), //
	装备(2, "装备"),//
	随从(3, "随从"),//
	材料(4, "材料"),//
	;//
	// 标识数值
	public final int sign;
	// 名称
	public final String name;

	/**
	 * <pre>
	 * 类型值与货币类型值对应
	 * 
	 * @param moneyType
	 * @author CamusHuang
	 * @creation 2014-12-29 下午3:09:17
	 * </pre>
	 */
	private KHotShopTypeEnum(int sign, String name) {
		this.sign = sign;
		this.name = name;
	}
	
	// 所有枚举
	private static final Map<Integer, KHotShopTypeEnum> typeMap = new HashMap<Integer, KHotShopTypeEnum>();
	static {
		KHotShopTypeEnum[] enums = KHotShopTypeEnum.values();
		KHotShopTypeEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			typeMap.put(type.sign, type);
		}
	}

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
	public static KHotShopTypeEnum getEnum(int sign) {
		return typeMap.get(sign);
	}	
}
