package com.kola.kmp.logic.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.fashion.KFashionDataStructs.KFashionTemplate;
import com.kola.kmp.logic.item.KItem.KItemCA.KItem_EquipmentData;
import com.kola.kmp.logic.item.KItem.KItemCA.KItem_EquipmentData.SecondWeapon;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempEquiBox.ItemTempEquiBoxForJob.ItemTempEquiBoxForLv;
import com.kola.kmp.logic.level.KLevelTemplate;
import com.kola.kmp.logic.mount.KMountDataManager;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountTemplate;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KEquipmentTypeEnum;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.other.KItemQualityEnum;
import com.kola.kmp.logic.other.KItemTypeEnum;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.other.KLimitTimeProduceActivityTypeEnum;
import com.kola.kmp.logic.pet.KPetTemplate;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.TimeLimitActivityDataManager.TimeLimieProduceActivity;
import com.kola.kmp.logic.role.KRoleModuleConfig;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.text.HyperTextTool;
import com.kola.kmp.logic.util.tips.ItemTips;

/**
 * <pre>
 * 本类负责定义本模块的数据结构
 * 本类纯粹定义数据结构,而不管理数据
 * @author CamusHuang
 * @creation 2012-11-6 上午11:09:57
 * </pre>
 */
public class KItemDataStructs {

	public static final Logger _LOGGER = KGameLogger.getLogger(KItemDataStructs.class);

	/**
	 * <pre>
	 * 背包扩容数据
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-7 下午3:34:04
	 * </pre>
	 */
	public static class KBagExtData {
		// ----------以下是EXCEL表格直导数据---------
		public int id;// 背包格子编号
		public int needopen;// 是否需要开启
		private int att_type;// 开启后增加属性类型
		private int att_value;// 效果值
		private int money_type;// 花费货币类型
		private int money_value;// 货币数量

		// ----------以下是逻辑数据---------
		public KCurrencyCountStruct payMoney;
		public AttValueStruct attValue;

		void onGameWorldInitComplete() throws Exception {
			//
			if (needopen == 1) {
				if (money_type > 0 && money_value > 0) {
					payMoney = new KCurrencyCountStruct(KCurrencyTypeEnum.getEnum(money_type), money_value);
					if (payMoney.currencyType == null) {
						throw new Exception("货币类型不存在，use_type=" + money_type);
					}
				} else {
					throw new Exception("免费格数错误，小格ID=" + id);
				}

				if (att_type > 0 && att_value > 0) {
					KGameAttrType type = KGameAttrType.getAttrTypeEnum(att_type);
					attValue = new AttValueStruct(type, att_value, 0);
					if (attValue.roleAttType == null) {
						throw new Exception("属性类型不存在，affect_type=" + att_type);
					}
				}
			}
			// CTODO 其它约束检查
		}
	}

	/**
	 * <pre>
	 * 道具模板抽象类
	 * 
	 * @param <ED>
	 * @author CamusHuang
	 * @creation 2014-2-23 下午4:59:40
	 * </pre>
	 */
	public static abstract class KItemTempAbs {
		// ----------以下是EXCEL表格直导数据---------
		String id;// 物品id号
		public String name;// 物品名称
		public int qua;// 品质
		public int type;// 物品类型
		public int lvl;// 使用等级
		public int stack;// 背包里叠加数量
		private int sgold;// 出售价格
		private int bgold;// 买入金币
		private int brmb;// 买入金钻
		public int bind;// 是否绑定
		public int icon;// 对应图片
		private String desc;// 物品描述

		// ----------以下是逻辑数据---------
		public String itemCode;// 即数据表中的物品ID
		/** 染色的道具名称 */
		public String extItemName;
		/** 道具类型 */
		public KItemTypeEnum ItemType;
		/** 道具品质 */
		public KItemQualityEnum ItemQuality;
		public KCurrencyCountStruct sellMoney;
		public KCurrencyCountStruct buyMoney;

		//
		/**
		 * <pre>
		 * 
		 * 
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2014-2-18 下午6:01:47
		 * </pre>
		 */
		void dataLoadFinishedNotify() throws Exception {
			itemCode = id;
			ItemType = KItemTypeEnum.getEnum(type);
			if (ItemType == null) {
				throw new Exception("道具类型不存在  type=" + type + " id=" + id);
			}
			ItemQuality = KItemQualityEnum.getEnum(qua);
			if (ItemQuality == null) {
				throw new Exception("品质不存在  qua=" + qua + " id=" + id);
			}
			if (stack < 1) {
				throw new Exception("叠加数量不能小于1 id=" + id);
			}

			extItemName = HyperTextTool.extColor(name, ItemQuality.color);
			// CTODO 其它约束检查

		}

		/**
		 * <pre>
		 * 
		 * 
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2014-2-18 下午6:01:47
		 * </pre>
		 */
		void onGameWorldInitComplete() throws Exception {

			if (sgold > 0) {
				sellMoney = new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD, sgold);
			}

			if (bgold > 0 && brmb > 0) {
				throw new Exception("购买价格错误 id=" + id);
			}

			if (bgold > 0) {
				buyMoney = new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD, bgold);
			} else if (brmb > 0) {
				buyMoney = new KCurrencyCountStruct(KCurrencyTypeEnum.DIAMOND, brmb);
			}

			// CTODO 其它约束检查
		}

		public boolean isBind() {
			return bind == 1;
		}

		public boolean isCanStack() {
			return stack > 1;
		}

		public String getDesc() {
			return desc;
		}

		private void setDesc(String newDesc) {
			this.desc = newDesc;
		}
	}

	/**
	 * <pre>
	 * 装备类道具模板
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-2 下午6:23:58
	 * </pre>
	 */
	public static class KItemTempEqui extends KItemTempAbs {
		// ----------以下是EXCEL表格直导数据---------
		public int job;// 需求职业
		public int part;// 穿戴部位
		public String showResId; // 场景展示的资源id
		private int[] attribute_id;// 增加属性编号
		private int[] MinAattribute;// 增加属性数量下限
		private int[] AattributeUnitSpan;// 基础属性跨度
		private int MaxAattributeSection;// 基础属性最大划分段数（限0~3）

		// public int holenum;// 宝石孔个数--默认免费开放的孔位数量
		private int[] stoneType;// 能插入的宝石类型

		private int resolve_gold;// 分解产出金币
		private String[] resolve_item;// 分解产出物品及数量

		// private float cohesion;// 副武器聚力时间(秒，非副武器为0)
		public int cohesionPct; // 副武器聚力伤害加成（万分比，非副武器为0）
		public int volume;// 弹夹容量
		public float overheated;// 副武器过热时间(秒，非副武器为0)

		// 装备产出
		private int sceneType;// 关卡类型
		private int droproute;// 关卡ID

		int bBornEquip;// 是否为出生装备
		// ----------以下是逻辑数据---------
		// <宝石类型,排序号0~N>
		public LinkedHashMap<Integer, Integer> stoneTypeMapForType = new LinkedHashMap<Integer, Integer>();// 能插入的宝石类型
		// <排序号0~N,宝石类型>
		public LinkedHashMap<Integer, Integer> stoneTypeMapForIndex = new LinkedHashMap<Integer, Integer>();// 能插入的宝石类型

		public KJobTypeEnum jobEnum;// null表示无职业要求
		public KEquipmentTypeEnum typeEnum;// 装备类型
		// ================================
		private Map<KGameAttrType, BaseEffectData> baseAttMap = new HashMap<KGameAttrType, BaseEffectData>();
		// 慎用<基础属性段级,角色等级1时的最大基础属性战斗力>
		public Map<Integer, Integer> battlePowerMap = new HashMap<Integer, Integer>();
		// 慎用<基础属性段级,基础属性>
		private Map<Integer, Map<KGameAttrType, Integer>> baseAttForLvMap = new HashMap<Integer, Map<KGameAttrType, Integer>>();
		// 慎用，最大段级的基础属性
		public Map<KGameAttrType, Integer> maxBaseAttMap;
		// ================================
		// 装备分解
		public KCurrencyCountStruct resolveMoney;// 分解价格
		public List<ItemCountStruct> resolveItems = Collections.emptyList();// 不包含0物品
		private List<ItemCountStruct> allRresolveItems = Collections.emptyList();// 包含0物品
		private List<Integer> resolveItemRates = Collections.emptyList();// 几率
		private int allResolveItemRate = 0;// 几率总和
		// 当前装备产出关卡
		private EquiProduceData mEquiProduceData;
		// 同级更高品质装备产出关卡
		private ArrayList<EquiProduceData> mTopQualityEquiProduceDataList = new ArrayList<EquiProduceData>(KItemQualityEnum.values().length);
		// 下一级装备产出关卡
		private ArrayList<EquiProduceData> mNextLevelEquiProduceDataList = new ArrayList<EquiProduceData>(KItemQualityEnum.values().length);

		ItemCountStruct randomResolveItem() {
			return ItemCountStruct.randomItem(allRresolveItems, resolveItemRates, allResolveItemRate);
		}

		/**
		 * <pre>
		 * 当前装备的产出数据
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2014-7-20 下午7:12:18
		 * </pre>
		 */
		EquiProduceData getEquiProduceData() {
			return mEquiProduceData;
		}

		/**
		 * <pre>
		 * 当前装备同级更高品质的产出数据
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2014-7-20 下午7:12:28
		 * </pre>
		 */
		ArrayList<EquiProduceData> getTopQualityEquiProduceDataList() {
			return mTopQualityEquiProduceDataList;
		}

		/**
		 * <pre>
		 * 当前装备更高级的产出数据
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2014-7-20 下午7:12:40
		 * </pre>
		 */
		ArrayList<EquiProduceData> getNextLevelEquiProduceDataList() {
			return mNextLevelEquiProduceDataList;
		}

		List<EquiProduceData> getEquiProduceDataList(int roleLv) {
			int maxLvl = KItemDataManager.mItemTemplateManager.checkMatchEquiLv(roleLv);
			if (maxLvl <= lvl) {
				if (!mTopQualityEquiProduceDataList.isEmpty()) {
					return mTopQualityEquiProduceDataList;
				}
				return mNextLevelEquiProduceDataList;
			}
			return mNextLevelEquiProduceDataList;
		}

		/**
		 * <pre>
		 * 固定镶嵌孔数
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2014-11-16 下午9:25:32
		 * </pre>
		 */
		int getFixEnchansePosition() {
			return KItemDataManager.mEquiEnchanseDataManager.getNum(ItemQuality);
		}

		/**
		 * <pre>
		 * 固定镶嵌孔数+购买孔数
		 * 
		 * @param equiData
		 * @return
		 * @author CamusHuang
		 * @creation 2014-12-4 下午3:21:18
		 * </pre>
		 */
		int getTotalEnchansePosition(KItem_EquipmentData equiData) {
			int result = equiData == null ? 0 : equiData.getBuyEnchansePosition();
			result += KItemDataManager.mEquiEnchanseDataManager.getNum(ItemQuality);
			return Math.min(result, KItemConfig.getInstance().MaxEnchansePositionPerOne);
		}

		// Map<KGameAttrType, Integer> randomBaseAttNs(Map<KGameAttrType,
		// Integer> baseAttNs) {
		// if (baseAttNs == null) {
		// baseAttNs = new HashMap<KGameAttrType, Integer>();
		// } else {
		// baseAttNs.clear();
		// }
		// for (Entry<KGameAttrType, BaseEffectData> e : baseEffects.entrySet())
		// {
		// baseAttNs.put(e.getKey(), randomN());
		// }
		// return baseAttNs;
		// }

		// /**
		// * <pre>
		// *
		// *
		// * @param Ns 随机数N
		// * @param baseAtts
		// * @return
		// * @author CamusHuang
		// * @creation 2014-10-27 下午3:22:48
		// * </pre>
		// */
		// void getBaseAtts(int N, Map<KGameAttrType, Integer> baseAtts) {
		//
		// baseAtts.putAll(baseAttForLvMap.get(N));
		//
		// // 清除多余的值
		// for (Iterator<Entry<KGameAttrType, Integer>> it =
		// baseAtts.entrySet().iterator(); it.hasNext();) {
		// Entry<KGameAttrType, Integer> e = it.next();
		// if (!baseAttMap.containsKey(e.getKey())) {
		// it.remove();
		// }
		// }
		// }

		/**
		 * <pre>
		 * 
		 * 
		 * @param 段级
		 * @param baseAtts
		 * @return
		 * @author CamusHuang
		 * @creation 2014-10-27 下午3:22:48
		 * </pre>
		 */
		Map<KGameAttrType, Integer> getBaseAttForLv(int N) {
			return baseAttForLvMap.get(N);
		}

		int randomN() {
			return UtilTool.random(0, MaxAattributeSection);
		}

		int getMaxAattributeSection() {
			return MaxAattributeSection;
		}

		/**
		 * <pre>
		 * 
		 * 
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2014-2-18 下午6:01:47
		 * </pre>
		 */
		void onGameWorldInitComplete() throws Exception {
			super.onGameWorldInitComplete();

			jobEnum = KJobTypeEnum.getJob((byte) job);
			typeEnum = KEquipmentTypeEnum.getEnum(part);
			if (typeEnum == null) {
				throw new Exception("装备部位错误 装备ID=" + id);
			}

			{
				// int holenum =
				// KItemDataManager.mEquiEnchanseDataManager.getNum(super.ItemQuality);
				int holenum = KItemConfig.getInstance().MaxEnchansePositionPerOne;
				if (stoneType.length < holenum) {
					throw new Exception("镶嵌宝石类型数量比最大孔数要少 装备ID=" + id);
				}

				// 宝石类型是否存在
				// 宝石类型与位置映射
				for (int index = 0; index < stoneType.length; index++) {
					int type = stoneType[index];
					if (!KItemDataManager.mItemTemplateManager.containStoneType(type)) {
						throw new Exception("镶嵌宝石类型不存在 装备ID=" + id);
					}
					if (stoneTypeMapForType.put(type, index) != null) {
						throw new Exception("镶嵌宝石类型重复=" + type + " 装备ID=" + id);
					}
					stoneTypeMapForIndex.put(index, type);
				}
			}

			if (resolve_gold > 0) {
				resolveMoney = new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD, resolve_gold);
			}

			if (resolve_item != null && resolve_item.length > 0) {
				allRresolveItems = new ArrayList<ItemCountStruct>();
				resolveItems = new ArrayList<ItemCountStruct>();
				resolveItemRates = new ArrayList<Integer>();
				for (String temp : resolve_item) {
					String[] temps = temp.split("\\*");
					ItemCountStruct tempS = new ItemCountStruct(temps[0], Long.parseLong(temps[1]));
					if (tempS.itemCount < 0) {
						// 允许数量填0表示不加任何道具
						throw new Exception("道具数量错误=" + temps[1] + " id=" + id);
					}
					int rate = Integer.parseInt(temps[2]);
					if (rate < 1) {
						throw new Exception("道具权重错误=" + rate + " id=" + id);
					}

					KItemTempAbs itemTemp = tempS.getItemTemplate();
					if (itemTemp == null) {
						if (!temps[0].equals("0")) {
							// 允许模板填0表示不加任何道具
							throw new Exception("道具模板不存在=" + temps[0] + " id=" + id);
						}
					}
					if (itemTemp != null && tempS.itemCount > 0) {
						resolveItems.add(tempS);
					}
					allRresolveItems.add(tempS);
					resolveItemRates.add(rate);
					allResolveItemRate += rate;
				}
			}

			// if (resolveMoney == null && resolve_item == null) {
			// throw new Exception("装备分解错误:没有配置产出");
			// }
			{
				if (attribute_id.length < 1) {
					throw new Exception("基础属性未配置 id=" + id);
				}

				if (attribute_id.length != MinAattribute.length || attribute_id.length != AattributeUnitSpan.length) {
					throw new Exception("基础属性 相关参数数量不一致");
				}

				if (MaxAattributeSection < 0 || MaxAattributeSection > 3) {
					throw new Exception("数值错误，限0~3 MaxAattributeSection=" + MaxAattributeSection);
				}

				{
					Map<KGameAttrType, BaseEffectData> tempEffects = new LinkedHashMap<KGameAttrType, BaseEffectData>();
					for (int index = 0; index < attribute_id.length; index++) {
						KGameAttrType type = KGameAttrType.getAttrTypeEnum(attribute_id[index]);
						if (type == null) {
							throw new Exception("基础属性类型不存在 type=" + attribute_id[index] + " id=" + id);
						}
						tempEffects.put(type, new BaseEffectData(type, MinAattribute[index], AattributeUnitSpan[index]));
					}

					if (tempEffects.isEmpty()) {
						throw new Exception("基础属性未配置 id=" + id);
					}
					baseAttMap = Collections.unmodifiableMap(tempEffects);
				}
				for (int N = 0; N <= MaxAattributeSection; N++) {
					Map<KGameAttrType, Integer> baseAtts = new LinkedHashMap<KGameAttrType, Integer>();
					for (BaseEffectData data : baseAttMap.values()) {
						baseAtts.put(data.attType, data.getValue(N));
					}

					baseAttForLvMap.put(N, baseAtts);
					battlePowerMap.put(N, KSupportFactory.getRoleModuleSupport().calculateBattlePower(baseAtts, 1));
				}
				maxBaseAttMap = baseAttForLvMap.get(MaxAattributeSection);
			}

			if (typeEnum == KEquipmentTypeEnum.副武器) {
				if (jobEnum == null) {
					throw new Exception("副武器职业错误 装备ID=" + id);
				}
				// 验证副武器参数是否正常
				try {
					SecondWeapon mSecondWeapon = new SecondWeapon(this);
				} catch (Exception e) {
					throw new Exception(e.getMessage(), e);
				}
			}

			// 更高级的装备产出
			if (droproute > 0) {
				byte levelTypeID = (byte) sceneType;
				int level = droproute;

				String desc = UtilTool.getNotNullString(null);
				KGameLevelTypeEnum levelType = KGameLevelTypeEnum.getEnum(levelTypeID);
				if (levelType == null) {
					throw new Exception("关卡类型不存在 类型=" + levelTypeID + " 当前装备ID=" + id);
				}
				KLevelTemplate leveldata = KSupportFactory.getLevelSupport().getNormalGameLevelTemplate(levelType, level);
				if (leveldata == null) {
					throw new Exception("高级装备产出关卡不存在 类型=" + levelTypeID + " 关卡=" + level + " 当前装备ID=" + id);
				}
				if (levelType == KGameLevelTypeEnum.普通关卡) {
					desc = KSupportFactory.getLevelSupport().getScenarioNameByLevelId(level);
				} else {
					desc = levelType.name();
				}
				mEquiProduceData = new EquiProduceData(this, levelType, level, desc);
			}

			if (bBornEquip == 1) {
				if (lvl != 1) {
					throw new Exception("出生装备穿戴等级不正确 装备ID=" + id);
				}
			}
			// CTODO 其它约束检查
		}

		/**
		 * <pre>
		 * 随机数n=rand（0，基础属性最大划分段数）
		 * n为随机在0到基础属性最大划分段数的整数中去一个值
		 * n在装备出生时随机设定，终身不变
		 * 
		 * 动态计算 基础属性=增加属性数量下限+n*基础属性跨度
		 * 
		 * 
		 * @author CamusHuang
		 * @creation 2014-10-27 下午12:15:42
		 * </pre>
		 */
		static class BaseEffectData {
			private KGameAttrType attType;// 增加属性编号
			private int MinAattribute;// 增加属性数量下限
			private int AattributeUnitSpan;// 基础属性跨度

			// ----------以下是逻辑数据---------

			BaseEffectData(KGameAttrType attType, int minAattribute, int aattributeUnitSpan) {
				this.attType = attType;
				MinAattribute = minAattribute;
				AattributeUnitSpan = aattributeUnitSpan;
			}

			int getValue(int n) {
				// 增加属性数量下限+n*基础属性跨度
				return MinAattribute + n * AattributeUnitSpan;
			}

			void onGameWorldInitComplete() throws Exception {
				if (MinAattribute < 1) {
					throw new Exception("MinAattribute<1, attribute_id=" + attType.sign);
				}
				if (AattributeUnitSpan < 1) {
					throw new Exception("AattributeUnitSpan<1, attribute_id=" + attType.sign);
				}
			}
		}

		/**
		 * <pre>
		 * 装备产出
		 * 
		 * @author CamusHuang
		 * @creation 2014-7-10 下午12:07:54
		 * </pre>
		 */
		public static class EquiProduceData {
			public final KItemTempEqui temp;
			public final KGameLevelTypeEnum levelType;
			public final int level;
			public final String desc;

			private EquiProduceData(KItemTempEqui temp, KGameLevelTypeEnum levelType, int level, String desc) {
				this.temp = temp;
				this.levelType = levelType;
				this.level = level;
				this.desc = desc;
			}
		}

	}

	/**
	 * <pre>
	 * 材料类道具模板
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-2 下午6:23:58
	 * </pre>
	 */
	public static class KItemTempMaterial extends KItemTempAbs {
		// ----------以下是EXCEL表格直导数据---------
		private String comptar;// 选择合成目标id
		public int compnum;// 合成需要数量

		// ----------以下是逻辑数据---------
		// <itemCode, ItemCountStruct>, null表示不能合成
		public Map<String, ItemCountStruct> composeTarget = Collections.emptyMap();
		public List<ItemCountStruct> composeTargetList = Collections.emptyList();
		// <itemCode, KCurrencyCountStruct>, value=null表示免费
		public Map<String, KCurrencyCountStruct> composeTargetPrice = Collections.emptyMap();

		// public KCurrencyCountStruct composePrice;//

		/**
		 * <pre>
		 * 
		 * 
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2014-2-18 下午6:01:47
		 * </pre>
		 */
		void onGameWorldInitComplete() throws Exception {
			super.onGameWorldInitComplete();

			if (comptar != null && !comptar.isEmpty()) {
				composeTarget = new HashMap<String, ItemCountStruct>();
				composeTargetList = new ArrayList<ItemCountStruct>();
				composeTargetPrice = new HashMap<String, KCurrencyCountStruct>();
				{
					String[] opentiem = comptar.split(",");
					for (String temp : opentiem) {
						String[] temps = temp.split("-");
						KItemTempAbs itemTemp = KItemDataManager.mItemTemplateManager.getItemTemplate(temps[0]);
						if (itemTemp == null) {
							throw new KGameServerException("道具模板不存在=" + temps[0]);
						}
						long price = Long.parseLong(temps[1]);
						if (price < 0) {
							throw new KGameServerException("合成价格错误=" + price);
						}
						ItemCountStruct struct = new ItemCountStruct(itemTemp, 1);
						composeTargetList.add(struct);
						composeTarget.put(itemTemp.itemCode, struct);
						if (price > 0) {
							composeTargetPrice.put(itemTemp.itemCode, new KCurrencyCountStruct(KCurrencyTypeEnum.DIAMOND, price));
						}
					}
				}

				if (composeTarget.isEmpty()) {
					throw new KGameServerException("合成物品错误=" + comptar);
				}

				if (compnum <= 0) {
					throw new Exception("材料合成需要数量错误 = " + compnum);
				}
				// if (gold < 0) {
				// throw new Exception("材料合成消耗金币数量错误 = " + gold);
				// }
				// if (compose <= 0) {
				// throw new Exception("材料合成基础成功率错误 = " + compose);
				// }
				// composePrice = new
				// KCurrencyCountStruct(KCurrencyTypeEnum.GOLD, gold);
			}

			// CTODO 其它约束检查
		}
	}

	/**
	 * <pre>
	 * 宝石类道具模板
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-2 下午6:23:58
	 * </pre>
	 */
	public static class KItemTempStone extends KItemTempAbs {
		// ----------以下是EXCEL表格直导数据---------
		public int stoneType;// 宝石类型
		public int stonelvl;// 宝石等级
		private String comptar;// 合成目标id
		public int compnum;// 合成需要数量
		public int complv;// 合成等级限制
		// private String[] usemoney;// 所需金币或钻石
		// private String[] lose;// 失败碎裂个数和几率
		private int[] attribute_id;// 增加属性编号
		private int[] attribute;// 增加属性数量
		private int downneedgold;// 卸下宝石所需金币
		private int downneedrmb;// 卸下宝石所需钻石
		// ----------以下是逻辑数据---------
		public ItemCountStruct composeTarget;// null表示不能合成
		// public KCurrencyCountStruct composePriceGold;//0表示不收费
		// public int composePriceGoldSuccessRate;// 金币付费的成功率(万分比)
		// public KCurrencyCountStruct composePriceDiamond;//0表示不收费
		// public int composePriceDiamondSuccessRate;// 钻石付费的成功率(万分比)
		// public int[] loseNum;// 失败时可能损失的数量，0长度表示不会损失
		// public int[] loseRate;// 失败时可能损失的数量对应几率，0长度表示不会损失
		// public int loseAllRate;// 0表示不会损失
		public KCurrencyCountStruct cancelEnchansePrice;// 卸载宝石货币消费，不为null，不为0
		public Map<KGameAttrType, Integer> allEffects = Collections.emptyMap();// LinkedHashMap

		// /**
		// * <pre>
		// * 随机计算损失数量
		// *
		// * @return
		// * @author CamusHuang
		// * @creation 2014-3-4 上午9:29:01
		// * </pre>
		// */
		// int randomLoseNum() {
		// if (composeTarget == null) {
		// return 0;
		// }
		// if (loseNum == null || loseNum.length < 1) {
		// return 0;
		// }
		// int rate = UtilTool.random(1, loseAllRate);
		// for (int index = 0; index < loseRate.length; index++) {
		// rate -= loseRate[index];
		// if (rate < 1) {
		// return loseNum[index];
		// }
		// }
		// return loseNum[0];
		// }

		/**
		 * <pre>
		 * 
		 * 
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2014-2-18 下午6:01:47
		 * </pre>
		 */
		void onGameWorldInitComplete() throws Exception {
			super.onGameWorldInitComplete();

			if ((downneedgold < 1 && downneedrmb < 1) || (downneedgold > 0 && downneedrmb > 0)) {
				throw new Exception("宝石卸载价格错误");
			}

			if (downneedgold > 0) {
				cancelEnchansePrice = new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD, downneedgold);
			} else {
				cancelEnchansePrice = new KCurrencyCountStruct(KCurrencyTypeEnum.DIAMOND, downneedrmb);
			}

			// 本宝石类型是否能镶嵌到某个装备当中
			{
				boolean isFind = false;
				for (KItemTempAbs temp : KItemDataManager.mItemTemplateManager.getItemTemplateList()) {
					if (temp.type == KItemTypeEnum.装备.sign) {
						KItemTempEqui equiTmpe = (KItemTempEqui) temp;
						for (int type : equiTmpe.stoneType) {
							if (type == stoneType) {
								isFind = true;
								break;
							}
						}
					}
				}
				if (!isFind) {
					throw new Exception("没有可镶嵌本宝石类型的装备 宝石ID=" + id);
				}
			}

			{
				if (attribute_id.length != attribute.length || attribute_id.length < 1) {
					throw new Exception("属性加成未配置 id=" + id);
				}
				Map<KGameAttrType, Integer> tempEffects = new LinkedHashMap<KGameAttrType, Integer>();
				for (int index = 0; index < attribute_id.length; index++) {
					if (attribute[index] < 1) {
						continue;
					}
					KGameAttrType type = KGameAttrType.getAttrTypeEnum(attribute_id[index]);
					if (type == null) {
						throw new Exception("属性加成类型不存在 type=" + attribute_id[index] + " id=" + id);
					}
					tempEffects.put(type, attribute[index]);
				}

				if (tempEffects.isEmpty()) {
					throw new Exception("属性加成未配置 id=" + id);
				}
				allEffects = Collections.unmodifiableMap(tempEffects);

//				if (allEffects.size() != 1) {
//					throw new Exception("属性加成不能超过1项 id=" + id);
//				}
			}
			if (comptar != null && !comptar.isEmpty()) {
				KItemTempStone itemTemplate = (KItemTempStone) KItemDataManager.mItemTemplateManager.getItemTemplate(comptar);
				if (itemTemplate == null) {
					throw new Exception("宝石合成结果错误 = " + comptar);
				}

				if (itemTemplate.stonelvl != stonelvl + 1) {
					throw new Exception("宝石合成结果错误:宝石等级非递增");
				}

				composeTarget = new ItemCountStruct(itemTemplate, 1);

				if (compnum <= 0) {
					throw new Exception("宝石合成需要数量错误 = " + compnum);
				}

				// if (usemoney.length != 2) {
				// throw new Exception("宝石合成价格错误");
				// }
				// //
				// {
				// String[] temp = usemoney[0].split("\\*");
				// composePriceGold = new
				// KCurrencyCountStruct(KCurrencyTypeEnum.GOLD,
				// Integer.parseInt(temp[0]));
				// composePriceGoldSuccessRate = (int)
				// (Float.parseFloat(temp[1]) * 10000);
				// if (composePriceGold.currencyCount < 0) {
				// throw new Exception("宝石合成消耗金币数量错误 = " +
				// composePriceGold.currencyCount);
				// }
				// if (composePriceGoldSuccessRate <= 0) {
				// throw new Exception("宝石合成金币成功率错误 = " + temp[1]);
				// }
				// }
				// //
				// {
				// String[] temp = usemoney[1].split("\\*");
				// composePriceDiamond = new
				// KCurrencyCountStruct(KCurrencyTypeEnum.DIAMOND,
				// Integer.parseInt(temp[0]));
				// composePriceDiamondSuccessRate = (int)
				// (Float.parseFloat(temp[1]) * 10000);
				// if (composePriceDiamond.currencyCount < 0) {
				// throw new Exception("宝石合成消耗钻石数量错误 = " +
				// composePriceDiamond.currencyCount);
				// }
				// if (composePriceDiamondSuccessRate <= 0) {
				// throw new Exception("宝石合成钻石成功率错误 = " + temp[1]);
				// }
				// }
				// // 3*50,2*100,1*200,0*2000
				// loseNum = new int[lose.length];
				// loseRate = new int[lose.length];
				// if (lose.length > 0) {
				// for (int i = 0; i < lose.length; i++) {
				// String[] temps = lose[i].split("\\*");
				// loseNum[i] = Integer.parseInt(temps[0]);
				// loseRate[i] = Integer.parseInt(temps[1]);
				// loseAllRate += loseRate[i];
				// }
				// }
			}
			{
				// "属性：抗暴击+226
				// 功能：用于宝石镶嵌
				// 合成：3个可合成1个十级宝石"
				// "属性：抗暴击+307
				// 功能：用于宝石镶嵌
				// 合成：顶级宝石，不能合成"

				StringBuffer descbuffer = new StringBuffer();
				if (composeTarget == null) {
					descbuffer.append(ItemTips.顶级宝石不能合成).append('\n');
				} else {
					descbuffer.append(StringUtil.format(ItemTips.x个可合成1个x, compnum, composeTarget.getItemTemplate().extItemName)).append('\n');
				}
				for (Entry<KGameAttrType, Integer> e : allEffects.entrySet()) {
					descbuffer.append(ItemTips.属性).append(e.getKey().getExtName()).append('+').append(e.getValue()).append('\n');
				}

				if (descbuffer.length() > 0) {
					String desc = super.getDesc();
					desc = desc.contains("{}") ? desc : (ItemTips.用于宝石镶嵌 + '\n' + "{}");
					desc = StringUtil.format(desc, descbuffer.toString());
					super.setDesc(desc);
				}
				// CTODO 其它约束检查
			}
		}

	}

	/**
	 * <pre>
	 * 消耗类道具模板
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-2 下午6:23:58
	 * </pre>
	 */
	public static class KItemTempConsume extends KItemTempAbs {
		// ----------以下是EXCEL表格直导数据---------
		private float cdtime;// 物品冷却时间
		private int att_type;// 增加属性类型
		private int att_value;// 效果值
		private float att_usetime;// 效果持续时间
		private int money_type;// 增加货币
		private int money_value;// 货币数量

		// ----------以下是逻辑数据---------
		public long cdTimeInMill;// 物品冷却时间(毫秒)
		public AttValueStruct addAtt;
		public KCurrencyCountStruct addMoney;

		/**
		 * <pre>
		 * 
		 * 
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2014-2-18 下午6:01:47
		 * </pre>
		 */
		void onGameWorldInitComplete() throws Exception {
			super.onGameWorldInitComplete();

			// CTODO 其它约束检查
			cdTimeInMill = (long) (cdtime * Timer.ONE_SECOND);

			if (att_usetime < 0) {
				throw new Exception("att_usetime 不能小于0");
			}

			StringBuffer descbuffer = new StringBuffer();
			if (att_type > 0 && att_value != 0) {
				KGameAttrType type = KGameAttrType.getAttrTypeEnum(att_type);
				addAtt = new AttValueStruct(type, att_value, (long) (att_usetime * Timer.ONE_SECOND));
				if (addAtt.roleAttType == null) {
					throw new Exception("属性类型不存在，att_type=" + att_type);
				}
				descbuffer.append(type.getExtName()).append(" x ").append(att_value).append('\n');
			}

			if (money_type > 0 && money_value > 0) {
				addMoney = new KCurrencyCountStruct(KCurrencyTypeEnum.getEnum(money_type), money_value);
				if (addMoney.currencyType == null) {
					throw new Exception("货币类型不存在，money_type=" + money_type);
				}
				descbuffer.append(addMoney.currencyType.extName).append(" x ").append(addMoney.currencyCount).append('\n');
			}

			if (addAtt == null && addMoney == null) {
				throw new Exception("没有有效数值");
			}

			if (descbuffer.length() > 0) {
				String desc = super.getDesc();
				desc = desc.contains("{}") ? desc : ItemTips.使用后可获得 + '\n' + "{}";
				desc = StringUtil.format(desc, descbuffer.deleteCharAt(descbuffer.length() - 1));
				super.setDesc(desc);
			}
		}
	}

	/**
	 * <pre>
	 * 固定宝箱类道具模板
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-2 下午6:23:58
	 * </pre>
	 */
	public static class KItemTempFixedBox extends KItemTempAbs {
		// ----------以下是EXCEL表格直导数据---------
		private int money_type;// 增加货币
		private int money_value;// 货币数量
		private String[] att_value;// 增加道具
		private String[] att_fashion;// 增加时装

		// ----------以下是逻辑数据---------
		public List<KCurrencyCountStruct> addMoneys = Collections.emptyList();
		private boolean isContainEquiBoxItem;// 如果物品里包括装备包，则直接开启装备包得到随机装备
		public List<ItemCountStruct> addItems = Collections.emptyList();
		// <时装模板,数量>
		public Map<KFashionTemplate, Integer> addFashionTempMap = Collections.emptyMap();
		// 如果一个时装有N数量，则会在以下List中插入N次同一个模板对象ID
		public List<Integer> addFashionTempList = Collections.emptyList();

		List<ItemCountStruct> getAddItems(KJobTypeEnum job, int lv) {
			if (!isContainEquiBoxItem) {
				return addItems;
			}

			// 如果是1个装备包，则直接随机出装备
			List<ItemCountStruct> newList = new ArrayList<ItemCountStruct>();
			for (ItemCountStruct itemStruct : addItems) {
				if (itemStruct.itemCount == 1) {
					KItemTempAbs temp = itemStruct.getItemTemplate();
					if (temp.ItemType == KItemTypeEnum.装备包) {
						newList.add(((KItemTempEquiBox) temp).random(job, lv));
						continue;
					}
				}
				newList.add(itemStruct);
			}

			return newList;
		}

		/**
		 * <pre>
		 * 
		 * 
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2014-2-18 下午6:01:47
		 * </pre>
		 */
		void onGameWorldInitComplete() throws Exception {
			super.onGameWorldInitComplete();

			StringBuffer descbuffer = new StringBuffer();
			if (money_type > 0 && money_value > 0) {
				if (KCurrencyTypeEnum.getEnum(money_type) == null) {
					throw new Exception("货币类型错误=" + money_type + " id=" + id);
				}
				KCurrencyCountStruct addMoney = new KCurrencyCountStruct(KCurrencyTypeEnum.getEnum(money_type), money_value);
				addMoneys = Arrays.asList(addMoney);
				descbuffer.append(addMoney.currencyType.extName).append(" x ").append(addMoney.currencyCount).append('\n');
			}

			// 310001*1,310002*1
			if (att_value.length > 0) {
				addItems = new ArrayList<ItemCountStruct>();
				ItemCountStruct.paramsItems(att_value, addItems, 1);
				for (ItemCountStruct temp : addItems) {
					descbuffer.append(temp.getItemTemplate().extItemName).append(" x ").append(temp.itemCount).append('\n');
					if (temp.getItemTemplate().ItemType == KItemTypeEnum.装备包) {
						isContainEquiBoxItem = true;
						// System.err.println(itemCode);
					}
				}
			}

			if (att_fashion.length > 0) {
				addFashionTempMap = new HashMap<KFashionTemplate, Integer>();
				addFashionTempList = new ArrayList<Integer>();
				for (String temp : att_fashion) {
					String[] temps = temp.split("\\*");
					int tempId = Integer.parseInt(temps[0]);
					int count = Integer.parseInt(temps[1]);

					KFashionTemplate addFashionTemp = KSupportFactory.getFashionModuleSupport().getFashionTemplate(tempId);
					if (addFashionTemp == null) {
						throw new Exception("时装模板错误=" + tempId + " id=" + id);
					}

					if (count < 1) {
						throw new Exception("时装数量错误=" + count + " id=" + id);
					}

					addFashionTempMap.put(addFashionTemp, count);
					for (int i = 0; i < count; i++) {
						addFashionTempList.add(tempId);
					}
					
					if(addFashionTemp.jobEnum==null){
						descbuffer.append(addFashionTemp.extName).append(" x ").append(count).append('\n');
					} else {
						descbuffer.append(addFashionTemp.extName).append(" x ").append(count).append('(').append(addFashionTemp.jobEnum.getJobName()).append(')').append('\n');
					}
				}
			}

			if (addMoneys.isEmpty() && addItems.isEmpty() && addFashionTempList.isEmpty()) {
				throw new Exception("宝箱内容无效 id=" + id);
			}

			if (descbuffer.length() > 0) {
				String desc = super.getDesc();
				desc = desc.contains("{}") ? desc : ItemTips.开启可获得 + '\n' + "{}";
				desc = StringUtil.format(desc, descbuffer.deleteCharAt(descbuffer.length() - 1));
				super.setDesc(desc);
			}
			// CTODO 其它约束检查
		}
	}

	/**
	 * <pre>
	 * 随机宝箱类道具模板
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-2 下午6:23:58
	 * </pre>
	 */
	public static class KItemTempRandomBox extends KItemTempAbs {
		// ----------以下是EXCEL表格直导数据---------
		private String[] openmoney;// 货币类型数量及权重
		private String[] opentiem;// 物品数量及权重
		private String[] petopentiem;// 宠物数量及权重
		private String[] openMount;// 机甲及权重

		// ----------以下是逻辑数据---------
		private List<KCurrencyCountStruct> addMoneys = Collections.emptyList();
		private List<Integer> addMoneyRates = Collections.emptyList();
		private List<ItemCountStruct> addItems = Collections.emptyList();
		private List<Integer> addItemRates = Collections.emptyList();
		public int MinEmptyItemBag;//开启此宝箱时，要求至少有多少个背包格
		public List<Integer> addPets = Collections.emptyList();
		private List<Integer> addPetRates = Collections.emptyList();
		private List<Integer> addMounts = Collections.emptyList();
		private List<Integer> addMountRates = Collections.emptyList();
		private int allRate = 0;

		Object random(KJobTypeEnum job, int lv) {
			Object obj = randomIn();
			if (obj == null) {
				return obj;
			}

			// 如果是1个装备包，则直接随机出装备
			if (obj instanceof ItemCountStruct) {
				ItemCountStruct itemStruct = (ItemCountStruct) obj;
				if (itemStruct.itemCount == 1) {
					KItemTempAbs temp = itemStruct.getItemTemplate();
					if (temp.ItemType == KItemTypeEnum.装备包) {
						return ((KItemTempEquiBox) temp).random(job, lv);
					}
				}
			}

			return obj;
		}
		
		/**
		 * <pre>
		 * 是否只产出宠物
		 * 
		 * @deprecated 
		 * @return
		 * @author CamusHuang
		 * @creation 2015-1-22 上午11:52:04
		 * </pre>
		 */
		public boolean isPetBox(){
			if(addMoneys.isEmpty() && addItems.isEmpty() && addMounts.isEmpty() && !addPets.isEmpty()){
				return true;
			}
			return false;
		}
		
		public List<Integer> getPets(){
			return addPets;
		}

		private Object randomIn() {
			int rate = UtilTool.random(1, allRate);
			for (int index = 0; index < addMoneyRates.size(); index++) {
				int tempRate = addMoneyRates.get(index);
				rate -= tempRate;
				if (rate < 1) {
					return addMoneys.get(index);
				}
			}
			for (int index = 0; index < addItemRates.size(); index++) {
				int tempRate = addItemRates.get(index);
				rate -= tempRate;
				if (rate < 1) {
					return addItems.get(index);
				}
			}

			for (int index = 0; index < addPetRates.size(); index++) {
				int tempRate = addPetRates.get(index);
				rate -= tempRate;
				if (rate < 1) {
					return addPets.get(index);
				}
			}
			
			for (int index = 0; index < addMountRates.size(); index++) {
				int tempRate = addMountRates.get(index);
				rate -= tempRate;
				if (rate < 1) {
					int tempId = addMounts.get(index);
					KMountTemplate temp = KMountDataManager.mMountTemplateManager.getTemplate(tempId);
					return temp;
				}
			}

			// 绝对不会出现以下情况
			if (!addMoneys.isEmpty()) {
				return addMoneys.get(0);
			}
			if (!addItems.isEmpty()) {
				return addItems.get(0);
			}
			if (!addPets.isEmpty()) {
				return addPets.get(0);
			}
			if (!addMounts.isEmpty()) {
				int tempId = addMounts.get(0);
				KMountTemplate temp = KMountDataManager.mMountTemplateManager.getTemplate(tempId);
				return temp;
			}
			return null;
		}

		/**
		 * <pre>
		 * 
		 * 
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2014-2-18 下午6:01:47
		 * </pre>
		 */
		void onGameWorldInitComplete() throws Exception {
			super.onGameWorldInitComplete();

			StringBuffer descbuffer = new StringBuffer();
			int typeCount = 0;
			if (openmoney.length > 0) {
				addMoneys = new ArrayList<KCurrencyCountStruct>();
				addMoneyRates = new ArrayList<Integer>();
				allRate += KCurrencyCountStruct.paramsMoneys(openmoney, addMoneys, addMoneyRates, 1);

				for (KCurrencyCountStruct temp : addMoneys) {
					descbuffer.append(temp.currencyType.extName).append(" x ").append(temp.currencyCount).append('\n');
				}
				typeCount += addMoneys.size();
			}

			if (opentiem.length > 0) {
				addItems = new ArrayList<ItemCountStruct>();
				addItemRates = new ArrayList<Integer>();
				allRate += ItemCountStruct.paramsItems(opentiem, addItems, addItemRates, 1);

				for (ItemCountStruct temp : addItems) {
					descbuffer.append(temp.getItemTemplate().extItemName).append(" x ").append(temp.itemCount).append('\n');
					// if(temp.getItemTemplate().ItemType==KItemTypeEnum.装备包){
					// System.err.println(itemCode);
					// }
					if (temp.getItemTemplate().isCanStack()) {
						if (MinEmptyItemBag < 1) {
							MinEmptyItemBag = 1;
						}
					} else {
						if (MinEmptyItemBag < temp.itemCount) {
							MinEmptyItemBag = (int) temp.itemCount;
						}
					}
				}
				typeCount += addItems.size();
			}

			if (petopentiem.length > 0) {
				addPets = new ArrayList<Integer>();
				addPetRates = new ArrayList<Integer>();
				{
					if (petopentiem != null) {
						for (String petStrs : petopentiem) {
							String[] temps = petStrs.split("\\*");
							int petTempId = Integer.parseInt(temps[0]);
							KPetTemplate petTemp = KSupportFactory.getPetModuleSupport().getPetTemplate(petTempId);
							if (petTemp == null) {
								throw new KGameServerException("宠物模板不存在=" + petTempId);
							}
							int petCount = Integer.parseInt(temps[1]);
							if (petCount != 1) {
								throw new KGameServerException("宠物数量错误=" + petCount);
							}
							int rate = Integer.parseInt(temps[2]);
							if (rate < 1) {
								throw new KGameServerException("宠物权重错误=" + rate);
							}
							addPets.add(petTempId);
							addPetRates.add(rate);
							allRate += rate;

							descbuffer.append(petTemp.getNameEx()).append(" x ").append(petCount).append('\n');
						}
						typeCount += petopentiem.length;
					}
				}
			}
			
			if (openMount.length > 0) {
				addMounts = new ArrayList<Integer>();
				addMountRates = new ArrayList<Integer>();
				{
					if (openMount != null) {
						for (String mountStrs : openMount) {
							String[] temps = mountStrs.split("\\*");
							int tempId = Integer.parseInt(temps[0]);
							KMountTemplate temp = KMountDataManager.mMountTemplateManager.getTemplate(tempId);
							if (temp == null) {
								throw new KGameServerException("机甲模板不存在=" + tempId);
							}
							int count = Integer.parseInt(temps[1]);
							if (count != 1) {
								throw new KGameServerException("机甲数量错误=" + count);
							}
							int rate = Integer.parseInt(temps[2]);
							if (rate < 1) {
								throw new KGameServerException("机甲权重错误=" + rate);
							}
							addMounts.add(tempId);
							addMountRates.add(rate);
							allRate += rate;

							descbuffer.append(temp.extName).append(" x ").append(count).append('\n');
						}
						typeCount += openMount.length;
					}
				}
			}

			if (addMoneys.isEmpty() && addItems.isEmpty() && addPets.isEmpty() && addMounts.isEmpty()) {
				throw new Exception("宝箱内容无效 id=" + id);
			}

			if (descbuffer.length() > 0) {
				String desc = super.getDesc();
				{
					String randomDescTemplate = desc.contains("{}") ? desc : ItemTips.开启可随机获得 + '\n' + "{}";
					String fixDescTemplate = desc.contains("{}") ? desc : ItemTips.开启可获得 + '\n' + "{}";
					desc = typeCount == 1 ? fixDescTemplate : randomDescTemplate;
				}
				desc = StringUtil.format(desc, descbuffer.deleteCharAt(descbuffer.length() - 1));
				super.setDesc(desc);
			}
			// CTODO 其它约束检查
		}
	}

	/**
	 * <pre>
	 * 装备包类道具模板
	 * by camus @20141212 扩展后，不限于装备，可以是任意物品
	 * 按职业按权重随机开出一件物品
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-2 下午6:23:58
	 * </pre>
	 */
	public static class KItemTempEquiBox extends KItemTempAbs {
		// ----------以下是EXCEL表格直导数据---------
		private boolean SelectionOfGrades;//是否等级筛选
		private String[] soldieropentiem;// 战士物品数量及权重
		private String[] Masteropentiem;// 法师物品数量及权重
		private String[] gunopentiem;// 枪手物品数量及权重

		// ----------以下是逻辑数据---------
		public int MinEmptyItemBag;//开启此宝箱时，要求至少有多少个背包格
		//<角色职业,职业数据>
		private Map<KJobTypeEnum, ItemTempEquiBoxForJob> addItems = new HashMap<KJobTypeEnum, ItemTempEquiBoxForJob>();
		
		/**
		 * <pre>
		 * 指定职业对应的数据
		 * 
		 * @author CamusHuang
		 * @creation 2015-3-18 上午10:28:44
		 * </pre>
		 */
		static class ItemTempEquiBoxForJob{
			final KJobTypeEnum job;
			//<角色等级,等级数据>
			private Map<Integer, ItemTempEquiBoxForLv> addItems = new HashMap<Integer, ItemTempEquiBoxForLv>();
			
			ItemTempEquiBoxForJob(KJobTypeEnum job){
				this.job = job;
			}
			
			/**
			 * <pre>
			 * 指定角色等级对应的数据
			 * 
			 * @author CamusHuang
			 * @creation 2015-3-18 上午10:28:35
			 * </pre>
			 */
			static class ItemTempEquiBoxForLv{
				int allRate;
				//<几率,等级数据>
				List<ItemCountStruct> addItems;
				List<Integer> addItemRates;
			}
			
		}

		ItemCountStruct random(KJobTypeEnum job, int lv) {
			ItemTempEquiBoxForJob mItemTempEquiBoxForJob = this.addItems.get(job);
			ItemTempEquiBoxForLv mItemTempEquiBoxForLv = mItemTempEquiBoxForJob.addItems.get(lv);
			
			int rate = UtilTool.random(1, mItemTempEquiBoxForLv.allRate);
			//
			for (int index = 0; index < mItemTempEquiBoxForLv.addItemRates.size(); index++) {
				int tempRate = mItemTempEquiBoxForLv.addItemRates.get(index);
				rate -= tempRate;
				if (rate < 1) {
					return mItemTempEquiBoxForLv.addItems.get(index);
				}
			}

			// 绝对不会出现以下情况
			if (!mItemTempEquiBoxForLv.addItems.isEmpty()) {
				return mItemTempEquiBoxForLv.addItems.get(0);
			}
			return null;
		}

		/**
		 * <pre>
		 * 
		 * 
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2014-2-18 下午6:01:47
		 * </pre>
		 */
		void onGameWorldInitComplete() throws Exception {
			super.onGameWorldInitComplete();
			if (soldieropentiem.length < 1 || Masteropentiem.length < 1 || gunopentiem.length < 1) {
				throw new Exception("职业装备不齐 id=" + id);
			}
			
			if (SelectionOfGrades) {
				// 物品区分等级
				ItemTempEquiBoxForJob mItemTempEquiBoxForJob = paramsForJob(KJobTypeEnum.WARRIOR, soldieropentiem);
				this.addItems.put(KJobTypeEnum.WARRIOR, mItemTempEquiBoxForJob);
				//
				mItemTempEquiBoxForJob = paramsForJob(KJobTypeEnum.SHADOW, Masteropentiem);
				this.addItems.put(KJobTypeEnum.SHADOW, mItemTempEquiBoxForJob);
				//
				mItemTempEquiBoxForJob = paramsForJob(KJobTypeEnum.GUNMAN, gunopentiem);
				this.addItems.put(KJobTypeEnum.GUNMAN, mItemTempEquiBoxForJob);
			} else {
				// 物品不区分角色等级：即所有等级一样
				ItemTempEquiBoxForJob mItemTempEquiBoxForJob = paramsForJobIgnoreLv(KJobTypeEnum.WARRIOR, soldieropentiem);
				this.addItems.put(KJobTypeEnum.WARRIOR, mItemTempEquiBoxForJob);
				//
				mItemTempEquiBoxForJob = paramsForJobIgnoreLv(KJobTypeEnum.SHADOW, Masteropentiem);
				this.addItems.put(KJobTypeEnum.SHADOW, mItemTempEquiBoxForJob);
				//
				mItemTempEquiBoxForJob = paramsForJobIgnoreLv(KJobTypeEnum.GUNMAN, gunopentiem);
				this.addItems.put(KJobTypeEnum.GUNMAN, mItemTempEquiBoxForJob);
			}

//			{
//				// 生成描述
//				StringBuffer descbuffer = new StringBuffer();
//				// 某职业的所有装备
//				List<ItemCountStruct> addItems = this.addItems.get(KJobTypeEnum.SHADOW);
//				for (ItemCountStruct temp : addItems) {
//					if (temp.getItemTemplate().ItemType == KItemTypeEnum.装备) {
//						KItemTempEqui equiTemp = (KItemTempEqui) temp.getItemTemplate();
//						String str = HyperTextTool.extColor(equiTemp.typeEnum.name, equiTemp.ItemQuality.color);
//						descbuffer.append(temp.getItemTemplate().lvl).append(ItemTips.级).append(str).append(" x ").append(temp.itemCount).append('\n');
//					}
//				}
//				// 所有职业的非装备
//				for (KJobTypeEnum job : KJobTypeEnum.values()) {
//					addItems = this.addItems.get(job);
//					for (ItemCountStruct temp : addItems) {
//						if (temp.getItemTemplate().ItemType != KItemTypeEnum.装备) {
//							descbuffer.append(temp.getItemTemplate().extItemName).append(" x ").append(temp.itemCount).append('(').append(job.getJobName()).append(')').append('\n');
//						}
//					}
//				}
//				//
//				if (descbuffer.length() > 0) {
//					String desc = super.getDesc();
//					{
//						String randomDescTemplate = desc.contains("{}") ? desc : ItemTips.开启可随机获得 + '\n' + "{}";
//						String fixDescTemplate = desc.contains("{}") ? desc : ItemTips.开启可获得 + '\n' + "{}";
//						desc = addItems.size() == 1 ? fixDescTemplate : randomDescTemplate;
//					}
//					desc = StringUtil.format(desc, descbuffer.deleteCharAt(descbuffer.length() - 1));
//					super.setDesc(desc);
//				}
//			}
			// CTODO 其它约束检查
		}
		
		private ItemTempEquiBoxForJob paramsForJobIgnoreLv(KJobTypeEnum job, String[] itemStrs) throws Exception {
			//
			ItemTempEquiBoxForJob mItemTempEquiBoxForJob = new ItemTempEquiBoxForJob(job);
			//
			{
				ItemTempEquiBoxForLv mItemTempEquiBoxForLv = newItemTempEquiBoxForLv(job, itemStrs);

				int MaxRoleLv = KRoleModuleConfig.getRoleMaxLv();
				for (int l = 1; l <= MaxRoleLv; l++) {
					mItemTempEquiBoxForJob.addItems.put(l, mItemTempEquiBoxForLv);
				}
			}

			return mItemTempEquiBoxForJob;
		}

		private ItemTempEquiBoxForJob paramsForJob(KJobTypeEnum job, String[] itemStrs) throws Exception {
			{
				// 还原String，然后按等级规则重新分析
				StringBuffer sbf = new StringBuffer();
				for (String a : itemStrs) {
					sbf.append(a);
				}
				String itemStr = sbf.toString();
				//
				itemStrs = itemStr.split(";");
			}

			ItemTempEquiBoxForJob mItemTempEquiBoxForJob = new ItemTempEquiBoxForJob(job);
			for (String tempStr : itemStrs) {
				// 分析等级范围 (1-19)311081*1*100,311082*1*100
				String[] lvStrs = tempStr.split("\\)")[0].substring(1).split("-");
				int minLv = Integer.parseInt(lvStrs[0]);
				int maxLv = Integer.parseInt(lvStrs[1]);
				//
				ItemTempEquiBoxForLv mItemTempEquiBoxForLv = newItemTempEquiBoxForLv(job, tempStr.split("\\)")[1].split(","));
				//
				for (int l = minLv; l <= maxLv; l++) {
					mItemTempEquiBoxForJob.addItems.put(l, mItemTempEquiBoxForLv);
				}
			}

			int MaxRoleLv = KRoleModuleConfig.getRoleMaxLv();
			for (int l = 1; l <= MaxRoleLv; l++) {
				if (!mItemTempEquiBoxForJob.addItems.containsKey(l)) {
					throw new Exception("缺少等级奖励 lv=" + l + " id=" + id + " 职业=" + job.getJobName());
				}
			}
			//
			return mItemTempEquiBoxForJob;
		}

		private ItemTempEquiBoxForLv newItemTempEquiBoxForLv(KJobTypeEnum job, String[] tempStr) throws Exception {
			List<ItemCountStruct> addItems = new ArrayList<ItemCountStruct>();
			List<Integer> addItemRates = new ArrayList<Integer>();
			int allRate = ItemCountStruct.paramsItems(tempStr, addItems, addItemRates, 1);
			//
			if (addItems.isEmpty()) {
				throw new Exception("宝箱内容无效 id=" + id + " 职业=" + job.getJobName());
			}

			// //by camus @20141212 扩展后，不限于装备，可以是任意物品
			// for (ItemCountStruct temp : addItems) {
			// if (temp.getItemTemplate().ItemType != KItemTypeEnum.装备) {
			// throw new Exception("宝箱内容必须为装备 id=" + id + " 职业=" +
			// job.getJobName());
			// }
			// }
			//
			
			for (ItemCountStruct temp : addItems) {
				if (temp.getItemTemplate().ItemType == KItemTypeEnum.装备) {
					if (temp.itemCount != 1) {
						throw new Exception("装备宝箱开出的装备数量必须为1 id=" + id + " 职业=" + job.getJobName());
					}
				}
				
				if (temp.getItemTemplate().isCanStack()) {
					if (MinEmptyItemBag < 1) {
						MinEmptyItemBag = 1;
					}
				} else {
					if (MinEmptyItemBag < temp.itemCount) {
						MinEmptyItemBag = (int) temp.itemCount;
					}
				}
			}

			//
			ItemTempEquiBoxForLv mItemTempEquiBoxForLv = new ItemTempEquiBoxForLv();
			mItemTempEquiBoxForLv.allRate = allRate;
			mItemTempEquiBoxForLv.addItems = addItems;
			mItemTempEquiBoxForLv.addItemRates = addItemRates;
			//
			return mItemTempEquiBoxForLv;
		}
	}

	/**
	 * <pre>
	 * 装备强化系数
	 * 
	 * @author CamusHuang
	 * @creation 2014-3-17 下午3:17:12
	 * </pre>
	 */
	public static class KEquiStrongPriceParam {
		// ----------以下是EXCEL表格直导数据---------
		int part;// 穿戴部位
		public float Parameter;// 对应部位参数
		// ----------以下是逻辑数据---------
		public KEquipmentTypeEnum equiType;

		void onGameWorldInitComplete() throws Exception {
			equiType = KEquipmentTypeEnum.getEnum(part);

			if (equiType == null) {
				throw new Exception("定义了不存在的装备部位 = " + part);
			}

			if (Parameter <= 0) {
				throw new Exception("系数错误  部位= " + part);
			}

			// CTODO 其它约束检查
		}
	}

	/**
	 * <pre>
	 * 镶嵌孔购买价格
	 * 
	 * @author CamusHuang
	 * @creation 2014-12-4 下午3:58:59
	 * </pre>
	 */
	public static class KEquiBuyEnchansePrice {
		// ----------以下是EXCEL表格直导数据---------
		public int HoleID;// 孔序号

		public ItemCountStruct payItem;// 必须有
		public KCurrencyCountStruct payMoney;// 可有可无

		// ----------以下是逻辑数据---------

		void onGameWorldInitComplete() throws Exception {

			if (payItem == null) {
				throw new Exception("没有有效的支付物品");
			}

			if (payItem.itemCount < 1 || payItem.getItemTemplate() == null) {
				throw new Exception("支付物品错误");
			}

			if (payMoney != null && payMoney.currencyCount < 1) {
				throw new Exception("支付价格错误");
			}

			if (HoleID < 1) {
				throw new Exception("孔号错误 = " + HoleID);
			}

			// CTODO 其它约束检查
		}
	}

	/**
	 * <pre>
	 * 强化等级属性比例
	 * 
	 * @author CamusHuang
	 * @creation 2014-10-27 下午6:14:17
	 * </pre>
	 */
	public static class KEquiStrongAttExtData {
		// ----------以下是EXCEL表格直导数据---------
		int lvl;// 强化等级
		public int StrongLVRate;// 强化等级属性比例

		// ----------以下是逻辑数据---------

		void onGameWorldInitComplete() throws Exception {
			if (lvl <= 0) {
				throw new Exception("lvl 错误= " + lvl);
			}

			if (StrongLVRate <= 0) {
				throw new Exception("StrongLVRate 错误= " + StrongLVRate);
			}
			// CTODO 其它约束检查
		}
	}

	/**
	 * <pre>
	 * 装备升星材料数据
	 * 
	 * @author CamusHuang
	 * @creation 2014-3-17 下午3:17:12
	 * </pre>
	 */
	public static class KEquiStarMaterialData {
		// ----------以下是EXCEL表格直导数据---------
		public int starLV;// 材料星阶
		public String itemId;// 材料ID

		// ----------以下是逻辑数据---------

		void onGameWorldInitComplete() throws Exception {
			KItemTempAbs temp = KItemDataManager.mItemTemplateManager.getItemTemplate(itemId);
			if (temp == null) {
				throw new Exception("定义了不存在的材料ID = " + itemId);
			}
			if (temp.type != KItemTypeEnum.改造材料.sign) {
				throw new Exception("定义了非材料道具ID = " + itemId);
			}
			// CTODO 其它约束检查
		}
	}

	/**
	 * <pre>
	 * 装备升星数据
	 * 
	 * @author CamusHuang
	 * @creation 2014-3-17 下午3:17:12
	 * </pre>
	 */
	public static class KEquiStarRateData {
		// ----------以下是EXCEL表格直导数据---------
		public int starLV;// 升星等级
		private int stargold;// 升星金币
		public int materialCount;// 对应消耗数量
		private int SuccessRate;// 成功率
		public int protectedCount;//保护次数
		// ----------以下是逻辑数据---------
		public KCurrencyCountStruct payMoney;
		
		
		public int getSuccessRate(){
			// 限时活动：装备升星成功率
			float limitRate = 1;
			{
				TimeLimieProduceActivity activity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.装备升星成功率);
				if (activity != null && activity.isActivityTakeEffectNow()) {
					int starSetLv = KItemLogic.ExpressionForTopStarLv(starLV-1);
					if (activity.activity21_starLvSet.contains(starSetLv)) {
						limitRate = activity.probabilityRatio;
					}
				}
			}
			
			return  Math.min(10000, (int)(SuccessRate * limitRate));
		}

		void onGameWorldInitComplete() throws Exception {
			if (materialCount < 1) {
				throw new Exception("材料消耗数量 = " + materialCount);
			}
			if (stargold < 1) {
				throw new Exception("金币消耗数量 = " + stargold);
			}
			payMoney = new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD, stargold);
			//
			if (protectedCount < 1) {
				throw new Exception("金币消耗数量 = " + stargold);
			}
			// CTODO 其它约束检查
		}
	}

	/**
	 * <pre>
	 * 属性比例参数
	 * 
	 * @author CamusHuang
	 * @creation 2014-10-27 下午6:17:39
	 * </pre>
	 */
	public static class KEquiAttExtData {
		// ----------以下是EXCEL表格直导数据---------
		int attribute_id;// 属性ID
		public float StrongAttributeRate;// 属性强化比例
		public float StarAttributeRate;// 属性升星比例

		// ----------以下是逻辑数据---------
		public KGameAttrType attTypeEnum;

		void onGameWorldInitComplete() throws Exception {

			if ((attTypeEnum = KGameAttrType.getAttrTypeEnum(attribute_id)) == null) {
				throw new Exception("attribute_id = " + attribute_id);
			}
			if (StrongAttributeRate < 0) {
				throw new Exception("StrongAttributeRate = " + StrongAttributeRate);
			}
			if (StarAttributeRate < 0) {
				throw new Exception("StarAttributeRate = " + StarAttributeRate);
			}
			// CTODO 其它约束检查
		}
	}

	/**
	 * <pre>
	 * 装备升星等级加成数据
	 * 
	 * @author CamusHuang
	 * @creation 2014-3-17 下午3:17:12
	 * </pre>
	 */
	public static class KEquiStarAttExtData {
		// ----------以下是EXCEL表格直导数据---------
		public int lvl;// 升星等级
		public int StarLVRate;// 升星等级属性比例

		// ----------以下是逻辑数据---------

		void onGameWorldInitComplete() throws Exception {
			if (lvl < 0) {
				throw new Exception("starLV 错误 = " + lvl);
			}
			if (StarLVRate < 0) {
				throw new Exception("StarLVRate 错误 = " + StarLVRate);
			}
			// CTODO 其它约束检查
		}
	}

	/**
	 * <pre>
	 * 装备继承数据
	 * 
	 * @author CamusHuang
	 * @creation 2014-3-17 下午3:17:12
	 * </pre>
	 */
	public static class KEquiInheritData {
		// ----------以下是EXCEL表格直导数据---------
		public int Quality;// 目标装备品质
		public int Targetequipment;// 目标装备等级
		private int Basisgold;// 继承金币消耗

		// ----------以下是逻辑数据---------
		public KCurrencyCountStruct commonGold;

		void onGameWorldInitComplete() throws Exception {
			if (KItemQualityEnum.getEnum(Quality) == null) {
				throw new Exception("定义了不存在的品质 = " + Quality);
			}

			if (Basisgold <= 0) {
				throw new Exception("数据错误 Basisgold = " + Basisgold);
			}
			commonGold = new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD, Basisgold);
			// CTODO 其它约束检查
		}
	}

	public static class KEquiStarSetData {
		// ----------以下是EXCEL表格直导数据---------
		private int id;// 套装ID
		public String name;// 套装名称
		public int Suitcondition;// 升星等级
		private int[] attribute;// 附加属性
		private int[] attributeCount;// 附加属性1值
		public int percent;// 装备基础属性万分比
		public int mapResId;// 地图特效资源

		// ----------以下是逻辑数据---------
		public Map<KGameAttrType, Integer> allEffects = Collections.emptyMap();// LinkedHashMap

		void onGameWorldInitComplete() throws Exception {
			{
				if (attribute.length != attributeCount.length || attribute.length < 1) {
					throw new Exception("属性加成未配置 id=" + id);
				}
				Map<KGameAttrType, Integer> tempEffects = new LinkedHashMap<KGameAttrType, Integer>();
				for (int index = 0; index < attribute.length; index++) {
					if (attributeCount[index] < 1) {
						continue;
					}
					KGameAttrType type = KGameAttrType.getAttrTypeEnum(attribute[index]);
					if (type == null) {
						throw new Exception("属性加成类型不存在 type=" + attribute[index] + " id=" + id);
					}
					tempEffects.put(type, attributeCount[index]);
				}

				if (tempEffects.isEmpty()) {
					throw new Exception("属性加成未配置 id=" + id);
				}
				allEffects = Collections.unmodifiableMap(tempEffects);
			}
			{
				if (percent < 0 || percent > 20000) {
					throw new Exception("属性万分比增量错误 id=" + id);
				}
			}
			// CTODO 其它约束检查
		}
	}

	public static class KEquiStoneSetDataOld {
		// ----------以下是EXCEL表格直导数据---------
		private int id;// 套装ID
		public String name;// 套装名称
		public int Suitcondition;// 宝石等级
		private int[] attribute1_id;// 附加属性1
		private int[] attribute1Count;// 附加属性1值
		public int mapResId;// 地图特效资源

		// ----------以下是逻辑数据---------
		public Map<KGameAttrType, Integer> allEffects = Collections.emptyMap();// LinkedHashMap

		void onGameWorldInitComplete() throws Exception {
			{
				if (attribute1_id.length != attribute1Count.length || attribute1_id.length < 1) {
					throw new Exception("属性加成未配置 id=" + id);
				}
				Map<KGameAttrType, Integer> tempEffects = new LinkedHashMap<KGameAttrType, Integer>();
				for (int index = 0; index < attribute1_id.length; index++) {
					if (attribute1Count[index] < 1) {
						continue;
					}
					KGameAttrType type = KGameAttrType.getAttrTypeEnum(attribute1_id[index]);
					if (type == null) {
						throw new Exception("属性加成类型不存在 type=" + attribute1_id[index] + " id=" + id);
					}
					tempEffects.put(type, attribute1Count[index]);
				}

				if (tempEffects.isEmpty()) {
					throw new Exception("属性加成未配置 id=" + id);
				}
				allEffects = Collections.unmodifiableMap(tempEffects);
			}
			// CTODO 其它约束检查
		}
	}

	public static class KEquiStoneSetData2 {
		// ----------以下是EXCEL表格直导数据---------
		public int Suitcondition;// 宝石等级
		public int resStoneNum;// 激活光效宝石数量
		public int mapResId;// 地图特效资源

		// ----------以下是逻辑数据---------

		void onGameWorldInitComplete() throws Exception {

			if (resStoneNum > KItemConfig.getInstance().TotalMaxEnchansePosition) {
				throw new Exception("数据错误 resStoneNum=" + resStoneNum);
			}

			// CTODO 其它约束检查
		}
	}

	public static class KEquiStrongSetData {
		// ----------以下是EXCEL表格直导数据---------
		int id;// 套装ID
		public String name;// 套装名称
		public int Suitcondition;// 强化等级
		private int[] attribute1_id;// 附加属性1
		private int[] attribute1Count;// 附加属性1值
		public int mapResId;// 地图特效资源

		// ----------以下是逻辑数据---------
		public Map<KGameAttrType, Integer> allEffects = Collections.emptyMap();// LinkedHashMap

		void onGameWorldInitComplete() throws Exception {
			{
				if (attribute1_id.length != attribute1Count.length || attribute1_id.length < 1) {
					throw new Exception("属性加成未配置 id=" + id);
				}
				Map<KGameAttrType, Integer> tempEffects = new LinkedHashMap<KGameAttrType, Integer>();
				for (int index = 0; index < attribute1_id.length; index++) {
					if (attribute1Count[index] < 1) {
						continue;
					}
					KGameAttrType type = KGameAttrType.getAttrTypeEnum(attribute1_id[index]);
					if (type == null) {
						throw new Exception("属性加成类型不存在 type=" + attribute1_id[index] + " id=" + id);
					}
					tempEffects.put(type, attribute1Count[index]);
				}

				if (tempEffects.isEmpty()) {
					throw new Exception("属性加成未配置 id=" + id);
				}
				allEffects = Collections.unmodifiableMap(tempEffects);
			}
			// CTODO 其它约束检查
		}
	}

	public static class KEquiQualitySetData implements Comparable<KEquiQualitySetData> {
		// ----------以下是EXCEL表格直导数据---------
		int id;// 套装ID
		public String name;// 套装名称
		public int lv;// 装备等级
		public int qua;// 品质
		public int Number;// 装备数量
		private int[] attribute1_id;// 附加属性1
		private int[] attribute1Count;// 附加属性1值
		public int mapResId;// 地图特效资源

		// ----------以下是逻辑数据---------
		KItemQualityEnum quaEnum;
		public Map<KGameAttrType, Integer> allEffects = Collections.emptyMap();// LinkedHashMap
		// public String desc;//3件:攻击+99、命中+99...

		void onGameWorldInitComplete() throws Exception {
			quaEnum = KItemQualityEnum.getEnum(qua);
			if (quaEnum == null) {
				throw new Exception("品质不存在 qua=" + qua);
			}

			if (!KItemDataManager.mItemTemplateManager.containEquiLv(lv)) {
				throw new Exception("装备等级不存在 lv=" + lv);
			}

			{
				if (attribute1_id.length != attribute1Count.length || attribute1_id.length < 1) {
					throw new Exception("属性加成数量 id=" + id);
				}

				// StringBuffer sbf=new StringBuffer();
				// sbf.append(Number).append("件：");
				Map<KGameAttrType, Integer> tempEffects = new LinkedHashMap<KGameAttrType, Integer>();
				for (int index = 0; index < attribute1_id.length; index++) {
					if (attribute1Count[index] < 1) {
						continue;
					}
					KGameAttrType type = KGameAttrType.getAttrTypeEnum(attribute1_id[index]);
					if (type == null) {
						throw new Exception("属性加成类型不存在 type=" + attribute1_id[index] + " id=" + id);
					}
					tempEffects.put(type, attribute1Count[index]);
					// if(index!=0){
					// sbf.append("、");
					// }
					// sbf.append(type.getExtName()).append("+").append(attribute1Count[index]);
				}

				// desc = sbf.toString();

				if (tempEffects.isEmpty()) {
					throw new Exception("属性加成未配置 id=" + id);
				}
				allEffects = tempEffects;
			}
			// CTODO 其它约束检查
		}

		@Override
		public int compareTo(KEquiQualitySetData o) {
			if (lv < o.lv) {
				return -1;
			}
			if (lv > o.lv) {
				return 1;
			}
			if (qua < o.qua) {
				return -1;
			}
			if (qua > o.qua) {
				return 1;
			}
			if (Number < o.Number) {
				return -1;
			}
			if (Number > o.Number) {
				return 1;
			}
			return 0;
		}
	}
}
