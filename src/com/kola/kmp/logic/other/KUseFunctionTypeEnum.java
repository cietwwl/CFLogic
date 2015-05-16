package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;

public enum KUseFunctionTypeEnum {

	升级技能(KFunctionTypeEnum.技能), //
	升级被动技能(KFunctionTypeEnum.被动技能), //
	装备强化(KFunctionTypeEnum.强化), //
	装备升星(KFunctionTypeEnum.升星), //
	装备继承(KFunctionTypeEnum.继承), //
	装备镶嵌(KFunctionTypeEnum.镶嵌), //
	宝石合成(KFunctionTypeEnum.合成), //
	座驾进阶(KFunctionTypeEnum.机甲打造), //
	座驾培养(KFunctionTypeEnum.机甲升级), //
	完成竞技场PK(KFunctionTypeEnum.竞技场), //
	完成金币购买(KFunctionTypeEnum.金币购买), //
	完成许愿(KFunctionTypeEnum.许愿),//	
	神秘商店购买道具(KFunctionTypeEnum.神秘商人),//
	随从合成(KFunctionTypeEnum.随从合成),//
	清理僵尸(KFunctionTypeEnum.保卫庄园),//
	灌溉好友庄园((short)10001),//
	添加好友(KFunctionTypeEnum.好友),//
	军团捐献(KFunctionTypeEnum.军团),//
	队伍竞技(KFunctionTypeEnum.队伍竞技),//
	;//

	// 功能编号
	public final short functionId;
	
	private KUseFunctionTypeEnum(short funId) {
		this.functionId = funId;
	}

	private KUseFunctionTypeEnum(KFunctionTypeEnum funType) {
		this.functionId = funType.functionId;
	}

	// 所有枚举
	private static final Map<Short, KUseFunctionTypeEnum> enumMap = new HashMap<Short, KUseFunctionTypeEnum>();

	static {
		KUseFunctionTypeEnum[] enums = KUseFunctionTypeEnum.values();
		KUseFunctionTypeEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			enumMap.put(type.functionId, type);
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
	public static KUseFunctionTypeEnum getEnum(short functionId) {
		return enumMap.get(functionId);
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
	public static KUseFunctionTypeEnum getEnum(KFunctionTypeEnum functionType) {
		return enumMap.get(functionType.functionId);
	}
}
