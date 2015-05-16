package com.kola.kmp.logic.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.koala.game.KGame;
import com.koala.game.logging.KGameLogger;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.flowdata.FlowDataModuleFactory;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kgame.cache.item.impl.KAItemPack;
import com.kola.kgame.cache.pet.Pet;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.chat.KWordBroadcastType;
import com.kola.kmp.logic.fashion.KFashionDataStructs.KFashionTemplate;
import com.kola.kmp.logic.flow.FlowManager;
import com.kola.kmp.logic.flow.FlowManager.OtherFlowTypeEnum;
import com.kola.kmp.logic.flow.FlowManager.PropertyTypeEnum;
import com.kola.kmp.logic.flow.KPetFlowType;
import com.kola.kmp.logic.flow.KRoleAttrModifyType;
import com.kola.kmp.logic.item.KItem.KItemCA.KItem_EquipmentData;
import com.kola.kmp.logic.item.KItemDataStructs.KBagExtData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiAttExtData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiBuyEnchansePrice;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiInheritData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStarAttExtData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStarMaterialData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStarRateData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStrongAttExtData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStrongPriceParam;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempConsume;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempEqui;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempEquiBox;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempFixedBox;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempMaterial;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempRandomBox;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempStone;
import com.kola.kmp.logic.item.message.KComposeListSelectMsg;
import com.kola.kmp.logic.item.message.KPushItemsMsg;
import com.kola.kmp.logic.item.message.KPushNewGiftMsg;
import com.kola.kmp.logic.item.message.KPushNewTopEquiMsg;
import com.kola.kmp.logic.item.message.KSelectDreamBoxRewardMsg;
import com.kola.kmp.logic.mount.KMount;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountTemplate;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KColorFunEnum;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.EquiSetStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KEquipmentTypeEnum;
import com.kola.kmp.logic.other.KFunctionTypeEnum;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KItemPackTypeEnum;
import com.kola.kmp.logic.other.KItemQualityEnum;
import com.kola.kmp.logic.other.KItemTypeEnum;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.other.KLimitTimeProduceActivityTypeEnum;
import com.kola.kmp.logic.other.KShopTypeEnum;
import com.kola.kmp.logic.pet.KPetTemplate;
import com.kola.kmp.logic.reward.activatecode.KActivateCodeCenter;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.TimeLimitActivityDataManager.TimeLimieProduceActivity;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.role.KRoleModuleConfig;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;
import com.kola.kmp.logic.util.ResultStructs.CommonResult_Ext;
import com.kola.kmp.logic.util.ResultStructs.EquiSetResult;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_AddItem;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_BuyEnchase;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_Compose;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_Enchase;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_Equi;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_EquiStrong;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_EquiStrongIn;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_EquiStrongOld;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_EquiUpStar;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_ExtPack;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_GetExtBagPrice;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_InheritEquiPrice;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_Item;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_Use;
import com.kola.kmp.logic.util.text.HyperTextTool;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.ItemTips;
import com.kola.kmp.logic.util.tips.ShopTips;

public class KItemLogic {

	static final KGameLogger _LOGGER = KGameLogger.getLogger(KItemLogic.class);
	public static final KGameLogger _OPEN_FIXEDBOX_LOGGER = KGameLogger.getLogger("openFixedBoxLogger");

	static void initEquipmentsForNewRole(KRole role) {
		Map<KEquipmentTypeEnum, KItemTempEqui> map = KItemDataManager.mItemTemplateManager.getNewRoleEquiments(KJobTypeEnum.getJob(role.getJob()));
		if (map == null) {
			return;
		}
		long slotId = KItemConfig.MAIN_BODYSLOT_ID;

		ItemResult_AddItem totalResult = new ItemResult_AddItem();
		totalResult.newItemList = new ArrayList<KItem>();
		totalResult.updateItemCountList = new ArrayList<KItem>();

		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		set.rwLock.lock();
		try {
			KItemPack_Bag bag = set.getBag();
			KItemPack_BodySlot slot = set.getSlot();

			// 加装备
			for (KItemTempEqui temp : map.values()) {
				if (bag.searchItem(temp.itemCode) != null) {
					continue;
				}
				// 如果装备栏已经有装备
				if (slot.searchSlotItem(slotId, temp.typeEnum) != null) {
					continue;
				}

				ItemResult_AddItem result = bag.addItem(temp, 1, null);
				if (!result.isSucess) {
					continue;
				}

				totalResult.newItemList.addAll(result.newItemList);
				totalResult.updateItemCountList.addAll(result.updateItemCountList);

				{
					// 将装备从背包移除再放入装备栏
					bag.moveOutItem(result.getItem().getId());
					slot.installItem(slotId, result.getItem());
				}
			}

			// 重算套装数据
			slot.recountEquiSetData(slotId);
		} finally {
			set.rwLock.unlock();

			// 刷新角色属性
			KItemAttributeProvider.notifyEffectAttrChange(role);
			// 刷新UI
			KSupportFactory.getRoleModuleSupport().updateEquipmentRes(role.getId());
			KSupportFactory.getTeamPVPSupport().notifyRoleEquipmentResUpdate(role.getId());

			// 财产日志
			FlowManager.logPropertyAdd(role.getId(), totalResult, null, "新角色默认装备");
		}
	}

	static ItemResult_AddItem addItemToBag(KRole role, String itemCode, long count, String sourceTips) {
		KItemTempAbs template = KItemDataManager.mItemTemplateManager.getItemTemplate(itemCode);
		return addItemToBag(role, template, count, sourceTips);
	}

	static ItemResult_AddItem addItemToBag(KRole role, KItemTempAbs template, long count, String sourceTips) {
		ItemResult_AddItem result = new ItemResult_AddItem();
		//
		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		set.rwLock.lock();
		try {
			KItemPack_Bag bag = set.getBag();
			result = bag.addItem(template, count, result);
			return result;
		} finally {
			set.rwLock.unlock();
			//
			if (result.isSucess) {
				// 同步道具
				KPushItemsMsg.pushItems(role, result.newItemList);
				KPushItemsMsg.pushItemCounts(role.getId(), result.updateItemCountList);

				// 装备安装提示
				KPushNewTopEquiMsg.sendMsg(role.getId(), filterNewTopEquipment(role, result.newItemList));
				// 打开礼包提示
				KPushNewGiftMsg.sendMsg(role.getId(), filterNewGiftItem(role, result.newItemList, result.updateItemCountList));

				// 财产日志
				FlowManager.logPropertyAdd(role.getId(), result, Arrays.asList(new ItemCountStruct(template, count)), sourceTips);
			}
		}
	}

	/**
	 * <pre>
	 * 增加道具
	 * 要不就全加，要不就一个都不加
	 * 
	 * @param role
	 * @param itemCounts （不允许包含重复的ItemCode）
	 * @return
	 * @author CamusHuang
	 * @creation 2012-12-5 下午12:34:23
	 * </pre>
	 */
	static ItemResult_AddItem addItemsToBag(KRole role, List<ItemCountStruct> itemCounts, String sourceTips) {
		ItemResult_AddItem result = new ItemResult_AddItem();
		//
		if (itemCounts == null || itemCounts.isEmpty()) {
			result.tips = ItemTips.物品数量不能小于1;
			return result;
		}
		//
		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		set.rwLock.lock();
		try {
			KItemPack_Bag bag = set.getBag();
			result = bag.addItems(itemCounts, result);
			return result;
		} finally {
			set.rwLock.unlock();
			//
			if (result.isSucess) {
				// 同步道具
				KPushItemsMsg.pushItems(role, result.newItemList);
				KPushItemsMsg.pushItemCounts(role.getId(), result.updateItemCountList);

				// 装备安装提示
				KPushNewTopEquiMsg.sendMsg(role.getId(), filterNewTopEquipment(role, result.newItemList));
				// 打开礼包提示
				KPushNewGiftMsg.sendMsg(role.getId(), filterNewGiftItem(role, result.newItemList, result.updateItemCountList));

				// 财产日志
				FlowManager.logPropertyAdd(role.getId(), result, itemCounts, sourceTips);
			}
		}
	}

	public static boolean isCanAddItemsToBag(long roleId, List<ItemCountStruct> itemCounts) {
		KItemSet set = KItemModuleExtension.getItemSet(roleId);
		set.rwLock.lock();
		try {
			KItemPack_Bag bag = set.getBag();
			return bag.isBagCanAddItems(itemCounts);
		} finally {
			set.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 未调用
	 * @return
	 * @author CamusHuang
	 * @creation 2014-2-13 下午3:15:31
	 * </pre>
	 */
	private static boolean deleteItemFromBag(long roleId, long itemId) {

		return false;
		// KItemSet set = KItemModuleExtension.getItemSet(roleId);
		// set.rwLock.lock();
		// try {
		// KItemPack_Bag bag = set.getBag();
		// KItem item = bag.deleteItem(itemId);
		// if (item == null) {
		// return false;
		// }
		//
		// // 同步道具
		// KPushItemsMsg.pushItemCount(roleId, itemId, 0);
		// return true;
		// } finally {
		// set.rwLock.unlock();
		// }
	}

	static boolean removeItemFromBag(long roleId, String itemCode, long count, String sourceTips) {
		KItemSet set = KItemModuleExtension.getItemSet(roleId);
		set.rwLock.lock();
		try {
			KItemPack_Bag bag = set.getBag();
			List<KItem> items = bag.searchItems(itemCode);
			if (items.isEmpty()) {
				return false;
			}

			KItem tempItem = items.get(0);
			KItemTempAbs tempAbs = tempItem.getItemTemplate();
			if (tempAbs.isCanStack()) {
				// 可合并的道具
				if (tempItem.changeCount(-count) < 0) {
					return false;
				}

				// 同步道具
				KPushItemsMsg.pushItemCount(roleId, tempItem.getId(), tempItem.getCount());
				// 财产日志
				FlowManager.logPropertyDelete(roleId, tempItem, count, sourceTips);
				return true;
			} else {
				// 不可合并的道具
				if (items.size() < count) {
					return false;
				}
				for (int i = 0; i < count; i++) {
					tempItem = items.get(i);
					tempItem.changeCount(-1);
					// 同步道具
					KPushItemsMsg.pushItemCount(roleId, tempItem.getId(), tempItem.getCount());
					// 财产日志
					FlowManager.logPropertyDelete(roleId, tempItem, 1, sourceTips);
				}
				return true;
			}
		} finally {
			set.rwLock.unlock();
		}
	}

	static boolean removeItemFromBag(long roleId, long itemId, long count, String sourceTips) {
		KItemSet set = KItemModuleExtension.getItemSet(roleId);
		set.rwLock.lock();
		try {
			KItemPack_Bag bag = set.getBag();
			KItem item = bag.getItem(itemId);

			if (item == null) {
				return false;
			}

			if (item.changeCount(-count) < 0) {
				return false;
			}

			// 同步道具
			KPushItemsMsg.pushItemCount(roleId, item.getId(), item.getCount());
			// 财产日志
			FlowManager.logPropertyDelete(roleId, item, count, sourceTips);
			return true;
		} finally {
			set.rwLock.unlock();
		}
	}

	public static CommonResult_Ext dealMsg_updateClientData(long roleId, String data) {
		CommonResult_Ext result = new CommonResult_Ext();
		//
		KItemSet set = KItemModuleExtension.getItemSet(roleId);
		set.setClientData(data);
		//
		result.isSucess = true;
		result.tips = ItemTips.数据保存成功;
		return result;
	}

	public static ItemResult_ExtPack dealMsg_extendBagVolume(KRole role, int buyCell) {
		ItemResult_ExtPack result = new ItemResult_ExtPack();
		ItemResult_GetExtBagPrice priceResult = null;
		//
		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		set.rwLock.lock();
		try {

			priceResult = dealMsg_getExtendPackPrice(role.getId(), buyCell);
			if (!priceResult.isSucess) {
				result.tips = priceResult.tips;
				return result;
			}

			if (!priceResult.price.isEmpty()) {
				// 付费扩容
				KCurrencyCountStruct resultMoney = KSupportFactory.getCurrencySupport().decreaseMoneys(role.getId(), priceResult.price, UsePointFunctionTypeEnum.背包扩容, true);
				if (resultMoney != null) {
					result.isGoMoneyUI = true;
					result.goMoneyUIType = resultMoney.currencyType;
					result.goMoneyUICount = resultMoney.currencyCount - KSupportFactory.getCurrencySupport().getMoney(role.getId(), resultMoney.currencyType);
					result.tips = StringUtil.format(ShopTips.x货币数量不足x, resultMoney.currencyType.extName, resultMoney.currencyCount);
					return result;
				}
			}
			//
			KItemPack_Bag bag = set.getBag();
			int orgVolume = bag.getVolume();
			bag.setVolume(orgVolume + buyCell);
			//
			result.isSucess = true;
			result.tips = StringUtil.format(ItemTips.成功扩容格数加x, buyCell);
			result.newVolume = bag.getVolume();
			for (KCurrencyCountStruct struct : priceResult.price) {
				result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, struct.currencyType.extName, struct.currencyCount));
			}
			// 背包扩容属性
			Map<KGameAttrType, AtomicInteger> tempResult = KItemDataManager.mBagExtDataManager.getBagExtEffect(orgVolume, bag.getVolume(), null);
			for (Entry<KGameAttrType, AtomicInteger> entry : tempResult.entrySet()) {
				result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, entry.getKey().getExtName(), entry.getValue().get()));
			}
			return result;
		} finally {
			set.rwLock.unlock();

			if (result.isSucess) {
				// 财产日志
				FlowManager.logOther(role.getId(), OtherFlowTypeEnum.背包扩容, StringUtil.format("购买格数:{};最终格数:{}", buyCell, result.newVolume));
			}
		}
	}

	public static ItemResult_GetExtBagPrice dealMsg_getExtendPackPrice(long roleId, int buyCell) {

		ItemResult_GetExtBagPrice result = new ItemResult_GetExtBagPrice();

		KItemSet set = KItemModuleExtension.getItemSet(roleId);
		set.rwLock.lock();
		try {
			KItemPack_Bag bag = set.getBag();
			int nowVol = bag.getVolume();
			{
				int maxVol = KItemDataManager.mBagExtDataManager.getMaxGridCount() - nowVol;
				if (buyCell > maxVol) {
					// 爆仓
					result.tips = StringUtil.format(ItemTips.最多只能扩容x格, maxVol);
					return result;
				}
			}

			Map<KCurrencyTypeEnum, AtomicLong> totalPrice = new HashMap<KCurrencyTypeEnum, AtomicLong>();

			for (int i = 1; i <= buyCell; i++) {
				int nextVol = nowVol + i;
				KBagExtData data = KItemDataManager.mBagExtDataManager.getData(nextVol);
				if (data == null) {
					// 爆仓
					result.tips = ItemTips.容量已达极限;
					return result;
				}

				if (data.payMoney == null) {
					// 免费
					continue;
				}

				// 付费
				AtomicLong price = totalPrice.get(data.payMoney.currencyType);
				if (price == null) {
					price = new AtomicLong();
					totalPrice.put(data.payMoney.currencyType, price);
				}
				price.addAndGet(data.payMoney.currencyCount);
			}

			// 属性叠加
			StringBuffer attStrBuf = new StringBuffer();
			{
				Map<KGameAttrType, AtomicInteger> totalAtts = KItemDataManager.mBagExtDataManager.getBagExtEffect(nowVol, nowVol + buyCell, null);
				for (Entry<KGameAttrType, AtomicInteger> entry : totalAtts.entrySet()) {
					attStrBuf.append('\n');
					attStrBuf.append(StringUtil.format(ShopTips.x加x, entry.getKey().getExtName(), entry.getValue().get()));
				}

			}

			result.isSucess = true;
			if (totalPrice.isEmpty()) {
				// 免费
				result.price = Collections.emptyList();
			} else {
				// 付费
				result.price = new ArrayList<KCurrencyCountStruct>();
				StringBuffer sbf = new StringBuffer();
				for (Entry<KCurrencyTypeEnum, AtomicLong> entry : totalPrice.entrySet()) {
					KCurrencyCountStruct price = new KCurrencyCountStruct(entry.getKey(), entry.getValue().get());
					result.price.add(price);
					sbf.append(price.currencyType.extName).append("x").append(price.currencyCount).append(GlobalTips.顿号);
				}
				sbf.deleteCharAt(sbf.length() - 1);
				result.tips = StringUtil.format(ItemTips.是否花费x数量x货币扩容x格, sbf.toString(), UtilTool.getNotNullString(null), buyCell);
				result.tips += attStrBuf.toString();
			}
			return result;
		} finally {
			set.rwLock.unlock();
		}
	}

	public static KItem getItem(long roleId, long itemId) {
		KItemSet set = KItemModuleExtension.getItemSet(roleId);
		set.rwLock.lock();
		try {
			for (KAItemPack<KItem> pack : set.getAllItemPacksCache().values()) {
				KItem item = pack.getItem(itemId);
				if (item != null) {
					return item;
				}
			}
		} finally {
			set.rwLock.unlock();
		}
		return null;
	}

	public static KItem searchItemFromBag(long roleId, String itemCode) {
		KItemSet set = KItemModuleExtension.getItemSet(roleId);
		set.rwLock.lock();
		try {
			KItemPack_Bag bag = set.getBag();
			return bag.searchItem(itemCode);
		} finally {
			set.rwLock.unlock();
		}
	}

	public static ItemResult_Use dealMsg_useBagItem(KGamePlayerSession session, KRole role, long useItemId, KItemTypeEnum itemType, boolean isUseAll) {
		ItemResult_Use result = null;
		try {
			if (itemType == KItemTypeEnum.消耗品) {
				result = useConsumeItem(role, useItemId, isUseAll);
			} else if (itemType == KItemTypeEnum.固定宝箱) {
				result = openFixedBox(session, role, useItemId, isUseAll, KItemConfig.FixeBOx_RewardType_NONE);
			} else if (itemType == KItemTypeEnum.随机宝箱) {
				result = openRandomBox(role, useItemId, isUseAll);
			} else if (itemType == KItemTypeEnum.装备包) {
				result = openEquiBox(role, useItemId, isUseAll);
			} else {
				result = new ItemResult_Use();
				result.tips = ItemTips.此物品不能使用;
			}
			return result;
		} catch (Exception e) {
			_LOGGER.error(e.getMessage(), e);
			result = new ItemResult_Use();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			if (result.isSucess) {
				// 财产日志
				FlowManager.logPropertyDelete(role.getId(), result.item, result.useCount, "使用道具");
			}
		}
	}

	/**
	 * <pre>
	 * 使用消耗类道具
	 * 限制使用背包内道具
	 * 
	 * @param role
	 * @param useItemId 要消耗的道具
	 * @return
	 * @author CamusHuang
	 * @creation 2012-11-21 下午3:08:08
	 * </pre>
	 */
	private static ItemResult_Use useConsumeItem(KRole role, long useItemId, boolean isUseAll) {
		ItemResult_Use result = new ItemResult_Use();
		//
		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		set.rwLock.lock();
		try {

			KItemPack_Bag bag = set.getBag();

			result.item = bag.getItem(useItemId);// 主道具
			if (result.item == null) {
				result.tips = ItemTips.物品不存在;
				return result;
			}

			if (result.item.getItemTemplate().ItemType != KItemTypeEnum.消耗品) {
				// 不是消耗类道具
				result.tips = ItemTips.此物品不能使用;
				return result;
			}

			KItemTempConsume itemTemp = (KItemTempConsume) result.item.getItemTemplate();

			// 检查角色等级要求
			if (itemTemp.lvl > 0 && role.getLevel() < itemTemp.lvl) {
				result.tips = StringUtil.format(ItemTips.x级以上才能使用此物品, itemTemp.lvl);
				return result;
			}

			useConsumeItemIn(result, role, bag, itemTemp, isUseAll);

			return result;
		} finally {
			set.rwLock.unlock();

			if (result.isSucess) {
				// 同步道具
				KPushItemsMsg.pushItemCount(role.getId(), result.item.getId(), result.item.getCount());
			}
		}
	}

	private static void useConsumeItemIn(ItemResult_Use result, KRole role, KItemPack_Bag bag, KItemTempConsume itemTemp, boolean isUseAll) {

		// 检查CD时间
		long releaseTime = Math.round(bag.getItemCDReleaseTime(itemTemp));
		if (releaseTime > 0) {
			result.tips = StringUtil.format(ItemTips.CD中请等候x时间再来, UtilTool.genReleaseCDTimeString(releaseTime));
			return;
		}

		// 可以使用的道具数量
		result.useCount = isUseAll ? (int) result.item.getCount() : 1;
		// 如果是体力道具，则根据缺失体力计算最大可使用数量
		if (itemTemp.addAtt != null && itemTemp.addAtt.roleAttType == KGameAttrType.PHY_POWER) {
			int usePhyPower = KSupportFactory.getRoleModuleSupport().checkPhyPowerUsed(role);
			if (usePhyPower < 1) {
				result.tips = ShopTips.体力值已满无需补充;
				return;
			}

			int count = (int) Math.ceil(((double) usePhyPower) / itemTemp.addAtt.addValue);
			result.useCount = Math.min(result.useCount, count);
		}

		// 有CD限制，只能使用一个
		if (itemTemp.cdTimeInMill > 0) {
			result.useCount = 1;
		}

		{
			// 扣道具
			if (result.item.changeCount(-result.useCount) < 0) {
				result.tips = ItemTips.物品数量不足;
				return;
			}

			// 加属性
			AttValueStruct addAtt = null;
			if (itemTemp.addAtt != null) {
				addAtt = result.useCount == 1 ? itemTemp.addAtt : new AttValueStruct(itemTemp.addAtt.roleAttType, itemTemp.addAtt.addValue * result.useCount);
				KSupportFactory.getRoleModuleSupport().notifyUseConsumeItemEffect(role.getId(), addAtt, KRoleAttrModifyType.使用道具, itemTemp.name);
			}
			// 加货币
			KCurrencyCountStruct addMoney = null;
			if (itemTemp.addMoney != null) {
				addMoney = result.useCount == 1 ? itemTemp.addMoney : new KCurrencyCountStruct(itemTemp.addMoney.currencyType, itemTemp.addMoney.currencyCount * result.useCount);
				KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), addMoney, PresentPointTypeEnum.开宝箱, true);
			}

			// 记录CD时间
			bag.setItemCDReleaseTime(itemTemp);

			result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, itemTemp.extItemName, result.useCount));
			if (addAtt != null) {
				result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, addAtt.roleAttType.getExtName(), addAtt.addValue));
			}
			if (addMoney != null) {
				result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, addMoney.currencyType.extName, addMoney.currencyCount));
			}
		}

		//
		result.isSucess = true;
		result.tips = ItemTips.使用物品成功;
		return;
	}

	public static ItemResult_Use dealMsg_openDreamBox(KGamePlayerSession session, KRole role, long useItemId, int useType) {
		ItemResult_Use result = null;
		try {
			result = openFixedBox(session, role, useItemId, false, useType);
			return result;
		} catch (Exception e) {
			_LOGGER.error(e.getMessage(), e);
			result = new ItemResult_Use();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			if (result.isSucess) {
				// 财产日志
				FlowManager.logPropertyDelete(role.getId(), result.item, result.useCount, "使用道具");
			}
		}
	}

	/**
	 * <pre>
	 * 
	 * 
	 * @param role
	 * @param useItemId
	 * @param isUseAll
	 * @param offlineType 0表示未进行确认，1表示选择离线奖励，2表示选择在线奖励
	 * @return
	 * @author CamusHuang
	 * @creation 2014-10-29 下午5:18:58
	 * </pre>
	 */
	private static ItemResult_Use openFixedBox(KGamePlayerSession session, KRole role, long useItemId, boolean isUseAll, int offlineType) {
		ItemResult_Use result = new ItemResult_Use();
		KItemTempFixedBox itemTemp = null;
		ItemResult_AddItem addResult = null;
		// 成功添加的道具及数量
		List<ItemCountStruct> successAddItems = new ArrayList<ItemCountStruct>();
		//
		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		set.rwLock.lock();
		try {

			KItemPack_Bag bag = set.getBag();

			result.item = bag.getItem(useItemId);// 主道具
			if (result.item == null || result.item.getCount() < 1) {
				result.tips = ItemTips.物品不存在;
				return result;
			}

			if (result.item.getItemTemplate().ItemType != KItemTypeEnum.固定宝箱) {
				// 不是固定宝箱道具
				result.tips = ItemTips.此物品不能使用;
				return result;
			}

			itemTemp = (KItemTempFixedBox) result.item.getItemTemplate();

			// 检查角色等级要求
			if (itemTemp.lvl > 0 && role.getLevel() < itemTemp.lvl) {
				result.tips = StringUtil.format(ItemTips.x级以上才能使用此物品, itemTemp.lvl);
				return result;
			}

			// 是否线下活动礼包，是否活动已经过期
			boolean isDreamBox = false;
			boolean isDreamBoxOutDate = true;
			TimeLimieProduceActivity dreamActivity = null;
			{
				dreamActivity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.话费礼包活动);
				if (dreamActivity != null) {
					isDreamBox = dreamActivity.dreamGiftItemTemp.itemCode.equals(itemTemp.id);
					isDreamBoxOutDate = !dreamActivity.isActivityTakeEffectNow();
				}
			}

			// 如果是线下活动礼包，则强制每次只能使用一个
			if (isDreamBox) {
				isUseAll = false;

				if (offlineType == KItemConfig.FixeBOx_RewardType_NONE) {// 0表示未进行确认
					// 弹框让玩家进行选择
					KSelectDreamBoxRewardMsg.pushDialog(role, result.item, dreamActivity.commonGiftItemTemp);
					return result;
				}

				addResult = new ItemResult_AddItem();
				
				if (offlineType == KItemConfig.FixeBOx_RewardType_OFFLINE) {// 1表示选择离线奖励
					// 活动是否已过期
					if (isDreamBoxOutDate) {
						result.tips = StringUtil.format(ItemTips.话费活动已结束请选择x物品, dreamActivity.commonGiftItemTemp.extItemName);
						return result;
					}
					
					//openDreamBoxForMoney(role, result, itemTemp, isDreamBoxOutDate);
					openFixedBoxForReward(role, bag, result, itemTemp, false, addResult, successAddItems);
					if (result.isSucess) {
						// 使用成功，记录日志
						_OPEN_FIXEDBOX_LOGGER.warn(",fixebox,gsId=,{},角色ID=,{},角色名=,{},itemCode=,{},itemName=,{},useType=,{}", KGame.getGSID(), role.getId(), role.getName(), itemTemp.itemCode,
								itemTemp.name, "线下");
						// 通知给梦想
						KActivateCodeCenter.notifyDreamByHttp(session, role, result.item.getCA().getActiviteCode(), "-1");
						
						result.addUprisingTips(ItemTips.你选择的线下奖励已登记);
					}
					return result;
				}

				if (offlineType == KItemConfig.FixeBOx_RewardType_ONLINE) {// 2表示选择在线奖励
					// 正常发奖，记录日志，使用成功
					addResult = new ItemResult_AddItem();
					openFixedBoxForReward(role, bag, result, dreamActivity.commonGiftItemTemp, isUseAll, addResult, successAddItems);
					if (result.isSucess) {
						// 使用成功，记录日志
						_OPEN_FIXEDBOX_LOGGER.warn(",fixebox,gsId=,{},角色ID=,{},角色名=,{},itemCode=,{},itemName=,{},useType=,{}", KGame.getGSID(), role.getId(), role.getName(), itemTemp.itemCode,
								itemTemp.name, "线上");
						// 通知给梦想
						KActivateCodeCenter.notifyDreamByHttp(session, role, result.item.getCA().getActiviteCode(), "-2");
					}
					return result;
				}

				result.tips = GlobalTips.服务器繁忙请稍候再试;
				return result;
			} else {
				// 普通礼包
				addResult = new ItemResult_AddItem();
				openFixedBoxForReward(role, bag, result, itemTemp, isUseAll, addResult, successAddItems);
				return result;
			}
		} finally {
			set.rwLock.unlock();

			if (result.isSucess) {
				// 同步道具
				KPushItemsMsg.pushItemCount(role.getId(), result.item.getId(), result.item.getCount());
				if (addResult != null) {
					Set<KItem> hset = Collections.emptySet();
					if (addResult.newItemList != null && !addResult.newItemList.isEmpty()) {
						hset = new HashSet<KItem>(addResult.newItemList);
						addResult.newItemList = new ArrayList<KItem>(hset);
						KPushItemsMsg.pushItems(role, addResult.newItemList);
					}
					if (addResult.updateItemCountList != null && !addResult.updateItemCountList.isEmpty()) {
						Set<KItem> hset2 = new HashSet<KItem>(addResult.updateItemCountList);
						hset2.removeAll(hset);
						addResult.updateItemCountList = new ArrayList<KItem>(hset2);
						KPushItemsMsg.pushItemCounts(role.getId(), addResult.updateItemCountList);
					}

					// 装备安装提示
					KPushNewTopEquiMsg.sendMsg(role.getId(), filterNewTopEquipment(role, addResult.newItemList));
					// 打开礼包提示
					KPushNewGiftMsg.sendMsg(role.getId(), filterNewGiftItem(role, addResult.newItemList, addResult.updateItemCountList));

					// 财产日志
					FlowManager.logPropertyAdd(role.getId(), addResult, successAddItems, "开固定宝箱");
				}
			}
		}
	}

//	/**
//	 * <pre>
//	 * 开启一个固定宝箱，登记话费奖励，记录日志
//	 * 
//	 * @param role
//	 * @param bag
//	 * @param result
//	 * @param itemTemp
//	 * @param isUseAll
//	 * @param addResult
//	 * @param successAddItems
//	 * @author CamusHuang
//	 * @creation 2014-10-29 下午5:56:49
//	 * </pre>
//	 */
//	private static void openDreamBoxForMoney(KRole role, ItemResult_Use result, KItemTempFixedBox itemTemp, boolean isDreamBoxOutDate) {
//
//		// 活动是否已过期
//		if (isDreamBoxOutDate) {
//			result.tips = ItemTips.话费活动已结束;
//			return;
//		}
//
//		// 扣道具
//		if (result.item.changeCount(-1) < 0) {
//			result.tips = ItemTips.物品数量不足;
//			return;
//		}
//
//		result.useCount = 1;
//		result.isSucess = true;
//		result.tips = ItemTips.使用物品成功;
//		// 打包提示
//		result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, itemTemp.extItemName, result.useCount));
//		result.addUprisingTips(ItemTips.你选择的线下奖励已登记);
//		return;
//	}

//	/**
//	 * <pre>
//	 * 循环开固定宝箱-》获取线上奖励
//	 * 
//	 * @param role
//	 * @param bag
//	 * @param result
//	 * @param itemTemp
//	 * @param isUseAll
//	 * @param addResult
//	 * @param successAddItems
//	 * @author CamusHuang
//	 * @creation 2014-10-29 下午5:56:49
//	 * </pre>
//	 */
//	private static void openFixedBoxForReward(KRole role, KItemPack_Bag bag, ItemResult_Use result, KItemTempFixedBox itemTemp, boolean isUseAll, ItemResult_AddItem addResult,
//			List<ItemCountStruct> successAddItems) {
//		addResult.newItemList = new ArrayList<KItem>();
//		addResult.updateItemCountList = new ArrayList<KItem>();
//
//		// 可以使用的道具数量
//		int useMaxCount = isUseAll ? (int) result.item.getCount() : 1;
//		// 成功添加的道具及数量
//		successAddItems = new ArrayList<ItemCountStruct>();
//		String tempResult = null;
//		for (int i = 0; i < useMaxCount; i++) {
//			tempResult = openFixedBoxForRewardIn(result.item, role, bag, itemTemp, addResult, successAddItems);
//			if (tempResult != null) {
//				break;
//			}
//			result.useCount++;
//		}
//
//		if (result.useCount < 1) {
//			result.tips = tempResult;
//			return;
//		}
//
//		result.isSucess = true;
//		if (tempResult == null) {
//			result.tips = ItemTips.使用物品成功;
//		} else {
//			result.tips = StringUtil.format(ItemTips.共成功使用x个x物品中断原因x, result.useCount, itemTemp.extItemName, tempResult);
//		}
//
//		// 打包提示
//		result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, itemTemp.extItemName, result.useCount));
//
//		if (!itemTemp.addFashionTempMap.isEmpty()) {
//			for (Entry<KFashionTemplate, Integer> entry : itemTemp.addFashionTempMap.entrySet()) {
//				result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, entry.getKey().name, entry.getValue() * result.useCount));
//			}
//		}
//		if (!successAddItems.isEmpty()) {
//			successAddItems = ItemCountStruct.mergeItemCountStructs(successAddItems);
//			for (ItemCountStruct data : successAddItems) {
//				result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, data.getItemTemplate().extItemName, data.itemCount));
//			}
//		}
//		if (!itemTemp.addMoneys.isEmpty()) {
//			for (KCurrencyCountStruct struct : itemTemp.addMoneys) {
//				result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, struct.currencyType.extName, struct.currencyCount * result.useCount));
//			}
//		}
//	}
//	
	/**
	 * <pre>
	 * 循环开固定宝箱-》获取线上奖励
	 * 
	 * @param role
	 * @param bag
	 * @param result
	 * @param itemTemp
	 * @param isUseAll
	 * @param addResult
	 * @param successAddItems
	 * @author CamusHuang
	 * @creation 2014-10-29 下午5:56:49
	 * </pre>
	 */
	private static void openFixedBoxForReward(KRole role, KItemPack_Bag bag, ItemResult_Use result, KItemTempFixedBox rewardItemTemp, boolean isUseAll, ItemResult_AddItem addResult,
			List<ItemCountStruct> successAddItems) {
		addResult.newItemList = new ArrayList<KItem>();
		addResult.updateItemCountList = new ArrayList<KItem>();

		// 可以使用的道具数量
		int useMaxCount = isUseAll ? (int) result.item.getCount() : 1;
		// 成功添加的道具及数量
		successAddItems = new ArrayList<ItemCountStruct>();
		String tempResult = null;
		for (int i = 0; i < useMaxCount; i++) {
			tempResult = openFixedBoxForRewardIn(result.item, role, bag, rewardItemTemp, addResult, successAddItems);
			if (tempResult != null) {
				break;
			}
			result.useCount++;
		}

		if (result.useCount < 1) {
			result.tips = tempResult;
			return;
		}

		result.isSucess = true;
		if (tempResult == null) {
			result.tips = ItemTips.使用物品成功;
		} else {
			result.tips = StringUtil.format(ItemTips.共成功使用x个x物品中断原因x, result.useCount, result.item.getItemTemplate().extItemName, tempResult);
		}

		// 打包提示
		result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, result.item.getItemTemplate().extItemName, result.useCount));

		if (!rewardItemTemp.addFashionTempMap.isEmpty()) {
			for (Entry<KFashionTemplate, Integer> entry : rewardItemTemp.addFashionTempMap.entrySet()) {
				if (entry.getKey().jobEnum == null || entry.getKey().job == role.getJob()) {
					result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, entry.getKey().name, entry.getValue() * result.useCount));
				}
			}
		}
		if (!successAddItems.isEmpty()) {
			successAddItems = ItemCountStruct.mergeItemCountStructs(successAddItems);
			for (ItemCountStruct data : successAddItems) {
				result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, data.getItemTemplate().extItemName, data.itemCount));
			}
		}
		if (!rewardItemTemp.addMoneys.isEmpty()) {
			for (KCurrencyCountStruct struct : rewardItemTemp.addMoneys) {
				result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, struct.currencyType.extName, struct.currencyCount * result.useCount));
			}
		}
	}	

	/**
	 * <pre>
	 * 开固定宝箱-》获取线上奖励
	 * 
	 * @param item
	 * @param role
	 * @param bag
	 * @param itemTemp
	 * @param addResult
	 * @param addItemsResult
	 * @return
	 * @author CamusHuang
	 * @creation 2014-10-29 下午5:57:08
	 * </pre>
	 */
	private static String openFixedBoxForRewardIn(KItem item, KRole role, KItemPack_Bag bag, KItemTempFixedBox itemTemp, ItemResult_AddItem addResult, List<ItemCountStruct> addItemsResult) {

		List<ItemCountStruct> addItems = itemTemp.getAddItems(KJobTypeEnum.getJob(role.getJob()), role.getLevel());
		if (!addItems.isEmpty()) {
			if (!bag.isBagCanAddItems(addItems)) {
				// 容量不足
				return ItemTips.背包容量不足;
			}
		}

		// 扣道具
		if (item.changeCount(-1) < 0) {
			return ItemTips.物品数量不足;
		}

		// 加时装
		if (!itemTemp.addFashionTempList.isEmpty()) {
			KSupportFactory.getFashionModuleSupport().addFashions(role, itemTemp.addFashionTempList, PresentPointTypeEnum.开宝箱.name());
		}

		// 加道具
		if (!addItems.isEmpty()) {
			ItemResult_AddItem tempResult = bag.addItems(addItems, null);
			if (!tempResult.isSucess) {
				return tempResult.tips;
			}

			if (tempResult.newItemList != null) {
				addResult.newItemList.addAll(tempResult.newItemList);
			}
			if (tempResult.updateItemCountList != null) {
				addResult.updateItemCountList.addAll(tempResult.updateItemCountList);
			}
			addItemsResult.addAll(addItems);
		}

		// 加货币
		if (!itemTemp.addMoneys.isEmpty()) {
			KSupportFactory.getCurrencySupport().increaseMoneys(role.getId(), itemTemp.addMoneys, PresentPointTypeEnum.开宝箱, true);
		}

		return null;
	}

	/**
	 * <pre>
	 * 开宝箱
	 * 限制使用背包内道具
	 * 
	 * @param role
	 * @param useItemId
	 * @author CamusHuang
	 * @creation 2012-11-21 下午3:55:35
	 * </pre>
	 */
	private static ItemResult_Use openRandomBox(KRole role, long useItemId, boolean isUseAll) {
		ItemResult_Use result = new ItemResult_Use();
		ItemResult_AddItem addResult = null;
		// 成功添加的道具及数量
		List<ItemCountStruct> successAddItems = new ArrayList<ItemCountStruct>();
		//
		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		set.rwLock.lock();
		try {

			KItemPack_Bag bag = set.getBag();

			result.item = bag.getItem(useItemId);// 主道具
			if (result.item == null || result.item.getCount() < 1) {
				result.tips = ItemTips.物品不存在;
				return result;
			}

			if (result.item.getItemTemplate().ItemType != KItemTypeEnum.随机宝箱) {
				// 不是随机宝箱道具
				result.tips = ItemTips.此物品不能使用;
				return result;
			}

			KItemTempRandomBox itemTemp = (KItemTempRandomBox) result.item.getItemTemplate();

			// 检查角色等级要求
			if (itemTemp.lvl > 0 && role.getLevel() < itemTemp.lvl) {
				result.tips = StringUtil.format(ItemTips.x级以上才能使用此物品, itemTemp.lvl);
				return result;
			}

			// 是否线下活动礼包，是否活动已经过期
			boolean isDreamBox = false;
			boolean isDreamBoxOutDate = true;
			TimeLimieProduceActivity dreamActivity = null;
			{
				dreamActivity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.节假副本特殊掉落);
				if (dreamActivity != null) {
					isDreamBox = dreamActivity.mutiMapItemTemp.itemCode.equals(itemTemp.id);
					isDreamBoxOutDate = !dreamActivity.isActivityTakeEffectNow();
				}
			}

			String offlineReward = null;// 线下奖励
			// 如果是线下活动礼包，则强制每次只能使用一个
			if (isDreamBox && !isDreamBoxOutDate) {
				// 是否能开出实物
				isUseAll = false;
				offlineReward = ItemGlobalDataImpl.randomOfflineReward(dreamActivity);
			}

			if (offlineReward != null) {
				// 开出线下实物
				String extOfflineReward = HyperTextTool.extColor(offlineReward, KColorFunEnum.品质_橙);

				// 扣道具数量
				result.useCount = 1;
				result.item.changeCount(-result.useCount);

				_OPEN_FIXEDBOX_LOGGER.warn(",randombox,gsId=,{},角色ID=,{},角色名=,{},itemCode=,{},itemName=,{},result=,{}", KGame.getGSID(), role.getId(), role.getName(), itemTemp.itemCode,
						itemTemp.name, offlineReward);

				result.isSucess = true;
				result.tips = ItemTips.使用物品成功;
				result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, extOfflineReward, 1));

				{
					// 世界广播
					// (1)恭喜XX玩家在圣诞大寻宝活动中获得30元话费充值卡
					// (2)恭喜XX玩家在圣诞大寻宝活动中获得10元骏网卡
					String worldTips = StringUtil.format(ItemTips.恭喜x玩家在圣诞大寻宝活动中获得x奖励, role.getExName(), extOfflineReward);
					KSupportFactory.getChatSupport().sendSystemChat(worldTips, true, true);
				}

				{
					// 并发送邮件给获奖玩家
					// (1)恭喜您在圣诞大寻宝活动中获得30元充值卡，奖励将会在5个工作日内发放，请留意邮箱！
					// (2)恭喜您在圣诞大寻宝活动中获得10元骏网卡，奖励将会在5个工作日内发放，请留意邮箱！
					String mailContent = StringUtil.format(ItemTips.恭喜您在圣诞大寻宝活动中获得x奖励, extOfflineReward);
					KSupportFactory.getMailModuleSupport().sendSimpleMailBySystem(role.getId(), ItemTips.圣诞大寻宝活动开奖邮件标题, mailContent);
				}

			} else {
				// 普通礼包
				addResult = new ItemResult_AddItem();
				addResult.newItemList = new ArrayList<KItem>();
				addResult.updateItemCountList = new ArrayList<KItem>();

				// 可以使用的道具数量
				int useMaxCount = isUseAll ? (int) result.item.getCount() : 1;
				if (result.item.getCount() < useMaxCount) {
					result.tips = ItemTips.物品数量不足;
					return result;
				}
				// 成功添加的货币及数量
				List<KCurrencyCountStruct> addMoneys = new ArrayList<KCurrencyCountStruct>();
				// 成功添加的宠物及数量
				Map<Integer, Integer> addPets = new HashMap<Integer, Integer>();
				// 成功添加的机甲
				Map<Integer, KMountTemplate> addMounts = new HashMap<Integer, KMountTemplate>();
				String tempResult = null;
				for (int i = 0; i < useMaxCount; i++) {
					tempResult = openRandomBoxIn(result.item, role, bag, itemTemp, addResult, successAddItems, addMoneys, addPets, addMounts);
					if (tempResult != null) {
						break;
					}
					result.useCount++;
				}

				if (result.useCount < 1) {
					result.tips = tempResult;
					return result;
				}

				// 扣道具数量
				result.item.changeCount(-result.useCount);

				result.isSucess = true;
				if (tempResult == null) {
					result.tips = ItemTips.使用物品成功;
				} else {
					result.tips = StringUtil.format(ItemTips.共成功使用x个x物品中断原因x, result.useCount, itemTemp.extItemName, tempResult);
				}

				// 打包提示
				result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, itemTemp.extItemName, result.useCount));
				if (!successAddItems.isEmpty()) {
					successAddItems = ItemCountStruct.mergeItemCountStructs(successAddItems);
					for (ItemCountStruct data : successAddItems) {
						result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, data.getItemTemplate().extItemName, data.itemCount));
					}
				}
				if (!addMoneys.isEmpty()) {
					addMoneys = KCurrencyCountStruct.mergeCurrencyCountStructs(addMoneys);
					for (KCurrencyCountStruct data : addMoneys) {
						result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, data.currencyType.extName, data.currencyCount));
					}
				}
				if (!addPets.isEmpty()) {
					for (Entry<Integer, Integer> entry : addPets.entrySet()) {
						KPetTemplate petTemp = KSupportFactory.getPetModuleSupport().getPetTemplate(entry.getKey());
						result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, petTemp.defaultName, entry.getValue()));
					}
				}
				if (!addMounts.isEmpty()) {
					for (KMountTemplate mount : addMounts.values()) {
						result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, mount.extName, 1));
					}
				}
			}
			return result;
		} finally {
			set.rwLock.unlock();

			if (result.isSucess) {
				// 同步道具
				KPushItemsMsg.pushItemCount(role.getId(), result.item.getId(), result.item.getCount());
				if (addResult != null) {
					Set<KItem> hset = Collections.emptySet();
					if (addResult.newItemList != null && !addResult.newItemList.isEmpty()) {
						hset = new HashSet<KItem>(addResult.newItemList);
						addResult.newItemList = new ArrayList<KItem>(hset);
						KPushItemsMsg.pushItems(role, addResult.newItemList);
					}
					if (addResult.updateItemCountList != null && !addResult.updateItemCountList.isEmpty()) {
						Set<KItem> hset2 = new HashSet<KItem>(addResult.updateItemCountList);
						hset2.removeAll(hset);
						addResult.updateItemCountList = new ArrayList<KItem>(hset2);
						KPushItemsMsg.pushItemCounts(role.getId(), addResult.updateItemCountList);
					}

					// 装备安装提示
					KPushNewTopEquiMsg.sendMsg(role.getId(), filterNewTopEquipment(role, addResult.newItemList));
					// 打开礼包提示
					KPushNewGiftMsg.sendMsg(role.getId(), filterNewGiftItem(role, addResult.newItemList, addResult.updateItemCountList));

					if (!successAddItems.isEmpty()) {
						// 财产日志
						FlowManager.logPropertyAdd(role.getId(), addResult, successAddItems, "开随机宝箱");
					}
				}
			}
		}
	}

	/**
	 * <pre>
	 * 纯加物品，不扣数量
	 * 
	 * @param item
	 * @param role
	 * @param bag
	 * @param itemTemp
	 * @param addResult
	 * @param addItems
	 * @param addMoneys
	 * @param addPets
	 * @return
	 * @author CamusHuang
	 * @creation 2014-8-6 上午10:10:15
	 * </pre>
	 */
	private static String openRandomBoxIn(KItem item, KRole role, KItemPack_Bag bag, KItemTempRandomBox itemTemp, ItemResult_AddItem addResult, List<ItemCountStruct> addItems,
			List<KCurrencyCountStruct> addMoneys, Map<Integer, Integer> addPets, Map<Integer, KMountTemplate> addMounts) {

		// by camus:20150318 防止利用背包满来开出特定奖励
		if (itemTemp.MinEmptyItemBag > 0) {
			if (bag.checkEmptyVolume() < itemTemp.MinEmptyItemBag) {
				return ItemTips.背包容量不足;
			}
		}
		
		// 随机得到物品或货币
		Object reward = itemTemp.random(KJobTypeEnum.getJob(role.getJob()), role.getLevel());

		// 加道具
		if (reward instanceof ItemCountStruct) {
			ItemCountStruct addItem = (ItemCountStruct) reward;
			ItemResult_AddItem tempResult = bag.addItem(addItem.getItemTemplate(), addItem.itemCount, null);
			if (!tempResult.isSucess) {
				return tempResult.tips;
			}

			if (tempResult.newItemList != null) {
				addResult.newItemList.addAll(tempResult.newItemList);
			}
			if (tempResult.updateItemCountList != null) {
				addResult.updateItemCountList.addAll(tempResult.updateItemCountList);
			}
			addItems.add(addItem);

		} else if (reward instanceof KCurrencyCountStruct) {
			// 加货币
			KCurrencyCountStruct addMoney = (KCurrencyCountStruct) reward;
			KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), addMoney, PresentPointTypeEnum.开宝箱, true);

			addMoneys.add(addMoney);
		} else if(reward instanceof KMountTemplate){
			// 加机甲
			KMountTemplate addMount = (KMountTemplate) reward;
			KActionResult<KMount> mountResult = KSupportFactory.getMountModuleSupport().presentMount(role, addMount, PresentPointTypeEnum.开宝箱.name());
			if (!mountResult.success) {
				return mountResult.tips;
			}

			addMounts.put(addMount.mountsID, addMount);
		} else {
			// 加宠物
			if (!KSupportFactory.getMissionSupport().checkFunctionIsOpen(role, KFunctionTypeEnum.随从)) {
				return ItemTips.请完成主线任务开启随从功能后再试;
			}

			Integer addPet = (Integer) reward;
			KActionResult<Pet> petResult = KSupportFactory.getPetModuleSupport().createPetToRole(role.getId(), addPet, KPetFlowType.使用道具.name());
			if (!petResult.success) {
				return petResult.tips;
			}

			Integer oldValue = addPets.get(addPet);
			addPets.put(addPet, oldValue == null ? 1 : oldValue + 1);
		}

		return null;
	}

	private static ItemResult_Use openEquiBox(KRole role, long useItemId, boolean isUseAll) {
		ItemResult_Use result = new ItemResult_Use();
		ItemResult_AddItem addResult = null;
		// 成功添加的道具及数量
		List<ItemCountStruct> successAddItems = new ArrayList<ItemCountStruct>();
		//
		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		set.rwLock.lock();
		try {

			KItemPack_Bag bag = set.getBag();

			result.item = bag.getItem(useItemId);// 主道具
			if (result.item == null || result.item.getCount() < 1) {
				result.tips = ItemTips.物品不存在;
				return result;
			}

			if (result.item.getItemTemplate().ItemType != KItemTypeEnum.装备包) {
				// 不是装备包道具
				result.tips = ItemTips.此物品不能使用;
				return result;
			}

			KItemTempEquiBox itemTemp = (KItemTempEquiBox) result.item.getItemTemplate();

			// 检查角色等级要求
			if (itemTemp.lvl > 0 && role.getLevel() < itemTemp.lvl) {
				result.tips = StringUtil.format(ItemTips.x级以上才能使用此物品, itemTemp.lvl);
				return result;
			}

			{
				addResult = new ItemResult_AddItem();
				addResult.newItemList = new ArrayList<KItem>();
				addResult.updateItemCountList = new ArrayList<KItem>();

				// 可以使用的道具数量
				int useMaxCount = isUseAll ? (int) result.item.getCount() : 1;
				String tempResult = null;
				for (int i = 0; i < useMaxCount; i++) {
					tempResult = openEquiBoxIn(result.item, role, bag, itemTemp, addResult, successAddItems);
					if (tempResult != null) {
						break;
					}
					result.useCount++;
				}

				if (result.useCount < 1) {
					result.tips = tempResult;
					return result;
				}

				result.isSucess = true;
				if (tempResult == null) {
					result.tips = ItemTips.使用物品成功;
				} else {
					result.tips = StringUtil.format(ItemTips.共成功使用x个x物品中断原因x, result.useCount, itemTemp.extItemName, tempResult);
				}

				// 打包提示
				result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, itemTemp.extItemName, result.useCount));
				successAddItems = ItemCountStruct.mergeItemCountStructs(successAddItems);
				for (ItemCountStruct data : successAddItems) {
					result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, data.getItemTemplate().extItemName, data.itemCount));
				}
			}
			return result;
		} finally {
			set.rwLock.unlock();

			if (result.isSucess) {
				// 同步道具
				KPushItemsMsg.pushItemCount(role.getId(), result.item.getId(), result.item.getCount());
				if (addResult != null) {
					Set<KItem> hset = Collections.emptySet();
					if (addResult.newItemList != null && !addResult.newItemList.isEmpty()) {
						hset = new HashSet<KItem>(addResult.newItemList);
						addResult.newItemList = new ArrayList<KItem>(hset);
						KPushItemsMsg.pushItems(role, addResult.newItemList);
					}
					if (addResult.updateItemCountList != null && !addResult.updateItemCountList.isEmpty()) {
						Set<KItem> hset2 = new HashSet<KItem>(addResult.updateItemCountList);
						hset2.removeAll(hset);
						addResult.updateItemCountList = new ArrayList<KItem>(hset2);
						KPushItemsMsg.pushItemCounts(role.getId(), addResult.updateItemCountList);
					}

					// 装备安装提示
					KPushNewTopEquiMsg.sendMsg(role.getId(), filterNewTopEquipment(role, addResult.newItemList));
					// 打开礼包提示
					KPushNewGiftMsg.sendMsg(role.getId(), filterNewGiftItem(role, addResult.newItemList, addResult.updateItemCountList));

					// 财产日志
					FlowManager.logPropertyAdd(role.getId(), addResult, successAddItems, "开装备宝箱");
				}
			}
		}
	}

	private static String openEquiBoxIn(KItem item, KRole role, KItemPack_Bag bag, KItemTempEquiBox itemTemp, ItemResult_AddItem addResult, List<ItemCountStruct> addItems) {

		// by camus:20150318 防止利用背包满来开出特定奖励
		if (itemTemp.MinEmptyItemBag > 0) {
			if (bag.checkEmptyVolume() < itemTemp.MinEmptyItemBag) {
				return ItemTips.背包容量不足;
			}
		}
		
		// 随机得到物品
		ItemCountStruct addItem = itemTemp.random(KJobTypeEnum.getJob(role.getJob()), role.getLevel());

		// 加道具
		{
			ItemResult_AddItem tempResult = bag.addItem(addItem.getItemTemplate(), addItem.itemCount, null);
			if (!tempResult.isSucess) {
				return tempResult.tips;
			}

			if (tempResult.newItemList != null) {
				addResult.newItemList.addAll(tempResult.newItemList);
			}
			if (tempResult.updateItemCountList != null) {
				addResult.updateItemCountList.addAll(tempResult.updateItemCountList);
			}
			addItems.add(addItem);
		}
		
		// 扣道具
		if (item.changeCount(-1) < 0) {
//			return ItemTips.物品数量不足;
		}

		return null;
	}

	/**
	 * <pre>
	 * 消耗指定的材料，合成新道具
	 * 
	 * @deprecated 专用于合成材料，会根据材料表头执行扣费和成功率
	 * @param role
	 * @param itemId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-3 下午4:24:30
	 * </pre>
	 */
	public static CommonResult_Ext dealMsg_composeItem(KRole role, long itemId) {

		CommonResult_Ext result = new CommonResult_Ext();
		result.tips = GlobalTips.服务器繁忙请稍候再试;
		return result;
		//
		// KItem item = null;
		// KItemTempMaterial temp = null;
		// ItemResult_AddItem addResult = null;
		// //
		// KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		// set.rwLock.lock();
		// try {
		// KItemPack_Bag bag = set.getBag();
		//
		// item = bag.getItem(itemId);
		// if (item == null) {
		// result.tips = ItemTips.物品不存在;
		// return result;
		// }
		//
		// KItemTempAbs tempAbs = item.getItemTemplate();
		// if (tempAbs.ItemType != KItemTypeEnum.材料) {
		// result.tips = ItemTips.指定的物品不能合成;
		// return result;
		// }
		//
		// temp = (KItemTempMaterial) tempAbs;
		//
		// if (temp.composeTarget == null) {
		// result.tips = ItemTips.指定的物品不能合成;
		// return result;
		// }
		//
		// if (!bag.isBagCanAddItem(temp.composeTarget)) {
		// result.tips = ItemTips.背包容量不足;
		// return result;
		// }
		//
		// if (item.getCount() < temp.compnum) {
		// result.tips = ItemTips.物品数量不足;
		// return result;
		// }
		//
		// result.dataUprisingTips = new ArrayList<String>();
		//
		// if (temp.composePrice.currencyCount > 0) {
		// if (KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(),
		// temp.composePrice, UsePointFunctionTypeEnum.道具合成消耗, true) < 0) {
		// result.isGoMoneyUI = true;
		// result.goMoneyUIType = temp.composePrice.currencyType;
		// result.tips = StringUtil.format(ShopTips.x货币数量不足x,
		// temp.composePrice.currencyType.extName,
		// temp.composePrice.currencyCount);
		// return result;
		// }
		// result.addDataUprisingTips(StringUtil.format(ShopTips.x减x,
		// temp.composePrice.currencyType.extName,
		// temp.composePrice.currencyCount));
		// }
		//
		// // 执行材料扣取
		// long resultCount = item.changeCount(-temp.compnum);
		// if (resultCount < 0) {
		// result.tips = ItemTips.物品数量不足;
		// return result;
		// }
		//
		// // 加新道具
		// addResult = bag.addItem(temp.composeTarget.getItemTemplate(),
		// temp.composeTarget.itemCount, null);
		// if (!addResult.isSucess) {
		// _LOGGER.warn("[警告]--角色(ID:{}) 执行合成道具，生成新道具失败！目标道具Code:{}!",
		// role.getId(), temp.composeTarget.itemCode);
		// result.tips = addResult.tips;
		// return result;
		// }
		//
		// result.isSucess = true;
		// result.tips = ItemTips.物品合成成功;
		//
		// result.addDataUprisingTips(StringUtil.format(ShopTips.x减x,
		// tempAbs.extItemName, temp.compnum));
		// result.addDataUprisingTips(StringUtil.format(ShopTips.x加x,
		// temp.composeTarget.getItemTemplate().extItemName,
		// temp.composeTarget.itemCount));
		// return result;
		// } finally {
		// set.rwLock.unlock();
		//
		// if (result.isSucess) {
		// // 同步道具
		// KPushItemsMsg.pushItemCount(role.getId(), item.getId(),
		// item.getCount());
		// KPushItemsMsg.pushItems(role, addResult.newItemList);
		// KPushItemsMsg.pushItemCounts(role.getId(),
		// addResult.updateItemCountList);
		// }
		// }
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 专用于合成宝石，分为金币合成和宝石合成，会根据材料表头执行扣费和成功率，并在失败时损失宝石
	 * @param role
	 * @param itemId
	 * @param isCommon
	 * @return
	 * @author CamusHuang
	 * @creation 2014-5-23 上午10:21:58
	 * </pre>
	 */
	public static CommonResult_Ext dealMsg_composeStone(KRole role, long itemId, boolean isCommon) {
		CommonResult_Ext result = new CommonResult_Ext();
		result.tips = GlobalTips.服务器繁忙请稍候再试;
		return result;

		// KItem item = null;
		// KItemTempStone temp = null;
		// ItemResult_AddItem addResult = null;
		// boolean isSuccess = false;
		// //
		// KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		// set.rwLock.lock();
		// try {
		// KItemPack_Bag bag = set.getBag();
		//
		// item = bag.getItem(itemId);
		// if (item == null) {
		// result.tips = ItemTips.物品不存在;
		// return result;
		// }
		//
		// KItemTempAbs tempAbs = item.getItemTemplate();
		// if (tempAbs.ItemType != KItemTypeEnum.宝石) {
		// result.tips = ItemTips.指定的物品不能合成;
		// return result;
		// }
		//
		// temp = (KItemTempStone) tempAbs;
		//
		// if (temp.composeTarget == null) {
		// result.tips = ItemTips.指定的物品不能合成;
		// return result;
		// }
		//
		// if (!bag.isBagCanAddItem(temp.composeTarget)) {
		// result.tips = ItemTips.背包容量不足;
		// return result;
		// }
		//
		// if (item.getCount() < temp.compnum) {
		// result.tips = ItemTips.物品数量不足;
		// return result;
		// }
		//
		// KCurrencyCountStruct composePrice = null;
		// int rate = 0;
		// if (isCommon) {
		// composePrice = temp.composePriceGold;
		// rate = temp.composePriceGoldSuccessRate;
		// } else {
		// composePrice = temp.composePriceDiamond;
		// rate = temp.composePriceDiamondSuccessRate;
		// }
		//
		// result.dataUprisingTips = new ArrayList<String>();
		//
		// if (composePrice != null && composePrice.currencyCount > 0) {
		// if (KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(),
		// composePrice, UsePointFunctionTypeEnum.道具合成消耗, true) < 0) {
		// result.isGoMoneyUI = true;
		// result.goMoneyUIType = composePrice.currencyType;
		// result.tips = StringUtil.format(ShopTips.x货币数量不足x,
		// composePrice.currencyType.extName, composePrice.currencyCount);
		// return result;
		// }
		// result.addDataUprisingTips(StringUtil.format(ShopTips.x减x,
		// composePrice.currencyType.extName, composePrice.currencyCount));
		// }
		// isSuccess = true;
		//
		// if (UtilTool.random(1, 10000) > rate) {
		// // 合成失败
		// // 执行材料扣取
		// int deleteStoneNum = temp.randomLoseNum();
		// if (deleteStoneNum > 0) {
		// long resultCount = item.changeCount(-deleteStoneNum);
		// if (resultCount < 0) {
		// result.tips = ItemTips.物品数量不足;
		// return result;
		// }
		// result.addDataUprisingTips(StringUtil.format(ShopTips.x减x,
		// temp.extItemName, deleteStoneNum));
		// }
		//
		// result.tips = ItemTips.宝石合成失败;
		// return result;
		// } else {
		// // 合成成功
		// // 执行材料扣取
		// long resultCount = item.changeCount(-temp.compnum);
		// if (resultCount < 0) {
		// result.tips = ItemTips.物品数量不足;
		// return result;
		// }
		//
		// // 加新道具
		// addResult = bag.addItem(temp.composeTarget.getItemTemplate(),
		// temp.composeTarget.itemCount, null);
		// if (!addResult.isSucess) {
		// _LOGGER.warn("[警告]--角色(ID:{}) 执行合成道具，生成新道具失败！目标道具Code:{}!",
		// role.getId(), temp.composeTarget.itemCode);
		// result.tips = addResult.tips;
		// return result;
		// }
		//
		// result.isSucess = true;
		// result.tips = ItemTips.宝石合成成功;
		// result.addDataUprisingTips(StringUtil.format(ShopTips.x减x,
		// temp.extItemName, temp.compnum));
		// result.addDataUprisingTips(StringUtil.format(ShopTips.x加x,
		// temp.composeTarget.getItemTemplate().extItemName,
		// temp.composeTarget.itemCount));
		// return result;
		// }
		// } finally {
		// set.rwLock.unlock();
		//
		// if (isSuccess) {
		// // 同步道具
		// KPushItemsMsg.pushItemCount(role.getId(), item.getId(),
		// item.getCount());
		// if (addResult != null) {
		// KPushItemsMsg.pushItems(role, addResult.newItemList);
		// KPushItemsMsg.pushItemCounts(role.getId(),
		// addResult.updateItemCountList);
		// }
		// }
		// }
	}

	/**
	 * <pre>
	 * 合成改版:专用于合成物品（包含材料和宝石）
	 * 如果合成物品有多种可能，则会返回{@link #SM_COMPOSE_LIST}
	 * 如果需要付费，则会返回Dialog进行二次确认
	 * 
	 * @param role
	 * @param itemId
	 * @param isConfirmPay 是否经过付费确认
	 * @param isComposeAll 是否同意合成多个
	 * @return
	 * @author CamusHuang
	 * @creation 2014-5-23 上午11:09:59
	 * </pre>
	 */
	public static ItemResult_Compose dealMsg_compose2(KRole role, long itemId, boolean isConfirmPay, boolean isComposeAll, String targetItemCode) {

		ItemResult_Compose result = new ItemResult_Compose();
		KItem item = null;
		ItemResult_AddItem addResult = null;
		int compnum = 0;// 消耗量
		//
		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		set.rwLock.lock();
		try {
			KItemPack_Bag bag = set.getBag();

			item = bag.getItem(itemId);
			if (item == null) {
				result.tips = ItemTips.物品不存在;
				return result;
			}

			KItemTempAbs tempAbs = item.getItemTemplate();

			int lvLimit = 0;// 角色等级限制
			ItemCountStruct composeTarget = null;
			KCurrencyCountStruct composePrice = null;
			{
				if (tempAbs.ItemType == KItemTypeEnum.改造材料) {
					KItemTempMaterial temp = (KItemTempMaterial) tempAbs;
					if (!temp.composeTarget.isEmpty()) {
						if (temp.composeTarget.size() > 1) {
							// 结果有多种可选
							if (targetItemCode == null) {
								// 未选定，则会返回{@link #SM_COMPOSE_LIST}
								KComposeListSelectMsg.pushComposeList(role, itemId, temp);
								return result;
							}

							composeTarget = temp.composeTarget.get(targetItemCode);
							if (composeTarget == null) {
								// 非法参数
								result.tips = ItemTips.物品不存在;
								return result;
							}

							// 有多种可选时，默认只能每次合成一个
							isComposeAll = false;
						} else {
							// 结果只有一种
							composeTarget = temp.composeTarget.values().iterator().next();
						}

						// 价格
						composePrice = temp.composeTargetPrice.get(composeTarget.itemCode);
						compnum = temp.compnum;
					}

					lvLimit = temp.lvl;
				} else if (tempAbs.ItemType == KItemTypeEnum.宝石) {
					KItemTempStone temp = (KItemTempStone) tempAbs;
					composeTarget = temp.composeTarget;
					compnum = temp.compnum;

					lvLimit = ((KItemTempStone) composeTarget.getItemTemplate()).complv;
				} else {
					result.tips = ItemTips.此物品不能合成;
					return result;
				}
			}

			if (composeTarget == null) {
				result.tips = ItemTips.此物品不能合成;
				return result;
			}

			if (role.getLevel() < lvLimit) {
				// 合成失败，人物等级达到XX以上方可合成下一等级宝石。
				result.tips = StringUtil.format(ItemTips.合成失败要求等级x, lvLimit);
				return result;
			}

			if (item.getCount() < compnum) {
				// 数量不足以合成一个
				result.tips = ItemTips.物品数量不足;
				return result;
			}

			result.successTime = 1;

			// 如果全部合成，则重新计算消耗、价格、结果
			if (isComposeAll) {
				result.successTime = (int) (item.getCount() / compnum);
				if (result.successTime > 1) {
					compnum = compnum * result.successTime;
					composeTarget = new ItemCountStruct(composeTarget.getItemTemplate(), composeTarget.itemCount * result.successTime);
					if (composePrice != null) {
						composePrice = new KCurrencyCountStruct(composePrice.currencyType, composePrice.currencyCount * result.successTime);
					}
				}
			}

			if (composePrice != null && !isConfirmPay) {
				// 未确认付费
				result.isGoConfirmPay = true;
				result.tips = StringUtil.format(ItemTips.是否花费x数量x货币合成x数量x物品, composePrice.currencyCount, composePrice.currencyType.extName, composeTarget.getItemTemplate().extItemName,
						composeTarget.itemCount);
				return result;
			}

			if (!bag.isBagCanAddItem(composeTarget)) {
				result.tips = ItemTips.背包容量不足;
				return result;
			}

			// 数量不足以合成指定数量
			if (item.getCount() < compnum) {
				result.tips = ItemTips.物品数量不足;
				return result;
			}

			// 付费
			if (composePrice != null) {
				if (KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), composePrice, UsePointFunctionTypeEnum.道具合成消耗, true) < 0) {
					result.isGoMoneyUI = true;
					result.goMoneyUIType = composePrice.currencyType;
					result.goMoneyUICount = composePrice.currencyCount - KSupportFactory.getCurrencySupport().getMoney(role.getId(), composePrice.currencyType);
					result.tips = StringUtil.format(ShopTips.x货币数量不足x, composePrice.currencyType.extName, composePrice.currencyCount);
					return result;
				}
			}

			// 执行材料扣取
			long resultCount = item.changeCount(-compnum);
			if (resultCount < 0) {
				// 付费回滚
				if (composePrice != null) {
					KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), composePrice, PresentPointTypeEnum.回滚, true);
				}

				result.tips = ItemTips.物品数量不足;
				return result;
			}

			// 加新道具
			addResult = bag.addItem(composeTarget.getItemTemplate(), composeTarget.itemCount, null);
			if (!addResult.isSucess) {
				_LOGGER.error("[警告]--角色(ID:{}) 执行合成道具，生成新道具失败！目标道具Code:{},数量:{}!", role.getId(), composeTarget.itemCode, composeTarget.itemCount);
				result.tips = addResult.tips;
				return result;
			}

			result.isSucess = true;
			result.tips = ItemTips.物品合成成功;
			result.itemTemp = tempAbs;
			if (composePrice != null) {
				result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, composePrice.currencyType.extName, composePrice.currencyCount));
			}
			result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, tempAbs.extItemName, compnum));
			result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, composeTarget.getItemTemplate().extItemName, composeTarget.itemCount));
			return result;
		} finally {
			set.rwLock.unlock();

			if (result.isSucess) {
				// 同步道具
				KPushItemsMsg.pushItemCount(role.getId(), item.getId(), item.getCount());
				KPushItemsMsg.pushItems(role, addResult.newItemList);
				KPushItemsMsg.pushItemCounts(role.getId(), addResult.updateItemCountList);

				// 财产日志
				FlowManager.logPropertyAdd(role.getId(), addResult, null, "合成");
				FlowManager.logPropertyDelete(role.getId(), item, compnum, "合成");
			}
		}
	}

	/**
	 * <pre>
	 * 将角色背包中的道具，安装到指定的装备栏
	 * 如果已经有装备，先卸载再安装
	 * 
	 * @param role
	 * @param itemId
	 * @param slotId
	 * @author CamusHuang
	 * @creation 2012-11-20 下午4:48:13
	 * </pre>
	 */
	public static ItemResult_Equi dealMsg_installEquipment(KRole role, long itemId, long slotId) {

		long roleId = role.getId();
		ItemResult_Equi result = new ItemResult_Equi();
		//
		KItemSet set = KItemModuleExtension.getItemSet(roleId);
		set.rwLock.lock();
		try {
			KItemPack_Bag bag = set.getBag();
			KItemPack_BodySlot slot = set.getSlot();

			KItem item = bag.getItem(itemId);
			if (item == null) {
				result.tips = ItemTips.装备不存在;
				return result;
			}

			KItemTempAbs tempAbs = item.getItemTemplate();
			if (tempAbs.ItemType != KItemTypeEnum.装备) {
				// 不是装备类型
				result.tips = ItemTips.此物品不是装备;
				return result;
			}

			// 检查角色等级要求
			if (tempAbs.lvl > 0 && role.getLevel() < tempAbs.lvl) {
				result.tips = StringUtil.format(ItemTips.x级以上才能使用此物品, tempAbs.lvl);
				return result;
			}

			KItemTempEqui temp = (KItemTempEqui) tempAbs;
			// 职业限制
			if (slotId == KItemConfig.MAIN_BODYSLOT_ID) {
				if (temp.jobEnum != null && role.getJob() != temp.jobEnum.getJobType()) {
					result.tips = ItemTips.您的职业不能安装此装备;
					return result;
				}
			} else {
				// CTODO
			}

			// 如果装备栏已经有装备，先卸载再安装
			KItem oldItem = slot.searchSlotItem(slotId, temp.typeEnum);
			if (oldItem != null) {
				// 将装备从装备栏移除再放入背包
				slot.uninstallItem(oldItem.getId());
				bag.moveInItem(oldItem);
			}

			// 将装备从背包移除再放入装备栏
			bag.moveOutItem(item.getId());
			slot.installItem(slotId, item);
			// 重算套装数据
			result.isSetChange = slot.recountEquiSetData(slotId);
			result.notifySlotId = slotId;

			result.isSucess = true;
			result.tips = ItemTips.穿戴装备成功;
			return result;
		} finally {
			set.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 卸载装备
	 * 
	 * @param roleId
	 * @param itemId
	 * @param slotId
	 * @return
	 * @author CamusHuang
	 * @creation 2012-11-20 下午4:59:26
	 * </pre>
	 */
	public static ItemResult_Equi dealMsg_uninstallEquipment(long roleId, long itemId, long slotId) {

		ItemResult_Equi result = new ItemResult_Equi();
		//
		KItemSet set = KItemModuleExtension.getItemSet(roleId);
		set.rwLock.lock();
		try {
			KItemPack_Bag bag = set.getBag();
			KItemPack_BodySlot slot = set.getSlot();

			Map<Long, KItem> itemMap = slot.searchSlotItemMap(slotId);
			KItem item = itemMap.get(itemId);
			if (item == null) {
				result.tips = ItemTips.未安装此装备;
				return result;
			}

			if (!bag.isBagCanAddItems(Arrays.asList(new ItemCountStruct(item.getItemCode(), item.getCount())))) {
				result.tips = ItemTips.背包容量不足;
				return result;
			}

			// 将装备从装备栏移除，将装备放入背包
			slot.uninstallItem(itemId);
			bag.moveInItem(item);
			// 重算套装数据
			result.isSetChange = slot.recountEquiSetData(slotId);
			result.notifySlotId = slotId;
			//
			result.isSucess = true;
			result.tips = ItemTips.卸下装备成功;
			return result;
		} finally {
			set.rwLock.unlock();
		}
	}

	public static CommonResult_Ext dealMsg_buyItem(KRole role, String itemCode, int buyCount) {
		ItemResult_Item result = new ItemResult_Item();
		//
		buyCount = Math.abs(buyCount);
		KItemTempAbs itemTemp = KItemDataManager.mItemTemplateManager.getItemTemplate(itemCode);
		if (itemTemp == null) {
			result.tips = ItemTips.物品不存在;
			return result;
		}

		if (itemTemp.buyMoney == null) {
			result.tips = ShopTips.此物品不允许购买;
			return result;
		}

		// 强力纠正数量
		int maxBuyCount = (int) (Integer.MAX_VALUE / itemTemp.buyMoney.currencyCount);
		if (buyCount > maxBuyCount) {
			buyCount = maxBuyCount;
		}

		// 限时活动：材料打折
		int discount = 0;
		{
			TimeLimieProduceActivity activity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.材料打折);
			if (activity != null && activity.isActivityTakeEffectNow()) {
				if (activity.discountItemCodeSet.contains(itemCode)) {
					discount = activity.discount;
				}
			}
		}

		long orgMoney = itemTemp.buyMoney.currencyCount * buyCount;
		long moneyCount = orgMoney;
		if (discount > 0) {
			// 打折
			moneyCount = moneyCount * discount / 100;
		}
		//
		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		set.rwLock.lock();
		try {

			KItemPack_Bag bag = set.getBag();
			ItemCountStruct itemStruct = new ItemCountStruct(itemTemp, buyCount);
			if (!bag.isBagCanAddItem(itemStruct)) {
				result.tips = ItemTips.背包容量不足;
				return result;
			}

			if (KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), itemTemp.buyMoney.currencyType, moneyCount, UsePointFunctionTypeEnum.购买系统道具, true) < 0) {
				result.isGoMoneyUI = true;
				result.goMoneyUIType = itemTemp.buyMoney.currencyType;
				result.goMoneyUICount = moneyCount-KSupportFactory.getCurrencySupport().getMoney(role.getId(), itemTemp.buyMoney.currencyType);
				result.tips = StringUtil.format(ShopTips.x货币数量不足x, itemTemp.buyMoney.currencyType.extName, moneyCount);
				return result;
			}

			ItemResult_AddItem addResult = addItemToBag(role, itemTemp, buyCount, KShopTypeEnum.普通商店.name());
			if (!addResult.isSucess) {
				result.tips = addResult.tips;
				return result;
			}

			// 记录流水
			if (itemTemp.buyMoney.currencyType == KCurrencyTypeEnum.DIAMOND) {
				long itemId = 0;
				if (addResult.newItemList != null && !addResult.newItemList.isEmpty()) {
					itemId = addResult.newItemList.get(0).getId();
				} else if (addResult.updateItemCountList != null && !addResult.updateItemCountList.isEmpty()) {
					itemId = addResult.updateItemCountList.get(0).getId();
				}
				FlowDataModuleFactory.getModule().recordBuyItemUsePoint(role, itemTemp.itemCode, itemTemp.name, itemId, buyCount, (int) moneyCount, KShopTypeEnum.普通商店.sign);
			}

			result.isSucess = true;
			if (discount > 0) {
				// 打折
				result.tips = StringUtil
						.format(ShopTips.购买成功打x折原价x数量x货币现价x数量x货币, discount / 10.0, orgMoney, itemTemp.buyMoney.currencyType.extName, moneyCount, itemTemp.buyMoney.currencyType.extName);// 购买成功，打85折，原价100钻石，现价85钻石
			} else {
				result.tips = ShopTips.购买成功;// 购买成功，打85折，原价100钻石，现价85钻石
			}
			result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, itemTemp.buyMoney.currencyType.extName, moneyCount));
			return result;
		} finally {
			set.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 出售角色<strong>背包</strong>指定itemId的指定数量(count)
	 * 内部会与客户端进行道具同步
	 * 若出售价格<1则不允许出售
	 * 
	 * @param roleId
	 * @param itemId
	 * @param count 只有可合并道具才允许>1
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-8 下午5:00:41
	 * </pre>
	 */
	public static ItemResult_Item dealMsg_sellItemFromBag(KRole role, long itemId, long count) {
		ItemResult_Item result = new ItemResult_Item();
		//
		KItemTempAbs itemTemp = null;
		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		set.rwLock.lock();
		try {
			KItemPack_Bag bag = set.getBag();
			KItem item = bag.getItem(itemId);
			if (item == null) {
				result.tips = ItemTips.物品不存在;
				return result;
			}

			itemTemp = item.getItemTemplate();
			if (itemTemp.sellMoney == null) {
				result.tips = ItemTips.此物品不能出售;
				return result;
			}

			boolean isSucess = removeItemFromBag(role.getId(), itemId, count, "出售");
			if (!isSucess) {
				result.tips = ItemTips.物品数量不足;
				return result;
			}

			long addMoney = itemTemp.sellMoney.currencyCount * count;

			// 加货币
			KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), itemTemp.sellMoney.currencyType, addMoney, PresentPointTypeEnum.出售道具, true);

			result.isSucess = true;
			result.tips = ShopTips.出售物品成功;
			result.item = item;
			result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, itemTemp.sellMoney.currencyType.extName, addMoney));
			return result;
		} finally {
			set.rwLock.unlock();
		}
	}

	public static CommonResult_Ext dealMsg_resolveEquipment(KRole role, long itemId) {
		CommonResult_Ext result = new CommonResult_Ext();
		KItemTempEqui itemTemp = null;
		ItemCountStruct addItem = null;
		ItemResult_AddItem addResult = null;
		List<ItemCountStruct> addStones = null;
		KItem equiItem = null;
		//
		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		set.rwLock.lock();
		try {

			KItemPack_Bag bag = set.getBag();

			equiItem = bag.getItem(itemId);// 主道具
			if (equiItem == null || equiItem.getCount() < 1) {
				result.tips = ItemTips.装备不存在;
				return result;
			}

			if (equiItem.getItemTemplate().ItemType != KItemTypeEnum.装备) {
				result.tips = ItemTips.此物品不是装备;
				return result;
			}

			itemTemp = (KItemTempEqui) equiItem.getItemTemplate();

			List<ItemCountStruct> allAddStones = new ArrayList<ItemCountStruct>();
			KItem_EquipmentData equiData = equiItem.getEquipmentData();
			for (String stoneItemCode : equiData.getEnchanseCache().values()) {
				KItemTempStone stoneTemp = (KItemTempStone) KItemDataManager.mItemTemplateManager.getItemTemplate(stoneItemCode);
				allAddStones.add(new ItemCountStruct(stoneTemp, 1));
			}

			{
				List<ItemCountStruct> addItems = new ArrayList<ItemCountStruct>();
				addItems.addAll(itemTemp.resolveItems);
				addItems.addAll(allAddStones);
				addItems = ItemCountStruct.mergeItemCountStructs(addItems);
				if (!addItems.isEmpty()) {
					if (!bag.isBagCanAddItems(addItems)) {
						// 容量不足
						result.tips = ItemTips.背包容量不足;
						return result;
					}
				}
			}

			// 扣道具
			equiItem.changeCount(-equiItem.getCount());
			// if (bag.deleteItem(itemId) == null) {
			// result.tips = ItemTips.物品数量不足;
			// return result;
			// }

			// 加道具
			{
				addStones = new ArrayList<ItemCountStruct>();
				addStones.addAll(allAddStones);

				addItem = itemTemp.randomResolveItem();
				if (addItem != null) {
					addStones.add(addItem);
				}

				if (!addStones.isEmpty()) {
					addResult = bag.addItems(addStones, null);
					if (!addResult.isSucess) {
						result.tips = addResult.tips;
						return result;
					}
					for (ItemCountStruct struct : addStones) {
						result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, struct.getItemTemplate().extItemName, struct.itemCount));
					}
				}
			}

			// 加货币
			if (itemTemp.resolveMoney != null) {
				KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), itemTemp.resolveMoney, PresentPointTypeEnum.开宝箱, true);
				result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, itemTemp.resolveMoney.currencyType.extName, itemTemp.resolveMoney.currencyCount));
			}

			result.isSucess = true;
			result.tips = ItemTips.装备分解成功;
			return result;
		} finally {
			set.rwLock.unlock();

			if (result.isSucess) {
				// 同步道具
				KPushItemsMsg.pushItemCount(role.getId(), itemId, 0);
				if (addResult != null) {
					KPushItemsMsg.pushItems(role, addResult.newItemList);
					KPushItemsMsg.pushItemCounts(role.getId(), addResult.updateItemCountList);

					// 装备安装提示
					KPushNewTopEquiMsg.sendMsg(role.getId(), filterNewTopEquipment(role, addResult.newItemList));
					// 打开礼包提示
					KPushNewGiftMsg.sendMsg(role.getId(), filterNewGiftItem(role, addResult.newItemList, addResult.updateItemCountList));
				}

				// 财产日志
				if (addResult != null) {
					FlowManager.logPropertyAdd(role.getId(), addResult, addStones, "装备分解");
				}
				FlowManager.logPropertyDelete(role.getId(), equiItem, 1, "装备分解");
			}
		}
	}

	public static void clearBagForTestOrder(long roleId) {
		KItemSet set = KItemModuleExtension.getItemSet(roleId);
		set.rwLock.lock();
		try {
			KItemPack_Bag bag = set.getBag();
			Map<Long, KItem> map = new HashMap<Long, KItem>(bag.getAllItemsCache());
			for (KItem item : map.values()) {
				removeItemFromBag(roleId, item.getId(), item.getCount(), "指令");
			}
		} finally {
			set.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 强化装备
	 * 
	 * @param role
	 * @param itemId -1表示全身
	 * @param times 一定>0
	 * @return
	 * @author CamusHuang
	 * @creation 2015-1-27 下午4:31:51
	 * </pre>
	 */
	public static ItemResult_EquiStrong dealMsg_strongEquipment(KRole role, long itemId, int times) {

		ItemResult_EquiStrong result = new ItemResult_EquiStrong();
		List<KItem> changeItems = new ArrayList<KItem>();
		//
		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		KItemPack_BodySlot slot = set.getSlot();
		//
		set.rwLock.lock();
		try {
			Map<Long, KItem> allEquiMap = slot.searchSlotItemMap(KItemConfig.MAIN_BODYSLOT_ID);
			if(allEquiMap.isEmpty()){
				result.tips = ItemTips.装备不存在;
				return result;
			}
			if(itemId>0 && !allEquiMap.containsKey(itemId)){
				result.tips = ItemTips.装备不存在;
				return result;
			}
			//
			int totalSuccessTime = 0;
			int totalStrongAddLv = 0;
			boolean isBreakAllEqui = false;
			Map<KCurrencyTypeEnum, AtomicLong> totalPayMoneyMap=new HashMap<KCurrencyTypeEnum, AtomicLong>();
			//
			
			for(KItem item : allEquiMap.values()){
				KItemTempAbs tempAbs = item.getItemTemplate();
				if (tempAbs.ItemType != KItemTypeEnum.装备) {
					continue;
				}
				if (itemId > 0 && item.getId() != itemId) {
					continue;
				}
				//
				int orgStrongLv = item.getEquipmentData().getStrongLv();
				int successTime = 0;
				//
				for (int time = 0; time < times; time++) {
					ItemResult_EquiStrongIn tempResult = strongEquipment(role, item, null);
					//
					result.addDataUprisingTips(tempResult.getDataUprisingTips());
					result.addUprisingTips(tempResult.getUprisingTips());
					//
					if (tempResult.isSucess) {
						successTime++;
						totalSuccessTime++;
						totalStrongAddLv += tempResult.strongAddLv;
						AtomicLong payMoney = totalPayMoneyMap.get(tempResult.payType);
						if(payMoney==null){
							payMoney = new AtomicLong();
							totalPayMoneyMap.put(tempResult.payType, payMoney);
						}
						payMoney.addAndGet(tempResult.payCount);
						//
					} else if(tempResult.isBreak){
						// 遇到严重原因失败，取消所有装备的强化操作
						isBreakAllEqui = true;
						result.tips = tempResult.tips;
						result.showMoneyTips = tempResult.showMoneyTips;
						result.isGoMoneyUI = tempResult.isGoMoneyUI;
						result.goMoneyUIType = tempResult.goMoneyUIType;
						result.goMoneyUICount = tempResult.goMoneyUICount;
						result.isGoVip = tempResult.isGoVip;
						result.showVipTips = tempResult.showVipTips;
						break;
					} else {
						// 强化满级，取消本装备的所有强化操作
						break;
					}
				}
				
				if (successTime > 0) {
					// 至少成功强化1次
					int nowStrongLv = item.getEquipmentData().getStrongLv();
					if(orgStrongLv!=nowStrongLv){
						changeItems.add(item);
					}
					
					if (nowStrongLv >= KRoleModuleConfig.getRoleMaxLv()) {
						result.fullStrongEquiNames.add(tempAbs.extItemName);
					}
					
					// 财产日志
					String tips = StringUtil.format("装备强化;dbId:{};成功次数:{};原等级:{};现强化:{}", item.getId(), successTime, orgStrongLv, nowStrongLv);
					FlowManager.logPropertyModify(role.getId(), PropertyTypeEnum.道具, item.getUUID(), tempAbs.id, tempAbs.name, tips);
				}
				
				if(isBreakAllEqui){
					break;
				}
			}

			if (totalSuccessTime < 1) {
				// 一次也没成功：装备全满级？没钱？
				return result;
			}
			
			result.isSucess = true;
			result.successTime = totalSuccessTime;
			result.strongAddLv = totalStrongAddLv;
			result.tips = StringUtil.format(ItemTips.共成功强化x次提升x级, totalSuccessTime, totalStrongAddLv);
			
			if(!totalPayMoneyMap.isEmpty()){
				// 同步所有货币
				KSupportFactory.getCurrencySupport().synCurrencyDataToClient(role.getId());
				for (Entry<KCurrencyTypeEnum, AtomicLong> e:totalPayMoneyMap.entrySet()) {
					result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, e.getKey().extName, e.getValue().get()));
				}
			}
			
			{
				// 需要通知角色模块，重算套装数据
				result.notifySlotId = KItemConfig.MAIN_BODYSLOT_ID;
				result.isSetChange = slot.recountEquiSetData(result.notifySlotId);
			}
			return result;
		} finally {
			set.rwLock.unlock();

			if (result.isSucess) {
				// 同步装备属性
				KPushItemsMsg.pushItems(role, changeItems);
			}
		}
	}
	
	/**
	 * <pre>
	 * 强化装备 
	 * 可以处于背包或装备栏
	 * 
	 * @deprecated 旧的方法
	 * @param role
	 * @param equiItemIdA
	 * @param isPayForBuyStrongStone
	 * @return
	 * @author CamusHuang
	 * @creation 2012-11-22 下午4:07:10
	 * </pre>
	 */
	public static ItemResult_EquiStrongOld dealMsg_strongEquipmentOld(KRole role, long itemId, int times) {

		ItemResult_EquiStrongOld result = new ItemResult_EquiStrongOld();
		KItem item = null;
		KItemTempAbs tempAbs = null;

		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		KItemPack_Bag bag = set.getBag();
		KItemPack_BodySlot slot = set.getSlot();

		set.rwLock.lock();
		try {
			item = bag.getItem(itemId);// 装备
			if (item == null) {
				item = slot.getItem(itemId);
			}

			if (item == null) {
				result.tips = ItemTips.装备不存在;
				return result;
			}

			tempAbs = item.getItemTemplate();
			if (tempAbs.ItemType != KItemTypeEnum.装备) {
				result.tips = ItemTips.装备不存在;
				return result;
			}

			int successTime = 0;
			result.orgStrongLv = item.getEquipmentData().getStrongLv();
			ItemResult_EquiStrongOld tempResult = null;
			KCurrencyTypeEnum payType = null;
			long payCount = 0;
			for (int time = 0; time < times; time++) {
				tempResult = strongEquipmentOld(role, item, null);
				//
				result.addDataUprisingTips(tempResult.getDataUprisingTips());
				result.addUprisingTips(tempResult.getUprisingTips());
				//
				if (tempResult.isSucess) {
					successTime++;
					result.strongAddLv += tempResult.strongAddLv;
					payType = tempResult.payType;
					payCount += tempResult.payCount;
				} else {
					// 遇到严重原因失败
					result.tips = tempResult.tips;
					result.isGoMoneyUI = tempResult.isGoMoneyUI;
					result.goMoneyUIType = tempResult.goMoneyUIType;
					result.goMoneyUICount = tempResult.goMoneyUICount;
					result.isGoVip = tempResult.isGoVip;
					result.showVipTips = tempResult.showVipTips;
					break;
				}
			}

			if (successTime < 1) {
				// 一次也没成功
				return result;
			}

			result.isSucess = true;
			result.successTime = successTime;
			result.nowStrongLv = item.getEquipmentData().getStrongLv();
			result.itemExtName = tempAbs.extItemName;
//			StringBuffer sbf = new StringBuffer();
//			sbf.append(StringUtil.format(ItemTips.共成功强化x次提升x级, successTime, result.strongAddLv));
//			if (!tempResult.isSucess) {
//				sbf.append(StringUtil.format(ItemTips.强化中断原因x, tempResult.tips));
//			}
//			result.tips = sbf.toString();
			result.tips = StringUtil.format(ItemTips.共成功强化x次提升x级, successTime, result.strongAddLv);
			if (!tempResult.isSucess) {
				result.addUprisingTips(tempResult.tips);
			}
			if (payCount > 0) {
				result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, payType.extName, payCount));
			}
			// 需要通知角色模块
			if (item.getItemPackType() == KItemPackTypeEnum.BODYSLOT.sign) {
				// 重算套装数据
				result.notifySlotId = slot.searchSlotIdByItemId(itemId);
				result.isSetChange = slot.recountEquiSetData(result.notifySlotId);
			}
			return result;
		} finally {
			set.rwLock.unlock();

			if (result.isSucess) {
				// 同步装备属性
				KPushItemsMsg.pushItem(role, item);
				// 财产日志
				String tips = StringUtil.format("装备强化;dbId:{};成功次数:{};原等级:{};现强化:{}", itemId, result.successTime, result.orgStrongLv, result.nowStrongLv);
				FlowManager.logPropertyModify(role.getId(), PropertyTypeEnum.道具, item.getUUID(), tempAbs.id, tempAbs.name, tips);
			}
		}
	}

	/**
	 * <pre>
	 * 强化装备1次 
	 * 可以处于背包、装备栏
	 * 
	 * @deprecated 旧的方法
	 * @param role
	 * @param equiItemIdA
	 * @param isPayForBuyStrongStone
	 * @return
	 * @author CamusHuang
	 * @creation 2012-11-22 下午4:07:10
	 * </pre>
	 */
	public static ItemResult_EquiStrongOld dealMsg_strongEquipmentForOneTimeOld(KRole role, long itemId) {

		ItemResult_EquiStrongOld result = new ItemResult_EquiStrongOld();
		KItem item = null;
		KItemTempAbs tempAbs = null;

		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		KItemPack_Bag bag = set.getBag();
		KItemPack_BodySlot slot = set.getSlot();

		set.rwLock.lock();
		try {
			item = bag.getItem(itemId);// 装备
			if (item == null) {
				item = slot.getItem(itemId);
			}

			if (item == null) {
				result.tips = ItemTips.装备不存在;
				return result;
			}

			tempAbs = item.getItemTemplate();
			if (tempAbs.ItemType != KItemTypeEnum.装备) {
				result.tips = ItemTips.装备不存在;
				return result;
			}

			result = strongEquipmentOld(role, item, result);
			//
			if (result.isSucess) {
				result.successTime = 1;
				result.tips = ItemTips.强化装备成功提升1级;
				result.itemExtName = tempAbs.extItemName;
				if (result.payCount > 0) {
					result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, result.payType.extName, result.payCount));
				}
				// 需要通知角色模块
				if (item.getItemPackType() == KItemPackTypeEnum.BODYSLOT.sign) {
					// 重算套装数据
					result.notifySlotId = slot.searchSlotIdByItemId(itemId);
					result.isSetChange = slot.recountEquiSetData(result.notifySlotId);
				}
			}
			return result;
		} finally {
			set.rwLock.unlock();

			if (result.isSucess) {
				// 同步装备属性
				KPushItemsMsg.pushItem(role, item);

				// 财产日志
				String tips = StringUtil.format("装备强化;dbId:{};成功次数:{};原等级:{};现强化:{}", itemId, result.successTime, result.orgStrongLv, result.nowStrongLv);
				FlowManager.logPropertyModify(role.getId(), PropertyTypeEnum.道具, item.getUUID(), tempAbs.id, tempAbs.name, tips);
			}
		}
	}
	
	/**
	 * <pre>
	 * 执行一次强化
	 * 
	 * 强化金币消耗(公式) 相关参数 装备等级 装备品质 装备部位
	 * 强化等级上限高于玩家等级10级
	 * 
	 * @param role
	 * @param item
	 * @param equiData
	 * @author CamusHuang
	 * @creation 2013-12-25 下午4:52:01
	 * </pre>
	 */
	private static ItemResult_EquiStrongIn strongEquipment(KRole role, KItem item, ItemResult_EquiStrongIn result) {
		long roleId = role.getId();
		if (result == null) {
			result = new ItemResult_EquiStrongIn();
		}

		KItem_EquipmentData equiData = item.getEquipmentData();
		KItemTempEqui temp = (KItemTempEqui) item.getItemTemplate();

		// 已达系统最大强化等级
		if (equiData.getStrongLv() >= ExpressionFroMaxStrongLv(role.getLevel())) {
			result.tips = ItemTips.已达最大强化等级;
			return result;
		}
		int nextStrongLv = equiData.getStrongLv() + 1;

		// 消耗货币
		long payMoney = ExpressionForStrongMoney(nextStrongLv, temp.typeEnum);
		// 扣货币
		if (payMoney > 0) {
			long payResult = KSupportFactory.getCurrencySupport().decreaseMoney(roleId, KItemConfig.getInstance().strongPayType, payMoney, UsePointFunctionTypeEnum.装备强化, false);
			if (payResult < 0) {
				result.isBreak = true;
				result.isGoMoneyUI = true;
				result.goMoneyUIType = KItemConfig.getInstance().strongPayType;
				result.goMoneyUICount = payMoney - KSupportFactory.getCurrencySupport().getMoney(role.getId(), KItemConfig.getInstance().strongPayType);
				result.tips = StringUtil.format(ShopTips.x货币数量不足x, KItemConfig.getInstance().strongPayType.extName, payMoney);
				return result;
			}
		}

		// 强化
		equiData.setStrongLv(nextStrongLv);

		result.isSucess = true;
		result.strongAddLv = 1;
		result.payType = KItemConfig.getInstance().strongPayType;
		result.payCount = payMoney;

		return result;
	}

	/**
	 * <pre>
	 * 执行一次强化
	 * 
	 * 强化金币消耗(公式) 相关参数 装备等级 装备品质 装备部位
	 * 强化等级上限高于玩家等级10级
	 * 
	 * @deprecated 旧的方法
	 * @param role
	 * @param item
	 * @param equiData
	 * @author CamusHuang
	 * @creation 2013-12-25 下午4:52:01
	 * </pre>
	 */
	private static ItemResult_EquiStrongOld strongEquipmentOld(KRole role, KItem item, ItemResult_EquiStrongOld result) {
		long roleId = role.getId();
		if (result == null) {
			result = new ItemResult_EquiStrongOld();
		}

		KItem_EquipmentData equiData = item.getEquipmentData();
		KItemTempEqui temp = (KItemTempEqui) item.getItemTemplate();
		result.orgStrongLv = equiData.getStrongLv();

		// 已达系统最大强化等级
		if (equiData.getStrongLv() >= ExpressionFroMaxStrongLv(role.getLevel())) {
			result.tips = ItemTips.已达最大强化等级;
			return result;
		}
		int nextStrongLv = equiData.getStrongLv() + 1;

		// 消耗货币
		long payMoney = ExpressionForStrongMoney(nextStrongLv, temp.typeEnum);
		// 扣货币
		if (payMoney > 0) {
			long payResult = KSupportFactory.getCurrencySupport().decreaseMoney(roleId, KItemConfig.getInstance().strongPayType, payMoney, UsePointFunctionTypeEnum.装备强化, true);
			if (payResult < 0) {
				result.isGoMoneyUI = true;
				result.goMoneyUIType = KItemConfig.getInstance().strongPayType;
				result.goMoneyUICount = payMoney - KSupportFactory.getCurrencySupport().getMoney(role.getId(), KItemConfig.getInstance().strongPayType);
				result.tips = StringUtil.format(ShopTips.x货币数量不足x, KItemConfig.getInstance().strongPayType.extName, payMoney);
				return result;
			}
		}

		// 强化
		equiData.setStrongLv(nextStrongLv);

		result.isSucess = true;
		result.strongAddLv = 1;
		result.nowStrongLv = equiData.getStrongLv();
		result.payType = KItemConfig.getInstance().strongPayType;
		result.payCount = payMoney;

		return result;
	}

	/***
	 * <pre>
	 * 
	 * @deprecated 仅限GM使用，无条件设置角色的装备等级
	 * @param roleId
	 * @param parseLong
	 * @param parseInt
	 * @return
	 * @author CamusHuang
	 * @creation 2013-7-28 下午8:03:01
	 * </pre>
	 */
	public static String setStrongLvForGM(KRole role, long equiItemIdA, int newStrongLv) {

		// 已达系统最大强化等级
		if (newStrongLv > ExpressionFroMaxStrongLv(role.getLevel())) {
			return ItemTips.已达最大强化等级;
		}

		if (newStrongLv < 1) {
			return "数值错误";
		}

		KItem item = null;

		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		KItemPack_Bag bag = set.getBag();
		KItemPack_BodySlot slot = set.getSlot();
		boolean isSuccess = false;

		int orgStrongLv = 0;
		set.rwLock.lock();
		try {
			item = bag.getItem(equiItemIdA);// 装备
			if (item == null) {
				item = slot.getItem(equiItemIdA);
			}

			if (item == null) {
				return ItemTips.装备不存在;
			}

			KItem_EquipmentData equiData = item.getEquipmentData();
			if (equiData == null) {
				return ItemTips.装备不存在;
			}

			orgStrongLv = equiData.getStrongLv();

			// 强化
			equiData.setStrongLv(newStrongLv);
			isSuccess = true;
			return "设置成功";

		} finally {
			set.rwLock.unlock();

			if (isSuccess) {
				// 同步装备属性
				KPushItemsMsg.pushItem(role, item);

				// 财产日志
				String tips = StringUtil.format("GM强化;dbId:{};成功次数:{};原等级:{};现强化:{}", equiItemIdA, 1, orgStrongLv, newStrongLv);
				FlowManager.logPropertyModify(role.getId(), PropertyTypeEnum.道具, item.getUUID(), item.getItemTemplate().id, item.getItemTemplate().name, tips);

			}
		}
	}

	/**
	 * <pre>
	 * 角色最大强化等级
	 * 不能超过角色最大等级
	 * 
	 * @param roleLv
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-21 下午6:06:49
	 * </pre>
	 */
	static int ExpressionFroMaxStrongLv(int roleLv) {
		int maxLv = roleLv + KItemConfig.getInstance().StrongLvAddMax;
		if (maxLv > KRoleModuleConfig.getRoleMaxLv()) {
			return KRoleModuleConfig.getRoleMaxLv();
		}
		return maxLv;
	}

	/**
	 * <pre>
	 * 根据星级，计算出星阶
	 * 
	 * @param starLv
	 * @return
	 * @author CamusHuang
	 * @creation 2014-7-17 下午4:54:59
	 * </pre>
	 */
	static int ExpressionForTopStarLv(int starLv) {
		int result = starLv / KItemConfig.getInstance().EquiStarBigLv;
		if (starLv % KItemConfig.getInstance().EquiStarBigLv == 0) {
			if (result == 0) {
				return 1;
			}
			return result;
		}
		return result + 1;
	}

	/**
	 * <pre>
	 * 强化金币消耗(公式)
	 * 相关参数金币= Round((62.5*（强化等级+2.4）^3/10+12.5*((强化等级+2.4)^3/10)^1.2)*部位参数,-2)
	 * 注：round（m，-2）表示对m的十位开始四舍五入。
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-17 下午4:21:50
	 * </pre>
	 */
	public static long ExpressionForStrongMoney(int nextStrongLv, KEquipmentTypeEnum equiType) {
		KEquiStrongPriceParam param = KItemDataManager.mEquiStrongPriceParamManager.getData(equiType);
		return ExpressionForStrongMoney(nextStrongLv, param);
	}

	/**
	 * <pre>
	 * 计算N次强化的总价格
	 * 相关参数金币= Round((62.5*（强化等级+2.4）^3/10+12.5*((强化等级+2.4)^3/10)^1.2)*部位参数,-2)
	 * 注：round（m，-2）表示对m的十位开始四舍五入。
	 * 
	 * @param nowStrongLv
	 * @param strongTime
	 * @param qualityEnum
	 * @param equiType
	 * @return
	 * @author CamusHuang
	 * @creation 2014-6-11 下午6:19:44
	 * </pre>
	 */
	public static long ExpressionForStrongMoney(int nowStrongLv, int strongTime, KEquipmentTypeEnum equiType) {
		KEquiStrongPriceParam param = KItemDataManager.mEquiStrongPriceParamManager.getData(equiType);

		int nextStrongLv = nowStrongLv;
		long result = 0;
		for (int time = 1; time <= strongTime; time++) {
			nextStrongLv++;
			long temp = ExpressionForStrongMoney(nextStrongLv, param);
			result += Math.max(0, temp);
		}
		return result;
	}

	/**
	 * <pre>
	 * 相关参数金币= Round((62.5*（强化等级+2.4）^3/10+12.5*((强化等级+2.4)^3/10)^1.2)*部位参数,-2)
	 * 注：round（m，-2）表示对m的十位开始四舍五入。
	 * 
	 * @param nextStrongLv
	 * @param param
	 * @return
	 * @author CamusHuang
	 * @creation 2014-6-25 上午11:20:00
	 * </pre>
	 */
	private static long ExpressionForStrongMoney(int nextStrongLv, KEquiStrongPriceParam param) {
		// (62.5*（强化等级+2.4）^3/10 + 12.5*((强化等级+2.4)^3/10)^1.2)*部位参数
		double result = (62.5 * Math.pow((nextStrongLv + 2.4), 3) / 10 + 12.5 * Math.pow(Math.pow(nextStrongLv + 2.4, 3) / 10, 1.2)) * param.Parameter;
		// 十位四舍五入
		result = Math.rint(result / 100) * 100;
		return (long) result;
	}

	/**
	 * <pre>
	 * 强化等级生效公式
	 * 强化属性=装备基础属性*属性强化比例*强化等级属性比例/10000
	 * 
	 * @param strongLv
	 * @param bigLv
	 * @param attType
	 * @param quality
	 * @param equiType
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-19 上午10:32:32
	 * </pre>
	 */
	static int ExpressionForStrongAtt(int strongLv, KGameAttrType attTypeEnum, int baseAttValue) {
		//
		KEquiAttExtData data1 = KItemDataManager.mEquiAttExtDataManager.getData(attTypeEnum);
		KEquiStrongAttExtData data2 = KItemDataManager.mEquiStrongAttExtDataManager.getData(strongLv);
		//
		int 强化属性 = (int) (baseAttValue * (data1 == null ? 0 : data1.StarAttributeRate) * (data2 == null ? 0 : data2.StrongLVRate) / 10000);
		return 强化属性;
	}

	/**
	 * <pre>
	 * 升星生效公式
	 * 升星属性=装备基础属性*属性升星比例*升星等级属性比例/10000
	 * 
	 * @param starLv 星级
	 * @param attValue 属性值
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-21 下午6:29:32
	 * </pre>
	 */
	static int ExpressionForStarAtt(int starLv, KGameAttrType attTypeEnum, int baseAttValue) {
		//
		KEquiAttExtData data1 = KItemDataManager.mEquiAttExtDataManager.getData(attTypeEnum);
		KEquiStarAttExtData data2 = KItemDataManager.mEquiStarAttExtManager.getData(starLv);
		//
		int 升星属性 = (int) (baseAttValue * (data1 == null ? 0 : data1.StarAttributeRate) * (data2 == null ? 0 : data2.StarLVRate) / 10000);
		return 升星属性;
	}

	/**
	 * <pre>
	 * 
	 * 
	 * @param role
	 * @param itemId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-12-4 下午4:50:10
	 * </pre>
	 */
	public static ItemResult_BuyEnchase dealMsg_extendEnchanse(KRole role, long itemId, boolean isConfirm) {

		ItemResult_BuyEnchase result = new ItemResult_BuyEnchase();

		KItem item = null;
		KItem metrialItem = null;
		KEquiBuyEnchansePrice priceData = null;
		int nextTotalPos = 0;

		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		KItemPack_Bag bag = set.getBag();
		KItemPack_BodySlot slot = set.getSlot();

		set.rwLock.lock();
		try {
			item = bag.getItem(itemId);// 装备
			if (item == null) {
				item = slot.getItem(itemId);
			}

			if (item == null) {
				result.tips = ItemTips.装备不存在;
				return result;
			}

			KItemTempAbs tempAbs = item.getItemTemplate();
			if (tempAbs.ItemType != KItemTypeEnum.装备) {
				result.tips = ItemTips.装备不存在;
				return result;
			}

			KItemTempEqui temp = (KItemTempEqui) tempAbs;

			KItem_EquipmentData equData = item.getEquipmentData();

			int nowTotalPos = temp.getTotalEnchansePosition(equData);
			if (nowTotalPos >= KItemConfig.getInstance().MaxEnchansePositionPerOne) {
				result.tips = ItemTips.容量已达极限;
				return result;
			}

			nextTotalPos = nowTotalPos + 1;
			priceData = KItemDataManager.mEquiBuyEnchanseDataManager.getData(nextTotalPos);
			if (priceData == null) {
				// 保护性质
				result.tips = ItemTips.容量已达极限;
				return result;
			}

			if (!isConfirm) {
				// 要求二次确认
				result.priceData = priceData;
				return result;
			}
			
			metrialItem = bag.searchItem(priceData.payItem.itemCode);
			if (metrialItem == null || metrialItem.getCount() < priceData.payItem.itemCount) {
				result.tips = StringUtil.format(ItemTips.您的x不足x个通过异能要塞可以获得更多x, priceData.payItem.getItemTemplate().extItemName, priceData.payItem.itemCount, priceData.payItem.getItemTemplate().extItemName);
				return result;
			}

			// 扣货币
			if (priceData.payMoney != null) {
				long payResult = KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), priceData.payMoney, UsePointFunctionTypeEnum.镶嵌槽扩容, true);
				if (payResult < 0) {
					result.isGoMoneyUI = true;
					result.goMoneyUIType = priceData.payMoney.currencyType;
					result.goMoneyUICount = priceData.payMoney.currencyCount - KSupportFactory.getCurrencySupport().getMoney(role.getId(), priceData.payMoney.currencyType);
					result.tips = StringUtil.format(ShopTips.x货币数量不足x, priceData.payMoney.currencyType.extName, priceData.payMoney.currencyCount);
					return result;
				}
				result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, priceData.payMoney.currencyType.extName, priceData.payMoney.currencyCount));
			}


			// 扣材料
			metrialItem.changeCount(-priceData.payItem.itemCount);
			result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, priceData.payItem.getItemTemplate().extItemName, priceData.payItem.itemCount));

			
			equData.setBuyEnchansePosition(equData.getBuyEnchansePosition() + 1);
			result.isSucess = true;
			result.tips = ItemTips.成功开启该镶嵌孔;
			return result;
		} finally {
			set.rwLock.unlock();
			
			if (result.isSucess) {
				// 同步道具
				KPushItemsMsg.pushItem(role, item);
				KPushItemsMsg.pushItemCount(role.getId(), metrialItem.getId(), metrialItem.getCount());

				// 财产日志
				FlowManager.logPropertyDelete(role.getId(), metrialItem, priceData.payItem.itemCount, "装备镶嵌扩容");

				String tips = StringUtil.format("装备镶嵌扩容;dbId:{};现孔数:{}", itemId, nextTotalPos);
				FlowManager.logPropertyModify(role.getId(), PropertyTypeEnum.道具, item.getUUID(), item.getItemTemplate().id, item.getItemTemplate().name, tips);

			}
		}
	}

	/**
	 * <pre>
	 * 镶嵌宝石
	 * 装备可以处于背包或装备栏
	 * 
	 * @param role
	 * @param itemId
	 * @param stoneId
	 * @author CamusHuang
	 * @creation 2012-11-22 下午4:06:34
	 * </pre>
	 */
	public static ItemResult_Enchase dealMsg_enchaseEquipment(KRole role, long itemId, long stoneId, boolean isConfirm) {
		ItemResult_Enchase result = new ItemResult_Enchase();
		KItem item = null;
		KItem stone = null;
		KItemTempStone stoneTemp = null;
		final int stoneNum = 1;

		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		KItemPack_Bag bag = set.getBag();
		KItemPack_BodySlot slot = set.getSlot();

		KItemTempStone oldStoneTemp = null;
		ItemResult_AddItem addOldStoneResult = null;

		set.rwLock.lock();
		try {
			stone = bag.getItem(stoneId);
			if (stone == null) {
				result.tips = ItemTips.宝石不存在;
				return result;
			}

			KItemTempAbs tempAbs = stone.getItemTemplate();
			if (tempAbs.ItemType != KItemTypeEnum.宝石) {
				result.tips = ItemTips.宝石不存在;
				return result;
			}
			stoneTemp = (KItemTempStone) tempAbs;

			item = bag.getItem(itemId);// 装备
			if (item == null) {
				item = slot.getItem(itemId);
			}

			if (item == null) {
				result.tips = ItemTips.装备不存在;
				return result;
			}

			tempAbs = item.getItemTemplate();
			if (tempAbs.ItemType != KItemTypeEnum.装备) {
				result.tips = ItemTips.装备不存在;
				return result;
			}

			KItemTempEqui equiTemp = (KItemTempEqui) tempAbs;
			KItem_EquipmentData equData = item.getEquipmentData();

			// 类型兼容检测
			if (!equiTemp.stoneTypeMapForType.containsKey(stoneTemp.stoneType)) {
				result.tips = ItemTips.不能镶嵌此宝石;
				return result;
			}

			// 检测同位置的宝石
			String oldStoneItemCode = equData.getEnchanseStone(stoneTemp.stoneType);
			if (oldStoneItemCode != null) {
				// 已有同类宝石、替换
				oldStoneTemp = (KItemTempStone) KItemDataManager.mItemTemplateManager.getItemTemplate(oldStoneItemCode);
				if (oldStoneTemp.stonelvl >= stoneTemp.stonelvl) {
					result.tips = ItemTips.不能镶嵌同级或更低级的宝石;
					return result;
				}

				if (!bag.isBagCanAddItem(new ItemCountStruct(oldStoneTemp, stoneNum))) {
					// 容量不足
					result.tips = ItemTips.背包容量不足;
					return result;
				}

				long nowMoney = KSupportFactory.getCurrencySupport().getMoney(role.getId(), oldStoneTemp.cancelEnchansePrice.currencyType); 
				if (nowMoney < oldStoneTemp.cancelEnchansePrice.currencyCount) {
					result.isGoMoneyUI = true;
					result.goMoneyUIType = oldStoneTemp.cancelEnchansePrice.currencyType;
					result.goMoneyUICount = oldStoneTemp.cancelEnchansePrice.currencyCount - nowMoney;
					result.tips = StringUtil.format(ShopTips.x货币数量不足x, oldStoneTemp.cancelEnchansePrice.currencyType.extName, oldStoneTemp.cancelEnchansePrice.currencyCount);
					return result;
				}

				if (!isConfirm) {
					// 要求二次确认
					result.isGoConfirmMuil = true;
					return result;
				}
			} else {
				// 未有同类宝石、需要有空孔位
				int totalEnchanse = equiTemp.getTotalEnchansePosition(equData);
				if (equData.getEnchanseCache().size() >= totalEnchanse) {
					int MaxEnchanse = KItemConfig.getInstance().MaxEnchansePositionPerOne;
					if (totalEnchanse >= MaxEnchanse) {
						result.tips = StringUtil.format(ItemTips.最多只能镶嵌x个宝石, MaxEnchanse);
						return result;
					}

					result.tips = ItemTips.请开启孔位后再进行镶嵌;
					return result;
				}
			}

			{
				// 扣取新宝石
				if (stone.changeCount(-stoneNum) < 0) {
					result.tips = ItemTips.宝石不存在;
					return result;
				}

				if (oldStoneTemp != null) {
					// 卸载旧宝石价格
					long payResult = KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), oldStoneTemp.cancelEnchansePrice, UsePointFunctionTypeEnum.装备卸载, true);
					if (payResult < 0) {
						result.isGoMoneyUI = true;
						result.goMoneyUIType = oldStoneTemp.cancelEnchansePrice.currencyType;
						result.goMoneyUICount = oldStoneTemp.cancelEnchansePrice.currencyCount - KSupportFactory.getCurrencySupport().getMoney(role.getId(), oldStoneTemp.cancelEnchansePrice.currencyType);
						result.tips = StringUtil.format(ShopTips.x货币数量不足x, oldStoneTemp.cancelEnchansePrice.currencyType.extName, oldStoneTemp.cancelEnchansePrice.currencyCount);
						return result;
					}

					// 返还旧宝石
					addOldStoneResult = bag.addItem(oldStoneTemp, stoneNum, null);
					if (!addOldStoneResult.isSucess) {
						result.tips = addOldStoneResult.tips;
						return result;
					}

					// 修改镶嵌数据
					equData.removeEnchanseStone(oldStoneTemp.itemCode);
				}

				equData.setEnchanseStone(stoneTemp);
			}

			result.isSucess = true;
			result.tips = ItemTips.宝石镶嵌成功;
			result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, stoneTemp.extItemName, stoneNum));
			if (oldStoneTemp != null) {
				result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, oldStoneTemp.cancelEnchansePrice.currencyType.extName, oldStoneTemp.cancelEnchansePrice.currencyCount));
				result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, oldStoneTemp.extItemName, stoneNum));
			}
			// 需要通知角色模块
			if (item.getItemPackType() == KItemPackTypeEnum.BODYSLOT.sign) {
				// 重算套装数据
				result.notifySlotId = slot.searchSlotIdByItemId(itemId);
				result.isSetChange = slot.recountEquiSetData(result.notifySlotId);
			}
			return result;
		} finally {
			set.rwLock.unlock();
			//
			if (result.isSucess) {
				// 更新宝石数量给客户端
				KPushItemsMsg.pushItemCount(role.getId(), stone.getId(), stone.getCount());
				if (addOldStoneResult != null) {
					KPushItemsMsg.pushItems(role, addOldStoneResult.newItemList);
					KPushItemsMsg.pushItemCounts(role.getId(), addOldStoneResult.updateItemCountList);
				}
				// 更新装备给客户端
				KPushItemsMsg.pushItem(role, item);

				// 财产日志
				FlowManager.logPropertyDelete(role.getId(), stone, stoneNum, "来源:镶嵌");
				FlowManager.logPropertyModify(role.getId(), PropertyTypeEnum.道具, item.getUUID(), item.getItemTemplate().id, item.getItemTemplate().name, "镶嵌;dbId:" + item.getId() + ";宝石模板id:"
						+ stoneTemp.id + ";宝石名称:" + stoneTemp.name);
				if (addOldStoneResult != null) {
					FlowManager.logPropertyAdd(role.getId(), addOldStoneResult, null, "镶嵌返回旧宝石");
				}

			}
		}
	}

	/**
	 * <pre>
	 * 取下镶嵌宝石
	 * 
	 * @param role
	 * @param equiItemIdA
	 * @param itemCode
	 * @author CamusHuang
	 * @creation 2012-11-22 下午4:06:34
	 * </pre>
	 */
	public static ItemResult_Equi dealMsg_cancelEnchaseEquipment(KRole role, long itemId, String stoneItemCode) {
		ItemResult_Equi result = new ItemResult_Equi();
		ItemResult_AddItem addResult = null;
		KItem item = null;
		KItemTempStone stoneTemp = null;
		int stoneNum = 1;

		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		KItemPack_Bag bag = set.getBag();
		KItemPack_BodySlot slot = set.getSlot();

		set.rwLock.lock();
		try {
			item = bag.getItem(itemId);// 装备
			if (item == null) {
				item = slot.getItem(itemId);
			}

			if (item == null) {
				result.tips = ItemTips.装备不存在;
				return result;
			}

			KItemTempAbs tempAbs = item.getItemTemplate();
			if (tempAbs.ItemType != KItemTypeEnum.装备) {
				result.tips = ItemTips.装备不存在;
				return result;
			}

			KItem_EquipmentData equData = item.getEquipmentData();

			// 检测宝石
			if (!equData.containEnchanseStone(stoneItemCode)) {
				result.tips = ItemTips.未镶嵌此宝石;
				return result;
			}

			stoneTemp = (KItemTempStone) KItemDataManager.mItemTemplateManager.getItemTemplate(stoneItemCode);

			if (!bag.isBagCanAddItem(new ItemCountStruct(stoneTemp, stoneNum))) {
				// 容量不足
				result.tips = ItemTips.背包容量不足;
				return result;
			}

			// 扣货币
			long payResult = KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), stoneTemp.cancelEnchansePrice, UsePointFunctionTypeEnum.装备卸载, true);
			if (payResult < 0) {
				result.isGoMoneyUI = true;
				result.goMoneyUIType = stoneTemp.cancelEnchansePrice.currencyType;
				result.goMoneyUICount = stoneTemp.cancelEnchansePrice.currencyCount - KSupportFactory.getCurrencySupport().getMoney(role.getId(), stoneTemp.cancelEnchansePrice.currencyType);
				result.tips = StringUtil.format(ShopTips.x货币数量不足x, stoneTemp.cancelEnchansePrice.currencyType.extName, stoneTemp.cancelEnchansePrice.currencyCount);
				return result;
			}

			// 加宝石
			addResult = bag.addItem(stoneTemp, stoneNum, null);
			if (!addResult.isSucess) {
				result.tips = addResult.tips;
				return result;
			}

			// 修改镶嵌数据
			equData.removeEnchanseStone(stoneTemp.itemCode);

			//
			result.isSucess = true;
			result.tips = ItemTips.宝石卸载成功;
			result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, stoneTemp.cancelEnchansePrice.currencyType.extName, stoneTemp.cancelEnchansePrice.currencyCount));
			result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, stoneTemp.extItemName, stoneNum));
			// 需要通知角色模块
			if (item.getItemPackType() == KItemPackTypeEnum.BODYSLOT.sign) {
				// 重算套装数据
				result.notifySlotId = slot.searchSlotIdByItemId(itemId);
				result.isSetChange = slot.recountEquiSetData(result.notifySlotId);
			}
			return result;
		} finally {
			set.rwLock.unlock();
			//
			if (result.isSucess) {
				// 更新道具给客户端
				KPushItemsMsg.pushItem(role, item);
				KPushItemsMsg.pushItems(role, addResult.newItemList);
				KPushItemsMsg.pushItemCounts(role.getId(), addResult.updateItemCountList);

				// 财产日志
				FlowManager.logPropertyAdd(role.getId(), addResult, null, "取消镶嵌");
				FlowManager.logPropertyModify(role.getId(), PropertyTypeEnum.道具, item.getUUID(), item.getItemTemplate().id, item.getItemTemplate().name, "取消镶嵌;dbId:" + item.getId() + ";宝石模板id:"
						+ stoneTemp.id + ";宝石名称:" + stoneTemp.name);
			}
		}
	}

	public static Object dealMsg_getCancelEnchasePrice(KRole role, long itemId, String stoneItemCode) {
		KItem item = null;
		int stoneNum = 1;

		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		KItemPack_Bag bag = set.getBag();
		KItemPack_BodySlot slot = set.getSlot();

		set.rwLock.lock();
		try {
			item = bag.getItem(itemId);// 装备
			if (item == null) {
				item = slot.getItem(itemId);
			}

			if (item == null) {
				return ItemTips.装备不存在;
			}

			KItemTempAbs tempAbs = item.getItemTemplate();
			if (tempAbs.ItemType != KItemTypeEnum.装备) {
				return ItemTips.装备不存在;
			}

			KItem_EquipmentData equData = item.getEquipmentData();

			// 检测宝石
			if (!equData.containEnchanseStone(stoneItemCode)) {
				return ItemTips.未镶嵌此宝石;
			}

			KItemTempStone stoneTemp = (KItemTempStone) KItemDataManager.mItemTemplateManager.getItemTemplate(stoneItemCode);

			if (!bag.isBagCanAddItem(new ItemCountStruct(stoneTemp, stoneNum))) {
				// 容量不足
				return ItemTips.背包容量不足;
			}

			// 货币
			return stoneTemp.cancelEnchansePrice;
		} finally {
			set.rwLock.unlock();
		}
	}

	public static Object dealMsg_getOneKeyCancelEnchasePrice(KRole role, long itemId) {
		KItem item = null;
		int stoneNum = 1;

		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		KItemPack_Bag bag = set.getBag();
		KItemPack_BodySlot slot = set.getSlot();

		set.rwLock.lock();
		try {
			item = bag.getItem(itemId);// 装备
			if (item == null) {
				item = slot.getItem(itemId);
			}

			if (item == null) {
				return ItemTips.装备不存在;
			}

			KItemTempAbs tempAbs = item.getItemTemplate();
			if (tempAbs.ItemType != KItemTypeEnum.装备) {
				return ItemTips.装备不存在;
			}

			KItem_EquipmentData equData = item.getEquipmentData();
			Map<Integer, String> enchanse = equData.getEnchanseCache();
			// 检测宝石
			if (enchanse.isEmpty()) {
				return ItemTips.未镶嵌任何宝石;
			}
			//
			List<ItemCountStruct> stones = new ArrayList<ItemCountStruct>();
			List<KCurrencyCountStruct> moneys = new ArrayList<KCurrencyCountStruct>();
			for (String stoneItemCode : enchanse.values()) {
				KItemTempStone stoneTemp = (KItemTempStone) KItemDataManager.mItemTemplateManager.getItemTemplate(stoneItemCode);
				stones.add(new ItemCountStruct(stoneTemp, stoneNum));
				moneys.add(stoneTemp.cancelEnchansePrice);
			}
			stones = ItemCountStruct.mergeItemCountStructs(stones);
			if (!bag.isBagCanAddItems(stones)) {
				// 容量不足
				return ItemTips.背包容量不足;
			}

			// 扣货币
			moneys = KCurrencyCountStruct.mergeCurrencyCountStructs(moneys);

			return moneys;
		} finally {
			set.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 一键取下镶嵌宝石
	 * 
	 * @param role
	 * @param equiItemIdA
	 * @author CamusHuang
	 * @creation 2012-11-22 下午4:06:34
	 * </pre>
	 */
	public static ItemResult_Equi dealMsg_oneKeyCancelEnchaseEquipment(KRole role, long itemId) {
		ItemResult_Equi result = new ItemResult_Equi();
		ItemResult_AddItem addResult = null;
		KItem item = null;
		int stoneNum = 1;
		List<ItemCountStruct> stones = null;

		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		KItemPack_Bag bag = set.getBag();
		KItemPack_BodySlot slot = set.getSlot();

		set.rwLock.lock();
		try {
			item = bag.getItem(itemId);// 装备
			if (item == null) {
				item = slot.getItem(itemId);
			}

			if (item == null) {
				result.tips = ItemTips.装备不存在;
				return result;
			}

			KItemTempAbs tempAbs = item.getItemTemplate();
			if (tempAbs.ItemType != KItemTypeEnum.装备) {
				result.tips = ItemTips.装备不存在;
				return result;
			}

			KItem_EquipmentData equData = item.getEquipmentData();
			Map<Integer, String> enchanse = equData.getEnchanseCache();
			// 检测宝石
			if (enchanse.isEmpty()) {
				result.tips = ItemTips.未镶嵌任何宝石;
				return result;
			}
			//
			stones = new ArrayList<ItemCountStruct>();
			List<KCurrencyCountStruct> moneys = new ArrayList<KCurrencyCountStruct>();
			for (String stoneItemCode : enchanse.values()) {
				KItemTempStone stoneTemp = (KItemTempStone) KItemDataManager.mItemTemplateManager.getItemTemplate(stoneItemCode);
				stones.add(new ItemCountStruct(stoneTemp, stoneNum));
				moneys.add(stoneTemp.cancelEnchansePrice);
			}
			stones = ItemCountStruct.mergeItemCountStructs(stones);
			if (!bag.isBagCanAddItems(stones)) {
				// 容量不足
				result.tips = ItemTips.背包容量不足;
				return result;
			}

			// 扣货币
			moneys = KCurrencyCountStruct.mergeCurrencyCountStructs(moneys);
			KCurrencyCountStruct payResult = KSupportFactory.getCurrencySupport().decreaseMoneys(role.getId(), moneys, UsePointFunctionTypeEnum.装备卸载, true);
			if (payResult != null) {
				result.isGoMoneyUI = true;
				result.goMoneyUIType = payResult.currencyType;
				result.goMoneyUICount = payResult.currencyCount - KSupportFactory.getCurrencySupport().getMoney(role.getId(), payResult.currencyType);
				result.tips = StringUtil.format(ShopTips.x货币数量不足x, payResult.currencyType.extName, payResult.currencyCount);
				return result;
			}

			// 加宝石
			addResult = bag.addItems(stones, null);
			if (!addResult.isSucess) {
				result.tips = addResult.tips;
				return result;
			}

			// 修改镶嵌数据
			enchanse.clear();
			equData.notifyDB();

			//
			result.isSucess = true;
			result.tips = ItemTips.宝石卸载成功;
			for (KCurrencyCountStruct money : moneys) {
				result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, money.currencyType.extName, money.currencyCount));
			}
			for (ItemCountStruct stone : stones) {
				result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, stone.getItemTemplate().extItemName, stone.itemCount));
			}
			// 需要通知角色模块
			if (item.getItemPackType() == KItemPackTypeEnum.BODYSLOT.sign) {
				// 重算套装数据
				result.notifySlotId = slot.searchSlotIdByItemId(itemId);
				result.isSetChange = slot.recountEquiSetData(result.notifySlotId);
			}
			return result;
		} finally {
			set.rwLock.unlock();
			//
			if (result.isSucess) {
				// 更新道具给客户端
				KPushItemsMsg.pushItem(role, item);
				KPushItemsMsg.pushItems(role, addResult.newItemList);
				KPushItemsMsg.pushItemCounts(role.getId(), addResult.updateItemCountList);

				// 财产日志
				FlowManager.logPropertyAdd(role.getId(), addResult, null, "一键取消镶嵌");
				FlowManager.logPropertyModify(role.getId(), PropertyTypeEnum.道具, item.getUUID(), item.getItemTemplate().id, item.getItemTemplate().name, "一键取消镶嵌");
			}
		}
	}

	public static ItemResult_EquiUpStar dealMsg_upEquipmentStar(KRole role, long itemId) {
		ItemResult_EquiUpStar result = new ItemResult_EquiUpStar();
		KItem item = null;
		KItem metrialItem = null;
		//
		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		KItemPack_Bag bag = set.getBag();
		KItemPack_BodySlot slot = set.getSlot();
		KEquiStarRateData nextRateData = null;

		set.rwLock.lock();
		try {

			item = bag.getItem(itemId);// 装备
			if (item == null) {
				item = slot.getItem(itemId);
			}

			if (item == null) {
				result.tips = ItemTips.装备不存在;
				return result;
			}

			if (item.getItemTemplate().ItemType != KItemTypeEnum.装备) {
				result.tips = ItemTips.此物品不是装备;
				return result;
			}

			KItem_EquipmentData equiData = item.getEquipmentData();
			//
			int nowStar = equiData.getStarLv();
			int nextStar = nowStar + 1;
			if (nextStar > KItemDataManager.mEquiStarRateManager.getMaxStarLv()) {
				result.tips = ItemTips.装备升星已达极限;
				return result;
			}
			nextRateData = KItemDataManager.mEquiStarRateManager.getData(nextStar);

			// 检查材料数量
			int nextTopStarLv = ExpressionForTopStarLv(nextStar);
			KEquiStarMaterialData meterialData = KItemDataManager.mEquiStarMetrialDataManager.getData(nextTopStarLv);
			if (meterialData == null) {
				result.tips = ItemTips.物品不存在;
				return result;
			}
			metrialItem = bag.searchItem(meterialData.itemId);
			if (metrialItem == null) {
				result.tips = ItemTips.物品不存在;
				return result;
			}
			KItemTempAbs metrialTemp = metrialItem.getItemTemplate();
			if (metrialItem.getCount() < nextRateData.materialCount) {
				result.tips = StringUtil.format(ItemTips.x物品数量不足x, metrialTemp.extItemName, nextRateData.materialCount);
				return result;
			}

			// 扣货币
			if (nextRateData.payMoney != null) {
				long payResult = KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), nextRateData.payMoney, UsePointFunctionTypeEnum.装备升星, true);
				if (payResult < 0) {
					result.isGoMoneyUI = true;
					result.goMoneyUIType = nextRateData.payMoney.currencyType;
					result.goMoneyUICount = nextRateData.payMoney.currencyCount - KSupportFactory.getCurrencySupport().getMoney(role.getId(), nextRateData.payMoney.currencyType);
					result.tips = StringUtil.format(ShopTips.x货币数量不足x, nextRateData.payMoney.currencyType.extName, nextRateData.payMoney.currencyCount);
					return result;
				}
				result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, nextRateData.payMoney.currencyType.extName, nextRateData.payMoney.currencyCount));
			}

			// 扣材料
			metrialItem.changeCount(-nextRateData.materialCount);
			result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, metrialTemp.extItemName, nextRateData.materialCount));

			result.orgStarLv = equiData.getStarLv();
			{
				AtomicInteger failTime = set.getUpStartFailTime(nextStar);
				if(failTime.get()+1>=nextRateData.protectedCount){
					// 必然成功
					equiData.setStarLv(nextStar);
					if(failTime.get()!=0){
						failTime.set(0);
						set.notifyDB();
					}
				} else {
					// 有几率升星失败
					if (UtilTool.random(1, 10000) <= nextRateData.getSuccessRate()) {
						// 成功
						equiData.setStarLv(nextStar);
						if(failTime.get()!=0){
							failTime.set(0);
							set.notifyDB();
						}
					} else {
						// 失败
						failTime.incrementAndGet();
						set.notifyDB();
					}
				}
			}

			result.isSucess = true;
			result.starChange = equiData.getStarLv() - nowStar;
			result.nowStarLv = equiData.getStarLv();
			result.itemName = item.getItemTemplate().extItemName;
			// 0表示等级不变，1表示升级，-1表示降级
			if (result.starChange > 0) {
				result.tips = ItemTips.升星成功;
			} else if (result.starChange == 0) {
				result.tips = ItemTips.升星失败;
			} else {
				result.tips = StringUtil.format(ItemTips.升星失败下降x星, result.starChange);
			}
			// 需要通知角色模块
			if (item.getItemPackType() == KItemPackTypeEnum.BODYSLOT.sign) {
				// 重算套装数据
				result.notifySlotId = slot.searchSlotIdByItemId(itemId);
				result.isSetChange = slot.recountEquiSetData(result.notifySlotId);
			}
			return result;
		} finally {
			set.rwLock.unlock();

			if (result.isSucess) {
				// 同步道具
				KPushItemsMsg.pushItem(role, item);
				KPushItemsMsg.pushItemCount(role.getId(), metrialItem.getId(), metrialItem.getCount());

				// 财产日志
				FlowManager.logPropertyDelete(role.getId(), metrialItem, nextRateData.materialCount, "装备升星");

				if (result.orgStarLv != result.nowStarLv) {
					String tips = StringUtil.format("装备升星;dbId:{};升星数:{};现星数:{}", itemId, result.orgStarLv, result.nowStarLv);
					FlowManager.logPropertyModify(role.getId(), PropertyTypeEnum.道具, item.getUUID(), item.getItemTemplate().id, item.getItemTemplate().name, tips);
				}
			}
		}
	}

	/**
	 * <pre>
	 * 继承
	 * 
	 * @param role
	 * @param srcItemId
	 * @param tarItemId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-19 下午8:03:37
	 * </pre>
	 */
	/**
	 * <pre>
	 * 继承
	 * 
	 * @param role
	 * @param srcItemId
	 * @param tarItemId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-19 下午8:03:37
	 * </pre>
	 */
	public static ItemResult_Equi dealMsg_inheritEquipment(KRole role, long srcItemId, long tarItemId) {
		ItemResult_Equi result = new ItemResult_Equi();
		KItem srcItem = null;
		KItem_EquipmentData srcEquiData = null;
		KItem tarItem = null;
		KItem_EquipmentData tarEquiData = null;
		//
		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		KItemPack_Bag bag = set.getBag();
		KItemPack_BodySlot slot = set.getSlot();

		set.rwLock.lock();
		try {
			srcItem = bag.getItem(srcItemId);// 装备
			if (srcItem == null) {
				srcItem = slot.getItem(srcItemId);
			}
			if (srcItem == null) {
				result.tips = ItemTips.装备不存在;
				return result;
			}
			if (srcItem.getItemTemplate().ItemType != KItemTypeEnum.装备) {
				result.tips = ItemTips.旧物品不是装备;
				return result;
			}
			srcEquiData = srcItem.getEquipmentData();
			// 旧装备没有强化等级\升星等级\宝石，不能继承
			if (srcEquiData.getStrongLv() < 1 && srcEquiData.getStarLv() < 1 && srcEquiData.getEnchanseCache().isEmpty()) {
				result.tips = ItemTips.旧装备没有强化等级升星等级不能继承;
				return result;
			}
			KItemTempEqui srcTemp = (KItemTempEqui) srcItem.getItemTemplate();

			// /////////////////////////////
			tarItem = bag.getItem(tarItemId);// 装备
			if (tarItem == null) {
				tarItem = slot.getItem(tarItemId);
			}
			if (tarItem == null) {
				result.tips = ItemTips.装备不存在;
				return result;
			}
			if (tarItem.getItemTemplate().ItemType != KItemTypeEnum.装备) {
				result.tips = ItemTips.新物品不是装备;
				return result;
			}
			tarEquiData = tarItem.getEquipmentData();
			// // 新装备拥有强化等级\升星等级\宝石，不能继承
			// if (tarEquiData.getStrongLv() >
			// KItemConfig.getInstance().EquiInheritMaxStrongLv ||
			// tarEquiData.getStarLv() > 0 ||
			// !tarEquiData.getEnchanseCache().isEmpty()) {
			// result.tips = ItemTips.新装备拥有强化等级升星等级不能继承;
			// return result;
			// }
			KItemTempEqui tarTemp = (KItemTempEqui) tarItem.getItemTemplate();

			// /////////////////////////////
			// 相同的部位只能继承到相同的部位上,无法继承到不同的部位
			if (srcTemp.part != tarTemp.part) {
				result.tips = ItemTips.只能传承到相同部位的装备;
				return result;
			}
			// // 不能传承到低级装备
			// if (tarTemp.lvl < srcTemp.lvl) {
			// result.tips = ItemTips.不能传承到低级装备;
			// return result;
			// }

			// // 不能传承到同级低品装备
			// if (tarTemp.lvl == srcTemp.lvl) {
			// // 不能传承到同级低质装备
			// if (srcTemp.qua >= tarTemp.qua) {
			// result.tips = ItemTips.不能传承到同级低品质装备;
			// return result;
			// }
			// } else {
			// // 不能跨级传承
			// if (tarTemp.lvl - srcTemp.lvl > 10) {
			// // 新装备等级大于来源等级0~10级
			// result.tips = ItemTips.新装备大于旧装备10级无法继承;
			// return result;
			// }
			// }

			// 只可以传承到同部位 &&（（同级&&高质）||（下一级））装备

			//
			KEquiInheritData tarEquiInheritData = KItemDataManager.mEquiInheritDataManager.getData(tarTemp.qua, tarTemp.lvl);
			//
			long payResult = KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), tarEquiInheritData.commonGold, UsePointFunctionTypeEnum.装备继承, false);
			if (payResult < 0) {
				result.isGoMoneyUI = true;
				result.goMoneyUIType = tarEquiInheritData.commonGold.currencyType;
				result.goMoneyUICount = tarEquiInheritData.commonGold.currencyCount - KSupportFactory.getCurrencySupport().getMoney(role.getId(), tarEquiInheritData.commonGold.currencyType);
				result.tips = StringUtil.format(ShopTips.x货币数量不足x, tarEquiInheritData.commonGold.currencyType.extName, tarEquiInheritData.commonGold.currencyCount);
				return result;
			}

			String PropertyTipsBefore=null;
			String PropertyTipsAfter=null;
			// 生成财产日志
			{
				StringBuffer sbf = new StringBuffer();
				for (String stoneCode : srcEquiData.getEnchanseCache().values()) {
					sbf.append(stoneCode).append('>');
				}
				PropertyTipsBefore = StringUtil.format("被继承前;dbId:{};强化:{};星级:{};镶嵌:{}", srcItem.getId(), srcEquiData.getStrongLv(), srcEquiData.getStarLv(), sbf.toString());
			}
			{
				StringBuffer sbf = new StringBuffer();
				for (String stoneCode : tarEquiData.getEnchanseCache().values()) {
					sbf.append(stoneCode).append('>');
				}
				PropertyTipsAfter = StringUtil.format("继承前;dbId:{};强化:{};星级:{};镶嵌:{}", tarItem.getId(), tarEquiData.getStrongLv(), tarEquiData.getStarLv(), sbf.toString());
			}

			// 镶嵌继承
			boolean isEnchanseFail = false;
			try {
				Map<Integer, String> tarEnchanse = tarEquiData.getEnchanseCache();
				Map<Integer, String> srcEnchanse = srcEquiData.getEnchanseCache();

				// 如果目标孔数<源镶嵌数，则源镶嵌退回邮件；否则将目标镶嵌退回背包，并继承源镶嵌
				int tarTotalNum = tarTemp.getTotalEnchansePosition(tarEquiData);
				if (tarTotalNum < srcEnchanse.size()) {
					// 源镶嵌退回邮件
					List<ItemCountStruct> stones = ItemCountStruct.changeItemStruct(srcEnchanse.values());
					ItemResult_AddItem addItemResult = addItemsToBag(role, stones, "装备被继承退回宝石");
					if (!addItemResult.isSucess) {
						result.tips = addItemResult.tips;
						isEnchanseFail = true;
						return result;
					}
					srcEnchanse.clear();
					srcItem.notifyDB();
				} else {
					// 目标镶嵌退回背包，并继承源镶嵌
					if (!tarEnchanse.isEmpty()) {
						// 目标镶嵌宝石退回背包
						List<ItemCountStruct> stones = ItemCountStruct.changeItemStruct(tarEnchanse.values());
						ItemResult_AddItem addItemResult = addItemsToBag(role, stones, "装备继承退回宝石");
						if (!addItemResult.isSucess) {
							result.tips = addItemResult.tips;
							isEnchanseFail = true;
							return result;
						}
						tarEnchanse.clear();
					}
					//
					tarEnchanse.putAll(srcEnchanse);
					tarEquiData.notifyDB();
					srcEnchanse.clear();
					srcEquiData.notifyDB();
				}
			} finally {
				if(isEnchanseFail){
					//失败
					KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), tarEquiInheritData.commonGold, PresentPointTypeEnum.回滚, false);
					return result;
				}
			}
			// 同步货币
			KSupportFactory.getCurrencySupport().synCurrencyDataToClient(role.getId());
			
			// 记录财产日志
			FlowManager.logPropertyModify(role.getId(), PropertyTypeEnum.道具, srcItem.getUUID(), srcItem.getItemTemplate().id, srcItem.getItemTemplate().name, PropertyTipsBefore);
			FlowManager.logPropertyModify(role.getId(), PropertyTypeEnum.道具, tarItem.getUUID(), tarItem.getItemTemplate().id, tarItem.getItemTemplate().name, PropertyTipsAfter);
			
			// 强化继承
			if (srcEquiData.getStrongLv() > tarEquiData.getStrongLv()) {
				tarEquiData.setStrongLv(srcEquiData.getStrongLv());
			}
			srcEquiData.setStrongLv(0);
			// 升星继承
			if (srcEquiData.getStarLv() > tarEquiData.getStarLv()) {
				tarEquiData.setStarLv(srcEquiData.getStarLv());
			}
			srcEquiData.setStarLv(0);
			//
			result.isSucess = true;
			result.tips = ItemTips.成功继承装备;
			result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, tarEquiInheritData.commonGold.currencyType.extName, tarEquiInheritData.commonGold.currencyCount));
			// 需要通知角色模块
			if (srcItem.getItemPackType() == KItemPackTypeEnum.BODYSLOT.sign) {
				// 重算套装数据
				result.notifySlotId = slot.searchSlotIdByItemId(srcItemId);
				result.isSetChange = slot.recountEquiSetData(result.notifySlotId);
			} else if (tarItem.getItemPackType() == KItemPackTypeEnum.BODYSLOT.sign) {
				// 重算套装数据
				result.notifySlotId = slot.searchSlotIdByItemId(tarItemId);
				result.isSetChange = slot.recountEquiSetData(result.notifySlotId);
			}
			return result;
		} finally {
			set.rwLock.unlock();

			if (result.isSucess) {
				// 同步道具
				KPushItemsMsg.pushItems(role, Arrays.asList(srcItem, tarItem));

				// 财产日志
				{
					StringBuffer sbf = new StringBuffer();
					for (String stoneCode : tarEquiData.getEnchanseCache().values()) {
						sbf.append(stoneCode).append('>');
					}
					String tips = StringUtil.format("继承后;dbId:{};强化:{};星级:{};镶嵌:{}", tarItem.getId(), tarEquiData.getStrongLv(), tarEquiData.getStarLv(), sbf.toString());
					FlowManager.logPropertyModify(role.getId(), PropertyTypeEnum.道具, tarItem.getUUID(), tarItem.getItemTemplate().id, tarItem.getItemTemplate().name, tips);
				}
			}
		}
	}

	/**
	 * <pre>
	 * 获取继承价格
	 * 
	 * @param role
	 * @param srcItemId
	 * @param tarItemId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-19 下午8:03:37
	 * </pre>
	 */
	public static ItemResult_InheritEquiPrice dealMsg_getInheritEquiPrice(KRole role, long srcItemId, long tarItemId) {
		ItemResult_InheritEquiPrice result = new ItemResult_InheritEquiPrice();
		KItem srcItem = null;
		KItem_EquipmentData srcEquiData = null;
		KItem tarItem = null;
		// KItem_EquipmentData tarEquiData = null;
		//
		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		KItemPack_Bag bag = set.getBag();
		KItemPack_BodySlot slot = set.getSlot();

		set.rwLock.lock();
		try {
			srcItem = bag.getItem(srcItemId);// 装备
			if (srcItem == null) {
				srcItem = slot.getItem(srcItemId);
			}
			if (srcItem == null) {
				result.tips = ItemTips.装备不存在;
				return result;
			}
			if (srcItem.getItemTemplate().ItemType != KItemTypeEnum.装备) {
				result.tips = ItemTips.旧物品不是装备;
				return result;
			}
			srcEquiData = srcItem.getEquipmentData();
			// 旧装备没有强化等级\升星等级\宝石，不能继承
			if (srcEquiData.getStrongLv() < 1 && srcEquiData.getStarLv() < 1 && srcEquiData.getEnchanseCache().isEmpty()) {
				result.tips = ItemTips.旧装备没有强化等级升星等级不能继承;
				return result;
			}
			KItemTempEqui srcTemp = (KItemTempEqui) srcItem.getItemTemplate();

			// /////////////////////////////
			tarItem = bag.getItem(tarItemId);// 装备
			if (tarItem == null) {
				tarItem = slot.getItem(tarItemId);
			}
			if (tarItem == null) {
				result.tips = ItemTips.装备不存在;
				return result;
			}
			if (tarItem.getItemTemplate().ItemType != KItemTypeEnum.装备) {
				result.tips = ItemTips.新物品不是装备;
				return result;
			}
			// tarEquiData = tarItem.getEquipmentData();
			// 新装备拥有强化等级\升星等级\宝石，不能继承
			// if (tarEquiData.getStrongLv() >
			// KItemConfig.getInstance().EquiInheritMaxStrongLv ||
			// tarEquiData.getStarLv() > 0 ||
			// !tarEquiData.getEnchanseCache().isEmpty()) {
			// result.tips = ItemTips.新装备拥有强化等级升星等级不能继承;
			// return result;
			// }
			KItemTempEqui tarTemp = (KItemTempEqui) tarItem.getItemTemplate();

			// /////////////////////////////
			// 相同的部位只能继承到相同的部位上,无法继承到不同的部位
			if (srcTemp.part != tarTemp.part) {
				result.tips = ItemTips.只能传承到相同部位的装备;
				return result;
			}
			// // 不能传承到低级装备
			// if (tarTemp.lvl < srcTemp.lvl) {
			// result.tips = ItemTips.不能传承到低级装备;
			// return result;
			// }

			// // 不能传承到同级低品装备
			// if (tarTemp.lvl == srcTemp.lvl) {
			// // 不能传承到同级低质装备
			// if (srcTemp.qua >= tarTemp.qua) {
			// result.tips = ItemTips.不能传承到同级低品质装备;
			// return result;
			// }
			// } else {
			// // 不能跨级传承
			// if (tarTemp.lvl - srcTemp.lvl > 10) {
			// // 新装备等级大于来源等级0~10级
			// result.tips = ItemTips.新装备大于旧装备10级无法继承;
			// return result;
			// }
			// }

			// 只可以传承到同部位 &&（（同级&&高质）||（下一级））装备
			//
			KEquiInheritData tarEquiInheritData = KItemDataManager.mEquiInheritDataManager.getData(tarTemp.qua, tarTemp.lvl);
			//
			result.commonPayGold = tarEquiInheritData.commonGold;

			result.isSucess = true;
			result.tips = "";
			return result;
		} finally {
			set.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 重算套装数据
	 * 
	 * @author CamusHuang
	 * @creation 2014-3-22 下午1:56:45
	 * </pre>
	 */
	static EquiSetResult recountEquiSetData(KRole role, long slotId) {
		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		set.rwLock.lock();
		try {
			KItemPack_BodySlot slot = set.getSlot();
			return slot.recountEquiSetData(slotId);
		} finally {
			set.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 同步套装数据
	 * 
	 * @author CamusHuang
	 * @creation 2014-3-22 下午1:56:45
	 * </pre>
	 */
	public static void synEquiSetData(KRole role, EquiSetResult setChangeResult) {
		KPushItemsMsg.pushEquiSetData(role);

		if (setChangeResult.isSetChange) {
			// 通知角色模块
			KSupportFactory.getRoleModuleSupport().updateEquipmentSetRes(role.getId());
			KSupportFactory.getTeamPVPSupport().notifyRoleEquipmentSetResUpdate(role.getId());

			EquiSetStruct sets = KSupportFactory.getItemModuleSupport().getEquiSets(role.getId());
			KSupportFactory.getExcitingRewardSupport().notifyEquiSetChange(role.getId(), sets);

			int[] setForBroadst = Arrays.copyOf(setChangeResult.setForBroast, setChangeResult.setForBroast.length);
			boolean isBroadst = false;
			if (setChangeResult.isStarSetUp) {
				int index = 0;
				if (setChangeResult.newSet.starSetLv > setChangeResult.setForBroast[0] && setChangeResult.newSet.starSetLv >= 4) {
					KWordBroadcastType _boradcastType = KWordBroadcastType.升星_XX激活了X阶升星套装;
					KSupportFactory.getChatSupport().sendSystemChat(StringUtil.format(_boradcastType.content, role.getExName(), setChangeResult.newSet.starSetLv), _boradcastType);
					setForBroadst[index] = setChangeResult.newSet.starSetLv;
					isBroadst = true;
				}
			}
			if (setChangeResult.isStoneSetUp) {
				int index = 1;
				if (setChangeResult.newSet.stoneSetLv > setChangeResult.setForBroast[index]) {
					KWordBroadcastType _boradcastType = KWordBroadcastType.宝石_XX激活X等宝石套装;
					KSupportFactory.getChatSupport().sendSystemChat(StringUtil.format(_boradcastType.content, role.getExName(), setChangeResult.newSet.stoneSetLv), _boradcastType);
					setForBroadst[index] = setChangeResult.newSet.stoneSetLv;
					isBroadst = true;
				}
			}
			// 强化套装世界广播，忽略
			if (isBroadst) {
				KItemSet set = KItemModuleExtension.getItemSet(role.getId());
				set.rwLock.lock();
				try {
					KItemPack_BodySlot slot = set.getSlot();
					slot.resetBroadstSet(KItemConfig.MAIN_BODYSLOT_ID, setForBroadst);
				} finally {
					set.rwLock.unlock();
				}
			}
		}
	}

	/**
	 * <pre>
	 * 获得新礼包
	 * 
	 * @param role
	 * @param list
	 * @author CamusHuang
	 * @creation 2014-6-29 下午4:37:24
	 * </pre>
	 */
	private static List<KItem> filterNewGiftItem(KRole role, List<KItem> newItemList, List<KItem> updateItemCountList) {
		List<KItem> resultList = new ArrayList<KItem>();

		if (newItemList != null) {
			// 只保留时装礼包
			for (KItem item : newItemList) {
				KItemTempAbs tempA = item.getItemTemplate();
				if (tempA.ItemType != KItemTypeEnum.固定宝箱) {
					continue;
				}
				KItemTempFixedBox temp = (KItemTempFixedBox) tempA;
				if (temp.addFashionTempList.isEmpty()) {
					continue;
				}
				boolean isFind = false;
				for (KFashionTemplate fashionTemp : temp.addFashionTempMap.keySet()) {
					if (fashionTemp.jobEnum != null && fashionTemp.job != role.getJob()) {
						continue;
					}
					isFind = true;
				}
				if (isFind) {
					resultList.add(item);
				}
			}
		}
		if (updateItemCountList != null) {
			// 只保留时装礼包
			for (KItem item : updateItemCountList) {
				KItemTempAbs tempA = item.getItemTemplate();
				if (tempA.ItemType != KItemTypeEnum.固定宝箱) {
					continue;
				}
				KItemTempFixedBox temp = (KItemTempFixedBox) tempA;
				if (temp.addFashionTempList.isEmpty()) {
					continue;
				}
				boolean isFind = false;
				for (KFashionTemplate fashionTemp : temp.addFashionTempMap.keySet()) {
					if (fashionTemp.jobEnum != null && fashionTemp.job != role.getJob()) {
						continue;
					}
					isFind = true;
				}
				if (isFind) {
					resultList.add(item);
				}
			}
		}
		return resultList;
	}

	/**
	 * <pre>
	 * 过滤，找出比指定角色现穿戴装备更高级的装备
	 * 0.职业等级符合要求
	 * 1.现在没穿，必弹
	 * 2.基础战力比现装备高，必弹
	 * 3.其它情况，不弹
	 * 
	 * @param list
	 * @return
	 * @author CamusHuang
	 * @creation 2013-4-2 下午5:09:15
	 * </pre>
	 */
	static List<KItem> filterNewTopEquipment(KRole role, List<KItem> list) {
		if (list == null || list.isEmpty()) {
			return null;
		}

		// 前置过滤
		List<KItem> resultList = filterNewTopEquipmentFront(role, list);

		if (resultList.isEmpty()) {
			return null;
		}

		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		set.rwLock.lock();
		try {
			KItemPack_BodySlot slot = set.getSlot();

			// 其它过滤条件
			for (Iterator<KItem> it = resultList.iterator(); it.hasNext();) {
				KItem item = it.next();
				KItem_EquipmentData equiData = item.getEquipmentData();
				KItemTempEqui temp = (KItemTempEqui) item.getItemTemplate();

				KItem oldEqui = slot.searchSlotItem(KItemConfig.MAIN_BODYSLOT_ID, temp.typeEnum);
				if (oldEqui == null) {
					// 可装
					continue;
				}
				KItemTempEqui oldTemp = (KItemTempEqui) oldEqui.getItemTemplate();
				KItem_EquipmentData oldEquiData = oldEqui.getEquipmentData();
				// 新装备基础战斗力比旧装备基础战斗力高时可装
				if (temp.battlePowerMap.get(equiData.getLv()) > oldTemp.battlePowerMap.get(oldEquiData.getLv())) {
					// 可装
					continue;
				} else {
					// 不可装
					it.remove();
					continue;
				}
			}

			if (resultList.isEmpty()) {
				return null;
			}

			return resultList;
		} finally {
			set.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 获得新装备，前置过滤
	 * 
	 * 过滤掉职业和等级不符合角色要求的
	 * 同一部位只保留最牛的一件（比基础战力）
	 * 
	 * @param role
	 * @param list
	 * @author CamusHuang
	 * @creation 2014-6-29 下午4:37:24
	 * </pre>
	 */
	private static List<KItem> filterNewTopEquipmentFront(KRole role, List<KItem> list) {
		int job = role.getJob();
		int roleLv = role.getLevel();

		List<KItem> resultList = new ArrayList<KItem>();

		// 过滤掉职业和等级不符合要求的
		for (KItem item : list) {
			KItemTempAbs tempA = item.getItemTemplate();
			if (tempA.ItemType != KItemTypeEnum.装备) {
				continue;
			}
			KItemTempEqui temp = (KItemTempEqui) tempA;
			if (temp.jobEnum != null && temp.job != job) {
				continue;
			}
			if (temp.lvl > roleLv) {
				continue;
			}
			resultList.add(item);
		}

		// 同一部位只保留最牛的
		Map<KEquipmentTypeEnum, KItem> map = new HashMap<KEquipmentTypeEnum, KItem>();
		for (KItem item : resultList) {
			KItemTempEqui temp = (KItemTempEqui) item.getItemTemplate();
			KItem_EquipmentData equiData = item.getEquipmentData();

			KItem oldItem = map.get(temp.typeEnum);
			if (oldItem == null) {
				map.put(temp.typeEnum, item);
				continue;
			}

			// 同一部位只保留最牛的一件（比基础战力）
			KItemTempEqui oldTemp = (KItemTempEqui) oldItem.getItemTemplate();
			KItem_EquipmentData oldEquiData = oldItem.getEquipmentData();

			// 新装备基础战斗力比旧装备基础战斗力高时可装
			if (temp.battlePowerMap.get(equiData.getLv()) > oldTemp.battlePowerMap.get(oldEquiData.getLv())) {
				map.put(temp.typeEnum, item);
			}
			continue;
		}

		resultList.clear();
		resultList.addAll(map.values());

		return resultList;
	}

	/**
	 * <pre>
	 * 清理角色非法的镶嵌宝石
	 * 
	 * @param role
	 * @return 减少了多少个镶嵌孔
	 * @author CamusHuang
	 * @creation 2014-11-18 下午7:02:20
	 * </pre>
	 */
	public static int clearIllegalEnchanse(KRole role) {

		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		if (set == null) {
			return 0;
		}

		set.rwLock.lock();
		try {

			AtomicInteger loseHoles = new AtomicInteger();

			List<String> stoneCodes = new ArrayList<String>();

			int ORG_HOLES = KItemConfig.getInstance().MaxEnchansePositionPerOne;
			for (KItemPackTypeEnum packTypeEnum : KItemPackTypeEnum.values()) {
				KAItemPack<KItem> bag = set.getPackByEnum(packTypeEnum);
				for (KItem item : bag.getAllItemsCache().values()) {
					KItemTempAbs tempAbs = item.getItemTemplate();
					if (tempAbs.ItemType != KItemTypeEnum.装备) {
						// 非装备
						continue;
					}

					KItemTempEqui tempEqui = (KItemTempEqui) tempAbs;
					KItem_EquipmentData equiData = item.getEquipmentData();

					int totalNum = tempEqui.getTotalEnchansePosition(equiData);
					if (packTypeEnum == KItemPackTypeEnum.BODYSLOT && ORG_HOLES > totalNum) {
						loseHoles.addAndGet(ORG_HOLES - totalNum);
					}

					Map<Integer, String> enchanse = equiData.getEnchanseCache();
					if (enchanse.isEmpty()) {
						// 没镶嵌
						continue;
					}

					int nowSize = enchanse.size();
					if (nowSize <= totalNum) {
						// 没溢出
						continue;
					}

					if (totalNum == 0) {
						// 全退
						stoneCodes.addAll(enchanse.values());
						enchanse.clear();
						item.notifyDB();
					} else {
						// 退部分
						Iterator<Entry<Integer, String>> it = enchanse.entrySet().iterator();
						for (; nowSize > totalNum; nowSize--) {
							Entry<Integer, String> e = it.next();
							it.remove();
							stoneCodes.add(e.getValue());
						}
						item.notifyDB();
					}
				}
			}

			if (stoneCodes.isEmpty()) {
				return loseHoles.get();
			}

			List<ItemCountStruct> itemList = ItemCountStruct.changeItemStruct(stoneCodes);
			itemList = ItemCountStruct.mergeItemCountStructs(itemList);
			// 亲爱的玩家,由于装备凹槽开启修改,系统已将您的宝石卸下,请注意查收您的宝石
			KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), itemList, ItemTips.宝石卸载通知邮件标题, ItemTips.宝石卸载通知邮件内容);

			return loseHoles.get();
		} finally {
			set.rwLock.unlock();
		}
	}
	
	/**
	 * <pre>
	 * 同步所有在线角色的装备数据
	 * 
	 * @author CamusHuang
	 * @creation 2015-1-2 下午5:14:28
	 * </pre>
	 */
	public static void synAllEquiForOnlineRoles(){
		
		RoleModuleSupport mRoleModuleSupport = KSupportFactory.getRoleModuleSupport();
		for (long roleId : mRoleModuleSupport.getAllOnLineRoleIds()) {
			KRole role = mRoleModuleSupport.getRole(roleId);
			KItemSet set = KItemModuleExtension.getItemSet(roleId);
			KItemPack_Bag bag = set.getBag();
			KItemPack_BodySlot slot = set.getSlot();

			set.rwLock.lock();
			try {
				
				Set<KItem> result = new HashSet<KItem>();
				for(KItem item:bag.getAllItemsCache().values()){
					if(item.getItemTemplate().ItemType == KItemTypeEnum.装备){
						result.add(item);
					}
				}
				result.addAll(slot.searchSlotItemList(KItemConfig.MAIN_BODYSLOT_ID));
				//
				KPushItemsMsg.pushItems(role, result);
			} finally {
				set.rwLock.unlock();
			}
		}
	}

	public static boolean isRedWepond(long roleId) {
		KItemSet set = KItemModuleExtension.getItemSet(roleId);
		set.rwLock.lock();
		try {
			KItemPack_BodySlot slot = set.getSlot();
			KItem item = slot.searchSlotItem(KItemConfig.MAIN_BODYSLOT_ID, KEquipmentTypeEnum.主武器);
			if(item==null){
				return false;
			}
			return item.getItemTemplate().ItemQuality==KItemQualityEnum.无敌的;
		} finally {
			set.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 扫描指定的角色背包，若角色存在且包含可自动打开的宝箱，则打开此类宝箱
	 * 
	 * @param roleIdSetCopy 此参数要求保留未被执行的角色ID，被调用者后续处理
	 * @author CamusHuang
	 * @creation 2015-1-28 下午3:15:34
	 * </pre>
	 */
	static void forAndAutoOpenBoxForRoles(List<Long> roleIdSetCopy) {

		RoleModuleSupport mRoleModuleSupport = KSupportFactory.getRoleModuleSupport();
		for (Iterator<Long> it = roleIdSetCopy.iterator(); it.hasNext();) {
			long roleId = it.next();
			KRole role = mRoleModuleSupport.getRole(roleId);
			if (role == null) {
				it.remove();
				continue;
			}
			if (!role.isOnline()) {
				// 保留未被执行的角色ID，被调用者后续处理
				continue;
			} else {
				it.remove();
				//
				autoOpenBoxForRole(roleId, role);
			}
		}
	}
	
	private static void autoOpenBoxForRole(long roleId, KRole role) {
		KItemSet set = KItemModuleExtension.getItemSet(roleId);
		if (set == null) {
			return;
		}
		//
		ItemResult_Use result = new ItemResult_Use();
		List<KItem> openItems = new LinkedList<KItem>();
		set.rwLock.lock();
		try {
			KItemPack_Bag bag = set.getBag();
			openItems.addAll(bag.getAllItemsCache().values());
			//
			
			for (Iterator<KItem> it = openItems.iterator(); it.hasNext();) {
				KItem item = it.next();

				KItemTempAbs itemTemp = item.getItemTemplate();
				if (!isAutoOpenBox(itemTemp)) {
					it.remove();
					continue;
				}
				// 检查角色等级要求
				if (itemTemp.lvl > 0 && role.getLevel() < itemTemp.lvl) {
					it.remove();
					continue;
				}

				result.item = item;
				result.isSucess = false;
				useConsumeItemIn(result, role, bag, (KItemTempConsume) itemTemp, true);
				if (!result.isSucess) {
					it.remove();
				}
			}
		} finally {
			set.rwLock.unlock();

			if (!openItems.isEmpty()) {
				// 同步道具
				KPushItemsMsg.pushItemCounts(role.getId(), openItems);
			}
			//任务周期尽量短，不需要显示tips
//			result.doFinally(role);
		}
	}

	/**
	 * <pre>
	 * 判断一个物品是否可自动打开的宝箱
	 * 非体力的消耗类物品
	 * 
	 * @param tempAbs
	 * @return
	 * @author CamusHuang
	 * @creation 2015-1-28 上午11:44:46
	 * </pre>
	 */
	public static boolean isAutoOpenBox(KItemTempAbs tempAbs) {
		if (tempAbs.ItemType != KItemTypeEnum.消耗品) {
			return false;
		}
		KItemTempConsume temp = (KItemTempConsume) tempAbs;
		if (temp.addAtt != null && temp.addAtt.roleAttType == KGameAttrType.PHY_POWER) {
			return false;
		}
		return true;
	}
}
