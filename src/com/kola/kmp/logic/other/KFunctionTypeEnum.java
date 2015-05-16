package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;

public enum KFunctionTypeEnum {

	技能(101), //
	被动技能(102), //
	任务(201), //
	背包(301), //
	时装(302), //
	强化(401), //
	升星(402), //
	继承(403), //
	镶嵌(404), //
	合成(405), //
	军团(501), //
	随从(601), //
	随从合成(602), //
	天赋(701), //
	机甲(801), //
	机甲打造(802), //
	机甲升级(803), //
	机甲进化(804), //
	日常任务(901), //
	活动列表(1001), //
	神秘商人(1011), //
	好友(1021), //
	每日幸运(1031), //
	累计登录(1032), //
	保卫庄园(1033), //
	活跃度(1034), //
	在线奖励(1035), //
	七日登录奖励(1036), //
	聊天(1041), //
	邮件(1051), //
	排行榜(1061), //
	系统设置(1071), //
	竞技场(1081), //
	名人堂(1082), //
	队伍竞技(1083), //
	精英副本(1091), //
	技术副本(1101), //
	好友地下城(1111), //
	随从副本(1112), //
	丧尸攻城(1121), //
	运送物资(1131), //
	军团战(1141), //
	资源争夺(1151), //
	赚金币(1155), //
	金币购买(1161), //
	许愿(1171), //
	欢乐送(1172), //
	关卡界面(1181), //
	关卡信息(1182), //
	关卡扫荡(1183), //
	VIP特权(1191), //
	公告(1201), //
	挖矿副本(1211), //
	爬塔副本(1221), //
	随从挑战(1231), //
	高级随从试炼(1232), //
	抽奖(1241), //
	全民竞猜(1251),//
	VIP充值(1503), //
	充值(1504), //
	Bug提交(1505),//
	VIP每日礼包(1506),//
	限时热购(1507),//
	;

	// 功能编号
	public final short functionId;

	private KFunctionTypeEnum(int functionId) {
		this.functionId = (short) functionId;
	}

	// 所有枚举
	private static final Map<Short, KFunctionTypeEnum> enumMap = new HashMap<Short, KFunctionTypeEnum>();

	static {
		KFunctionTypeEnum[] enums = KFunctionTypeEnum.values();
		KFunctionTypeEnum type;
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
	public static KFunctionTypeEnum getEnum(short functionId) {
		return enumMap.get(functionId);
	}

}
