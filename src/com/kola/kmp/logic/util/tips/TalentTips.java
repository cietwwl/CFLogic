package com.kola.kmp.logic.util.tips;

import com.koala.game.util.StringUtil;

/**
 * 
 * @author PERRY CHAN
 */
public class TalentTips {

	private static String _tipsTreeNotOpenByLv; // 天赋树未开放
	private static String _tipsPreTalentNotEnable; // 前置天赋未激活
	private static String _tipsTalentRequireLvNotMatch; // 天赋需求等级不符合
	private static String _tipsTalentTreeNotOpen; // 天赋树未开放
	private static String _tipsTalentLvIsMax; // 天赋已经达到最高等级
	private static String _tipsTalentLvUpSuccess; // 天赋点升级成功
	private static String _tipsTalentTreeAlreadyActivate; // 天赋树已经激活了
	private static String _tipsTalentTreeIsActivate; // 天赋树被激活了
	private static String _tipsPreTalentMustBeMaxLv; // 前置天赋树必须先满级
	private static String _tipsFlowTalentLevelUp; // 天赋升级的流水描述
	private static String _tipsFlowTalentTreeActivate; // 天赋激活的流水描述
	private static String _tipsFlowTalentTreeOpen; // 天赋树开启的流水描述
	
	/**
	 * 
	 * @return
	 */
	public static String getTipsTreeNotOpenByLv(int lv) {
		return StringUtil.format(_tipsTreeNotOpenByLv, lv);
	}
	/**
	 * 
	 * @return
	 */
	public static String getTipsPreTalentNotEnable() {
		return _tipsPreTalentNotEnable;
	}
	/**
	 * 
	 * @return
	 */
	public static String getTipsTalentRequireLvNotMatch(int lv) {
		return StringUtil.format(_tipsTalentRequireLvNotMatch, lv);
	}
	/**
	 * 
	 * @return
	 */
	public static String getTipsTalentTreeNotOpen() {
		return _tipsTalentTreeNotOpen;
	}
	/**
	 * 
	 * @return
	 */
	public static String getTipsTalentLvIsMax() {
		return _tipsTalentLvIsMax;
	}
	/**
	 * 
	 * @return
	 */
	public static String getTipsTalentLvUpSuccess() {
		return _tipsTalentLvUpSuccess;
	}
	/**
	 * 
	 * @return
	 */
	public static String getTipsTalentTreeAlreadyActivate() {
		return _tipsTalentTreeAlreadyActivate;
	}
	/**
	 * 
	 * @return
	 */
	public static String getTipsTalentTreeIsActivate(String name) {
		return StringUtil.format(_tipsTalentTreeIsActivate, name);
	}
	/**
	 * 
	 * @param roleId
	 * @param talentName
	 * @param lv
	 * @return
	 */
	public static String getTipsFlowTalentLevelUp(String treeName, String talentName, int lv) {
		return StringUtil.format(_tipsFlowTalentLevelUp, treeName, talentName, lv);
	}
	/**
	 * 
	 * @param roleId
	 * @param treeName
	 * @return
	 */
	public static String getTipsFlowTalentTreeActivate(String treeName) {
		return StringUtil.format(_tipsFlowTalentTreeActivate, treeName);
	}

	/**
	 * 
	 * @param roleId
	 * @param treeName
	 * @return
	 */
	public static String getTipsFlowTalentTreeOpen(String treeName) {
		return StringUtil.format(_tipsFlowTalentTreeOpen, treeName);
	}
	
	/**
	 * 
	 * @return
	 */
	public static String getTipsPreTalentMustBeMaxLv() {
		return _tipsPreTalentMustBeMaxLv;
	}
	
}
