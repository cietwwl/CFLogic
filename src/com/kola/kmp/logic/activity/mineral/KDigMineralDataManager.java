package com.kola.kmp.logic.activity.mineral;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.timer.Timer;

import org.jdom.Element;

import com.google.code.hs4j.network.util.ConcurrentHashSet;
import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.ReflectPaser;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.activity.mineral.KDigMineralDataManager.GoldBaseDataManger.KMineGoldBaseData;
import com.kola.kmp.logic.activity.mineral.KDigMineralDataManager.MineralTempDataManger.KMineralTemplate;
import com.kola.kmp.logic.competition.KCompetitionBattlefield;
import com.kola.kmp.logic.item.KItemDataManager;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMap;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMapBornPoint;
import com.kola.kmp.logic.npc.KNPCDataStructs.KNPCTemplate;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KItemTypeEnum;
import com.kola.kmp.logic.role.KRoleModuleConfig;
import com.kola.kmp.logic.support.KSupportFactory;

public class KDigMineralDataManager {

	private static int 挖矿场景地图;// 22001
	public static KDuplicateMap mineMap;// 当前场景的地图
	public static KDuplicateMapBornPoint mineBornPoint;// 当前场景的地图出生点

	public static String 挖矿PVP地图文件名;
	public static int 挖矿PVP地图背景音乐;
	public static int 驱赶名单最大数量;

	public static long 产出周期;// 毫秒

	public static long 初次同步周围玩家延时 = 4 * Timer.ONE_SECOND;// 毫秒

	public static String 铁镐ItemCode;

	public static final int 奖励日志最大数量 = 10;

	// 用于切磋PVP的战斗地图
	static final KCompetitionBattlefield PVPBattlefield = new KCompetitionBattlefield();
	// 签名字数限制
	static final int DeclareMaxSize = 70;

	static void loadConfig(Element logicE) throws KGameServerException {

		挖矿场景地图 = Integer.parseInt(logicE.getChildTextTrim("挖矿场景地图"));
		挖矿PVP地图文件名 = logicE.getChildTextTrim("挖矿PVP地图文件名");
		挖矿PVP地图背景音乐 = Integer.parseInt(logicE.getChildTextTrim("挖矿PVP地图背景音乐"));

		驱赶名单最大数量 = Integer.parseInt(logicE.getChildTextTrim("驱赶名单最大数量"));
		if (驱赶名单最大数量 > Byte.MAX_VALUE) {
			throw new KGameServerException("驱赶名单最大数量 不能大于" + Byte.MAX_VALUE);
		}

		产出周期 = UtilTool.parseDHMS(logicE.getChildTextTrim("产出周期"));

		铁镐ItemCode = logicE.getChildTextTrim("铁镐ItemCode");

		if (挖矿PVP地图文件名 == null) {
			throw new KGameServerException("挖矿PVP 战斗场景不存在");
		}

		PVPBattlefield.initBattlefield(挖矿PVP地图文件名, 挖矿PVP地图背景音乐);
	}

	//
	static final String SheetName_矿点参数 = "矿点参数";
	static final String SheetName_金币基数 = "金币基数";

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
			loadMineTemplateDatas(file.getTable(SheetName_矿点参数, HeaderIndex));

			loadGoldBaseDatas(file.getTable(SheetName_金币基数, HeaderIndex));
		}
	}

	/**
	 * <pre>
	 * 
	 * @param table
	 * @throws Exception
	 * @author CamusHuang
	 * @creation 2014-12-5 下午4:57:50
	 * </pre>
	 */
	private static void loadMineTemplateDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		try {
			List<KMineralTemplate> datas = new ArrayList<KMineralTemplate>();
			//
			for (KGameExcelRow row : rows) {
				KMineralTemplate template = new KMineralTemplate(row);
				mMineralTempDataManger.addData(template);
			}
		} catch (Exception e) {
			throw new Exception("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
		}
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
	private static void loadGoldBaseDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<KMineGoldBaseData> datas = new ArrayList<KMineGoldBaseData>();
		for (KGameExcelRow row : rows) {
			KMineGoldBaseData obj = new KMineGoldBaseData();
			ReflectPaser.parseExcelData(obj, row, false);

			for (int i = 1; i < 100; i++) {
				String colName = "itemBase" + i;
				if (!row.containsCol(colName)) {
					break;
				}
				String temp = row.getData(colName);
				if (temp.isEmpty()) {
					break;
				}
				float value = Float.parseFloat(temp);
				if (value <= 0) {
					throw new KGameServerException(colName + " = " + value);
				}
				obj.itemBaseRateList.add(value);
			}
			if (obj.itemBaseRateList.isEmpty()) {
				throw new KGameServerException("itemBase数据缺失");
			}
			
			datas.add(obj);
		}

		mGoldBaseDataManger.init(datas);
	}

	// /////////////////////////////////////////

	// 矿区模板数据管理器
	static MineralTempDataManger mMineralTempDataManger = new MineralTempDataManger();

	// 矿区数据管理器
	static MineralDataManger mMineralDataManger = new MineralDataManger();

	// 金币基数数据管理器
	static GoldBaseDataManger mGoldBaseDataManger = new GoldBaseDataManger();

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 矿区数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2014-12-5 下午4:56:23
	 * </pre>
	 */
	public static class MineralDataManger {
		// <mineType, KMineral>
		private Map<Integer, KMineral> mineralMap = new HashMap<Integer, KMineral>();

		KMineral getMineral(int mineType) {
			return mineralMap.get(mineType);
		}

		Map<Integer, KMineral> getDataCache() {
			return mineralMap;
		}

		void removeDiggerFromAllMineral(long roleId) {
			for (KMineral mine : mineralMap.values()) {
				mine.removeDigger(roleId);
			}
		}

		void notifyCacheLoadComplete() throws KGameServerException {

			for (KMineralTemplate temp : mMineralTempDataManger.map.values()) {
				mineralMap.put(temp.mineType, new KMineral(temp));
			}
		}

		/**
		 * 
		 * 矿石数据结构
		 * 
		 * @author PERRY
		 * 
		 */
		public static class KMineral {

			public final ReentrantLock rwLock = new ReentrantLock();

			public final KMineralTemplate template;
			// 正在本矿进行作业的矿工角色
			private Set<Long> diggerSet = new ConcurrentHashSet<Long>();

			KMineral(KMineralTemplate template) {
				this.template = template;
			}

			int getDiggerSize() {
				return diggerSet.size();
			}

			boolean containDigger(long roleId) {
				return diggerSet.contains(roleId);
			}

			boolean removeDigger(long roleId) {
				return diggerSet.remove(roleId);
			}

			boolean addDigger(long roleId) {
				return diggerSet.add(roleId);
			}

			/**
			 * @deprecated 请谨慎使用
			 * @return
			 */
			Set<Long> getAllDiggerCache() {
				return diggerSet;
			}
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 矿区模板数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2014-12-5 下午4:56:23
	 * </pre>
	 */
	public static class MineralTempDataManger {
		// <mineType, KMineralTemplate>
		private Map<Integer, KMineralTemplate> map = new HashMap<Integer, KMineralTemplate>();

		void addData(KMineralTemplate data) throws Exception {
			if (map.put(data.mineType, data) != null) {
				throw new Exception("矿区模板ID重复=" + data.mineType);
			}
		}

		KMineralTemplate getTemplate(int mineType) {
			return map.get(mineType);
		}

		void notifyCacheLoadComplete() throws KGameServerException {

			for (KMineralTemplate temp : map.values()) {
				temp.notifyCacheLoadComplete();
			}
		}

		/**
		 * 
		 * 矿石模板
		 * 
		 * @author PERRy
		 * 
		 */
		static class KMineralTemplate {

			public final int mineType;
			public String mineName;
			public final int maxDiggerCount;
			// public final boolean isCanBanish;//是否允许驱赶
			public final float goldPara;
			public final List<MineralItemStruct> itemList;

			public KMineralTemplate(KGameExcelRow row) {
				this.mineType = row.getInt("MineType");
				// this.mineName = row.getData("MineName");
				this.maxDiggerCount = row.getInt("LimitNumber");
				// this.isCanBanish = row.getBoolean("isCanBanish");
				this.goldPara = row.getFloat("GoldCoefficient");

				List<MineralItemStruct> tempItemList = new ArrayList<MineralItemStruct>();
				for (int i = 1; i <= 20; i++) {
					String colName = "itemTempId"+i;
					if(!row.containsCol(colName)){
						continue;
					}
					String itemCode = row.getData(colName);
					if(itemCode.isEmpty()){
						continue;
					}
					int itemCount = row.getInt("itemCount"+i);
					int rate = row.getInt("outputProbability"+i);
					tempItemList.add(new MineralItemStruct(rate, new ItemCountStruct(itemCode, itemCount)));
				}
				if (tempItemList.isEmpty()) {
					itemList = Collections.emptyList();
				} else {
					itemList = tempItemList;
				}
			}

			void notifyCacheLoadComplete() throws KGameServerException {

				if (mineType < 1) {
					throw new KGameServerException("mineType不能小于1 mineType=" + mineType);
				}

				for (MineralItemStruct struct : itemList) {
					if (struct.item.getItemTemplate() == null) {
						throw new KGameServerException("物品不存在 mineType=" + mineType + " itemCode=" + struct.item.itemCode);
					}

					if (struct.baseRate < 1) {
						throw new KGameServerException("物品概率错误  mineType=" + mineType + " itemCode=" + struct.item.itemCode);
					}
				}

				for (KMineGoldBaseData baseData : mGoldBaseDataManger.map.values()) {
					if (baseData.itemBaseRateList.size() < itemList.size()) {
						throw new KGameServerException("itemBase 参数数量与 物品数量不一致 lv=" + baseData.lv + " mineType=" + mineType);
					}
				}

				KNPCTemplate npc = KSupportFactory.getNpcModuleSupport().getNPCTemplate(mineType);
				if (npc == null) {
					throw new KGameServerException("NPC不存在 mineType=" + mineType);
				}
				mineName = npc.name;
			}

			List<ItemCountStruct> randomItem(KMineGoldBaseData data) {

				if (itemList.isEmpty()) {
					return Collections.emptyList();
				}

				List<ItemCountStruct> list = new ArrayList<ItemCountStruct>();

				for (int index = 0; index < itemList.size(); index++) {
					MineralItemStruct struct = itemList.get(index);

					float frate = data.itemBaseRateList.get(index);

					if (UtilTool.random(10000) <= (int) (struct.baseRate * frate)) {
						list.add(struct.item);
					}
				}
				return list.isEmpty() ? Collections.<ItemCountStruct> emptyList() : list;
			}

			long getGold(KMineGoldBaseData data) {
				return (long) (goldPara * data.GoldBase);
			}
		}

		static class MineralItemStruct {
			int baseRate;
			ItemCountStruct item;

			MineralItemStruct(int baseRate, ItemCountStruct item) {
				this.baseRate = baseRate;
				this.item = item;
			}
		}
	}

	// ///////////////////////////////////////////////////

	/**
	 * <pre>
	 * 金币基数数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2014-12-5 下午5:00:05
	 * </pre>
	 */
	public static class GoldBaseDataManger {
		// <角色等级, KMineGoldBaseData>
		private Map<Integer, KMineGoldBaseData> map = new HashMap<Integer, KMineGoldBaseData>();

		void init(List<KMineGoldBaseData> datas) throws Exception {
			for (KMineGoldBaseData data : datas) {
				if (map.put(data.lv, data) != null) {
					throw new Exception("金币基数 等级重复=" + data.lv);
				}
			}
		}

		KMineGoldBaseData getData(int roleLv) {
			return map.get(roleLv);
		}

		void notifyCacheLoadComplete() throws KGameServerException {
			int maxRoleLv = KRoleModuleConfig.getRoleMaxLv();
			for (int lv = 1; lv <= maxRoleLv; lv++) {
				KMineGoldBaseData data = map.get(lv);
				if (data == null) {
					throw new KGameServerException("缺少等级 = " + lv);
				}
				data.notifyCacheLoadComplete();
			}
		}

		/**
		 * <pre>
		 * 金币基数数据
		 * 
		 * @author CamusHuang
		 * @creation 2014-12-5 下午5:15:14
		 * </pre>
		 */
		public static class KMineGoldBaseData {

			public int lv;
			public int GoldBase;// 金币基数
			// ---------
			List<Float> itemBaseRateList = new ArrayList<Float>();

			void notifyCacheLoadComplete() throws KGameServerException {
				if (GoldBase < 1) {
					throw new KGameServerException("GoldBase = " + GoldBase);
				}
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
			mMineralTempDataManger.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_矿点参数 + "]错误：" + e.getMessage(), e);
		}

		try {
			mGoldBaseDataManger.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_金币基数 + "]错误：" + e.getMessage(), e);
		}

		mMineralDataManger.notifyCacheLoadComplete();

		KItemTempAbs temp = KItemDataManager.mItemTemplateManager.getItemTemplate(铁镐ItemCode);
		if (temp == null || temp.ItemType != KItemTypeEnum.改造材料) {
			throw new KGameServerException("铁镐模板ID错误=" + 铁镐ItemCode);
		}

		mineMap = KSupportFactory.getDuplicateMapSupport().createDuplicateMap(挖矿场景地图);
		if (mineMap == null) {
			throw new KGameServerException("挖矿场景地图 错误=" + 挖矿场景地图);
		}

		List<KDuplicateMapBornPoint> points = mineMap.getAllBornPointEntity();
		if (points.size() < 1) {
			throw new KGameServerException("挖矿场景地图 没有出生点");
		}
		mineBornPoint = points.get(0);
	}
}
