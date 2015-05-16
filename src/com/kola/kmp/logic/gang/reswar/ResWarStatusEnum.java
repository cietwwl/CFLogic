package com.kola.kmp.logic.gang.reswar;

import java.util.HashMap;
import java.util.Map;

import com.kola.kmp.logic.other.KColorFunEnum;
import com.kola.kmp.logic.util.text.HyperTextTool;

/**
 * <pre>
 * 
 * 军团资源战状态
 * 时效：休战结束+竞价开始、竞价截止+战前准备、准备结束+对战开始、对战结束+结算+休战开始
 * 
 * @author camus
 * @creation 2012-12-31 下午5:08:51
 * </pre>
 */

enum ResWarStatusEnum {
	BID_START(1, "竞价开始"), //
	READY_START(2, "准备开始"), //
	WAR_START(3, "对战开始"), //
	REST_START(4, "休战开始"), //
	;

	// 标识数值
	public final byte sign;
	// 名称
	public final String name;

	private ResWarStatusEnum(int sign, String name) {
		this.sign = (byte) sign;
		this.name = name;
	}

	// 所有枚举
	private static final Map<Byte, ResWarStatusEnum> typeMap = new HashMap<Byte, ResWarStatusEnum>();
	static {
		ResWarStatusEnum[] enums = ResWarStatusEnum.values();
		ResWarStatusEnum type;
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
	static ResWarStatusEnum getEnum(byte sign) {
		return typeMap.get(sign);
	}
}