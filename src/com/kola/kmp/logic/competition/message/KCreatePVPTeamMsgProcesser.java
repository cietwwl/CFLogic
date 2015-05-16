package com.kola.kmp.logic.competition.message;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPConfig;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPManager;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPMsgCenter;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPTeam;
import com.kola.kmp.logic.item.message.KPushItemsMsg;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.CompetitionTips;
import com.kola.kmp.logic.util.tips.ShopTips;
import com.kola.kmp.protocol.competition.KCompetitionProtocol;

/**
 * 
 * @author PERRY CHAN
 */
public class KCreatePVPTeamMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KCreatePVPTeamMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return KCompetitionProtocol.CM_REQUEST_CREATE_TEAM;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		String teamName = msgEvent.getMessage().readUtf8String();
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(msgEvent.getPlayerSession());
		String tips = null;
		KTeamPVPTeam team = null;
		boolean createSuccess = false;
		int length = 0;
		if (KSupportFactory.getDirtyWordSupport().containDirtyWord(teamName) != null) {
			tips = CompetitionTips.getTipsTeamNameContainsDirtyWord();
		} else if (KTeamPVPManager.isNameExists(teamName)) {
			tips = CompetitionTips.getTipsDuplicateTeamName();
		} else if ((length = UtilTool.getStringLength(teamName)) < KTeamPVPConfig.getTeamNameLengthMin() || length > KTeamPVPConfig.getTeamNameLengthMax()) {
			tips = CompetitionTips.getTipsTeamLengthNotPassed();
		} else if (KTeamPVPManager.isInTeam(role.getId())) {
			tips = CompetitionTips.getTipsYouAreInTeam();
		} else if (KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), KCurrencyTypeEnum.GOLD, KTeamPVPConfig.getPriceForCreateTeam(), UsePointFunctionTypeEnum.战队竞技创建队伍, true) < 0) {
			KDialogService.showExchangeDialog(role.getId(), ShopTips.您的金币不足是否前去兑换,
					(KTeamPVPConfig.getPriceForCreateTeam() - KSupportFactory.getCurrencySupport().getMoney(role.getId(), KCurrencyTypeEnum.GOLD)));
			return;
		} else {
			KActionResult<KTeamPVPTeam> result = KTeamPVPManager.createCompetitionTeam(teamName, role);
			if(result.success) {
				createSuccess = true;
				team = result.attachment;
				tips = CompetitionTips.getTipsCreateTeamSuccess();
			} else {
				KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD, KTeamPVPConfig.getPriceForCreateTeam()), PresentPointTypeEnum.回滚, true);
				tips = result.tips;
			}
		}
//		int challengeCount = KCompetitionTeamPVPConfig.getChallengeCountPerDay();
//		KGameMessage msg = KGame.newLogicMessage(KCompetitionProtocol.SM_RESPOND_CREATE_TEAM);
//		msg.writeBoolean(createSuccess);
//		msg.writeUtf8String(tips);
//		if (createSuccess) {
//			msg.writeInt(team.getTeamBattlePower());
//			msg.writeInt(team.getDanData().iconId);
//			msg.writeUtf8String(team.getDanRankInfo().danRankName);
//			msg.writeByte(challengeCount);
//			msg.writeByte(challengeCount);
//		}
//		msgEvent.getPlayerSession().send(msg);
		KDialogService.sendUprisingDialog(msgEvent.getPlayerSession(), tips);
		KGameMessage msg = KTeamPVPMsgCenter.createTeamInfoMessage(team, role.getId());
		msgEvent.getPlayerSession().send(msg);
		if(createSuccess) {
			KSupportFactory.getTeamPVPSupport().notifyRoleTeamDataChange(role);
		}
	}

}
