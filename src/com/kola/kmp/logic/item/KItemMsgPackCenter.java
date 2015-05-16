package com.kola.kmp.logic.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.koala.game.KGameMessage;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.StringUtil;
import com.kola.kmp.logic.item.KItem.KItemCA.KItem_EquipmentData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiQualitySetData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStarMaterialData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStarRateData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStarSetData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStoneSetData2;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStrongSetData;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempConsume;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempEqui;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempEqui.EquiProduceData;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempMaterial;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempStone;
import com.kola.kmp.logic.item.KItemPack_BodySlot.KItemPack_BodySlot_CA.BodySlotData;
import com.kola.kmp.logic.item.KItemPack_BodySlot.KItemPack_BodySlot_CA.BodySlotData.SetData;
import com.kola.kmp.logic.level.KLevelTemplate;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KItemPackTypeEnum;
import com.kola.kmp.logic.other.KItemTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.TeamPVPSupport.ITeamPVPAttrSetInfo;
import com.kola.kmp.logic.util.KGameUtilTool;
import com.kola.kmp.logic.util.text.HyperTextTool;
import com.kola.kmp.logic.util.tips.ItemTips;
import com.kola.kmp.protocol.item.KItemProtocol;

public class KItemMsgPackCenter {

	private static Logger _LOGGER = KGameLogger.getLogger(KItemMsgPackCenter.class);

	/**
	 * <pre>
	 * 参考{@link KItemProtocol#SM_PUSH_ALLITEM_POWER}
	 * 
	 * @param msg
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2014-3-21 下午6:34:37
	 * </pre>
	 */
	public static void packAllItemsPower(KGameMessage msg, long roleId) {
		KItemSet set = KItemModuleExtension.getItemSet(roleId);
		set.rwLock.lock();
		try {
			// 道具包数据打包
			{
				KItemPack_Bag bagPack = set.getBag();
				packItemsPower(msg, roleId, bagPack.getAllItemsCache().values());
			}

			// 装备栏数据打包
			{
				KItemPack_BodySlot slotPack = set.getSlot();
				List<Long> slotIdList = slotPack.getAllSlotId();
				//
				msg.writeByte(slotIdList.size());// 装备栏数量
				for (long slotId : slotIdList) {
					msg.writeLong(slotId);
					packItemsPower(msg, roleId, slotPack.searchSlotItemList(slotId));
				}
			}
		} finally {
			set.rwLock.unlock();
		}
	}

	private static void packItemsPower(KGameMessage msg, long roleId, Collection<KItem> datas) {
		int writeIndex = msg.writerIndex();
		msg.writeShort(datas.size());
		int count = 0;
		for (KItem item : datas) {
			if (packItemPower(msg, roleId, item)) {
				count++;
			}
		}
		msg.setShort(writeIndex, count);
	}

	private static boolean packItemPower(KGameMessage msg, long roleId, KItem item) {
		KItemTempAbs temp = item.getItemTemplate();

		switch (temp.ItemType) {
		case 装备:
			msg.writeLong(item.getId());
			KItem_EquipmentData equiData = item.getEquipmentData();
			msg.writeInt(KSupportFactory.getRoleModuleSupport().calculateBattlePower(KGameUtilTool.changeAttMap(equiData.getAllEffect(null)), roleId));
			return true;
		case 改造材料:
			break;
		case 宝石:
			msg.writeLong(item.getId());
			msg.writeInt(KSupportFactory.getRoleModuleSupport().calculateBattlePower(((KItemTempStone) temp).allEffects, roleId));
			return true;
		case 消耗品:
			break;
		case 固定宝箱:
			break;
		// case 宠物:
		// packItem_pet(msg);
		// break;
		case 随机宝箱:
			break;
		}
		return false;
	}

	/**
	 * <pre>
	 * 参考{@link KItemProtocol#SM_PUSH_ALLITEM_LIST}
	 * 
	 * @param msg
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2014-3-21 下午6:34:37
	 * </pre>
	 */
	public static void packAllItems(KGameMessage msg, long roleId, int roleLv) {
		KItemSet set = KItemModuleExtension.getItemSet(roleId);
		set.rwLock.lock();
		try {
			// 客户端自定义数据
			msg.writeUtf8String(set.getClientData());

			// 道具包数据打包
			{
				KItemPack_Bag bagPack = set.getBag();
				msg.writeShort(KItemDataManager.mBagExtDataManager.getMaxGridCount());
				msg.writeShort(bagPack.getVolume());
				packItems(msg, roleId, roleLv, bagPack.getAllItemsCache());
			}

			// 装备栏数据打包
			{
				KItemPack_BodySlot slotPack = set.getSlot();
				List<Long> slotIdList = slotPack.getAllSlotId();
				//
				msg.writeByte(slotIdList.size());// 装备栏数量
				for (long slotId : slotIdList) {
					msg.writeLong(slotId);
					packItems(msg, roleId, roleLv, slotPack.searchSlotItemList(slotId));
				}
			}

			// 升星材料
			{
				Collection<KEquiStarMaterialData> datas = KItemDataManager.mEquiStarMetrialDataManager.getDataCache();
				msg.writeByte(datas.size());
				for (KEquiStarMaterialData data : datas) {
					packItem(msg, KItemDataManager.mItemTemplateManager.getItemTemplate(data.itemId), 1);
				}
			}
		} finally {
			set.rwLock.unlock();
		}
	}

	private static void packItems(KGameMessage msg, long roleId, int roleLv, Map<Long, KItem> datas) {
		msg.writeShort(datas.size());
		// _LOGGER.error("打包道具数量=" + size);
		for (KItem item : datas.values()) {
			packItem(msg, roleId, roleLv, item);
		}
	}

	private static void packItems(KGameMessage msg, long roleId, int roleLv, List<KItem> datas) {
		msg.writeShort(datas.size());
		// _LOGGER.error("打包道具数量=" + size);
		for (KItem item : datas) {
			packItem(msg, roleId, roleLv, item);
		}
	}

	/**
	 * <pre>
	 * 将一个道具，按指定协议打包进消息
	 * 参考{@link KItemProtocol#MSG_STRUCT_ITEM_DETAILS}
	 * 
	 * @param msg
	 * @param item
	 * @author CamusHuang
	 * @creation 2013-1-15 下午8:35:38
	 * </pre>
	 */
	public static void packItem(KGameMessage msg, long roleId, int roleLv, KItem item) {
		KItemTempAbs temp = item.getItemTemplate();

		// 打包道具通用属性
		packItemBaseInfo(msg, item.getId(), temp, null, item.getCount(), temp.icon);

		switch (temp.ItemType) {
		case 装备:
			packItem_equi(msg, roleId, roleLv, item, (KItemTempEqui) temp);
			break;
		case 改造材料:
			packItem_material(msg, (KItemTempMaterial) temp);
			break;
		case 宝石:
			packItem_stone(msg, roleId, roleLv, (KItemTempStone) temp, true);
			break;
		case 消耗品:
			packItem_consume(msg, (KItemTempConsume) temp);
			break;
		case 固定宝箱:
			packItem_fixedBox(msg);
			break;
		// case 宠物:
		// packItem_pet(msg);
		// break;
		case 随机宝箱:
			packItem_randomBox(msg);
			break;
		case 装备包:
			packItem_equiBox(msg);
			break;
		}
	}

	/**
	 * <pre>
	 * 将一个道具模板，按指定协议打包进消息
	 * 参考{@link KItemProtocol#MSG_STRUCT_ITEM_BASE_DETAILS}
	 * 
	 * @param msg
	 * @param temp
	 * @author CamusHuang
	 * @creation 2013-1-15 下午8:36:27
	 * </pre>
	 */
	private static void packItemBaseInfo(KGameMessage msg, long itemId, KItemTempAbs temp, KCurrencyCountStruct price, long count, int icon) {
		msg.writeLong(itemId);
		msg.writeUtf8String(temp.itemCode);
		msg.writeUtf8String(temp.name);
		msg.writeUtf8String(temp.ItemQuality.name);
		msg.writeInt(icon);
		msg.writeLong(count);
		msg.writeShort(temp.lvl);
		msg.writeBoolean(temp.stack > 1);
		msg.writeBoolean(temp.isBind());
		msg.writeUtf8String(temp.getDesc());
		msg.writeByte(KCurrencyTypeEnum.GOLD.sign);
		msg.writeInt((int) (temp.sellMoney == null ? 0 : temp.sellMoney.currencyCount));
		//
		if (price == null) {
			price = temp.buyMoney;
		}
		msg.writeByte(price == null ? 1 : price.currencyType.sign);
		msg.writeInt((int) (price == null ? 0 : price.currencyCount));
		//
		msg.writeByte(temp.ItemType.sign);
	}

	/**
	 * <pre>
	 * 装备道具专有属性
	 * 
	 * 参考{@link KItemProtocol#MSG_STRUCT_ITEM_DETAILS_EQUI}
	 * </pre>
	 */
	private static void packItem_equi(KGameMessage msg, long roleId, int roleLv, KItem item, KItemTempEqui temp) {
		KItem_EquipmentData equiData = item == null ? null : item.getEquipmentData();
		//
		msg.writeByte(temp.job);// 需求职业
		msg.writeByte(temp.part);// 穿戴部位
		msg.writeUtf8String(temp.showResId);// 场景展示的资源id
		msg.writeByte(equiData == null ? 0 : equiData.getLv());
		//
		int nowStrongLv = equiData == null ? 0 : equiData.getStrongLv();
		msg.writeShort(nowStrongLv);
		int maxStrongLv = KItemLogic.ExpressionFroMaxStrongLv(roleLv);
		if (nowStrongLv >= maxStrongLv) {
			msg.writeBoolean(false);
		} else {
			msg.writeBoolean(true);
			// 单次强化价格
			int payMoney = (int) KItemLogic.ExpressionForStrongMoney(nowStrongLv + 1, temp.typeEnum);
			payMoney = Math.max(0, payMoney);
			msg.writeByte(KItemConfig.getInstance().strongPayType.sign);
			msg.writeInt(payMoney);
			// 强满价格
			int canStrongLv = maxStrongLv - nowStrongLv;
			payMoney = (int) KItemLogic.ExpressionForStrongMoney(nowStrongLv, canStrongLv, temp.typeEnum);
			msg.writeByte(KItemConfig.getInstance().strongPayType.sign);
			msg.writeInt(payMoney);
		}
		//
		int nowStarLv = equiData == null ? 0 : equiData.getStarLv();
		msg.writeShort(nowStarLv);
		int maxStarLv = KItemDataManager.mEquiStarRateManager.getMaxStarLv();
		if (nowStarLv >= maxStarLv) {
			msg.writeBoolean(false);
		} else {
			msg.writeBoolean(true);
			//
			KEquiStarRateData rateData = KItemDataManager.mEquiStarRateManager.getData(nowStarLv + 1);
			msg.writeByte(rateData.payMoney.currencyType.sign);
			msg.writeInt((int) rateData.payMoney.currencyCount);
			//
			int nextTopStarLv = KItemLogic.ExpressionForTopStarLv(nowStarLv + 1);
			KEquiStarMaterialData data = KItemDataManager.mEquiStarMetrialDataManager.getData(nextTopStarLv);
			msg.writeUtf8String(data.itemId);
			msg.writeShort(rateData.materialCount);
			msg.writeShort(rateData.getSuccessRate());
		}
		//
		{
			Map<KGameAttrType, Integer> baseAtts = equiData == null ? temp.maxBaseAttMap : equiData.getBaseAtts();
			//
			msg.writeByte(baseAtts.size());
			for (Entry<KGameAttrType, Integer> entry : baseAtts.entrySet()) {
				msg.writeInt(entry.getKey().sign);
				msg.writeInt(entry.getValue());
				msg.writeInt(KItemLogic.ExpressionForStrongAtt(nowStrongLv, entry.getKey(), entry.getValue()));
				msg.writeInt(KItemLogic.ExpressionForStrongAtt(nowStrongLv + 1, entry.getKey(), entry.getValue()));
				msg.writeInt(KItemLogic.ExpressionForStarAtt(nowStarLv, entry.getKey(), entry.getValue()));
				msg.writeInt(KItemLogic.ExpressionForStarAtt(nowStarLv + 1, entry.getKey(), entry.getValue()));
			}
		}

		msg.writeByte(temp.getTotalEnchansePosition(equiData));
		
		// 遍历装备上的宝石
		Map<Integer, String> datas = Collections.emptyMap();
		if (equiData != null) {
			datas = equiData.getEnchanseCache();
		}
		msg.writeByte(datas.size());
		for (String itemCode : datas.values()) {
			KItemTempStone stoneTemp = (KItemTempStone) KItemDataManager.mItemTemplateManager.getItemTemplate(itemCode);
			packItem(msg, roleId, roleLv, stoneTemp, 1, null, stoneTemp.icon);
		}

		msg.writeByte(temp.stoneTypeMapForType.size());
		for (int type : temp.stoneTypeMapForType.keySet()) {
			msg.writeShort(type);
		}

		//
		if (equiData != null) {
			msg.writeInt(KSupportFactory.getRoleModuleSupport().calculateBattlePower(KGameUtilTool.changeAttMap(equiData.getAllEffect(null)), roleId));
		} else {
			msg.writeInt(KSupportFactory.getRoleModuleSupport().calculateBattlePower(temp.maxBaseAttMap, roleId));
		}
		msg.writeInt(temp.battlePowerMap.get(equiData == null ? temp.getMaxAattributeSection() : equiData.getLv()));

		// * 更高级装备获得途径
		// * byte N途径数量
		// * for(1~N){
		// * String 装备名称
		// * String 描述
		// * byte 关卡类型
		// * int 关卡ID
		// * int 关卡ICON
		// * String 关卡名称
		// * }

		//此功能已取消UI，无须发数据，但保留协议
		List<EquiProduceData> produceDatas = Collections.emptyList();//temp.getEquiProduceDataList(roleLv);
		msg.writeByte(produceDatas.size());
		for (EquiProduceData pdata : produceDatas) {
			try {
				msg.writeUtf8String(HyperTextTool.extColor(pdata.temp.ItemQuality.name + pdata.temp.name, pdata.temp.ItemQuality.color));
				msg.writeUtf8String(pdata.desc);
				msg.writeByte(pdata.levelType.levelType);
				msg.writeInt(pdata.level);
				KLevelTemplate leveldata = KSupportFactory.getLevelSupport().getNormalGameLevelTemplate(pdata.levelType, pdata.level);
				msg.writeInt(leveldata.getIconResId());
				msg.writeUtf8String(leveldata.getLevelName());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void packItem_material(KGameMessage msg, KItemTempMaterial temp) {
		
		if (temp.composeTarget.isEmpty()) {
			msg.writeBoolean(false);
		} else {
			msg.writeBoolean(true);
		}
	}

	/**
	 * <pre>
	 * 宝石道具基础属性
	 * 参考{@link KItemProtocol#MSG_STRUCT_ITEM_DETAILS_STONE}
	 * 
	 * @param msg
	 * @param roleId
	 * @param data
	 * @param isWithCompose 是否打包宝石合成数据
	 * @author CamusHuang
	 * @creation 2014-3-21 下午7:22:08
	 * </pre>
	 */
	private static void packItem_stone(KGameMessage msg, long roleId, int roleLv, KItemTempStone data, boolean isWithCompose) {
		msg.writeShort(data.stoneType);
		msg.writeShort(data.stonelvl);
		msg.writeByte(data.allEffects.size());
		for (Entry<KGameAttrType, Integer> entry : data.allEffects.entrySet()) {
			msg.writeInt(entry.getKey().sign);
			msg.writeInt(entry.getValue());
		}

		msg.writeByte(data.cancelEnchansePrice.currencyType.sign);
		msg.writeInt((int) data.cancelEnchansePrice.currencyCount);

		msg.writeInt(KSupportFactory.getRoleModuleSupport().calculateBattlePower(data.allEffects, roleId));
		//
		if (!isWithCompose) {
			msg.writeBoolean(false);
			return;
		}

		KItemTempStone temp = data.composeTarget == null ? null : (KItemTempStone) data.composeTarget.getItemTemplate();
		if (temp == null) {
			msg.writeBoolean(false);
			return;
		}
		// 宝石合成数据
		msg.writeBoolean(true);
		packItem(msg, roleId, roleLv, temp, 1, null, temp.icon);
		//
		msg.writeInt(data.compnum);
		// msg.writeByte(data.composePriceGold.currencyType.sign);
		// msg.writeInt((int) data.composePriceGold.currencyCount);
		// msg.writeShort(data.composePriceGoldSuccessRate);
		// msg.writeByte(data.composePriceDiamond.currencyType.sign);
		// msg.writeInt((int) data.composePriceDiamond.currencyCount);
		// msg.writeShort(data.composePriceDiamondSuccessRate);
	}

	private static void packItem_consume(KGameMessage msg, KItemTempConsume data) {
	}

	private static void packItem_fixedBox(KGameMessage msg) {
	}

	private static void packItem_randomBox(KGameMessage msg) {
	}

	private static void packItem_equiBox(KGameMessage msg) {
	}

	// private static void packItem_pet(KGameMessage msg) {
	// // CTODO
	// }
	//

	/**
	 * <pre>
	 * 将一个道具模板，按指定协议打包进消息
	 * 参考{@link KItemProtocol#MSG_STRUCT_ITEM_DETAILS}
	 * 
	 * @param msg
	 * @param temp
	 * @author CamusHuang
	 * @creation 2013-1-15 下午8:36:27
	 * </pre>
	 */
	public static void packItem(KGameMessage msg, KItemTempAbs temp, long count) {
		packItem(msg, 0, 1, temp, count, null, temp.icon);
	}
	
	/**
	 * <pre>
	 * 将一个道具模板，按指定协议打包进消息
	 * 参考{@link KItemProtocol#MSG_STRUCT_ITEM_DETAILS}
	 * 
	 * @param msg
	 * @param temp
	 * @param count
	 * @author CamusHuang
	 * @creation 2015-2-2 下午12:34:18
	 * </pre>
	 */
	public static void packItem(KGameMessage msg, long roleId, KItemTempAbs temp, long count) {
		packItem(msg, roleId, 1, temp, count, null, temp.icon);
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 要求使用指定的购买价格，而不使用道具模板默认的购买价格
	 * @param msg
	 * @param temp
	 * @param price
	 * @param count
	 * @author CamusHuang
	 * @creation 2014-4-12 下午3:21:53
	 * </pre>
	 */
	public static void packItem(KGameMessage msg, KItemTempAbs temp, long count, KCurrencyCountStruct price) {
		packItem(msg, 0, 1, temp, count, price, temp.icon);
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 要求使用指定的购买价格、icon，而不使用道具模板默认的数据
	 * @param msg
	 * @param temp
	 * @param count
	 * @param price
	 * @param icon
	 * @author CamusHuang
	 * @creation 2014-7-17 上午11:44:42
	 * </pre>
	 */
	public static void packItem(KGameMessage msg, KItemTempAbs temp, long count, KCurrencyCountStruct price, int icon) {
		packItem(msg, 0, 1, temp, count, price, icon);
	}

	private static void packItem(KGameMessage msg, long roleId, int roleLv, KItemTempAbs temp, long count, KCurrencyCountStruct price, int icon) {

		// 打包道具通用属性
		packItemBaseInfo(msg, -1, temp, price, count, icon);

		switch (temp.ItemType) {
		case 装备:
			packItem_equi(msg, roleId, roleLv, null, (KItemTempEqui) temp);
			break;
		case 改造材料:
			packItem_material(msg, (KItemTempMaterial) temp);
			break;
		case 宝石:
			packItem_stone(msg, roleId, roleLv, (KItemTempStone) temp, false);
			break;
		case 消耗品:
			packItem_consume(msg, (KItemTempConsume) temp);
			break;
		case 固定宝箱:
			packItem_fixedBox(msg);
			break;
		// case 宠物:
		// packItem_pet(msg);
		// break;
		case 随机宝箱:
			packItem_randomBox(msg);
			break;
		case 装备包:
			packItem_equiBox(msg);
			break;
		}
	}
	
	private static void packTeamPVPAttrSet(KGameMessage msg, ITeamPVPAttrSetInfo attrInfo, boolean packActivate) {
		msg.writeUtf8String(attrInfo.getSetName());
		msg.writeByte(attrInfo.getLevel());
		if (packActivate) {
			msg.writeBoolean(attrInfo.isActivate());
		}
		msg.writeByte(attrInfo.getAttrMap().size());
		for(Iterator<Map.Entry<KGameAttrType, Integer>> itr = attrInfo.getAttrMap().entrySet().iterator(); itr.hasNext();) {
			Map.Entry<KGameAttrType, Integer> entry = itr.next();
			msg.writeInt(entry.getKey().sign);
			msg.writeInt(entry.getValue());
		}
	}

	/**
	 * <pre>
	 * 
	 * 
	 * @param msg
	 * @param roleId
	 * @param item
	 * @author CamusHuang
	 * @creation 2014-7-14 下午4:58:53
	 * </pre>
	 */
	public static void packItemAndSlotId(KGameMessage msg, long roleId, int roleLv, KItem item) {
		KItemPackTypeEnum packType = item.getItemPackTypeEnum();
		msg.writeByte(packType.sign);
		if (packType == KItemPackTypeEnum.BODYSLOT) {
			KItemPack_BodySlot pack = KItemModuleExtension.getItemSet(item.getRoleId()).getSlot();
			long slotId = pack.searchSlotIdByItemId(item.getId());
			msg.writeLong(slotId);
		}
		packItem(msg, roleId, roleLv, item);
	}

	public static void packEquiSetData(KRole role, KGameMessage msg) {
		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		set.rwLock.lock();
		try {

			KItemPack_BodySlot bodySlot = set.getSlot();
			BodySlotData slot = bodySlot.getBodySlotData(KItemConfig.getInstance().MAIN_BODYSLOT_ID);
			SetData setData = slot.getSetData();
//			// 宝石套装
//			{
//				KEquiStoneSetData data = KItemDataManager.mEquiStoneSetDataManager.getData(setData.nowStoneSetLv);
//				msg.writeUtf8String(StringUtil.format(ItemTips.套装名称x已有数量x要求数量x, data.name, setData.nowStoneSetCount, KItemConfig.getInstance().TotalMaxEnchansePosition));
//				msg.writeByte(data.Suitcondition);
//				msg.writeBoolean(setData.isNowStoneSetEffect);
//				msg.writeByte(data.allEffects.size());
//				for (Entry<KGameAttrType, Integer> entry : data.allEffects.entrySet()) {
//					msg.writeInt(entry.getKey().sign);
//					msg.writeInt(entry.getValue());
//				}
//				msg.writeInt(data.mapResId);
//				// _LOGGER.warn("宝石套装=" + data.name + "(" +
//				// setData.nowStoneSetCount + "/" +
//				// KItemConfig.getInstance().TotalMaxEnchanseNum + ")");
//				//
//				msg.writeBoolean(setData.nextStoneSetLv > 0);
//				if (setData.nextStoneSetLv > 0) {
//					data = KItemDataManager.mEquiStoneSetDataManager.getData(setData.nextStoneSetLv);
//					msg.writeUtf8String(StringUtil.format(ItemTips.套装名称x已有数量x要求数量x, data.name, setData.nextStoneSetCount, KItemConfig.getInstance().TotalMaxEnchansePosition));
//					msg.writeByte(data.Suitcondition);
//					msg.writeByte(data.allEffects.size());
//					for (Entry<KGameAttrType, Integer> entry : data.allEffects.entrySet()) {
//						msg.writeInt(entry.getKey().sign);
//						msg.writeInt(entry.getValue());
//					}
//				}
//				// _LOGGER.warn("下一宝石套装=" + data.name + "(" +
//				// setData.nextStoneSetCount + "/" +
//				// KItemConfig.getInstance().TotalMaxEnchanseNum + ")");
//			}
			// 宝石套装
			{
				List<KEquiStoneSetData2> dataList = KItemDataManager.mEquiStoneSetDataManager2.getDataCache();
				msg.writeByte(dataList.size());
				for(KEquiStoneSetData2 data:dataList){
					
					AtomicInteger count = setData.stoneSetMap.get(data.Suitcondition);
					Map<KGameAttrType,AtomicInteger> attMap = setData.stoneSetAttMap.get(data.Suitcondition);
					if(attMap==null){
						attMap = Collections.emptyMap();
					}
					
					msg.writeByte(data.Suitcondition);
					msg.writeByte(count==null?0:count.get());
					msg.writeByte(data.resStoneNum);
					
					msg.writeByte(attMap.size());
					for(Entry<KGameAttrType,AtomicInteger> e:attMap.entrySet()){
						msg.writeInt(e.getKey().sign);
						msg.writeInt(e.getValue().get());
					}
				}
				msg.writeInt(setData.getEquiSetMapResIds()[1]);
			}	
			// 升星套装
			{
				KEquiStarSetData data = KItemDataManager.mEquiStarSetDataManager.getData(setData.nowStarSetLv);
				msg.writeUtf8String(StringUtil.format(ItemTips.套装名称x已有数量x要求数量x,data.name,setData.nowStarSetCount,KItemConfig.getInstance().TotalMaxEquiNum));
				msg.writeByte(data.Suitcondition);
				msg.writeBoolean(setData.isNowStarSetEffect);
				msg.writeByte(data.allEffects.size());
				for (Entry<KGameAttrType, Integer> entry : data.allEffects.entrySet()) {
					msg.writeInt(entry.getKey().sign);
					msg.writeInt(entry.getValue());
				}
				msg.writeInt(data.percent);
				msg.writeInt(data.mapResId);
				// _LOGGER.warn("升星套装=" + data.name + "(" +
				// setData.nowStarSetCount + "/" +
				// KItemConfig.getInstance().TotalMaxEquiNum + ")");
				//
				msg.writeBoolean(setData.nextStarSetLv > 0);
				if (setData.nextStarSetLv > 0) {
					data = KItemDataManager.mEquiStarSetDataManager.getData(setData.nextStarSetLv);
					msg.writeUtf8String(StringUtil.format(ItemTips.套装名称x已有数量x要求数量x,data.name,setData.nextStarSetCount,KItemConfig.getInstance().TotalMaxEquiNum));
					msg.writeByte(data.Suitcondition);
					msg.writeByte(data.allEffects.size());
					for (Entry<KGameAttrType, Integer> entry : data.allEffects.entrySet()) {
						msg.writeInt(entry.getKey().sign);
						msg.writeInt(entry.getValue());
					}
					msg.writeInt(data.percent);
				}
				// _LOGGER.warn("下一升星套装=" + data.name + "(" +
				// setData.nextStarSetCount + "/" +
				// KItemConfig.getInstance().TotalMaxEquiNum + ")");
			}
			// 强化套装
			{
				KEquiStrongSetData data = KItemDataManager.mEquiStrongSetDataManager.getDataByLv(setData.nowStrongSetLv);
				msg.writeUtf8String(StringUtil.format(ItemTips.套装名称x已有数量x要求数量x,data.name,setData.nowStrongSetCount,KItemConfig.getInstance().TotalMaxEquiNum));
				msg.writeByte(data.Suitcondition);
				msg.writeBoolean(setData.isNowStrongSetEffect);
				msg.writeByte(data.allEffects.size());
				for (Entry<KGameAttrType, Integer> entry : data.allEffects.entrySet()) {
					msg.writeInt(entry.getKey().sign);
					msg.writeInt(entry.getValue());
				}
				msg.writeInt(data.mapResId);
				msg.writeBoolean(setData.nextStrongSetLv > 0);
				if (setData.nextStrongSetLv > 0) {
					data = KItemDataManager.mEquiStrongSetDataManager.getDataByLv(setData.nextStrongSetLv);
					msg.writeUtf8String(StringUtil.format(ItemTips.套装名称x已有数量x要求数量x,data.name,setData.nextStrongSetCount,KItemConfig.getInstance().TotalMaxEquiNum));
					msg.writeByte(data.Suitcondition);
					msg.writeByte(data.allEffects.size());
					for (Entry<KGameAttrType, Integer> entry : data.allEffects.entrySet()) {
						msg.writeInt(entry.getKey().sign);
						msg.writeInt(entry.getValue());
					}
				}
			}
			
			// 品质套装
			{
				msg.writeByte(setData.quaSetIds.size());
				for(int id:setData.quaSetIds){
					msg.writeInt(id);
				}
			}
			
		} finally {
			set.rwLock.unlock();
		}
		ITeamPVPAttrSetInfo[] teamPVPAttrSet = KSupportFactory.getTeamPVPSupport().getTeamPVPAttrSetInfo(role.getId());
		packTeamPVPAttrSet(msg, teamPVPAttrSet[0], true);
		if(teamPVPAttrSet[1] != null) {
			msg.writeBoolean(true);
			packTeamPVPAttrSet(msg, teamPVPAttrSet[1], false);
		} else {
			msg.writeBoolean(false);
		}
	}

	public static void packItemListForGM(List<String> infos, long roleId) {
		StringBuffer sbf = new StringBuffer();
		sbf.append("ID").append('\t').append("类型").append('\t').append("ItemCode").append('\t').append("名称").append('\t').append("数量").append('\t').append("品质").append('\t').append("描述");
		infos.add(sbf.toString());

		KItemSet set = KItemModuleExtension.getItemSet(roleId);

		KItemPack_Bag pack = set.getBag();
		Map<Long, KItem> items = null;
		if (pack == null) {
			infos.add("【背包】");
		} else {
			infos.add("【背包】" + '\t' + pack.getVolume() + "格");
			items = pack.getAllItemsCache();
			for (KItem item : items.values()) {
				infos.add(itemSimpleInfoForGM(item));
			}
		}

		infos.add("");

		infos.add("【装备栏】");
		KItemPack_BodySlot slot = set.getSlot();
		if (slot != null) {
			items = slot.getAllItemsCache();
			for (KItem item : items.values()) {
				infos.add(itemSimpleInfoForGM(item));
			}
		}
	}

	private static String itemSimpleInfoForGM(KItem item) {
		StringBuffer sbf = new StringBuffer();
		KItemTempAbs temp = item.getItemTemplate();
		sbf.append(item.getId()).append('\t').append(temp.ItemType.name).append('\t').append(temp.id).append('\t').append(temp.name).append('\t').append(item.getCount()).append('\t')
				.append(temp.ItemQuality.name).append('\t').append(temp.getDesc());
		return sbf.toString();
	}

	public static void packItemForGM(List<String> infos, long roleId, long itemId) {
		StringBuffer sbf = new StringBuffer();
		sbf.append("项目").append('\t').append("数据1").append('\t').append("数据2").append('\t').append("数据3").append('\t').append("数据4");
		infos.add(sbf.toString());

		KItem item = KItemLogic.getItem(roleId, itemId);
		if (item == null) {
			infos.add("道具不存在");
			return;
		}
		KItemTempAbs temp = item.getItemTemplate();

		infos.add("ID" + '\t' + itemId);
		infos.add("ItemCode" + '\t' + temp.id);
		infos.add("名称" + '\t' + temp.name);
		infos.add("数量" + '\t' + item.getCount());
		infos.add("等级限制" + '\t' + temp.lvl);

		switch (temp.ItemType) {
		case 装备:
			equiInfoForGM(infos, item, (KItemTempEqui) temp);
			break;
		case 宝石:
			infos.add("宝石类型" + '\t' + ((KItemTempStone) temp).stoneType);
			infos.add("宝石等级" + '\t' + ((KItemTempStone) temp).stonelvl);
			for (Entry<KGameAttrType, Integer> e : ((KItemTempStone) temp).allEffects.entrySet()) {
				infos.add(e.getKey().getName() + '\t' + e.getValue());
			}
			break;
		case 改造材料:
			break;
		case 消耗品:
			break;
		case 固定宝箱:
			break;
		case 随机宝箱:
			break;
		case 装备包:
			break;
		}

		infos.add("");

		infos.add("描述" + '\t' + temp.getDesc());
	}

	private static void equiInfoForGM(List<String> infos, KItem item, KItemTempEqui tempdata) {
		infos.add("部位限制" + '\t' + (tempdata.typeEnum.name));
		infos.add("职业限制" + '\t' + (tempdata.jobEnum == null ? "不限" : tempdata.jobEnum.getJobName()));
		infos.add("等级限制" + '\t' + tempdata.lvl);
		infos.add("强化等级" + '\t' + item.getEquipmentData().getStrongLv());
		infos.add("升星" + '\t' + item.getEquipmentData().getStarLv());

		// 基础属性
		infos.add("");
		infos.add("【基础属性】" + '\t' + "【数值】");
		for (Entry<KGameAttrType, Integer> entry : item.getEquipmentData().getBaseAtts().entrySet()) {
			infos.add(entry.getKey().getName() + '\t' + entry.getValue());
		}
		// /////////////////////
		// 镶嵌
		infos.add("");
		infos.add("【镶嵌】" + '\t' + "【ItemCode】" + '\t' + "【品质】" + '\t' + "【类型】" + '\t' + "【等级】");
		for (String stoneItemCode : item.getEquipmentData().getEnchanseCache().values()) {
			KItemTempStone stone = (KItemTempStone) KItemDataManager.mItemTemplateManager.getItemTemplate(stoneItemCode);
			infos.add(stone.name + '\t' + stone.id + '\t' + stone.ItemQuality.name + '\t' + stone.stoneType + '\t' + stone.stonelvl);

			for (Entry<KGameAttrType, Integer> entry : stone.allEffects.entrySet()) {
				infos.add("" + '\t' + entry.getKey().getName() + '\t' + entry.getValue());
			}
		}
		// /////////////////////
		// 总属性
		infos.add("");
		infos.add("【总属性】" + '\t' + "【数值】");
		for (Entry<KGameAttrType, AtomicInteger> entry : item.getEquipmentData().getAllEffect(null).entrySet()) {
			infos.add(entry.getKey().getName() + '\t' + entry.getValue().get());
		}

	}

	public static Map<String, List<String>> packAllItemTemplatesForGM() {

		Map<String, List<String>> map = new HashMap<String, List<String>>();
		for (KItemTypeEnum type : KItemTypeEnum.values()) {
			List<String> list = new ArrayList<String>();
			map.put(type.name, list);
			StringBuffer sbf = new StringBuffer();
			sbf.append("ItemCode").append('\t').append("名称").append('\t').append("品质").append('\t').append("售价").append('\t').append("等级限制").append('\t');
			switch (type) {
			case 装备:
				sbf.append("部位").append('\t').append("职业").append('\t');
				break;
			case 宝石:
				sbf.append("宝石类型").append('\t').append("宝石等级").append('\t');
				break;
			case 消耗品:
				break;
			case 改造材料:
				break;
			case 固定宝箱:
				break;
			case 随机宝箱:
				break;
			case 装备包:
				break;
			}
			sbf.append("描述");
			list.add(sbf.toString());
		}

		for (KItemTempAbs temp : KItemDataManager.mItemTemplateManager.getItemTemplateList()) {
			StringBuffer sbf = new StringBuffer();
			sbf.append(temp.id).append('\t').append(temp.name).append('\t').append(temp.ItemQuality.name).append('\t');
			if (temp.sellMoney == null) {
				sbf.append("-");
			} else {
				sbf.append(temp.sellMoney.currencyCount);
			}
			sbf.append('\t').append(temp.lvl).append('\t');
			//
			switch (temp.ItemType) {
			case 装备:
				KItemTempEqui equi = ((KItemTempEqui) temp);
				sbf.append(equi.typeEnum.name).append('\t').append(equi.jobEnum == null ? "不限" : equi.jobEnum.getJobName()).append('\t');
				break;
			case 宝石:
				sbf.append(((KItemTempStone) temp).stoneType).append('\t').append(((KItemTempStone) temp).stonelvl).append('\t');
				break;
			default:
				break;
			}

			sbf.append(temp.getDesc());
			map.get(temp.ItemType.name).add(sbf.toString());
		}
		return map;
	}

	public static void packItemQualitySetConstance(KGameMessage msg){
		List<KEquiQualitySetData> setList = KItemDataManager.mEquiQualitySetDataManager.getDataCache();
		msg.writeShort(setList.size());
		for(KEquiQualitySetData set:setList){
			msg.writeInt(set.id);
			msg.writeUtf8String(set.name);
			msg.writeByte(set.lv);
			msg.writeByte(set.qua);
			msg.writeByte(set.Number);
			msg.writeByte(set.allEffects.size());
			for(Entry<KGameAttrType, Integer> e:set.allEffects.entrySet()){
				msg.writeInt(e.getKey().sign);
				msg.writeInt(e.getValue());
			}
		}
	}
}
