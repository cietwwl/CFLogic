package com.kola.kmp.logic.level.tower;

import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;

public class KTowerReward {
	// 怪物波数编号
	private int towerId;
	// 
	private BaseRewardData reward;
	public int getTowerId() {
		return towerId;
	}
	public void setTowerId(int towerId) {
		this.towerId = towerId;
	}
	public BaseRewardData getReward() {
		return reward;
	}
	public void setReward(BaseRewardData reward) {
		this.reward = reward;
	}
	
	
	
}
