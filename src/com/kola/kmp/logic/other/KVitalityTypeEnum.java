package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * 活跃度功能类型
 * 
 * @author CamusHuang
 * @creation 2013-1-14 上午10:37:36
 * </pre>
 */
public enum KVitalityTypeEnum {
	强化装备		(	1, "强化装备"),
	通关普通副本		(	2, "通关普通副本"),
	通关普通精英副本		(	3, "通关普通精英副本"),
	通关困难精英副本		(	4, "通关困难精英副本"),
	名人堂膜拜		(	5, "名人堂膜拜"),
	排行榜点赞		(	6, "排行榜点赞"),
	竞技场切磋		(	7, "竞技场切磋"),
	
	随从合成		(	8, "随从合成"),
	收获庄园植物		(	9, "收获庄园植物"),
	清理庄园僵尸		(	10, "清理庄园僵尸"),
	好友地下城		(	11, "好友地下城"),
	运送物资		(	12, "运送物资"),
	抢夺他人物资		(	13, "抢夺他人物资"),
	参加丧尸攻城活动		(	14, "参加丧尸攻城活动"),
	浇灌庄园植物		(	15, "浇灌庄园植物"),
	随从营救		(	16, "随从营救", false),
	随从试炼		(	17, "随从试炼"),
	异能要塞		(	18, "异能要塞"),
	挖矿		(	19, "挖矿"),
	天梯赛		(	20, "天梯赛"),
	赚金币		(	21, "赚金币"),
	切磋		(	22, "切磋"),
	机甲升级	(   23, "机甲升级"),
 ;
	// 标识数值
	public final int sign;
	// 名称
	public final String name;
	public final boolean isCheckLoad;

	private KVitalityTypeEnum(int sign, String name) {
		this.sign = sign;
		this.name = name;
		this.isCheckLoad = true;
	}
	
	private KVitalityTypeEnum(int sign, String name, boolean isCheckLoad) {
		this.sign = sign;
		this.name = name;
		this.isCheckLoad = isCheckLoad;
	}

	// 所有枚举
	private static final Map<Integer, KVitalityTypeEnum> TypeMap = new HashMap<Integer, KVitalityTypeEnum>();
	static {
		KVitalityTypeEnum[] enums = KVitalityTypeEnum.values();
		KVitalityTypeEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			TypeMap.put(type.sign, type);
		}
	}

	// //////////////////
	/**
	 * <pre>
	 * 通过标识数值获取枚举对象
	 * 
	 * @param sign
	 * @return
	 * @author camus
	 * @creation 2012-12-30 下午11:56:06
	 * </pre>
	 */
	public static KVitalityTypeEnum getEnum(int sign) {
		return TypeMap.get(sign);
	}
}
