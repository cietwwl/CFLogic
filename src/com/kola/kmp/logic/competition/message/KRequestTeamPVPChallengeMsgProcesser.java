package com.kola.kmp.logic.competition.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPTeam;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPManager;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KUseFunctionTypeEnum;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.CompetitionTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.competition.KCompetitionProtocol;

/**
 * 
 * @author PERRY CHAN
 */
public class KRequestTeamPVPChallengeMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KRequestTeamPVPChallengeMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return KCompetitionProtocol.CM_REQUEST_TEAM_PVP_CHALLENGE;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(msgEvent.getPlayerSession());
		String tips = null;
		KActionResult<Integer> result = null;
		boolean success = false;
		if (role != null) {
			KTeamPVPTeam team = KTeamPVPManager.getTeamByRoleId(role.getId());
			if (team != null) {
				result = team.processChallenge(role);
				success = result.success;
				tips = result.tips;
			} else {
				tips = CompetitionTips.getTipsYouAreNotInTeam();
			}
		} else {
			tips = GlobalTips.getTipsServerBusy();
		}
		KGameMessage msg = KGame.newLogicMessage(KCompetitionProtocol.SM_RESPOND_TEAM_PVP_CHALLENGE);
		msg.writeBoolean(success);
		if (!success) {
			msg.writeUtf8String(tips);
		}
		msgEvent.getPlayerSession().send(msg);
		if (success) {
			KSupportFactory.getMissionSupport().notifyUseFunction(role, KUseFunctionTypeEnum.队伍竞技);
			KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.天梯赛);
		}
	}

}
