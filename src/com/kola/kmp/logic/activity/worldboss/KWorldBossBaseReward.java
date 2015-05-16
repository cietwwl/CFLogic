package com.kola.kmp.logic.activity.worldboss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;

/**
 * 
 * @author PERRY CHAN
 */
public class KWorldBossBaseReward {
	
	public final int expProportion;
	public final int goldProportion;
	public final int potentialProportion;
	public final List<ItemCountStruct> itemRewards;
	public final Map<String, Integer> itemRewardMap;

	public KWorldBossBaseReward(KGameExcelRow row) {
		if(row.containsCol("expProportion")) {
			this.expProportion = row.getInt("expProportion");
		} else {
			this.expProportion = 0;
		}
		if (row.containsCol("goldProportion")){
			this.goldProportion = row.getInt("goldProportion");
		} else {
			this.goldProportion = 0;
		}
		if (row.containsCol("potentialProportion")) {
			this.potentialProportion = row.getInt("potentialProportion");
		} else {
			this.potentialProportion = 0;
		}
		String[] items = row.getData("script").split(";");
		if (items.length > 0 && items[0].length() > 0) {
			Map<String, Integer> tempMap = new LinkedHashMap<String, Integer>();
			List<ItemCountStruct> tempList = new ArrayList<ItemCountStruct>();
			this.itemRewards = Collections.unmodifiableList(tempList);
			this.itemRewardMap = Collections.unmodifiableMap(tempMap);
			String[] singleItem;
			String itemCode;
			int itemCount;
			for (int i = 0; i < items.length; i++) {
				singleItem = items[i].split(",");
				itemCode = singleItem[0];
				itemCount = Integer.parseInt(singleItem[1]);
				tempMap.put(itemCode, itemCount);
				tempList.add(new ItemCountStruct(itemCode, itemCount));
			}
		} else {
			itemRewards = Collections.emptyList();
			itemRewardMap = Collections.emptyMap();
		}
	}
}
