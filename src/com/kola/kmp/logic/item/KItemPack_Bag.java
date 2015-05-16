package com.kola.kmp.logic.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.item.impl.KAItemPack;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempConsume;
import com.kola.kmp.logic.item.listener.KItemListenerManager;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KItemPackTypeEnum;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_AddItem;
import com.kola.kmp.logic.util.tips.ItemTips;

/**
 * <pre>
 * 游戏逻辑对于背包和仓库的封装
 * 
 * @author CamusHuang
 * @creation 2012-11-7 下午12:38:56
 * </pre>
 */
public class KItemPack_Bag extends KAItemPack<KItem> {

	public final KItemPackTypeEnum packTypeEnum;

	private KItemPack_Common_CA ca;

	/**
	 * <pre>
	 * 
	 * 
	 * @param roleId
	 * @param itemPackTemplate
	 * @param itemList
	 * @author CamusHuang
	 * @creation 2012-11-20 下午12:41:15
	 * </pre>
	 */
	KItemPack_Bag(KItemSet owner, long roleId, KItemPackTypeEnum packTypeEnum, boolean isFirstNew) {
		super(owner, roleId, packTypeEnum.sign, isFirstNew);

		this.packTypeEnum = packTypeEnum;
		ca = new KItemPack_Common_CA(this, isFirstNew);
	}

	protected void decodeCA(JSONObject caObj) {
		ca.decodeAttribute(caObj);
	}

	protected void dbDataInitFinished() {
		// 忽略
	}

	protected JSONObject encodeCA() {
		return ca.encodeAttribute();
	}

	public KItemPackTypeEnum getItemPackTypeEnum() {
		return KItemPackTypeEnum.getEnum(super.getItemPackType());
	}

	public void setVolume(int volume) {
		rwLock.lock();
		try {
			if (volume == ca.currentVolume) {
				return;
			}

			int maxVolume = KItemDataManager.mBagExtDataManager.getMaxGridCount();
			if (volume > maxVolume) {
				volume = maxVolume;
			}
			ca.currentVolume = volume;
			notifyDB();
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 当前容量
	 * 即格子数量
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-31 上午10:53:01
	 * </pre>
	 */
	public int getVolume() {
		return ca.currentVolume;
	}

	/**
	 * <pre>
	 * 检查剩余容量
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2012-11-19 下午5:04:36
	 * </pre>
	 */
	public int checkEmptyVolume() {
		rwLock.lock();
		try {
			return ca.currentVolume - _elementsMap.size();
		} finally {
			rwLock.unlock();
		}
	}

	protected void addItem(KItem item) {
		super.addItem(item);
		
		// 通知监听器
		KItemListenerManager.notifyBagItemChangeCount(owner._roleId, item.getItemTemplate(), item.getCount(), true);
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 将道具从其它道具包移入
	 * @param item
	 * @author CamusHuang
	 * @creation 2014-3-4 上午11:32:35
	 * </pre>
	 */
	protected void moveInItem(KItem item) {
		rwLock.lock();
		try {
			item.resetOwner(this);
			_elementsMap.put(item.getId(), item);
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 警告：本方法将令道具无主，道具内部rwLock将为null，部分方法可能因此抛异常！
	 * 因此在本方法之后必须立即调用相关方法，对道具进行【有主】处理；并且不能在中间做其它操作！
	 * 
	 * @deprecated 将道具从背包移出
	 * @param itemId
	 * @author CamusHuang
	 * @creation 2014-3-4 上午11:32:35
	 * </pre>
	 */
	protected void moveOutItem(long itemId) {
		rwLock.lock();
		try {
			KItem item = _elementsMap.remove(itemId);
			if (item != null) {
				item.resetOwner(null);
			}
		} finally {
			rwLock.unlock();
		}
	}

	ItemResult_AddItem addItem(KItemTempAbs template, long count, ItemResult_AddItem result) {
		if (result == null) {
			result = new ItemResult_AddItem();
		}
		if (template == null) {
			result.tips = ItemTips.物品不存在;
			return result;
		}
		if (result.updateItemCountList == null) {
			result.updateItemCountList = new ArrayList<KItem>();
		}
		if (result.newItemList == null) {
			result.newItemList = new ArrayList<KItem>();
		}
		//
		rwLock.lock();
		try {
			if (template.isCanStack()) {
				// 可叠加
				KItem oldItem = searchItem(template.itemCode);
				if (oldItem != null) {
					// 直接修改已有道具的数量
					oldItem.changeCount(count);
					//
					result.isSucess = true;
					result.tips = ItemTips.添加物品成功;
					result.updateItemCountList.add(oldItem);
					return result;
				} else {
					// 添加新道具
					if (checkEmptyVolume() < 1) {
						// 背包已满
						result.tips = ItemTips.背包已满;
						return result;
					}

					KItem item = new KItem(template, count);
					addItem(item);
					//
					result.isSucess = true;
					result.tips = ItemTips.添加物品成功;
					result.newItemList.add(item);
					return result;
				}
			} else {
				// 不可叠加
				// 添加新道具
				if (checkEmptyVolume() < count) {
					// 背包已满
					result.tips = ItemTips.背包已满;
					return result;
				}
				for (int i = 0; i < count; i++) {
					KItem item = new KItem(template, 1);
					addItem(item);
					result.newItemList.add(item);
				}
				//
				result.isSucess = true;
				result.tips = ItemTips.添加物品成功;
				return result;
			}
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 增加指定数量的道具
	 * 会判断是否可合并道具
	 * 要不就全加，要不就一个都不加
	 * 
	 * @param itemStructs （不允许包含重复的ItemCode）
	 * @author CamusHuang
	 * @creation 2012-11-21 下午4:36:23
	 * </pre>
	 */
	ItemResult_AddItem addItems(List<ItemCountStruct> itemStructs, ItemResult_AddItem result) {
		if (result == null) {
			result = new ItemResult_AddItem();
		}
		if (itemStructs == null || itemStructs.isEmpty()) {
			result.isSucess = true;
			result.tips = ItemTips.添加物品成功;
			return result;
		}

		if (result.updateItemCountList == null) {
			result.updateItemCountList = new ArrayList<KItem>();
		}
		if (result.newItemList == null) {
			result.newItemList = new ArrayList<KItem>();
		}

		rwLock.lock();
		try {
			if (!isBagCanAddItems(itemStructs)) {
				// 容量不足
				result.tips = ItemTips.背包容量不足;
				return result;
			}

			for (ItemCountStruct data : itemStructs) {
				long count = data.itemCount;
				if (count < 1) {
					continue;
				}
				KItemTempAbs template = data.getItemTemplate();
				if (template == null) {
					continue;
				}
				//
				if (template.isCanStack()) {
					// 可叠加
					KItem oldItem = searchItem(template.itemCode);
					if (oldItem != null) {
						// 直接修改已有道具的数量
						oldItem.changeCount(count);
						//
						result.updateItemCountList.add(oldItem);
						continue;
					} else {
						// 添加新道具
						KItem item = new KItem(template, count);
						addItem(item);
						//
						result.newItemList.add(item);
						continue;
					}
				} else {
					// 不可叠加
					// 添加新道具
					for (int i = 0; i < count; i++) {
						KItem item = new KItem(template, 1);
						addItem(item);
						//
						result.newItemList.add(item);
					}
					continue;
				}
			}

			result.isSucess = true;
			result.tips = ItemTips.添加物品成功;
			return result;
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 检查是否能放入指定道具
	 * 
	 * @param itemStructs （不允许包含重复的ItemCode）
	 * @return
	 * @author CamusHuang
	 * @creation 2012-12-7 上午10:33:59
	 * </pre>
	 */
	boolean isBagCanAddItems(List<ItemCountStruct> itemStructs) {
		rwLock.lock();
		try {
			int gridCount = checkUseEmptyVolume(itemStructs);// 独占格数计算
			return checkEmptyVolume() >= gridCount;
		} finally {
			rwLock.unlock();
		}
	}

	boolean isBagCanAddItem(ItemCountStruct itemStruct) {
		rwLock.lock();
		try {
			int gridCount = checkUseEmptyVolume(itemStruct.getItemTemplate(), itemStruct.itemCount);// 独占格数计算
			return checkEmptyVolume() >= gridCount;
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 检查放入指定的道具，需要占用多少格空格
	 * 与已有道具合并，当作不占用空格
	 * 
	 * @param itemStructs （不允许包含重复的ItemCode，调用者必须保证数量为正数）
	 * @return
	 * @author CamusHuang
	 * @creation 2012-11-21 下午4:11:01
	 * </pre>
	 */
	private int checkUseEmptyVolume(List<ItemCountStruct> itemStructs) {
		int gridCount = 0;// 独占格数计算
		if (itemStructs == null || itemStructs.isEmpty()) {
			return gridCount;
		}

		for (ItemCountStruct itemCountStruct : itemStructs) {
			KItemTempAbs temp = itemCountStruct.getItemTemplate();
			gridCount += checkUseEmptyVolume(temp, itemCountStruct.itemCount);
		}

		return gridCount;
	}

	/**
	 * <pre>
	 * 检查放入指定的道具，需要占用多少格空格
	 * 与已有道具合并，当作不占用空格
	 * 
	 * @param temp 
	 * @param addItemCount 调用者必须保证数量为正数
	 * @return
	 * @author CamusHuang
	 * @creation 2012-12-11 下午3:41:49
	 * </pre>
	 */
	private int checkUseEmptyVolume(KItemTempAbs template, long addItemCount) {
		if (template == null || addItemCount < 1) {
			return 0;
		}

		if (template.isCanStack()) {
			// 可叠加
			KItem oldItem = searchItem(template.itemCode);
			if (oldItem != null) {
				return 0;
			} else {
				return 1;
			}
		} else {
			// 不可叠加
			return (int) addItemCount;
		}
	}

	/**
	 * <pre>
	 * 检查道具包中是否有参数所指定数量的道具
	 * 
	 * @param checkMap <ItemCode,要求数量>
	 * @return 缺少道具则返回相应的道具ItemCode
	 * @author CamusHuang
	 * @creation 2013-5-23 下午8:43:04
	 * </pre>
	 */
	String checkItemCount(Map<String, Integer> checkMap) {
		if (checkMap == null || checkMap.isEmpty()) {
			return null;
		}
		rwLock.lock();
		try {
			KItem item;
			for (Entry<String, Integer> e : checkMap.entrySet()) {
				item = searchItem(e.getKey());
				if (item == null || item.getCount() < e.getValue()) {
					return e.getKey();
				}
			}
			return null;
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 获取指定道具的CD剩余时间
	 * 
	 * @param tempCoume
	 * @return
	 * @author CamusHuang
	 * @creation 2014-2-27 下午3:33:32
	 * </pre>
	 */
	public long getItemCDReleaseTime(KItemTempConsume tempCoume) {
		if (tempCoume.cdTimeInMill <= 0) {
			return 0;
		}
		rwLock.lock();
		try {
			Long endTime = ca.useItemCDEndTimeMap.get(tempCoume.itemCode);
			if (endTime == null) {
				return 0;
			}
			long nowTime = System.currentTimeMillis();
			if (nowTime > endTime) {
				ca.useItemCDEndTimeMap.remove(tempCoume.itemCode);
				return 0;
			}
			return nowTime - endTime;
		} finally {
			rwLock.unlock();
		}
	}

	public void setItemCDReleaseTime(KItemTempConsume tempCoume) {
		if (tempCoume.cdTimeInMill <= 0) {
			return;
		}
		rwLock.lock();
		try {
			long nowTime = System.currentTimeMillis();
			ca.useItemCDEndTimeMap.put(tempCoume.itemCode, nowTime + tempCoume.cdTimeInMill);
		} finally {
			rwLock.unlock();
		}
	}

	static class KItemPack_Common_CA implements KItemPackCustomizeAttribute {

		final KItemPack_Bag owner;
		final ReentrantLock rwLock;

		/** 当前容量 */
		private int currentVolume;

		/** 下一天的凌晨零点 */
		private long nextDayStartTime;

		/** 道具使用CD时间记录：不保存到DB */
		private HashMap<String, Long> useItemCDEndTimeMap = new HashMap<String, Long>();

		// //////////////////
		protected static final String JSON_PACK_NULL = "NULL";// null
		protected static final String JSON_PACK_VER = "0";// 版本
		//
		// 背包、仓库
		protected static final String JSON_PACK_BASEINFO = "A";// 基础信息
		protected static final String JSON_PACK_BASEINFO_NEXTDAYTIME = "1";// 明天开始的时间
		protected static final String JSON_PACK_BASEINFO_VOLUME = "2";// 当前容量

		//

		KItemPack_Common_CA(KItemPack_Bag owner, boolean isFirstNew) {
			this.owner = owner;
			this.rwLock = owner.rwLock;
			//
			this.currentVolume = KItemDataManager.mBagExtDataManager.getFreeGridCount();
		}

		public void decodeAttribute(JSONObject obj) {
			if (obj == null) {
				return;
			}
			// 由底层调用,解释出逻辑层数据
			rwLock.lock();
			try {
				int ver = obj.getInt(JSON_PACK_VER);// 默认版本
				// CEND 道具--暂时只有版本0
				switch (ver) {
				case 0:
					decodeBaseInfo(obj.getJSONObject(JSON_PACK_BASEINFO));
					//
					// CEND 道具--背包或仓库独有信息解释
					break;
				}

			} catch (Exception ex) {
				_LOGGER.error("decode数据时发生错误 roleId=" + owner.getRoleId() + " packType=" + owner.packTypeEnum + " ----丢失数据！", ex);
			} finally {
				rwLock.unlock();
			}
		}

		/**
		 * <pre>
		 * 
		 * @param obj
		 * @throws JSONException
		 * @author CamusHuang
		 * @creation 2012-11-20 下午12:01:05
		 * </pre>
		 */
		private void decodeBaseInfo(JSONObject obj) throws JSONException {
			nextDayStartTime = obj.optLong(JSON_PACK_BASEINFO_NEXTDAYTIME);
			currentVolume = obj.getInt(JSON_PACK_BASEINFO_VOLUME);
		}

		public JSONObject encodeAttribute() {
			rwLock.lock();
			try {
				JSONObject obj = new JSONObject();
				obj.put(JSON_PACK_VER, 0);// 默认版本
				// CEND 道具--暂时只有版本0
				obj.put(JSON_PACK_BASEINFO, encodeBaseInfo());// 基础信息
				// CEND 道具--背包或仓库独有信息打包
				return obj;
			} catch (Exception ex) {
				_LOGGER.error("encode数据时发生错误roleId=" + owner.getRoleId() + " packType=" + owner._itemPackType + " ----丢失数据！", ex);
				return null;
			} finally {
				rwLock.unlock();
			}
		}

		/**
		 * <pre>
		 * 基础信息打包
		 * 
		 * @return
		 * @throws JSONException
		 * @author CamusHuang
		 * @creation 2012-11-12 下午8:34:48
		 * </pre>
		 */
		private JSONObject encodeBaseInfo() throws JSONException {
			JSONObject obj = new JSONObject();
			obj.put(JSON_PACK_BASEINFO_NEXTDAYTIME, this.nextDayStartTime);
			obj.put(JSON_PACK_BASEINFO_VOLUME, this.currentVolume);
			return obj;
		}
	}

}
