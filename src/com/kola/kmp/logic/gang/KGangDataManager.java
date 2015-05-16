package com.kola.kmp.logic.gang;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.koala.game.exception.KGameServerException;
import com.kola.kmp.logic.gang.KGangDataStruct.GangContributionData;
import com.kola.kmp.logic.gang.KGangDataStruct.GangGoodsData;
import com.kola.kmp.logic.gang.KGangDataStruct.GangLevelData;
import com.kola.kmp.logic.gang.KGangDataStruct.GangProsperityData;
import com.kola.kmp.logic.gang.KGangDataStruct.GangTechTemplate;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KGangTecTypeEnum;

/**
 * <pre>
 * 本类负责管理本模块的公共数据 
 * 
 * @author camus
 * @creation 2012-12-30 下午2:52:07
 * </pre>
 */
public class KGangDataManager {
	private KGangDataManager() {
	}

	/**
	 * <pre>
	 * 军团等级数据管理器
	 * </pre>
	 */
	public static GangLevelDataManager mGangLevelDataManager = new GangLevelDataManager();

	/**
	 * <pre>
	 * 军团科技数据管理器
	 * </pre>
	 */
	static GangTechDataManager mGangTechDataManager = new GangTechDataManager();

	/**
	 * <pre>
	 * 军团商品数据管理器
	 * </pre>
	 */
	static GangGoodsDataManager mGangGoodsDataManager = new GangGoodsDataManager();

	/**
	 * <pre>
	 * 军团捐献数据管理器
	 * </pre>
	 */
	static GangContributionDataManager mGangContributionDataManager = new GangContributionDataManager();

	/**
	 * <pre>
	 * 军团繁荣度基数
	 * </pre>
	 */
	static GangProsperityData mGangProsperityData;

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 军团等级数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2013-1-24 上午11:24:53
	 * </pre>
	 */
	public static class GangLevelDataManager {
		/**
		 * <pre>
		 * 各等级数据
		 * KEY=等级
		 * </pre>
		 */
		private Map<Integer, GangLevelData> dataMap = new HashMap<Integer, GangLevelData>();

		void initData(List<GangLevelData> datas) throws Exception {
			dataMap.clear();

			for (GangLevelData tempData : datas) {
				GangLevelData oldData = dataMap.put(tempData.LegionLv, tempData);
				if (oldData != null) {
					throw new Exception("军团重复等级=" + tempData.LegionLv);
				}
			}

			// LV是否正确
			int minLv = 1;
			int maxLv = dataMap.size() - 1;
			for (int lv = minLv; lv <= maxLv; lv++) {
				if (!dataMap.containsKey(lv)) {
					throw new Exception(" 军团缺少等级=" + lv);
				}
			}

		}

		GangLevelData getDefaultLevel() {
			return dataMap.get(1);
		}

		public GangLevelData getMaxLevel() {
			return dataMap.get(dataMap.size());
		}

		public GangLevelData getLevelData(int level) {
			return dataMap.get(level);
		}

		/**
		 * <pre>
		 * 验证数据/跨模块操作
		 * 
		 * @throws KGameServerException
		 * @author CamusHuang
		 * @creation 2013-1-11 下午5:19:13
		 * </pre>
		 */
		private void notifyCacheLoadComplete() throws KGameServerException {

			for (GangLevelData data : dataMap.values()) {
				data.notifyCacheLoadComplete();
			}
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 全部科技数据管理
	 * 
	 * @author CamusHuang
	 * @creation 2013-1-11 下午5:06:00
	 * </pre>
	 */
	static class GangTechDataManager {
		/**
		 * <pre>
		 * 全部科技
		 * 
		 * <科技ID,科技等级数据>
		 * </pre>
		 */
		private final Map<Integer, GangTechTemplate> dataMap = new HashMap<Integer, GangTechTemplate>();
		private final Map<KGangTecTypeEnum, GangTechTemplate> dataMap2 = new HashMap<KGangTecTypeEnum, GangTechTemplate>();

		private GangTechDataManager() {
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
		void initData(List<GangTechTemplate> datas) throws Exception {
			dataMap.clear();

			for (GangTechTemplate tempData : datas) {
				GangTechTemplate oldData = dataMap.put(tempData.ID, tempData);
				if (oldData != null) {
					throw new Exception("重复的科技ID=" + tempData.ID);
				}
			}
		}

		GangTechTemplate getData(int techID) {
			return dataMap.get(techID);
		}

		GangTechTemplate getData(KGangTecTypeEnum type) {
			return dataMap2.get(type);
		}

		/**
		 * <pre>
		 * 
		 * @deprecated 慎用
		 * @return
		 * @author CamusHuang
		 * @creation 2014-3-26 下午12:00:44
		 * </pre>
		 */
		Map<Integer, GangTechTemplate> getCache() {
			return dataMap;
		}

		private void notifyCacheLoadComplete() throws KGameServerException {
			for (GangTechTemplate data : dataMap.values()) {
				data.notifyCacheLoadComplete();
			}

			dataMap2.clear();
			for (GangTechTemplate tempData : dataMap.values()) {
				GangTechTemplate oldData = dataMap2.put(tempData.type, tempData);
				if (oldData != null) {
					throw new KGameServerException("重复的科技类型=" + tempData.type);
				}
			}

			if (KGangTecTypeEnum.values().length != dataMap.size()) {
				throw new KGameServerException("缺少科技类型");
			}
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 军团商品数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2013-1-24 上午11:24:53
	 * </pre>
	 */
	static class GangGoodsDataManager {
		/**
		 * <pre>
		 * KEY = ID
		 * </pre>
		 */
		private Map<Integer, GangGoodsData> dataMap = new HashMap<Integer, KGangDataStruct.GangGoodsData>();

		void initData(List<GangGoodsData> datas) throws Exception {
			dataMap.clear();

			for (GangGoodsData tempData : datas) {
				GangGoodsData oldData = dataMap.put(tempData.ID, tempData);
				if (oldData != null) {
					throw new Exception("军团重复商品ID=" + tempData.ID);
				}
			}
		}

		GangGoodsData getGoodsData(int id) {
			return dataMap.get(id);
		}

		/**
		 * <pre>
		 * 
		 * @deprecated 慎用
		 * @return
		 * @author CamusHuang
		 * @creation 2014-3-26 下午12:00:44
		 * </pre>
		 */
		Map<Integer, GangGoodsData> getCache() {
			return dataMap;
		}

		/**
		 * <pre>
		 * 验证数据
		 * 
		 * @throws KGameServerException
		 * @author CamusHuang
		 * @creation 2013-1-11 下午5:19:13
		 * </pre>
		 */
		private void notifyCacheLoadComplete() throws KGameServerException {

			for (GangGoodsData data : dataMap.values()) {
				data.notifyCacheLoadComplete();
			}
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 军团商品数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2013-1-24 上午11:24:53
	 * </pre>
	 */
	static class GangContributionDataManager {
		/**
		 * <pre>
		 * <捐献类型,<捐献次数,GangContributionData>>
		 * </pre>
		 */
		private Map<Integer, Map<Integer, GangContributionData>> dataMap = new HashMap<Integer, Map<Integer, GangContributionData>>();

		void initData(List<GangContributionData> datas) throws Exception {
			dataMap.clear();

			for (GangContributionData tempData : datas) {
				Map<Integer, GangContributionData> temp = dataMap.get(tempData.DonateType);
				if (temp == null) {
					temp = new HashMap<Integer, GangContributionData>();
					dataMap.put(tempData.DonateType, temp);
				}
				GangContributionData oldData = temp.put(tempData.time, tempData);
				if (oldData != null) {
					throw new Exception("军团捐献数据重复 类型=" + tempData.DonateType + " 次数 = " + tempData.time);
				}
			}
		}

		boolean containType(KCurrencyTypeEnum type) {
			return dataMap.containsKey((int) type.sign);
		}

		/**
		 * <pre>
		 * 
		 * 
		 * @param type
		 * @param time
		 * @return null表示不支持此次数
		 * @author CamusHuang
		 * @creation 2014-4-11 下午5:25:35
		 * </pre>
		 */
		GangContributionData getData(int type, int time) {
			Map<Integer, GangContributionData> temp = dataMap.get(type);
			if (temp == null) {
				return null;
			}
			GangContributionData data = temp.get(time);
			if (data != null) {
				return data;
			}

			// 0次默认无限次数
			return temp.get(0);
		}

		/**
		 * <pre>
		 * 
		 * 
		 * @param type 类型
		 * @param nowTime 当前次数
		 * @return 剩余次数 -1表示无限
		 * @author CamusHuang
		 * @creation 2014-10-11 下午3:41:05
		 * </pre>
		 */
		int getReleaseTime(int type, int nowTime) {
			Map<Integer, GangContributionData> temp = dataMap.get(type);
			if (temp == null) {
				return 0;
			}
			if (temp.containsKey(0)) {
				return -1;
			}

			return Math.max(0, temp.size() - nowTime);
		}

		Map<Integer, Map<Integer, GangContributionData>> getCache() {
			return dataMap;
		}

		/**
		 * <pre>
		 * 验证数据
		 * 
		 * @throws KGameServerException
		 * @author CamusHuang
		 * @creation 2013-1-11 下午5:19:13
		 * </pre>
		 */
		private void notifyCacheLoadComplete() throws KGameServerException {
			for (Entry<Integer, Map<Integer, GangContributionData>> entry : dataMap.entrySet()) {
				Map<Integer, GangContributionData> tempData = entry.getValue();
				int maxLen = tempData.size();
				// if (tempData.containsKey(0)) {
				// if (maxLen != 1) {
				// throw new KGameServerException("军团捐献数据次数错误 类型=" +
				// entry.getKey() + " 次数 = " + 0);
				// }
				//
				// tempData.get(0).notifyCacheLoadComplete();
				// continue;
				// }
				{
					GangContributionData data = tempData.get(0);
					if (data != null) {
						data.notifyCacheLoadComplete();
						maxLen--;
					}
				}
				for (int i = 1; i <= maxLen; i++) {
					GangContributionData data = tempData.get(i);
					if (data == null) {
						throw new KGameServerException("军团捐献数据次数错误 类型=" + entry.getKey() + " 次数 = " + i);
					}
					data.notifyCacheLoadComplete();
				}
			}
		}
	}

	static void notifyCacheLoadComplete() throws KGameServerException {
		try {
			mGangLevelDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载" + KGangDataLoader.SheetName_军团等级 + "错误：" + e.getMessage(), e);
		}
		try {
			mGangTechDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载" + KGangDataLoader.SheetName_军团科技 + "错误：" + e.getMessage(), e);
		}
		try {
			mGangGoodsDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载" + KGangDataLoader.SheetName_军团商店 + "错误：" + e.getMessage(), e);
		}

		try {
			mGangContributionDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载" + KGangDataLoader.SheetName_军团捐献 + "错误：" + e.getMessage(), e);
		}

		try {
			mGangProsperityData.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载" + KGangDataLoader.SheetName_军团繁荣度 + "错误：" + e.getMessage(), e);
		}
	}
}
