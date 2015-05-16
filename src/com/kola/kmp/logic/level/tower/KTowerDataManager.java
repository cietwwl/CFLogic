package com.kola.kmp.logic.level.tower;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.read.biff.BiffException;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kmp.logic.level.KLevelReward.NormalItemRewardTemplate;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 好友副本关卡战场波数数据管理器
 * @author zhaizl
 *
 */
public class KTowerDataManager {

	private static Map<Integer, KTowerData> _towerDataMap = new HashMap<Integer, KTowerData>();

	private static Map<Integer, KTowerReward> _towerRewardMap = new HashMap<Integer, KTowerReward>();

	public static void initTowerData(String xlsPath)
			throws KGameServerException {
		KGameExcelFile xlsFile = null;
		try {
			xlsFile = new KGameExcelFile(xlsPath);
		} catch (BiffException e) {
			throw new KGameServerException("读取关卡剧本excel表头发生错误！", e);
		} catch (IOException e) {
			throw new KGameServerException("读取关卡剧本excel表头发生错误！", e);
		}

		if (xlsFile != null) {
			String tableName = "塔防波数数据";
			int towerDataRowIndex = 5;
			KGameExcelTable towerDataTable = xlsFile.getTable(tableName,
					towerDataRowIndex);
			KGameExcelRow[] allTowerDataRows = towerDataTable.getAllDataRows();

			if (allTowerDataRows != null) {
				for (int i = 0; i < allTowerDataRows.length; i++) {
					KTowerData data = new KTowerData();
					data.init(tableName, allTowerDataRows[i]);
					_towerDataMap.put(data.getTowerId(), data);

					// 塔防奖励
					if (!data.isMission()) {
						int towerId = allTowerDataRows[i].getInt("towerId");
						List<AttValueStruct> attList = new ArrayList<AttValueStruct>();
						int exp = allTowerDataRows[i].getInt("exp");
						if (exp <= 0) {
							throw new KGameServerException("读取表<" + tableName
									+ ">的字段：exp错误,好友副本的塔防经验值不能小于0！xls行数："
									+ allTowerDataRows[i].getIndexInFile());
						}
						attList.add(new AttValueStruct(
								KGameAttrType.EXPERIENCE, exp, 0));

						List<KCurrencyCountStruct> curList = new ArrayList<KCurrencyCountStruct>();
						for (int j = 1; j <= 2; j++) {
							String curTypeKey = "cur" + j;
							String curCountTypeKey = "count" + j;
							if (!allTowerDataRows[i].getData(curTypeKey)
									.equals("")
									&& allTowerDataRows[i].getData(curTypeKey) != null) {
								KCurrencyTypeEnum curType = KCurrencyTypeEnum
										.getEnum(allTowerDataRows[i]
												.getByte(curTypeKey));
								int count = allTowerDataRows[i]
										.getInt(curCountTypeKey);
								if (curType == null) {
									throw new KGameServerException(
											"读取表<"
													+ tableName
													+ ">的字段："
													+ curTypeKey
													+ "错误,找不到货币类型！xls行数："
													+ allTowerDataRows[i]
															.getIndexInFile());
								}
								curList.add(new KCurrencyCountStruct(curType,
										count));
							}
						}

						String itemInfo = allTowerDataRows[i]
								.getData("item_info");
						List<ItemCountStruct> itemList = initItemReward(
								tableName, itemInfo,
								allTowerDataRows[i].getIndexInFile());
						/*if (itemList.isEmpty()) {
							throw new KGameServerException("读取表<" + tableName
									+ ">的字段：item_info错误,不能没有道具数据！xls行数："
									+ allTowerDataRows[i].getIndexInFile());
						}*/

						KTowerReward reward = new KTowerReward();
						reward.setTowerId(towerId);
						reward.setReward(new BaseRewardData(attList, curList,
								itemList, Collections.<Integer> emptyList(),
								Collections.<Integer> emptyList()));

						_towerRewardMap.put(reward.getTowerId(), reward);
					}
				}
			}
		}
	}

	private static List<ItemCountStruct> initItemReward(String tableName,
			String dropData, int index) throws KGameServerException {
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
						if (KSupportFactory.getItemModuleSupport()
								.getItemTemplate(itemCode) == null) {
							throw new KGameServerException("初始化表<" + tableName
									+ ">的道具错误，找不到道具类型：" + itemCode
									+ "，excel行数：" + index);
						}
						list.add(new ItemCountStruct(KSupportFactory
								.getItemModuleSupport().getItemTemplate(
										itemCode), count));
					}/* else {
						throw new KGameServerException("初始化表<" + tableName
								+ ">的道具格式错误：：" + dropData + "，excel行数：" + index);
					}*/
				}
			}
		}
		return list;
	}

	public static KTowerData getTowerData(int towerId) {
		return _towerDataMap.get(towerId);
	}

	public static KTowerReward getTowerReward(int towerId) {
		return _towerRewardMap.get(towerId);
	}

	public static BaseRewardData caculateTowerReward(KRole role, int towerId) {
		return null;
	}

	public static BaseRewardData caculateFriendTowerReward(KRole role,
			long friendRoleId, int towerId) {
		return null;
	}

}
