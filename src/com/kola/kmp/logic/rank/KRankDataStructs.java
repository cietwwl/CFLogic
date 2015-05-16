package com.kola.kmp.logic.rank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.management.timer.Timer;

import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;

public class KRankDataStructs {

	/**
	 * <pre>
	 * 排行榜点赞数据
	 * 
	 * @author CamusHuang
	 * @creation 2014-2-21 下午3:09:18
	 * </pre>
	 */
	public static class KRankGoodPrice {
		// ----------以下是EXCEL表格直导数据---------
		public int templateId;// 模板ID
		private int use_diamond;// 消耗钻石
		public int time;// 每天点赞次数
		private String[] opentiem;// 物品数量及权重
		private int gold;// 金币奖励
		private int honor;// 荣誉奖励
		//
		private String[] praisedOpentiem;// 被点赞人物品数量及权重
		private int praisedGold;// 被点赞人金币奖励
		private int praisedHonor;// 被点赞人荣誉奖励
		// ----------以下是逻辑数据---------
		public KCurrencyCountStruct payMoney;
		//
		public RankGoodReward payReward = new RankGoodReward();// 点赞者获得的奖励
		public RankGoodReward topReward = new RankGoodReward();// 冠军获得的奖励

		/**
		 * <pre>
		 * onGameWorldInitComplete
		 * 
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2014-2-18 下午6:01:47
		 * </pre>
		 */
		void notifyCacheLoadComplete() throws Exception {
			if (time <= 0) {
				throw new Exception("数据错误 time = " + time);
			}

			if (use_diamond > 0) {
				payMoney = new KCurrencyCountStruct(KCurrencyTypeEnum.DIAMOND, use_diamond);
			}

			try {
				payReward.init(opentiem, gold, honor);
			} catch (Exception e) {
				throw new Exception("点赞者奖励：" + e.getMessage() + " templateId=" + templateId);
			}

			try {
				topReward.init(praisedOpentiem, praisedGold, praisedHonor);
			} catch (Exception e) {
				throw new Exception("冠军奖励：" + e.getMessage() + " templateId=" + templateId);
			}

			// CTODO 其它约束检查
		}

		public static class RankGoodReward {
			public List<ItemCountStruct> addItems = Collections.emptyList();
			public List<Integer> addItemRates = Collections.emptyList();
			public int addItemRate = 0;
			//
			public List<KCurrencyCountStruct> addMoneys = Collections.emptyList();

			private void init(String[] opentiem, int gold, int honor) throws Exception {
				addMoneys = new ArrayList<KCurrencyCountStruct>();
				if (gold > 0) {
					addMoneys.add(new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD, gold));
				}
				if (honor > 0) {
					addMoneys.add(new KCurrencyCountStruct(KCurrencyTypeEnum.SCORE, honor));
				}
				if (addMoneys.isEmpty()) {
					addMoneys = Collections.emptyList();
				}

				if (opentiem != null && opentiem.length > 0) {
					addItems = new ArrayList<ItemCountStruct>();
					addItemRates = new ArrayList<Integer>();
					for (String temp : opentiem) {
						String[] temps = temp.split("\\*");
						ItemCountStruct tempS = new ItemCountStruct(temps[0], Long.parseLong(temps[1]));
						if (tempS.itemCount <= 0) {
							throw new Exception("道具数量错误=" + temps[1]);
						}
						if (tempS.getItemTemplate() == null) {
							throw new Exception("道具模板不存在=" + temps[0]);
						}
						addItems.add(tempS);
						int rate = Integer.parseInt(temps[2]);
						if (rate < 1) {
							throw new Exception("道具权重错误=" + rate);
						}
						addItemRates.add(rate);
						addItemRate += rate;
					}
					if (addItemRates.isEmpty()) {
						addItemRates = Collections.emptyList();
					}
					if (addItems.isEmpty()) {
						addItems = Collections.emptyList();
					}
				}
			}

			public ItemCountStruct randomAddItem() {
				return ItemCountStruct.randomItem(addItems, addItemRates, addItemRate);
			}
		}
	}

	/**
	 * <pre>
	 * 军团战力榜点赞奖励
	 * 
	 * @author CamusHuang
	 * @creation 2014-2-21 下午3:09:18
	 * </pre>
	 */
	public static class KGangRankGoodReward {
		// ----------以下是EXCEL表格直导数据---------
		public int start;// 军团排名
		public int Funds;// 奖励军团资金

		// ----------以下是逻辑数据---------

		void notifyCacheLoadComplete() throws Exception {
			if (start <= 0) {
				throw new Exception("数据错误 start = " + start);
			}

			if (Funds <= 0) {
				throw new Exception("数据错误 Funds = " + Funds);
			}

			// CTODO 其它约束检查
		}
	}
}
