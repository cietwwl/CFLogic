package com.kola.kmp.logic.reward.garden;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.management.timer.Timer;

import com.koala.game.KGameMessage;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kgame.cache.role.RoleBaseInfo;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.item.KItemMsgPackCenter;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.reward.garden.KGardenDataManager.KGardenCommonTreeDataManager.GardenCommonRewardData;
import com.kola.kmp.logic.reward.garden.KGardenDataManager.KGardenRoleRateDataManager.GardenRoleRateData;
import com.kola.kmp.logic.reward.garden.KGardenDataManager.KGardenTopTreeDataManager.GardenTopRewardData;
import com.kola.kmp.logic.reward.garden.KGardenDataManager.KTreeRipeTimeDataManager.TreeRipeTimeData;
import com.kola.kmp.logic.reward.garden.KRoleGarden.TreeData;
import com.kola.kmp.logic.reward.garden.KRoleGarden.VIPSaveData;
import com.kola.kmp.logic.reward.garden.message.KGardenSynMsg;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.ResultStructs.CommonResult_Ext;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_AddItem;
import com.kola.kmp.logic.util.ResultStructs.RewardResult_Garden;
import com.kola.kmp.logic.util.ResultStructs.RewardResult_GardenCollect;
import com.kola.kmp.logic.util.ResultStructs.RewardResult_GardenCollectTop;
import com.kola.kmp.logic.util.ResultStructs.RewardResult_GardenOneKeyCollect;
import com.kola.kmp.logic.util.ResultStructs.RewardResult_SendMail;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.RewardTips;
import com.kola.kmp.logic.util.tips.ShopTips;
import com.kola.kmp.protocol.reward.KRewardProtocol;

public class KGardenCenter {

	/**
	 * <pre>
	 * 参考{@link KRewardProtocol#SM_GARDEN_PUSH_CONSTANCE}
	 * 
	 * @param msg
	 * @author CamusHuang
	 * @creation 2014-4-29 下午3:40:25
	 * </pre>
	 */
	public static void packGardenConstance(KRole role, KGameMessage msg) {
		int writeindex = msg.writerIndex();
		msg.writeByte(0);
		int count = 0;
		for (GardenCommonRewardData data : KGardenDataManager.mGardenCommonTreeDataManager.getDataCache().values()) {
			msg.writeByte(data.type);
			msg.writeInt(data.icon);
			msg.writeUtf8String(data.name);
			TreeRipeTimeData temp = KGardenDataManager.mTreeRipeTimeDataManager.getData(data.type);
			msg.writeLong(temp.ripeTime / Timer.ONE_SECOND);
			msg.writeBoolean(false);
			count++;
		}

		// <类型,<角色等级,数据>>
		int roleLv = role.getLevel();
		for (Map<Integer, GardenTopRewardData> tempMap : KGardenDataManager.mGardenTopTreeDataManager.getDataCache().values()) {
			GardenTopRewardData data = tempMap.get(roleLv);
			msg.writeByte(data.type);
			msg.writeInt(data.icon);
			msg.writeUtf8String(data.name);
			TreeRipeTimeData temp = KGardenDataManager.mTreeRipeTimeDataManager.getData(data.type);
			msg.writeLong(temp.ripeTime / Timer.ONE_SECOND);
			msg.writeBoolean(true);
			//
			msg.writeByte(data.addItems.size());
			for (ItemCountStruct item : data.addItems) {
				KItemMsgPackCenter.packItem(msg, item.getItemTemplate(), item.itemCount);
			}
			count++;
		}
		msg.setByte(writeindex, count);
		//
		KRoleGarden garden = KGardenRoleExtCACreator.getRoleGarden(role.getId());
		garden.rwLock.lock();
		try {
			msg.writeByte(KGardenDataManager.FeetLogMaxCount);
			List<String> logs = garden.getOldFeetLogCache();
			msg.writeByte(logs.size());
			for (String log : logs) {
				msg.writeUtf8String(log);
			}
		} finally {
			garden.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 参考{@link KRewardProtocol#MSG_STRUCT_GARDEN_DATA}
	 * 
	 * @param msg
	 * @param data
	 * @author CamusHuang
	 * @creation 2014-4-29 下午4:04:30
	 * </pre>
	 */
	public static void packGardenData(KGameMessage msg, RoleBaseInfo role, KRoleGarden garden, long speedCDReleaseTime) {
		garden.rwLock.lock();
		try {
			msg.writeLong(speedCDReleaseTime / Timer.ONE_SECOND);
			int writeindex = msg.writerIndex();
			msg.writeByte(0);
			int count = 0;
			for (GardenCommonRewardData data : KGardenDataManager.mGardenCommonTreeDataManager.getDataCache().values()) {
				TreeData tree = garden.getTreeData(data.type);
				packTree(msg, tree);
				count++;
			}

			// <类型,<角色等级,数据>>
			int roleLv = role.getLevel();
			for (Map<Integer, GardenTopRewardData> tempMap : KGardenDataManager.mGardenTopTreeDataManager.getDataCache().values()) {
				GardenTopRewardData data = tempMap.get(roleLv);
				TreeData tree = garden.getTreeData(data.type);
				packTree(msg, tree);
				count++;
			}
			msg.setByte(writeindex, count);
		} finally {
			garden.rwLock.unlock();
		}
	}

	public static void packTree(KGameMessage msg, TreeData tree) {
		msg.writeByte(tree.type);
		msg.writeInt(tree.getReleaseTime());
		msg.writeBoolean(tree.isBinZombie());
	}

	public static RewardResult_GardenCollect dealMsg_Collect(KRole role, byte type) {
		RewardResult_GardenCollect result = new RewardResult_GardenCollect();

		KRoleGarden roleData = KGardenRoleExtCACreator.getRoleGarden(role.getId());
		roleData.rwLock.lock();
		try {
			TreeData tree = roleData.getTreeData(type);
			if (tree == null) {
				result.tips = RewardTips.不存在此植物;
				return result;
			}

			int releaseTime = tree.getReleaseTime();
			if (releaseTime > 0) {
				if (tree.type < KGardenDataManager.TYPE_TOP_MIN) {
					result.tips = RewardTips.此植物未成熟;
					return result;
				} else {
					// 高级植物
					int diamondCount = (int) ExpressionForRipeTopTree(type, releaseTime, role.getLevel());
					if (diamondCount > 0) {
						result.diamondCount = diamondCount;
						result.tips = RewardTips.此植物未成熟;
						return result;
					}
					// 若价格<1，则当作成熟
				}
			}

			GardenCommonRewardData commonReward = KGardenDataManager.mGardenCommonTreeDataManager.getData(tree.type);
			if (commonReward != null) {
				// 普通奖励
				long resultMoney = ExpressionForCommonReward(role.getLevel(), commonReward.addMoney);
				KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), commonReward.addMoney.currencyType, resultMoney, PresentPointTypeEnum.庄园收获, true);
				result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, commonReward.addMoney.currencyType.extName, resultMoney));
			} else {
				GardenTopRewardData topReward = KGardenDataManager.mGardenTopTreeDataManager.getData(tree.type, role.getLevel());

				ItemCountStruct addItem = ItemCountStruct.randomItem(topReward.addItems, topReward.addItemRates, topReward.allRate);
				ItemResult_AddItem addItemResult = KSupportFactory.getItemModuleSupport().addItemToBag(role, addItem, "庄园");
				if (!addItemResult.isSucess) {
					result.tips = addItemResult.tips;
					return result;
				}
				result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, addItem.getItemTemplate().extItemName, addItem.itemCount));
			}

			// 重置植物
			tree.reborn();
			//
			result.isSucess = true;
			result.tips = RewardTips.成功领取奖励;
			result.treeData = tree;
			return result;
		} finally {
			roleData.rwLock.unlock();
		}
	}

	public static RewardResult_GardenCollectTop dealMsg_CollectTop(KRole role, byte type, boolean isConfirm) {
		RewardResult_GardenCollectTop result = new RewardResult_GardenCollectTop();

		KRoleGarden roleData = KGardenRoleExtCACreator.getRoleGarden(role.getId());
		roleData.rwLock.lock();
		try {
			if (type < KGardenDataManager.TYPE_TOP_MIN) {
				result.tips = RewardTips.不存在此植物;
				return result;
			}

			TreeData tree = roleData.getTreeData(type);
			if (tree == null) {
				result.tips = RewardTips.不存在此植物;
				return result;
			}

			//
			int releaseTime = tree.getReleaseTime();
			int diamondCount = 0;
			if (releaseTime > 0) {
				// 钻石消耗=Ceiling（剩余时间/该植物配置时间基数，1）*5
				diamondCount = (int) ExpressionForRipeTopTree(type, releaseTime, role.getLevel());
				if (diamondCount > 0) {
					
					if (!isConfirm) {
						result.isGoConfirm = true;
						result.tips = StringUtil.format(RewardTips.是否支付x数量x货币摘取果实, diamondCount, KCurrencyTypeEnum.DIAMOND.extName);
						return result;
					}
					
					// 未成熟，需要付费
					if (0 > KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), KCurrencyTypeEnum.DIAMOND, diamondCount, UsePointFunctionTypeEnum.果实催熟, true)) {
						// 货币不足
						result.isGoMoneyUI = true;
						result.goMoneyUIType = KCurrencyTypeEnum.DIAMOND;
						result.goMoneyUICount = diamondCount - KSupportFactory.getCurrencySupport().getMoney(role.getId(), KCurrencyTypeEnum.DIAMOND);
						result.tips = StringUtil.format(ShopTips.x货币数量不足x, KCurrencyTypeEnum.DIAMOND.extName, diamondCount);
						return result;
					}
					result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, KCurrencyTypeEnum.DIAMOND.extName, diamondCount));
				}
			}
			//
			{
				GardenTopRewardData topReward = KGardenDataManager.mGardenTopTreeDataManager.getData(tree.type, role.getLevel());

				ItemCountStruct addItem = ItemCountStruct.randomItem(topReward.addItems, topReward.addItemRates, topReward.allRate);
				ItemResult_AddItem addItemResult = KSupportFactory.getItemModuleSupport().addItemToBag(role, addItem, "庄园");
				if (!addItemResult.isSucess) {
					if (diamondCount > 0) {
						// 回滚货币
						result.getDataUprisingTips().clear();
						KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), KCurrencyTypeEnum.DIAMOND, diamondCount, PresentPointTypeEnum.回滚, true);
					}
					result.tips = addItemResult.tips;
					return result;
				}
				result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, addItem.getItemTemplate().extItemName, addItem.itemCount));
				result.addItem = addItem;
			}
			// 重置植物
			tree.reborn();
			//
			result.isSucess = true;
			result.tips = RewardTips.成功领取奖励;
			result.treeData = tree;
			return result;
		} finally {
			roleData.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 计算高级植物催熟的价格
	 * 
	 * @param tree
	 * @param roleLv
	 * @param baseMoney
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-29 下午7:32:20
	 * </pre>
	 */
	public static long ExpressionForRipeTopTree(int type, long releaseTime, int roleLv) {
		// 钻石消耗=Ceiling（剩余时间/该植物配置时间基数，1）*5
		GardenTopRewardData data = KGardenDataManager.mGardenTopTreeDataManager.getData(type, roleLv);
		long diamondCount = (long) (Math.ceil(((double) releaseTime) / data.timeBase) * 5);
		return diamondCount;
	}

	/**
	 * <pre>
	 * 计算植物的最终产出
	 * 
	 * @param tree
	 * @param roleLv
	 * @param baseMoney
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-29 下午7:32:20
	 * </pre>
	 */
	public static long ExpressionForCommonReward(int roleLv, KCurrencyCountStruct baseMoney) {
		// 植物最终产量=植物产出资源*玩家对应资源等级系数.
		GardenRoleRateData rateData = KGardenDataManager.mGardenRoleRateDataManager.getData(roleLv);
		float roleLvRate = rateData.moneyRateMap.get(baseMoney.currencyType);
		long result = (long) (baseMoney.currencyCount * roleLvRate);
		return result;
	}

	public static RewardResult_Garden dealMsg_KillZombie(KRole role, long oppRoleId, byte type) {
		RewardResult_Garden result = new RewardResult_Garden();

		KRoleGarden myRoleData = KGardenRoleExtCACreator.getRoleGarden(role.getId());
		int myKillZombieTime = myRoleData.getKillZomCount();

		KRoleGarden oppRoleData = KGardenRoleExtCACreator.getRoleGarden(oppRoleId);
		if(oppRoleData == null){
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			return result;
		}
		oppRoleData.rwLock.lock();
		try {
			TreeData tree = oppRoleData.getTreeData(type);
			if (tree == null) {
				result.tips = RewardTips.不存在此植物;
				return result;
			}

			if (!tree.isBinZombie()) {
				result.treeData = tree;
				result.tips = RewardTips.此植物没有僵尸;
				return result;
			}

			KCurrencyCountStruct addMoney = ExpressionForKillZombie(myKillZombieTime, role.getLevel(), oppRoleId == role.getId());
			// 奖励
			KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), addMoney, PresentPointTypeEnum.庄园赶尸, true);
			result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, addMoney.currencyType.extName, addMoney.currencyCount));
			// 意外奖励
			if (ExpressionForKillZombie(myKillZombieTime)) {
				result.specialRwardItem = KGardenDataManager.KillZombieItemReward;
				RewardResult_SendMail sendMailResult = KGardenDataManager.KillZombieItemRewardMail.sendReward(role, PresentPointTypeEnum.庄园赶尸, true);
				if (sendMailResult.isSendByMail) {
					result.addUprisingTips(RewardTips.背包已满奖励通过邮件发送);
				} else {
					result.addDataUprisingTips(KGardenDataManager.KillZombieItemRewardMail.baseRewardData.dataUprisingTips);
				}
			}

			// 重置植物
			tree.setBinZombie(false);
			if (oppRoleId != role.getId()) {
				String feet = StringUtil.format(RewardTips.x击杀僵尸, role.getExName());
				oppRoleData.addFeet(feet);
			}
			//
			result.isSucess = true;
			result.tips = RewardTips.成功杀死僵尸;
			result.treeData = tree;
			return result;
		} finally {
			oppRoleData.rwLock.unlock();

			if (result.isSucess) {
				myRoleData.increaseKillZomCount();
			}
		}
	}

	/**
	 * <pre>
	 * 计算清理僵尸的最终产出
	 * 
	 * @param tree
	 * @param roleLv
	 * @param baseMoney
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-29 下午7:32:20
	 * </pre>
	 */
	private static KCurrencyCountStruct ExpressionForKillZombie(int time, int roleLv, boolean isMyGarden) {
		// 帮其他玩家进行清理僵尸获得金币=500*玩家等级清理金币系数
		// 自己庄园清理获得金别=1000*玩家等级清理金币系数
		// 每天清理僵尸次数大于20次时，获得的金币奖励减少为1%。
		GardenRoleRateData rateData = KGardenDataManager.mGardenRoleRateDataManager.getData(roleLv);
		long money = (long) ((isMyGarden ? KGardenDataManager.KillZombieMoneyBaseCountForSelf : KGardenDataManager.KillZombieMoneyBaseCountForOther) * rateData.clearpoint);
		if (time > KGardenDataManager.KillZombieMoneyRewardMaxTimePerDay) {
			money *= KGardenDataManager.KillZombieMoneyRewardRate;
		}
		money = Math.max(1, money);
		return new KCurrencyCountStruct(KGardenDataManager.KillZombieRewardMoneyType, money);
	}
	
	/**
	 * <pre>
	 * 是否获得特殊物品奖励
	 * 
	 * @param time
	 * @param isMyGarden
	 * @return
	 * @author CamusHuang
	 * @creation 2014-12-14 下午12:46:01
	 * </pre>
	 */
	private static boolean ExpressionForKillZombie(int time) {
		int rate  = time<=KGardenDataManager.KillZombieItemRewardMaxTimePerDay?KGardenDataManager.KillZombieItemRewardRate:KGardenDataManager.KillZombieItemRewardOtherRate;
		return UtilTool.random(100) <= rate;
	}

	/**
	 * <pre>
	 * 计算浇灌的最终产出
	 * 
	 * @param tree
	 * @param roleLv
	 * @param baseMoney
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-29 下午7:32:20
	 * </pre>
	 */
	private static KCurrencyCountStruct ExpressionForSpeed(int roleLv) {
		// 0001521: 浇灌获得钱币数量公式修改
		// 描述 浇灌获得金币奖励=(300*清理僵尸金币系数)
		GardenRoleRateData rateData = KGardenDataManager.mGardenRoleRateDataManager.getData(roleLv);
		long money = (long) (KGardenDataManager.SpeedRewardMoneyBase * rateData.clearpoint);
		money = Math.max(1, money);
		return new KCurrencyCountStruct(KGardenDataManager.SpeedRewardMoneyType, money);
	}

	public static RewardResult_GardenOneKeyCollect dealMsg_OneKeyCollect(KRole role) {
		RewardResult_GardenOneKeyCollect result = new RewardResult_GardenOneKeyCollect();

		KRoleGarden roleData = KGardenRoleExtCACreator.getRoleGarden(role.getId());
		roleData.rwLock.lock();
		try {

			int roleLv = role.getLevel();

			LinkedHashMap<Integer, GardenCommonRewardData> dataMap = KGardenDataManager.mGardenCommonTreeDataManager.getDataCache();
			for (GardenCommonRewardData commonReward : dataMap.values()) {
				// 普通植物
				TreeData tree = roleData.getTreeData(commonReward.type);

				int releaseTime = tree.getReleaseTime();
				if (releaseTime > 0) {
					continue;
				}

				long resultMoney = ExpressionForCommonReward(roleLv, commonReward.addMoney);
				KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), commonReward.addMoney.currencyType, resultMoney, PresentPointTypeEnum.庄园收获, true);
				// 重置植物
				tree.reborn();

				result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, commonReward.addMoney.currencyType.extName, resultMoney));
				result.collectedCount++;
			}

			// <类型,<角色等级,数据>>
			for (Map<Integer, GardenTopRewardData> tempMap : KGardenDataManager.mGardenTopTreeDataManager.getDataCache().values()) {
				// 高级植物
				GardenTopRewardData topReward = tempMap.get(roleLv);
				TreeData tree = roleData.getTreeData(topReward.type);
				int releaseTime = tree.getReleaseTime();
				if (releaseTime > 0) {
					continue;
				}

				ItemCountStruct addItem = ItemCountStruct.randomItem(topReward.addItems, topReward.addItemRates, topReward.allRate);
				ItemResult_AddItem addItemResult = KSupportFactory.getItemModuleSupport().addItemToBag(role, addItem, "庄园");
				if (!addItemResult.isSucess) {
					result.tips = addItemResult.tips;
					return result;
				}
				// 重置植物
				tree.reborn();
				result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, addItem.getItemTemplate().extItemName, addItem.itemCount));
				result.collectedCount++;
			}

			//
			if (result.collectedCount > 0) {
				result.isSucess = true;
				result.tips = RewardTips.成功领取奖励;
			} else {
				result.isSucess = false;
				result.tips = RewardTips.不存在已成熟的植物;
			}
			return result;
		} finally {
			roleData.rwLock.unlock();
		}
	}

	public static CommonResult_Ext dealMsg_Speed(KRole role, long oppRoleId) {
		CommonResult_Ext result = new CommonResult_Ext();

		KRoleGarden myRoleData = KGardenRoleExtCACreator.getRoleGarden(role.getId());
		int releaseSpeedTime = myRoleData.getReleaseSpeedTime();
		if (releaseSpeedTime < 1) {
			result.tips = RewardTips.今天浇灌次数已用完;
			return result;
		}

		long cdTime = myRoleData.getSpeedCDReleaseTime(oppRoleId);
		if (cdTime > Timer.ONE_MINUTE) {
			result.tips = StringUtil.format(RewardTips.请等候x分钟冷却时间, cdTime / Timer.ONE_MINUTE);
			return result;
		}

		KRoleGarden oppRoleData = KGardenRoleExtCACreator.getRoleGarden(oppRoleId);
		if(oppRoleData == null){
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			return result;
		}
		oppRoleData.rwLock.lock();
		try {
			// 次数上限、CD时间、奖励金币+体力、加速时长
			// 植物全部成熟状态下,无法进行浇灌
			// 脚印中,文字格式”XXX帮你浇灌\击杀僵尸解放植物

			// 找出可浇灌的植物
			List<TreeData> speedTrees = oppRoleData.searchCouldSpeedTrees(true);

			if (speedTrees.isEmpty()) {
				result.tips = RewardTips.没有需要浇灌的植物;
				return result;
			}

			// 执行浇灌
			for (TreeData tree : speedTrees) {
				TreeRipeTimeData data = KGardenDataManager.mTreeRipeTimeDataManager.getData(tree.type);
				tree.speedRipe(data.speedTime);
			}

			if (oppRoleId != role.getId()) {
				String feet = StringUtil.format(RewardTips.x帮你浇灌, role.getExName());
				oppRoleData.addFeet(feet);
			}

			result.isSucess = true;
			result.tips = RewardTips.成功浇灌;
			return result;
		} finally {
			oppRoleData.rwLock.unlock();

			if (result.isSucess) {
				// 次数++
				myRoleData.increaseSpeedTime();
				// 记录CD
				long nowTime = System.currentTimeMillis();
				if (oppRoleId == role.getId()) {
					myRoleData.recordSpeedCDEndTime(oppRoleId, nowTime + KGardenDataManager.SpeedForMySelfCD);
				} else {
					myRoleData.recordSpeedCDEndTime(oppRoleId, nowTime + KGardenDataManager.SpeedForOtherCD);
				}

				// 奖励
				{
					KCurrencyCountStruct addMoney = ExpressionForSpeed(role.getLevel());
					KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), addMoney, PresentPointTypeEnum.庄园浇灌, true);
					result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, addMoney.currencyType.extName, addMoney.currencyCount));
					//
					KSupportFactory.getRoleModuleSupport().addPhyPower(role.getId(), KGardenDataManager.SpeedRewardPhyPower, true, "庄园浇水");
					result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, KGameAttrType.PHY_POWER.getExtName(), KGardenDataManager.SpeedRewardPhyPower));
				}
			}
		}
	}

	public static CommonResult speedByGM(KRole role, int treeId, int minute) {
		KRoleGarden roleData = KGardenRoleExtCACreator.getRoleGarden(role.getId());
		CommonResult result = roleData.speedByGM(treeId, minute);
		KGardenSynMsg.sendMyGardenData(role);
		return result;
	}

	public static CommonResult_Ext dealMsg_GetVipSaveDesc(KRole role) {
		CommonResult_Ext result = new CommonResult_Ext();

		KRoleGarden roleData = KGardenRoleExtCACreator.getRoleGarden(role.getId());
		roleData.rwLock.lock();
		try {
			List<VIPSaveData> vipdatas = roleData.getVIPSaveDataManager().getVIPSaveDataCache();
			if (vipdatas.isEmpty()) {
				result.tips = RewardTips.VIP存储没有任何收成;
				return result;
			}

			StringBuffer sbf = new StringBuffer();
			{

				int vipLv = KSupportFactory.getVIPModuleSupport().getVipLv(role.getId());
				long offlineTime = role.getLastJoinGameTime() - role.getLastLeaveGameTime();
				String offlineTimeStr = UtilTool.genReleaseCDTimeString(offlineTime);
				String tempTips = StringUtil.format(RewardTips.亲爱的VIPx您离线x时间VIP储存帮您自动收获了, vipLv, offlineTimeStr);
				sbf.append(tempTips).append('\n').append('\n');
			}
			// 先提取货币
			{
				List<KCurrencyCountStruct> moneys = new ArrayList<KCurrencyCountStruct>();
				for (Iterator<VIPSaveData> it = vipdatas.iterator(); it.hasNext();) {
					VIPSaveData data = it.next();
					if (data.addMoney != null) {
						moneys.add(data.addMoney);
					}
				}

				if (!moneys.isEmpty()) {
					StringBuffer sbfmoney = new StringBuffer();

					// 合并货币
					moneys = KCurrencyCountStruct.mergeCurrencyCountStructs(moneys);
					for (KCurrencyCountStruct struct : moneys) {
						sbfmoney.append(StringUtil.format(ShopTips.xxx, struct.currencyType.extName, struct.currencyCount)).append(GlobalTips.顿号);
					}

					if (sbfmoney.length() > 0) {
						sbfmoney.deleteCharAt(sbfmoney.length() - 1);
						sbf.append(sbfmoney.toString()).append('\n');
					}
				}

			}
			//
			// 提取物品
			{

				List<ItemCountStruct> items = new ArrayList<ItemCountStruct>();
				for (Iterator<VIPSaveData> it = vipdatas.iterator(); it.hasNext();) {
					VIPSaveData data = it.next();
					if (data.addItem != null) {
						items.add(data.addItem);
					}
				}

				if (!items.isEmpty()) {
					StringBuffer sbfitem = new StringBuffer();

					// 合并货币
					items = ItemCountStruct.mergeItemCountStructs(items);
					for (ItemCountStruct struct : items) {
						sbfitem.append(StringUtil.format(ShopTips.xxx, struct.getItemTemplate().extItemName, struct.itemCount)).append('、');
					}

					if (sbfitem.length() > 0) {
						sbfitem.deleteCharAt(sbfitem.length() - 1);
						sbf.append(sbfitem.toString()).append('\n');
					}
				}

			}
			if (sbf.length() > 0) {
				sbf.deleteCharAt(sbf.length() - 1);
			}

			result.isSucess = true;
			result.tips = sbf.toString();
			return result;
		} finally {
			roleData.rwLock.unlock();
		}
	}

	public static CommonResult_Ext dealMsg_GetVipSave(KRole role) {
		CommonResult_Ext result = new CommonResult_Ext();

		KRoleGarden roleData = KGardenRoleExtCACreator.getRoleGarden(role.getId());
		roleData.rwLock.lock();
		try {
			List<VIPSaveData> vipdatas = roleData.getVIPSaveDataManager().getVIPSaveDataCache();
			if (vipdatas.isEmpty()) {
				result.tips = RewardTips.VIP存储没有任何收成;
				return result;
			}

			boolean isGetSome = false;
			// 先提取货币
			{
				List<KCurrencyCountStruct> moneys = new ArrayList<KCurrencyCountStruct>();
				for (Iterator<VIPSaveData> it = vipdatas.iterator(); it.hasNext();) {
					VIPSaveData data = it.next();
					if (data.addMoney != null) {
						moneys.add(data.addMoney);
						it.remove();
						isGetSome = true;
					}
				}

				if (!moneys.isEmpty()) {

					// 合并货币
					moneys = KCurrencyCountStruct.mergeCurrencyCountStructs(moneys);
					KSupportFactory.getCurrencySupport().increaseMoneys(role.getId(), moneys, PresentPointTypeEnum.庄园收获, true);
					for (KCurrencyCountStruct struct : moneys) {
						result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, struct.currencyType.extName, struct.currencyCount));
					}
				}

			}
			//
			// 提取物品
			{
				for (Iterator<VIPSaveData> it = vipdatas.iterator(); it.hasNext();) {
					VIPSaveData data = it.next();
					if (data.addItem != null) {
						ItemResult_AddItem addItemResult = KSupportFactory.getItemModuleSupport().addItemToBag(role, data.addItem, "庄园");
						if (!addItemResult.isSucess) {
							result.tips = addItemResult.tips;
							break;
						}
						result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, data.addItem.getItemTemplate().extItemName, data.addItem.itemCount));
						it.remove();
						isGetSome = true;
					}
				}
			}

			result.isSucess = isGetSome;
			return result;
		} finally {
			roleData.rwLock.unlock();
			if (result.isSucess) {
				roleData.notifyUpdate();
			}
		}
	}

	static void notifyForZombieRefreshTask() {
		List<TreeData> synDatas = new ArrayList<KRoleGarden.TreeData>();
		long nowTime = System.currentTimeMillis();

		// 遍历在线角色
		for (long roleId : KSupportFactory.getRoleModuleSupport().getAllOnLineRoleIds()) {
			//
			KRoleGarden garden = KGardenRoleExtCACreator.getRoleGarden(roleId);
			if (garden == null) {
				continue;
			}
			
			//
			garden.notifyForZombieRefresh(synDatas, nowTime);

			if (!synDatas.isEmpty()) {
				KRole role = KSupportFactory.getRoleModuleSupport().getRole(garden.getRoleId());
				KGardenSynMsg.sendTreeDatas(role, garden.getRoleId(), synDatas);
				synDatas.clear();
			}
		}
	}

	public static void packFriendSates(KRole role, KGameMessage backmsg) {
		List<Long> roleIds = KSupportFactory.getRelationShipModuleSupport().getAllFriends(role.getId());
		List<Long> zombieIds = new ArrayList<Long>();
		List<Long> speedIds = new ArrayList<Long>();

		KRoleGarden myRoleData = KGardenRoleExtCACreator.getRoleGarden(role.getId());

		long nowTime = System.currentTimeMillis();
		RoleModuleSupport roleSupport = KSupportFactory.getRoleModuleSupport();
		for (long oppRoleId : roleIds) {
			KRoleGarden oppRoleData = KGardenRoleExtCACreator.getRoleGarden(oppRoleId);
			if (oppRoleData == null) {
				continue;
			}

			// 检查是否在自己的CD中
			boolean isSpeedCD = myRoleData.getSpeedCDReleaseTime(oppRoleId) > 0;

			oppRoleData.rwLock.lock();
			try {
				KRole oppRole = roleSupport.getRole(oppRoleId);
				if (oppRole == null) {
					continue;
				}

				if (!oppRole.isOnline()) {
					oppRoleData.autoRefreshZombieAndCollectForVIP(oppRole, nowTime);
				}

				if (oppRoleData.isContainZombies()) {
					zombieIds.add(oppRoleId);
				}

				if (isSpeedCD) {
					continue;
				}

				// 找出可浇灌的植物
				List<TreeData> speedTrees = oppRoleData.searchCouldSpeedTrees(false);
				if (speedTrees.isEmpty()) {
					continue;
				}

				speedIds.add(oppRoleId);
			} finally {
				oppRoleData.rwLock.unlock();
			}
		}

		backmsg.writeShort(zombieIds.size());
		for (long roleId : zombieIds) {
			backmsg.writeLong(roleId);
		}
		backmsg.writeShort(speedIds.size());
		for (long roleId : speedIds) {
			backmsg.writeLong(roleId);
		}
	}
}
