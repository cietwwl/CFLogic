package com.kola.kmp.logic.competition.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPManager;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPMsgCenter;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPTeam;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPTeam.KTeamPVPTeamMember;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.CompetitionTips;
import com.kola.kmp.logic.util.tips.CurrencyTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.ShopTips;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;
import com.kola.kmp.protocol.competition.KCompetitionProtocol;

/**
 * 
 * @author PERRY CHAN
 */
public class KBuyTeamPVPChallengeTimeMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KBuyTeamPVPChallengeTimeMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return KCompetitionProtocol.CM_REQUEST_TEAM_PVP_ADD_CHALLENGE;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(msgEvent.getPlayerSession());
		String tips;
//		boolean full = false;
		if (role != null) {
			KTeamPVPTeam team = KTeamPVPManager.getTeamByRoleId(role.getId());
			if (team != null) {
				VIPLevelData vipData = KSupportFactory.getVIPModuleSupport().getVIPLevelData(role.getId());
				KTeamPVPTeamMember member = team.getMember(role.getId());
				int price = (member.getCurrentBuyTime() + 1) * 10; //  临时购买价钱
//				if (member.getCurrentBuyTime() < vipData.ladderbuyrmb.length) {
//					price = vipData.ladderbuyrmb[member.getCurrentBuyTime()];
//				} else {
//					price = vipData.ladderbuyrmb[vipData.ladderbuyrmb.length];
//				}
				{
					if (KSupportFactory.getCurrencySupport().getMoney(role.getId(), KCurrencyTypeEnum.DIAMOND) < price) {
						KDialogService.showChargeDialog(role.getId(), ShopTips.您的钻石不足是否前去充值);
						return;
					} else if (KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), KCurrencyTypeEnum.DIAMOND, price, UsePointFunctionTypeEnum.金币兑换, true) < 0) {
						KDialogService.showChargeDialog(role.getId(), ShopTips.您的钻石不足是否前去充值);
						return;
					} else {
						member.notifyBuyTime();
						tips = CompetitionTips.getTipsBuyChallengeTimeSuccess();
						KGameMessage msg = KTeamPVPMsgCenter.createSyncChallengeTimeMsg(member);
						msgEvent.getPlayerSession().send(msg);
					}
				} /*else {
					tips = CompetitionTips.getTipsCannotBuyAnyMore(vipData.lvl, vipData.ladderbuyrmb.length);
//					full = true;
				}*/
			} else {
				tips = CompetitionTips.getTipsYouAreNotInTeam();
			}
		} else {
			tips = GlobalTips.getTipsServerBusy();
		}
		if (tips.contains("\n")) {
			KDialogService.sendUprisingDialog(msgEvent.getPlayerSession(), tips.split("\n"));
		} else {
			KDialogService.sendUprisingDialog(msgEvent.getPlayerSession(), tips);
		}
	}
}
