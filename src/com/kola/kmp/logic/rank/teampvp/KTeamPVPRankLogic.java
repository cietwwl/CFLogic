package com.kola.kmp.logic.rank.teampvp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kmp.logic.rank.KRankDataManager;
import com.kola.kmp.logic.rank.KRankDataStructs.KRankGoodPrice;
import com.kola.kmp.logic.rank.KRankLogic;
import com.kola.kmp.logic.rank.KRankRoleExtCACreator;
import com.kola.kmp.logic.rank.KRoleRankData;
import com.kola.kmp.logic.rank.abs.ElementAbs;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult_Ext;
import com.kola.kmp.logic.util.ResultStructs.RankResult_GoodJob;
import com.kola.kmp.logic.util.tips.RankTips;
import com.kola.kmp.logic.util.tips.ShopTips;

/**
 * <pre>
 * 军团相关的排行榜
 * 
 * @author CamusHuang
 * @creation 2013-8-12 下午5:47:37
 * </pre>
 */
public class KTeamPVPRankLogic {

	private static final Logger _LOGGER = KGameLogger.getLogger(KTeamPVPRankLogic.class);

	private KTeamPVPRankLogic() {
	}

	// 所有排行榜
	private static final List<TeamPVPRank<TeamPVPRankElement>> AllRankList = new ArrayList<TeamPVPRank<TeamPVPRankElement>>();
	private static final Map<KTeamPVPRankTypeEnum, TeamPVPRank<TeamPVPRankElement>> AllRankMap = new HashMap<KTeamPVPRankTypeEnum, TeamPVPRank<TeamPVPRankElement>>();
	static {
		for (KTeamPVPRankTypeEnum enuma : KTeamPVPRankTypeEnum.values()) {
			TeamPVPRank<TeamPVPRankElement> rank = new TeamPVPRank<TeamPVPRankElement>(enuma);
			AllRankList.add(rank);
			AllRankMap.put(enuma, rank);
		}
	}

	public static void init(Element root) throws KGameServerException {
		{
			Element rankTypes = root.getChild("RankType");
			for (KTeamPVPRankTypeEnum type : KTeamPVPRankTypeEnum.values()) {
				Element dataE = rankTypes.getChild(type.name());
				type.reset(Integer.parseInt(dataE.getChildTextTrim("maxLen")));
			}
		}
	}

	public static void notifyCacheLoadComplete() throws KGameServerException {
		// 从DB加载排行榜
		loadRankFromDB();

		// 启动任务
		TeamPVPRankTaskManager.notifyCacheLoadComplete();
	}

	private static void loadRankFromDB() throws KGameServerException {

		for (TeamPVPRank<TeamPVPRankElement> rank : AllRankList) {
			try {
				rank.load();
			} catch (Exception e) {
				throw new KGameServerException("加载排行榜错误：" + e.getMessage(), e);
			}
		}
	}

	public static void serverShutdown() throws KGameServerException {
		// 关服前发布一份排行榜
		onTimeSignalForPublish(true, true, true);
	}

	/**
	 * <pre>
	 * 提供于手工调用
	 * 即随时保存一份当前发布榜的数据
	 * 
	 * @param isAddDate
	 * @author CamusHuang
	 * @creation 2014-2-21 上午10:08:12
	 * </pre>
	 */
	public static void saveRankByGM(boolean isSaveToDB, boolean isSaveToFile, boolean isFileNameAddDate) {
		for (TeamPVPRank<TeamPVPRankElement> rank : AllRankList) {
			rank.save(isSaveToDB, isSaveToFile, isFileNameAddDate);
		}
	}

	/**
	 * <pre>
	 * 发布排行榜
	 * 
	 * @deprecated 仅供时效任务调用
	 * @author CamusHuang
	 * @creation 2014-2-21 上午9:48:25
	 * </pre>
	 */
	static void onTimeSignalForPublish(boolean isSaveToDB, boolean isSaveToFile, boolean isFileNameAddDate) {
		for (TeamPVPRank<TeamPVPRankElement> rank : AllRankList) {
			rank.onTimeSignalForPublish(isSaveToDB, isSaveToFile, isFileNameAddDate);
		}
	}

	/**
	 * <pre>
	 * 异步通知：排行榜变化
	 * 
	 * @param rankType
	 * @param changeElementIds
	 * @author CamusHuang
	 * @creation 2013-7-7 下午2:06:03
	 * </pre>
	 */
	static void synNotifyForRankChange(KTeamPVPRankTypeEnum rankType, Set<Long> changeElementIds) {
		// CTODO
	}

	static TeamPVPRank<TeamPVPRankElement> getRank(KTeamPVPRankTypeEnum type) {
		return AllRankMap.get(type);
	}

	private static List<TeamPVPRankElement> getRankElementList(KTeamPVPRankTypeEnum type) {
		TeamPVPRank<TeamPVPRankElement> rank = AllRankMap.get(type);
		return rank.getPublishData().getUnmodifiableElementList();
	}

	static int checkRank(KTeamPVPRankTypeEnum type, long tempId) {
		TeamPVPRank<TeamPVPRankElement> rank = getRank(type);
		if (rank == null) {
			return -1;
		}
		ElementAbs e = rank.getPublishData().getElement(tempId);
		if (e == null) {
			return -1;
		}
		return e.getRank();
	}
	
	static int checkRank(long tempId) {
		for(KTeamPVPRankTypeEnum type:KTeamPVPRankTypeEnum.values()){
			TeamPVPRank<TeamPVPRankElement> rank = getRank(type);
			if (rank == null) {
				continue;
			}
			TeamPVPRankElement e = rank.getPublishData().getElement(tempId);
			if(e!=null){
				return e.getRank();
			}
		}
		return -1;
	}

	static TeamPVPRankElement getRankElement(KTeamPVPRankTypeEnum type, long tempId) {
		TeamPVPRank<TeamPVPRankElement> rank = getRank(type);
		if (rank == null) {
			return null;
		}
		return rank.getPublishData().getElement(tempId);
	}
	
	static TeamPVPRankElement getRankElement(long tempId) {
		for(KTeamPVPRankTypeEnum type:KTeamPVPRankTypeEnum.values()){
			TeamPVPRank<TeamPVPRankElement> rank = getRank(type);
			if (rank == null) {
				continue;
			}
			TeamPVPRankElement e = rank.getPublishData().getElement(tempId);
			if(e!=null){
				return e;
			}
		}
		return null;
	}

	/**
	 * <pre>
	 * 队伍所属段位、段级、胜点变化时通知
	 * 会根据数据新增入榜
	 * 
	 * @param tempId
	 * @param tempName
	 * @param type 当前段位
	 * @param lv 段级
	 * @param exp 胜点
	 * @param battlePow 队伍战力
	 * @param leaderRoleId
	 * @param leaderRoleName
	 * @param leaderRoleVip
	 * @param memRoleId
	 * @param memRoleName
	 * @param memRoleVip
	 * @author CamusHuang
	 * @creation 2014-9-3 下午3:51:17
	 * </pre>
	 */
	static void notifyTempChange(long tempId, String tempName, KTeamPVPRankTypeEnum type, int lv, int exp, int battlePow, long leaderRoleId, String leaderRoleName, int leaderRoleVip, long memRoleId,
			String memRoleName, int memRoleVip) {
		for (TeamPVPRank<TeamPVPRankElement> rank : AllRankList) {
			if (rank.getType() != type) {
				rank.getTempCacheData().removeElement(tempId);
			}
		}
		AllRankMap.get(type).notifyTempChange(tempId, tempName, lv, exp, battlePow, leaderRoleId, leaderRoleName, leaderRoleVip, memRoleId, memRoleName, memRoleVip);
	}

	/**
	 * <pre>
	 * 单纯队伍队长、队员变更时通知
	 * 只更新现存于排行榜中的队伍的信息，不新增入榜
	 * 
	 * @param tempId
	 * @param type 当前段位
	 * @param leaderRoleId
	 * @param leaderRoleName
	 * @param leaderRoleVip
	 * @param memRoleId
	 * @param memRoleName
	 * @param memRoleVip
	 * @author CamusHuang
	 * @creation 2014-9-3 下午3:52:57
	 * </pre>
	 */
	static void resetTeamMemChange(long tempId, KTeamPVPRankTypeEnum type, long leaderRoleId, String leaderRoleName, int leaderRoleVip, long memRoleId, String memRoleName, int memRoleVip) {
		AllRankMap.get(type).resetTeamMemChange(tempId, leaderRoleId, leaderRoleName, leaderRoleVip, memRoleId, memRoleName, memRoleVip);
	}

	/**
	 * <pre>
	 * 单纯队伍战力变化时通知
	 * 只更新现存于排行榜中的队伍的信息，不新增入榜
	 * 
	 * @param tempId
	 * @param type 当前段位
	 * @param battlePower
	 * @return
	 * @author CamusHuang
	 * @creation 2013-7-7 上午11:43:58
	 * </pre>
	 */
	static void resetTeamBattlePower(long tempId, KTeamPVPRankTypeEnum type, int battlePower) {
		AllRankMap.get(type).resetTeamBattlePower(tempId, battlePower);
	}

	/**
	 * <pre>
	 * 队伍解散
	 * 
	 * @param tempId
	 * @author CamusHuang
	 * @creation 2013-8-28 下午3:46:23
	 * </pre>
	 */
	static void notifyTeampDelete(long tempId) {
		for (TeamPVPRank<TeamPVPRankElement> rank : AllRankList) {
			rank.getTempCacheData().removeElement(tempId);
		}
	}

	/**
	 * <pre>
	 * 找出最TOP的那个
	 * 
	 * @return [排行榜类型,排行榜元素]
	 * @author CamusHuang
	 * @creation 2014-9-3 下午5:06:43
	 * </pre>
	 */
	static Object[] searchTopElement() {
		int max = KTeamPVPRankTypeEnum.最强王者.sign;
		int min = KTeamPVPRankTypeEnum.青铜.sign;
		for (int type = max; type >= min; type--) {
			KTeamPVPRankTypeEnum enuma = KTeamPVPRankTypeEnum.getEnum(type);
			TeamPVPRank<TeamPVPRankElement> rank = AllRankMap.get(enuma);
			TeamPVPRankElement e = rank.getPublishData().getElementByRank(1);
			if (e != null) {
				return new Object[]{enuma,e};
			}
		}
		return null;
	}

	public static RankResult_GoodJob dealMsg_goodJob(KRole role, KTeamPVPRankTypeEnum type, long teamId, byte goodTime, boolean isConfirm) {
		RankResult_GoodJob result = new RankResult_GoodJob();
		CommonResult_Ext beGoodLeaderResult = new CommonResult_Ext();
		CommonResult_Ext beGoodMemResult = new CommonResult_Ext();

		if (type == null) {
			result.tips = RankTips.此排行榜未开放;
			return result;
		}

		// 点赞数据结构
		KRankGoodPrice data = KRankDataManager.mRoleRankGoodPriceManager.getData(goodTime);
		if (data == null) {
			result.tips = RankTips.点赞次数错误;
			return result;
		}

		TeamPVPRankElement topTeamElement = null;
		{
			Object[] objs = KTeamPVPRankLogic.searchTopElement();
			if(objs == null){
				result.tips = RankTips.此排行榜未产生冠军;
				return result;
			}
			
			topTeamElement = (TeamPVPRankElement)objs[1];
			if (topTeamElement == null) {
				result.tips = RankTips.此排行榜未产生冠军;
				return result;
			}

			if (teamId != topTeamElement.elementId) {
				result.tips = RankTips.此队伍不是冠军;
				return result;
			}
		}

		KRoleRankData doRoleData = KRankRoleExtCACreator.getRoleRankData(role.getId());// 点赞玩家数据
		doRoleData.rwLock.lock();
		try {
				KSupportFactory.getItemModuleSupport().lockItemSet(role.getId());
				try {
					// 检查玩家点赞次数
					long goodDoTime = doRoleData.getGoodDoTime(goodTime);
					if (goodDoTime >= data.time) {
						if (goodTime == 1) {
							result.tips = StringUtil.format(RankTips.每天只能操作x次点赞1次, data.time);
						} else {
							result.tips = StringUtil.format(RankTips.每天只能操作x次点赞32次, data.time);
						}
						return result;
					}

					// 扣费
					if (data.payMoney != null) {

						// 是否二次确认
						if (!isConfirm) {
							result.isGoConfirm = true;
							result.tips = StringUtil.format(RankTips.是否花费x数量x货币点赞x次, data.payMoney.currencyCount, data.payMoney.currencyType.extName, goodTime);
							return result;
						}

						if (0 > KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), data.payMoney, UsePointFunctionTypeEnum.冠军点赞, false)) {
							result.isGoMoneyUI = true;
							result.goMoneyUIType = data.payMoney.currencyType;
							result.goMoneyUICount = data.payMoney.currencyCount - KSupportFactory.getCurrencySupport().getMoney(role.getId(), data.payMoney.currencyType);
							result.tips = StringUtil.format(ShopTips.x货币数量不足x, data.payMoney.currencyType.extName, data.payMoney.currencyCount);
							return result;
						}
						result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, data.payMoney.currencyType.extName, data.payMoney.currencyCount));
					}

					// 记录点赞次数
					doRoleData.recordGoodDoTime(goodTime);

					// 记录被点赞
					KSupportFactory.getTeamPVPSupport().increaseTeamPVPGoodCount(teamId, goodTime);

					// 向点赞者发奖
					KRankLogic.sendGoodJobRewardToDoGood(role, data.payReward, result);

					// 向冠军队长发奖
					{
						KRole tempRole = KSupportFactory.getRoleModuleSupport().getRole(topTeamElement.getLeaderRoleId());
						if (tempRole != null) {
							KRankLogic.sendGoodJobRewardToTop(tempRole, data.topReward, beGoodLeaderResult);
						}
					}

					// 向冠军队员发奖
					{
						KRole tempRole = KSupportFactory.getRoleModuleSupport().getRole(topTeamElement.getMemRoleId());
						if (tempRole != null) {
							KRankLogic.sendGoodJobRewardToTop(tempRole, data.topReward, beGoodMemResult);
						}
					}

					result.isSucess = true;
					result.tips = RankTips.点赞成功;
					return result;
				} finally {
					KSupportFactory.getItemModuleSupport().unlockItemSet(role.getId());
				}
		} finally {
			doRoleData.rwLock.unlock();

			if (result.isSucess) {
				if (!data.payReward.addMoneys.isEmpty()) {
					KSupportFactory.getCurrencySupport().synCurrencyDataToClient(role.getId());
				}
				if (!data.topReward.addMoneys.isEmpty()) {
					KSupportFactory.getCurrencySupport().synCurrencyDataToClient(topTeamElement.getLeaderRoleId());
					KSupportFactory.getCurrencySupport().synCurrencyDataToClient(topTeamElement.getMemRoleId());
				}

				beGoodLeaderResult.doFinally(KSupportFactory.getRoleModuleSupport().getRole(topTeamElement.getLeaderRoleId()));
				beGoodLeaderResult.doFinally(KSupportFactory.getRoleModuleSupport().getRole(topTeamElement.getMemRoleId()));
			}
		}
	}

}
