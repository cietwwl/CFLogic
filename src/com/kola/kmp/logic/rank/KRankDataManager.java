package com.kola.kmp.logic.rank;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.koala.game.exception.KGameServerException;
import com.kola.kmp.logic.rank.KRankDataStructs.KGangRankGoodReward;
import com.kola.kmp.logic.rank.KRankDataStructs.KRankGoodPrice;

public final class KRankDataManager {
	// 排行榜点赞数据
	public static KRankGoodPriceManager mRoleRankGoodPriceManager = new KRankGoodPriceManager();
	
	// 军团战力榜点赞数据
	public static KRankGoodPriceManager mGangRankGoodPriceManager = new KRankGoodPriceManager();
	
	// 军团战力榜点赞奖励管理器
	public static KGangGoodRewardManager mGangGoodRewardManager = new KGangGoodRewardManager();

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 排行榜点赞价格数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-27 下午9:11:34
	 * </pre>
	 */
	public static class KRankGoodPriceManager {
		/**
		 * <pre>
		 * 全部数据
		 * KEY = 点赞次数
		 * </pre>
		 */
		private final HashMap<Integer, KRankGoodPrice> templateMap = new HashMap<Integer, KRankGoodPrice>();

		private KRankGoodPriceManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param temps
		 * @throws KGameServerException
		 * @author CamusHuang
		 * @creation 2012-11-27 下午9:13:25
		 * </pre>
		 */
		void initData(List<KRankGoodPrice> tempDatas) throws Exception {
			templateMap.clear();

			for (KRankGoodPrice tempData : tempDatas) {
				if (tempData.templateId < 1) {
					throw new Exception("错误的模板ID=" + tempData.templateId);
				}
				KRankGoodPrice oldData = templateMap.put(tempData.templateId, tempData);
				if (oldData != null) {
					throw new Exception("重复的模板ID=" + tempData.templateId);
				}
			}
		}

		public KRankGoodPrice getData(int time) {
			return templateMap.get(time);
		}

		boolean containData(int time) {
			return templateMap.containsKey(time);
		}

		private void notifyCacheLoadComplete() throws Exception {
			for (KRankGoodPrice temp : templateMap.values()) {
				temp.notifyCacheLoadComplete();
			}
		}
	}
	
	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 军团战力榜点赞奖励管理器
	 * 点赞时，按军团排名，奖励不同数值给军团
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-27 下午9:11:34
	 * </pre>
	 */
	public static class KGangGoodRewardManager {
		/**
		 * <pre>
		 * 全部数据
		 * KEY = 排名
		 * </pre>
		 */
		private final HashMap<Integer, KGangRankGoodReward> templateMap = new HashMap<Integer, KGangRankGoodReward>();

		private KGangGoodRewardManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param temps
		 * @throws KGameServerException
		 * @author CamusHuang
		 * @creation 2012-11-27 下午9:13:25
		 * </pre>
		 */
		void initData(List<KGangRankGoodReward> tempDatas) throws Exception {
			templateMap.clear();

			for (KGangRankGoodReward tempData : tempDatas) {
				KGangRankGoodReward oldData = templateMap.put(tempData.start, tempData);
				if (oldData != null) {
					throw new Exception("重复的start=" + tempData.start);
				}
			}
		}

		public KGangRankGoodReward getData(int rank) {
			return templateMap.get(rank);
		}

		boolean containData(int rank) {
			return templateMap.containsKey(rank);
		}
		
		public Map<Integer, KGangRankGoodReward> getDataCache(){
			return templateMap;
		}

		private void notifyCacheLoadComplete() throws Exception {
			for (KGangRankGoodReward temp : templateMap.values()) {
				temp.notifyCacheLoadComplete();
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
			mRoleRankGoodPriceManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + KRankDataLoader.SheetName_个人排行榜点赞表 + "]错误：" + e.getMessage(), e);
		}
		
		try {
			mGangRankGoodPriceManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + KRankDataLoader.SheetName_军团点赞表 + "]错误：" + e.getMessage(), e);
		}
		
		try {
			mGangGoodRewardManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + KRankDataLoader.SheetName_军团战力榜点赞排名奖励 + "]错误：" + e.getMessage(), e);
		}
	}
}
