package com.kola.kmp.logic.reward.garden;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.management.timer.Timer;

import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.ReflectPaser;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KItemQualityEnum;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.reward.garden.KGardenDataManager.KGardenCommonTreeDataManager.GardenCommonRewardData;
import com.kola.kmp.logic.reward.garden.KGardenDataManager.KGardenRoleRateDataManager.GardenRoleRateData;
import com.kola.kmp.logic.reward.garden.KGardenDataManager.KGardenTopTreeDataManager.GardenTopRewardData;
import com.kola.kmp.logic.reward.garden.KGardenDataManager.KTreeRipeTimeDataManager.TreeRipeTimeData;
import com.kola.kmp.logic.role.KRoleModuleConfig;
import com.kola.kmp.logic.util.tips.RewardTips;

/**
 * <pre>
 * 本类负责管理本模块的公共数据 
 * 
 * @author CamusHuang
 * @creation 2013-4-8 下午2:39:56
 * </pre>
 */
public class KGardenDataManager {

	/** 脚印日志的最大数量 */
	static int FeetLogMaxCount = 10;
	/** 僵尸刷新任务周期（毫秒） */
	static long ZombieRefreshPeriod = 2 * Timer.ONE_HOUR;

	/** 普通奖励的起始结束类型值 */
	static final int TYPE_COMMON_MIN = 1;
	static final int TYPE_COMMON_MAX = 6;
	/** 高级奖励的起始结束类型值 */
	public static final int TYPE_TOP_MIN = 7;// 金果
	static final int TYPE_TOP_MAX = 8;// 银果
	// 高级奖励>=此品质才会世界广播
	public static final KItemQualityEnum TOP_BROCAST_QUALITY = KItemQualityEnum.史诗的;
	// ////////////////////////////////////////////////
	public static KCurrencyTypeEnum KillZombieRewardMoneyType;
	// 帮其他玩家进行清理僵尸获得金币=500*玩家等级清理金币系数
	public static long KillZombieMoneyBaseCountForOther;
	// 自己庄园清理获得金别=1000*玩家等级清理金币系数
	public static long KillZombieMoneyBaseCountForSelf;
	// 每天清理僵尸次数大于20次时，获得的金币奖励减少为1%。
	public static int KillZombieMoneyRewardMaxTimePerDay;
	public static float KillZombieMoneyRewardRate;
	//
	// 清理丧尸 有1%的概率 获得道具ID为: 370001 （获得道具提示）
	public static ItemCountStruct KillZombieItemReward;
	public static BaseMailRewardData KillZombieItemRewardMail = null;
	public static int KillZombieItemRewardRate;
	// 每天清理僵尸次数大于20次时，概率提升到2%。
	public static int KillZombieItemRewardMaxTimePerDay;
	public static int KillZombieItemRewardOtherRate;
	// ////////////////////////////////////////////////
	// 每日浇灌次数上限
	public static int SpeedTimePerDay;
	// 每次浇灌奖励体力
	public static int SpeedRewardPhyPower;
	// 浇灌获得金币奖励(玩家等级*1000)
	public static KCurrencyTypeEnum SpeedRewardMoneyType;
	public static float SpeedRewardMoneyBase;
	// 玩家为同一个玩家浇灌冷却时间为30分钟
	public static long SpeedForOtherCD;
	// 玩家为自己浇灌冷却时间为2小时
	public static long SpeedForMySelfCD;

	// ////////////////////////////////////////////////

	static void loadConfig(Element e) throws KGameServerException {
		ZombieRefreshPeriod = UtilTool.parseDHMS(e.getChildTextTrim("ZombieRefreshPeriod"));
		FeetLogMaxCount = Integer.parseInt(e.getChildTextTrim("FeetLogMaxCount"));

		{
			Element tempE1 = e.getChild("KillZombieReward");
			{
				Element tempE = tempE1.getChild("KillZombieMoneyReward");
				
				KillZombieRewardMoneyType = KCurrencyTypeEnum.getEnum(Integer.parseInt(tempE.getAttributeValue("MoneyType")));
				KillZombieMoneyBaseCountForOther = Long.parseLong(tempE.getAttributeValue("Other"));
				KillZombieMoneyBaseCountForSelf = Long.parseLong(tempE.getAttributeValue("Self"));

				KillZombieMoneyRewardMaxTimePerDay = Integer.parseInt(tempE.getChildTextTrim("KillZombieMaxTimePerDay"));
				KillZombieMoneyRewardRate = Float.parseFloat(tempE.getChildTextTrim("KillZombieMaxTimeMoneyCountRate"));
			}
			{
				Element tempE = tempE1.getChild("KillZombieItemReward");
				KillZombieItemRewardRate = Integer.parseInt(tempE.getAttributeValue("rate"));
				if (KillZombieItemRewardRate < 0 || KillZombieItemRewardRate >= 100) {
					throw new KGameServerException("加载庄园数据错误 KillZombieItemReward.rate=" + KillZombieItemRewardRate);
				}

				String itemCode = tempE.getAttributeValue("itemCode");
				int itemCount = Integer.parseInt(tempE.getAttributeValue("itemCount"));
				if (itemCount < 1) {
					throw new KGameServerException("加载庄园数据错误 KillZombieItemReward.itemCount=" + itemCount);
				}
				KillZombieItemReward = new ItemCountStruct(itemCode, itemCount);

				BaseMailContent baseMail = new BaseMailContent(RewardTips.庄园灭尸特殊奖励邮件标题, RewardTips.庄园灭尸特殊奖励邮件内容, null, null);
				BaseRewardData baseMailReward = new BaseRewardData(null, null, Arrays.asList(KillZombieItemReward), null, null);
				KillZombieItemRewardMail = new BaseMailRewardData(1, baseMail, baseMailReward);
				
				//
				KillZombieItemRewardMaxTimePerDay = Integer.parseInt(tempE.getChild("KillZombieMaxTimePerDay").getTextTrim());
				KillZombieItemRewardOtherRate = Integer.parseInt(tempE.getChild("OtherRate").getTextTrim());
				if (KillZombieItemRewardMaxTimePerDay <= 1) {
					throw new KGameServerException("加载庄园数据错误 KillZombieItemReward.KillZombieMaxTimePerDay=" + KillZombieItemRewardMaxTimePerDay);
				}
				if (KillZombieItemRewardOtherRate < KillZombieItemRewardRate) {
					throw new KGameServerException("加载庄园数据错误 KillZombieItemReward.OtherRate=" + KillZombieItemRewardOtherRate);
				}
			}
		}

		{
			Element tempE = e.getChild("Speed");
			SpeedTimePerDay = Integer.parseInt(tempE.getChildTextTrim("SpeedTimePerDay"));
			SpeedRewardPhyPower = Integer.parseInt(tempE.getChildTextTrim("SpeedRewardPhyPower"));
			SpeedForOtherCD = UtilTool.parseDHMS(tempE.getChildTextTrim("SpeedForOtherCD"));
			SpeedForMySelfCD = UtilTool.parseDHMS(tempE.getChildTextTrim("SpeedForMySelfCD"));

			tempE = tempE.getChild("SpeedRewardMoney");
			SpeedRewardMoneyType = KCurrencyTypeEnum.getEnum(Integer.parseInt(tempE.getAttributeValue("MoneyType")));
			SpeedRewardMoneyBase = Integer.parseInt(tempE.getAttributeValue("Base"));
		}
	}

	//
	static final String SheetName_植物成熟时间 = "植物成熟时间";
	static final String SheetName_保卫庄园普通奖励 = "保卫庄园普通奖励";
	static final String SheetName_保卫庄园银果奖励 = "保卫庄园银果奖励";
	static final String SheetName_保卫庄园等级系数 = "保卫庄园等级系数";

	/**
	 * <pre>
	 * 开始加载文件
	 * @param configPath
	 * @author CamusHuang
	 * @creation 2012-11-9 下午7:38:27
	 * </pre>
	 */
	static void goToLoadData(KGameExcelFile file, int HeaderIndex) throws Exception {

		// 加载数据
		{
			loadTreeRipeTimeDatas(file.getTable(SheetName_植物成熟时间, HeaderIndex));

			loadCommonTreeDatas(file.getTable(SheetName_保卫庄园普通奖励, HeaderIndex));

			loadTopTreeDatas(file.getTable(SheetName_保卫庄园银果奖励, HeaderIndex));

			loadRoleRateDatas(file.getTable(SheetName_保卫庄园等级系数, HeaderIndex));
		}
	}

	private static void loadTreeRipeTimeDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<TreeRipeTimeData> datas = ReflectPaser.parseExcelData(TreeRipeTimeData.class, table.getHeaderNames(), rows, true);
		mTreeRipeTimeDataManager.init(datas);
	}

	/**
	 * <pre>
	 * 
	 * @param rows
	 * @throws Exception
	 * @throws Exception
	 * @author CamusHuang
	 * @creation 2013-1-24 下午12:14:53
	 * </pre>
	 */
	private static void loadCommonTreeDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<GardenCommonRewardData> datas = ReflectPaser.parseExcelData(GardenCommonRewardData.class, table.getHeaderNames(), rows, true);
		mGardenCommonTreeDataManager.init(datas);
	}

	/**
	 * <pre>
	 * 
	 * @param rows
	 * @throws Exception
	 * @throws Exception
	 * @author CamusHuang
	 * @creation 2013-1-24 下午12:14:53
	 * </pre>
	 */
	private static void loadTopTreeDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<GardenTopRewardData> datas = ReflectPaser.parseExcelData(GardenTopRewardData.class, table.getHeaderNames(), rows, true);
		mGardenTopTreeDataManager.init(datas);
	}

	private static void loadRoleRateDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<GardenRoleRateData> datas = ReflectPaser.parseExcelData(GardenRoleRateData.class, table.getHeaderNames(), rows, true);
		mGardenRoleRateDataManager.init(datas);
	}

	// /////////////////////////////////////////
	/**
	 * <pre>
	 * 植物成熟时长数据
	 * </pre>
	 */
	public static KTreeRipeTimeDataManager mTreeRipeTimeDataManager = new KTreeRipeTimeDataManager();

	/**
	 * <pre>
	 * 庄园普通奖励数据
	 * </pre>
	 */
	public static KGardenCommonTreeDataManager mGardenCommonTreeDataManager = new KGardenCommonTreeDataManager();

	/**
	 * <pre>
	 * 庄园特殊奖励数据
	 * </pre>
	 */
	public static KGardenTopTreeDataManager mGardenTopTreeDataManager = new KGardenTopTreeDataManager();

	/**
	 * <pre>
	 * 角色等级系数数据
	 * </pre>
	 */
	public static KGardenRoleRateDataManager mGardenRoleRateDataManager = new KGardenRoleRateDataManager();

	/**
	 * <pre>
	 * 植物成熟时长数据
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-8 下午2:40:38
	 * </pre>
	 */
	public static class KTreeRipeTimeDataManager {
		/**
		 * <pre>
		 * KEY = 类型
		 * </pre>
		 */
		private LinkedHashMap<Integer, TreeRipeTimeData> dataMap = new LinkedHashMap<Integer, TreeRipeTimeData>();

		void init(List<TreeRipeTimeData> datas) throws Exception {
			for (TreeRipeTimeData data : datas) {
				if (dataMap.put(data.id, data) != null) {
					throw new KGameServerException("类型重复 type=" + data.id);
				}
			}
		}

		/**
		 * <pre>
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2013-4-23 下午7:53:53
		 * </pre>
		 */
		public TreeRipeTimeData getData(int type) {
			return dataMap.get(type);
		}

		/**
		 * <pre>
		 * 
		 * @deprecated 直接获取缓存，谨慎使用
		 * @author CamusHuang
		 * @creation 2014-4-25 下午12:21:21
		 * </pre>
		 */
		public LinkedHashMap<Integer, TreeRipeTimeData> getDataCache() {
			return dataMap;
		}

		/**
		 * <pre>
		 * 验证数据/跨模块操作
		 * 
		 * @throws KGameServerException
		 * @author CamusHuang
		 * @creation 2013-4-25 下午4:26:38
		 * </pre>
		 */
		void notifyCacheLoadComplete() throws KGameServerException {
			if (dataMap.size() != TYPE_TOP_MAX) {
				throw new KGameServerException("植物数量必须为" + TYPE_TOP_MAX);
			}
			for (int type = TYPE_COMMON_MIN; type <= TYPE_TOP_MAX; type++) {
				TreeRipeTimeData data = dataMap.get(type);
				if (data == null) {
					throw new KGameServerException("植物成熟时间 缺少type=" + type);
				}
				data.notifyCacheLoadComplete();
			}
		}

		public static class TreeRipeTimeData {
			// ----------以下是EXCEL表格直导数据---------
			public int id;// 植物编号
			private int time;// 成熟时间(s)
			private int Watering;// 浇灌时间减少(s)
			// ----------以下是逻辑数据---------
			public long ripeTime;// 成熟时间(ms)
			public long speedTime;// 浇灌时间减少(ms)

			void notifyCacheLoadComplete() throws KGameServerException {
				ripeTime = time * Timer.ONE_SECOND;
				if (ripeTime < Timer.ONE_MINUTE) {
					throw new KGameServerException("数据错误 time=" + time);
				}
				//
				if(Watering!=0){
					speedTime = Watering * Timer.ONE_SECOND;
					if (speedTime < Timer.ONE_MINUTE) {
						throw new KGameServerException("数据错误 Watering=" + Watering);
					}
				}
			}
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 庄园普通奖励数据
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-8 下午2:40:38
	 * </pre>
	 */
	public static class KGardenCommonTreeDataManager {
		/**
		 * <pre>
		 * KEY = 类型
		 * </pre>
		 */
		private LinkedHashMap<Integer, GardenCommonRewardData> dataMap = new LinkedHashMap<Integer, GardenCommonRewardData>();

		void init(List<GardenCommonRewardData> datas) throws Exception {
			for (GardenCommonRewardData data : datas) {
				if (dataMap.put(data.type, data) != null) {
					throw new KGameServerException("类型重复 type=" + data.type);
				}
			}
		}

		/**
		 * <pre>
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2013-4-23 下午7:53:53
		 * </pre>
		 */
		public GardenCommonRewardData getData(int type) {
			return dataMap.get(type);
		}

		/**
		 * <pre>
		 * 
		 * @deprecated 直接获取缓存，谨慎使用
		 * @author CamusHuang
		 * @creation 2014-4-25 下午12:21:21
		 * </pre>
		 */
		public LinkedHashMap<Integer, GardenCommonRewardData> getDataCache() {
			return dataMap;
		}

		/**
		 * <pre>
		 * 验证数据/跨模块操作
		 * 
		 * @throws KGameServerException
		 * @author CamusHuang
		 * @creation 2013-4-25 下午4:26:38
		 * </pre>
		 */
		void notifyCacheLoadComplete() throws KGameServerException {
			if (dataMap.size() != TYPE_COMMON_MAX) {
				throw new KGameServerException("普通奖励数量必须为" + TYPE_COMMON_MAX);
			}
			for (int type = TYPE_COMMON_MIN; type <= TYPE_COMMON_MAX; type++) {
				GardenCommonRewardData data = dataMap.get(type);
				if (data == null) {
					throw new KGameServerException("普通奖励数量 缺少=" + type);
				}
				data.notifyCacheLoadComplete();
			}
		}

		// //////////////////////////
		/**
		 * <pre>
		 * 庄园普通奖励
		 * 
		 * @author CamusHuang
		 * @creation 2014-2-21 下午3:10:02
		 * </pre>
		 */
		public static class GardenCommonRewardData {
			// ----------以下是EXCEL表格直导数据---------
			public int type;// ID
			public String name;// 植物名称
			public int icon;// 植物资源
			private int moneyType;// 获得资源类型
			private int moneyCount;// 获得货币数量
			public int Corpsechance;// 随机出现僵尸概率

			// ----------以下是逻辑数据---------
			public KCurrencyCountStruct addMoney;

			void notifyCacheLoadComplete() throws KGameServerException {
				if (moneyCount < 1) {
					throw new KGameServerException("数值错误 moneyCount = " + moneyCount);
				}
				addMoney = new KCurrencyCountStruct(KCurrencyTypeEnum.getEnum(moneyType), moneyCount);
				if (addMoney.currencyType == null) {
					throw new KGameServerException("货币类型错误=" + moneyType + " type=" + type);
				}
				// 必须在GardenRoleRateData中存在
				GardenRoleRateData rateData = mGardenRoleRateDataManager.getData(1);
				if (!rateData.moneyRateMap.containsKey(addMoney.currencyType)) {
					throw new KGameServerException("不匹配的货币类型=" + moneyType + " type=" + type);
				}
				// CTODO 其它约束检查
			}

		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 庄园特殊奖励数据
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-8 下午2:40:38
	 * </pre>
	 */
	public static class KGardenTopTreeDataManager {
		/**
		 * <pre>
		 * <类型,<角色等级,数据>>
		 * </pre>
		 */
		private LinkedHashMap<Integer, Map<Integer, GardenTopRewardData>> dataMap = new LinkedHashMap<Integer, Map<Integer, GardenTopRewardData>>();

		void init(List<GardenTopRewardData> datas) throws Exception {
			for (GardenTopRewardData data : datas) {
				Map<Integer, GardenTopRewardData> typeMap = dataMap.get(data.type);
				if (typeMap == null) {
					typeMap = new HashMap<Integer, GardenTopRewardData>();
					dataMap.put(data.type, typeMap);
				}

				for (int lv = data.mixlvl; lv <= data.maxlvl; lv++) {
					if (typeMap.put(lv, data) != null) {
						throw new KGameServerException("等级重复 type=" + data.type + " lv=" + lv);
					}
				}
			}
		}

		/**
		 * <pre>
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2013-4-23 下午7:53:53
		 * </pre>
		 */
		public GardenTopRewardData getData(int type, int roleLv) {
			Map<Integer, GardenTopRewardData> typeMap = dataMap.get(type);
			if (typeMap == null) {
				return null;
			}
			return typeMap.get(roleLv);
		}

		/**
		 * <pre>
		 * 
		 * @deprecated 直接获取缓存，谨慎使用
		 * @author CamusHuang
		 * @creation 2014-4-25 下午12:21:21
		 * </pre>
		 */
		public LinkedHashMap<Integer, Map<Integer, GardenTopRewardData>> getDataCache() {
			return dataMap;
		}

		/**
		 * <pre>
		 * 验证数据/跨模块操作
		 * 
		 * @throws KGameServerException
		 * @author CamusHuang
		 * @creation 2013-4-25 下午4:26:38
		 * </pre>
		 */
		void notifyCacheLoadComplete() throws KGameServerException {

			if (dataMap.size() != (TYPE_TOP_MAX - TYPE_TOP_MIN + 1)) {
				throw new KGameServerException("特殊奖励数量必须为" + (TYPE_TOP_MAX - TYPE_TOP_MIN + 1));
			}
			int maxRoleLv = KRoleModuleConfig.getRoleMaxLv();
			for (int type = TYPE_TOP_MIN; type <= TYPE_TOP_MAX; type++) {
				Map<Integer, GardenTopRewardData> tempMap = dataMap.get(type);
				if (tempMap == null) {
					throw new KGameServerException("特殊奖励数量 缺少=" + type);
				}
				for (int lv = 1; lv <= maxRoleLv; lv++) {
					GardenTopRewardData data = tempMap.get(lv);
					if (data == null) {
						throw new KGameServerException("缺少等级 type=" + type + " lv=" + lv);
					}
					data.notifyCacheLoadComplete();
				}
			}
		}

		// //////////////////////////
		/**
		 * <pre>
		 * 庄园特殊奖励
		 * 
		 * @author CamusHuang
		 * @creation 2014-2-21 下午3:10:02
		 * </pre>
		 */
		public static class GardenTopRewardData {
			// ----------以下是EXCEL表格直导数据---------
			public int type;// ID
			public String name;// 植物名称
			public int icon;// 植物资源
			public int mixlvl;// 最低等级
			public int maxlvl;// 最高等级
			private String[] ItemID;// 物品ID
			public int timeBase;// 时间基数

			// ----------以下是逻辑数据---------
			public List<ItemCountStruct> addItems = Collections.emptyList();
			public List<Integer> addItemRates = Collections.emptyList();
			public int allRate = 0;

			void notifyCacheLoadComplete() throws KGameServerException {
				if (mixlvl < 1) {
					throw new KGameServerException("数值错误 mixlvl = " + mixlvl);
				}
				if (maxlvl < 1) {
					throw new KGameServerException("数值错误 maxlvl = " + maxlvl);
				}

				addItems = new ArrayList<ItemCountStruct>();
				addItemRates = new ArrayList<Integer>();
				allRate = ItemCountStruct.paramsItems(ItemID, addItems, addItemRates, 1);
				// CTODO 其它约束检查
			}
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 角色等级系数数据
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-8 下午2:40:38
	 * </pre>
	 */
	public static class KGardenRoleRateDataManager {
		/**
		 * <pre>
		 * KEY = 角色等级
		 * </pre>
		 */
		private LinkedHashMap<Integer, GardenRoleRateData> dataMap = new LinkedHashMap<Integer, GardenRoleRateData>();

		void init(List<GardenRoleRateData> datas) throws Exception {
			for (GardenRoleRateData data : datas) {
				if (dataMap.put(data.level, data) != null) {
					throw new KGameServerException("等级重复 level=" + data.level);
				}
			}

		}

		/**
		 * <pre>
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2013-4-23 下午7:53:53
		 * </pre>
		 */
		public GardenRoleRateData getData(int roleLv) {
			return dataMap.get(roleLv);
		}

		/**
		 * <pre>
		 * 
		 * @deprecated 直接获取缓存，谨慎使用
		 * @author CamusHuang
		 * @creation 2014-4-25 下午12:21:21
		 * </pre>
		 */
		public LinkedHashMap<Integer, GardenRoleRateData> getDataCache() {
			return dataMap;
		}

		/**
		 * <pre>
		 * 验证数据/跨模块操作
		 * 
		 * @throws KGameServerException
		 * @author CamusHuang
		 * @creation 2013-4-25 下午4:26:38
		 * </pre>
		 */
		void notifyCacheLoadComplete() throws KGameServerException {
			int maxRoleLv = KRoleModuleConfig.getRoleMaxLv();
			for (int lv = 1; lv <= maxRoleLv; lv++) {
				GardenRoleRateData data = dataMap.get(lv);
				if (data == null) {
					throw new KGameServerException("缺少等级 lv=" + lv);
				}
				data.notifyCacheLoadComplete();
			}
		}

		// //////////////////////////
		/**
		 * <pre>
		 * 角色等级系数
		 * 
		 * @author CamusHuang
		 * @creation 2014-2-21 下午3:10:02
		 * </pre>
		 */
		public static class GardenRoleRateData {
			// ----------以下是EXCEL表格直导数据---------
			public int level;// 角色等级
			private float goldpoint;// 金币奖励系数
			private float potentialpoint;// 潜能奖励系数
			private float honorpoint;// 荣誉奖励系数
			public float clearpoint;// 清理僵尸金币系数

			// ----------以下是逻辑数据---------
			public Map<KCurrencyTypeEnum, Float> moneyRateMap = new HashMap<KCurrencyTypeEnum, Float>();

			void notifyCacheLoadComplete() throws KGameServerException {
				if (level < 1) {
					throw new KGameServerException("数值错误 level = " + level);
				}
				if (goldpoint <= 0) {
					throw new KGameServerException("数值错误 goldpoint = " + goldpoint);
				}
				if (potentialpoint <= 0) {
					throw new KGameServerException("数值错误 potentialpoint = " + potentialpoint);
				}
				if (honorpoint <= 0) {
					throw new KGameServerException("数值错误 honorpoint = " + honorpoint);
				}
				moneyRateMap.put(KCurrencyTypeEnum.GOLD, goldpoint);
				moneyRateMap.put(KCurrencyTypeEnum.POTENTIAL, potentialpoint);
				moneyRateMap.put(KCurrencyTypeEnum.SCORE, honorpoint);
				if (clearpoint <= 0) {
					throw new KGameServerException("数值错误 clearpoint = " + clearpoint);
				}
				// CTODO 其它约束检查
			}
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
	static void notifyCacheLoadComplete() throws KGameServerException {

		try {
			mGardenRoleRateDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_保卫庄园等级系数 + "]错误：" + e.getMessage(), e);
		}

		try {
			mTreeRipeTimeDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_植物成熟时间 + "]错误：" + e.getMessage(), e);
		}

		try {
			mGardenCommonTreeDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_保卫庄园普通奖励 + "]错误：" + e.getMessage(), e);
		}

		try {
			mGardenTopTreeDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_保卫庄园银果奖励 + "]错误：" + e.getMessage(), e);
		}

		{
			if (KillZombieItemReward.getItemTemplate() == null) {
				throw new KGameServerException("加载庄园配置错误 杀僵尸随机物品奖励不存在 itemCode=" + KillZombieItemReward.itemCode);
			}
		}
	}
}
