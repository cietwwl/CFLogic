package com.kola.kmp.logic.gang.war;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.ReflectPaser;
import com.kola.kmp.logic.gang.KGangDataManager;
import com.kola.kmp.logic.gang.war.KGangWarDataManager.KBossDataManager.BossData;
import com.kola.kmp.logic.gang.war.KGangWarDataManager.KGangMedalDataManager.GangMedalData;
import com.kola.kmp.logic.gang.war.KGangWarDataManager.KGangWarRewardDataManager.WarRankRewardData;
import com.kola.kmp.logic.gang.war.KGangWarDataManager.KGangWarRewardRatioDataManager.RewardRatioData;
import com.kola.kmp.logic.gang.war.KGangWarDataManager.KPVPScoreDataManager.PVPScore;
import com.kola.kmp.logic.gang.war.KGangWarDataManager.KRoleScoreRankRewardManager.ScoreRankReward;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.npc.KNPCDataStructs.KMonstTemplate;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.role.KRoleModuleConfig;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.GangWarTips;

/**
 * <pre>
 * 本类负责管理本模块的公共数据 
 * 
 * @author CamusHuang
 * @creation 2013-4-8 下午2:39:56
 * </pre>
 */
public class KGangWarDataManager {

	//
	static final String SheetName_BOSS模板 = "BOSS模板";
	static final String SheetName_战场积分 = "战场积分";
	static final String SheetName_军团排名奖励 = "军团排名奖励";
	static final String SheetName_军团奖励系数 = "军团奖励系数";
	static final String SheetName_积分排名奖励 = "积分排名奖励";
	static final String SheetName_军团勋章 = "军团勋章";

	/**
	 * <pre>
	 * 加载[军团勋章]
	 * 
	 * </pre>
	 */

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
			loadBossTemplateDatas(file.getTable(SheetName_BOSS模板, HeaderIndex));

			loadPVPScoreDatas(file.getTable(SheetName_战场积分, HeaderIndex));

			loadScoreRankRewardDatas(file.getTable(SheetName_积分排名奖励, HeaderIndex));

			loadWarRankRewardDatas(file.getTable(SheetName_军团排名奖励, HeaderIndex));

			loadRewardRatioDatas(file.getTable(SheetName_军团奖励系数, HeaderIndex));

			loadGangMedalDatas(file.getTable(SheetName_军团勋章, HeaderIndex));
		}
	}

	private static void loadBossTemplateDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<BossData> datas = ReflectPaser.parseExcelData(BossData.class, table.getHeaderNames(), rows, true);
		mBossDataManager.init(datas);
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
	private static void loadPVPScoreDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<PVPScore> datas = ReflectPaser.parseExcelData(PVPScore.class, table.getHeaderNames(), rows, true);
		mPVPScoreDataManager.init(datas);
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
	private static void loadScoreRankRewardDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<ScoreRankReward> datas = ReflectPaser.parseExcelData(ScoreRankReward.class, table.getHeaderNames(), rows, true);
		mRoleScoreRankRewardManager.init(datas);
	}

	private static void loadWarRankRewardDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<WarRankRewardData> datas = ReflectPaser.parseExcelData(WarRankRewardData.class, table.getHeaderNames(), rows, true);
		mGangWarRewardDataManager.init(datas);
	}

	private static void loadRewardRatioDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<RewardRatioData> datas = ReflectPaser.parseExcelData(RewardRatioData.class, table.getHeaderNames(), rows, true);
		mGangWarRewardRatioDataManager.init(datas);
	}

	private static void loadGangMedalDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<GangMedalData> datas = ReflectPaser.parseExcelData(GangMedalData.class, table.getHeaderNames(), rows, true);
		mGangMedalDataManager.init(datas);
	}

	// ///////////////////////////////////////
	/**
	 * <pre>
	 * BOSS模板数据
	 * </pre>
	 */
	public static KBossDataManager mBossDataManager = new KBossDataManager();

	/**
	 * <pre>
	 * 战场积分数据
	 * </pre>
	 */
	public static KPVPScoreDataManager mPVPScoreDataManager = new KPVPScoreDataManager();

	/**
	 * <pre>
	 * 积分排名奖励
	 * </pre>
	 */
	public static KRoleScoreRankRewardManager mRoleScoreRankRewardManager = new KRoleScoreRankRewardManager();

	/**
	 * <pre>
	 * 军团排名奖励
	 * </pre>
	 */
	public static KGangWarRewardDataManager mGangWarRewardDataManager = new KGangWarRewardDataManager();

	/**
	 * <pre>
	 * 军团奖励系数
	 * </pre>
	 */
	public static KGangWarRewardRatioDataManager mGangWarRewardRatioDataManager = new KGangWarRewardRatioDataManager();
	/**
	 * <pre>
	 * 军团勋章
	 * </pre>
	 */
	public static KGangMedalDataManager mGangMedalDataManager = new KGangMedalDataManager();

	/**
	 * <pre>
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-8 下午2:40:38
	 * </pre>
	 */
	public static class KBossDataManager {
		/**
		 * <pre>
		 * KEY = 世界等级
		 * </pre>
		 */
		private Map<Integer, BossData> dataMap = new HashMap<Integer, BossData>();

		void init(List<BossData> datas) throws Exception {
			for (BossData data : datas) {
				for (int lv = data.leastLV; lv <= data.maxLV; lv++) {
					if (dataMap.put(lv, data) != null) {
						throw new KGameServerException("平均等级重复 lv=" + lv);
					}
				}
			}
			int maxRoleLv = KRoleModuleConfig.getRoleMaxLv();
			for(int lv = 1; lv<=maxRoleLv;lv++){
				if(!dataMap.containsKey(lv)){
					throw new KGameServerException("平均等级缺失 lv=" + lv);
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
		public BossData getData(int lv) {
			return dataMap.get(lv);
		}

		public boolean containLv(int lv) {
			return dataMap.containsKey(lv);
		}

		/**
		 * <pre>
		 * 
		 * @deprecated 直接获取缓存，谨慎使用
		 * @author CamusHuang
		 * @creation 2014-4-25 下午12:21:21
		 * </pre>
		 */
		public Map<Integer, BossData> getDataCache() {
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
			for (BossData data : dataMap.values()) {
				data.notifyCacheLoadComplete();
			}
		}

		public static class BossData {
			// ----------以下是EXCEL表格直导数据---------
			public int leastLV;// 最小平均等级
			public int maxLV;// 最大平均等级
			private int bossid1;// 调用BOSS模板1
			private int bossid2;// 调用BOSS模板2
			public int HarmIntegral;// 基础积分伤害值

			// ----------以下是逻辑数据---------
			public KMonstTemplate bossTemp1;
			public KMonstTemplate bossTemp2;

			void notifyCacheLoadComplete() throws KGameServerException {
				if (leastLV < 1) {
					throw new KGameServerException("数据错误 leastLV=" + leastLV);
				}
				if (maxLV < 1 || maxLV < leastLV) {
					throw new KGameServerException("数据错误 maxLV=" + maxLV);
				}
				if (HarmIntegral < 1) {
					throw new KGameServerException("数据错误 HarmIntegral=" + HarmIntegral);
				}
				
				// BOSS是否存在
				bossTemp1=KSupportFactory.getNpcModuleSupport().getMonstTemplate(bossid1);
				bossTemp2=KSupportFactory.getNpcModuleSupport().getMonstTemplate(bossid2);
				if (bossTemp1 == null) {
					throw new KGameServerException("不存在的BOSS1 id=" + bossid1);
				}
				if (bossTemp2 == null) {
					throw new KGameServerException("不存在的BOSS id=" + bossid2);
				}
				// CTODO 其它约束检查
			}
		}
	}

	/**
	 * <pre>
	 * 角色等级的PVP战斗积分
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-8 下午2:40:38
	 * </pre>
	 */
	public static class KPVPScoreDataManager {
		/**
		 * <pre>
		 * KEY = 角色等级
		 * </pre>
		 */
		private Map<Integer, PVPScore> dataMap = new HashMap<Integer, PVPScore>();

		void init(List<PVPScore> datas) throws Exception {
			for (PVPScore data : datas) {
				if (dataMap.put(data.lv, data) != null) {
					throw new KGameServerException("角色等级重复 lv=" + data.lv);
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
		public PVPScore getData(int lv) {
			return dataMap.get(lv);
		}

		/**
		 * <pre>
		 * 
		 * @deprecated 直接获取缓存，谨慎使用
		 * @author CamusHuang
		 * @creation 2014-4-25 下午12:21:21
		 * </pre>
		 */
		public Map<Integer, PVPScore> getDataCache() {
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
			int minLv = 1;
			int maxLv = KRoleModuleConfig.getRoleMaxLv();
			if (dataMap.size() > maxLv) {
				throw new KGameServerException("角色等级溢出");
			}
			for (int lv = minLv; lv <= maxLv; lv++) {
				PVPScore data = dataMap.get(lv);
				if (data == null) {
					throw new KGameServerException("缺少角色等级=" + lv);
				}
				data.notifyCacheLoadComplete();
			}
		}

		public static class PVPScore {
			// ----------以下是EXCEL表格直导数据---------
			public int lv;// 角色等级
			public int integral;// 基础积分

			// ----------以下是逻辑数据---------

			void notifyCacheLoadComplete() throws KGameServerException {
				if (lv < 1) {
					throw new KGameServerException("数据错误 lv=" + lv);
				}
				if (integral < 1) {
					throw new KGameServerException("数据错误 integral=" + integral);
				}
			}
		}
	}

	/**
	 * <pre>
	 * 单场角色积分排名奖励
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-8 下午2:40:38
	 * </pre>
	 */
	public static class KRoleScoreRankRewardManager {
		/**
		 * <pre>
		 * KEY = 角色等级
		 * </pre>
		 */
		private Map<Integer, ScoreRankReward> dataMap = new HashMap<Integer, ScoreRankReward>();

		void init(List<ScoreRankReward> datas) throws Exception {
			for (ScoreRankReward data : datas) {
				for (int rank = data.least; rank <= data.max; rank++) {
					if (dataMap.put(rank, data) != null) {
						throw new KGameServerException("名次重复 rank=" + rank);
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
		public ScoreRankReward getData(int rank) {
			return dataMap.get(rank);
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
			int min = 1;
			int max = dataMap.size();
			for (int rank = min; rank <= max; rank++) {
				ScoreRankReward data = dataMap.get(rank);
				if (data == null) {
					throw new KGameServerException("缺少名次 rank=" + rank);
				}
				data.notifyCacheLoadComplete();
			}
		}

		public static class ScoreRankReward {
			// ----------以下是EXCEL表格直导数据---------
			public int least;// 积分排名段
			public int max;//
			private int itemid;// 奖励道具
			private int count;// 道具数量

			// ----------以下是逻辑数据---------
			public ItemCountStruct addItem;

			void notifyCacheLoadComplete() throws KGameServerException {
				if (least < 1) {
					throw new KGameServerException("数据错误 least=" + least);
				}
				if (max < 1) {
					throw new KGameServerException("数据错误 max=" + max);
				}
				if (count < 1) {
					throw new KGameServerException("数据错误 count=" + count);
				}
				KItemTempAbs temp = KSupportFactory.getItemModuleSupport().getItemTemplate(itemid + "");
				if (temp == null) {
					throw new KGameServerException("数据错误 道具模板不存在 id=" + itemid);
				}

				addItem = new ItemCountStruct(temp, count);
			}
		}
	}

	/**
	 * <pre>
	 * 场次
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-8 下午2:40:38
	 * </pre>
	 */
	public static class KGangWarRewardDataManager {
		/**
		 * <pre>
		 * KEY = 场次
		 * </pre>
		 */
		private Map<Integer, WarRankRewardData> windatas = new HashMap<Integer, WarRankRewardData>();
		// 将胜利奖励进行叠加（用于客户端显示）
		private Map<Integer, WarRankRewardData> windatasForShow = new HashMap<Integer, WarRankRewardData>();
		private Map<Integer, WarRankRewardData> losedatas = new HashMap<Integer, WarRankRewardData>();
		

		void init(List<WarRankRewardData> datas) throws Exception {
			for (WarRankRewardData data : datas) {
				if (data.WinLose > 0) {
					windatas.put(data.rank, data);
				} else {
					losedatas.put(data.rank, data);
				}
			}
			if (windatas.size() != losedatas.size()) {
				throw new KGameServerException("胜负奖励场数不相等");
			}
			if (windatas.size() != KGangWarConfig.getInstance().MaxRound) {
				throw new KGameServerException("胜负奖励场数错误");
			}
			
			//将胜利奖励进行叠加（用于客户端显示）
			for(WarRankRewardData orgData:datas){
				if (orgData.WinLose == 1) {
					WarRankRewardData tempData=new WarRankRewardData();
					tempData.rank= orgData.rank;
					tempData.WinLose = 1;
					windatasForShow.put(tempData.rank, tempData);
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
		public WarRankRewardData getData(int round, boolean isWin) {
			if (isWin) {
				return windatas.get(round);
			}
			return losedatas.get(round);
		}
		
		/**
		 * <pre>
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2013-4-23 下午7:53:53
		 * </pre>
		 */
		public Map<Integer, WarRankRewardData> getDataCacheForShow() {
			return windatasForShow;
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
			for (int rank = 1; rank <= KGangWarConfig.getInstance().MaxRound; rank++) {
				WarRankRewardData data = windatas.get(rank);
				if (data == null) {
					throw new KGameServerException("胜出奖励缺少名次=" + rank);
				}
				data.notifyCacheLoadComplete();
				//
				data = losedatas.get(rank);
				if (data == null) {
					throw new KGameServerException("战败奖励缺少名次=" + rank);
				}
				data.notifyCacheLoadComplete();
			}
			
			//将胜利奖励进行叠加（用于客户端显示）
			for(WarRankRewardData tempData:windatasForShow.values()){
				for(WarRankRewardData data:windatas.values()){
					if(data.rank<=tempData.rank && data.isWin){
						tempData.gold += data.gold;
						tempData.Contribution += data.Contribution;
						tempData.exp += data.exp;
						tempData.GangMoney += data.GangMoney;
					}
				}
				tempData.notifyCacheLoadComplete();
			}			
		}

		public static class WarRankRewardData {
			// ----------以下是EXCEL表格直导数据---------
			public int rank;// 军团战场次
			private int WinLose;// 胜利/失败
			private int gold;// 金币奖励基础值
			public int Contribution;// 贡献奖励基础值
			public int exp;// 军团经验基础值
			public int GangMoney;// 军团资金基础值

			// ----------以下是逻辑数据---------
			public boolean isWin;
			private List<KCurrencyCountStruct> addMoneys;
			public BaseMailContent baseMailContent;

			void notifyCacheLoadComplete() throws KGameServerException {

				isWin = WinLose == 1;

				if (rank < 1) {
					throw new KGameServerException("数据错误 rank=" + rank);
				}
				if (gold < 0) {
					throw new KGameServerException("数据错误 gold=" + gold);
				}
				if (Contribution < 1) {
					throw new KGameServerException("数据错误 Contribution=" + Contribution);
				}
				if (exp < 1) {
					throw new KGameServerException("数据错误 exp=" + exp);
				}
				if (GangMoney < 1) {
					throw new KGameServerException("数据错误 GangMoney=" + GangMoney);
				}

				addMoneys = new ArrayList<KCurrencyCountStruct>();
				if(gold>0){
					addMoneys.add(new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD, gold));
				}
				addMoneys.add(new KCurrencyCountStruct(KCurrencyTypeEnum.GANG_CONTRIBUTION, Contribution));
				
				String Q = GangWarLogic.getQiangFromRound(rank, isWin);
				if (isWin) {
					String title = StringUtil.format(GangWarTips.军团战第x场奖励发放, rank);
					String content = StringUtil.format(GangWarTips.恭喜您的军团在军团战中晋级x, Q);
					baseMailContent = new BaseMailContent(title, content, null, null);
				} else {
					String title = StringUtil.format(GangWarTips.军团战第x场奖励发放, rank);
					String content = StringUtil.format(GangWarTips.很遗憾您的军团在本次军团战中止步于x, Q);
					baseMailContent = new BaseMailContent(title, content, null, null);
				}
			}
			
			public List<KCurrencyCountStruct> getAddMoneys(float ratio){
				if(ratio==1){
					return addMoneys;
				}
				List<KCurrencyCountStruct> temp = new ArrayList<KCurrencyCountStruct>();
				for(KCurrencyCountStruct data: addMoneys){
					temp.add(new KCurrencyCountStruct(data.currencyType, (long)(data.currencyCount*ratio)));
				}
				return temp;
			}
		}
	}

	/**
	 * <pre>
	 * 军团奖励系数
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-8 下午2:40:38
	 * </pre>
	 */
	public static class KGangWarRewardRatioDataManager {
		/**
		 * <pre>
		 * KEY = 军团等级
		 * </pre>
		 */
		private Map<Integer, RewardRatioData> dataMap = new HashMap<Integer, RewardRatioData>();

		void init(List<RewardRatioData> datas) throws Exception {
			for (RewardRatioData data : datas) {
				if (dataMap.put(data.legion, data) != null) {
					throw new KGameServerException("军团奖励系数重复 legion=" + data.legion);
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
		public RewardRatioData getData(int lv) {
			return dataMap.get(lv);
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
			int maxLv = KGangDataManager.mGangLevelDataManager.getMaxLevel().LegionLv;
			for (int lv = 1; lv <= maxLv; lv++) {
				RewardRatioData data = dataMap.get(lv);
				if (data == null) {
					throw new KGameServerException("缺少军团等级 = " + lv);
				}
				data.notifyCacheLoadComplete();
			}
		}

		public static class RewardRatioData {
			// ----------以下是EXCEL表格直导数据---------
			public int legion;// 军团等级
			public float coefficient;// 奖励系数

			// ----------以下是逻辑数据---------

			void notifyCacheLoadComplete() throws KGameServerException {
				// 检查是否存在此城市等级
				if (legion < 1) {
					throw new KGameServerException("数据错误 legion=" + legion);
				}

				if (coefficient <= 0) {
					throw new KGameServerException("数据错误 coefficient=" + coefficient);
				}
			}
		}
	}

	/**
	 * <pre>
	 * 
	 * 勋章等级
	 * 1.冠军
	 * 2.亚军
	 * 3.季军
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-8 下午2:40:38
	 * </pre>
	 */
	public static class KGangMedalDataManager {

		/**
		 * <pre>
		 * KEY = 勋章等级
		 * </pre>
		 */
		private Map<Integer, GangMedalData> dataMap = new HashMap<Integer, GangMedalData>();

		void init(List<GangMedalData> datas) throws Exception {
			for (GangMedalData data : datas) {
				if (dataMap.put(data.rank, data) != null) {
					throw new KGameServerException("勋章rank重复 rank=" + data.rank);
				}
			}
		}

		public GangMedalData getDataByRank(int rank) {
			return dataMap.get(rank);
		}
		/**
		 * <pre>
		 * 
		 * @deprecated 直接获取缓存，请谨慎使用
		 * @return
		 * @author CamusHuang
		 * @creation 2014-5-21 下午3:08:41
		 * </pre>
		 */
		public Map<Integer, GangMedalData> getDataCache() {
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
			int minLv = 1;
			int maxLv = 3;

			if (dataMap.size() != maxLv) {
				throw new KGameServerException("rank不齐全或溢出");
			}
			for (int rank = minLv; rank <= maxLv; rank++) {
				GangMedalData data = dataMap.get(rank);
				if (data == null) {
					throw new KGameServerException("缺少rank=" + rank);
				}
				data.notifyCacheLoadComplete();
			}
		}

		public static class GangMedalData {
			// ----------以下是EXCEL表格直导数据---------
			private int ID;// 勋章ID
			public int icon;// 勋章ICON
			public int rank;// 颁发军团
			public String name;// 勋章名称
			public String explain;// 勋章说明
			private int[] attribute;// 附加属性类型
			private int[] price;// 附加属性值

			// ----------以下是逻辑数据---------
			public List<AttValueStruct> addAtts = new ArrayList<AttValueStruct>();
			public Map<KGameAttrType, Integer> addAttsMap = new HashMap<KGameAttrType, Integer>();

			void notifyCacheLoadComplete() throws KGameServerException {
				if (rank < 1) {
					throw new KGameServerException("数据错误 rank=" + rank);
				}
				if (attribute.length < 1) {
					throw new KGameServerException("数据错误 attribute=" + attribute);
				}
				if (price.length < 1) {
					throw new KGameServerException("数据错误 price=" + price);
				}
				if (attribute.length != price.length) {
					throw new KGameServerException("数据错误 属性数组长度不匹配");
				}
				for (int index = 0; index < attribute.length; index++) {
					KGameAttrType att = KGameAttrType.getAttrTypeEnum(attribute[index]);
					if (att == null) {
						throw new KGameServerException("数据错误 不存在的属性=" + attribute[index]);
					}
					if (price[index] < 1) {
						throw new KGameServerException("数据错误 属性值=" + price[index]);
					}
					addAtts.add(new AttValueStruct(att, price[index]));
					addAttsMap.put(att, price[index]);
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
			mBossDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_BOSS模板 + "]错误：" + e.getMessage(), e);
		}

		try {
			mPVPScoreDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_战场积分 + "]错误：" + e.getMessage(), e);
		}

		try {
			mRoleScoreRankRewardManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_积分排名奖励 + "]错误：" + e.getMessage(), e);
		}

		try {
			mGangWarRewardDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_军团排名奖励 + "]错误：" + e.getMessage(), e);
		}

		try {
			mGangWarRewardRatioDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_军团奖励系数 + "]错误：" + e.getMessage(), e);
		}

		try {
			mGangMedalDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_军团勋章 + "]错误：" + e.getMessage(), e);
		}
	}
}
