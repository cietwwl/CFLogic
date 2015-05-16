package com.kola.kmp.logic.reward.activatecode;

import java.util.HashMap;
import java.util.Map;

import com.koala.game.exception.KGameServerException;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;


/**
 * <pre>
 * 关于激活码渠道类型的枚举
 * 
 * @author CamusHuang
 * @creation 2014-12-29 下午2:49:15
 * </pre>
 */
public enum KActivateCodePromoTypeEnum {
	通用(0), //
	梦想(1),//
	YY(2),//
	;//
	// 标识数值
	public final int sign;

	/**
	 * <pre>
	 * 类型值与货币类型值对应
	 * 
	 * @param moneyType
	 * @author CamusHuang
	 * @creation 2014-12-29 下午3:09:17
	 * </pre>
	 */
	private KActivateCodePromoTypeEnum(int sign) {
		this.sign = sign;
	}
	
	// 所有枚举
	private static final Map<Integer, KActivateCodePromoTypeEnum> typeMap = new HashMap<Integer, KActivateCodePromoTypeEnum>();
	static {
		KActivateCodePromoTypeEnum[] enums = KActivateCodePromoTypeEnum.values();
		KActivateCodePromoTypeEnum type;
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
	public static KActivateCodePromoTypeEnum getEnum(int sign) {
		return typeMap.get(sign);
	}	
}
