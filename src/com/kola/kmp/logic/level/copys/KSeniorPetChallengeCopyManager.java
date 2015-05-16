package com.kola.kmp.logic.level.copys;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.kola.kmp.logic.combat.ICombatRoleSideHpUpdater;
import com.kola.kmp.logic.combat.KCombatType;
import com.kola.kmp.logic.gamble.wish2.KWish2ItemPool.KWish2DropItem;
import com.kola.kmp.logic.level.KGameBattlefield;
import com.kola.kmp.logic.level.KBattleObjectDataStruct.MonsterData;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.level.KGameLevelRecord.PetChallengeCopyData;
import com.kola.kmp.logic.level.KGameLevelRecord.SeniorPetChallengeCopyData;
import com.kola.kmp.logic.level.copys.KPetChallengeCopyManager.PetChallengeCopyCombatRoleSideHpUpdater;
import com.kola.kmp.logic.level.gamestory.Animation;
import com.kola.kmp.logic.level.BattlefieldWaveViewInfo;
import com.kola.kmp.logic.level.FightEventListener;
import com.kola.kmp.logic.level.FightResult;
import com.kola.kmp.logic.level.KGameLevelRecord;
import com.kola.kmp.logic.level.KGameLevelSet;
import com.kola.kmp.logic.level.KLevelReward;
import com.kola.kmp.logic.level.KLevelTemplate;
import com.kola.kmp.logic.npc.KNPCDataStructs.KMonstTemplate;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KGameBattlefieldTypeEnum;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.pet.KPet;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.ItemModuleSupport;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.LevelTips;
import com.kola.kmp.logic.util.tips.RewardTips;
import com.kola.kmp.logic.vip.KVIPDataManager;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;
import com.kola.kmp.protocol.level.KLevelProtocol;

public class KSeniorPetChallengeCopyManager {
	private static final KGameLogger _LOGGER = KGameLogger.getLogger(KSeniorPetChallengeCopyManager.class);

	public static final int BATTLE_MONSTER_TYPE_BOSS = 1;
	public static final int BATTLE_MONSTER_TYPE_ELITE = 2;
	public static final int BATTLE_MONSTER_TYPE_NORMAL = 3;

	// 副本的所有关卡MAP
	public static Map<Integer, KLevelTemplate> seniorPetChallengeCopyLevelMap = new LinkedHashMap<Integer, KLevelTemplate>();
	// 副本中的所有战场
	public static Map<Integer, KGameBattlefield> allKGameBattlefieldTemplate = new LinkedHashMap<Integer, KGameBattlefield>();

	public static Map<Integer, Map<Integer, List<MonsterInfoData>>> monsterInfoMap = new LinkedHashMap<Integer, Map<Integer, List<MonsterInfoData>>>();
	public static Map<Integer, Map<Integer, Integer>> monsterInfoWeightMap = new LinkedHashMap<Integer, Map<Integer, Integer>>();
	public static Map<Integer, Map<Integer, MonsterInfoData>> defaultMonsterInfoMap = new LinkedHashMap<Integer, Map<Integer, MonsterInfoData>>();
	public static Map<Integer, Integer> monsterHeadIdMap = new HashMap<Integer, Integer>();
	public static Set<Integer> monsterTempIdSet = new HashSet<Integer>();
	public static Set<Integer> bossMonsterTempIdSet = new HashSet<Integer>();

	public static KLevelTemplate firstLevel;
	public static KLevelTemplate endLevel;
	// 副本免费挑战次数
	public static int free_challenge_count = 5;

	public static int saodangVipLevel = 1;

	public void init(String xlsPath) throws KGameServerException {
		KGameExcelFile xlsFile = null;
		try {
			xlsFile = new KGameExcelFile(xlsPath);
		} catch (BiffException e) {
			throw new KGameServerException("读取关卡剧本excel《高级随从试练》表头发生错误！", e);
		} catch (IOException e) {
			throw new KGameServerException("读取关卡剧本excel《高级随从试练》表头发生错误！", e);
		}

		if (xlsFile != null) {
			loadExcelData(xlsFile);
		}

	}

	private void loadExcelData(KGameExcelFile xlsFile) throws KGameServerException {
		String tableName = "高级随从试练";
		int levelDataRowIndex = 5;
		KGameExcelTable levelDataTable = xlsFile.getTable(tableName, levelDataRowIndex);
		KGameExcelRow[] allLevelDataRows = levelDataTable.getAllDataRows();

		if (allLevelDataRows != null) {
			int groupId = 1;
			for (int i = 0; i < allLevelDataRows.length; i++) {
				KLevelTemplate level = new KLevelTemplate();
				level.init(tableName, allLevelDataRows[i], KGameLevelTypeEnum.高级随从挑战副本关卡);
				level.setLevelNumber((i + 1));
				seniorPetChallengeCopyLevelMap.put(level.getLevelId(), level);
				if (level.getEnterCondition().getFrontLevelId() > 0) {
					if (seniorPetChallengeCopyLevelMap.containsKey(level.getEnterCondition().getFrontLevelId())) {
						(seniorPetChallengeCopyLevelMap.get(level.getEnterCondition().getFrontLevelId()).getHinderGameLevelList()).add(level);
					}
				}
				if (i == 0) {
					firstLevel = level;
				} else if (i == allLevelDataRows.length - 1) {
					endLevel = level;
				}

				if (!monsterInfoMap.containsKey(level.getLevelId())) {
					monsterInfoMap.put(level.getLevelId(), new LinkedHashMap<Integer, List<MonsterInfoData>>());
				}
				if (!monsterInfoWeightMap.containsKey(level.getLevelId())) {
					monsterInfoWeightMap.put(level.getLevelId(), new LinkedHashMap<Integer, Integer>());
				}
				if (!defaultMonsterInfoMap.containsKey(level.getLevelId())) {
					defaultMonsterInfoMap.put(level.getLevelId(), new LinkedHashMap<Integer, MonsterInfoData>());
				}

				String bossMonStr = allLevelDataRows[i].getData("BOSSid");
				List<MonsterInfoData> bossList = analyseMonstTemplateData(tableName, "BOSSid", allLevelDataRows[i], bossMonStr, true);
				monsterInfoMap.get(level.getLevelId()).put(BATTLE_MONSTER_TYPE_BOSS, bossList);
				int bossTotalWeight = 0;
				for (MonsterInfoData infoData : bossList) {
					bossTotalWeight += infoData.weight;
				}
				monsterInfoWeightMap.get(level.getLevelId()).put(BATTLE_MONSTER_TYPE_BOSS, bossTotalWeight);
				defaultMonsterInfoMap.get(level.getLevelId()).put(BATTLE_MONSTER_TYPE_BOSS, bossList.get(0));

				String eliteMonStr = allLevelDataRows[i].getData("jyid");
				List<MonsterInfoData> eliteList = analyseMonstTemplateData(tableName, "jyid", allLevelDataRows[i], eliteMonStr, false);
				monsterInfoMap.get(level.getLevelId()).put(BATTLE_MONSTER_TYPE_ELITE, eliteList);
				int eliteTotalWeight = 0;
				for (MonsterInfoData infoData : eliteList) {
					eliteTotalWeight += infoData.weight;
				}
				monsterInfoWeightMap.get(level.getLevelId()).put(BATTLE_MONSTER_TYPE_ELITE, eliteTotalWeight);
				defaultMonsterInfoMap.get(level.getLevelId()).put(BATTLE_MONSTER_TYPE_ELITE, eliteList.get(0));

				String normalMonStr = allLevelDataRows[i].getData("Enemyid");
				List<MonsterInfoData> normalList = analyseMonstTemplateData(tableName, "Enemyid", allLevelDataRows[i], normalMonStr, false);
				monsterInfoMap.get(level.getLevelId()).put(BATTLE_MONSTER_TYPE_NORMAL, normalList);
				int normalTotalWeight = 0;
				for (MonsterInfoData infoData : normalList) {
					normalTotalWeight += infoData.weight;
				}
				monsterInfoWeightMap.get(level.getLevelId()).put(BATTLE_MONSTER_TYPE_NORMAL, normalTotalWeight);
				defaultMonsterInfoMap.get(level.getLevelId()).put(BATTLE_MONSTER_TYPE_NORMAL, normalList.get(0));
			}

			tableName = "随从试炼参数";
			levelDataTable = xlsFile.getTable(tableName, levelDataRowIndex);
			allLevelDataRows = levelDataTable.getAllDataRows();
			for (int i = 0; i < allLevelDataRows.length; i++) {
				free_challenge_count = allLevelDataRows[i].getInt("high_limit_join");
			}
		}
	}

	public List<MonsterInfoData> analyseMonstTemplateData(String tableName, String rowName, KGameExcelRow xlsRow, String monTempIdStr, boolean isBoss) throws KGameServerException {
		List<MonsterInfoData> monList = new ArrayList<MonsterInfoData>();
		if (monTempIdStr == null || monTempIdStr.length() == 0 || monTempIdStr.equals("")) {
			// if (isEnd) {
			return monList;
			// }
			// throw new KGameServerException("加载表<" + tableName +
			// ">的战场battle_res_path的XML文件数据错误，怪物数据不能为空，值=" + monTempIdStr +
			// "，Row=" + xlsRow.getIndexInFile());
		}
		String[] monDataStr = monTempIdStr.split(",");
		if (monDataStr == null || monDataStr.length == 0) {
			throw new KGameServerException("加载表<" + tableName + ">的字段" + rowName + "数据错误，怪物数据不能为空，值=" + monTempIdStr + "，Row=" + xlsRow.getIndexInFile());
		}
		for (int i = 0; i < monDataStr.length; i++) {
			String[] monData = monDataStr[i].split("\\*");
			if (monData == null || monData.length != 3) {
				throw new KGameServerException("加载表<" + tableName + ">的字段" + rowName + "数据错误，怪物数据格式错误，值=" + monTempIdStr + "，Row=" + xlsRow.getIndexInFile());
			}
			int monTempId = Integer.parseInt(monData[0]);
			KMonstTemplate mon = KSupportFactory.getNpcModuleSupport().getMonstTemplate(monTempId);
			if (mon == null) {
				throw new KGameServerException("加载表<" + tableName + ">的字段" + rowName + "数据错误，找不到怪物模版=" + monData[0] + "！，excel Row=" + xlsRow.getIndexInFile());
			}
			int count = Integer.parseInt(monData[1]);
			int weight = Integer.parseInt(monData[2]);

			monList.add(new MonsterInfoData(mon, count, weight));

			monsterTempIdSet.add(mon.id);
			if (isBoss) {
				bossMonsterTempIdSet.add(mon.id);
			}
		}
		return monList;
	}

	public void checkInit() throws KGameServerException {
		boolean checkBattlefied = true;
		for (KGameBattlefield battle : allKGameBattlefieldTemplate.values()) {

			if (battle.getBornPoint() == null) {
				checkBattlefied = false;
				_LOGGER.error("#########  加载levelConfig.xls表<随从试炼>的战场xml数据错误，该战场没有设置出生点，xml文件名=" + battle.getBattlePathName());
			}
			if (!battle.isInitOK) {
				_LOGGER.error("#########  加载levelConfig.xls表<随从试炼>的战场xml数据错误，该战场初始化失败，xml文件名={}，关卡ID={}", battle.getBattlePathName(), battle.getLevelId());
				checkBattlefied = false;
			}

		}
		if (!checkBattlefied) {
			throw new KGameServerException("#########  加载levelConfig.xls表<随从试炼>的战场xml数据错误。");
		}

		for (int vipLv = 0; vipLv <= KVIPDataManager.mVIPLevelDataManager.getMaxLevel().lvl; vipLv++) {
			VIPLevelData data = KSupportFactory.getVIPModuleSupport().getVIPLevelData(vipLv);
			if (data != null) {
				if (data.petTestSweepCount >= 1) {
					saodangVipLevel = vipLv;
					break;
				}
			}
		}

		for (Integer monTempId : monsterTempIdSet) {
			KMonstTemplate mon = KSupportFactory.getNpcModuleSupport().getMonstTemplate(monTempId);
			if (!monsterHeadIdMap.containsKey(mon.id)) {
				monsterHeadIdMap.put(mon.id, mon.monstUIData.monster_head);
			}
		}

		KSupportFactory.getCombatModuleSupport().registerCombatHpUpdater(new SeniorPetChallengeCopyCombatRoleSideHpUpdater());
	}

	public KGameBattlefield constructBattlefield(KRole role, KLevelTemplate level, KGameLevelSet levelSet, SeniorPetChallengeCopyData copyData) {

		KGameBattlefield battlefieldTemplate = level.getAllNormalBattlefields().get(0);
		BattlefieldWaveViewInfo viewInfoTemplate = battlefieldTemplate.getAllWaveInfo().get(0);
		List<MonsterData> targetMonsterDataList = new ArrayList<MonsterData>();

		if (!copyData.monsterMap.containsKey(level.getLevelId()) || copyData.monsterMap.get(level.getLevelId()).isEmpty()) {
			List<MonsterData> monsterDataList = new ArrayList<MonsterData>();
			monsterDataList.addAll(viewInfoTemplate.getAllMonsters());
			UtilTool.randomList(monsterDataList);

			List<MonsterInfoData> monInfoDataList = caculateBattleMonsterTemplateList(level.getLevelId());
			int index = 0;
			for (MonsterInfoData infoData : monInfoDataList) {
				for (int i = 0; i < infoData.count && index < monsterDataList.size(); i++, index++) {
					MonsterData oldData = monsterDataList.get(index);
					targetMonsterDataList.add(new MonsterData(oldData._objInstanceId, infoData.monTemp, oldData._corX, oldData._corY));
				}
			}
		} else {
			List<MonsterData> monsterDataList = new ArrayList<MonsterData>();
			monsterDataList.addAll(viewInfoTemplate.getAllMonsters());
			UtilTool.randomList(monsterDataList);
			Map<Integer, Integer> monMap = copyData.getLevelMonsterInfo(level.getLevelId());
			List<KMonstTemplate> monTempList = new ArrayList<KMonstTemplate>();
			for (Integer monTempId : monMap.keySet()) {
				KMonstTemplate monTemp = KSupportFactory.getNpcModuleSupport().getMonstTemplate(monTempId);
				int count = monMap.get(monTempId);
				for (int i = 0; i < count; i++) {
					monTempList.add(monTemp);
				}
			}
			for (int i = 0; i < monTempList.size() && i < monsterDataList.size(); i++) {
				KMonstTemplate monTemp = monTempList.get(i);
				MonsterData oldData = monsterDataList.get(i);
				targetMonsterDataList.add(new MonsterData(oldData._objInstanceId, monTemp, oldData._corX, oldData._corY));
			}
		}

		KGameBattlefield targetBattlefield = new KGameBattlefield();
		BattlefieldWaveViewInfo wave = new BattlefieldWaveViewInfo(viewInfoTemplate.getWaveId(), battlefieldTemplate.getBattlefieldId());
		targetBattlefield.setBattlefieldId(battlefieldTemplate.getBattlefieldId());
		targetBattlefield.setBattlefieldType(KGameBattlefieldTypeEnum.高级随从挑战副本战场);
		targetBattlefield.setLevelId(level.getLevelId());
		targetBattlefield.setBattlefieldSerialNumber(1);
		targetBattlefield.setBattlefieldResId(0);
		targetBattlefield.setBattlePathName(battlefieldTemplate.getBattlePathName());
		targetBattlefield.setBgMusicResId(targetBattlefield.getBgMusicResId());
		targetBattlefield.setFirstBattlefield(true);
		int nextBattleFieldId = -1;
		targetBattlefield.setLastBattlefield(true);
		targetBattlefield.setNextBattleFieldId(-1);

		wave._allObstructions.addAll(viewInfoTemplate._allObstructions);
		wave._allMonsters.addAll(targetMonsterDataList);
		wave._exitData = viewInfoTemplate._exitData;
		wave._isHasExit = viewInfoTemplate._isHasExit;
		targetBattlefield.allWaveInfo.add(wave);
		targetBattlefield.obstructionMap.putAll(battlefieldTemplate.obstructionMap);
		targetBattlefield.resetAllMonsterData(targetMonsterDataList);
		targetBattlefield.setBornPoint(battlefieldTemplate.getBornPoint());
		targetBattlefield.sectionPointDataList.addAll(battlefieldTemplate.sectionPointDataList);
		targetBattlefield.isInitOK = true;
		return targetBattlefield;
	}

	public List<MonsterInfoData> caculateBattleMonsterTemplateList(int levelId) {
		List<MonsterInfoData> resultList = new ArrayList<MonsterInfoData>();
		if (monsterInfoMap.containsKey(levelId)) {
			for (Integer battleMonType : monsterInfoMap.get(levelId).keySet()) {
				List<MonsterInfoData> infoDataList = monsterInfoMap.get(levelId).get(battleMonType);
				int totalWeight = monsterInfoWeightMap.get(levelId).get(battleMonType);
				int weight = UtilTool.random(0, totalWeight);
				int tempRate = 0;

				MonsterInfoData targetInfoData = null;
				L2: for (MonsterInfoData infoData : infoDataList) {
					if (tempRate < weight && weight <= (tempRate + infoData.weight)) {
						targetInfoData = infoData;
						break L2;
					} else {
						tempRate += infoData.weight;
					}
				}
				if (targetInfoData == null) {
					resultList.add(defaultMonsterInfoMap.get(levelId).get(battleMonType));
				} else {
					resultList.add(targetInfoData);
				}
			}

		}
		return resultList;
	}

	public void resetAllLevelMonster(KRole role, KGameLevelSet levelSet, SeniorPetChallengeCopyData copyData) {

		Map<Integer, Map<Integer, Integer>> monsterMap = new HashMap<Integer, Map<Integer, Integer>>();

		for (KLevelTemplate levelTemp : seniorPetChallengeCopyLevelMap.values()) {
			if (!monsterMap.containsKey(levelTemp.getLevelId())) {
				monsterMap.put(levelTemp.getLevelId(), new HashMap<Integer, Integer>());
			}
			List<MonsterInfoData> monInfoList = caculateBattleMonsterTemplateList(levelTemp.getLevelId());
			for (MonsterInfoData monInfo : monInfoList) {
				monsterMap.get(levelTemp.getLevelId()).put(monInfo.monTemp.id, monInfo.count);
			}
		}

		copyData.resetAllLevelMonsterInfo(monsterMap);
	}

	public void checkAndResetSeniorPetChallengeCopyData(KRole role) {
		KGameLevelSet levelSet = KGameLevelModuleExtension.getGameLevelSet(role.getId());
		SeniorPetChallengeCopyData copyData = levelSet.getCopyRecord(KGameLevelTypeEnum.高级随从挑战副本关卡).seniorPetChallengeCopyData;
		boolean isReset = false;
		if (copyData.monsterMap.isEmpty()) {
			isReset = true;
		}
		for (KLevelTemplate levelTemp : seniorPetChallengeCopyLevelMap.values()) {
			if (!copyData.monsterMap.containsKey(levelTemp.getLevelId())) {
				isReset = true;
				break;
			}
		}
		if (isReset) {
			resetAllLevelMonster(role, levelSet, copyData);
		}
	}

	public void sendCopyData(KRole role) {
		checkAndResetSeniorPetChallengeCopyData(role);
		// 获取关卡记录
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
		KGameLevelTypeEnum levelTypeEnum = KGameLevelTypeEnum.高级随从挑战副本关卡;

		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_GET_SENIOR_PET_CHALLENGE_COPY_DATA);
		KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.高级随从挑战副本关卡);
		int nowLevelId = firstLevel.getLevelId();
		if (record != null && record.seniorPetChallengeCopyData != null) {
			record.seniorPetChallengeCopyData.checkAndResetSeniorPetChallengeCopyData(true);
			if (record.seniorPetChallengeCopyData.nowLevelId == 0) {
				nowLevelId = firstLevel.getLevelId();
			} else {
				nowLevelId = record.seniorPetChallengeCopyData.nowLevelId;
			}
		}
		KLevelTemplate nowLevelTemplate = seniorPetChallengeCopyLevelMap.get(nowLevelId);
		sendMsg.writeInt(nowLevelId);
		sendMsg.writeByte(KGameLevelTypeEnum.高级随从挑战副本关卡.levelType);
		sendMsg.writeInt(nowLevelTemplate.getEnterCondition().getUseStamina());
		sendMsg.writeByte(seniorPetChallengeCopyLevelMap.size());
		for (KLevelTemplate levelTemplate : seniorPetChallengeCopyLevelMap.values()) {
			sendMsg.writeInt(levelTemplate.getLevelId());
			sendMsg.writeUtf8String(levelTemplate.getLevelName());
			sendMsg.writeUtf8String(levelTemplate.getDesc());
			sendMsg.writeInt(levelTemplate.getLevelNumber());

			List<Integer> bossIconList = new ArrayList<Integer>();
			int bossHeadId = 0;
			for (Integer monTemplateId : record.seniorPetChallengeCopyData.getLevelMonsterInfo(levelTemplate.getLevelId()).keySet()) {
				if (monsterHeadIdMap.containsKey(monTemplateId)) {
					if (bossMonsterTempIdSet.contains(monTemplateId)) {
						bossHeadId = monsterHeadIdMap.get(monTemplateId);
					} else {
						bossIconList.add(monsterHeadIdMap.get(monTemplateId));
					}
				}
			}
			bossIconList.add(0, bossHeadId);
			sendMsg.writeInt(bossIconList.size());
			for (Integer headId : bossIconList) {
				sendMsg.writeInt(headId);
			}
			sendMsg.writeInt(levelTemplate.getFightPower());

			levelTemplate.getReward().getShowRewardData(role.getJob()).packMsg(sendMsg);

			List<Byte> itemDropRateList = levelTemplate.getReward().getShowRewardDataDropRate(role.getJob());
			if (itemDropRateList != null) {
				sendMsg.writeByte(itemDropRateList.size());
				for (byte rate : itemDropRateList) {
					sendMsg.writeByte(rate);
				}
			} else {
				sendMsg.writeByte(0);
			}
		}
		sendMsg.writeInt(getRestChallengeCount(role));
		sendMsg.writeInt(free_challenge_count);
		sendMsg.writeInt(getRestVIPSaodangCount(role));
		sendMsg.writeInt(getTotalVIPSaodangCount(role));
		role.sendMsg(sendMsg);
	}

	public int getRestChallengeCount(KRole role) {
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
		KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.高级随从挑战副本关卡);
		int restCount = free_challenge_count;
		if (record != null && record.seniorPetChallengeCopyData != null) {
			restCount = restCount - record.seniorPetChallengeCopyData.challengeCount;
			if (restCount < 0) {
				restCount = 0;
			}
		}
		return restCount;
	}

	public int getRestVIPSaodangCount(KRole role) {
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
		KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.高级随从挑战副本关卡);
		VIPLevelData vipData = KSupportFactory.getVIPModuleSupport().getVIPLevelData(role.getId());
		int restCount = vipData.petTestSweepCount;
		if (record != null && record.seniorPetChallengeCopyData != null) {
			restCount = restCount - record.seniorPetChallengeCopyData.saodangCount;
			if (restCount < 0) {
				restCount = 0;
			}
		}
		return restCount;
	}

	public int getTotalVIPSaodangCount(KRole role) {
		VIPLevelData vipData = KSupportFactory.getVIPModuleSupport().getVIPLevelData(role.getId());
		int restCount = vipData.petTestSweepCount;
		return restCount;
	}

	public KActionResult processPlayerRoleJoinLevel(KRole role, int levelId) {
		if (role == null) {
			_LOGGER.error("角色进入高级随从试炼副本关卡失败，角色为null，关卡ID：" + levelId);
			// KDialogService.sendUprisingDialog(role.getId(),
			// LevelTips.getTipsJoinLevelFailed());
			return new KActionResult(false, LevelTips.getTipsJoinLevelFailed());
		}
		KLevelTemplate level = seniorPetChallengeCopyLevelMap.get(levelId);
		if (level == null) {
			_LOGGER.error("角色进入高级随从挑战副本关卡失败，找不到对应的关卡。角色id:" + role.getId() + "，关卡ID：" + levelId);
			KDialogService.sendUprisingDialog(role.getId(), LevelTips.getTipsJoinLevelFailed());
			return new KActionResult(false, LevelTips.getTipsJoinLevelFailed());
		}
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
		KGameLevelRecord ptRecord = levelSet.getCopyRecord(KGameLevelTypeEnum.高级随从挑战副本关卡);
		SeniorPetChallengeCopyData copyData = ptRecord.seniorPetChallengeCopyData;

		KActionResult joinResult = checkPlayerRoleCanJoinGameLevel(role, levelSet, level, true, true);
		if (joinResult.success) {
			// 当前进入为普通战场模式，取得关卡第一层战场的数据，并通知战斗模块
			List<KGameBattlefield> battlefields = new ArrayList<KGameBattlefield>();
			battlefields.add(constructBattlefield(role, level, levelSet, copyData));
			if (battlefields.isEmpty()) {
				_LOGGER.error("角色进入关卡失败，找不到对应的第一层战场。角色id:" + role.getId() + "，关卡ID：" + levelId);
				KGameLevelModuleExtension.getManager().sendJoinNormalGameLevelTipsMessage(role, (short) -1, LevelTips.getTipsJoinLevelFailed(), false, null);
				return new KActionResult(false, LevelTips.getTipsJoinLevelFailed());
			}

			if (level.getLevelId() == firstLevel.getLevelId()) {
				copyData.recordResetSeniorPetChallengeCopy();
			}

			// 通知战斗模块，角色进入战场
			List<Animation> animation = new ArrayList<Animation>();
			for (FightEventListener listener : KGameLevelModuleExtension.getManager().getFightEventListenerList()) {
				listener.notifyBattle(role, battlefields, animation);
			}
			// 活跃度
			KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.随从试炼);
		}

		return joinResult;
	}

	private KActionResult checkPlayerRoleCanJoinGameLevel(KRole role, KGameLevelSet levelSet, KLevelTemplate level, boolean isNeedCheckCondition, boolean isSendDialog) {
		KActionResult result = new KActionResult();
		String tips = "";
		KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.高级随从挑战副本关卡);
		if (isNeedCheckCondition) {
			// 获取角色当前体力值

			if (level.getLevelId() == endLevel.getLevelId() && record.seniorPetChallengeCopyData.isCompleteLastLevel) {
				tips = LevelTips.getTipsTowerCopyEndLevel();
				if (isSendDialog) {
					KGameLevelModuleExtension.getManager().sendJoinNormalGameLevelTipsMessage(role, (short) -1, tips, false, null);
				}
				result.success = false;
				result.tips = tips;
				return result;
			}

			// 判断关卡是否开放
			int nowLevelId = firstLevel.getLevelId();
			if (record != null && record.seniorPetChallengeCopyData != null) {
				if (record.seniorPetChallengeCopyData.nowLevelId == 0) {
					nowLevelId = firstLevel.getLevelId();
				} else {
					nowLevelId = record.seniorPetChallengeCopyData.nowLevelId;
				}
			}
			if (level.getLevelId() != nowLevelId) {
				tips = LevelTips.getTipsLevelNotOpen();
				if (isSendDialog) {
					KGameLevelModuleExtension.getManager().sendJoinNormalGameLevelTipsMessage(role, (short) -1, tips, false, null);
				}
				result.success = false;
				result.tips = tips;
				return result;
			}
			// else if (level.getLevelId() < nextTargetLevel.getLevelId()) {
			// tips = LevelTips.getTipsTowerCopyLevelIsComplete();
			// if (isSendDialog) {
			// KGameLevelModuleExtension.getManager().sendJoinNormalGameLevelTipsMessage(role,
			// (short) -1, tips, false, null);
			// }
			// result.success = false;
			// result.tips = tips;
			// return result;
			// }

			if (level.getLevelId() == firstLevel.getLevelId()) {
				// 进入次数
				if (getRestChallengeCount(role) <= 0) {
					tips = LevelTips.getTipsJoinEliteCopyFailedCountNotEnough();
					if (isSendDialog) {
						KGameLevelModuleExtension.getManager().sendJoinNormalGameLevelTipsMessage(role, (short) -1, tips, false, null);
					}
					result.success = false;
					result.tips = tips;
					return result;
				}

				// 获取角色当前体力值
				int roleStamina = role.getPhyPower();

				// 检测体力
				int useStamina = level.getEnterCondition().getUseStamina();

				if (useStamina > 0 && roleStamina < useStamina) {
					tips = LevelTips.getTipsJoinLevelFailedWhileStaminaNotEnough();
					if (isSendDialog) {
						KGameLevelModuleExtension.getManager().checkAndSendPhyPowerNotEnoughDialog(role);
					}
					result.success = false;
					result.tips = tips;
					return result;
				}
			}
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
		SeniorPetChallengeCopyData copyData = record.seniorPetChallengeCopyData;

		boolean hasReward = false;
		BaseRewardData rewardData = null;// caculateLevelReward(role, level);
		String tips = "";
		if (result.isWin()) {
			rewardData = caculateLevelReward(role, level, levelSet);
			hasReward = true;
			if (level.getLevelId() == endLevel.getLevelId()) {
				tips = LevelTips.getTipsPetChallengeCopyLastBattleWin();
			} else {
				tips = LevelTips.getTipsPetChallengeCopyBattleWin(level.getLevelNumber());
			}

			if (level.getLevelId() == firstLevel.getLevelId()) {
				copyData.recordChallengeSeniorPetChallengeCopy();
				// 修改角色体力,减少值为该关卡的消耗体力值
				int useStamina = level.getEnterCondition().getUseStamina();
				String reason = "高级随从试炼关卡扣除体力";
				KSupportFactory.getRoleModuleSupport().decreasePhyPower(role, useStamina, reason);
			}
		} else {
			if (level.getLevelId() > firstLevel.getLevelId() && level.getEnterCondition().getFrontLevelId() > 0) {
				KLevelTemplate caculateLevel = seniorPetChallengeCopyLevelMap.get(level.getEnterCondition().getFrontLevelId());
				rewardData = record.seniorPetChallengeCopyData.completeLevelReward;
				hasReward = true;
				tips = LevelTips.getTipsPetChallengeCopyBattleFailed(LevelTips.getTipsPetChallengeCopyBattleFailedReward());
			} else {
				tips = LevelTips.getTipsPetChallengeCopyBattleFailed("");
			}
		}
		if (rewardData == null) {
			hasReward = false;
		}

		if (result.isWin()) {
			if (level.getHinderGameLevelList().size() > 0) {
				KLevelTemplate nextLevel = level.getHinderGameLevelList().get(0);
				long restHp = result.getRoleCurrentHp();
				long restPetHp = result.getPetCurrentHp();
				copyData.recordCompleteSeniorPetChallengeCopy(nextLevel.getLevelId(), restHp, restPetHp);
				completeOrUpdateCopyInfo(role, nextLevel.getLevelId());
			} else if (level.getLevelId() == endLevel.getLevelId()) {
				copyData.recordCompleteSeniorPetChallengeCopyLastLevel(endLevel.getLevelId());
			}
		}

		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_SHOW_COMPLETE_SENIOR_PET_CHALLENGE_COPY_BATTLE_RESULT);
		sendMsg.writeInt(level.getLevelId());
		sendMsg.writeByte(level.getLevelType().levelType);
		sendMsg.writeBoolean(result.isWin());
		sendMsg.writeBoolean(hasReward);
		if (hasReward) {
			rewardData.packMsg(sendMsg);
		}
		sendMsg.writeUtf8String(tips);
		role.sendMsg(sendMsg);
	}

	public void completeOrUpdateCopyInfo(KRole role, int nextLevelId) {
		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_UPDATE_SENIOR_PET_CHALLENGE_COPY_DATA);
		sendMsg.writeInt(nextLevelId);
		sendMsg.writeInt(getRestChallengeCount(role));
		sendMsg.writeInt(free_challenge_count);
		sendMsg.writeInt(getRestVIPSaodangCount(role));
		role.sendMsg(sendMsg);
	}

	public void updateCopyInfo(KRole role) {
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
		KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.高级随从挑战副本关卡);
		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_UPDATE_SENIOR_PET_CHALLENGE_COPY_DATA);
		sendMsg.writeInt(record.seniorPetChallengeCopyData.nowLevelId);
		sendMsg.writeInt(getRestChallengeCount(role));
		sendMsg.writeInt(free_challenge_count);
		sendMsg.writeInt(getRestVIPSaodangCount(role));
		role.sendMsg(sendMsg);
	}

	public void confirmCompleteAndExitLevel(KRole role, int levelId) {
		// 获取关卡记录
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());

		KLevelTemplate level = seniorPetChallengeCopyLevelMap.get(levelId);

		KGameLevelRecord record = levelSet.getCopyRecord(level.getLevelType());
		SeniorPetChallengeCopyData copyData = record.seniorPetChallengeCopyData;

		int nowLevelId = 0;
		if (record != null && record.seniorPetChallengeCopyData != null) {
			nowLevelId = record.seniorPetChallengeCopyData.nowLevelId;
		}
		if (nowLevelId <= firstLevel.getLevelId()) {
			return;
		} else {
			KLevelTemplate caculateLevel;
			if (nowLevelId > levelId) {
				caculateLevel = level;
			} else {
				caculateLevel = seniorPetChallengeCopyLevelMap.get(level.getEnterCondition().getFrontLevelId());
			}
			BaseRewardData rewardData = record.seniorPetChallengeCopyData.completeLevelReward;
			if (!rewardData.sendReward(role, PresentPointTypeEnum.高级随从挑战副本奖励)) {
				BaseMailContent mainContent = new BaseMailContent(LevelTips.getTipsSendMailItemForBagFullTitle(), LevelTips.getTipsSendMailItemForBagFull(), null, null);
				KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), mainContent, rewardData, PresentPointTypeEnum.高级随从挑战副本奖励);

				KDialogService.sendDataUprisingDialog(role, RewardTips.背包已满奖励通过邮件发送);
			}
		}

		copyData.recordResetSeniorPetChallengeCopy();
		resetAllLevelMonster(role, levelSet, copyData);

		// completeOrUpdateCopyInfo(role, firstLevel.getLevelId());
		sendCopyData(role);
	}

	public void resetCopyAndGetReward(KRole role) {
		// 获取关卡记录
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
		KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.高级随从挑战副本关卡);
		SeniorPetChallengeCopyData copyData = record.seniorPetChallengeCopyData;
		int nowLevelId = 0;
		if (record != null && record.seniorPetChallengeCopyData != null) {
			nowLevelId = record.seniorPetChallengeCopyData.nowLevelId;
		}
		if (nowLevelId <= firstLevel.getLevelId()) {
			KDialogService.sendDataUprisingDialog(role, LevelTips.getTipsRestCopyLevelFailedWhileNotFinished());
			return;
		}
		KLevelTemplate level = seniorPetChallengeCopyLevelMap.get((seniorPetChallengeCopyLevelMap.get(nowLevelId).getEnterCondition().getFrontLevelId()));

		BaseRewardData rewardData = record.seniorPetChallengeCopyData.completeLevelReward;
		if (!rewardData.sendReward(role, PresentPointTypeEnum.随从挑战副本奖励)) {
			BaseMailContent mainContent = new BaseMailContent(LevelTips.getTipsSendMailItemForBagFullTitle(), LevelTips.getTipsSendMailItemForBagFull(), null, null);
			KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), mainContent, rewardData, PresentPointTypeEnum.爬塔副本奖励);

			KDialogService.sendUprisingDialog(role, RewardTips.背包已满奖励通过邮件发送);
		} else {
			KDialogService.sendDataUprisingDialog(role, rewardData.dataUprisingTips);
		}

		copyData.recordResetSeniorPetChallengeCopy();
		resetAllLevelMonster(role, levelSet, copyData);

		// completeOrUpdateCopyInfo(role, firstLevel.getLevelId());
		sendCopyData(role);

		KDialogService.sendNullDialog(role);
	}

	public BaseRewardData caculateLevelReward(KRole role, KLevelTemplate level, KGameLevelSet levelSet) {
		KGameLevelRecord record = levelSet.getCopyRecord(level.getLevelType());

		if (record.seniorPetChallengeCopyData == null) {
			record.seniorPetChallengeCopyData = new SeniorPetChallengeCopyData(record);
			record.notifyDB();
		}
		SeniorPetChallengeCopyData copyData = record.seniorPetChallengeCopyData;

		// 检测是否有限时产出活动
		boolean isCopyActivityPrice = false;
		float expRate = 1, goldRate = 1, potentialRate = 1;
		int itemMultiple = 1;
		// if (level.getLevelType() == KGameLevelTypeEnum.技术副本关卡 ||
		// level.getLevelType() == KGameLevelTypeEnum.精英副本关卡) {
		// TimeLimieProduceActivity activity =
		// KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.精英副本技术副本活动);
		//
		// if (activity != null && activity.isActivityTakeEffectNow()) {
		// isCopyActivityPrice = true;
		// expRate = activity.expRate;
		// goldRate = activity.goldRate;
		// potentialRate = activity.potentialRate;
		// if (activity.isDropItemDouble) {
		// int mRate = UtilTool.random(0, UtilTool.TEN_THOUSAND_RATIO_UNIT);
		// if (mRate >= (UtilTool.TEN_THOUSAND_RATIO_UNIT -
		// activity.itemMultipleRate)) {
		// itemMultiple = activity.itemMultiple;
		// }
		// }
		// KSupportFactory.getMailModuleSupport().sendSimpleMailBySystem(role.getId(),
		// activity.mailTitle, activity.mailContent);
		// }
		// }

		BaseRewardData baseReward;
		List<AttValueStruct> attList = new ArrayList<AttValueStruct>();
		List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();
		List<ItemCountStruct> itemRewardList = new ArrayList<ItemCountStruct>();
		KLevelReward reward = level.getReward();
		// 计算关卡数值奖励

		for (AttValueStruct attReward : reward.probableReward.get(role.getJob()).attList) {
			if (attReward.roleAttType == KGameAttrType.EXPERIENCE) {
				KGameAttrType attrType = attReward.roleAttType;
				int baseValue = attReward.addValue;
				int value = (int) (baseValue * expRate);

				attList.add(new AttValueStruct(attrType, value, 0));
			}
		}
		// 处理关卡货币奖励

		for (KCurrencyCountStruct attReward : reward.probableReward.get(role.getJob()).moneyList) {
			long baseValue = attReward.currencyCount;
			if (attReward.currencyType == KCurrencyTypeEnum.GOLD) {
				baseValue = (long) (baseValue * goldRate);
			} else if (attReward.currencyType == KCurrencyTypeEnum.POTENTIAL) {
				baseValue = (long) (baseValue * potentialRate);
			}
			moneyList.add(new KCurrencyCountStruct(attReward.currencyType, baseValue));
		}

		ItemModuleSupport itemSupport = KSupportFactory.getItemModuleSupport();

		// 计算获得道具列表
		// 再处理关卡常规掉落道具
		if (!reward.getAllItemReward(role.getJob()).isEmpty()) {
			List<ItemCountStruct> itemList = reward.caculateItemReward(role.getJob(), itemMultiple);
			itemRewardList.addAll(itemList);
		}

		// 计算掉落池道具
		List<ItemCountStruct> dropPoolItems = reward.caculateDropPoolItems(itemMultiple);
		itemRewardList.addAll(dropPoolItems);

		// 处理数值和货币奖励
		// baseReward = new BaseRewardData(attList, moneyList, itemRewardList,
		// Collections.<Integer> emptyList(), Collections.<Integer>
		// emptyList());

		attList.addAll(record.seniorPetChallengeCopyData.completeLevelReward.attList);
		moneyList.addAll(record.seniorPetChallengeCopyData.completeLevelReward.moneyList);
		itemRewardList.addAll(record.seniorPetChallengeCopyData.completeLevelReward.itemStructs);

		attList = AttValueStruct.mergeCountStructs(attList);
		moneyList = KCurrencyCountStruct.mergeCurrencyCountStructs(moneyList);
		itemRewardList = ItemCountStruct.mergeItemCountStructs(itemRewardList);

		baseReward = new BaseRewardData(attList, moneyList, itemRewardList, null, null);

		copyData.recordUpdateSeniorPetChallengeCopyReward(baseReward);

		return baseReward;
	}

	public void processSaodangCopy(KRole role) {
		if (role == null) {
			_LOGGER.error("角色扫荡高级随从试炼副本关卡失败，角色为null。");
			sendSaodangFailedMsg(role);
			KDialogService.sendUprisingDialog(role.getId(), GlobalTips.服务器繁忙请稍候再试);
			return;
		}

		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
		KGameLevelRecord ptRecord = levelSet.getCopyRecord(KGameLevelTypeEnum.高级随从挑战副本关卡);
		SeniorPetChallengeCopyData copyData = ptRecord.seniorPetChallengeCopyData;
		if (!ptRecord.seniorPetChallengeCopyData.isPassCopy) {
			sendSaodangFailedMsg(role);
			KDialogService.sendUprisingDialog(role.getId(), LevelTips.getTipsCannotSaodang1());
			return;
		}

		if (ptRecord.seniorPetChallengeCopyData.nowLevelId == endLevel.getLevelId() && ptRecord.seniorPetChallengeCopyData.isCompleteLastLevel) {
			sendSaodangFailedMsg(role);
			KDialogService.sendUprisingDialog(role.getId(), LevelTips.getTipsTowerCopyEndLevel());
			return;
		}
		// TODO VIP扫荡次数检测
		VIPLevelData vipData = KSupportFactory.getVIPModuleSupport().getVIPLevelData(role.getId());
		if (vipData.petTestSweepCount == 0) {
			// VIP等级不足
			sendSaodangFailedMsg(role);
			KDialogService.sendUprisingDialog(role, LevelTips.getTipsSaodangVipLvNotEnough(saodangVipLevel));
			return;
		}
		if (ptRecord.seniorPetChallengeCopyData.saodangCount >= vipData.petTestSweepCount) {
			sendSaodangFailedMsg(role);
			KDialogService.sendUprisingDialog(role, LevelTips.getTipsSaodangMaxCount(vipData.petTestSweepCount));
			return;
		}
		// 检测体力
		if (ptRecord.seniorPetChallengeCopyData.nowLevelId == firstLevel.getLevelId()) {
			// 获取角色当前体力值
			int roleStamina = role.getPhyPower();
			int useStamina = firstLevel.getEnterCondition().getUseStamina();

			if (useStamina > 0 && roleStamina < useStamina) {
				sendSaodangFailedMsg(role);
				KGameLevelModuleExtension.getManager().checkAndSendPhyPowerNotEnoughDialog(role);
				return;
			}
			KSupportFactory.getRoleModuleSupport().decreasePhyPower(role, useStamina, "高级随从挑战关卡扣除体力");
		}

		BaseRewardData rewardData = caculateSaodangLevelReward(role, levelSet);

		if (!rewardData.sendReward(role, PresentPointTypeEnum.高级随从挑战副本奖励)) {
			BaseMailContent mainContent = new BaseMailContent(LevelTips.getTipsSendMailItemForBagFullTitle(), LevelTips.getTipsSendMailItemForBagFull(), null, null);
			KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), mainContent, rewardData, PresentPointTypeEnum.高级随从挑战副本奖励);

			KDialogService.sendDataUprisingDialog(role, RewardTips.背包已满奖励通过邮件发送);
		}

		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_VIP_SAODANG_SENIOR_PET_CHALLENGE_COPY);
		sendMsg.writeBoolean(true);
		rewardData.packMsg(sendMsg);
		role.sendMsg(sendMsg);

		copyData.recordSaodangPetChallengeCopy();
		copyData.recordResetSeniorPetChallengeCopy();
		resetAllLevelMonster(role, levelSet, copyData);
		sendCopyData(role);

		KDialogService.sendNullDialog(role);
	}

	public static void sendSaodangFailedMsg(KRole role) {
		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_VIP_SAODANG_SENIOR_PET_CHALLENGE_COPY);
		sendMsg.writeBoolean(false);
		role.sendMsg(sendMsg);
	}

	public BaseRewardData caculateSaodangLevelReward(KRole role, KGameLevelSet levelSet) {
		KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.高级随从挑战副本关卡);

		BaseRewardData baseReward;
		List<AttValueStruct> attList = new ArrayList<AttValueStruct>();
		List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();
		List<ItemCountStruct> itemRewardList = new ArrayList<ItemCountStruct>();
		if (record.seniorPetChallengeCopyData.nowLevelId > firstLevel.getLevelId() && record.seniorPetChallengeCopyData.completeLevelReward != null) {
			attList.addAll(record.seniorPetChallengeCopyData.completeLevelReward.attList);
			moneyList.addAll(record.seniorPetChallengeCopyData.completeLevelReward.moneyList);
			itemRewardList.addAll(record.seniorPetChallengeCopyData.completeLevelReward.itemStructs);
		}

		// 检测是否有限时产出活动
		boolean isCopyActivityPrice = false;
		float expRate = 1, goldRate = 1, potentialRate = 1;
		int itemMultiple = 1;

		for (int levelId = record.seniorPetChallengeCopyData.nowLevelId; levelId <= endLevel.getLevelId(); levelId++) {
			KLevelReward reward = seniorPetChallengeCopyLevelMap.get(levelId).getReward();

			for (AttValueStruct attReward : reward.probableReward.get(role.getJob()).attList) {
				if (attReward.roleAttType == KGameAttrType.EXPERIENCE) {
					KGameAttrType attrType = attReward.roleAttType;
					int baseValue = attReward.addValue;
					int value = (int) (baseValue * expRate);

					attList.add(new AttValueStruct(attrType, value, 0));
				}
			}
			// 处理关卡货币奖励

			for (KCurrencyCountStruct attReward : reward.probableReward.get(role.getJob()).moneyList) {
				long baseValue = attReward.currencyCount;
				if (attReward.currencyType == KCurrencyTypeEnum.GOLD) {
					baseValue = (long) (baseValue * goldRate);
				} else if (attReward.currencyType == KCurrencyTypeEnum.POTENTIAL) {
					baseValue = (long) (baseValue * potentialRate);
				}
				moneyList.add(new KCurrencyCountStruct(attReward.currencyType, baseValue));
			}

			ItemModuleSupport itemSupport = KSupportFactory.getItemModuleSupport();

			// 计算获得道具列表
			// 再处理关卡常规掉落道具
			if (!reward.getAllItemReward(role.getJob()).isEmpty()) {
				List<ItemCountStruct> itemList = reward.caculateItemReward(role.getJob(), itemMultiple);
				itemRewardList.addAll(itemList);
			}

			// 计算掉落池道具
			List<ItemCountStruct> dropPoolItems = reward.caculateDropPoolItems(itemMultiple);
			itemRewardList.addAll(dropPoolItems);

			// 处理数值和货币奖励
			// baseReward = new BaseRewardData(attList, moneyList,
			// itemRewardList,
			// Collections.<Integer> emptyList(), Collections.<Integer>
			// emptyList());

			attList.addAll(record.seniorPetChallengeCopyData.completeLevelReward.attList);
			moneyList.addAll(record.seniorPetChallengeCopyData.completeLevelReward.moneyList);
			itemRewardList.addAll(record.seniorPetChallengeCopyData.completeLevelReward.itemStructs);
		}

		attList = AttValueStruct.mergeCountStructs(attList);
		moneyList = KCurrencyCountStruct.mergeCurrencyCountStructs(moneyList);
		itemRewardList = ItemCountStruct.mergeItemCountStructs(itemRewardList);

		baseReward = new BaseRewardData(attList, moneyList, itemRewardList, null, null);

		return baseReward;
	}

	private void getFrontLevelList(KLevelTemplate level, List<KLevelTemplate> caculateLevelList) {
		caculateLevelList.add(level);
		int frontLevelId = level.getEnterCondition().getFrontLevelId();
		if (frontLevelId > 0 && seniorPetChallengeCopyLevelMap.containsKey(frontLevelId)) {
			KLevelTemplate frontLevel = seniorPetChallengeCopyLevelMap.get(frontLevelId);
			getFrontLevelList(frontLevel, caculateLevelList);
		} else {
			return;
		}
	}

	public static class MonsterInfoData {
		public KMonstTemplate monTemp;
		public int count;
		public int weight;

		public MonsterInfoData(KMonstTemplate monTemp, int count, int weight) {
			this.monTemp = monTemp;
			this.count = count;
			this.weight = weight;
		}
	}

	public static class SeniorPetChallengeCopyCombatRoleSideHpUpdater implements ICombatRoleSideHpUpdater {

		@Override
		public KCombatType getCombatTypeResponse() {
			return KCombatType.PET_CHALLENGE_SENIOR_COPY;
		}

		@Override
		public boolean handleRoleHpUpdate() {
			return true;
		}

		@Override
		public long getRoleHp(long roleId) {
			long roleHp = 0;
			KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (role != null) {
				roleHp = role.getMaxHp();
			}
			KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(roleId);
			KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.高级随从挑战副本关卡);
			if (record != null && record.seniorPetChallengeCopyData != null) {
				if (record.seniorPetChallengeCopyData.restHp > 0) {
					roleHp = record.seniorPetChallengeCopyData.restHp;
				}
			}
			return roleHp;
		}

		@Override
		public boolean handlePetHpUpdate() {
			return true;
		}

		@Override
		public long getPetHp(long roleId, long petId) {
			long petHp = 0;

			KPet pet = KSupportFactory.getPetModuleSupport().getFightingPet(roleId);
			if (pet != null) {
				petHp = pet.getMaxHp();
			}
			KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(roleId);
			KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.高级随从挑战副本关卡);
			if (record != null && record.seniorPetChallengeCopyData != null) {
				if (record.seniorPetChallengeCopyData.restPetHp > 0) {
					petHp = record.seniorPetChallengeCopyData.restPetHp;
				}
			}
			return petHp;
		}
	}
}
