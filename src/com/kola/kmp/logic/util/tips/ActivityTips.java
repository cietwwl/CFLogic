package com.kola.kmp.logic.util.tips;

import com.koala.game.util.StringUtil;

public class ActivityTips {
	private static String _tipsNotOpen;// 该功活动暂未开放
	private static String _tipsNotOpenByRoleLv;// 角色等级不足，活动未开放
	private static String _tipsNotOpenByNotGang;// 不是公会成员，活动未开放
	private static String _tipsNotOpenByGangLv;// 角色所在公会等级不足，活动未开放
	private static String _tipsActivityCdTime;// 进入等待CD时间

	public static String 更新成功 = "更新成功";
	public static String 不存在此矿区 = "不存在此矿区";
	public static String 开始挖矿 = "开始挖矿";
	public static String 请先停止挖矿 = "请先停止挖矿";
	public static String 取消挖矿成功 = "取消挖矿成功";
	public static String 该玩家已被驱赶出矿区 = "该玩家已被驱赶出矿区";
	public static String 该玩家正在被挑战 = "该玩家正在被挑战";
	public static String 战斗胜利矿区已满 = "战斗胜利矿区已满";
	public static String 驱赶成功缺少工具 = "驱赶成功缺少工具";
	public static String 驱赶成功开始挖矿 = "驱赶成功开始挖矿";
	public static String 缺少工具 = "缺少工具";
	public static String 驱赶失败 = "驱赶失败";
	public static String x在x矿驱赶了你是否复仇 = "{}在{}中一脚把你踢了下来，并大喊滚粗。"+'\n'+"是否前往复仇?";
	public static String 被驱赶 = "被驱赶!~";
	public static String 此玩家不处于挖矿状态中无法复仇 = "此玩家不处于挖矿状态中，无法复仇";
	public static String 复仇失败 = "复仇失败";
	public static String 复仇成功缺少工具 = "复仇成功缺少工具";
	public static String 复仇成功开始挖矿 = "复仇成功开始挖矿";
	public static String x时间由于背包已满停止挖矿 = "{}由于背包已满停止挖矿";
	public static String x时间由于工具用完停止挖矿 = "{}由于工具用完停止挖矿";
	public static String x时间挖矿获得了x剩余xxx = "{}挖矿获得了{}，剩余{}x{}";
	public static String x时间被x玩家驱动出x矿区 = "{}被{}驱动出{}";
	public static String 挖矿获得物品x = "！突然发现一颗闪光的东西，偷偷的打开一看，获得{}";
	public static String 挖矿获得货币x = "经过辛勤努力，挖矿获得：{}";
	public static String 离线挖矿结果邮件标题 = "离线挖矿结果";
	public static String 挖矿x时间消耗工具x获得了x = "挖矿{}，消耗{}，获得了{}";
	public static String 今天已挖矿x时间获得奖励x = "今天已挖矿{}"+'\n'+"获得奖励："+'\n'+"{}";
	public static String 此活动未开放 = "此活动未开放";

	public static String getTipsNotOpen() {
		return _tipsNotOpen;
	}

	public static String getTipsNotOpenByRoleLv(int openLv) {
		return StringUtil.format(_tipsNotOpenByRoleLv, openLv);
	}

	public static String getTipsNotOpenByNotGang() {
		return _tipsNotOpenByNotGang;
	}

	public static String getTipsNotOpenByGangLv(int openLv) {
		return StringUtil.format(_tipsNotOpenByGangLv, openLv);
	}

	public static String getTipsActivityCdTime(String actName) {
		return StringUtil.format(_tipsActivityCdTime, actName);
	}

}
