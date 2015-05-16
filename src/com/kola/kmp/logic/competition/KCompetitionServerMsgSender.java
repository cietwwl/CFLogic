package com.kola.kmp.logic.competition;

import java.util.List;
import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.combat.ICombatCommonResult;
import com.kola.kmp.logic.competition.KCompetitionManager.CompetitionRewardShowData;
import com.kola.kmp.logic.competition.KCompetitor.KCompetitionBattleHistory;
import com.kola.kmp.logic.competition.KCompetitor.KCompetitionReward;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.competition.KCompetitionProtocol;

/**
 * 
 * @author PERRY CHAN
 */
public class KCompetitionServerMsgSender {

	public static void sendUpdateCompetitionList(KCompetitor roleC,
			List<KCompetitor> list, int preRanking) {

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(
				roleC.getRoleId());
		if (role != null) {
			KGameMessage msg = KGame
					.newLogicMessage(KCompetitionProtocol.SM_UPDATE_COMPETITION_LIST);
			msg.writeByte(list.size());
			for (int i = 0; i < list.size(); i++) {
				list.get(i).packInfo(msg);
			}
			role.sendMsg(msg);
		}
	}

	public static void sendUpdateRank(KCompetitor competitor, boolean isUpdate,
			KCompetitionReward result) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(
				competitor.getRoleId());
		if (role != null) {

			if (isUpdate) {
				KGameMessage msg = KGame
						.newLogicMessage(KCompetitionProtocol.SM_UPDATE_COMPETITION_REWARD);
				// 今日奖励
				msg.writeBoolean(true);
				result.getReward().packRewardMsg(msg);
				msg.writeBoolean(result.isHadReceivedReward());

				msg.writeBoolean(false);
				msg.writeBoolean(false);
				msg.writeBoolean(false);
				role.sendMsg(msg);
			}
		}
	}

	public static void sendUpdateCanChallengeTimes(KCompetitor competitor) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(
				competitor.getRoleId());
		if (role != null) {
			KGameMessage msg = KGame
					.newLogicMessage(KCompetitionProtocol.SM_UPDATE_COMPETITION_RANK_COUNT);
			msg.writeInt(competitor.getRanking());
			msg.writeShort(competitor.getCanChallengeTimes());
			msg.writeShort(KCompetitionModuleConfig.getMaxChallengeTimePerDay());
			boolean isCdTime = competitor.isCdTime();
			msg.writeBoolean(isCdTime);
			if (isCdTime) {
				int restTimeSeconds = competitor.getRestCDTimeSeconds();
				msg.writeInt(restTimeSeconds);
				msg.writeInt(KCompetitionManager.clearCDTimePerMin);
			}
			role.sendMsg(msg);
		}
	}

	public static void sendNewHistory(KCompetitor competitor) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(
				competitor.getRoleId());
		if (role != null) {
			KCompetitionBattleHistory history = competitor.getRecentlyHistory();
			if (history != null) {
				KGameMessage msg = KGame
						.newLogicMessage(KCompetitionProtocol.SM_ADD_CHALLENGE_HISTORY);
				msg.writeBoolean(history.isWin);
				msg.writeUtf8String(history.tips);
				role.sendMsg(msg);
			}
		}
	}

	public static void sendUpdateRewardWhileLvUp(KCompetitor competitor) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(
				competitor.getRoleId());
		if (role != null) {
			KGameMessage msg = KGame
					.newLogicMessage(KCompetitionProtocol.SM_UPDATE_COMPETITION_REWARD);
			// 今日奖励
			msg.writeBoolean(true);
			CompetitionRewardShowData todayReward = competitor
					.getTodayCompetitionReward().getReward();
			todayReward.packRewardMsg(msg);
			msg.writeBoolean(competitor.getTodayCompetitionReward()
					.isHadReceivedReward());

			msg.writeBoolean(false);

			// 今日排名奖励信息表奖励条数
			msg.writeBoolean(true);
			List<CompetitionRewardShowData> todayList = KCompetitionManager.todayCompetitionRewardShowDataMap
					.get(role.getLevel());
			if (todayList == null) {
				todayList = KCompetitionManager.todayCompetitionRewardShowDataMap
						.get(1);
			}
			msg.writeByte(todayList.size());
			for (CompetitionRewardShowData data : todayList) {
				data.packInfoMsg(msg);
			}

			// // 上周排名奖励信息表奖励条数
			// msg.writeBoolean(true);
			// List<CompetitionRewardShowData> weekList =
			// KCompetitionManager.weekCompetitionRewardShowDataMap
			// .get(role.getLevel());
			// if (weekList == null) {
			// weekList = KCompetitionManager.weekCompetitionRewardShowDataMap
			// .get(1);
			// }
			// msg.writeByte(weekList.size());
			// for (CompetitionRewardShowData data : weekList) {
			// data.packInfoMsg(msg);
			// }

			role.sendMsg(msg);
		}
	}

	public static void sendBattleResult(KRole role, ICombatCommonResult result,
			int honor, int newRanking) {
		KGameMessage msg = KGame
				.newLogicMessage(KCompetitionProtocol.SM_SHOW_COMPLETE_COMPETITION_BATTLE_RESULT);
		msg.writeBoolean(result.isWin());
		msg.writeInt((int) (result.getCombatTime() / 1000));
		msg.writeInt(honor);
		if (result.isWin()) {
			msg.writeInt(newRanking);
		}
		role.sendMsg(msg);
	}

	public static void sendUpdateRewardWhileGet(KRole role,
			CompetitionRewardShowData rwardData, boolean isToday, boolean isGet) {
		KGameMessage msg = KGame
				.newLogicMessage(KCompetitionProtocol.SM_UPDATE_COMPETITION_REWARD);

		if (isToday) {
			// 若果是今日奖励
			msg.writeBoolean(true);
			rwardData.packRewardMsg(msg);
			msg.writeBoolean(isGet);

			msg.writeBoolean(false);
		} else {
			// 若果是本周奖励
			msg.writeBoolean(false);

			msg.writeBoolean(true);
			rwardData.packRewardMsg(msg);
			msg.writeBoolean(isGet);
		}

		// 今日排名奖励信息表奖励条数
		msg.writeBoolean(false);

		// 上周排名奖励信息表奖励条数
		msg.writeBoolean(false);

		role.sendMsg(msg);
	}
}
