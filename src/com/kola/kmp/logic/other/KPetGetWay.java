package com.kola.kmp.logic.other;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * 随从获取方式
 * 
 * @author PERRY
 *
 */
public enum KPetGetWay {

	WAY_COMMON_COPY(1, KNPCOrderEnum.ORDER_OPEN_SCENARIO_UI, "普通副本"),
	WAY_ELITE_COPY(2, KNPCOrderEnum.ORDER_OPEN_ELITE_COPY, "精英副本"),
	WAY_PET_CHALLENGE_COPY(3, KNPCOrderEnum.ORDER_OPEN_PET_CHALLENGE_COPY, "随从试炼"),
	WAY_GANG_SHOP(4, KNPCOrderEnum.ORDER_OPEN_GANG_SHOP, "军团商店"),
	WAY_RANDOM_SHOP(5, KNPCOrderEnum.ORDER_OPEN_RANDOMSHOP, "随机商店"),
	WAY_VITALITY(6, KNPCOrderEnum.ORDER_OPEN_VITALITY, "活跃度"),
	WAY_GARDERN(7, KNPCOrderEnum.ORDER_OPEN_ZOMBIE, "保卫庄园"),
	WAY_COMMON_WISH(8, KNPCOrderEnum.ORDER_OPEN_LUCKY_DRAW, "普通许愿"),
	WAY_SENIOR_WISH(9, KNPCOrderEnum.ORDER_OPEN_LUCKY_DRAW, "高级许愿"),
	WAY_VIP(10, KNPCOrderEnum.ORDER_OPEN_VIPUI, "VIP"),
	WAY_SENIOR_PET_CHALLENGE_COPY(11, KNPCOrderEnum.ORDER_OPEN_SENIOR_PET_CHALLENGE_COPY, "高级随从试炼"),
	;
	
	private static final Map<Integer, KPetGetWay> _MAP;
	static {
		Map<Integer, KPetGetWay> map = new HashMap<Integer, KPetGetWay>();
		for (int i = 0; i < values().length; i++) {
			KPetGetWay temp = values()[i];
			temp = map.put(temp.sign, temp);
		}
		_MAP = Collections.unmodifiableMap(map);
	}
	
	public final int sign;
	public final KNPCOrderEnum npcOrderEnum;
	public String name;
	
	private KPetGetWay(int pSign, KNPCOrderEnum pNpcOrderEnum, String pName) {
		this.sign = pSign;
		this.npcOrderEnum = pNpcOrderEnum;
		this.name = pName;
	}
	
	public static KPetGetWay getEnum(int flag) {
		return _MAP.get(flag);
	}
}
