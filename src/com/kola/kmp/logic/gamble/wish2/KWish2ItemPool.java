package com.kola.kmp.logic.gamble.wish2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.koala.game.exception.KGameServerException;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KWish2ItemPool {

	public static final byte GOLD_POOL = 4;// 金币抽奖池类型
	public static final byte EQUIP_POOL = 1;// 装备池类型
	public static final byte PET_POOL = 2;// 随从池类型
	public static final byte RES_POOL = 3;// 资源池类型

	public static final byte[] poolType = { KWish2ItemPool.EQUIP_POOL, KWish2ItemPool.PET_POOL, KWish2ItemPool.RES_POOL, KWish2ItemPool.GOLD_POOL };

	public static Map<Byte, List<KWish2DropItem>> _dropItemListMap = new HashMap<Byte, List<KWish2DropItem>>();
	public static Map<Integer, KWish2DropItem> _alldropItemMap = new HashMap<Integer, KWish2DropItem>();
	public static Map<Byte, KWish2DropItem> _defaultDropItemMap = new HashMap<Byte, KWish2DropItem>();
	public static KWish2DropItem guideDropItem1;
	public static KWish2DropItem guideDropItem2;

	public static Map<Byte, PoolInfoData> _poolInfoMap = new HashMap<Byte, PoolInfoData>();
	static {
		_dropItemListMap.put(GOLD_POOL, new ArrayList<KWish2DropItem>());
		_dropItemListMap.put(EQUIP_POOL, new ArrayList<KWish2DropItem>());
		_dropItemListMap.put(PET_POOL, new ArrayList<KWish2DropItem>());
		_dropItemListMap.put(RES_POOL, new ArrayList<KWish2DropItem>());
	}

	public static void addDropableItem(byte poolType, KWish2DropItem item) {
		if (_alldropItemMap.containsKey(item.dropId)) {
			throw new RuntimeException("许愿掉落池存在相同的掉落id：" + item.dropId);
		}
		_dropItemListMap.get(poolType).add(item);
		_alldropItemMap.put(item.dropId, item);
		if (item.isDefaultItem) {
			_defaultDropItemMap.put(poolType, item);
		}
	}

	public static boolean constainsItem(int dropId) {
		return _alldropItemMap.containsKey(dropId);
	}

	public static List<KWish2DropItem> caculateDropableItemList(KRole role, byte poolType) {
		List<KWish2DropItem> poolItemList = _dropItemListMap.get(poolType);

		List<KWish2DropItem> lotteryList = new ArrayList<KWish2DropItem>();
		L1: for (int index = 1; index <= 10; index++) {
			int totalWeight = 0;
			List<KWish2DropItem> preLotteryList = new ArrayList<KWish2DropItem>();
			for (KWish2DropItem itemData : poolItemList) {
				if (role.getJob() == itemData.job || itemData.job == 0) {
					if (role.getLevel() >= itemData.openLv && role.getLevel() <= itemData.closeLv) {
						if (itemData.minAppearIndexs <= index && itemData.maxAppearIndexs >= index) {
							preLotteryList.add(itemData);
							totalWeight += itemData.appearWeight;
						}
					}
				}
			}
			if (preLotteryList.isEmpty()) {
				lotteryList.add(_defaultDropItemMap.get(poolType));
				continue;
			} else {
				int weight = UtilTool.random(0, totalWeight);
				int tempRate = 0;
				KWish2DropItem targetItemData = null;
				L2: for (KWish2DropItem itemData : preLotteryList) {
					if (tempRate < weight && weight <= (tempRate + itemData.appearWeight)) {						
						targetItemData = itemData;
						break L2;
					} else {
						tempRate += itemData.appearWeight;
					}
				}
				if(targetItemData == null){
					lotteryList.add(_defaultDropItemMap.get(poolType));
				}else{
					lotteryList.add(targetItemData);
				}
			}
		}
		UtilTool.randomList(lotteryList);
		return lotteryList;
	}

	public static List<KWish2DropItem> caculateGuideDropableItemList() {
		List<KWish2DropItem> lotteryList = new ArrayList<KWish2DropItem>();
		lotteryList.add(guideDropItem2);
		for (int i = 0; i < 9; i++) {
			lotteryList.add(guideDropItem1);
		}
		return lotteryList;
	}

	public static void initComplete() throws KGameServerException {
		if (_alldropItemMap.isEmpty()) {
			throw new RuntimeException("许愿2掉落池可掉落的道具数量为0！");
		} else {
			for (KWish2DropItem dropItem : _alldropItemMap.values()) {
				if (dropItem.dropCount == 0) {
					throw new KGameServerException("许愿2掉落池掉落编号：" + dropItem.dropId + "，可掉落数量为0");
				} else if (KSupportFactory.getItemModuleSupport().getItemTemplate(dropItem.itemCode) == null) {
					throw new KGameServerException("许愿2掉落池不存在[" + dropItem.itemCode + "]这个道具！");
				}
			}
			if (_defaultDropItemMap.get(GOLD_POOL) == null) {
				throw new KGameServerException("许愿2的金币掉落池没有配置默认掉落道具！");
			}
			if (_defaultDropItemMap.get(RES_POOL) == null) {
				throw new KGameServerException("许愿2资源掉落池没有配置默认掉落道具！");
			}
			if (_defaultDropItemMap.get(EQUIP_POOL) == null) {
				throw new KGameServerException("许愿2装备掉落池没有配置默认掉落道具！");
			}
			if (_defaultDropItemMap.get(PET_POOL) == null) {
				throw new KGameServerException("许愿2随从掉落池没有配置默认掉落道具！");
			}
			if (guideDropItem1 == null || guideDropItem2 == null) {
				throw new KGameServerException("许愿2随从掉落池没有配置新手引导掉落道具！");
			}
			for (byte poolType : KWish2ItemPool.poolType) {
				if (!_poolInfoMap.containsKey(poolType)) {
					throw new KGameServerException("许愿2找不到掉落池信息，掉落池类型：" + poolType);
				}
			}
		}
	}

	public static class KWish2DropItem {
		public final int dropId;
		public final int openLv;
		public final int closeLv;
		public final byte job;
		public final int appearWeight;// 出现进入抽奖池权重
		public final int minAppearIndexs;// 最小保护次数
		public final int maxAppearIndexs;// 最大保护次数
		public final int lotteryWeight;// 抽奖权重
		public final String itemCode;
		public final String extItemName;
		public final int dropCount;
		public final boolean isDefaultItem;
		public final boolean isMarqueeShow;// 是否跑马灯显示
		public final boolean isRare;
		public final int petId;
		public final ItemCountStruct itemCountStruct;

		public KWish2DropItem(int dropId, int openLv, int closeLv, byte job, int appearWeight, int minAppearIndexs, int maxAppearIndexs, int lotteryWeight, String itemCode, String extItemName,
				int dropCount, boolean isDefaultItem, boolean isMarqueeShow, boolean isRare) {
			super();
			this.dropId = dropId;
			this.openLv = openLv;
			this.closeLv = closeLv;
			this.job = job;
			this.appearWeight = appearWeight;
			this.minAppearIndexs = minAppearIndexs;
			this.maxAppearIndexs = maxAppearIndexs;
			this.lotteryWeight = lotteryWeight;
			this.itemCode = itemCode;
			this.extItemName = extItemName;
			this.dropCount = dropCount;
			this.isDefaultItem = isDefaultItem;
			this.isMarqueeShow = isMarqueeShow;
			this.isRare = isRare;
			this.petId = 0;
			this.itemCountStruct = new ItemCountStruct(itemCode, dropCount);
		}

		public KWish2DropItem(int dropId, int openLv, int closeLv, byte job, int appearWeight, int minAppearIndexs, int maxAppearIndexs, int lotteryWeight, String itemCode, String extItemName,
				int dropCount, boolean isDefaultItem, boolean isMarqueeShow, boolean isRare, int petId) {
			super();
			this.dropId = dropId;
			this.openLv = openLv;
			this.closeLv = closeLv;
			this.job = job;
			this.appearWeight = appearWeight;
			this.minAppearIndexs = minAppearIndexs;
			this.maxAppearIndexs = maxAppearIndexs;
			this.lotteryWeight = lotteryWeight;
			this.itemCode = itemCode;
			this.extItemName = extItemName;
			this.dropCount = dropCount;
			this.isDefaultItem = isDefaultItem;
			this.isMarqueeShow = isMarqueeShow;
			this.isRare = isRare;
			this.petId = petId;
			this.itemCountStruct = new ItemCountStruct(itemCode, dropCount);
		}
	}

	public static class PoolInfoData {
		public final byte poolType;
		public final String poolName;
		public final boolean isCanReflash;// 是否可刷新
		public final KCurrencyTypeEnum reflashCurrType;// 刷新使用类型
		public final int reflashUseCount;// 刷新使用货币数量
		public final KCurrencyTypeEnum lotteryCurrType;
		public final int wishUseCount;// 单次抽奖使用货币数量
		public final int wish10DiscountRate;// 单次抽奖使用货币数量
		public final boolean isCan10Lottery; 

		public PoolInfoData(byte poolType, String poolName, KCurrencyTypeEnum reflashCurrType, int reflashUseCount, KCurrencyTypeEnum lotteryCurrType, int wishUseCount, int wish10UseCount) {
			super();
			this.poolType = poolType;
			this.poolName = poolName;
			this.reflashCurrType = reflashCurrType;
			this.reflashUseCount = reflashUseCount;
			this.lotteryCurrType = lotteryCurrType;
			this.wishUseCount = wishUseCount;
			this.wish10DiscountRate = wish10UseCount;
			this.isCanReflash = true;
			this.isCan10Lottery = (this.wish10DiscountRate>0);
		}

		public PoolInfoData(byte poolType, String poolName, KCurrencyTypeEnum lotteryCurrType, int wishUseCount, int wish10UseCount) {
			super();
			this.poolType = poolType;
			this.poolName = poolName;
			this.lotteryCurrType = lotteryCurrType;
			this.wishUseCount = wishUseCount;
			this.wish10DiscountRate = wish10UseCount;
			this.reflashCurrType = null;
			this.reflashUseCount = 0;
			this.isCanReflash = false;
			this.isCan10Lottery = (this.wish10DiscountRate>0);
		}

	}

}
