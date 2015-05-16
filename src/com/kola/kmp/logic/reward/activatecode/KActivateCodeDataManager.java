package com.kola.kmp.logic.reward.activatecode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.StringUtil;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.ReflectPaser;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.reward.activatecode.KActivateCodeDataManager.ActivationRewardDataManager.ActivationRewardData;
import com.kola.kmp.logic.reward.activatecode.KActivateCodeDataManager.BigTypeDataManager.BigTypeData;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager;
import com.kola.kmp.logic.shop.timehot.KHotShopCenter;
import com.kola.kmp.logic.shop.timehot.KHotShopDataManager;
import com.kola.kmp.logic.shop.timehot.message.KPushHotGoodsMsg;

/**
 * <pre>
 * 本类负责管理本模块的公共数据 
 * 
 * @author CamusHuang
 * @creation 2013-4-8 下午2:39:56
 * </pre>
 */
public class KActivateCodeDataManager {

	static String DreamHTTPAddress = "http://data.xxwan.com/gift";
	static String DreamKey = "0d6f9fc43f4952879357dc06a3f1c5d5";
	//
	static String YYHTTPAddress = "http://task.g.yy.com/task/daily/complete.do";
	static String YYPassport = "my_good_game";
	static String YYTid = "1118";
	static String YYKey = "4@#aX9Has*72";
	
	static void loadConfig(Element e) throws KGameServerException {
		{
			Element tempE = e.getChild("Dream");
			DreamHTTPAddress = tempE.getChildTextTrim("DreamHTTPAddress");
			DreamKey  = tempE.getChildTextTrim("DreamKey");
		}
		{
			Element tempE = e.getChild("YY");
			YYHTTPAddress = tempE.getChildTextTrim("YYHTTPAddress");
			YYPassport = tempE.getChildTextTrim("YYPassport");
			YYTid = tempE.getChildTextTrim("YYTid");
			YYKey = tempE.getChildTextTrim("YYKey");
		}
	}

	private static String ExcelPath;
	private static int HeaderIndex;

	static final String SheetName_大类型 = "大类型";
	static final String SheetName_子类型及奖励 = "子类型及奖励";

	/**
	 * <pre>
	 * 开始加载文件
	 * @param configPath
	 * @author CamusHuang
	 * @creation 2012-11-9 下午7:38:27
	 * </pre>
	 */
	static void goToLoadData(String fileUrl, int headerIndex) throws Exception {

		ExcelPath = fileUrl;
		HeaderIndex = headerIndex;

		loadData(false);
	}

	/**
	 * <pre>
	 * 开始加载文件
	 * @param configPath
	 * @author CamusHuang
	 * @creation 2012-11-9 下午7:38:27
	 * </pre>
	 */
	public static String reloadData() {
		try {
			loadData(true);
			return "加载完成";
		} catch (Exception e) {
			KActivateCodeCenter._ACTIVATECODELOGGER.error(e.getMessage(), e);
			return "发生异常：" + e.getMessage();
		}
	}

	public static void loadData(boolean isReload) throws Exception {
		BigTypeDataManager mBigTypeDataManager = new BigTypeDataManager();
		ActivationRewardDataManager mActivationRewardDataManager = new ActivationRewardDataManager();

		// 加载数据
		KGameExcelFile file = new KGameExcelFile(ExcelPath);
		{
			KGameExcelTable table = file.getTable(SheetName_大类型, HeaderIndex);
			KGameExcelRow[] rows = table.getAllDataRows();
			if (rows.length < 1) {
				throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
			}
			//
			List<BigTypeData> datas = ReflectPaser.parseExcelData(BigTypeData.class, table.getHeaderNames(), rows, true);

			mBigTypeDataManager.initDatas(datas);
		}
		{
			KGameExcelTable table = file.getTable(SheetName_子类型及奖励, HeaderIndex);
			KGameExcelRow[] rows = table.getAllDataRows();
			if (rows.length < 1) {
				throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
			}

			List<ActivationRewardData> datas = new ArrayList<ActivationRewardData>();
			{
				List<String> headerNames = table.getHeaderNames();
				Class<ActivationRewardData> clazz = ActivationRewardData.class;
				for (KGameExcelRow row : rows) {
					ActivationRewardData obj = clazz.newInstance();
					ReflectPaser.parseExcelData(obj, headerNames, row, false, false);
					obj.baseRewardData = BaseRewardData.loadData(row, false);
					datas.add(obj);
				}
			}
			mActivationRewardDataManager.initDatas(datas);
		}

		if (isReload) {
			notifyCacheLoadComplete(mBigTypeDataManager, mActivationRewardDataManager);
		}

		KActivateCodeDataManager.mBigTypeDataManager = mBigTypeDataManager;
		KActivateCodeDataManager.mActivationRewardDataManager = mActivationRewardDataManager;
	}

	// /////////////////////////////////////////

	/**
	 * <pre>
	 * 大类型管理器
	 * </pre>
	 */
	public static BigTypeDataManager mBigTypeDataManager = new BigTypeDataManager();

	/**
	 * <pre>
	 * 子类型及奖励管理器
	 * </pre>
	 */
	public static ActivationRewardDataManager mActivationRewardDataManager = new ActivationRewardDataManager();

	// /////////////////////////////////////
	public static class BigTypeDataManager {
		/**
		 * <pre>
		 * KEY = 大类型
		 * unmodifiable
		 * </pre>
		 */
		private Map<String, BigTypeData> dataMap = new HashMap<String, BigTypeData>();

		void initDatas(List<BigTypeData> datas) throws Exception {
			for (BigTypeData data : datas) {
				if (dataMap.put(data.type, data) != null) {
					throw new Exception("大类型重复 type=" + data.type);
				}
			}
		}

		BigTypeData getData(String type) {
			return dataMap.get(type);
		}

		void notifyCacheLoadComplete(ActivationRewardDataManager mActivationRewardDataManager) throws Exception {

			for (Entry<String, BigTypeData> e : dataMap.entrySet()) {
				if (!mActivationRewardDataManager.containType(e.getKey())) {
					throw new Exception("大类型未配置奖励 type=" + e.getKey());
				}

				try {
					e.getValue().notifyCacheLoadComplete();
				} catch (Exception ex) {
					throw new Exception(ex.getMessage() + " type=" + ex.getMessage(), ex);
				}
			}
		}

		public static class BigTypeData {
			public String type;// 大类型
			public int useSonTypeCount;// 每个角色可用子类型数量
			public boolean oneCodeMuilUse;// 是否一码多用
			private int promoType;// 使用后是否需要通知合作方接口
			//
			public KActivateCodePromoTypeEnum promoTypeEnum;// 使用后是否需要通知合作方接口

			void notifyCacheLoadComplete() throws Exception {
				if (useSonTypeCount < 1) {
					throw new Exception("useSonTypeCount错误=" + useSonTypeCount);
				}
				
				promoTypeEnum = KActivateCodePromoTypeEnum.getEnum(promoType);
				if (promoTypeEnum == null) {
					throw new KGameServerException("激活码渠道类型错误 = " + promoType);
				}
			}
		}
	}

	// /////////////////////////////////////
	/**
	 * <pre>
	 * 子类型及奖励管理器
	 * 
	 * @author CamusHuang
	 * @creation 2013-5-17 上午11:53:20
	 * </pre>
	 */
	public static class ActivationRewardDataManager {
		/**
		 * <pre>
		 * KEY = 激活码数据库分组ID
		 * unmodifiable
		 * </pre>
		 */
		private Map<Integer, ActivationRewardData> dataMapByCodeGroupId = new HashMap<Integer, ActivationRewardData>();

		private List<ActivationRewardData> dataList = new ArrayList<ActivationRewardData>();

		private ActivationRewardDataManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param data
		 * @throws KGameServerException
		 * @author CamusHuang
		 * @creation 2013-5-17 下午5:21:57
		 * </pre>
		 */
		void initDatas(List<ActivationRewardData> datas) throws Exception {

			// <大类型,<小类型>>
			Map<String, Set<Integer>> typeData = new HashMap<String, Set<Integer>>();

			for (ActivationRewardData data : datas) {

				Set<Integer> set = typeData.get(data.type);
				if (set == null) {
					set = new HashSet<Integer>();
					typeData.put(data.type, set);
				}

				if (!set.add(data.sonType)) {
					throw new Exception("重复的子类型=" + data.sonType + " 类型=" + data.type);
				}

				for (int codeGroupId : data.codeGroupId) {
					if (dataMapByCodeGroupId.put(codeGroupId, data) != null) {
						throw new Exception("重复的激活码数据库分组=" + codeGroupId + " 子类型=" + data.sonType + " 类型=" + data.type);
					}
				}
			}

			dataList.addAll(datas);
		}

		/**
		 * <pre>
		 * 检查所有奖励是否包含指定的大类型
		 * 
		 * @param type
		 * @return
		 * @author CamusHuang
		 * @creation 2014-10-10 上午11:24:58
		 * </pre>
		 */
		boolean containType(String type) {
			for (ActivationRewardData data : dataMapByCodeGroupId.values()) {
				if (data.type.equals(type)) {
					return true;
				}
			}
			return false;
		}

		ActivationRewardData getData(int codeGroupId) {
			return dataMapByCodeGroupId.get(codeGroupId);
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
		void notifyCacheLoadComplete(BigTypeDataManager mBigTypeDataManager) throws Exception {

			if (dataMapByCodeGroupId.isEmpty()) {
				throw new Exception("子类型及奖励 数据不能为空");
			}

			for (ActivationRewardData data : dataList) {
				try {
					data.notifyCacheLoadComplete(mBigTypeDataManager);
				} catch (Exception e) {
					throw new Exception(e.getMessage() + " 子类型=" + data.sonType + " 类型=" + data.type, e);
				}
			}
		}

		// //////////////////////////
		/**
		 * <pre>
		 * 激活码奖励类型
		 * 
		 * @author CamusHuang
		 * @creation 2013-5-30 上午9:40:13
		 * </pre>
		 */
		public static class ActivationRewardData {
			// ----------以下是EXCEL表格直导数据---------
			public int sonType;// 子类型名称
			public String type;// 所属大类型
			private int[] codeGroupId;// 所包含的激活码数据库分组
			private String endTimeStr;// 有效期

			// ----------以下是逻辑数据---------
			public long endTime;// 有效期
			// 奖励内容
			public BaseRewardData baseRewardData;

			void notifyCacheLoadComplete(BigTypeDataManager mBigTypeDataManager) throws Exception {
				// 验证
				baseRewardData.notifyCacheLoadComplete();
				baseRewardData.checkEffect();

				Set<Integer> temp = new HashSet<Integer>();
				for (int id : codeGroupId) {
					if (!temp.add(id)) {
						throw new Exception("激活码数据库分组ID重复 codeGroupId=" + id);
					}
				}

				if (codeGroupId.length < 1) {
					throw new Exception("所包含的激活码数据库分组 不能为空");
				}

				if (mBigTypeDataManager.getData(type) == null) {
					throw new Exception("type错误=" + type);
				}

				endTime = UtilTool.DATE_FORMAT.parse(endTimeStr).getTime();
			}
		}
	}

	static void notifyCacheLoadComplete() throws KGameServerException {
		notifyCacheLoadComplete(mBigTypeDataManager, mActivationRewardDataManager);
	}

	/**
	 * <pre>
	 * 服务器启动完成
	 * 
	 * @author CamusHuang
	 * @creation 2012-12-3 下午8:41:04
	 * </pre>
	 */
	private static void notifyCacheLoadComplete(BigTypeDataManager mBigTypeDataManager, ActivationRewardDataManager mActivationRewardDataManager) throws KGameServerException {
		try {
			mBigTypeDataManager.notifyCacheLoadComplete(mActivationRewardDataManager);
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_大类型 + "]错误：" + e.getMessage(), e);
		}

		try {
			mActivationRewardDataManager.notifyCacheLoadComplete(mBigTypeDataManager);
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_子类型及奖励 + "]错误：" + e.getMessage(), e);
		}
	}
}
