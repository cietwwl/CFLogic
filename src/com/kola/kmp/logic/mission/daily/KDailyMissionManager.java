package com.kola.kmp.logic.mission.daily;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import jxl.read.biff.BiffException;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.actionrecord.KActionRecorder;
import com.kola.kmp.logic.actionrecord.KActionType;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.level.KGameLevelSet;
import com.kola.kmp.logic.level.KLevelReward.NormalItemRewardTemplate;
import com.kola.kmp.logic.map.KGameNormalMap;
import com.kola.kmp.logic.map.KMapModule;
import com.kola.kmp.logic.mission.IMissionMenuImpl;
import com.kola.kmp.logic.mission.KMission;
import com.kola.kmp.logic.mission.KMission.AnswerQuestionRecord;
import com.kola.kmp.logic.mission.KMission.CompletedGameLevelRecord;
import com.kola.kmp.logic.mission.KMission.KillMonsterRecord;
import com.kola.kmp.logic.mission.KMission.UseFunctionRecord;
import com.kola.kmp.logic.mission.KMissionModuleDialogProcesser;
import com.kola.kmp.logic.mission.KMissionModuleExtension;
import com.kola.kmp.logic.mission.KMissionModuleSupportImpl;
import com.kola.kmp.logic.mission.KMissionSet;
import com.kola.kmp.logic.mission.KMissionSet.UpdateDailyMissionStruct;
import com.kola.kmp.logic.mission.KMissionTemplate;
import com.kola.kmp.logic.mission.MissionCompleteCondition;
import com.kola.kmp.logic.mission.MissionCompleteCondition.AnswerQuestionTask;
import com.kola.kmp.logic.mission.MissionCompleteCondition.CollectItemTask;
import com.kola.kmp.logic.mission.MissionCompleteCondition.GameLevelTask;
import com.kola.kmp.logic.mission.MissionCompleteCondition.KillMonsterTask;
import com.kola.kmp.logic.mission.MissionCompleteCondition.UseFunctionTask;
import com.kola.kmp.logic.mission.MissionCompleteCondition.UseItemTask;
import com.kola.kmp.logic.mission.guide.KGuideManager;
import com.kola.kmp.logic.npc.dialog.KDefaultDialogProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogButton;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.npc.menu.IMissionMenu;
import com.kola.kmp.logic.npc.menu.KMenuService;
import com.kola.kmp.logic.other.DailyMissionOperateTypeEnum;
import com.kola.kmp.logic.other.KColorFunEnum.KColor;
import com.kola.kmp.logic.other.KColorFunEnum.KColorManager;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KFunctionTypeEnum;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KGameMissionFunTypeEnum;
import com.kola.kmp.logic.other.KGameMissionStatusEnum;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KRelationShipTypeEnum;
import com.kola.kmp.logic.other.KUseFunctionTypeEnum;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.role.KRoleModuleConfig;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.text.HyperTextTool;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.LevelTips;
import com.kola.kmp.logic.util.tips.MissionTips;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;
import com.kola.kmp.protocol.mission.KMissionProtocol;

public class KDailyMissionManager {

	public final static byte MISSION_REFLASH_TYPE_FIRST = 1;

	public final static byte MISSION_REFLASH_TYPE_GUIDE = 2;

	public final static byte MISSION_REFLASH_TYPE_NORMAL = 3;

	/**
	 * 修行任务检测的时效任务每次检测的时间间隔（单位：秒）
	 */
	public static long dailyMissionCheckerTimeSeconds = 60;

	public static int mission_lv_mod = 5;

	public static int default_mission_lv = 999;
	// 任务品质权重总值
	private int mission_star_total_weight;
	// 默认的任务品质
	public static DailyMissionQualityType default_mission_quality_type;
	// 默认使用钻石刷新后必现的任务品质
	public static DailyMissionQualityType default_use_point_mission_quality_type;
	// 默认的任务标准奖励数据
	private DailyMissionPriceData default_mission_price_data;
	// 默认宝箱数据
	private DailyMissionPriceBox default_price_box;

	// 默认宝箱数据
	private List<DailyMissionPriceBox> default_price_box_list;
	// 默认宝箱数据
	private List<DailyMissionPriceBox> max_lv_price_box_list;
	// 刷新任务需要消耗点数
	public static int reflash_use_point = 5;
	// 可领取宝箱奖励的目标积分
	public static int target_price_box_star = 100;

	// public static int vip_complete_mission_count = 10;
	// 每天免费完成任务次数
	public static int free_complete_mission_count = 10;
	// 每天免费刷新任务次数
	public static int free_reflash_count = 5;
	// 自动完成任务消耗钻石数
	public static int auto_complete_mission_use_point = 5;
	// 每次购买增加可完成任务次数
	public static int add_complete_mission_count = 5;

	// 日常任务模版Map
	private final Map<Integer, KMissionTemplate> allMissionTemplates = new LinkedHashMap<Integer, KMissionTemplate>();

	/**
	 * <pre>
	 * Map<Integer, List<KMissionTemplate>>的Key表示：角色等级段
	 * 
	 * List<KMissionTemplate>表示每个品质的任务模版列表
	 * </pre>
	 */
	private final Map<Integer, List<KMissionTemplate>> missionTemplateMapByRoleLevel = new HashMap<Integer, List<KMissionTemplate>>();

	/**
	 * 日常任务品质数据map，Key：星数
	 */
	public Map<Integer, DailyMissionQualityType> qualityTypeMap = new LinkedHashMap<Integer, DailyMissionQualityType>();
	// 新手引导的指定任务品质List
	public List<DailyMissionQualityType> guideQualityList = new ArrayList<DailyMissionQualityType>();

	/**
	 * 日常任务标准奖励Map，Key：角色等级
	 */
	public Map<Integer, DailyMissionPriceData> priceDataMap = new LinkedHashMap<Integer, DailyMissionPriceData>();

	/**
	 * 日常任务宝箱列表
	 */
	public List<List<DailyMissionPriceBox>> priceBoxList = new ArrayList<List<DailyMissionPriceBox>>();

	/**
	 * 日常任务宝箱列表
	 */
	public Map<Integer, DailyMissionPriceBox> priceBoxListById = new LinkedHashMap<Integer, DailyMissionPriceBox>();
	/**
	 * 购买日常任务消耗元宝表，Key：购买次数
	 */
	public Map<Integer, Integer> buyMissionUsePointMap = new LinkedHashMap<Integer, Integer>();

	private DailyMissionCheckerTask checkerTask;

	private Map<Integer, Integer> reflashUsePointMap = new LinkedHashMap<Integer, Integer>();
	private int maxReflashUsePointKey = 0;
	private int maxReflashUsePoint;

	/**
	 * 获取一个任务模版
	 * 
	 * @param templateId
	 * @return
	 */
	public KMissionTemplate getMissionTemplate(int templateId) {
		return this.allMissionTemplates.get(templateId);
	}

	public boolean isMissionTemplateExist(int templateId) {
		return this.allMissionTemplates.containsKey(templateId);
	}

	public void init(String excelPath) throws Exception {
		KGameExcelFile xlsFile = null;
		try {
			xlsFile = new KGameExcelFile(excelPath);
		} catch (BiffException e) {
			throw new KGameServerException("读取日常任务excel表头发生错误！", e);
		} catch (IOException e) {
			throw new KGameServerException("读取日常任务excel表头发生错误！", e);
		}

		if (xlsFile != null) {
			// 初始化剧本数据
			// 初始化任务基本数据
			int missionDataRowIndex = 4;
			KGameExcelTable missionDataTable = xlsFile.getTable("日常任务模版配置", missionDataRowIndex);
			KGameExcelRow[] allMissionDataRows = missionDataTable.getAllDataRows();
			for (int i = 0; i < allMissionDataRows.length; i++) {
				KMissionTemplate missionTemplate = new KMissionTemplate();
				missionTemplate.initDailyKMissionTemplateData(allMissionDataRows[i], missionDataRowIndex + i + 1);

				if (allMissionTemplates.containsKey(missionTemplate.missionTemplateId)) {
					throw new KGameServerException("加载日常任务的《日常任务模版配置》错误：设置了重复的任务，值=" + missionTemplate.missionTemplateId + "，Row=" + allMissionDataRows[i].getIndexInFile());
				}

				allMissionTemplates.put(missionTemplate.missionTemplateId, missionTemplate);
				int missionLevelKey = (missionTemplate.missionTriggerCondition.roleLevelLimit);

				if (!missionTemplateMapByRoleLevel.containsKey(missionLevelKey)) {
					missionTemplateMapByRoleLevel.put(missionLevelKey, new ArrayList<KMissionTemplate>());
				}
				missionTemplateMapByRoleLevel.get(missionLevelKey).add(missionTemplate);
			}

			// 初始化送信任务类型NPC对话数据
			int missionDialogDataRowIndex = 2;
			KGameExcelTable missionDialogTable = xlsFile.getTable("送信任务类型NPC对话数据", missionDialogDataRowIndex);
			KGameExcelRow[] allMissionDialogRows = missionDialogTable.getAllDataRows();
			for (int i = 0; i < allMissionDialogRows.length; i++) {
				int missionTemplateId = allMissionDialogRows[i].getInt("missionTemplateId");

				if (allMissionTemplates.containsKey(missionTemplateId)) {
					KMissionTemplate missionTemplate = allMissionTemplates.get(missionTemplateId);
					if (missionTemplate.isInitDialogData) {
						throw new KGameServerException("加载日常任务《送信任务类型NPC对话数据》错误：设置重复的任务ID，值=" + missionTemplate.missionTemplateId + "，Row=" + allMissionDialogRows[i].getIndexInFile());
					}
					missionTemplate.isInitDialogData = true;
					missionTemplate.initDailyMissionTemplateDialog(allMissionDialogRows[i], missionDialogDataRowIndex + i + 1);
				}
			}

			// 任务星级系数表
			int starDataRowIndex = 2;
			KGameExcelTable starDataTable = xlsFile.getTable("任务星级系数表", starDataRowIndex);
			KGameExcelRow[] allStarDataRows = starDataTable.getAllDataRows();
			for (int i = 0; i < allStarDataRows.length; i++) {
				DailyMissionQualityType qualityType = new DailyMissionQualityType();
				qualityType.star = allStarDataRows[i].getInt("missionStar");
				qualityType.weight = allStarDataRows[i].getInt("weight");
				qualityType.priceRate = allStarDataRows[i].getInt("ratio");
				qualityType.jifen = allStarDataRows[i].getInt("points");
				String colorStr = allStarDataRows[i].getData("color");
				qualityType.color = KColorManager.getColor(colorStr);
				if (qualityType.color == null) {
					throw new KGameServerException("加载日常任务《任务星级系数表》错误：不存在的颜色值，值=" + colorStr + "，Row=" + allStarDataRows[i].getIndexInFile());
				}
				this.qualityTypeMap.put(qualityType.star, qualityType);
				this.mission_star_total_weight += qualityType.weight;
				if (i == 0) {
					default_mission_quality_type = qualityType;
				}
				if (qualityType.star == 3) {
					default_use_point_mission_quality_type = qualityType;
				}
				if (qualityType.star >= 3) {
					guideQualityList.add(qualityType);
				}
			}

			if (default_use_point_mission_quality_type == null) {
				throw new KGameServerException("加载日常任务《任务星级系数表》错误：必须存在3星的任务品质！！");
			}

			// 标准奖励系数
			int priceDataRowIndex = 2;
			KGameExcelTable priceDataTable = xlsFile.getTable("标准奖励系数", priceDataRowIndex);
			KGameExcelRow[] allPriceDataRows = priceDataTable.getAllDataRows();
			for (int i = 0; i < allPriceDataRows.length; i++) {
				DailyMissionPriceData data = new DailyMissionPriceData();
				data.roleLv = allPriceDataRows[i].getInt("roleLevel");
				data.expBaseValue = allPriceDataRows[i].getInt("missionExpValue");
				data.copperBaseValue = allPriceDataRows[i].getInt("missionCopperValue");
				data.potentialBaseValue = allPriceDataRows[i].getInt("missionPotentialValue");
				this.priceDataMap.put(data.roleLv, data);
				if (i == 0) {
					default_mission_price_data = data;
				}
			}

			// 宝箱奖励配置
			int priceBoxRowIndex = 2;
			KGameExcelTable priceBoxTable = xlsFile.getTable("宝箱奖励配置", priceBoxRowIndex);
			KGameExcelRow[] allPriceBoxDataRows = priceBoxTable.getAllDataRows();
			for (int i = 0; i < allPriceBoxDataRows.length; i++) {
				int minLv = allPriceBoxDataRows[i].getInt("minLv");
				int maxLv = allPriceBoxDataRows[i].getInt("maxLv");
				int id = allPriceBoxDataRows[i].getInt("Chests_ID");
				int score = allPriceBoxDataRows[i].getInt("score");
				int exp = allPriceBoxDataRows[i].getInt("expValue");
				int copper = allPriceBoxDataRows[i].getInt("copperValue");
				int potential = allPriceBoxDataRows[i].getInt("potentialValue");
				byte boxIndex = (byte) (i % 4 + 1);
				String itemInfo = allPriceBoxDataRows[i].getData("itemInfo");
				DailyMissionPriceBox box = new DailyMissionPriceBox(id, score, boxIndex, minLv, maxLv, exp, copper, potential, itemInfo, allPriceBoxDataRows[i].getIndexInFile());
				// if (!this.priceBoxList.containsKey(maxLv)) {
				// this.priceBoxList.put(maxLv,
				// new ArrayList<DailyMissionPriceBox>());
				// }
				int index = i / 4;
				if (i % 4 == 0) {
					this.priceBoxList.add(new ArrayList<DailyMissionPriceBox>());
				}
				this.priceBoxList.get(index).add(box);
				this.priceBoxListById.put(id, box);
				if (i == 0) {
					default_price_box = box;
				}
			}
			default_price_box_list = priceBoxList.get(0);
			max_lv_price_box_list = priceBoxList.get((priceBoxList.size() - 1));
			if (max_lv_price_box_list == null) {
				throw new KGameServerException("加载日常任务《宝箱奖励配置》错误：没有配置最大等级：" + KRoleModuleConfig.getRoleMaxLv() + "级的宝箱奖励");
			}

			// 购买任务消耗钻石
			int buyMissionRowIndex = 2;
			KGameExcelTable buyMissionTable = xlsFile.getTable("购买任务消耗钻石", priceBoxRowIndex);
			KGameExcelRow[] allByMissionRows = buyMissionTable.getAllDataRows();
			for (int i = 0; i < allByMissionRows.length; i++) {
				int count = allByMissionRows[i].getInt("buyCount");
				int point = allByMissionRows[i].getInt("usePoint");
				this.buyMissionUsePointMap.put(count, point);
			}

			// 系统参数配置
			int configRowIndex = 5;
			KGameExcelTable configTable = xlsFile.getTable("系统参数", configRowIndex);
			KGameExcelRow[] allConfigDataRows = configTable.getAllDataRows();
			for (int i = 0; i < allConfigDataRows.length; i++) {
				free_complete_mission_count = allConfigDataRows[i].getInt("freeCount");
				free_reflash_count = allConfigDataRows[i].getInt("reflashCount");
				auto_complete_mission_use_point = allConfigDataRows[i].getInt("autoPoint");
				reflash_use_point = allConfigDataRows[i].getInt("reflashPoint");
				target_price_box_star = allConfigDataRows[i].getInt("boxJifen");
				add_complete_mission_count = allConfigDataRows[i].getInt("addComlpeteCount");
			}

			// 刷新任务消耗钻石
			int reflashMissionRowIndex = 2;
			KGameExcelTable reflashMissionTable = xlsFile.getTable("刷新任务消耗钻石", reflashMissionRowIndex);
			KGameExcelRow[] allReflashMissionRows = reflashMissionTable.getAllDataRows();
			for (int i = 0; i < allReflashMissionRows.length; i++) {
				int count = allReflashMissionRows[i].getInt("reflashCount");
				int point = allReflashMissionRows[i].getInt("usePoint");
				this.reflashUsePointMap.put(count, point);
				if (count > this.maxReflashUsePointKey) {
					this.maxReflashUsePointKey = count;
					this.maxReflashUsePoint = point;
				}
			}
		}

		checkerTask = new DailyMissionCheckerTask();
		checkerTask.start();
	}

	/**
	 * 随机计算角色等级生成日常任务模版
	 * 
	 * @param role
	 * @return
	 */
	public KMissionTemplate caculateNewMissionTemplate(KRole role) {
		int missionLevel = role.getLevel();
		/** (role.getLevel() / mission_lv_mod) * mission_lv_mod; */
		if (missionLevel == 0) {
			missionLevel = 1;
		}
		KMissionTemplate template = null;
		if (UtilTool.randomNextBoolean()) {
			if (this.missionTemplateMapByRoleLevel.containsKey(missionLevel)) {
				List<KMissionTemplate> list = this.missionTemplateMapByRoleLevel.get(missionLevel);
				if (list != null && list.size() > 0) {
					KMissionTemplate temp = list.get(UtilTool.random(list.size()));
					if (temp.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_GAME_LEVEL) {
						GameLevelTask task = temp.missionCompleteCondition.getGameLevelTask();
						if (task != null) {
							KGameLevelSet levelSet = KGameLevelModuleExtension.getGameLevelSet(role.getId());
							int targetLevelId = task.levelId;
							if (targetLevelId != GameLevelTask.ANY_TYPE_LEVEL && targetLevelId <= levelSet.maxCompleteNormalLevelId) {
								template = temp;
							}
						}
					} else if (temp.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_FUNCTION) {
						UseFunctionTask task = temp.missionCompleteCondition.getUseFunctionTask();
						if (KGuideManager.checkFunctionIsOpen(role, task.functionId)) {
							if (task.functionId == KUseFunctionTypeEnum.军团捐献.functionId) {
								if (KSupportFactory.getGangSupport().getGangIdByRoleId(role.getId()) > 0) {
									template = temp;
								}
							} else if (task.functionId == KUseFunctionTypeEnum.添加好友.functionId) {
								if (!KSupportFactory.getRelationShipModuleSupport().isRelationShipFull(role.getId(), KRelationShipTypeEnum.好友)) {
									template = temp;
								}
							} else {
								template = temp;
							}
						}
					} else {
						template = temp;
					}
				}
			}
		}
		if (template == null) {
			List<KMissionTemplate> list = this.missionTemplateMapByRoleLevel.get(default_mission_lv);
			int index = 0;
			while (template == null && !list.isEmpty() && index < 5) {
				KMissionTemplate temp = list.get(UtilTool.random(list.size()));
				if (temp.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_GAME_LEVEL) {
					GameLevelTask task = temp.missionCompleteCondition.getGameLevelTask();
					if (task != null) {
						KGameLevelSet levelSet = KGameLevelModuleExtension.getGameLevelSet(role.getId());
						int targetLevelId = task.levelId;
						if (targetLevelId != GameLevelTask.ANY_TYPE_LEVEL && targetLevelId <= levelSet.maxCompleteNormalLevelId) {
							template = temp;
						}
					}
				} else if (temp.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_FUNCTION) {
					UseFunctionTask task = temp.missionCompleteCondition.getUseFunctionTask();
					if (KGuideManager.checkFunctionIsOpen(role, task.functionId)) {
						if (task.functionId == KUseFunctionTypeEnum.军团捐献.functionId) {
							if (KSupportFactory.getGangSupport().getGangIdByRoleId(role.getId()) > 0) {
								template = temp;
							}
						} else if (task.functionId == KUseFunctionTypeEnum.添加好友.functionId) {
							if (!KSupportFactory.getRelationShipModuleSupport().isRelationShipFull(role.getId(), KRelationShipTypeEnum.好友)) {
								template = temp;
							}
						} else {
							template = temp;
						}
					}
				} else {
					template = temp;
				}
				index++;
			}
		}
		if (template == null) {
			if (this.missionTemplateMapByRoleLevel.containsKey(missionLevel)) {
				List<KMissionTemplate> list = this.missionTemplateMapByRoleLevel.get(missionLevel);
				if (list != null && list.size() > 0) {
					KMissionTemplate temp = list.get(UtilTool.random(list.size()));
					if (temp.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_GAME_LEVEL) {
						GameLevelTask task = temp.missionCompleteCondition.getGameLevelTask();
						if (task != null) {
							KGameLevelSet levelSet = KGameLevelModuleExtension.getGameLevelSet(role.getId());
							int targetLevelId = task.levelId;
							if (targetLevelId != GameLevelTask.ANY_TYPE_LEVEL && targetLevelId <= levelSet.maxCompleteNormalLevelId) {
								template = temp;
							}
						}
					} else if (temp.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_FUNCTION) {
						UseFunctionTask task = temp.missionCompleteCondition.getUseFunctionTask();
						if (KGuideManager.checkFunctionIsOpen(role, task.functionId)) {
							if (task.functionId == KUseFunctionTypeEnum.军团捐献.functionId) {
								if (KSupportFactory.getGangSupport().getGangIdByRoleId(role.getId()) > 0) {
									template = temp;
								}
							} else if (task.functionId == KUseFunctionTypeEnum.添加好友.functionId) {
								if (!KSupportFactory.getRelationShipModuleSupport().isRelationShipFull(role.getId(), KRelationShipTypeEnum.好友)) {
									template = temp;
								}
							} else {
								template = temp;
							}
						}
					} else {
						template = temp;
					}
				}
			}
		}

		return template;
	}

	/**
	 * 根据权重计算随机任务品质
	 * 
	 * @return
	 */
	public DailyMissionQualityType caculateDailyMissionQualityType() {
		int weight = UtilTool.random(0, this.mission_star_total_weight);
		int tempRate = 0;
		for (DailyMissionQualityType type : this.qualityTypeMap.values()) {
			if (tempRate < weight && weight <= (tempRate + type.weight)) {
				return type;
			} else {
				tempRate += type.weight;
			}
		}
		return default_mission_quality_type;
	}

	/**
	 * 角色提交任务，在提交时需检测任务是否完成，如果完成则进行相关完成任务处理
	 * 
	 * @param role
	 * @param missionTemplateId
	 * @return
	 */
	public boolean playerRoleSubmitDailyMission(KRole role, int missionTemplateId) {
		List<String> upperfailedStringList = new ArrayList<String>();
		// upperfailedStringList.add("提交任务失败！");
		upperfailedStringList.add(MissionTips.getTipsSubmitMissionFail());

		if (!this.allMissionTemplates.containsKey(missionTemplateId)) {
			// 发送操作失败结果提示
			processSendOperateMissionResultMsg(role, upperfailedStringList);
			return false;
		}
		KMissionTemplate missionTemplate = this.allMissionTemplates.get(missionTemplateId);

		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role.getId());
		if (missionSet == null) {
			// 发送操作失败结果提示
			processSendOperateMissionResultMsg(role, upperfailedStringList);
			return false;
		}
		missionSet.checkAndResetDailyMissionInfo(true);

		KMission mission = missionSet.getDailyMission(missionTemplateId);
		if (mission == null) {
			// 发送操作失败结果提示
			processSendOperateMissionResultMsg(role, upperfailedStringList);
			return false;
		}
		DailyMissionQualityType quality = mission.qualityType;
		// 检测任务状态是否为完成可提交状态KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT
		if (mission.getMissionStatus() == KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT) {
			// int nowCompleteCount = missionSet.getDailyMissionInfo()
			// .getTodayCompletedMissionCount();
			int buyCount = missionSet.getDailyMissionInfo().getBuyCount();

			int restFreeCount = missionSet.getDailyMissionInfo().getRestFreeCompletedMissionCount();

			if (restFreeCount <= 0) {
				VIPLevelData vipData = KSupportFactory.getVIPModuleSupport().getVIPLevelData(role.getId());
				int vip_can_buy_count = vipData.daytaskrmb.length;
				if (buyCount >= vip_can_buy_count) {
					KDialogService.sendUprisingDialog(role, MissionTips.getTipsCannotAcceptNewDailyMission());
					return false;
				} else {
					int restBuyCount = vipData.daytaskrmb.length - buyCount;
					int usePoint = vipData.daytaskrmb[buyCount];
					sendDailyMissionTipsMessage(role, KMissionModuleDialogProcesser.KEY_BUY_DAILY_MISSION,
							MissionTips.getTipsBuyDailyMissionUsePoint(usePoint, add_complete_mission_count, KSupportFactory.getVIPModuleSupport().getVipLv(role.getId()), restBuyCount), true, "");
					return false;

				}
			}
			BaseRewardData reward = new BaseRewardData(mission.dailyMissionReward.attList, mission.dailyMissionReward.moneyList, mission.dailyMissionReward.itemStructs,
					Collections.<Integer> emptyList(), Collections.<Integer> emptyList());

			// 在角色任务容器中处理这个完成任务
			UpdateDailyMissionStruct struct = missionSet.completeDailyMission(role, mission);
			// 如果处理成功，则进行任务奖励结算
			if (struct.isNeedNotify) {

				// 如果是收集道具任务，扣除道具
				if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_COLLECT_ITEMS) {
					CollectItemTask task = missionTemplate.missionCompleteCondition.getCollectItemTask();
					int decreseItemCount = task.collectCount;
					KItemTempAbs itemTemplate;
					if (task.isLimitJob) {
						itemTemplate = task.itemTemplateMap.get(role.getJob());
					} else {
						itemTemplate = task.itemTemplate;
					}
					KSupportFactory.getItemModuleSupport().removeItemFromBag(role.getId(), itemTemplate.itemCode, decreseItemCount);

				}
				// 处理任务奖励
				List<String> upperStringList = processDailyMissionReward(role, missionTemplate, quality, reward);

				// 发送操作成功结果提示
				processSendOperateMissionResultMsg(role, upperStringList);

				// 刷新新的日常任务
				// autoReflashNewDailyMission(role);
				completeOrDropMissionReflashNewDailyMission(role, struct);
				// 通知完成一次修行任务功能
				// KSupportFactory.getMissionSupport().notifyUseFunction(role,
				// KFunctionTypeEnum.日常任务);
				// KSupportFactory.getRewardModuleSupport().recordFun(
				// role.getId(), KFunTypeEnum.修行任务);

				if (struct != null) {
					processUpdateDailyMissionStructNpcDialog(role, struct);
				}
				// 角色行为统计
				KActionRecorder.recordAction(role.getId(), KActionType.ACTION_TYPE_DAILY_MISSION, 1);
				return true;
			}
		}

		// 发送操作失败结果提示
		processSendOperateMissionResultMsg(role, upperfailedStringList);

		return false;
	}

	/**
	 * 角色自动完成日常任务，需要消耗元宝
	 * 
	 * @param role
	 * @param missionTemplateId
	 * @return
	 */
	public boolean playerRoleAutoSubmitDailyMission(KRole role, int missionTemplateId, boolean isNeedCheck) {

		List<String> upperfailedStringList = new ArrayList<String>();
		// upperfailedStringList.add("提交任务失败！");
		upperfailedStringList.add(MissionTips.getTipsSubmitMissionFail());

		if (!this.allMissionTemplates.containsKey(missionTemplateId)) {
			// 发送操作失败结果提示
			processSendOperateMissionResultMsg(role, upperfailedStringList);
			return false;
		}
		KMissionTemplate missionTemplate = this.allMissionTemplates.get(missionTemplateId);

		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role.getId());
		if (missionSet == null) {
			// 发送操作失败结果提示
			processSendOperateMissionResultMsg(role, upperfailedStringList);
			return false;
		}
		missionSet.checkAndResetDailyMissionInfo(true);

		KMission mission = missionSet.getDailyMission(missionTemplateId);

		if (mission == null) {
			// 发送操作失败结果提示
			processSendOperateMissionResultMsg(role, upperfailedStringList);
			return false;
		}
		DailyMissionQualityType quality = mission.qualityType;

		// int nowCompleteCount = missionSet.getDailyMissionInfo()
		// .getTodayCompletedMissionCount();
		int buyCount = missionSet.getDailyMissionInfo().getBuyCount();
		int restFreeCount = missionSet.getDailyMissionInfo().getRestFreeCompletedMissionCount();

		if (restFreeCount <= 0) {
			VIPLevelData vipData = KSupportFactory.getVIPModuleSupport().getVIPLevelData(role.getId());
			int vip_can_buy_count = vipData.daytaskrmb.length;
			if (buyCount >= vip_can_buy_count) {
				KDialogService.sendUprisingDialog(role, MissionTips.getTipsCannotAcceptNewDailyMission());
				return false;
			} else {
				int restBuyCount = vipData.daytaskrmb.length - buyCount;
				int usePoint = vipData.daytaskrmb[buyCount];
				sendDailyMissionTipsMessage(role, KMissionModuleDialogProcesser.KEY_BUY_DAILY_MISSION,
						MissionTips.getTipsBuyDailyMissionUsePoint(usePoint, add_complete_mission_count, KSupportFactory.getVIPModuleSupport().getVipLv(role.getId()), restBuyCount), true, "");
				return false;

			}
		}

		// 检测角色元宝数量是否足够
		if (isNeedCheck) {
			sendDailyMissionTipsMessage(role, KMissionModuleDialogProcesser.KEY_AUTO_SUBMIT_DAILY_MISSION, MissionTips.getTipsAutoSubmitDailyMission(auto_complete_mission_use_point), true,
					missionTemplateId + "");
			return false;
		}

		BaseRewardData reward = new BaseRewardData(mission.dailyMissionReward.attList, mission.dailyMissionReward.moneyList, mission.dailyMissionReward.itemStructs, Collections.<Integer> emptyList(),
				Collections.<Integer> emptyList());

		// 在角色任务容器中处理这个完成任务
		UpdateDailyMissionStruct struct = missionSet.completeDailyMission(role, mission);
		// 如果处理成功，则进行任务奖励结算
		if (struct.isNeedNotify) {
			// 处理任务奖励
			List<String> upperStringList = processDailyMissionReward(role, missionTemplate, quality, reward);

			// 发送操作成功结果提示
			processSendOperateMissionResultMsg(role, upperStringList);

			// 刷新新的日常任务
			// autoReflashNewDailyMission(role);
			completeOrDropMissionReflashNewDailyMission(role, struct);
			// 通知完成一次修行任务功能
			// KSupportFactory.getMissionSupport().notifyUseFunction(role,
			// KFunctionTypeEnum.日常任务);
			// KSupportFactory.getRewardModuleSupport().recordFun(
			// role.getId(), KFunTypeEnum.修行任务);

			if (struct != null) {
				processUpdateDailyMissionStructNpcDialog(role, struct);
			}

			// 角色行为统计
			KActionRecorder.recordAction(role.getId(), KActionType.ACTION_TYPE_DAILY_MISSION, 1);
			return true;
		}
		return false;

	}

	/**
	 * 
	 * @param role
	 * @param missionTemplate
	 * @param operateType
	 * @param operateResult
	 * @param upperStringList
	 */
	private void processSendOperateMissionResultMsg(KRole role, List<String> upperStringList) {
		KDialogService.sendDataUprisingDialog(role, upperStringList);
	}

	/**
	 * 处理完成日常任务的数值奖励
	 * 
	 * @param role
	 * @param template
	 */
	private List<String> processDailyMissionReward(KRole role, KMissionTemplate template, DailyMissionQualityType quality, BaseRewardData data) {
		data.sendReward(role, PresentPointTypeEnum.日常任务奖励);

		List<String> resultList = new ArrayList<String>();
		resultList.add(MissionTips.getTipsDailyMissionCompleteTips2(getDailyMissionName(template.missionName, quality)));
		resultList.addAll(data.dataUprisingTips);
		return resultList;

	}

	private void addDailyMissionNPCDialog(KRole role, KMissionTemplate newAcceptableMissionTemplate) {

		Map<Integer, List<IMissionMenu>> menuListMap = new HashMap<Integer, List<IMissionMenu>>();
		KGameNormalMap currentMap = KMapModule.getGameMapManager().getGameMap(role.getRoleMapData().getCurrentMapId());

		int submitMissionNpcId = newAcceptableMissionTemplate.submitMissionNPCTemplate.templateId;
		if (currentMap == null || !currentMap.isNpcEntityInMap(submitMissionNpcId)) {
			return;
		}
		if (!menuListMap.containsKey(submitMissionNpcId)) {
			menuListMap.put(submitMissionNpcId, new ArrayList<IMissionMenu>());
		}
		IMissionMenuImpl menu = KMissionModuleSupportImpl.constructIMissionMenu(role, newAcceptableMissionTemplate,
				newAcceptableMissionTemplate.getMissionTypeNameText(newAcceptableMissionTemplate.missionType) + newAcceptableMissionTemplate.missionExtName,
				KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT.statusType, IMissionMenu.ACTION_AFTER_TALK_SUBMIT, newAcceptableMissionTemplate.getCompletedMissionDialog());

		menuListMap.get(submitMissionNpcId).add(menu);

		if (menuListMap.size() > 0) {
			for (Integer addMenuNpcId : menuListMap.keySet()) {
				List<IMissionMenu> missionMenuList = menuListMap.get(addMenuNpcId);
				if (missionMenuList != null && missionMenuList.size() > 0) {

					KMenuService.synNpcAddOrUpdateMenus(role, addMenuNpcId, missionMenuList);
				}
			}
		}
	}

	private void removeDailyMissionNPCDialog(KRole role, KMissionTemplate removeMissionTemplate) {
		KGameNormalMap currentMap = KMapModule.getGameMapManager().getGameMap(role.getRoleMapData().getCurrentMapId());

		if (removeMissionTemplate.submitMissionNPCTemplate != null) {
			int submitMissionNpcId = removeMissionTemplate.submitMissionNPCTemplate.templateId;
			if (currentMap == null || !currentMap.isNpcEntityInMap(submitMissionNpcId)) {
				return;
			}
			KMenuService.synNpcDeleteMenus(role, submitMissionNpcId, removeMissionTemplate.missionTemplateId);
		}
	}

	public void completeOrDropMissionReflashNewDailyMission(KRole role, UpdateDailyMissionStruct struct) {
		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role.getId());

		processSendUpdateMissionData(role, struct.deleteMissionIdList, struct.updateMissionList, struct.addMissionList, getTodayRestCompleteCount(role, missionSet),
				getRestFreeReflashCount(role, missionSet), missionSet.getDailyMissionInfo().getTotalStar());
	}

	public void processSendUpdateMissionData(KRole role, KMissionSet missionSet) {
		processSendUpdateMissionData(role, null, null, null, getTodayRestCompleteCount(role, missionSet), getRestFreeReflashCount(role, missionSet), missionSet.getDailyMissionInfo().getTotalStar());
	}

	private void processSendUpdateMissionData(KRole role, List<Integer> deleteList, List<KMission> updateList, List<KMission> addList, int todayRestCompleteCount, int restFreeReflashCount,
			int nowStarCount) {

		KGameMessage sendMsg = KGame.newLogicMessage(KMissionProtocol.SM_UPDATE_DAILY_MISSION);
		if (deleteList != null) {
			sendMsg.writeByte(deleteList.size());
			for (Integer deleteId : deleteList) {
				sendMsg.writeInt(deleteId);
			}
		} else {
			sendMsg.writeByte(0);
		}

		if (updateList != null) {
			sendMsg.writeByte(updateList.size());
			for (KMission mission : updateList) {
				sendMsg.writeInt(mission.getMissionTemplate().missionTemplateId);
				sendMsg.writeUtf8String(getMissionListTips(role, mission, false));
				List<DailyMissionOperateTypeEnum> operateList = getMissionOperateType(mission);
				sendMsg.writeByte(operateList.size());
				for (DailyMissionOperateTypeEnum operate : operateList) {
					sendMsg.writeUtf8String(operate.typeName);
					sendMsg.writeByte(operate.operateType);
					if (operate == DailyMissionOperateTypeEnum.OPERATE_TYPE_AUTO_SEARCH_ROAD) {
						KMissionModuleExtension.getManager().setSearchRoadDataInMessage(role, mission, sendMsg, false);
					}
				}
			}
		} else {
			sendMsg.writeByte(0);
		}

		if (addList != null) {
			sendMsg.writeByte(addList.size());
			for (KMission mission : addList) {
				sendMsg.writeInt(mission.getMissionTemplate().missionTemplateId);
				// sendMsg.writeUtf8String(mission.getMissionTemplate().missionName);
				sendMsg.writeUtf8String(getDailyMissionName(mission.getMissionTemplate().missionName, mission.qualityType));
				sendMsg.writeUtf8String(mission.getMissionTemplate().desc);
				sendMsg.writeUtf8String(getMissionListTips(role, mission, false));
				sendMsg.writeInt(mission.qualityType.star);
				List<DailyMissionOperateTypeEnum> operateList = getMissionOperateType(mission);
				sendMsg.writeByte(operateList.size());
				for (DailyMissionOperateTypeEnum operate : operateList) {
					sendMsg.writeUtf8String(operate.typeName);
					sendMsg.writeByte(operate.operateType);
					if (operate == DailyMissionOperateTypeEnum.OPERATE_TYPE_AUTO_SEARCH_ROAD) {
						KMissionModuleExtension.getManager().setSearchRoadDataInMessage(role, mission, sendMsg, false);
					}
				}

				mission.dailyMissionReward.packMsg(sendMsg);
			}
		} else {
			sendMsg.writeByte(0);
		}
		List<DailyMissionPriceBox> boxList = getDailyMissionPriceBoxList(role);
		sendMsg.writeInt(todayRestCompleteCount);
		sendMsg.writeInt(restFreeReflashCount);
		sendMsg.writeInt(getReflashMissionUsePoint(role));
		sendMsg.writeInt(boxList.get((boxList.size() - 1)).score);
		sendMsg.writeInt(nowStarCount);

		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role.getId());

		sendMsg.writeByte(boxList.size());
		for (DailyMissionPriceBox box : boxList) {
			sendMsg.writeBoolean(missionSet.isGetDailyMissionPriceBox(box.boxIndex));
			sendMsg.writeInt(box.id);
			sendMsg.writeInt(box.score);
			box.rewardData.packMsg(sendMsg);
		}

		role.sendMsg(sendMsg);
	}

	/**
	 * 处理任务列表中的某个已接任务提示字符串
	 * 
	 * @param mission
	 * @return
	 */
	public String getMissionListTips(KRole role, KMission mission, boolean isNeedTitle) {
		KMissionTemplate missionTemplate = mission.getMissionTemplate();
		MissionCompleteCondition condition = missionTemplate.missionCompleteCondition;
		// String mtips = missionTemplate.getHyperText("任务提示：", 12)
		// + missionTemplate.getHyperText(
		// missionTemplate.getMissionTips(), 13) + "\n";

		String tips = "";
		if (isNeedTitle) {
			tips = "日常任务" + HyperTextTool.extColor("【" + getDailyMissionName(missionTemplate.missionName, mission.qualityType) + "】", 1);
		}
		if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_DIALOG) {
			tips += HyperTextTool.extColor(missionTemplate.missionTips, 11);
		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_KILL_MONSTER) {
			// atips += missionTemplate.getHyperText("任务目标：", 12);
			tips += HyperTextTool.extColor(missionTemplate.missionTips, 11);
			for (KillMonsterTask task : condition.getKillMonsterTaskMap().values()) {
				KillMonsterRecord record = mission.getKillMonsterRecordMap().get(task.isAnyTypeMonster ? KillMonsterTask.ANY_TYPE_MONSTER_ID : task.monsterTemplate.id);
				String monsterName = (task.isAnyTypeMonster) ? (MissionTips.getTipsKillMonsterTypeMonsterName1(task.monsterLevel)) : (task.monsterTemplate.name);
				int targetCount = task.killCount;
				int nowCount = (record != null) ? (record.killCount) : 0;
				if (nowCount > targetCount) {
					nowCount = targetCount;
				}
				// tips += (getHyperText(monsterName,7) + getHyperText(" (" +
				// nowCount + "/"
				// + targetCount + ")", (nowCount < targetCount) ? 1 : 6));
				tips += HyperTextTool.extColor(" (" + nowCount + "/" + targetCount + ")", (nowCount < targetCount) ? 9 : 6);
			}
		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_COLLECT_ITEMS) {
			// atips += missionTemplate.getHyperText("任务目标：", 12);
			tips += HyperTextTool.extColor(missionTemplate.missionTips, 11);
			CollectItemTask task = condition.getCollectItemTask();
			String itemCode;
			if (task.isLimitJob) {
				itemCode = task.itemTemplateMap.get(role.getJob()).itemCode;
			} else {
				itemCode = task.itemTemplate.itemCode;
			}
			int targetCount = task.collectCount;
			long nowCount = KSupportFactory.getItemModuleSupport().checkItemCountInBag(mission.getRoleId(), itemCode);
			if (nowCount > targetCount) {
				nowCount = targetCount;
			}
			tips +=
			// missionTemplate.getHyperText(("【" + itemName + "】"
			// + targetCount + "个  "), 13)+
			HyperTextTool.extColor(" (" + nowCount + "/" + targetCount + ")", (nowCount < targetCount) ? 9 : 6);

		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_GAME_LEVEL) {
			// atips += missionTemplate.getHyperText("任务目标：", 12);
			CompletedGameLevelRecord record = mission.getCompletedGameLevelRecord();
			GameLevelTask task = condition.getGameLevelTask();
			int targetCount = task.completeCount;
			int nowCount = record.completeCount;
			if (nowCount > targetCount) {
				nowCount = targetCount;
			}

			// String info = "";
			// if (task.isLevelTypeAny()) {
			// info = MissionTips.getTipsCompleteLevelTypeInfo(targetCount);
			// } else {
			// String levelName = KGameLevelModuleExtension.getManager().
			// getKGameLevel(task.levelId).getLevelName();
			// info = MissionTips.getTipsCompleteLevelTypeInfo1(levelName,
			// targetCount);
			// }
			// tips += getHyperText(missionTemplate.missionTips, 11) + info
			// + getHyperText(" (" + nowCount + "/" + targetCount + ")",
			// (nowCount < targetCount) ? 1 : 6);
			tips += HyperTextTool.extColor(missionTemplate.missionTips, 11) + HyperTextTool.extColor(" (" + nowCount + "/" + targetCount + ")", (nowCount < targetCount) ? 9 : 6);
		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_FUNCTION) {
			// atips += missionTemplate.getHyperText("任务目标：", 12);
			UseFunctionRecord record = mission.getUseFunctionRecord();
			UseFunctionTask task = condition.getUseFunctionTask();
			int targetCount = task.useCount;
			int nowCount = record.completeCount;
			if (nowCount > targetCount) {
				nowCount = targetCount;
			}
			tips += HyperTextTool.extColor(missionTemplate.missionTips, 11) + HyperTextTool.extColor(" (" + nowCount + "/" + targetCount + ")", (nowCount < targetCount) ? 9 : 6);

		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_ITEM) {
			// atips += missionTemplate.getHyperText("任务目标：", 12);
			UseItemTask task = condition.getUseItemTask();
			// tips += missionTemplate.getHyperText(("使用或装备道具【"
			// + task.itemTemplate.ItemName + "】 1次 "), 13);
			tips += HyperTextTool.extColor(missionTemplate.missionTips, 11);
		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_QUESTION) {
			// atips += getHyperText("任务目标：", 12);
			AnswerQuestionRecord record = mission.getAnswerQuestionRecord();
			AnswerQuestionTask task = condition.getAnswerQuestionTask();
			int targetCount = task.getTotalQuestionCount();
			int nowCount = record.completeCount;
			if (nowCount > targetCount) {
				nowCount = targetCount;
			}
			tips += HyperTextTool.extColor(missionTemplate.missionTips, 11) + HyperTextTool.extColor(" (" + nowCount + "/" + targetCount + ")", (nowCount < targetCount) ? 9 : 6);

		}

		return tips;
	}

	public List<DailyMissionOperateTypeEnum> getMissionOperateType(KMission mission) {
		List<DailyMissionOperateTypeEnum> list = new ArrayList<DailyMissionOperateTypeEnum>();
		if (mission.getMissionStatus() == KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH) {
			list.add(DailyMissionOperateTypeEnum.OPERATE_TYPE_AUTO_SUBMIT);
			list.add(DailyMissionOperateTypeEnum.OPERATE_TYPE_AUTO_SEARCH_ROAD);
		} else if (mission.getMissionStatus() == KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT) {
			list.add(DailyMissionOperateTypeEnum.OPERATE_TYPE_SUBMIT);
		}
		return list;
	}

	public int getTodayRestCompleteCount(KRole role, KMissionSet missionSet) {

		return missionSet.getDailyMissionInfo().getRestFreeCompletedMissionCount();
	}

	public int getRestFreeReflashCount(KRole role, KMissionSet missionSet) {
		if (free_reflash_count - missionSet.getDailyMissionInfo().getTodayManualReflashCount() <= 0) {
			return 0;
		}
		return free_reflash_count - missionSet.getDailyMissionInfo().getTodayManualReflashCount();
	}

	public BaseRewardData initDailyMissionReward(int roleLv, DailyMissionQualityType qualityType) {
		DailyMissionPriceData data = getDailyMissionPriceData(roleLv);

		int exp = data.getExpBaseValue(qualityType.priceRate);
		List<AttValueStruct> attList = new ArrayList<AttValueStruct>();
		if (exp > 0) {
			attList.add(new AttValueStruct(KGameAttrType.EXPERIENCE, exp, 0));
		}
		int copper = data.getCopperBaseValue(qualityType.priceRate);
		List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();
		if (copper > 0) {
			KCurrencyCountStruct struct = new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD, copper);
			moneyList.add(struct);
		}
		int potential = data.getPotentialBaseValue(qualityType.priceRate);
		if (potential > 0) {
			KCurrencyCountStruct struct = new KCurrencyCountStruct(KCurrencyTypeEnum.POTENTIAL, potential);
			moneyList.add(struct);
		}
		List<ItemCountStruct> itemList = Collections.emptyList();

		return new BaseRewardData(attList, moneyList, itemList, Collections.<Integer> emptyList(), Collections.<Integer> emptyList());
	}

	public DailyMissionPriceData getDailyMissionPriceData(int roleLv) {
		if (this.priceDataMap.containsKey(roleLv)) {
			return this.priceDataMap.get(roleLv);
		} else {
			return default_mission_price_data;
		}
	}

	/**
	 * 处理角色刷新日常任务
	 * 
	 * @param role
	 * @param isNeedCheck
	 */
	public void manualReflashDailyMission(KRole role, boolean isNeedCheck, boolean isUsePoint) {
		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role.getId());

		missionSet.checkAndResetDailyMissionInfo(true);

		// 在角色任务容器中处理这个完成任务
		if (isNeedCheck) {
			if (missionSet.getDailyMissionInfo().getRestFreeCompletedMissionCount() <= 0) {
				KDialogService.sendUprisingDialog(role, MissionTips.getTipsCannotManualReflashDailyMission());
				return;
			}

			int todayManualReflashCount = missionSet.getDailyMissionInfo().getTodayManualReflashCount();
			int usePoint = getReflashMissionUsePoint(role);
			if (usePoint > 0) {

				// sendDailyMissionTipsMessage(
				// role,
				// KMissionModuleDialogProcesser.KEY_MANUAL_REFLASH_DAILY_MISSION,
				// MissionTips
				// .getTipsManualReflashDailyMissionMaxCount(reflash_use_point),
				// true, "");
				long result = KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), KCurrencyTypeEnum.DIAMOND, usePoint, UsePointFunctionTypeEnum.日常任务刷新, true);
				if (result == -1) {
					KDialogService.showChargeDialog(role.getId(), MissionTips.getTipsManualReflashDailyMissionNotEnoughIgot(usePoint));
					return;
				} else {
					isUsePoint = true;
				}
			}
		}
		UpdateDailyMissionStruct struct = missionSet.reflashDailyMission(role, isUsePoint);
		// StringBuffer byffer = new StringBuffer();
		// byffer.append("### Daily deleteID:");
		// for (int i = 0; i < struct.deleteMissionIdList.size(); i++) {
		// byffer.append(struct.deleteMissionIdList.get(i) + ",");
		// }
		// byffer.append(" --- addID:");
		// for (int i = 0; i < struct.addMissionList.size(); i++) {
		// byffer.append(struct.addMissionList.get(i).getMissionTemplateId()
		// + ",");
		// }
		// System.err.println(byffer.toString());

		// commonAttr.reflashDailyMission();
		missionSet.recordManualReflashDailyMission();

		processSendUpdateMissionData(role, struct.deleteMissionIdList, struct.updateMissionList, struct.addMissionList, getTodayRestCompleteCount(role, missionSet),
				getRestFreeReflashCount(role, missionSet), missionSet.getDailyMissionInfo().getTotalStar());

		if (!struct.deleteMissionIdList.isEmpty()) {
			for (Integer deleteMid : struct.deleteMissionIdList) {
				KMissionTemplate mTemplate = getMissionTemplate(deleteMid);
				if (mTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_DIALOG) {
					removeDailyMissionNPCDialog(role, mTemplate);
				}
			}
		}

		if (!struct.addMissionList.isEmpty()) {
			for (KMission addMission : struct.addMissionList) {
				// 如果是送信任务，处理NPC对话菜单
				if (addMission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_DIALOG) {
					addDailyMissionNPCDialog(role, addMission.getMissionTemplate());
				} else if (addMission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_QUESTION) {
					// processQuestionMissionDialog(role,
					// missionTemplateId);
				}
			}
		}
	}

	/**
	 * 发送日常任务数据
	 * 
	 * @param role
	 */
	public void sendDailyMissionData(KRole role) {

		if (!KGuideManager.checkFunctionIsOpen(role, KFunctionTypeEnum.日常任务.functionId)) {
			return;
		}

		UpdateDailyMissionStruct struct = null;

		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role.getId());
		if (missionSet.getDailyMissionInfo() == null) {
			missionSet.initDailyMissionInfo(0, free_complete_mission_count, 0, 0, 0, true, System.currentTimeMillis(), 0, 0);
			struct = missionSet.reflashDailyMission(role, false);
		} else {
			missionSet.checkAndResetDailyMissionInfo(true);
			if (missionSet.getDailyMissionMap().isEmpty() || missionSet.getDailyMissionMap().size() < missionSet.MAX_ACCEPTABLE_DAILY_MISSION_COUNT) {
				struct = missionSet.reflashDailyMission(role, false);
			}
		}

		KGameMessage sendMsg = KGame.newLogicMessage(KMissionProtocol.SM_SEND_DAILY_MISSION);
		sendMsg.writeByte(missionSet.getDailyMissionMap().size());
		for (KMission mission : missionSet.getDailyMissionMap().values()) {
			sendMsg.writeInt(mission.getMissionTemplate().missionTemplateId);
			sendMsg.writeUtf8String(getDailyMissionName(mission.getMissionTemplate().missionName, mission.qualityType));
			sendMsg.writeUtf8String(mission.getMissionTemplate().desc);
			sendMsg.writeUtf8String(getMissionListTips(role, mission, false));
			sendMsg.writeInt(mission.qualityType.star);
			List<DailyMissionOperateTypeEnum> operateList = getMissionOperateType(mission);
			sendMsg.writeByte(operateList.size());
			for (DailyMissionOperateTypeEnum operate : operateList) {
				sendMsg.writeUtf8String(operate.typeName);
				sendMsg.writeByte(operate.operateType);
				if (operate == DailyMissionOperateTypeEnum.OPERATE_TYPE_AUTO_SEARCH_ROAD) {
					KMissionModuleExtension.getManager().setSearchRoadDataInMessage(role, mission, sendMsg, false);
				}
			}
			// 任务奖励
			mission.dailyMissionReward.packMsg(sendMsg);
		}
		List<DailyMissionPriceBox> boxList = getDailyMissionPriceBoxList(role);

		sendMsg.writeInt(getTodayRestCompleteCount(role, missionSet));
		sendMsg.writeInt(getRestFreeReflashCount(role, missionSet));
		sendMsg.writeInt(getReflashMissionUsePoint(role));
		sendMsg.writeInt(boxList.get((boxList.size() - 1)).score);
		int nowStar = missionSet.getDailyMissionInfo().getTotalStar();
		sendMsg.writeInt(nowStar);

		sendMsg.writeByte(boxList.size());
		for (DailyMissionPriceBox box : boxList) {
			sendMsg.writeBoolean(missionSet.isGetDailyMissionPriceBox(box.boxIndex));
			sendMsg.writeInt(box.id);
			sendMsg.writeInt(box.score);
			box.rewardData.packMsg(sendMsg);
		}

		role.sendMsg(sendMsg);

		if (struct != null) {
			processUpdateDailyMissionStructNpcDialog(role, struct);
		}

	}

	private void processUpdateDailyMissionStructNpcDialog(KRole role, UpdateDailyMissionStruct struct) {
		if (!struct.deleteMissionIdList.isEmpty()) {
			for (Integer deleteMid : struct.deleteMissionIdList) {
				KMissionTemplate mTemplate = getMissionTemplate(deleteMid);
				if (mTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_DIALOG) {
					removeDailyMissionNPCDialog(role, mTemplate);
				}
			}
		}

		if (!struct.addMissionList.isEmpty()) {
			for (KMission addMission : struct.addMissionList) {
				// 如果是送信任务，处理NPC对话菜单
				if (addMission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_DIALOG) {
					addDailyMissionNPCDialog(role, addMission.getMissionTemplate());
				} else if (addMission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_QUESTION) {
					// processQuestionMissionDialog(role,
					// missionTemplateId);
				}
			}
		}
	}

	private String getDailyMissionName(String missionName, DailyMissionQualityType quality) {
		return HyperTextTool.extColor(missionName, quality.color);
	}

	private List<DailyMissionPriceBox> getDailyMissionPriceBoxList(KRole role) {
		if (role.getLevel() >= KRoleModuleConfig.getRoleMaxLv()) {
			return max_lv_price_box_list;
		}
		List<DailyMissionPriceBox> boxList = null;
		int roleLv = role.getLevel();
		for (List<DailyMissionPriceBox> tempBoxList : this.priceBoxList) {
			DailyMissionPriceBox box = tempBoxList.get(0);
			if (roleLv >= box.minLv && roleLv <= box.maxLv) {
				boxList = tempBoxList;
				break;
			}
		}

		if (boxList == null) {
			boxList = default_price_box_list;
		}
		return boxList;
	}

	public void processGetPriceBox(KRole role, int boxId) {
		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role.getId());
		if (missionSet.getDailyMissionInfo() == null) {
			KDialogService.sendDataUprisingDialog(role, GlobalTips.getTipsServerBusy());
			return;
		}

		DailyMissionPriceBox box = this.priceBoxListById.get(boxId);
		if (box == null) {
			KDialogService.sendDataUprisingDialog(role, GlobalTips.getTipsServerBusy());
			return;
		}

		if (missionSet.isGetDailyMissionPriceBox(box.boxIndex)) {
			KDialogService.sendDataUprisingDialog(role, MissionTips.getTipsAlreadyPriceBox());
			return;
		}

		if (missionSet.getDailyMissionInfo().getTotalStar() < box.score) {
			KDialogService.sendDataUprisingDialog(role, MissionTips.getTipsGetPriceBoxNotEnoughScore(box.score));
			return;
		}

		if (box.rewardData.sendReward(role, PresentPointTypeEnum.日常任务宝箱奖励)) {

			missionSet.recordGetDailyMissionPriceBox(box.boxIndex);

			KDialogService.sendDataUprisingDialog(role, box.rewardData.dataUprisingTips);

			completeOrDropMissionReflashNewDailyMission(role, new UpdateDailyMissionStruct());

			KSupportFactory.getExcitingRewardSupport().notifyExpTaskLvRewardCollected(role.getId(), box.boxIndex);
		} else {
			KDialogService.sendDataUprisingDialog(role, LevelTips.getTipsBagCapacityNotEnough());
			return;
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
	public void sendDailyMissionTipsMessage(KRole role, short confirmKey, String tips, boolean isHasConfirmButton, String script) {

		List<KDialogButton> list = new ArrayList<KDialogButton>();
		if (isHasConfirmButton) {
			list.add(KDialogButton.CANCEL_BUTTON);
			list.add(new KDialogButton(confirmKey, script, KDialogButton.CONFIRM_DISPLAY_TEXT));
		} else {
			list.add(KDialogButton.CONFIRM_BUTTON);
		}

		KDialogService.sendFunDialog(role, GlobalTips.getTipsDefaultTitle(), tips, list, true, (byte) -1);
	}

	public void dailyMissionConditionReached(KRole role, KMission mission) {

		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role.getId());

		List<KMission> missionList = new ArrayList<KMission>();
		missionList.add(mission);
		processSendUpdateMissionData(role, null, missionList, null, getTodayRestCompleteCount(role, missionSet), getRestFreeReflashCount(role, missionSet), missionSet.getDailyMissionInfo()
				.getTotalStar());
	}

	/**
	 * 日常任务品质数据
	 * 
	 * @author zhaizl
	 * 
	 */
	public static class DailyMissionQualityType {
		public final static int RATIO_MOD_VALUE = 100;

		/**
		 * 星数
		 */
		public int star;

		/**
		 * 任务品质出现权重
		 */
		public int weight;

		/**
		 * 奖励系数
		 */
		public int priceRate;

		/**
		 * 完成任务品质积分
		 */
		public int jifen;

		/**
		 * 品质颜色值
		 */
		public KColor color;
	}

	/**
	 * 日常任务奖励标准值
	 * 
	 * @author zhaizl
	 * 
	 */
	public static class DailyMissionPriceData {
		/**
		 * 角色等级
		 */
		public int roleLv;
		/**
		 * 经验标准值
		 */
		public int expBaseValue;
		/**
		 * 金币标准值
		 */
		public int copperBaseValue;
		/**
		 * 潜能标准值
		 */
		public int potentialBaseValue;

		public int getExpBaseValue(int ratio) {
			return (expBaseValue * ratio) / DailyMissionQualityType.RATIO_MOD_VALUE;
		}

		public int getCopperBaseValue(int ratio) {
			return (copperBaseValue * ratio) / DailyMissionQualityType.RATIO_MOD_VALUE;
		}

		public int getPotentialBaseValue(int ratio) {
			return (potentialBaseValue * ratio) / DailyMissionQualityType.RATIO_MOD_VALUE;
		}

	}

	/**
	 * 日常任务宝箱奖励数据
	 * 
	 * @author zhaizl
	 * 
	 */
	public static class DailyMissionPriceBox {
		public int minLv;
		public int maxLv;
		public int score;
		public int id;
		public byte boxIndex;

		public BaseRewardData rewardData;

		public DailyMissionPriceBox(int id, int score, byte boxIndex, int minLv, int maxLv, int exp, int copper, int potential, String itemInfo, int index) throws Exception {
			this.id = id;
			this.score = score;
			this.minLv = minLv;
			this.maxLv = maxLv;
			this.boxIndex = boxIndex;

			List<AttValueStruct> attList = new ArrayList<AttValueStruct>();
			if (exp > 0) {
				attList.add(new AttValueStruct(KGameAttrType.EXPERIENCE, exp, 0));
			}

			List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();
			if (copper > 0) {
				KCurrencyCountStruct struct = new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD, copper);
				moneyList.add(struct);
			}
			if (potential > 0) {
				KCurrencyCountStruct struct = new KCurrencyCountStruct(KCurrencyTypeEnum.POTENTIAL, potential);
				moneyList.add(struct);
			}
			List<ItemCountStruct> itemList = new ArrayList<ItemCountStruct>();
			if (!itemInfo.equals("0")) {
				itemList = initItemReward(itemInfo, index);
			}

			rewardData = new BaseRewardData(attList, moneyList, itemList, Collections.<Integer> emptyList(), Collections.<Integer> emptyList());
		}

		private List<ItemCountStruct> initItemReward(String dropData, int index) throws KGameServerException {
			List<ItemCountStruct> list = new ArrayList<ItemCountStruct>();
			if (dropData != null) {
				String[] itemInfoStr = dropData.split(",");
				if (itemInfoStr != null && itemInfoStr.length > 0) {
					for (int i = 0; i < itemInfoStr.length; i++) {
						String[] itemData = itemInfoStr[i].split("\\*");
						if (itemData != null && itemData.length == 2) {
							NormalItemRewardTemplate itemTemplate = null;
							String itemCode = itemData[0];
							if (KSupportFactory.getItemModuleSupport().getItemTemplate(itemCode) == null) {
								throw new KGameServerException("初始化日常任务的宝箱奖励的道具错误，找不到道具类型：" + itemCode + "，excel行数：" + index);
							}

							int count = Integer.parseInt(itemData[1]);
							itemTemplate = new NormalItemRewardTemplate(itemCode, count);

							list.add(new ItemCountStruct(KSupportFactory.getItemModuleSupport().getItemTemplate(itemCode), count));
						} else {
							throw new KGameServerException("初始化日常任务的宝箱奖励的道具格式错误，excel行数：" + index);
						}
					}
				}
			}
			return list;
		}
	}

	public int getReflashMissionUsePoint(KRole role) {
		int point = -1;
		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role.getId());
		int todayManualReflashCount = missionSet.getDailyMissionInfo().getTodayManualReflashCount() + 1;
		if (todayManualReflashCount >= this.maxReflashUsePointKey) {
			return this.maxReflashUsePoint;
		}
		for (Integer reflashCount : reflashUsePointMap.keySet()) {
			if (todayManualReflashCount <= reflashCount) {
				point = reflashUsePointMap.get(reflashCount);
				break;
			} else {
				continue;
			}
		}
		if (point < 0) {
			point = maxReflashUsePoint;
		}

		return point;
	}
}
