package com.kola.kmp.logic.competition.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPTeam;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPManager;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.tips.CompetitionTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.competition.KCompetitionProtocol;

/**
 * 
 * @author PERRY CHAN
 */
public class KRequestGetTeamPVPDailyRewardMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KRequestGetTeamPVPDailyRewardMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return KCompetitionProtocol.CM_REQUEST_GET_REWARD;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(msgEvent.getPlayerSession());
		String tips;
		boolean success = false;
		if (role != null) {
			KTeamPVPTeam team = KTeamPVPManager.getTeamByRoleId(role.getId());
			if (team != null) {
				CommonResult result = team.processGetDailyReward(role);
				success = result.isSucess;
				tips = result.tips;
			} else {
				tips = CompetitionTips.getTipsYouAreNotInTeam();
			}
		} else {
			tips = GlobalTips.getTipsServerBusy();
		}
		KGameMessage msg = KGame.newLogicMessage(KCompetitionProtocol.SM_RESPOND_GET_REWARD);
		msg.writeBoolean(success);
		msg.writeUtf8String(tips);
		msgEvent.getPlayerSession().send(msg);
	}

}
