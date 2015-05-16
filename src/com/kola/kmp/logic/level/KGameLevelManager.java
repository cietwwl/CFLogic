package com.kola.kmp.logic.level;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import jxl.read.biff.BiffException;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.exception.KGameServerException;
import com.koala.game.gameserver.KGameServer;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.StringUtil;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.flowdata.FlowDataModuleFactory;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kgame.cache.level.impl.KGameLevelModule;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.actionrecord.KActionRecorder;
import com.kola.kmp.logic.actionrecord.KActionType;
import com.kola.kmp.logic.activity.goldact.KGoldActivityManager;
import com.kola.kmp.logic.activity.newglodact.KNewGoldActivityManager;
import com.kola.kmp.logic.chat.KWordBroadcastType;
import com.kola.kmp.logic.flow.KRoleAttrModifyType;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.item.KItem;
import com.kola.kmp.logic.item.KItemMsgPackCenter;
import com.kola.kmp.logic.level.KGameLevelDropPool.KDropGroup;
import com.kola.kmp.logic.level.KGameLevelRecord.PlayerRoleGamelevelData;
import com.kola.kmp.logic.level.KLevelReward.LotteryGroup;
import com.kola.kmp.logic.level.KLevelReward.NormalCurrencyRewardTemplate;
import com.kola.kmp.logic.level.KLevelReward.NormalItemRewardTemplate;
import com.kola.kmp.logic.level.copys.KCopyManager;
import com.kola.kmp.logic.level.copys.KFriendCopyManager;
import com.kola.kmp.logic.level.copys.KPetChallengeCopyManager;
import com.kola.kmp.logic.level.copys.KSeniorPetChallengeCopyManager;
import com.kola.kmp.logic.level.copys.KTowerCopyManager;
//import com.kola.kmp.logic.level.copys.KCopyManager.CopyActivityConfig;
import com.kola.kmp.logic.level.gamestory.Animation;
import com.kola.kmp.logic.level.gamestory.AnimationManager;
import com.kola.kmp.logic.level.petcopy.KPetCopyBattlefield;
import com.kola.kmp.logic.level.petcopy.KPetCopyLevelTemplate;
import com.kola.kmp.logic.level.petcopy.KPetCopyManager;
import com.kola.kmp.logic.level.tower.KTowerDataManager;
import com.kola.kmp.logic.map.KMapRoleEventListener;
import com.kola.kmp.logic.mission.KMissionCompleteRecordSet;
import com.kola.kmp.logic.mission.KMissionModuleExtension;
import com.kola.kmp.logic.npc.dialog.KDialogButton;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.npc.message.KNPCOrderMsg;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KColorFunEnum;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KColorFunEnum.KColor;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KGameBattlefieldTypeEnum;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.other.KGangTecTypeEnum;
import com.kola.kmp.logic.other.KLimitTimeProduceActivityTypeEnum;
import com.kola.kmp.logic.other.KNPCOrderEnum;
import com.kola.kmp.logic.other.KNoviceGuideStepEnum;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.TimeLimitActivityDataManager.TimeLimieProduceActivity;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.role.KRoleModuleConfig;
import com.kola.kmp.logic.shop.PhyPowerShopCenter;
import com.kola.kmp.logic.support.ItemModuleSupport;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.MissionModuleSupport;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_AddItem;
import com.kola.kmp.logic.util.text.HyperTextTool;
import com.kola.kmp.logic.util.tips.GambleTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.LevelTips;
import com.kola.kmp.logic.util.tips.RewardTips;
import com.kola.kmp.logic.util.tips.TimeLimitProducActivityTips;
import com.kola.kmp.logic.vip.KVIPDataManager;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;
import com.kola.kmp.protocol.level.KLevelProtocol;

public class KGameLevelManager {

	private static final KGameLogger _LOGGER = KGameLogger.getLogger(KGameLevelManager.class);

	public Map<Integer, KGameScenario> allKGameScenario = new LinkedHashMap<Integer, KGameScenario>();

	public Map<Integer, KLevelTemplate> allKGameLevel = new LinkedHashMap<Integer, KLevelTemplate>();

	public Map<Integer, List<KLevelTemplate>> allKGameLevelByRoleLv = new LinkedHashMap<Integer, List<KLevelTemplate>>();

	public Map<Integer, KGameBattlefield> allKGameBattlefield = new LinkedHashMap<Integer, KGameBattlefield>();

	private BattlefieldIdGenerator battlefieldIdGenerator;

	public KLevelTemplate firstNormalGameLevel;

	// public KLevelTemplate noviceGuideGameLevel;

	public KGameBattlefield firstNoviceGuideBattle;

	public KLevelReward firstNoviceGuideBattleReward;

	private List<FightEventListener> fightEventListenerList;

	public List<FightEventListener> getFightEventListenerList() {
		return fightEventListenerList;
	}

	public static final int lotteryVipLevel = 1;

	public ConcurrentHashMap<Long, CompleteGameLevelTempRecord> allCompleteGameLevelTempRecord = new ConcurrentHashMap<Long, CompleteGameLevelTempRecord>();

	private KCopyManager copyManager;

	private KFriendCopyManager friendCopyManager;

	private KPetCopyManager petCopyManager;

	private KTowerCopyManager towerCopyManager;

	private KPetChallengeCopyManager petChallengeCopyManager;

	private KSeniorPetChallengeCopyManager seniorPetChallengeCopyManager;

	private Map<Integer, KGameBattlefield> worldBossBattlefields = new HashMap<Integer, KGameBattlefield>();

	private Map<Integer, KGameBattlefield> familyWarBattlefields = new HashMap<Integer, KGameBattlefield>();

	// private Map<Integer, KGameBattlefield> noviceGuideBattlefields = new
	// HashMap<Integer, KGameBattlefield>();

	/**
	 * 初始化关卡剧本模块所有数据
	 * 
	 * @param configPath
	 * @throws Exception
	 */
	public void init(String configPath) throws Exception {
		battlefieldIdGenerator = new BattlefieldIdGenerator(1);

		Document doc = XmlUtil.openXml(configPath);
		Element root = doc.getRootElement();

		List<Element> listenerElementList = root.getChild("fightEventListener").getChildren("listener");
		fightEventListenerList = new LinkedList<FightEventListener>();
		for (Element listenerElement : listenerElementList) {
			String classpath = listenerElement.getAttributeValue("classPath");
			int id = Integer.parseInt(listenerElement.getAttributeValue("id"));

			fightEventListenerList.add((FightEventListener) (Class.forName(classpath).newInstance()));
		}
		String lotteryExcelFilePath = root.getChildTextTrim("lotteryExcelFilePath");

		String xlsConfigPath = root.getChildText("levelDataExcelFilePath");

		// String copyActivityTimeStr = root.getChild("copyActivity")
		// .getAttributeValue("time");
		// int priceMultiple = Integer.parseInt(root.getChild("copyActivity")
		// .getAttributeValue("multiple"));
		// boolean isOpen = (root.getChild("copyActivity")
		// .getAttributeValue("isOpen")).equals("true");
		//
		// CopyActivityConfig.init(copyActivityTimeStr, priceMultiple, isOpen);

		KCopyManager.free_challenge_count = Integer.parseInt(root.getChildText("eliteCopyFreeCount"));

		loadScenarioExcelData(xlsConfigPath);

		loadLotteryConfig(xlsConfigPath);

		copyManager = new KCopyManager();

		copyManager.init(xlsConfigPath);

		KTowerDataManager.initTowerData(xlsConfigPath);

		friendCopyManager = new KFriendCopyManager();

		friendCopyManager.init(xlsConfigPath);

		petCopyManager = new KPetCopyManager();

		petCopyManager.init(xlsConfigPath);

		towerCopyManager = new KTowerCopyManager();

		towerCopyManager.init(xlsConfigPath);

		petChallengeCopyManager = new KPetChallengeCopyManager();

		petChallengeCopyManager.init(xlsConfigPath);

		seniorPetChallengeCopyManager = new KSeniorPetChallengeCopyManager();

		seniorPetChallengeCopyManager.init(xlsConfigPath);

		loadWorldBossBattlefieldExcelData(xlsConfigPath);

		AnimationManager.getInstance().init(xlsConfigPath);

	}

	private void loadScenarioExcelData(String filePath) throws Exception {
		KGameExcelFile xlsFile = null;
		try {
			xlsFile = new KGameExcelFile(filePath);
		} catch (BiffException e) {
			throw new KGameServerException("读取关卡剧本excel表头发生错误！", e);
		} catch (IOException e) {
			throw new KGameServerException("读取关卡剧本excel表头发生错误！", e);
		}

		if (xlsFile != null) {
			// 初始化掉落池数据
			int dropPoolRowIndex = 5;
			KGameExcelTable dropPoolTable = xlsFile.getTable("掉落方案配置", dropPoolRowIndex);
			KGameExcelRow[] allDropPoolRows = dropPoolTable.getAllDataRows();

			if (allDropPoolRows != null) {
				for (int i = 0; i < allDropPoolRows.length; i++) {
					KDropGroup group = new KDropGroup();
					group.init(allDropPoolRows[i]);
					if (KGameLevelDropPool.dropGroupMap.containsKey(group.groupId)) {
						throw new KGameServerException("初始化表<掉落方案配置>错误" + "设置了重复的掉落组ID=" + group.groupId + "，excel行数：" + allDropPoolRows[i].getIndexInFile());
					}
					KGameLevelDropPool.dropGroupMap.put(group.groupId, group);
				}
			}

			// 初始化剧本数据
			int scenarioDataRowIndex = 5;
			KGameExcelTable scenarioDataTable = xlsFile.getTable("章节总表", scenarioDataRowIndex);
			KGameExcelRow[] allScenarioDataRows = scenarioDataTable.getAllDataRows();

			if (allScenarioDataRows != null) {
				for (int i = 0; i < allScenarioDataRows.length; i++) {
					// // 剧本ID
					int scenarioId = allScenarioDataRows[i].getInt("sceneId");
					// 剧本名字
					String scenarioName = allScenarioDataRows[i].getData("name");
					// 剧本背景图片资源Id
					int scenarioBgResId = allScenarioDataRows[i].getInt("bg_res_id");
					// 剧本描述
					String scenarioDesc = allScenarioDataRows[i].getData("desc");
					// 表示章节数目（从1开始）(add 2014-1-4)
					// short chapterId = (short) (i + 1);

					KGameScenario scenario = new KGameScenario();
					scenario.setScenarioId(scenarioId);
					scenario.setScenarioName(scenarioName);
					scenario.setScenarioBgResId(scenarioBgResId);
					scenario.setChapterId(allScenarioDataRows[i].getShort("scene_icon_id"));
					scenario.setMinRoleLv(allScenarioDataRows[i].getInt("minLv"));
					scenario.setFitRoleLv(allScenarioDataRows[i].getInt("fightpoint"));
					scenario.setFrontScenarioId(allScenarioDataRows[i].getInt("pre_copy"));
					scenario.setMapId(allScenarioDataRows[i].getInt("townInfo"));

					String itemRewardData = allScenarioDataRows[i].getData("all_awardbox");
					if (itemRewardData != null) {
						KGameScenarioReward reward = new KGameScenarioReward();
						reward.initScenarioReward(allScenarioDataRows[i], "all_awardbox");
						scenario.setReward(reward);
					}
					String s_itemRewardData = allScenarioDataRows[i].getData("all_s_reward");
					if (s_itemRewardData != null) {
						KGameScenarioReward reward = new KGameScenarioReward();
						reward.initScenarioReward(allScenarioDataRows[i], "all_s_reward");
						scenario.setS_reward(reward);
					}

					this.allKGameScenario.put(scenario.getScenarioId(), scenario);
				}
			}

			// 初始化关卡数据
			int levelDataRowIndex = 5;
			KGameExcelTable levelDataTable = xlsFile.getTable("普通副本", levelDataRowIndex);
			KGameExcelRow[] allLevelDataRows = levelDataTable.getAllDataRows();

			if (allLevelDataRows != null) {
				for (int i = 0; i < allLevelDataRows.length; i++) {
					KLevelTemplate level = new KLevelTemplate();
					byte type = allLevelDataRows[i].getByte("sceneType");
					KGameLevelTypeEnum levelType = KGameLevelTypeEnum.getEnum(type);
					level.init("普通副本", allLevelDataRows[i], levelType);

					this.allKGameLevel.put(level.getLevelId(), level);
					this.allKGameScenario.get(level.getScenarioId()).addKGameLevel(level);
					if (!allKGameLevelByRoleLv.containsKey(level.getEnterCondition().getOpenRoleLevel())) {
						allKGameLevelByRoleLv.put(level.getEnterCondition().getOpenRoleLevel(), new ArrayList<KLevelTemplate>());
					}
					allKGameLevelByRoleLv.get(level.getEnterCondition().getOpenRoleLevel()).add(level);

					if (i == 0) {
						this.firstNormalGameLevel = level;
					}
					// if (levelType == KGameLevelTypeEnum.新手引导关卡) {
					// noviceGuideGameLevel = level;
					// }

					if (level.getEnterCondition().getFrontLevelId() > 0) {
						if (allKGameLevel.containsKey(level.getEnterCondition().getFrontLevelId())) {
							(allKGameLevel.get(level.getEnterCondition().getFrontLevelId()).getHinderGameLevelList()).add(level);
							// System.err.println("00000000000000000000000000000::::"+level.getLevelId());
						}
					}

					// ////////////////////
				}

				for (KGameScenario scenario : this.allKGameScenario.values()) {
					int num = 1;
					for (KLevelTemplate template : scenario.getAllGameLevel()) {
						template.setLevelNumber(num);
						num++;
					}
				}
			}
		}
	}

	/**
	 * 初始化抽奖数据
	 * 
	 * @param xlsPath
	 * @throws Exception
	 */
	private void loadLotteryConfig(String xlsPath) throws Exception {
		KGameExcelFile xlsFile = null;
		try {
			xlsFile = new KGameExcelFile(xlsPath);
		} catch (BiffException e) {
			throw new KGameServerException("读取关卡剧本excel表头发生错误！", e);
		} catch (IOException e) {
			throw new KGameServerException("读取关卡剧本excel表头发生错误！", e);
		}

		if (xlsFile != null) {
			// 初始化抽奖数据
			int lotteryRewardDataRowIndex = 5;
			KGameExcelTable lotteryRewardDataTable = xlsFile.getTable("翻牌奖励", lotteryRewardDataRowIndex);
			KGameExcelRow[] lotteryRewardDataRows = lotteryRewardDataTable.getAllDataRows();
			for (int i = 1; i < lotteryRewardDataRows.length; i++) {
				int levelId = lotteryRewardDataRows[i].getInt("id");

				KLevelTemplate level = allKGameLevel.get(levelId);
				if (level == null) {
					throw new KGameServerException("初始化表<翻牌奖励>的字段<id>错误，找不到对应的关卡：，excel行数：" + lotteryRewardDataRows[i].getIndexInFile() + ",关卡ID：" + levelId);
				}
				level.getReward().initLotteryReward("翻牌奖励", lotteryRewardDataRows[i]);
			}
		}
	}

	private void loadWorldBossBattlefieldExcelData(String filePath) throws Exception {
		KGameExcelFile xlsFile = null;
		try {
			xlsFile = new KGameExcelFile(filePath);
		} catch (BiffException e) {
			throw new KGameServerException("读取关卡剧本excel表头发生错误！", e);
		} catch (IOException e) {
			throw new KGameServerException("读取关卡剧本excel表头发生错误！", e);
		}

		if (xlsFile != null) {
			// 初始化剧本数据
			int scenarioDataRowIndex = 5;
			KGameExcelTable scenarioDataTable = xlsFile.getTable("世界BOSS关卡", scenarioDataRowIndex);
			KGameExcelRow[] allScenarioDataRows = scenarioDataTable.getAllDataRows();

			if (allScenarioDataRows != null) {
				for (int i = 0; i < allScenarioDataRows.length; i++) {
					int levelId = allScenarioDataRows[i].getInt("levelId");
					String battlePath = allScenarioDataRows[i].getData("battle_res_path");
					int musicResId = allScenarioDataRows[i].getInt("music");

					KGameBattlefield battlefield = new KGameBattlefield();
					battlefield.setBattlefieldId(levelId);
					battlefield.setBattlefieldType(KGameBattlefieldTypeEnum.世界BOSS战场);
					battlefield.setBattlefieldResId(0);
					battlefield.setLevelId(levelId);
					battlefield.setBattlefieldSerialNumber(1);
					battlefield.setBgMusicResId(musicResId);
					battlefield.setFirstBattlefield(true);
					battlefield.setLastBattlefield(true);
					battlefield.initBattlefield("世界BOSS关卡", battlePath, 0, allScenarioDataRows[i].getIndexInFile());
					worldBossBattlefields.put(levelId, battlefield);
				}
			}
			// 军团战BOSS战场
			scenarioDataTable = xlsFile.getTable("军团boss", scenarioDataRowIndex);
			allScenarioDataRows = scenarioDataTable.getAllDataRows();

			if (allScenarioDataRows != null) {
				for (int i = 0; i < allScenarioDataRows.length; i++) {
					int levelId = allScenarioDataRows[i].getInt("levelId");
					String battlePath = allScenarioDataRows[i].getData("battle_res_path");
					int musicResId = allScenarioDataRows[i].getInt("music");

					KGameBattlefield battlefield = new KGameBattlefield();
					battlefield.setBattlefieldId(levelId);
					battlefield.setBattlefieldType(KGameBattlefieldTypeEnum.军团战BOSS战场);
					battlefield.setBattlefieldResId(0);
					battlefield.setLevelId(levelId);
					battlefield.setBattlefieldSerialNumber(1);
					battlefield.setBgMusicResId(musicResId);
					battlefield.setFirstBattlefield(true);
					battlefield.setLastBattlefield(true);
					battlefield.initBattlefield("军团boss", battlePath, 0, allScenarioDataRows[i].getIndexInFile());
					familyWarBattlefields.put(levelId, battlefield);
				}
			}

			// 新手引导战场
			scenarioDataTable = xlsFile.getTable("新手引导战场", scenarioDataRowIndex);
			allScenarioDataRows = scenarioDataTable.getAllDataRows();

			if (allScenarioDataRows != null) {
				for (int i = 0; i < allScenarioDataRows.length; i++) {
					int levelId = allScenarioDataRows[i].getInt("levelId");
					String battlePath = allScenarioDataRows[i].getData("battle_res_path");
					int musicResId = allScenarioDataRows[i].getInt("music");

					KGameBattlefield battlefield = new KGameBattlefield();
					battlefield.setBattlefieldId(levelId);
					battlefield.setBattlefieldType(KGameBattlefieldTypeEnum.新手引导战场);
					battlefield.setBattlefieldResId(0);
					battlefield.setLevelId(levelId);
					battlefield.setBattlefieldSerialNumber(1);
					battlefield.setBgMusicResId(musicResId);
					battlefield.setFirstBattlefield(true);
					battlefield.setLastBattlefield(true);
					battlefield.initBattlefield("新手引导战场", battlePath, 0, allScenarioDataRows[i].getIndexInFile());
					this.firstNoviceGuideBattle = battlefield;

					this.firstNoviceGuideBattleReward = new KLevelReward();
					this.firstNoviceGuideBattleReward.initFirstNoviceGuideBattleLevelReward("新手引导战场", allScenarioDataRows[i]);
					// this.firstNoviceGuideBattleReward.initLotteryReward(
					// "新手引导战场", allScenarioDataRows[i]);
				}
			}

			// 新手引导的结算翻牌奖励
			scenarioDataTable = xlsFile.getTable("翻牌奖励", scenarioDataRowIndex);
			allScenarioDataRows = scenarioDataTable.getAllDataRows();

			if (allScenarioDataRows != null) {
				firstNoviceGuideBattleReward.initLotteryReward("翻牌奖励", allScenarioDataRows[0]);
			}
		}
	}

	public void checkInit() throws KGameServerException {
		boolean checkBattlefied = true;
		for (KGameBattlefield battle : allKGameBattlefield.values()) {

			if (battle.getBornPoint() == null) {
				checkBattlefied = false;
				_LOGGER.error("#########  加载levelConfig.xls表<普通副本>的战场xml数据错误，该战场没有设置出生点，xml文件名=" + battle.getBattlePathName());
			}
			if (!battle.isInitOK) {
				_LOGGER.error("#########  加载levelConfig.xls表<普通副本>的战场xml数据错误，该战场初始化失败，xml文件名={}，关卡ID={}", battle.getBattlePathName(), battle.getLevelId());
				checkBattlefied = false;
			}
		}
		if (!checkBattlefied) {
			throw new KGameServerException("#########  加载levelConfig.xls表<普通副本>的战场xml数据错误。");
		}

		for (KGameBattlefield battle : worldBossBattlefields.values()) {
			if (battle.getBornPoint() == null) {
				checkBattlefied = false;
				_LOGGER.error("#########  加载levelConfig.xls表<世界BOSS>的战场xml数据错误，该战场没有设置出生点，xml文件名=" + battle.getBattlePathName());
			}
			if (!battle.isInitOK) {
				_LOGGER.error("#########  加载levelConfig.xls表<世界BOSS>的战场xml数据错误，该战场初始化失败，xml文件名={}，战场ID={}", battle.getBattlePathName(), battle.getBattlefieldId());
				checkBattlefied = false;
			}
		}
		if (!checkBattlefied) {
			throw new KGameServerException("#########  加载levelConfig.xls表<世界BOSS>的战场xml数据错误。");
		}

		for (KGameBattlefield battle : familyWarBattlefields.values()) {
			if (battle.getBornPoint() == null) {
				checkBattlefied = false;
				_LOGGER.error("#########  加载levelConfig.xls表<军团boss>的战场xml数据错误，该战场没有设置出生点，xml文件名=" + battle.getBattlePathName());
			}
			if (!battle.isInitOK) {
				_LOGGER.error("#########  加载levelConfig.xls表<军团boss>的战场xml数据错误，该战场初始化失败，xml文件名={}，战场ID={}", battle.getBattlePathName(), battle.getBattlefieldId());
				checkBattlefied = false;
			}
		}
		if (!checkBattlefied) {
			throw new KGameServerException("#########  加载levelConfig.xls表<军团boss>的战场xml数据错误。");
		}

		if (firstNoviceGuideBattle != null) {
			if (firstNoviceGuideBattle.getBornPoint() == null) {
				checkBattlefied = false;
				_LOGGER.error("#########  加载levelConfig.xls表<新手引导战场>的战场xml数据错误，该战场没有设置出生点，xml文件名=" + firstNoviceGuideBattle.getBattlePathName());
			}
			if (!firstNoviceGuideBattle.isInitOK) {
				_LOGGER.error("#########  加载levelConfig.xls表<新手引导战场>的战场xml数据错误，该战场初始化失败，xml文件名={}，战场ID={}", firstNoviceGuideBattle.getBattlePathName(), firstNoviceGuideBattle.getBattlefieldId());
				checkBattlefied = false;
			}
		} else {
			_LOGGER.error("#########  加载levelConfig.xls表<新手引导战场>的战场xml数据错误，该战场初始化失败，战场实例为NULL");
		}
		if (!checkBattlefied) {
			throw new KGameServerException("#########  加载levelConfig.xls表<新手引导战场>的战场xml数据错误。");
		}
	}

	public void serverStartComplete() throws KGameServerException {
		checkInit();
		copyManager.checkInit();
		petChallengeCopyManager.checkInit();
		seniorPetChallengeCopyManager.checkInit();
		AnimationManager.getInstance().checkInitAnimation();
	}

	/**
	 * 获取战场ID生成器
	 * 
	 * @return
	 */
	public BattlefieldIdGenerator getBattlefieldIdGenerator() {
		return battlefieldIdGenerator;
	}

	/**
	 * 根据剧本ID获取某个剧本
	 * 
	 * @param scenarioId
	 * @return
	 */
	public KGameScenario getKGameScenario(int scenarioId) {
		return allKGameScenario.get(scenarioId);
	}

	/**
	 * 添加一个剧本
	 * 
	 * @param scenario
	 */
	public void addKGameScenario(KGameScenario scenario) {
		allKGameScenario.put(scenario.getScenarioId(), scenario);
	}

	/**
	 * 根据关卡ID获取某个关卡
	 * 
	 * @param levelId
	 * @return
	 */
	public KLevelTemplate getKGameLevel(int levelId) {
		return allKGameLevel.get(levelId);
	}

	/**
	 * 添加一个关卡
	 * 
	 * @param level
	 */
	public void addKGameLevel(KLevelTemplate level) {
		allKGameLevel.put(level.getLevelId(), level);
		if (allKGameScenario.containsKey(level.getScenarioId())) {
			allKGameScenario.get(level.getScenarioId()).addKGameLevel(level);
		}
	}

	/**
	 * 根据战场ID获取某个战斗场景
	 * 
	 * @param levelId
	 * @return
	 */
	public KGameBattlefield getKGameBattlefield(int battlefieldId) {
		return allKGameBattlefield.get(battlefieldId);
	}

	/**
	 * 精英副本管理器
	 * 
	 * @return
	 */
	public KCopyManager getCopyManager() {
		return copyManager;
	}

	/**
	 * 好友副本管理器
	 * 
	 * @return
	 */
	public KFriendCopyManager getFriendCopyManager() {
		return friendCopyManager;
	}

	public KPetCopyManager getPetCopyManager() {
		return petCopyManager;
	}

	public KTowerCopyManager getTowerCopyManager() {
		return towerCopyManager;
	}

	public KPetChallengeCopyManager getPetChallengeCopyManager() {
		return petChallengeCopyManager;
	}

	public KSeniorPetChallengeCopyManager getKSeniorPetChallengeCopyManager() {
		return seniorPetChallengeCopyManager;
	}

	/**
	 * 向客户端发送剧本关卡数据
	 * 
	 * @param role
	 *            对应的玩家角色
	 * @param scenarioId
	 *            地图出口对应的剧本ID
	 */
	public void sendScenarioData(KRole role, KGameScenario scenario, int mapId, int exitId) {

		// 获取关卡记录
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());

		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_SEND_SCENARIO_DATA);
		// 发送场景基本信息
		sendMsg.writeInt(mapId);
		sendMsg.writeInt(exitId);
		sendMsg.writeInt(scenario.getScenarioId());
		sendMsg.writeUtf8String(scenario.getScenarioName());
		sendMsg.writeInt(scenario.getScenarioBgResId());
		sendMsg.writeUtf8String(scenario.getScenarioDesc());
		sendMsg.writeShort(scenario.getChapterId());

		byte levelSize = (byte) scenario.getAllGameLevel().size();
		sendMsg.writeByte(levelSize);
		for (KLevelTemplate level : scenario.getAllGameLevel()) {
			PlayerRoleGamelevelData levelData = levelSet.getPlayerRoleNormalGamelevelData(level.getLevelId());

			sendMsg.writeInt(level.getLevelId());
			sendMsg.writeByte(level.getLevelType().levelType);
			sendMsg.writeUtf8String(level.getLevelName());
			sendMsg.writeUtf8String(level.getDesc());
			sendMsg.writeByte(level.getLevelNumber());
			sendMsg.writeInt(level.getIconResId());
			sendMsg.writeInt(level.getBossIconResId());
			sendMsg.writeInt(level.getItemIconResId());
			sendMsg.writeShort(level.getEnterCondition().getOpenRoleLevel());

			byte levelViewType = judgeGameLevelOpenState(role, levelSet, level);
			sendMsg.writeByte(levelViewType);
			sendMsg.writeByte(level.getEnterCondition().getUseStamina());

			if (levelData != null) {
				byte levelCompleteType = (byte) (levelData.isCompleted() ? 1 : 0);
				sendMsg.writeByte(levelCompleteType);
				// sendMsg.writeByte((level.getLevelId() % 1));
				sendMsg.writeInt(level.getFightPower());
				byte levelEvaluate = levelData.getLevelEvaluate(); // 表示关卡的评价（5星，未完成为0）
				sendMsg.writeByte(levelEvaluate);
				sendMsg.writeInt(level.getEnterCondition().getLevelLimitJoinCount());// 表示关卡的当天最大进入次数：0表示无限次
				int remainJoinCount = levelData.getRemainJoinLevelCount(); // 表示关卡的当天剩余进入次数
				sendMsg.writeInt(remainJoinCount);
			} else {
				sendMsg.writeByte(0);
				sendMsg.writeInt(level.getFightPower());
				sendMsg.writeByte(0);
				sendMsg.writeInt(level.getEnterCondition().getLevelLimitJoinCount());
				sendMsg.writeInt(level.getEnterCondition().getLevelLimitJoinCount());
			}

			// 奖励信息
			level.getReward().getShowRewardData(role.getJob()).packMsg(sendMsg);
			// S级别奖励
			level.getReward().s_probableReward.packMsg(sendMsg);

			// 扫荡信息
			if (levelData != null) {
				sendMsg.writeBoolean(levelData.isCompleted());
			} else {
				sendMsg.writeBoolean(false);
			}
			sendMsg.writeByte(level.getEnterCondition().getUseStamina());
			sendMsg.writeByte(checkSaodangSuccessRate(role, level));
		}

		// 剧本通关奖励
		if (scenario.getReward() != null) {
			boolean isGet = levelSet.checkHasGetScenarioPrice(scenario.getScenarioId());
			sendMsg.writeBoolean(isGet);
			if (!isGet) {
				scenario.getReward().probableReward.packMsg(sendMsg);
			}
		} else {
			sendMsg.writeBoolean(true);
		}

		// 剧本全S奖励
		if (scenario.getS_reward() != null) {
			boolean isGet = levelSet.checkHasGetScenarioSLevelPrice(scenario.getScenarioId());
			sendMsg.writeBoolean(isGet);
			if (!isGet) {
				scenario.getS_reward().probableReward.packMsg(sendMsg);
			}
		} else {
			sendMsg.writeBoolean(true);
		}

		role.sendMsg(sendMsg);

	}

	/**
	 * 处理角色进入关卡
	 * 
	 * @param role
	 * @param levelId
	 * @param isNeedCheck
	 *            是否需要检测进入条件
	 */
	public KActionResult<Integer> playerRoleJoinGameLevel(KRole role, int levelId, boolean isNeedCheck, boolean isSendDialog) {
		if (role == null) {
			_LOGGER.error("角色进入关卡失败，角色为null，关卡ID：" + levelId);
			// return new KActionResult(false, GlobalTips.getTipsServerBusy());
			return new KActionResult(false, GlobalTips.getTipsServerBusy());
		}
		KLevelTemplate level = allKGameLevel.get(levelId);
		if (level == null) {
			_LOGGER.error("角色进入关卡失败，找不到对应的关卡。角色id:" + role.getId() + "，关卡ID：" + levelId);
			// return new KActionResult(false, GlobalTips.getTipsServerBusy());
			return new KActionResult(false, GlobalTips.getTipsServerBusy(), 0);
		}

		// 获取关卡开启条件
		KEnterLevelCondition condition = level.getEnterCondition();

		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());

		KGameLevelRecord.PlayerRoleGamelevelData levelData = levelSet.getPlayerRoleNormalGamelevelData(level.getLevelId());

		// 判断角色是否能进入关卡，参考方法checkPlayerRoleCanJoinGameLevel()
		KActionResult joinLevelState = checkPlayerRoleCanJoinGameLevel(role, levelSet, level, isNeedCheck, isSendDialog);
		// 如果可以进入
		if (joinLevelState.success) {
			// 当前进入为普通战场模式，取得关卡第一层战场的数据，并通知战斗模块
			if (level.getAllNormalBattlefields().isEmpty()) {
				_LOGGER.error("角色进入关卡失败，找不到对应的第一层战场。角色id:" + role.getId() + "，关卡ID：" + levelId);
				// sendJoinGameLevelTipsMessage(
				// role,
				// KGameLevel.PLAYERROLE_JOIN_GAMELEVEL_STATE_OTHER_REASON,
				// "进入关卡失败，发生未知错误");
				if (isSendDialog) {
					sendJoinNormalGameLevelTipsMessage(role, (short) -1, LevelTips.getTipsJoinLevelFailed(), false, null);
				}
				return new KActionResult(false, LevelTips.getTipsJoinLevelFailed(), 0);
			}

			// 修改个人关卡记录
			if (levelData == null) {
				levelSet.addOrModifyNormalGameLevelData(level.getLevelId(), (condition.isLimitJoinCountLevel()) ? (condition.getLevelLimitJoinCount() - 1) : 0, (byte) 0, false);

				// // 记录第一次进入关卡行为数据
				FlowDataModuleFactory.getModule().recordFirstEnterGameLevel(levelId);

			} else {
				// 如果进入次数大于0，则修改个人关卡数据记录，将剩余次数减1
				if (condition.isLimitJoinCountLevel()) {
					levelSet.addOrModifyNormalGameLevelData(level.getLevelId(), (levelData.getRemainJoinLevelCount() > 0) ? (levelData.getRemainJoinLevelCount() - 1) : 0,
							levelData.isCompleted() ? levelData.getLevelEvaluate() : 0, levelData.isCompleted());
				}
			}

		}

		if (joinLevelState.success) {
			// // 修改角色体力,减少值为该关卡的消耗体力值
			// if (condition.getUseStamina() > 0
			// && role.getPhyPower() >= condition.getUseStamina()) {
			// // KSupportFactory.getRoleModuleSupport().decreasePhyPower(role,
			// // condition.getUseStamina());
			// KSupportFactory.getRoleModuleSupport().decreasePhyPower(role,
			// condition.getUseStamina(), "普通关卡扣除体力");
			// }

			// 检测是否有触发的剧情
			List<Animation> animation = null;
			if (levelData == null || !levelData.isCompleted()) {
				animation = AnimationManager.getInstance().getLevelTypeAnimations().get(level.getLevelId());
			}
			if (animation == null) {
				animation = Collections.emptyList();
			}
			// 检测是否有机甲或副武器战斗引导
			KSupportFactory.getNoviceGuideSupport().checkAndNotifyWeaponGuideBattle(role, levelSet, levelId);

			// 通知战斗模块，角色进入战场
			for (FightEventListener listener : fightEventListenerList) {
				listener.notifyBattle(role, level.getAllNormalBattlefields(), animation);
			}
			// 进入战斗，角色离开地图
			KSupportFactory.getMapSupport().processRoleJoinNormalGameLevelAndLeaveMap(role);

			// 角色行为统计
			KActionRecorder.recordAction(role.getId(), KActionType.ACTION_TYPE_NORMAL_FB, 1);

			return new KActionResult(true, "", 0);
		}
		return joinLevelState;
	}

	/**
	 * <pre>
	 * 检测角色能否进入关卡，如果可以进入，发送进入关卡等待提示，否则发送错误提示
	 * 
	 * @param role
	 * @param level
	 * @return 返回：-1表示不能进入战场
	 *               1 表示进入普通战场
	 *               2 表示进入boss战场
	 * </pre>
	 */
	private KActionResult<Integer> checkPlayerRoleCanJoinGameLevel(KRole role, KGameLevelSet levelSet, KLevelTemplate level, boolean isNeedCheckCondition, boolean isSendDialog) {
		KActionResult<Integer> result = new KActionResult();
		String tips = "";
		if (isNeedCheckCondition) {
			// 获取角色当前体力值
			int roleStamina = role.getPhyPower();

			// 判断关卡是否开放
			byte levelOpenType = judgeGameLevelOpenState(role, levelSet, level);
			if (levelOpenType != KLevelTemplate.GAME_LEVEL_STATE_OPEN) {
				tips = LevelTips.getTipsLevelNotOpen();
				if (isSendDialog) {
					sendJoinNormalGameLevelTipsMessage(role, (short) -1, tips, false, null);
				}
				result.success = false;
				result.tips = tips;
				result.attachment = 0;
				return result;
			}

			// // 判断角色体力是否足够
			// if (level.getEnterCondition().getUseStamina() > 0
			// && roleStamina < level.getEnterCondition().getUseStamina()) {
			// // sendJoinGameLevelTipsMessage(
			// // role,
			// // KGameLevel.PLAYERROLE_JOIN_GAMELEVEL_STATE_STAMINA_NOT_ENOUGH,
			// // "角色体力不足，是否使用道具进入关卡？");
			// // sendJoinNormalGameLevelTipsMessage(role, false,
			// // "角色体力不足，请恢复体力值后再进入关卡。", false, null);
			// KItem phyItem = PhyPowerBuyManager.searchPhyPowerItem(role
			// .getRoleId());
			// if (phyItem != null) {
			// sendJoinNormalGameLevelTipsMessage(
			// role,
			// false,
			// ScenarioTips.getTipsJoinLevelFailedRecoverStamina2(
			// 1, phyItem.getItemTemplate().extItemName),
			// true,
			// level.getLevelId() + "," + 0 + ","
			// + phyItem.getId());
			// } else {
			// int buyReleaseTime = PhyPowerBuyManager
			// .getBuyReleaseTime(role.getRoleId());
			// int getConsumIngot = PhyPowerBuyManager.getConsumIngot(role
			// .getRoleId());
			//
			// int recoverStaminaUsePoint = ((buyReleaseTime > 0) ?
			// (getConsumIngot)
			// : 0);
			// if (recoverStaminaUsePoint <= 0) {
			// sendJoinNormalGameLevelTipsMessage(
			// role,
			// false,
			// ScenarioTips
			// .getTipsJoinLevelFailedWhileStaminaNotEnough(),
			// false, null);
			// } else {
			// // sendJoinNormalGameLevelTipsMessage(
			// // role,
			// // false,
			// // "角色体力不足，是否要花费 " + recoverStaminaUsePoint
			// // + " 元宝补充体力后进入关卡？【确定】则消耗元宝进入关卡，【取消】则关闭确认界面。",
			// // true, level.getLevelId() + ","
			// // + recoverStaminaUsePoint);
			//
			// sendJoinNormalGameLevelTipsMessage(
			// role,
			// false,
			// ScenarioTips
			// .getTipsJoinLevelFailedRecoverStamina(recoverStaminaUsePoint),
			// true, level.getLevelId() + ","
			// + recoverStaminaUsePoint + "," + 0);
			// }
			// }
			//
			// return -1;
			// }

			if (level.getEnterCondition().getUseStamina() > 0 && roleStamina < level.getEnterCondition().getUseStamina()) {
				// sendJoinNormalGameLevelTipsMessage(role, false,
				// "角色体力不足，请恢复体力值后再进入关卡。", false, null);
				tips = LevelTips.getTipsJoinLevelFailedWhileStaminaNotEnough();
				if (isSendDialog) {
					// sendJoinNormalGameLevelTipsMessage(role, (short) -1,
					// tips,
					// false, null);
					checkAndSendPhyPowerNotEnoughDialog(role);
				}
				result.success = false;
				result.tips = tips;
				result.attachment = 1;
				return result;
			}

			// 判断是否限制进入次数关卡
			PlayerRoleGamelevelData levelData = levelSet.getPlayerRoleNormalGamelevelData(level.getLevelId());

			if (level.getEnterCondition().isLimitJoinCountLevel()) {
				// 如果是限制次数，则判断其剩余进入次数
				if (levelData != null && levelData.getRemainJoinLevelCount() <= 0) {
					// 剩余次数小于等于0，则发送错误提示
					// sendJoinGameLevelTipsMessage(
					// role,
					// KGameLevel.PLAYERROLE_JOIN_GAMELEVEL_STATE_REMAINCOUNT_NOT_ENOUGH,
					// "进入关卡的剩余次数不足，不能进入。是否使用道具进入？");
					tips = LevelTips.getTipsJoinLevelFailedWhileMaxCount();
					if (isSendDialog) {
						sendJoinNormalGameLevelTipsMessage(role, (short) -1, tips, false, null);
					}

					result.success = false;
					result.tips = tips;
					result.attachment = 0;
					return result;
				}
			}
		}
		result.success = true;
		result.tips = tips;
		result.attachment = 0;
		return result;
	}

	/**
	 * 判断剧本关卡开启状态
	 * 
	 * @param levelAttr
	 * @param level
	 * @return
	 */
	public byte judgeGameLevelOpenState(KRole role, KGameLevelSet levelSet, KLevelTemplate level) {
		if (role.getRoleGameSettingData() != null && role.getRoleGameSettingData().isDebugOpenLevel()) {
			return KLevelTemplate.GAME_LEVEL_STATE_OPEN;
		}

		if (level.getLevelType() == KGameLevelTypeEnum.普通关卡 && levelSet.checkGameLevelIsCompleted(level.getLevelId())) {
			return KLevelTemplate.GAME_LEVEL_STATE_OPEN;
		}

		if (level.getEnterCondition() != null) {
			// TODO 处理任务控制关卡开放
			if (level.getEnterCondition().getOpenRoleLevel() > role.getLevel()) {
				return KLevelTemplate.GAME_LEVEL_STATE_NOT_OPEN;
			}

			if (level.getLevelType() == KGameLevelTypeEnum.普通关卡 && level.getEnterCondition().getFrontMissionTemplateId() > 0) {
				boolean missionIsOpen = KSupportFactory.getMissionSupport().checkMissionIsAcceptedOrCompleted(role, level.getEnterCondition().getFrontMissionTemplateId());
				if (!missionIsOpen) {
					return KLevelTemplate.GAME_LEVEL_STATE_NOT_OPEN;
				}
			}

			if (level.getEnterCondition().getFrontLevelId() > 0) {
				PlayerRoleGamelevelData frontLevelData = null;
				if (level.getLevelType() == KGameLevelTypeEnum.普通关卡) {
					frontLevelData = levelSet.getPlayerRoleNormalGamelevelData(level.getEnterCondition().getFrontLevelId());
				} else if (level.getLevelType() == KGameLevelTypeEnum.技术副本关卡 || level.getLevelType() == KGameLevelTypeEnum.精英副本关卡) {
					frontLevelData = levelSet.getCopyLevelData(level.getEnterCondition().getFrontLevelId(), level.getLevelType());
				}

				if (frontLevelData == null) {
					return KLevelTemplate.GAME_LEVEL_STATE_NOT_OPEN;
				}

				if (!frontLevelData.isCompleted()) {
					return KLevelTemplate.GAME_LEVEL_STATE_NOT_OPEN;
				}
			}

			// if (level.getLevelType() == KGameLevelTypeEnum.技术副本关卡
			// && level.getHinderGameLevelList().size() > 0) {
			// KLevelTemplate hiderLevel = level.getHinderGameLevelList().get(
			// 0);
			// if (hiderLevel != null
			// && hiderLevel.getEnterCondition().getOpenRoleLevel() <= role
			// .getLevel()) {
			// PlayerRoleGamelevelData hiderLevelData = levelSet
			// .getCopyLevelData(hiderLevel.getLevelId(),
			// level.getLevelType());
			// PlayerRoleGamelevelData levelData = levelSet
			// .getCopyLevelData(level.getLevelId(),
			// level.getLevelType());
			// if (levelData != null && hiderLevelData != null
			// && hiderLevelData.isCompleted()
			// && levelData.getRemainJoinLevelCount() <= 0) {
			// return KLevelTemplate.GAME_LEVEL_STATE_NOT_OPEN;
			// }
			// }
			// }

		}

		return KLevelTemplate.GAME_LEVEL_STATE_OPEN;
	}

	/**
	 * 发送进入关卡的提示信息
	 * 
	 * @param isCanJoin
	 *            ，能否进入关卡的状态
	 * @param tips
	 *            提示信息
	 */
	public void sendJoinNormalGameLevelTipsMessage(KRole role, short confirmKey, String tips, boolean isHasConfirmButton, String script) {
		List<KDialogButton> list = new ArrayList<KDialogButton>();
		if (isHasConfirmButton) {
			list.add(KDialogButton.CANCEL_BUTTON);
			list.add(new KDialogButton(confirmKey, script, GlobalTips.确定));
		} else {
			list.add(KDialogButton.CONFIRM_BUTTON);
		}
		// KDialogService.sendNullDialog(role);
		KDialogService.sendFunDialog(role, GlobalTips.getTipsDefaultTitle(), tips, list, false, (byte) -1);

	}

	/**
	 * 处理角色完成某个战场及相关结算
	 * 
	 * @param role
	 * @param battlefield
	 */
	public void processPlayerRoleCompleteBattlefield(KRole role, FightResult result) {
		//
		int battlefieldId = result.getBattlefieldId();

		switch (result.getBattlefieldType()) {
		case 普通关卡战场:
			if (this.allKGameBattlefield.containsKey(battlefieldId)) {
				KGameBattlefield battlefield = this.allKGameBattlefield.get(battlefieldId);
				KLevelTemplate level = this.allKGameLevel.get(battlefield.getLevelId());
				if (result.isWin()) {
					processPlayerRoleCompleteGameLevel(role, level, result);
				} else {
					processPlayerRoleLevelFailed(role, level, result);
				}
				// 检测是否关闭机甲战斗引导
				KSupportFactory.getNoviceGuideSupport().checkAndCloseMountGuideBattle(role, battlefield.getLevelId());
			} else {
				// playerRoleCompleteBattlefield方法处理找不到战场数据情况
				// sendBattleFaildResultMessage(role, result.getBattlefieldId(),
				// result.getBattlefieldType().battlefieldType);
				_LOGGER.error("### Exctpeion----战斗结束关卡剧本模块找不到对应的战场实例，" + "战场ID:" + result.getBattlefieldId() + "，战场类型：" + result.getBattlefieldType().battlefieldType);
				return;

			}

			break;
		case 精英副本战场:
			if (this.copyManager.getAllKGameBattlefieldMap().containsKey(battlefieldId)) {
				KGameBattlefield battlefield = this.copyManager.getAllKGameBattlefieldMap().get(battlefieldId);
				KLevelTemplate level = this.copyManager.getEliteCopyLevelMap().get(battlefield.getLevelId());
				if (result.isWin()) {
					this.copyManager.processPlayerRoleCompleteCopyLevel(role, level, result);
				} else {
					processPlayerRoleLevelFailed(role, level, result);
				}
			}

			break;
		case 技术副本战场:
			if (this.copyManager.getAllKGameBattlefieldMap().containsKey(battlefieldId)) {
				KGameBattlefield battlefield = this.copyManager.getAllKGameBattlefieldMap().get(battlefieldId);
				KLevelTemplate level = this.copyManager.getTechCopyLevelMap().get(battlefield.getLevelId());
				if (result.isWin()) {
					this.copyManager.processPlayerRoleCompleteCopyLevel(role, level, result);
				} else {
					processPlayerRoleLevelFailed(role, level, result);
				}
			}
			break;
		case 好友副本战场:
			break;
		case 新手引导战场:

			// if (this.allKGameBattlefield.containsKey(battlefieldId)) {
			// KGameBattlefield battlefield = this.allKGameBattlefield
			// .get(battlefieldId);
			// KLevelTemplate level = this.allKGameLevel.get(battlefield
			// .getLevelId());
			// if (result.isWin()) {
			// processPlayerRoleCompleteGameLevel(role, level, result);
			// if (result.isWin()) {
			// KMissionCompleteRecordSet set = KMissionModuleExtension
			// .getMissionCompleteRecordSet(role.getId());
			// if (set != null) {
			// set.completeNoviceGuideStep(KNoviceGuideStepEnum.第二场战斗结算.type);
			// }
			// }
			// } else {
			// processPlayerRoleLevelFailed(role, level, result);
			// }
			// } else
			if (this.firstNoviceGuideBattle.getBattlefieldId() == battlefieldId) {
				if (result.isWin()) {
					KSupportFactory.getNoviceGuideSupport().notifyRoleCompleteFirstNoviceGuideBattle(role);
				}
				// 移除新手引导坐骑
				KSupportFactory.getMountModuleSupport().notifyCancelMountFromNewRole(role.getId());
				// 移除新手引导辅助宠物
				KSupportFactory.getPetModuleSupport().removeNoviceGuideFightingPet(role);
				sendNoviceGuideBattleResultMsg(role, result);
			} else {
				// playerRoleCompleteBattlefield方法处理找不到战场数据情况
				// sendBattleFaildResultMessage(role, result.getBattlefieldId(),
				// result.getBattlefieldType().battlefieldType);
				_LOGGER.error("### Exctpeion----战斗结束关卡剧本模块找不到对应的战场实例，" + "战场ID:" + result.getBattlefieldId() + "，战场类型：" + result.getBattlefieldType().battlefieldType);
				return;

			}
			break;
		case 产金活动战场:
			KGoldActivityManager.processCompleteBattle(role, result);
			break;
		case 新产金活动战场:
			KNewGoldActivityManager.processCompleteBattle(role, result);
			break;
		case 随从副本战场:
			KPetCopyBattlefield battlefieldTemplate = petCopyManager.allKPetCopyBattlefieldTemplate.get(result.getBattlefieldId());
			if (battlefieldTemplate != null) {
				KPetCopyLevelTemplate petCopyLevelTemplate = petCopyManager.getPetCopyLevelMap().get(battlefieldTemplate.levelId);
				petCopyManager.processPlayerRoleCompleteCopyLevel(role, petCopyLevelTemplate, result);
			}
			break;
		case 爬塔副本战场:
			if (KTowerCopyManager.allKGameBattlefield.containsKey(battlefieldId)) {
				int levelId = KTowerCopyManager.allKGameBattlefield.get(battlefieldId).getLevelId();
				KLevelTemplate levelTemplate = KTowerCopyManager.towerCopyLevelMap.get(levelId);
				if (levelTemplate == null) {
					_LOGGER.error("### Exctpeion----战斗结束爬塔副本管理模块找不到关卡模版数据，" + "关卡ID:" + levelId + "，战场类型：" + result.getBattlefieldType().battlefieldType);
					return;
				}
				if (result.isWin()) {
					towerCopyManager.processPlayerRoleCompleteCopyLevel(role, levelTemplate, result);
				} else {
					if (result.getEndType() == FightResult.FIGHT_END_TYPE_ESCAPE) {
						KSupportFactory.getMapSupport().processRoleFinishBattleReturnToMap(role);
					} else {
						towerCopyManager.sendSpecialBattleFailedMsg(role, levelId, KGameLevelTypeEnum.爬塔副本关卡.levelType, levelTemplate.getLevelNumber());
					}
				}
			} else {
				_LOGGER.error("### Exctpeion----战斗结束爬塔副本管理模块找不到对应的战场实例，" + "战场ID:" + result.getBattlefieldId() + "，战场类型：" + result.getBattlefieldType().battlefieldType);
				return;
			}

			break;
		case 随从挑战副本战场:
			if (KPetChallengeCopyManager.allKGameBattlefield.containsKey(battlefieldId)) {
				int levelId = KPetChallengeCopyManager.allKGameBattlefield.get(battlefieldId).getLevelId();
				KLevelTemplate levelTemplate = KPetChallengeCopyManager.petChallengeCopyLevelMap.get(levelId);
				if (levelTemplate == null) {
					_LOGGER.error("### Exctpeion----战斗结束随从挑战副本管理模块找不到关卡模版数据，" + "关卡ID:" + levelId + "，战场类型：" + result.getBattlefieldType().battlefieldType);
					return;
				}
				if (!result.isWin() && result.getEndType() == FightResult.FIGHT_END_TYPE_ESCAPE) {
					KSupportFactory.getMapSupport().processRoleFinishBattleReturnToMap(role);
				} else {
					petChallengeCopyManager.processPlayerRoleCompleteCopyLevel(role, levelTemplate, result);
				}
			}
			break;
		case 高级随从挑战副本战场:
			if (KSeniorPetChallengeCopyManager.allKGameBattlefieldTemplate.containsKey(battlefieldId)) {
				int levelId = KSeniorPetChallengeCopyManager.allKGameBattlefieldTemplate.get(battlefieldId).getLevelId();
				KLevelTemplate levelTemplate = KSeniorPetChallengeCopyManager.seniorPetChallengeCopyLevelMap.get(levelId);
				if (levelTemplate == null) {
					_LOGGER.error("### Exctpeion----战斗结束高级随从挑战副本管理模块找不到关卡模版数据，" + "关卡ID:" + levelId + "，战场类型：" + result.getBattlefieldType().battlefieldType);
					return;
				}
				if (!result.isWin() && result.getEndType() == FightResult.FIGHT_END_TYPE_ESCAPE) {
					KSupportFactory.getMapSupport().processRoleFinishBattleReturnToMap(role);
				} else {
					seniorPetChallengeCopyManager.processPlayerRoleCompleteCopyLevel(role, levelTemplate, result);
				}
			}
			break;
		}

		// 通知战场监听器通知战场结束，以及战斗结果
		for (FightEventListener listener : fightEventListenerList) {
			listener.notifyBattleFinished(role, result);
		}
	}

	/**
	 * 处理角色完成关卡，处理关卡结算相关流程
	 * 
	 * @param role
	 * @param level
	 */
	public void processPlayerRoleCompleteGameLevel(KRole role, KLevelTemplate level, FightResult result) {
		// 关卡开启条件
		KEnterLevelCondition condition = level.getEnterCondition();

		// 修改角色体力,减少值为该关卡的消耗体力值
		if (condition.getUseStamina() > 0 && role.getPhyPower() >= condition.getUseStamina()) {
			// KSupportFactory.getRoleModuleSupport().decreasePhyPower(role,
			// condition.getUseStamina());
			KSupportFactory.getRoleModuleSupport().decreasePhyPower(role, condition.getUseStamina(), "普通关卡扣除体力");
		}

		// 获取关卡记录
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());

		PlayerRoleGamelevelData levelData = levelSet.getPlayerRoleNormalGamelevelData(level.getLevelId());

		KLevelReward reward = level.getReward();

		// 计算关卡战斗等级
		byte fightLv = caculateLevelFightEvaluate(role, result, level);

		// 计算关卡所有奖励
		LevelRewardResultData rewardData = caculateLevelReward(role, level, result, fightLv, PresentPointTypeEnum.关卡奖励, true);

		// 发送完成关卡消息
		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_SHOW_COMPLETE_GAME_LEVEL_RESULT);
		sendMsg.writeInt(level.getLevelId());
		sendMsg.writeByte(level.getLevelType().levelType);
		sendMsg.writeBoolean(result.isWin());
		sendMsg.writeByte(fightLv);
		sendMsg.writeInt((int) (result.getBattleTime() / 1000));
		sendMsg.writeShort(result.getMaxBeHitCount());
		sendMsg.writeInt(result.getTotalDamage());
		String tips = "";
		if (KSupportFactory.getItemModuleSupport().checkEmptyVolumeInBag(role.getId()) < rewardData.totalItemSize) {
			tips = LevelTips.getTipsBagCapacityNotEnough();// "您的背包剩余空间不足，请整理背包。";
		}
		sendMsg.writeUtf8String(tips);
		String[] sLevelTips = level.getSLevelFightEvaluateDataTips(role.getJob());
		sendMsg.writeUtf8String(sLevelTips[0]);
		sendMsg.writeUtf8String(sLevelTips[1]);

		sendMsg.writeInt(getNextRoleLvUpgradeExp(role));

		// 经验加成
		sendMsg.writeByte(rewardData.expAddRate);

		rewardData.baseReward.packMsg(sendMsg);
		sendMsg.writeBoolean(rewardData.isDropItemDouble);

		if (fightLv == FightEvaluateData.MAX_FIGHT_LEVEL && rewardData.sLevelReward != null) {
			sendMsg.writeBoolean(true);
			rewardData.sLevelReward.packMsg(sendMsg);
		} else {
			sendMsg.writeBoolean(false);
		}
		// 经验、货币结算显示
		if (level.getLevelType() == KGameLevelTypeEnum.好友副本关卡) {
			int addRate = KSupportFactory.getGangSupport().getGangEffect(KGangTecTypeEnum.好友副本潜能产出, role.getId());
			sendMsg.writeInt(addRate);
		} else {
			for (int i = 0; i < rewardData.attrAndCurrencyShowData.length; i++) {
				sendMsg.writeBoolean(rewardData.isAttrDouble[i]);
				for (int j = 0; j < rewardData.attrAndCurrencyShowData[i].length; j++) {
					sendMsg.writeInt(rewardData.attrAndCurrencyShowData[i][j]);
				}
			}
		}

		// 处理发送抽奖信息
		if (reward.isHasLotteryReward() && rewardData.lotteryRewardList != null && rewardData.lotteryRewardUsePointList != null && rewardData.lotteryRewardList.size() > 0) {
			sendMsg.writeBoolean(true);
			sendMsg.writeByte(rewardData.lotteryRewardList.size());
			sendMsg.writeInt(rewardData.lotteryRewardList.size());
			for (int i = 0; i < rewardData.lotteryRewardList.size(); i++) {
				ItemCountStruct lotteryReward = rewardData.lotteryRewardList.get(i);
				sendMsg.writeByte(i);
				sendMsg.writeByte(2);
				KItemMsgPackCenter.packItem(sendMsg, lotteryReward.getItemTemplate(), lotteryReward.itemCount);
			}
			for (int i = 0; i < rewardData.lotteryRewardUsePointList.size(); i++) {
				sendMsg.writeInt(rewardData.lotteryRewardUsePointList.get(i));
			}
		} else {
			sendMsg.writeBoolean(false);
		}
		role.sendMsg(sendMsg);

		// 处理个人关卡记录，计算关卡评价

		boolean isLevelDataChange = false;
		boolean isCompletedAndTriggerOpenHinderLevel = false;
		// 是否检测星级评定并发送更新信息
		boolean isSendUpdateLevelEvaluateInfo = false;

		if (levelData == null) {
			// 找不到关卡记录，则添加记录并将状态改为完成状态，设入关卡评价
			levelSet.addOrModifyNormalGameLevelData(level.getLevelId(), condition.isLimitJoinCountLevel() ? condition.getLevelLimitJoinCount() : 0, fightLv, true);

			isLevelDataChange = true;
			if (level.getHinderGameLevelList().size() > 0) {
				isCompletedAndTriggerOpenHinderLevel = true;
			}
			isSendUpdateLevelEvaluateInfo = true;

			// // 记录第一次进入关卡行为数据
			FlowDataModuleFactory.getModule().recordFirstCompleteGameLevel(level.getLevelId(), role.getLevel());
		} else {
			if (levelData.getLevelEvaluate() < fightLv) {
				isSendUpdateLevelEvaluateInfo = true;
			}

			if (!levelData.isCompleted()) {
				if (level.getHinderGameLevelList().size() > 0) {
					isCompletedAndTriggerOpenHinderLevel = true;
				}

				// // 记录第一次进入关卡行为数据
				FlowDataModuleFactory.getModule().recordFirstCompleteGameLevel(level.getLevelId(), role.getLevel());
			}

			levelSet.addOrModifyNormalGameLevelData(level.getLevelId(), levelData.getRemainJoinLevelCount(), ((levelData.getLevelEvaluate() < fightLv) ? fightLv : levelData.getLevelEvaluate()), true);
			isLevelDataChange = true;
		}

		// 发消息通知用户该关卡记录有发生改变
		if (isLevelDataChange) {
			sendUpdateGameLevelInfoMsg(role, level, levelData);
		}

		// 通知战场监听器通知关卡完成
		for (FightEventListener listener : fightEventListenerList) {
			listener.notifyGameLevelCompleted(role, level, result);
		}

		// 处理后置关卡开放状态
		if (isCompletedAndTriggerOpenHinderLevel) {
			// KMissionModuleSupport missionSupport = KSupportFactory
			// .getMissionSupport();
			for (KLevelTemplate hinderLevel : level.getHinderGameLevelList()) {
				// if
				// (hinderLevel.getEnterCondition().getFrontMissionTemplateId()
				// > 0) {
				// boolean missionIsOpen = missionSupport
				// .checkMissionIsAcceptedOrCompleted(role,
				// hinderLevel.getCondition()
				// .getFrontMissionTemplateId());
				// if (missionIsOpen) {
				// sendUpdateGameLevelOpenedOrGrayState(role,
				// hinderLevel.getScenarioId(),
				// hinderLevel.getLevelId(),
				// KGameLevel.GAME_LEVEL_STATE_OPEN, hinderLevel
				// .getCondition()
				// .getLevelLimitJoinCount());
				// }
				// } else {
				if (role.getLevel() >= hinderLevel.getEnterCondition().getOpenRoleLevel()) {
					sendUpdateGameLevelOpenedOrGrayState(role, hinderLevel.getScenarioId(), hinderLevel.getLevelId(), KLevelTemplate.GAME_LEVEL_STATE_OPEN, hinderLevel.getEnterCondition()
							.getLevelLimitJoinCount());
				}
			}
			// }
		}

		// 通知活跃度模块
		KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.通关普通副本);

		// 发送跟新星级评定奖励信息
		// if (isSendUpdateLevelEvaluateInfo) {
		// checkAndSendLevelEvaluateRewardData(role, false, true);
		// }

		// if (level.getLevelType() == KGameLevelTypeEnum.LEVEL_TYPE_NORMAL
		// && result.isWin()) {
		// KPlayerRoleAnimationExtAttribute animationAttr =
		// (KPlayerRoleAnimationExtAttribute) KSupportFactory
		// .getDataCacheSupport()
		// .getExtCAByType(
		// role.getRoleId(),
		// KAbsPlayerRoleExtAttribute.EXTCA_TYPE_GAME_ANIMATION,
		// true);
		//
		// // 检测是否有触发的剧情,
		// if (AnimationManager.getInstance().getLevelEndTypeAnimations()
		// .containsKey(level.getLevelId())) {
		// Animation temp = AnimationManager.getInstance()
		// .getLevelEndTypeAnimations().get(level.getLevelId());
		//
		// if (!animationAttr.isAnimationCompleted(temp.animationId)) {
		// animationAttr.addCompleteAnimation(temp);
		// }
		// }
		// }

		// // 检测是否完成剧本
		// if
		// (!recordContainer.getScenarioRecord().getScenarioData().completeScenarioMap
		// .containsKey(level.getScenarioId())) {
		// KGameScenario scenario = allKGameScenario
		// .get(level.getScenarioId());
		// boolean isCompleteScenario = true;
		// for (KGameLevel checkLevel : scenario.getAllGameLevel()) {
		// if (!checkLevel.isHiddenLevel()) {
		// PlayerRoleGamelevelData checkData = recordContainer
		// .getPlayerRoleNormalGamelevelData(checkLevel
		// .getLevelId());
		// if (checkData == null || !checkData.isCompleted()) {
		// isCompleteScenario = false;
		// break;
		// }
		// if (checkData.getLevelEvaluate() < 5) {
		// isCompleteScenario = false;
		// break;
		// }
		// }
		// }
		// if (isCompleteScenario) {
		// recordContainer.getScenarioRecord().addOrModifyScenarioData(
		// scenario.getScenarioId(), false);
		// }
		// }
	}

	public void processPlayerRoleLevelFailed(KRole role, KLevelTemplate level, FightResult result) {

		if (role.isOnline()) {
			if (result.getEndType() == FightResult.FIGHT_END_TYPE_NORMAL) {
				sendBattleFaildResultMessage(role, level.getLevelId(), level.getLevelType().levelType);
				if (level.getLevelType() == KGameLevelTypeEnum.普通关卡) {
					// 普通关卡失败扣除体力值
					KEnterLevelCondition condition = level.getEnterCondition();
					if (condition.getUseStamina() > 0 && role.getPhyPower() >= condition.getUseStamina()) {
						// KSupportFactory.getRoleModuleSupport().decreasePhyPower(role,
						// condition.getUseStamina());
						KSupportFactory.getRoleModuleSupport().decreasePhyPower(role, condition.getUseStamina(), "普通关卡扣除体力");
					}
				}
			} else {
				if (level.getLevelType() == KGameLevelTypeEnum.普通关卡) {
					KSupportFactory.getMapSupport().processRoleFinishNormalGameLevelAndReturnToMap(role);
				} else {
					KSupportFactory.getMapSupport().processRoleFinishBattleReturnToMap(role);
				}
			}
		}
		// 通知战斗模块清除这场战斗的缓存数据
		for (FightEventListener listener : fightEventListenerList) {
			listener.notifyBattleRewardFinished(role);
		}
	}

	/**
	 * 发送战斗失败消息
	 * 
	 * @param role
	 * @param battlefieldId
	 * @param battlefieldType
	 */
	public void sendBattleFaildResultMessage(KRole role, int levelId, byte levelType) {
		// 发送战场结算消息
		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_SHOW_COMPLETE_GAME_LEVEL_RESULT);
		sendMsg.writeInt(levelId);
		sendMsg.writeByte(levelType);
		sendMsg.writeBoolean(false);
		role.sendMsg(sendMsg);
	}

	public void checkAndUpdateGameLevelOpenState(KRole role) {
		List<KLevelTemplate> levelList = allKGameLevelByRoleLv.get(role.getLevel());
		if (levelList != null && levelList.size() > 0) {
			KGameLevelSet set = KGameLevelModuleExtension.getGameLevelSet(role.getId());
			for (KLevelTemplate template : levelList) {
				if (template.getEnterCondition().getOpenRoleLevel() == role.getLevel()) {
					PlayerRoleGamelevelData levelData = set.getPlayerRoleNormalGamelevelData(template.getLevelId());
					if (levelData != null) {
						sendUpdateGameLevelInfoMsg(role, template, levelData);
					}

					if (template.getEnterCondition().getFrontLevelId() > 0) {
						if (set.checkGameLevelIsCompleted(template.getEnterCondition().getFrontLevelId())) {
							if (!set.checkGameLevelIsCompleted(template.getLevelId())) {
								sendUpdateGameLevelOpenedOrGrayState(role, template.getScenarioId(), template.getLevelId(), KLevelTemplate.GAME_LEVEL_STATE_OPEN, template.getEnterCondition()
										.getLevelLimitJoinCount());
							}
						}
					} else {
						sendUpdateGameLevelOpenedOrGrayState(role, template.getScenarioId(), template.getLevelId(), KLevelTemplate.GAME_LEVEL_STATE_OPEN, template.getEnterCondition()
								.getLevelLimitJoinCount());
					}
				}
			}
		}
	}

	public LevelRewardResultData caculateLevelReward(KRole role, KLevelTemplate level, FightResult fightResult, int fightLv, PresentPointTypeEnum presentPointType, boolean isAddTempRecord) {
		KLevelReward reward = level.getReward();
		LevelRewardResultData resultData = new LevelRewardResultData(CompleteGameLevelTempRecord.LOTTERY_TYPE_ITEM);

		// 检测是否有限时产出活动
		boolean isCopyActivityPrice = false;
		float expRate = 1, goldRate = 1, potentialRate = 1;
		int itemMultiple = 1;
		if (level.getLevelType() == KGameLevelTypeEnum.技术副本关卡 || level.getLevelType() == KGameLevelTypeEnum.精英副本关卡) {
			TimeLimieProduceActivity activity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.精英副本技术副本活动);

			if (activity != null && activity.isActivityTakeEffectNow()) {
				isCopyActivityPrice = true;
				expRate = activity.expRate;
				goldRate = activity.goldRate;
				potentialRate = activity.potentialRate;
				if (activity.isDropItemDouble) {
					int mRate = UtilTool.random(0, UtilTool.TEN_THOUSAND_RATIO_UNIT);
					if (mRate >= (UtilTool.TEN_THOUSAND_RATIO_UNIT - activity.itemMultipleRate)) {
						itemMultiple = activity.itemMultiple;
						resultData.isDropItemDouble = true;
					}
				}
				resultData.isAttrDouble[0] = activity.isExpDouble;
				resultData.isAttrDouble[1] = activity.isGoldDouble;
				resultData.isAttrDouble[2] = activity.isPotentialDouble;

				KSupportFactory.getMailModuleSupport().sendSimpleMailBySystem(role.getId(), activity.mailTitle, activity.mailContent);
			}
		}

		// 计算关卡数值奖励
		List<AttValueStruct> attList = new ArrayList<AttValueStruct>();
		List<AttValueStruct> attShowList = new ArrayList<AttValueStruct>();
		for (AttValueStruct attReward : reward.probableReward.get(role.getJob()).attList) {
			KGameAttrType attrType = attReward.roleAttType;
			int baseValue = attReward.addValue;
			int value = (int) (baseValue * expRate);
			if (attReward.roleAttType == KGameAttrType.EXPERIENCE) {
				int[] addExp = caculateVipAddExp(role, baseValue);
				resultData.attrAndCurrencyShowData[LevelRewardResultData.EXP_LINE][LevelRewardResultData.NORMAL_ROW] = value;
				resultData.expAddRate = (byte) addExp[0];
				value += addExp[1];
				resultData.attrAndCurrencyShowData[LevelRewardResultData.EXP_LINE][LevelRewardResultData.VIP_ROW] = addExp[1];
			}
			attList.add(new AttValueStruct(attrType, value, 0));
			attShowList.add(new AttValueStruct(attrType, value, 0));
		}
		// 处理关卡货币奖励
		List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();
		List<KCurrencyCountStruct> moneyShowList = new ArrayList<KCurrencyCountStruct>();
		for (KCurrencyCountStruct attReward : reward.probableReward.get(role.getJob()).moneyList) {
			long baseValue = attReward.currencyCount;
			if (attReward.currencyType == KCurrencyTypeEnum.GOLD) {
				baseValue = (long) (baseValue * goldRate);
				resultData.attrAndCurrencyShowData[LevelRewardResultData.GOLD_LINE][LevelRewardResultData.NORMAL_ROW] = (int) baseValue;
			} else if (attReward.currencyType == KCurrencyTypeEnum.POTENTIAL) {
				baseValue = (long) (baseValue * potentialRate);
				resultData.attrAndCurrencyShowData[LevelRewardResultData.POTENTIAL_LINE][LevelRewardResultData.NORMAL_ROW] = (int) baseValue;
			} else {

			}
			moneyList.add(new KCurrencyCountStruct(attReward.currencyType, baseValue));
			moneyShowList.add(new KCurrencyCountStruct(attReward.currencyType, baseValue));
		}

		ItemModuleSupport itemSupport = KSupportFactory.getItemModuleSupport();

		// 计算获得道具列表
		List<ItemCountStruct> itemRewardList = new ArrayList<ItemCountStruct>();
		List<ItemCountStruct> itemRewardShowList = new ArrayList<ItemCountStruct>();
		// 再处理关卡常规掉落道具
		if (!reward.getAllItemReward(role.getJob()).isEmpty()) {
			List<ItemCountStruct> itemList = reward.caculateItemReward(role.getJob(), itemMultiple);
			for (ItemCountStruct itemData : itemList) {
				itemRewardList.add(itemData);
				itemRewardShowList.add(itemData);
			}
		}

		// 计算掉落池道具
		List<ItemCountStruct> dropPoolItems = reward.caculateDropPoolItems(itemMultiple);
		for (ItemCountStruct itemData : dropPoolItems) {
			itemRewardList.add(itemData);
			itemRewardShowList.add(itemData);
		}

		// 计算关卡战斗的掉落
		if (fightResult.getBattleReward() != null) {
			if (!fightResult.getBattleReward().getAdditionalCurrencyReward().isEmpty()) {
				for (KCurrencyTypeEnum currType : fightResult.getBattleReward().getAdditionalCurrencyReward().keySet()) {
					int value = fightResult.getBattleReward().getAdditionalCurrencyReward().get(currType);
					moneyShowList.add(new KCurrencyCountStruct(currType, value));
					if (currType == KCurrencyTypeEnum.GOLD) {
						resultData.attrAndCurrencyShowData[LevelRewardResultData.GOLD_LINE][LevelRewardResultData.NORMAL_ROW] += value;
					} else if (currType == KCurrencyTypeEnum.POTENTIAL) {
						resultData.attrAndCurrencyShowData[LevelRewardResultData.POTENTIAL_LINE][LevelRewardResultData.NORMAL_ROW] += value;
					}
				}
				moneyShowList = KCurrencyCountStruct.mergeCurrencyCountStructs(moneyShowList);
			}
			// 道具
			if (fightResult.getBattleReward().getAdditionalItemReward().size() > 0) {
				for (String itemCode : fightResult.getBattleReward().getAdditionalItemReward().keySet()) {
					int count = fightResult.getBattleReward().getAdditionalItemReward().get(itemCode);
					ItemCountStruct itemData = getItemRewardResultData(itemCode, count);
					if (itemData != null) {
						itemRewardShowList.add(itemData);
					}
				}
			}
		}

		// 节假日副本特殊掉落活动奖励
		if (level.getLevelType() == KGameLevelTypeEnum.普通关卡 || level.getLevelType() == KGameLevelTypeEnum.精英副本关卡) {
			TimeLimieProduceActivity activity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.节假副本特殊掉落);

			if (activity != null && activity.isActivityTakeEffectNow()) {
				List<ItemCountStruct> itemList = KExcitingDataManager.mTimeLimitActivityDataManager.caculateHolidayActivityReward(role, level.getLevelType(), level.getLevelId());
				for (ItemCountStruct itemData : itemList) {
					itemRewardList.add(itemData);
					itemRewardShowList.add(itemData);
				}

			}
		}

		itemRewardShowList = ItemCountStruct.mergeItemCountStructs(itemRewardShowList);

		// 处理S级别奖励
		BaseRewardData sLevelReward = null;
		List<ItemCountStruct> sLv_itemRewardList = new ArrayList<ItemCountStruct>();
		if (fightLv == FightEvaluateData.MAX_FIGHT_LEVEL) {
			sLv_itemRewardList.addAll(reward.caculateS_ItemReward(1));
			sLevelReward = new BaseRewardData(reward.s_probableReward.attList, reward.s_probableReward.moneyList, sLv_itemRewardList, Collections.<Integer> emptyList(),
					Collections.<Integer> emptyList());
			KRoleAttrModifyType attrType;
			if (level.getLevelType() == KGameLevelTypeEnum.普通关卡) {
				attrType = KRoleAttrModifyType.关卡奖励;
			} else if (level.getLevelType() == KGameLevelTypeEnum.精英副本关卡) {
				attrType = KRoleAttrModifyType.精英副本奖励;
			} else {
				attrType = KRoleAttrModifyType.技术副本奖励;
			}

			KSupportFactory.getRoleModuleSupport().addAttsFromBaseReward(role, reward.s_probableReward.attList, attrType, level.getLevelId());

			for (KCurrencyCountStruct attReward : reward.s_probableReward.moneyList) {
				processAttributeReward(role, attReward.currencyType, attReward.currencyCount, presentPointType);
				if (attReward.currencyType == KCurrencyTypeEnum.GOLD) {
					resultData.attrAndCurrencyShowData[LevelRewardResultData.GOLD_LINE][LevelRewardResultData.S_ROW] = (int) attReward.currencyCount;
				} else if (attReward.currencyType == KCurrencyTypeEnum.POTENTIAL) {
					resultData.attrAndCurrencyShowData[LevelRewardResultData.POTENTIAL_LINE][LevelRewardResultData.S_ROW] = (int) attReward.currencyCount;
				}
			}
		}

		// 处理数值和货币奖励
		BaseRewardData baseReward = new BaseRewardData(attShowList, moneyShowList, itemRewardShowList, Collections.<Integer> emptyList(), Collections.<Integer> emptyList());
		// KSupportFactory.getRoleModuleSupport().addAttsFromBaseReward(role,
		// attList);
		KSupportFactory.getRoleModuleSupport().addAttsFromBaseReward(role, attList, KRoleAttrModifyType.关卡奖励, level.getLevelId());
		// for (AttValueStruct attr : attList) {
		// if (attr.roleAttType == KGameAttrType.EXPERIENCE) {
		// KSupportFactory.getPetModuleSupport().addExpToFightingPet(role,
		// attr.addValue);
		// }
		// }

		for (KCurrencyCountStruct attReward : moneyList) {
			processAttributeReward(role, attReward.currencyType, attReward.currencyCount, presentPointType);
		}

		// 得出所有的道具奖励，生成等待确认奖励的临时记录
		CompleteGameLevelTempRecord temprecord = new CompleteGameLevelTempRecord(CompleteGameLevelTempRecord.LOTTERY_TYPE_ITEM);
		temprecord.setRoleId(role.getId());
		temprecord.setLevelId(level.getLevelId());
		temprecord.setLevelType(level.getLevelType().levelType);
		List<ItemCountStruct> temprecordItemList = new ArrayList<ItemCountStruct>();
		temprecordItemList.addAll(itemRewardList);
		temprecordItemList.addAll(sLv_itemRewardList);
		temprecordItemList = ItemCountStruct.mergeItemCountStructs(temprecordItemList);
		temprecord.setItemRewardResultDataList(temprecordItemList);
		List<ItemCountStruct> lotteryRewardList = null;
		List<Integer> lotteryRewardUsePointList = null;
		if (reward.isHasLotteryReward()) {
			temprecord.setHasLotteryReward(true);
			// lotteryRewardList =
			// reward.getLotteryReward().getLotteryGroup()
			// .caculateLotteryRewards(role.getLevel());
			List<NormalItemRewardTemplate> lotteryList = reward.lotteryMap.get(role.getJob()).getCaculateItemRewardList();

			lotteryRewardList = reward.lotteryMap.get(role.getJob()).getLotteryRewardShowItemList(lotteryList);
			lotteryRewardUsePointList = getLotteryUsePointList(role, reward);
			temprecord.setLotteryInfo(lotteryList, lotteryRewardUsePointList);
		}
		if (isAddTempRecord) {
			allCompleteGameLevelTempRecord.put(temprecord.roleId, temprecord);
		}

		resultData.baseReward = baseReward;
		resultData.sLevelReward = sLevelReward;
		resultData.lotteryRewardList = lotteryRewardList;
		resultData.lotteryRewardUsePointList = lotteryRewardUsePointList;
		resultData.totalItemSize = itemRewardList.size() + sLv_itemRewardList.size();

		return resultData;
	}

	private int[] caculateVipAddExp(KRole role, int baseExp) {
		VIPLevelData vipData = KSupportFactory.getVIPModuleSupport().getVIPLevelData(role.getId());

		int addExp = 0;
		int addRate = 0;
		if (vipData.lvl > 0) {
			addRate = vipData.dungeonsexpup / 100;
			addExp = (baseExp * addRate) / 100;
		}
		return new int[] { addRate, addExp };
	}

	/**
	 * 处理数值奖励
	 * 
	 * @param role
	 * @param type
	 * @param value
	 */
	public boolean processAttributeReward(KRole role, KCurrencyTypeEnum type, long value, PresentPointTypeEnum presentPointType) {
		// 游戏币奖励
		// if (type == KCurrencyTypeEnum.DIAMOND) {
		// presentType = PresentPointTypeEnum.关卡奖励;
		// }
		long result = KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), type, value, presentPointType, true);
		if (result != -1) {
			return true;
		} else {
			return false;
		}

	}

	// public String caculateSaodangReward(KRole role, KLevelTemplate level,
	// PresentPointTypeEnum presentPointType) {
	// // 检测是否有限时产出活动
	// boolean isCopyActivityPrice = false;
	// float expRate = 1, goldRate = 1, potentialRate = 1;
	// int itemMultiple = 1;
	// if (level.getLevelType() == KGameLevelTypeEnum.技术副本关卡
	// || level.getLevelType() == KGameLevelTypeEnum.精英副本关卡) {
	// TimeLimieProduceActivity activity = KSupportFactory
	// .getExcitingRewardSupport().getTimeLimieProduceActivity(
	// KLimitTimeProduceActivityTypeEnum.精英副本技术副本活动);
	//
	// if (activity != null && activity.isActivityPrice()) {
	// isCopyActivityPrice = true;
	// expRate = activity.expRate;
	// goldRate = activity.goldRate;
	// potentialRate = activity.potentialRate;
	// itemMultiple = activity.itemMultiple;
	// KSupportFactory.getMailModuleSupport().sendSimpleMailBySystem(
	// role.getId(),
	// activity.mailTitle,
	// activity.mailContent);
	// }
	// }
	//
	// KLevelReward reward = level.getReward();
	//
	// StringBuffer buffer = new StringBuffer();
	//
	// // 计算关卡数值奖励
	// List<AttValueStruct> attList = new ArrayList<AttValueStruct>();//
	// reward.probableReward.get(role.getJob()).attList;
	// for (AttValueStruct attReward : reward.probableReward
	// .get(role.getJob()).attList) {
	// int baseValue = attReward.addValue;
	// if (attReward.roleAttType == KGameAttrType.EXPERIENCE) {
	// baseValue = (int) (baseValue * expRate);
	// }
	// attList.add(new AttValueStruct(attReward.roleAttType, baseValue));
	// buffer.append(attReward.roleAttType.getName() + "x" + baseValue
	// + " ");
	// }
	// KRoleAttrModifyType attrType;
	// if (level.getLevelType() == KGameLevelTypeEnum.普通关卡) {
	// attrType = KRoleAttrModifyType.关卡奖励;
	// } else if (level.getLevelType() == KGameLevelTypeEnum.精英副本关卡) {
	// attrType = KRoleAttrModifyType.精英副本奖励;
	// } else {
	// attrType = KRoleAttrModifyType.技术副本奖励;
	// }
	// KSupportFactory.getRoleModuleSupport().addAttsFromBaseReward(role,
	// attList, attrType, level.getLevelId());
	//
	// // 处理关卡货币奖励
	// List<KCurrencyCountStruct> moneyList = new
	// ArrayList<KCurrencyCountStruct>();
	// for (KCurrencyCountStruct attReward : reward.probableReward.get(role
	// .getJob()).moneyList) {
	// long baseValue = attReward.currencyCount;
	// if (attReward.currencyType == KCurrencyTypeEnum.GOLD) {
	// baseValue = (long) (baseValue * goldRate);
	// } else if (attReward.currencyType == KCurrencyTypeEnum.POTENTIAL) {
	// baseValue = (long) (baseValue * potentialRate);
	// }
	// moneyList.add(new KCurrencyCountStruct(attReward.currencyType,
	// baseValue));
	// }
	// for (KCurrencyCountStruct attReward : moneyList) {
	// processAttributeReward(role, attReward.currencyType,
	// attReward.currencyCount, presentPointType);
	// buffer.append(attReward.currencyType.name + "x"
	// + attReward.currencyCount + " ");
	// }
	//
	// ItemModuleSupport itemSupport = KSupportFactory.getItemModuleSupport();
	//
	// // 计算获得道具列表
	// List<ItemCountStruct> itemRewardList = new ArrayList<ItemCountStruct>();
	//
	// // 再处理关卡常规掉落道具
	// if (!reward.getAllItemReward(role.getJob()).isEmpty()) {
	// itemRewardList.addAll(reward.caculateItemReward(role.getJob(),
	// itemMultiple));
	// }
	// // 计算掉落池道具
	// List<ItemCountStruct> dropPoolItems = reward
	// .caculateDropPoolItems(itemMultiple);
	// itemRewardList.addAll(dropPoolItems);
	//
	// for (ItemCountStruct data : itemRewardList) {
	// buffer.append(data.getItemTemplate().extItemName + "x"
	// + data.itemCount + " ");
	// ItemResult_AddItem result = itemSupport.addItemToBag(role, data,
	// this.getClass().getSimpleName());
	// if (!result.isSucess) {
	// // 如果背包满
	// KSupportFactory.getMailModuleSupport().sendAttMailBySystem(
	// role.getId(), data,
	// LevelTips.getTipsSendMailItemForBagFullTitle(),
	// LevelTips.getTipsSendMailItemForBagFull());
	//
	// KDialogService
	// .sendUprisingDialog(role, RewardTips.背包已满奖励通过邮件发送);
	// }
	// }
	//
	// return buffer.toString();
	// }

	public LevelRewardResultData caculateSaodangRewardMoreCounts(KRole role, KLevelTemplate level, PresentPointTypeEnum presentPointType, byte saodangCount) {
		LevelRewardResultData resultData = new LevelRewardResultData(CompleteGameLevelTempRecord.LOTTERY_TYPE_ITEM);

		// 检测是否有限时产出活动
		boolean isCopyActivityPrice = false;
		float expRate = 1, goldRate = 1, potentialRate = 1;
		int itemMultiple = 1;
		if (level.getLevelType() == KGameLevelTypeEnum.技术副本关卡 || level.getLevelType() == KGameLevelTypeEnum.精英副本关卡) {
			TimeLimieProduceActivity activity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.精英副本技术副本活动);

			if (activity != null && activity.isActivityTakeEffectNow()) {
				isCopyActivityPrice = true;
				expRate = activity.expRate;
				goldRate = activity.goldRate;
				potentialRate = activity.potentialRate;
				if (activity.isDropItemDouble) {
					int mRate = UtilTool.random(0, UtilTool.TEN_THOUSAND_RATIO_UNIT);
					if (mRate >= (UtilTool.TEN_THOUSAND_RATIO_UNIT - activity.itemMultipleRate)) {
						itemMultiple = activity.itemMultiple;
						resultData.isDropItemDouble = true;
					}
				}
				KSupportFactory.getMailModuleSupport().sendSimpleMailBySystem(role.getId(), activity.mailTitle, activity.mailContent);
				resultData.isAttrDouble[0] = activity.isExpDouble;
				resultData.isAttrDouble[1] = activity.isGoldDouble;
				resultData.isAttrDouble[2] = activity.isPotentialDouble;
			}
		}

		KLevelReward reward = level.getReward();

		// 计算关卡数值奖励
		List<AttValueStruct> attList = new ArrayList<AttValueStruct>();// reward.probableReward.get(role.getJob()).attList;
		for (int i = 0; i < saodangCount; i++) {
			for (AttValueStruct attReward : reward.probableReward.get(role.getJob()).attList) {
				int baseValue = attReward.addValue;
				if (attReward.roleAttType == KGameAttrType.EXPERIENCE) {
					baseValue = (int) (baseValue * expRate);
				}
				attList.add(new AttValueStruct(attReward.roleAttType, baseValue));
				// buffer.append(attReward.roleAttType.getName() + "x" +
				// baseValue
				// + " ");
			}
		}
		attList = AttValueStruct.mergeCountStructs(attList);

		KRoleAttrModifyType attrType;
		if (level.getLevelType() == KGameLevelTypeEnum.普通关卡) {
			attrType = KRoleAttrModifyType.关卡奖励;
		} else if (level.getLevelType() == KGameLevelTypeEnum.精英副本关卡) {
			attrType = KRoleAttrModifyType.精英副本奖励;
		} else {
			attrType = KRoleAttrModifyType.技术副本奖励;
		}
		KSupportFactory.getRoleModuleSupport().addAttsFromBaseReward(role, attList, attrType, level.getLevelId());

		// 处理关卡货币奖励
		List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();
		for (int i = 0; i < saodangCount; i++) {
			for (KCurrencyCountStruct attReward : reward.probableReward.get(role.getJob()).moneyList) {
				long baseValue = attReward.currencyCount;
				if (attReward.currencyType == KCurrencyTypeEnum.GOLD) {
					baseValue = (long) (baseValue * goldRate);
				} else if (attReward.currencyType == KCurrencyTypeEnum.POTENTIAL) {
					baseValue = (long) (baseValue * potentialRate);
				}
				moneyList.add(new KCurrencyCountStruct(attReward.currencyType, baseValue));
			}
		}
		moneyList = KCurrencyCountStruct.mergeCurrencyCountStructs(moneyList);
		for (KCurrencyCountStruct attReward : moneyList) {
			processAttributeReward(role, attReward.currencyType, attReward.currencyCount, presentPointType);
			// buffer.append(attReward.currencyType.name + "x"
			// + attReward.currencyCount + " ");
		}

		ItemModuleSupport itemSupport = KSupportFactory.getItemModuleSupport();

		// 计算获得道具列表
		List<ItemCountStruct> itemRewardList = new ArrayList<ItemCountStruct>();
		for (int i = 0; i < saodangCount; i++) {
			// 再处理关卡常规掉落道具
			if (!reward.getAllItemReward(role.getJob()).isEmpty()) {
				List<ItemCountStruct> itemList = reward.caculateItemReward(role.getJob(), itemMultiple);
				for (ItemCountStruct itemData : itemList) {
					itemRewardList.add(itemData);
				}
			}
			// 计算掉落池道具
			List<ItemCountStruct> dropPoolItems = reward.caculateDropPoolItems(itemMultiple);
			for (ItemCountStruct itemData : dropPoolItems) {
				itemRewardList.add(itemData);
			}

			// 节假日副本特殊掉落活动奖励
			if (level.getLevelType() == KGameLevelTypeEnum.普通关卡 || level.getLevelType() == KGameLevelTypeEnum.精英副本关卡) {
				TimeLimieProduceActivity activity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.节假副本特殊掉落);

				if (activity != null && activity.isActivityTakeEffectNow()) {
					List<ItemCountStruct> itemList = KExcitingDataManager.mTimeLimitActivityDataManager.caculateHolidayActivityReward(role, level.getLevelType(), level.getLevelId());
					for (ItemCountStruct itemData : itemList) {
						itemRewardList.add(itemData);
					}
				}
			}
		}
		itemRewardList = ItemCountStruct.mergeItemCountStructs(itemRewardList);

		List<ItemCountStruct> lotteryItemRewardList = new ArrayList<ItemCountStruct>();
		if (reward.isHasLotteryReward()) {
			for (int i = 0; i < saodangCount; i++) {
				lotteryItemRewardList.addAll(caculateSaodangLotteryItemList(role, level, reward));
			}
			lotteryItemRewardList = ItemCountStruct.mergeItemCountStructs(lotteryItemRewardList);
		}

		if (itemRewardList.size() > 0) {
			ItemResult_AddItem result = itemSupport.addItemsToBag(role, itemRewardList, presentPointType.name() + "结算奖励");
			if (!result.isSucess) {
				// 如果背包满
				KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), itemRewardList, LevelTips.getTipsSendMailItemForBagFullTitle(), LevelTips.getTipsSendMailItemForBagFull());

				KDialogService.sendUprisingDialog(role, RewardTips.背包已满奖励通过邮件发送);
			}
		}

		ItemResult_AddItem result1 = itemSupport.addItemsToBag(role, lotteryItemRewardList, presentPointType.name() + "结算卡牌抽奖奖励");
		if (!result1.isSucess) {
			// 如果背包满
			KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), lotteryItemRewardList, LevelTips.getTipsSendMailItemForBagFullTitle(), LevelTips.getTipsSendMailItemForBagFull());

			KDialogService.sendUprisingDialog(role, RewardTips.背包已满奖励通过邮件发送);
		}

		BaseRewardData baseReward = new BaseRewardData(attList, moneyList, itemRewardList, null, null);

		resultData.baseReward = baseReward;
		resultData.sLevelReward = null;
		resultData.saodangLotteryReward = new BaseRewardData(null, null, lotteryItemRewardList, null, null);
		resultData.lotteryRewardList = null;
		resultData.lotteryRewardUsePointList = null;
		resultData.totalItemSize = itemRewardList.size() + lotteryItemRewardList.size();

		return resultData;
	}

	private List<ItemCountStruct> caculateSaodangLotteryItemList(KRole role, KLevelTemplate level, KLevelReward reward) {
		List<ItemCountStruct> lotteryItemRewardList = new ArrayList<ItemCountStruct>();
		List<NormalItemRewardTemplate> lotteryList = reward.lotteryMap.get(role.getJob()).getCaculateItemRewardList();
		if (lotteryList.size() > 0) {
			int count = 1;
			if (level.getLevelType() == KGameLevelTypeEnum.普通关卡 && KSupportFactory.getVIPModuleSupport().getVipLv(role.getId()) > 0) {
				count = 2;
			}

			int totalRate = 0;
			Map<Integer, Boolean> lotteryIndexMap = new LinkedHashMap<Integer, Boolean>();
			Map<Integer, Integer> dropIndexMap = new LinkedHashMap<Integer, Integer>();
			AtomicInteger lotteryIndex = new AtomicInteger(0);
			int index = -1;
			L1: for (int k = 0; k < count; k++) {

				for (int i = 0; i < lotteryList.size(); i++) {
					if (lotteryIndexMap.containsKey(i)) {
						continue;
					} else if (lotteryList.get(i).dropProtectCount > lotteryIndex.get()) {
						continue;
					} else {
						totalRate += lotteryList.get(i).getDropWeight();
						dropIndexMap.put(i, totalRate);
					}
				}

				int rate = UtilTool.random(totalRate);
				L2: for (Integer idKey : dropIndexMap.keySet()) {
					if (rate < dropIndexMap.get(idKey)) {
						index = idKey;
						lotteryIndexMap.put(idKey, true);
						break L2;
					}
				}
				if (index >= 0 && index < lotteryList.size()) {
					NormalItemRewardTemplate template = lotteryList.get(index);
					lotteryItemRewardList.add(new ItemCountStruct(template.itemCode, template.rewardCount));
				}
				lotteryIndex.incrementAndGet();
			}
		}

		return lotteryItemRewardList;
	}

	public LevelRewardResultData caculateSaodangReward(KRole role, KLevelTemplate level, PresentPointTypeEnum presentPointType) {
		LevelRewardResultData resultData = new LevelRewardResultData(CompleteGameLevelTempRecord.LOTTERY_TYPE_ITEM);

		// 检测是否有限时产出活动
		boolean isCopyActivityPrice = false;
		float expRate = 1, goldRate = 1, potentialRate = 1;
		int itemMultiple = 1;
		if (level.getLevelType() == KGameLevelTypeEnum.技术副本关卡 || level.getLevelType() == KGameLevelTypeEnum.精英副本关卡) {
			TimeLimieProduceActivity activity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.精英副本技术副本活动);

			if (activity != null && activity.isActivityTakeEffectNow()) {
				isCopyActivityPrice = true;
				expRate = activity.expRate;
				goldRate = activity.goldRate;
				potentialRate = activity.potentialRate;
				if (activity.isDropItemDouble) {
					int mRate = UtilTool.random(0, UtilTool.TEN_THOUSAND_RATIO_UNIT);
					if (mRate >= (UtilTool.TEN_THOUSAND_RATIO_UNIT - activity.itemMultipleRate)) {
						itemMultiple = activity.itemMultiple;
						resultData.isDropItemDouble = true;
					}
				}
				KSupportFactory.getMailModuleSupport().sendSimpleMailBySystem(role.getId(), activity.mailTitle, activity.mailContent);
				resultData.isAttrDouble[0] = activity.isExpDouble;
				resultData.isAttrDouble[1] = activity.isGoldDouble;
				resultData.isAttrDouble[2] = activity.isPotentialDouble;
			}
		}

		KLevelReward reward = level.getReward();

		// 计算关卡数值奖励
		List<AttValueStruct> attList = new ArrayList<AttValueStruct>();// reward.probableReward.get(role.getJob()).attList;
		for (AttValueStruct attReward : reward.probableReward.get(role.getJob()).attList) {
			int baseValue = attReward.addValue;
			if (attReward.roleAttType == KGameAttrType.EXPERIENCE) {
				baseValue = (int) (baseValue * expRate);
			}
			attList.add(new AttValueStruct(attReward.roleAttType, baseValue));
			// buffer.append(attReward.roleAttType.getName() + "x" + baseValue
			// + " ");
		}
		KRoleAttrModifyType attrType;
		if (level.getLevelType() == KGameLevelTypeEnum.普通关卡) {
			attrType = KRoleAttrModifyType.关卡奖励;
		} else if (level.getLevelType() == KGameLevelTypeEnum.精英副本关卡) {
			attrType = KRoleAttrModifyType.精英副本奖励;
		} else {
			attrType = KRoleAttrModifyType.技术副本奖励;
		}
		KSupportFactory.getRoleModuleSupport().addAttsFromBaseReward(role, attList, attrType, level.getLevelId());

		// 处理关卡货币奖励
		List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();
		for (KCurrencyCountStruct attReward : reward.probableReward.get(role.getJob()).moneyList) {
			long baseValue = attReward.currencyCount;
			if (attReward.currencyType == KCurrencyTypeEnum.GOLD) {
				baseValue = (long) (baseValue * goldRate);
			} else if (attReward.currencyType == KCurrencyTypeEnum.POTENTIAL) {
				baseValue = (long) (baseValue * potentialRate);
			}
			moneyList.add(new KCurrencyCountStruct(attReward.currencyType, baseValue));
		}
		for (KCurrencyCountStruct attReward : moneyList) {
			processAttributeReward(role, attReward.currencyType, attReward.currencyCount, presentPointType);
			// buffer.append(attReward.currencyType.name + "x"
			// + attReward.currencyCount + " ");
		}

		ItemModuleSupport itemSupport = KSupportFactory.getItemModuleSupport();

		// 计算获得道具列表
		List<ItemCountStruct> itemRewardList = new ArrayList<ItemCountStruct>();

		// 再处理关卡常规掉落道具
		if (!reward.getAllItemReward(role.getJob()).isEmpty()) {
			List<ItemCountStruct> itemList = reward.caculateItemReward(role.getJob(), itemMultiple);
			for (ItemCountStruct itemData : itemList) {
				itemRewardList.add(itemData);
			}
		}
		// 计算掉落池道具
		List<ItemCountStruct> dropPoolItems = reward.caculateDropPoolItems(itemMultiple);
		for (ItemCountStruct itemData : dropPoolItems) {
			itemRewardList.add(itemData);
		}

		// 节假日副本特殊掉落活动奖励
		if (level.getLevelType() == KGameLevelTypeEnum.普通关卡 || level.getLevelType() == KGameLevelTypeEnum.精英副本关卡) {
			TimeLimieProduceActivity activity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.节假副本特殊掉落);

			if (activity != null && activity.isActivityTakeEffectNow()) {
				List<ItemCountStruct> itemList = KExcitingDataManager.mTimeLimitActivityDataManager.caculateHolidayActivityReward(role, level.getLevelType(), level.getLevelId());
				for (ItemCountStruct itemData : itemList) {
					itemRewardList.add(itemData);
				}
			}
		}

		if (itemRewardList.size() > 0) {
			ItemResult_AddItem result = itemSupport.addItemsToBag(role, itemRewardList, presentPointType.name() + "结算奖励");
			if (!result.isSucess) {
				// 如果背包满
				KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), itemRewardList, LevelTips.getTipsSendMailItemForBagFullTitle(), LevelTips.getTipsSendMailItemForBagFull());

				KDialogService.sendUprisingDialog(role, RewardTips.背包已满奖励通过邮件发送);
			}
		}

		BaseRewardData baseReward = new BaseRewardData(attList, moneyList, itemRewardList, null, null);

		// 得出所有的道具奖励，生成等待确认奖励的临时记录
		CompleteGameLevelTempRecord temprecord = new CompleteGameLevelTempRecord(CompleteGameLevelTempRecord.LOTTERY_TYPE_ITEM);
		temprecord.setRoleId(role.getId());
		temprecord.setLevelId(level.getLevelId());
		temprecord.setLevelType(level.getLevelType().levelType);
		List<ItemCountStruct> temprecordItemList = new ArrayList<ItemCountStruct>();
		temprecord.setItemRewardResultDataList(temprecordItemList);
		List<ItemCountStruct> lotteryRewardList = null;
		List<Integer> lotteryRewardUsePointList = null;
		if (reward.isHasLotteryReward()) {
			temprecord.setHasLotteryReward(true);
			// lotteryRewardList =
			// reward.getLotteryReward().getLotteryGroup()
			// .caculateLotteryRewards(role.getLevel());

			List<NormalItemRewardTemplate> lotteryList = reward.lotteryMap.get(role.getJob()).getCaculateItemRewardList();

			lotteryRewardList = reward.lotteryMap.get(role.getJob()).getLotteryRewardShowItemList(lotteryList);
			lotteryRewardUsePointList = getLotteryUsePointList(role, reward);
			temprecord.setLotteryInfo(lotteryList, lotteryRewardUsePointList);
		}

		allCompleteGameLevelTempRecord.put(temprecord.roleId, temprecord);

		resultData.baseReward = baseReward;
		resultData.sLevelReward = null;
		resultData.lotteryRewardList = lotteryRewardList;
		resultData.lotteryRewardUsePointList = lotteryRewardUsePointList;
		resultData.totalItemSize = itemRewardList.size();

		return resultData;
	}

	/**
	 * 获取ItemRewardResultData
	 * 
	 * @param role
	 *            角色ID
	 * @param itemCode
	 *            道具模版ID
	 * @param count
	 *            添加数量
	 * @return
	 */
	public ItemCountStruct getItemRewardResultData(String itemCode, int count) {
		ItemModuleSupport support = KSupportFactory.getItemModuleSupport();

		KItemTempAbs itemTemplate = support.getItemTemplate(itemCode);

		if (itemTemplate == null) {
			return null;
		} else {
			return new ItemCountStruct(itemTemplate, count);
		}
	}

	public byte caculateLevelFightEvaluate(KRole role, FightResult result, KLevelTemplate level) {
		byte value = -1;
		// byte temp = -1;
		int battleTime = (int) (result.getBattleTime() / 1000);
		int hitCount = result.getMaxDoubleHitCount();
		int beHitCount = result.getMaxBeHitCount();
		for (FightEvaluateData data : level.getFightEvaluateDataMap(role.getJob()).values()) {
			// if (battleTime <= data.fightTime
			// && hitCount >= data.maxHitCount
			// && beHitCount <= data.hitByCount) {
			// value = data.fightLv;
			// }
			if (battleTime > data.fightTime) {
				continue;
			} else if (hitCount < data.maxHitCount) {
				continue;
			} else if (beHitCount > data.hitByCount) {
				continue;
			} else {
				value = data.fightLv;
				break;
			}
		}
		if (value == -1) {
			value = 1;
		}
		return value;
	}

	/**
	 * 发送关卡更新数据
	 * 
	 * @param role
	 * @param scenarioId
	 * @param levelData
	 */
	public void sendUpdateGameLevelInfoMsg(KRole role, KLevelTemplate level, PlayerRoleGamelevelData levelData) {
		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_UPDATE_GAME_LEVEL);
		sendMsg.writeInt(level.getScenarioId());
		sendMsg.writeInt(levelData.getLevelId());

		sendMsg.writeByte(KLevelTemplate.GAME_LEVEL_STATE_OPEN);
		// 完成状态
		byte completeType = levelData.isCompleted() ? ((byte) 1) : ((byte) 0);
		sendMsg.writeByte(completeType);
		// 关卡评价
		sendMsg.writeByte(levelData.getLevelEvaluate());
		// 表示关卡的当天剩余进入次数
		sendMsg.writeInt(levelData.getRemainJoinLevelCount());

		// 是否可以扫荡
		sendMsg.writeBoolean(levelData.isCompleted());

		role.sendMsg(sendMsg);
	}

	public void sendUpdateGameLevelOpenedOrGrayState(KRole role, int scenarioId, int levelId, byte state, int remainJoinCount) {

		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_UPDATE_GAME_LEVEL);
		sendMsg.writeInt(scenarioId);
		sendMsg.writeInt(levelId);
		// 关卡显示状态
		sendMsg.writeByte(state);
		// 完成状态
		sendMsg.writeByte(0);
		// 关卡评价
		sendMsg.writeByte(0);
		// 表示关卡的当天剩余进入次数
		sendMsg.writeInt(remainJoinCount);

		sendMsg.writeBoolean(false);
		// 是否可以扫荡
		sendMsg.writeBoolean(false);

		role.sendMsg(sendMsg);

	}

	// public List<ItemCountStruct> caculateLotteryReward(KRole role,
	// KLevelReward reward) {
	//
	// List<ItemCountStruct> lotteryRewardList = new
	// ArrayList<ItemCountStruct>();
	// for (LotteryGroup group : reward.lotteryMap.get(role.getJob()).values())
	// {
	// ItemCountStruct struct = group.caculateItemReward();
	// if (struct != null) {
	// lotteryRewardList.add(struct);
	// }
	// }
	// return lotteryRewardList;
	// }

	public List<Integer> getLotteryUsePointList(KRole role, KLevelReward reward) {
		return reward.lotteryMap.get(role.getJob()).lotteryGroupUsePointList;
	}

	public void processGetAndSendLotteryInfo(KRole role, boolean isNeedCheck) {
		CompleteGameLevelTempRecord record = allCompleteGameLevelTempRecord.get(role.getId());

		if (record != null && record.isHasLotteryReward) {
			String tips = "";
			// 计算已抽奖次数
			// int getLotteryCount = 0;
			// for (Boolean isGet : record.getIsGetLotteryMap().values()) {
			// if (isGet) {
			// getLotteryCount++;
			// }
			// }
			// 检测是否已超过抽奖次数，如果超过发送提示
			KLevelTemplate level = null;
			if (record.getLevelType() == KGameLevelTypeEnum.普通关卡.levelType) {
				level = getKGameLevel(record.levelId);
			} else if (record.getLevelType() == KGameLevelTypeEnum.精英副本关卡.levelType) {
				level = KGameLevelModuleExtension.getManager().getCopyManager().getCopyLevelTemplate(record.levelId, KGameLevelTypeEnum.精英副本关卡);
			}

			if (isNeedCheck) {
				int index = record.lotteryIndex.get();

				if (index > 0 && record.lotteryType == record.LOTTERY_TYPE_ITEM) {
					if (KSupportFactory.getVIPModuleSupport().getVipLv(role.getId()) < 1) {
						KDialogService.sendUprisingDialog(role, LevelTips.getTipsLotteryNotVip(lotteryVipLevel));
						return;
					}
				}

				if ((record.lotteryType == record.LOTTERY_TYPE_ITEM && record.lotteryList != null && index > (record.lotteryList.size() - 1))
						|| (record.lotteryType == record.LOTTERY_TYPE_CURRENCY && record.lotteryCurrencyList != null && index > (record.lotteryCurrencyList.size() - 1))) {
					KDialogService.sendUprisingDialog(role, LevelTips.getTipsGetLotteryRewardCountFull(index + 1));
					return;
				}
				if (index >= 0) {
					int usePoint = record.usePointList.get(index);
					// if (usePoint > 0) {
					// sendLevelTipsMessage(
					// role,
					// KLevelModuleDialogProcesser.KEY_GET_LOTTORY,
					// LevelTips
					// .getTipsGetLotteryRewardUsePoint(usePoint),
					// true, "");
					// return;
					// }
					if (usePoint > 0) {
						long result = KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), KCurrencyTypeEnum.DIAMOND, usePoint, UsePointFunctionTypeEnum.关卡宝箱抽奖, true);
						// 元宝不足，不能进入关卡，发送提示
						if (result == -1) {
							KDialogService.sendUprisingDialog(role, LevelTips.getTipsGetLotteryNotEnoughIgot(usePoint));
							return;
						}
					}
				}
			}

			byte lotteryId = -1;
			if (record.lotteryType == record.LOTTERY_TYPE_ITEM) {
				lotteryId = (byte) record.getNextLottery();
			} else {
				lotteryId = (byte) record.getNextCurrencyLottery();
			}
			if (lotteryId < 0) {
				KDialogService.sendUprisingDialog(role, GlobalTips.服务器繁忙请稍候再试);
				return;
			}

			KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_GET_LOTTORY);
			sendMsg.writeByte(lotteryId);

			role.sendMsg(sendMsg);
		} else {
			KDialogService.sendSimpleDialog(role, GlobalTips.getTipsDefaultTitle(), GlobalTips.getTipsServerBusy());
		}
	}

	/**
	 * 处理角色获抽奖奖励
	 * 
	 * @param role
	 * @param lotteryId
	 */
	public List<ItemCountStruct> processConfirmGetLottery(KRole role) {
		List<ItemCountStruct> itemList = new ArrayList<ItemCountStruct>();
		CompleteGameLevelTempRecord record = allCompleteGameLevelTempRecord.get(role.getId());
		if (record != null && record.isHasLotteryReward) {
			if (record.lotteryType == CompleteGameLevelTempRecord.LOTTERY_TYPE_ITEM) {
				for (Integer index : record.lotteryIndexMap.keySet()) {
					if (record.lotteryIndexMap.get(index)) {
						NormalItemRewardTemplate template = record.lotteryList.get(index);
						ItemCountStruct reward = new ItemCountStruct(template.getItemCode(), template.getRewardCount());

						// ItemResult_AddItem result = KSupportFactory
						// .getItemModuleSupport().addItemToBag(role,
						// reward);
						// if (!result.isSucess) {
						// KSupportFactory
						// .getMailModuleSupport()
						// .sendAttMailBySystem(
						// role.getId(),
						// reward,
						// LevelTips
						// .getTipsSendMailItemForBagFullTitle(),
						// LevelTips
						// .getTipsSendMailItemForBagFull());
						//
						// KDialogService.sendUprisingDialog(role,
						// RewardTips.背包已满奖励通过邮件发送);
						// }
						itemList.add(reward);
					}
				}
			} else {
				for (Integer index : record.lotteryIndexMap.keySet()) {
					if (record.lotteryIndexMap.get(index)) {
						NormalCurrencyRewardTemplate template = record.lotteryCurrencyList.get(index);
						KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), template.getCurrencyType(), template.getRewardCount(), PresentPointTypeEnum.关卡奖励, true);
					}
				}

				if (record.lotteryIndexList.size() > 0) {
					TimeLimieProduceActivity activity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.赚金币结算卡牌奖励翻倍);
					if (activity != null && activity.isActivityTakeEffectNow() && activity.goldActivityCardId != null && activity.goldActivityCardId.length == record.lotteryCurrencyList.size()) {
						List<KCurrencyCountStruct> currencyList = new ArrayList<KCurrencyCountStruct>();
						boolean isActivitySendMail = false;
						int id = 0;
						for (Integer index : record.lotteryIndexList) {
							if (record.lotteryIndexMap.get(index) && activity.goldActivityCardId[id] > 1f) {
								NormalCurrencyRewardTemplate template = record.lotteryCurrencyList.get(index);
								currencyList.add(new KCurrencyCountStruct(template.getCurrencyType(), (long) (template.getRewardCount() * (activity.goldActivityCardId[id] - 1f))));
								isActivitySendMail = true;
							}
							id++;
						}
						if (isActivitySendMail && currencyList.size() > 0 && activity.isSendMail) {
							currencyList = KCurrencyCountStruct.mergeCurrencyCountStructs(currencyList);
							KSupportFactory.getMailModuleSupport().sendMoneyMailBySystem(role.getId(), currencyList, PresentPointTypeEnum.限时产出活动, activity.mailTitle, activity.mailContent);
						}
					}
				}
			}
		}

		return itemList;
	}

	public int getLotteryUsePoint(KRole role) {
		int result = -1;
		CompleteGameLevelTempRecord record = allCompleteGameLevelTempRecord.get(role.getId());

		if (record != null && record.isHasLotteryReward) {
			int index = record.lotteryIndex.get();
			if (index > (record.lotteryList.size() - 1)) {
				KDialogService.sendSimpleDialog(role, GlobalTips.getTipsDefaultTitle(), LevelTips.getTipsGetLotteryRewardCountFull(index + 1));
				return result;
			}
			result = record.usePointList.get(index);

		}
		return result;
	}

	/**
	 * <pre>
	 * 处理客户端完成关卡结算的确认，并在背包添加奖励道具（暂时规则为背包容量不足够，则全部丢弃）
	 * @param role
	 * @param getReward 是否获取道具奖励，true为获取全部奖励道具，false为全部丢弃
	 * @param levelType 关卡类型，该值参考{@link KGameLevelTypeEnum#LEVEL_TYPE_NORMAL}||
	 *                          {@link KGameLevelTypeEnum#LEVEL_TYPE_12TG_DUPLICATED}||
	 *                          {@link KGameLevelTypeEnum#LEVEL_TYPE_6LH_DUPLICATED}
	 * @param isGetLottory 是否获取抽奖奖励
	 * @param lottoryID    抽奖卡牌ID序号
	 * </pre>
	 */
	public void confirmPlayerRoleCompleteGameLevel(KRole role, int levelId, byte levelType) {
		// 通知战斗模块清除这场战斗的缓存数据
		for (FightEventListener listener : fightEventListenerList) {
			listener.notifyBattleRewardFinished(role);
		}

		// 处理关卡奖励道具的临时记录
		if (allCompleteGameLevelTempRecord.containsKey(role.getId())) {
			// 取出关卡奖励记录
			CompleteGameLevelTempRecord record = allCompleteGameLevelTempRecord.get(role.getId());

			// 判断玩家是否需要获取奖励
			List<ItemCountStruct> itemList = new ArrayList<ItemCountStruct>();
			if (levelId == record.getLevelId()) {

				if (!record.itemRewardResultDataList.isEmpty()) {

					// ItemModuleSupport itemSupport = KSupportFactory
					// .getItemModuleSupport();
					//
					// for (ItemCountStruct data :
					// record.itemRewardResultDataList) {
					// ItemResult_AddItem result = itemSupport.addItemToBag(
					// role, data);
					// if (!result.isSucess) {
					// // 如果背包满
					// KSupportFactory
					// .getMailModuleSupport()
					// .sendAttMailBySystem(
					// role.getId(),
					// data,
					// LevelTips
					// .getTipsSendMailItemForBagFullTitle(),
					// LevelTips
					// .getTipsSendMailItemForBagFull());
					// KDialogService.sendUprisingDialog(role,
					// RewardTips.背包已满奖励通过邮件发送);
					// }
					// }
					itemList.addAll(record.itemRewardResultDataList);

				}
			}
			// 如果没有抽奖，则删除该笔记录
			if (record.isHasLotteryReward) {
				itemList.addAll(processConfirmGetLottery(role));
			}
			if (itemList.size() > 0) {
				ItemCountStruct.mergeItemCountStructs(itemList);

				if (!KSupportFactory.getItemModuleSupport().addItemsToBag(role, itemList, "confirmPlayerRoleCompleteGameLevel()").isSucess) {

					BaseRewardData priceData = new BaseRewardData(Collections.<AttValueStruct> emptyList(), Collections.<KCurrencyCountStruct> emptyList(), itemList,
							Collections.<Integer> emptyList(), Collections.<Integer> emptyList());
					BaseMailContent mailContent = new BaseMailContent(LevelTips.getTipsSendMailItemForBagFullTitle(), LevelTips.getTipsSendMailItemForBagFull(), null, null);
					BaseMailRewardData mailData = new BaseMailRewardData(1, mailContent, priceData);

					mailData.sendReward(role, PresentPointTypeEnum.关卡奖励, true);
					KDialogService.sendUprisingDialog(role, RewardTips.背包已满奖励通过邮件发送);
				}
			}

			allCompleteGameLevelTempRecord.remove(role.getId());
		}

		KGameLevelTypeEnum type = KGameLevelTypeEnum.getEnum(levelType);
		// if (type != null) {
		// switch (type) {
		// case 普通关卡:
		//
		// break;
		// case 精英副本关卡:
		//
		// break;
		//
		// default:
		// break;
		// }
		// }

		if (type == KGameLevelTypeEnum.新手引导关卡) {
			// KSupportFactory.getMissionSupport().nofityForRoleSelectedMission(role,
			// 1);
			// if (levelId == firstNoviceGuideBattle.getLevelId()) {
			KMissionCompleteRecordSet set = KMissionModuleExtension.getMissionCompleteRecordSet(role.getId());
			// if (set.getNoviceGuideRecord().isCompleteFirstGuideBattle) {
			// KMapRoleEventListener.getInstance().notifyRoleJoinMap(role,
			// true);
			// set.completeNoviceGuideStep(KNoviceGuideStepEnum.进入新手营地.type);
			// } else {
			// KSupportFactory.getNoviceGuideSupport()
			// .notifyRoleEnterFirstNoviceGuideBattle(role);
			// }
			set.finishNoviceGuide();
			KMapRoleEventListener.getInstance().notifyRoleJoinMap(role);

			// } else {
			// KGameLevelSet set = KGameLevelModuleExtension
			// .getGameLevelSet(role.getId());
			// if (set != null) {
			// if (set.checkGameLevelIsCompleted(levelId)) {
			//
			// KSupportFactory.getMissionSupport()
			// .nofityForRoleSelectedMission(role, 1);
			// KSupportFactory.getMapSupport()
			// .notifyFinishNoviceGuideBattleAndJumpMap(role);
			// }
			// }
			// }
		} else {
			if (type == KGameLevelTypeEnum.普通关卡) {
				KSupportFactory.getMapSupport().processRoleFinishNormalGameLevelAndReturnToMap(role);
			} else {
				KSupportFactory.getMapSupport().processRoleFinishBattleReturnToMap(role);
			}
			if (type == KGameLevelTypeEnum.精英副本关卡) {
				KLevelTemplate level = KGameLevelModuleExtension.getManager().getCopyManager().getCopyLevelTemplate(levelId, KGameLevelTypeEnum.精英副本关卡);
				if (level != null) {
					if (level.difficulty == 0) {
						KNPCOrderMsg.sendNPCMenuOrder(role, KNPCOrderEnum.ORDER_OPEN_ELITE_COPY, UtilTool.getNotNullString(null));
					} else {
						KNPCOrderMsg.sendNPCMenuOrder(role, KNPCOrderEnum.ORDER_OPEN_TECH_COPY, UtilTool.getNotNullString(null));
					}
				} else {
					KNPCOrderMsg.sendNPCMenuOrder(role, KNPCOrderEnum.ORDER_OPEN_ELITE_COPY, UtilTool.getNotNullString(null));
				}
			} else if (type == KGameLevelTypeEnum.技术副本关卡) {
				KNPCOrderMsg.sendNPCMenuOrder(role, KNPCOrderEnum.ORDER_OPEN_TECH_COPY, UtilTool.getNotNullString(null));
			} else if (type == KGameLevelTypeEnum.好友副本关卡) {
				KNPCOrderMsg.sendNPCMenuOrder(role, KNPCOrderEnum.ORDER_OPEN_FRIEND_COPY, UtilTool.getNotNullString(null));
			} else if (type == KGameLevelTypeEnum.爬塔副本关卡) {
				KNPCOrderMsg.sendNPCMenuOrder(role, KNPCOrderEnum.ORDER_OPEN_TOWER_COPY, UtilTool.getNotNullString(null));
			} else if (type == KGameLevelTypeEnum.随从挑战副本关卡) {
				petChallengeCopyManager.confirmCompleteAndExitLevel(role, levelId);
				KNPCOrderMsg.sendNPCMenuOrder(role, KNPCOrderEnum.ORDER_OPEN_PET_CHALLENGE_COPY, UtilTool.getNotNullString(null));
			} else if (type == KGameLevelTypeEnum.高级随从挑战副本关卡) {
				seniorPetChallengeCopyManager.confirmCompleteAndExitLevel(role, levelId);
				KNPCOrderMsg.sendNPCMenuOrder(role, KNPCOrderEnum.ORDER_OPEN_SENIOR_PET_CHALLENGE_COPY, UtilTool.getNotNullString(null));
			}
		}

	}

	/**
	 * <pre>
	 * 处理客户端完成关卡关卡扫荡，并将奖励发往背包
	 * @param role
	 * </pre>
	 */
	public void confirmPlayerRoleCompleteSaodang(KRole role) {

		// 处理关卡奖励道具的临时记录
		if (allCompleteGameLevelTempRecord.containsKey(role.getId())) {
			// 取出关卡奖励记录
			CompleteGameLevelTempRecord record = allCompleteGameLevelTempRecord.get(role.getId());

			// 判断玩家是否需要获取奖励
			List<ItemCountStruct> itemList = new ArrayList<ItemCountStruct>();

			if (!record.itemRewardResultDataList.isEmpty()) {

				itemList.addAll(record.itemRewardResultDataList);

			}

			// 如果没有抽奖，则删除该笔记录
			if (record.isHasLotteryReward) {
				itemList.addAll(processConfirmGetLottery(role));
			}
			if (itemList.size() > 0) {
				ItemCountStruct.mergeItemCountStructs(itemList);

				if (!KSupportFactory.getItemModuleSupport().addItemsToBag(role, itemList, "confirmPlayerRoleCompleteGameLevel()").isSucess) {

					BaseRewardData priceData = new BaseRewardData(Collections.<AttValueStruct> emptyList(), Collections.<KCurrencyCountStruct> emptyList(), itemList,
							Collections.<Integer> emptyList(), Collections.<Integer> emptyList());
					BaseMailContent mailContent = new BaseMailContent(LevelTips.getTipsSendMailItemForBagFullTitle(), LevelTips.getTipsSendMailItemForBagFull(), null, null);
					BaseMailRewardData mailData = new BaseMailRewardData(1, mailContent, priceData);

					mailData.sendReward(role, PresentPointTypeEnum.关卡奖励, true);
					KDialogService.sendUprisingDialog(role, RewardTips.背包已满奖励通过邮件发送);
				}
			}

			allCompleteGameLevelTempRecord.remove(role.getId());
		}
	}

	/**
	 * 发送进入关卡的提示信息
	 * 
	 * @param isCanJoin
	 *            ，能否进入关卡的状态
	 * @param tips
	 *            提示信息
	 */
	public void sendLevelTipsMessage(KRole role, short confirmKey, String tips, boolean isHasConfirmButton, String script) {

		List<KDialogButton> list = new ArrayList<KDialogButton>();
		if (isHasConfirmButton) {
			list.add(KDialogButton.CANCEL_BUTTON);
			list.add(new KDialogButton(confirmKey, script, KDialogButton.CONFIRM_DISPLAY_TEXT));
		} else {
			list.add(KDialogButton.CONFIRM_BUTTON);
		}

		KDialogService.sendFunDialog(role, GlobalTips.getTipsDefaultTitle(), tips, list, true, (byte) -1);
	}

	public void processNormalLevelSaodang(KRole role, int levelId, byte saodangCount) {
		// TODO 检测体力
		KLevelTemplate level = this.getKGameLevel(levelId);
		if (level == null) {
			sendSaodangFailedMsg(role);
			KDialogService.sendUprisingDialog(role, LevelTips.getTipsProcessDataError());
			return;
		}

		// 获取关卡记录
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());

		PlayerRoleGamelevelData levelData = levelSet.getPlayerRoleNormalGamelevelData(level.getLevelId());
		if (levelData == null || !levelData.isCompleted()) {
			// 该关卡未完成
			sendSaodangFailedMsg(role);
			KDialogService.sendUprisingDialog(role, LevelTips.getTipsCannotSaodang());
			return;
		}

		if (saodangCount < 1) {
			// 该关卡未完成
			sendSaodangFailedMsg(role);
			KDialogService.sendUprisingDialog(role, GlobalTips.服务器繁忙请稍候再试);
			return;
		}

		int usePhyPower = level.getEnterCondition().getUseStamina() * saodangCount;
		if (role.getPhyPower() < usePhyPower) {
			// TODO 发送购买体力提示
			// KDialogService.sendUprisingDialog(role,
			// LevelTips.getTipsSaodangStaminaNotEnough());
			sendSaodangFailedMsg(role);
			checkAndSendPhyPowerNotEnoughDialog(role);
			return;
		}
		// 扣除体力
		// KSupportFactory.getRoleModuleSupport().decreasePhyPower(role,
		// usePhyPower);
		KSupportFactory.getRoleModuleSupport().decreasePhyPower(role, usePhyPower, "普通关卡扫荡扣除体力");

		// 计算扫荡成功率
		// int successRate = (role.getBattlePower() * 100)
		// / (level.getFightPower());
		// if (successRate > 100) {
		// successRate = 100;
		// }
		// List<KActionResult> resultList = new ArrayList<KActionResult>();
		// for (int i = 0; i < saodangCount; i++) {
		// boolean saodangSuccess = successRate >= (UtilTool.random(0, 100));
		// if (saodangSuccess) {
		// String tips = caculateSaodangReward(role, level,
		// PresentPointTypeEnum.关卡奖励);
		// resultList.add(new KActionResult(true, tips));
		// } else {
		// resultList.add(new KActionResult(false, LevelTips
		// .getTipsSaodangNothing()));
		// }
		// }

		if (saodangCount == 1) {
			LevelRewardResultData rewardData = caculateSaodangReward(role, level, PresentPointTypeEnum.关卡奖励);

			KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_RESPONSE_SAODANG_DATA);
			sendMsg.writeBoolean(true);
			rewardData.baseReward.packMsg(sendMsg);
			// 处理发送抽奖信息
			if (rewardData.lotteryRewardList != null && rewardData.lotteryRewardUsePointList != null && rewardData.lotteryRewardList.size() > 0) {
				sendMsg.writeBoolean(true);
				sendMsg.writeByte(rewardData.lotteryRewardList.size());
				sendMsg.writeInt(rewardData.lotteryRewardList.size());
				for (int i = 0; i < rewardData.lotteryRewardList.size(); i++) {
					ItemCountStruct lotteryReward = rewardData.lotteryRewardList.get(i);
					sendMsg.writeByte(i);
					sendMsg.writeByte(2);
					KItemMsgPackCenter.packItem(sendMsg, lotteryReward.getItemTemplate(), lotteryReward.itemCount);
				}
				for (int i = 0; i < rewardData.lotteryRewardUsePointList.size(); i++) {
					sendMsg.writeInt(rewardData.lotteryRewardUsePointList.get(i));
				}
			} else {
				sendMsg.writeBoolean(false);
			}
			role.sendMsg(sendMsg);
			KDialogService.sendNullDialog(role);

			FightResult fightResult = new FightResult();
			fightResult.setWin(true);
			// 通知战场监听器通知关卡完成
			for (FightEventListener listener : fightEventListenerList) {
				listener.notifyGameLevelCompleted(role, level, fightResult);
			}
			// 通知活跃度模块
			KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.通关普通副本);
		} else {
			LevelRewardResultData rewardData = caculateSaodangRewardMoreCounts(role, level, PresentPointTypeEnum.关卡奖励, saodangCount);

			KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_RESPONSE_SAODANG_DATA1);
			sendMsg.writeBoolean(true);
			rewardData.baseReward.packMsg(sendMsg);
			rewardData.saodangLotteryReward.packMsg(sendMsg);
			role.sendMsg(sendMsg);

			KDialogService.sendNullDialog(role);

			FightResult fightResult = new FightResult();
			fightResult.setWin(true);
			// 通知战场监听器通知关卡完成
			for (int i = 0; i < saodangCount; i++) {
				for (FightEventListener listener : fightEventListenerList) {
					listener.notifyGameLevelCompleted(role, level, fightResult);
				}
				// 通知活跃度模块
				KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.通关普通副本);
			}
		}
	}

	public int checkSaodangSuccessRate(KRole role, KLevelTemplate level) {
		int successRate = (role.getBattlePower() * 100) / (level.getFightPower());
		if (successRate > 100) {
			successRate = 100;
		}
		return successRate;
	}

	public static void sendSaodangFailedMsg(KRole role) {
		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_RESPONSE_SAODANG_DATA);
		sendMsg.writeBoolean(false);
		role.sendMsg(sendMsg);
	}

	/**
	 * 获取角色下一等级的升级所需经验，如果角色当前等级已经为最大等级，则返回-1
	 * 
	 * @param role
	 * @return
	 */
	public static int getNextRoleLvUpgradeExp(KRole role) {
		if (role.getLevel() == KRoleModuleConfig.getRoleMaxLv()) {
			return -1;
		} else {
			return KSupportFactory.getRoleModuleSupport().getUpgradeExp(role.getLevel() + 1);
		}
	}

	public KGameBattlefield getWorldBossBattlefield(int levelId) {
		return worldBossBattlefields.get(levelId);
	}

	public KGameBattlefield getFamilyWarBattlefield(int levelId) {
		return familyWarBattlefields.get(levelId);
	}

	public KGameBattlefield getNoviceGuideBattlefield() {
		return firstNoviceGuideBattle;
	}

	/**
	 * 发送新手引导战斗（第一场）的结束界面消息
	 * 
	 * @param role
	 */
	public void sendNoviceGuideBattleResultMsg(KRole role, FightResult result) {
		BaseRewardData baseReward = firstNoviceGuideBattleReward.probableReward.get(role.getJob());
		if (result.isWin()) {
			for (KCurrencyCountStruct struct : baseReward.moneyList) {
				processAttributeReward(role, struct.currencyType, struct.currencyCount, PresentPointTypeEnum.关卡奖励);
			}
			KSupportFactory.getItemModuleSupport().addItemsToBag(role, baseReward.itemStructs, "新手引导关卡奖励道具");
			KSupportFactory.getRoleModuleSupport().addAttsFromBaseReward(role, baseReward.attList, KRoleAttrModifyType.关卡奖励, firstNormalGameLevel.getLevelId());
		}

		// 得出所有的道具奖励，生成等待确认奖励的临时记录
		CompleteGameLevelTempRecord temprecord = new CompleteGameLevelTempRecord(CompleteGameLevelTempRecord.LOTTERY_TYPE_ITEM);
		temprecord.setRoleId(role.getId());
		temprecord.setLevelId(firstNoviceGuideBattle.getLevelId());
		temprecord.setLevelType(KGameLevelTypeEnum.新手引导关卡.levelType);
		List<ItemCountStruct> temprecordItemList = new ArrayList<ItemCountStruct>();
		temprecord.setItemRewardResultDataList(temprecordItemList);
		List<ItemCountStruct> lotteryRewardList = null;
		List<Integer> lotteryRewardUsePointList = null;
		if (firstNoviceGuideBattleReward.isHasLotteryReward()) {
			temprecord.setHasLotteryReward(true);
			// lotteryRewardList =
			// reward.getLotteryReward().getLotteryGroup()
			// .caculateLotteryRewards(role.getLevel());
			List<NormalItemRewardTemplate> lotteryList = firstNoviceGuideBattleReward.lotteryMap.get(role.getJob()).getCaculateItemRewardList();

			lotteryRewardList = firstNoviceGuideBattleReward.lotteryMap.get(role.getJob()).getLotteryRewardShowItemList(lotteryList);
			lotteryRewardUsePointList = getLotteryUsePointList(role, firstNoviceGuideBattleReward);
			temprecord.setLotteryInfo(lotteryList, lotteryRewardUsePointList);
		}

		allCompleteGameLevelTempRecord.put(temprecord.roleId, temprecord);

		// 发送完成关卡消息
		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_SHOW_COMPLETE_GAME_LEVEL_RESULT);
		sendMsg.writeInt(firstNoviceGuideBattle.getLevelId());
		sendMsg.writeByte(KGameLevelTypeEnum.新手引导关卡.levelType);
		sendMsg.writeBoolean(result.isWin());
		if (result.isWin()) {
			sendMsg.writeByte(5);
			sendMsg.writeInt((int) (result.getBattleTime() / 1000));
			sendMsg.writeShort(result.getMaxBeHitCount()); // 2015-01-29
															// 修改：这里应该是受击次数
			sendMsg.writeInt(result.getTotalDamage());
			String tips = "";
			sendMsg.writeUtf8String(tips);
			sendMsg.writeUtf8String("");
			sendMsg.writeUtf8String("");

			sendMsg.writeInt(getNextRoleLvUpgradeExp(role));
			// 经验加成百分比
			sendMsg.writeByte(0);
			// //无基础奖励
			baseReward.packMsg(sendMsg);
			sendMsg.writeBoolean(false);
			// 无S级别奖励
			sendMsg.writeBoolean(false);
			// 无抽奖数据
			// 经验、货币结算显示
			int[][] attrAndCurrencyShowData = new int[3][4];
			for (AttValueStruct attReward : baseReward.attList) {
				KGameAttrType attrType = attReward.roleAttType;
				int value = attReward.addValue;
				if (attReward.roleAttType == KGameAttrType.EXPERIENCE) {
					attrAndCurrencyShowData[LevelRewardResultData.EXP_LINE][LevelRewardResultData.NORMAL_ROW] = value;
				}
			}
			for (KCurrencyCountStruct attReward : baseReward.moneyList) {
				if (attReward.currencyType == KCurrencyTypeEnum.GOLD) {
					attrAndCurrencyShowData[LevelRewardResultData.GOLD_LINE][LevelRewardResultData.NORMAL_ROW] = (int) attReward.currencyCount;
				} else if (attReward.currencyType == KCurrencyTypeEnum.POTENTIAL) {
					attrAndCurrencyShowData[LevelRewardResultData.POTENTIAL_LINE][LevelRewardResultData.NORMAL_ROW] = (int) attReward.currencyCount;
				}
			}
			for (int i = 0; i < attrAndCurrencyShowData.length; i++) {
				sendMsg.writeBoolean(false);
				for (int j = 0; j < attrAndCurrencyShowData[i].length; j++) {
					sendMsg.writeInt(attrAndCurrencyShowData[i][j]);
				}
			}
			// 处理发送抽奖信息
			if (firstNoviceGuideBattleReward.isHasLotteryReward() && lotteryRewardList != null && lotteryRewardUsePointList != null && lotteryRewardList.size() > 0) {
				sendMsg.writeBoolean(true);
				sendMsg.writeByte(lotteryRewardList.size());
				sendMsg.writeInt(lotteryRewardList.size());
				for (int i = 0; i < lotteryRewardList.size(); i++) {
					ItemCountStruct lotteryReward = lotteryRewardList.get(i);
					sendMsg.writeByte(i);
					sendMsg.writeByte(2);
					KItemMsgPackCenter.packItem(sendMsg, lotteryReward.getItemTemplate(), lotteryReward.itemCount);
				}
				for (int i = 0; i < lotteryRewardUsePointList.size(); i++) {
					sendMsg.writeInt(lotteryRewardUsePointList.get(i));
				}
			} else {
				sendMsg.writeBoolean(false);
			}
		}

		role.sendMsg(sendMsg);
	}

	public void processGetScenarioPassItem(KRole role, int scenarioId) {
		KGameScenario scenario = this.getKGameScenario(scenarioId);
		KGameLevelSet levelSet = KGameLevelModuleExtension.getGameLevelSet(role.getId());
		if (levelSet.checkHasGetScenarioPrice(scenarioId)) {
			// _LOGGER.error("角色获取剧本首通奖励失败，剧本数据为null，剧本ID：" + scenarioId +
			// "角色ID："
			// + role.getId());
			sendGetScenarioPassItemMsg(role, scenarioId, false, LevelTips.getTipsAlreadyGetScenarioPrice());
			return;
		}

		if (scenario == null) {
			_LOGGER.error("角色获取剧本首通奖励失败，剧本数据为null，剧本ID：" + scenarioId + "角色ID：" + role.getId());
			sendGetScenarioPassItemMsg(role, scenarioId, false, GlobalTips.getTipsServerBusy());
			return;
		}

		KGameScenarioReward reward = scenario.getReward();
		if (reward == null) {
			_LOGGER.error("角色获取剧本首通奖励失败，剧本首通奖励数据为null，剧本ID：" + scenarioId + "角色ID：" + role.getId());
			sendGetScenarioPassItemMsg(role, scenarioId, false, GlobalTips.getTipsServerBusy());
			return;
		}

		boolean isCanGet = true;

		for (KLevelTemplate level : scenario.getAllGameLevel()) {
			if (!levelSet.checkGameLevelIsCompleted(level.getLevelId())) {
				isCanGet = false;
				break;
			}
		}
		if (!isCanGet) {
			sendGetScenarioPassItemMsg(role, scenarioId, false, LevelTips.getTipsCarnnotGetScenarioPrice());
			return;
		} else {
			ItemModuleSupport itemSupport = KSupportFactory.getItemModuleSupport();
			List<ItemCountStruct> itemList = reward.getAllItems();
			if (!itemList.isEmpty()) {
				if (!itemSupport.isCanAddItemsToBag(role.getId(), itemList)) {
					BaseRewardData priceData = new BaseRewardData(Collections.<AttValueStruct> emptyList(), Collections.<KCurrencyCountStruct> emptyList(), itemList,
							Collections.<Integer> emptyList(), Collections.<Integer> emptyList());
					BaseMailContent mailContent = new BaseMailContent(LevelTips.getTipsGetScenarioPassItemBagFullMailTitle(), LevelTips.getTipsGetScenarioPassItemBagFull(), null, null);
					BaseMailRewardData mailData = new BaseMailRewardData(1, mailContent, priceData);

					mailData.sendReward(role, PresentPointTypeEnum.关卡奖励, true);
					KDialogService.sendUprisingDialog(role, LevelTips.getTipsGetScenarioPassItemBagFull());

				} else {
					itemSupport.addItemsToBag(role, itemList, "processGetScenarioPassItem()");
				}
			}

			levelSet.recordGetScenarioPrice(scenarioId);
			List<String> tips = new ArrayList<String>();

			for (ItemCountStruct data : itemList) {
				tips.add(data.getItemTemplate().extItemName + "x" + data.itemCount + " ");
			}

			sendGetScenarioPassItemMsg(role, scenarioId, true, "");
			KDialogService.sendDataUprisingDialog(role, tips);
		}
	}

	private void sendGetScenarioPassItemMsg(KRole role, int scenarioId, boolean isGetSuccess, String tips) {
		// 发送完成关卡消息
		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_GET_SCENARIO_PASS_ITEM);
		sendMsg.writeInt(scenarioId);
		sendMsg.writeBoolean(isGetSuccess);
		sendMsg.writeUtf8String(tips);
		role.sendMsg(sendMsg);
	}

	public void processGetScenarioSLevelItem(KRole role, int scenarioId) {
		KGameScenario scenario = this.getKGameScenario(scenarioId);
		KGameLevelSet levelSet = KGameLevelModuleExtension.getGameLevelSet(role.getId());
		if (levelSet.checkHasGetScenarioSLevelPrice(scenarioId)) {
			// _LOGGER.error("角色获取剧本首通奖励失败，剧本数据为null，剧本ID：" + scenarioId +
			// "角色ID："
			// + role.getId());
			sendGetScenarioSLevelItemMsg(role, scenarioId, false, LevelTips.getTipsAlreadyGetScenarioSLevelPrice());
			return;
		}

		if (scenario == null) {
			_LOGGER.error("角色获取剧本首通奖励失败，剧本数据为null，剧本ID：" + scenarioId + "角色ID：" + role.getId());
			sendGetScenarioSLevelItemMsg(role, scenarioId, false, GlobalTips.getTipsServerBusy());
			return;
		}

		KGameScenarioReward reward = scenario.getS_reward();
		if (reward == null) {
			_LOGGER.error("角色获取剧本首通奖励失败，剧本首通奖励数据为null，剧本ID：" + scenarioId + "角色ID：" + role.getId());
			sendGetScenarioSLevelItemMsg(role, scenarioId, false, GlobalTips.getTipsServerBusy());
			return;
		}

		boolean isCanGet = true;

		for (KLevelTemplate level : scenario.getAllGameLevel()) {
			PlayerRoleGamelevelData levelData = levelSet.getPlayerRoleNormalGamelevelData(level.getLevelId());
			if (levelData == null || !levelData.isCompleted() || levelData.getLevelEvaluate() != FightEvaluateData.MAX_FIGHT_LEVEL) {
				isCanGet = false;
				break;
			}
		}
		if (!isCanGet) {
			sendGetScenarioSLevelItemMsg(role, scenarioId, false, LevelTips.getTipsCarnnotGetScenarioSLevelPrice());
			return;
		} else {
			ItemModuleSupport itemSupport = KSupportFactory.getItemModuleSupport();
			List<ItemCountStruct> itemList = reward.getAllItems();
			if (!itemList.isEmpty()) {
				if (!itemSupport.isCanAddItemsToBag(role.getId(), itemList)) {
					BaseRewardData priceData = new BaseRewardData(Collections.<AttValueStruct> emptyList(), Collections.<KCurrencyCountStruct> emptyList(), itemList,
							Collections.<Integer> emptyList(), Collections.<Integer> emptyList());
					BaseMailContent mailContent = new BaseMailContent(LevelTips.getTipsGetScenarioSLevelItemBagFullMailTitle(), LevelTips.getTipsGetScenarioSLevelItemBagFull(), null, null);
					BaseMailRewardData mailData = new BaseMailRewardData(1, mailContent, priceData);

					mailData.sendReward(role, PresentPointTypeEnum.关卡奖励, true);
					KDialogService.sendUprisingDialog(role, LevelTips.getTipsGetScenarioSLevelItemBagFull());

				} else {
					itemSupport.addItemsToBag(role, itemList, "processGetScenarioSLevelItem()");
				}
			}

			levelSet.recordGetScenarioSLevelPrice(scenarioId);
			List<String> tips = new ArrayList<String>();

			for (ItemCountStruct data : itemList) {
				tips.add(data.getItemTemplate().extItemName + "x" + data.itemCount + " ");
			}
			sendGetScenarioSLevelItemMsg(role, scenarioId, true, "");
			KDialogService.sendDataUprisingDialog(role, tips);

			// 世界广播
			String content = KWordBroadcastType.章节S奖励_XXX奋勇杀敌X章中达到所有S评级.content;
			String scenarioName = HyperTextTool.extColor(scenario.getScenarioName(), KColorFunEnum.品质_蓝);
			content = StringUtil.format(content, role.getExName(), scenarioName);
			KSupportFactory.getChatSupport().sendSystemChat(content, KWordBroadcastType.章节S奖励_XXX奋勇杀敌X章中达到所有S评级);
		}
	}

	private void sendGetScenarioSLevelItemMsg(KRole role, int scenarioId, boolean isGetSuccess, String tips) {
		// 发送完成关卡消息
		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_GET_SCENARIO_S_ITEM);
		sendMsg.writeInt(scenarioId);
		sendMsg.writeBoolean(isGetSuccess);
		sendMsg.writeUtf8String(tips);
		role.sendMsg(sendMsg);
	}

	public void checkAndSendPhyPowerNotEnoughDialog(KRole role) {
		VIPLevelData vipData = KSupportFactory.getVIPModuleSupport().getVIPLevelData(role.getId());
		int restBuyCount = PhyPowerShopCenter.getBuyReleaseTime(role.getId());

		long itemId = 0;
		List<KItem> phyItemList = PhyPowerShopCenter.searchPhyPowerItemCount(role.getId());
		int restPhyItemCount = 0;
		if (phyItemList.size() > 0) {
			for (KItem item : phyItemList) {
				restPhyItemCount += item.getCount();
				if (itemId == 0) {
					itemId = item.getId();
				}
			}
		}
		String tips3 = "";
		byte buttonType = 0;
		if (restBuyCount > 0) {
			tips3 = LevelTips.getTipsSaodangStaminaNotEnoughDialogTips4(vipData.lvl, restBuyCount);
		} else if (vipData.lvl == KVIPDataManager.mVIPLevelDataManager.getMaxLevel().lvl) {
			tips3 = LevelTips.getTipsSaodangStaminaNotEnoughDialogTips4(vipData.lvl, restBuyCount);
		} else if (vipData.lvl == 0) {
			tips3 = LevelTips.getTipsSaodangStaminaNotEnoughDialogTips3(LevelTips.getTipsSaodangStaminaNotEnoughDialogTips6());
			buttonType = 1;
		} else {
			tips3 = LevelTips.getTipsSaodangStaminaNotEnoughDialogTips3(LevelTips.getTipsSaodangStaminaNotEnoughDialogTips5(vipData.lvl, restBuyCount));
			buttonType = 1;
		}

		// 发送完成关卡消息
		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_SHOW_PHY_POWER_NOT_ENOUGH_DIALOG);
		sendMsg.writeUtf8String(LevelTips.getTipsSaodangStaminaNotEnoughDialogTips1());
		sendMsg.writeUtf8String(LevelTips.getTipsSaodangStaminaNotEnoughDialogTips2(restPhyItemCount));
		sendMsg.writeUtf8String(tips3);
		sendMsg.writeLong(itemId);
		sendMsg.writeByte(buttonType);
		role.sendMsg(sendMsg);

		KDialogService.sendNullDialog(role);
	}

	// public int[][] caculateBattleResultShowData(){
	//
	// }

	/**
	 * 地图事件ID生成器
	 * 
	 * @author Administrator
	 * 
	 */
	public static class BattlefieldIdGenerator {
		private AtomicInteger id;

		public BattlefieldIdGenerator(int initialValue) {
			id = new AtomicInteger(initialValue);
		}

		public int currentBattlefieldId() {
			return id.get();
		}

		public int nextBattlefieldId() {
			return id.incrementAndGet();
		}
	}

	/**
	 * 角色完成关卡道具奖励临时记录
	 * 
	 * @author zhaizl
	 * 
	 */
	public static class CompleteGameLevelTempRecord {

		public static final byte LOTTERY_TYPE_CURRENCY = 1;

		public static final byte LOTTERY_TYPE_ITEM = 2;

		public byte lotteryType;

		private int levelId;

		public byte levelType;

		private long roleId;

		private List<ItemCountStruct> itemRewardResultDataList;

		public long completeTimeMillis = System.currentTimeMillis();

		private boolean isHasLotteryReward = false;

		private List<Integer> usePointList;

		private List<NormalItemRewardTemplate> lotteryList;

		private List<NormalCurrencyRewardTemplate> lotteryCurrencyList;

		public AtomicInteger lotteryIndex = new AtomicInteger(0);

		public Map<Integer, Boolean> lotteryIndexMap = new LinkedHashMap<Integer, Boolean>();

		public List<Integer> lotteryIndexList = new ArrayList<Integer>();

		public CompleteGameLevelTempRecord(byte lotteryType) {
			this.lotteryType = lotteryType;
		}

		public int getLevelId() {
			return levelId;
		}

		public void setLevelId(int levelId) {
			this.levelId = levelId;
		}

		public long getRoleId() {
			return roleId;
		}

		public void setRoleId(long roleId) {
			this.roleId = roleId;
		}

		public List<ItemCountStruct> getItemRewardResultDataList() {
			return itemRewardResultDataList;
		}

		public void setItemRewardResultDataList(List<ItemCountStruct> itemRewardResultDataList) {
			this.itemRewardResultDataList = itemRewardResultDataList;
		}

		public void setHasLotteryReward(boolean isHasLotteryReward) {
			this.isHasLotteryReward = isHasLotteryReward;
		}

		public boolean isHasLotteryReward() {
			return isHasLotteryReward;
		}

		public List<Integer> getUsePointList() {
			return usePointList;
		}

		public List<NormalItemRewardTemplate> getLotteryList() {
			return lotteryList;
		}

		public List<NormalCurrencyRewardTemplate> getLotteryCurrencyList() {
			return lotteryCurrencyList;
		}

		public void setLotteryInfo(List<NormalItemRewardTemplate> lotteryList, List<Integer> usePointList) {
			this.lotteryList = lotteryList;
			this.usePointList = usePointList;
			for (int i = 0; i < lotteryList.size(); i++) {
				lotteryIndexMap.put(i, false);
			}
		}

		public void setLotteryCurrencyInfo(List<NormalCurrencyRewardTemplate> lotteryList, List<Integer> usePointList) {
			this.lotteryCurrencyList = lotteryList;
			this.usePointList = usePointList;
			for (int i = 0; i < lotteryList.size(); i++) {
				lotteryIndexMap.put(i, false);
			}
		}

		public byte getLevelType() {
			return levelType;
		}

		public void setLevelType(byte levelType) {
			this.levelType = levelType;
		}

		public int getNextLottery() {
			// int index = lotteryIndex.get();
			// lotteryIndex.incrementAndGet();

			int index = -1;

			int totalRate = 0;
			Map<Integer, Integer> dropIndexMap = new LinkedHashMap<Integer, Integer>();
			for (int i = 0; i < lotteryList.size(); i++) {
				if (lotteryIndexMap.get(i)) {
					continue;
				} else if (lotteryList.get(i).dropProtectCount > lotteryIndex.get()) {
					continue;
				} else {
					totalRate += lotteryList.get(i).getDropWeight();
					dropIndexMap.put(i, totalRate);
				}
			}

			int rate = UtilTool.random(totalRate);
			if (rate > 0) {
				for (Integer idKey : dropIndexMap.keySet()) {
					if (rate < dropIndexMap.get(idKey)) {
						index = idKey;
						lotteryIndexMap.put(idKey, true);
						lotteryIndexList.add(index);
						break;
					}
				}
			}
			if (index > -1) {
				lotteryIndex.incrementAndGet();
			}

			return index;
		}

		public int getNextCurrencyLottery() {
			// int index = lotteryIndex.get();
			// lotteryIndex.incrementAndGet();

			int index = -1;

			int totalRate = 0;
			Map<Integer, Integer> dropIndexMap = new LinkedHashMap<Integer, Integer>();
			for (int i = 0; i < lotteryCurrencyList.size(); i++) {
				if (lotteryIndexMap.get(i)) {
					continue;
				} else if (lotteryCurrencyList.get(i).dropProtectCount > lotteryIndex.get()) {
					continue;
				} else {
					totalRate += lotteryCurrencyList.get(i).getDropWeight();
					dropIndexMap.put(i, totalRate);
				}
			}

			int rate = UtilTool.random(totalRate);
			for (Integer idKey : dropIndexMap.keySet()) {
				if (rate < dropIndexMap.get(idKey)) {
					index = idKey;
					lotteryIndexMap.put(idKey, true);
					lotteryIndexList.add(index);
					break;
				}
			}
			lotteryIndex.incrementAndGet();

			return index;
		}
	}

	public static class LevelRewardResultData {

		public static final int EXP_LINE = 0;
		public static final int GOLD_LINE = 1;
		public static final int POTENTIAL_LINE = 2;

		public static final int NORMAL_ROW = 0;
		public static final int VIP_ROW = 1;
		public static final int GANG_ROW = 2;
		public static final int S_ROW = 3;

		public byte lotteryType;
		public BaseRewardData baseReward;
		public BaseRewardData sLevelReward;
		public BaseRewardData saodangLotteryReward;
		public List<ItemCountStruct> lotteryRewardList;
		public List<KCurrencyCountStruct> lotteryCurrencyRewardList;
		public List<Integer> lotteryRewardUsePointList;
		public int totalItemSize = 0;
		public byte expAddRate = 0;

		// public boolean isExpDouble = false;// 是否经验产出双倍活动
		// public boolean isGoldDouble = false;// 是否金币产出双倍活动
		// public boolean isPotentialDouble = false;// 是否潜能产出双倍活动
		public boolean[] isAttrDouble = new boolean[3];
		public boolean isDropItemDouble = false;// 是否道具掉落双倍活动

		public int[][] attrAndCurrencyShowData = new int[3][4];

		public LevelRewardResultData(byte lotteryType) {
			this.lotteryType = lotteryType;
		}

	}

}
