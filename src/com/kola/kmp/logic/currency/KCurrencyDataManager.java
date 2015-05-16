package com.kola.kmp.logic.currency;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.kola.kmp.logic.currency.KCurrencyDataManager.ChargeInfoManager.ChargeInfoStruct;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardDataForJobs;
import com.kola.kmp.logic.role.KRoleModuleConfig;
import com.kola.kmp.logic.util.tips.CurrencyTips;

/**
 * <pre>
 * 本类负责管理本模块的公共数据 
 * 
 * @author CamusHuang
 * @creation 2013-4-8 下午2:39:56
 * </pre>
 */
public class KCurrencyDataManager {

	static final String SheetName_等级兑换比例 = "等级兑换比例";
	static final String SheetName_充值配置 = "充值配置";
	static final String SheetName_充值档位 = "充值档位";
	static final String SheetName_首充奖励数据表 = "首充奖励数据表";

	// /////////////////////////////////////////

	/**
	 * <pre>
	 * 金币兑换数据
	 * </pre>
	 */
	public static KDiamondToGoldDataManager mDiamondToGoldDataManager = new KDiamondToGoldDataManager();
	/**
	 * <pre>
	 * 充值数据
	 * </pre>
	 */
	public static ChargeInfoManager mChargeInfoManager = new ChargeInfoManager();

	/**
	 * <pre>
	 * 首充奖励数据
	 * </pre>
	 */
	public static FirstChargeRewardDataManager mFirstChargeRewardDataManager;

	
	// /////////////////////////////////////////

	public static class KDiamondToGoldDataManager {
		//<等级，每10个钻石可兑换的金币数量>
		private Map<Integer, Integer> datasForTen = new HashMap<Integer, Integer>();
		//<等级，每个钻石可兑换的金币数量>
		private Map<Integer, Integer> datasForOne = new HashMap<Integer, Integer>();

		void addData(int lv, int data) throws Exception {
			if (data < 1) {
				throw new Exception("兑换金币数量错误");
			}
			if(data%KCurrencyConfig.getInstance().DiamondToGoldBase!=0){
				throw new Exception("兑换金币数量必须是"+KCurrencyConfig.getInstance().DiamondToGoldBase+"的整数倍");
			}
			if (datasForTen.put(lv, data) != null) {
				throw new Exception("角色等级重复");
			}
			datasForOne.put(lv, data/KCurrencyConfig.getInstance().DiamondToGoldBase);
		}

		/**
		 * <pre>
		 * 每10个钻石可兑换的金币数量
		 * 
		 * @param lv
		 * @return
		 * @author CamusHuang
		 * @creation 2014-10-29 上午9:47:40
		 * </pre>
		 */
		public int getRateForTen(int lv) {
			Integer value = datasForTen.get(lv);
			if (value == null) {
				return 1;
			}
			return value;
		}
		
		/**
		 * <pre>
		 * 每个钻石可兑换的金币数量
		 * 
		 * @param lv
		 * @return
		 * @author CamusHuang
		 * @creation 2014-10-29 上午9:47:40
		 * </pre>
		 */
		public int getRateForOne(int lv) {
			Integer value = datasForOne.get(lv);
			if (value == null) {
				return 1;
			}
			return value;
		}

		void notifyCacheLoadComplete() throws Exception {
			int maxlv = KRoleModuleConfig.getRoleMaxLv();
			for (int lv = 1; lv <= maxlv; lv++) {
				if (!datasForTen.containsKey(lv)) {
					throw new Exception("角色等级缺漏 lv=" + lv);
				}

				if (datasForTen.get(lv) < 1) {
					throw new Exception("数值错误 lv=" + lv);
				}
			}
		}
	}

	// ////////////////////////////////////////////////////////////////////
	public static class ChargeInfoManager {
		private String CNY_UNIT;// 货币(元、美元...)
		private String tips;// 温馨提示（比如：温馨提示：非本界面指定的充值金额（如31元）不享受元宝赠送喔）
		//
		private List<ChargeInfoStruct> infos = new ArrayList<ChargeInfoStruct>();
		// <充值RMB对应的元宝数,ChargeInfoStruct>
		private Map<Integer, ChargeInfoStruct> infosMap = new HashMap<Integer, ChargeInfoStruct>();
		// 月卡
		public ChargeInfoStruct monthCard;

		void initDatas(List<ChargeInfoStruct> datas) throws Exception{
			infos.addAll(datas);
			for (ChargeInfoStruct data : infos) {
				data.init();
				//
				if (infosMap.put(data.rmbIngot, data) != null) {
					throw new Exception("重复的充值金额 =" + data.goodsPrice);
				}
				if(data.isMonthCard()){
					if(monthCard==null){
						monthCard = data;
					} else {
						throw new Exception("不能配置多个月卡");
					}
				}
			}
		}

		public String getCNY_UNIT() {
			return CNY_UNIT;
		}

		public List<ChargeInfoStruct> getInfos() {
			return infos;
		}
		
		public ChargeInfoStruct getInfo(int priceIngot) {
			return infosMap.get(priceIngot);
		}

		public String getTips() {
			return tips;
		}

		void notifyCacheLoadComplete() throws Exception {
			for (ChargeInfoStruct data : infos) {
				data.notifyCacheLoadComplete();
			}
		}

		public static class ChargeInfoStruct {
			public String goodsName;// 商品名称 （如果需要赠送可填：5000+100元宝）
			public String goodsPrice;// 商品价格
			public String presentTips;// 另外赠送
			public String ext;// 扩展内容
			public boolean isHot;//是否热销
			//
			public int baseIngot;// 充值获得钻石数量(充10元不一定获得100钻)
			public int returnRateForFirst;// 首充返利率(百分比)
			public int presentIngot;// 赠送钻石数量
			//
			public int monthCardKeepDays;//月卡时效（天）
			public int monthCardIngotForDay;// 每日返回金额(钻)
			public boolean isMonthCardCanBuyMuil;// 是否可以无限购买
			// ----------以下是逻辑数据---------
			public int rmbIngot;//充值货币对应的钻石数量(充10元即100钻)
			public List<KCurrencyCountStruct> monthCardMoneyForDay;
			
			void init() throws Exception {
				int RMB = Integer.parseInt(goodsPrice);
				if (RMB < 1) {
					throw new Exception("price错误 goodsName=" + goodsName + " price=" + goodsPrice);
				}
				
				rmbIngot = RMB * KPaymentListener.ChargeRate;

				if (presentIngot < 0) {
					throw new Exception("presentIngot错误 goodsName=" + goodsName + " price=" + goodsPrice);
				}
				
				if (returnRateForFirst < 0 || returnRateForFirst > 100) {
					throw new Exception("returnRateForFirst错误 goodsName=" + goodsName + " price=" + goodsPrice);
				}
				
//				if(baseIngot < 1 && returnRateForFirst > 0){
//					throw new Exception("returnRateForFirst错误 goodsName=" + goodsName + " price=" + goodsPrice);
//				}
				
				if (monthCardKeepDays > 0) {
					//月卡
					if(monthCardIngotForDay < 1){
						throw new Exception("monthCardIngotForDay错误 goodsName=" + goodsName + " price=" + goodsPrice);
					}
					monthCardMoneyForDay = Arrays.asList(new KCurrencyCountStruct(KCurrencyTypeEnum.DIAMOND, monthCardIngotForDay));
				} else {
					// 非月卡
					if(monthCardIngotForDay > 0 || isMonthCardCanBuyMuil){
						throw new Exception("月卡参数错误 goodsName=" + goodsName + " price=" + goodsPrice);
					}
					
					if(baseIngot < 1){
						throw new Exception("baseIngot错误 goodsName=" + goodsName + " price=" + goodsPrice);
					}
				}
			}

			public boolean isMonthCard(){
				return monthCardKeepDays > 0;
			}
			
			void notifyCacheLoadComplete() throws Exception {
			}
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 首充奖励数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2013-5-17 上午11:53:20
	 * </pre>
	 */
	public static class FirstChargeRewardDataManager {
		// ----------以下是EXCEL表格直导数据---------
		public int keepDays;
		public int minChargeForGift;// 礼包要求最低充值额度(元宝)
	
		// ----------以下是逻辑数据---------
		private BaseRewardDataForJobs baseReForJobs;
	
		FirstChargeRewardDataManager(BaseRewardDataForJobs baseReForJobs) {
			this.baseReForJobs = baseReForJobs;
		}
	
		public List<ItemCountStruct> getReward(byte job) {
			return baseReForJobs.getBaseRewardData(job).itemStructs;
		}
	
		/**
		 * <pre>
		 * 验证所有数据
		 * 
		 * @throws KGameServerException
		 * @author CamusHuang
		 * @creation 2013-1-4 下午8:26:02
		 * </pre>
		 */
		void notifyCacheLoadComplete() throws Exception {
			
			
			if (keepDays < 1) {
				throw new Exception("首充结束天数 keepDays=" + keepDays);
			}
	
			if (minChargeForGift < 0) {
				throw new Exception("首充返利最低充值额度错误 minChargeForGift=" + minChargeForGift);
			}
	
			if (baseReForJobs == null) {
				throw new Exception("首充奖励未配置");
			}
			baseReForJobs.notifyCacheLoadComplete();
	
			for (KJobTypeEnum job : KJobTypeEnum.values()) {
				BaseRewardData temp = baseReForJobs.getBaseRewardData(job.getJobType());
				if (!temp.attList.isEmpty()) {
					throw new Exception("首充奖励不能配置属性");
				}
				if (!temp.petTempIdList.isEmpty()) {
					throw new Exception("首充奖励不能配置宠物");
				}
				if (!temp.fashionTempIdList.isEmpty()) {
					throw new Exception("首充奖励不能配置时装");
				}
				if (!temp.moneyList.isEmpty()) {
					throw new Exception("首充奖励不能配置货币");
				}
				if (temp.itemStructs.isEmpty()) {
					throw new Exception("首充奖励必须配置物品");
				}
			}
		}
	}

	/**
	 * <pre>
	 * 开始加载文件
	 * @param configPath
	 * @author CamusHuang
	 * @creation 2012-11-9 下午7:38:27
	 * </pre>
	 */
	static void goToLoadData(Element excelE) throws Exception {

		try {
			// 加载数据
			{
				Element tempE = excelE.getChild("diamondToGold");
				int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
				KGameExcelFile file = new KGameExcelFile(tempE.getChildTextTrim("path"));

				loadDiamondToGoldDatas(file.getTable(SheetName_等级兑换比例, HeaderIndex));
			}
			// 加载数据
			{
				Element tempE = excelE.getChild("chargeInfos");
				int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
				KGameExcelFile file = new KGameExcelFile(tempE.getChildTextTrim("path"));
				{
					KGameExcelTable configTable = file.getTable(SheetName_充值配置, HeaderIndex);
					KGameExcelTable dataTable = file.getTable(SheetName_充值档位, HeaderIndex);
					loadChargeInfoDatas(configTable, dataTable);
				}
				{
					KGameExcelTable dataTable = file.getTable(SheetName_首充奖励数据表, HeaderIndex);
					loadFirstChargeRewardDatas(dataTable);
				}
			}
		} catch (KGameServerException e) {
			throw e;
		} catch (Exception e) {
			throw new KGameServerException(e.getMessage(), e);
		}
	}

	// /////////////////////////////////////////
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
	private static void loadDiamondToGoldDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		for (KGameExcelRow row : rows) {
			// level GoldExchangeFor10

			int lv = row.getInt("level");
			int value = row.getInt("GoldExchangeFor10");
			mDiamondToGoldDataManager.addData(lv, value);
		}
	}

	/**
	 * <pre>
	 * 加载首充奖励数据
	 * 
	 * @param rows
	 * @throws KGameServerException
	 * @throws Exception
	 * @author camus
	 * @creation 2012-12-30 下午11:46:22
	 * </pre>
	 */
	private static void loadFirstChargeRewardDatas(KGameExcelTable table) throws KGameServerException, Exception {

		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			return;
		}

		if (rows.length > 1) {
			throw new KGameServerException("加载" + table.getTableName() + "错误：有效行数为不能大于1！");
		}

		KGameExcelRow row = rows[0];
		
		try {
			mFirstChargeRewardDataManager = new FirstChargeRewardDataManager(BaseRewardDataForJobs.loadData(row, false));
			ReflectPaser.parseExcelData(mFirstChargeRewardDataManager, row, false);
			
		} catch (Exception e) {
			throw new Exception("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
		}
	}
	

	// /////////////////////////////////////////

	private static void loadChargeInfoDatas(KGameExcelTable configTable, KGameExcelTable dataTable) throws KGameServerException {
		{
			KGameExcelRow[] rows = configTable.getAllDataRows();
			mChargeInfoManager.CNY_UNIT = rows[0].getData("CNY_UNIT");
			mChargeInfoManager.tips = rows[0].getData("tips");

			if (mChargeInfoManager.CNY_UNIT == null || mChargeInfoManager.CNY_UNIT.isEmpty()) {
				throw new KGameServerException("加载【" + configTable.getTableName() + "】有误：CNY_UNIT＝＝null");
			}
			if (mChargeInfoManager.tips == null || mChargeInfoManager.tips.isEmpty()) {
				throw new KGameServerException("加载【" + configTable.getTableName() + "】有误：tips＝＝null");
			}
		}
		// //////////////////////
		{
			KGameExcelRow[] rows = dataTable.getAllDataRows();
			if (rows.length < 1) {
				throw new KGameServerException("加载【" + dataTable.getTableName() + "】有误：行数为0");
			}

			try {
				mChargeInfoManager.initDatas(ReflectPaser.parseExcelData(ChargeInfoStruct.class, dataTable.getHeaderNames(), rows, true));
			} catch (Exception e) {
				throw new KGameServerException("加载【" + dataTable.getTableName() + "】有误：" + e.getMessage(), e);
			}
		}
	}

	// /////////////////////////////////////////

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
			mDiamondToGoldDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_等级兑换比例 + "]错误：" + e.getMessage(), e);
		}

		try {
			mChargeInfoManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_充值档位 + "]错误：" + e.getMessage(), e);
		}
		
		try {
			mFirstChargeRewardDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_首充奖励数据表 + "]错误：" + e.getMessage(), e);
		}
	}
}
