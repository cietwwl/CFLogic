package com.kola.kmp.logic.gang;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.kola.kmp.logic.rank.gang.KGangRankTypeEnum;

/**
 * <pre>
 * 军团职位
 * 
 * @author camus
 * @creation 2012-12-31 下午5:08:51
 * </pre>
 */

public enum KGangPositionEnum {
	军团长(1, "军团长", 1), //
	副团长(2, "副团长", 1), //
	监察长(3, "监察长", 4), //
	成员(4, "成员", 300),//
	;

	// 标识数值
	public final int sign;
	// 名称
	public final String name;	
	// 最大人数(一个军团中)
	public final int size;

	private KGangPositionEnum(int sign, String name, int size) {
		this.sign = sign;
		this.name = name;
		this.size = size;
	}

	// 所有枚举
	private static final Map<Integer, KGangPositionEnum> typeMap = new HashMap<Integer, KGangPositionEnum>();
	static {
		KGangPositionEnum[] enums = KGangPositionEnum.values();
		KGangPositionEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			typeMap.put(type.sign, type);
		}
	}
	
	
	public static Collection<KGangPositionEnum> getAll(){
		return typeMap.values();
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
	public static KGangPositionEnum getEnum(int sign) {
		return typeMap.get(sign);
	}
}