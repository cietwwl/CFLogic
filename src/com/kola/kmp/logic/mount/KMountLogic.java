package com.kola.kmp.logic.mount;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.koala.game.logging.KGameLogger;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kmp.logic.flow.FlowManager;
import com.kola.kmp.logic.flow.FlowManager.PropertyTypeEnum;
import com.kola.kmp.logic.mount.KMount.KMountSkill;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountEquiTemp;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountLv;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountResetSPData;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountTemplate;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountUpBigLvData;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountUpLvData;
import com.kola.kmp.logic.mount.message.KPushMountMsg;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KLimitTimeProduceActivityTypeEnum;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.TimeLimitActivityDataManager.TimeLimieProduceActivity;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRoleIniSkillTemp;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRoleSkillTempAbs.SkillTempLevelData;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.KGameUtilTool;
import com.kola.kmp.logic.util.ResultStructs.MountResult_BuildEqui;
import com.kola.kmp.logic.util.ResultStructs.MountResult_ResetSP;
import com.kola.kmp.logic.util.ResultStructs.MountResult_UpBigLv;
import com.kola.kmp.logic.util.ResultStructs.MountResult_UpLv;
import com.kola.kmp.logic.util.ResultStructs.MountResult_UpLvSkill;
import com.kola.kmp.logic.util.ResultStructs.MountResult_Use;
import com.kola.kmp.logic.util.tips.ItemTips;
import com.kola.kmp.logic.util.tips.MountTips;
import com.kola.kmp.logic.util.tips.ShopTips;
import com.kola.kmp.logic.util.tips.SkillTips;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2014-3-15 上午11:58:28
 * </pre>
 */
public class KMountLogic {

	private static final KGameLogger _LOGGER = KGameLogger.getLogger(KMountLogic.class);

	/**
	 * <pre>
	 * 玩家升级赠送机甲
	 * 
	 * @param role
	 * @param preLv
	 * @author CamusHuang
	 * @creation 2015-1-8 下午4:43:06
	 * </pre>
	 */
	public static void presentMountForLv(KRole role) {

		List<KMount> newMountList = new ArrayList<KMount>();
		KMount useNewMount = null;
		//
		KMountSet set = KMountModuleExtension.getMountSet(role.getId());
		set.rwLock.lock();
		try {
			int roleLv = role.getLevel();
			for (LinkedHashMap<Integer, KMountTemplate> map : KMountDataManager.mMountTemplateManager.getDataCache().values()) {
				KMountTemplate temp = map.get(1);
				if (temp.openlv > 0 && roleLv >= temp.openlv) {
					//
					KMount newMount = presentMount(role, temp, set, newMountList, "升级开放");
					if (newMount != null && newMount.isUsed()) {
						useNewMount = newMount;
					}
				}
			}
		} finally {
			set.rwLock.unlock();
		}

		if (newMountList != null && role.isOnline()) {
			// 同步新机甲到客户端
			KPushMountMsg.SM_SYN_MOUNT(role, newMountList);
			// 刷新角色属性
			KSupportFactory.getRoleModuleSupport().notifyEffectAttrChange(role.getId(), KMountAttributeProvider.getType());
			// 通知地图模块
			if (useNewMount != null) {
				KSupportFactory.getMapSupport().notifyMountStatus(role.getId(), true, useNewMount.getInMapResId());
			}
		}
	}

	public static KActionResult<KMount> presentMount(KRole role, KMountTemplate temp, String sourceTips) {
		KActionResult<KMount> result = new KActionResult<KMount>();
		if (temp == null) {
			result.tips = MountTips.机甲不存在;
			return result;
		}
		//
		List<KMount> newMountList = new ArrayList<KMount>();
		KMount useNewMount = null;
		//
		KMountSet set = KMountModuleExtension.getMountSet(role.getId());
		set.rwLock.lock();
		try {
			KMount newMount = presentMount(role, temp, set, newMountList, sourceTips);
			if (newMount == null) {
				result.tips = MountTips.你已拥有此机甲;
				return result;
			}
			result.attachment = newMount;
			if (newMount.isUsed()) {
				useNewMount = newMount;
			}

			result.success = true;
			return result;
		} finally {
			set.rwLock.unlock();

			if (newMountList != null && role.isOnline()) {
				// 同步新机甲到客户端
				KPushMountMsg.SM_SYN_MOUNT(role, newMountList);
				// 刷新角色属性
				KSupportFactory.getRoleModuleSupport().notifyEffectAttrChange(role.getId(), KMountAttributeProvider.getType());
				// 通知地图模块
				if (useNewMount != null) {
					KSupportFactory.getMapSupport().notifyMountStatus(role.getId(), true, useNewMount.getInMapResId());
				}
			}
		}
	}

	private static KMount presentMount(KRole role, KMountTemplate temp, KMountSet set, List<KMount> newMountList, String sourceTips) {
		KMount newMount = set.getMountByModel(temp.Model);
		if (newMount != null) {
			// 已有此机甲
			return null;
		}
		{
			// 没有则新增
			newMount = new KMount(set, temp);
			set.addMount(newMount);
			//
			if (set.getUsedModelId() < 1) {
				// 若没有机甲使用中，则设为使用
				set.setUsedModelId(temp.Model);
			}
			//
			newMountList.add(newMount);
			//
			// 财产日志
			FlowManager.logPropertyAddOrDelete(role.getId(), PropertyTypeEnum.坐驾, newMount._uuid, temp.mountsID, temp.Name, true, sourceTips);
			//
			return newMount;
		}
	}

	public static void giveMountToNewRole(KRole role) {
		// 给新角色一个用于体验的临时机甲
		KMountTemplate temp = KMountDataManager.mMountTemplateManager.getMountForNewRole();
		if (temp == null) {
			return;
		}

		KActionResult<KMount> result = new KActionResult<KMount>();
		//
		List<KMount> newMountList = new ArrayList<KMount>();
		KMount useNewMount = null;
		//
		KMountSet set = KMountModuleExtension.getMountSet(role.getId());
		set.rwLock.lock();
		try {
			KMount newMount = presentMount(role, temp, set, newMountList, "新手引导");
			if (newMount == null) {
				result.tips = MountTips.你已拥有此机甲;
				return;
			}
			result.attachment = newMount;
			if (newMount.isUsed()) {
				useNewMount = newMount;
			}

			result.success = true;
			return;
		} finally {
			set.rwLock.unlock();

			if (newMountList != null && role.isOnline()) {
				// 同步新机甲到客户端
				KPushMountMsg.SM_SYN_MOUNT(role, newMountList);
				if (useNewMount != null) {
//					// 刷新角色属性
//					KSupportFactory.getRoleModuleSupport().notifyEffectAttrChange(role.getId(), KMountAttributeProvider.getType());
//					// 通知地图模块
//					KSupportFactory.getMapSupport().notifyMountStatus(role.getId(), true, useNewMount.getInMapResId());
				}
			}
		}
	}

	public static void cancelMountFromNewRole(KRole role) {
		// 取消新手机甲
		KMountTemplate temp = KMountDataManager.mMountTemplateManager.getMountForNewRole();
		if (temp == null) {
			return;
		}

		boolean isDeleteUsedMount = false;
		boolean isChange = false;
		KMountSet set = KMountModuleExtension.getMountSet(role.getId());
		set.rwLock.lock();
		try {
			// 搜索
			List<KMount> deleteMounts = new ArrayList<KMount>();
			for (KMount mount : set.getMountCache().values()) {
				if (mount.getTemplateId() == temp.mountsID) {
					deleteMounts.add(mount);
					if (mount.isUsed()) {
						isDeleteUsedMount = true;
					}
				}
			}

			// 删除
			for (KMount mount : deleteMounts) {
				set.notifyElementDelete(mount._id);
				isChange = true;
				// 财产日志
				FlowManager.logPropertyAddOrDelete(role.getId(), PropertyTypeEnum.坐驾, mount._uuid, temp.mountsID, temp.Name, false, "删除新手引导机甲");
			}
		} finally {
			set.rwLock.unlock();

			if (isChange && role.isOnline()) {
				// 同步新机甲到客户端
				KPushMountMsg.SM_PUSH_MOUNTDATA(role);
				if (isDeleteUsedMount) {
					// 刷新角色属性
					KSupportFactory.getRoleModuleSupport().notifyEffectAttrChange(role.getId(), KMountAttributeProvider.getType());
					// 通知地图模块
					KSupportFactory.getMapSupport().notifyMountStatus(role.getId(), false, -1);
				}
			}
		}
	}

	public static MountResult_Use dealMsg_useMountInMap(KRole role, int modelId, boolean isUse) {
		MountResult_Use result = new MountResult_Use();
		//
		KMountSet set = KMountModuleExtension.getMountSet(role.getId());
		set.rwLock.lock();
		try {
			KMount mount = set.getMountByModel(modelId);
			if (mount == null) {
				result.tips = MountTips.机甲不存在;
				return result;
			}
			result.mountTemplate = mount.getTemplate();

			if (isUse) {
				if (set.getUsedModelId() == modelId) {
					result.isSucess = true;
					result.tips = MountTips.骑乘成功;
					return result;
				} else {
					set.setUsedModelId(modelId);
					result.isSucess = true;
					result.tips = MountTips.骑乘成功;
					return result;
				}
			} else {
				set.setUsedModelId(-1);
				result.isSucess = true;
				result.tips = MountTips.取消骑乘成功;
				return result;
			}
		} finally {
			set.rwLock.unlock();
		}
	}

	public static MountResult_UpLv dealMsg_uplvMount(KRole role, int modelId, String payItemCode, boolean isAutoBuy) {
		MountResult_UpLv result = new MountResult_UpLv();
		//
		// 限时活动：材料打折
		int discountForItem = 0;
		{
			TimeLimieProduceActivity activity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.材料打折);
			if (activity != null && activity.isActivityTakeEffectNow()) {
				if (activity.discountItemCodeSet.contains(payItemCode)) {
					discountForItem = activity.discount;
				}
			}
		}

		KMountSet set = KMountModuleExtension.getMountSet(role.getId());
		set.rwLock.lock();
		try {

			KMount mount = set.getMountByModel(modelId);
			if (mount == null) {
				result.tips = MountTips.机甲不存在;
				return result;
			}
			result.mount = mount;
			result.oldLv = mount.getLevel();

			AtomicInteger addExps = new AtomicInteger();
			List<KCurrencyCountStruct> payMoneys = new ArrayList<KCurrencyCountStruct>();// 金币
			List<ItemCountStruct> payItems = new ArrayList<ItemCountStruct>();// 材料
			List<KCurrencyCountStruct> payMoneyForItem = new ArrayList<KCurrencyCountStruct>();// 购买材料原价
			List<KCurrencyCountStruct> payMoneyForItemWithDiscount = new ArrayList<KCurrencyCountStruct>();// 购买材料折后价

			//
			result = uplvMountIn(role, mount, payItemCode, isAutoBuy, result, payMoneys, payItems, payMoneyForItem, payMoneyForItemWithDiscount, addExps, discountForItem, false);

			{
				// 升级经验限时活动
				int successTime = result.isSucess ? 1 : 0;
				uplvTimeLimitActivity(addExps, mount, successTime, result);
			}

			{
				payItems = ItemCountStruct.mergeItemCountStructs(payItems);
				for (ItemCountStruct payItem : payItems) {
					result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, payItem.getItemTemplate().extItemName, payItem.itemCount));
				}

				// 金币
				payMoneys = KCurrencyCountStruct.mergeCurrencyCountStructs(payMoneys);
				for (KCurrencyCountStruct struct : payMoneys) {
					result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, struct.currencyType.extName, struct.currencyCount));
				}

				if (payMoneyForItemWithDiscount.isEmpty()) {
					// 无打折
					payMoneyForItem = KCurrencyCountStruct.mergeCurrencyCountStructs(payMoneyForItem);
					for (KCurrencyCountStruct struct : payMoneyForItem) {
						result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, struct.currencyType.extName, struct.currencyCount));
					}
				} else {
					// 有打折
					payMoneyForItem = KCurrencyCountStruct.mergeCurrencyCountStructs(payMoneyForItem);
					KCurrencyCountStruct orgMoney = payMoneyForItem.get(0);
					payMoneyForItemWithDiscount = KCurrencyCountStruct.mergeCurrencyCountStructs(payMoneyForItemWithDiscount);
					KCurrencyCountStruct orgMoneyWithDiscount = payMoneyForItemWithDiscount.get(0);
					// 打折
					result.addUprisingTips(StringUtil.format(MountTips.升级材料打x折原价x数量x货币现价x数量x货币, discountForItem / 10.0, orgMoney.currencyCount, orgMoney.currencyType.extName,
							orgMoneyWithDiscount.currencyCount, orgMoneyWithDiscount.currencyType.extName));// 购买成功，打85折，原价100钻石，现价85钻石
				}
			}

			KMountTemplate temp = mount.getTemplate();
			if (result.isLvUp) {
				// 财产日志
				FlowManager
						.logPropertyModify(role.getId(), PropertyTypeEnum.坐驾, mount._uuid, temp.mountsID, temp.Name, "经验加:" + addExps.get() + ";达到:" + mount.getExp() + ";升级到:" + result.newLv + "级");
			} else {
				// 财产日志
				FlowManager.logPropertyModify(role.getId(), PropertyTypeEnum.坐驾, mount._uuid, temp.mountsID, temp.Name, "经验加:" + addExps.get() + ";达到:" + mount.getExp());
			}

			result.newLv = mount.getLevel();
			return result;
		} finally {
			set.rwLock.unlock();
		}
	}

	private static void uplvTimeLimitActivity(AtomicInteger addExps, KMount mount, int successTime, MountResult_UpLv result) {
		// 升级经验限时活动
		if (addExps.get() > 0) {

			KMountTemplate temp = mount.getTemplate();
			KMountUpBigLvData mKMountUpBigLvData = KMountDataManager.mMountUpBigLvDataManager.getData(temp.bigLv);
			if (mount.getLevel() >= mKMountUpBigLvData.lv) {
				// 等级已经达到本阶上限
				return;
			}

			TimeLimieProduceActivity activity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.机甲升级经验倍率);
			if (activity != null && activity.isActivityTakeEffectNow()) {
				if (successTime >= activity.activity15_MIN) {
					int presentExp = (int) (addExps.get() * (activity.expRate - 1));
					if (presentExp > 0) {
						// 给进阶经验
						int newExp = mount.getExp() + presentExp;
						mount.setExp(newExp);

						result.addUprisingTips(StringUtil.format(MountTips.升级经验限时活动额外获得x经验, presentExp));

						// 是否能升级
						KMountLv mKMountLv = KMountDataManager.mMountLvDataManager.getData(mount.getLevel());
						if (newExp >= mKMountLv.exp) {
							mount.upLv(mount.getLevel() + 1, newExp - mKMountLv.exp);
							//
							result.isLvUp = true;
							result.addUprisingTips(MountTips.恭喜你机甲升级成功);
						}
					}
				}
			}
		}
	}

	private static MountResult_UpLv uplvMountIn(KRole role, KMount mount, String payItemCode, boolean isAutoBuy, MountResult_UpLv result, List<KCurrencyCountStruct> payMoneys,
			List<ItemCountStruct> payItems, List<KCurrencyCountStruct> payMoneyForItem, List<KCurrencyCountStruct> payMoneyForItemWithDiscount, AtomicInteger addExps, int discountForItem,
			boolean isExcuteCommon) {
		if (result == null) {
			result = new MountResult_UpLv();
		}
		//
		KMountTemplate temp = mount.getTemplate();
		KMountUpBigLvData mKMountUpBigLvData = KMountDataManager.mMountUpBigLvDataManager.getData(temp.bigLv);
		if (mount.getLevel() >= mKMountUpBigLvData.lv) {
			// 等级已经达到本阶上限
			if (temp.bigLv >= KMountDataManager.mMountUpBigLvDataManager.getMaxBigLv()) {
				result.tips = MountTips.机甲已达系统最大等级;
				return result;
			} else {
				result.tips = MountTips.机甲已达本阶最大等级请进阶后再来;
				return result;
			}
		}

		KMountUpLvData dataForMetrial = KMountDataManager.mMountUpLvDataManager.getData(payItemCode);
		if (dataForMetrial == null) {
			result.tips = MountTips.不能使用此材料进行机甲升级;
			return result;
		}

		KCurrencyCountStruct tempPayMoneys = null;// 应该支付的货币
		ItemCountStruct tempPayItems = null;// 应该扣取的材料
		KCurrencyCountStruct tempPayMoneyForItem = null;// 应该支付的材料价格
		KCurrencyCountStruct tempPayMoneyForItemWithDiscount = null;// 打折后应该支付的材料价格
		{
			KSupportFactory.getItemModuleSupport().lockItemSet(role.getId());
			try {
				long itemCount = KSupportFactory.getItemModuleSupport().checkItemCountInBag(role.getId(), dataForMetrial.itemTempId);
				if (dataForMetrial.itemStruct.itemCount > itemCount) {
					if (!isAutoBuy) {
						result.tips = ItemTips.物品数量不足;
						return result;
					}
					if (itemCount != 0) {
						tempPayItems = new ItemCountStruct(dataForMetrial.itemStruct.getItemTemplate(), itemCount);
					}
					// 应该为自动购买材料支付的货币
					KCurrencyCountStruct moneyForItem = dataForMetrial.itemStruct.getItemTemplate().buyMoney;
					tempPayMoneyForItem = new KCurrencyCountStruct(moneyForItem.currencyType, moneyForItem.currencyCount * (dataForMetrial.itemStruct.itemCount - itemCount));
				} else {
					tempPayItems = dataForMetrial.itemStruct;
				}

				tempPayMoneys = dataForMetrial.moneyStruct;

				// 扣货币
				{
					List<KCurrencyCountStruct> allPayMoneys = new ArrayList<KCurrencyCountStruct>();
					if (tempPayMoneys != null) {
						allPayMoneys.add(tempPayMoneys);
					}
					if (tempPayMoneyForItem != null) {
						if (discountForItem > 0) {
							tempPayMoneyForItemWithDiscount = new KCurrencyCountStruct(tempPayMoneyForItem.currencyType, tempPayMoneyForItem.currencyCount * discountForItem / 100);
							allPayMoneys.add(tempPayMoneyForItemWithDiscount);
						} else {
							allPayMoneys.add(tempPayMoneyForItem);
						}
					}
					// 合并货币
					allPayMoneys = KCurrencyCountStruct.mergeCurrencyCountStructs(allPayMoneys);

					if (!allPayMoneys.isEmpty()) {
						KCurrencyCountStruct moneyResult = KSupportFactory.getCurrencySupport().decreaseMoneys(role.getId(), allPayMoneys, UsePointFunctionTypeEnum.机甲升级, true);
						if (moneyResult != null) {
							result.isGoMoneyUI = true;
							result.goMoneyUIType = moneyResult.currencyType;
							result.goMoneyUICount = moneyResult.currencyCount - KSupportFactory.getCurrencySupport().getMoney(role.getId(), moneyResult.currencyType);
							result.tips = StringUtil.format(ShopTips.x货币数量不足x, moneyResult.currencyType.extName, moneyResult.currencyCount);
							return result;
						}
					}
				}

				// 扣道具
				if (tempPayItems != null) {
					boolean itemResult = KSupportFactory.getItemModuleSupport().removeItemFromBag(role.getId(), tempPayItems.itemCode, tempPayItems.itemCount);
					if (!itemResult) {
						result.tips = ItemTips.物品数量不足;
						return result;
					}
				}
			} finally {
				KSupportFactory.getItemModuleSupport().unlockItemSet(role.getId());
			}
		}

		// 给进阶经验
		int addExp = dataForMetrial.addExp;
		int newExp = mount.getExp() + addExp;
		mount.setExp(newExp);
		addExps.addAndGet(addExp);

		result.isSucess = true;
		result.tips = StringUtil.format(MountTips.注入成功机甲获得x经验, addExp);
		// 是否能升级
		KMountLv mKMountLv = KMountDataManager.mMountLvDataManager.getData(mount.getLevel());
		if (newExp >= mKMountLv.exp) {
			mount.upLv(mount.getLevel() + 1, newExp - mKMountLv.exp);
			//
			result.isLvUp = true;
			result.addUprisingTips(MountTips.恭喜你机甲升级成功);
		}

		if (tempPayMoneys != null) {
			payMoneys.add(tempPayMoneys);
		}
		if (tempPayItems != null) {
			payItems.add(tempPayItems);
		}
		if (tempPayMoneyForItem != null) {
			payMoneyForItem.add(tempPayMoneyForItem);
		}
		if (tempPayMoneyForItemWithDiscount != null) {
			payMoneyForItemWithDiscount.add(tempPayMoneyForItemWithDiscount);
		}

		return result;
	}

	public static MountResult_BuildEqui dealMsg_buildEqui(KRole role, int modelId, int equiId) {
		MountResult_BuildEqui result = new MountResult_BuildEqui();
		//
		KMountSet set = KMountModuleExtension.getMountSet(role.getId());
		set.rwLock.lock();
		try {

			KMount mount = set.getMountByModel(modelId);
			if (mount == null) {
				result.tips = MountTips.机甲不存在;
				return result;
			}
			KMountTemplate temp = mount.getTemplate();
			if (temp == null) {
				result.tips = MountTips.机甲不存在;
				return result;
			}

			result.mount = mount;
			result.isMountInUsed = set.getUsedModelId() == modelId;
			//
			if (!temp.equiIdSet.contains(equiId)) {
				result.tips = MountTips.你还不能打造此装备;
				return result;
			}
			
			if (mount.checkEqui(equiId)) {
				result.tips = MountTips.你还不能打造此装备;
				return result;
//				result.tips = MountTips.你已拥有此装备;
//				return result;
			}

			KMountEquiTemp equiTemp = KMountDataManager.mMountEquiDataManager.getData(equiId);

			{
				KSupportFactory.getItemModuleSupport().lockItemSet(role.getId());
				try {

					if (!equiTemp.itemList.isEmpty()) {
						String itemCode = KSupportFactory.getItemModuleSupport().checkItemCountInBag(role.getId(), equiTemp.itemCountMap);
						if (itemCode != null) {
							ItemCountStruct itemStruct = equiTemp.itemMap.get(itemCode);
							result.tips = StringUtil.format(ItemTips.x物品数量不足x, itemStruct.getItemTemplate().extItemName, itemStruct.itemCount);
							return result;
						}
					}

					// 扣货币
					{
						if (!equiTemp.moneyList.isEmpty()) {
							KCurrencyCountStruct moneyResult = KSupportFactory.getCurrencySupport().decreaseMoneys(role.getId(), equiTemp.moneyList, UsePointFunctionTypeEnum.机甲装备, true);
							if (moneyResult != null) {
								result.isGoMoneyUI = true;
								result.goMoneyUIType = moneyResult.currencyType;
								result.goMoneyUICount = moneyResult.currencyCount - KSupportFactory.getCurrencySupport().getMoney(role.getId(), moneyResult.currencyType);
								result.tips = StringUtil.format(ShopTips.x货币数量不足x, moneyResult.currencyType.extName, moneyResult.currencyCount);
								return result;
							}
						}
					}

					// 扣道具
					if (!equiTemp.itemList.isEmpty()) {
						for (ItemCountStruct s : equiTemp.itemList) {
							boolean itemResult = KSupportFactory.getItemModuleSupport().removeItemFromBag(role.getId(), s.itemCode, s.itemCount);
							if (!itemResult) {
								result.tips = ItemTips.物品数量不足;
								return result;
							}
						}
					}
				} finally {
					KSupportFactory.getItemModuleSupport().unlockItemSet(role.getId());
				}
			}

			{
				int equiIndex = temp.equiIdList.indexOf(equiId);
				int oldEquiId = -1;
				KMountTemplate frontTemp = KMountDataManager.mMountTemplateManager.getTemplateByLv(temp.Model, temp.bigLv - 1);
				if (frontTemp != null && frontTemp.equiIdList.size() > equiIndex) {
					oldEquiId = frontTemp.equiIdList.get(equiIndex);
				}
				mount.upLvEqui(oldEquiId, equiId);
			}

			{
				for (ItemCountStruct payItem : equiTemp.itemList) {
					result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, payItem.getItemTemplate().extItemName, payItem.itemCount));
				}

				for (KCurrencyCountStruct struct : equiTemp.moneyList) {
					result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, struct.currencyType.extName, struct.currencyCount));
				}
			}

			// 财产日志
			FlowManager.logPropertyModify(role.getId(), PropertyTypeEnum.坐驾, mount._uuid, temp.mountsID, temp.Name, "打造装备ID:" + equiId);

			result.isSucess = true;
			result.tips = MountTips.成功打造装备;
			return result;
		} finally {
			set.rwLock.unlock();
		}
	}

	public static MountResult_UpBigLv dealMsg_upBigLvMount(KRole role, int modelId) {
		MountResult_UpBigLv result = new MountResult_UpBigLv();
		//
		KMountSet set = KMountModuleExtension.getMountSet(role.getId());
		set.rwLock.lock();
		try {

			KMount mount = set.getMountByModel(modelId);
			if (mount == null) {
				result.tips = MountTips.机甲不存在;
				return result;
			}
			KMountTemplate temp = mount.getTemplate();
			if (temp == null) {
				result.tips = MountTips.机甲不存在;
				return result;
			}

			result.mount = mount;
			result.isMountInUsed = set.getUsedModelId() == modelId;
			result.oldBigLv = temp.bigLv;

			KMountTemplate nextTemp = KMountDataManager.mMountTemplateManager.getTemplateByLv(temp.Model, temp.bigLv + 1);
			if (nextTemp == null) {
				result.tips = MountTips.机甲已达系统最大阶级;
				return result;
			}
			//
			// 等级、装备、货币
			KMountUpBigLvData mKMountUpBigLvData = KMountDataManager.mMountUpBigLvDataManager.getData(temp.bigLv);
			if (mount.getLevel() < mKMountUpBigLvData.lv) {
				result.tips = StringUtil.format(MountTips.请先把机甲升级到x级, mKMountUpBigLvData.lv);
				return result;
			}

			for (int equId : temp.equiIdList) {
				if (!mount.checkEqui(equId)) {
					KMountEquiTemp equiTemp = KMountDataManager.mMountEquiDataManager.getData(equId);
					result.tips = StringUtil.format(MountTips.请先打造装备x, equiTemp.name);
					return result;
				}
			}

			if (!mKMountUpBigLvData.moneyList.isEmpty()) {
				// 扣货币
				KCurrencyCountStruct moneyResult = KSupportFactory.getCurrencySupport().decreaseMoneys(role.getId(), mKMountUpBigLvData.moneyList, UsePointFunctionTypeEnum.机甲进阶, true);
				if (moneyResult != null) {
					result.isGoMoneyUI = true;
					result.goMoneyUIType = moneyResult.currencyType;
					result.goMoneyUICount = moneyResult.currencyCount - KSupportFactory.getCurrencySupport().getMoney(role.getId(), moneyResult.currencyType);
					result.tips = StringUtil.format(ShopTips.x货币数量不足x, moneyResult.currencyType.extName, moneyResult.currencyCount);
					return result;
				}
			}

			mount.upBigLv(nextTemp);

			{
				for (KCurrencyCountStruct struct : mKMountUpBigLvData.moneyList) {
					result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, struct.currencyType.extName, struct.currencyCount));
				}
			}

			// 财产日志
			FlowManager.logPropertyModify(role.getId(), PropertyTypeEnum.坐驾, mount._uuid, temp.mountsID, temp.Name, "机甲进阶,型号:" + temp.Model + ",阶级:" + nextTemp.bigLv);

			result.isSucess = true;
			result.tips = MountTips.成功进阶;
			return result;
		} finally {
			set.rwLock.unlock();
		}
	}

	public static MountResult_UpLvSkill dealMsg_uplvSkill(KRole role, int modelId, int skillTempId) {
		MountResult_UpLvSkill result = new MountResult_UpLvSkill();
		//
		KMountSet set = KMountModuleExtension.getMountSet(role.getId());
		set.rwLock.lock();
		try {

			KMount mount = set.getMountByModel(modelId);
			if (mount == null) {
				result.tips = MountTips.机甲不存在;
				return result;
			}
			KMountTemplate temp = mount.getTemplate();
			if (temp == null) {
				result.tips = MountTips.机甲不存在;
				return result;
			}
			if (!temp.skillIdSet.contains(skillTempId)) {
				result.tips = SkillTips.未掌握此技能;
				return result;
			}
			KRoleIniSkillTemp skillTemp = KSupportFactory.getSkillModuleSupport().getMountSkillTemplate(skillTempId);
			if (skillTemp == null) {
				result.tips = SkillTips.未掌握此技能;
				return result;
			}

			int skillLv = 1;
			{
				KMountSkill skill = mount.getSkillCache().get(skillTempId);
				if (skill != null) {
					skillLv = skill.getLv();
				}
			}

			int nextLv = skillLv + 1;
			// 检查最高等级
			if (nextLv > skillTemp.max_lvl) {
				result.tips = SkillTips.此技能已达等级上限;
				return result;
			}

			SkillTempLevelData nextLvData = skillTemp.getLevelData(nextLv);
			// 升级条件检测(金币、SP)

			KMountLv mKMountLv = KMountDataManager.mMountLvDataManager.getData(mount.getLevel());
			KCurrencyCountStruct sp = nextLvData.learnLvMoneys.get(0);
			int releaseSP = mKMountLv.spPoint - mount.getUsedSP();
			if (sp.currencyCount > releaseSP) {
				result.tips = MountTips.SP不足;
				return result;
			}

			KCurrencyCountStruct gold = nextLvData.learnLvMoneys.get(1);
			{
				long resultMoney = KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), gold, UsePointFunctionTypeEnum.机甲技能, true);
				if (resultMoney < 0) {
					// 货币不足
					result.isGoMoneyUI = true;
					result.goMoneyUIType = gold.currencyType;
					result.goMoneyUICount = gold.currencyCount - KSupportFactory.getCurrencySupport().getMoney(role.getId(), gold.currencyType);
					result.tips = StringUtil.format(ShopTips.x货币数量不足x, gold.currencyType.extName, gold.currencyCount);
					return result;
				}
			}
			mount.upLvSkill((int) sp.currencyCount, skillTempId, nextLv);
			//

			// 财产日志
			FlowManager.logPropertyModify(role.getId(), PropertyTypeEnum.坐驾, mount._uuid, temp.mountsID, temp.Name, "机甲技能升级,型号:" + temp.Model + ",阶级:" + temp.bigLv + ",技能:" + skillTempId + ",达到:"
					+ nextLv);

			result.isSucess = true;
			result.tips = SkillTips.升级技能成功;
			result.releaseSP = releaseSP - (int) sp.currencyCount;
			return result;
		} finally {
			set.rwLock.unlock();
		}
	}

	public static MountResult_ResetSP dealMsg_resetSP(KRole role, int modelId, boolean isConfirm) {
		MountResult_ResetSP result = new MountResult_ResetSP();
		//
		KMountSet set = KMountModuleExtension.getMountSet(role.getId());
		set.rwLock.lock();
		try {

			KMount mount = set.getMountByModel(modelId);
			if (mount == null) {
				result.tips = MountTips.机甲不存在;
				return result;
			}
			KMountTemplate temp = mount.getTemplate();
			if (temp == null) {
				result.tips = MountTips.机甲不存在;
				return result;
			}
			
			result.mountTemplate = temp;

			boolean isUsedSP = false;
			for (int skillId : temp.skillIdList) {
				KMountSkill skill = mount.getSkillCache().get(skillId);
				if (skill != null && skill.getLv() > 1) {
					isUsedSP = true;
					break;
				}
			}

			if (!isUsedSP) {
				result.tips = MountTips.无须重置SP;
				return result;
			}

			KMountResetSPData resetData = KMountDataManager.mMountResetSPDataManager.getData(mount.getLevel());
			if (resetData == null) {
				result.tips = MountTips.无须重置SP;
				return result;
			}

			{
				if (resetData.diamond != null && !isConfirm) {
					result.isGoConfirm = true;
					result.tips = StringUtil.format(MountTips.本次重置需要消耗x数量x货币确定要重置吗, resetData.diamond.currencyCount, resetData.diamond.currencyType.extName);
					return result;
				}
				KCurrencyCountStruct resultMoney = KSupportFactory.getCurrencySupport().decreaseMoneys(role.getId(), resetData.moneyList, UsePointFunctionTypeEnum.机甲技能, true);
				if (resultMoney != null) {
					// 货币不足
					result.isGoMoneyUI = true;
					result.goMoneyUIType = resultMoney.currencyType;
					result.goMoneyUICount = resultMoney.currencyCount - KSupportFactory.getCurrencySupport().getMoney(role.getId(), resultMoney.currencyType);
					result.tips = StringUtil.format(ShopTips.x货币数量不足x, resultMoney.currencyType.extName, resultMoney.currencyCount);
					return result;
				}
			}

			mount.resetSP();
			//

			// 财产日志
			FlowManager.logPropertyModify(role.getId(), PropertyTypeEnum.坐驾, mount._uuid, temp.mountsID, temp.Name, "机甲技能重置,型号:" + temp.Model + ",阶级:" + temp.bigLv);

			result.isSucess = true;
			result.tips = MountTips.技能重置成功;
			KMountLv mKMountLv = KMountDataManager.mMountLvDataManager.getData(mount.getLevel());
			result.releaseSP = mKMountLv.spPoint;
			return result;
		} finally {
			set.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 骑乘属性=升级属性（培养属性）
	 * 
	 * @param roleId
	 * @return
	 * @author CamusHuang
	 * @creation 2015-1-13 下午5:03:10
	 * </pre>
	 */
	static Map<KGameAttrType, Integer> getAllMountAttsForLv(long roleId) {
		KMountSet set = KMountModuleExtension.getMountSet(roleId);
		set.rwLock.lock();
		try {
			if(set.getMountCache().isEmpty()){
				return Collections.emptyMap();
			}
			
			Map<KGameAttrType, AtomicInteger> map = new HashMap<KGameAttrType, AtomicInteger>();
			for(KMount mount:set.getMountCache().values()){
				KGameUtilTool.combinMap2(map, mount.getAttsForLv());
			}
			return KGameUtilTool.changeAttMap(map);
		} finally {
			set.rwLock.unlock();
		}
	}
}
