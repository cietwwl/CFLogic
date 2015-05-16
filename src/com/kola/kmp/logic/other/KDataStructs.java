package com.kola.kmp.logic.other;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.koala.game.exception.KGameServerException;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.item.KItem;
import com.kola.kmp.logic.item.KItemDataManager;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * <pre>
 * 本类负责定义本模块的数据结构
 * 本类纯粹定义数据结构,而不管理数据
 * 
 * 定义的数据结构一般是比较小的，用于各个模块传输的小结构
 * 
 * @author CamusHuang
 * @creation 2012-11-6 上午11:09:57
 * </pre>
 */
public class KDataStructs {

	// private static Logger _LOGGER =
	// KItemModuleManager.getLogger(KItemDataStruct.class);
	//
	// /**
	// * <pre>
	// * 装备的基础属性及增量
	// *
	// * @author CamusHuang
	// * @creation 2012-11-6 上午10:58:42
	// *
	// * </pre>
	// */
	// public static class EquiBaseAttData {
	// /** 装备的基础属性及基础增量 */
	// public final RoleAttAndValueStruct mEquiBaseAttAndBaseValue;
	// /** 基础属性顺序编号(表格中的顺序) */
	// public final long baseAttIndex;
	// /** 强化值----由强化等级根据强化功能的数据得到此值 */
	// private long strongValue;
	//
	// EquiBaseAttData(RoleAttAndValueStruct mEquiBaseAttAndBaseValue, long
	// baseAttIndex) {
	// this.mEquiBaseAttAndBaseValue = mEquiBaseAttAndBaseValue;
	// this.baseAttIndex = baseAttIndex;
	// }
	//
	// void resetStrongValue(long strongValue) {
	// this.strongValue = strongValue;
	// }
	//
	// public long getTotalValue() {
	// return mEquiBaseAttAndBaseValue.addValue + strongValue;
	// }
	// }
	//
	// /**
	// * <pre>
	// * 技能及其几率
	// * 此几率属于范围内竞争几率
	// * @author CamusHuang
	// * @creation 2012-11-8 下午3:51:26
	// * </pre>
	// */
	// static class EquiSkillAndRateStruct {
	// /** 技能模板ID */
	// private final long skillTemplateId;
	// private KGameSkillTemplate skillTemplate;
	// /** 此几率属于范围内竞争几率 0~100 */
	// final long rate;
	//
	// EquiSkillAndRateStruct(long skillTemplateId, long rate) {
	// this.skillTemplateId = skillTemplateId;
	// this.rate = rate;
	// }
	//
	// KGameSkillTemplate getSkillTemplate() {
	// return skillTemplate;
	// }
	//
	// void serverStartCompleted() throws KGameServerException {
	// skillTemplate =
	// KSupportFactory.getSkillSupport().getSkillTemplate(skillTemplateId);
	// if (skillTemplate == null) {
	// throw new KGameServerException("加载道具表数据错误：未经定义的技能模板ID =" +
	// skillTemplateId);
	// }
	// }
	// }

	/**
	 * <pre>
	 * 套装组
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-6 下午6:13:46
	 * </pre>
	 */
	public static class EquiSetDataStruct {
		/** 套装组ID */
		public final long equiSetId;
		/** 套装组名称 */
		public final String equiSetName;
		/** 装备ICON发光资源ID */
		public final long lightResIdForIcon;
		/** 套装角色发光资源ID <性别,<职业,资源ID>> */
		public final Map<Byte, Map<Byte, Integer>> lightResIdForSet;
		/** 本套装组包含的所有属性 */
		public final List<EquiSetAttStruct> mEquiSetAttList;

		public EquiSetDataStruct(long equiSetId, String equiSetName, long lightResIdForIcon, Map<Byte, Map<Byte, Integer>> lightResIdForSet, List<EquiSetAttStruct> attsList) {
			this.equiSetId = equiSetId;
			this.equiSetName = equiSetName;
			this.lightResIdForIcon = lightResIdForIcon;
			this.lightResIdForSet = lightResIdForSet;
			if (attsList.isEmpty()) {
				this.mEquiSetAttList = Collections.emptyList();
			} else {
				this.mEquiSetAttList = Collections.unmodifiableList(attsList);
			}
		}

		/**
		 * <pre>
		 * 套装组属性
		 * 
		 * @author CamusHuang
		 * @creation 2012-11-6 下午6:14:06
		 * </pre>
		 */
		public static class EquiSetAttStruct {
			// 激活本属性的最小套装内装备数量
			public final long limit_minEquiCount;
			public final AttValueStruct roleAttAndAddValue;

			EquiSetAttStruct(long limit_minEquiCount, AttValueStruct roleAttAndAddValue) {
				this.limit_minEquiCount = limit_minEquiCount;
				this.roleAttAndAddValue = roleAttAndAddValue;
			}
		}
	}

	//
	// // //////////////////////////////////
	//
	// // /**
	// // * 关于装备品质的定义
	// // *
	// // * <pre>
	// // *
	// // *
	// // * @author CamusHuang
	// // * @creation 2012-11-6 下午7:30:36
	// // * </pre>
	// // */
	// // static class EquiQualityStruct {
	// // /** 标识值 */
	// // final String sign;
	// //
	// // EquiQualityStruct(String sign) {
	// // this.sign = sign;
	// // }
	// // }

	/**
	 * <pre>
	 * 道具及数量
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-5 上午11:00:45
	 * </pre>
	 */
	public static class ItemCountStruct2 {
		/** 道具 */
		public final KItem item;
		/** 道具的数量 */
		public final long itemCount;

		public ItemCountStruct2(KItem item, long itemCount) {
			this.item = item;
			this.itemCount = itemCount;
		}
	}

	//
	// // /**
	// // * <pre>
	// // * 道具及数量
	// // *
	// // * @author CamusHuang
	// // * @creation 2012-12-12 下午12:07:07
	// // * </pre>
	// // */
	// // public static class ItemCountStruct3 {
	// // /** 道具模板 */
	// // public final KItemTemplate itemTemplate;
	// // /** 道具的数量 */
	// // public final long itemCount;
	// //
	// // public ItemCountStruct3(KItemTemplate itemTemplate, long itemCount) {
	// // this.itemTemplate = itemTemplate;
	// // this.itemCount = itemCount;
	// // }
	// // }

	/**
	 * <pre>
	 * 道具及数量
	 * 
	 * @author CamusHuang
	 * @creation 2012-12-12 下午12:07:07
	 * </pre>
	 */
	public static class ItemCountStruct {
		/** 道具模板 */
		private KItemTempAbs itemTemplate;
		/** 道具模板编码 */
		public final String itemCode;
		/** 道具的数量 */
		public final long itemCount;

		public ItemCountStruct(String itemCode, long itemCount) {
			this.itemCode = itemCode;
			this.itemCount = itemCount;
		}

		public ItemCountStruct(KItemTempAbs itemTemplate, long itemCount) {
			this(itemTemplate.itemCode, itemCount);
			this.itemTemplate = itemTemplate;
		}

		public KItemTempAbs getItemTemplate() {
			if (itemTemplate == null) {
				itemTemplate = KItemDataManager.mItemTemplateManager.getItemTemplate(itemCode);
			}
			return itemTemplate;
		}

		public static List<ItemCountStruct> changeItemStruct(Collection<String> itemCodes) {
			List<ItemCountStruct> result = new ArrayList<KDataStructs.ItemCountStruct>();
			for (String itemCode : itemCodes) {
				result.add(new ItemCountStruct(itemCode, 1));
			}
			return result;
		}

		public static Map<String, ItemCountStruct> changeItemStruct(List<ItemCountStruct> itemStructs) {
			if (itemStructs == null || itemStructs.isEmpty()) {
				return Collections.emptyMap();
			}
			Map<String, ItemCountStruct> result = new HashMap<String, ItemCountStruct>();
			for (ItemCountStruct struct : itemStructs) {
				result.put(struct.itemCode, struct);
			}
			return result;
		}

		/**
		 * <pre>
		 * 将结构数组中类型重复ItemCode的数量进行合并
		 * 
		 * @param structs
		 * @return
		 * @author CamusHuang
		 * @creation 2012-12-10 下午7:48:49
		 * </pre>
		 */
		public static List<ItemCountStruct> mergeItemCountStructs(List<ItemCountStruct> structs) {
			if (structs == null || structs.isEmpty()) {
				return Collections.emptyList();
			}
			//
			Map<String, Long> map = new HashMap<String, Long>();
			for (ItemCountStruct struct : structs) {
				if (struct == null || struct.itemCount < 1) {
					continue;
				}

				Long old = map.get(struct.itemCode);
				if (old == null) {
					map.put(struct.itemCode, struct.itemCount);
				} else {
					map.put(struct.itemCode, struct.itemCount + old);
				}
			}
			//
			if (map.size() == structs.size()) {
				return structs;
			}
			//
			if (map.isEmpty()) {
				return Collections.emptyList();
			}
			//
			List<ItemCountStruct> list = new ArrayList<ItemCountStruct>();
			for (String type : map.keySet()) {
				KItemTempAbs temp = KSupportFactory.getItemModuleSupport().getItemTemplate(type);
				list.add(new ItemCountStruct(temp, map.get(type)));
			}
			return list;
		}

		/**
		 * <pre>
		 * 从指定道具中，按权重随机选择一个道具数据返回
		 * 
		 * @param addItems 备选道具
		 * @param addItemRates 备选道具的权重
		 * @param allRate 总权重
		 * @return
		 * @author CamusHuang
		 * @creation 2014-4-1 上午9:43:35
		 * </pre>
		 */
		public static ItemCountStruct randomItem(List<ItemCountStruct> addItems, List<Integer> addItemRates, int allRate) {
			if (addItems.isEmpty()) {
				return null;
			}
			int rate = UtilTool.random(1, allRate);
			for (int index = 0; index < addItemRates.size(); index++) {
				int tempRate = addItemRates.get(index);
				rate -= tempRate;
				if (rate < 1) {
					ItemCountStruct temp = addItems.get(index);
					if (temp.getItemTemplate() == null || temp.itemCount < 1) {
						return null;// 随机到0物品
					}
					return temp;
				}
			}
			return null;
		}

		/**
		 * <pre>
		 * 解释道具参数
		 * 物品id*物品数量*权重
		 * 
		 * @param opentiem
		 * @param addItems
		 * @param addItemRates
		 * @param totalRate
		 * @param minCount 物品数量以及权重允许的最小值
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2014-4-28 下午12:24:56
		 * </pre>
		 */
		public static int paramsItems(String[] opentiem, List<ItemCountStruct> addItems, List<Integer> addItemRates, int minCount) throws KGameServerException {
			int totalRate = 0;
			if (opentiem != null) {
				for (String temp : opentiem) {
					String[] temps = temp.split("\\*");
					KItemTempAbs itemTemp = KItemDataManager.mItemTemplateManager.getItemTemplate(temps[0]);
					if (itemTemp == null) {
						throw new KGameServerException("道具模板不存在=" + temps[0]);
					}
					ItemCountStruct tempS = new ItemCountStruct(itemTemp, Long.parseLong(temps[1]));
					if (tempS.itemCount < minCount) {
						throw new KGameServerException("道具数量错误=" + temps[1]);
					}
					int rate = Integer.parseInt(temps[2]);
					if (rate < minCount) {
						throw new KGameServerException("道具权重错误=" + rate);
					}
					addItems.add(tempS);
					addItemRates.add(rate);
					totalRate += rate;
				}
			}
			return totalRate;
		}

		/**
		 * <pre>
		 * 解释道具参数
		 * 物品id*物品数量
		 * 
		 * @param opentiem
		 * @param addItems
		 * @param minCount 物品数量
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2014-4-28 下午12:24:56
		 * </pre>
		 */
		public static void paramsItems(String[] opentiem, List<ItemCountStruct> addItems, int minCount) throws KGameServerException {
			if (opentiem != null) {
				for (String temp : opentiem) {
					String[] temps = temp.split("\\*");
					KItemTempAbs itemTemp = KItemDataManager.mItemTemplateManager.getItemTemplate(temps[0]);
					if (itemTemp == null) {
						throw new KGameServerException("道具模板不存在=" + temps[0]);
					}
					ItemCountStruct tempS = new ItemCountStruct(itemTemp, Long.parseLong(temps[1]));
					if (tempS.itemCount < minCount) {
						throw new KGameServerException("道具数量错误=" + temps[1]);
					}
					addItems.add(tempS);
				}
			}
		}
		
		
		public static List<ItemCountStruct> copyForRate(List<ItemCountStruct> items, int rate){
			List<ItemCountStruct> newItems = items.isEmpty() ? null : new ArrayList<ItemCountStruct>();
			for (ItemCountStruct temp : items) {
				newItems.add(new ItemCountStruct(temp.itemCode, temp.itemCount * rate));
			}
			return newItems;
		}
	}

	//
	// /**
	// * <pre>
	// * 道具及数量
	// *
	// * @author CamusHuang
	// * @creation 2013-3-20 上午11:29:31
	// * </pre>
	// */
	// public static class ItemCountStruct4 extends ItemCountStruct3 {
	// /** 商品 */
	// private Goods goods;
	//
	// ItemCountStruct4(KItemTemplate temp, long itemCount) {
	// super(temp, itemCount);
	// }
	//
	// public Goods getGoods() {
	// return goods;
	// }
	//
	// void setGoods(Goods goods) {
	// this.goods = goods;
	// }
	// }
	//
	// /**
	// * <pre>
	// * 道具及数量
	// *
	// * @author CamusHuang
	// * @creation 2012-11-5 上午11:00:45
	// * </pre>
	// */
	// public static class ItemCountStruct5 {
	// /** 道具DB ID */
	// public final long itemId;
	// /** 道具的数量 */
	// public final long itemCount;
	//
	// public ItemCountStruct5(long itemId, long itemCount) {
	// this.itemId = itemId;
	// this.itemCount = itemCount;
	// }
	// }
	//
	// ///////////////////////////////
	/**
	 * <pre>
	 * 道具出售的相关信息
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-5 下午6:28:53
	 * </pre>
	 */
	public static class ItemSellStruct {
		/** 是否可出售 */
		public final boolean isCanBeSell;
		/** 出售的货币类型及数量 */
		public final KCurrencyCountStruct sellPrice;

		ItemSellStruct(boolean isCanBeSell, KCurrencyCountStruct sellPrice) {
			this.isCanBeSell = isCanBeSell;
			this.sellPrice = sellPrice;
		}

	}

	//
	// /**
	// * <pre>
	// * 货币及其数量
	// *
	// * @deprecated 请尽量使用{@link #CurrencyCountStruct2}
	// * @author CamusHuang
	// * @creation 2012-11-9 上午10:57:34
	// * </pre>
	// */
	// public static class CurrencyCountStruct {
	// /** 货币类型 */
	// public final KGameCurrencyType currencyType;
	// // /** 货币数量 正数表示增加货币，负数表示减少货币 */
	// // public final long currencyCount;
	// /** 货币数量 用于显示 */
	// public final long currencyCountForShow;
	//
	// public CurrencyCountStruct(KGameCurrencyType currencyType, long
	// currencyCount) {
	// this.currencyType = currencyType;
	// // this.currencyCount = currencyCount;
	// this.currencyCountForShow = Math.abs(currencyCount);
	// }
	// }
	//
	// /**
	// * <pre>
	// * 货币及其数量
	// *
	// * @author CamusHuang
	// * @creation 2012-11-9 上午10:57:34
	// * </pre>
	// */
	// public static class CurrencyCountStruct2 {
	// /** 货币类型 */
	// public final KGameCurrencyType currencyType;
	// /** 货币数量 用于显示 */
	// public final long currencyCountForShow;
	//
	// public CurrencyCountStruct2(KGameCurrencyType currencyType, long
	// currencyCount) {
	// this.currencyType = currencyType;
	// this.currencyCountForShow = Math.abs(currencyCount);
	// }
	// }
	//
	// /////////////////////////////
	/**
	 * <pre>
	 * 定义了角色的属性类型,以及增量
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-6 上午11:28:45
	 * </pre>
	 */
	public static class AttValueStruct {
		/** 角色属性类型 */
		public KGameAttrType roleAttType;
		/** 效果值(由属性类型决定此值是整形值或是百分比、万分比) */
		public int addValue;
		/** 效果持续时间(毫秒) 0表示无时间限制 */
		public long usetime;

		public AttValueStruct(KGameAttrType roleAttType, int addValue) {
			this(roleAttType, addValue, 0);
		}

		public AttValueStruct(KGameAttrType roleAttType, int addValue, long usetime) {
			this.roleAttType = roleAttType;
			this.addValue = addValue;
			this.usetime = usetime;
		}
		
		/**
		 * <pre>
		 * 将结构数组中类型重复的进行合并
		 * 
		 * @param structs
		 * @return
		 * @author CamusHuang
		 * @creation 2012-12-10 下午7:48:49
		 * </pre>
		 */
		public static List<AttValueStruct> mergeCountStructs(List<AttValueStruct> structs) {
			if (structs == null || structs.size() < 1) {
				return Collections.emptyList();
			}
			//
			Map<KGameAttrType, Integer> map = new HashMap<KGameAttrType, Integer>();
			for (AttValueStruct struct : structs) {
				if (struct == null || struct.addValue < 1) {
					continue;
				}

				Integer old = map.get(struct.roleAttType);
				if (old == null) {
					map.put(struct.roleAttType, struct.addValue);
				} else {
					map.put(struct.roleAttType, struct.addValue + old);
				}
			}
			//
			if (map.size() == structs.size()) {
				return structs;
			}
			//
			if (map.isEmpty()) {
				return Collections.emptyList();
			}
			//
			List<AttValueStruct> list = new ArrayList<AttValueStruct>();
			for (KGameAttrType type : map.keySet()) {
				list.add(new AttValueStruct(type, map.get(type)));
			}
			return list;
		}		
	}
	// /////////////////////////////
	/**
	 * <pre>
	 * 定义了角色的套装数据
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-6 上午11:28:45
	 * </pre>
	 */
	public static class EquiSetStruct {
		public int starSetLv;//升星套装等级(星阶)
		public int stoneSetLv;//宝石套装等级
		public int strongSetLv;//强化套装等级
		public Set<Integer> quaSetIds;//品质套装ID
	}	
	//
	// // /////////////////////////////
	// /**
	// * <pre>
	// * 定义了角色的属性类型,以及取值范围
	// *
	// * @author CamusHuang
	// * @creation 2012-11-8 下午5:54:50
	// * </pre>
	// */
	// static class RoleAttAndValueRangStruct {
	// /** 角色属性类型 */
	// final long roleAttType;
	// /** 最小值 */
	// final long minValue;
	// /** 最大值 */
	// final long maxValue;
	//
	// RoleAttAndValueRangStruct(long roleAttType, long minValue, long maxValue)
	// {
	// this.roleAttType = roleAttType;
	// this.minValue = minValue;
	// this.maxValue = maxValue;
	// }
	// }
	//
	// // ////////////////////////////////
	// /**
	// * <pre>
	// * 某一条铸魂规则
	// *
	// * @author CamusHuang
	// * @creation 2012-11-8 下午5:59:41
	// * </pre>
	// */
	// public static class SoulChastenRuleStruct {
	// final byte jobType;// -1表示不限职业
	// final KGameItemQualityEnum quality;
	// /** 单条属性消耗货币 */
	// final CurrencyCountStruct consumeMoney;
	// /** 单个属性锁魂消耗货币 */
	// final CurrencyCountStruct consumeMoneyForLock;
	// /** 锁魂最大条数 */
	// final long maxLockAttCount;
	// /**
	// * <pre>
	// * 所有可选属性的取值范围
	// * 铸魂表格中，某个属性如果最大最小值均是0，则表示此属性不用被加载
	// * unmodifiable
	// * </pre>
	// */
	// final List<RoleAttAndValueRangStruct> attValueRangeList;
	//
	// /**
	// * <pre>
	// * 所有可选属性的取值范围
	// * 铸魂表格中，某个属性如果最大最小值均是0，则表示此属性不用被加载
	// * KEY=属性类型
	// * unmodifiable
	// * </pre>
	// */
	// final Map<Integer, RoleAttAndValueRangStruct> attValueRangeMap;
	//
	// SoulChastenRuleStruct(byte jobType, KGameItemQualityEnum quality,
	// CurrencyCountStruct consumeMoney, CurrencyCountStruct
	// consumeMoneyForLock, long maxLockAttCount,
	// List<RoleAttAndValueRangStruct> attValueRangeList) {
	//
	// this.jobType = jobType;
	// this.quality = quality;
	// this.consumeMoney = consumeMoney;
	// this.consumeMoneyForLock = consumeMoneyForLock;
	// this.maxLockAttCount = maxLockAttCount;
	// this.attValueRangeList = Collections.unmodifiableList(attValueRangeList);
	// Map<Integer, RoleAttAndValueRangStruct> tempMap = new HashMap<Integer,
	// KItemDataStruct.RoleAttAndValueRangStruct>();
	// for (RoleAttAndValueRangStruct struct : attValueRangeList) {
	// tempMap.put(struct.roleAttType, struct);
	// }
	// attValueRangeMap = Collections.unmodifiableMap(tempMap);
	// }
	// }
	//
	// // ////////////////////////////////
	// /**
	// * <pre>
	// * 宝石合成数据 按类型分
	// *
	// * @author CamusHuang
	// * @creation 2013-4-11 下午4:44:16
	// * </pre>
	// */
	// public static class StoneComposeStruct_Type {
	//
	// final long stoneType;
	// /**
	// * <pre>
	// * KEY = 宝石等级
	// * </pre>
	// */
	// private Map<Integer, StoneComposeStruct_Lv> composeDataMap = new
	// HashMap<Integer, StoneComposeStruct_Lv>();
	//
	// StoneComposeStruct_Type(long stoneType) {
	// this.stoneType = stoneType;
	// }
	//
	// /**
	// * <pre>
	// * 数据添加
	// * 如果存在类型重复，则抛异常
	// *
	// * @param data
	// * @throws KGameServerException
	// * @author CamusHuang
	// * @creation 2013-4-11 下午4:40:43
	// * </pre>
	// */
	// void addData(StoneComposeStruct_Lv data) throws KGameServerException {
	// // 放入MAP
	// StoneComposeStruct_Lv oldData = composeDataMap.put(data.stoneLv, data);
	// if (oldData != null) {
	// throw new KGameServerException("加载宝石合成数据错误：重复的宝石等级 =" + data.stoneLv +
	// ",当前类型=" + stoneType);
	// }
	// }
	//
	// StoneComposeStruct_Lv getData(long stoneLv) {
	// return composeDataMap.get(stoneLv);
	// }
	//
	// void serverStartCompleted() throws KGameServerException {
	// for (StoneComposeStruct_Lv data : composeDataMap.values()) {
	// data.serverStartCompleted();
	// }
	// }
	//
	// // ////////////////////////////////
	// /**
	// * <pre>
	// * 某类型的宝石合成数据 按等级分
	// *
	// * @author CamusHuang
	// * @creation 2013-4-11 下午4:44:16
	// * </pre>
	// */
	// public static class StoneComposeStruct_Lv {
	// /** 宝石种类 */
	// final long stoneType;
	// /** 宝石等级 */
	// final long stoneLv;
	// /** 基础成功率 */
	// final byte baseRate;
	// /** 幸运石Item */
	// final ItemCountStruct3 luckItem;
	// final byte luckRate;// 幸运石成功率
	// final byte failLuck;// 失败祝福值
	//
	// StoneComposeStruct_Lv(long stoneType, long stoneLv, byte baseRate,
	// ItemCountStruct3 luckItem, byte luckRate, byte failLuck) {
	// this.stoneType = stoneType;
	// this.stoneLv = stoneLv;
	// this.baseRate = baseRate;
	// this.luckItem = luckItem;
	// this.luckRate = luckRate;
	// this.failLuck = failLuck;
	// }
	//
	// void serverStartCompleted() throws KGameServerException {
	// KItemTemplate temp =
	// KItemDataManager.mItemTemplateManager.getItemTemplate(luckItem.itemCode);
	// if (temp == null) {
	// throw new KGameServerException("宝石合成幸运石不存在 itemCode=" +
	// luckItem.itemCode);
	// }
	//
	// luckItem.setItemTemplate(temp);
	//
	// if (KSupportFactory.getShopSupport().getGoodsFromMall(luckItem.itemCode)
	// == null) {
	// throw new KGameServerException("宝石合成幸运石不在商城中 itemCode=" +
	// luckItem.itemCode);
	// }
	// }
	// }
	//
	// }
	//
	// // ////////////////////////////////
	// /**
	// * <pre>
	// * 道具合成数据
	// *
	// * @author CamusHuang
	// * @creation 2012-12-11 下午5:17:03
	// * </pre>
	// */
	// public static class ItemComposeStruct {
	// /** 合成规则ID */
	// public final long composeRuleId;
	// /** 标签 */
	// public final String label;
	// /** 合成得到的新道具 */
	// public final KItemTemplate newItemTemplate;
	// /** 要消耗的材料 */
	// public final List<ItemCountStruct3> consumeMaterials;
	// /** 要消耗的货币 */
	// public final CurrencyCountStruct consumeMoney;
	//
	// ItemComposeStruct(long composeRuleId, String label, KItemTemplate
	// newItemTemplate, List<ItemCountStruct3> consumeMaterials,
	// CurrencyCountStruct consumeMoney) {
	// this.composeRuleId = composeRuleId;
	// this.label = label;
	// this.newItemTemplate = newItemTemplate;
	// this.consumeMaterials = consumeMaterials;
	// this.consumeMoney = consumeMoney;
	// }
	// }
	//
	// // ////////////////////////////////
	// /**
	// * <pre>
	// * 装备升级规则数据
	// *
	// * @author CamusHuang
	// * @creation 2012-12-11 下午5:17:03
	// * </pre>
	// */
	// public static class EquipUplevelRuleStruct {
	// /** 旧装备 */
	// public final KItemTemplate oldEquiItemTemplate;
	// /** 新装备 */
	// public final KItemTemplate newEquiItemTemplate;
	// /** 要消耗的材料 */
	// public final List<ItemCountStruct3> consumeMaterials;
	// /**
	// * <pre>
	// * 要消耗的货币
	// * </pre>
	// */
	// final CurrencyCountStruct currencyCount;
	//
	// /** 基础成功率 */
	// final byte baseRate;
	//
	// EquipUplevelRuleStruct(KItemTemplate oldEquiItemTemplate, KItemTemplate
	// newEquiItemTemplate, byte baseRate, List<ItemCountStruct3>
	// consumeMaterials, CurrencyCountStruct currencyCount) {
	// this.oldEquiItemTemplate = oldEquiItemTemplate;
	// this.newEquiItemTemplate = newEquiItemTemplate;
	// this.baseRate = baseRate;
	// this.consumeMaterials = consumeMaterials;
	// this.currencyCount = currencyCount;
	// }
	// }
	//
	// // ///////////////////////////////////////////
	//
	// /**
	// * <pre>
	// * 对宝箱开启规则的定义
	// *
	// * @author CamusHuang
	// * @creation 2012-11-9 上午11:02:11
	// * </pre>
	// */
	// public abstract static class ABoxOpenRule {
	// /** 规则ID */
	// final long ruleId;
	//
	// /** 修为 */
	// final long xiuwei;
	//
	// /**
	// * <pre>
	// * 开宝箱所能获得的道具
	// * 0长度表示不需要加道具
	// * unmodifiable
	// * </pre>
	// */
	// public final List<ItemCountStruct3> itemCounts;
	// /**
	// * <pre>
	// * 开宝箱所能获得的货币
	// * 0长度表示不需要加货币
	// * unmodifiable
	// * </pre>
	// */
	// final List<CurrencyCountStruct> currencyCounts;
	//
	// /**
	// * <pre>
	// * 开宝箱所能获得的元神
	// * 0长度表示不需要加元神
	// * unmodifiable
	// * </pre>
	// */
	// final List<VigourType> vigours;
	//
	// ABoxOpenRule(long ruleId, long xiuwei, List<ItemCountStruct3> itemCounts,
	// List<CurrencyCountStruct> currencyCounts, List<VigourType> vigours) {
	// this.ruleId = ruleId;
	// this.xiuwei = xiuwei;
	// this.itemCounts = Collections.unmodifiableList(itemCounts);
	// this.currencyCounts = Collections.unmodifiableList(currencyCounts);
	// this.vigours = Collections.unmodifiableList(vigours);
	// }
	//
	// static class VigourType{
	// final long vigour;
	// final long count;
	//
	// VigourType(long vigour, long count) {
	// this.vigour = vigour;
	// this.count = count;
	// }
	// }
	// }
	//
	// /**
	// * <pre>
	// * 对宝箱开启规则的定义
	// * CEND 道具--开宝箱只能加道具和货币，不能获得角色属性增加？
	// *
	// * @author CamusHuang
	// * @creation 2012-11-9 上午11:02:11
	// * </pre>
	// */
	// static class BoxOpenRule extends ABoxOpenRule {
	// /** 法宝模板ID */
	// final long talismanTeamplateId;//
	// private boolean isTalismanExist;
	//
	// /** 元神需求格数 */
	// final long maxVigourGridCount;
	//
	// BoxOpenRule(long ruleId, long talismanTeamplateId, long xiuwei,
	// List<ItemCountStruct3> itemCounts, List<CurrencyCountStruct>
	// currencyCounts, List<VigourType> vigours) {
	// super(ruleId, xiuwei, itemCounts, currencyCounts, vigours);
	// this.talismanTeamplateId = talismanTeamplateId;
	//
	// {
	// if (vigours.isEmpty()) {
	// maxVigourGridCount = 0;
	// } else {
	// long maxGrid = 1;
	// for (VigourType temp : vigours) {
	// maxGrid += temp.count;
	// }
	// maxVigourGridCount = maxGrid;
	// }
	//
	// }
	// }
	//
	// boolean isTalismanExist() {
	// return isTalismanExist;
	// }
	//
	// void setTalismanExist(boolean isTalismanExist) {
	// this.isTalismanExist = isTalismanExist;
	// }
	// }
	//
	// // ///////////////////////////////////////////
	// /**
	// * <pre>
	// * 对宝箱随机开启规则的定义
	// *
	// * @author CamusHuang
	// * @creation 2013-6-27 下午5:31:46
	// * </pre>
	// */
	// static class BoxRandomOpenRule extends ABoxOpenRule {
	//
	// /** 修为 */
	// final long xiuweiRate;
	//
	// /**
	// * <pre>
	// * 开宝箱所能获得的道具
	// * 0长度表示不需要加道具
	// * unmodifiable
	// * </pre>
	// */
	// final List<Integer> itemRates;
	//
	// /** 道具需求最大背包格数 */
	// final long maxBagGridCount;
	//
	// /**
	// * <pre>
	// * 开宝箱所能获得的货币
	// * 0长度表示不需要加货币
	// * unmodifiable
	// * </pre>
	// */
	// final List<Integer> currencyRates;
	//
	// /**
	// * <pre>
	// * 开宝箱所能获得的元神
	// * 0长度表示不需要加元神
	// * unmodifiable
	// * </pre>
	// */
	// final List<Integer> vigourRates;
	//
	// /** 元神需求最大格数 */
	// final long maxVigourGridCount;
	//
	// private long totalRatel;
	//
	// BoxRandomOpenRule(long ruleId, long xiuwei, long xiuweiRate,
	// List<ItemCountStruct3> itemCounts, List<Integer> itemRates,
	// List<CurrencyCountStruct> currencyCounts, List<Integer> currencyRates,
	// List<VigourType> vigours, List<Integer> vigourRates) {
	// super(ruleId, xiuwei, itemCounts, currencyCounts, vigours);
	// this.xiuweiRate = xiuweiRate;
	// this.itemRates = Collections.unmodifiableList(itemRates);
	// this.currencyRates = Collections.unmodifiableList(currencyRates);
	// this.vigourRates = Collections.unmodifiableList(vigourRates);
	//
	// if (xiuweiRate > 0) {
	// totalRatel += xiuweiRate;
	// }
	// for (Integer rate : itemRates) {
	// totalRatel += rate;
	// }
	// for (Integer rate : currencyRates) {
	// totalRatel += rate;
	// }
	// for (Integer rate : vigourRates) {
	// totalRatel += rate;
	// }
	//
	// {
	// if (itemCounts.isEmpty()) {
	// maxBagGridCount = 0;
	// } else {
	// long maxGrid = 1;
	// for (ItemCountStruct3 temp : itemCounts) {
	// if (temp.itemTemplate.Limit_StackMaxCount < 2) {
	// // 不可叠加
	// if (temp.itemCount > maxGrid) {
	// maxGrid = temp.itemCount;
	// }
	// }
	// }
	// maxBagGridCount = maxGrid;
	// }
	//
	// }
	//
	// {
	// if (vigours.isEmpty()) {
	// maxVigourGridCount = 0;
	// } else {
	// long maxGrid = 1;
	// for (VigourType temp : vigours) {
	// if (temp.count > maxGrid) {
	// maxGrid = temp.count;
	// }
	// }
	// maxVigourGridCount = maxGrid;
	// }
	//
	// }
	// }
	//
	// Object random() {
	// long rrate = KGameUtilTool.random(1, totalRatel);
	//
	// if (xiuweiRate > 0) {
	// rrate -= xiuweiRate;
	// if (rrate <= 0) {
	// return xiuwei;
	// }
	// }
	// for (long index = itemRates.size() - 1; index >= 0; index--) {
	// rrate -= itemRates.get(index);
	// if (rrate <= 0) {
	// return itemCounts.get(index);
	// }
	// }
	// for (long index = currencyRates.size() - 1; index >= 0; index--) {
	// rrate -= currencyRates.get(index);
	// if (rrate <= 0) {
	// return currencyCounts.get(index);
	// }
	// }
	// for (long index = vigourRates.size() - 1; index >= 0; index--) {
	// rrate -= vigourRates.get(index);
	// if (rrate <= 0) {
	// return vigours.get(index);
	// }
	// }
	// return null;
	// }
	// }
	//
	// // ///////////////////////////////////////////
	// /**
	// * <pre>
	// * 道具使用时相关的脚本结构
	// *
	// * @author CamusHuang
	// * @creation 2012-11-10 下午12:16:15
	// * </pre>
	// */
	// public static class ItemUseScriptStruct {
	// /** 脚本名称 */
	// public final String scriptName;
	// /** 脚本参数 */
	// public final String scriptParam;
	//
	// //宝箱类道具使用时间限制
	// public static final String scriptLimitTime = "limitTime";
	// private LimitTime mLimitTime;
	//
	// //消耗类道具每天限量使用
	// public static final String scriptLimitUse = "limitUse";
	// private long limit_useCountPetDay;
	//
	// //消耗类道具技能书的类型和等级
	// public static final String scriptSkillBook = "skillBook";
	// private SkillBookData mSkillBookTypeLv;
	//
	// ItemUseScriptStruct(String scriptName, String scriptParam) {
	// this.scriptName = scriptName;
	// this.scriptParam = scriptParam;
	// }
	//
	// String params() {
	// if (scriptName.equals(scriptLimitTime)) {
	// String datas[] = scriptParam.split(",");
	// mLimitTime = new LimitTime(Integer.parseInt(datas[0]),
	// Integer.parseInt(datas[1]));
	// if (mLimitTime.openDay >= mLimitTime.limitDay) {
	// return "开启天数限制不能大于等于作废天数限制";
	// }
	// if (mLimitTime.limitDay < 1) {
	// return "道具不能当天作废";
	// }
	// } else if (scriptName.equals(scriptSkillBook)) {
	// String datas[] = scriptParam.split(",");
	// mSkillBookTypeLv = new SkillBookData(Integer.parseInt(datas[0]),
	// Integer.parseInt(datas[1]));
	// } else if (scriptName.equals(scriptLimitUse)) {
	// limit_useCountPetDay = Integer.parseInt(scriptParam);
	// }
	// return null;
	// }
	//
	// public LimitTime getmLimitTime() {
	// return mLimitTime;
	// }
	//
	// public SkillBookData getSkillBookData() {
	// return mSkillBookTypeLv;
	// }
	//
	// /**
	// * <pre>
	// * 消耗类道具的每天可使用次数
	// *
	// * @return
	// * @author CamusHuang
	// * @creation 2013-12-28 下午1:57:54
	// * </pre>
	// */
	// public long getLimitUseCountPetDay(){
	// return limit_useCountPetDay;
	// }
	//
	// /**
	// * <pre>
	// * 时间限制
	// *
	// * @author CamusHuang
	// * @creation 2013-7-24 下午8:34:55
	// * </pre>
	// */
	// public class LimitTime {
	// public final long openDay;// 得到道具后多少天可以使用，例如1表示：周一领取的，周二凌晨0点整即可使用
	// public final long limitDay;// 得到道具后多少天作废，例如1表示：周一领取的，周二凌晨0点整即报废
	//
	// LimitTime(long openDay, long limitDay) {
	// this.openDay = openDay;
	// this.limitDay = limitDay;
	// }
	// }
	//
	// /**
	// * <pre>
	// * SKLL BOOK 类型和等级
	// *
	// * @author CamusHuang
	// * @creation 2013-7-24 下午8:34:55
	// * </pre>
	// */
	// public class SkillBookData {
	// public final long type;
	// public final long lv;
	//
	// SkillBookData(long type, long lv) {
	// this.type = type;
	// this.lv = lv;
	// }
	// }
	// }
	//
	// // /**
	// // * <pre>
	// // * 装备强化等级的强化数据
	// // *
	// // * @author CamusHuang
	// // * @creation 2012-11-9 下午3:55:15
	// // * </pre>
	// // */
	// // static class EquiStrongLevelData {
	// // final long strongLv;
	// // /**
	// // * 当前强化等级对应的强化数据 KEY=装备类型
	// // */
	// // final Map<KGameEquipmentTypeEnum, EquiTypeStrongData>
	// equiTypeStrongMap =
	// // new HashMap<KGameEquipmentTypeEnum, EquiTypeStrongData>();
	// //
	// // EquiStrongLevelData(long strongLv) {
	// // this.strongLv = strongLv;
	// // }
	// //
	// // /**
	// // * <pre>
	// // * 某装备类型的强化数据
	// // *
	// // * @author CamusHuang
	// // * @creation 2012-11-9 下午4:30:14
	// // * </pre>
	// // */
	// // static class EquiTypeStrongData {
	// // /** 装备类型 */
	// // final KGameEquipmentTypeEnum equiType;
	// // /**
	// // * <pre>
	// // * 某类型装备的所有不同职业下的强化数据
	// // * KEY=职业类型
	// // * VALUE=某职业对应的强化数据（武器按职业各不相同，但其它非武器装备不分职业数据相同）
	// // * </pre>
	// // */
	// // final Map<KGameOccupation, EquiJobStrongData> strongJobData = new
	// // HashMap<KGameOccupation, EquiJobStrongData>();
	// //
	// // EquiTypeStrongData(KGameEquipmentTypeEnum equiType) {
	// // this.equiType = equiType;
	// // }
	// //
	// // /**
	// // * <pre>
	// // * 某装备类型的某种职业类型下的强化数据
	// // *
	// // * @author CamusHuang
	// // * @creation 2012-11-12 下午4:51:53
	// // * </pre>
	// // */
	// // static class EquiJobStrongData {
	// // /** 职业类型 */
	// // final KGameOccupation jobType;
	// //
	// // /** 消耗的货币;NULL表示不需要消耗货币 */
	// // final CurrencyCountStruct cosumMoney;
	// // /**
	// // * <pre>
	// // * 强化得到的附加属性增量值
	// // * KEY=基础属性的序号
	// // * VALUE=增量值
	// // *
	// // * unmodifiable
	// // * </pre>
	// // */
	// // final Map<Integer, Integer> strongValue;
	// //
	// // EquiJobStrongData(KGameOccupation jobType, CurrencyCountStruct
	// // cosumMoney, Map<Integer, Integer> strongValue) {
	// // this.jobType = jobType;
	// // this.cosumMoney = cosumMoney;
	// // this.strongValue = Collections.unmodifiableMap(strongValue);
	// // }
	// //
	// // EquiJobStrongData(KGameOccupation jobType, EquiJobStrongData sameData)
	// {
	// // this.jobType = jobType;
	// // this.cosumMoney = sameData.cosumMoney;
	// // this.strongValue = sameData.strongValue;
	// // }
	// // }
	// // }
	// // }
	//
	// /**
	// * <pre>
	// * 某职业类型装备的强化数据
	// *
	// * @author CamusHuang
	// * @creation 2013-4-2 下午6:12:37
	// * </pre>
	// */
	// static class EquiStrongData_JobType {
	//
	// final byte job;
	//
	// /**
	// * <pre>
	// * 所有装备类型的强化数据
	// * KEY=装备类型
	// * </pre>
	// */
	// final Map<KGameEquipmentTypeEnum, EquiStrongData_EquiType> strongDataMap
	// = new HashMap<KGameEquipmentTypeEnum, EquiStrongData_EquiType>();
	//
	// EquiStrongData_JobType(byte job) {
	// this.job = job;
	// }
	//
	// /**
	// * <pre>
	// * 某装备类型的强化数据
	// *
	// * @author CamusHuang
	// * @creation 2013-4-2 下午6:14:35
	// * </pre>
	// */
	// static class EquiStrongData_EquiType {
	//
	// final KGameEquipmentTypeEnum equiTypeEnum;
	//
	// /**
	// * <pre>
	// * 所有品质类型的强化数据
	// * KEY=品质类型
	// * </pre>
	// */
	// final Map<KGameItemQualityEnum, EquiStrongData_Quality> strongDataMap =
	// new HashMap<KGameItemQualityEnum, EquiStrongData_Quality>();
	//
	// EquiStrongData_EquiType(KGameEquipmentTypeEnum equiTypeEnum) {
	// this.equiTypeEnum = equiTypeEnum;
	// }
	//
	// /**
	// * <pre>
	// * 某品质类型的强化数据
	// *
	// * @author CamusHuang
	// * @creation 2013-4-2 下午6:16:04
	// * </pre>
	// */
	// static class EquiStrongData_Quality {
	//
	// final KGameItemQualityEnum qualityEnum;
	// //
	// private EquiStrongData minStrongLevel;
	// private EquiStrongData maxStrongLevel;
	// /**
	// * <pre>
	// * 所有等级的强化数据
	// * KEY=强化等级
	// * </pre>
	// */
	// private final Map<Integer, EquiStrongData> strongDataMap = new
	// HashMap<Integer, EquiStrongData>();
	//
	// EquiStrongData_Quality(KGameItemQualityEnum qualityEnum) {
	// this.qualityEnum = qualityEnum;
	// }
	//
	// EquiStrongData getMinStrongLevel() {
	// return minStrongLevel;
	// }
	//
	// /**
	// * <pre>
	// *
	// * @deprecated 系统最大强化等级
	// * @return
	// * @author CamusHuang
	// * @creation 2013-4-10 下午4:29:21
	// * </pre>
	// */
	// long getMaxStrongLevel() {
	// return maxStrongLevel.strongLv;
	// }
	//
	// String addData(EquiStrongData data) {
	// if (strongDataMap.containsKey(data.strongLv)) {
	// return "重复的强化等级";
	// }
	//
	// strongDataMap.put(data.strongLv, data);
	// if (maxStrongLevel == null || data.strongLv > maxStrongLevel.strongLv) {
	// maxStrongLevel = data;
	// }
	// if (minStrongLevel == null || data.strongLv < minStrongLevel.strongLv) {
	// minStrongLevel = data;
	// }
	// return null;
	// }
	//
	// EquiStrongData getData(long lv) {
	// // if(lv >= maxStrongLevel.strongLv){
	// // return maxStrongLevel;
	// // }
	// return strongDataMap.get(lv);
	// }
	//
	// /**
	// * <pre>
	// * 检查强化等级断链
	// *
	// * @author CamusHuang
	// * @creation 2013-3-20 上午11:39:54
	// * </pre>
	// */
	// void loadFinishNotify(byte job, KGameEquipmentTypeEnum equiType,
	// KGameCurrencyType moneyType) throws KGameServerException {
	//
	// long minStrongLv = 1;
	// if (minStrongLevel.strongLv != minStrongLv) {
	// throw new KGameServerException("加载强化表数据错误：最小强化等级必须等于" + minStrongLv);
	// }
	// // 检查强化等级断链
	// // 各等级最终值=前置等级叠加
	// Map<Integer, Integer> totalStrongValues = new HashMap<Integer,
	// Integer>();
	// for (long i = minStrongLevel.strongLv; i <= maxStrongLevel.strongLv; i++)
	// {
	// EquiStrongData lvdata = strongDataMap.get(i);
	// if (lvdata == null) {
	// throw new KGameServerException("加载强化表数据错误：缺少装备强化数据 职业=" + job + ",装备类型="
	// + equiType.equiTypeName + ",品质=" + qualityEnum.qualityName + ",强化等级=" +
	// i);
	// }
	// if (lvdata.cosumMoney.currencyType != moneyType) {
	// throw new KGameServerException("加载强化表数据错误：货币类型必须统一 职业=" + job + ",装备类型="
	// + equiType.equiTypeName + ",品质=" + qualityEnum.qualityName + ",强化等级=" +
	// i);
	// }
	// // 叠加
	// addAttValues(totalStrongValues, lvdata.addStrongValue);
	// lvdata.initTotalStrongValue(totalStrongValues);
	// }
	// }
	//
	// /**
	// * <pre>
	// * 叠加属性值
	// *
	// * @param totalStrongValues
	// * @param values
	// * @author CamusHuang
	// * @creation 2013-4-29 下午12:21:38
	// * </pre>
	// */
	// private void addAttValues(Map<Integer, Integer> totalStrongValues,
	// Map<Integer, Integer> values) {
	// for (Entry<Integer, Integer> entry : values.entrySet()) {
	// Integer org = totalStrongValues.get(entry.getKey());
	// if (org == null) {
	// totalStrongValues.put(entry.getKey(), entry.getValue());
	// } else {
	// totalStrongValues.put(entry.getKey(), entry.getValue() + org);
	// }
	// }
	// }
	//
	// void serverStartCompleted() throws Exception {
	// long minLv = KVIPDataManager.mVIPLevelDataManager.getMinLevel().level;
	// long maxLv = KVIPDataManager.mVIPLevelDataManager.getMaxLevel().level;
	//
	// for (EquiStrongData data : strongDataMap.values()) {
	// for (long i = minLv; i <= maxLv; i++) {
	// if (!data.mEquiStrongLvRateMap.containsKey(i)) {
	// throw new Exception(",品质=" + qualityEnum.qualityName + ",强化等级=" +
	// data.strongLv + ",缺少VIP等级=" + i + " 的越级强化数据");
	// }
	// }
	// }
	// }
	//
	// /**
	// * <pre>
	// * 强化数据
	// *
	// * @author CamusHuang
	// * @creation 2013-4-2 下午6:16:04
	// * </pre>
	// */
	// static class EquiStrongData {
	// /** 强化等级 */
	// final long strongLv;
	//
	// /** 消耗的货币;不能为NULL */
	// final CurrencyCountStruct cosumMoney;
	// /**
	// * <pre>
	// * 强化得到的附加属性增量值，即当前强化等级比上一强化等级增加的强化值
	// * KEY=基础属性的序号
	// * VALUE=增量值
	// *
	// * unmodifiable
	// * </pre>
	// */
	// final Map<Integer, Integer> addStrongValue;
	//
	// /**
	// * <pre>
	// * 强化得到的附加属性增量值，即当前强化等级增加的总的强化值
	// * KEY=基础属性的序号
	// * VALUE=增量值
	// *
	// * unmodifiableMap
	// * </pre>
	// */
	// private Map<Integer, Integer> totalStrongValue;
	//
	// /** 强化成功率 */
	// final byte successRate;
	//
	// /** 强化石数量 */
	// final long strongStoneCount;
	//
	// /** 越级强化参数 KEY=VIP等级 */
	// final Map<Integer, EquiStrongLvRate> mEquiStrongLvRateMap;
	//
	// // /** 强制成功的货币代价 */
	// // final CurrencyCountStruct successMoney;
	//
	// EquiStrongData(long strongLv, CurrencyCountStruct cosumMoney,
	// Map<Integer, Integer> strongValue, byte successRate, long
	// strongStoneCount,
	// Map<Integer, EquiStrongLvRate> mEquiStrongLvRateMap) {// ,
	// // CurrencyCountStruct
	// // successMoney) {
	// this.strongLv = strongLv;
	// this.cosumMoney = cosumMoney;
	// this.addStrongValue = Collections.unmodifiableMap(strongValue);
	// this.successRate = successRate;
	// this.strongStoneCount = strongStoneCount;
	// this.mEquiStrongLvRateMap =
	// Collections.unmodifiableMap(mEquiStrongLvRateMap);
	// // this.successMoney = successMoney;
	// }
	//
	// void initTotalStrongValue(Map<Integer, Integer> totalStrongValue) {
	// this.totalStrongValue = Collections.unmodifiableMap(new HashMap<Integer,
	// Integer>(totalStrongValue));
	// }
	//
	// /**
	// * <pre>
	// * 强化得到的附加属性增量值，即当前强化等级增加的总的强化值
	// *
	// * @return
	// * @author CamusHuang
	// * @creation 2013-4-29 上午11:57:37
	// * </pre>
	// */
	// Map<Integer, Integer> getTotalStrongValue() {
	// return totalStrongValue;
	// }
	// }
	//
	// /**
	// * <pre>
	// * 越级强化
	// *
	// * @author CamusHuang
	// * @creation 2013-9-3 下午4:35:24
	// * </pre>
	// */
	// static class EquiStrongLvRate {
	// // overLvs
	// // 1,50;3,20;5,30
	// final long totalRate;
	// final List<int[]> lvs;// int[]{权重,强化级别}
	//
	// EquiStrongLvRate(List<int[]> lvs) {
	// this.lvs = lvs;
	// long rate = 0;
	// for (int[] data : lvs) {
	// rate += data[1];
	// }
	// this.totalRate = rate;
	// }
	//
	// long randomLv() {
	// long rate = KGameUtilTool.random(1, totalRate);
	// for (int[] data : lvs) {
	// rate -= data[1];
	// if (rate < 1) {
	// return data[0];
	// }
	// }
	// return lvs.get(KGameUtilTool.random(lvs.size()))[0];
	// }
	// }
	// }
	// }
	// }
	//
	// /**
	// * <pre>
	// * 技能及其应该放置的技能栏位置
	// *
	// * @author CamusHuang
	// * @creation 2013-1-28 下午12:05:34
	// * </pre>
	// */
	// public static class ItemSkillData {
	// public final long skillTemplateId;
	// private KGamePassiveSkillTemplate passiveSkillTemplate;
	//
	// public ItemSkillData(long skillTemplateId) {
	// this.skillTemplateId = skillTemplateId;
	// }
	//
	// public void onServerComplete() {
	// this.passiveSkillTemplate =
	// KSupportFactory.getSkillSupport().getPassiveSkillTemplate(skillTemplateId);
	// }
	//
	// public KGamePassiveSkillTemplate getPassiveSkillTemplate() {
	// return passiveSkillTemplate;
	// }
	// }
	//
	// public static class StrongResult {
	// public EquiStrongData data;
	// public boolean isOverflow;
	// }
	//
	// /**
	// * <pre>
	// * 祝福值
	// *
	// * @author CamusHuang
	// * @creation 2013-9-12 下午12:40:55
	// * </pre>
	// */
	// public static class LuckData {
	// // ////读写锁
	// public final ReentrantLock rwLock;
	// /**
	// * <pre>
	// * 祝福值
	// * KEY=类型值，VALUE=祝福值
	// * </pre>
	// */
	// private Map<Integer, Integer> luck = new HashMap<Integer, Integer>();
	//
	// public LuckData(ReentrantLock rwLock) {
	// this.rwLock = rwLock;
	// }
	//
	// public void addLuck(long stoneLv, long addValue) {
	// rwLock.lock();
	// try {
	// Integer temp = luck.get(stoneLv);
	// if (temp != null) {
	// addValue += temp;
	// }
	// if (addValue > 100) {
	// addValue = 100;
	// } else if (addValue < 0) {
	// addValue = 0;
	// }
	// luck.put(stoneLv, addValue);
	// } finally {
	// rwLock.unlock();
	// }
	// }
	//
	// public long getLuck(long stoneLv) {
	// rwLock.lock();
	// try {
	// Integer temp = luck.get(stoneLv);
	// if (temp == null) {
	// return 0;
	// } else {
	// return temp;
	// }
	// } finally {
	// rwLock.unlock();
	// }
	// }
	//
	// public Map<Integer, Integer> getLuckDatas() {
	// rwLock.lock();
	// try {
	// return new HashMap<Integer, Integer>(luck);
	// } finally {
	// rwLock.unlock();
	// }
	// }
	//
	// public void clearLuck(long stoneLv) {
	// rwLock.lock();
	// try {
	// luck.remove(stoneLv);
	// } finally {
	// rwLock.unlock();
	// }
	// }
	//
	// public JSONObject encodeData() throws JSONException {
	// rwLock.lock();
	// try {
	// JSONObject obj = new JSONObject();
	// for (Entry<Integer, Integer> e : luck.entrySet()) {
	// if (e.getValue() > 0) {
	// obj.put(e.getKey() + "", e.getValue().intValue());
	// }
	// }
	// return obj;
	// } finally {
	// rwLock.unlock();
	// }
	// }
	//
	// public void decodeData(JSONObject obj) throws JSONException {
	// if (obj == null) {
	// return;
	// }
	// for (Iterator<String> it = obj.keys(); it.hasNext();) {
	// String key = it.next();
	// luck.put(Integer.parseInt(key), obj.getInt(key));
	// }
	// }
	// }
}
