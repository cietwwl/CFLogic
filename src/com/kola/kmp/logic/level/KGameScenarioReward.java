package com.kola.kmp.logic.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kmp.logic.level.KLevelReward.NormalItemRewardTemplate;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.support.KSupportFactory;

public class KGameScenarioReward {
	// 所有的数值奖励列表
	private HashMap<Byte, KCurrencyCountStruct> attributeRewardMap = new HashMap<Byte, KCurrencyCountStruct>();

	// 所有的常规道具掉落奖励模版列表
	private List<NormalItemRewardTemplate> allItemReward = new ArrayList<NormalItemRewardTemplate>();

	public BaseRewardData probableReward;

	public HashMap<Byte, KCurrencyCountStruct> getAttributeRewardMap() {
		return attributeRewardMap;
	}

	public List<NormalItemRewardTemplate> getAllItemReward() {
		return allItemReward;
	}

	public BaseRewardData getProbableReward() {
		return probableReward;
	}

	public void initScenarioReward(KGameExcelRow xlsRow,String paraName)
			throws KGameServerException {
		List<AttValueStruct> attList = new ArrayList<AttValueStruct>();

		List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();

		String itemRewardData = xlsRow.getData(paraName);
		if (itemRewardData != null) {
			List<ItemCountStruct> itemList = initNormalItemReward(itemRewardData);
			probableReward = new BaseRewardData(attList, moneyList, itemList,
					Collections.<Integer> emptyList(),
					Collections.<Integer> emptyList());
		}
	}

	private List<ItemCountStruct> initNormalItemReward(String dropData)
			throws KGameServerException {
		List<ItemCountStruct> list = new ArrayList<ItemCountStruct>();
		if (dropData != null) {
			String[] itemInfoStr = dropData.split(",");
			if (itemInfoStr != null && itemInfoStr.length > 0) {
				for (int i = 0; i < itemInfoStr.length; i++) {
					String[] itemData = itemInfoStr[i].split("\\*");
					if (itemData != null && itemData.length == 2) {
						NormalItemRewardTemplate itemTemplate = null;
						String itemCode = itemData[0];
						int count = Integer.parseInt(itemData[1]);
						itemTemplate = new NormalItemRewardTemplate(itemCode,
								count);
						this.allItemReward.add(itemTemplate);
						try {
							list.add(new ItemCountStruct(KSupportFactory
									.getItemModuleSupport().getItemTemplate(
											itemCode), count));
						} catch (Exception e) {
							throw new KGameServerException(e.getMessage()
									+ itemCode, e);
						}
					}
				}
			}
		}
		return list;
	}

	public List<ItemCountStruct> getAllItems() {
		List<ItemCountStruct> list = new ArrayList<ItemCountStruct>();
		for (NormalItemRewardTemplate template : allItemReward) {
			if (!template.isNoneDrop && template.getItemCode() != null) {
				list.add(new ItemCountStruct(template.getItemCode(), template
						.getRewardCount()));
			}
		}
		return list;
	}
}
