package com.kola.kmp.logic.gang.reswar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.timer.Timer;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.koala.game.util.StringUtil;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.util.ReflectPaser;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.gang.KGangDataManager;
import com.kola.kmp.logic.gang.reswar.KResWarDataManager.KCityTempManager.CityTemplate;
import com.kola.kmp.logic.gang.reswar.KResWarDataManager.KGangLvRewardBaseDataManager.GangLvData;
import com.kola.kmp.logic.gang.reswar.KResWarDataManager.KLevyRewardDataManager.LevyRewardData;
import com.kola.kmp.logic.gang.reswar.KResWarDataManager.KResPointTempManager.ResPointTemplate;
import com.kola.kmp.logic.gang.reswar.KResWarDataManager.KRewardDataManager.RewardData;
import com.kola.kmp.logic.gang.reswar.KResWarDataManager.KRoleLvRewardBaseDataManager.RoleLvData;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.role.KRoleModuleConfig;
import com.kola.kmp.logic.util.tips.GangResWarTips;

/**
 * <pre>
 * 本类负责管理本模块的公共数据 
 * 
 * @author CamusHuang
 * @creation 2013-4-8 下午2:39:56
 * </pre>
 */
public class KResWarDataManager {

	//
	static final String SheetName_城市 = "城市";
	static final String SheetName_资源点 = "资源点";
	static final String SheetName_奖励 = "奖励";
	static final String SheetName_征收奖励 = "征收奖励";
	static final String SheetName_角色等级 = "人物等级奖励基数";
	static final String SheetName_军团等级 = "军团等级奖励基数";

	/**
	 * <pre>
	 * 开始加载文件
	 * @param configPath
	 * @author CamusHuang
	 * @creation 2012-11-9 下午7:38:27
	 * </pre>
	 */
	static void goToLoadData(Element excelE) throws Exception {

		// 加载数据
		Element tempE = excelE.getChild("resWar");
		int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
		KGameExcelFile file = new KGameExcelFile(tempE.getChildTextTrim("path"));
		{
			loadCityDatas(file.getTable(SheetName_城市, HeaderIndex));

			loadResPointDatas(file.getTable(SheetName_资源点, HeaderIndex));

			loadRewardDatas(file.getTable(SheetName_奖励, HeaderIndex));

			loadLevyRewardDatas(file.getTable(SheetName_征收奖励, HeaderIndex));

			loadRoleLvDatas(file.getTable(SheetName_角色等级, HeaderIndex));
			
			loadGangLvDatas(file.getTable(SheetName_军团等级, HeaderIndex));
		}
	}

	private static void loadCityDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<CityTemplate> datas = ReflectPaser.parseExcelData(CityTemplate.class, table.getHeaderNames(), rows, true);
		mCityTempManager.init(datas);
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
	private static void loadResPointDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<ResPointTemplate> datas = ReflectPaser.parseExcelData(ResPointTemplate.class, table.getHeaderNames(), rows, true);
		mResPointTempManager.init(datas);
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
	private static void loadRewardDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<RewardData> datas = ReflectPaser.parseExcelData(RewardData.class, table.getHeaderNames(), rows, true);
		mRewardDataManager.init(datas);
	}

	private static void loadLevyRewardDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<LevyRewardData> datas = ReflectPaser.parseExcelData(LevyRewardData.class, table.getHeaderNames(), rows, true);
		mLevyRewardDataManager.init(datas);
	}

	private static void loadRoleLvDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<RoleLvData> datas = ReflectPaser.parseExcelData(RoleLvData.class, table.getHeaderNames(), rows, true);
		mRoleLvDataManager.init(datas);
	}
	
	private static void loadGangLvDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<GangLvData> datas = ReflectPaser.parseExcelData(GangLvData.class, table.getHeaderNames(), rows, true);
		mGangLvDataManager.init(datas);
	}

	// ///////////////////////////////////////
	/**
	 * <pre>
	 * 城市数据
	 * </pre>
	 */
	public static KCityTempManager mCityTempManager = new KCityTempManager();

	/**
	 * <pre>
	 * 资源点数据
	 * </pre>
	 */
	public static KResPointTempManager mResPointTempManager = new KResPointTempManager();

	/**
	 * <pre>
	 * 奖励
	 * </pre>
	 */
	public static KRewardDataManager mRewardDataManager = new KRewardDataManager();

	/**
	 * <pre>
	 * 征收奖励
	 * </pre>
	 */
	public static KLevyRewardDataManager mLevyRewardDataManager = new KLevyRewardDataManager();

	/**
	 * <pre>
	 * 角色奖励等级系数
	 * </pre>
	 */
	public static KRoleLvRewardBaseDataManager mRoleLvDataManager = new KRoleLvRewardBaseDataManager();
	/**
	 * <pre>
	 * 军团奖励等级系数
	 * </pre>
	 */
	public static KGangLvRewardBaseDataManager mGangLvDataManager = new KGangLvRewardBaseDataManager();

	/**
	 * <pre>
	 * 城市数据
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-8 下午2:40:38
	 * </pre>
	 */
	public static class KCityTempManager {
		/**
		 * <pre>
		 * KEY = 城市ID
		 * </pre>
		 */
		private Map<Integer, CityTemplate> dataMap = new HashMap<Integer, CityTemplate>();

		void init(List<CityTemplate> datas) throws Exception {
			for (CityTemplate data : datas) {
				if (dataMap.put(data.ID, data) != null) {
					throw new KGameServerException("ID重复 ID=" + data.ID);
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
		public CityTemplate getData(int cityId) {
			return dataMap.get(cityId);
		}

		public boolean containLv(int cityLv) {
			for (CityTemplate data : dataMap.values()) {
				if (data.citylv == cityLv) {
					return true;
				}
			}
			return false;
		}

		/**
		 * <pre>
		 * 
		 * @deprecated 直接获取缓存，谨慎使用
		 * @author CamusHuang
		 * @creation 2014-4-25 下午12:21:21
		 * </pre>
		 */
		public Map<Integer, CityTemplate> getDataCache() {
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
			for (CityTemplate data : dataMap.values()) {
				data.notifyCacheLoadComplete();
				if (mRewardDataManager.getData(data.citylv, true) == null) {
					throw new KGameServerException("缺少奖励数据 城市等级=" + data.citylv + " 胜负=true");
				}
				if (mRewardDataManager.getData(data.citylv, false) == null) {
					throw new KGameServerException("缺少奖励数据 城市等级=" + data.citylv + " 胜负=false");
				}
				if (mLevyRewardDataManager.getData(data.citylv) == null) {
					throw new KGameServerException("缺少征收奖励 城市等级=" + data.citylv);
				}
			}
		}

		public static class CityTemplate {
			// ----------以下是EXCEL表格直导数据---------
			public int ID;// 城市ID
			public String cityname;// 城市名称
			public int citylv;// 城市等级
			public int icon;// 美术资源

			// ----------以下是逻辑数据---------

			void notifyCacheLoadComplete() throws KGameServerException {
				if (citylv < 1) {
					throw new KGameServerException("数据错误 citylv=" + citylv);
				}
			}
		}
	}

	/**
	 * <pre>
	 * 资源点模板
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-8 下午2:40:38
	 * </pre>
	 */
	public static class KResPointTempManager {
		/**
		 * <pre>
		 * KEY = 资源点ID
		 * </pre>
		 */
		private Map<Integer, ResPointTemplate> dataMap = new HashMap<Integer, ResPointTemplate>();

		void init(List<ResPointTemplate> datas) throws Exception {
			for (ResPointTemplate data : datas) {
				if (dataMap.put(data.ID, data) != null) {
					throw new KGameServerException("ID重复 ID=" + data.ID);
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
		public ResPointTemplate getData(int id) {
			return dataMap.get(id);
		}

		/**
		 * <pre>
		 * 
		 * @deprecated 直接获取缓存，谨慎使用
		 * @author CamusHuang
		 * @creation 2014-4-25 下午12:21:21
		 * </pre>
		 */
		public Map<Integer, ResPointTemplate> getDataCache() {
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
			for (ResPointTemplate data : dataMap.values()) {
				data.notifyCacheLoadComplete();
			}
		}

		public static class ResPointTemplate {
			// ----------以下是EXCEL表格直导数据---------
			public int ID;// 资源点ID
			public String name;// 资源点名称
			public int icon;// 美术资源
			public int Integral;// 每5秒产出积分
			public int OccupyTime;// 最大占领时间(秒)

			// ----------以下是逻辑数据---------
			public String desc;// x积分x秒

			void notifyCacheLoadComplete() throws KGameServerException {
				if (Integral < 1) {
					throw new KGameServerException("数据错误 Integral=" + Integral);
				}
				if (OccupyTime < 1) {
					throw new KGameServerException("数据错误 OccupyTime=" + OccupyTime);
				}

				desc = StringUtil.format(GangResWarTips.x积分x秒, Integral, KResWarConfig.AddScorePeroid);
			}
		}
	}

	/**
	 * <pre>
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-8 下午2:40:38
	 * </pre>
	 */
	public static class KRewardDataManager {
		/**
		 * <pre>
		 * </pre>
		 */
		private List<RewardData> datas = new ArrayList<RewardData>();

		void init(List<RewardData> datas) throws Exception {
			this.datas.addAll(datas);
		}

		/**
		 * <pre>
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2013-4-23 下午7:53:53
		 * </pre>
		 */
		public RewardData getData(int cityLv, boolean isWin) {
			for (RewardData data : datas) {
				if (data.CityLv == cityLv && data.isWin == isWin) {
					return data;
				}
			}
			return null;
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
			for (RewardData data : datas) {
				data.notifyCacheLoadComplete();
			}
		}

		public static class RewardData {
			// ----------以下是EXCEL表格直导数据---------
			public int CityLv;// 城市等级
			private int WinLose;// 胜利/失败
			public int Contribution;// 贡献奖励
			public int honor;// 荣誉奖励
			public int LegionExp;// 军团经验奖励
			private int item;// 道具奖励
			private int quantity;// 道具数量

			// ----------以下是逻辑数据---------
			public boolean isWin;
			public ItemCountStruct addItem;

			void notifyCacheLoadComplete() throws KGameServerException {

				isWin = WinLose == 1;

				// 检查是否存在此城市等级
				if (!mCityTempManager.containLv(CityLv)) {
					throw new KGameServerException("不存在的城市等级 CityLv=" + CityLv);
				}

				if (Contribution < 1) {
					throw new KGameServerException("数据错误 Contribution=" + Contribution);
				}
				if (honor < 1) {
					throw new KGameServerException("数据错误 honor=" + honor);
				}
				if (LegionExp < 1) {
					throw new KGameServerException("数据错误 LegionExp=" + LegionExp);
				}
				if (item > 0 && quantity > 0) {
					addItem = new ItemCountStruct(item + "", quantity);
					if (addItem.getItemTemplate() == null) {
						throw new KGameServerException("数据错误 item=" + item);
					}
				}
			}
		}
	}

	/**
	 * <pre>
	 * 植物成熟时长数据
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-8 下午2:40:38
	 * </pre>
	 */
	public static class KLevyRewardDataManager {
		/**
		 * <pre>
		 * KEY = 城市等级
		 * </pre>
		 */
		private Map<Integer, LevyRewardData> dataMap = new HashMap<Integer, LevyRewardData>();

		void init(List<LevyRewardData> datas) throws Exception {
			for (LevyRewardData data : datas) {
				if (dataMap.put(data.citylv, data) != null) {
					throw new KGameServerException("等级重复 citylv=" + data.citylv);
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
		public LevyRewardData getData(int citylv) {
			return dataMap.get(citylv);
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
			for (LevyRewardData data : dataMap.values()) {
				data.notifyCacheLoadComplete();
			}
		}

		public static class LevyRewardData {
			// ----------以下是EXCEL表格直导数据---------
			public int citylv;// 城市等级
			public int gold;// 金币系数
			public int exp;// 经验系数
			public int contribution;// 占领贡献基数

			// ----------以下是逻辑数据---------

			void notifyCacheLoadComplete() throws KGameServerException {
				// 检查是否存在此城市等级
				if (!mCityTempManager.containLv(citylv)) {
					throw new KGameServerException("不存在的城市等级 CityLv=" + citylv);
				}

				if (gold < 1) {
					throw new KGameServerException("数据错误 gold=" + gold);
				}
				if (exp < 1) {
					throw new KGameServerException("数据错误 exp=" + exp);
				}
				if (contribution < 1) {
					throw new KGameServerException("数据错误 contribution=" + contribution);
				}
			}
		}
	}

	/**
	 * <pre>
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-8 下午2:40:38
	 * </pre>
	 */
	public static class KRoleLvRewardBaseDataManager {
		/**
		 * <pre>
		 * KEY = 角色等级
		 * </pre>
		 */
		private Map<Integer, RoleLvData> dataMap = new HashMap<Integer, RoleLvData>();

		void init(List<RoleLvData> datas) throws Exception {
			for (RoleLvData data : datas) {
				if (dataMap.put(data.Lv, data) != null) {
					throw new KGameServerException("角色等级重复 Lv=" + data.Lv);
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
		public RoleLvData getData(int roleLv) {
			return dataMap.get(roleLv);
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
			int minLv = 1;
			int maxLv = KRoleModuleConfig.getRoleMaxLv();
			if (dataMap.size() != maxLv) {
				throw new KGameServerException("等级不齐全或溢出");
			}
			for (int lv = minLv; lv <= maxLv; lv++) {
				RoleLvData data = dataMap.get(lv);
				if (data == null) {
					throw new KGameServerException("缺少角色等级 Lv=" + lv);
				}
				data.notifyCacheLoadComplete();
			}
		}

		public static class RoleLvData {
			// ----------以下是EXCEL表格直导数据---------
			public int Lv;// 玩家角色等级
			public int exp;// 经验奖励基数
			public int gold;// 金币奖励基数
			public int honor;// 荣誉奖励基数
			public int contribute;// 贡献奖励基数

			// ----------以下是逻辑数据---------

			void notifyCacheLoadComplete() throws KGameServerException {
				if (exp < 1) {
					throw new KGameServerException("数据错误 exp=" + exp);
				}
				if (gold < 1) {
					throw new KGameServerException("数据错误 gold=" + gold);
				}
				if (honor < 1) {
					throw new KGameServerException("数据错误 honor=" + honor);
				}
				if (contribute < 1) {
					throw new KGameServerException("数据错误 contribute=" + contribute);
				}
			}
		}
	}

	/**
	 * <pre>
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-8 下午2:40:38
	 * </pre>
	 */
	public static class KGangLvRewardBaseDataManager {
		/**
		 * <pre>
		 * KEY = 军团等级
		 * </pre>
		 */
		private Map<Integer, GangLvData> dataMap = new HashMap<Integer, GangLvData>();

		void init(List<GangLvData> datas) throws Exception {
			for (GangLvData data : datas) {
				if (dataMap.put(data.Lv, data) != null) {
					throw new KGameServerException("角色等级重复 Lv=" + data.Lv);
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
		public GangLvData getData(int gangLv) {
			return dataMap.get(gangLv);
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
			int minLv = 1;
			int maxLv = KGangDataManager.mGangLevelDataManager.getMaxLevel().LegionLv;
					
			if (dataMap.size() != maxLv) {
				throw new KGameServerException("等级不齐全或溢出");
			}
			for (int lv = minLv; lv <= maxLv; lv++) {
				GangLvData data = dataMap.get(lv);
				if (data == null) {
					throw new KGameServerException("缺少军团等级 Lv=" + lv);
				}
				data.notifyCacheLoadComplete();
			}
		}

		public static class GangLvData {
			// ----------以下是EXCEL表格直导数据---------
			public int Lv;// 军团等级
			public int exp;// 军团经验奖励基数

			// ----------以下是逻辑数据---------

			void notifyCacheLoadComplete() throws KGameServerException {
				if (exp < 1) {
					throw new KGameServerException("数据错误 exp=" + exp);
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
			mResPointTempManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_资源点 + "]错误：" + e.getMessage(), e);
		}

		try {
			mRewardDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_奖励 + "]错误：" + e.getMessage(), e);
		}

		try {
			mLevyRewardDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_征收奖励 + "]错误：" + e.getMessage(), e);
		}

		try {
			mRoleLvDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_角色等级 + "]错误：" + e.getMessage(), e);
		}
		
		try {
			mGangLvDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_军团等级 + "]错误：" + e.getMessage(), e);
		}

		try {
			mCityTempManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_城市 + "]错误：" + e.getMessage(), e);
		}
	}
}
