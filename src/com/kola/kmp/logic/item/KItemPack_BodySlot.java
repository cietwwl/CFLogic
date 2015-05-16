package com.kola.kmp.logic.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.item.impl.KAItemPack;
import com.kola.kmp.logic.item.KItem.KItemCA.KItem_EquipmentData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiQualitySetData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStarSetData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStoneSetData2;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStrongSetData;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempEqui;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempStone;
import com.kola.kmp.logic.item.KItemPack_BodySlot.KItemPack_BodySlot_CA.BodySlotData;
import com.kola.kmp.logic.item.KItemPack_BodySlot.KItemPack_BodySlot_CA.BodySlotData.SetData;
import com.kola.kmp.logic.other.KDataStructs.EquiSetStruct;
import com.kola.kmp.logic.other.KEquipmentTypeEnum;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KItemPackTypeEnum;
import com.kola.kmp.logic.util.KGameUtilTool;
import com.kola.kmp.logic.util.ResultStructs.EquiSetResult;

/**
 * <pre>
 * 游戏逻辑对于装备栏的封装
 * 用一个底层MAP装着所有装备栏的道具；用一个BodySlotData表示一个装备栏，里面保存了这个装备栏对应的装备道具的ID；
 * 
 * @author CamusHuang
 * @creation 2012-11-7 下午12:38:56
 * </pre>
 */
class KItemPack_BodySlot extends KAItemPack<KItem> {

	private KItemPack_BodySlot_CA ca;

	/**
	 * <pre>
	 * 
	 * 
	 * @param pMap
	 * @param type
	 * @param itemPackTemplate
	 * @author CamusHuang
	 * @creation 2012-11-7 下午6:02:04
	 * </pre>
	 */
	KItemPack_BodySlot(KItemSet owner, long roleId, KItemPackTypeEnum packTypeEnum, boolean isFirstNew) {
		super(owner, roleId, packTypeEnum.sign, isFirstNew);

		// 必须在itemPackTemplate初始化之后才能NEW CA
		ca = new KItemPack_BodySlot_CA(this, isFirstNew);

		// 新角色，初始化套装属性
		if (isFirstNew) {
			recountEquiSetData();
		}
		// CEND 道具--装备栏初始化是否有什么事情要做？
	}

	protected void decodeCA(JSONObject caObj) {
		ca.decodeAttribute(caObj);
	}

	protected void dbDataInitFinished() {
		// 重算一次套装数据
		recountEquiSetData();
	}

	protected JSONObject encodeCA() {
		return ca.encodeAttribute();
	}

	// /**
	// * <pre>
	// *
	// * @deprecated 装备栏不允许直接添加道具
	// * @param item
	// * @author CamusHuang
	// * @creation 2014-2-28 下午3:27:15
	// * </pre>
	// */
	// public void addItem(KItem item) {
	// }

	// /**
	// * <pre>
	// *
	// * @deprecated 装备栏不允许直接删除某件道具
	// * @param slotId
	// * @param itemId
	// * @return false表示道具不存在
	// * @author CamusHuang
	// * @creation 2012-11-20 下午2:56:28
	// * </pre>
	// */
	// public KItem deleteItem(long itemId) {
	// rwLock.lock();
	// try {
	// KItem item = super.deleteItem(itemId);
	// for (BodySlotData slot : ca.bodySlotMap.values()) {
	// if (slot.equiList.remove(itemId)) {
	// notifyDB();
	// }
	// }
	// return item;
	// } finally {
	// rwLock.unlock();
	// }
	// }

	/**
	 * <pre>
	 * 警告：本方法将令道具无主，道具内部rwLock将为null，部分方法可能因此抛异常！
	 * 因此在本方法之后必须立即调用相关方法，对道具进行【有主】处理；并且不能在中间做其它操作！
	 * 
	 * @deprecated 本方法仅将道具从缓存移除，并不从DB彻底删除，仅用于装备卸载
	 * @param itemId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-4 上午11:20:27
	 * </pre>
	 */
	KItem uninstallItem(long itemId) {
		rwLock.lock();
		try {
			KItem item = _elementsMap.remove(itemId);
			KItemTempEqui equiTemp = (KItemTempEqui) item.getItemTemplate();

			for (BodySlotData slot : ca.bodySlotMap.values()) {
				Long tempId = slot.equiMap.get(equiTemp.typeEnum);
				if (tempId != null && tempId == itemId) {
					slot.equiMap.remove(equiTemp.typeEnum);
					notifyDB();
				}
			}

			if (item != null) {
				item.resetOwner(null);
			}
			return item;
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 警告：安装装备前，请先将原装备移除
	 * 
	 * @deprecated 本方法仅将道具加入缓存，并不重新插入DB，仅用于装备安装
	 * @param item
	 * @author CamusHuang
	 * @creation 2014-3-4 下午1:23:59
	 * </pre>
	 */
	void installItem(long slotId, KItem item) {
		rwLock.lock();
		try {
			item.resetOwner(this);
			_elementsMap.put(item.getId(), item);

			BodySlotData slot = ca.getOrNewBodySlotData(slotId);
			KItemTempEqui equiTemp = (KItemTempEqui) item.getItemTemplate();
			slot.equiMap.put(equiTemp.typeEnum, item.getId());
			notifyDB();
		} finally {
			rwLock.unlock();
		}
	}

	KItemPackTypeEnum getItemPackTypeEnum() {
		return KItemPackTypeEnum.getEnum(super.getItemPackType());
	}

	List<Long> getAllSlotId() {
		return new ArrayList<Long>(ca.bodySlotMap.keySet());
	}

	// boolean containSlotId(long slotId) {
	// return ca.bodySlotMap.containsKey(slotId);
	// }

	List<KItem> searchSlotItemList(long slotId) {
		rwLock.lock();
		try {

			BodySlotData slot = ca.bodySlotMap.get(slotId);
			if (slot != null && !slot.equiMap.isEmpty()) {
				List<KItem> list = new ArrayList<KItem>();
				for (Long itemId : slot.equiMap.values()) {
					KItem item = _elementsMap.get(itemId);
					if (item != null) {
						list.add(item);
					}
				}
				return list;
			}
			return Collections.emptyList();
		} finally {
			rwLock.unlock();
		}
	}

	Map<Long, KItem> searchSlotItemMap(long slotId) {
		rwLock.lock();
		try {
			BodySlotData slot = ca.bodySlotMap.get(slotId);
			if (slot != null && !slot.equiMap.isEmpty()) {
				Map<Long, KItem> map = new HashMap<Long, KItem>();
				for (Long itemId : slot.equiMap.values()) {
					KItem item = _elementsMap.get(itemId);
					if (item != null) {
						map.put(item.getId(), item);
					}
				}
				return map;
			}
			return Collections.emptyMap();
		} finally {
			rwLock.unlock();
		}
	}

	KItem searchSlotItem(long slotId, KEquipmentTypeEnum type) {
		rwLock.lock();
		try {
			BodySlotData slot = ca.bodySlotMap.get(slotId);
			if (slot != null && !slot.equiMap.isEmpty()) {

				Long itemId = slot.equiMap.get(type);
				if (itemId == null) {
					return null;
				}

				return _elementsMap.get(itemId);
			}
			return null;
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 低效
	 * @param itemId
	 * @return
	 * @author CamusHuang
	 * @creation 2013-2-22 上午10:43:40
	 * </pre>
	 */
	long searchSlotIdByItemId(long itemId) {
		rwLock.lock();
		try {
			for (BodySlotData slot : ca.bodySlotMap.values()) {
				if (slot.equiMap.containsValue(itemId)) {
					return slot.slotId;
				}
			}
			return -1;
		} finally {
			rwLock.unlock();
		}
	}

	boolean recountEquiSetData() {
		rwLock.lock();
		try {
			boolean isSetChange = false;
			for (long slotId : ca.bodySlotMap.keySet()) {
				EquiSetResult setResult = recountEquiSetData(slotId);
				if (setResult.isSetChange) {
					isSetChange = true;
				}
			}
			return isSetChange;
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 重新计算装备的宝石、升星、强化套装数据
	 * 
	 * @param slotId
	 * @return 套装是否发生变化
	 * @author CamusHuang
	 * @creation 2014-9-2 下午5:31:55
	 * </pre>
	 */
	EquiSetResult recountEquiSetData(long slotId) {
		rwLock.lock();
		try {
			BodySlotData slot = ca.getOrNewBodySlotData(slotId);
			if (slot == null) {
				return new EquiSetResult();
			}
			{
				// 数据清0
				SetData setData = slot.getSetData();
				EquiSetStruct oldSet = setData.getEquiSets();
				setData.clear();

				{
					//<装备等级,<装备品质,装备数量>>
					Map<Integer,Map<Integer,AtomicInteger>> quaCountMap = new HashMap<Integer, Map<Integer,AtomicInteger>>();
					// 将宝石数量进行叠加用于计算套装光效
					Map<Integer, AtomicInteger> tempStoneCountMap = new HashMap<Integer, AtomicInteger>();
					{
						for(int key:setData.stoneSetMap.keySet()){
							tempStoneCountMap.put(key, new AtomicInteger());
						}
					}
					
					// 遍历装备
					List<KItem> items = searchSlotItemList(slotId);
					for (KItem item : items) {
						KItem_EquipmentData equiData = item.getEquipmentData();
						// 遍历装备上的宝石
						for (String itemCode : equiData.getEnchanseCache().values()) {
							KItemTempStone stoneTemp = (KItemTempStone) KItemDataManager.mItemTemplateManager.getItemTemplate(itemCode);
							AtomicInteger count = setData.stoneSetMap.get(stoneTemp.stonelvl);
							count.incrementAndGet();// 宝石等级数量++
							Map<KGameAttrType,AtomicInteger> map = setData.stoneSetAttMap.get(stoneTemp.stonelvl);
							KGameUtilTool.combinMap2(map, stoneTemp.allEffects);
							
							//
							// 将宝石数量进行叠加用于计算套装光效
							for (Entry<Integer, AtomicInteger> entry : tempStoneCountMap.entrySet()) {
								if (stoneTemp.stonelvl >= entry.getKey()) {
									entry.getValue().incrementAndGet();// 星级数量++
								}
							}
						}
						// 装备上的星级
						for (Entry<Integer, AtomicInteger> entry : setData.starSetMap.entrySet()) {
							if (equiData.getStarLv() >= entry.getKey()) {
								entry.getValue().incrementAndGet();// 星级数量++
							}
						}
						// 装备上的强化等级
						for (Entry<Integer, AtomicInteger> entry : setData.strongSetMap.entrySet()) {
							if (equiData.getStrongLv() >= entry.getKey()) {
								entry.getValue().incrementAndGet();// 强化等级++
							}
						}
						// 装备等级对应的品质及数量
						KItemTempAbs temp = item.getItemTemplate();
						Map<Integer,AtomicInteger> map = quaCountMap.get(temp.lvl);
						if(map==null){
							map = new HashMap<Integer, AtomicInteger>();
							quaCountMap.put(temp.lvl, map);
						}
						AtomicInteger count = map.get(temp.qua);
						if(count==null){
							count = new AtomicInteger();
							map.put(temp.qua, count);
						}
						count.incrementAndGet();
					}
					// 找出当前宝石套装生效等级
					{
						// 将宝石数量进行叠加用于计算套装光效
						for(KEquiStoneSetData2 temp:KItemDataManager.mEquiStoneSetDataManager2.getDataCache()){
							AtomicInteger count = tempStoneCountMap.get(temp.Suitcondition);
							if(count.get()>=temp.resStoneNum){
								if(temp.Suitcondition>setData.nowStoneSetLv){
									setData.nowStoneSetLv = temp.Suitcondition;
									setData.isNowStoneSetEffect = true;
								}
							}
						}
					}
					// 找出当前升星套装生效星阶和下一星阶
					{
						// 找出当前星阶
						for (Entry<Integer, AtomicInteger> entry : setData.starSetMap.entrySet()) {
							if (entry.getValue().get() >= KItemConfig.getInstance().TotalMaxEquiNum) {
								// 已激活，需要看下一星阶是否激活
								setData.nowStarSetLv = entry.getKey();
								setData.nowStarSetCount = entry.getValue().get();
								setData.isNowStarSetEffect = true;
							} else {
								// 未激活，结束
								if (setData.nowStarSetLv < 1) {
									setData.nowStarSetLv = entry.getKey();
									setData.nowStarSetCount = entry.getValue().get();
									setData.isNowStarSetEffect = false;
								}
								break;
							}
						}
						// 找出下一星阶
						int nextStarSetLv = setData.nowStarSetLv + KItemConfig.getInstance().EquiStarBigLv;
						AtomicInteger nextStarCount = setData.starSetMap.get(nextStarSetLv);
						if (nextStarCount != null) {
							setData.nextStarSetLv = nextStarSetLv;
							setData.nextStarSetCount = nextStarCount.get();
						}
					}
					// 找出当前强化套装生效星阶和下一套装
					{
						// 找出当前套装
						for (Entry<Integer, AtomicInteger> entry : setData.strongSetMap.entrySet()) {
							if (entry.getValue().get() >= KItemConfig.getInstance().TotalMaxEquiNum) {
								// 已激活，需要看下一套装是否激活
								setData.nowStrongSetLv = entry.getKey();
								setData.nowStrongSetCount = entry.getValue().get();
								setData.isNowStrongSetEffect = true;
							} else {
								// 未激活，结束
								if (setData.nowStrongSetLv < 1) {
									setData.nowStrongSetLv = entry.getKey();
									setData.nowStrongSetCount = entry.getValue().get();
									setData.isNowStrongSetEffect = false;
								}
								break;
							}
						}
						// 找出下一套装
						KEquiStrongSetData nowStrongSetData = KItemDataManager.mEquiStrongSetDataManager.getDataByLv(setData.nowStrongSetLv);
						KEquiStrongSetData nextStrongSetData = KItemDataManager.mEquiStrongSetDataManager.getDataById(nowStrongSetData.id + 1);
						if (nextStrongSetData != null) {
							AtomicInteger nextStrongCount = setData.strongSetMap.get(nextStrongSetData.Suitcondition);
							if (nextStrongCount != null) {
								setData.nextStrongSetLv = nextStrongSetData.Suitcondition;
								setData.nextStrongSetCount = nextStrongCount.get();
							}
						}
					}
					// 找出当前品质套装
					{
						//<装备等级,<装备品质,装备数量>>
						//Map<Integer,Map<Integer,AtomicInteger>> quaCountMap
						for(Entry<Integer,Map<Integer,AtomicInteger>> e:quaCountMap.entrySet()){
							for(Entry<Integer,AtomicInteger> ee:e.getValue().entrySet()){
								KEquiQualitySetData data = KItemDataManager.mEquiQualitySetDataManager.getData(e.getKey(), ee.getKey(), ee.getValue().get());
								if(data!=null){
									setData.quaSetIds.add(data.id);
								}
							}
						}
					}
				}

				EquiSetStruct newSet = setData.getEquiSets();
				return new EquiSetResult(oldSet, newSet, setData.getBroadstedEquiSets());
			}
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 
	 * 
	 * @param slotId
	 * @param setForBroadst 已经世界广播的等级{升星套装等级,宝石套装等级,强化套装等级}
	 * @author CamusHuang
	 * @creation 2014-10-23 下午5:02:56
	 * </pre>
	 */
	void resetBroadstSet(long slotId, int[] setForBroadst) {
		rwLock.lock();
		try {
			BodySlotData slot = ca.getOrNewBodySlotData(slotId);
			if (slot == null) {
				return;
			}
			slot.setData.maxStarSetLvForBrocast = setForBroadst[0];
			slot.setData.maxStoneSetLvForBrocast = setForBroadst[1];
			slot.setData.maxStrongSetLvForBrocast = setForBroadst[2];
			notifyDB();
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 装备套装属性加成 = 装备基础属性*(升星)套装额外增加比例 +(强化、品质)套装额外增加固定值
	 * 
	 * @param slotId
	 * @param baseResult 累加单件装备基础属性
	 * @param totalResult 结果存储
	 * @return totalResult
	 * @author CamusHuang
	 * @creation 2014-3-31 下午3:15:18
	 * </pre>
	 */
	Map<KGameAttrType, AtomicInteger> getAllSlotSetEffect(long slotId, Map<KGameAttrType, AtomicInteger> baseResult, Map<KGameAttrType, AtomicInteger> totalResult) {
		if (totalResult == null) {
			totalResult = new HashMap<KGameAttrType, AtomicInteger>();
		}
		if (baseResult == null) {
			baseResult = new HashMap<KGameAttrType, AtomicInteger>();
		}
		rwLock.lock();
		try {
			//
			BodySlotData slot = ca.bodySlotMap.get(slotId);
			if (slot != null && !slot.equiMap.isEmpty()) {
				SetData setData = slot.getSetData();
				if (setData.isNowStarSetEffect && setData.nowStarSetLv > 0) {
					// 升星套装
					KEquiStarSetData data = KItemDataManager.mEquiStarSetDataManager.getData(setData.nowStarSetLv);
					if (data != null) {
						// 升星套装额外增加固定值
						for (Entry<KGameAttrType, Integer> entry : data.allEffects.entrySet()) {
							AtomicInteger totalValue = totalResult.get(entry.getKey());
							if (totalValue == null) {
								totalValue = new AtomicInteger();
								totalResult.put(entry.getKey(), totalValue);
							}
							totalValue.addAndGet(entry.getValue());
						}

						if (data.percent > 0) {
							// 装备基础属性*升星套装额外增加比例
							for (Entry<KGameAttrType, AtomicInteger> entry : baseResult.entrySet()) {
								KGameAttrType baseAttType = entry.getKey();
								AtomicInteger baseValue = entry.getValue();

								AtomicInteger totalValue = totalResult.get(baseAttType);
								if (totalValue == null) {
									totalValue = new AtomicInteger();
									totalResult.put(baseAttType, totalValue);
								}
								totalValue.addAndGet((baseValue.get() * data.percent) / 10000);
							}
						}
					}
				}
//				if (setData.isNowStoneSetEffect && setData.nextStoneSetLv > 0) {
//					// 宝石套装
//					KEquiStoneSetData data = KItemDataManager.mEquiStoneSetDataManager.getData(setData.nextStoneSetLv);
//					if (data != null) {
//						for (Entry<KGameAttrType, Integer> entry : data.allEffects.entrySet()) {
//							AtomicInteger totalValue = totalResult.get(entry.getKey());
//							if (totalValue == null) {
//								totalValue = new AtomicInteger();
//								totalResult.put(entry.getKey(), totalValue);
//							}
//							totalValue.addAndGet(entry.getValue());
//						}
//					}
//				}
				if (setData.isNowStrongSetEffect && setData.nowStrongSetLv > 0) {
					// 强化套装
					KEquiStrongSetData data = KItemDataManager.mEquiStrongSetDataManager.getDataByLv(setData.nowStrongSetLv);
					if (data != null) {
						// 强化套装额外增加固定值
						for (Entry<KGameAttrType, Integer> entry : data.allEffects.entrySet()) {
							AtomicInteger totalValue = totalResult.get(entry.getKey());
							if (totalValue == null) {
								totalValue = new AtomicInteger();
								totalResult.put(entry.getKey(), totalValue);
							}
							totalValue.addAndGet(entry.getValue());
						}
					}
				}
				if (!setData.quaSetIds.isEmpty()) {
					// 品质套装
					for(int id:setData.quaSetIds){
						KEquiQualitySetData data = KItemDataManager.mEquiQualitySetDataManager.getDataById(id);
						if (data != null) {
							// 套装额外增加固定值
							for (Entry<KGameAttrType, Integer> entry : data.allEffects.entrySet()) {
								AtomicInteger totalValue = totalResult.get(entry.getKey());
								if (totalValue == null) {
									totalValue = new AtomicInteger();
									totalResult.put(entry.getKey(), totalValue);
								}
								totalValue.addAndGet(entry.getValue());
							}
						}
					}
				}
			}
		} finally {
			rwLock.unlock();
		}

		return totalResult;
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 直接获取缓存数据，谨慎使用
	 * @param slotId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-21 下午9:04:03
	 * </pre>
	 */
	BodySlotData getBodySlotData(long slotId) {
		return ca.getOrNewBodySlotData(slotId);
	}

	static class KItemPack_BodySlot_CA implements KItemPackCustomizeAttribute {

		final KItemPack_BodySlot owner;
		final ReentrantLock rwLock;

		/**
		 * <pre>
		 * 所有的装备栏
		 * KEY=装备栏ID号。其中主角色默认为1；副将由调用者设计。
		 * </pre>
		 */
		private final Map<Long, BodySlotData> bodySlotMap = new ConcurrentHashMap<Long, BodySlotData>();

		// //////////////////
		protected static final String JSON_PACK_NULL = "NULL";// null
		protected static final String JSON_PACK_VER = "0";// 版本
		//
		// 装备栏
		protected static final String JSON_PACK_BODYSLOT = "Z";// 装备栏数据
		protected static final String JSON_PACK_BODYSLOT_SLOTDATA = "1";// 各装备栏数据
		protected static final String JSON_PACK_BODYSLOT_SLOTDATA_CA = "A";// 装备栏内自定义数据（1~10用于装备部位道具ID记录）
		protected static final String JSON_PACK_BODYSLOT_SLOTDATA_CA_BROCAST_STARTLV = "1";// 已世界广播的最高星阶
		protected static final String JSON_PACK_BODYSLOT_SLOTDATA_CA_BROCAST_STONELV = "2";// 已世界广播的最高宝石等级
		protected static final String JSON_PACK_BODYSLOT_SLOTDATA_CA_BROCAST_STRONGLV = "3";// 已世界广播的最高强化等级

		KItemPack_BodySlot_CA(KItemPack_BodySlot owner, boolean isFirstNew) {
			this.owner = owner;
			this.rwLock = owner.rwLock;
			//
			this.getOrNewBodySlotData(KItemConfig.MAIN_BODYSLOT_ID);
		}

		@Override
		public void decodeAttribute(JSONObject obj) {
			if (obj == null) {
				return;
			}

			rwLock.lock();
			// 由底层调用,解释出逻辑层数据
			try {
				decodeSlotInfo(obj.getJSONObject(JSON_PACK_BODYSLOT));
				// 纠正位置映射与道具MAP的错误
				correctData();
			} catch (Exception ex) {
				_LOGGER.error("decode数据时发生错误 roleId=" + owner.getRoleId() + " packType=" + owner._itemPackType + " ----丢失数据！", ex);
			} finally {
				rwLock.unlock();
			}
		}

		private void decodeSlotInfo(JSONObject obj) throws JSONException {
			// 解释装备栏数据
			JSONObject allSlotDatas = obj.getJSONObject(JSON_PACK_BODYSLOT_SLOTDATA);
			for (Iterator<String> it = allSlotDatas.keys(); it.hasNext();) {
				String slotId = it.next();
				JSONObject slotData = allSlotDatas.getJSONObject(slotId);
				Map<KEquipmentTypeEnum, Long> itemMap = new HashMap<KEquipmentTypeEnum, Long>();
				for (KEquipmentTypeEnum type : KEquipmentTypeEnum.values()) {
					long itemId = slotData.optLong(type.sign + "");
					if (owner._elementsMap.containsKey(itemId)) {
						itemMap.put(type, itemId);
					}
				}
				
				BodySlotData data = getOrNewBodySlotData(Long.parseLong(slotId));
				data.equiMap.clear();
				data.equiMap.putAll(itemMap);
				{
					JSONObject caData = slotData.optJSONObject(JSON_PACK_BODYSLOT_SLOTDATA_CA);
					if (caData != null) {
						data.setData.maxStarSetLvForBrocast=caData.optInt(JSON_PACK_BODYSLOT_SLOTDATA_CA_BROCAST_STARTLV);
						data.setData.maxStoneSetLvForBrocast=caData.optInt(JSON_PACK_BODYSLOT_SLOTDATA_CA_BROCAST_STONELV);
						data.setData.maxStrongSetLvForBrocast=caData.optInt(JSON_PACK_BODYSLOT_SLOTDATA_CA_BROCAST_STRONGLV);
					}
				}
			}
			// CEND 道具--装备栏有没有其它数据需要解释？
		}

		@Override
		public JSONObject encodeAttribute() {
			rwLock.lock();
			try {
				JSONObject obj = new JSONObject();
				obj.put(JSON_PACK_BODYSLOT, encodeSlotInfo());
				return obj;
			} catch (Exception ex) {
				_LOGGER.error("encode数据时发生错误roleId=" + owner.getRoleId() + " packType=" + owner._itemPackType + " ----丢失数据！", ex);
				return null;
			} finally {
				rwLock.unlock();
			}
		}

		private JSONObject encodeSlotInfo() throws JSONException {
			JSONObject obj = new JSONObject();
			// 纠正位置映射与道具MAP的错误
			correctData();
			// 打包装备栏数据
			JSONObject allSlotDatas = new JSONObject();
			for (BodySlotData slot : bodySlotMap.values()) {
				JSONObject slotData = new JSONObject();
				for (Iterator<Entry<KEquipmentTypeEnum, Long>> it2 = slot.equiMap.entrySet().iterator(); it2.hasNext();) {
					Entry<KEquipmentTypeEnum, Long> element = it2.next();
					if (owner._elementsMap.containsKey(element.getValue())) {
						slotData.put(element.getKey().sign + "", element.getValue());
					} else {
						it2.remove();
					}
				}
				{
					JSONObject caData = new JSONObject();
					slotData.put(JSON_PACK_BODYSLOT_SLOTDATA_CA, caData);
					caData.put(JSON_PACK_BODYSLOT_SLOTDATA_CA_BROCAST_STARTLV, slot.setData.maxStarSetLvForBrocast);
					caData.put(JSON_PACK_BODYSLOT_SLOTDATA_CA_BROCAST_STONELV, slot.setData.maxStoneSetLvForBrocast);
					caData.put(JSON_PACK_BODYSLOT_SLOTDATA_CA_BROCAST_STRONGLV, slot.setData.maxStrongSetLvForBrocast);
				}
				
				allSlotDatas.put(slot.slotId + "", slotData);
			}
			obj.put(JSON_PACK_BODYSLOT_SLOTDATA, allSlotDatas);
			// CEND 道具--装备栏有没有其它数据需要打包？

			return obj;
		}

		/**
		 * <pre>
		 * 初始化一个装备栏，ID必须大于1
		 * 不保存数据
		 * 
		 * @param slotId
		 * @return
		 * @author CamusHuang
		 * @creation 2012-11-20 下午3:52:51
		 * </pre>
		 */
		private BodySlotData getOrNewBodySlotData(long slotId) {
			BodySlotData result = bodySlotMap.get(slotId);
			if (result != null) {
				return result;
			}

			result = new BodySlotData(this, slotId);
			bodySlotMap.put(slotId, result);
			return result;
		}

		/**
		 * <pre>
		 * 纠正位置映射与道具MAP的错误
		 * 
		 * @author CamusHuang
		 * @creation 2012-12-8 上午11:02:39
		 * </pre>
		 */
		private void correctData() {
			rwLock.lock();
			try {
				// 用于装载所有装备栏已有的装备类型，以便“纠正实际上未使用的装备”时可以提高效率
				// CTODO
			} finally {
				rwLock.unlock();
			}
		}

		static class BodySlotData {
			private final KItemPack_BodySlot_CA owner;
			/** 装备栏ID */
			private final long slotId;

			/** 所有本装备栏的道具ID */
			private final Map<KEquipmentTypeEnum, Long> equiMap = new HashMap<KEquipmentTypeEnum, Long>();

			private final SetData setData;

			private BodySlotData(KItemPack_BodySlot_CA owner, long slotId) {
				this.owner = owner;
				this.slotId = slotId;
				//
				setData = new SetData();
				// 初始化各级宝石的数量
				List<KGameAttrType> attList = KItemDataManager.mEquiStoneSetDataManager2.getAttListForAllStoneLv();
				for (KEquiStoneSetData2 data : KItemDataManager.mEquiStoneSetDataManager2.getDataCache()) {
					setData.stoneSetMap.put(data.Suitcondition, new AtomicInteger());
					
					Map<KGameAttrType,AtomicInteger> attsMap = new HashMap<KGameAttrType,AtomicInteger>();
					setData.stoneSetAttMap.put(data.Suitcondition, attsMap);
					for(KGameAttrType att:attList){
						attsMap.put(att, new AtomicInteger());
					}
				}
				// 初始化各级星阶的数量
				for (KEquiStarSetData data : KItemDataManager.mEquiStarSetDataManager.getDataCache()) {
					setData.starSetMap.put(data.Suitcondition, new AtomicInteger());
				}
				// 初始化各级强化套装的数量
				for (KEquiStrongSetData data : KItemDataManager.mEquiStrongSetDataManager.getDataCache()) {
					setData.strongSetMap.put(data.Suitcondition, new AtomicInteger());
				}
			}

			/**
			 * <pre>
			 * 
			 * @deprecated 直接获取缓存数据，谨慎使用
			 * @param isInit
			 * @return
			 * @author CamusHuang
			 * @creation 2014-3-22 下午1:45:36
			 * </pre>
			 */
			SetData getSetData() {
				return setData;
			}

			static class SetData {
				/**
				 * <pre>
				 * 宝石套装数据(无须保存，动态刷新)
				 * <宝石等级,等于此等级的宝石数量>
				 * </pre>
				 */
				final Map<Integer, AtomicInteger> stoneSetMap = new LinkedHashMap<Integer, AtomicInteger>();
				//<等级,<属性类型,属性值>>
				final Map<Integer,Map<KGameAttrType,AtomicInteger>> stoneSetAttMap = new HashMap<Integer, Map<KGameAttrType,AtomicInteger>>();
				int nowStoneSetLv;// 当前宝石等级
				int maxStoneSetLvForBrocast;// 已世界广播的最高宝石等级
				boolean isNowStoneSetEffect;
//				int nowStoneSetCount;// 当前宝石等级数量
//				int nextStoneSetLv;// 下一宝石等级(0表示不存在下一等级)
//				int nextStoneSetCount;// 下一宝石等级数量
				
				/**
				 * <pre>
				 * 升星套装数据(无须保存，动态刷新)
				 * <星阶(星数),大于等于此星阶(星数)的装备数量>
				 * </pre>
				 */
				final Map<Integer, AtomicInteger> starSetMap = new LinkedHashMap<Integer, AtomicInteger>();
				int nowStarSetLv;// 当前星阶(星数)
				int maxStarSetLvForBrocast;// 已世界广播的最高星阶
				int nowStarSetCount;// 当前升星等级数量
				boolean isNowStarSetEffect;
				int nextStarSetLv;// 下一星阶(星数，0表示不存在下一星阶)
				int nextStarSetCount;// 下一星阶等级数量
				// /** 套装角色发光资源ID */
				// private Map<Byte, Map<Byte, Integer>> lightResIdForSet;

				/**
				 * <pre>
				 * 强化套装数据(无须保存，动态刷新)
				 * <强化等级,大于等于此强化等级的装备数量>
				 * </pre>
				 */
				final Map<Integer, AtomicInteger> strongSetMap = new LinkedHashMap<Integer, AtomicInteger>();
				int nowStrongSetLv;// 当前强化等级
				int maxStrongSetLvForBrocast;// 已世界广播的最高强化等级
				int nowStrongSetCount;// 当前强化等级数量
				boolean isNowStrongSetEffect;
				int nextStrongSetLv;// 下一强化等级(0表示不存在下一星阶)
				int nextStrongSetCount;// 下一强化等级数量
				
				/**
				 * <pre>
				 * 品质套装数据(无须保存，动态刷新)
				 * </pre>
				 */
				Set<Integer> quaSetIds=new HashSet<Integer>();

				private void clear() {
					for (AtomicInteger count : stoneSetMap.values()) {
						count.set(0);
					}
					for (Map<KGameAttrType,AtomicInteger> map : stoneSetAttMap.values()) {
						for (AtomicInteger count : map.values()) {
							count.set(0);
						}
					}
					isNowStoneSetEffect = false;
					nowStoneSetLv = -1;
//					nowStoneSetCount = 0;
//					nextStoneSetLv = -1;
//					nextStoneSetCount = 0;
					// ////////////////////
					for (Entry<Integer, AtomicInteger> entry : starSetMap.entrySet()) {
						entry.getValue().set(0);
					}
					isNowStarSetEffect = false;
					nowStarSetLv = -1;
					nowStarSetCount = 0;
					nextStarSetLv = -1;
					nextStarSetCount = 0;
					// ////////////////////
					for (Entry<Integer, AtomicInteger> entry : strongSetMap.entrySet()) {
						entry.getValue().set(0);
					}
					isNowStrongSetEffect = false;
					nowStrongSetLv = -1;
					nowStrongSetCount = 0;
					nextStrongSetLv = -1;
					nextStrongSetCount = 0;
					// ///////////////////
					quaSetIds.clear();
				}

				/**
				 * <pre>
				 * 
				 * 
				 * @return []{升星套装地图资源ID,宝石套装地图资源ID}
				 * @author CamusHuang
				 * @creation 2014-9-2 下午5:28:43
				 * </pre>
				 */
				int[] getEquiSetMapResIds() {
					int[] result = new int[2];

					{
						if (isNowStarSetEffect) {
							KEquiStarSetData data = KItemDataManager.mEquiStarSetDataManager.getData(nowStarSetLv);
							if (data != null) {
								result[0] = data.mapResId;
							}
						}
					}
					{
						if (isNowStoneSetEffect) {
							KEquiStoneSetData2 data = KItemDataManager.mEquiStoneSetDataManager2.getData(nowStoneSetLv);
							if (data != null) {
								result[1] = data.mapResId;
							}
						}
					}
					return result;
				}

				/**
				 * <pre>
				 * 
				 * 
				 * @return []{升星套装等级,宝石套装等级,强化套装等级}
				 * @author CamusHuang
				 * @creation 2014-9-2 下午5:28:34
				 * </pre>
				 */
				EquiSetStruct getEquiSets() {
					EquiSetStruct setStruct = new EquiSetStruct();

					{
						if (isNowStarSetEffect) {
							KEquiStarSetData data = KItemDataManager.mEquiStarSetDataManager.getData(nowStarSetLv);
							if (data != null) {
								setStruct.starSetLv = data.Suitcondition/KItemConfig.getInstance().EquiStarBigLv;
							}
						}
					}
					{
						if (isNowStoneSetEffect) {
							KEquiStoneSetData2 data = KItemDataManager.mEquiStoneSetDataManager2.getData(nowStoneSetLv);
							if (data != null) {
								setStruct.stoneSetLv = data.Suitcondition;
							}
						}
					}
					{
						if (isNowStrongSetEffect) {
							KEquiStrongSetData data = KItemDataManager.mEquiStrongSetDataManager.getDataByLv(nowStrongSetLv);
							if (data != null) {
								setStruct.strongSetLv = data.Suitcondition;
							}
						}
					}
					{
						if(quaSetIds.isEmpty()){
							setStruct.quaSetIds=Collections.emptySet();
						} else {
							setStruct.quaSetIds =Collections.unmodifiableSet(quaSetIds);
						}
					}
					
					return setStruct;
				}

				/**
				 * <pre>
				 * 
				 * 
				 * @return []{升星套装等级,宝石套装等级,强化套装等级}
				 * @author CamusHuang
				 * @creation 2014-9-2 下午5:28:34
				 * </pre>
				 */
				int[] getBroadstedEquiSets() {
					return new int[] { maxStarSetLvForBrocast, maxStoneSetLvForBrocast, maxStrongSetLvForBrocast };
				}
			}
		}
	}
}
