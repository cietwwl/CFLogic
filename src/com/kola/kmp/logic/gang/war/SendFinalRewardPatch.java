package com.kola.kmp.logic.gang.war;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.gang.GangIntegrateData;
import com.kola.kmp.logic.RunTimeTask;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.gang.KGangExtCASet;
import com.kola.kmp.logic.gang.KGangLogic;
import com.kola.kmp.logic.gang.KGangModuleExtension;
import com.kola.kmp.logic.gang.war.GangWarRound.GangRace;
import com.kola.kmp.logic.gang.war.GangWarRound.GangRace.RaceGangData;
import com.kola.kmp.logic.gang.war.KGangWarDataManager.KGangMedalDataManager.GangMedalData;
import com.kola.kmp.logic.gang.war.KGangWarDataManager.KGangWarRewardDataManager.WarRankRewardData;
import com.kola.kmp.logic.gang.war.KGangWarDataManager.KGangWarRewardRatioDataManager.RewardRatioData;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.GangWarTips;
import com.kola.kmp.logic.util.tips.RewardTips;

/**
 * <pre>
 * 中场停服，重新恢复数据有BOG。此为补丁。
 * 例如第3场 和第4场之间重启，恢复数据失败时，使用此补丁进行补发发奖。
 * 
 * @author CamusHuang
 * @creation 2013-7-24 上午2:58:19
 * </pre>
 */
public class SendFinalRewardPatch implements RunTimeTask {

	public String run(String params) {

		try {

			boolean isTest = params.equalsIgnoreCase("test");

			GangData NO1 = null;
			GangData NO2 = null;
			GangData NO3_1 = null;
			GangData NO3_2 = null;

			{
				int roundId = KGangWarConfig.getInstance().MaxRound - 1;
				GangWarRound round = GangWarDataCenter.getRoundData(roundId);
				List<GangData> lastWinGangList = round.searchAllWinGangDatas();

				NO1 = lastWinGangList.get(0);
				NO2 = lastWinGangList.get(1);

				// 发第4场奖励
//				sendRoundRewardToAllMems(KGangWarConfig.getInstance().MaxRound, NO1.gangId, true, isTest);
//				sendRoundRewardToAllMems(KGangWarConfig.getInstance().MaxRound, NO2.gangId, true, isTest);
			}

			{
				int roundId = KGangWarConfig.getInstance().MaxRound - 1;
				GangWarRound round = GangWarDataCenter.getRoundData(roundId);
				List<GangData> lastLoseGangList = searchAllLoseGangDatas(round);

				NO3_1 = lastLoseGangList.get(0);
				NO3_2 = lastLoseGangList.get(1);
			}

			// 发送终极奖励
			{
				sendFinallyReward(NO1, 1, isTest);
				sendFinallyReward(NO2, 1, isTest);
				sendFinallyReward(NO3_1, 3, isTest);
				sendFinallyReward(NO3_2, 3, isTest);
			}
			return "执行完毕";
		} catch (Exception e) {
			e.printStackTrace();
			return "发生异常=" + e.getMessage();
		}
	}

	static List<GangData> searchAllLoseGangDatas(GangWarRound round) {
		List<GangData> list = new ArrayList<GangData>();
		for (GangRace race : round.getUnmodifyRaceList()) {
			RaceGangData data = race.getLoser();
			if (data != null) {
				list.add(data.gangData);
			}
		}
		return list;
	}

	private static void sendFinallyReward(GangData tempWarGang, int rank, boolean isTest) {
		if (tempWarGang == null || tempWarGang.gangId < 0) {
			return;
		}
		//
		GangMedalData medal = KGangWarDataManager.mGangMedalDataManager.getDataByRank(rank);
		if (!isTest) {
			KGang gang = KGangLogic.sendGangWarFinalReward(tempWarGang.gangId, medal);
			if (gang == null) {
				GangWarLogic.GangWarLogger.error("警告：军团战排行{}，军团ID={}，军团名称={}，发送最终奖励时找不到此军团！", GangWarLogic.getTitle(rank), tempWarGang.gangId, tempWarGang.gangName);
			}
		}
	}

	/**
	 * <pre>
	 * 发放单场军团和成员奖励
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-29 下午6:51:13
	 * </pre>
	 */
	static void sendRoundRewardToAllMems(int round, long gangId, boolean isWin, boolean isTest) {
		GangIntegrateData gangs = KGangModuleExtension.getGangAndSet(gangId);
		if (gangs == null) {
			return;
		}
		KGang gang = (KGang) gangs.getGang();
		KGangExtCASet gangExtSet = (KGangExtCASet) gangs.getGangExtCASet();
		//
		WarRankRewardData reward = KGangWarDataManager.mGangWarRewardDataManager.getData(round, isWin);
		RewardRatioData ratio = KGangWarDataManager.mGangWarRewardRatioDataManager.getData(gang.getLevel());
		{// 发放军团奖励
			int addExp = (int) (reward.exp * ratio.coefficient);
			int addResource = (int) (reward.GangMoney * ratio.coefficient);
			if (!isTest) {
				KSupportFactory.getGangSupport().addGangExp(gangId, addExp, addResource);
			}
			// 军团日志
			String Q = GangWarLogic.getQiangFromRound(round, isWin);
			String dialy = null;
			if (isWin) {
				dialy = StringUtil.format(GangWarTips.军团战第x场成功晋级x军团获得奖励x经验和x资金, round, Q, addExp, addResource);
			} else {
				dialy = StringUtil.format(GangWarTips.军团战第x场止步于x军团获得奖励x经验和x资金, round, Q, addExp, addResource);
			}
			if (!isTest) {
				KGangLogic.addDialy(gang, gangExtSet, dialy, true, true, false);
			}
		}
		{// 发放成员奖励
			for (Long roleId : gang.getAllElementRoleIds()) {
				KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
				if (role == null) {
					GangWarLogic.GangWarLogger.error("警告：军团战第{}场，军团ID={}，军团名称={}，是否胜出={}，成员角色ID={}，成员积分={}，发送奖励时找不到此军团！", round, gangId, gang.getName(), isWin, roleId, 0);
					continue;
				}

				GangWarLogic.GangWarLogger.warn("单场奖励：军团战第{}场，军团ID={}，军团名称={}，是否胜出={}，成员角色ID={}，成员积分={}", round, gangId, gang.getName(), isWin, roleId, 0);

				// 发出邮件
				if (!isTest) {
					KSupportFactory.getMailModuleSupport().sendMoneyMailBySystem(role.getId(), reward.getAddMoneys(ratio.coefficient), PresentPointTypeEnum.军团战, reward.baseMailContent.getMailTitle(),
							reward.baseMailContent.getMailContent());
					// 浮动提示通知
					KDialogService.sendUprisingDialog(role, RewardTips.系统奖励已发送请查看邮件);
				}
			}
		}
	}
}
