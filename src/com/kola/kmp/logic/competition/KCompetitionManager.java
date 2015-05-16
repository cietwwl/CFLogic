package com.kola.kmp.logic.competition;

import static com.kola.kmp.logic.competition.KCompetitionModule._LOGGER;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import jxl.read.biff.BiffException;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.dataaccess.KGameDBException;
import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.koala.game.util.StringUtil;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.DataStatus;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kgame.db.DataIdGeneratorFactory;
import com.kola.kgame.db.dataaccess.DataAccesserFactory;
import com.kola.kgame.db.dataobject.DBRank;
import com.kola.kmp.logic.actionrecord.KActionRecorder;
import com.kola.kmp.logic.actionrecord.KActionType;
import com.kola.kmp.logic.chat.KWordBroadcastType;
import com.kola.kmp.logic.combat.ICombatCommonResult;
import com.kola.kmp.logic.combat.KCombatType;
import com.kola.kmp.logic.competition.KCompetitor.KCompetitionBattleHistory;
import com.kola.kmp.logic.competition.KCompetitor.KCompetitionReward;
import com.kola.kmp.logic.competition.teampvp.KTeamPVPManager;
import com.kola.kmp.logic.flow.KRoleAttrModifyType;
import com.kola.kmp.logic.item.KItemMsgPackCenter;
import com.kola.kmp.logic.level.KLevelReward.NormalItemRewardTemplate;
import com.kola.kmp.logic.map.KGameMapEntity.RoleEquipShowData;
import com.kola.kmp.logic.npc.dialog.KDialogButton;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KColorFunEnum;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KEquipmentTypeEnum;
import com.kola.kmp.logic.other.KItemQualityEnum;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.other.KUseFunctionTypeEnum;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KRankTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.role.KRoleVipLvListener;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;
import com.kola.kmp.logic.util.DefaultRoleMapResInfoManager;
import com.kola.kmp.logic.util.IRoleMapResInfo;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_AddItem;
import com.kola.kmp.logic.util.text.HyperTextTool;
import com.kola.kmp.logic.util.tips.CombatTips;
import com.kola.kmp.logic.util.tips.CompetitionTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.ShopTips;
import com.kola.kmp.logic.vip.KRoleVIP;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;
import com.kola.kmp.logic.vip.KVIPUpLvListener;
import com.kola.kmp.protocol.competition.KCompetitionProtocol;

public class KCompetitionManager {

	private final String _savePath = "./res/output/hallofframe.k";
	private final String _saveXmlPath = "./res/output/hallofframe.xml";
	private final int VERSION_FIRST = 20140415;
	private final int CURRENT_VERSION = VERSION_FIRST;
	private final Charset UTF = Charset.forName("Utf-8");

	// public static int vip_canChallengeCount;
	// 竞技场的参与者Map，Key：角色Id，value：名次
	private Map<Long, Integer> _rankingMap = new ConcurrentHashMap<Long, Integer>();
	private Map<Integer, KCompetitor> _competitorRanking = new ConcurrentHashMap<Integer, KCompetitor>();
	private AtomicInteger _currentRank = new AtomicInteger();
	// 竞技场挑战成功的基础金币奖励表，Key：角色等级
	private static Map<Integer, Float> _winBaseGoldReward = new HashMap<Integer, Float>();
	// 竞技场挑战成功的基础荣誉奖励表，Key：角色等级
	private static Map<Integer, Float> _winBaseScoreReward = new HashMap<Integer, Float>();
	// 竞技场挑战成功的金币奖励系数表，Key：排名
	private static Map<Integer, Float> _rewardRankGoldRatio = new HashMap<Integer, Float>();
	// 竞技场挑战成功的荣誉奖励系数表，Key：排名
	private static Map<Integer, Float> _rewardRankScoreRatio = new HashMap<Integer, Float>();

	public static KCompetitionBattlefield battlefield;

	private static int maxCurrecyRewardRankNum;

	private static int maxDiamondRewardRankNum;
	// 每周奖励礼包，Key：排名
	// private static Map<Integer, ItemCountStruct> _weekItemReward = new
	// LinkedHashMap<Integer, ItemCountStruct>();

	// 每天排名钻石奖励
	private static Map<Integer, Integer> _todayDiamondReward = new LinkedHashMap<Integer, Integer>();

	// 世界广播连赢次数
	private static int broadcastSerialWinCount = 5;

	// 排名基数
	private static float rankBaseRatio;
	// 日荣誉产量系数
	private static float dayHonorRatio;
	// 周荣誉产量系数
	private static float weekHonorRatio;
	// 挑战系数
	private static float challengeRatio;
	// 次数常量
	private static float numberConstantRatio;
	// 失败系数
	private static float failureFactorRatio;
	// 周奖励金币倍数
	private static float weekGoldRatio;
	// 挑战失败的CD时间（单位：秒）
	public static int challengeCdTimeSecond;
	// 挑战失败的CD时间（单位：豪秒）
	public static int challengeCdTimeMillis;
	// 清除CD的单位时间小时消耗钻石数（单位：x钻/分钟）
	public static int clearCDTimePerMin;
	// 竞技场战斗时间限制（单位：毫秒）
	public static int battleTimeMillis;

	public static Map<Integer, List<CompetitionRewardShowData>> todayCompetitionRewardShowDataMap = new LinkedHashMap<Integer, List<CompetitionRewardShowData>>();
	// public static Map<Integer, List<CompetitionRewardShowData>>
	// weekCompetitionRewardShowDataMap = new LinkedHashMap<Integer,
	// List<CompetitionRewardShowData>>();

	public static Map<Integer, KHallOfFrameData> hallOfFrameDataMap = new LinkedHashMap<Integer, KHallOfFrameData>();
	private Deque<String> hallOfFrameHistory = new LinkedBlockingDeque<String>(10);
	// 参拜名人堂经验
	public static Map<Integer, Integer> visitHallExpMap = new LinkedHashMap<Integer, Integer>();

	public static Map<Integer, String> hallOfFramePositionNameMap = new HashMap<Integer, String>();

	public static Map<Integer, Map<Byte, Integer>> hallOfFrameFashionMap = new HashMap<Integer, Map<Byte, Integer>>();

	KCompetitionSaveTask saveTask;
	KCompetitionSettleTask settleWeekTask;
	KCompetitionSettleTodayRewardTask settleTodayTask;

	private static final long DEFAULT_REFLASH_TIME = 30000;

	public void init(String configPath) throws Exception {
		try {
			_LOGGER.info("！！！竞技场模块加载开始！！！");
			Document doc = XmlUtil.openXml(configPath);
			if (doc != null) {
				Element root = doc.getRootElement();
				String excelFilePath = root.getChildText("configExcelFilePath");
				String xmlConfigPath = root.getChildText("logicConfigPath");
				String teamPVPConfigPath = root.getChildTextTrim("teamPVPConfigPath");
				KCompetitionModuleConfig.init(xmlConfigPath);

				loadExcelData(excelFilePath);

				battlefield = new KCompetitionBattlefield();
				battlefield.initBattlefield(KCompetitionModuleConfig.getCompetitionBattlePath(), KCompetitionModuleConfig.getCompetitionAudioResId());

				KSupportFactory.getVIPModuleSupport().addVipUpLvListener(new CompetitionVipModuleListener());
				KTeamPVPManager.init(teamPVPConfigPath);
			}
		} catch (Exception e) {
			throw new KGameServerException("读取竞技场excel表头发生错误！", e);
		}
	}

	private void loadExcelData(String excelFilePath) throws Exception {
		KGameExcelFile xlsFile = null;
		try {
			xlsFile = new KGameExcelFile(excelFilePath);
		} catch (BiffException e) {
			throw new KGameServerException("读取竞技场excel表头发生错误！", e);
		} catch (IOException e) {
			throw new KGameServerException("读取竞技场excel表头发生错误！", e);
		}

		if (xlsFile != null) {
			// 初始化竞技场人物等级数据表
			int dataRowIndex = 5;
			KGameExcelTable dataTable = xlsFile.getTable("竞技场人物等级数据表", dataRowIndex);
			KGameExcelRow[] allDataRows = dataTable.getAllDataRows();

			if (allDataRows != null) {
				for (int i = 0; i < allDataRows.length; i++) {
					int roleLv = allDataRows[i].getInt("rolelv");
					float honor = allDataRows[i].getFloat("HonorBasis");
					float copper = allDataRows[i].getFloat("goldBasis");

					_winBaseScoreReward.put(roleLv, honor);
					_winBaseGoldReward.put(roleLv, copper);
				}
			}

			// 初始化竞技场排名数据表
			dataTable = xlsFile.getTable("竞技场排名数据表", dataRowIndex);
			allDataRows = dataTable.getAllDataRows();

			if (allDataRows != null) {
				for (int i = 0; i < allDataRows.length; i++) {
					int rank = allDataRows[i].getInt("rolelv");
					float honor = allDataRows[i].getFloat("HonorBasis");
					float copper = allDataRows[i].getFloat("goldBasis");

					_rewardRankGoldRatio.put(rank, copper);
					_rewardRankScoreRatio.put(rank, honor);

					if (i == allDataRows.length - 1) {
						maxCurrecyRewardRankNum = rank;
					}
				}
			}

			// 初始化竞技场排名奖励
			dataTable = xlsFile.getTable("竞技场排名奖励", dataRowIndex);
			allDataRows = dataTable.getAllDataRows();

			if (allDataRows != null) {
				for (int i = 0; i < allDataRows.length; i++) {
					int beginRank = allDataRows[i].getInt("start");

					if (i == allDataRows.length - 1) {
						maxDiamondRewardRankNum = beginRank;
					}
					int diamond = allDataRows[i].getInt("DayDiamonds");
					_todayDiamondReward.put(beginRank, diamond);

					// _weekItemReward.put(
					// beginRank,
					// initItemReward(itemInfo,
					// allDataRows[i].getIndexInFile()));
				}
			}

			// 初始化竞技场排名奖励
			dataTable = xlsFile.getTable("公式参数", dataRowIndex);
			allDataRows = dataTable.getAllDataRows();

			if (allDataRows != null) {
				for (int i = 0; i < allDataRows.length; i++) {
					this.rankBaseRatio = allDataRows[i].getFloat("RankParam");
					this.dayHonorRatio = allDataRows[i].getFloat("DayHonor");
					this.weekHonorRatio = allDataRows[i].getFloat("WeekHonor");
					this.challengeRatio = allDataRows[i].getFloat("ChallengePoint");
					this.numberConstantRatio = allDataRows[i].getFloat("NumberConstant");
					this.failureFactorRatio = allDataRows[i].getFloat("FailureFactor");
					this.weekGoldRatio = allDataRows[i].getFloat("WeekGold");
					this.challengeCdTimeSecond = allDataRows[i].getInt("cd") * 60;
					this.challengeCdTimeMillis = this.challengeCdTimeSecond * 1000;
					this.clearCDTimePerMin = allDataRows[i].getInt("TimeUnit");
					this.battleTimeMillis = allDataRows[i].getInt("battleTime") * 1000;
				}
			}

			// 参拜名人堂经验
			dataTable = xlsFile.getTable("名人堂参拜经验", dataRowIndex);
			allDataRows = dataTable.getAllDataRows();

			if (allDataRows != null) {
				for (int i = 0; i < allDataRows.length; i++) {
					int lv = allDataRows[i].getInt("lvl");
					int exp = allDataRows[i].getInt("exp");
					visitHallExpMap.put(lv, exp);
				}
			}
			//
			// 初始化竞技场排名奖励
			dataTable = xlsFile.getTable("名人堂职位", dataRowIndex);
			allDataRows = dataTable.getAllDataRows();

			if (allDataRows != null) {
				for (int i = 0; i < allDataRows.length; i++) {
					int id = allDataRows[i].getInt("id");
					String name = allDataRows[i].getData("name");
					hallOfFramePositionNameMap.put(id, name);

					int warriorFashionId = allDataRows[i].getInt("warriorId");
					int ninjaFashionId = allDataRows[i].getInt("ninjaId");
					int gunFashionId = allDataRows[i].getInt("gunId");

					hallOfFrameFashionMap.put(id, new HashMap<Byte, Integer>());
					hallOfFrameFashionMap.get(id).put(KJobTypeEnum.WARRIOR.getJobType(), warriorFashionId);
					hallOfFrameFashionMap.get(id).put(KJobTypeEnum.SHADOW.getJobType(), ninjaFashionId);
					hallOfFrameFashionMap.get(id).put(KJobTypeEnum.GUNMAN.getJobType(), gunFashionId);
				}
			}
			//
			initCompetitionRewardShowDataMap();
		}
	}

	private ItemCountStruct initItemReward(String dropData, int index) throws KGameServerException {
		if (dropData != null) {
			String[] itemData = dropData.split("\\*");
			if (itemData != null && itemData.length == 2) {
				NormalItemRewardTemplate itemTemplate = null;
				String itemCode = itemData[0];
				if (KSupportFactory.getItemModuleSupport().getItemTemplate(itemCode) == null) {
					throw new KGameServerException("初始化<竞技场排名奖励>的道具错误，找不到道具类型：" + itemCode + "，excel行数：" + index);
				}

				int count = Integer.parseInt(itemData[1]);
				itemTemplate = new NormalItemRewardTemplate(itemCode, count);

				return new ItemCountStruct(KSupportFactory.getItemModuleSupport().getItemTemplate(itemCode), count);
			} else {
				throw new KGameServerException("初始化<竞技场排名奖励>的道具格式错误，excel行数：" + index);
			}
		}
		return null;
	}

	private void initCompetitionRewardShowDataMap() {
		for (Integer lv : _winBaseScoreReward.keySet()) {
			List<CompetitionRewardShowData> todayList = new ArrayList<CompetitionRewardShowData>();

			List<CompetitionRewardShowData> weekList = new ArrayList<CompetitionRewardShowData>();
			Integer[] rankNumList = new Integer[_todayDiamondReward.keySet().size()];
			_todayDiamondReward.keySet().toArray(rankNumList);
			for (int i = 0; i < rankNumList.length; i++) {
				// if (i < 3) {
				// todayList.add(caculateTodayShowReward(rankNumList[i]
				// + " ~ " + (rankNumList[i + 1] - 1), lv,
				// rankNumList[i], (rankNumList[i + 1] - 1)));
				// // weekList.add(caculateLastWeekShowReward(rankNumList[i]
				// // + " ~ " + (rankNumList[i + 1] - 1), lv,
				// // rankNumList[i], (rankNumList[i + 1] - 1)));
				// } else if (i == rankNumList.length - 1) {
				// todayList.add(caculateTodayShowReward((rankNumList[i])
				// + " ...", lv, rankNumList[i], -1));
				// // weekList.add(caculateLastWeekShowReward((rankNumList[i])
				// // + " ...", lv, rankNumList[i], -1));
				// } else {
				// todayList.add(caculateTodayShowReward((rankNumList[i])
				// + " ~ " + (rankNumList[i + 1] - 1), lv,
				// rankNumList[i], (rankNumList[i + 1] - 1)));
				// // weekList.add(caculateLastWeekShowReward((rankNumList[i])
				// // + " ~ " + (rankNumList[i + 1] - 1), lv,
				// // rankNumList[i], (rankNumList[i + 1] - 1)));
				// }

				if (i < rankNumList.length - 1) {
					if (rankNumList[i + 1] - rankNumList[i] == 1) {
						todayList.add(caculateTodayShowReward(rankNumList[i] + "", lv, rankNumList[i], -1));
					} else {
						todayList.add(caculateTodayShowReward(rankNumList[i] + " ~ " + (rankNumList[i + 1] - 1), lv, rankNumList[i], (rankNumList[i + 1] - 1)));
					}
				} else {
					todayList.add(caculateTodayShowReward((rankNumList[i]) + " ...", lv, rankNumList[i], -1));
				}
			}
			todayCompetitionRewardShowDataMap.put(lv, todayList);
			// weekCompetitionRewardShowDataMap.put(lv, weekList);

		}
	}

	public void serverStartCompleted() throws KGameServerException {

		for (Integer position : hallOfFrameFashionMap.keySet()) {
			Map<Byte, Integer> map = hallOfFrameFashionMap.get(position);
			for (Integer fashionId : map.values()) {
				if (KSupportFactory.getFashionModuleSupport().getFashionTemplate(fashionId) == null) {
					throw new KGameServerException("初始化竞技场的<名人堂职位>表的时装错误，找不到时装类型：" + fashionId + "职位=" + position);
				}
			}
		}

		this.saveTask = new KCompetitionSaveTask();
		this.saveTask.start();

		this.settleWeekTask = new KCompetitionSettleTask(KCompetitionModuleConfig.getCompetitionSettleWeekday(), KCompetitionModuleConfig.getCompetitionTodayRewardSettleTime());
		this.settleWeekTask.start();

		this.settleTodayTask = new KCompetitionSettleTodayRewardTask(KCompetitionModuleConfig.getCompetitionTodayRewardSettleTime());
		this.settleTodayTask.start();
		KTeamPVPManager.onGameWorldInitComplete();
	}

	public void serverShutDown() throws KGameServerException {
		_LOGGER.info("--->>>>>>>>>> 竞技场模块开始保存排行榜。。。。");
		// settleCompetition();
		save();
		checkAndSaveHallOfFrameWhileShutdown();
		KTeamPVPManager.shutdown();
		_LOGGER.info("--->>>>>>>>>> 竞技场模块保存排行榜成功！。");
	}

	public void notifyCacheLoadComplete() throws KGameServerException {
		_LOGGER.info("--->>>>>>>>>> 竞技场模块加载DB排行榜数据。");
		readRanking();
		readHallOfFrame();
		KTeamPVPManager.notifyCacheLoadComplete();
		_LOGGER.info("--->>>>>>>>>> 完成竞技场模块加载DB排行榜数据。");
	}

	/**
	 * 
	 * 处理角色升级的操作
	 * 
	 * @param role
	 * @param previousLevel
	 * @param currentLevel
	 */
	void notifyRoleLevelUp(KRole role, int previousLevel, int currentLevel) {
		int joinLv = KCompetitionModuleConfig.getJoinedCompetitionLv();
		if (currentLevel == joinLv || (previousLevel < joinLv && currentLevel > joinLv) || (currentLevel > joinLv && !_rankingMap.containsKey(role.getId()))) {
			int rank = _currentRank.incrementAndGet();
			// KCompetitor c = new KCompetitor(role.getId(), role.getName(),
			// currentLevel, role.getJob(), rank, role.getInMapResId(),
			// role.getRecordOccupation(), System.currentTimeMillis(),
			// KSupportFactory.getVipModuleSupport().getVIPLevelData(role.getRoleId()).competitionFreeTime);
			long recordId = DataIdGeneratorFactory.getRankIdGenerator().nextId();
			KCompetitionReward cResult = new KCompetitionReward(role.getId(), role.getLevel(), rank, false);
			int vipLv = KSupportFactory.getVIPModuleSupport().getVipLv(role.getId());
			KCompetitor c = new KCompetitor(recordId, role.getId(), role.getName(), role.getLevel(), rank, role.getJob(), role.getHeadResId(), role.getInMapResId(), System.currentTimeMillis(),
					KCompetitionModuleConfig.getMaxChallengeTimePerDay(), role.getBattlePower(), vipLv, cResult);
			c.setOnLine(true);
			c.setHadOpen(true);
			putToMap(rank, c);
			// if(rank <= 50) {
			// try {
			// KSupportFactory.getGameTitleSupport().rankingChanged(KGameRankType.COMPETITION);
			// } catch (Exception e) {
			// LOGGER.error("通知称号模块排名变化时出现异常！！", e);
			// }
			// }
			sendCompetitionData(role, c);

			if (rank >= 1 && rank <= 5) {
				KHallOfFrameData data = hallOfFrameDataMap.get(rank);
				if (data == null) {
					data = new KHallOfFrameData();
					data.position = rank;
					data.isHasData = false;
				}
				if (data.roleId == 0 || !data.isHasData) {
					data.roleId = c.getRoleId();
					data.isHasData = true;
					data.roleName = c.getRoleName();
					data.inMapResId = c.getInMapResId();
					data.job = c.getOccupation();
					data.vipLv = c.getVipLv();
					hallOfFrameDataMap.put(rank, data);
					if (rank == 1) {
						String dateInfo = UtilTool.DATE_FORMAT4.format(new Date(System.currentTimeMillis()));
						addHallOfFrameHistory(CompetitionTips.getTipsHistory1(dateInfo, c.getExtRoleName()));
					}
				}
			}
		} else if (currentLevel > KCompetitionModuleConfig.getJoinedCompetitionLv()) {
			KCompetitor competitor = getCompetitorByRoleId(role.getId());
			competitor.setRoleLevel(currentLevel);
			competitor.setHeadResId(role.getHeadResId());
			competitor.setInMapResId(role.getInMapResId());
			competitor.setFightPower(role.getBattlePower());
			// competitor.getTodayCompetitionReward().changeRank(currentLevel,
			// competitor.getRanking());
			competitor.notifyDB();
			// KCompetitionServerMsgSender.sendUpdateRewardWhileLvUp(competitor);
		}
	}

	public String joinCompetitionForGM(long roleId) {
		if (_rankingMap.containsKey(roleId)) {
			return "此角色已在竞技场中";
		} else {
			KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (role == null) {
				return "不存在此角色ID：" + roleId;
			} else if (role.getLevel() < KCompetitionModuleConfig.getJoinedCompetitionLv()) {
				return "角色等级不符合！id：" + roleId;
			}
			int rank = _currentRank.incrementAndGet();
			long recordId = DataIdGeneratorFactory.getRankIdGenerator().nextId();
			KCompetitionReward cResult = new KCompetitionReward(role.getId(), role.getLevel(), rank, false);
			int vipLv = KSupportFactory.getVIPModuleSupport().getVipLv(role.getId());
			KCompetitor c = new KCompetitor(recordId, role.getId(), role.getName(), role.getLevel(), rank, role.getJob(), role.getHeadResId(), role.getInMapResId(), System.currentTimeMillis(),
					KCompetitionModuleConfig.getMaxChallengeTimePerDay(), role.getBattlePower(), vipLv, cResult);
			c.setOnLine(true);
			c.setHadOpen(true);
			putToMap(rank, c);

			return "添加角色进入竞技场成功";
		}
	}

	public void notifyRoleDelete(long roleId) {
		if (_rankingMap.containsKey(roleId)) {

			int rank = _rankingMap.get(roleId);
			KCompetitor removeComp = _competitorRanking.get(rank);
			if (removeComp != null) {
				if (rank <= 20) {
					KCompetitor tempComp;
					List<KCompetitor> competitorList = new ArrayList<KCompetitor>();
					for (int i = rank; i <= 20; i++) {
						tempComp = _competitorRanking.get(i);
						if (tempComp != null) {
							synchronized (tempComp) {
								_rankingMap.remove(tempComp.getRoleId());
								_competitorRanking.remove(i);
								if (tempComp.getRoleId() != roleId) {
									competitorList.add(tempComp);
								}
							}
						}
					}

					if (competitorList.size() > 0) {
						int tempRank = rank;
						for (int i = 0; i < competitorList.size(); i++, tempRank++) {
							tempComp = competitorList.get(i);
							synchronized (tempComp) {
								tempComp.setRanking(tempRank);
								putToMap(tempRank, tempComp);
								tempComp.notifyDB();
							}
						}
					}

					if (rank <= KHallOfFrameData.HOF_MIN_POSITION && hallOfFrameDataMap.containsKey(rank)) {
						KHallOfFrameData data = hallOfFrameDataMap.get(rank);
						if (data != null) {
							data.isHasData = false;
							data.roleId = 0;
							data.roleName = null;
							data.inMapResId = 0;
							data.job = -1;
							data.vipLv = 0;
						}
					}
				}
				try {
					DataAccesserFactory.getRankDataAccesser().deleteRank(removeComp.getRecordId());
				} catch (KGameDBException e) {
					_LOGGER.error("### 角色删除时，附带删除竞技场排行榜数据发生异常", e);
				}
			}
		}
	}

	private void putToMap(int ranking, KCompetitor competitor) {
		_competitorRanking.put(ranking, competitor);
		_rankingMap.put(competitor.getRoleId(), ranking);
	}

	public KCompetitor getCompetitorByRoleId(long roleId) {
		Integer ranking = _rankingMap.get(roleId);
		if (ranking == null) {
			return null;
		} else {
			return _competitorRanking.get(ranking);
		}
	}

	public int getCurrentLastRank() {
		return _currentRank.get();
	}

	void notifyRoleJoinedGame(KRole role) {
		if (role.getLevel() >= KCompetitionModuleConfig.getJoinedCompetitionLv()) {
			KCompetitor roleC = getCompetitorByRoleId(role.getId());
			if (roleC != null) {
				roleC.setOnLine(true);
				roleC.setHadOpen(true);
				// roleC.checkChallengeTime();
				// KCompetitionServerMsgSender.sendUpdateCanChallengeTimes(roleC);
				sendCompetitionData(role, roleC);
			} else {
				notifyRoleLevelUp(role, 0, KCompetitionModuleConfig.getJoinedCompetitionLv());
			}
		}
	}

	void notifyRoleLeaveGame(KRole role) {
		if (role.getLevel() >= KCompetitionModuleConfig.getJoinedCompetitionLv()) {
			KCompetitor roleC = getCompetitorByRoleId(role.getId());
			if (roleC != null) {
				roleC.setHadOpen(false);
				roleC.setOnLine(false);
			}
		}
	}

	public boolean challenge(KRole challenger, int ranking, long defenderRoleId, boolean isNeedCheck) {
		KCompetitor defender = getCompetitorByRoleId(defenderRoleId);
		KCompetitor roleC = getCompetitorByRoleId(challenger.getId());
		if (isNeedCheck) {
			if (defender == null) {
				// 找不到防守者
				_LOGGER.error("找不到防守者对象，防守者的角色ID是：" + defenderRoleId);
				KDialogService.sendSimpleDialog(challenger, GlobalTips.getTipsDefaultTitle(), CompetitionTips.getTipsRoleNotFound());
				return false;
			}
			int defenderRank = defender.getRanking();

			KRole defenderRole = KSupportFactory.getRoleModuleSupport().getRole(defender.getRoleId());
			if (roleC == null) {
				// 找不到竞技场对象
				_LOGGER.error("找不到挑战者对象，挑战者id是：" + challenger.getId());
				KDialogService.sendSimpleDialog(challenger, GlobalTips.getTipsDefaultTitle(), GlobalTips.getTipsServerBusy());
				return false;
			} else if (defenderRole == null) {
				// 找不到防守者
				_LOGGER.error("找不到防守者的角色对象，防守者的角色ID是：" + defenderRoleId);
				KDialogService.sendSimpleDialog(challenger, GlobalTips.getTipsDefaultTitle(), CompetitionTips.getTipsRoleNotFound());
				return false;
			} else if (challenger.isFighting()) {
				KDialogService.sendUprisingDialog(challenger, CombatTips.getTipsRoleIsFighting(challenger.getName()));
				return false;
			}
			// else if (!roleC.getPicks().contains(defenderRank)) {
			// // 不包含排名
			// KDialogService.sendSimpleDialog(challenger,
			// GlobalTips.getTipsDefaultTitle(),
			// CompetitionTips.getTipsNoInYourRanking());
			// return false;
			// }
			else if (roleC.getCanChallengeTimes() <= 0) {
				// 挑战次数已满
				// KDialogService.sendSimpleDialog(challenger, "",
				// KCompetitionTipsManager.getTipsNoMoreChallengeTime());

				if (roleC.getCanChallengeTimes() >= KCompetitionModuleConfig.getMaxChallengeTimePerDay()) {
					KDialogService.sendUprisingDialog(challenger, CompetitionTips.getTipsChallengeTimeFull());
					return false;
				}

				VIPLevelData vipData = KSupportFactory.getVIPModuleSupport().getVIPLevelData(challenger.getId());
				int vip_canChallengeCount = vipData.pvpbuyrmb.length;
				int buyCount = roleC.getTodayBuyCount().get();
				if (buyCount >= vip_canChallengeCount) {
					KDialogService.sendUprisingDialog(challenger, CompetitionTips.getTipsCannotBuyChallengeTime(vipData.lvl, vip_canChallengeCount));
					return false;
				}
				// int point =
				// KCompetitionModuleConfig.getAddChallengeTimeMoney();
				int point = vipData.pvpbuyrmb[buyCount];

				List<KDialogButton> buttons = new ArrayList<KDialogButton>();
				buttons.add(KDialogButton.CANCEL_BUTTON);
				buttons.add(new KDialogButton(KCompetitionDialogProcesser.KEY_CONFIRM_ADD_CHALLENGE_TIME, "", KDialogButton.CONFIRM_DISPLAY_TEXT));
				KDialogService.sendFunDialog(challenger, GlobalTips.getTipsDefaultTitle(), CompetitionTips.getTipsNoMoreChallengeTime(point), buttons, true, (byte) -1);
				// KDialogService.sendUprisingDialog(challenger,
				// CompetitionTips.getTipsNoMoreChallengeTime(KCompetitionModuleConfig.getAddChallengeTimeMoney()));
				return false;
			} else {

				boolean isCdTime = roleC.isCdTime();
				if (isCdTime) {
					int restTimeSeconds = roleC.getRestCDTimeSeconds();
					int usePoint = caculateCDTimeUsePoint(restTimeSeconds);
					List<KDialogButton> buttons = new ArrayList<KDialogButton>();
					buttons.add(KDialogButton.CANCEL_BUTTON);
					buttons.add(new KDialogButton(KCompetitionDialogProcesser.KEY_CONFIRM_CLEAR_CHALLENGE_CD_TIME, ranking + "," + defenderRoleId, KDialogButton.CONFIRM_DISPLAY_TEXT));
					KDialogService.sendFunDialog(challenger, GlobalTips.getTipsDefaultTitle(), CompetitionTips.getTipsChallengeClearCdTime(usePoint), buttons, true, (byte) -1);
					// KDialogService.sendUprisingDialog(challenger,
					// CompetitionTips.getTipsNoMoreChallengeTime(KCompetitionModuleConfig.getAddChallengeTimeMoney()));
					return false;
				}
			}
		}

		roleC.notifyChallengeStart();
		try {
			// TODO 通知战斗模块进入战场
			CompetitionCombatData data = new CompetitionCombatData();
			data.defenderRoleId = defender.getRoleId();
			data.defenderRanking = defender.getRanking();
			// KSupportFactory.getCombatModuleSupport().fightWithAI(challenger,
			// defender.getRoleId(), KCombatType.COMPETITION, battlefield,
			// data);
			KSupportFactory.getCombatModuleSupport().fightWithAIWithTimeLimit(challenger, defender.getRoleId(), KCombatType.COMPETITION, battlefield, data, battleTimeMillis);

			// 角色行为统计
			KActionRecorder.recordAction(challenger.getId(), KActionType.ACTION_TYPE_COMPETITION, 1);
		} catch (Exception ex) {
			_LOGGER.error("进行竞技场挑战的时候出现异常，挑战者id是：" + challenger.getId() + "，被挑战者是：" + defender.getRoleId(), ex);
			KDialogService.sendSimpleDialog(challenger, "", GlobalTips.getTipsServerBusy());
			return false;
		}
		return true;

	}

	public static int caculateCDTimeUsePoint(int restTimeSeconds) {
		if (restTimeSeconds > 0) {
			return (restTimeSeconds / 60 + 1) * clearCDTimePerMin;
		} else {
			return 0;
		}
	}

	public KCompetitor getCompetitorByRanking(int ranking) {
		return _competitorRanking.get(ranking);
	}

	public void notifyBattleFinished(KRole challenger, ICombatCommonResult result) {
		CompetitionCombatData data = null;
		try {
			data = (CompetitionCombatData) result.getAttachment();
		} catch (Exception e) {
			_LOGGER.error("响应竞技场战斗技术时发生异常！", e);
			return;
		}

		if (data == null) {
			return;
		}
		KCompetitor chc = getCompetitorByRoleId(challenger.getId());
		KCompetitor defc = getCompetitorByRoleId(data.defenderRoleId);
		int honerRanking;
		if (result.isWin()) {
			honerRanking = defc.getRanking();
			chc.notifyBattleResult(result.isWin());
			synchronized (defc) {
				// if (defc.getRanking() == data.defenderRanking) {
				if (chc.getRanking() > honerRanking) {
					int temp = chc.getRanking();
					chc.changeRanking(honerRanking); // 改变挑战者的排名
					defc.changeRanking(temp); // 改变被挑战者的排名
					putToMap(honerRanking, chc); // 修改挑战者名次后，更新到榜里
					putToMap(temp, defc); // 修改排被挑战者名次后，更新到榜里
					chc.afterRankingChange(temp); // 挑战者排名修改完毕的通知
					defc.afterRankingChange(honerRanking); // 被挑战者排名修改完毕的通知
					chc.addHistory(result.isWin(), CompetitionTips.getChallengeResult(defc.getExtRoleName(), result.isWin(), chc.getRanking()));
					defc.addHistory(result.isWin(), CompetitionTips.getBeChallengeResult(chc.getExtRoleName(), !result.isWin(), defc.getRanking()));
					if (honerRanking == 1) {
						KSupportFactory.getChatSupport().sendSystemChat(CompetitionTips.getTipsChallengeFirstSuccess(challenger.getExName(), defc.getExtRoleName()), true, true);
					} else if (honerRanking <= 10) {
						KSupportFactory.getChatSupport().sendSystemChat(CompetitionTips.getTipsChallengeFirstTenSuccess(challenger.getExName(), defc.getExtRoleName(), honerRanking), true, true);
					}
					// try {
					// if (defenderRanking <= 50) {
					// // 50名以内的排名变化才通知
					// KSupportFactory.getGameTitleSupport().rankingChanged(KGameRankType.COMPETITION);
					// }
					// } catch (Exception e) {
					// LOGGER.error("通知竞技场排名变化时产生异常！！！", e);
					// }
				} else {
					chc.addHistory(result.isWin(),
							result.isWin() ? CompetitionTips.getTipsChallengeLowerSuccess(defc.getExtRoleName()) : CompetitionTips.getTipsChallengeLowerFail(defc.getExtRoleName()));
					defc.addHistory(result.isWin(),
							result.isWin() ? CompetitionTips.getTipsBeChallengeFailByHigher(chc.getExtRoleName()) : CompetitionTips.getTipsBeChallengeSuccessByHigher(chc.getExtRoleName()));
				}
				// } else {
				// _LOGGER.info(
				// "战斗完成后，防守者的排名已经更改，不做任何操作！防守者id：{}，挑战时的排名：{}，目前排名：{}，挑战者id：{}，挑战者当前排名：{}",
				// data.defenderRoleId, data.defenderRanking,
				// defc.getRanking(), chc.getRoleId(),
				// chc.getRanking());
				// }
			}
		} else {
			chc.notifyBattleResult(result.isWin());
			honerRanking = chc.getRanking();
			chc.addHistory(result.isWin(), CompetitionTips.getChallengeResult(defc.getExtRoleName(), result.isWin(), chc.getRanking()));

		}
		chc.notifyDB();
		// ActionRecord.recordAction(KActionTypeEnum.竞技场, challenger,
		// chc.getRanking());

		// 结算以及发送结算界面消息
		KCurrencyCountStruct honor = caculateBattleHonor(result.isWin(), challenger.getLevel(), honerRanking);
		KCompetitionServerMsgSender.sendBattleResult(challenger, result, (int) honor.currencyCount, chc.getRanking());
		KSupportFactory.getCurrencySupport().increaseMoney(challenger.getId(), honor, PresentPointTypeEnum.竞技场排名奖励, true);

		KCompetitionServerMsgSender.sendUpdateCanChallengeTimes(chc);

		// 通知活跃度模块
		KSupportFactory.getRewardModuleSupport().recordFun(challenger, KVitalityTypeEnum.竞技场切磋);

		KSupportFactory.getMissionSupport().notifyUseFunction(challenger, KUseFunctionTypeEnum.完成竞技场PK);

		reflashCompetitionData(challenger, false, false);

		//5连胜世界广播
		if (result.isWin()) {
			if (chc.getSerialWinCount() == broadcastSerialWinCount) {
				String content = KWordBroadcastType.竞技场_XXX已经连胜5场.content;
				content = StringUtil.format(content, challenger.getExName(), broadcastSerialWinCount);
				KSupportFactory.getChatSupport().sendSystemChat(content, KWordBroadcastType.竞技场_XXX已经连胜5场);
			}
		}
	}

	public List<KCompetitor> pickCompetitor(KRole role, boolean isReflash) {
		KCompetitor roleC = getCompetitorByRoleId(role.getId());
		return pickCompetitor(roleC, isReflash);
	}

	public List<KCompetitor> pickCompetitor(KCompetitor roleC, boolean isReflash) {
		List<KCompetitor> list = new ArrayList<KCompetitor>();
		int roleRank = roleC.getRanking();
		int serialWinCount = roleC.getSerialWinCount();
		List<Integer> rankingList;
		if (roleC.need2Repick() || isReflash) {
			// rankingList = pickRanking(roleRank);
			rankingList = getRankNums(roleRank, serialWinCount, _currentRank.get());
			roleC.setPicks(rankingList, roleC.getRanking());
		} else {
			rankingList = roleC.getPicks();
		}
		KCompetitor c;
		for (int i = 0; i < rankingList.size(); i++) {
			int rank = rankingList.get(i);
			if (rank == roleRank) {
				c = roleC;
			} else {
				c = getCompetitorByRanking(rank);
			}
			if (c != null) {
				list.add(c);
			}
		}
		return list;
	}

	public static List<Integer> pickRanking(int rank) {
		int rankSize = 10;
		int frontIdx = 6;
		int backIdx = 3;
		List<Integer> rankList = new ArrayList<Integer>();
		rankList.add(rank);
		int selectCount = 1;
		if (rank <= 30) {
			for (int tempRank = (rank - 1); selectCount <= frontIdx && tempRank > 0; selectCount++, tempRank--) {
				rankList.add(tempRank);
			}
			for (int tempRank = (rank + 1); selectCount < rankSize; selectCount++, tempRank++) {
				rankList.add(tempRank);
			}
		} else if (rank > 30 && rank <= 101) {
			pick(rankList, (rank - 10), (rank - 1), 3);
			pick(rankList, (rank - 20), (rank - 11), 2);
			pick(rankList, (rank + 1), (rank + 10), 2);
			pick(rankList, (rank + 11), (rank + 20), 1);
		} else {
			pick(rankList, (rank - 10), (rank - 1), 3);
			pick(rankList, (rank - 11), (rank - 20), 3);
			pick(rankList, (rank - 51), (rank - 100), 3);
			pick(rankList, (rank + 1), (rank + 10), 2);
			pick(rankList, (rank + 11), (rank + 20), 1);
		}

		Collections.sort(rankList);

		return rankList;
	}

	private static void pick(List<Integer> rankList, int front, int end, int pickSize) {
		int size = 0;
		while (size < pickSize) {
			int tempRank = UtilTool.random(front, end);
			if (!rankList.contains(tempRank)) {
				rankList.add(tempRank);
				size++;
			}
		}
	}

	public void reflashCompetitionData(KRole role, boolean checkTime, boolean isManualReflash) {
		KCompetitor roleC = getCompetitorByRoleId(role.getId());
		if (roleC == null) {
			KDialogService.sendUprisingDialog(role, GlobalTips.getTipsServerBusy());
			return;
		}
		if (checkTime && ((System.currentTimeMillis() - roleC.last_reflash_picks_time) < DEFAULT_REFLASH_TIME)) {
			KDialogService.sendUprisingDialog(role, CompetitionTips.getTipsReflashTimeTooShort());
			return;
		}

		List<KCompetitor> compList = pickCompetitor(role, true);
		if (compList != null && compList.size() > 0) {
			KCompetitionServerMsgSender.sendUpdateCompetitionList(roleC, compList, roleC.getRanking());
		}
		if(isManualReflash){
			KDialogService.sendUprisingDialog(role, CompetitionTips.getTipsReflashCompetitionSuccess());
		}
	}

	void resetChallengeTime() {
		KCompetitor temp;
		for (Iterator<KCompetitor> itr = _competitorRanking.values().iterator(); itr.hasNext();) {
			temp = itr.next();

			// temp.resetChallengeTime();
			temp.checkChallengeTime();
		}
	}

	void settleCompetitionWeekReward() {
		_LOGGER.info("》》》》》》》》竞技场奖励每周结算通知，当前时间：{}《《《《《《《《", new Date().toString());
		Set<KCompetitor> allCompetitors = new HashSet<KCompetitor>(_competitorRanking.values());
		KCompetitor current;

		// for (Iterator<KCompetitor> itr = allCompetitors.iterator(); itr
		// .hasNext();) {
		// current = itr.next();
		// current.setLastWeekReward(new KCompetitionReward(false, current
		// .getRoleId(), current.getRoleLevel(), current.getRanking(),
		// false));
		// current.notifyDB();
		// }

		// List<Long> allOnlineRoles = KSupportFactory.getRoleModuleSupport()
		// .getAllOnLineRoleIds();
		//
		// for (Long roleId : allOnlineRoles) {
		// KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		// current = getCompetitorByRoleId(roleId);
		// if (role != null && current != null) {
		// KCompetitionServerMsgSender.sendUpdateRewardWhileGet(role,
		// current.getLastWeekReward().getReward(), false, false);
		// }
		// }

		// 处理名人堂
		KCompetitor oldNo1C = null, newNo1C = null;
		if (hallOfFrameDataMap.containsKey(KHallOfFrameData.HOF_MAX_POSITION)) {
			KHallOfFrameData data = hallOfFrameDataMap.get(KHallOfFrameData.HOF_MAX_POSITION);
			if (data.isHasData) {
				oldNo1C = getCompetitorByRoleId(data.roleId);
			}
		}

		hallOfFrameDataMap.clear();
		for (int i = KHallOfFrameData.HOF_MAX_POSITION; i <= KHallOfFrameData.HOF_MIN_POSITION; i++) {
			KHallOfFrameData data = new KHallOfFrameData();
			data.position = i;
			hallOfFrameDataMap.put(i, data);
		}

		List<KCompetitor> hallList = KSupportFactory.getCompetitionModuleSupport().getCurrentRanks(5);
		for (KCompetitor c : hallList) {
			if (hallOfFrameDataMap.containsKey(c.getRanking())) {
				KHallOfFrameData data = hallOfFrameDataMap.get(c.getRanking());
				data.roleId = c.getRoleId();
				data.isHasData = true;
				data.roleName = c.getRoleName();
				data.inMapResId = c.getInMapResId();
				data.job = c.getOccupation();
				data.vipLv = c.getVipLv();
				if (c.getRanking() == KHallOfFrameData.HOF_MAX_POSITION) {
					newNo1C = c;
				}
			}
		}

		String dateInfo = UtilTool.DATE_FORMAT4.format(new Date(System.currentTimeMillis()));
		if (newNo1C != null) {
			if (oldNo1C != null) {
				if (newNo1C.getRoleId() == oldNo1C.getRoleId()) {
					addHallOfFrameHistory(CompetitionTips.getTipsHistory3(dateInfo, newNo1C.getExtRoleName()));
				} else {
					addHallOfFrameHistory(CompetitionTips.getTipsHistory2(dateInfo, newNo1C.getExtRoleName(), oldNo1C.getExtRoleName()));
				}
			} else {
				addHallOfFrameHistory(CompetitionTips.getTipsHistory1(dateInfo, newNo1C.getExtRoleName()));
			}
		}
		// 保存数据
		saveHallOfFrame();
		// 世界播报名人堂
		broadcastHallOfFrame();
		// 发系统消息
		KSupportFactory.getChatSupport().sendSystemChat(CompetitionTips.getTipsSettleWeekReward(), true, false);
		// 发放名人堂时装
		for (KHallOfFrameData data : hallOfFrameDataMap.values()) {
			if (hallOfFrameFashionMap.containsKey(data.position) && hallOfFrameFashionMap.get(data.position).containsKey(data.job)) {
				KRole role = KSupportFactory.getRoleModuleSupport().getRole(data.roleId);
				if (role != null) {
					List<Integer> idList = new ArrayList<Integer>();
					idList.add(hallOfFrameFashionMap.get(data.position).get(data.job));
					KSupportFactory.getFashionModuleSupport().addFashions(role, idList, "名人堂发放时装套装。");
					KSupportFactory.getMailModuleSupport().sendSimpleMailBySystem(data.roleId, CompetitionTips.getTipsSettleFashionMailTitle(),
							CompetitionTips.getTipsSettleFashionMailContent(hallOfFramePositionNameMap.get(data.position)));
				}
			}
		}
	}

	void settleCompetitionTodayReward() {
		_LOGGER.info("》》》》》》》》竞技场奖励每天结算通知，当前时间：{}《《《《《《《《", new Date().toString());
		Set<KCompetitor> allCompetitors = new HashSet<KCompetitor>(_competitorRanking.values());
		KCompetitor current;
		for (Iterator<KCompetitor> itr = allCompetitors.iterator(); itr.hasNext();) {
			current = itr.next();
			current.setTodayCompetitionReward(new KCompetitionReward(current.getRoleId(), current.getRoleLevel(), current.getRanking(), false));
			// current.resetChallengeTime();
			current.notifyDB();
			// 通知封测活动奖励
			KSupportFactory.getRewardModuleSupport().notifyForFengceCompetionReward(current.getRoleId(), current.getRanking());
		}

		List<Long> allOnlineRoles = KSupportFactory.getRoleModuleSupport().getAllOnLineRoleIds();

		for (Long roleId : allOnlineRoles) {
			KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			current = getCompetitorByRoleId(roleId);
			if (role != null && current != null) {
				KCompetitionServerMsgSender.sendUpdateRewardWhileGet(role, current.getTodayCompetitionReward().getReward(), true, false);
			}
		}

		KSupportFactory.getChatSupport().sendSystemChat(CompetitionTips.getTipsSettleTodayReward(), true, false);
	}

	private void broadcastHallOfFrame() {
		for (int i = KHallOfFrameData.HOF_MAX_POSITION; i <= KHallOfFrameData.HOF_MIN_POSITION; i++) {
			KHallOfFrameData data = hallOfFrameDataMap.get(i);
			if (data != null) {
				if (i == KHallOfFrameData.HOF_MAX_POSITION) {
					String content = KWordBroadcastType.竞技榜_x角色问鼎大元帅.content;
					content = StringUtil.format(content, data.roleName);
					KSupportFactory.getChatSupport().sendSystemChat(content, KWordBroadcastType.竞技榜_x角色问鼎大元帅);
				} else {
					String content = KWordBroadcastType.竞技榜_恭喜x角色获得x职位.content;
					String position = hallOfFramePositionNameMap.get(i);
					content = StringUtil.format(content, data.roleName, position);
					KSupportFactory.getChatSupport().sendSystemChat(content, KWordBroadcastType.竞技榜_x角色问鼎大元帅);
				}
			}
		}
	}

	void save() {
		List<DBRank> insertList = new ArrayList<DBRank>();
		List<DBRank> updateList = new ArrayList<DBRank>();
		KCompetitor competitor;
		int currentRank = _currentRank.get();
		for (int i = 1; i <= currentRank; i++) {
			competitor = _competitorRanking.get(i);
			if (competitor != null) {
				synchronized (competitor) {
					if (competitor._dataStatus.getStatus() == DataStatus.STATUS_INSERT) {
						insertList.add(competitor.notifyScan());
					} else if (competitor._dataStatus.getStatus() == DataStatus.STATUS_UPDATE) {
						updateList.add(competitor.notifyScan());
					}
				}
			}
		}
		try {
			DataAccesserFactory.getRankDataAccesser().addRanks(insertList);
			DataAccesserFactory.getRankDataAccesser().updateRanks(updateList);
		} catch (KGameDBException ex) {
			_LOGGER.error("保存竞技场数据时出现异常！", ex);
		}
	}

	private void readRanking() throws KGameServerException {
		List<DBRank> list = null;
		try {
			list = DataAccesserFactory.getRankDataAccesser().getRankDataByType(KRankTypeEnum.竞技榜.sign);
		} catch (KGameDBException ex) {
			throw new KGameServerException("加载竞技场数据时出现异常！", ex);
		}
		if (list != null) {
			KCompetitor competitor;
			int lastRank = 0;

			for (DBRank dbRank : list) {
				competitor = new KCompetitor(dbRank);
				putToMap(competitor.getRanking(), competitor);
				if (lastRank < competitor.getRanking()) {
					lastRank = competitor.getRanking();
				}
			}
			_currentRank.set(lastRank);
			checkRankingComplete();
		}
	}

	private void checkRankingComplete() {
		int tempCurrentRank = _currentRank.get();
		List<KCompetitor> competitorList = new ArrayList<KCompetitor>();
		KCompetitor competitor;
		boolean reset = false;
		for (int i = 1; i <= tempCurrentRank; i++) {
			competitor = _competitorRanking.get(i);
			if (competitor != null) {
				competitorList.add(competitor);
			}
			if (competitor == null && !reset) {
				reset = true;
			}
		}
		if (reset) {
			_LOGGER.warn("$$$$####开始重新排列竞技场排名####$$$$");
			_competitorRanking.clear();
			_rankingMap.clear();
			tempCurrentRank = 0;
			for (int i = 0; i < competitorList.size(); i++) {
				tempCurrentRank++;
				competitor = competitorList.get(i);
				competitor.setRanking(tempCurrentRank);
				_competitorRanking.put(tempCurrentRank, competitor);
				_rankingMap.put(competitor.getRoleId(), tempCurrentRank);
				competitor.notifyDB();
			}
			_currentRank.set(tempCurrentRank);
			save();
		}
	}

	public static KCurrencyCountStruct caculateBattleHonor(boolean isWin, int roleLv, int rank) {
		float rankScoreRatio;
		float lvScoreRatio;
		if (_rewardRankScoreRatio.containsKey(rank)) {
			rankScoreRatio = _rewardRankScoreRatio.get(rank);
		} else {
			rankScoreRatio = _rewardRankScoreRatio.get(maxCurrecyRewardRankNum);
		}
		if (_winBaseScoreReward.containsKey(roleLv)) {
			lvScoreRatio = _winBaseScoreReward.get(roleLv);
		} else {
			lvScoreRatio = _winBaseScoreReward.get(1);
		}
		float socre;
		if (isWin) {
			// 挑战胜利
			// 获得荣誉=被挑战人荣誉排名参数/排名基数*等级每日荣誉量*挑战系数/次数常量
			socre = ((rankScoreRatio / rankBaseRatio) * lvScoreRatio * challengeRatio) / numberConstantRatio;
		} else {
			// 挑战失败
			// 挑战人荣誉排名参数/排名基数*等级每日荣誉量*挑战系数/次数常量*失败系数
			socre = (((rankScoreRatio / rankBaseRatio) * lvScoreRatio * challengeRatio) / numberConstantRatio) * failureFactorRatio;
		}

		return new KCurrencyCountStruct(KCurrencyTypeEnum.SCORE, (long) socre);
	}

	public static CompetitionRewardShowData caculateTodayReward(String title, int roleLv, int rank) {
		float baseGlod;
		float baseScore;
		if (_winBaseGoldReward.containsKey(roleLv) && _winBaseScoreReward.containsKey(roleLv)) {
			baseGlod = _winBaseGoldReward.get(roleLv);
			baseScore = _winBaseScoreReward.get(roleLv);
		} else {
			baseGlod = _winBaseGoldReward.get(1);
			baseScore = _winBaseScoreReward.get(1);
		}
		float rankGlodRatio, rankScoreRatio;
		if (_rewardRankGoldRatio.containsKey(rank) && _rewardRankScoreRatio.containsKey(rank)) {
			rankGlodRatio = _rewardRankGoldRatio.get(rank);
			rankScoreRatio = _rewardRankScoreRatio.get(rank);
		} else {
			rankGlodRatio = _rewardRankGoldRatio.get(maxCurrecyRewardRankNum);
			rankScoreRatio = _rewardRankScoreRatio.get(maxCurrecyRewardRankNum);
		}
		// 每日荣誉奖励=等级每日荣誉量*日荣誉产量系数*荣誉排名参数/排名基数
		int score = (int) ((baseScore * rankScoreRatio * dayHonorRatio) / rankBaseRatio);
		// 每日金币奖励：等级每日金币量*当前金币排名参数/排名基数
		int gold = (int) ((baseGlod * rankGlodRatio) / rankBaseRatio);

		int diamond = _todayDiamondReward.get(maxDiamondRewardRankNum);
		Integer[] rankNumList = new Integer[_todayDiamondReward.keySet().size()];
		_todayDiamondReward.keySet().toArray(rankNumList);
		for (int i = 0; i < rankNumList.length; i++) {
			if (i != rankNumList.length - 1) {
				if (rank >= rankNumList[i] && rank < rankNumList[i + 1]) {
					diamond = _todayDiamondReward.get(rankNumList[i]);
					break;
				}
			}
		}

		CompetitionRewardShowData reward = new CompetitionRewardShowData(title, score, gold, diamond, "", "", "");
		return reward;
	}

	public static CompetitionRewardShowData caculateTodayShowReward(String title, int roleLv, int beginRank, int endRank) {
		float baseGlod;
		float baseScore;
		if (_winBaseGoldReward.containsKey(roleLv) && _winBaseScoreReward.containsKey(roleLv)) {
			baseGlod = _winBaseGoldReward.get(roleLv);
			baseScore = _winBaseScoreReward.get(roleLv);
		} else {
			baseGlod = _winBaseGoldReward.get(1);
			baseScore = _winBaseScoreReward.get(1);
		}
		float beginRankGlodRatio, beginRankScoreRatio, endRankGlodRatio = -1f, endRankScoreRatio = -1f;
		if (_rewardRankGoldRatio.containsKey(beginRank) && _rewardRankScoreRatio.containsKey(beginRank)) {
			beginRankGlodRatio = _rewardRankGoldRatio.get(beginRank);
			beginRankScoreRatio = _rewardRankScoreRatio.get(beginRank);
		} else {
			beginRankGlodRatio = _rewardRankGoldRatio.get(maxCurrecyRewardRankNum);
			beginRankScoreRatio = _rewardRankScoreRatio.get(maxCurrecyRewardRankNum);
		}
		if (endRank != -1) {
			if (_rewardRankGoldRatio.containsKey(endRank) && _rewardRankScoreRatio.containsKey(endRank)) {
				endRankGlodRatio = _rewardRankGoldRatio.get(endRank);
				endRankScoreRatio = _rewardRankScoreRatio.get(endRank);
			} else {
				endRankGlodRatio = _rewardRankGoldRatio.get(maxCurrecyRewardRankNum);
				endRankScoreRatio = _rewardRankScoreRatio.get(maxCurrecyRewardRankNum);
			}
		}
		// 开始排名每日荣誉奖励=等级每日荣誉量*日荣誉产量系数*荣誉排名参数/排名基数
		int beginScore = (int) ((baseScore * beginRankScoreRatio * dayHonorRatio) / rankBaseRatio);
		// 开始排名每日金币奖励：等级每日金币量*当前金币排名参数/排名基数
		int beginGold = (int) ((baseGlod * beginRankGlodRatio) / rankBaseRatio);

		int endScore = -1, endGold = -1;
		if (endRank != -1) {
			// 结束排名每日荣誉奖励=等级每日荣誉量*日荣誉产量系数*荣誉排名参数/排名基数
			endScore = (int) ((baseScore * endRankScoreRatio * dayHonorRatio) / rankBaseRatio);
			// 结束排名每日金币奖励：等级每日金币量*当前金币排名参数/排名基数
			endGold = (int) ((baseGlod * endRankGlodRatio) / rankBaseRatio);
		}

		int diamond;
		if (_todayDiamondReward.containsKey(beginRank)) {
			diamond = _todayDiamondReward.get(beginRank);
		} else {
			diamond = _todayDiamondReward.get(maxDiamondRewardRankNum);
		}

		CompetitionRewardShowData reward = new CompetitionRewardShowData(title, beginScore, beginGold, diamond, beginScore + ((endRank == -1) ? "" : ("~" + endScore)), beginGold
				+ ((endRank == -1) ? "" : ("~" + endGold)), diamond + "");
		return reward;
	}

	// public int getTodayScore(int roleLv, int rank){
	// float baseScore;
	// if ( _winBaseScoreReward.containsKey(roleLv)) {
	// baseScore = _winBaseScoreReward.get(roleLv);
	// } else {
	// baseScore = _winBaseScoreReward.get(1);
	// }
	// float rankScoreRatio;
	// if (_rewardRankScoreRatio.containsKey(rank)) {
	// rankScoreRatio = _rewardRankScoreRatio.get(rank);
	// } else {
	// rankScoreRatio = _rewardRankScoreRatio.get(maxCurrecyRewardRankNum);
	// }
	// // 每日荣誉奖励=等级每日荣誉量*日荣誉产量系数*荣誉排名参数/排名基数
	// return (int)((baseScore * rankScoreRatio * dayHonorRatio)
	// / rankBaseRatio);
	// }

	// public int getTodayGold(int roleLv, int rank){
	// float baseGlod;
	// if (_winBaseGoldReward.containsKey(roleLv)) {
	// baseGlod = _winBaseGoldReward.get(roleLv);
	// } else {
	// baseGlod = _winBaseGoldReward.get(1);
	// }
	// float rankGlodRatio;
	// if (_rewardRankGoldRatio.containsKey(rank)) {
	// rankGlodRatio = _rewardRankGoldRatio.get(rank);
	// } else {
	// rankGlodRatio = _rewardRankGoldRatio.get(maxCurrecyRewardRankNum);
	// }
	// // 每日金币奖励：等级每日金币量*当前金币排名参数/排名基数
	// return (int)((baseGlod * rankGlodRatio) / rankBaseRatio);
	// }

	// public static CompetitionRewardShowData caculateLastWeekReward(
	// String title, int roleLv, int rank) {
	// float baseGlod;
	// float baseScore;
	// if (_winBaseGoldReward.containsKey(roleLv)
	// && _winBaseScoreReward.containsKey(roleLv)) {
	// baseGlod = _winBaseGoldReward.get(roleLv);
	// baseScore = _winBaseScoreReward.get(roleLv);
	// } else {
	// baseGlod = _winBaseGoldReward.get(1);
	// baseScore = _winBaseScoreReward.get(1);
	// }
	// float rankGlodRatio, rankScoreRatio;
	// if (_rewardRankGoldRatio.containsKey(rank)
	// && _rewardRankScoreRatio.containsKey(rank)) {
	// rankGlodRatio = _rewardRankGoldRatio.get(rank);
	// rankScoreRatio = _rewardRankScoreRatio.get(rank);
	// } else {
	// rankGlodRatio = _rewardRankGoldRatio.get(maxCurrecyRewardRankNum);
	// rankScoreRatio = _rewardRankScoreRatio.get(maxCurrecyRewardRankNum);
	// }
	// // 每周荣誉奖励=等级每日荣誉量*日荣誉产量系数*荣誉排名参数/排名基数*周奖励荣誉倍数
	// int score = (int) (((baseScore * rankScoreRatio * dayHonorRatio) /
	// rankBaseRatio) * weekHonorRatio);
	// // 每周金币奖励=等级每日金币量*当前金币排名参数/排名基数*周奖励金币倍数
	// int gold = (int) (((baseGlod * rankGlodRatio) / rankBaseRatio) *
	// weekGoldRatio);
	// ItemCountStruct item = null;
	// int lastCheckRank = 0;
	// if (rank > maxItemRewardRankNum) {
	// item = _weekItemReward.get(maxItemRewardRankNum);
	// } else {
	// for (Integer checkRank : _weekItemReward.keySet()) {
	// /*
	// * if (rank > lastCheckRank && rank <= checkRank) { item =
	// * _weekItemReward.get(checkRank); }
	// */if (rank >= lastCheckRank && rank < checkRank) {
	// item = _weekItemReward.get(lastCheckRank);
	// break;
	// } else {
	// lastCheckRank = checkRank;
	// }
	// }
	// if (item == null) {
	// item = _weekItemReward.get(maxItemRewardRankNum);
	// }
	// }
	//
	// CompetitionRewardShowData reward = new CompetitionRewardShowData(title,
	// score, gold, item, "", "");
	// return reward;
	// }

	// public static CompetitionRewardShowData caculateLastWeekShowReward(
	// String title, int roleLv, int beginRank, int endRank) {
	// float baseGlod;
	// float baseScore;
	// if (_winBaseGoldReward.containsKey(roleLv)
	// && _winBaseScoreReward.containsKey(roleLv)) {
	// baseGlod = _winBaseGoldReward.get(roleLv);
	// baseScore = _winBaseScoreReward.get(roleLv);
	// } else {
	// baseGlod = _winBaseGoldReward.get(1);
	// baseScore = _winBaseScoreReward.get(1);
	// }
	// float beginRankGlodRatio, beginRankScoreRatio, endRankGlodRatio = -1f,
	// endRankScoreRatio = -1f;
	// if (_rewardRankGoldRatio.containsKey(beginRank)
	// && _rewardRankScoreRatio.containsKey(beginRank)) {
	// beginRankGlodRatio = _rewardRankGoldRatio.get(beginRank);
	// beginRankScoreRatio = _rewardRankScoreRatio.get(beginRank);
	// } else {
	// beginRankGlodRatio = _rewardRankGoldRatio
	// .get(maxCurrecyRewardRankNum);
	// beginRankScoreRatio = _rewardRankScoreRatio
	// .get(maxCurrecyRewardRankNum);
	// }
	// if (endRank != -1) {
	// if (_rewardRankGoldRatio.containsKey(endRank)
	// && _rewardRankScoreRatio.containsKey(endRank)) {
	// endRankGlodRatio = _rewardRankGoldRatio.get(endRank);
	// endRankScoreRatio = _rewardRankScoreRatio.get(endRank);
	// } else {
	// endRankGlodRatio = _rewardRankGoldRatio
	// .get(maxCurrecyRewardRankNum);
	// endRankScoreRatio = _rewardRankScoreRatio
	// .get(maxCurrecyRewardRankNum);
	// }
	// }
	// // 开始排名每周荣誉奖励=等级每日荣誉量*日荣誉产量系数*荣誉排名参数/排名基数*周奖励荣誉倍数
	// int beginScore = (int) (((baseScore * beginRankScoreRatio *
	// dayHonorRatio) / rankBaseRatio) * weekHonorRatio);
	// // 开始排名每周金币奖励=等级每日金币量*当前金币排名参数/排名基数*周奖励金币倍数
	// int beginGold = (int) (((baseGlod * beginRankGlodRatio) / rankBaseRatio)
	// * weekGoldRatio);
	// int endScore = -1, endGold = -1;
	// if (endRank != -1) {
	// // 结束排名每周荣誉奖励=等级每日荣誉量*日荣誉产量系数*荣誉排名参数/排名基数*周奖励荣誉倍数
	// endScore = (int) (((baseScore * endRankScoreRatio * dayHonorRatio) /
	// rankBaseRatio) * weekHonorRatio);
	// // 结束排名每周金币奖励=等级每日金币量*当前金币排名参数/排名基数*周奖励金币倍数
	// endGold = (int) (((baseGlod * endRankGlodRatio) / rankBaseRatio) *
	// weekGoldRatio);
	// }
	//
	// ItemCountStruct item = null;
	// int lastCheckRank = 0;
	// if (beginRank > maxItemRewardRankNum) {
	// item = _weekItemReward.get(maxItemRewardRankNum);
	// } else {
	// for (Integer checkRank : _weekItemReward.keySet()) {
	// /*
	// * if (beginRank > lastCheckRank && beginRank <= checkRank) {
	// * item = _weekItemReward.get(checkRank); }
	// */
	// if (beginRank >= lastCheckRank && beginRank < checkRank) {
	// item = _weekItemReward.get(lastCheckRank);
	// break;
	// } else {
	// lastCheckRank = checkRank;
	// }
	// }
	// if (item == null) {
	// item = _weekItemReward.get(maxItemRewardRankNum);
	// }
	// }
	//
	// CompetitionRewardShowData reward = new CompetitionRewardShowData(title,
	// beginScore, beginGold, item, beginScore
	// + ((endRank == -1) ? "" : ("~" + endScore)), beginGold
	// + ((endRank == -1) ? "" : ("~" + endGold)));
	// return reward;
	// }

	public void sendCompetitionData(KRole role, KCompetitor comp) {

		checkAndRestCompetitionData(role);

		KGameMessage msg = KGame.newLogicMessage(KCompetitionProtocol.SM_SYNC_GET_COMPETITION_LIST);
		msg.writeInt(comp.getRanking());
		msg.writeShort(comp.getCanChallengeTimes());
		msg.writeShort(KCompetitionModuleConfig.getMaxChallengeTimePerDay());
		// 今日奖励
		msg.writeBoolean(true);
		msg.writeInt(comp.getTodayCompetitionReward().getReward().score);
		msg.writeInt(comp.getTodayCompetitionReward().getReward().gold);
		msg.writeInt(comp.getTodayCompetitionReward().getReward().diamond);
		msg.writeBoolean(comp.getTodayCompetitionReward().isHadReceivedReward());
		// // 本周奖励
		// boolean hasWeekReward = (comp.getLastWeekReward() != null);
		//
		// msg.writeBoolean(hasWeekReward);
		// if (hasWeekReward) {
		// msg.writeInt(comp.getLastWeekReward().getReward().score);
		// msg.writeInt(comp.getLastWeekReward().getReward().gold);
		// ItemCountStruct item = comp.getLastWeekReward().getReward().item;
		// KItemMsgPackCenter.packItem(msg, item.getItemTemplate(),
		// item.itemCount);
		// msg.writeBoolean(comp.getLastWeekReward().isHadReceivedReward());
		// }
		// 挑战记录
		KCompetitionBattleHistory[] history = comp.getHistory();
		if (history != null) {
			msg.writeByte(history.length);
			for (int i = 0; i < history.length; i++) {
				msg.writeBoolean(history[i].isWin);
				msg.writeUtf8String(history[i].tips);
			}
		} else {
			msg.writeByte(0);
		}
		List<KCompetitor> pickList = pickCompetitor(comp, true);
		msg.writeByte(pickList.size());
		for (KCompetitor temp : pickList) {
			temp.packInfo(msg);
		}
		// 今日排名奖励信息表奖励条数
		List<CompetitionRewardShowData> todayList = todayCompetitionRewardShowDataMap.get(role.getLevel());
		if (todayList == null) {
			todayList = todayCompetitionRewardShowDataMap.get(1);
		}
		msg.writeByte(todayList.size());
		for (CompetitionRewardShowData data : todayList) {
			data.packInfoMsg(msg);
		}

		// // 上周排名奖励信息表奖励条数
		// List<CompetitionRewardShowData> weekList =
		// weekCompetitionRewardShowDataMap
		// .get(role.getLevel());
		// if (weekList == null) {
		// weekList = weekCompetitionRewardShowDataMap.get(1);
		// }
		// msg.writeByte(weekList.size());
		// for (CompetitionRewardShowData data : weekList) {
		// data.packInfoMsg(msg);
		// }
		// 是否有CD时间
		boolean isCdTime = comp.isCdTime();
		msg.writeBoolean(isCdTime);
		if (isCdTime) {
			int restTimeSeconds = comp.getRestCDTimeSeconds();
			msg.writeInt(restTimeSeconds);
			msg.writeInt(KCompetitionManager.clearCDTimePerMin);
		}

		role.sendMsg(msg);
	}

	public void getTodayCompetitionPrice(KRole role) {
		KCompetitor roleC = getCompetitorByRoleId(role.getId());
		if (roleC == null) {
			KDialogService.sendUprisingDialog(role, GlobalTips.getTipsServerBusy());
			return;
		}
		KCompetitionReward reward = roleC.getTodayCompetitionReward();
		if (reward == null) {
			KDialogService.sendUprisingDialog(role, GlobalTips.getTipsServerBusy());
			return;
		}
		if (reward.isHadReceivedReward()) {
			KDialogService.sendUprisingDialog(role, GlobalTips.getTipsServerBusy());
			return;
		}

		boolean result = processGetReward(role, reward);
		if (result) {
			KCompetitionServerMsgSender.sendUpdateRewardWhileGet(role, reward.getReward(), true, true);

			reward.notifyReceivedReward();
			roleC.notifyDB();
			KDialogService.sendDataUprisingDialog(role, reward.getReward().dataUprisingTips);
		} else {
			KDialogService.sendUprisingDialog(role, GlobalTips.getTipsServerBusy());
		}
	}

	// public void getLastWeekCompetitionPrice(KRole role) {
	// KCompetitor roleC = getCompetitorByRoleId(role.getId());
	// if (roleC == null) {
	// KDialogService.sendUprisingDialog(role,
	// GlobalTips.getTipsServerBusy());
	// return;
	// }
	// KCompetitionReward reward = roleC.getLastWeekReward();
	// if (reward == null) {
	// KDialogService.sendUprisingDialog(role,
	// GlobalTips.getTipsServerBusy());
	// return;
	// }
	// if (reward.isHadReceivedReward()) {
	// KDialogService.sendUprisingDialog(role,
	// GlobalTips.getTipsServerBusy());
	// return;
	// }
	//
	// boolean result = processGetReward(role, reward);
	// if (result) {
	// KCompetitionServerMsgSender.sendUpdateRewardWhileGet(role,
	// reward.getReward(), false, true);
	//
	// reward.notifyReceivedReward();
	// roleC.notifyDB();
	// KDialogService.sendDataUprisingDialog(role,
	// reward.getReward().dataUprisingTips);
	// } else {
	// KDialogService.sendUprisingDialog(role,
	// GlobalTips.getTipsServerBusy());
	// }
	// }

	private boolean processGetReward(KRole role, KCompetitionReward reward) {
		CompetitionRewardShowData data = reward.getReward();
		if (reward != null) {
			KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), KCurrencyTypeEnum.GOLD, data.gold, PresentPointTypeEnum.竞技场排名奖励, true);
			KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), KCurrencyTypeEnum.SCORE, data.score, PresentPointTypeEnum.竞技场排名奖励, true);
			KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), KCurrencyTypeEnum.DIAMOND, data.diamond, PresentPointTypeEnum.竞技场排名奖励, true);
			// if (data.isHasItem && data.item != null) {
			// ItemResult_AddItem result = KSupportFactory
			// .getItemModuleSupport().addItemToBag(role, data.item,
			// this.getClass().getSimpleName());
			// }

			return true;
		} else {
			return false;
		}
	}

	public void confirmCompleteBattle(KRole role) {
		KSupportFactory.getMapSupport().processRoleFinishBattleReturnToMap(role);
	}

	public void processAddChallengeTime(KRole role, boolean isNeedCheck) {
		KCompetitor roleC = getCompetitorByRoleId(role.getId());
		if (roleC == null) {
			KDialogService.sendUprisingDialog(role, GlobalTips.getTipsServerBusy());
			return;
		}
		if (isNeedCheck) {
			if (roleC.getCanChallengeTimes() >= KCompetitionModuleConfig.getMaxChallengeTimePerDay()) {
				KDialogService.sendUprisingDialog(role, CompetitionTips.getTipsChallengeTimeFull());
				return;
			}

			VIPLevelData vipData = KSupportFactory.getVIPModuleSupport().getVIPLevelData(role.getId());
			int vip_canChallengeCount = vipData.pvpbuyrmb.length;
			int buyCount = roleC.getTodayBuyCount().get();
			if (buyCount >= vip_canChallengeCount) {
				KDialogService.sendUprisingDialog(role, CompetitionTips.getTipsCannotBuyChallengeTime(vipData.lvl, vip_canChallengeCount));
				return;
			}
			// int point = KCompetitionModuleConfig.getAddChallengeTimeMoney();
			int point = vipData.pvpbuyrmb[buyCount];

			List<KDialogButton> buttons = new ArrayList<KDialogButton>();
			buttons.add(KDialogButton.CANCEL_BUTTON);
			buttons.add(new KDialogButton(KCompetitionDialogProcesser.KEY_CONFIRM_ADD_CHALLENGE_TIME, "", KDialogButton.CONFIRM_DISPLAY_TEXT));
			KDialogService.sendFunDialog(role, GlobalTips.getTipsDefaultTitle(), CompetitionTips.getTipsBuyChallengeTime(point), buttons, true, (byte) -1);
			return;
		}

		roleC.increaseCanChallengeTime(1);
	}

	public void checkAndRestCompetitionData(KRole role) {
		KCompetitor roleC = getCompetitorByRoleId(role.getId());
		if (roleC == null) {
			return;
		}
		roleC.checkChallengeTime();
	}

	public void processGetHallOfFrameData(KRole role) {
		KCompetitor roleC = getCompetitorByRoleId(role.getId());
		if (roleC != null) {
			KGameMessage msg = KGame.newLogicMessage(KCompetitionProtocol.SM_GET_HALL_OF_FRAME);
			msg.writeByte(hallOfFrameDataMap.size());
			for (Integer position : roleC.getHallOfFrameVisitData().keySet()) {
				boolean isVisit = roleC.isVisitHall(position);
				KHallOfFrameData data = hallOfFrameDataMap.get(position);
				msg.writeByte(position);
				if (data != null) {
					IRoleMapResInfo info = null;
					KRole hRole = null;
					RoleModuleSupport roleSupport = KSupportFactory.getRoleModuleSupport();
					if (roleSupport.isRoleDataInCache(data.roleId)) {
						hRole = roleSupport.getRole(data.roleId);
						info = hRole;
					} else {
						info = DefaultRoleMapResInfoManager.getDefaultRoleMapResInfo(KJobTypeEnum.getJob(data.job));
					}
					msg.writeBoolean(data.isHasData);
					if (data.isHasData) {
						msg.writeUtf8String(data.roleName);
						msg.writeInt(data.inMapResId);
						KSupportFactory.getRoleModuleSupport().packRoleResToMsg(info, data.job, msg);
						boolean hasRedEquip = false;
						if(hRole!=null){
							List<RoleEquipShowData> list = KSupportFactory.getItemModuleSupport().getRoleEquipShowDataList(hRole.getId());
							if(list!=null){
								for (RoleEquipShowData equipData:list) {
									if(equipData.equipType == KEquipmentTypeEnum.主武器.sign){
										if(equipData.getQuality() == KItemQualityEnum.无敌的){
											hasRedEquip = true;
										}
									}
								}
							}
						}
						msg.writeBoolean(hasRedEquip);
						msg.writeBoolean(isVisit);
						msg.writeByte(data.vipLv);
						int[] itemResInfo = KSupportFactory.getItemModuleSupport().getEquiSetMapResIds(data.roleId);
						if (itemResInfo == null || itemResInfo.length != 2) {
							itemResInfo = new int[] { 0, 0 };
						}
						msg.writeInt(itemResInfo[0]);
						msg.writeInt(itemResInfo[1]);
					}
				} else {
					msg.writeBoolean(false);
				}
			}
			msg.writeByte(hallOfFrameHistory.size());
			for (String info : hallOfFrameHistory) {
				msg.writeUtf8String(info);
			}
			role.sendMsg(msg);
		} else {
			KDialogService.sendUprisingDialog(role, GlobalTips.getTipsServerBusy());
		}
	}

	public void processVisitHall(KRole role, int position) {
		KCompetitor roleC = getCompetitorByRoleId(role.getId());
		if (roleC == null) {
			KDialogService.sendUprisingDialog(role, GlobalTips.getTipsServerBusy());
			return;
		}
		if (roleC.isVisitHall(position)) {
			KDialogService.sendUprisingDialog(role, CompetitionTips.getTipsAlreadyVisit());
			return;
		} else {
			boolean result = roleC.visitHall(position);
			if (result) {
				int exp = visitHallExpMap.get(role.getLevel());
				String info = StringUtil.format(ShopTips.x加x, KGameAttrType.EXPERIENCE.getExtName(), exp);
				List<AttValueStruct> struct = new ArrayList<AttValueStruct>();
				struct.add(new AttValueStruct(KGameAttrType.EXPERIENCE, exp));
				// KSupportFactory.getRoleModuleSupport().addAttsFromBaseReward(
				// role, struct);
				KSupportFactory.getRoleModuleSupport().addAttsFromBaseReward(role, struct, KRoleAttrModifyType.队伍竞技每日奖励);

				KGameMessage msg = KGame.newLogicMessage(KCompetitionProtocol.SM_VISIT_HALL);
				msg.writeByte(position);
				msg.writeBoolean(true);
				role.sendMsg(msg);

				KDialogService.sendUprisingDialog(role, CompetitionTips.getTipsVisitSuccess());
				KDialogService.sendDataUprisingDialog(role, info);

				// 通知活跃度模块
				KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.名人堂膜拜);

			} else {
				KDialogService.sendUprisingDialog(role, CompetitionTips.getTipsAlreadyVisit());
				return;
			}
		}
	}

	public void notifyVipLvChange(long roleId, int nowVipLv) {
		KCompetitor roleC = getCompetitorByRoleId(roleId);
		if (roleC == null) {
			return;
		}
		roleC.notifyVipLvChange(nowVipLv);
		for (KHallOfFrameData data : hallOfFrameDataMap.values()) {
			if (data.isHasData && data.roleId == roleId) {
				data.vipLv = nowVipLv;
				break;
			}
		}
	}

	private void saveHallOfFrame() {
		try {
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(_savePath));
			dos.writeInt(CURRENT_VERSION);
			dos.writeInt(hallOfFrameDataMap.size());
			byte[] names;
			KHallOfFrameData data;
			for (int i = KHallOfFrameData.HOF_MAX_POSITION; i <= KHallOfFrameData.HOF_MIN_POSITION; i++) {
				data = hallOfFrameDataMap.get(i);
				if (data != null) {
					dos.writeInt(data.position);
					if (data.isHasData) {
						dos.writeByte(1);
						dos.writeLong(data.roleId);
						names = data.roleName.getBytes(UTF);
						dos.writeShort(names.length);
						dos.write(names);
						dos.writeInt(data.inMapResId);
						dos.writeByte(data.job);
					} else {
						dos.writeByte(0);
					}
				}
			}
			dos.writeInt(hallOfFrameHistory.size());
			for (String info : hallOfFrameHistory) {
				names = info.getBytes(UTF);
				dos.writeShort(names.length);
				dos.write(names);
			}
			dos.flush();
			dos.close();
		} catch (Exception e) {
			_LOGGER.error("保存竞技场名人堂的时候出现异常！", e);
		}
	}

	public void readHallOfFrame() {
		File file = new File(_savePath);
		if (file.exists()) {
			try {
				DataInputStream dis = new DataInputStream(new FileInputStream(file));
				int version = dis.readInt(); // 版本号
				int count = dis.readInt();
				KHallOfFrameData data;
				for (int i = 0; i < count; i++) {
					data = new KHallOfFrameData();
					data.position = dis.readInt();
					data.isHasData = (dis.readByte() == 1);
					if (data.isHasData) {
						data.roleId = dis.readLong();
						short nameLen = dis.readShort();
						byte[] names = new byte[nameLen];
						dis.read(names);
						data.roleName = new String(names, UTF);
						data.inMapResId = dis.readInt();
						data.job = dis.readByte();
						hallOfFrameDataMap.put(data.position, data);
						_LOGGER.info("----------->>>>>>>>>竞技场名人堂：职位：{}，名：{}", data.position, data.roleName);
					}
				}
				int hallOfFrameHistorySize = dis.readInt();
				for (int i = 0; i < hallOfFrameHistorySize; i++) {
					short nameLen = dis.readShort();
					byte[] names = new byte[nameLen];
					dis.read(names);
					String info = new String(names, UTF);
					hallOfFrameHistory.addFirst(info);
					_LOGGER.info("----------->>>>>>>>>读取竞技场名人堂历史记录：{}", info);
				}
				dis.close();
			} catch (Exception e) {
				_LOGGER.error("读取竞技场排行榜的时候出现异常！", e);
			}
		} else {
			_LOGGER.info("$$$$####没有竞技场排行榜文件####$$$$");
		}

		if (hallOfFrameDataMap.isEmpty()) {
			for (int i = KHallOfFrameData.HOF_MAX_POSITION; i <= KHallOfFrameData.HOF_MIN_POSITION; i++) {
				KHallOfFrameData data = new KHallOfFrameData();
				data.position = i;

				KCompetitor c = getCompetitorByRanking(i);
				if (c != null) {
					data.roleId = c.getRoleId();
					data.isHasData = true;
					data.roleName = c.getRoleName();
					data.inMapResId = c.getInMapResId();
					data.job = c.getOccupation();
					data.vipLv = c.getVipLv();
					hallOfFrameDataMap.put(i, data);
					if (i == 1) {
						String dateInfo = UtilTool.DATE_FORMAT4.format(new Date(System.currentTimeMillis()));
						addHallOfFrameHistory(CompetitionTips.getTipsHistory1(dateInfo, c.getExtRoleName()));
					}
				}
				hallOfFrameDataMap.put(i, data);
			}
		}
	}

	void checkAndSaveHallOfFrameWhileShutdown() {
		File confile = new File(this._savePath);
		if (confile.exists() && confile.isFile()) {
			return;
		} else {
			saveHallOfFrame();
		}
	}

	void addHallOfFrameHistory(String history) {
		if (!hallOfFrameHistory.offerFirst(history)) {
			hallOfFrameHistory.removeLast();
			hallOfFrameHistory.addFirst(history);
		}
	}

	public static class CompetitionRewardShowData {
		public String rankInfo;
		public int score;
		public int gold;
		public int diamond;
		public final List<String> dataUprisingTips = new ArrayList<String>();// 所有奖励内容的浮动提示
		public String scoreInfo;
		public String goldInfo;
		public String diamondInfo;

		public CompetitionRewardShowData(String rankInfo, int score, int gold, int diamond, String scoreInfo, String goldInfo, String diamondInfo) {
			super();
			this.rankInfo = rankInfo;
			this.score = score;
			this.gold = gold;
			this.diamond = diamond;
			this.scoreInfo = scoreInfo;
			this.goldInfo = goldInfo;
			this.diamondInfo = diamondInfo;
			if (gold > 0) {
				dataUprisingTips.add(StringUtil.format(ShopTips.x加x, KCurrencyTypeEnum.GOLD.extName, gold));
			}
			if (score > 0) {
				dataUprisingTips.add(StringUtil.format(ShopTips.x加x, KCurrencyTypeEnum.SCORE.extName, score));
			}
			if (diamond > 0) {
				dataUprisingTips.add(StringUtil.format(ShopTips.x加x, KCurrencyTypeEnum.DIAMOND.extName, diamond));
			}

		}

		public void packRewardMsg(KGameMessage msg) {
			msg.writeInt(score);
			msg.writeInt(gold);
			msg.writeInt(diamond);
		}

		public void packInfoMsg(KGameMessage msg) {
			msg.writeUtf8String(rankInfo);
			msg.writeUtf8String(scoreInfo);
			msg.writeUtf8String(goldInfo);
			msg.writeUtf8String(diamondInfo);
		}
	}

	public static class CompetitionCombatData {
		public long defenderRoleId;
		public int defenderRanking;
	}

	public static class CompetitionVipModuleListener implements KVIPUpLvListener {

		@Override
		public void notifyVIPLevelUp(KRoleVIP vip, int preLv) {
			KCompetitionModule.getCompetitionManager().notifyVipLvChange(vip.getRoleId(), vip.getLv());
		}

	}

	private static String getReward(int rank, Map<Integer, String> map) {
		int last = 0;
		String str = null;
		for (Integer now : map.keySet()) {
			if (rank >= last && rank < now) {
				str = map.get(last);
				break;
			} else {
				last = now;
			}
		}
		if (str == null) {
			str = map.get(5001);
		}
		return str;
	}

	public static void main(String[] args) {
		// Map<Integer, String> map = new LinkedHashMap<Integer, String>();
		// map.put(1, "340518");
		// map.put(11, "340517");
		// map.put(51, "340516");
		// map.put(201, "340515");
		// map.put(501, "340514");
		// map.put(1001, "340513");
		// map.put(3001, "340512");
		// map.put(5001, "340511");
		// System.out.println("1=" + getReward(1, map));
		// System.out.println("2=" + getReward(2, map));
		// System.out.println("10=" + getReward(10, map));
		//
		// System.out.println("11=" + getReward(11, map));
		// System.out.println("22=" + getReward(22, map));
		// System.out.println("50=" + getReward(50, map));
		//
		// System.out.println("51=" + getReward(51, map));
		// System.out.println("81=" + getReward(81, map));
		// System.out.println("200=" + getReward(200, map));
		//
		// System.out.println("201=" + getReward(201, map));
		// System.out.println("401=" + getReward(401, map));
		// System.out.println("500=" + getReward(500, map));
		//
		// System.out.println("501=" + getReward(501, map));
		// System.out.println("801=" + getReward(801, map));
		// System.out.println("1000=" + getReward(1000, map));
		//
		// System.out.println("1001=" + getReward(1001, map));
		// System.out.println("2001=" + getReward(2001, map));
		// System.out.println("3000=" + getReward(3000, map));
		//
		// System.out.println("3001=" + getReward(3001, map));
		// System.out.println("4001=" + getReward(4001, map));
		// System.out.println("5000=" + getReward(5000, map));
		//
		// System.out.println("5001=" + getReward(5001, map));
		// System.out.println("6000=" + getReward(6000, map));

		int currentRank = 500, serialWinCount = 100, lastRank = 520;

		// int nextRankNum = currentRank + 1;
		// int maxRankNum = (int) (Math.pow(1.12d,
		// (double) (Math.abs(serialWinCount))) * (Math.pow(
		// (double) (currentRank), 0.6d) + 12));
		//
		// int preRankNum = currentRank - 1;
		// int minRankNum = Math.max(1, (int)(currentRank -
		// (int)(Math.pow(1.12d, (double)(serialWinCount))*(Math.pow(
		// (double) (currentRank), 0.6d) + 12))));
		//
		// System.out.println("minRankNum:" + minRankNum);

		for (int k = 0; k < 10; k++) {
			List<Integer> result = getRankNums(currentRank, serialWinCount, lastRank);
			System.out.println("RankSize:" + result.size());
			System.out.print("Rank:");
			for (int i = 0; i < result.size(); i++) {
				System.out.print(result.get(i) + ",");
			}
			System.out.println();
		}

	}

	public static List<Integer> getRankNums(int currentRank, int serialWinCount, int lastRankNum) {
		List<Integer> rankNums = new ArrayList<Integer>();

		if (serialWinCount > 7) {
			serialWinCount = 7;
		}
		if (serialWinCount < -7) {
			serialWinCount = -7;
		}

		// 当排名小于等于7
		if (currentRank <= 7) {
			// 取1~当前排名
			for (int i = 1; i <= currentRank; i++) {
				rankNums.add(i);
			}
			// 再在区间，当前排名+1 ~ [当前排名+1.12^|n|*(当前排名^0.6+12)] 取（10-当前排名）个
			int nextRankNum = currentRank + 1;
			int maxRankNum = (int) (Math.pow(1.12d, (double) (Math.abs(serialWinCount))) * (Math.pow((double) (currentRank), 0.6d) + 12));

			List<Integer> tempRank = new ArrayList<Integer>();
			for (int i = nextRankNum; i <= maxRankNum; i++) {
				tempRank.add(i);
			}
			UtilTool.randomList(tempRank);

			for (int i = 0; i < (10 - currentRank); i++) {
				rankNums.add(tempRank.get(i));
			}
			Collections.sort(rankNums);

		} else {
			// 将自己排名加入列表
			rankNums.add(currentRank);
			// 随机数s=rand(5,7)，s为整数,随机数s代表，本人在挑战列表中的位置
			int index;
			boolean isNearLast = false;
			if (lastRankNum - currentRank > 5) {
				index = UtilTool.random(5, 7);
			} else {
				index = 10 - (lastRankNum - currentRank);
				isNearLast = true;
			}

			// 在区间 max{ 1, 当前排名-[1.12^n*(当前排名^0.6+12)]} ~ 当前排名-1 ，中随机取s-1个加上本身
			int preRankNum = currentRank - 1;
			int minRankNum = Math.max(1, (int) (currentRank - (int) (Math.pow(1.12d, (double) (serialWinCount)) * (Math.pow((double) (currentRank), 0.6d) + 12))));
			List<Integer> tempRank = new ArrayList<Integer>();
			for (int i = minRankNum; i <= preRankNum; i++) {
				tempRank.add(i);
			}
			UtilTool.randomList(tempRank);

			for (int i = 0; i < (index - 1) && i < tempRank.size(); i++) {
				rankNums.add(tempRank.get(i));
			}

			// 在区间 当前排名+1 ~ [当前排名+1.12^|n|*(当前排名^0.6+12)] ，中随机取10-s个
			if (!isNearLast) {
				int nextRankNum = currentRank + 1;
				int maxRankNum = currentRank + (int) (Math.pow(1.12d, (double) (Math.abs(serialWinCount))) * (Math.pow((double) (currentRank), 0.6d) + 12));
				if (maxRankNum > lastRankNum) {
					maxRankNum = lastRankNum;
				}

				tempRank.clear();
				for (int i = nextRankNum; i <= maxRankNum; i++) {
					tempRank.add(i);
				}
				UtilTool.randomList(tempRank);

				for (int i = 0; i < (10 - index) && i < tempRank.size(); i++) {
					rankNums.add(tempRank.get(i));
				}
			} else {
				for (int i = (currentRank + 1); i <= lastRankNum; i++) {
					rankNums.add(i);
				}
			}
			Collections.sort(rankNums);

		}

		return rankNums;
	}

	// public static <T> void randomList(List<T> list) {
	// Collections.sort(list, new Comparator<T>() {
	// HashMap<T, Double> map = new HashMap<T, Double>();
	//
	// public int compare(T v1, T v2) {
	// init(v1);
	// init(v2);
	//
	// double n1 = ((Double) map.get(v1)).doubleValue();
	// double n2 = ((Double) map.get(v2)).doubleValue();
	// if (n1 > n2)
	// return 1;
	// else if (n1 < n2)
	// return -1;
	// return 0;
	// }
	//
	// private void init(T v) {
	// if (map.get(v) == null) {
	// map.put(v, new Double(Math.random()));
	// }
	// }
	//
	// protected void finalize() throws Throwable {
	// map = null;
	// }
	// });
	// }
	//
	// public static int random(int min, int max) {
	// if (min > max) {
	// int temp = min;
	// min = max;
	// max = temp;
	// }
	// return new Random().nextInt((max + 1) - min) + min;
	// }
}
