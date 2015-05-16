package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * 军团科技类型
 * 
 * 1	随从合成经验加成：万分比提高随从合成时所获得的随从经验值
 * 2	赚金币产出加成：万分比提高赚金币活动最终结算获得的金币
 * 3	随从升星成功率：提高随从升星时的万分比成功率
 * 4	好友副本潜能产出：万分比提高通关好友副本时所获得的潜能
 * 5	竞技场天梯赛荣誉产出：万分比提高竞技场、天梯赛战斗结束时所获得的荣誉值
 * 
 * @author camus
 * @creation 2012-12-31 下午5:08:51
 * </pre>
 */

public enum KGangTecTypeEnum {
	随从合成经验加成(2), //
	赚金币产出加成(1), //
	好友副本潜能产出(3), //
	竞技场天梯赛荣誉产出(4), //
//	随从升星成功率(5), //
	;

	// 标识数值
	public final int sign;

	private KGangTecTypeEnum(int sign) {
		this.sign = sign;
	}

	// 所有枚举
	private static final Map<Integer, KGangTecTypeEnum> typeMap = new HashMap<Integer, KGangTecTypeEnum>();
	static {
		KGangTecTypeEnum[] enums = KGangTecTypeEnum.values();
		KGangTecTypeEnum type;
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
	public static KGangTecTypeEnum getEnum(int sign) {
		return typeMap.get(sign);
	}
}