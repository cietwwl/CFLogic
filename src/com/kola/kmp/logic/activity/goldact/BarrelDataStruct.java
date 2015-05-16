package com.kola.kmp.logic.activity.goldact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.level.KLevelReward.NormalItemRewardTemplate;
import com.kola.kmp.logic.npc.KNPCDataStructs.KMonstTemplate;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.support.KSupportFactory;

public class BarrelDataStruct {

	public int lvLimit;
	public List<BarrelData> barrelDataList = new ArrayList<BarrelData>();
	List<Integer> barrelIdShowSet = new ArrayList<Integer>();
	// 油桶出现总权重值
	public int _totalBarrelWeight;

	public BarrelData caculateBarrelData() {
		if (barrelDataList.size() == 0) {
			return null;
		}
		int weight = UtilTool.random(0, _totalBarrelWeight);
		int tempRate = 0;
		for (BarrelData data : barrelDataList) {
			if (tempRate < weight && weight <= (tempRate + data.weight)) {
				return data;
			} else {
				tempRate += data.weight;
			}
		}

		return barrelDataList.get(0);
	}

	public static class BarrelData {
		public int templateId;

		public KMonstTemplate template;
		// 油桶出现权重
		public int weight;
		// 掉落表Map，Key：掉落Id，Value：掉落值
		public Map<Integer, Integer> dropMap = new HashMap<Integer, Integer>();
		// 总掉落值
		public int _totalDropRate;
	}

}
