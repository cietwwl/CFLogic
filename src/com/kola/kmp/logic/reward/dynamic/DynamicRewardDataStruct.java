package com.kola.kmp.logic.reward.dynamic;

import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailRewardData;

/**
 * <pre>
 * 本类负责定义本模块的数据结构
 * 本类纯粹定义数据结构,而不管理数据
 * 
 * @author CamusHuang
 * @creation 2013-5-17 上午11:56:24
 * </pre>
 */
public class DynamicRewardDataStruct {

	private DynamicRewardDataStruct() {
	}

	/**
	 * <pre>
	 * 奖励名单成员
	 * 
	 * @author CamusHuang
	 * @creation 2013-5-24 上午11:09:46
	 * </pre>
	 */
	public static class RewardElement {

		public final long id;// 要奖励的角色或军团ID
		public final String name;// 要奖励的角色或军团名称
		public final int rewardId;

		RewardElement(long id, String name, int rewardId) {
			this.id = id;
			this.name = name;
			this.rewardId = rewardId;
		}
	}

	/**
	 * <pre>
	 * 包含军团奖励的奖励项
	 * 
	 * @author CamusHuang
	 * @creation 2013-7-13 下午11:58:17
	 * </pre>
	 */
	public static class GangRewardData {
		public final BaseMailRewardData baseMailRewardData;
		public final int gangResource; // 军团资金

		public GangRewardData(BaseMailRewardData baseMailRewardData, int gangResource) {
			this.baseMailRewardData = baseMailRewardData;
			this.gangResource = gangResource;
		}
	}

}
