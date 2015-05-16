package com.kola.kmp.logic.relationship;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.koala.game.exception.KGameServerException;
import com.kola.kmp.logic.competition.KCompetitionBattlefield;
import com.kola.kmp.logic.gang.war.KGangWarConfig;
import com.kola.kmp.logic.relationship.KRelationShipDataStructs.RSPushData;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;

/**
 * <pre>
 * 本类负责管理本模块的公共数据 
 * 
 * @author CamusHuang
 * @creation 2014-8-29 上午11:34:12
 * </pre>
 */
public class KRelationShipDataManager {
	private KRelationShipDataManager() {
	}
	
	// 用于切磋PVP的战斗地图
	static final KCompetitionBattlefield PVPBattlefield = new KCompetitionBattlefield();

	/**
	 * <pre>
	 * 好友推荐数据管理器
	 * </pre>
	 */
	public static KRSPushDataManager mRSPushDataManager = new KRSPushDataManager();

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 好友推荐数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-8 下午2:40:38
	 * </pre>
	 */
	public static class KRSPushDataManager {
		/**
		 * <pre>
		 * 各等级数据
		 * KEY=等级
		 * </pre>
		 */
		private Map<Integer, RSPushData> dataMap = new HashMap<Integer, RSPushData>();

		void init(List<RSPushData> datas) throws Exception {
			for (RSPushData data : datas) {
				dataMap.put(data.lvl, data);
			}
		}

		public RSPushData getData(int roleLv) {
			return dataMap.get(roleLv);
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
			for (RSPushData data : dataMap.values()) {
				try {
					data.notifyCacheLoadComplete();
				} catch (Exception e) {
					throw new KGameServerException(e.getMessage() + ", lvl = " + data.lvl, e);
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
			mRSPushDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + KRelationShipDataLoader.SheetName_好友推送 + "]错误：" + e.getMessage(), e);
		}
		
		//
		PVPBattlefield.initBattlefield(KRelationShipConfig.getInstance().切磋PVP地图文件名, KRelationShipConfig.getInstance().切磋PVP地图背景音乐);
		try {
			if (KRelationShipConfig.getInstance().切磋PVP地图文件名 == null) {
				throw new KGameServerException("切磋PVP 战斗场景不存在");
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
