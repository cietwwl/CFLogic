package com.kola.kmp.logic.reward.daylucky;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.StringUtil;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.util.ReflectPaser;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KItemTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.reward.daylucky.KDayluckyDataManager.KDayluckyRateDataManager.DayluckyRateData;
import com.kola.kmp.logic.reward.daylucky.KDayluckyDataManager.KDayluckyRewardDataManager.DayluckyRewardData;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.RewardTips;

/**
 * <pre>
 * 本类负责管理本模块的公共数据 
 * 
 * @author CamusHuang
 * @creation 2013-4-8 下午2:39:56
 * </pre>
 */
public class KDayluckyDataManager {

	static final String DayluckyDailySaveDir = "./res/output/DayluckyDialy/";
	static final String DayluckyDailySaveFileName = "daily.xml";

	/** 大奖日志的最大数量 */
	static int BigRewardLogMaxCount = 10;
	/** 卡片的数量 */
	static final int NUM_COUNT = 3;

	static void loadConfig(Element e) {
		BigRewardLogMaxCount = Integer.parseInt(e.getChildTextTrim("BidRewardLogMaxCount"));
	}

	//
	static final String SheetName_每日幸运奖励 = "每日幸运奖励";
	static final String SheetName_每日幸运概率 = "每日幸运概率";

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
			loadDayluckyRewardDatas(file.getTable(SheetName_每日幸运奖励, HeaderIndex));

			loadDayluckyRateDatas(file.getTable(SheetName_每日幸运概率, HeaderIndex));
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
	private static void loadDayluckyRewardDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<DayluckyRewardData> datas = ReflectPaser.parseExcelData(DayluckyRewardData.class, table.getHeaderNames(), rows, true);
		KDayluckyDataManager.mDayluckyRewardDataManager.init(datas);
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
	private static void loadDayluckyRateDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<DayluckyRateData> datas = ReflectPaser.parseExcelData(DayluckyRateData.class, table.getHeaderNames(), rows, true);
		mDayluckyRateDataManager.init(datas);
	}

	// /////////////////////////////////////////

	/**
	 * <pre>
	 * 每日幸运奖项数据
	 * </pre>
	 */
	public static KDayluckyRewardDataManager mDayluckyRewardDataManager = new KDayluckyRewardDataManager();

	/**
	 * <pre>
	 * 每日幸运概率数据
	 * </pre>
	 */
	public static KDayluckyRateDataManager mDayluckyRateDataManager = new KDayluckyRateDataManager();

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 每日幸运奖项数据
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-8 下午2:40:38
	 * </pre>
	 */
	public static class KDayluckyRewardDataManager {
		/**
		 * <pre>
		 * 各等级数据
		 * KEY=中几张牌
		 * </pre>
		 */
		private LinkedHashMap<Integer, DayluckyRewardData> dataMap = new LinkedHashMap<Integer, DayluckyRewardData>();

		void init(List<DayluckyRewardData> datas) throws Exception {
			for (DayluckyRewardData data : datas) {
				if (dataMap.put(data.RewardGroup, data) != null) {
					throw new Exception("中奖号码组重复 RewardGroup=" + data.RewardGroup);
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
		public DayluckyRewardData getData(int cardNum) {
			return dataMap.get(cardNum);
		}

		/**
		 * <pre>
		 * 
		 * @deprecated 直接获取缓存，请谨慎使用
		 * @return
		 * @author CamusHuang
		 * @creation 2014-4-23 上午9:50:17
		 * </pre>
		 */
		public LinkedHashMap<Integer, DayluckyRewardData> getDataCache() {
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

			int rewardNum = KDayluckyDataManager.NUM_COUNT + 1;

			if (dataMap.size() != rewardNum) {
				throw new Exception("中奖数据不足");
			}

			for (int type = 1; type <= rewardNum; type++) {
				DayluckyRewardData data = dataMap.get(type);
				if (data == null) {
					throw new Exception("缺失中奖号码组 =" + type);
				}
				data.notifyCacheLoadComplete();
			}
		}

		// //////////////////////////
		/**
		 * <pre>
		 * 
		 * @author CamusHuang
		 * @creation 2014-2-21 下午3:10:02
		 * </pre>
		 */
		public static class DayluckyRewardData {
			// ----------以下是EXCEL表格直导数据---------
			private String ItemID;// 物品ID*数量;物品ID*数量
			public int RewardGroup;// 中奖号码组，即中几张牌
			private int news;// 显示记录

			// ----------以下是逻辑数据---------
			public List<ItemCountStruct> addItems;
			public BaseMailRewardData mailReward;
			public boolean isShowInDialy;

			void notifyCacheLoadComplete() throws Exception {
				String[] opentiem = ItemID.split(",");
				addItems = new ArrayList<ItemCountStruct>();
				ItemCountStruct.paramsItems(opentiem, addItems, 1);
				
				BaseMailContent baseMail = new BaseMailContent(StringUtil.format(RewardTips.刮刮卡x档发奖邮件标题, RewardGroup), RewardTips.背包已满通用奖励邮件内容, null, null);
				BaseRewardData baseRewardData = new BaseRewardData(null, null, addItems, null, null, true, false);
				mailReward = new BaseMailRewardData(RewardGroup, baseMail, baseRewardData);
				mailReward.notifyCacheLoadComplete();
				
				if (news > 0) {
					isShowInDialy = true;
				}
			}

		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 每日幸运概率数据
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-8 下午2:40:38
	 * </pre>
	 */
	public static class KDayluckyRateDataManager {
		/**
		 * <pre>
		 * KEY = 次数
		 * </pre>
		 */
		private Map<Integer, DayluckyRateData> dataMap = new HashMap<Integer, DayluckyRateData>();

		void init(List<DayluckyRateData> datas) throws Exception {
			for (DayluckyRateData data : datas) {
				dataMap.put(data.LegionLv, data);
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
		public DayluckyRateData getData(int time) {
			return dataMap.get(time);
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

			for (int time = 1; time <= dataMap.size(); time++) {
				DayluckyRateData data = dataMap.get(time);
				if (data == null) {
					throw new KGameServerException("缺失次数 =" + data);
				}
				data.notifyCacheLoadComplete();
			}
		}

		// //////////////////////////
		/**
		 * <pre>
		 * 
		 * @author CamusHuang
		 * @creation 2014-2-21 下午3:10:02
		 * </pre>
		 */
		public static class DayluckyRateData {
			// ----------以下是EXCEL表格直导数据---------
			public int LegionLv;// 刮奖次数
			private int luck1;// 幸运1号权重
			private int luck2;// 幸运2号权重
			private int luck3;// 幸运3号权重
			private int luck4;// 普通4号权重
			private int luck5;// 普通5号权重
			private int luck6;// 普通6号权重
			private int luck7;// 普通7号权重
			private int luck8;// 普通8号权重
			private int luck9;// 普通9号权重
			private int luck10;// 普通10号权重
			public int needvitality;// 需活跃度

			// ----------以下是逻辑数据---------
			// 1，2，3位置的幸运号码<号码位置,万分比几率>
			private Map<Integer, Integer> luckyRates = new HashMap<Integer, Integer>();
			// 4~10位置的非幸运号码<号码位置,权重>
			private Map<Integer, Integer> commonRates = new HashMap<Integer, Integer>();
			// 非幸运号码位置的权重总和
			private int commonTotalRate;

			void notifyCacheLoadComplete() throws KGameServerException {
				try {
					for (int pos = 1; pos <= 10; pos++) {
						String name = "luck" + pos;
						Field field = this.getClass().getDeclaredField(name);
						int rate = field.getInt(this);
						if (rate < 1) {
							throw new KGameServerException("权重错误 " + name + "= " + rate);
						}

						if (pos > KDayluckyDataManager.NUM_COUNT) {
							commonTotalRate += rate;
							commonRates.put(pos, rate);
						} else {
							luckyRates.put(pos, rate);
						}
					}
				} catch (Exception e) {
					throw new KGameServerException(e.getMessage(), e);
				}
			}

			/**
			 * <pre>
			 * 生成刮刮卡号码位置
			 * 1.先从3个幸运号码中按万比较几率单独随机，选出n（0~3）个号码位置
			 * 2.若n<3，则从7个非幸运号码中按权重随机选出（3-n）个号码位置
			 * 
			 * 警告：只是得到号码所处的位置，不是得到号码
			 * 
			 * @return 返回1~10中的三个位置
			 * @author CamusHuang
			 * @creation 2014-4-23 下午5:25:20
			 * </pre>
			 */
			public int[] randomNum() {
				int[] result = new int[KDayluckyDataManager.NUM_COUNT];
				int index = 0;
				for (int pos = 1; pos <= KDayluckyDataManager.NUM_COUNT; pos++) {
					if (UtilTool.random(1, 10000) <= luckyRates.get(pos)) {
						result[index] = pos;
						index++;
					}
				}

				if (index < result.length) {
					Map<Integer, Integer> rates = new HashMap<Integer, Integer>(this.commonRates);
					int totalRate = this.commonTotalRate;
					for (; index < result.length; index++) {
						Entry<Integer, Integer> temp = randomEntry(rates, totalRate);
						rates.remove(temp.getKey());
						totalRate -= temp.getValue();
						//
						result[index] = temp.getKey();
					}
				}

				return result;
			}

			private static Entry<Integer, Integer> randomEntry(Map<Integer, Integer> rates, int totalRate) {
				int rate = UtilTool.random(1, totalRate);
				for (Entry<Integer, Integer> entry : rates.entrySet()) {
					rate -= entry.getValue();
					if (rate <= 0) {
						return entry;
					}
				}
				// 不可能出现的情况
				return rates.entrySet().iterator().next();
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
			mDayluckyRewardDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_每日幸运奖励 + "]错误：" + e.getMessage(), e);
		}

		try {
			mDayluckyRateDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_每日幸运概率 + "]错误：" + e.getMessage(), e);
		}
	}
}
