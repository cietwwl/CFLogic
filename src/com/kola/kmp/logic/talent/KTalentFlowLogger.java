package com.kola.kmp.logic.talent;

import com.kola.kmp.logic.flow.FlowManager;
import com.kola.kmp.logic.flow.FlowManager.OtherFlowTypeEnum;
import com.kola.kmp.logic.util.tips.TalentTips;

/**
 * 
 * @author PERRY CHAN
 */
public class KTalentFlowLogger {

//	private static final Logger _LOGGER = KGameLogger.getLogger("talentLogger");
	
	public static void logTalentUpgrade(long roleId, String treeName, String talentName, int lv) {
//		_LOGGER.info(TalentTips.getTipsTalentLevelUpFlow(roleId, talentName, lv));
		FlowManager.logOther(roleId, OtherFlowTypeEnum.天赋, TalentTips.getTipsFlowTalentLevelUp(treeName, talentName, lv));
	}
	
	public static void logTalentActivate(long roleId, String treeName) {
//		_LOGGER.info(TalentTips.getTipsTalentTreeActivateFlow(roleId, treeName));
		FlowManager.logOther(roleId, OtherFlowTypeEnum.天赋, TalentTips.getTipsFlowTalentTreeActivate(treeName));
	}
	
	public static void logTalentTreeOpen(long roleId, String treeName) {
//		_LOGGER.info(TalentTips.getTipsTalentTreeOpenFlow(roleId, treeName));
		FlowManager.logOther(roleId, OtherFlowTypeEnum.天赋, TalentTips.getTipsFlowTalentTreeOpen(treeName));
	}
}
