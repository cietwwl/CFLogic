package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;

/**
 * 产出活动类型枚举
 * @author Administrator
 *
 */
public enum KLimitTimeProduceActivityTypeEnum {
	物资运输活动(1), //
	赚金币活动(2), //
	好友地下城活动(3), //
	精英副本技术副本活动(4), //
	随从副本活动(5), //
	神秘商店活动(6),
//	机甲进阶暴击几率翻倍(7),
	材料打折(8),
	金币兑换额外奖励(9),
	初级扭蛋10连抽概率翻倍(10),
//	限时热购(11),
	初级扭蛋10连抽打折(12),
	赚金币结算卡牌奖励翻倍(13),
	话费礼包活动(14),
	机甲升级经验倍率(15),
	随从合成经验倍率(16),
	限时充值优惠(17),
	节假副本特殊掉落(18),
	初级扭蛋10连抽送道具(19),
	高级扭蛋10连抽送道具(20),	
	高级扭蛋10连抽打折(21),
	精英副本消耗体力减半(22), //
	赚金币增加挑战次数(23), //
	装备升星成功率(24),	
//	机甲进阶10次保底小暴击(25),//
	幸运转盘打折(26),//
	幸运转盘10连抽送道具(27),//
	;

	// 功能编号
	public final int type;

	private KLimitTimeProduceActivityTypeEnum(int type) {
		this.type =  type;
	}

	// 所有枚举
	private static final Map<Integer, KLimitTimeProduceActivityTypeEnum> enumMap = new HashMap<Integer, KLimitTimeProduceActivityTypeEnum>();

	static {
		KLimitTimeProduceActivityTypeEnum[] enums = KLimitTimeProduceActivityTypeEnum.values();
		KLimitTimeProduceActivityTypeEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			enumMap.put(type.type, type);
		}
	}

	/**
	 * <pre>
	 * 通过标识数值获取枚举FunctionTypeEnum对象
	 * 
	 * @param type
	 * @return
	 * @creation 2012-12-3 下午3:53:28
	 * </pre>
	 */
	public static KLimitTimeProduceActivityTypeEnum getEnum(int type) {
		return enumMap.get(type);
	}
}
