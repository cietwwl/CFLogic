package com.kola.kmp.logic.util.tips;

import com.koala.game.util.StringUtil;

/**
 * 
 * @author PERRY CHAN
 */
public class FlowTips {

	private static String _tipsMissionReward;
	private static String _tipsLevelReward;
	private static String _tipsTeamPVPReward;
	private static String _tipsVisitHeroHall;
	private static String _tipsBlockReward;
	private static String _tipsGmAction;
	private static String _tipsCompetitionReward;
	private static String _tipsTransportReward;
	private static String _tipsInterceptReward;

	private static String _tipsJYCopyReward;
	private static String _tipsJSCopyReward;
	private static String _tipsFriendCopyReward;
	private static String _tipsPetCopyReward;
	
	public static String getTipsMissionReward(int missionId) {
		return StringUtil.format(_tipsMissionReward, missionId);
	}
	
	public static String getTipsLevelReward(int levelId) {
		return StringUtil.format(_tipsLevelReward, levelId);
	}
	
	public static String getTipsTeamPVPReward(int danStageId) {
		return StringUtil.format(_tipsTeamPVPReward, danStageId);
	}
	
	public static String getTipsVisitHeroHall() {
		return _tipsVisitHeroHall;
	}
	
	public static String getTipsBlockReward() {
		return _tipsBlockReward;
	}
	
	public static String getTipsGmAction() {
		return _tipsGmAction;
	}

	public static String getTipsCompetitionReward() {
		return _tipsCompetitionReward;
	}

	public static String getTipsTransportReward(int carrierId) {
		return StringUtil.format(_tipsTransportReward, carrierId);
	}

	public static String getTipsInterceptReward(int carrierId) {
		return StringUtil.format(_tipsInterceptReward, carrierId);
	}

	public static String getTipsJYCopyReward(int levelId) {
		return StringUtil.format(_tipsJYCopyReward, levelId);
	}

	public static String getTipsJSCopyReward(int levelId) {
		return StringUtil.format(_tipsJSCopyReward, levelId);
	}

	public static String getTipsFriendCopyReward(int levelId) {
		return StringUtil.format(_tipsFriendCopyReward, levelId);
	}

	public static String getTipsPetCopyReward(int levelId) {
		return StringUtil.format(_tipsPetCopyReward, levelId);
	}
	
	
}
