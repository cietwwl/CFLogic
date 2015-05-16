package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * 服务器命令：服务器要求客户端执行相应操作
 * 一般用于客户端点击对话框按钮后，服务器响应时会发送此消息
 * 
 * @author CamusHuang
 * @creation 2014-3-11 下午3:13:05
 * </pre>
 */
public enum KNPCOrderEnum {
	NPC_ORDER_SHOP(1), // 打开商店界面"),
	ORDER_OPEN_VIPUI(KFunctionTypeEnum.VIP特权), // 打开vip界面"),
	ORDER_OPEN_VIP_CHARGE(1503), // 打开vip充值界面"),
	ORDER_OPEN_CHARGE(1504), // 打开充值界面"),
	ORDER_OPEN_EXCHANGE(KFunctionTypeEnum.金币购买), // 打开金币兑换界面"),
	ORDER_OPEN_SCENARIO_UI(KFunctionTypeEnum.关卡界面), // 打开关卡选择界面"),
	ORDER_OPEN_FIRST_CHARGE(1501), // 打开首充界面"),
	ORDER_OPEN_COMPETITION(KFunctionTypeEnum.竞技场),//打开竞技场界面
	ORDER_OPEN_ELITE_COPY(KFunctionTypeEnum.精英副本),//打开精英副本界面
	ORDER_OPEN_TECH_COPY(KFunctionTypeEnum.技术副本),//打开技术副本界面
	ORDER_OPEN_FRIEND_COPY(KFunctionTypeEnum.好友地下城),//打开好友副本界面
	ORDER_OPEN_PET_COPY(KFunctionTypeEnum.随从副本),//打开好友副本界面
	ORDER_OPEN_WISH(KFunctionTypeEnum.许愿),//打开许愿界面
	ORDER_OPEN_MISSION_LIST(KFunctionTypeEnum.任务),//打开任务界面
	ORDER_OPEN_DAILY_MISSION(KFunctionTypeEnum.日常任务),//打开日常任务界面
	ORDER_OPEN_REWARD_UI(KFunctionTypeEnum.每日幸运),//打开奖励界面
	ORDER_OPEN_STRONG_EQUI(KFunctionTypeEnum.强化),//打开装备改造界面
	ORDER_OPEN_STRONG_MOUNT(KFunctionTypeEnum.机甲打造),//打开机甲进阶界面
	ORDER_OPEN_STRONG_PET(KFunctionTypeEnum.随从),//打开宠物培养界面
	ORDER_OPEN_TALENT_UI(KFunctionTypeEnum.天赋),//打开天赋界面
	ORDER_OPEN_GEM_UI(KFunctionTypeEnum.镶嵌),//打开宝石镶嵌界面
	ORDER_OPEN_ACTIVE_DEGREE(KFunctionTypeEnum.活跃度),//打开活跃度界面
	ORDER_OPEN_ZOMBIE(KFunctionTypeEnum.保卫庄园),//打开驱赶僵尸界面
	ORDER_OPEN_ACTIVITY(KFunctionTypeEnum.活动列表),//打开活动列表界面
	ORDER_OPEN_NOTICE(KFunctionTypeEnum.公告),//打开公告界面
	ORDER_OPEN_RANK(KFunctionTypeEnum.排行榜),//打开排行榜界面
	ORDER_OPEN_GANG_REWWAR(KFunctionTypeEnum.资源争夺),//打开军团资源战界面
	ORDER_OPEN_TRANSPORT(KFunctionTypeEnum.运送物资),//打开物资运送活动界面
	ORDER_OPEN_GANG_WAR(KFunctionTypeEnum.军团战),//打开军团战界面
	ORDER_OPEN_ASSISTANT(1502),//打开小助手
	ORDER_OPEN_RANDOMSHOP(KFunctionTypeEnum.神秘商人),//打开小助手
	ORDER_OPEN_ACTIVE_SKILL(KFunctionTypeEnum.技能),
	ORDER_OPEN_PASSIVE_SKILL(KFunctionTypeEnum.被动技能),
	ORDER_OPEN_TEAM_PVP(KFunctionTypeEnum.队伍竞技),
	ORDER_OPEN_DAILY_LUCKY(KFunctionTypeEnum.每日幸运),
	ORDER_OPEN_LOGIN_REWARD(KFunctionTypeEnum.累计登录),
	ORDER_OPEN_TOWER_COPY(KFunctionTypeEnum.爬塔副本),
	ORDER_OPEN_WAKUANG_COPY(KFunctionTypeEnum.挖矿副本),
	ORDER_OPEN_PET_CHALLENGE_COPY(KFunctionTypeEnum.随从挑战),
	ORDER_OPEN_SENIOR_PET_CHALLENGE_COPY(KFunctionTypeEnum.高级随从试炼),
	ORDER_OPEN_COMMON_WISH(KFunctionTypeEnum.许愿),
	ORDER_OPEN_BAG_UI(KFunctionTypeEnum.背包),
	ORDER_OPEN_GANG_SHOP(KFunctionTypeEnum.军团),
	ORDER_OPEN_VITALITY(KFunctionTypeEnum.活动列表),
	ORDER_OPEN_LUCKY_DRAW(KFunctionTypeEnum.抽奖),
	ORDER_OPEN_EARN_GOLD(KFunctionTypeEnum.赚金币),
	ORDER_OPEN_WORLD_BOSS(KFunctionTypeEnum.丧尸攻城),
	;

	public final int sign; // 数据类型的标识

	private KNPCOrderEnum(int sign) {
		this.sign = sign;
	}
	
	private KNPCOrderEnum(KFunctionTypeEnum funType) {
		this.sign = funType.functionId;
	}

	// 所有枚举
	private static final Map<Integer, KNPCOrderEnum> enumMap = new HashMap<Integer, KNPCOrderEnum>();
	static {
		KNPCOrderEnum[] enums = KNPCOrderEnum.values();
		KNPCOrderEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			enumMap.put(type.sign, type);
		}
	}

	/**
	 * <pre>
	 * 通过标识数值获取类型枚举对象
	 * 
	 * @param functionId
	 * @return
	 * @author CamusHuang
	 * @creation 2012-11-28 上午11:41:04
	 * </pre>
	 */
	public static KNPCOrderEnum getEnum(int order) {
		if(order == 1032) {
			System.out.println();
		}
		return enumMap.get(order);
	}
	
	public static KNPCOrderEnum getEnum(KFunctionTypeEnum funType) {
		return enumMap.get((int)(funType.functionId));
	}
}
