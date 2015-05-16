package com.kola.kmp.logic.util.tips;

import com.koala.game.util.StringUtil;
import com.kola.kmp.logic.other.KColorFunEnum;
import com.kola.kmp.logic.other.KColorFunEnum.KColorManager;
import com.kola.kmp.logic.role.KRoleModuleConfig;

/**
 * 
 * @author PERRY CHAN
 */
public class RoleTips {

	private static String _tipsRoleNameLengthNotPass;
	private static String _tipsRoleNameDuplicate;
	private static String _tipsRoleNameContainsDirtyWord;
	private static String _tipsRoleCountLimited;
	private static String _tipsCreateRoleFail;
	private static String _tipsDeleteRoleFail;
	private static String _tipsRoleLvUp;
	private static String _tipsNoSuchRole;
	private static String _tipsFlowRoleLevelUp;
	private static String _tipsFlowPhyPowerRecovery;
	private static String _tipsFlowOfflineIncreasPhyPower;
	private static String _tipsFlowOnlineIncreasPhyPower;
	private static String _tipsFlowDecPhyPower;
	//对【未曾染色的原始角色名】进行染色+下线划+菜单项处理:[url=a4:角色名,角色ID,menuId,menuId,menuId][u][ffffff]文本[-][/u][/url]
	private static String _RoleNameWithMenuFormat="[url=a4:{},{},1,2,3,4][u][{}]{}[-][/u][/url]";//url,RGP,文本
	
	public static void onGameWorldInitComplete() {
		_tipsRoleNameLengthNotPass = StringUtil.format(_tipsRoleNameLengthNotPass, KRoleModuleConfig.getRoleNameLengthMin(), KRoleModuleConfig.getRoleNameLengthMax());
		//
		_RoleNameWithMenuFormat=StringUtil.format(_RoleNameWithMenuFormat, "{}", "{}", KColorManager.getColor(KColorFunEnum.角色名.sign).color, "{}");
	}
	
	public static String getRoleNameWithMenuFormat() {
		return _RoleNameWithMenuFormat;
	}
	
	public static String getTipsRoleNameLengthNotPass() {
		return _tipsRoleNameLengthNotPass;
	}
	
	public static String getTipsRoleNameDuplicate() {
		return _tipsRoleNameDuplicate;
	}
	
	public static String getTipsRoleNameContainsDirtyWord() {
		return _tipsRoleNameContainsDirtyWord;
	}
	
	public static String getTipsRoleCountLimited() {
		return _tipsRoleCountLimited;
	}

	public static String getTipsCreateRoleFail(int errCode) {
		return StringUtil.format(_tipsCreateRoleFail, errCode);
	}

	public static String getTipsDeleteRoleFail(int errCode) {
		return StringUtil.format(_tipsDeleteRoleFail, errCode);
	}
	
	public static String getTipsRoleLvUp(int lv) {
		return StringUtil.format(_tipsRoleLvUp, lv);
	}

	public static String getTipsNoSuchRole() {
		return _tipsNoSuchRole;
	}

	public static String getTipsFlowRoleLevelUp(int preLv, int nowLv) {
		return StringUtil.format(_tipsFlowRoleLevelUp, preLv, nowLv);
	}

	public static String getTipsFlowPhyPowerRecovery(int value, String reason) {
		return StringUtil.format(_tipsFlowPhyPowerRecovery, value, reason);
	}

	public static String getTipsFlowOfflineIncreasPhyPower(long lastLeaveTimeMillis, long currentTimeMillis) {
		return StringUtil.format(_tipsFlowOfflineIncreasPhyPower, lastLeaveTimeMillis, currentTimeMillis);
	}
	
	public static String getTipsFlowOnlineIncreasPhyPower() {
		return _tipsFlowOnlineIncreasPhyPower;
	}

	public static String getTipsFlowDecPhyPower(int count, String reason) {
		return StringUtil.format(_tipsFlowDecPhyPower, count, reason);
	}
}
