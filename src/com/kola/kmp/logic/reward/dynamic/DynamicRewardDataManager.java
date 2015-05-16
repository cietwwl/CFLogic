package com.kola.kmp.logic.reward.dynamic;

import java.util.HashMap;
import java.util.Map;

import com.koala.game.exception.KGameServerException;
import com.kola.kmp.logic.reward.dynamic.DynamicRewardDataStruct.GangRewardData;
import com.kola.kmp.logic.reward.dynamic.DynamicRewardDataStruct.RewardElement;

public class DynamicRewardDataManager {

	DynamicRewardDataManager() {
	}

	// 奖励名单管理器
	RewardRoleDataManager mRewardRoleDataManager = new RewardRoleDataManager();
	// 奖励数据管理器
	RewardDataManager mRewardDataManager = new RewardDataManager();

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 奖励名单管理器
	 * 
	 * @author CamusHuang
	 * @creation 2013-5-17 上午11:53:20
	 * </pre>
	 */
	static class RewardRoleDataManager {
		/**
		 * <pre>
		 * KEY = 角色ID
		 * unmodifiable
		 * </pre>
		 */
		private Map<Long, RewardElement> dataMap = new HashMap<Long, RewardElement>();

		private RewardRoleDataManager() {
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
		String addData(RewardElement data) {

			if (dataMap.containsKey(data.id)) {
				return "重复的名单ID=" + data.id;
			}

			dataMap.put(data.id, data);
			return null;
		}

		Map<Long, RewardElement> getAllDatas() {
			return dataMap;
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 奖励管理器
	 * 
	 * @author CamusHuang
	 * @creation 2013-5-17 上午11:53:20
	 * </pre>
	 */
	static class RewardDataManager {
		/**
		 * <pre>
		 * KEY = 奖励IDID
		 * </pre>
		 */
		private Map<Integer, GangRewardData> dataMap = new HashMap<Integer, GangRewardData>();

		private RewardDataManager() {
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
		String addData(GangRewardData data) {

			if (dataMap.containsKey(data.baseMailRewardData.id)) {
				return "重复的奖励ID=" + data.baseMailRewardData.id;
			}

			dataMap.put(data.baseMailRewardData.id, data);
			return null;
		}

		GangRewardData getData(int id) {
			return dataMap.get(id);
		}

		void serverStartCompleted() throws Exception {
			for (GangRewardData data : dataMap.values()) {
				data.baseMailRewardData.notifyCacheLoadComplete();
			}
		}
	}

}
