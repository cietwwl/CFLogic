package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * 
 * 1	钻石	付费货币，通过充值兑换，用于使用付费功能和物品购买	玩家通过充值获得，无法交易，可以用于兑换金币
 * 2	金币	系统基础消耗货币，非付费货币，可以用于功能消耗和物品购买	主要产出的消耗型货币
 * 3	潜能	副本产出，用于升级技能、天赋等功能消耗	
 * 4	荣誉	竞技场产出货币，可以用于功能使用消耗和物品兑换	
 * 6	军团贡献	在军团中捐献金币、潜能和钻石后转换的货币	仅供军团内使用军团建筑和兑换物品消耗
 * 7	徽记类	通过关卡或功能产出	放在背包内，用于物品兑换时消耗
 * 8    扭蛋券     通过活动赠送产出          用于许愿时的消耗
 * 
 * @author camus
 * @creation 2012-12-31 下午5:08:51
 * </pre>
 */

public enum KCurrencyTypeEnum {
	DIAMOND(1, "钻石"), //
	GOLD(2, "金币"), //
	POTENTIAL(3, "潜能"), //
	SCORE(4, "荣誉"), //
	GANG_CONTRIBUTION(6, "军团贡献"), //
	EMBLEM(7, "徽记"),
	WISH_POINT(8,"扭蛋券"),//
	;

	// 标识数值
	public final byte sign;
	// 名称
	public final String name;
	// 富文本名称
	public final String extName;

	private KCurrencyTypeEnum(int sign, String name) {
		this.sign = (byte) sign;
		this.name = name;
		this.extName = name;// HyperTextTool.extColor(name,
							// ExtColorType.货币);
	}

	// 所有枚举
	private static final Map<Byte, KCurrencyTypeEnum> typeMap = new HashMap<Byte, KCurrencyTypeEnum>();
	static {
		KCurrencyTypeEnum[] enums = KCurrencyTypeEnum.values();
		KCurrencyTypeEnum type;
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
	public static KCurrencyTypeEnum getEnum(byte sign) {
		return typeMap.get(sign);
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
	public static KCurrencyTypeEnum getEnum(int sign) {
		return typeMap.get((byte) sign);
	}
}