package com.kola.kmp.logic.level.petcopy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.level.KLevelReward.NormalItemRewardTemplate;
import com.kola.kmp.logic.level.petcopy.KPetCopyReward.KPetCopyItemRewardTemplate;
import com.kola.kmp.logic.other.KPetCopyDropTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 随从副本掉落方案
 * 
 * @author Administrator
 * 
 */
public class KPetCopyBattlefieldDropTemplate {

	public int dropId;
	public int levelId;
	public int resId;
	public int appearWeight;// 出现权重
	public KPetCopyDropTypeEnum dropType;// 掉落类型
	public float expAdditionRate;// 经验加成比例
	public boolean isDefault;

	public Map<Integer, Integer> monsterMap = new HashMap<Integer, Integer>();// 打开笼子出现的怪物列表
	public List<NormalItemRewardTemplate> dropItemList = new ArrayList<NormalItemRewardTemplate>();// 打开笼子出现的道具列表
	public int totalItemDropWeight = 0;// 打开笼子出现的道具列表的总权重
	public int dropGold;// 打开笼子出现的金币

	public void init(String tableName, KGameExcelRow xlsRow)
			throws KGameServerException {
		this.dropId = xlsRow.getInt("dropId");
		this.levelId = xlsRow.getInt("CopyID");
		this.resId = xlsRow.getInt("resId");
		this.appearWeight = xlsRow.getInt("appearWeight");
		this.isDefault = xlsRow.getBoolean("bAppear");
		this.expAdditionRate = xlsRow.getFloat("expAdditionRate");
		this.dropType = KPetCopyDropTypeEnum.getTypeEnum(xlsRow
				.getInt("dropType"));
		if (this.dropType == null) {
			throw new KGameServerException("初始化表<" + tableName
					+ ">字段dropType的掉落类型错误：" + xlsRow.getInt("dropType")
					+ "，找不到掉落类型，excel行数：" + xlsRow.getIndexInFile());
		}
		if (this.dropType == KPetCopyDropTypeEnum.MONSTER) {
			String monsterKey = "monster";
			for (int i = 1; i <= 3; i++) {
				String monsterData = xlsRow.getData(monsterKey + i);
				initMonsterData(tableName, monsterKey + i, monsterData,
						xlsRow.getIndexInFile());
			}
		} else if (this.dropType == KPetCopyDropTypeEnum.ITEM) {
			String itemKey = "dropitem";
			for (int i = 1; i <= 6; i++) {
				String itemData = xlsRow.getData(itemKey + i);
				decodeDropItem(tableName, itemKey + i, itemData,
						xlsRow.getIndexInFile());
			}
		} else if (this.dropType == KPetCopyDropTypeEnum.CURRENCY) {
			this.dropGold = xlsRow.getInt("gold");
		}
	}

	private void initMonsterData(String tableName, String fieldName,
			String monsterData, int index) throws KGameServerException {
		if (monsterData != null && monsterData.length() > 0
				&& !monsterData.equals("0")) {
			String[] monsterIdStr = monsterData.split("\\*");
				if (monsterIdStr == null || monsterIdStr.length != 2) {
					throw new KGameServerException("初始化表<" + tableName
							+ ">的怪物数据错误,字段：" + fieldName + "=" + monsterData
							+ "，excel行数：" + index);
				}
				int monsterTemplateId = Integer.parseInt(monsterIdStr[0]);
				int count = Integer.parseInt(monsterIdStr[1]);

				if (KSupportFactory.getNpcModuleSupport().getMonstTemplate(
						monsterTemplateId) == null) {
					throw new KGameServerException("初始化表<" + tableName
							+ ">的怪物错误，找不到怪物模版类型：" + monsterTemplateId + "，字段："
							+ fieldName + "=" + monsterData + "，excel行数："
							+ index);
				}
				monsterMap.put(monsterTemplateId, count);
			
		}
	}

	private void decodeDropItem(String tableName, String fieldName,
			String data, int index) throws KGameServerException {
		if (data != null && data.length() > 0 && !data.equals("0")) {
			String[] codes = data.split("\\*");
			if (codes == null || codes.length != 3) {
				throw new KGameServerException("初始化表<" + tableName
						+ ">的道具错误,字段：" + fieldName + "=" + data + "，excel行数："
						+ index);
			}
			if (KSupportFactory.getItemModuleSupport()
					.getItemTemplate(codes[0]) == null) {
				throw new KGameServerException("初始化表<" + tableName
						+ ">的道具错误，找不到道具类型：" + codes[0] + "，字段：" + fieldName
						+ "=" + data + "，excel行数：" + index);
			}
			int count = Integer.parseInt(codes[1]);
			int rate = Integer.parseInt(codes[2]);

			NormalItemRewardTemplate temp = new NormalItemRewardTemplate(
					codes[0], count, rate);
			dropItemList.add(temp);
			totalItemDropWeight += rate;
		}
	}

	public Map<String, Integer> caculateItemReward(int multiple) {
		Map<String, Integer> itemMap = new HashMap<String, Integer>();
		KItemTempAbs itemTemplate;
		if (totalItemDropWeight > 0) {
			int weight = UtilTool.random(0, totalItemDropWeight);
			int tempRate = 0;
			for (NormalItemRewardTemplate template : this.dropItemList) {
				if (tempRate < weight
						&& weight <= (tempRate + template.getDropWeight())) {
					if (!template.isNoneDrop) {
						itemTemplate = KSupportFactory.getItemModuleSupport()
								.getItemTemplate(template.getItemCode());
						itemMap.put(template.getItemCode(),
								template.getRewardCount() * multiple);
					}
					break;
				} else {
					tempRate += template.getDropWeight();
				}
			}
		}

		return itemMap;
	}
}
