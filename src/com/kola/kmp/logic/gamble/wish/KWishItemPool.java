package com.kola.kmp.logic.gamble.wish;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.other.KLimitTimeProduceActivityTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.TimeLimitActivityDataManager.TimeLimieProduceActivity;
import com.kola.kmp.logic.support.KSupportFactory;

public class KWishItemPool {

	public static final byte FREE_NORMAL_POOL = 0;// 免费普通许愿池类型
	public static final byte POOR_NORMAL_POOL = 1;// 屌丝普通许愿池类型
	public static final byte POOR_SPECIAL_POOL = 2;// 屌丝特殊许愿池类型
	public static final byte RICH_NORMAL_POOL = 3;// 高富帅普通许愿池类型
	public static final byte RICH_SPECIAL_POOL = 4;// 高富帅特殊许愿池类型
	public static final byte RICH_SPECIAL_ITEM_POOL = 5;// 高富帅特殊装备许愿池类型

	private static Map<Byte, List<KDropableItem>> _dropItemListMap = new HashMap<Byte, List<KDropableItem>>();
	private static Map<Integer, KDropableItem> _alldropItemMap = new HashMap<Integer, KDropableItem>();
	static {
		_dropItemListMap.put(FREE_NORMAL_POOL, new ArrayList<KDropableItem>());
		_dropItemListMap.put(POOR_NORMAL_POOL, new ArrayList<KDropableItem>());
		_dropItemListMap.put(POOR_SPECIAL_POOL, new ArrayList<KDropableItem>());
		_dropItemListMap.put(RICH_NORMAL_POOL, new ArrayList<KDropableItem>());
		_dropItemListMap.put(RICH_SPECIAL_POOL, new ArrayList<KDropableItem>());
		_dropItemListMap.put(RICH_SPECIAL_ITEM_POOL, new ArrayList<KDropableItem>());
	}

	void addDropableItem(byte poolType, KDropableItem item) {
		if (_alldropItemMap.containsKey(item.dropId)) {
			throw new RuntimeException("许愿掉落池存在相同的掉落id：" + item.dropId);
		}
		_dropItemListMap.get(poolType).add(item);
		_alldropItemMap.put(item.dropId, item);
	}

	public static boolean constainsItem(int dropId) {
		return _alldropItemMap.containsKey(dropId);
	}

	public int getRewardDropId(byte poolType, int roleLv) {
		KDropableItem current = getDropableItem(poolType, roleLv);
		if (current != null) {
			return current.dropId;
		}
		return 0;
	}

	public ItemCountStruct getReward(byte poolType, int roleLv) {
		KDropableItem current = getDropableItem(poolType, roleLv);
		if (current != null) {
			return new ItemCountStruct(current.itemCode, current.dropCount);
		} else {
			return null;
		}
	}

	public KDropableItem getDropableItem(byte poolType, int roleLv) {
		int totalRate = 0;
		KDropableItem current;
		LinkedHashMap<Integer, KDropableItem> map = new LinkedHashMap<Integer, KDropableItem>();

		// 检测是否限时产出活动
		float multiple = 1f;
		TimeLimieProduceActivity activity = KSupportFactory
				.getExcitingRewardSupport().getTimeLimieProduceActivity(
						KLimitTimeProduceActivityTypeEnum.初级扭蛋10连抽概率翻倍);

		if (activity != null && activity.isActivityTakeEffectNow()) {
			multiple = activity.probabilityRatio;
		}

		for (int i = 0; i < _dropItemListMap.get(poolType).size(); i++) {
			current = _dropItemListMap.get(poolType).get(i);
			if (current.openLv <= roleLv && roleLv <= current.closeLv) {
				if (activity != null && activity.niudanSet.contains(current.dropId)) {
					totalRate += (int) (current.rate * multiple);
				} else {
					totalRate += current.rate;
				}
				map.put(totalRate, current);
			}
		}
		if (map.size() > 0) {
			int actualRate = UtilTool.random(totalRate);
			Map.Entry<Integer, KDropableItem> entry;
			for (Iterator<Map.Entry<Integer, KDropableItem>> itr = map
					.entrySet().iterator(); itr.hasNext();) {
				entry = itr.next();
				if (actualRate < entry.getKey()) {
					return entry.getValue();
				}
			}
			return null;
		} else {
			return null;
		}
	}

	public ItemCountStruct getItemCountByDropId(int dropId) {
		KDropableItem dropableItem = _alldropItemMap.get(dropId);
		if (dropableItem != null) {
			return new ItemCountStruct(dropableItem.itemCode,
					dropableItem.dropCount);
		}
		return null;
	}

	void initComplete() {
		if (this._alldropItemMap.isEmpty()) {
			throw new RuntimeException("许愿掉落池可掉落的道具数量为0！");
		} else {
			for (KDropableItem dropItem : _alldropItemMap.values()) {
				if (dropItem.dropCount == 0) {
					throw new RuntimeException("许愿掉落池掉落编号：" + dropItem.dropId
							+ "，可掉落数量为0");
				} else if (KSupportFactory.getItemModuleSupport()
						.getItemTemplate(dropItem.itemCode) == null) {
					throw new RuntimeException("许愿掉落池不存在[" + dropItem.itemCode
							+ "]这个道具！");
				}
			}
		}
	}

	// public List<String> getPropableDropItemCodeList() {
	// List<String> result = new ArrayList<String>();
	// for (KDropableItem item : _dropItemList) {
	// if (!result.contains(item.itemCode)) {
	// result.add(item.itemCode);
	// }
	// }
	// return result;
	// }

	public static class KDropableItem {

		public final int dropId;
		public final int openLv;
		public final int closeLv;
		public final int rate;
		public final String itemCode;
		public final String extItemName;
		public final int dropCount;
		public final boolean isShow;

		public KDropableItem(int pDropId, int pOpenLv, int pCloseLv, int pRate,
				String pItemCode, String itemName, int pDropCount,
				boolean isShow) {
			this.dropId = pDropId;
			this.openLv = pOpenLv;
			this.closeLv = pCloseLv;
			this.rate = pRate;
			this.itemCode = pItemCode;
			this.extItemName = itemName;
			this.dropCount = pDropCount;
			this.isShow = isShow;
		}
	}
}
