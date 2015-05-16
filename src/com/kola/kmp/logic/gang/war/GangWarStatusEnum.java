package com.kola.kmp.logic.gang.war;

import java.util.HashMap;
import java.util.Map;

import com.kola.kmp.logic.other.KColorFunEnum;
import com.kola.kmp.logic.util.text.HyperTextTool;

/**
 * <pre>
 * 
 * 军团资源战状态
 * 状态机：
 * SIGNUP_START_NOW->
 * WAR_WAIT_NOW->WAR_ROUND_READY_NOW->WAR_ROUND_START_NOW->
 * WAR_WAIT_NOW->WAR_ROUND_READY_NOW->WAR_ROUND_START_NOW->
 * WAR_WAIT_NOW->WAR_ROUND_READY_NOW->WAR_ROUND_START_NOW->
 * WAR_WAIT_NOW->WAR_ROUND_READY_NOW->WAR_ROUND_START_NOW->
 * ->REST_START_NOW
 * 
 * @author camus
 * @creation 2012-12-31 下午5:08:51
 * </pre>
 */

public enum GangWarStatusEnum {
	SIGNUP_START_NOW(1, "报名开始"), //
	SIGNUP_END(2, "报名截止"), //
	WAR_WAIT_NOW(11, "对战等待"), //
	WAR_ROUND_READY_NOW(12, "对战准备"), //
	WAR_ROUND_START_NOW(13, "对战开始"), //
	WAR_ROUND_END_NOW(14, "对战结束"), //
	REST_START_NOW(101, "休战期开始"), //
	REST_END(102, "休战期结束"), //
	;

	// 标识数值
	public final byte sign;
	// 名称
	public final String name;

	private GangWarStatusEnum(int sign, String name) {
		this.sign = (byte) sign;
		this.name = name;
	}

	// 所有枚举
	private static final Map<Byte, GangWarStatusEnum> typeMap = new HashMap<Byte, GangWarStatusEnum>();
	static {
		GangWarStatusEnum[] enums = GangWarStatusEnum.values();
		GangWarStatusEnum type;
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
	static GangWarStatusEnum getEnum(byte sign) {
		return typeMap.get(sign);
	}
}