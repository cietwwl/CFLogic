package com.kola.kmp.logic.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.koala.game.exception.KGameServerException;
import com.kola.kmp.logic.item.KItemDataStructs.KBagExtData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiAttExtData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiBuyEnchansePrice;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiInheritData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiQualitySetData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStarAttExtData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStarMaterialData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStarRateData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStarSetData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStoneSetData2;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStoneSetDataOld;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStrongAttExtData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStrongPriceParam;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStrongSetData;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempConsume;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempEqui;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempEqui.EquiProduceData;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempEquiBox;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempFixedBox;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempMaterial;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempRandomBox;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempStone;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KEquipmentTypeEnum;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KItemQualityEnum;
import com.kola.kmp.logic.other.KItemTypeEnum;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.role.KRoleModuleConfig;
import com.kola.kmp.logic.util.DefaultRoleMapResInfoManager;

public class KItemDataManager {
	/** 背包扩容数据管理器 */
	public final static BagExtDataManager mBagExtDataManager = new BagExtDataManager();
	/** 所有道具模板数据 */
	public final static ItemTemplateManager mItemTemplateManager = new ItemTemplateManager();
	/** 装备强化价格系数管理器 */
	public final static EquiStrongPriceParamManager mEquiStrongPriceParamManager = new EquiStrongPriceParamManager();
	/** 强化等级属性比例数据管理器 */
	public final static EquiStrongAttExtDataManager mEquiStrongAttExtDataManager = new EquiStrongAttExtDataManager();
	/** 装备升星材料管理器 */
	final static EquiStarMetrialDataManager mEquiStarMetrialDataManager = new EquiStarMetrialDataManager();
	/** 装备升星成功率管理器 */
	public final static EquiStarRateManager mEquiStarRateManager = new EquiStarRateManager();
	/** 属性比例参数管理器 */
	public final static EquiAttExtDataManager mEquiAttExtDataManager = new EquiAttExtDataManager();
	/** 升星等级属性比例管理器 */
	public final static EquiStarAttExtManager mEquiStarAttExtManager = new EquiStarAttExtManager();
	/** 装备继承数据管理器 */
	public final static EquiInheritDataManager mEquiInheritDataManager = new EquiInheritDataManager();
	/** 装备宝石套装数据管理器 */
	private final static EquiStoneSetDataManagerOld mEquiStoneSetDataManagerOld = new EquiStoneSetDataManagerOld();
	/** 装备宝石套装数据管理器 */
	public final static EquiStoneSetDataManager2 mEquiStoneSetDataManager2 = new EquiStoneSetDataManager2();
	/** 装备升星套装数据管理器 */
	public final static EquiStarSetDataManager mEquiStarSetDataManager = new EquiStarSetDataManager();
	/** 装备强化套装数据管理器 */
	public final static EquiStrongSetDataManager mEquiStrongSetDataManager = new EquiStrongSetDataManager();
	/** 装备品质套装数据管理器 */
	public final static EquiQualitySetDataManager mEquiQualitySetDataManager = new EquiQualitySetDataManager();
	/** 装备镶嵌孔数据管理器 */
	public final static EquiEnchanseDataManager mEquiEnchanseDataManager = new EquiEnchanseDataManager();
	/** 装备镶嵌孔购买数据管理器 */
	public final static EquiBuyEnchanseDataManager mEquiBuyEnchanseDataManager = new EquiBuyEnchanseDataManager();

	// /////////////////////////////
	/**
	 * <pre>
	 * 装备镶嵌孔数据管理器
	 * 写死的数据，不导表
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-7 下午3:33:35
	 * </pre>
	 */
	static class EquiEnchanseDataManager {
		// 孔位-品质
		Map<Integer, KItemQualityEnum> dataMapA = new HashMap<Integer, KItemQualityEnum>();
		// 品质-孔位
		Map<KItemQualityEnum, Integer> dataMapB = new HashMap<KItemQualityEnum, Integer>();

		EquiEnchanseDataManager() {
			// 写死数据，数量与品质对应
			for (int index = 0; index <= KItemConfig.getInstance().MaxEnchansePositionPerOne; index++) {
				dataMapA.put(index, KItemQualityEnum.getEnum(index + 1));
			}

			for (Entry<Integer, KItemQualityEnum> e : dataMapA.entrySet()) {
				dataMapB.put(e.getValue(), e.getKey());
			}
		}

		int getNum(KItemQualityEnum type) {
			// return 4;
			return dataMapB.get(type);
		}

		KItemQualityEnum getType(int num) {
			// return KItemQualityEnum.优秀的;
			return dataMapA.get(num);
		}

		void onGameWorldInitComplete() throws Exception {
			for (KItemQualityEnum enuma : KItemQualityEnum.values()) {
				if (!dataMapB.containsKey(enuma)) {
					throw new Exception("装备品质与镶嵌孙数对应错误，没有一一对应");
				}
			}
		}
	}

	// /////////////////////////////
	/**
	 * <pre>
	 * 背包扩容数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-7 下午3:33:35
	 * </pre>
	 */
	static class BagExtDataManager {
		private List<KBagExtData> dataList = new ArrayList<KBagExtData>();
		/**
		 * <pre>
		 * KEY = 格子编号
		 * </pre>
		 */
		private Map<Integer, KBagExtData> dataMap = new HashMap<Integer, KBagExtData>();

		private int freeGridCount;// 免费的格子数量
		private int maxGridCount;// 格子最大数量

		// 免费时的价格表示
		public final KCurrencyCountStruct freeMoney = new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD, 0);

		private BagExtDataManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param itemTemplate
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2012-11-12 下午1:28:11
		 * </pre>
		 */
		void initDatas(List<KBagExtData> datas) throws Exception {
			dataList.addAll(datas);
			//
			for (KBagExtData data : datas) {
				if (dataMap.put(data.id, data) != null) {
					throw new Exception("重复的小格ID=" + data.id);
				}
				if (data.needopen != 1) {
					if (data.id > freeGridCount) {
						freeGridCount = data.id;
					}
				}

				if (data.id > maxGridCount) {
					maxGridCount = data.id;
				}
			}
		}

		public KBagExtData getData(int gridId) {
			return dataMap.get(gridId);
		}

		int getFreeGridCount() {
			return freeGridCount;
		}

		int getMaxGridCount() {
			return maxGridCount;
		}

		void onGameWorldInitComplete() throws Exception {
			// CTODO

			for (KBagExtData data : dataList) {
				data.onGameWorldInitComplete();
			}
		}

		public Map<KGameAttrType, AtomicInteger> getBagExtEffect(int volume, Map<KGameAttrType, AtomicInteger> result) {
			return getBagExtEffect(0, volume, result);
		}

		public Map<KGameAttrType, AtomicInteger> getBagExtEffect(int startVolume, int nowVolume, Map<KGameAttrType, AtomicInteger> result) {
			if (result == null) {
				result = new HashMap<KGameAttrType, AtomicInteger>();
			}
			if (nowVolume <= freeGridCount) {
				return result;
			}

			startVolume = Math.max(startVolume, freeGridCount);

			for (int vol = startVolume + 1; vol <= nowVolume; vol++) {
				KBagExtData data = dataMap.get(vol);
				if (data != null && data.attValue != null) {
					AtomicInteger oldValue = result.get(data.attValue.roleAttType);
					if (oldValue == null) {
						oldValue = new AtomicInteger();
						result.put(data.attValue.roleAttType, oldValue);
					}
					oldValue.addAndGet(data.attValue.addValue);
				}
			}

			return result;
		}
	}

	// /////////////////////////////
	/**
	 * <pre>
	 * 道具模板数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-7 下午6:26:36
	 * </pre>
	 */
	public static class ItemTemplateManager {
		/**
		 * <pre>
		 * 全部道具模板
		 * 主要用于使用ItemCode查询模板数据，提高性能
		 * KEY = ItemCode
		 * </pre>
		 */
		private Map<String, KItemTempAbs> itemTemplateMap = new HashMap<String, KItemTempAbs>();
		/**
		 * <pre>
		 * 全部道具模板
		 * 主要用于遍历，提高性能
		 * </pre>
		 */
		private List<KItemTempAbs> itemTemplateList = new ArrayList<KItemTempAbs>();

		/**
		 * <pre>
		 * 创建角色时，初始给角色的装备
		 * 在装备表中，bBornEquip==1，且装备职业归属等于自己职业的装备，作为初始化装备，直接穿戴在玩家身上
		 * </pre>
		 */
		private Map<KJobTypeEnum, Map<KEquipmentTypeEnum, KItemTempEqui>> newRoleEquiTemplateMap = new HashMap<KJobTypeEnum, Map<KEquipmentTypeEnum, KItemTempEqui>>();

		/**
		 * 所有装备的等级
		 */
		private Set<Integer> allEquiTempLvs = new HashSet<Integer>();
		/**
		 * <角色等级,能安装的装备最高等级>
		 */
		private Map<Integer, Integer> matchEquiTempLvs = new HashMap<Integer, Integer>();

		private ItemTemplateManager() {
		}

		/**
		 * <pre>
		 * 检查角色升级是否需要更新装备数据
		 * 主要是更高级装备的关卡引导数据
		 * 
		 * @param orgLv
		 * @param nowLv
		 * @return
		 * @author CamusHuang
		 * @creation 2014-7-20 下午7:38:34
		 * </pre>
		 */
		public boolean checkSenstiveLv(int orgLv, int nowLv) {
			for (int lv = orgLv + 1; lv <= nowLv; lv++) {
				if (allEquiTempLvs.contains(lv)) {
					return true;
				}
			}
			return false;
		}

		/**
		 * <pre>
		 * 查询角色等级当前匹配的装备等级
		 * 
		 * @param roleLv
		 * @return
		 * @author CamusHuang
		 * @creation 2014-7-20 下午7:45:37
		 * </pre>
		 */
		public int checkMatchEquiLv(int roleLv) {
			Integer result = matchEquiTempLvs.get(roleLv);
			if (result == null) {
				return 1;
			}
			return result;
		}

		boolean containEquiLv(int lv) {
			return allEquiTempLvs.contains(lv);
		}

		/**
		 * <pre>
		 * 数据添加
		 * 如果存在ItemCode重复，则抛异常
		 * 
		 * @param itemTemplate
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2012-11-12 下午1:27:53
		 * </pre>
		 */
		void addDatas(List<KItemTempAbs> datas) throws Exception {
			for (KItemTempAbs itemTemplate : datas) {
				KItemTempAbs oldData = itemTemplateMap.put(itemTemplate.id, itemTemplate);
				if (oldData != null) {
					throw new Exception("重复的ItemCode=" + oldData.id);
				}
			}
			itemTemplateList.addAll(datas);
		}

		public KItemTempAbs getItemTemplate(String itemCode) {
			return itemTemplateMap.get(itemCode);
		}

		boolean containTemplate(String itemCode) {
			return itemTemplateMap.containsKey(itemCode);
		}

		Map<KEquipmentTypeEnum, KItemTempEqui> getNewRoleEquiments(KJobTypeEnum job) {
			return newRoleEquiTemplateMap.get(job);
		}

		boolean containStoneType(int type) {
			for (KItemTempAbs temp : itemTemplateList) {
				if (temp.type == KItemTypeEnum.宝石.sign) {
					if (((KItemTempStone) temp).stoneType == type) {
						return true;
					}
				}
			}
			return false;
		}

		public List<KItemTempAbs> getItemTemplateList() {
			return itemTemplateList;
		}

		void dataLoadFinishedNotify() throws Exception {
			// itemTemplateMap = Collections.unmodifiableMap(itemTemplateMap);
			// itemTemplateList =
			// Collections.unmodifiableList(itemTemplateList);

			for (KItemTempAbs temp : itemTemplateList) {
				temp.dataLoadFinishedNotify();
			}
		}

		void onGameWorldInitComplete() throws Exception {

			List<KItemTempStone> allStoneTemps = new ArrayList<KItemTempStone>();
			List<KItemTempEqui> allEquiTemps = new ArrayList<KItemTempEqui>();
			for (KItemTempAbs temp : itemTemplateList) {
				try{
					temp.onGameWorldInitComplete();
				} catch(Exception e){
					throw new Exception("加载道具模板错误："+e.getMessage()+",物品类型=" + temp.ItemType.sign + " ItemCode=" + temp.id, e);
				}
				
				switch (temp.ItemType) {
				case 装备:
					if (!(temp instanceof KItemTempEqui)) {
						throw new Exception("加载道具模板错误：物品类型错误=" + temp.ItemType.sign + " ItemCode=" + temp.id);
					}
					allEquiTemps.add((KItemTempEqui) temp);
					allEquiTempLvs.add(temp.lvl);
					break;
				case 固定宝箱:
					if (!(temp instanceof KItemTempFixedBox)) {
						throw new Exception("加载道具模板错误：物品类型错误=" + temp.ItemType.sign + " ItemCode=" + temp.id);
					}
					break;
				case 宝石:
					if (!(temp instanceof KItemTempStone)) {
						throw new Exception("加载道具模板错误：物品类型错误=" + temp.ItemType.sign + " ItemCode=" + temp.id);
					}
					allStoneTemps.add((KItemTempStone) temp);
					break;
				case 改造材料:
					if (!(temp instanceof KItemTempMaterial)) {
						throw new Exception("加载道具模板错误：装备类型错误=" + temp.ItemType.sign + " ItemCode=" + temp.id);
					}
					break;
				case 消耗品:
					if (!(temp instanceof KItemTempConsume)) {
						throw new Exception("加载道具模板错误：装备类型错误=" + temp.ItemType.sign + " ItemCode=" + temp.id);
					}
					break;
				case 装备包:
					if (!(temp instanceof KItemTempEquiBox)) {
						throw new Exception("加载道具模板错误：装备类型错误=" + temp.ItemType.sign + " ItemCode=" + temp.id);
					}
					break;
				case 随机宝箱:
					if (!(temp instanceof KItemTempRandomBox)) {
						throw new Exception("加载道具模板错误：装备类型错误=" + temp.ItemType.sign + " ItemCode=" + temp.id);
					}
					break;
				}
			}

			// <角色等级,能安装的装备最高等级>
			List<Integer> allEquiTempLvList = new ArrayList<Integer>(allEquiTempLvs);
			Collections.sort(allEquiTempLvList);
			{
				for (int lv : allEquiTempLvList) {
					int temp = lv;
					matchEquiTempLvs.put(temp, lv);
					for (temp++; temp <= KRoleModuleConfig.getRoleMaxLv(); temp++) {
						if (allEquiTempLvs.contains(temp)) {
							break;
						}
						matchEquiTempLvs.put(temp, lv);
					}
				}
			}

			// 初始化装备的更高级装备数据

			{
				initNextTopEquiSearchData(allEquiTemps, allEquiTempLvList);
			}

			// 新建角色装备
			{
				for (KItemTempAbs temp : itemTemplateList) {
					// 在装备表中，bBornEquip==1，且装备职业归属等于自己职业的装备，作为初始化装备，直接穿戴在玩家身上
					if (temp.ItemType == KItemTypeEnum.装备) {
						KItemTempEqui tempE = (KItemTempEqui) temp;
						if (tempE.bBornEquip == 1) {
							Map<KEquipmentTypeEnum, KItemTempEqui> map = newRoleEquiTemplateMap.get(tempE.jobEnum);
							if (map == null) {
								map = new HashMap<KEquipmentTypeEnum, KItemDataStructs.KItemTempEqui>();
								newRoleEquiTemplateMap.put(tempE.jobEnum, map);
							}
							if (map.put(tempE.typeEnum, tempE) != null) {
								throw new KGameServerException("角色初始装备重复 职业=" + tempE.jobEnum.name() + " 类型=" + tempE.typeEnum.name());
							}
						}
					}
				}
				for (KJobTypeEnum jobEnum : KJobTypeEnum.values()) {
					Map<KEquipmentTypeEnum, KItemTempEqui> tempMap = newRoleEquiTemplateMap.get(jobEnum);
					if (tempMap == null) {
						throw new KGameServerException("角色初始装备 缺漏职业=" + jobEnum.name());
					}
					if (tempMap.size() != KEquipmentTypeEnum.values().length) {
						throw new KGameServerException("角色初始装备 职业=" + jobEnum.name() + " 缺漏装备类型");
					}
					//
					DefaultRoleMapResInfoManager.getDefaultRoleMapResInfo(jobEnum).initEquipmentRes(tempMap);
				}
			}

			// 同一部位的装备，镶嵌宝石类型必须相同
			{
				Map<Integer, int[]> tempMap = new HashMap<Integer, int[]>();
				for (KItemTempAbs temp : itemTemplateList) {
					if (temp.type != KItemTypeEnum.装备.sign) {
						continue;
					}

					KItemTempEqui equiTemp = (KItemTempEqui) temp;
					int[] old = tempMap.get(equiTemp.part);
					if (old == null) {
						tempMap.put(equiTemp.part, old);
						continue;
					}
					// 比较类型
					if (old.length != equiTemp.stoneTypeMapForType.size()) {
						throw new KGameServerException("装备镶嵌类型错误 id=" + temp.id);
					}
					for (int i = 0; i < old.length; i++) {
						if (old[i] != equiTemp.stoneTypeMapForIndex.get(i)) {
							throw new KGameServerException("装备镶嵌类型错误 id=" + temp.id);
						}
					}
				}
			}
			// 同一类型的宝石，所加属性类型必须相同，随等级递增
			// 不同类型的宝石不能有属性重复
			{

				// <宝石类型,<宝石等级,属性值>>
				Map<Integer, Map<Integer, Integer>> typeToAttValue = new HashMap<Integer, Map<Integer, Integer>>();
				// <宝石类型,属性类型>
				Map<Integer, KGameAttrType> typeToAtt = new HashMap<Integer, KGameAttrType>();
//				// 1级宝石的所有属性类型
//				Set<KGameAttrType> attSetForLv1 = new HashSet<KGameAttrType>();
				// 1级宝石的所有属性类型
				List<KGameAttrType> attListForLv1 = new ArrayList<KGameAttrType>();

				for (KItemTempStone temp : allStoneTemps) {
					Entry<KGameAttrType, Integer> e = temp.allEffects.entrySet().iterator().next();
					KGameAttrType nowAttType = e.getKey();

					if (temp.stonelvl == 1) {
//						if (!attSetForLv1.add(nowAttType)) {
//							throw new KGameServerException("同一等级宝石属性加成重复 id=" + temp.id);
//						}
						attListForLv1.add(nowAttType);
					}

					KGameAttrType attType = typeToAtt.get(temp.stoneType);
					if (attType == null) {
						typeToAtt.put(temp.stoneType, nowAttType);
					} else if (attType != nowAttType) {
						throw new KGameServerException("同一宝石类型属性加成类型不一致 id=" + temp.id);
					}

					Map<Integer, Integer> lvValueMap = typeToAttValue.get(temp.stoneType);
					if (lvValueMap == null) {
						lvValueMap = new HashMap<Integer, Integer>();
						typeToAttValue.put(temp.stoneType, lvValueMap);
					}
					if (lvValueMap.put(temp.stonelvl, e.getValue()) != null) {
						throw new KGameServerException("同一宝石类型宝石等级重复 id=" + temp.id);
					}
				}

				// 同一类型的宝石，所加属性类型必须相同，随等级递增
				int lastValue = 0;
				for (Entry<Integer, Map<Integer, Integer>> e : typeToAttValue.entrySet()) {
					Map<Integer, Integer> map = e.getValue();
					for (int i = 1; i <= map.size(); i++) {
						int value = map.get(i);
						if (value <= lastValue) {
							throw new KGameServerException("宝石属性加成不能下降 type=" + e.getKey() + " lv=" + i);
						}
					}
				}

				//
				mEquiStoneSetDataManager2.setAttList(attListForLv1);
			}
		}

		/**
		 * <pre>
		 * 初始化装备的更高级装备数据
		 * 
		 * @author CamusHuang
		 * @creation 2014-7-20 下午6:40:42
		 * </pre>
		 */
		private static void initNextTopEquiSearchData(List<KItemTempEqui> allEquiTemps, List<Integer> allEquiTempLvList) {

			for (KItemTempEqui temp : allEquiTemps) {
				// 搜索同一等级，更高品质的
				searchTopEquiProduceData(temp.getTopQualityEquiProduceDataList(), temp.job, temp.part, temp.lvl, temp.qua, allEquiTemps);
				// 搜索下一等级，所有品质
				int nextLvIndex = allEquiTempLvList.indexOf(temp.lvl) + 1;
				if (nextLvIndex < allEquiTempLvList.size()) {
					searchTopEquiProduceData(temp.getNextLevelEquiProduceDataList(), temp.job, temp.part, allEquiTempLvList.get(nextLvIndex), -1, allEquiTemps);
				}
			}
		}

		/**
		 * <pre>
		 * 搜索指定等级，更高品质的
		 * 
		 * @param nowTemp
		 * @param allEquiTemps
		 * @author CamusHuang
		 * @creation 2014-7-20 下午6:46:23
		 * </pre>
		 */
		private static void searchTopEquiProduceData(ArrayList<EquiProduceData> result, int job, int part, int lvl, int qua, List<KItemTempEqui> allEquiTemps) {
			for (KItemTempEqui temp : allEquiTemps) {
				if (temp.job != job || temp.part != part || temp.lvl != lvl || temp.qua <= qua) {
					continue;
				}
				if (temp.getEquiProduceData() != null) {
					result.add(temp.getEquiProduceData());
				}
			}
		}
	}

	// /////////////////////////////
	/**
	 * <pre>
	 * 装备强化价格系数管理器
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-7 下午3:33:35
	 * </pre>
	 */
	static class EquiStrongPriceParamManager {
		/**
		 * <pre>
		 * KEY = 装备类型
		 * </pre>
		 */
		private Map<Integer, KEquiStrongPriceParam> dataMap = new HashMap<Integer, KEquiStrongPriceParam>();

		private EquiStrongPriceParamManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param itemTemplate
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2012-11-12 下午1:28:11
		 * </pre>
		 */
		void initDatas(List<KEquiStrongPriceParam> datas) throws Exception {
			for (KEquiStrongPriceParam data : datas) {
				if (dataMap.put(data.part, data) != null) {
					throw new Exception("重复的装备类型=" + data.part);
				}
			}
		}

		public KEquiStrongPriceParam getData(KEquipmentTypeEnum typeEnum) {
			return dataMap.get(typeEnum.sign);
		}

		public KEquiStrongPriceParam getData(int type) {
			return dataMap.get(type);
		}

		void onGameWorldInitComplete() throws Exception {
			for (KEquiStrongPriceParam data : dataMap.values()) {
				data.onGameWorldInitComplete();
			}

			for (KEquipmentTypeEnum type : KEquipmentTypeEnum.values()) {
				if (!dataMap.containsKey(type.sign)) {
					throw new Exception("缺少装备类型=" + type.sign);
				}
			}
			// CTODO
		}
	}

	// /////////////////////////////
	/**
	 * <pre>
	 * 装备镶嵌孔购买数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2014-12-4 下午3:58:36
	 * </pre>
	 */
	static class EquiBuyEnchanseDataManager {
		/**
		 * <pre>
		 * KEY = 孔序号
		 * </pre>
		 */
		private Map<Integer, KEquiBuyEnchansePrice> dataMap = new HashMap<Integer, KEquiBuyEnchansePrice>();

		private EquiBuyEnchanseDataManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param itemTemplate
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2012-11-12 下午1:28:11
		 * </pre>
		 */
		void initDatas(List<KEquiBuyEnchansePrice> datas) throws Exception {
			for (KEquiBuyEnchansePrice data : datas) {
				if (dataMap.put(data.HoleID, data) != null) {
					throw new Exception("重复的孔号=" + data.HoleID);
				}
			}
		}

		public KEquiBuyEnchansePrice getData(int holeId) {
			return dataMap.get(holeId);
		}

		void onGameWorldInitComplete() throws Exception {
			for (KEquiBuyEnchansePrice data : dataMap.values()) {
				data.onGameWorldInitComplete();
			}

			for (int num = 1; num <= KItemConfig.getInstance().MaxEnchansePositionPerOne; num++) {
				if (!dataMap.containsKey(num)) {
					throw new Exception("缺少孔数=" + num);
				}
			}
			// CTODO
		}
	}

	// /////////////////////////////
	/**
	 * <pre>
	 * 装备类型强化系数数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-7 下午3:33:35
	 * </pre>
	 */
	static class EquiStrongAttExtDataManager {
		/**
		 * <pre>
		 * KEY = 强化等级
		 * </pre>
		 */
		private Map<Integer, KEquiStrongAttExtData> dataMap = new HashMap<Integer, KEquiStrongAttExtData>();

		private EquiStrongAttExtDataManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param itemTemplate
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2012-11-12 下午1:28:11
		 * </pre>
		 */
		void initDatas(List<KEquiStrongAttExtData> datas) throws Exception {
			for (KEquiStrongAttExtData data : datas) {
				if (dataMap.put(data.lvl, data) != null) {
					throw new Exception("重复的强化等级=" + data.lvl);
				}
			}
		}

		public KEquiStrongAttExtData getData(int strongLv) {
			return dataMap.get(strongLv);
		}

		void onGameWorldInitComplete() throws Exception {
			for (KEquiStrongAttExtData data : dataMap.values()) {
				data.onGameWorldInitComplete();
			}

			int maxLv = KRoleModuleConfig.getRoleMaxLv();
			for (int lv = 1; lv <= maxLv; lv++) {
				if (!dataMap.containsKey(lv)) {
					throw new Exception("缺少装备强化等级=" + lv);
				}
			}
			// CTODO
		}
	}

	// /////////////////////////////
	/**
	 * <pre>
	 * 装备升星材料数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-7 下午3:33:35
	 * </pre>
	 */
	static class EquiStarMetrialDataManager {
		// <星阶,物品数据>
		private Map<Integer, KEquiStarMaterialData> dataMap = new HashMap<Integer, KEquiStarMaterialData>();

		private EquiStarMetrialDataManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param itemTemplate
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2012-11-12 下午1:28:11
		 * </pre>
		 */
		void initDatas(List<KEquiStarMaterialData> datas) throws KGameServerException {
			Set<String> set = new HashSet<String>();
			for (KEquiStarMaterialData data : datas) {
				if (!set.add(data.itemId)) {
					throw new KGameServerException("装备升星材料重复 = " + data.itemId);
				}
				dataMap.put(data.starLV, data);
			}
		}

		/**
		 * <pre>
		 * 
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2014-7-17 下午4:56:33
		 * </pre>
		 */
		Collection<KEquiStarMaterialData> getDataCache() {
			return dataMap.values();
		}

		/**
		 * <pre>
		 * 
		 * 
		 * @param starLv 星阶
		 * @return
		 * @author CamusHuang
		 * @creation 2014-7-17 下午4:56:33
		 * </pre>
		 */
		KEquiStarMaterialData getData(int starLv) {
			return dataMap.get(starLv);
		}

		void onGameWorldInitComplete() throws Exception {
			for (KEquiStarMaterialData data : dataMap.values()) {
				data.onGameWorldInitComplete();
			}
			// CTODO
		}

	}

	// /////////////////////////////
	/**
	 * <pre>
	 * 装备升星数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-7 下午3:33:35
	 * </pre>
	 */
	public static class EquiStarRateManager {
		/**
		 * <pre>
		 * KEY = 星级
		 * </pre>
		 */
		private Map<Integer, KEquiStarRateData> dataMap = new HashMap<Integer, KEquiStarRateData>();
		private int MaxStarLv;

		private EquiStarRateManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param itemTemplate
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2012-11-12 下午1:28:11
		 * </pre>
		 */
		void initDatas(List<KEquiStarRateData> datas) throws Exception {
			for (KEquiStarRateData data : datas) {
				if (dataMap.put(data.starLV, data) != null) {
					throw new Exception("重复的装备星级=" + data.starLV);
				}
				if (data.starLV > MaxStarLv) {
					MaxStarLv = data.starLV;
				}
			}
		}

		KEquiStarRateData getData(int starLv) {
			return dataMap.get(starLv);
		}

		public int getMaxStarLv() {
			return MaxStarLv;
		}

		void onGameWorldInitComplete() throws Exception {
			for (KEquiStarRateData data : dataMap.values()) {
				data.onGameWorldInitComplete();
			}

			for (int lv = 1; lv < MaxStarLv; lv++) {
				if (!dataMap.containsKey(lv)) {
					throw new Exception("缺少装备星级=" + lv);
				}
				if (mEquiStarMetrialDataManager.getData(KItemLogic.ExpressionForTopStarLv(lv)) == null) {
					throw new Exception("缺少升星材料 星级=" + lv);
				}
			}
			// CTODO
		}
	}

	// /////////////////////////////
	/**
	 * <pre>
	 * 属性比例参数
	 * 
	 * @author CamusHuang
	 * @creation 2014-10-27 下午6:19:24
	 * </pre>
	 */
	static class EquiAttExtDataManager {
		/**
		 * <pre>
		 * <属性类型, KEquiStarExtData>
		 * </pre>
		 */
		private Map<Integer, KEquiAttExtData> dataMap = new HashMap<Integer, KEquiAttExtData>();

		private EquiAttExtDataManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param itemTemplate
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2012-11-12 下午1:28:11
		 * </pre>
		 */
		void initDatas(List<KEquiAttExtData> datas) throws Exception {
			for (KEquiAttExtData data : datas) {
				if (dataMap.put(data.attribute_id, data) != null) {
					throw new Exception("数据重复 属性类型=" + data.attribute_id);
				}
			}
		}

		KEquiAttExtData getData(KGameAttrType attTypeEnum) {
			return dataMap.get(attTypeEnum.sign);
		}

		void onGameWorldInitComplete() throws Exception {
			// 必须包含所有装备的基础属性类型
			for (KItemTempAbs temp : mItemTemplateManager.itemTemplateList) {
				if (temp.ItemType == KItemTypeEnum.装备) {
					KItemTempEqui equi = (KItemTempEqui) temp;
					for (KGameAttrType type : equi.maxBaseAttMap.keySet()) {
						if (!dataMap.containsKey(type.sign)) {
							throw new Exception("缺少属性类型对应数值 type=" + type.sign);
						}
					}
				}
			}
			// CTODO
		}
	}

	// /////////////////////////////
	/**
	 * <pre>
	 * 升星等级属性比例管理器
	 * 
	 * @author CamusHuang
	 * @creation 2014-10-27 下午6:10:45
	 * </pre>
	 */
	static class EquiStarAttExtManager {
		/**
		 * <pre>
		 * KEY = 星级
		 * </pre>
		 */
		private Map<Integer, KEquiStarAttExtData> dataMap = new HashMap<Integer, KEquiStarAttExtData>();
		private int MaxStarLv;

		private EquiStarAttExtManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param itemTemplate
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2012-11-12 下午1:28:11
		 * </pre>
		 */
		void initDatas(List<KEquiStarAttExtData> datas) throws Exception {
			for (KEquiStarAttExtData data : datas) {
				if (dataMap.put(data.lvl, data) != null) {
					throw new Exception("重复的装备星级=" + data.lvl);
				}
				if (data.lvl > MaxStarLv) {
					MaxStarLv = data.lvl;
				}
			}
		}

		KEquiStarAttExtData getData(int starLv) {
			return dataMap.get(starLv);
		}

		int getMaxStarLv() {
			return MaxStarLv;
		}

		void onGameWorldInitComplete() throws Exception {
			if (MaxStarLv != mEquiStarRateManager.MaxStarLv) {
				throw new Exception("最大星级与其它表不统一");
			}

			for (KEquiStarAttExtData data : dataMap.values()) {
				data.onGameWorldInitComplete();
			}

			for (int i = 1; i < MaxStarLv; i++) {
				if (!dataMap.containsKey(i)) {
					throw new Exception("缺少装备星级=" + i);
				}
			}
			// CTODO
		}
	}

	// /////////////////////////////
	/**
	 * <pre>
	 * 装备继承数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-7 下午3:33:35
	 * </pre>
	 */
	static class EquiInheritDataManager {
		/**
		 * <pre>
		 * <目标装备品质,<目标装备等级,数据>>
		 * </pre>
		 */
		private Map<Integer, Map<Integer, KEquiInheritData>> dataMap = new HashMap<Integer, Map<Integer, KEquiInheritData>>();

		private EquiInheritDataManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param itemTemplate
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2012-11-12 下午1:28:11
		 * </pre>
		 */
		void initDatas(List<KEquiInheritData> datas) throws Exception {
			for (KEquiInheritData data : datas) {
				Map<Integer, KEquiInheritData> map = dataMap.get(data.Quality);
				if (map == null) {
					map = new HashMap<Integer, KItemDataStructs.KEquiInheritData>();
					dataMap.put(data.Quality, map);
				}
				if (map.put(data.Targetequipment, data) != null) {
					throw new Exception("重复的数据 品质=" + data.Quality + " 装备等级=" + data.Targetequipment);
				}
			}
		}

		KEquiInheritData getData(int quality, int lvl) {
			Map<Integer, KEquiInheritData> map = dataMap.get(quality);
			if (map == null) {
				return null;
			}
			return map.get(lvl);
		}

		void onGameWorldInitComplete() throws Exception {
			for (Map<Integer, KEquiInheritData> map : dataMap.values()) {
				for (KEquiInheritData data : map.values()) {
					data.onGameWorldInitComplete();
				}
			}
			// 是否覆盖所有装备
			for (KItemTempAbs temp : mItemTemplateManager.itemTemplateList) {
				if (temp.ItemType != KItemTypeEnum.装备) {
					continue;
				}
				KItemTempEqui equiTemp = (KItemTempEqui) temp;
				if (getData(equiTemp.qua, equiTemp.lvl) == null) {
					throw new Exception("缺少数据 品质=" + equiTemp.qua + ",  等级=" + equiTemp.lvl);
				}
			}
			// CTODO
		}
	}

	// /////////////////////////////
	/**
	 * <pre>
	 * 装备升星套装数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-7 下午3:33:35
	 * </pre>
	 */
	static class EquiStarSetDataManager {

		private List<KEquiStarSetData> dataList = new ArrayList<KItemDataStructs.KEquiStarSetData>();
		private Map<Integer, KEquiStarSetData> dataMap = new HashMap<Integer, KItemDataStructs.KEquiStarSetData>();

		private EquiStarSetDataManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param itemTemplate
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2012-11-12 下午1:28:11
		 * </pre>
		 */
		void initDatas(List<KEquiStarSetData> datas) throws Exception {
			dataList.addAll(datas);
			for (KEquiStarSetData data : datas) {
				dataMap.put(data.Suitcondition, data);
			}
		}

		List<KEquiStarSetData> getDataCache() {
			return dataList;
		}

		KEquiStarSetData getData(int starLv) {
			return dataMap.get(starLv);
		}

		void onGameWorldInitComplete() throws Exception {
			Set<Integer> starSet = new HashSet<Integer>();
			for (KEquiStarSetData data : dataList) {
				data.onGameWorldInitComplete();
				if (!starSet.add(data.Suitcondition)) {
					throw new Exception("重复的数据 升星等级=" + data.Suitcondition);
				}
				// 是否符合每阶6星的规则
				if (data.Suitcondition % KItemConfig.getInstance().EquiStarBigLv != 0) {
					throw new Exception("数据错误 星级=" + data.Suitcondition);
				}
			}

			// 是否覆盖所有星级
			for (int star = 1; star <= KItemDataManager.mEquiStarRateManager.getMaxStarLv(); star++) {
				if (star % KItemConfig.getInstance().EquiStarBigLv == 0) {
					if (!starSet.remove(star)) {
						throw new Exception("缺少数据 升星等级=" + star);
					}
				}
			}
			if (!starSet.isEmpty()) {
				throw new Exception("缺少数据 填了不存在的升星等级");
			}
			// CTODO
		}
	}

	// /////////////////////////////
	/**
	 * <pre>
	 * 装备镶嵌套装数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-7 下午3:33:35
	 * </pre>
	 */
	private static class EquiStoneSetDataManagerOld {

		private List<KEquiStoneSetDataOld> dataList = new ArrayList<KEquiStoneSetDataOld>();
		private Map<Integer, KEquiStoneSetDataOld> dataMap = new HashMap<Integer, KEquiStoneSetDataOld>();

		private EquiStoneSetDataManagerOld() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param itemTemplate
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2012-11-12 下午1:28:11
		 * </pre>
		 */
		void initDatas(List<KEquiStoneSetDataOld> datas) throws Exception {
			dataList.addAll(datas);
			for (KEquiStoneSetDataOld data : datas) {
				dataMap.put(data.Suitcondition, data);
			}
		}

		List<KEquiStoneSetDataOld> getDataCache() {
			return dataList;
		}

		KEquiStoneSetDataOld getData(int stoneLv) {
			return dataMap.get(stoneLv);
		}

		void onGameWorldInitComplete() throws Exception {
			Set<Integer> starSet = new HashSet<Integer>();
			for (KEquiStoneSetDataOld data : dataList) {
				data.onGameWorldInitComplete();
				if (!starSet.add(data.Suitcondition)) {
					throw new Exception("重复的数据 宝石等级=" + data.Suitcondition);
				}
			}

			// 是否覆盖所有装备
			for (KItemTempAbs temp : mItemTemplateManager.itemTemplateList) {
				if (temp.ItemType != KItemTypeEnum.宝石) {
					continue;
				}
				KItemTempStone equiTemp = (KItemTempStone) temp;
				if (!starSet.contains(equiTemp.stonelvl)) {
					throw new Exception("缺少数据 宝石等级=" + equiTemp.stonelvl);
				}
			}
			// CTODO
		}
	}

	// /////////////////////////////
	/**
	 * <pre>
	 * 装备宝石套装数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2014-11-14 上午11:24:28
	 * </pre>
	 */
	static class EquiStoneSetDataManager2 {

		private List<KEquiStoneSetData2> dataList = new ArrayList<KEquiStoneSetData2>();
		private Map<Integer, KEquiStoneSetData2> dataMap = new HashMap<Integer, KEquiStoneSetData2>();
		private List<KGameAttrType> attListForAllStoneLv;

		private EquiStoneSetDataManager2() {
		}

		void setAttList(List<KGameAttrType> attListForLv1) {
			attListForAllStoneLv = attListForLv1;
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param datas
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2014-11-14 上午11:24:45
		 * </pre>
		 */
		void initDatas(List<KEquiStoneSetData2> datas) throws Exception {
			dataList.addAll(datas);
			for (KEquiStoneSetData2 data : datas) {
				dataMap.put(data.Suitcondition, data);
			}
		}

		List<KGameAttrType> getAttListForAllStoneLv() {
			return attListForAllStoneLv;
		}

		List<KEquiStoneSetData2> getDataCache() {
			return dataList;
		}

		KEquiStoneSetData2 getData(int stoneLv) {
			return dataMap.get(stoneLv);
		}

		void onGameWorldInitComplete() throws Exception {
			Set<Integer> stoneLvSet = new HashSet<Integer>();
			for (KEquiStoneSetData2 data : dataList) {
				data.onGameWorldInitComplete();
				if (!stoneLvSet.add(data.Suitcondition)) {
					throw new Exception("重复的数据 宝石等级=" + data.Suitcondition);
				}
			}

			// 是否覆盖所有装备
			Set<Integer> stoneLvSet2 = new HashSet<Integer>(stoneLvSet);
			for (KItemTempAbs temp : mItemTemplateManager.itemTemplateList) {
				if (temp.ItemType != KItemTypeEnum.宝石) {
					continue;
				}
				KItemTempStone stoneTemp = (KItemTempStone) temp;
				if (!stoneLvSet.contains(stoneTemp.stonelvl)) {
					throw new Exception("缺少数据 宝石等级=" + stoneTemp.stonelvl);
				}
				stoneLvSet2.remove(stoneTemp.stonelvl);
			}

			// 多余的宝石等级
			if (!stoneLvSet2.isEmpty()) {
				throw new Exception("非法数据 宝石等级=" + stoneLvSet2.iterator().next());
			}
			// CTODO
		}
	}

	// /////////////////////////////
	/**
	 * <pre>
	 * 装备强化套装数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2014-7-28 下午5:17:32
	 * </pre>
	 */
	static class EquiStrongSetDataManager {

		private List<KEquiStrongSetData> dataList = new ArrayList<KEquiStrongSetData>();
		private Map<Integer, KEquiStrongSetData> dataMapByLv = new HashMap<Integer, KEquiStrongSetData>();
		private Map<Integer, KEquiStrongSetData> dataMapById = new HashMap<Integer, KEquiStrongSetData>();

		private EquiStrongSetDataManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param itemTemplate
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2012-11-12 下午1:28:11
		 * </pre>
		 */
		void initDatas(List<KEquiStrongSetData> datas) throws Exception {
			dataList.addAll(datas);
			int maxId = 0;
			for (KEquiStrongSetData data : datas) {
				if (dataMapByLv.put(data.Suitcondition, data) != null) {
					throw new Exception("强化等级重复 = " + data.Suitcondition);
				}
				if (dataMapById.put(data.id, data) != null) {
					throw new Exception("强化ID重复 = " + data.id);
				}
				if (data.id > maxId) {
					maxId = data.id;
				}
			}
			for (int id = 1; id <= maxId; id++) {
				if (!dataMapById.containsKey(id)) {
					throw new Exception("强化ID缺漏 = " + id);
				}
			}
		}

		List<KEquiStrongSetData> getDataCache() {
			return dataList;
		}

		KEquiStrongSetData getDataByLv(int strongLv) {
			return dataMapByLv.get(strongLv);
		}

		KEquiStrongSetData getDataById(int id) {
			return dataMapById.get(id);
		}

		void onGameWorldInitComplete() throws Exception {

			int maxStrongLv = KRoleModuleConfig.getRoleMaxLv();

			Set<Integer> stongSet = new HashSet<Integer>();
			for (KEquiStrongSetData data : dataList) {
				data.onGameWorldInitComplete();
				if (!stongSet.add(data.Suitcondition)) {
					throw new Exception("重复的数据 强化等级=" + data.Suitcondition);
				}
				if (data.Suitcondition > maxStrongLv) {
					throw new Exception("错误的数据 强化等级=" + data.Suitcondition);
				}
			}
			// CTODO
		}
	}

	// /////////////////////////////
	/**
	 * <pre>
	 * 装备品质套装数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2014-10-27 上午11:43:47
	 * </pre>
	 */
	static class EquiQualitySetDataManager {

		private List<KEquiQualitySetData> dataList = new ArrayList<KEquiQualitySetData>();
		// <套装ID,数据>
		private Map<Integer, KEquiQualitySetData> dataMap = new HashMap<Integer, KEquiQualitySetData>();
		// <装备等级,<装备品质,<装备数量,数据>>>
		private Map<Integer, Map<Integer, Map<Integer, KEquiQualitySetData>>> dataMapByNum = new HashMap<Integer, Map<Integer, Map<Integer, KEquiQualitySetData>>>();
		// <装备等级,<装备品质,<数据>>>
		private Map<Integer, Map<Integer, List<KEquiQualitySetData>>> dataMapByLv = new HashMap<Integer, Map<Integer, List<KEquiQualitySetData>>>();

		private EquiQualitySetDataManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param itemTemplate
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2012-11-12 下午1:28:11
		 * </pre>
		 */
		void initDatas(List<KEquiQualitySetData> datas) throws Exception {

			Collections.sort(datas);
			dataList.addAll(datas);

			// <装备等级,<装备品质,装备数量>>
			Map<Integer, Map<Integer, List<Integer>>> tempAs = new HashMap<Integer, Map<Integer, List<Integer>>>();

			for (KEquiQualitySetData data : datas) {
				dataMap.put(data.id, data);
				{
					// <装备等级,<装备品质,<装备数量,数据>>>
					Map<Integer, Map<Integer, KEquiQualitySetData>> map1 = dataMapByNum.get(data.lv);
					if (map1 == null) {
						map1 = new HashMap<Integer, Map<Integer, KEquiQualitySetData>>();
						dataMapByNum.put(data.lv, map1);
					}

					Map<Integer, KEquiQualitySetData> map2 = map1.get(data.qua);
					if (map2 == null) {
						map2 = new HashMap<Integer, KEquiQualitySetData>();
						map1.put(data.qua, map2);
					}

					if (map2.put(data.Number, data) != null) {
						throw new Exception("数据重复 lv=" + data.lv + " 品质=" + data.qua + " 数量=" + data.Number);
					}
				}
				{
					// <装备等级,<装备品质,<数据>>>
					Map<Integer, List<KEquiQualitySetData>> map1 = dataMapByLv.get(data.lv);
					if (map1 == null) {
						map1 = new HashMap<Integer, List<KEquiQualitySetData>>();
						dataMapByLv.put(data.lv, map1);
					}

					List<KEquiQualitySetData> map2 = map1.get(data.qua);
					if (map2 == null) {
						map2 = new ArrayList<KEquiQualitySetData>();
						map1.put(data.qua, map2);
					}

					map2.add(data);
				}
				{
					// <装备等级,<装备品质,装备数量>>
					Map<Integer, List<Integer>> map1 = tempAs.get(data.lv);
					if (map1 == null) {
						map1 = new HashMap<Integer, List<Integer>>();
						tempAs.put(data.lv, map1);
					}

					List<Integer> list = map1.get(data.qua);
					if (list == null) {
						list = new ArrayList<Integer>();
						map1.put(data.qua, list);
					}

					list.add(data.Number);
				}
			}

			// <装备等级,<装备品质,装备数量>>
			for (Entry<Integer, Map<Integer, List<Integer>>> e : tempAs.entrySet()) {
				int lvl = e.getKey();
				for (Entry<Integer, List<Integer>> ee : e.getValue().entrySet()) {
					int qua = ee.getKey();
					List<Integer> list = ee.getValue();
					Collections.sort(list);
					for (int i = 0; (i + 1) < list.size(); i++) {
						int start = list.get(i);
						int end = list.get(i + 1);

						Map<Integer, KEquiQualitySetData> map = dataMapByNum.get(lvl).get(qua);
						KEquiQualitySetData data = map.get(start);
						for (int num = start; num < end; num++) {
							map.put(num, data);
						}
					}
				}
			}
		}

		List<KEquiQualitySetData> getDataCache() {
			return dataList;
		}

		KEquiQualitySetData getDataById(int id) {
			return dataMap.get(id);
		}

		KEquiQualitySetData getData(int lvl, int qua, int num) {
			Map<Integer, Map<Integer, KEquiQualitySetData>> map = dataMapByNum.get(lvl);
			if (map == null) {
				return null;
			}
			Map<Integer, KEquiQualitySetData> map2 = map.get(qua);
			if (map2 == null) {
				return null;
			}
			return map2.get(num);
		}

		List<KEquiQualitySetData> getData(int lvl, int qua) {
			Map<Integer, List<KEquiQualitySetData>> map = dataMapByLv.get(lvl);
			if (map == null) {
				return null;
			}
			List<KEquiQualitySetData> map2 = map.get(qua);
			if (map2 == null) {
				return Collections.emptyList();
			}
			return map2;
		}

		void onGameWorldInitComplete() throws Exception {

			for (KEquiQualitySetData data : dataList) {
				try {
					data.onGameWorldInitComplete();
				} catch (Exception e) {
					throw new Exception(e.getMessage() + ",套装ID= " + data.id, e);
				}
			}

			for (KItemTempAbs temp : mItemTemplateManager.itemTemplateList) {
				if (temp.ItemType == KItemTypeEnum.装备) {
					if (temp.qua >= KItemQualityEnum.史诗的.sign) {
						if (temp.lvl < 10) {
							continue;
						}
						// <装备等级,<装备品质,<装备数量,数据>>>
						// Map<Integer, Map<Integer, Map<Integer,
						// KEquiQualitySetData>>>
						Map<Integer, Map<Integer, KEquiQualitySetData>> mapA = dataMapByNum.get(temp.lvl);
						if (mapA == null) {
							throw new Exception("缺少装备品质套装数据 等级=" + temp.lvl);
						}
						Map<Integer, KEquiQualitySetData> mapB = mapA.get(temp.qua);
						if (mapB == null) {
							throw new Exception("缺少装备品质套装数据 等级=" + temp.lvl + ",品质=" + temp.qua);
						}

						KEquiQualitySetData data = mapB.get(KItemConfig.getInstance().TotalMaxEquiNum);
						if (data == null) {
							throw new Exception("缺少装备品质套装数据 等级=" + temp.lvl + ",品质=" + temp.qua + ",数量=" + KItemConfig.getInstance().TotalMaxEquiNum);
						}
					}
				}
			}

			// 属性叠加
			for (Map<Integer, List<KEquiQualitySetData>> map : dataMapByLv.values()) {
				for (List<KEquiQualitySetData> list : map.values()) {
					for (int i = 0; (i + 1) < list.size(); i++) {
						KEquiQualitySetData frontData = list.get(i);
						KEquiQualitySetData nowData = list.get(i + 1);

						for (Entry<KGameAttrType, Integer> e : nowData.allEffects.entrySet()) {
							Integer frontValue = frontData.allEffects.get(e.getKey());
							if (frontValue != null) {
								e.setValue(e.getValue() + frontValue);
							}
						}
					}
				}
			}
			// CTODO
		}
	}

	/**
	 * <pre>
	 * 服务器启动完成
	 * 
	 * @author CamusHuang
	 * @creation 2012-12-3 下午8:41:04
	 * </pre>
	 */
	static void dataLoadFinishedNotify() throws KGameServerException {
		try {
			mItemTemplateManager.dataLoadFinishedNotify();
		} catch (Exception e) {
			throw new KGameServerException("加载道具模板错误：" + e.getMessage(), e);
		}
	}

	/**
	 * <pre>
	 * 服务器启动完成
	 * 
	 * @author CamusHuang
	 * @creation 2012-12-3 下午8:41:04
	 * </pre>
	 */
	static void onGameWorldInitComplete() throws KGameServerException {
		try {
			mBagExtDataManager.onGameWorldInitComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载" + KItemDataLoader.SheetName_背包_扩容表 + "错误：" + e.getMessage(), e);
		}

		try {
			mItemTemplateManager.onGameWorldInitComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载道具模板错误：" + e.getMessage(), e);
		}

		try {
			mEquiStrongAttExtDataManager.onGameWorldInitComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载" + KItemDataLoader.SheetName_装备玩法_强化等级属性比例 + "错误：" + e.getMessage(), e);
		}

		try {
			mEquiStarMetrialDataManager.onGameWorldInitComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载" + KItemDataLoader.SheetName_装备玩法_升星材料 + "错误：" + e.getMessage(), e);
		}

		try {
			mEquiStarRateManager.onGameWorldInitComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载" + KItemDataLoader.SheetName_装备玩法_升星数据 + "错误：" + e.getMessage(), e);
		}

		try {
			mEquiAttExtDataManager.onGameWorldInitComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载" + KItemDataLoader.SheetName_装备玩法_属性比例参数 + "错误：" + e.getMessage(), e);
		}

		try {
			mEquiStarAttExtManager.onGameWorldInitComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载" + KItemDataLoader.SheetName_装备玩法_升星等级属性比例 + "错误：" + e.getMessage(), e);
		}

		try {
			mEquiInheritDataManager.onGameWorldInitComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载" + KItemDataLoader.SheetName_装备玩法_装备继承 + "错误：" + e.getMessage(), e);
		}

		try {
			mEquiStarSetDataManager.onGameWorldInitComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载" + KItemDataLoader.SheetName_装备套装_升星套装 + "错误：" + e.getMessage(), e);
		}

		try {
			mEquiStoneSetDataManager2.onGameWorldInitComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载" + KItemDataLoader.SheetName_装备套装_宝石套装 + "错误：" + e.getMessage(), e);
		}

		try {
			mEquiStrongSetDataManager.onGameWorldInitComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载" + KItemDataLoader.SheetName_装备套装_强化套装 + "错误：" + e.getMessage(), e);
		}

		try {
			mEquiEnchanseDataManager.onGameWorldInitComplete();
		} catch (Exception e) {
			throw new KGameServerException("错误：" + e.getMessage(), e);
		}

		try {
			mEquiQualitySetDataManager.onGameWorldInitComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载" + KItemDataLoader.SheetName_装备套装_品质套装 + "错误：" + e.getMessage(), e);
		}
		
		try {
			mEquiBuyEnchanseDataManager.onGameWorldInitComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载" + KItemDataLoader.SheetName_装备玩法_装备开孔 + "错误：" + e.getMessage(), e);
		}
	}
}
