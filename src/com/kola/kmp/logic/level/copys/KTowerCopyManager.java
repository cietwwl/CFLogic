package com.kola.kmp.logic.level.copys;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jxl.read.biff.BiffException;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.actionrecord.KActionRecorder;
import com.kola.kmp.logic.actionrecord.KActionType;
import com.kola.kmp.logic.level.BattlefieldWaveViewInfo;
import com.kola.kmp.logic.level.KBattleObjectDataStruct.MonsterData;
import com.kola.kmp.logic.level.KLevelReward.NormalItemRewardTemplate;
import com.kola.kmp.logic.level.gamestory.Animation;
import com.kola.kmp.logic.level.FightEventListener;
import com.kola.kmp.logic.level.FightResult;
import com.kola.kmp.logic.level.KCompleteLevelCondition;
import com.kola.kmp.logic.level.KEnterLevelCondition;
import com.kola.kmp.logic.level.KGameBattlefield;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.level.KGameLevelRecord;
import com.kola.kmp.logic.level.KGameLevelSet;
import com.kola.kmp.logic.level.KLevelReward;
import com.kola.kmp.logic.level.KLevelTemplate;
import com.kola.kmp.logic.npc.KNPCDataStructs.KMonstTemplate;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KGameBattlefieldTypeEnum;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.LevelTips;
import com.kola.kmp.logic.util.tips.RewardTips;
import com.kola.kmp.protocol.level.KLevelProtocol;

/**
 * 爬塔副本逻辑管理类
 * 
 * @author zhaizl
 * 
 */
public class KTowerCopyManager {
	private static final KGameLogger _LOGGER = KGameLogger.getLogger(KTowerCopyManager.class);

	// 副本的所有关卡MAP
	public static Map<Integer, KLevelTemplate> towerCopyLevelMap = new LinkedHashMap<Integer, KLevelTemplate>();
	// 副本的分组关卡MAP
	public static Map<Integer, List<KLevelTemplate>> towerCopyLevelGroupMap = new LinkedHashMap<Integer, List<KLevelTemplate>>();
	// 副本中的所有战场
	public static Map<Integer, KGameBattlefield> allKGameBattlefield = new LinkedHashMap<Integer, KGameBattlefield>();

	// 副本的关卡对应的通关奖励MAP，Key：关卡Id
	public static Map<Integer, BaseRewardData> passLevelRewardMap = new LinkedHashMap<Integer, BaseRewardData>();

	// 副本的关卡对应的每天奖励MAP，Key：关卡Id
	public static Map<Integer, BaseRewardData> dayRewardMap = new LinkedHashMap<Integer, BaseRewardData>();

	public static KLevelTemplate firstLevel;
	public static KLevelTemplate endLevel;

	// 副本免费挑战次数
	public static int free_challenge_count = 5;

	public void init(String xlsPath) throws KGameServerException {
		KGameExcelFile xlsFile = null;
		try {
			xlsFile = new KGameExcelFile(xlsPath);
		} catch (BiffException e) {
			throw new KGameServerException("读取关卡剧本excel《异能要塞》表头发生错误！", e);
		} catch (IOException e) {
			throw new KGameServerException("读取关卡剧本excel《异能要塞》表头发生错误！", e);
		}

		if (xlsFile != null) {
			loadExcelData(xlsFile);
		}
	}

	private void loadExcelData(KGameExcelFile xlsFile) throws KGameServerException {

		String tableName = "异能要塞";
		int levelDataRowIndex = 5;
		KGameExcelTable levelDataTable = xlsFile.getTable(tableName, levelDataRowIndex);
		KGameExcelRow[] allLevelDataRows = levelDataTable.getAllDataRows();

		if (allLevelDataRows != null) {
			int groupId = 1;
			for (int i = 0; i < allLevelDataRows.length; i++) {
				KLevelTemplate level = new KLevelTemplate();
				level.setLevelType(KGameLevelTypeEnum.爬塔副本关卡);
				level.setLevelId(allLevelDataRows[i].getInt("sceneId"));
				level.setLevelName(allLevelDataRows[i].getData("Name"));
				level.setLevelNumber((i + 1));

				level.setFightPower(allLevelDataRows[i].getInt("fightpoint"));

				int frontLevelId = allLevelDataRows[i].getInt("pre_copy");
				KEnterLevelCondition enterCon = new KEnterLevelCondition(0, 0, 0, frontLevelId, 0);
				level.setEnterCondition(enterCon);
				KCompleteLevelCondition completeCon = new KCompleteLevelCondition();
				level.setCompleteCondition(completeCon);

				boolean isBoss = allLevelDataRows[i].getBoolean("IsBoss");				
				level.setScenarioId(groupId);
				if (allLevelDataRows[i].getData("HeadID") != null && allLevelDataRows[i].getData("HeadID").length() > 0) {
					int bossIconId = allLevelDataRows[i].getInt("HeadID");
					level.setBossIconResId(bossIconId);
				}

				// 读取战场数据 ///////////////////
				String battleResPath = allLevelDataRows[i].getData("battle_res_path");

				if (battleResPath == null || battleResPath.length() == 0) {
					throw new KGameServerException("加载表<" + tableName + ">的字段battle_res_path数据错误，值=" + battleResPath + "，Row=" + allLevelDataRows[i].getIndexInFile());
				}
				int battleMusicResId = allLevelDataRows[i].getInt("music");
				int battlefieldId = KGameLevelModuleExtension.getManager().getBattlefieldIdGenerator().nextBattlefieldId();
				KGameBattlefield battle = new KGameBattlefield();
				battle.setBattlefieldId(battlefieldId);
				battle.setBattlefieldType(KGameBattlefieldTypeEnum.爬塔副本战场);
				battle.setLevelId(level.getLevelId());
				battle.setBattlefieldSerialNumber(1);
				battle.setBattlefieldResId(0);
				battle.setBgMusicResId(battleMusicResId);
				battle.setFirstBattlefield(true);
				int nextBattleFieldId = -1;
				battle.setLastBattlefield(true);
				battle.initBattlefield(tableName, battleResPath, nextBattleFieldId, allLevelDataRows[i].getIndexInFile());
				level.getNormalBattlefieldMap().put(battle.getBattlefieldId(), battle);
				allKGameBattlefield.put(battle.getBattlefieldId(), battle);

				if (battle.getAllWaveInfo().isEmpty() || battle.getAllWaveInfo().size() == 0 || battle.getAllWaveInfo().size() > 1) {
					throw new KGameServerException("加载表<" + tableName + ">的战场battle_res_path的XML文件数据错误，分段波数数据为0或大于1，值=" + battleResPath + "，Row=" + allLevelDataRows[i].getIndexInFile());
				}

				// 替换怪物模版
				String frontMonTempIdStr = allLevelDataRows[i].getData("FrontRowMonsterID");
				List<KMonstTemplate> frontList = analyseMonstTemplateData(tableName, allLevelDataRows[i], frontMonTempIdStr, false);
				String middleMonTempIdStr = allLevelDataRows[i].getData("MiddleRowMonsterID");
				List<KMonstTemplate> middleList = analyseMonstTemplateData(tableName, allLevelDataRows[i], middleMonTempIdStr, false);
				String endMonTempIdStr = allLevelDataRows[i].getData("BackRowMonsterID");
				List<KMonstTemplate> endList = analyseMonstTemplateData(tableName, allLevelDataRows[i], endMonTempIdStr, true);

				BattlefieldWaveViewInfo viewInfo = battle.getAllWaveInfo().get(0);

				List<MonsterData> monList = constructBattlefieldWaveViewInfo(tableName, allLevelDataRows[i], viewInfo, frontList, middleList, endList);
				viewInfo.resetAllMonsterData(monList);
				battle.resetAllMonsterData(monList);

				towerCopyLevelMap.put(level.getLevelId(), level);
				if (!towerCopyLevelGroupMap.containsKey(groupId)) {
					towerCopyLevelGroupMap.put(groupId, new ArrayList<KLevelTemplate>());
				}
				towerCopyLevelGroupMap.get(groupId).add(level);
				if (level.getEnterCondition().getFrontLevelId() > 0) {
					if (towerCopyLevelMap.containsKey(level.getEnterCondition().getFrontLevelId())) {
						(towerCopyLevelMap.get(level.getEnterCondition().getFrontLevelId()).getHinderGameLevelList()).add(level);
					}
				}

				if (isBoss) {
					groupId++;
				}

				if (i == 0) {
					firstLevel = level;
				} else if (i == allLevelDataRows.length - 1) {
					endLevel = level;
				}

				// 通关奖励
				List<KCurrencyCountStruct> curList = new ArrayList<KCurrencyCountStruct>();
				for (int j = 0; j < 2; j++) {
					byte curType = allLevelDataRows[i].getByte("Clearance_Currency" + (j + 1));
					KCurrencyTypeEnum currencyTypeEnum = KCurrencyTypeEnum.getEnum(curType);
					if (currencyTypeEnum == null) {
						throw new KGameServerException("加载表<" + tableName + ">的字段Clearance_Currency" + (j + 1) + "数据错误，找不到货币类型值=" + curType + "，Row=" + allLevelDataRows[i].getIndexInFile());
					}
					long curCount = allLevelDataRows[i].getLong("Clearance_need_Currency" + (j + 1));
					curList.add(new KCurrencyCountStruct(currencyTypeEnum, curCount));
				}

				String itemInfo = allLevelDataRows[i].getData("Clearance_Item");
				List<ItemCountStruct> itemList = null;
				if(itemInfo != null && !itemInfo.equals("") && itemInfo.length()>0){
					itemList = initItemReward(tableName, "Clearance_Item", itemInfo, allLevelDataRows[i].getIndexInFile());
				}
				
				BaseRewardData levelPassReward = new BaseRewardData(null, curList, itemList, null, null);
				passLevelRewardMap.put(level.getLevelId(), levelPassReward);

				// 通关奖励
				List<KCurrencyCountStruct> curList1 = new ArrayList<KCurrencyCountStruct>();
				for (int j = 0; j < 2; j++) {
					byte curType = allLevelDataRows[i].getByte("Daily_Currency" + (j + 1));
					KCurrencyTypeEnum currencyTypeEnum = KCurrencyTypeEnum.getEnum(curType);
					if (currencyTypeEnum == null) {
						throw new KGameServerException("加载表<" + tableName + ">的字段Daily_Currency" + (j + 1) + "数据错误，找不到货币类型值=" + curType + "，Row=" + allLevelDataRows[i].getIndexInFile());
					}
					long curCount = allLevelDataRows[i].getLong("Daily_need_Currency" + (j + 1));
					curList1.add(new KCurrencyCountStruct(currencyTypeEnum, curCount));
				}

				String itemInfo1 = allLevelDataRows[i].getData("Daily_Item");
				List<ItemCountStruct> itemList1 = null;
				if(itemInfo1 != null && !itemInfo1.equals("") && itemInfo1.length()>0){
					itemList1 = initItemReward(tableName, "Daily_Item", itemInfo1, allLevelDataRows[i].getIndexInFile());
				}				
				BaseRewardData dayReward = new BaseRewardData(null, curList1, itemList1, null, null);
				dayRewardMap.put(level.getLevelId(), dayReward);
			}
		}
	}

	private static List<ItemCountStruct> initItemReward(String tableName, String paramName, String dropData, int index) throws KGameServerException {
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
						itemTemplate = new NormalItemRewardTemplate(itemCode, count);
						if (KSupportFactory.getItemModuleSupport().getItemTemplate(itemCode) == null) {
							throw new KGameServerException("初始化表<" + tableName + ">的道具错误，找不到道具类型：" + itemCode + "，字段：" + paramName + "，excel行数：" + index);
						}
						list.add(new ItemCountStruct(KSupportFactory.getItemModuleSupport().getItemTemplate(itemCode), count));
					} else {
						throw new KGameServerException("初始化表<" + tableName + ">的道具格式错误，字段：" + paramName + "，excel行数：" + index);
					}
				}
			}
		}
		return list;
	}

	public List<KMonstTemplate> analyseMonstTemplateData(String tableName, KGameExcelRow xlsRow, String monTempIdStr, boolean isEnd) throws KGameServerException {
		List<KMonstTemplate> monList = new ArrayList<KMonstTemplate>();
		if (monTempIdStr == null || monTempIdStr.length() == 0 || monTempIdStr.equals("")) {
//			if (isEnd) {
				return monList;
//			}
//			throw new KGameServerException("加载表<" + tableName + ">的战场battle_res_path的XML文件数据错误，怪物数据不能为空，值=" + monTempIdStr + "，Row=" + xlsRow.getIndexInFile());
		}
		String[] monDataStr = monTempIdStr.split(",");
		if (monDataStr == null || monDataStr.length == 0) {
			throw new KGameServerException("加载表<" + tableName + ">的战场battle_res_path的XML文件数据错误，怪物数据不能为空，值=" + monTempIdStr + "，Row=" + xlsRow.getIndexInFile());
		}
		for (int i = 0; i < monDataStr.length; i++) {
			String[] monData = monDataStr[i].split("\\*");
			if (monData == null || monData.length != 2) {
				throw new KGameServerException("加载表<" + tableName + ">的战场battle_res_path的XML文件数据错误，怪物数据格式错误，值=" + monTempIdStr + "，Row=" + xlsRow.getIndexInFile());
			}
			int monTempId = Integer.parseInt(monData[0]);
			KMonstTemplate mon = KSupportFactory.getNpcModuleSupport().getMonstTemplate(monTempId);
			if (mon == null) {
				throw new KGameServerException("加载表<" + tableName + ">的战场battle_res_path的XML文件数据错误，找不到怪物模版=" + monData[0] + "！，excel Row=" + xlsRow.getIndexInFile());
			}
			int count = Integer.parseInt(monData[1]);
			for (int j = 0; j < count; j++) {
				monList.add(mon);
			}

		}
		return monList;
	}

	private List<MonsterData> constructBattlefieldWaveViewInfo(String tableName, KGameExcelRow xlsRow, BattlefieldWaveViewInfo viewInfo, List<KMonstTemplate> frontList,
			List<KMonstTemplate> middleList, List<KMonstTemplate> endList) throws KGameServerException {
		List<MonsterData> resultList = new ArrayList<MonsterData>();
		List<MonsterData> monsterDataList = viewInfo.getAllMonsters();
		int totalMonSize = frontList.size() + middleList.size() + endList.size();
		if (monsterDataList.size() < totalMonSize) {
			throw new KGameServerException("加载表<" + tableName + ">的战场battle_res_path的XML文件数据错误，战场怪物实体不足=" + totalMonSize + "！，excel Row=" + xlsRow.getIndexInFile());
		}
		int emptySize = monsterDataList.size() - totalMonSize;
		int frontSize = frontList.size();
		int middleSize = middleList.size();
		int endSize = endList.size();
		for (int i = 0; i < emptySize; i++) {
			if (i % 3 == 0) {
				frontSize++;
			} else if (i % 3 == 1) {
				middleSize++;
			} else {
				endSize++;
			}
		}
		int frontIndex = 0;
		int middleIndex = frontSize;
		int endIndex = middleIndex + middleSize;
		List<Integer> frontIndexList = new ArrayList<Integer>();
		for (int i = 0; i < middleIndex; i++) {
			frontIndexList.add(i);
		}
		UtilTool.randomList(frontIndexList);

		List<Integer> middleIndexList = new ArrayList<Integer>();
		for (int i = middleIndex; i < endIndex; i++) {
			middleIndexList.add(i);
		}
		UtilTool.randomList(middleIndexList);

		List<Integer> endIndexList = new ArrayList<Integer>();
		for (int i = endIndex; i < endIndex + endSize; i++) {
			endIndexList.add(i);
		}
		UtilTool.randomList(endIndexList);

		Collections.sort(monsterDataList);

		for (int i = 0; i < frontList.size(); i++) {
			MonsterData tempData = monsterDataList.get(frontIndexList.get(i));
			if (tempData == null) {
				throw new KGameServerException("加载表<" + tableName + ">的战场battle_res_path的XML文件数据错误，战场怪物实体不足=" + totalMonSize + "！，excel Row=" + xlsRow.getIndexInFile());
			}
			KMonstTemplate newTemp = frontList.get(i);
			MonsterData newMonData = new MonsterData(tempData._objInstanceId, newTemp, tempData._corX, tempData._corY);
			resultList.add(newMonData);
		}

		for (int i = 0; i < middleList.size(); i++) {
			MonsterData tempData = monsterDataList.get(middleIndexList.get(i));
			if (tempData == null) {
				throw new KGameServerException("加载表<" + tableName + ">的战场battle_res_path的XML文件数据错误，战场怪物实体不足=" + totalMonSize + "！，excel Row=" + xlsRow.getIndexInFile());
			}
			KMonstTemplate newTemp = middleList.get(i);
			MonsterData newMonData = new MonsterData(tempData._objInstanceId, newTemp, tempData._corX, tempData._corY);
			resultList.add(newMonData);
		}

		for (int i = 0; i < endList.size(); i++) {
			MonsterData tempData = monsterDataList.get(endIndexList.get(i));
			if (tempData == null) {
				throw new KGameServerException("加载表<" + tableName + ">的战场battle_res_path的XML文件数据错误，战场怪物实体不足=" + totalMonSize + "！，excel Row=" + xlsRow.getIndexInFile());
			}
			KMonstTemplate newTemp = endList.get(i);
			MonsterData newMonData = new MonsterData(tempData._objInstanceId, newTemp, tempData._corX, tempData._corY);
			resultList.add(newMonData);
		}

		return resultList;
	}

	public void sendCopyData(KRole role) {
		// checkAndResetCopyDatas(role, true);
		checkAndResetTowerCopyDatas(role, true);
		// 获取关卡记录
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
		KGameLevelTypeEnum levelTypeEnum = KGameLevelTypeEnum.getEnum(KGameLevelTypeEnum.爬塔副本关卡.levelType);

		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_GET_TOWER_COPY_DATA);
		KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.爬塔副本关卡);
		int nowLevelId = firstLevel.getLevelId();
		boolean isGetReward = false;
		if (record != null && record.towerCopyData != null) {
			if (record.towerCopyData.nowLevelId == endLevel.getLevelId()) {
				nowLevelId = endLevel.getLevelId();
				isGetReward = record.towerCopyData.todayGetReward;
			} else if (record.towerCopyData.nowLevelId == 0) {
				nowLevelId = firstLevel.getLevelId();
				isGetReward = true;
			} else {
				nowLevelId = towerCopyLevelMap.get(record.towerCopyData.nowLevelId).getHinderGameLevelList().get(0).getLevelId();
				isGetReward = record.towerCopyData.todayGetReward;
			}
		}
		KLevelTemplate nowLevelTemplate = towerCopyLevelMap.get(nowLevelId);

		BaseRewardData dayRewardData = null;
		if(dayRewardMap.containsKey(record.towerCopyData.nowLevelId)){
			dayRewardData = dayRewardMap.get(record.towerCopyData.nowLevelId);
		}else{
			dayRewardData = new BaseRewardData(null, null, null, null, null);
		}
		List<KLevelTemplate> levelList = towerCopyLevelGroupMap.get(nowLevelTemplate.getScenarioId());

		sendMsg.writeInt(nowLevelId);
		sendMsg.writeByte(KGameLevelTypeEnum.爬塔副本关卡.levelType);
		sendMsg.writeByte(levelList.size());
		for (KLevelTemplate levelTemplate : levelList) {
			sendMsg.writeInt(levelTemplate.getLevelId());
			sendMsg.writeUtf8String(levelTemplate.getLevelName());
			sendMsg.writeInt(levelTemplate.getLevelNumber());
			sendMsg.writeInt(levelTemplate.getBossIconResId());
			sendMsg.writeInt(levelTemplate.getFightPower());
			boolean isComplete = false;
			if (levelTemplate.getLevelId() <= record.towerCopyData.nowLevelId) {
				isComplete = true;
			}
			sendMsg.writeBoolean(isComplete);

			BaseRewardData levelRewardData = passLevelRewardMap.get(levelTemplate.getLevelId());
			levelRewardData.packMsg(sendMsg);
		}

		dayRewardData.packMsg(sendMsg);
		sendMsg.writeBoolean(isGetReward);

		role.sendMsg(sendMsg);
	}

	public void completeOrUpdateCopyInfo(KRole role, KGameLevelSet levelSet, KLevelTemplate completeLevel) {
		KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.爬塔副本关卡);
		int nowLevelId = firstLevel.getLevelId();
		boolean isGetReward = false;
		if (record != null && record.towerCopyData != null) {
			if (record.towerCopyData.nowLevelId == endLevel.getLevelId()) {
				nowLevelId = endLevel.getLevelId();
				isGetReward = record.towerCopyData.todayGetReward;
			} else if (record.towerCopyData.nowLevelId == 0) {
				nowLevelId = firstLevel.getLevelId();
				isGetReward = true;
			} else {
				nowLevelId = towerCopyLevelMap.get(record.towerCopyData.nowLevelId).getHinderGameLevelList().get(0).getLevelId();
				isGetReward = record.towerCopyData.todayGetReward;
			}
		}
		BaseRewardData dayRewardData = null;
		if(dayRewardMap.containsKey(record.towerCopyData.nowLevelId)){
			dayRewardData = dayRewardMap.get(record.towerCopyData.nowLevelId);
		}else{
			dayRewardData = new BaseRewardData(null, null, null, null, null);
		}

		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_UPDATE_TOWER_COPY_DATA);
		sendMsg.writeInt(nowLevelId);
		if (completeLevel != null) {
			sendMsg.writeBoolean(true);
			sendMsg.writeInt(completeLevel.getLevelId());
			sendMsg.writeBoolean(true);
		} else {
			sendMsg.writeBoolean(false);
		}
		
		dayRewardData.packMsg(sendMsg);
		sendMsg.writeBoolean(isGetReward);
		
		role.sendMsg(sendMsg);
	}

	public KActionResult processPlayerRoleJoinLevel(KRole role, int levelId) {
		if (role == null) {
			_LOGGER.error("角色进入爬塔副本关卡失败，角色为null，关卡ID：" + levelId);
			// KDialogService.sendUprisingDialog(role.getId(),
			// LevelTips.getTipsJoinLevelFailed());
			return new KActionResult(false, LevelTips.getTipsJoinLevelFailed());
		}
		KLevelTemplate level = towerCopyLevelMap.get(levelId);
		if (level == null) {
			_LOGGER.error("角色进入爬塔副本关卡失败，找不到对应的关卡。角色id:" + role.getId() + "，关卡ID：" + levelId);
			KDialogService.sendUprisingDialog(role.getId(), LevelTips.getTipsJoinLevelFailed());
			return new KActionResult(false, LevelTips.getTipsJoinLevelFailed());
		}
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
		KGameLevelRecord ptRecord = levelSet.getCopyRecord(KGameLevelTypeEnum.爬塔副本关卡);

		KActionResult joinResult = checkPlayerRoleCanJoinGameLevel(role, levelSet, level, true, true);
		if (joinResult.success) {
			// 当前进入为普通战场模式，取得关卡第一层战场的数据，并通知战斗模块
			if (level.getAllNormalBattlefields().isEmpty()) {
				_LOGGER.error("角色进入关卡失败，找不到对应的第一层战场。角色id:" + role.getId() + "，关卡ID：" + levelId);
				KGameLevelModuleExtension.getManager().sendJoinNormalGameLevelTipsMessage(role, (short) -1, LevelTips.getTipsJoinLevelFailed(), false, null);
				return new KActionResult(false, LevelTips.getTipsJoinLevelFailed());
			}			

			// 通知战斗模块，角色进入战场
			List<Animation> animation = new ArrayList<Animation>();
			for (FightEventListener listener : KGameLevelModuleExtension.getManager().getFightEventListenerList()) {
				listener.notifyBattle(role, level.getAllNormalBattlefields(), animation);
			}

			KActionRecorder.recordAction(role.getId(), KActionType.ACTION_TYPE_TOWER_COPY, 1);
			
			KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.异能要塞);
		}

		return joinResult;
	}

	private KActionResult checkPlayerRoleCanJoinGameLevel(KRole role, KGameLevelSet levelSet, KLevelTemplate level, boolean isNeedCheckCondition, boolean isSendDialog) {
		KActionResult result = new KActionResult();
		String tips = "";
		KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.爬塔副本关卡);
		if (isNeedCheckCondition) {
			// 获取角色当前体力值

			// 判断关卡是否开放
			int nowLevelId = firstLevel.getLevelId();
			if (record != null && record.towerCopyData != null) {
				if (record.towerCopyData.nowLevelId == endLevel.getLevelId()) {
					nowLevelId = endLevel.getLevelId();
				} else if (record.towerCopyData.nowLevelId == 0) {
					nowLevelId = firstLevel.getLevelId();
				} else {
					nowLevelId = towerCopyLevelMap.get(record.towerCopyData.nowLevelId).getHinderGameLevelList().get(0).getLevelId();
				}
			}
			if(record.towerCopyData.nowLevelId == endLevel.getLevelId()){
				tips = LevelTips.getTipsTowerCopyEndLevel();
				if (isSendDialog) {
					KGameLevelModuleExtension.getManager().sendJoinNormalGameLevelTipsMessage(role, (short) -1, tips, false, null);
				}
				result.success = false;
				result.tips = tips;
				return result;
			}
			
			KLevelTemplate nextTargetLevel = towerCopyLevelMap.get(nowLevelId);
			if (level.getLevelId() > nextTargetLevel.getLevelId()) {
				tips = LevelTips.getTipsLevelNotOpen();
				if (isSendDialog) {
					KGameLevelModuleExtension.getManager().sendJoinNormalGameLevelTipsMessage(role, (short) -1, tips, false, null);
				}
				result.success = false;
				result.tips = tips;
				return result;
			} else if (level.getLevelId() < nextTargetLevel.getLevelId()) {
				tips = LevelTips.getTipsTowerCopyLevelIsComplete();
				if (isSendDialog) {
					KGameLevelModuleExtension.getManager().sendJoinNormalGameLevelTipsMessage(role, (short) -1, tips, false, null);
				}
				result.success = false;
				result.tips = tips;
				return result;
			}
			// 进入次数
//			if (record.towerCopyData.remainChallengeCount <= 0) {
//				tips = LevelTips.getTipsTowerCopyChallengeCountFull();
//				if (isSendDialog) {
//					KGameLevelModuleExtension.getManager().sendJoinNormalGameLevelTipsMessage(role, (short) -1, tips, false, null);
//				}
//				result.success = false;
//				result.tips = tips;
//				return result;
//			}
		}
		result.success = true;
		result.tips = tips;
		return result;
	}

	/**
	 * 处理角色完成关卡，处理关卡结算相关流程
	 * 
	 * @param role
	 * @param level
	 */
	public void processPlayerRoleCompleteCopyLevel(KRole role, KLevelTemplate level, FightResult result) {
		// 获取关卡记录
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());

		KGameLevelRecord record = levelSet.getCopyRecord(level.getLevelType());

		BaseRewardData reward = passLevelRewardMap.get(level.getLevelId());

		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_SHOW_COMPLETE_TOWER_COPY_BATTLE_RESULT);

		sendMsg.writeInt(level.getLevelId());
		sendMsg.writeByte(level.getLevelType().levelType);
		sendMsg.writeInt(level.getLevelNumber());
		sendMsg.writeBoolean(true);
		reward.packMsg(sendMsg);

		role.sendMsg(sendMsg);
		// 记录完成关卡
		levelSet.recordCompleteTowerCopy(level.getLevelId());
		// 发通关奖励
		if (!reward.sendReward(role, PresentPointTypeEnum.爬塔副本奖励)) {
			BaseMailContent mainContent = new BaseMailContent(LevelTips.getTipsSendMailItemForBagFullTitle(), LevelTips.getTipsSendMailItemForBagFull(), null, null);
			KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), mainContent, reward, PresentPointTypeEnum.爬塔副本奖励);
			KDialogService.sendUprisingDialog(role, RewardTips.背包已满奖励通过邮件发送);
		}

		// 发消息通知用户该关卡记录有发生改变
		KLevelTemplate nextLevel = null;
		if (level.getHinderGameLevelList().size() > 0) {
			nextLevel = level.getHinderGameLevelList().get(0);
		}
		if (nextLevel == null) {
			completeOrUpdateCopyInfo(role, levelSet, level);
		} else {
			if (nextLevel.getScenarioId() == level.getScenarioId()) {
				completeOrUpdateCopyInfo(role, levelSet, level);
			} else {
				sendCopyData(role);
			}
		}
	}

	public boolean checkAndResetTowerCopyDatas(KRole role, boolean isNeedCheck) {
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());

		return levelSet.checkAndResetTowerCopyData(isNeedCheck);
	}

	public void processGetDayReward(KRole role) {
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
		KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.爬塔副本关卡);
		if (record == null || record.towerCopyData == null) {
			sendGetDayRewardTips(role, false, GlobalTips.服务器繁忙请稍候再试);
			return;
		}
		if (record.towerCopyData.nowLevelId == 0) {
			sendGetDayRewardTips(role, false, GlobalTips.服务器繁忙请稍候再试);
			return;
		}
		if (record.towerCopyData.todayGetReward) {
			sendGetDayRewardTips(role, false, LevelTips.getTipsAlreadyGetTowerCopyDayPrice());
			return;
		}
		BaseRewardData rewardData = null;
		if (dayRewardMap.containsKey(record.towerCopyData.nowLevelId)) {
			rewardData = dayRewardMap.get(record.towerCopyData.nowLevelId);
		} else {
			rewardData = dayRewardMap.get(firstLevel.getLevelId());
		}
		if (rewardData == null) {
			sendGetDayRewardTips(role, false, GlobalTips.服务器繁忙请稍候再试);
			return;
		}
		// 发通关奖励
		String tips = RewardTips.成功领取奖励;
		if (!rewardData.sendReward(role, PresentPointTypeEnum.爬塔副本奖励)) {
			BaseMailContent mainContent = new BaseMailContent(LevelTips.getTipsSendMailItemForBagFullTitle(), LevelTips.getTipsSendMailItemForBagFull(), null, null);
			KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), mainContent, rewardData, PresentPointTypeEnum.爬塔副本奖励);

			tips = RewardTips.背包已满奖励通过邮件发送;
		} else {
			KDialogService.sendDataUprisingDialog(role, rewardData.dataUprisingTips);
		}
		levelSet.recordGetTowerReward();
		sendGetDayRewardTips(role, true, tips);
	}

	private void sendGetDayRewardTips(KRole role, boolean isGetSuccess, String tips) {
		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_GET_TOWER_COPY_REWARD);
		sendMsg.writeBoolean(isGetSuccess);
		sendMsg.writeUtf8String(tips);
		role.sendMsg(sendMsg);
	}
	
	public static void sendSpecialBattleFailedMsg(KRole role,int levelId,byte levelType, int levelNum){
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
		// 记录进入次数
		levelSet.recordChallengeTowerCopy();
		
		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_SHOW_COMPLETE_TOWER_COPY_BATTLE_RESULT);
		sendMsg.writeInt(levelId);
		sendMsg.writeByte(levelType);
		sendMsg.writeInt(levelNum);
		sendMsg.writeBoolean(false);
		
		role.sendMsg(sendMsg);
		
		KDialogService.sendNullDialog(role);
	}
}
