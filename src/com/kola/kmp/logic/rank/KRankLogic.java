package com.kola.kmp.logic.rank;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kmp.logic.competition.KCompetitor;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KRankTypeEnum;
import com.kola.kmp.logic.rank.KRankDataStructs.KRankGoodPrice;
import com.kola.kmp.logic.rank.KRankDataStructs.KRankGoodPrice.RankGoodReward;
import com.kola.kmp.logic.rank.gang.GangRankElementAbs;
import com.kola.kmp.logic.rank.gang.KGangRankLogic;
import com.kola.kmp.logic.rank.gang.KGangRankTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult_Ext;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_AddItem;
import com.kola.kmp.logic.util.ResultStructs.RankResult_GoodJob;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.RankTips;
import com.kola.kmp.logic.util.tips.RewardTips;
import com.kola.kmp.logic.util.tips.ShopTips;

/**
 * <pre>
 * 排行榜
 * 
 * 竞技排行榜由竞技模块提供
 * 
 * @author camus
 * @creation 2012-12-30 下午2:50:58
 * </pre>
 */
public class KRankLogic {

	public static final Logger _LOGGER = KGameLogger.getLogger(KRankLogic.class);

	private KRankLogic() {
	}

	// 以下不包含竞技场排行榜
	static final Rank<RankElementPower> aresRank = new Rank<RankElementPower>(KRankTypeEnum.战力榜);
	static final Rank<RankElementLevel> levelRank = new Rank<RankElementLevel>(KRankTypeEnum.等级榜);
	static final Rank<RankElementPet> petRank = new Rank<RankElementPet>(KRankTypeEnum.随从榜);

	// 以下不包含竞技场排行榜
	private static final Rank[] AllRankList = new Rank[] { aresRank, levelRank, petRank };
	private static final Map<KRankTypeEnum, Rank> AllRankMap = new HashMap<KRankTypeEnum, Rank>();
	static {
		for (Rank rank : AllRankList) {
			AllRankMap.put(rank.getType(), rank);
		}
	}

	static void loadRankFromDB() throws KGameServerException {
		for (Rank rank : AllRankList) {
			try {
				rank.load();
			} catch (Exception e) {
				throw new KGameServerException("加载排行榜错误：" + e.getMessage(), e);
			}
		}
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
		for (Rank rank : AllRankList) {
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
		for (Rank rank : AllRankList) {
			rank.onTimeSignalForPublish(isSaveToDB, isSaveToFile, isFileNameAddDate);
		}
	}

	/**
	 * <pre>
	 * 异步通知：排行榜变化
	 * 
	 * @param rankType
	 * @param changeRoles
	 * @author CamusHuang
	 * @creation 2013-7-7 下午2:06:03
	 * </pre>
	 */
	static void synNotifyForRankChange(KRankTypeEnum rankType, Set<Long> changeRoles) {
		// CTODO 通知称号系统
	}

	public static Rank getRank(KRankTypeEnum type) {
		return AllRankMap.get(type);
	}

	static void notifyPlayerRoleLevelUp(KRole role, int lv, int exp) {
		for (Rank rank : AllRankList) {
			rank.resetRoleLevelForAllRank(role, lv);
			rank.notifyRoleLevelUp(role, lv, exp);
		}
	}

	static void notifyBattlePowerChange(KRole role, int battlePower) {
		for (Rank rank : AllRankList) {
			rank.resetRoleBattlePowerForAllRank(role, battlePower);
			rank.notifyBattlePowerChange(role, battlePower);
		}
	}

	static void notifyPetInfoChange(KRole role, String petName, int petLv, int petPow) {
		for (Rank rank : AllRankList) {
			rank.notifyPetInfoChange(role, petName, petLv, petPow);
		}
	}

	static void notifyRoleDeleted(long roleId) {
		for (Rank rank : AllRankList) {
			rank.getTempCacheData().removeElement(roleId);
		}
	}

	static void notifyRoleVipUp(long roleId, byte vipLv) {
		for (Rank rank : AllRankList) {
			RankElementAbs e = (RankElementAbs) rank.getTempCacheData().getElement(roleId);
			if (e != null) {
				e.setVipLv(vipLv);
			}
		}
	}

	public static RankResult_GoodJob dealMsg_goodJob(KRole role, KRankTypeEnum type, long beGoodRoleId, byte goodTime, boolean isConfirm) {
		RankResult_GoodJob result = new RankResult_GoodJob();
		CommonResult_Ext beGoodResult = new CommonResult_Ext();

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

		{
			long firstRoleId = 0;
			switch (type) {
			case 等级榜: {
				RankElementAbs firstE = levelRank.getPublishData().getElementByRank(1);
				if (firstE != null) {
					firstRoleId = firstE.elementId;
				}
				break;
			}
			case 战力榜: {
				RankElementAbs firstE = aresRank.getPublishData().getElementByRank(1);
				if (firstE != null) {
					firstRoleId = firstE.elementId;
				}
				break;
			}
			case 随从榜: {
				RankElementAbs firstE = petRank.getPublishData().getElementByRank(1);
				if (firstE != null) {
					firstRoleId = firstE.elementId;
				}
				break;
			}
			case 竞技榜: {
				KCompetitor firstE = KSupportFactory.getCompetitionModuleSupport().getCompetitor(1);
				if (firstE != null) {
					firstRoleId = firstE.getRoleId();
				}
				break;
			}
			}
			//
			if (firstRoleId < 1) {
				result.tips = RankTips.此排行榜未产生冠军;
				return result;
			}

			if (beGoodRoleId != firstRoleId) {
				result.tips = RankTips.此玩家不是冠军;
				return result;
			}
		}

		KRole beGoodRole = KSupportFactory.getRoleModuleSupport().getRole(beGoodRoleId);
		if (beGoodRole == null) {
			result.tips = GlobalTips.角色不存在;
			return result;
		}

		KRoleRankData doRoleData = KRankRoleExtCACreator.getRoleRankData(role.getId());// 点赞玩家数据
		KRoleRankData bedoRoleData = KRankRoleExtCACreator.getRoleRankData(beGoodRoleId);// 被点赞玩家数据
		lockRoleRanDatas(doRoleData, bedoRoleData);
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
				bedoRoleData.setGood(bedoRoleData.getGood() + goodTime);

				// 向点赞者发奖
				sendGoodJobRewardToDoGood(role, data.payReward, result);

				// 向冠军发奖
				sendGoodJobRewardToTop(beGoodRole, data.topReward, beGoodResult);

				result.isSucess = true;
				result.tips = RankTips.点赞成功;
				return result;
			} finally {
				KSupportFactory.getItemModuleSupport().unlockItemSet(role.getId());
			}
		} finally {
			unlockRoleRanDatas(doRoleData, bedoRoleData);

			if (result.isSucess) {
				if (!data.payReward.addMoneys.isEmpty()) {
					KSupportFactory.getCurrencySupport().synCurrencyDataToClient(role.getId());
				}
				if (!data.topReward.addMoneys.isEmpty()) {
					KSupportFactory.getCurrencySupport().synCurrencyDataToClient(beGoodRoleId);
				}

				beGoodResult.doFinally(beGoodRole);
			}
		}
	}
	
	public static RankResult_GoodJob dealMsg_goodJob(KRole role, KGangRankTypeEnum type, long beGoodRoleId, byte goodTime, boolean isConfirm) {
		RankResult_GoodJob result = new RankResult_GoodJob();
		CommonResult_Ext beGoodResult = new CommonResult_Ext();

		if (type == null) {
			result.tips = RankTips.此排行榜未开放;
			return result;
		}

		// 点赞数据结构
		KRankGoodPrice data = KRankDataManager.mGangRankGoodPriceManager.getData(goodTime);
		if (data == null) {
			result.tips = RankTips.点赞次数错误;
			return result;
		}

		{
			if (type != KGangRankTypeEnum.军团战力) {
				result.tips = RankTips.此排行榜未开放;
				return result;
			}
			long firstGangId = 0;
			{
				GangRankElementAbs firstE = (GangRankElementAbs)KGangRankLogic.getRank(type).getPublishData().getElementByRank(1);
				if (firstE != null) {
					firstGangId = firstE.elementId;
				}
			}
			//
			if (firstGangId < 1) {
				result.tips = RankTips.此排行榜未产生冠军;
				return result;
			}

			long beGoodGangId = KSupportFactory.getGangSupport().getGangIdByRoleId(beGoodRoleId);
			if (beGoodGangId != firstGangId) {
				result.tips = RankTips.此军团不是冠军;
				return result;
			}
		}

		KRole beGoodRole = KSupportFactory.getRoleModuleSupport().getRole(beGoodRoleId);
		if (beGoodRole == null) {
			result.tips = GlobalTips.角色不存在;
			return result;
		}

		KRoleRankData doRoleData = KRankRoleExtCACreator.getRoleRankData(role.getId());// 点赞玩家数据
		KRoleRankData bedoRoleData = KRankRoleExtCACreator.getRoleRankData(beGoodRoleId);// 被点赞玩家数据
		lockRoleRanDatas(doRoleData, bedoRoleData);
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
				bedoRoleData.setGood(bedoRoleData.getGood() + goodTime);

				// 向点赞者发奖
				sendGoodJobRewardToDoGood(role, data.payReward, result);

				// 向冠军发奖
				sendGoodJobRewardToTop(beGoodRole, data.topReward, beGoodResult);

				result.isSucess = true;
				result.tips = RankTips.点赞成功;
				return result;
			} finally {
				KSupportFactory.getItemModuleSupport().unlockItemSet(role.getId());
			}
		} finally {
			unlockRoleRanDatas(doRoleData, bedoRoleData);

			if (result.isSucess) {
				if (!data.payReward.addMoneys.isEmpty()) {
					KSupportFactory.getCurrencySupport().synCurrencyDataToClient(role.getId());
				}
				if (!data.topReward.addMoneys.isEmpty()) {
					KSupportFactory.getCurrencySupport().synCurrencyDataToClient(beGoodRoleId);
				}

				beGoodResult.doFinally(beGoodRole);
			}
		}
	}	

	/**
	 * <pre>
	 * 向发起点赞者发奖
	 * 
	 * @param tempRole
	 * @param tempReward
	 * @param tempResult
	 * @author CamusHuang
	 * @creation 2014-9-3 下午6:24:41
	 * </pre>
	 */
	public static void sendGoodJobRewardToDoGood(KRole tempRole, RankGoodReward tempReward, CommonResult_Ext tempResult) {
		ItemCountStruct addItem = tempReward.randomAddItem();
		if (addItem != null) {
			ItemResult_AddItem addItemResult = KSupportFactory.getItemModuleSupport().addItemToBag(tempRole, addItem, "点赞");
			if (addItemResult.isSucess) {
				tempResult.addDataUprisingTips(StringUtil.format(ShopTips.x加x, addItem.getItemTemplate().extItemName, addItem.itemCount));
			} else {
				// 转发邮件
				KSupportFactory.getMailModuleSupport().sendAttMailBySystem(tempRole.getId(), addItem, RewardTips.点赞发奖邮件标题, RewardTips.背包已满通用奖励邮件内容);
				tempResult.addUprisingTips(RewardTips.背包已满奖励通过邮件发送);
			}
		}
		if (!tempReward.addMoneys.isEmpty()) {
			KSupportFactory.getCurrencySupport().increaseMoneys(tempRole.getId(), tempReward.addMoneys, PresentPointTypeEnum.冠军点赞, true);
			for (KCurrencyCountStruct money : tempReward.addMoneys) {
				tempResult.addDataUprisingTips(StringUtil.format(ShopTips.x加x, money.currencyType.extName, money.currencyCount));
			}
		}
	}

	/**
	 * <pre>
	 * 向被点赞冠军发奖
	 * 
	 * @param tempRole
	 * @param tempReward
	 * @param tempResult
	 * @author CamusHuang
	 * @creation 2014-9-3 下午6:24:14
	 * </pre>
	 */
	public static void sendGoodJobRewardToTop(KRole tempRole, RankGoodReward tempReward, CommonResult_Ext tempResult) {
		//
		tempResult.addUprisingTips(RewardTips.被赞发奖邮件标题);
		//
		ItemCountStruct addItem = tempReward.randomAddItem();
		if (addItem != null) {
			ItemResult_AddItem addItemResult = KSupportFactory.getItemModuleSupport().addItemToBag(tempRole, addItem, "被点赞");
			if (addItemResult.isSucess) {
				tempResult.addDataUprisingTips(StringUtil.format(ShopTips.x加x, addItem.getItemTemplate().extItemName, addItem.itemCount));
			} else {
				// 转发邮件
				KSupportFactory.getMailModuleSupport().sendAttMailBySystem(tempRole.getId(), addItem, RewardTips.被赞发奖邮件标题, RewardTips.背包已满通用奖励邮件内容);
				tempResult.addUprisingTips(RewardTips.背包已满奖励通过邮件发送);
			}
		}
		if (!tempReward.addMoneys.isEmpty()) {
			KSupportFactory.getCurrencySupport().increaseMoneys(tempRole.getId(), tempReward.addMoneys, PresentPointTypeEnum.冠军点赞, true);
			for (KCurrencyCountStruct money : tempReward.addMoneys) {
				tempResult.addDataUprisingTips(StringUtil.format(ShopTips.x加x, money.currencyType.extName, money.currencyCount));
			}
		}
	}

	private static void lockRoleRanDatas(KRoleRankData set1, KRoleRankData set2) {
		KRoleRankData setA = null;
		KRoleRankData setB = null;
		if (set1.getRoleId() > set2.getRoleId()) {
			setA = set1;
			setB = set2;
		} else {
			setA = set2;
			setB = set1;
		}

		setA.rwLock.lock();
		setB.rwLock.lock();
	}

	private static void unlockRoleRanDatas(KRoleRankData set1, KRoleRankData set2) {
		KRoleRankData setA = null;
		KRoleRankData setB = null;
		if (set1.getRoleId() > set2.getRoleId()) {
			setA = set1;
			setB = set2;
		} else {
			setA = set2;
			setB = set1;
		}

		setA.rwLock.unlock();
		setB.rwLock.unlock();
	}
}
