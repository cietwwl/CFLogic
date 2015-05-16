package com.kola.kmp.logic.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.koala.game.KGameMessage;
import com.kola.kmp.logic.currency.KCurrencyAccountSet;
import com.kola.kmp.logic.currency.KCurrencyModuleExtension;
import com.kola.kmp.logic.item.KItem.KItemCA.KItem_EquipmentData;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempEqui;
import com.kola.kmp.logic.item.KItemPack_BodySlot.KItemPack_BodySlot_CA.BodySlotData.SetData;
import com.kola.kmp.logic.map.KGameMapEntity.RoleEquipShowData;
import com.kola.kmp.logic.other.KDataStructs.EquiSetStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KEquipmentTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.ItemModuleSupport;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_AddItem;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-2-21 下午4:59:04
 * </pre>
 */
public class KItemSupportImpl implements ItemModuleSupport {

	@Override
	public KItemTempAbs getItemTemplate(String itemCode) {
		return KItemDataManager.mItemTemplateManager.getItemTemplate(itemCode);
	}

	@Override
	public ItemResult_AddItem addItemToBag(KRole role, String itemCode, long addCount, String sourceTips) {
		return KItemLogic.addItemToBag(role, itemCode, addCount, sourceTips);
	}

	@Override
	public ItemResult_AddItem addItemsToBag(KRole role, List<ItemCountStruct> itemCounts, String sourceTips) {
		itemCounts = ItemCountStruct.mergeItemCountStructs(itemCounts);
		return KItemLogic.addItemsToBag(role, itemCounts, sourceTips);
	}

	@Override
	public ItemResult_AddItem addItemToBag(KRole role, ItemCountStruct itemStruct, String sourceTips) {
		return KItemLogic.addItemToBag(role, itemStruct.getItemTemplate(), itemStruct.itemCount, sourceTips);
	}

	@Override
	public boolean removeItemFromBag(long roleId, String itemCode, long count) {
		return KItemLogic.removeItemFromBag(roleId, itemCode, count, "其它模块");
	}

	public boolean removeItemFromBag(long roleId, long itemId, long count) {
		return KItemLogic.removeItemFromBag(roleId, itemId, count, "其它模块");
	}

	@Override
	public boolean isCanAddItemsToBag(long roleId, List<ItemCountStruct> itemCounts) {
		itemCounts = ItemCountStruct.mergeItemCountStructs(itemCounts);
		return KItemLogic.isCanAddItemsToBag(roleId, itemCounts);
	}

	@Override
	public int checkEmptyVolumeInBag(long roleId) {
		KItemSet set = KItemModuleExtension.getItemSet(roleId);
		set.rwLock.lock();
		try {
			KItemPack_Bag bag = set.getBag();
			return bag.checkEmptyVolume();
		} finally {
			set.rwLock.unlock();
		}
	}

	@Override
	public long checkItemCountInBag(long roleId, String itemCode) {
		KItemSet set = KItemModuleExtension.getItemSet(roleId);
		set.rwLock.lock();
		try {
			KItemPack_Bag bag = set.getBag();
			return bag.countItems(itemCode);
		} finally {
			set.rwLock.unlock();
		}
	}

	@Override
	public String checkItemCountInBag(long roleId, Map<String, Integer> checkMap) {
		KItemSet set = KItemModuleExtension.getItemSet(roleId);
		set.rwLock.lock();
		try {
			KItemPack_Bag bag = set.getBag();
			return bag.checkItemCount(checkMap);
		} finally {
			set.rwLock.unlock();
		}
	}

	@Override
	public void lockItemSet(long roleId) {
		KItemSet set = KItemModuleExtension.getItemSet(roleId);
		set.rwLock.lock();
	}

	@Override
	public void unlockItemSet(long roleId) {
		KItemSet set = KItemModuleExtension.getItemSet(roleId);
		set.rwLock.unlock();
	}

	@Override
	public void packSimpleEquipmentInfo(KGameMessage msg, long roleId) {
		KItemSet itemSet = KItemModuleExtension.getItemSet(roleId);
		KItemPack_BodySlot slot = itemSet.getSlot();
		List<KItem> list = slot.searchSlotItemList(KItemConfig.MAIN_BODYSLOT_ID);
		KItem item;
		KItemTempEqui template;
		msg.writeByte(list.size());
		for (int i = 0; i < list.size(); i++) {
			item = list.get(i);
			template = (KItemTempEqui) item.getItemTemplate();
			msg.writeByte(template.part);
			msg.writeUtf8String(template.showResId);
		}
	}

	public List<RoleEquipShowData> getRoleEquipShowDataList(long roleId) {
		List<RoleEquipShowData> list = new ArrayList<RoleEquipShowData>();
		KItemSet itemSet = KItemModuleExtension.getItemSet(roleId);
		KItemPack_BodySlot slot = itemSet.getSlot();
		List<KItem> searchlist = slot.searchSlotItemList(KItemConfig.MAIN_BODYSLOT_ID);
		KItem item;
		KItemTempEqui template;
		for (int i = 0; i < searchlist.size(); i++) {
			item = searchlist.get(i);
			template = (KItemTempEqui) item.getItemTemplate();
			RoleEquipShowData data = new RoleEquipShowData();
			data.equipType = (byte) (template.part);
			data.equipResData = template.showResId;
			data.quality = template.ItemQuality;
			list.add(data);
		}
		return list;
	}

	@Override
	public ISecondWeapon getSecondWeaponArgs(long roleId) {
		KItemSet itemSet = KItemModuleExtension.getItemSet(roleId);
		KItemPack_BodySlot slot = itemSet.getSlot();
		KItem item = slot.searchSlotItem(KItemConfig.MAIN_BODYSLOT_ID, KEquipmentTypeEnum.副武器);
		if (item == null || item.getEquipmentData() == null) {
			return null;
		}

		item.getEquipmentData().mSecondWeapon.reset();
		return item.getEquipmentData().mSecondWeapon;
	}

	@Override
	public int[] getWeaponIcons(long roleId) {
		int[] array = new int[2];
		KItemSet itemSet = KItemModuleExtension.getItemSet(roleId);
		KItemPack_BodySlot slot = itemSet.getSlot();
		KItem item = slot.searchSlotItem(KItemConfig.MAIN_BODYSLOT_ID, KEquipmentTypeEnum.主武器);
		if (item != null) {
			array[0] = item.getItemTemplate().icon;
		}
		item = slot.searchSlotItem(KItemConfig.MAIN_BODYSLOT_ID, KEquipmentTypeEnum.副武器);
		if (item != null) {
			array[1] = item.getItemTemplate().icon;
		}
		return array;
	}

	@Override
	public List<KItem> getRoleEquipmentList(long roleId) {
		KItemSet set = KItemModuleExtension.getItemSet(roleId);
		KItemPack_BodySlot slot = set.getSlot();

		set.rwLock.lock();
		try {
			return slot.searchSlotItemList(KItemConfig.MAIN_BODYSLOT_ID);
		} finally {
			set.rwLock.unlock();
		}
	}

	@Override
	public void packItemDataToMsg(KItem item, int roleLv, KGameMessage msg) {
		KItemMsgPackCenter.packItem(msg, item.getRoleId(), roleLv, item);
	}

	@Override
	public void packEquiSetDataToMsg(KRole role, KGameMessage msg) {
		KItemMsgPackCenter.packEquiSetData(role, msg);
	}

	public int[] getEquiSetMapResIds(long roleId) {
		KItemSet set = KItemModuleExtension.getItemSet(roleId);
		KItemPack_BodySlot slot = set.getSlot();

		set.rwLock.lock();
		try {
			SetData setData = slot.getBodySlotData(KItemConfig.MAIN_BODYSLOT_ID).getSetData();
			return setData.getEquiSetMapResIds();
		} finally {
			set.rwLock.unlock();
		}
	}

	@Override
	public EquiSetStruct getEquiSets(long roleId) {
		KItemSet set = KItemModuleExtension.getItemSet(roleId);
		KItemPack_BodySlot slot = set.getSlot();

		set.rwLock.lock();
		try {
			SetData setData = slot.getBodySlotData(KItemConfig.MAIN_BODYSLOT_ID).getSetData();
			return setData.getEquiSets();
		} finally {
			set.rwLock.unlock();
		}
	}

	@Override
	public void cloneRoleByCamus(KRole myRole, KRole srcRole) {
		KItemSet myset = KItemModuleExtension.getItemSet(myRole.getId());
		KItemPack_Bag mybag = myset.getBag();
		KItemPack_BodySlot myslot = myset.getSlot();
		KItemLogic.clearBagForTestOrder(myRole.getId());
		
		
		KItemSet srcset = KItemModuleExtension.getItemSet(srcRole.getId());
		KItemPack_Bag srcbag = srcset.getBag();
		KItemPack_BodySlot srcslot = srcset.getSlot();
		//克隆背包
		{
			mybag.setVolume(srcbag.getVolume());
			
			ItemResult_AddItem result = new ItemResult_AddItem();
			for(KItem item:srcbag.copyAllItems()){
				mybag.addItem(item.getItemTemplate(), item.getCount(), result);
			}
		}
		//克隆装备栏
		{
			myset.rwLock.lock();
			try {
				//清理原装备
				for(KItem item:myslot.copyAllItems()){
					myslot.uninstallItem(item.getId());
					mybag.moveInItem(item);
					item.changeCount(-item.getCount());
				}
				// 加装备
				mybag.setVolume(mybag.getVolume()+20);
				for (KItem srcitem:srcslot.getAllItemsCache().values()) {
					ItemResult_AddItem addResult = mybag.addItem(srcitem.getItemTemplate(), 1, null);
					if(addResult.isSucess){
						KItem newItem =addResult.getItem();

						// 将装备从背包移除再放入装备栏
						mybag.moveOutItem(newItem.getId());
						myslot.installItem(KItemConfig.MAIN_BODYSLOT_ID, newItem);
						
						KItem_EquipmentData newdata = newItem.getEquipmentData();
						
						KItem_EquipmentData srcdata = srcitem.getEquipmentData();
						
						newdata.setStrongLv(srcdata.getStrongLv());
						newdata.setStarLv(srcdata.getStarLv());
						for(Entry<Integer,String> srce:srcdata.getEnchanseCache().entrySet()){
							newdata.setEnchanseStone(srce.getKey(), srce.getValue());
						}
					}
				}
				mybag.setVolume(mybag.getVolume()-20);

				// 重算套装数据
				myslot.recountEquiSetData(KItemConfig.MAIN_BODYSLOT_ID);
			} finally {
				myset.rwLock.unlock();
			}
		}
		
		

		// 刷新角色属性
		KItemAttributeProvider.notifyEffectAttrChange(myRole);
		// 刷新UI
		KSupportFactory.getRoleModuleSupport().updateEquipmentRes(myRole.getId());
		KSupportFactory.getTeamPVPSupport().notifyRoleEquipmentResUpdate(myRole.getId());
	}

	@Override
	public boolean isRedWepond(long roleId) {
		return KItemLogic.isRedWepond(roleId);
	}
}
