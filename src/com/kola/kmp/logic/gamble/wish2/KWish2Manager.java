package com.kola.kmp.logic.gamble.wish2;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import jxl.read.biff.BiffException;

import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.gamble.KGambleRoleExtCACreator;
import com.kola.kmp.logic.gamble.KGambleRoleExtData;
import com.kola.kmp.logic.gamble.wish2.KRoleWish2Data.RoleGirdData;
import com.kola.kmp.logic.gamble.wish2.KRoleWish2Data.RolePoolData;
import com.kola.kmp.logic.gamble.wish2.KWish2ItemPool.KWish2DropItem;
import com.kola.kmp.logic.gamble.wish2.KWish2ItemPool.PoolInfoData;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.item.KItemMsgPackCenter;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KLimitTimeProduceActivityTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.TimeLimitActivityDataManager.TimeLimieProduceActivity;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.CompetitionTips;
import com.kola.kmp.logic.util.tips.GambleTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.RewardTips;
import com.kola.kmp.protocol.gamble.KGambleProtocol;

public class KWish2Manager {

	static final Logger _LOGGER = KGameLogger.getLogger(KWish2Manager.class);

	public static String dayReflashTimeStr;
	public static long dayReflashTimeDelay;

	public void init(String configPath) throws KGameServerException {

		Document doc = XmlUtil.openXml(configPath);
		if (doc != null) {
			Element root = doc.getRootElement();

			String wishExcelFilePath = root.getChildText("wish2ExcelFilePath");
			loadExcelData(wishExcelFilePath);
			KWish2ItemPool.initComplete();
		} else {
			throw new NullPointerException("许愿系统配置不存在！！");
		}
	}

	private void loadExcelData(String xlsPath) throws KGameServerException {
		KGameExcelFile xlsFile = null;
		try {
			xlsFile = new KGameExcelFile(xlsPath);
		} catch (BiffException e) {
			throw new KGameServerException("读取许愿系统excel表头发生错误！", e);
		} catch (IOException e) {
			throw new KGameServerException("读取许愿系统excel表头发生错误！", e);
		}

		if (xlsFile != null) {
			// 屌丝扭蛋数据表
			int dataRowIndex = 5;
			KGameExcelTable dataTable;
			KGameExcelRow[] allDataRows;

			String[] tableName = { "装备抽奖", "随从抽奖", "资源抽奖", "金币抽奖" };

			for (int k = 0; k < tableName.length; k++) {
				dataTable = xlsFile.getTable(tableName[k], dataRowIndex);
				allDataRows = dataTable.getAllDataRows();

				if (allDataRows != null) {
					for (int i = 0; i < allDataRows.length; i++) {
						int dropId = allDataRows[i].getInt("index");
						String itemCode = allDataRows[i].getData("id");
						int count = allDataRows[i].getInt("quantity");
						int openLv = allDataRows[i].getInt("mixlvl");
						int closeLv = allDataRows[i].getInt("maxlvl");
						byte job = allDataRows[i].getByte("job");
						int appearWeight = allDataRows[i].getInt("pro");
						boolean isMarqueeShow = allDataRows[i].getInt("isMarqueeShow") == 1;
						boolean isRare = allDataRows[i].getInt("isRare") == 1;
						int minAppearTime = allDataRows[i].getInt("minAppearTime");
						int maxAppearTime = allDataRows[i].getInt("maxAppearTime");
						int lotteryWeight = allDataRows[i].getInt("LatticePro");
						boolean isDefault = allDataRows[i].getBoolean("isDefault");

						KItemTempAbs itemTemp = KSupportFactory.getItemModuleSupport().getItemTemplate(itemCode);
						if (itemTemp == null) {
							throw new KGameServerException("初始化许愿2系统表<" + tableName[k] + ">的字段<id>错误，找不到对应的道具：" + itemCode + "，excel行数：" + allDataRows[i].getIndexInFile());
						}

						KWish2DropItem itemData = new KWish2DropItem(dropId, openLv, closeLv, job, appearWeight, minAppearTime, maxAppearTime, lotteryWeight, itemCode, itemTemp.extItemName, count,
								isDefault, isMarqueeShow, isRare);
						KWish2ItemPool.addDropableItem(KWish2ItemPool.poolType[k], itemData);
					}
				}
			}

			// 掷点表数据
			dataTable = xlsFile.getTable("抽奖引导", dataRowIndex);
			allDataRows = dataTable.getAllDataRows();

			if (allDataRows != null) {
				if (allDataRows.length > 2) {
					throw new KGameServerException("初始化许愿2系统表<抽奖引导>的行数错误，数据只能为2条。");
				}
				for (int i = 0; i < allDataRows.length; i++) {
					int dropId = allDataRows[i].getInt("index");
					String itemCode = allDataRows[i].getData("id");
					int count = allDataRows[i].getInt("quantity");
					int openLv = allDataRows[i].getInt("mixlvl");
					int closeLv = allDataRows[i].getInt("maxlvl");
					byte job = allDataRows[i].getByte("job");
					int appearWeight = allDataRows[i].getInt("pro");
					boolean isMarqueeShow = allDataRows[i].getInt("isMarqueeShow") == 1;
					boolean isRare = allDataRows[i].getInt("isRare") == 1;
					int minAppearTime = allDataRows[i].getInt("minAppearTime");
					int maxAppearTime = allDataRows[i].getInt("maxAppearTime");
					int lotteryWeight = allDataRows[i].getInt("LatticePro");
					boolean isDefault = allDataRows[i].getBoolean("isDefault");
					int petId = allDataRows[i].getInt("actualPet");

					KItemTempAbs itemTemp = KSupportFactory.getItemModuleSupport().getItemTemplate(itemCode);
					if (itemTemp == null) {
						throw new KGameServerException("初始化许愿2系统表<抽奖引导>的字段<id>错误，找不到对应的道具：" + itemCode + "，excel行数：" + allDataRows[i].getIndexInFile());
					}

					if (isDefault) {
						KWish2ItemPool.guideDropItem1 = new KWish2DropItem(dropId, openLv, closeLv, job, appearWeight, minAppearTime, maxAppearTime, lotteryWeight, itemCode, itemTemp.extItemName,
								count, isDefault, isMarqueeShow, isRare, petId);
						KWish2ItemPool._alldropItemMap.put(dropId, KWish2ItemPool.guideDropItem1);
					} else {
						KWish2ItemPool.guideDropItem2 = new KWish2DropItem(dropId, openLv, closeLv, job, appearWeight, minAppearTime, maxAppearTime, lotteryWeight, itemCode, itemTemp.extItemName,
								count, isDefault, isMarqueeShow, isRare, petId);
						KWish2ItemPool._alldropItemMap.put(dropId, KWish2ItemPool.guideDropItem2);
					}
				}
			}

			// 掷点表数据
			dataTable = xlsFile.getTable("抽奖参数", dataRowIndex);
			allDataRows = dataTable.getAllDataRows();

			if (allDataRows != null) {
				for (int i = 0; i < allDataRows.length; i++) {

					dayReflashTimeStr = allDataRows[i].getData("reflashTtime");
					try {
						dayReflashTimeDelay = UtilTool.parseHHmmToMillis(dayReflashTimeStr);
					} catch (ParseException e) {
						throw new KGameServerException("初始化许愿2系统表<抽奖参数>的字段<reflashTime>错误，转换不了时间格式：" + dayReflashTimeStr + "，excel行数：" + allDataRows[i].getIndexInFile(), e);
					}
				}
			}

			// 掷点表数据
			dataTable = xlsFile.getTable("抽奖池信息", dataRowIndex);
			allDataRows = dataTable.getAllDataRows();

			if (allDataRows != null) {
				for (int i = 0; i < allDataRows.length; i++) {
					byte poolType = allDataRows[i].getByte("poolType");
					String poolName = allDataRows[i].getData("poolName");
					boolean isCanReflash = allDataRows[i].getBoolean("canReflash");
					byte reflashCurrType = allDataRows[i].getByte("ReflashMoneyTypes");
					int reflashUseCount = allDataRows[i].getInt("ReflashCost");
					byte lotteryCurrType = allDataRows[i].getByte("LotteryMoneyTypes");
					int wishUseCount = allDataRows[i].getInt("LotteryCost");
					int wish10UseCount = allDataRows[i].getInt("Lottery10Cost");

					if (isCanReflash && KCurrencyTypeEnum.getEnum(reflashCurrType) == null) {
						throw new KGameServerException("初始化许愿2系统表<抽奖池信息>的字段<ReflashMoneyTypes>错误，找不到货币类型：" + reflashCurrType + "，excel行数：" + allDataRows[i].getIndexInFile());
					}
					if (KCurrencyTypeEnum.getEnum(lotteryCurrType) == null) {
						throw new KGameServerException("初始化许愿2系统表<抽奖池信息>的字段<LotteryMoneyTypes>错误，找不到货币类型：" + lotteryCurrType + "，excel行数：" + allDataRows[i].getIndexInFile());
					}
					if (isCanReflash) {
						PoolInfoData poolInfoData = new PoolInfoData(poolType, poolName, KCurrencyTypeEnum.getEnum(reflashCurrType), reflashUseCount, KCurrencyTypeEnum.getEnum(lotteryCurrType),
								wishUseCount, wish10UseCount);
						KWish2ItemPool._poolInfoMap.put(poolType, poolInfoData);
					} else {
						PoolInfoData poolInfoData = new PoolInfoData(poolType, poolName, KCurrencyTypeEnum.getEnum(lotteryCurrType), wishUseCount, wish10UseCount);
						KWish2ItemPool._poolInfoMap.put(poolType, poolInfoData);
					}
				}
			}
		}
	}

	/**
	 * 发送许愿数据
	 * 
	 * @param role
	 */
	public void sendWishData(KRole role) {
		KGambleRoleExtData extData = KGambleRoleExtCACreator.getGambleRoleExtData(role.getId());
		KRoleWish2Data wish2Data = extData.getWish2Data();
		wish2Data.checkAndResetWishData(role);

		RolePoolData poolData;
		PoolInfoData poolInfo;
		KGameMessage msg = KGame.newLogicMessage(KGambleProtocol.SM_GET_WISH2_DATA);
		msg.writeInt(KWish2ItemPool.poolType.length);
		for (byte poolType : KWish2ItemPool.poolType) {
			poolData = wish2Data._rolePoolDataMap.get(poolType);
			poolInfo = KWish2ItemPool._poolInfoMap.get(poolType);
			msg.writeByte(poolType);
			msg.writeUtf8String(poolInfo.poolName);
			msg.writeInt(poolData.nowIndex);
			msg.writeInt(poolData._roleGirdDataList.size());
			for (RoleGirdData girdData : poolData._roleGirdDataList) {
				KWish2DropItem itemData = KWish2ItemPool._alldropItemMap.get(girdData.dropId);
				msg.writeInt(girdData.girdIndex);
				msg.writeBoolean(itemData.isRare);
				msg.writeBoolean(girdData.isUse);
				KItemMsgPackCenter.packItem(msg, role.getId(), itemData.itemCountStruct.getItemTemplate(), itemData.dropCount);
			}

			msg.writeBoolean(poolInfo.isCanReflash);
			if (poolInfo.isCanReflash) {
				msg.writeByte(poolInfo.reflashCurrType.sign);
				msg.writeInt(poolInfo.reflashUseCount);
			}
			msg.writeByte(poolInfo.lotteryCurrType.sign);
			msg.writeInt(getSingleWishUseCount(poolType));
			msg.writeBoolean(poolInfo.isCan10Lottery);
			if (poolInfo.isCan10Lottery) {
				msg.writeByte(poolInfo.lotteryCurrType.sign);
				msg.writeInt(getRestWishUseCount(poolType, poolData.getRestAllLotteryUseCurrCount()));
			}
		}
		msg.writeInt(wish2Data._history.size());
		for (String history : wish2Data._history) {
			msg.writeUtf8String(history);
		}

		role.sendMsg(msg);
	}

	public int getSingleWishUseCount(byte poolType) {
		PoolInfoData poolInfo = KWish2ItemPool._poolInfoMap.get(poolType);
		int discount = 100;
		TimeLimieProduceActivity activity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.幸运转盘打折);
		if (activity != null && activity.wish2PoolType != null && activity.isActivityTakeEffectNow()) {
			for (int i = 0; i < activity.wish2PoolType.length; i++) {
				if (activity.wish2PoolType[i] == poolType) {
					discount = activity.discount;
					break;
				}
			}
		}
		return poolInfo.wishUseCount * discount / 100;
	}

	public int getRestWishUseCount(byte poolType, int restCount) {
		PoolInfoData poolInfo = KWish2ItemPool._poolInfoMap.get(poolType);
		int discount = 100;
		TimeLimieProduceActivity activity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.幸运转盘打折);
		if (activity != null && activity.isActivityTakeEffectNow()) {
			for (int i = 0; i < activity.wish2PoolType.length; i++) {
				if (activity.wish2PoolType[i] == poolType) {
					discount = activity.discount;
					break;
				}
			}
		}
		return restCount * discount / 100;
	}

	public void reflashWishData(KRole role, byte poolType, boolean isCheck) {
		KGambleRoleExtData extData = KGambleRoleExtCACreator.getGambleRoleExtData(role.getId());
		KRoleWish2Data wish2Data = extData.getWish2Data();

		RolePoolData poolData = wish2Data._rolePoolDataMap.get(poolType);
		PoolInfoData poolInfo = KWish2ItemPool._poolInfoMap.get(poolType);

		if (poolInfo == null || poolData == null) {
			KDialogService.sendUprisingDialog(role, GlobalTips.服务器繁忙请稍候再试);
			return;
		}
		if (isCheck) {
			if (!poolInfo.isCanReflash) {
				KDialogService.sendUprisingDialog(role, GlobalTips.服务器繁忙请稍候再试);
				return;
			}

			long result = KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), poolInfo.reflashCurrType, poolInfo.reflashUseCount, UsePointFunctionTypeEnum.幸运转盘刷新, true);
			// 元宝不足，发送提示
			if (result == -1) {
				KDialogService.sendUprisingDialog(role, GambleTips.getTipsReflashWish2NotEnoughIgot(poolInfo.reflashUseCount, poolInfo.lotteryCurrType.extName));
				return;
			}
		}

		wish2Data.reflashPool(role, poolType);

		sendReflashWishMsg(role, poolType, poolData, poolInfo);

		KDialogService.sendUprisingDialog(role, CompetitionTips.getTipsReflashCompetitionSuccess());
	}

	public void sendReflashWishMsg(KRole role, byte poolType, RolePoolData poolData, PoolInfoData poolInfo) {
		KGameMessage msg = KGame.newLogicMessage(KGambleProtocol.SM_REFLASH_WISH2_DATA);
		msg.writeByte(poolType);
		msg.writeUtf8String(poolInfo.poolName);
		msg.writeInt(poolData.nowIndex);
		msg.writeInt(poolData._roleGirdDataList.size());
		for (RoleGirdData girdData : poolData._roleGirdDataList) {
			KWish2DropItem itemData = KWish2ItemPool._alldropItemMap.get(girdData.dropId);
			msg.writeInt(girdData.girdIndex);
			msg.writeBoolean(itemData.isRare);
			msg.writeBoolean(girdData.isUse);
			KItemMsgPackCenter.packItem(msg, role.getId(), itemData.itemCountStruct.getItemTemplate(), itemData.dropCount);
		}

		msg.writeBoolean(poolInfo.isCanReflash);
		if (poolInfo.isCanReflash) {
			msg.writeByte(poolInfo.reflashCurrType.sign);
			msg.writeInt(poolInfo.reflashUseCount);
		}
		msg.writeByte(poolInfo.lotteryCurrType.sign);
		msg.writeInt(getSingleWishUseCount(poolType));
		msg.writeBoolean(poolInfo.isCan10Lottery);
		if (poolInfo.isCan10Lottery) {
			msg.writeByte(poolInfo.lotteryCurrType.sign);
			msg.writeInt(getRestWishUseCount(poolType, poolData.getRestAllLotteryUseCurrCount()));
		}
		role.sendMsg(msg);
	}

	public void processWish(KRole role, byte poolType, boolean isUse10Count) {
		KGambleRoleExtData extData = KGambleRoleExtCACreator.getGambleRoleExtData(role.getId());
		KRoleWish2Data wish2Data = extData.getWish2Data();

		RolePoolData poolData = wish2Data._rolePoolDataMap.get(poolType);
		PoolInfoData poolInfo = KWish2ItemPool._poolInfoMap.get(poolType);

		if (poolInfo == null || poolData == null) {
			KDialogService.sendUprisingDialog(role, GlobalTips.服务器繁忙请稍候再试);
			return;
		}
		if (isUse10Count) {
			// for (int i = 0; i <
			// wish2Data._rolePoolDataMap.get(poolType)._roleGirdDataList.size();
			// i++) {
			// RoleGirdData girdData =
			// wish2Data._rolePoolDataMap.get(poolType)._roleGirdDataList.get(i);
			// if (girdData.isUse) {
			// KDialogService.sendUprisingDialog(role,
			// GambleTips.getTipsWish2CanNotLotteryAll());
			// return;
			// }
			// }
		} else {
			boolean isCanLottery = false;
			for (int i = 0; i < wish2Data._rolePoolDataMap.get(poolType)._roleGirdDataList.size(); i++) {
				RoleGirdData girdData = wish2Data._rolePoolDataMap.get(poolType)._roleGirdDataList.get(i);
				if (!girdData.isUse) {
					isCanLottery = true;
					break;
				}
			}
			if (!isCanLottery) {
				KDialogService.sendUprisingDialog(role, GlobalTips.服务器繁忙请稍候再试);
				reflashWishData(role, poolType, false);
				return;
			}
		}

		if (!wish2Data.isGuideWish) {
			int point = getSingleWishUseCount(poolType);
			if (isUse10Count) {
				point = getRestWishUseCount(poolType, poolData.getRestAllLotteryUseCurrCount());
			}
			if (point > 0) {
				// 元宝不足，发送提示
				long result = KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), poolInfo.lotteryCurrType, point, UsePointFunctionTypeEnum.幸运转盘刷新, true);
				if (result == -1) {
					KDialogService.sendUprisingDialog(role, GambleTips.getTipsWish2NotEnoughIgot(isUse10Count ? 10 : 1, point, poolInfo.lotteryCurrType.extName));
					return;
				}
			} else {
				KDialogService.sendUprisingDialog(role, GlobalTips.服务器繁忙请稍候再试);
				return;
			}
		}

		boolean isActivty10CountPresentItem = false;
		List<ItemCountStruct> activty10CountPresentItemList = new ArrayList<ItemCountStruct>();
		if (!wish2Data.isGuideWish && isUse10Count && poolData.restLotteryCount == 10) {
			TimeLimieProduceActivity presentActivity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.幸运转盘10连抽送道具);
			if (presentActivity != null && presentActivity.isActivityTakeEffectNow()) {
				if (presentActivity.wish2PresentItemList.containsKey(poolType) && presentActivity.wish2PresentItemList.get(poolType).size() > 0) {
					activty10CountPresentItemList.addAll(presentActivity.wish2PresentItemList.get(poolType));
					isActivty10CountPresentItem = true;
				}
			}
		}

		List<ItemCountStruct> priceList = new ArrayList<ItemCountStruct>();
		List<Integer> pricePetList = new ArrayList<Integer>();
		List<String> tipsList = new ArrayList<String>();
		int nowIndex = -1;
		if (isUse10Count) {
			for (int i = 0; i < wish2Data._rolePoolDataMap.get(poolType)._roleGirdDataList.size(); i++) {
				RoleGirdData girdData = wish2Data._rolePoolDataMap.get(poolType)._roleGirdDataList.get(i);
				int dropId = girdData.dropId;
				if (poolType == KWish2ItemPool.GOLD_POOL && wish2Data.isGuideWish) {
					pricePetList.add(KWish2ItemPool._alldropItemMap.get(dropId).petId);
				} else {
					ItemCountStruct struct = KWish2ItemPool._alldropItemMap.get(dropId).itemCountStruct;
					priceList.add(struct);
					tipsList.add(GambleTips.getTipsWishPriceInfo(struct.getItemTemplate().extItemName, struct.itemCount));
				}
			}
		} else {
			int caculateCount = 0;
			while (nowIndex == -1 && caculateCount < 10) {
				int totalWeight = 0;
				List<RoleGirdData> caculateList = new ArrayList<RoleGirdData>();
				for (int i = 0; i < wish2Data._rolePoolDataMap.get(poolType)._roleGirdDataList.size(); i++) {
					RoleGirdData girdData = wish2Data._rolePoolDataMap.get(poolType)._roleGirdDataList.get(i);
					if (!girdData.isUse) {
						totalWeight += girdData.dropWeight;
						caculateList.add(girdData);
					}
				}
				if (!caculateList.isEmpty()) {
					int weight = UtilTool.random(0, totalWeight);
					int tempRate = 0;
					L2: for (RoleGirdData girdData : caculateList) {
						if (tempRate < weight && weight <= (tempRate + girdData.dropWeight)) {
							nowIndex = girdData.girdIndex;
							ItemCountStruct struct = KWish2ItemPool._alldropItemMap.get(girdData.dropId).itemCountStruct;
							priceList.add(struct);
							tipsList.add(GambleTips.getTipsWishPriceInfo(struct.getItemTemplate().extItemName, struct.itemCount));
							break L2;
						} else {
							tempRate += girdData.dropWeight;
						}
					}
				}
				caculateCount++;
			}
		}

		BaseRewardData rewardData = null;
		if (poolType == KWish2ItemPool.GOLD_POOL && wish2Data.isGuideWish) {
			rewardData = new BaseRewardData(null, null, null, null, pricePetList);
		} else {
			rewardData = new BaseRewardData(null, null, priceList, null, null);
		}
		if (!rewardData.sendReward(role, PresentPointTypeEnum.幸运转盘奖励)) {
			BaseMailContent mainContent = new BaseMailContent(GambleTips.getTipsWish2PriceMailTitle(), GambleTips.getTipsWish2PriceMailContent(), null, null);
			KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), mainContent, rewardData, PresentPointTypeEnum.幸运转盘奖励);

			KDialogService.sendDataUprisingDialog(role, RewardTips.背包已满奖励通过邮件发送);
		}

		boolean isNeedReflash = false;
		if (poolType == KWish2ItemPool.GOLD_POOL && wish2Data.isGuideWish) {
			wish2Data.guideWish(role);
			isNeedReflash = true;
		} else {
			if (isUse10Count) {
				wish2Data.wish10Count(role, poolType);
				isNeedReflash = true;
			} else {
				isNeedReflash = wish2Data.wish(role, poolType, nowIndex);
			}
			for (String tips : tipsList) {
				wish2Data.addHistory(tips);
			}
		}

		KGameMessage msg = KGame.newLogicMessage(KGambleProtocol.SM_WISH2);
		msg.writeByte(poolType);
		msg.writeBoolean(isUse10Count);
		if (!isUse10Count) {
			msg.writeInt(nowIndex);
			msg.writeBoolean(poolInfo.isCan10Lottery);
			if (poolInfo.isCan10Lottery) {
				msg.writeByte(poolInfo.lotteryCurrType.sign);
				msg.writeInt(getRestWishUseCount(poolType, poolData.getRestAllLotteryUseCurrCount()));
			}
		}
		msg.writeInt(tipsList.size());
		for (String tips : tipsList) {
			msg.writeUtf8String(tips);
		}
		role.sendMsg(msg);

		KDialogService.sendNullDialog(role);

		if (isNeedReflash) {
			sendReflashWishMsg(role, poolType, poolData, poolInfo);
		}

		if (isActivty10CountPresentItem) {
			TimeLimieProduceActivity presentActivity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.幸运转盘10连抽送道具);
			KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), activty10CountPresentItemList, presentActivity.mailTitle, presentActivity.mailContent);
		}
	}

	public static void notifyRoleDataInitComplete(KRole role) {
		KGambleRoleExtData extData = KGambleRoleExtCACreator.getGambleRoleExtData(role.getId());
		KRoleWish2Data wish2Data = extData.getWish2Data();
		wish2Data.checkPoolDatas();
	}

	public void checkAndReflashWish2Data(KRole role) {
		KGambleRoleExtData extData = KGambleRoleExtCACreator.getGambleRoleExtData(role.getId());
		KRoleWish2Data wish2Data = extData.wish2Data;
		if (wish2Data != null) {
			boolean isReflash = wish2Data.checkAndResetWishData(role);
			if (isReflash) {
				sendWishData(role);
			}
		}

	}
}
