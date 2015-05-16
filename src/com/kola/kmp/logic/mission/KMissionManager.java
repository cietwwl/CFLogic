package com.kola.kmp.logic.mission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jxl.read.biff.BiffException;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.StringUtil;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.flowdata.FlowDataModuleFactory;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kmp.logic.item.listener.KItemEventListener;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.level.KGameLevelSet;
import com.kola.kmp.logic.level.KLevelTemplate;
import com.kola.kmp.logic.level.gamestory.AnimationManager;
import com.kola.kmp.logic.map.AutoSearchRoadTrack;
import com.kola.kmp.logic.map.AutoSearchRoadTrack.RoadPath;
import com.kola.kmp.logic.map.KGameNormalMap;
import com.kola.kmp.logic.map.KMapModule;
import com.kola.kmp.logic.mission.IMissionMenuImpl.IMissionConversationImpl;
import com.kola.kmp.logic.mission.KMission.AnswerQuestionRecord;
import com.kola.kmp.logic.mission.KMission.CompletedGameLevelRecord;
import com.kola.kmp.logic.mission.KMission.KillMonsterRecord;
import com.kola.kmp.logic.mission.KMission.UseFunctionRecord;
import com.kola.kmp.logic.mission.MissionCompleteCondition.AnswerQuestionTask;
import com.kola.kmp.logic.mission.MissionCompleteCondition.CollectItemTask;
import com.kola.kmp.logic.mission.MissionCompleteCondition.GameLevelTask;
import com.kola.kmp.logic.mission.MissionCompleteCondition.KillMonsterTask;
import com.kola.kmp.logic.mission.MissionCompleteCondition.UseFunctionTask;
import com.kola.kmp.logic.mission.MissionCompleteCondition.UseItemTask;
import com.kola.kmp.logic.mission.MissionDialog.Dialogue;
import com.kola.kmp.logic.mission.assistant.KAssistantManager;
import com.kola.kmp.logic.mission.daily.KDailyMissionManager;
import com.kola.kmp.logic.mission.guide.KGuideManager;
import com.kola.kmp.logic.npc.KNPCDataStructs.KNPCTemplate;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.npc.menu.IMissionMenu;
import com.kola.kmp.logic.npc.menu.KMenuService;
import com.kola.kmp.logic.other.KColorFunEnum;
import com.kola.kmp.logic.other.KColorFunEnum.KColor;
import com.kola.kmp.logic.other.KColorFunEnum.KColorManager;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KFunctionTypeEnum;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KGameMissionFunTypeEnum;
import com.kola.kmp.logic.other.KGameMissionStatusEnum;
import com.kola.kmp.logic.other.KGameMissionTemplateTypeEnum;
import com.kola.kmp.logic.other.KMissionSearchRoadTypeEnum;
import com.kola.kmp.logic.other.KUseFunctionTypeEnum;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.text.HyperTextTool;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.LevelTips;
import com.kola.kmp.logic.util.tips.MissionTips;
import com.kola.kmp.logic.util.tips.ShopTips;
import com.kola.kmp.protocol.mission.KMissionProtocol;

public class KMissionManager {

	private static final KGameLogger _LOGGER = KGameLogger
			.getLogger(KMissionManager.class);

	public static final int SUBMIT_MISSION_RESULT_SUCCESS = 0;

	public static final int SUBMIT_MISSION_RESULT_FAILD = 1;

	public static final int SUBMIT_MISSION_RESULT_BAG_FULL = 2;

	private KDailyMissionManager dailyMissionManager;

	// 任务模版Map
	private final Map<Integer, KMissionTemplate> allMissionTemplates = new LinkedHashMap<Integer, KMissionTemplate>();
	// 任务事件监听器
	private List<KMissionEventListener> missionEventListeners = new ArrayList<KMissionEventListener>();

	private static int searchRoadLevelIconId = 310000;

	/**
	 * 初始化任务模块管理器
	 * 
	 * @param configPath
	 * @throws Exception
	 */
	public void init(String configPath) throws Exception {
		try {
			_LOGGER.info("！！！任务模块加载开始！！！");
			Document doc = XmlUtil.openXml(configPath);
			if (doc != null) {
				Element root = doc.getRootElement();

				// String tipsFilePath = root.getChildTextTrim("tipsPath");
				//
				// initTips(tipsFilePath);

				String missionDataExcelFilePath = root
						.getChildText("missionDataExcelFilePath");

				String assistantDataExcelFilePath = root
						.getChildText("assistantDataExcelFilePath");

				KGuideManager.isTestOpenAllFunction = root.getChildText(
						"isTestOpenAllFunction").equals("true");

				KGuideManager.isCloseNoviceGuide = root.getChildText(
						"isCloseNoviceGuide").equals("true");
				KNoviceGuideSupportImpl.second_weapon_guide_mission_id = Integer
						.parseInt(root.getChild("secondWeaponGuide")
								.getAttributeValue("missionTemplateId"));
				KNoviceGuideSupportImpl.second_weapon_guide_level_id = Integer
						.parseInt(root.getChild("secondWeaponGuide")
								.getAttributeValue("levelId"));
				KNoviceGuideSupportImpl.mount_guide_mission_id = Integer
						.parseInt(root.getChild("mountGuide")
								.getAttributeValue("missionTemplateId"));
				KNoviceGuideSupportImpl.mount_guide_level_id = Integer
						.parseInt(root.getChild("mountGuide")
								.getAttributeValue("levelId"));

				// System.out.println("########### " +
				// scenarioDataExcelFilePath);

				// 读取关卡剧本excel表头，初始化关卡剧本数据以及加载相关资源
				loadMissionExcelData(missionDataExcelFilePath);

				// 新手引导初始化
				// noviceGuideManager = new NoviceGuideManager();
				// noviceGuideManager.isTestOpenAllFunction = root.getChildText(
				// "isTestOpenAllFunction").equals("true");
				// noviceGuideManager.init(missionDataExcelFilePath);

				// 读取日常任务配置表
				dailyMissionManager = new KDailyMissionManager();
				String dailyMissionDataExcelPath = root
						.getChildText("dailyMissionDataExcelPath");
				dailyMissionManager.init(dailyMissionDataExcelPath);

				// 加载主菜单功能类型信息
				KGuideManager.init(missionDataExcelFilePath);

				KAssistantManager.init(assistantDataExcelFilePath);

				// 读取阶段目标数据配置表
				// String stageDataExcelPath = root
				// .getChildText("stageDataExcelPath");
				// StageTargetManager.init(stageDataExcelPath);

				// 读取完成任务赠送宠物的数据
				// List<Element> addPetMissionListE = root.getChild(
				// "completeMissionAddPet").getChildren("mission");
				// for (Element addPetE : addPetMissionListE) {
				// int missionTemplateId = Integer.parseInt(addPetE
				// .getChildText("templateId"));
				// int warriorPetId = Integer.parseInt(addPetE
				// .getChildText("warriorPetId"));
				// int magicianPetId = Integer.parseInt(addPetE
				// .getChildText("magicianPetId"));
				// int bowmanPetId = Integer.parseInt(addPetE
				// .getChildText("bowmanPetId"));
				//
				// if (!KSupportFactory.getPetSupport().isPetTemplateExist(
				// warriorPetId)
				// || !KSupportFactory.getPetSupport()
				// .isPetTemplateExist(magicianPetId)
				// || !KSupportFactory.getPetSupport()
				// .isPetTemplateExist(bowmanPetId)) {
				// throw new KGameServerException(
				// "加载任务config配置文件的completeMissionAddPet错误：找不到对应的宠物模版，模版ID,"
				// + "战士：" + warriorPetId + ",法师："
				// + warriorPetId + ",弓手：" + warriorPetId);
				// }
				//
				// CompleteMissionAddPetInfo info = new
				// CompleteMissionAddPetInfo();
				// info.missionTemplateId = missionTemplateId;
				// info.warriorPetId = warriorPetId;
				// info.magicianPetId = magicianPetId;
				// info.bowmanPetId = bowmanPetId;
				// completeMissionAddPetMap.put(missionTemplateId, info);
				// }

				// 其他辅助功能开启状态配置
				// String otherFunctionOpenStateConfigPath = root
				// .getChildText("otherFunctionOpenStateConfigPath");
				// initOtherFunOpenState(otherFunctionOpenStateConfigPath);

			} else {
				throw new NullPointerException("任务模块配置不存在！！");
			}
		} catch (Exception e) {
			throw new KGameServerException("读取任务模块excel表头发生错误！", e);
		}
	}

	/**
	 * 读取任务Excel配置表，初始化任务模版数据
	 * 
	 * @param filePath
	 * @throws Exception
	 */
	private void loadMissionExcelData(String filePath) throws Exception {
		KGameExcelFile xlsFile = null;
		try {
			xlsFile = new KGameExcelFile(filePath);
		} catch (BiffException e) {
			throw new KGameServerException("读取任务模块excel表头发生错误！", e);
		} catch (IOException e) {
			throw new KGameServerException("读取任务模块excel表头发生错误！", e);
		}

		if (xlsFile != null) {
			// 初始答题任务题库数据
			// KGameExcelTable questionTable = xlsFile.getTable("答题任务题库", 4);
			// questionManager =
			// AnswerQuestionMissionManagerFactory.getInstance()
			// .initQuestionDataManager(questionTable);

			// 初始化任务基本数据
			int missionDataRowIndex = 4;
			KGameExcelTable missionDataTable = xlsFile.getTable("任务模版数据",
					missionDataRowIndex);
			KGameExcelRow[] allMissionDataRows = missionDataTable
					.getAllDataRows();
			for (int i = 0; i < allMissionDataRows.length; i++) {
				KMissionTemplate missionTemplate = new KMissionTemplate();
				missionTemplate.initKMissionTemplateData(allMissionDataRows[i],
						missionDataRowIndex + i + 1);
				if (allMissionTemplates
						.containsKey(missionTemplate.missionTemplateId)) {
					throw new KGameServerException("加载任务模版表错误：设置重复的任务ID，值="
							+ missionTemplate.missionTemplateId + "，Row="
							+ allMissionDataRows[i].getIndexInFile());
				}
				allMissionTemplates.put(missionTemplate.missionTemplateId,
						missionTemplate);

			}

			// 初始化任务对话数据
			int missionDialogDataRowIndex = 2;
			KGameExcelTable missionDialogTable = xlsFile.getTable("任务对话数据",
					missionDialogDataRowIndex);
			KGameExcelRow[] allMissionDialogRows = missionDialogTable
					.getAllDataRows();
			for (int i = 0; i < allMissionDialogRows.length; i++) {
				int missionTemplateId = allMissionDialogRows[i]
						.getInt("missionTemplateId");

				if (allMissionTemplates.containsKey(missionTemplateId)) {
					KMissionTemplate missionTemplate = allMissionTemplates
							.get(missionTemplateId);
					if (missionTemplate.isInitDialogData) {
						throw new KGameServerException("加载任务对话表错误：设置重复的任务ID，值="
								+ missionTemplate.missionTemplateId + "，Row="
								+ allMissionDataRows[i].getIndexInFile());
					}
					missionTemplate.isInitDialogData = true;
					missionTemplate.initKMissionTemplateDialog(
							allMissionDialogRows[i], missionDialogDataRowIndex
									+ i + 1);
				}
			}

			// 初始化所有任务模版中的完成本任务触发新的可接任务模版列表
			initNextAcceptableMissionTemplateList();

		}
	}

	/**
	 * 初始化所有任务模版中的完成本任务触发新的可接任务模版列表
	 */
	private void initNextAcceptableMissionTemplateList() {
		for (KMissionTemplate template : allMissionTemplates.values()) {
			if (template.missionTriggerCondition != null
					&& template.missionTriggerCondition.frontMissionTemplateId > 0) {
				KMissionTemplate frontMissionTemplate = allMissionTemplates
						.get(template.missionTriggerCondition.frontMissionTemplateId);
				if (frontMissionTemplate != null) {
					frontMissionTemplate.nextAcceptableMissionTemplateList
							.add(template);
				}
			}
		}
	}

	public void checkAllMissionDialog() throws Exception {
		for (KMissionTemplate template : allMissionTemplates.values()) {

			if (template.missionReward.getPetRewardMap().size() > 0) {
				for (Integer petTempId : template.missionReward
						.getPetRewardMap().keySet()) {
					if (KSupportFactory.getPetModuleSupport().getPetTemplate(
							petTempId) == null) {
						throw new KGameServerException(
								"加载任务模版表的petReward错误：找不到对应的宠物模版,值=" + petTempId
										+ "，任务模版ID="
										+ template.missionTemplateId);

					}
				}
			}

			if (template.missionCompleteCondition.isCollectItemTask
					&& template.missionCompleteCondition.getCollectItemTask() != null) {
				KLevelTemplate targetLevel = KGameLevelModuleExtension
						.getManager()
						.getKGameLevel(
								template.missionCompleteCondition.completedTargetId);

				CollectItemTask task = template.missionCompleteCondition
						.getCollectItemTask();
				for (String itemCode : task.itemCodeSet) {
					if (!targetLevel.getReward().getCheckDropItemCodeSet()
							.contains(itemCode)) {
						throw new KGameServerException(
								"加载任务模版表的collectItemCode错误：目标关卡="
										+ targetLevel.getLevelId()
										+ "没有道具code=" + itemCode
										+ "的产出，任务模版ID="
										+ template.missionTemplateId);
						// System.err.println("加载任务模版表的collectItemCode错误：目标关卡="
						// + targetLevel.getLevelId() + "没有道具code="
						// + itemCode + "的产出，任务模版ID="
						// + template.missionTemplateId);
					}
				}

				if (task.isLimitJob) {
					for (byte job : task.itemTemplateMap.keySet()) {
						String itemName = task.itemTemplateMap.get(job).extItemName;
						int count = task.collectCount;
						template.acceptMissionDialog.collectItemDialogContentMap
								.put(job,
										MissionTips
												.getTipsCollectItemMissionAcceptDiaLogTips(
														HyperTextTool
																.extColor(
																		targetLevel
																				.getLevelName(),
																		KColorFunEnum.品质_蓝),
														itemName, count));
					}
				} else {
					String itemName = task.itemTemplate.extItemName;
					int count = task.collectCount;
					template.acceptMissionDialog.collectItemDialogContentMap
							.put(MissionDialog.default_job, MissionTips
									.getTipsCollectItemMissionAcceptDiaLogTips(
											HyperTextTool.extColor(
													targetLevel.getLevelName(),
													KColorFunEnum.品质_蓝),
											itemName, count));
				}
			}

			if (template.acceptMissionDialog == null
					|| template.acceptMissionDialog.getDialogueList() == null) {
				throw new KGameServerException("加载任务对话表错误：任务ID，值="
						+ template.missionTemplateId + "的接受任务对话为NULL");
			}
			if (template.getCompletedMissionDialog() == null
					|| template.getCompletedMissionDialog().getDialogueList() == null) {
				throw new KGameServerException("加载任务对话表错误：任务ID，值="
						+ template.missionTemplateId + "的完成任务对话为NULL");
			}
			if (template.missionFunType != KGameMissionFunTypeEnum.MISSION_FUN_TYPE_DIALOG) {
				if (template.getUncompletedMissionDialog() == null
						|| template.getUncompletedMissionDialog()
								.getDialogueList() == null) {
					throw new KGameServerException("加载任务对话表错误：任务ID，值="
							+ template.missionTemplateId + "的未完成任务对话为NULL");
				}
			}

			if (!KMapModule.getGameMapManager().getNpcMappingGameMap()
					.containsKey(template.acceptMissionNPCTemplate.templateId)) {
				throw new KGameServerException("加载任务模版表错误：任务ID，值="
						+ template.missionTemplateId
						+ "，找不到接受任务的NPC所在的地图，NPC的模版ID="
						+ template.acceptMissionNPCTemplate.templateId);
			}
			if (!KMapModule.getGameMapManager().getNpcMappingGameMap()
					.containsKey(template.submitMissionNPCTemplate.templateId)) {
				throw new KGameServerException("加载任务模版表错误：任务ID，值="
						+ template.missionTemplateId
						+ "，找不到提交任务的NPC所在的地图，NPC的模版ID="
						+ template.submitMissionNPCTemplate.templateId);
			}
		}
	}

	public void onGameWorldInitComplete() throws Exception {
		checkAllMissionDialog();

		Map<Integer, Integer> missionLvMap = new LinkedHashMap<Integer, Integer>();
		for (Integer missionTemplateId : allMissionTemplates.keySet()) {
			if (missionTemplateId > 129) {
				break;
			}
			missionLvMap
					.put(missionTemplateId,
							allMissionTemplates.get(missionTemplateId).missionTriggerCondition.roleLevelLimit);
		}
		FlowDataModuleFactory.getModule().initMissionLvMap(missionLvMap);

		KGuideManager.checkInit();

		KAssistantManager.checkInit();
	}

	/**
	 * 获取一个任务模版
	 * 
	 * @param templateId
	 * @return
	 */
	public KMissionTemplate getMissionTemplate(int templateId) {
		return this.allMissionTemplates.get(templateId);
	}

	/**
	 * 检测任务模版是否存在
	 * 
	 * @param templateId
	 * @return
	 */
	public boolean isMissionTemplateExist(int templateId) {
		return this.allMissionTemplates.containsKey(templateId);
	}

	public KDailyMissionManager getDailyMissionManager() {
		return dailyMissionManager;
	}

	/**
	 * 注册一个任务事件监听器
	 * 
	 * @param listener
	 */
	public void registerMissionEventListener(KMissionEventListener listener) {
		this.missionEventListeners.add(listener);
	}

	/**
	 * 获取所有注册的任务事件监听器
	 * 
	 * @return
	 */
	public List<KMissionEventListener> getAllMissionEventListener() {
		return missionEventListeners;
	}

	/**
	 * 处理角色登录时查找角色可接任务
	 * 
	 * @param role
	 * @return
	 */
	public void processSearchCanAcceptedMission(KRole role) {

		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role
				.getId());
		KMissionCompleteRecordSet completeSet = KMissionModuleExtension
				.getMissionCompleteRecordSet(role.getId());

		for (KMissionTemplate missionTemplate : this.allMissionTemplates
				.values()) {
			// 判断该任务是否可接
			if (checkMissionCanAccepted(missionTemplate, role, missionSet,
					completeSet)) {
				missionSet.getAcceptableMissionTemplateMap().put(
						missionTemplate.missionTemplateId, missionTemplate);
			}
		}
	}

	/**
	 * 根据任务模版判断该任务是否可接
	 * 
	 * @param missionTemplate
	 * @param role
	 * @param container
	 * @return
	 */
	public boolean checkMissionCanAccepted(KMissionTemplate missionTemplate,
			KRole role, KMissionSet missionSet,
			KMissionCompleteRecordSet completeSet) {
		// 判断该任务模版ID是否存在于角色任务容器的已完成任务表中，如果该任务已经完成则返回false
		if (completeSet
				.checkMissionIsCompleted(missionTemplate.missionTemplateId)) {
			return false;
		}
		// 判断该任务模版ID是否存在于角色任务容器未关闭状态任务表中，如果存在则返回false
		if (missionSet
				.checkMissionIsUncompleted(missionTemplate.missionTemplateId)) {
			return false;
		}

		MissionTriggerCondition triggerCondition = missionTemplate.missionTriggerCondition;
		// 如果是非主线任务并且角色等级小于任务触发条件的角色等级限制，则返回false
		if (!missionTemplate.isMainLineMission
				&& role.getLevel() < triggerCondition.roleLevelLimit) {
			return false;
		}
		// 判断该任务是否有前置任务的限制
		if (triggerCondition.frontMissionTemplateId > 0) {
			// 如果角色的前置任务不存在于角色任务容器的已完成任务表中（即前置任务未完成），则跳过
			if (!completeSet
					.checkMissionIsCompleted(triggerCondition.frontMissionTemplateId)) {
				return false;
			}
		}
		// 判断是否有角色职业限制
		if (triggerCondition.isOccupationCondition) {
			// 如果触发条件限制的职业与角色不符，则跳过
			if (triggerCondition.occupationType.getJobType() != role.getJob()) {
				return false;
			}
		}
		// 判断是否有以加入帮会为限制条件
		if (triggerCondition.isGangCondition) {

		}

		return true;
	}

	/**
	 * 获取角色任务列表
	 * 
	 * @param role
	 */
	public void processGetPlayerRoleMissionList(KRole role) {

		KGameMessage sendMsg = KGame
				.newLogicMessage(KMissionProtocol.SM_GET_MISSION_LIST);

		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role
				.getId());

		MainUISearchRoadSturct searchRoadstruct = getMainUISearchRoadMessage(
				role, missionSet, sendMsg);
		// 主线任务
		KMission mainLineMission = null;
		// 主线任务模版
		KMissionTemplate mainLineMissionTemplate = null;

		int missionSize = missionSet.getAllUnclosedMission().size()
				+ missionSet.getAllAcceptableMissionTemplate().size();
		sendMsg.writeInt(missionSize);
		if (missionSize > 0) {
			for (int i = 0; i < missionSet.getAllUnclosedMission().size(); i++) {
				KMission mission = missionSet.getAllUnclosedMission().get(i);
				if (mission.getMissionTemplate().missionType == KGameMissionTemplateTypeEnum.MISSION_TYPE_MAIN_LINE) {
					mainLineMission = mission;
				}
				KMissionTemplate missionTemplate = mission.getMissionTemplate();
				MissionCompleteCondition condition = missionTemplate.missionCompleteCondition;
				sendMsg.writeInt(missionTemplate.missionTemplateId);
				sendMsg.writeUtf8String(missionTemplate
						.getMissionNameByStatusType(mission.getMissionStatus(),
								role));
				sendMsg.writeUtf8String(HyperTextTool.extColor(
						missionTemplate.desc, 5));
				sendMsg.writeUtf8String(missionTemplate.acceptMissionNPCTemplate.name);
				sendMsg.writeUtf8String(missionTemplate.submitMissionNPCTemplate.name);
				sendMsg.writeUtf8String(getMissionListTips(role, mission));
				// 处理任务奖励
				MissionReward reward = missionTemplate.missionReward;
				reward.getBaseRewardData(role.getJob()).packMsg(sendMsg);

				sendMsg.writeByte(missionTemplate.missionType.missionType);
				sendMsg.writeByte(mission.getMissionStatus().statusType);
				setSearchRoadDataInMessage(role, mission, sendMsg, false);
			}

			for (KMissionTemplate missionTemplate : missionSet
					.getAllAcceptableMissionTemplate()) {
				if (missionTemplate.missionType == KGameMissionTemplateTypeEnum.MISSION_TYPE_MAIN_LINE) {
					mainLineMissionTemplate = missionTemplate;
				}
				MissionCompleteCondition condition = missionTemplate.missionCompleteCondition;
				sendMsg.writeInt(missionTemplate.missionTemplateId);
				sendMsg.writeUtf8String(missionTemplate
						.getMissionNameByStatusType(
								KGameMissionStatusEnum.MISSION_STATUS_TRYRECEIVE,
								role));
				sendMsg.writeUtf8String(HyperTextTool.extColor(
						missionTemplate.desc, 5));
				sendMsg.writeUtf8String(missionTemplate.acceptMissionNPCTemplate.name);
				sendMsg.writeUtf8String(missionTemplate.submitMissionNPCTemplate.name);
				sendMsg.writeUtf8String(getMissionListTips(role,
						missionTemplate));

				// 处理任务奖励
				MissionReward reward = missionTemplate.missionReward;
				reward.getBaseRewardData(role.getJob()).packMsg(sendMsg);

				sendMsg.writeByte(missionTemplate.missionType.missionType);
				sendMsg.writeByte(KGameMissionStatusEnum.MISSION_STATUS_TRYRECEIVE.statusType);
				sendMsg.writeBoolean(missionTemplate.isMissionCanAccept(role));
				setSearchRoadDataInMessage(role, missionTemplate, sendMsg,
						false, KMissionSearchRoadTypeEnum.SEARCH_ROAD_TYPE_NPC);
			}
		}
		role.sendMsg(sendMsg);

		// 判断是否有新手引导并处理消息
		if (mainLineMission != null) {
			processNoviceGuideMessage(role, mainLineMission);
		} else if (mainLineMissionTemplate != null) {
			processNoviceGuideMessage(role, mainLineMissionTemplate);
		}

	}

	/**
	 * 处理主城UI界面的任务追踪功能的消息数据
	 * 
	 * @param container
	 * @param sendMsg
	 */
	private MainUISearchRoadSturct getMainUISearchRoadMessage(KRole role,
			KMissionSet missionSet, KGameMessage sendMsg) {
		byte mainSearchRoadType;
		int mainSearchRoadIconId;
		byte mainSearchRoadTargetType;
		String mainSearchRoadTargetId;

		// 先扫描任务容器是否有当前正在操作的任务模版Id
		if (missionSet.currentTargetMissionTemplateId > 0) {
			if (missionSet
					.checkMissionIsUncompleted(missionSet.currentTargetMissionTemplateId)) {
				// 如果任务容器中当前正在操作的任务模版Id为一个已接任务
				KMission currentMission = missionSet
						.getUnclosedMission(missionSet.currentTargetMissionTemplateId);
				return setSearchRoadDataInMessage(role, currentMission,
						sendMsg, true);

			} else if (missionSet.getAcceptableMissionTemplateMap()
					.containsKey(missionSet.currentTargetMissionTemplateId)) {

				KMissionTemplate currentMissionTemplate = missionSet
						.getAcceptableMissionTemplateMap().get(
								missionSet.currentTargetMissionTemplateId);
				if (currentMissionTemplate.isMissionCanAccept(role)) {
					// 如果任务容器中当前正在操作的任务模版Id为一个可接任务
					return setSearchRoadDataInMessage(role,
							currentMissionTemplate, sendMsg, true,
							KMissionSearchRoadTypeEnum.SEARCH_ROAD_TYPE_NPC);
				}
			}
		}

		boolean isHasMissionTracking = false;
		// 定义当前寻路已接任务
		KMission mainTrackingUncompletedMission = null;
		// 定义当前寻路可接任务模版
		KMissionTemplate mainTrackingAcceptableMissionTemplate = null;

		// 先扫描已接未提交的任务
		for (KMission mission : missionSet.getAllUnclosedMission()) {
			if (mainTrackingUncompletedMission == null) {
				// 如果当前已接寻路任务为null，则将该已接任务设置为当前寻路已接任务
				mainTrackingUncompletedMission = mission;
			} else {
				if (mainTrackingUncompletedMission.getMissionStatus().statusTrackingSerial < mission
						.getMissionStatus().statusTrackingSerial) {
					// 如果当前已接寻路任务的任务状态优先级低于该已接任务的状态优先级，则将该已接任务设置为
					// 当前寻路已接任务
					mainTrackingUncompletedMission = mission;
				} else if (mainTrackingUncompletedMission.getMissionStatus().statusTrackingSerial == mission
						.getMissionStatus().statusTrackingSerial) {
					// 如果当前已接寻路任务的任务状态优先级等于该已接任务的状态优先级
					if (mainTrackingUncompletedMission.getMissionTemplate().missionType.trackingSerial < mission
							.getMissionTemplate().missionType.trackingSerial) {
						// 如果当前已接寻路任务的任务类型优先级等于该已接任务的类型优先级，则将该已接任务设置为
						// 当前寻路已接任务
						mainTrackingUncompletedMission = mission;
					}
				}
			}
		}

		if (mainTrackingUncompletedMission != null) {
			// 如果当前寻路已接任务非空，则可以组装消息
			return setSearchRoadDataInMessage(role,
					mainTrackingUncompletedMission, sendMsg, true);
		} else {
			// 如果当前寻路已接任务为空，则表示当前没有已接任务的追踪目标，需要扫描可接任务模版列表
			for (KMissionTemplate missiontemplate : missionSet
					.getAllAcceptableMissionTemplate()) {
				if (mainTrackingAcceptableMissionTemplate == null) {
					// 当前寻路可接任务模版为null，则将该可接任务模版设置为当前寻路可接接任务模版
					mainTrackingAcceptableMissionTemplate = missiontemplate;
				} else {
					if (mainTrackingAcceptableMissionTemplate.missionType.trackingSerial < missiontemplate.missionType.trackingSerial) {
						// 如果当前寻路可接任务模版的任务类型优先级等于该可接寻路任务模版的类型优先级，则将该可接寻路任务模版设置为
						// 当前寻路可接寻路任务模版
						if (missiontemplate.isMissionCanAccept(role)) {
							mainTrackingAcceptableMissionTemplate = missiontemplate;
						}
					} else if (mainTrackingAcceptableMissionTemplate.missionType.trackingSerial > missiontemplate.missionType.trackingSerial) {
						if (!mainTrackingAcceptableMissionTemplate
								.isMissionCanAccept(role)
								&& missiontemplate.isMissionCanAccept(role)) {
							mainTrackingAcceptableMissionTemplate = missiontemplate;
						}
					}
				}
			}

			if (mainTrackingAcceptableMissionTemplate != null) {

				// 如果当前寻路可接任务模版非空，则可以组装消息
				if (mainTrackingAcceptableMissionTemplate
						.isMissionCanAccept(role)) {
					return setSearchRoadDataInMessage(role,
							mainTrackingAcceptableMissionTemplate, sendMsg,
							true,
							KMissionSearchRoadTypeEnum.SEARCH_ROAD_TYPE_NPC);
				} else {
					return setSearchRoadDataInMessage(
							role,
							mainTrackingAcceptableMissionTemplate,
							sendMsg,
							true,
							KMissionSearchRoadTypeEnum.SEARCH_ROAD_TYPE_MISSION_PANEL);
				}

			}
		}

		// 执行至这里则表示没有任何任务追踪目标，组装消息
		sendMsg.writeBoolean(false);// 没有追踪目标

		return null;
	}

	/**
	 * 设置角色任务列表消息中的寻路目标相关数据
	 * 
	 * @param mission
	 *            某个已接的任务
	 * @param sendMsg
	 * @param isHasIcon
	 *            是否主界面UI的任务寻路追踪数据
	 */
	public MainUISearchRoadSturct setSearchRoadDataInMessage(KRole role,
			KMission mission, KGameMessage sendMsg, boolean isHasIcon) {
		if (isHasIcon) {
			sendMsg.writeBoolean(true);// 有追踪目标
		}
		KMissionSearchRoadTypeEnum type = getSearchRoadTypeEnum(mission);
		sendMsg.writeByte(type.getOperateType());
		sendMsg.writeByte(type.getSearchRoadType());

		MainUISearchRoadSturct struct = new MainUISearchRoadSturct();
		struct.searchRoadType = type;
		struct.missionTemplate = mission.getMissionTemplate();
		struct.missionStatus = mission.getMissionStatus();

		switch (type) {
		case SEARCH_ROAD_TYPE_NPC:
			KNPCTemplate npcTemplate;
			// if (mission.getMissionStatus() ==
			// KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH
			// && mission.getMissionTemplate().missionFunType ==
			// KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_FUNCTION
			// && KFunctionTypeEnum
			// .getEnum(mission.getMissionTemplate().missionCompleteCondition
			// .getUseFunctionTask().functionId).functionBigType ==
			// FunctionTypeEnum.FUN_BIG_TYPE_NPC_SHOP) {
			// npcTemplate = KSupportFactory
			// .getNpcModuleSupport()
			// .getNPCTemplate(
			// mission.getMissionTemplate().missionCompleteCondition.completedTargetId);
			// } else {
			npcTemplate = mission.getMissionTemplate().submitMissionNPCTemplate;
			// }
			struct.searchRoadTargetId = npcTemplate.templateId + "";
			// struct.searchRoadTargetName = mission.getMissionTemplate()
			// .getSubmitMissionNPCTemplate().name;
			struct.searchRoadTargetName = getSearchRoadTargetNameHyperText(
					mission.getMissionTemplate(), npcTemplate.name);

			String npcTemplateIdStr = npcTemplate.templateId + "";
			sendMsg.writeUtf8String(npcTemplateIdStr);

			if (isHasIcon) {
				// 处理提交任务的NPC头像
				int npcIcon = npcTemplate.taskHeadUI;
				sendMsg.writeInt(npcIcon);
				sendMsg.writeUtf8String(npcTemplate.name);
				// sendMsg.writeUtf8String(struct.searchRoadTargetName);
			}

			break;
		case SEARCH_ROAD_TYPE_LEVEL:
			int levelId = mission.getMissionTemplate().missionCompleteCondition.completedTargetId;
			struct.searchRoadTargetId = levelId + "";

			String levelName = "";
			int levelIcon = searchRoadLevelIconId;
			if (levelId == GameLevelTask.ANY_TYPE_LEVEL) {
				// levelName = "任意关卡";
				levelName = MissionTips.getTipsSearchRoadAnyGameLevelDesc();
			} else {
				levelName = KGameLevelModuleExtension.getManager()
						.getKGameLevel(levelId).getLevelName();
				// levelIcon = KGameLevelModuleExtension.getManager()
				// .getKGameLevel(levelId).getIconResId();
				// struct.searchRoadTargetName = levelName;
				struct.searchRoadTargetName = getSearchRoadTargetNameHyperText(
						mission.getMissionTemplate(), levelName);
			}
			if (mission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_COLLECT_ITEMS) {
				CollectItemTask task = mission.getMissionTemplate().missionCompleteCondition
						.getCollectItemTask();
				if (task.isLimitJob) {
					levelName += ("\n"
							+ MissionTips.getTipsCollectItemTypeTips() +"\n"+ task.itemTemplateMap
							.get(role.getJob()).extItemName);
				} else {
					levelName += ("\n"
							+ MissionTips.getTipsCollectItemTypeTips() +"\n"+ task.itemTemplate.extItemName);
				}
			}

			sendMsg.writeUtf8String(levelId + "");
			if (isHasIcon) {
				// 处理关卡图标
				sendMsg.writeInt(levelIcon);
				sendMsg.writeUtf8String(levelName);
				// sendMsg.writeUtf8String(struct.searchRoadTargetName);
			}

			break;
		case SEARCH_ROAD_TYPE_ITEM:
			struct.searchRoadTargetId = mission.getMissionTemplate().missionCompleteCondition
					.getUseItemTask().itemTemplate.itemCode;
			// struct.searchRoadTargetName = getSearchRoadTargetNameHyperText(
			// mission.getMissionTemplate(), "使用道具["
			// + mission.getMissionTemplate()
			// .getMissionCompleteCondition()
			// .getUseItemTask().itemTemplate.ItemName
			// + "]");
			struct.searchRoadTargetName = getSearchRoadTargetNameHyperText(
					mission.getMissionTemplate(),
					MissionTips.getTipsSearchRoadUseItemDesc(mission
							.getMissionTemplate().missionCompleteCondition
							.getUseItemTask().itemTemplate.name));
			sendMsg.writeUtf8String(mission.getMissionTemplate().missionCompleteCondition
					.getUseItemTask().itemTemplate.itemCode);
			if (isHasIcon) {
				// TODO 处理道具图标ID
				sendMsg.writeInt(mission.getMissionTemplate().missionCompleteCondition
						.getUseItemTask().itemTemplate.icon);
				// sendMsg.writeInt(0);
				sendMsg.writeUtf8String(struct.searchRoadTargetName);
			}
			break;
		case SEARCH_ROAD_TYPE_FUNCTION:
			// TODO 处理功能寻路
			short functionType = 0;
			String functionTarget = "";
			short functionId = 0;

			if (mission.getMissionTemplate().missionCompleteCondition.isUseFunctionTask) {
				functionType = mission.getMissionTemplate().missionCompleteCondition
						.getUseFunctionTask().functionId;
				functionTarget = mission.getMissionTemplate().missionCompleteCondition
						.getUseFunctionTask().functionTarget;
				functionId = mission.getMissionTemplate().missionCompleteCondition
						.getUseFunctionTask().functionId;
			} else if (mission.getMissionTemplate().missionCompleteCondition.isUpgradeFunLvTask) {
				functionType = mission.getMissionTemplate().missionCompleteCondition
						.getUpgradeFunLvTask().functionId;
				functionTarget = mission.getMissionTemplate().missionCompleteCondition
						.getUpgradeFunLvTask().functionTarget;
				functionId = mission.getMissionTemplate().missionCompleteCondition
						.getUpgradeFunLvTask().functionId;
			}
			if (functionType == KUseFunctionTypeEnum.灌溉好友庄园.functionId) {
				functionType = KFunctionTypeEnum.保卫庄园.functionId;
			}

			sendMsg.writeUtf8String(functionType + "," + functionTarget);
			if (isHasIcon) {
				int functionIcon = KGuideManager.getMainMenuFunctionInfoMap()
						.get(functionId).getIconResId();
				String functionName = KGuideManager
						.getMainMenuFunctionInfoMap().get(functionId)
						.getFunctionName();
				sendMsg.writeInt(functionIcon);
				sendMsg.writeUtf8String(getSearchRoadTargetNameHyperText(
						mission.getMissionTemplate(), "【" + functionName + "】"));
			}

			struct.searchRoadTargetId = functionId + "";

			break;
		case SEARCH_ROAD_TYPE_QUESTION:
			sendMsg.writeUtf8String(mission.getMissionTemplate().missionTemplateId
					+ "");
			break;
		}

		if (isHasIcon) {
			sendMsg.writeInt(mission.getMissionTemplate().missionTemplateId);
		}

		return struct;

	}

	/**
	 * 设置角色任务列表消息中的寻路目标相关数据
	 * 
	 * @param mission
	 *            某个可接的任务模版
	 * @param sendMsg
	 * @param isHasIcon
	 *            是否主界面UI的任务寻路追踪数据
	 */
	private MainUISearchRoadSturct setSearchRoadDataInMessage(KRole role,
			KMissionTemplate missionTemplate, KGameMessage sendMsg,
			boolean isHasIcon, KMissionSearchRoadTypeEnum type) {

		if (isHasIcon) {
			sendMsg.writeBoolean(true);// 有追踪目标
		}
		MainUISearchRoadSturct struct = new MainUISearchRoadSturct();
		struct.searchRoadType = type;
		struct.missionTemplate = missionTemplate;
		struct.searchRoadTargetId = missionTemplate.acceptMissionNPCTemplate.templateId
				+ "";
		// struct.searchRoadTargetName = missionTemplate
		// .getAcceptMissionNPCTemplate().name;

		struct.missionStatus = KGameMissionStatusEnum.MISSION_STATUS_TRYRECEIVE;
		switch (type) {
		case SEARCH_ROAD_TYPE_NPC:
			struct.searchRoadTargetName = getSearchRoadTargetNameHyperText(
					missionTemplate,
					missionTemplate.acceptMissionNPCTemplate.name);
			sendMsg.writeByte(type.getOperateType());
			sendMsg.writeByte(type.getSearchRoadType());
			sendMsg.writeUtf8String(missionTemplate.acceptMissionNPCTemplate.templateId
					+ "");
			if (isHasIcon) {
				// 处理提交任务的NPC头像
				int npcIcon = missionTemplate.acceptMissionNPCTemplate.taskHeadUI;
				sendMsg.writeInt(npcIcon);
				// sendMsg.writeUtf8String(missionTemplate
				// .getAcceptMissionNPCTemplate().name);
				sendMsg.writeUtf8String(missionTemplate.acceptMissionNPCTemplate.name);
				sendMsg.writeInt(missionTemplate.missionTemplateId);
			}
			break;

		case SEARCH_ROAD_TYPE_MISSION_PANEL:
			struct.searchRoadTargetName = missionTemplate
					.getMissionNameBySearchRoadButton(role);
			sendMsg.writeByte(type.getOperateType());
			sendMsg.writeByte(type.getSearchRoadType());
			sendMsg.writeUtf8String(0 + "");
			if (isHasIcon) {
				// 处理提交任务的NPC头像
				int npcIcon = missionTemplate.acceptMissionNPCTemplate.taskHeadUI;
				sendMsg.writeInt(npcIcon);
				// sendMsg.writeUtf8String(missionTemplate
				// .getAcceptMissionNPCTemplate().name);
				sendMsg.writeUtf8String(struct.searchRoadTargetName);
				sendMsg.writeInt(missionTemplate.missionTemplateId);
			}
			break;
		default:
			break;
		}

		return struct;

	}

	/**
	 * 获取任务自动寻路的目标类型
	 * 
	 * @return
	 */
	public KMissionSearchRoadTypeEnum getSearchRoadTypeEnum(KMission mission) {
		if (mission.getMissionTemplate().missionType != KGameMissionTemplateTypeEnum.MISSION_TYPE_DAILY) {
			if (mission.getMissionStatus() == KGameMissionStatusEnum.MISSION_STATUS_TRYRECEIVE
					|| mission.getMissionStatus() == KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT) {
				return KMissionSearchRoadTypeEnum.SEARCH_ROAD_TYPE_NPC;
			} else if (mission.getMissionStatus() == KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH) {
				if (mission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_DIALOG) {
					return KMissionSearchRoadTypeEnum.SEARCH_ROAD_TYPE_NPC;
				} else if (mission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_KILL_MONSTER) {
					return KMissionSearchRoadTypeEnum.SEARCH_ROAD_TYPE_LEVEL;
				} else if (mission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_COLLECT_ITEMS) {
					return KMissionSearchRoadTypeEnum.SEARCH_ROAD_TYPE_LEVEL;
				} else if (mission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_GAME_LEVEL) {
					return KMissionSearchRoadTypeEnum.SEARCH_ROAD_TYPE_LEVEL;
				} else if (mission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_ITEM) {
					return KMissionSearchRoadTypeEnum.SEARCH_ROAD_TYPE_ITEM;
				} else if (mission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_FUNCTION) {
					// if (mission.getMissionStatus() ==
					// KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH
					// && mission.getMissionTemplate().missionFunType ==
					// KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_FUNCTION
					// && FunctionTypeEnum
					// .getEnum(mission.getMissionTemplate().missionCompleteCondition
					// .getUseFunctionTask().functionId).functionBigType ==
					// FunctionTypeEnum.FUN_BIG_TYPE_NPC_SHOP) {
					// return KMissionSearchRoadTypeEnum.SEARCH_ROAD_TYPE_NPC;
					// }
					return KMissionSearchRoadTypeEnum.SEARCH_ROAD_TYPE_FUNCTION;
				} else if (mission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_ATTRIBUTE_DATA) {
					return KMissionSearchRoadTypeEnum.SEARCH_ROAD_TYPE_NPC;
				} else if (mission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_UP_FUNC_LV) {
					// if (mission.getMissionStatus() ==
					// KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH
					// && mission.getMissionTemplate().missionFunType ==
					// KGameMissionFunTypeEnum.MISSION_FUN_TYPE_UP_FUNC_LV
					// && FunctionTypeEnum
					// .getEnum(mission.getMissionTemplate().missionCompleteCondition
					// .getUpgradeFunLvTask().functionId).functionBigType ==
					// FunctionTypeEnum.FUN_BIG_TYPE_NPC_SHOP) {
					// return KMissionSearchRoadTypeEnum.SEARCH_ROAD_TYPE_NPC;
					// }
					return KMissionSearchRoadTypeEnum.SEARCH_ROAD_TYPE_FUNCTION;
				}
			}
			return KMissionSearchRoadTypeEnum.SEARCH_ROAD_TYPE_NPC;
		} else {
			if (mission.getMissionStatus() == KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH) {
				if (mission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_DIALOG) {
					return KMissionSearchRoadTypeEnum.SEARCH_ROAD_TYPE_NPC;
				} else if (mission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_FUNCTION) {
					return KMissionSearchRoadTypeEnum.SEARCH_ROAD_TYPE_FUNCTION;
				} else if (mission.getMissionTemplate().missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_QUESTION) {
					return KMissionSearchRoadTypeEnum.SEARCH_ROAD_TYPE_QUESTION;
				} else {
					return KMissionSearchRoadTypeEnum.SEARCH_ROAD_TYPE_LEVEL;
				}
			}
			return KMissionSearchRoadTypeEnum.SEARCH_ROAD_TYPE_LEVEL;
		}
	}

	private String getSearchRoadTargetNameHyperText(
			KMissionTemplate missionTemplate, String targetName) {
		String tips = MissionTips.getTipsSearchRoadTargetNameDesc(
				missionTemplate
						.getMissionTypeNameText(missionTemplate.missionType),
				missionTemplate.missionExtName, targetName);
		return tips;
	}

	/**
	 * 处理任务列表中的某个已接任务提示字符串
	 * 
	 * @param mission
	 * @return
	 */
	private String getMissionListTips(KRole role, KMission mission) {
		KMissionTemplate missionTemplate = mission.getMissionTemplate();
		MissionCompleteCondition condition = missionTemplate.missionCompleteCondition;
		String tips = "";
		if (mission.getMissionStatus() == KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH) {
			if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_KILL_MONSTER) {
				tips += MissionTips.getTipsKillMonsterTypeTips();
				for (KillMonsterTask task : condition.getKillMonsterTaskMap()
						.values()) {
					KillMonsterRecord record = mission
							.getKillMonsterRecordMap()
							.get(task.isAnyTypeMonster ? KillMonsterTask.ANY_TYPE_MONSTER_ID
									: task.monsterTemplate.id);
					String monsterName = (task.isAnyTypeMonster) ? ((task.isMonsterLevelLimit) ? MissionTips
							.getTipsKillMonsterTypeMonsterName()
							: MissionTips
									.getTipsKillMonsterTypeMonsterName1(task.monsterLevel))
							: (task.monsterTemplate.name);
					int targetCount = task.killCount;
					int nowCount = (record != null) ? (record.killCount) : 0;
					if (nowCount > targetCount) {
						nowCount = targetCount;
					}
					tips += MissionTips.getTipsKillMonsterTypeMonsterInfo(
							monsterName, targetCount)
							+ MissionTips.getTipsMissionTipsCompleteStatus(
									nowCount, targetCount,
									(nowCount < targetCount) ? 9 : 6);
				}
			} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_COLLECT_ITEMS) {
				tips += MissionTips.getTipsCollectItemTypeTips();
				CollectItemTask task = condition.getCollectItemTask();
				String itemName;
				String itemCode;
				if (task.isLimitJob) {
					itemName = task.itemTemplateMap.get(role.getJob()).extItemName;
					itemCode = task.itemTemplateMap.get(role.getJob()).itemCode;
				} else {
					itemName = task.itemTemplate.extItemName;
					itemCode = task.itemTemplate.itemCode;
				}
				int targetCount = task.collectCount;
				int nowCount = (int)KSupportFactory.getItemModuleSupport()
						.checkItemCountInBag(mission.getRoleId(), itemCode);
				if (nowCount > targetCount) {
					nowCount = targetCount;
				}
				tips += MissionTips.getTipsCollectItemTypeItemInfo(itemName,
						targetCount)
						+ MissionTips.getTipsMissionTipsCompleteStatus(
								nowCount, targetCount,
								(nowCount < targetCount) ? 9 : 6);

			} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_GAME_LEVEL) {
				CompletedGameLevelRecord record = mission
						.getCompletedGameLevelRecord();
				GameLevelTask task = condition.getGameLevelTask();
				int targetCount = task.completeCount;
				int nowCount = record.completeCount;
				if (nowCount > targetCount) {
					nowCount = targetCount;
				}

				// String info = "";
				if (task.isLevelTypeAny()) {
					// info = "完成任意关卡 " + task.completeCount + "次";
					tips += MissionTips
							.getTipsCompleteLevelTypeInfo(task.completeCount);
				} else {
					String levelName = KGameLevelModuleExtension.getManager()
							.getKGameLevel(task.levelId).getLevelName();
					// info = ("完成关卡【" + levelName + "】" + task.completeCount +
					// "次");
					tips += MissionTips.getTipsCompleteLevelTypeInfo1(
							levelName, task.completeCount);
				}
				// tips += getHyperText(info, 13)
				// + getHyperText(" (" + nowCount + "/" + targetCount
				// + ")", (nowCount < targetCount) ? 1 : 15);
				tips += MissionTips.getTipsMissionTipsCompleteStatus(nowCount,
						targetCount, (nowCount < targetCount) ? 9 : 6);
			} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_FUNCTION) {
				UseFunctionRecord record = mission.getUseFunctionRecord();
				UseFunctionTask task = condition.getUseFunctionTask();
				int targetCount = task.useCount;
				int nowCount = record.completeCount;
				if (nowCount > targetCount) {
					nowCount = targetCount;
				}
				String funTips = "";

				// funTips =
				// FunctionTypeEnum.getEnum(task.functionId).functionTips;
				// tips += getHyperText((funTips + " " + task.useCount + "次"),
				// 13)
				// + getHyperText(" (" + nowCount + "/" + targetCount
				// + ")", (nowCount < targetCount) ? 1 : 15);
				funTips = MissionTips.getTipsUseFunTypeTips(KGuideManager
						.getMainMenuFunctionInfoMap().get(task.functionId)
						.getFunctionName());
				tips += MissionTips.getTipsUseFunTypeInfo(funTips, targetCount)
						+ MissionTips.getTipsMissionTipsCompleteStatus(
								nowCount, targetCount,
								(nowCount < targetCount) ? 9 : 6);
			} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_ITEM) {
				UseItemTask task = condition.getUseItemTask();
				// tips += getHyperText(
				// ("使用或装备道具【" + task.itemTemplate.ItemName + "】 1次 "), 13);
				tips += MissionTips
						.getTipsUseItemTypeInfo(task.itemTemplate.name);
			} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_QUESTION) {
				AnswerQuestionRecord record = mission.getAnswerQuestionRecord();
				AnswerQuestionTask task = condition.getAnswerQuestionTask();
				int targetCount = task.getTotalQuestionCount();
				int nowCount = record.completeCount;
				if (nowCount > targetCount) {
					nowCount = targetCount;
				}
				// String funTips = "回答NPC问题";
				// tips += getHyperText(
				// (funTips + " " + task.getTotalQuestionCount() + "次"),
				// 13)
				// + getHyperText(" (" + nowCount + "/" + targetCount
				// + ")", (nowCount < targetCount) ? 1 : 15);
				tips += MissionTips.getTipsQuestionTypeInfo(targetCount)
						+ MissionTips.getTipsMissionTipsCompleteStatus(
								nowCount, targetCount,
								(nowCount < targetCount) ? 9 : 6);

			} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_UP_FUNC_LV) {
				tips += missionTemplate.missionTips;
			}
		} else if (mission.getMissionStatus() == KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT) {
			// tips += getHyperText(("到NPC 【"
			// + mission.getMissionTemplate()
			// .getSubmitMissionNPCTemplate().name + "】 提交任务"), 13);
			tips += MissionTips.getTipsTalkToNpcTypeInfo(mission
					.getMissionTemplate().submitMissionNPCTemplate.name);
		}
		return tips;
	}

	/**
	 * 处理任务列表中的某个可接任务模版提示字符串
	 * 
	 * @param mission
	 * @return
	 */
	private String getMissionListTips(KRole role,
			KMissionTemplate missionTemplate) {
		MissionCompleteCondition condition = missionTemplate.missionCompleteCondition;
		String tips = "";
		if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_KILL_MONSTER) {
			tips += MissionTips.getTipsKillMonsterTypeTips();
			for (KillMonsterTask task : condition.getKillMonsterTaskMap()
					.values()) {
				String monsterName = (task.isAnyTypeMonster) ? ((task.isMonsterLevelLimit) ? MissionTips
						.getTipsKillMonsterTypeMonsterName() : MissionTips
						.getTipsKillMonsterTypeMonsterName1(task.monsterLevel))
						: (task.monsterTemplate.name);
				int targetCount = task.killCount;
				tips += MissionTips.getTipsKillMonsterTypeMonsterInfo(
						monsterName, targetCount);
			}
		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_DIALOG) {
			tips += missionTemplate.missionTips;
		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_COLLECT_ITEMS) {
			tips += MissionTips.getTipsCollectItemTypeTips();
			CollectItemTask task = condition.getCollectItemTask();
			String itemName;
			if (task.isLimitJob) {
				itemName = task.itemTemplateMap.get(role.getJob()).extItemName;
			} else {
				itemName = task.itemTemplate.extItemName;
			}
			int targetCount = task.collectCount;
			tips += MissionTips.getTipsCollectItemTypeItemInfo(itemName,
					targetCount);

		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_GAME_LEVEL) {
			GameLevelTask task = condition.getGameLevelTask();
			int targetCount = task.completeCount;
			if (task.isLevelTypeAny()) {
				tips += MissionTips
						.getTipsCompleteLevelTypeInfo(task.completeCount);
			} else {
				String levelName = KGameLevelModuleExtension.getManager()
						.getKGameLevel(task.levelId).getLevelName();
				tips += MissionTips.getTipsCompleteLevelTypeInfo1(levelName,
						task.completeCount);
			}
		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_FUNCTION) {
			UseFunctionTask task = condition.getUseFunctionTask();
			int targetCount = task.useCount;

			String funTips = "";

			// funTips = FunctionTypeEnum.getEnum(task.functionId).functionTips;
			funTips = MissionTips.getTipsUseFunTypeTips(KGuideManager
					.getMainMenuFunctionInfoMap().get(task.functionId)
					.getFunctionName());
			tips += MissionTips.getTipsUseFunTypeInfo(funTips, targetCount);

		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_ITEM) {
			UseItemTask task = condition.getUseItemTask();
			tips += MissionTips.getTipsUseItemTypeInfo(task.itemTemplate.name);
		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_QUESTION) {
			AnswerQuestionTask task = condition.getAnswerQuestionTask();
			int targetCount = task.getTotalQuestionCount();

			tips += MissionTips.getTipsQuestionTypeInfo(targetCount);
		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_UP_FUNC_LV) {
			tips += missionTemplate.missionTips;
		}
		return tips;
	}

	/**
	 * 根据任务模版ID，处理角色接受一个任务的流程
	 * 
	 * @param role
	 * @param missionTemplateId
	 * @return
	 */
	public KMission playerRoleAcceptMission(KRole role, int missionTemplateId) {
		// 如果从任务模版表中找不到该模版ID，返回null
		if (!this.allMissionTemplates.containsKey(missionTemplateId)) {
			return null;
		}

		KMissionTemplate missionTemplate = this.allMissionTemplates
				.get(missionTemplateId);

		if (!missionTemplate.isMissionCanAccept(role)) {

			// KDialogService
			// .sendSimpleDialog(
			// role,
			// "",
			// "您的角色等级需要达到"
			// + missionTemplate
			// .getMissionTriggerCondition().roleLevelLimit
			// + "级才能接受该任务。");
			KDialogService
					.sendSimpleDialog(
							role,
							GlobalTips.getTipsDefaultTitle(),
							MissionTips
									.getTipsCanNotAcceptMissionByLowLv(missionTemplate.missionTriggerCondition.roleLevelLimit));
			return null;
		}

		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role
				.getId());

		if (missionSet == null) {
			return null;
		}
		// 接受任务，并从任务容器中增加这个任务，得到一个任务数据对象实例
		KMission mission = missionSet.receiveNewMission(role, missionTemplate);

		return mission;
	}

	/**
	 * 角色提交任务，在提交时需检测任务是否完成，如果完成则进行相关完成任务处理
	 * 
	 * @param role
	 * @param missionTemplateId
	 * @return
	 */
	public int playerRoleSubmitMission(KRole role, int missionTemplateId) {
		if (!this.allMissionTemplates.containsKey(missionTemplateId)) {
			return SUBMIT_MISSION_RESULT_FAILD;
		}
		KMissionTemplate missionTemplate = this.allMissionTemplates
				.get(missionTemplateId);

		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role
				.getId());
		if (missionSet == null) {
			return SUBMIT_MISSION_RESULT_FAILD;
		}

		KMission mission = missionSet.getUnclosedMission(missionTemplateId);
		if (mission == null) {
			return SUBMIT_MISSION_RESULT_FAILD;
		}
		// 检测任务状态是否为完成可提交状态KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT
		if (mission.getMissionStatus() == KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT) {
			// 如果该任务有道具奖励，需要检测背包容量
			if (missionTemplate.missionReward.getAllMissionItemRewardTemplate()
					.size() > 0) {
				BaseRewardData reward = missionTemplate.missionReward
						.getBaseRewardData(role.getJob());
				if (!KSupportFactory.getItemModuleSupport().isCanAddItemsToBag(
						role.getId(), reward.itemStructs)) {
					// TODO 任务完成提交时背包已满，发送提示消息
					processMissionStatusChangeEffect(role, missionTemplate,
							missionTemplate.MISSION_EFFECT_TYPE_SUBMIT_FAILD);
					KDialogService.sendUprisingDialog(role,
							LevelTips.getTipsBagCapacityNotEnough());

					return SUBMIT_MISSION_RESULT_BAG_FULL;
				}
			}

			// 在角色任务容器中处理这个完成任务
			boolean completeResult = missionSet.completeMission(mission);
			// 如果处理成功，则进行任务奖励结算
			if (completeResult) {
				processMissionReward(role, missionTemplate);

				// 记录任务完成流水
				FlowDataModuleFactory.getModule().recordMissionCompleted(
						missionTemplate.missionTemplateId);
				return SUBMIT_MISSION_RESULT_SUCCESS;
			}
		}

		return SUBMIT_MISSION_RESULT_FAILD;
	}

	/**
	 * 角色放弃任务
	 * 
	 * @param role
	 * @param missionTemplateId
	 */
	public boolean playerRoleDropMission(KRole role, int missionTemplateId) {
		if (!this.allMissionTemplates.containsKey(missionTemplateId)) {
			return false;
		}
		KMissionTemplate missionTemplate = this.allMissionTemplates
				.get(missionTemplateId);

		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role
				.getId());
		if (missionSet == null) {
			return false;
		}

		if (missionSet.dropMission(missionTemplate)) {
			// 放弃成功，此任务变成可接状态
			KGameNormalMap currentMap = KMapModule.getGameMapManager()
					.getGameMap(role.getRoleMapData().getCurrentMapId());
			int acceptMissionNpcId = missionTemplate.acceptMissionNPCTemplate.templateId;
			// 如果此可接任务的接受NPC与角色在同一地图，则需要通知更新NPC菜单
			if (currentMap != null
					|| currentMap.isNpcEntityInMap(acceptMissionNpcId)) {
				IMissionMenuImpl menu = KMissionModuleSupportImpl
						.constructIMissionMenu(
								role,
								missionTemplate,
								missionTemplate
										.getMissionNameByStatusType(
												KGameMissionStatusEnum.MISSION_STATUS_TRYRECEIVE,
												role),
								KGameMissionStatusEnum.MISSION_STATUS_TRYRECEIVE.statusType,
								IMissionMenu.ACTION_AFTER_TALK_SUBMIT,
								missionTemplate.getPrologueMissionDialog(),
								missionTemplate.getAcceptMissionDialog(role));

				// Dialogue dialogue = missionTemplate.acceptMissionTipsDialog
				// .getDialogueList().get(0);
				// menu.addMissionConversation(new IMissionConversationImpl(
				// dialogue.getContent(role.getJob()), dialogue
				// .getQuestion()));
				List<IMissionMenu> menuList = new ArrayList<IMissionMenu>();
				menuList.add(menu);
				KMenuService.synNpcAddOrUpdateMenus(role, acceptMissionNpcId,
						menuList);
			}

			if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_DIALOG
					&& acceptMissionNpcId != missionTemplate.submitMissionNPCTemplate.templateId) {
				if (currentMap != null
						|| currentMap
								.isNpcEntityInMap(missionTemplate.submitMissionNPCTemplate.templateId)) {
					KMenuService
							.synNpcDeleteMenus(
									role,
									missionTemplate.submitMissionNPCTemplate.templateId,
									missionTemplate.missionTemplateId);
				}
			}

			// 更新任务列表
			processUpdateMissionListWhileDropMission(role, missionTemplate);

			return true;
		}

		return false;
	}

	/**
	 * 检测并获取该完成任务模版中的完成本任务触发新的可接任务模版列表
	 * 
	 * @param role
	 * @param completedMissionTemplateId
	 * @return
	 */
	public List<KMissionTemplate> getNewAcceptableMissionTemplateWhileMissionCompleted(
			KRole role, int completedMissionTemplateId) {
		List<KMissionTemplate> newAcceptableMissionTemplateList = new ArrayList<KMissionTemplate>();
		if (!this.allMissionTemplates.containsKey(completedMissionTemplateId)) {
			return newAcceptableMissionTemplateList;
		}
		KMissionTemplate completedMissionTemplate = this.allMissionTemplates
				.get(completedMissionTemplateId);

		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role
				.getId());
		KMissionCompleteRecordSet completeRecordSet = KMissionModuleExtension
				.getMissionCompleteRecordSet(role.getId());
		if (missionSet == null) {
			return newAcceptableMissionTemplateList;
		}

		List<KMissionTemplate> acceptableMissionTemplateList = completedMissionTemplate.nextAcceptableMissionTemplateList;

		if (acceptableMissionTemplateList != null
				&& acceptableMissionTemplateList.size() > 0) {
			for (KMissionTemplate template : acceptableMissionTemplateList) {
				if (missionSet.getAcceptableMissionTemplateMap().containsKey(
						template.missionTemplateId)) {
					continue;
				}
				// 判断该任务模版是否可接
				boolean checkCanAccept = this.checkMissionCanAccepted(template,
						role, missionSet, completeRecordSet);
				if (checkCanAccept) {
					newAcceptableMissionTemplateList.add(template);
					// 将其放入任务容器的可接任务列表
					missionSet.getAcceptableMissionTemplateMap().put(
							template.missionTemplateId, template);
				}
			}

		}

		return newAcceptableMissionTemplateList;
	}

	/**
	 * 处理完成任务的数值奖励与道具奖励
	 * 
	 * @param role
	 * @param template
	 */
	private boolean processMissionReward(KRole role, KMissionTemplate template) {
		MissionReward reward = template.missionReward;
		return template.missionReward.getBaseRewardData(role.getJob())
				.sendReward(role, PresentPointTypeEnum.普通任务奖励);
	}

	/**
	 * 处理角色完某个任务状态发生改变时的特效消息
	 * 
	 * @param role
	 * @param template
	 * @param statusType
	 */
	public void processMissionStatusChangeEffect(KRole role,
			KMissionTemplate template, byte statusType) {
		KGameMessage sendMsg = KGame
				.newLogicMessage(KMissionProtocol.SM_SEND_MISSION_STATUS_CHANGE);
		if (template.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_DIALOG
				&& statusType == KMissionTemplate.MISSION_EFFECT_TYPE_CONDITION_FINISHED) {
			statusType = KMissionTemplate.MISSION_EFFECT_TYPE_ACCEPT;
		}
		sendMsg.writeByte(statusType);

		boolean hasAnnimation = false;
		int animationId = 0;
		if (statusType == KMissionTemplate.MISSION_EFFECT_TYPE_ACCEPT) {
			if (AnimationManager.getInstance().getMissionAcceptTypeAnimations()
					.containsKey(template.missionTemplateId)) {
				animationId = AnimationManager.getInstance()
						.getMissionAcceptTypeAnimations()
						.get(template.missionTemplateId).animationResId;
				hasAnnimation = true;
			}
		} else if (statusType == KMissionTemplate.MISSION_EFFECT_TYPE_CONDITION_FINISHED
				&& template.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_DIALOG) {
			if (AnimationManager.getInstance().getMissionAcceptTypeAnimations()
					.containsKey(template.missionTemplateId)) {
				animationId = AnimationManager.getInstance()
						.getMissionAcceptTypeAnimations()
						.get(template.missionTemplateId).animationResId;
				hasAnnimation = true;
			}
		} else if (statusType == KMissionTemplate.MISSION_EFFECT_TYPE_SUBMIT_SUCCEED) {
			if (AnimationManager.getInstance().getMissionSubmitTypeAnimations()
					.containsKey(template.missionTemplateId)) {
				animationId = AnimationManager.getInstance()
						.getMissionSubmitTypeAnimations()
						.get(template.missionTemplateId).animationResId;
				hasAnnimation = true;
			}
		}

		sendMsg.writeBoolean(hasAnnimation);
		if (hasAnnimation) {
			sendMsg.writeInt(animationId);
		}

		role.sendMsg(sendMsg);

		if (statusType == KMissionTemplate.MISSION_EFFECT_TYPE_SUBMIT_SUCCEED) {
			processSendMissionRewardTips(role, template.missionReward);
		} else if (statusType == KMissionTemplate.MISSION_EFFECT_TYPE_CONDITION_FINISHED) {
			if (template.isMainLineMission) {
				KDialogService
						.sendUprisingDialog(
								role,
								MissionTips
										.getTipsMainLineMissionConditionComplete(template.missionExtName));
			} else {
				KDialogService
						.sendUprisingDialog(
								role,
								MissionTips
										.getTipsBranchLineMissionConditionComplete(template.missionExtName));
			}
		}

	}

	/**
	 * 发送任务奖励提示（头顶冒字）
	 * 
	 * @param role
	 * @param reward
	 */
	public void processSendMissionRewardTips(KRole role, MissionReward reward) {
		BaseRewardData baseRw = reward.getBaseRewardData(role.getJob());
		List<String> tipsList = new ArrayList<String>();
		for (AttValueStruct struct : baseRw.attList) {
			tipsList.add(StringUtil.format(ShopTips.x加x,
					struct.roleAttType.getExtName(), struct.addValue));
		}
		for (KCurrencyCountStruct struct : baseRw.moneyList) {
			tipsList.add(StringUtil.format(ShopTips.x加x,
					struct.currencyType.extName, struct.currencyCount));
		}
		for (ItemCountStruct struct : baseRw.itemStructs) {
			tipsList.add(StringUtil.format(ShopTips.x加x,
					struct.getItemTemplate().extItemName, struct.itemCount));
		}
		KDialogService.sendDataUprisingDialog(role, tipsList);
	}

	/**
	 * 处理当角色接受某个新任务时的任务列表状态更新
	 * 
	 * @param role
	 * @param acceptedMission
	 */
	public void processUpdateMissionListWhileAccepteNewMission(KRole role,
			KMission acceptedMission) {
		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role
				.getId());
		KGameMessage sendMsg = KGame
				.newLogicMessage(KMissionProtocol.SM_UPDATE_MISSION_LIST);

		MainUISearchRoadSturct struct = getMainUISearchRoadMessage(role,
				missionSet, sendMsg);
		// 更新已接任务
		sendMsg.writeInt(1);

		KMissionTemplate missionTemplate = acceptedMission.getMissionTemplate();
		sendMsg.writeInt(missionTemplate.missionTemplateId);
		sendMsg.writeUtf8String(missionTemplate.getMissionNameByStatusType(
				acceptedMission.getMissionStatus(), role));
		sendMsg.writeUtf8String(getMissionListTips(role, acceptedMission));
		sendMsg.writeByte(acceptedMission.getMissionStatus().statusType);
		setSearchRoadDataInMessage(role, acceptedMission, sendMsg, false);

		sendMsg.writeInt(0);// 不用更新删除的任务
		sendMsg.writeInt(0);// 不用更新添加的可接任务
		sendMsg.writeInt(0);// 没有更新开放的可接任务

		role.sendMsg(sendMsg);

		// TODO 判断是否有通过引导任务开放的功能，并处理功能开启消息
		// processOpenNewFunctionByNoviceGuideMission(role,
		// acceptedMission.getMissionTemplate().missionTemplateId);

		// 判断是否有新手引导并处理消息
		processNoviceGuideMessage(role, acceptedMission);
	}

	/**
	 * 处理当角色完成并提交某个任务时的任务列表状态更新
	 * 
	 * @param role
	 * @param completedMission
	 * @param acceptableMissionTemplateList
	 */
	public void processUpdateMissionListWhileCompleteMission(KRole role,
			KMissionTemplate completedMission,
			List<KMissionTemplate> acceptableMissionTemplateList) {

		// 新的可接受主线任务模版
		KMissionTemplate mainLineMissionTemplate = null;

		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role
				.getId());
		KGameMessage sendMsg = KGame
				.newLogicMessage(KMissionProtocol.SM_UPDATE_MISSION_LIST);

		MainUISearchRoadSturct searchRoadstruct = getMainUISearchRoadMessage(
				role, missionSet, sendMsg);

		sendMsg.writeInt(0);// 不用更新已接任务
		// 通知删除该笔任务
		sendMsg.writeInt(1);
		sendMsg.writeInt(completedMission.missionTemplateId);

		sendMsg.writeInt(acceptableMissionTemplateList.size());
		for (KMissionTemplate missionTemplate : acceptableMissionTemplateList) {
			if (missionTemplate.missionType == KGameMissionTemplateTypeEnum.MISSION_TYPE_MAIN_LINE) {
				mainLineMissionTemplate = missionTemplate;
			}
			MissionCompleteCondition condition = missionTemplate.missionCompleteCondition;

			sendMsg.writeInt(missionTemplate.missionTemplateId);
			sendMsg.writeUtf8String(missionTemplate.getMissionNameByStatusType(
					KGameMissionStatusEnum.MISSION_STATUS_TRYRECEIVE, role));
			sendMsg.writeUtf8String(HyperTextTool.extColor(
					missionTemplate.desc, 0));
			sendMsg.writeUtf8String(missionTemplate.acceptMissionNPCTemplate.name);
			sendMsg.writeUtf8String(missionTemplate.submitMissionNPCTemplate.name);
			sendMsg.writeUtf8String(getMissionListTips(role, missionTemplate));
			MissionReward reward = missionTemplate.missionReward;

			reward.getBaseRewardData(role.getJob()).packMsg(sendMsg);

			sendMsg.writeByte(missionTemplate.missionType.missionType);
			sendMsg.writeByte(KGameMissionStatusEnum.MISSION_STATUS_TRYRECEIVE.statusType);
			sendMsg.writeBoolean(missionTemplate.isMissionCanAccept(role));
			setSearchRoadDataInMessage(role, missionTemplate, sendMsg, false,
					KMissionSearchRoadTypeEnum.SEARCH_ROAD_TYPE_NPC);
		}

		sendMsg.writeInt(0);// 没有更新开放的可接任务

		role.sendMsg(sendMsg);

		// 判断是否有新手引导并处理消息
		if (completedMission.isMainLineMission
				&& completedMission.isNewPlayerGuildMission) {
			// // 先判断该完成任务是否新手引导任务，如果是通知引导结束
			// if (mainLineMissionTemplate == null
			// || !mainLineMissionTemplate.isNewPlayerGuildMission) {
			boolean isCloseGuide = true;
			for (KMissionTemplate nextMissionTemp : completedMission.nextAcceptableMissionTemplateList) {
				if (nextMissionTemp.isMainLineMission
						&& nextMissionTemp.isNewPlayerGuildMission) {
					isCloseGuide = false;
					break;
				}
			}
			if (isCloseGuide) {
				processNoviceGuideFinishMessage(role,
						completedMission.missionTemplateId);
			}
			// }
		}
		if (mainLineMissionTemplate != null) {
			processNoviceGuideMessage(role, mainLineMissionTemplate);
		}

	}

	/**
	 * 处理当角色放弃某个任务时的任务列表状态更新
	 * 
	 * @param role
	 * @param dropMissionTemplate
	 */
	public void processUpdateMissionListWhileDropMission(KRole role,
			KMissionTemplate dropMissionTemplate) {
		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role
				.getId());
		KGameMessage sendMsg = KGame
				.newLogicMessage(KMissionProtocol.SM_UPDATE_MISSION_LIST);

		MainUISearchRoadSturct struct = getMainUISearchRoadMessage(role,
				missionSet, sendMsg);
		// 更新已接任务
		sendMsg.writeInt(1);

		sendMsg.writeInt(dropMissionTemplate.missionTemplateId);
		sendMsg.writeUtf8String(dropMissionTemplate.getMissionNameByStatusType(
				KGameMissionStatusEnum.MISSION_STATUS_TRYRECEIVE, role));
		sendMsg.writeUtf8String(getMissionListTips(role, dropMissionTemplate));
		sendMsg.writeByte(KGameMissionStatusEnum.MISSION_STATUS_TRYRECEIVE.statusType);
		setSearchRoadDataInMessage(role, dropMissionTemplate, sendMsg, false,
				KMissionSearchRoadTypeEnum.SEARCH_ROAD_TYPE_NPC);

		sendMsg.writeInt(0);// 不用更新删除的任务
		sendMsg.writeInt(0);// 不用更新添加的可接任务
		sendMsg.writeInt(0);// 没有更新开放的可接任务

		role.sendMsg(sendMsg);

		// 判断是否有新手引导并处理消息
		processNoviceGuideMessage(role, dropMissionTemplate);

	}

	/**
	 * 处理当角色已接受某个任务的状态发生改变时的任务列表状态更新
	 * 
	 * @param role
	 * @param acceptedMission
	 */
	public void processUpdateMissionListWhileAcceptedMissionStatusChanged(
			KRole role, KMission acceptedMission) {
		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role
				.getId());
		KGameMessage sendMsg = KGame
				.newLogicMessage(KMissionProtocol.SM_UPDATE_MISSION_LIST);

		MainUISearchRoadSturct struct = getMainUISearchRoadMessage(role,
				missionSet, sendMsg);
		// 更新已接任务
		sendMsg.writeInt(1);

		KMissionTemplate missionTemplate = acceptedMission.getMissionTemplate();
		sendMsg.writeInt(missionTemplate.missionTemplateId);
		sendMsg.writeUtf8String(missionTemplate.getMissionNameByStatusType(
				acceptedMission.getMissionStatus(), role));
		sendMsg.writeUtf8String(getMissionListTips(role, acceptedMission));
		sendMsg.writeByte(acceptedMission.getMissionStatus().statusType);
		setSearchRoadDataInMessage(role, acceptedMission, sendMsg, false);

		sendMsg.writeInt(0);// 不用更新删除的任务
		sendMsg.writeInt(0);// 不用更新添加的可接任务
		sendMsg.writeInt(0);// 没有更新开放的可接任务

		role.sendMsg(sendMsg);

		// 判断是否有新手引导并处理消息
		processNoviceGuideMessage(role, acceptedMission);

	}

	public void processUpdateMissionListWhileTriggerOpenAcceptableMissions(
			KRole role, List<KMissionTemplate> openMissionTemplateList) {
		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role
				.getId());
		KGameMessage sendMsg = KGame
				.newLogicMessage(KMissionProtocol.SM_UPDATE_MISSION_LIST);

		// 新的可接受主线任务模版
		KMissionTemplate mainLineMissionTemplate = null;

		MainUISearchRoadSturct struct = getMainUISearchRoadMessage(role,
				missionSet, sendMsg);
		sendMsg.writeInt(0);// 不用更新已接任务
		// 不用删除该笔任务
		sendMsg.writeInt(0);
		// 不用增加新任务
		sendMsg.writeInt(0);

		sendMsg.writeInt(openMissionTemplateList.size());
		for (KMissionTemplate template : openMissionTemplateList) {
			if (template.missionType == KGameMissionTemplateTypeEnum.MISSION_TYPE_MAIN_LINE) {
				mainLineMissionTemplate = template;
			}
			sendMsg.writeInt(template.missionTemplateId);
			sendMsg.writeUtf8String(template.getMissionNameByStatusType(
					KGameMissionStatusEnum.MISSION_STATUS_TRYRECEIVE, role));
		}
		role.sendMsg(sendMsg);

		// 判断是否有新手引导并处理消息
		if (mainLineMissionTemplate != null) {
			processNoviceGuideMessage(role, mainLineMissionTemplate);
		}
	}

	/**
	 * 处理当角色完成并提交某个任务时的任务列表状态更新
	 * 
	 * @param role
	 * @param completedMission
	 * @param acceptableMissionTemplateList
	 */
	public void processUpdateMissionListWhileTriggerNewMissions(KRole role,
			List<KMissionTemplate> acceptableMissionTemplateList) {
		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role
				.getId());
		KGameMessage sendMsg = KGame
				.newLogicMessage(KMissionProtocol.SM_UPDATE_MISSION_LIST);

		MainUISearchRoadSturct searchRoadstruct = getMainUISearchRoadMessage(
				role, missionSet, sendMsg);

		// 新的可接受主线任务模版
		KMissionTemplate mainLineMissionTemplate = null;

		sendMsg.writeInt(0);// 不用更新已接任务
		// 通知删除该笔任务
		sendMsg.writeInt(0);

		sendMsg.writeInt(acceptableMissionTemplateList.size());
		for (KMissionTemplate missionTemplate : acceptableMissionTemplateList) {
			if (missionTemplate.missionType == KGameMissionTemplateTypeEnum.MISSION_TYPE_MAIN_LINE) {
				mainLineMissionTemplate = missionTemplate;
			}
			MissionCompleteCondition condition = missionTemplate.missionCompleteCondition;

			sendMsg.writeInt(missionTemplate.missionTemplateId);
			sendMsg.writeUtf8String(missionTemplate.getMissionNameByStatusType(
					KGameMissionStatusEnum.MISSION_STATUS_TRYRECEIVE, role));
			sendMsg.writeUtf8String(HyperTextTool.extColor(
					missionTemplate.desc, 0));
			sendMsg.writeUtf8String(missionTemplate.acceptMissionNPCTemplate.name);
			sendMsg.writeUtf8String(missionTemplate.submitMissionNPCTemplate.name);
			sendMsg.writeUtf8String(getMissionListTips(role, missionTemplate));
			MissionReward reward = missionTemplate.missionReward;

			reward.getBaseRewardData(role.getJob()).packMsg(sendMsg);

			sendMsg.writeByte(missionTemplate.missionType.missionType);
			sendMsg.writeByte(KGameMissionStatusEnum.MISSION_STATUS_TRYRECEIVE.statusType);
			sendMsg.writeBoolean(missionTemplate.isMissionCanAccept(role));
			setSearchRoadDataInMessage(role, missionTemplate, sendMsg, false,
					KMissionSearchRoadTypeEnum.SEARCH_ROAD_TYPE_NPC);
		}

		sendMsg.writeInt(0);// 没有更新开放的可接任务

		role.sendMsg(sendMsg);

		// 判断是否有新手引导并处理消息
		if (mainLineMissionTemplate != null) {
			processNoviceGuideMessage(role, mainLineMissionTemplate);
		}

	}

	/**
	 * 获取已接任务进度提示字符串
	 * 
	 * @param mission
	 * @return
	 */
	public String getMissionProgressTips(KRole role, KMission mission) {
		KMissionTemplate missionTemplate = mission.getMissionTemplate();
		MissionCompleteCondition condition = missionTemplate.missionCompleteCondition;
		String mtips = MissionTips
				.getTipsMissionTitle(missionTemplate.missionName);

		// String atips = "";
		String tips = "";
		if (mission.getMissionStatus() == KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH) {
			if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_KILL_MONSTER) {
				tips += MissionTips.getTipsKillMonsterTypeTips();
				for (KillMonsterTask task : condition.getKillMonsterTaskMap()
						.values()) {
					KillMonsterRecord record = mission
							.getKillMonsterRecordMap()
							.get(task.isAnyTypeMonster ? KillMonsterTask.ANY_TYPE_MONSTER_ID
									: task.monsterTemplate.id);
					String monsterName = (task.isAnyTypeMonster) ? ((task.isMonsterLevelLimit) ? MissionTips
							.getTipsKillMonsterTypeMonsterName()
							: MissionTips
									.getTipsKillMonsterTypeMonsterName1(task.monsterLevel))
							: (task.monsterTemplate.name);
					int targetCount = task.killCount;
					int nowCount = record.killCount;
					if (nowCount > targetCount) {
						nowCount = targetCount;
					}
					tips += MissionTips.getTipsKillMonsterTypeMonsterInfo(
							monsterName, targetCount)
							+ MissionTips.getTipsMissionTipsCompleteStatus(
									nowCount, targetCount,
									(nowCount < targetCount) ? 1 : 6);
				}
			} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_COLLECT_ITEMS) {
				// atips += getHyperText("任务目标：", 12);
				tips += MissionTips.getTipsCollectItemTypeTips();
				CollectItemTask task = condition.getCollectItemTask();
				String itemName;
				String itemCode;
				if (task.isLimitJob) {
					itemName = task.itemTemplateMap.get(role.getJob()).extItemName;
					itemCode = task.itemTemplateMap.get(role.getJob()).itemCode;
				} else {
					itemName = task.itemTemplate.extItemName;
					itemCode = task.itemTemplate.itemCode;
				}
				int targetCount = task.collectCount;
				int nowCount = (int)KSupportFactory.getItemModuleSupport()
						.checkItemCountInBag(mission.getRoleId(), itemCode);
				if (nowCount > targetCount) {
					nowCount = targetCount;
				}
				tips += MissionTips.getTipsCollectItemTypeItemInfo(itemName,
						targetCount)
						+ MissionTips.getTipsMissionTipsCompleteStatus(
								nowCount, targetCount,
								(nowCount < targetCount) ? 1 : 6);

			} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_GAME_LEVEL) {
				// atips += getHyperText("任务目标：", 12);
				CompletedGameLevelRecord record = mission
						.getCompletedGameLevelRecord();
				GameLevelTask task = condition.getGameLevelTask();
				int targetCount = task.completeCount;
				int nowCount = record.completeCount;
				if (nowCount > targetCount) {
					nowCount = targetCount;
				}

				if (task.isLevelTypeAny()) {
					tips += MissionTips
							.getTipsCompleteLevelTypeInfo(task.completeCount);
				} else {
					String levelName = KGameLevelModuleExtension.getManager()
							.getKGameLevel(task.levelId).getLevelName();
					tips += MissionTips.getTipsCompleteLevelTypeInfo1(
							levelName, task.completeCount);
				}
				tips += MissionTips.getTipsMissionTipsCompleteStatus(nowCount,
						targetCount, (nowCount < targetCount) ? 1 : 6);
			} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_FUNCTION) {
				UseFunctionRecord record = mission.getUseFunctionRecord();
				UseFunctionTask task = condition.getUseFunctionTask();
				int targetCount = task.useCount;
				int nowCount = record.completeCount;
				if (nowCount > targetCount) {
					nowCount = targetCount;
				}
				String funTips = "";

				// funTips =
				// FunctionTypeEnum.getEnum(task.functionId).functionTips;
				funTips = MissionTips.getTipsUseFunTypeTips(KGuideManager
						.getMainMenuFunctionInfoMap().get(task.functionId)
						.getFunctionName());
				tips += MissionTips.getTipsUseFunTypeInfo(funTips, targetCount)
						+ MissionTips.getTipsMissionTipsCompleteStatus(
								nowCount, targetCount,
								(nowCount < targetCount) ? 1 : 6);

			} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_ITEM) {
				UseItemTask task = condition.getUseItemTask();
				tips += MissionTips
						.getTipsUseItemTypeInfo(task.itemTemplate.name);
			} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_QUESTION) {
				AnswerQuestionRecord record = mission.getAnswerQuestionRecord();
				AnswerQuestionTask task = condition.getAnswerQuestionTask();
				int targetCount = task.getTotalQuestionCount();
				int nowCount = record.completeCount;
				if (nowCount > targetCount) {
					nowCount = targetCount;
				}
				tips += MissionTips.getTipsQuestionTypeInfo(targetCount)
						+ MissionTips.getTipsMissionTipsCompleteStatus(
								nowCount, targetCount,
								(nowCount < targetCount) ? 1 : 6);

			} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_UP_FUNC_LV) {
				tips += missionTemplate.missionTips;
			}
		} else if (mission.getMissionStatus() == KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT) {
			tips += MissionTips.getTipsTalkToNpcTypeInfo(mission
					.getMissionTemplate().submitMissionNPCTemplate.name);
		}
		return mtips + tips;
	}

	/**
	 * 处理角色自动寻路
	 * 
	 * @param role
	 */
	public void processPlayerRoleSearchRoad(KRole role, byte searchRoadType,
			String targetId, int missionTemplateId) {
		KMissionSearchRoadTypeEnum type = KMissionSearchRoadTypeEnum
				.getEnum(searchRoadType);

		AutoSearchRoadTrack track = null;

		switch (type) {
		case SEARCH_ROAD_TYPE_NPC:
			try {
				int npcTemplateId = Integer.parseInt(targetId);
				track = KSupportFactory.getMapSupport().autoDirectToNpc(role,
						npcTemplateId);
			} catch (NumberFormatException e) {				
			}
			break;
		case SEARCH_ROAD_TYPE_LEVEL:
			try {
				if (Integer.parseInt(targetId) > 0) {
					int levelId = Integer.parseInt(targetId);
					int sccnarioId = KGameLevelModuleExtension.getManager()
							.getKGameLevel(levelId).getScenarioId();
					track = KSupportFactory.getMapSupport().autoDirectToGameLevel(
							role, sccnarioId);
				} else if (Integer.parseInt(targetId) == GameLevelTask.ANY_TYPE_LEVEL) {
					KGameLevelSet levelSet = KGameLevelModuleExtension
							.getGameLevelSet(role.getId());
					if (levelSet != null) {
						int levelId = levelSet.getMaxCompleteNormalLevelId();
						int sccnarioId = KGameLevelModuleExtension.getManager()
								.getKGameLevel(levelId).getScenarioId();
						track = KSupportFactory.getMapSupport()
								.autoDirectToGameLevel(role, sccnarioId);
					}
				}
			} catch (NumberFormatException e) {
			}
			break;
		case SEARCH_ROAD_TYPE_QUESTION:
			// int mTemplateId = Integer.parseInt(targetId);
			// track = new AutoSearchRoadTrack();
			// this.dailyMissionManager.processQuestionMissionDialog(role,
			// mTemplateId);
		default:
			break;
		}

		if (track != null) {
			KGameMessage sendMsg = KGame
					.newLogicMessage(KMissionProtocol.SM_AUTO_SEARCH_ROAD);
			sendMsg.writeInt(track.getRoadPathStack().size());
			while (!track.getRoadPathStack().isEmpty()) {
				RoadPath path = track.getRoadPathStack().pop();
				_LOGGER.debug("#########  processPlayerRoleSearchRoad:::PATH:"
						+ path.pathType + ",,,targetId:" + path.targetId
						+ "  trackSize:" + track.getRoadPathStack().size());
				sendMsg.writeByte(path.pathType);
				sendMsg.writeInt(path.targetId);

				if (path.pathType == RoadPath.PATH_TYPE_WALK_TO_EXITS) {
					if (track.getRoadPathStack().isEmpty()) {
						int levelId = Integer.parseInt(targetId);
						sendMsg.writeBoolean(true);
						sendMsg.writeInt(levelId);
					} else {
						sendMsg.writeBoolean(false);
					}
				}
			}
			role.sendMsg(sendMsg);

			// 判断任务容器中当前角色正在操作的任务模版Id，如果不等于missionTemplateId，则将其设为missionTemplateId的值
			KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role
					.getId());
			if (missionSet.currentTargetMissionTemplateId != missionTemplateId
					&& missionTemplateId != -1) {
				missionSet.currentTargetMissionTemplateId = missionTemplateId;
			}
		}
	}

	private void processNoviceGuideMessage(KRole role,
			KMissionTemplate missionTemplate) {
		if (missionTemplate.isNewPlayerGuildMission
				&& missionTemplate.isMissionCanAccept(role)) {
			KGameMessage sendMsg = KGame
					.newLogicMessage(KMissionProtocol.SM_SEND_NOVICE_GUIDE_NPC);
			sendMsg.writeInt(missionTemplate.acceptMissionNPCTemplate.templateId);
			sendMsg.writeInt(missionTemplate.missionTemplateId);
			role.sendMsg(sendMsg);
		}
	}

	private void processNoviceGuideMessage(KRole role, KMission mission) {
		KMissionTemplate missionTemplate = mission.getMissionTemplate();
		if (missionTemplate.isNewPlayerGuildMission) {
			if (mission.getMissionStatus() == KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH) {
				if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_GAME_LEVEL
						|| missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_KILL_MONSTER
						|| missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_COLLECT_ITEMS) {
					KGameMessage sendMsg = KGame
							.newLogicMessage(KMissionProtocol.SM_SEND_NOVICE_GUIDE_LEVEL);
					sendMsg.writeInt(missionTemplate.missionCompleteCondition.completedTargetId);
					role.sendMsg(sendMsg);
				} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_FUNCTION) {
					KGameMessage sendMsg = KGame
							.newLogicMessage(KMissionProtocol.SM_SEND_NOVICE_GUIDE_FUNCTION);
					// sendMsg.writeShort(missionTemplate
					// .getMissionCompleteCondition().getUseFunctionTask().functionId);
					sendMsg.writeShort(missionTemplate.missionCompleteCondition
							.getUseFunctionTask().functionId);
					role.sendMsg(sendMsg);
				} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_BATTLEFIELD) {
					KGameMessage sendMsg = KGame
							.newLogicMessage(KMissionProtocol.SM_SEND_NOVICE_GUIDE_NPC);
					sendMsg.writeInt(missionTemplate.acceptMissionNPCTemplate.templateId);
					sendMsg.writeInt(missionTemplate.missionTemplateId);
					role.sendMsg(sendMsg);
				}
			} else if (mission.getMissionStatus() == KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT) {
				KGameMessage sendMsg = KGame
						.newLogicMessage(KMissionProtocol.SM_SEND_NOVICE_GUIDE_NPC);
				sendMsg.writeInt(missionTemplate.submitMissionNPCTemplate.templateId);
				sendMsg.writeInt(missionTemplate.missionTemplateId);
				role.sendMsg(sendMsg);
			}
		}
	}

	/**
	 * 处理通知客户端新手引导流程结束消息
	 * 
	 * @param role
	 */
	public void processNoviceGuideFinishMessage(KRole role,
			int missionTemplateId) {
		KMissionTemplate missionTemplate = getMissionTemplate(missionTemplateId);

		// if (missionTemplate != null &&
		// missionTemplate.isNewPlayerGuildMission) {
		KGameMessage sendMsg = KGame
				.newLogicMessage(KMissionProtocol.SM_SEND_NOVICE_GUIDE_CLOSE);
		role.sendMsg(sendMsg);
		// }
	}

	/**
	 * 处理当角色升级时，查找角色是否触发对应角色等级新的可接任务
	 * 
	 * @param role
	 * @return
	 */
	public List<KMissionTemplate> searchNewAcceptableMissionWhileRoleLevelUp(
			KRole role, int preLevel, int nowLevel) {

		List<KMissionTemplate> newAcceptableMissionList = new ArrayList<KMissionTemplate>();

		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role
				.getId());
		KMissionCompleteRecordSet completeRecordSet = KMissionModuleExtension
				.getMissionCompleteRecordSet(role.getId());

		for (KMissionTemplate missionTemplate : this.allMissionTemplates
				.values()) {
			// 如果该任务模版已存在于可接任务列表中，跳过
			if (missionSet.getAcceptableMissionTemplateMap().containsKey(
					missionTemplate.missionTemplateId)) {
				continue;
			}
			// MissionTriggerCondition condition = missionTemplate
			// .getMissionTriggerCondition();

			// if (condition.roleLevelLimit > 0) {
			// if (condition.roleLevelLimit > preLevel
			// && condition.roleLevelLimit <= nowLevel) {

			// 判断该任务是否可接
			if (checkMissionCanAccepted(missionTemplate, role, missionSet,
					completeRecordSet)) {
				newAcceptableMissionList.add(missionTemplate);
				missionSet.getAcceptableMissionTemplateMap().put(
						missionTemplate.missionTemplateId, missionTemplate);
			}
			// }
			// }
		}

		return newAcceptableMissionList;
	}

	public static class MainUISearchRoadSturct {
		/**
		 * 表示主城UI界面上的自动寻路目标类型。 1：目标类型为NPC 2：目标类型为关卡 3：目标类型为使用道具 4：目标类型为使用功能
		 */
		public KMissionSearchRoadTypeEnum searchRoadType;
		public String searchRoadTargetId; // 表示主城UI界面上的自动寻路目标ID
		public String searchRoadTargetName; // 表示主城UI界面上的自动寻路目标名称
		public KMissionTemplate missionTemplate; // 表示当前主城UI界面上的自动寻路目标所属的任务模版
		public KGameMissionStatusEnum missionStatus;// 任务状态

	}
}
