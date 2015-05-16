package com.kola.kmp.logic.mount;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KItemQualityEnum;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.KGameUtilTool;
import com.kola.kmp.logic.util.text.HyperTextTool;

public class KMountDataStructs {

	/**
	 * <pre>
	 * [机甲信息]
	 * 
	 * @author CamusHuang
	 * @creation 2015-1-5 下午4:26:52
	 * </pre>
	 */
	public static class KMountTemplate {
		// ----------以下是EXCEL表格直导数据---------
		public int mountsID;// 机甲ID
		public int Model;// 机甲
		public String Name;// 座驾名称
		public int bigLv;// 座驾阶级
		public int HeadID;// 头像ID
		public int res_id;// 座驾形象ID
		public String mountsAI;// 机甲AI
		public int att_range;// 攻击距离
		public float cityMoveSpeedup;// 主城移动速度影响
		public float fightMoveSpeed;// 战斗移动速度影响
		public int openlv;// 开放等级
		public String Description;// 获得界面描述
		public String ModelDescription;// 型号类型描述
		public int functionId;// 打开界面指令ID
		public String SpecialtyDescription;// 机甲特长
		public int armortime;// 霸体时长
		public int armorinterval;// 霸体间隔
		public int atkCountPerTime;// 单次普通攻击次数
		public boolean isForNewRole;// 是否新手引导座驾
		private int BeanTime1;// 1怒气豆时间
		private int BeanTime2;// 2怒气豆时间
		private int BeanTime3;// 3怒气豆时间

		//
		private int qua;// 品质
		private int[] attribute_id;// 增加属性基础编号
		private int[] attribute;// 增加属性基础数量
		private int templateId1;// 技能1ID
		private int templateId2;// 技能2
		private int templateId3;// 技能3
		private int templateId4;// 技能4
		private int equipID1;// 装备1ID
		private int equipID2;// 装备2ID
		private int equipID3;// 装备3ID
		private int equipID4;// 装备4ID
		// ----------以下是逻辑数据---------
		public String extName;// 机甲名称
		public KItemQualityEnum quaEnum;
		public LinkedHashMap<KGameAttrType, Integer> allEffects;// LinkedHashMap
		public List<Integer> skillIdList = new ArrayList<Integer>();
		public Set<Integer> skillIdSet = new HashSet<Integer>();
		public List<Integer> equiIdList = new ArrayList<Integer>();
		public Set<Integer> equiIdSet = new HashSet<Integer>();
		// <怒气豆数量,有效时长（秒）>，1，2，3个怒气豆
		public Map<Integer, Integer> beanTimeMap = new HashMap<Integer, Integer>();
		//
		private Map<KGameAttrType, Integer> baseAttAndLv1;// 用于计算模板战斗力（基础属性加1级属性）
		private Map<KGameAttrType, Integer> baseAttAndFullEqui;// 用于假角色战斗

		void initAtts() throws Exception {
			quaEnum = KItemQualityEnum.getEnum(qua);

			extName = HyperTextTool.extColor(Name, quaEnum.color);

			if (quaEnum == null) {
				throw new Exception("qua错误 =" + qua);
			}

			if (attribute_id.length < 1) {
				throw new Exception("属性数量错误 id=" + mountsID);
			}
			try {
				allEffects = KGameUtilTool.genAttribute(attribute_id, attribute, false);
			} catch (Exception e) {
				throw new Exception(e.getMessage() + " id=" + mountsID);
			}

			{
				if (templateId1 > 0) {
					skillIdList.add(templateId1);
				}
				if (templateId2 > 0) {
					skillIdList.add(templateId2);
				}
				if (templateId3 > 0) {
					skillIdList.add(templateId3);
				}
				if (templateId4 > 0) {
					skillIdList.add(templateId4);
				}
				skillIdSet.addAll(skillIdList);
				if (skillIdSet.size() != skillIdList.size()) {
					throw new Exception("机甲技能重复");
				}
			}

			{
				if (equipID1 > 0) {
					equiIdList.add(equipID1);
				}
				if (equipID2 > 0) {
					equiIdList.add(equipID2);
				}
				if (equipID3 > 0) {
					equiIdList.add(equipID3);
				}
				if (equipID4 > 0) {
					equiIdList.add(equipID4);
				}
				equiIdSet.addAll(equiIdList);
				if (equiIdSet.size() != equiIdList.size()) {
					throw new Exception("机甲装备重复");
				}
				if (equiIdList.size() != KMountConfig.EQUI_FIXED_COUNT) {
					throw new Exception("机甲装备数量必须为" + KMountConfig.EQUI_FIXED_COUNT + "个");
				}
			}
		}

		/**
		 * <pre>
		 * 战斗力计算方式=培养属性+机甲基础属性+机甲装备属性
		 * 
		 * @param roleId
		 * @return
		 * @author CamusHuang
		 * @creation 2015-1-26 上午11:09:11
		 * </pre>
		 */
		int getBattlePower(long roleId) {
			if (baseAttAndLv1 == null) {
				// 基础+1级，无装备
				baseAttAndLv1 = new HashMap<KGameAttrType, Integer>(allEffects);
				Map<KGameAttrType, Integer> lvAtts = KMountDataManager.mMountLvDataManager.getData(1).allEffects.get(Model);
				//
				for (Entry<KGameAttrType, Integer> e : lvAtts.entrySet()) {
					Integer baseValue = baseAttAndLv1.get(e.getKey());
					int equValue = e.getValue() + (baseValue == null ? 0 : baseValue);
					baseAttAndLv1.put(e.getKey(), equValue);
				}
			}
			// 基础+1级，无装备
			return KSupportFactory.getRoleModuleSupport().calculateBattlePower(baseAttAndLv1, roleId);
		}

		/**
		 * <pre>
		 * 获取战斗属性（即模板基础属性+满装备属性）
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2015-1-31 下午3:14:03
		 * </pre>
		 */
		public Map<KGameAttrType, Integer> getWarAtts() {
			if (baseAttAndFullEqui == null) {
				// 基础+1级，无装备
				baseAttAndFullEqui = new HashMap<KGameAttrType, Integer>(allEffects);
				//
				for (int equipID : equiIdList) {
					KMountEquiTemp equTemp = KMountDataManager.mMountEquiDataManager.getData(equipID);
					if (equTemp == null) {
						continue;
					}
					for (Entry<KGameAttrType, Integer> e : equTemp.allEffects.entrySet()) {
						Integer baseValue = baseAttAndFullEqui.get(e.getKey());
						int equValue = e.getValue() + (baseValue == null ? 0 : baseValue);
						baseAttAndFullEqui.put(e.getKey(), equValue);
					}
				}
			}
			return baseAttAndFullEqui;
		}

		/**
		 * <pre>
		 * onGameWorldInitComplete
		 * 
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2014-2-18 下午6:01:47
		 * </pre>
		 */
		void notifyCacheLoadComplete() throws Exception {

			if (cityMoveSpeedup < 1) {
				throw new Exception("cityMoveSpeed错误 =" + cityMoveSpeedup);
			}
			if (fightMoveSpeed < 1) {
				throw new Exception("warMoveSpeed错误 =" + fightMoveSpeed);
			}

			if (bigLv < 1) {
				throw new Exception("bigLv错误 =" + bigLv);
			}
			if (att_range < 1) {
				throw new Exception("att_range错误 =" + att_range);
			}

			if (openlv > 0 && bigLv != 1) {
				throw new Exception("只有1阶机甲允许按等级开放");
			}

			if (isForNewRole && bigLv != 1) {
				throw new Exception("只有1阶机甲允许作为新手引导机甲");
			}

			if (openlv > 0 && isForNewRole) {
				throw new Exception("新手引导机甲不应该按等级开放");
			}

			if (BeanTime1 < 1) {
				throw new Exception("BeanTime1错误 =" + BeanTime1);
			}
			beanTimeMap.put(1, BeanTime1);

			if (BeanTime2 < 1) {
				throw new Exception("BeanTime2错误 =" + BeanTime2);
			}
			beanTimeMap.put(2, BeanTime2);

			if (BeanTime3 < 1) {
				throw new Exception("BeanTime3错误 =" + BeanTime3);
			}
			beanTimeMap.put(3, BeanTime3);

			if (BeanTime2 < BeanTime1 || BeanTime3 < BeanTime2) {
				throw new Exception("怒气豆持续时长必须递增");
			}

			for (int skillTempId : skillIdList) {
				if (null == KSupportFactory.getSkillModuleSupport().getMountSkillTemplate(skillTempId)) {
					throw new Exception("不存在的机甲技能  skillId=" + skillTempId);
				}
			}
			for (int equiId : equiIdList) {
				KMountEquiTemp equiTemp = KMountDataManager.mMountEquiDataManager.getData(equiId);
				if (equiTemp == null) {
					throw new Exception("不存在的机甲装备  =" + equiId);
				}
				// 设置装备的阶数
				equiTemp.bigLv = this.bigLv;
			}

			// 基础属性、升级属性、装备属性类型必须完全一致
			// KMountLvMaxAtts maxAtts =
			// KMountDataManager.mMountLvMaxDataManager.getData(Model);
			// if (allEffects.size() != maxAtts.allEffects.size()) {
			// throw new Exception("模板培养属性必须与型号最大属性一致");
			// }
			// for (KGameAttrType attType : allEffects.keySet()) {
			// if (!maxAtts.allEffects.containsKey(attType)) {
			// throw new Exception("模板培养属性必须与型号最大属性一致");
			// }
			// }
			// for (int equiId : equiIdList) {
			// KMountEquiTemp equiTemp =
			// KMountDataManager.mMountEquiDataManager.getData(equiId);
			//
			// if (equiTemp.allEffects.size() != maxAtts.allEffects.size()) {
			// throw new Exception("装备属性必须与型号最大属性一致  装备  =" + equiId);
			// }
			// for (KGameAttrType attType : allEffects.keySet()) {
			// if (!maxAtts.allEffects.containsKey(attType)) {
			// throw new Exception("装备属性必须与型号最大属性一致  装备  =" + equiId);
			// }
			// }
			// }
		}
	}

	/**
	 * <pre>
	 * [机甲培养经验与属性比例]
	 * 
	 * @author CamusHuang
	 * @creation 2015-1-5 下午4:26:46
	 * </pre>
	 */
	public static class KMountLv {

		// ----------以下是EXCEL表格直导数据---------
		public int lv;// 等级
		public int exp;// 经验
		public int attributeProportion;// 属性比例
		public int spPoint;// 可用sp点

		// ----------以下是逻辑数据---------
		// 本等级的属性加成 <型号,属性数据>
		public LinkedHashMap<Integer, Map<KGameAttrType, Integer>> allEffects = new LinkedHashMap<Integer, Map<KGameAttrType, Integer>>();

		void notifyCacheLoadComplete() throws Exception {
			if (lv < 1) {
				throw new Exception("lv错误 =" + lv);
			}
			if (exp < 1) {
				throw new Exception("exp错误 =" + exp);
			}
			if (attributeProportion < 1) {
				throw new Exception("attributeProportion错误 =" + attributeProportion);
			}
			if (spPoint < 0) {
				throw new Exception("spPoint错误 =" + spPoint);
			}

			// 根据最大值，生成相应的等级比例值
			for (Entry<Integer, KMountLvMaxAtts> e : KMountDataManager.mMountLvMaxDataManager.getDataCache().entrySet()) {
				int model = e.getKey();
				Map<KGameAttrType, Integer> tempMap = new LinkedHashMap<KGameAttrType, Integer>();
				for (Entry<KGameAttrType, Integer> ee : e.getValue().allEffects.entrySet()) {
					tempMap.put(ee.getKey(), ee.getValue() * attributeProportion / 10000);
				}
				allEffects.put(model, tempMap);
			}
		}
	}

	/**
	 * <pre>
	 * [机甲培养属性上限]
	 * 
	 * @author CamusHuang
	 * @creation 2015-1-5 下午4:54:54
	 * </pre>
	 */
	public static class KMountLvMaxAtts {

		// ----------以下是EXCEL表格直导数据---------
		public int Model;// 机甲
		private int[] attribute_id;// 增加属性编号
		private int[] attribute;// 增加属性数量
		// ----------以下是逻辑数据---------
		public LinkedHashMap<KGameAttrType, Integer> allEffects;// LinkedHashMap

		void initAtts() throws Exception {
			if (attribute_id.length < 1) {
				throw new Exception("属性数量错误 Model=" + Model);
			}
			try {
				allEffects = KGameUtilTool.genAttribute(attribute_id, attribute, false);
			} catch (Exception e) {
				throw new Exception(e.getMessage() + " Model=" + Model);
			}
		}

		void notifyCacheLoadComplete() throws Exception {

		}
	}

	/**
	 * <pre>
	 * [机甲培养材料]
	 * 
	 * @author CamusHuang
	 * @creation 2015-1-5 下午5:24:27
	 * </pre>
	 */
	public static class KMountUpLvData {

		// ----------以下是EXCEL表格直导数据---------
		public String itemTempId;// 道具编码
		public int addExp;// 增加经验值
		//
		private int moneyType1;// 货币类型1
		private int moneyCount1;// 货币数量1
		private int moneyType2;// 货币类型2
		private int moneyCount2;// 货币数量2
		// ----------以下是逻辑数据---------
		public ItemCountStruct itemStruct;
		public KCurrencyCountStruct moneyStruct;

		void notifyCacheLoadComplete() throws Exception {
			itemStruct = new ItemCountStruct(itemTempId, 1L);
			if (itemStruct.getItemTemplate() == null) {
				throw new Exception("物品不存在");
			}

			if (addExp < 1) {
				throw new Exception("addExp错误 =" + addExp);
			}

			Set<KCurrencyTypeEnum> muiSet = new HashSet<KCurrencyTypeEnum>();
			List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();
			if (moneyType1 > 0 && moneyCount1 > 0) {
				KCurrencyTypeEnum type = KCurrencyTypeEnum.getEnum(moneyType1);
				if (type == null) {
					throw new Exception("货币类型不存在 moneyType1=" + moneyType1);
				}
				moneyList.add(new KCurrencyCountStruct(type, moneyCount1));
				if (!muiSet.add(type)) {
					throw new Exception("货币类型重复 moneyType1=" + moneyType1);
				}
			}
			if (moneyType2 > 0) {
				KCurrencyTypeEnum type = KCurrencyTypeEnum.getEnum(moneyType2);
				if (type == null) {
					throw new Exception("货币类型不存在 moneyType2=" + moneyType2);
				}
				moneyList.add(new KCurrencyCountStruct(type, moneyCount2));
				if (!muiSet.add(type)) {
					throw new Exception("货币类型重复 moneyType2=" + moneyType2);
				}
			}

			if (moneyList.size() != 1) {
				throw new Exception("UI要求货币要填且只能填一种");
			}
			moneyStruct = moneyList.get(0);
		}
	}

	/**
	 * <pre>
	 * [机甲进阶条件]
	 * 
	 * @author CamusHuang
	 * @creation 2015-1-5 下午6:18:37
	 * </pre>
	 */
	public static class KMountUpBigLvData {
		// ----------以下是EXCEL表格直导数据---------
		public int bigLv;// 座驾阶级
		public int lv;// 所需机甲等级
		private int moneyType1;// 货币类型1
		private int moneyCount1;// 货币数量1
		private int moneyType2;// 货币类型2
		private int moneyCount2;// 货币数量2
		// ----------以下是逻辑数据---------
		public List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();

		void notifyCacheLoadComplete() throws Exception {
			if (bigLv < 1) {
				throw new Exception("bigLv错误 =" + bigLv);
			}
			if (lv < 1) {
				throw new Exception("lv错误 =" + lv);
			}

			Set<KCurrencyTypeEnum> muiSet = new HashSet<KCurrencyTypeEnum>();
			if (moneyType1 > 0 && moneyCount1 > 0) {
				KCurrencyTypeEnum type = KCurrencyTypeEnum.getEnum(moneyType1);
				if (type == null) {
					throw new Exception("货币类型不存在 moneyType1=" + moneyType1);
				}
				moneyList.add(new KCurrencyCountStruct(type, moneyCount1));
				if (!muiSet.add(type)) {
					throw new Exception("货币类型重复 moneyType1=" + moneyType1);
				}
			}
			if (moneyType2 > 0) {
				KCurrencyTypeEnum type = KCurrencyTypeEnum.getEnum(moneyType2);
				if (type == null) {
					throw new Exception("货币类型不存在 moneyType2=" + moneyType2);
				}
				moneyList.add(new KCurrencyCountStruct(type, moneyCount2));
				if (!muiSet.add(type)) {
					throw new Exception("货币类型重复 moneyType2=" + moneyType2);
				}
			}
			if (moneyList.isEmpty()) {
				moneyList = Collections.emptyList();
			}
		}
	}

	/**
	 * <pre>
	 * [机甲装备打造]
	 * 
	 * @author CamusHuang
	 * @creation 2015-1-5 下午6:32:13
	 * </pre>
	 */
	public static class KMountEquiTemp {
		// ----------以下是EXCEL表格直导数据---------
		public int equipID;// 机甲装备ID
		public int icon;// 对应图片
		public String name;// 名称
		public int bigLv;// 阶数(通过机甲模板进行设置)
		//
		private int qua;// 品质
		private String itemTempId1;// 道具编码1
		private int itemCount1;// 道具数量1
		private String itemTempId2;// 道具编码2
		private int itemCount2;// 道具数量2
		private String itemTempId3;// 道具编码3
		private int itemCount3;// 道具数量3
		private int moneyType1;// 货币类型1
		private int moneyCount1;// 货币数量1
		private int moneyType2;// 货币类型2
		private int moneyCount2;// 货币数量2
		private int[] attribute_id;// 增加属性编号
		private int[] attribute;// 增加属性数量
		// ----------以下是逻辑数据---------
		public String extName;// 名称
		public KItemQualityEnum quaEnum;
		public List<ItemCountStruct> itemList = new ArrayList<ItemCountStruct>();
		public Map<String, Integer> itemCountMap = new HashMap<String, Integer>();
		public Map<String, ItemCountStruct> itemMap = new HashMap<String, ItemCountStruct>();
		public List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();
		public LinkedHashMap<KGameAttrType, Integer> allEffects;// LinkedHashMap

		void initAtts() throws Exception {
			if (attribute_id.length < 1) {
				throw new Exception("属性数量错误 equipID=" + equipID);
			}
			try {
				allEffects = KGameUtilTool.genAttribute(attribute_id, attribute, false);
			} catch (Exception e) {
				throw new Exception(e.getMessage() + " equipID=" + equipID);
			}
		}

		void notifyCacheLoadComplete() throws Exception {

			quaEnum = KItemQualityEnum.getEnum(qua);
			if (quaEnum == null) {
				throw new Exception("qua错误 =" + qua);
			}

			extName = HyperTextTool.extColor(name, quaEnum.color);

			{
				Set<String> muiSet = new HashSet<String>();
				if (!itemTempId1.isEmpty() && itemCount1 > 0) {
					ItemCountStruct itemStruct = new ItemCountStruct(itemTempId1, itemCount1);
					if (itemStruct.getItemTemplate() == null) {
						throw new Exception("物品不存在 itemTempId1=" + itemTempId1);
					}
					itemList.add(itemStruct);
					if (!muiSet.add(itemTempId1)) {
						throw new Exception("物品重复 itemTempId1=" + itemTempId1);
					}
				}
				if (!itemTempId2.isEmpty() && itemCount2 > 0) {
					ItemCountStruct itemStruct = new ItemCountStruct(itemTempId2, itemCount2);
					if (itemStruct.getItemTemplate() == null) {
						throw new Exception("物品不存在 itemTempId2=" + itemTempId2);
					}
					itemList.add(itemStruct);
					if (!muiSet.add(itemTempId2)) {
						throw new Exception("物品重复 itemTempId2=" + itemTempId2);
					}
				}
				if (!itemTempId3.isEmpty() && itemCount3 > 0) {
					ItemCountStruct itemStruct = new ItemCountStruct(itemTempId3, itemCount3);
					if (itemStruct.getItemTemplate() == null) {
						throw new Exception("物品不存在 itemTempId3=" + itemTempId3);
					}
					itemList.add(itemStruct);
					if (!muiSet.add(itemTempId3)) {
						throw new Exception("物品重复 itemTempId3=" + itemTempId3);
					}
				}
				if (itemList.isEmpty()) {
					itemList = Collections.emptyList();
				}
				for (ItemCountStruct s : itemList) {
					itemCountMap.put(s.itemCode, (int) s.itemCount);
					itemMap.put(s.itemCode, s);
				}
			}

			{
				Set<KCurrencyTypeEnum> muiSet = new HashSet<KCurrencyTypeEnum>();
				if (moneyType1 > 0 && moneyCount1 > 0) {
					KCurrencyTypeEnum type = KCurrencyTypeEnum.getEnum(moneyType1);
					if (type == null) {
						throw new Exception("货币类型不存在 moneyType1=" + moneyType1);
					}
					moneyList.add(new KCurrencyCountStruct(type, moneyCount1));
					if (!muiSet.add(type)) {
						throw new Exception("货币类型重复 moneyType1=" + moneyType1);
					}
				}
				if (moneyType2 > 0) {
					KCurrencyTypeEnum type = KCurrencyTypeEnum.getEnum(moneyType2);
					if (type == null) {
						throw new Exception("货币类型不存在 moneyType2=" + moneyType2);
					}
					moneyList.add(new KCurrencyCountStruct(type, moneyCount2));
					if (!muiSet.add(type)) {
						throw new Exception("货币类型重复 moneyType2=" + moneyType2);
					}
				}
				if (moneyList.isEmpty()) {
					moneyList = Collections.emptyList();
				}
			}
		}
	}

	/**
	 * <pre>
	 * [重置sp点花费]
	 * 
	 * @author CamusHuang
	 * @creation 2015-1-5 下午7:36:14
	 * </pre>
	 */
	public static class KMountResetSPData {
		// ----------以下是EXCEL表格直导数据---------
		public int lv;// 等级
		private int moneyType1;// 货币类型1
		private int moneyCount1;// 货币数量1
		private int moneyType2;// 货币类型2
		private int moneyCount2;// 货币数量2
		// ----------以下是逻辑数据---------
		public List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();
		public KCurrencyCountStruct diamond;

		void notifyCacheLoadComplete() throws Exception {
			if (lv < 1) {
				throw new Exception("lv错误 =" + lv);
			}
			{
				Set<KCurrencyTypeEnum> moneyTypeSet = new HashSet<KCurrencyTypeEnum>();
				if (moneyType1 > 0 && moneyCount1 > 0) {
					KCurrencyTypeEnum type = KCurrencyTypeEnum.getEnum(moneyType1);
					if (type == null) {
						throw new Exception("货币类型不存在 moneyType1=" + moneyType1);
					}
					KCurrencyCountStruct temp = new KCurrencyCountStruct(type, moneyCount1);
					moneyList.add(temp);
					if (type == KCurrencyTypeEnum.DIAMOND) {
						diamond = temp;
					}
					moneyTypeSet.add(type);
				}
				if (moneyType2 > 0) {
					KCurrencyTypeEnum type = KCurrencyTypeEnum.getEnum(moneyType2);
					if (type == null) {
						throw new Exception("货币类型不存在 moneyType2=" + moneyType2);
					}
					KCurrencyCountStruct temp = new KCurrencyCountStruct(type, moneyCount2);
					moneyList.add(temp);
					if (type == KCurrencyTypeEnum.DIAMOND) {
						diamond = temp;
					}
					if (!moneyTypeSet.add(type)) {
						throw new Exception("货币类型重复 = " + type.name);
					}
				}
				if (moneyList.isEmpty()) {
					throw new Exception("货币消耗未配置");
				}
			}
		}
	}
}
