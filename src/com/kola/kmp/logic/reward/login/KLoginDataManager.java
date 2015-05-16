package com.kola.kmp.logic.reward.login;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.ReflectPaser;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailRewardDataForJobs;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardDataForJobs;
import com.kola.kmp.logic.reward.login.KLoginDataManager.KAddCheckDataManager.AddCheckData;
import com.kola.kmp.logic.reward.login.KLoginDataManager.KCheckUpRewardDataManager.CheckUpRewardData;
import com.kola.kmp.logic.reward.login.KLoginDataManager.KSevenRewardDataManager.SevenRewardData;
import com.kola.kmp.logic.util.tips.RewardTips;

/**
 * <pre>
 * 本类负责管理本模块的公共数据 
 * 
 * @author CamusHuang
 * @creation 2013-4-8 下午2:39:56
 * </pre>
 */
public class KLoginDataManager {

	// 签到最大天数
	public static final int MaxCheckUpSize = 31;

	static void loadConfig(Element e) throws KGameServerException {
		// MaxRewardShowSize =
		// Integer.parseInt(e.getChildTextTrim("MaxCheckUpSize"));
		// if (MaxRewardShowSize < 7) {
		// throw new KGameServerException("MaxRewardShowSize 数值错误");
		// }
	}

	//
	static final String SheetName_签到登录奖励 = "签到登录奖励";
	static final String SheetName_补签钻石消耗 = "补签钻石消耗";
	static final String SheetName_七天登录奖励 = "七天登录奖励";

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
			loadCheckUpRewardDatas(file.getTable(SheetName_签到登录奖励, HeaderIndex));

			loadAddCheckDatas(file.getTable(SheetName_补签钻石消耗, HeaderIndex));

			loadSevenRewardRateDatas(file.getTable(SheetName_七天登录奖励, HeaderIndex));
		}
	}

	private static void loadAddCheckDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<AddCheckData> datas = null;
		try {
			datas = ReflectPaser.parseExcelData(AddCheckData.class, table.getHeaderNames(), rows, true);
		} catch (Exception e) {
			throw new Exception("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
		}

		mAddCheckDataManager.init(datas);
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
	private static void loadCheckUpRewardDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		try {
			List<CheckUpRewardData> datas = new ArrayList<CheckUpRewardData>();
			//
			for (KGameExcelRow row : rows) {
				int id = row.getInt("ID");
				BaseRewardDataForJobs baseReward = BaseRewardDataForJobs.loadData(row, false);

				datas.add(new CheckUpRewardData(id, baseReward));
			}
			mCheckUpRewardDataManager.init(datas);
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
	private static void loadSevenRewardRateDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		try {
			List<SevenRewardData> datas = new ArrayList<SevenRewardData>();
			//
			for (KGameExcelRow row : rows) {
				int id = row.getInt("ID");
				int vip = row.getInt("vip");
				BaseRewardDataForJobs baseReward = BaseRewardDataForJobs.loadData(row, false);

				datas.add(new SevenRewardData(id, vip, baseReward));
			}
			mSevenRewardDataManager.init(datas);
		} catch (Exception e) {
			throw new Exception("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
		}
	}

	// /////////////////////////////////////////

	/**
	 * <pre>
	 * 签到奖励数据
	 * </pre>
	 */
	public static KCheckUpRewardDataManager mCheckUpRewardDataManager = new KCheckUpRewardDataManager();

	/**
	 * <pre>
	 * 补签价格数据
	 * </pre>
	 */
	public static KAddCheckDataManager mAddCheckDataManager = new KAddCheckDataManager();

	/**
	 * <pre>
	 * 七天登录奖励数据
	 * </pre>
	 */
	public static KSevenRewardDataManager mSevenRewardDataManager = new KSevenRewardDataManager();

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 签到奖励数据
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-8 下午2:40:38
	 * </pre>
	 */
	public static class KCheckUpRewardDataManager {
		/**
		 * <pre>
		 * 累计登陆奖励
		 * 不一定连续
		 * KEY = 累计登陆天数
		 * </pre>
		 */
		private HashMap<Integer, CheckUpRewardData> dataMap = new HashMap<Integer, CheckUpRewardData>();

		private List<CheckUpRewardData> dataList = new ArrayList<CheckUpRewardData>();

		private int MaxDay;

		void init(List<CheckUpRewardData> datas) throws Exception {
			if (datas.size() > MaxCheckUpSize) {
				throw new Exception("天数必须 <= " + MaxCheckUpSize);
			}
			dataMap.clear();
			for (CheckUpRewardData data : datas) {
				if (dataMap.put(data.ID, data) != null) {
					throw new Exception("重复的天数 = " + data.ID);
				}

				if (data.ID > MaxDay) {
					MaxDay = data.ID;
				}
			}

			dataList.clear();
			dataList.addAll(datas);
			Collections.sort(dataList);
		}

		/**
		 * <pre>
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2013-4-23 下午7:53:53
		 * </pre>
		 */
		public CheckUpRewardData getData(int day) {
			return dataMap.get(day);
		}

		public CheckUpRewardData getNextData(int day) {
			if (day >= MaxDay) {
				return null;
			}
			for (int temp = day + 1; temp <= MaxDay; temp++) {
				if (dataMap.containsKey(temp)) {
					return dataMap.get(temp);
				}
			}
			return null;
		}

		public List<CheckUpRewardData> getDataCache() {
			return dataList;
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
			for (CheckUpRewardData data : dataMap.values()) {
				data.notifyCacheLoadComplete();
			}
		}

		// //////////////////////////
		/**
		 * <pre>
		 * 签到奖励
		 * 
		 * @author CamusHuang
		 * @creation 2014-2-21 下午3:10:02
		 * </pre>
		 */
		public static class CheckUpRewardData implements Comparable<CheckUpRewardData> {
			// ----------以下是EXCEL表格直导数据---------

			// ----------以下是逻辑数据---------
			public int ID;// 登录天数
			public BaseRewardDataForJobs baseReward;// 全职业奖励

			CheckUpRewardData(int iD, BaseRewardDataForJobs baseReward) {
				ID = iD;
				this.baseReward = baseReward;
			}

			void notifyCacheLoadComplete() throws Exception {
				if (ID < 1) {
					throw new KGameServerException("数值错误 ID = " + ID);
				}

				baseReward.notifyCacheLoadComplete();
			}

			@Override
			public int compareTo(CheckUpRewardData o) {
				if (ID < o.ID) {
					return -1;
				}
				if (ID > o.ID) {
					return 1;
				}
				return 0;
			}
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 补签价格
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-8 下午2:40:38
	 * </pre>
	 */
	public static class KAddCheckDataManager {

		private LinkedHashMap<Integer, AddCheckData> dataMap = new LinkedHashMap<Integer, AddCheckData>();

		void init(List<AddCheckData> datas) throws Exception {
			if (datas.size() != MaxCheckUpSize) {
				throw new Exception("天数必须 = " + MaxCheckUpSize);
			}
			dataMap.clear();
			for (AddCheckData data : datas) {
				if (dataMap.put(data.ID, data) != null) {
					throw new Exception("重复的天数 = " + data.ID);
				}
			}

			for (int day = 1; day <= dataMap.size(); day++) {
				if (!dataMap.containsKey(day)) {
					throw new Exception("缺少天数 = " + day);
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
		public AddCheckData getData(int day) {
			return dataMap.get(day);
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
			for (AddCheckData data : dataMap.values()) {
				data.notifyCacheLoadComplete();
			}
		}

		// //////////////////////////
		/**
		 * <pre>
		 * 签到价格
		 * 
		 * @author CamusHuang
		 * @creation 2014-11-16 下午9:51:41
		 * </pre>
		 */
		public static class AddCheckData {
			// ----------以下是EXCEL表格直导数据---------
			public int ID;// 补签次数
			private int Diamonds;// 需要钻石

			// ----------以下是逻辑数据---------
			public KCurrencyCountStruct price;

			void notifyCacheLoadComplete() throws KGameServerException {
				if (ID < 1) {
					throw new KGameServerException("数值错误 ID = " + ID);
				}

				if (Diamonds < 0) {
					throw new KGameServerException("数值错误 Diamonds = " + Diamonds);
				}

				price = new KCurrencyCountStruct(KCurrencyTypeEnum.DIAMOND, Diamonds);

			}
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 七天登录特殊奖励数据
	 * 
	 * @author CamusHuang
	 * @creation 2014-11-11 下午7:22:08
	 * </pre>
	 */
	public static class KSevenRewardDataManager {
		/**
		 * <pre>
		 * KEY = 天数
		 * </pre>
		 */
		private LinkedHashMap<Integer, SevenRewardData> dataMap = new LinkedHashMap<Integer, SevenRewardData>();

		void init(List<SevenRewardData> datas) throws Exception {
			for (SevenRewardData data : datas) {
				if (dataMap.put(data.ID, data) != null) {
					throw new Exception("重复的天数 = " + data.ID);
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
		public SevenRewardData getData(int day) {
			return dataMap.get(day);
		}

		/**
		 * <pre>
		 * 
		 * @deprecated 直接获取缓存，谨慎使用
		 * @author CamusHuang
		 * @creation 2014-4-25 下午12:21:21
		 * </pre>
		 */
		public LinkedHashMap<Integer, SevenRewardData> getDataCache() {
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
			for (int day = 1; day <= dataMap.size(); day++) {

				SevenRewardData data = dataMap.get(day);
				if (data == null) {
					throw new KGameServerException("七天奖励数据缺失 day=" + day);
				}
				data.notifyCacheLoadComplete();
			}
		}

		// //////////////////////////
		/**
		 * <pre>
		 * 七天登陆奖励
		 * 
		 * @author CamusHuang
		 * @creation 2014-11-11 下午7:22:21
		 * </pre>
		 */
		public static class SevenRewardData {
			// ----------以下是EXCEL表格直导数据---------

			// ----------以下是逻辑数据---------
			public int ID;// 登录天数
			public int vip;// 双倍vip级别(>0时表示有双倍)
			private BaseRewardDataForJobs baseReward;// 全职业奖励
			//
			public BaseMailRewardDataForJobs baseMailReward;// 全职业奖励
			public BaseMailRewardDataForJobs vipBaseMailReward;// VIP全职业奖励

			SevenRewardData(int id, int vip, BaseRewardDataForJobs baseReward) {
				ID = id;
				this.vip = vip;
				this.baseReward = baseReward;
			}

			void notifyCacheLoadComplete() throws Exception {
				if (ID < 1) {
					throw new KGameServerException("数值错误 ID = " + ID);
				}

				baseReward.notifyCacheLoadComplete();

				// 检查奖励内容是否有效
				if (!baseReward.checkIsEffect()) {
					throw new KGameServerException("所有奖励项无效");
				}

				BaseMailContent baseMail = new BaseMailContent(RewardTips.七天登陆奖励邮件标题, RewardTips.背包已满通用奖励邮件内容, null, null);
				baseMailReward = new BaseMailRewardDataForJobs(1, baseMail, baseReward);
				// VIP双倍
				BaseRewardDataForJobs baseRewardForVip = BaseRewardDataForJobs.copyForRate(baseReward, 2, false);
				vipBaseMailReward = new BaseMailRewardDataForJobs(1, baseMail, baseRewardForVip);
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
			mCheckUpRewardDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_签到登录奖励 + "]错误：" + e.getMessage(), e);
		}

		try {
			mAddCheckDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_补签钻石消耗 + "]错误：" + e.getMessage(), e);
		}

		try {
			mSevenRewardDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_七天登录奖励 + "]错误：" + e.getMessage(), e);
		}
	}
}
