package com.kola.kmp.logic.level;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.level.KLevelReward.NormalItemRewardTemplate;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.support.KSupportFactory;

public class KGameLevelDropPool {
	
	public static Map<Integer,KDropGroup> dropGroupMap = new LinkedHashMap<Integer, KDropGroup>();

	public static class KDropGroup {
		public int groupId;
		public int totalDropWeight = 0;
		public List<KDropGroupItemTemplate> itemList = new ArrayList<KDropGroupItemTemplate>();
		public KDropGroupItemTemplate defaultTemplate;

		public void init(KGameExcelRow row) throws KGameServerException {
			this.groupId = row.getInt("drop_ID");
			for (int i = 1; i <= 6; i++) {
				String itemCode = row.getData("item_" + i);
				if (itemCode != null && !itemCode.equals("")
						&& !itemCode.equals("0")) {
					if (KSupportFactory.getItemModuleSupport().getItemTemplate(
							itemCode) == null) {
						throw new KGameServerException("初始化表<掉落方案配置>的item_" + i
								+ " 道具错误，找不到道具类型：" + itemCode + "，excel行数："
								+ row.getIndexInFile());
					}
					int minCount = row.getInt("smallnumber" + i);
					int maxCount = row.getInt("largenumber" + i);
					if (minCount <= 0 || maxCount <= 0 || minCount > maxCount) {
						throw new KGameServerException("初始化表<掉落方案配置>的item_" + i
								+ " 道具的数量错误，最小值=" + minCount + "最大值="
								+ maxCount + "，excel行数：" + row.getIndexInFile());
					}
					int weight = row.getInt("weight" + i);

					KDropGroupItemTemplate template = new KDropGroupItemTemplate(
							itemCode, minCount, maxCount, weight);
					itemList.add(template);
					totalDropWeight += weight;
					if (i == 1) {
						this.defaultTemplate = template;
					}
				}
			}
			if (itemList.size() <= 0) {
				throw new KGameServerException(
						"初始化表<掉落方案配置>错误，该掉落组不能没有任何道具：，excel行数："
								+ row.getIndexInFile());
			}
		}

		public KDropGroupItemTemplate caculateDropItem() {
			int weight = UtilTool.random(0, totalDropWeight);
			int tempRate = 0;
			for (KDropGroupItemTemplate template : itemList) {
				if (tempRate < weight && weight <= (tempRate + template.weight)) {
					return template;
				} else {
					tempRate += template.weight;
				}
			}
			return defaultTemplate;
		}
	}

	public static class KDropGroupItemTemplate {
		public String itemCode;
		public int minDropCount;
		public int maxDropCount;
		public int weight;

		public KDropGroupItemTemplate(String itemCode, int minDropCount,
				int maxDropCount, int weight) {
			super();
			this.itemCode = itemCode;
			this.minDropCount = minDropCount;
			this.maxDropCount = maxDropCount;
			this.weight = weight;
		}

		public ItemCountStruct getItemCountStruct(int multiple) {
			int count;
			if (minDropCount == maxDropCount) {
				count = minDropCount * multiple;
			} else {
				count = UtilTool.random(minDropCount, maxDropCount) * multiple;
			}
			return new ItemCountStruct(itemCode, count);
		}
	}
}
