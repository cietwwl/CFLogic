package com.kola.kmp.logic.mission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;

public class MissionReward {
	// 所有属性增量，不为NULL
	public List<AttValueStruct> attList = new ArrayList<AttValueStruct>();
	// 所有货币，不为NULL
	public List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();
	// 任务奖励道具模版列表
	private List<MissionItemRewardTemplate> itemRewardList = new ArrayList<MissionItemRewardTemplate>();
	//宠物奖励表：key：宠物模版Id，value：奖励数量
	private Map<Integer,Integer> petRewardMap = new HashMap<Integer, Integer>();

	/**
	 * 获取所有任务奖励道具模版
	 * 
	 * @return
	 */
	public List<MissionItemRewardTemplate> getAllMissionItemRewardTemplate() {
		return itemRewardList;
	}

	/**
	 * 添加一个任务奖励道具模版
	 * 
	 * @param reward
	 */
	public void addMissionItemRewardTemplate(MissionItemRewardTemplate reward) {
		this.itemRewardList.add(reward);
	}
	
	/**
	 * 宠物奖励
	 * @return
	 */
	public Map<Integer, Integer> getPetRewardMap() {
		return petRewardMap;
	}

	public BaseRewardData getBaseRewardData(byte occupationType){
		List<ItemCountStruct> itemStructs = new ArrayList<ItemCountStruct>();
		for (MissionItemRewardTemplate tempalte:itemRewardList) {
			if(tempalte.isLimitOccupation){
				itemStructs.add(new ItemCountStruct(tempalte.getRewardItemTemplate(occupationType), tempalte.rewardCount));
			}else{
				itemStructs.add(new ItemCountStruct(tempalte.itemTemplate, tempalte.rewardCount));
			}
		}
		List<Integer> petIdList = new ArrayList<Integer>();
		for (Integer petId:petRewardMap.keySet()) {
			int count = petRewardMap.get(petId);
			for (int i = 0; i < count; i++) {
				petIdList.add(petId);
			}
		}
		
		return new BaseRewardData(attList, moneyList, itemStructs, Collections.<Integer>emptyList(), petIdList);
	}

	/**
	 * 表示任务奖励道具的数据模版
	 * 
	 * @author zhaizl
	 * 
	 */
	public static class MissionItemRewardTemplate {
		// 限制职业的掉落道具的模版（当isLimitOccupation为true时有效）
		public Map<Byte, KItemTempAbs> itemTemplateMapByJobType = new HashMap<Byte, KItemTempAbs>();
		// 不限制职业的道具模版
		public KItemTempAbs itemTemplate;
		// 掉落数量
		public int rewardCount;

		// 是否限制职业
		public boolean isLimitOccupation;

		public KItemTempAbs getRewardItemTemplate(byte occputionType) {
			if (isLimitOccupation) {
				return itemTemplateMapByJobType.get(occputionType);
			}
			return itemTemplate;
		}

	}
}
