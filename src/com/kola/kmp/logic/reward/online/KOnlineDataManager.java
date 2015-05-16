package com.kola.kmp.logic.reward.online;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.timer.Timer;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.ReflectPaser;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.reward.online.KOnlineDataManager.KOnlineRewardDataManager.OnlineRewardData;

/**
 * <pre>
 * 本类负责管理本模块的公共数据 
 * 
 * @author CamusHuang
 * @creation 2013-4-8 下午2:39:56
 * </pre>
 */
public class KOnlineDataManager {
	//
	static final String SheetName_第一天在线奖励 = "第一天在线奖励";
	static final String SheetName_在线奖励 = "在线奖励";
	static final long AllowanceOnlineTime = 20*Timer.ONE_SECOND;//在线时长宽容度

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
			loadFirstDayRewardDatas(file.getTable(SheetName_第一天在线奖励, HeaderIndex));

			loadOtherDayRewardDatas(file.getTable(SheetName_在线奖励, HeaderIndex));
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
	private static void loadFirstDayRewardDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<OnlineRewardData> datas = ReflectPaser.parseExcelData(OnlineRewardData.class, table.getHeaderNames(), rows, true);
		mFirstDayRewardDataManager.init(datas);
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
	private static void loadOtherDayRewardDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<OnlineRewardData> datas = ReflectPaser.parseExcelData(OnlineRewardData.class, table.getHeaderNames(), rows, true);
		mOtherDayRewardDataManager.init(datas);
	}

	// /////////////////////////////////////////

	/**
	 * <pre>
	 * 登陆第一天在线奖励数据
	 * </pre>
	 */
	public static KOnlineRewardDataManager mFirstDayRewardDataManager = new KOnlineRewardDataManager();

	/**
	 * <pre>
	 * 登陆第N天在线奖励数据
	 * </pre>
	 */
	public static KOnlineRewardDataManager mOtherDayRewardDataManager = new KOnlineRewardDataManager();

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 在线奖励数据
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-8 下午2:40:38
	 * </pre>
	 */
	public static class KOnlineRewardDataManager {
		// <ID,数据>
		private Map<Integer, OnlineRewardData> dataMap = new HashMap<Integer, OnlineRewardData>();

		void init(List<OnlineRewardData> datas) throws Exception {
			int id = 1;
			for (OnlineRewardData data : datas) {
				data.id = id;
				dataMap.put(id, data);
				id++;
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
		public OnlineRewardData getData(int id) {
			return dataMap.get(id);
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
			for (OnlineRewardData data : dataMap.values()) {
				data.notifyCacheLoadComplete();
			}
		}

		// //////////////////////////
		/**
		 * <pre>
		 * 在线奖励
		 * 
		 * @author CamusHuang
		 * @creation 2014-2-21 下午3:10:02
		 * </pre>
		 */
		public static class OnlineRewardData {
			// ----------以下是EXCEL表格直导数据---------
			public int time;// 在线时间(s)
			public String ItemID;// 奖励道具ID
			public int moneyType;// 奖励货币类型
			public int moneyCount;// 货币数量

			// ----------以下是逻辑数据---------
			public int id;
			public long timeMills;// 在线时间(ms)
			public BaseRewardData baseReward;

			void notifyCacheLoadComplete() throws KGameServerException {
				if (time < 1) {
					throw new KGameServerException("数值错误 time = " + time);
				}
				timeMills = time *  Timer.ONE_SECOND;
				List<KCurrencyCountStruct> addMoneys = null;
				List<ItemCountStruct> addItems = null;
				if (ItemID.length() > 0) {
					addItems = new ArrayList<KDataStructs.ItemCountStruct>();
					String[] temps = ItemID.split("\\*");
					ItemCountStruct tempS = new ItemCountStruct(temps[0], Long.parseLong(temps[1]));
					if (tempS.getItemTemplate() == null) {
						throw new KGameServerException("道具模板不存在=" + temps[0]);
					}
					if (tempS.itemCount < 1) {
						throw new KGameServerException("道具数量错误=" + temps[1]);
					}
					addItems.add(tempS);
				}
				if (moneyType > 0 && moneyCount > 0) {
					addMoneys = new ArrayList<KCurrencyCountStruct>();
					KCurrencyCountStruct money = new KCurrencyCountStruct(KCurrencyTypeEnum.getEnum(moneyType), moneyCount);
					if (money.currencyType == null) {
						throw new KGameServerException("货币类型错误=" + moneyType);
					}
					addMoneys.add(money);
				}

				if ( (addMoneys==null || addMoneys.isEmpty()) && (addItems==null || addItems.isEmpty())) {
					throw new KGameServerException("在线奖励内容无效");
				}

				baseReward = new BaseRewardData(null, addMoneys, addItems, null, null);
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
			mFirstDayRewardDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_第一天在线奖励 + "]错误：" + e.getMessage(), e);
		}

		try {
			mOtherDayRewardDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_在线奖励 + "]错误：" + e.getMessage(), e);
		}
	}
}
