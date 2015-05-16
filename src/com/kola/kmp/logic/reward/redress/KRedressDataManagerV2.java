package com.kola.kmp.logic.reward.redress;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.ReflectPaser;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.level.copys.KTowerCopyManager;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.reward.redress.KRedressDataManagerV2.KMountStoneRedressDataManager.MountStoneRedressData;
import com.kola.kmp.logic.reward.redress.KRedressDataManagerV2.KMountRedressDataManager.MountRedressData;
import com.kola.kmp.logic.reward.redress.KRedressDataManagerV2.KVIPRedressDataManager.VIPRedressData;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRoleIniSkillTemp;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRolePasSkillTemp;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.KGameUtilTool;
import com.kola.kmp.logic.util.tips.RewardTips;
import com.kola.kmp.logic.vip.KVIPDataManager;

/**
 * <pre>
 * 1.0.5改版补偿
 * 
 * @author CamusHuang
 * @creation 2015-1-31 下午8:08:00
 * </pre>
 */
public class KRedressDataManagerV2 {

	public static long RoleCreateStartTime;
	// 补偿的角色创建时间截止日期
	public static long RoleCreateEndTime;

	static void loadConfig(Element e) throws KGameServerException {

		try {
			RoleCreateStartTime = UtilTool.DATE_FORMAT.parse("1900-1-1 00:00").getTime();
		
			RoleCreateEndTime = UtilTool.DATE_FORMAT.parse(e.getChildTextTrim("RoleCreateEndTime")).getTime();
		} catch (ParseException e1) {
			throw new KGameServerException(e1.getMessage(), e1);
		}
	}

	//
	static final String SheetName_异能要塞补偿 = "异能要塞补偿";
	static final String SheetName_机甲养成补偿 = "机甲养成补偿";
	static final String SheetName_机甲培养补偿 = "机甲培养补偿";
	static final String SheetName_VIP补偿 = "VIP补偿";
	static final String SheetName_欢乐送次数补偿 = "欢乐送次数补偿";
	static final String SheetName_机甲石替换 = "机甲石替换";
	static final String SheetName_全局补偿 = "全局补偿";

	/**
	 * <pre>
	 * 开始加载文件
	 * @param configPath
	 * @author CamusHuang
	 * @creation 2012-11-9 下午7:38:27
	 * </pre>
	 */
	static void goToLoadData(String ExcelPath, int HeaderIndex) throws Exception {

		KGameExcelFile file = new KGameExcelFile(ExcelPath);
		{
			KGameExcelTable table = file.getTable(SheetName_异能要塞补偿, HeaderIndex);

			KGameExcelRow[] rows = table.getAllDataRows();
			if (rows.length < 1) {
				throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
			}

			try {
				Map<Integer, BaseMailRewardData> datas = new HashMap<Integer, BaseMailRewardData>();
				//
				for (KGameExcelRow row : rows) {
					int sceneId = row.getInt("sceneId");
					BaseMailRewardData mailReward = BaseMailRewardData.loadData(row, false);
					datas.put(sceneId, mailReward);
				}
				mLadderRedressDataManager.init(datas);
			} catch (Exception e) {
				throw new Exception("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
			}
		}
		{
			KGameExcelTable table = file.getTable(SheetName_欢乐送次数补偿, HeaderIndex);

			KGameExcelRow[] rows = table.getAllDataRows();
			if (rows.length != 1) {
				throw new Exception("加载[" + table.getTableName() + "]错误：有效行数必须为1！");
			}

			try {
				KGameExcelRow row = rows[rows.length - 1];
				int SingleDiamonds = row.getInt("SingleDiamonds");
				BaseMailContent mailContent = BaseMailContent.loadData(row);
				//
				mHappyTimeRedressDataManager = new KHappyTimeRedressDataManager(SingleDiamonds, mailContent);
			} catch (Exception e) {
				throw new Exception("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
			}
		}
		{
			KGameExcelTable table = file.getTable(SheetName_VIP补偿, HeaderIndex);

			KGameExcelRow[] rows = table.getAllDataRows();
			if (rows.length < 1) {
				throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
			}

			try {
				List<VIPRedressData> datas = new ArrayList<VIPRedressData>();
				//
				List<String> headerNames = table.getHeaderNames();
				Class<VIPRedressData> clazz = VIPRedressData.class;
				for (KGameExcelRow row : rows) {
					VIPRedressData obj = clazz.newInstance();
					ReflectPaser.parseExcelData(obj, headerNames, row, false, false);
					{
						BaseMailContent baseMail = null;
						BaseRewardData baseRewardData = BaseRewardData.loadData(row, false);
						if (baseRewardData.checkIsEffect()) {
							baseMail = BaseMailContent.loadData(row);
							obj.mailReward = new BaseMailRewardData(1, baseMail, baseRewardData);
						}
					}
					datas.add(obj);
				}
				mVIPRedressDataManager.init(datas);
			} catch (Exception e) {
				throw new Exception("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
			}
		}

		{
			KGameExcelTable table = file.getTable(SheetName_全局补偿, HeaderIndex);

			KGameExcelRow[] rows = table.getAllDataRows();
			if (rows.length != 1) {
				throw new Exception("加载[" + table.getTableName() + "]错误：有效行数必须为1！");
			}

			try {
				redressForAllRole = BaseMailRewardData.loadData(rows[rows.length - 1], false);
			} catch (Exception e) {
				throw new Exception("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
			}
		}

		{
			KGameExcelTable table = file.getTable(SheetName_机甲石替换, HeaderIndex);

			KGameExcelRow[] rows = table.getAllDataRows();
			if (rows.length < 1) {
				throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
			}

			try {
				List<MountStoneRedressData> datas = new ArrayList<MountStoneRedressData>();
				//
				List<String> headerNames = table.getHeaderNames();
				Class<MountStoneRedressData> clazz = MountStoneRedressData.class;
				for (KGameExcelRow row : rows) {
					MountStoneRedressData obj = clazz.newInstance();
					ReflectPaser.parseExcelData(obj, headerNames, row, false, false);
					datas.add(obj);
				}
				mMountStoneRedressDataManager.init(datas);
			} catch (Exception e) {
				throw new Exception("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
			}
		}

		{
			KGameExcelTable table = file.getTable(SheetName_机甲养成补偿, HeaderIndex);

			KGameExcelRow[] rows = table.getAllDataRows();
			if (rows.length < 1) {
				throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
			}

			try {
				List<MountRedressData> datas = new ArrayList<MountRedressData>();
				//
				List<String> headerNames = table.getHeaderNames();
				Class<MountRedressData> clazz = MountRedressData.class;
				for (KGameExcelRow row : rows) {
					MountRedressData obj = clazz.newInstance();
					ReflectPaser.parseExcelData(obj, headerNames, row, false, false);
					{
						obj.mailContent = BaseMailContent.loadData(row);
						if (obj.mailContent == null) {
							throw new Exception("未配置邮件内容");
						}
					}
					datas.add(obj);
				}
				mMountRedressDataManager.init(datas);
			} catch (Exception e) {
				throw new Exception("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
			}
		}

		{
			KGameExcelTable table = file.getTable(SheetName_机甲培养补偿, HeaderIndex);

			KGameExcelRow[] rows = table.getAllDataRows();
			if (rows.length != 1) {
				throw new Exception("加载[" + table.getTableName() + "]错误：有效行数必须为1！");
			}

			try {
				mMountTrainRedressMail = BaseMailContent.loadData(rows[rows.length - 1]);
				if (mMountTrainRedressMail == null) {
					throw new Exception("缺少邮件内容");
				}
			} catch (Exception e) {
				throw new Exception("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
			}
		}

	}

	// /////////////////////////////////////////

	/**
	 * <pre>
	 * 异能要塞补偿
	 * </pre>
	 */
	public static KLadderRedressDataManager mLadderRedressDataManager = new KLadderRedressDataManager();

	/**
	 * <pre>
	 * 欢乐送补偿
	 * </pre>
	 */
	public static KHappyTimeRedressDataManager mHappyTimeRedressDataManager;

	/**
	 * <pre>
	 * VIP补偿补偿数据
	 * </pre>
	 */
	public static KVIPRedressDataManager mVIPRedressDataManager = new KVIPRedressDataManager();

	/**
	 * <pre>
	 * 机甲石替换数据
	 * </pre>
	 */
	public static KMountStoneRedressDataManager mMountStoneRedressDataManager = new KMountStoneRedressDataManager();

	/**
	 * <pre>
	 * 机甲养成补偿
	 * </pre>
	 */
	public static KMountRedressDataManager mMountRedressDataManager = new KMountRedressDataManager();

	/**
	 * <pre>
	 * 机甲培养补偿邮件
	 * </pre>
	 */
	public static BaseMailContent mMountTrainRedressMail;

	/**
	 * <pre>
	 * 全局补偿
	 * </pre>
	 */
	public static BaseMailRewardData redressForAllRole;

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 异能要塞补偿
	 * 
	 * @author CamusHuang
	 * @creation 2015-1-30 上午10:05:10
	 * </pre>
	 */
	public static class KLadderRedressDataManager {
		/**
		 * <pre>
		 * KEY = 当前可挑战关卡ID
		 * </pre>
		 */
		private HashMap<Integer, BaseMailRewardData> dataMap = new HashMap<Integer, BaseMailRewardData>();

		void init(Map<Integer, BaseMailRewardData> datas) throws Exception {
			dataMap.putAll(datas);
		}

		public BaseMailRewardData getData(int levelId) {
			return dataMap.get(levelId);
		}

		Map<Integer, BaseMailRewardData> getDataCache() {
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
		void notifyCacheLoadComplete() throws Exception {
			for (Entry<Integer, BaseMailRewardData> e : dataMap.entrySet()) {
				if (!KTowerCopyManager.towerCopyLevelMap.containsKey(e.getKey())) {
					throw new Exception("不存在指定关卡 id=" + e.getKey());
				}
				e.getValue().notifyCacheLoadComplete();
			}
			for (int level : KTowerCopyManager.towerCopyLevelMap.keySet()) {
				if (!dataMap.containsKey(level)) {
					throw new Exception("未配置补偿数据 关卡 id=" + level);
				}
			}
		}

	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 欢乐送补偿
	 * 
	 * @author CamusHuang
	 * @creation 2015-1-30 上午10:05:10
	 * </pre>
	 */
	public static class KHappyTimeRedressDataManager {

		final int SingleDiamonds;// 每次补偿钻石量
		final BaseMailContent mailContent;

		KHappyTimeRedressDataManager(int singleDiamonds, BaseMailContent mailContent) {
			SingleDiamonds = singleDiamonds;
			this.mailContent = mailContent;
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
		void notifyCacheLoadComplete() throws Exception {
			if (SingleDiamonds < 1) {
				throw new Exception("SingleDiamonds 错误 =" + SingleDiamonds);
			}
			if (mailContent == null) {
				throw new Exception("邮件未配置");
			}
		}

	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * VIP补偿
	 * 
	 * @author CamusHuang
	 * @creation 2015-1-30 下午2:40:49
	 * </pre>
	 */
	public static class KVIPRedressDataManager {
		/**
		 * <pre>
		 * KEY = VIP等级
		 * </pre>
		 */
		private HashMap<Integer, VIPRedressData> dataMap = new HashMap<Integer, VIPRedressData>();

		void init(List<VIPRedressData> datas) throws Exception {
			for (VIPRedressData data : datas) {
				if (dataMap.put(data.lvl, data) != null) {
					throw new Exception("数据重复 lvl=" + data.lvl);
				}
			}
		}

		public VIPRedressData getData(int vip) {
			return dataMap.get(vip);
		}

		void notifyCacheLoadComplete() throws Exception {
			int maxLv = KVIPDataManager.mVIPLevelDataManager.getMaxLevel().lvl;

			int totalCharge = 0;
			for (int lv = 1; lv <= maxLv; lv++) {
				VIPRedressData data = dataMap.get(lv);
				if (data == null) {
					throw new Exception("缺少VIP等级 = " + lv);
				}
				totalCharge += data.oldNeedrmb;
				data.totalOldCharge = totalCharge;
				data.notifyCacheLoadComplete();
			}
		}

		// //////////////////////////
		public static class VIPRedressData {
			// ----------以下是EXCEL表格直导数据---------
			public int lvl;// VIP等级
			public int oldNeedrmb;// 原需要充值钻石数额

			// ----------以下是逻辑数据---------
			public BaseMailRewardData mailReward;
			public int totalOldCharge;// 原需要充值的钻石总额

			void notifyCacheLoadComplete() throws Exception {

				if (oldNeedrmb < 1) {
					throw new Exception("oldNeedrmb 错误 = " + oldNeedrmb);
				}

				if (mailReward != null) {
					mailReward.notifyCacheLoadComplete();
				}
				
			}

		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 机甲石替换
	 * 
	 * @author CamusHuang
	 * @creation 2015-1-30 下午2:40:49
	 * </pre>
	 */
	public static class KMountStoneRedressDataManager {

		Map<String, MountStoneRedressData> dataMap = new HashMap<String, MountStoneRedressData>();

		void init(List<MountStoneRedressData> datas) throws Exception {
			for (MountStoneRedressData data : datas) {
				if (dataMap.put(data.oldItemId, data) != null) {
					throw new Exception("数据重复 oldItemId=" + data.oldItemId);
				}
			}
		}

		void notifyCacheLoadComplete() throws Exception {
			for (MountStoneRedressData data : dataMap.values()) {
				data.notifyCacheLoadComplete();
			}
		}

		// //////////////////////////
		public static class MountStoneRedressData {
			// ----------以下是EXCEL表格直导数据---------
			public String oldItemId;// 原道具id
			public String newItemId;// 新道具id
			public int newItemCount;// 新道具数量

			// ----------以下是逻辑数据---------

			void notifyCacheLoadComplete() throws Exception {

				if (KSupportFactory.getItemModuleSupport().getItemTemplate(oldItemId) == null) {
					throw new Exception("不存在此物品oldItemId=" + oldItemId);
				}
				if (KSupportFactory.getItemModuleSupport().getItemTemplate(newItemId) == null) {
					throw new Exception("不存在此物品newItemId=" + newItemId);
				}

				if (newItemCount < 1) {
					throw new Exception("数值错误 newItemCount=" + newItemCount);
				}
				
			}

		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 机甲养成补偿
	 * 
	 * @author CamusHuang
	 * @creation 2015-1-31 下午12:21:36
	 * </pre>
	 */
	public static class KMountRedressDataManager {

		/**
		 * <pre>
		 * <旧版机甲 板ID,补偿奖励>
		 * </pre>
		 */
		private Map<Integer, MountRedressData> dataMap = new HashMap<Integer, MountRedressData>();

		void init(List<MountRedressData> datas) throws Exception {
			for (MountRedressData data : datas) {
				if (dataMap.put(data.mountsID, data) != null) {
					throw new Exception("数据重复 mountsID=" + data.mountsID);
				}
			}
		}

		public MountRedressData getData(int mountsID) {
			return dataMap.get(mountsID);
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
		void notifyCacheLoadComplete() throws Exception {
			for (MountRedressData data : dataMap.values()) {
				data.notifyCacheLoadComplete();
			}
		}

		Map<Integer, MountRedressData> getDataCache() {
			return dataMap;
		}

		// //////////////////////////
		public static class MountRedressData {
			// ----------以下是EXCEL表格直导数据---------
			int mountsID;// 座驾ID
			int oldMaxExp;// 原经验最大值（0表示无须额外补偿）
			private String basicItem;// 基本材料
			private int basicGold;// 基本金币
			private int additionalItemBase;// 额外材料基数
			private String additionalItemID;// 额外材料ID
			private int additionalGoldBase;// 额外金币基数

			// ----------以下是逻辑数据---------
			public List<ItemCountStruct> baseItems = new ArrayList<ItemCountStruct>();
			public KCurrencyCountStruct baseMoney;
			public ItemCountStruct additionalItem;
			public KCurrencyCountStruct additionalMoney;
			public BaseMailContent mailContent;

			void notifyCacheLoadComplete() throws Exception {
				// if(oldMaxExp<1){
				// throw new Exception("oldMaxExp 错误 = "+oldMaxExp);
				// }
				//
				if (basicGold < 1) {
					throw new Exception("basicGold 错误 = " + basicGold);
				}
				baseMoney = new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD, basicGold);
				//
				if (basicItem.isEmpty()) {
					throw new Exception("basicItem 错误 = " + basicItem);
				}
				ItemCountStruct.paramsItems(basicItem.split(","), baseItems, 1);
				//
				if (oldMaxExp > 0) {
					additionalItem = new ItemCountStruct(additionalItemID, additionalItemBase);
					if (additionalItem.getItemTemplate() == null) {
						throw new Exception("additionalItemID 模板不存在 = " + additionalItemID);
					}
					if (additionalItem.itemCount < 1) {
						throw new Exception("additionalItemBase 错误 = " + additionalItemBase);
					}
					//
					if (additionalGoldBase < 1) {
						throw new Exception("additionalGoldBase 错误 = " + additionalGoldBase);
					}
					additionalMoney = new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD, additionalGoldBase);
				} else {
					if (additionalItemBase > 0) {
						throw new Exception("additionalItemBase 错误 = " + additionalItemBase);
					}
					//
					if (additionalGoldBase > 0) {
						throw new Exception("additionalGoldBase 错误 = " + additionalGoldBase);
					}
				}
				//
				//
				if (mailContent == null) {
					throw new Exception("邮件未配置");
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
			mLadderRedressDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_异能要塞补偿 + "]错误：" + e.getMessage(), e);
		}

		try {
			mHappyTimeRedressDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_欢乐送次数补偿 + "]错误：" + e.getMessage(), e);
		}

		try {
			mVIPRedressDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_VIP补偿 + "]错误：" + e.getMessage(), e);
		}

		try {
			redressForAllRole.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_全局补偿 + "]错误：" + e.getMessage(), e);
		}

		try {
			mMountStoneRedressDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_机甲石替换 + "]错误：" + e.getMessage(), e);
		}

		try {
			mMountRedressDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_机甲养成补偿 + "]错误：" + e.getMessage(), e);
		}

	}
}
