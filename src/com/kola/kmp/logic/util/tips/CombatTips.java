package com.kola.kmp.logic.util.tips;

import com.koala.game.util.StringUtil;

/**
 * 
 * @author PERRY CHAN
 */
public class CombatTips {

	private static String _tipsRoleIsOffline; // 角色不在线
	private static String _tipsRoleIsFighting; // 角色正在战斗中
	private static String _tipsTargetNotExists; // 目标不存在
	
	public static String getTipsRoleIsOffline(String roleName) {
		return StringUtil.format(_tipsRoleIsOffline, roleName);
	}
	
	public static String getTipsRoleIsFighting(String roleName) {
		return StringUtil.format(_tipsRoleIsFighting, roleName);
	}

	public static String getTipsTargetNotExists() {
		return _tipsTargetNotExists;
	}
}
