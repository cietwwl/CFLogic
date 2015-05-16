package com.kola.kmp.logic.util.tips;

import com.koala.game.util.StringUtil;

/**
 * 
 * @author PERRY CHAN
 */
public class GlobalTips {

	private static String _tipsServerBusy;
	private static String _tipsDefaultTitle;
	private static String _tipsMaterialNotEnough;
	private static String _tipsMaterialNeedCount;
	private static String _tipsRoleNameLabel;
	private static String _tipsRoleIdLabel;
	private static String _tipsRoleLvLabel;

	public static String 确定 = "确定";
	public static String 取消 = "取消";
	public static String 在线 = "在线";

	public static String 服务器繁忙请稍候再试 = "服务器繁忙，请稍候再试";

	public static String 您的操作太频繁了请歇一歇 = "您的操作太频繁了，请歇一歇";
	public static String 角色不存在 = "角色不存在";
	public static String 角色不在线 = "角色不在线";

	// public static String 刚刚 = "刚刚";
	public static String 系统发送奖励 = "系统发送奖励：";
	
	public static String 此角色数据暂不能访问 = "此角色数据暂不能访问";
	public static String x功能未开放 = "{}功能未开放";
	public static String x功能将于x级开放 = "{}功能将于{}级开放";
	public static String 此角色功能未开放 = "此角色功能未开放";
	public static String 顿号 = "、";
	public static String 系统 = "系统";

	public static String getTipsServerBusy() {
		return _tipsServerBusy;
	}

	public static String getTipsDefaultTitle() {
		return _tipsDefaultTitle;
	}

	public static String getTipsMaterialNotEnough(String name, int quantity) {
		return StringUtil.format(_tipsMaterialNotEnough, name, quantity);
	}

	public static String getTipsRoleNameLabel() {
		return _tipsRoleNameLabel;
	}

	public static String getTipsRoleIdLabel() {
		return _tipsRoleIdLabel;
	}

	public static String getTipsRoleLvLabel() {
		return _tipsRoleLvLabel;
	}

	public static String getTipsMaterialNeedCount(String name, int count) {
		return StringUtil.format(_tipsMaterialNeedCount, name, count);
	}

}
