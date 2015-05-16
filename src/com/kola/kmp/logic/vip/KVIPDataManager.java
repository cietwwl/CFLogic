package com.kola.kmp.logic.vip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.koala.game.exception.KGameServerException;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;

/**
 * <pre>
 * 本类负责管理本模块的公共数据 
 * 
 * @author CamusHuang
 * @creation 2013-4-8 下午2:39:56
 * </pre>
 */
public class KVIPDataManager {
	private KVIPDataManager() {
	}

	/**
	 * <pre>
	 * VIP等级数据管理器
	 * </pre>
	 */
	public static KVIPLevelDataManager mVIPLevelDataManager = new KVIPLevelDataManager();

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * VIP等级数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-8 下午2:40:38
	 * </pre>
	 */
	public static class KVIPLevelDataManager {
		/**
		 * <pre>
		 * 各等级数据
		 * KEY=等级
		 * </pre>
		 */
		private Map<Integer, VIPLevelData> dataMap = new HashMap<Integer, KVIPDataStructs.VIPLevelData>();
		private List<VIPLevelData> dataList = new ArrayList<KVIPDataStructs.VIPLevelData>();
		/** 0等级 */
		private VIPLevelData zeroLevel;
		/** 最小等级 */
		private VIPLevelData minLevel;
		/** 最大等级 */
		private VIPLevelData maxLevel;

		// 封测专用
		private Map<Integer, VIPLevelData> dataMapByRoleLv = new HashMap<Integer, KVIPDataStructs.VIPLevelData>();

		void init(List<VIPLevelData> datas) throws Exception {
			dataList.addAll(datas);
			for (VIPLevelData data : datas) {
				dataMap.put(data.lvl, data);
			}
			zeroLevel = dataMap.get(0);
			minLevel = dataList.get(1);
			maxLevel = dataList.get(dataList.size() - 1);

			//
			int totalCharge = 0;
			for (int lv = zeroLevel.lvl; lv <= maxLevel.lvl; lv++) {
				VIPLevelData data = dataMap.get(lv);
				if (data == null) {
					throw new KGameServerException("缺漏等级 = " + lv);
				}
				totalCharge += data.needrmb;
				data.totalCharge = totalCharge;
			}
		}

		/**
		 * <pre>
		 * 0级的VIP等级数据
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2013-4-23 下午7:53:53
		 * </pre>
		 */
		public VIPLevelData getZeroLevel() {
			return zeroLevel;
		}

		/**
		 * <pre>
		 * 最小的VIP等级数据，即VIP1
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2013-4-23 下午7:53:53
		 * </pre>
		 */
		public VIPLevelData getMinLevel() {
			return minLevel;
		}

		/**
		 * <pre>
		 * 最大的VIP等级数据
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2013-4-23 下午7:54:01
		 * </pre>
		 */
		public VIPLevelData getMaxLevel() {
			return maxLevel;
		}

		/**
		 * <pre>
		 * 指定等级的VIP数据
		 * 
		 * @param level
		 * @return
		 * @author CamusHuang
		 * @creation 2013-4-23 下午7:54:09
		 * </pre>
		 */
		public VIPLevelData getLevelData(int level) {
			return dataMap.get(level);
		}

		/**
		 * <pre>
		 * 搜索指定角色等级区间里，赠送的最大VIP等级
		 * 
		 * @param preRoleLv
		 * @param nowRoleLv
		 * @return
		 * @author CamusHuang
		 * @creation 2014-8-14 下午3:28:27
		 * </pre>
		 */
		public VIPLevelData searchDataByRoleLv(int preRoleLv, int nowRoleLv) {
			VIPLevelData result = null;

			VIPLevelData temp = null;
			for (int lv = preRoleLv; lv <= nowRoleLv; lv++) {
				temp = dataMapByRoleLv.get(lv);
				if (temp == null) {
					continue;
				}
				if (result == null) {
					result = temp;
				} else if (temp.lvl >= result.lvl) {
					result = temp;
				}
			}
			return result;
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
			for (VIPLevelData data : dataList) {
				try {
					data.notifyCacheLoadComplete();
				} catch (Exception e) {
					throw new KGameServerException(e.getMessage() + ", viplv = " + data.lvl, e);
				}
				dataMapByRoleLv.put(data.presentRoleLv, data);
			}

			// 保证角色等级升序
			int tempRoleLv = getLevelData(zeroLevel.lvl).presentRoleLv;
			for (int viplv = zeroLevel.lvl + 1; viplv <= maxLevel.lvl; viplv++) {
				if (getLevelData(viplv).presentRoleLv == 0) {
					continue;
				}
				if (tempRoleLv >= getLevelData(viplv).presentRoleLv) {
					throw new KGameServerException("数值错误 presentRoleLv = " + getLevelData(viplv).presentRoleLv + ", viplv = " + viplv);
				}
				tempRoleLv = getLevelData(viplv).presentRoleLv;
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
	static void onGameWorldInitComplete() throws KGameServerException {
		try {
			mVIPLevelDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + KVIPDataLoader.SheetName_vip功能 + "]错误：" + e.getMessage(), e);
		}
	}
}
