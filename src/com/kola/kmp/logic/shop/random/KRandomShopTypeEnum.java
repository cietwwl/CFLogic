package com.kola.kmp.logic.shop.random;

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
public enum KRandomShopTypeEnum {
	VIP商城(21, "VIP商城"),//类型值必须避开货币类型值
	钻石商城(KCurrencyTypeEnum.DIAMOND, "钻石商城"), //
	金币商城(KCurrencyTypeEnum.GOLD, "金币商城"),//
	随从商城(20, "随从商城"),//类型值必须避开货币类型值
	;//
	// 标识数值
	public final int sign;
	// 名称
	public final String name;
	// 限制货币类型
	public KCurrencyTypeEnum moneyType;

	/**
	 * <pre>
	 * 类型值与货币类型值对应
	 * 
	 * @param moneyType
	 * @author CamusHuang
	 * @creation 2014-12-29 下午3:09:17
	 * </pre>
	 */
	private KRandomShopTypeEnum(KCurrencyTypeEnum moneyType, String name) {
		this.sign = moneyType.sign;
		this.name = name;
		this.moneyType = moneyType;
	}
	
	/**
	 * <pre>
	 * 类型值必须避开货币类型值
	 * 
	 * @param sign
	 * @author CamusHuang
	 * @creation 2014-12-29 下午3:09:30
	 * </pre>
	 */
	private KRandomShopTypeEnum(int sign, String name) {
		this.sign = sign;
		this.name = name;
		this.moneyType = null;
	}
	
	public static void checkType() throws KGameServerException{
		for(KRandomShopTypeEnum type:KRandomShopTypeEnum.values()){
			if(type.moneyType==null){
				if (KCurrencyTypeEnum.getEnum(type.sign) != null) {
					throw new KGameServerException("随机商品类型必须避开货币类型 name=" + type.name);
				}
			}
		}
	}
	
	// 所有枚举
	private static final Map<Integer, KRandomShopTypeEnum> typeMap = new HashMap<Integer, KRandomShopTypeEnum>();
	static {
		KRandomShopTypeEnum[] enums = KRandomShopTypeEnum.values();
		KRandomShopTypeEnum type;
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
	public static KRandomShopTypeEnum getEnum(int sign) {
		return typeMap.get(sign);
	}	
}
