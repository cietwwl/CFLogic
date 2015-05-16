package com.kola.kmp.logic.mission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.mission.MissionCompleteCondition.AnswerQuestionTask;
import com.kola.kmp.logic.mission.MissionCompleteCondition.AttributeTask;
import com.kola.kmp.logic.mission.MissionCompleteCondition.CollectItemTask;
import com.kola.kmp.logic.mission.MissionCompleteCondition.GameLevelTask;
import com.kola.kmp.logic.mission.MissionCompleteCondition.KillMonsterTask;
import com.kola.kmp.logic.mission.MissionCompleteCondition.UpgradeFunLvTask;
import com.kola.kmp.logic.mission.MissionCompleteCondition.UseFunctionTask;
import com.kola.kmp.logic.mission.MissionCompleteCondition.UseItemTask;
import com.kola.kmp.logic.mission.MissionDialog.Dialogue;
import com.kola.kmp.logic.mission.MissionReward.MissionItemRewardTemplate;
import com.kola.kmp.logic.mission.MissionTriggerCondition.AttributeCondition;
import com.kola.kmp.logic.npc.KNPCDataStructs.KMonstTemplate;
import com.kola.kmp.logic.npc.KNPCDataStructs.KNPCTemplate;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KColorFunEnum.KColorManager;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KEquipmentTypeEnum;
import com.kola.kmp.logic.other.KFunctionTypeEnum;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KGameMissionFunTypeEnum;
import com.kola.kmp.logic.other.KGameMissionStatusEnum;
import com.kola.kmp.logic.other.KGameMissionTemplateTypeEnum;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.other.KUseFunctionTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.text.HyperTextTool;
import com.kola.kmp.logic.util.tips.MissionTips;

public class KMissionTemplate {
	/**
	 * 任务特效类型：接受任务
	 */
	public static final byte MISSION_EFFECT_TYPE_ACCEPT = 1;
	/**
	 * 任务特效类型：达到完成条件
	 */
	public static final byte MISSION_EFFECT_TYPE_CONDITION_FINISHED = 2;
	/**
	 * 任务特效类型：完成并提交成功
	 */
	public static final byte MISSION_EFFECT_TYPE_SUBMIT_SUCCEED = 3;
	/**
	 * 任务特效类型：提交失败
	 */
	public static final byte MISSION_EFFECT_TYPE_SUBMIT_FAILD = 4;

	public int missionTemplateId; // 任务名称@author CamusHuang
	public String missionName; // 任务名称@author CamusHuang
	public String missionExtName; // 任务名称(染色)

	// 任务描述
	public String desc;
	// 任务提示信息
	public String missionTips;
	// 任务类型
	public KGameMissionTemplateTypeEnum missionType;
	// 任务功能目标类型
	public KGameMissionFunTypeEnum missionFunType;

	// 是否主线任务
	public boolean isMainLineMission;
	// 是否支线任务
	public boolean isBranchLineMission;
	// 是否新手引导任务
	public boolean isNewPlayerGuildMission;
	// 是否日常任务
	public boolean isDailyMission;
	// 表示该任务可否重复领取
	public boolean canDuplicateReceive;

	// 任务触发条件
	public MissionTriggerCondition missionTriggerCondition;
	// 任务完成条件
	public MissionCompleteCondition missionCompleteCondition;

	// 任务奖励
	public MissionReward missionReward;

	// 完成本任务触发新的可接任务模版列表
	public List<KMissionTemplate> nextAcceptableMissionTemplateList = new ArrayList<KMissionTemplate>();

	/************************** 任务对话数据 *************************************/

	// 接受任务前对话（开场白）
	private MissionDialog prologueMissionDialog;
	// 接受任务后对话
	public MissionDialog acceptMissionDialog;
	// 完成任务后对话
	private MissionDialog completedMissionDialog;
	// 未完成任务后对话
	private MissionDialog uncompletedMissionDialog;
	// 接受任务时在接受任务对话后的任务提示与奖励对话
	// public MissionDialog acceptMissionTipsDialog;
	// 接受任务的NPC模版
	public KNPCTemplate acceptMissionNPCTemplate;
	// 提交任务的NPC模版
	public KNPCTemplate submitMissionNPCTemplate;

	public boolean isInitTemplateData = false;

	public boolean isInitDialogData = false;

	public void initKMissionTemplateData(KGameExcelRow missionDataRow,
			int rowIndex) throws Exception {

		try {
			// 初始化基础数据
			this.missionTemplateId = missionDataRow.getInt("missionTemplateId");
			this.missionName = missionDataRow.getData("missionName");
			this.missionExtName = HyperTextTool.extColor(missionName, 0);
			this.desc = missionDataRow.getData("desc");
			this.missionTips = missionDataRow.getData("missionTips");
			byte missionType = missionDataRow.getByte("missionType");
			if (missionType < 1 || missionType > 6) {
				throw new KGameServerException("加载任务模版表的missionType错误：不合法的属性值="
						+ missionType + "，Row="
						+ missionDataRow.getIndexInFile());
			}
			this.missionType = KGameMissionTemplateTypeEnum
					.getEnum(missionType);
			checkMissionType(this.missionType);

			this.isNewPlayerGuildMission = missionDataRow
					.getBoolean("isNoviceGuide");

			int acceptNpcId = missionDataRow.getInt("acceptNpcId");
			if (acceptNpcId == 0
					|| KSupportFactory.getNpcModuleSupport().getNPCTemplate(
							acceptNpcId) == null) {
				throw new KGameServerException(
						"加载任务模版表的acceptNpcId错误：找不到该NPC模版，值=" + acceptNpcId
								+ "，Row=" + missionDataRow.getIndexInFile());
			}
			this.acceptMissionNPCTemplate = KSupportFactory
					.getNpcModuleSupport().getNPCTemplate(acceptNpcId);

			int submitNpcId = missionDataRow.getInt("submitNpcId");
			if (submitNpcId == 0
					|| KSupportFactory.getNpcModuleSupport().getNPCTemplate(
							submitNpcId) == null) {
				throw new KGameServerException(
						"加载任务模版表的submitNpcId错误：找不到该NPC模版，值=" + submitNpcId
								+ "，Row=" + missionDataRow.getIndexInFile());
			}
			this.submitMissionNPCTemplate = KSupportFactory
					.getNpcModuleSupport().getNPCTemplate(submitNpcId);

			this.canDuplicateReceive = missionDataRow
					.getBoolean("isDuplicated");

			// 初始化任务触发条件数据
			this.missionTriggerCondition = new MissionTriggerCondition();
			this.missionTriggerCondition.roleLevelLimit = missionDataRow
					.getInt("roleLevelLimit");
			this.missionTriggerCondition.frontMissionTemplateId = missionDataRow
					.getInt("frontMissiontemplateId");
			byte occupationType = missionDataRow.getByte("occupationType");
			if (occupationType > 0) {
				if (KJobTypeEnum.getJob(occupationType) == null) {
					throw new KGameServerException(
							"加载任务模版表的occupationType错误：找不到该角色职业模版，值="
									+ occupationType + "，Row="
									+ missionDataRow.getIndexInFile());
				}
				this.missionTriggerCondition.occupationType = KJobTypeEnum
						.getJob(occupationType);
				this.missionTriggerCondition.isOccupationCondition = true;
			}
			this.missionTriggerCondition.isGangCondition = missionDataRow
					.getBoolean("isGangCondition");
			byte attributeLimitType = missionDataRow
					.getByte("attributeLimitType");
			int attributeLimitValue = missionDataRow
					.getInt("attributeLimitValue");
			if (attributeLimitType > 0) {
				if (KGameAttrType.getAttrTypeEnum(attributeLimitType) == null) {
					throw new KGameServerException(
							"加载任务模版表的attributeLimitType错误：找不到该属性限制模版，值="
									+ attributeLimitType + "，Row="
									+ missionDataRow.getIndexInFile());
				}
				AttributeCondition attCondition = new AttributeCondition(
						KGameAttrType.getAttrTypeEnum(attributeLimitType),
						attributeLimitValue);
				this.missionTriggerCondition.attributeCondition = attCondition;
				this.missionTriggerCondition.isAttributeCondition = true;
			}
			this.missionTriggerCondition.acceptLimitCount = missionDataRow
					.getInt("acceptLimitCount");
			this.missionTriggerCondition.openTimeLimitSeconds = missionDataRow
					.getInt("openTimeLimit");

			// 初始化任务完成条件
			this.missionCompleteCondition = new MissionCompleteCondition();
			byte missionTargetType = missionDataRow
					.getByte("missionTargetType");
			if (missionTargetType < 1 || missionTargetType > 10) {
				throw new KGameServerException(
						"加载任务模版表的missionTargetType错误：找不到该任务目标类型，值="
								+ missionTargetType + "，Row="
								+ missionDataRow.getIndexInFile());
			}
			this.missionFunType = KGameMissionFunTypeEnum
					.getEnum(missionTargetType);
			initCompleteCondition(this.missionCompleteCondition,
					this.missionFunType, missionDataRow, rowIndex);
			this.missionCompleteCondition.completeTimeLimitSeconds = missionDataRow
					.getInt("completeTimeLimit");

			// 初始化任务奖励
			this.missionReward = new MissionReward();
			int exp = missionDataRow.getInt("expValue");
			if (exp > 0) {
				this.missionReward.attList.add(new AttValueStruct(
						KGameAttrType.EXPERIENCE, exp, 0));
			}
			int copper = missionDataRow.getInt("coinValue");
			if (copper > 0) {
				this.missionReward.moneyList.add(new KCurrencyCountStruct(
						KCurrencyTypeEnum.GOLD, copper));
			}
			int potential = missionDataRow.getInt("facultyValue");
			if (potential > 0) {
				this.missionReward.moneyList.add(new KCurrencyCountStruct(
						KCurrencyTypeEnum.POTENTIAL, potential));
			}
			for (int i = 1; i <= 3; i++) {
				String itemCodeStr = missionDataRow.getData("rewardItemCode"
						+ i);
				if (itemCodeStr == null || itemCodeStr.length() == 0) {
					throw new KGameServerException("加载任务模版表的rewardItemCode" + i
							+ "错误：不合法的属性值=" + itemCodeStr + "，Row="
							+ missionDataRow.getIndexInFile());
				}
				if (!itemCodeStr.equals("0")) {
					String[] itemcode = itemCodeStr.split(",");
					int count = missionDataRow.getInt("rewardItemCount" + i);
					boolean isLimitOccupation = missionDataRow
							.getBoolean("itemOccLimit" + i);
					MissionItemRewardTemplate itemReward = new MissionItemRewardTemplate();
					itemReward.isLimitOccupation = isLimitOccupation;
					itemReward.rewardCount = count;
					if (itemcode == null && itemcode.length == 0) {
						throw new KGameServerException("加载任务模版表的rewardItemCode"
								+ i + "错误：不合法的属性值=" + itemCodeStr + "，Row="
								+ missionDataRow.getIndexInFile());
					} else if ((isLimitOccupation && itemcode.length != KJobTypeEnum
							.values().length)
							|| (!isLimitOccupation && itemcode.length > 1)) {
						throw new KGameServerException("加载任务模版表的rewardItemCode"
								+ i + "错误，职业限制与填写的道具不匹配：不合法的属性值=" + itemCodeStr
								+ "，Row=" + missionDataRow.getIndexInFile());
					} else {
						if (isLimitOccupation) {
							for (int j = 0; j < itemcode.length; j++) {

								String[] itemStr = itemcode[j].split("\\*");

								if (itemStr == null || itemStr.length != 2) {
									throw new KGameServerException(
											"加载任务模版表的rewardItemCode"
													+ i
													+ "错误：限制职业类型的填写格式有误,值="
													+ itemCodeStr
													+ "，Row="
													+ missionDataRow
															.getIndexInFile());
								}
								byte jobType = Byte.parseByte(itemStr[0]);
								if (KJobTypeEnum.getJob(jobType) == null) {
									throw new KGameServerException(
											"加载任务模版表的rewardItemCode"
													+ i
													+ "错误：限制职业类型的填写格式有误,值="
													+ itemCodeStr
													+ "，找不到对应的职业类型，Row="
													+ missionDataRow
															.getIndexInFile());
								}

								KItemTempAbs template = KSupportFactory
										.getItemModuleSupport()
										.getItemTemplate(itemStr[1]);

								if (template == null) {
									throw new KGameServerException(
											"加载任务模版表的rewardItemCode"
													+ i
													+ "错误：找不到对应的道具模版,值="
													+ itemCodeStr
													+ "，Row="
													+ missionDataRow
															.getIndexInFile());
								}
								itemReward.itemTemplateMapByJobType.put(
										jobType, template);
							}
						} else {
							KItemTempAbs template = KSupportFactory
									.getItemModuleSupport().getItemTemplate(
											itemcode[0]);

							if (template == null) {
								throw new KGameServerException(
										"加载任务模版表的rewardItemCode"
												+ i
												+ "错误：找不到对应的道具模版,值="
												+ itemCodeStr
												+ "，Row="
												+ missionDataRow
														.getIndexInFile());
							}
							itemReward.itemTemplate = template;
						}
						this.missionReward
								.addMissionItemRewardTemplate(itemReward);
					}
				}
			}
			String petRewardData = missionDataRow.getData("petReward");
			if (petRewardData != null && petRewardData.length() > 0
					&& !petRewardData.equals("0")) {
				String[] petRewards = petRewardData.split(",");
				if (petRewards != null && petRewards.length > 0) {
					for (int j = 0; j < petRewards.length; j++) {
						String[] petInfo = petRewards[j].split("\\*");
						if (petInfo == null || petInfo.length != 2) {
							throw new KGameServerException(
									"加载任务模版表的petReward错误：填写格式错误,值="
											+ petRewardData + "，Row="
											+ missionDataRow.getIndexInFile());
						}
						int petTempId = Integer.parseInt(petInfo[0]);
						int count = Integer.parseInt(petInfo[1]);
						this.missionReward.getPetRewardMap().put(petTempId,
								count);
					}
				}
			}
		} catch (Exception e) {
			throw new KGameServerException("加载任务模版表错误，行数Row="
					+ missionDataRow.getIndexInFile(), e);
		}

		// 初始化关卡剧情动画数据
		// byte startType = missionDataRow.getByte("animationStartType");
		// if(startType>0){
		// this.isHasMissionAnimation = true;
		// int animationId = missionDataRow.getInt("animationId");
		// this.animation = new MissionAnimation(startType, animationId);
		// }
	}

	/**
	 * 初始化任务完成条件数据
	 * 
	 * @param condition
	 * @param type
	 * @param missionDataRow
	 * @throws Exception
	 */
	private void initCompleteCondition(MissionCompleteCondition condition,
			KGameMissionFunTypeEnum type, KGameExcelRow missionDataRow,
			int rowIndex) throws Exception {
		switch (type) {
		case MISSION_FUN_TYPE_DIALOG:
			condition.isTaskTask = true;
			break;
		case MISSION_FUN_TYPE_KILL_MONSTER:
			condition.isKillMonsterTask = true;

			condition
					.setKillMonsterTaskMap(new HashMap<Integer, KillMonsterTask>());
			String monsterTemplateStr = missionDataRow
					.getData("monsterTemplateId");
			if (monsterTemplateStr == null || monsterTemplateStr.length() == 0) {
				throw new KGameServerException(
						"加载任务模版表的monsterTemplateId错误：不合法的属性值="
								+ monsterTemplateStr + "，Row="
								+ missionDataRow.getIndexInFile());
			}

			if (!monsterTemplateStr.equals("0")) {
				if ((monsterTemplateStr.toLowerCase()).equals("any")) {
					int killCount = missionDataRow.getInt("killCount");
					int monsterLevel = missionDataRow.getInt("monsterLevel");
					KillMonsterTask task = new KillMonsterTask(null, killCount,
							monsterLevel, true, monsterLevel == 0 ? false
									: true);
					condition.getKillMonsterTaskMap().put(
							KillMonsterTask.ANY_TYPE_MONSTER_ID, task);
				} else {
					String[] monsterTemplateIdStr = monsterTemplateStr
							.split(",");
					String[] killCountStr = missionDataRow.getData("killCount")
							.split(",");
					int monsterLevel = missionDataRow.getInt("monsterLevel");
					for (int i = 0; i < monsterTemplateIdStr.length; i++) {
						int monsterTemplateId = Integer
								.parseInt(monsterTemplateIdStr[i]);
						int killCount = Integer.parseInt(killCountStr[i]);
						KMonstTemplate template = KSupportFactory
								.getNpcModuleSupport().getMonstTemplate(
										monsterTemplateId);
						if (template == null) {
							throw new KGameServerException(
									"加载任务模版表的monsterTemplateId错误,找不到对应的怪物模版：不合法的属性值="
											+ monsterTemplateStr + "，Row="
											+ missionDataRow.getIndexInFile());
						} else {
							KillMonsterTask task = new KillMonsterTask(
									template, killCount, monsterLevel, false,
									false);
							condition.getKillMonsterTaskMap().put(
									task.monsterTemplate.id, task);
						}
					}
				}
			}

			condition.completedTargetId = missionDataRow
					.getInt("searchRoadGameLevelId");
			if (KGameLevelModuleExtension.getManager().getKGameLevel(
					condition.completedTargetId) == null) {
				throw new KGameServerException(
						"加载任务模版表的searchRoadGameLevelId错误：找不到对应的关卡="
								+ condition.completedTargetId + "，Row="
								+ missionDataRow.getIndexInFile());
			}

			break;
		case MISSION_FUN_TYPE_COLLECT_ITEMS:
			condition.isCollectItemTask = true;
			condition.completedTargetId = missionDataRow
					.getInt("searchRoadGameLevelId");

			if (KGameLevelModuleExtension.getManager().getKGameLevel(
					condition.completedTargetId) == null) {
				throw new KGameServerException(
						"加载任务模版表的searchRoadGameLevelId错误：找不到对应的关卡="
								+ condition.completedTargetId + "，Row="
								+ missionDataRow.getIndexInFile());
			}

			String collectItemStr = missionDataRow.getData("collectItemCode");
			int collectCount = missionDataRow.getInt("collectCount");
			if (collectItemStr == null || collectItemStr.length() == 0) {
				throw new KGameServerException(
						"加载任务模版表的collectItemCode错误：不合法的属性值=" + collectItemStr
								+ "，Row=" + missionDataRow.getIndexInFile());
			} else {

				if (collectItemStr.indexOf(",") > 0) {
					String[] itemCodeStr = collectItemStr.split(",");
					if (itemCodeStr == null || itemCodeStr.length != 3) {
						throw new KGameServerException(
								"加载任务模版表的collectItemCode错误,限制职业的道具必须有3种：不合法的属性值="
										+ collectItemStr + "，Row="
										+ missionDataRow.getIndexInFile());
					}
					Map<Byte, KItemTempAbs> map = new HashMap<Byte, KItemTempAbs>();
					for (int i = 0; i < itemCodeStr.length; i++) {
						String[] itemInfo = itemCodeStr[i].split("\\*");
						if (!itemCodeStr[i].startsWith((i + 1) + "")
								|| itemInfo == null || itemInfo.length != 2) {
							throw new KGameServerException(
									"加载任务模版表的collectItemCode错误,限制职业类型的道具格式错误：不合法的属性值="
											+ collectItemStr + "，Row="
											+ missionDataRow.getIndexInFile());
						}
						byte jobType = Byte.parseByte(itemInfo[0]);
						String itemCode = itemInfo[1];
						KItemTempAbs template = KSupportFactory
								.getItemModuleSupport().getItemTemplate(
										itemCode);
						if (template == null) {
							throw new KGameServerException(
									"加载任务模版表的collectItemCode错误,找不到对应的道具模版：不合法的属性值="
											+ itemCode + "，Row="
											+ missionDataRow.getIndexInFile());
						}
						map.put(jobType, template);
					}

					CollectItemTask task = new CollectItemTask(map,
							collectCount);
					condition.setCollectItemTask(task);
				} else {
					KItemTempAbs template = KSupportFactory
							.getItemModuleSupport().getItemTemplate(
									collectItemStr);
					if (template == null) {
						throw new KGameServerException(
								"加载任务模版表的collectItemCode错误,找不到对应的道具模版：不合法的属性值="
										+ collectItemStr + "，Row="
										+ missionDataRow.getIndexInFile());
					}

					CollectItemTask task = new CollectItemTask(template,
							collectCount);
					condition.setCollectItemTask(task);
				}
			}
			break;
		case MISSION_FUN_TYPE_GAME_LEVEL:
			condition.isGameLeveTask = true;
			String levelIdStr = missionDataRow.getData("levelId");
			int completeCount = missionDataRow.getInt("completeLevelCount");
			if (levelIdStr == null || levelIdStr.length() == 0) {
				throw new KGameServerException("加载任务模版表的levelId错误：不合法的属性值="
						+ levelIdStr + "，Row="
						+ missionDataRow.getIndexInFile());
			} else {
				if ((levelIdStr.toLowerCase()).equals("any")) {
					GameLevelTask levelTask = new GameLevelTask(
							GameLevelTask.ANY_TYPE_LEVEL, completeCount, true);
					condition.setGameLevelTask(levelTask);
					condition.completedTargetId = GameLevelTask.ANY_TYPE_LEVEL;
				} else {
					int levelId = Integer.parseInt(levelIdStr);
					GameLevelTask levelTask = new GameLevelTask(levelId,
							completeCount, false);
					condition.setGameLevelTask(levelTask);
					condition.completedTargetId = levelId;
				}
			}
			if (condition.completedTargetId != GameLevelTask.ANY_TYPE_LEVEL
					&& KGameLevelModuleExtension.getManager().getKGameLevel(
							condition.completedTargetId) == null) {
				throw new KGameServerException(
						"加载任务模版表的searchRoadGameLevelId错误：找不到对应的关卡="
								+ condition.completedTargetId + "，Row="
								+ missionDataRow.getIndexInFile());
			}
			break;
		case MISSION_FUN_TYPE_USE_ITEM:
			condition.isUseItemTask = true;
			String itemCode = missionDataRow.getData("useItemCode");
			if (itemCode != null && !itemCode.equals("0")) {
				KItemTempAbs template = KSupportFactory.getItemModuleSupport()
						.getItemTemplate(itemCode);
				if (template == null) {
					throw new KGameServerException(
							"加载任务模版表的useItemCode错误,找不到对应的道具模版：不合法的属性值="
									+ itemCode + "，Row="
									+ missionDataRow.getIndexInFile());
				} else {
					UseItemTask useItemTask = new UseItemTask(template);
					condition.setUseItemTask(useItemTask);
				}
			}
			break;
		case MISSION_FUN_TYPE_USE_FUNCTION:
			condition.isUseFunctionTask = true;
			short functionType = missionDataRow.getShort("functionType");
			if (this.missionType != KGameMissionTemplateTypeEnum.MISSION_TYPE_DAILY) {
				condition.completedTargetId = missionDataRow
						.getInt("searchRoadGameLevelId");
			}
			if (functionType < 0
					|| KUseFunctionTypeEnum.getEnum(functionType) == null) {
				throw new KGameServerException(
						"加载任务模版表的functionType错误：不合法的属性值=" + functionType
								+ "，Row=" + missionDataRow.getIndexInFile());
			} else {
				String functionTarget = missionDataRow.getData("targetId");
				int useCount = missionDataRow.getInt("useFunctionCount");
				UseFunctionTask useFunTask = new UseFunctionTask(functionType,
						functionTarget, useCount);
				condition.setUseFunctionTask(useFunTask);
			}
			if (this.missionType != KGameMissionTemplateTypeEnum.MISSION_TYPE_DAILY) {
				// if (functionType == FunctionTypeEnum.普通商店购买.functionId
				// || functionType == FunctionTypeEnum.NPC道具合成.functionId
				// || functionType == FunctionTypeEnum.NPC装备镶嵌.functionId) {
				// KNPCTemplate npcTemplate = KSupportFactory
				// .getNpcModuleSupport().getNPCTemplate(
				// condition.completedTargetId);
				// if (npcTemplate == null) {
				// throw new KGameServerException(
				// "加载任务模版表的searchRoadGameLevelId错误：不合法的属性值="
				// + condition.completedTargetId
				// + "，功能类型="
				// + functionType
				// + "【"
				// + FunctionTypeEnum
				// .getEnum(functionType).functionName
				// + "】找不到对应NPC寻路目标，Row="
				// + missionDataRow.getIndexInFile());
				// }
				// }
			}

			break;
		case MISSION_FUN_TYPE_ATTRIBUTE_DATA:
			condition.isAttributeTask = true;
			int attributeType = missionDataRow.getInt("attributeType");
			if (attributeType > 0) {
				int attributeValue = missionDataRow.getInt("attributeValue");
				AttributeTask attrTask = new AttributeTask(attributeType,
						attributeValue);
				condition.setAttributeTask(attrTask);
			}
			break;
		case MISSION_FUN_BATTLEFIELD:

			break;
		case MISSION_FUN_TYPE_QUESTION:

			break;
		case MISSION_FUN_TYPE_UP_FUNC_LV:
			condition.isUpgradeFunLvTask = true;
			functionType = missionDataRow.getShort("funType");
			if (this.missionType != KGameMissionTemplateTypeEnum.MISSION_TYPE_DAILY) {
				condition.completedTargetId = missionDataRow
						.getInt("searchRoadGameLevelId");
			}
			KFunctionTypeEnum funType = KFunctionTypeEnum.getEnum(functionType);
			if (functionType < 0 || funType == null) {
				throw new KGameServerException(
						"加载任务模版表的functionType错误：不合法的属性值=" + functionType
								+ "，Row=" + missionDataRow.getIndexInFile());
			}

			// if (functionType > 0) {
			// if (funType == KFunctionTypeEnum.强化
			// || funType == KFunctionTypeEnum.装备升级
			// || funType == FunctionTypeEnum.进阶
			// || funType == FunctionTypeEnum.轮回
			// || funType == FunctionTypeEnum.角色技能
			// || funType == FunctionTypeEnum.队伍) {
			//
			// } else {
			// throw new KGameServerException(
			// "加载任务模版表的functionType错误：不合法的属性值=" + functionType
			// + "，目前功能只支持装备强化、升级，副将进阶、轮回，主角技能的升级"
			// + "，行数Row="
			// + missionDataRow.getIndexInFile());
			// }
			// String functionTarget = missionDataRow.getData("funTargetId");
			// int upLevel = missionDataRow.getInt("upLevel");
			//
			// if (functionTarget.equals("0")) {
			// functionTarget = UpgradeFunLvTask.ANY_TARGET_TYPE;
			// } else {
			// if (functionType == FunctionTypeEnum.强化.functionId
			// || functionType == FunctionTypeEnum.装备升级.functionId) {
			// // KItemTemplate template =
			// // KSupportFactory.getItemSupport()
			// // .getKItemTemplate(functionTarget);
			// KEquipmentTypeEnum equipType = KEquipmentTypeEnum
			// .getEnum(Byte.parseByte(functionTarget));
			// if (equipType == null) {
			// throw new KGameServerException(
			// "加载任务模版表的'提升功能等级的任务完成条件'的funTargetId错误,找不到对应的装备位置：不合法的属性值="
			// + functionTarget + "，Row="
			// + missionDataRow.getIndexInFile());
			// }
			// } else if (functionType == FunctionTypeEnum.进阶.functionId
			// || functionType == FunctionTypeEnum.轮回.functionId) {
			// // KPetEnhanceType enhanceType = KPetEnhanceType
			// // .getEnum(Byte.parseByte(functionTarget));
			// // if (enhanceType == null) {
			// // throw new KGameServerException(
			// // "加载任务模版表的'提升功能等级的任务完成条件'的funTargetId错误,进阶和轮回目标值错误，"
			// // + "找不到对应的位置，不合法的属性值="
			// // + functionTarget + "，Row="
			// // + missionDataRow.getIndexInFile());
			// // }
			// } else if (functionType == FunctionTypeEnum.角色技能.functionId) {
			// functionTarget = UpgradeFunLvTask.ANY_TARGET_TYPE;
			// } else if (functionType == FunctionTypeEnum.队伍.functionId) {
			// functionTarget = UpgradeFunLvTask.ANY_TARGET_TYPE;
			// }
			// }
			// UpgradeFunLvTask upTask = new UpgradeFunLvTask(functionType,
			// functionTarget, upLevel);
			// condition.setUpgradeFunLvTask(upTask);
			//
			// if (this.missionType !=
			// KGameMissionTemplateTypeEnum.MISSION_TYPE_DAILY) {
			// if (functionType == FunctionTypeEnum.普通商店购买.functionId
			// || functionType == FunctionTypeEnum.NPC道具合成.functionId
			// || functionType == FunctionTypeEnum.NPC装备镶嵌.functionId) {
			// KNPCTemplate npcTemplate = KSupportFactory
			// .getNpcModuleSupport().getNPCTemplate(
			// condition.completedTargetId);
			// if (npcTemplate == null) {
			// throw new KGameServerException(
			// "加载任务模版表的searchRoadGameLevelId错误：不合法的属性值="
			// + condition.completedTargetId
			// + "，功能类型="
			// + functionType
			// + "【"
			// + FunctionTypeEnum
			// .getEnum(functionType).functionName
			// + "】找不到对应NPC寻路目标，Row="
			// + missionDataRow.getIndexInFile());
			// }
			// }
			// }
			// }

			break;
		}
	}

	/**
	 * 初始化日常任务excel数据任务模版
	 * 
	 * @param missionDataRow
	 * @param rowIndex
	 * @throws Exception
	 */
	public void initDailyKMissionTemplateData(KGameExcelRow missionDataRow,
			int rowIndex) throws Exception {

		// 初始化基础数据
		this.missionTemplateId = missionDataRow.getInt("missionTemplateId");
		this.missionName = missionDataRow.getData("missionName");
		this.missionExtName = HyperTextTool.extColor(missionName, 0);
		this.desc = missionDataRow.getData("desc");
		this.missionTips = missionDataRow.getData("missionTips");

		this.missionType = KGameMissionTemplateTypeEnum.MISSION_TYPE_DAILY;
		checkMissionType(this.missionType);

		this.canDuplicateReceive = true;

		// this.dailyMissionRoleLevel = missionDataRow.getInt("misisonLevel");

		int submitNpcId = missionDataRow.getInt("submitNpcId");

		// 初始化任务触发条件数据
		this.missionTriggerCondition = new MissionTriggerCondition();
		this.missionTriggerCondition.roleLevelLimit = missionDataRow
				.getInt("roleLevelLimit");
		this.missionTriggerCondition.frontMissionTemplateId = missionDataRow
				.getInt("frontMissiontemplateId");
		byte occupationType = missionDataRow.getByte("occupationType");
		if (occupationType > 0) {
			if (KJobTypeEnum.getJob(occupationType) == null) {
				throw new KGameServerException(
						"加载任务模版表的occupationType错误：找不到该角色职业模版，值="
								+ occupationType + "，Row="
								+ missionDataRow.getIndexInFile());
			}
			this.missionTriggerCondition.occupationType = KJobTypeEnum
					.getJob(occupationType);
			this.missionTriggerCondition.isOccupationCondition = true;
		}
		this.missionTriggerCondition.isGangCondition = missionDataRow
				.getBoolean("isGangCondition");
		byte attributeLimitType = missionDataRow.getByte("attributeLimitType");
		int attributeLimitValue = missionDataRow.getInt("attributeLimitValue");
		if (attributeLimitType > 0) {
			if (KGameAttrType.getAttrTypeEnum(attributeLimitType) == null) {
				throw new KGameServerException(
						"加载任务模版表的attributeLimitType错误：找不到该属性限制模版，值="
								+ attributeLimitType + "，Row="
								+ missionDataRow.getIndexInFile());
			}
			AttributeCondition attCondition = new AttributeCondition(
					KGameAttrType.getAttrTypeEnum(attributeLimitType),
					attributeLimitValue);
			this.missionTriggerCondition.attributeCondition = attCondition;
			this.missionTriggerCondition.isAttributeCondition = true;
		}
		this.missionTriggerCondition.acceptLimitCount = missionDataRow
				.getInt("acceptLimitCount");
		this.missionTriggerCondition.openTimeLimitSeconds = missionDataRow
				.getInt("openTimeLimit");

		// 初始化任务完成条件
		this.missionCompleteCondition = new MissionCompleteCondition();
		byte missionTargetType = missionDataRow.getByte("missionTargetType");
		if (missionTargetType < 1 || missionTargetType > 9) {
			throw new KGameServerException(
					"加载任务模版表的missionTargetType错误：找不到该任务目标类型，值="
							+ missionTargetType + "，Row="
							+ missionDataRow.getIndexInFile());
		}
		this.missionFunType = KGameMissionFunTypeEnum
				.getEnum(missionTargetType);
		initCompleteCondition(this.missionCompleteCondition,
				this.missionFunType, missionDataRow, rowIndex);
		this.missionCompleteCondition.completeTimeLimitSeconds = missionDataRow
				.getInt("completeTimeLimit");

		if (this.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_DIALOG) {
			if (submitNpcId == 0
					|| KSupportFactory.getNpcModuleSupport().getNPCTemplate(
							submitNpcId) == null) {
				throw new KGameServerException(
						"加载修行任务模版表的submitNpcId错误：找不到该NPC模版，值=" + submitNpcId
								+ "，Row=" + missionDataRow.getIndexInFile());
			}
			this.submitMissionNPCTemplate = KSupportFactory
					.getNpcModuleSupport().getNPCTemplate(submitNpcId);
		}

	}

	public MissionDialog getPrologueMissionDialog() {
		return prologueMissionDialog;
	}

	public MissionDialog getAcceptMissionDialog(KRole role) {

		if (this.missionCompleteCondition.isCollectItemTask
				&& this.missionCompleteCondition.getCollectItemTask() != null) {
			if (acceptMissionDialog.collectItemDialogContentMap
					.containsKey(MissionDialog.default_job)) {
				return acceptMissionDialog
						.constructCollectItemAcceptDialog(MissionDialog.default_job);
			} else {
				return acceptMissionDialog
						.constructCollectItemAcceptDialog(role.getJob());
			}
		}

		return acceptMissionDialog;
	}

	public MissionDialog getCompletedMissionDialog() {
		return completedMissionDialog;
	}

	public MissionDialog getUncompletedMissionDialog() {
		return uncompletedMissionDialog;
	}

	/**
	 * 检测任务模版类型
	 * 
	 * @param type
	 */
	private void checkMissionType(final KGameMissionTemplateTypeEnum type) {
		switch (type) {
		case MISSION_TYPE_MAIN_LINE:
			this.isMainLineMission = true;
			break;
		case MISSION_TYPE_BRANCH_LINE:
			this.isBranchLineMission = true;
			break;
		case MISSION_TYPE_NEW_GUIDE:
			this.isMainLineMission = true;
			// this.isNewPlayerGuildMission = true;
			break;
		case MISSION_TYPE_GANG:
			break;
		case MISSION_TYPE_ACTIVITY:
			break;
		case MISSION_TYPE_DAILY:
			this.isDailyMission = true;
			this.isMainLineMission = false;
			break;
		}
	}

	public void checkConditionWhileStartCompleted() throws KGameServerException {
		if (this.missionCompleteCondition.isKillMonsterTask) {
			if (this.missionType != KGameMissionTemplateTypeEnum.MISSION_TYPE_DAILY) {

				if (missionCompleteCondition.completedTargetId == 0
						|| KGameLevelModuleExtension
								.getManager()
								.getKGameLevel(
										missionCompleteCondition.completedTargetId) == null) {
					throw new KGameServerException(
							"加载任务模版表的searchRoadGameLevelId错误：找不到该任务目标关卡，值="
									+ missionCompleteCondition.completedTargetId
									+ "，任务模版ID=" + this.missionTemplateId);
				}
			} else {
				if (missionCompleteCondition.completedTargetId != 0
						&& KGameLevelModuleExtension
								.getManager()
								.getKGameLevel(
										missionCompleteCondition.completedTargetId) == null) {
					throw new KGameServerException(
							"加载日常任务模版表的searchRoadGameLevelId错误：找不到该任务目标关卡，值="
									+ missionCompleteCondition.completedTargetId
									+ "，任务模版ID=" + this.missionTemplateId);
				} else if (missionCompleteCondition.completedTargetId == 0) {
					missionCompleteCondition.completedTargetId = GameLevelTask.ANY_TYPE_LEVEL;
				}
			}
		}

		if (this.missionCompleteCondition.isCollectItemTask) {
			if (this.missionType != KGameMissionTemplateTypeEnum.MISSION_TYPE_DAILY) {

				if (missionCompleteCondition.completedTargetId == 0
						|| KGameLevelModuleExtension
								.getManager()
								.getKGameLevel(
										missionCompleteCondition.completedTargetId) == null) {
					throw new KGameServerException(
							"加载任务模版表的searchRoadGameLevelId错误：找不到该任务目标关卡，值="
									+ missionCompleteCondition.completedTargetId
									+ "，任务模版ID=" + this.missionTemplateId);
				}
			} else {
				if (missionCompleteCondition.completedTargetId != 0
						&& KGameLevelModuleExtension
								.getManager()
								.getKGameLevel(
										missionCompleteCondition.completedTargetId) == null) {
					throw new KGameServerException(
							"加载修行任务模版表的searchRoadGameLevelId错误：找不到该任务目标关卡，值="
									+ missionCompleteCondition.completedTargetId
									+ "，任务模版ID=" + this.missionTemplateId);
				} else if (missionCompleteCondition.completedTargetId == 0) {
					missionCompleteCondition.completedTargetId = GameLevelTask.ANY_TYPE_LEVEL;
				}
			}
		}

		if (this.missionCompleteCondition.isGameLeveTask) {
			GameLevelTask levelTask = this.missionCompleteCondition
					.getGameLevelTask();
			if (levelTask.isLevelTypeAny()) {
				if (levelTask.levelId == 0
						|| KGameLevelModuleExtension.getManager()
								.getKGameLevel(levelTask.levelId) == null) {
					throw new KGameServerException(
							"加载任务模版表的levelId错误：找不到该任务目标关卡，值="
									+ levelTask.levelId + "，任务模版ID="
									+ this.missionTemplateId);
				}
			}
		}
	}

	/**
	 * 初始化任务模版NPC对话数据
	 * 
	 * @param missionDialogRow
	 * @throws Exception
	 */
	public void initKMissionTemplateDialog(KGameExcelRow missionDialogRow,
			int rowIndex) throws Exception {
		try {
			String prologueString = missionDialogRow
					.getData("prologueMissionDialog");
			if (prologueString != null && !prologueString.equals("null")) {
				if (prologueString.indexOf("&a：") > 0
						|| prologueString.indexOf("&q：") > 0) {
					throw new KGameServerException(
							"加载任务模版对话数据表的prologueMissionDialog错误：该值为null或含有不合法字符，值="
									+ prologueString + "，Row="
									+ missionDialogRow.getIndexInFile());
				}
				this.prologueMissionDialog = new MissionDialog();
				initDialog("prologueMissionDialog", prologueString,
						this.prologueMissionDialog,
						missionDialogRow.getIndexInFile());
			}

			String acceptString = missionDialogRow
					.getData("acceptMissionDialog");
			if (acceptString != null && !acceptString.equals("null")) {
				if (acceptString.indexOf("&a：") > 0
						|| acceptString.indexOf("&q：") > 0) {
					throw new KGameServerException(
							"加载任务模版对话数据表的acceptMissionDialog错误：该值为null或含有不合法字符，值="
									+ acceptString + "，Row="
									+ missionDialogRow.getIndexInFile());
				}
				this.acceptMissionDialog = new MissionDialog();
				initDialog("acceptMissionDialog", acceptString,
						this.acceptMissionDialog,
						missionDialogRow.getIndexInFile());
				if (this.acceptMissionDialog.getDialogueList() == null
						|| this.acceptMissionDialog.getDialogueList().size() <= 0) {
					throw new KGameServerException(
							"加载任务模版对话数据表的acceptMissionDialog错误：该值为null或含有不合法字符，值="
									+ acceptString + "，Row="
									+ missionDialogRow.getIndexInFile());
				}
			} else {
				throw new KGameServerException(
						"加载任务模版对话数据表的acceptMissionDialog错误：该值为null或含有不合法字符，值="
								+ acceptString + "，Row="
								+ missionDialogRow.getIndexInFile());
			}

			String uncompletedString = missionDialogRow
					.getData("uncompletedMissionDialog");
			if (uncompletedString != null && !uncompletedString.equals("null")) {
				if (uncompletedString.indexOf("&a：") > 0
						|| uncompletedString.indexOf("&q：") > 0) {
					throw new KGameServerException(
							"加载任务模版对话数据表的uncompletedMissionDialog错误：该值为null或含有不合法字符，值="
									+ uncompletedString + "，Row="
									+ missionDialogRow.getIndexInFile());
				}
				this.uncompletedMissionDialog = new MissionDialog();
				initDialog("uncompletedMissionDialog", uncompletedString,
						this.uncompletedMissionDialog,
						missionDialogRow.getIndexInFile());
			} else {
				if (this.missionFunType != KGameMissionFunTypeEnum.MISSION_FUN_TYPE_DIALOG) {
					throw new KGameServerException(
							"加载任务模版对话数据表的completedMissionDialog错误：该值为null或含有不合法字符，值="
									+ uncompletedString + "，Row="
									+ missionDialogRow.getIndexInFile());
				}
			}

			String completedString = missionDialogRow
					.getData("completedMissionDialog");
			if (completedString != null && !completedString.equals("null")) {
				if (completedString.indexOf("&a：") > 0
						|| completedString.indexOf("&q：") > 0) {
					throw new KGameServerException(
							"加载任务模版对话数据表的completedMissionDialog错误：该值为null或含有不合法字符，值="
									+ completedString + "，Row="
									+ missionDialogRow.getIndexInFile());
				}
				this.completedMissionDialog = new MissionDialog();
				initDialog("completedMissionDialog", completedString,
						this.completedMissionDialog,
						missionDialogRow.getIndexInFile());
				if (this.completedMissionDialog.getDialogueList() == null
						|| this.completedMissionDialog.getDialogueList().size() <= 0) {
					throw new KGameServerException(
							"加载任务模版对话数据表的completedMissionDialog错误：该值为null或含有不合法字符，值="
									+ completedString + "，Row="
									+ missionDialogRow.getIndexInFile());
				}
			} else {
				throw new KGameServerException(
						"加载任务模版对话数据表的completedMissionDialog错误：该值为null或含有不合法字符，值="
								+ completedString + "，Row="
								+ missionDialogRow.getIndexInFile());
			}

			// String content = getMissionAcceptTips();
			// String question = getMissionTypeNameText(this.missionType)
			// + getHyperText(missionName, 0) + getHyperText("（接受）", 7);
			// Dialogue dialogue = new Dialogue(content, question, true,
			// Dialogue.DIALOGUE_TYPE_MISSION_TIPS);
			// dialogue.setItemRewardTemplate(missionReward
			// .getAllMissionItemRewardTemplate());
			// this.acceptMissionTipsDialog = new MissionDialog();
			// this.acceptMissionTipsDialog.getDialogueList().add(dialogue);
		} catch (Exception e) {
			throw new KGameServerException("加载载《任务模版对话数据表》错误，任务ID="
					+ missionTemplateId + "，行数Row="
					+ missionDialogRow.getIndexInFile(), e);
		}

	}

	/**
	 * 初始化任务模版NPC对话数据
	 * 
	 * @param missionDialogRow
	 * @throws Exception
	 */
	public void initDailyMissionTemplateDialog(KGameExcelRow missionDialogRow,
			int rowIndex) throws Exception {
		try {
			String completedString = missionDialogRow
					.getData("completedMissionDialog");
			if (completedString != null && !completedString.equals("null")) {
				if (completedString.indexOf("&a：") > 0
						|| completedString.indexOf("&q：") > 0) {
					throw new KGameServerException(
							"加载修行任务的《送信任务类型NPC对话数据》的completedMissionDialog错误：该值为null或含有不合法字符，值="
									+ completedString + "，Row="
									+ missionDialogRow.getIndexInFile());
				}
				this.completedMissionDialog = new MissionDialog();
				initDialog("completedMissionDialog", completedString,
						this.completedMissionDialog,
						missionDialogRow.getIndexInFile());
				if (this.completedMissionDialog.getDialogueList() == null
						|| this.completedMissionDialog.getDialogueList().size() <= 0) {
					throw new KGameServerException(
							"加载修行任务的《送信任务类型NPC对话数据》的completedMissionDialog错误：该值为null或含有不合法字符，值="
									+ completedString + "，Row="
									+ missionDialogRow.getIndexInFile());
				}
			} else {
				throw new KGameServerException(
						"加载修行任务的《送信任务类型NPC对话数据》的completedMissionDialog错误：该值为null或含有不合法字符，值="
								+ completedString + "，Row="
								+ missionDialogRow.getIndexInFile());
			}

		} catch (Exception e) {
			throw new KGameServerException("加载载《任务模版对话数据表》错误，行数Row="
					+ missionDialogRow.getIndexInFile(), e);
		}

	}

	public static void initDialog(String listName, String dialogStringData,
			MissionDialog dialog, int rowIndex) throws Exception {

		String[] paragraph = (dialogStringData.replaceAll("\t|\r|\n", ""))
				.split("&a:");
		// System.out.println("paragraph length：" + paragraph.length
		// + "-----index:" + rowIndex + "-----listName:" + listName);
		int aLength = paragraph.length - 1;
		if (paragraph.length > 1) {
			int qLength = 0;

			for (int i = 1; i < paragraph.length; i++) {
				// System.out.println("paragraph length："+paragraph[i]+"****"+paragraph[i].indexOf("&a:")+"**"+paragraph[i].indexOf("&q:"));
				String str = paragraph[i];
				if ((str.split("&q:")).length > 1
						&& (str.split("&q:")).length % 2 == 0) {
					qLength++;
					String content = str.substring(0, str.indexOf("&q:"));
					String question = str.substring(str.indexOf("&q:")
							+ "&q:".length());
					if (content != null) {
						if (listName.equals("acceptMissionDialog")
								&& i == (paragraph.length - 1)) {
							question += MissionTips
									.getTipsAcceptMissionButton();
						}
						Dialogue dialogue = new Dialogue(content, question,
								question == null ? false : true,
								Dialogue.DIALOGUE_TYPE_NORMAL);
						dialog.getDialogueList().add(dialogue);
					}
				} else {
					throw new KGameServerException("加载任务模版对话数据表的" + listName
							+ "字段错误：该值为null或含有不合法字符，值=" + dialogStringData
							+ "，Row=" + rowIndex);
				}
			}
			if (aLength != qLength) {
				throw new KGameServerException("加载任务模版对话数据表的" + listName
						+ "字段错误：该值为null或含有不合法字符，值=" + dialogStringData
						+ "，Row=" + rowIndex);
			}
		} else {
			throw new KGameServerException("加载任务模版对话数据表的" + listName
					+ "字段错误：该值为null或含有不合法字符，值=" + dialogStringData + "，Row="
					+ rowIndex);
		}
	}

	/**
	 * 处理任务列表中的某个可接任务模版提示字符串
	 * 
	 * @param mission
	 * @return
	 */
	public String getMissionAcceptTips(KRole role) {

		String tips = missionTips;
		if (this.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_KILL_MONSTER) {
			tips += MissionTips.getTipsMissionTargetTitle1();
			for (KillMonsterTask task : this.missionCompleteCondition
					.getKillMonsterTaskMap().values()) {
				// String monsterName = task.isAnyTypeMonster ? "任意Lv."
				// + task.monsterLevel + "怪物" : task.monsterTemplate.name;
				String monsterName = (task.isAnyTypeMonster) ? ((task.isMonsterLevelLimit) ? MissionTips
						.getTipsKillMonsterTypeMonsterName() : MissionTips
						.getTipsKillMonsterTypeMonsterName1(task.monsterLevel))
						: (task.monsterTemplate.name);
				int targetCount = task.killCount;
				// tips += (monsterName + " x" + targetCount + "只    ");
				tips += MissionTips.getTipsKillMonsterTypeMonsterInfo1(
						monsterName, targetCount);
			}
		} else if (this.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_COLLECT_ITEMS) {
			tips += MissionTips.getTipsMissionTargetTitle1();
			CollectItemTask task = missionCompleteCondition
					.getCollectItemTask();
			String itemName = "";
			if (task.isLimitJob) {
				itemName = task.itemTemplateMap.get(role.getJob()).extItemName;
			} else {
				itemName = task.itemTemplate.extItemName;
			}
			int targetCount = task.collectCount;
			tips += MissionTips.getTipsCollectItemTypeItemInfo1(itemName,
					targetCount);

		} else if (this.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_GAME_LEVEL) {
			tips += MissionTips.getTipsMissionTargetTitle1();
			GameLevelTask task = missionCompleteCondition.getGameLevelTask();
			int targetCount = task.completeCount;
			// String levelName = KSupportFactory.getScenarioSupport()
			// .getKGameLevel(task.levelId).getLevelName();
			// tips += ("\n任务目标：  完成关卡[" + levelName + "]" + targetCount + "次");
			if (task.isLevelTypeAny()) {
				tips += MissionTips
						.getTipsCompleteLevelTypeInfo2(task.completeCount);
			} else {
				String levelName = KGameLevelModuleExtension.getManager()
						.getKGameLevel(task.levelId).getLevelName();
				tips += MissionTips.getTipsCompleteLevelTypeInfo3(levelName,
						task.completeCount);
			}
		} else if (this.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_QUESTION) {
			tips += MissionTips.getTipsMissionTargetTitle1();
			AnswerQuestionTask task = missionCompleteCondition
					.getAnswerQuestionTask();
			int targetCount = task.getTotalQuestionCount();
			// tips += ("\n任务目标：  完成答题 " + targetCount + "次");
			tips += MissionTips.getTipsQuestionTypeInfo1(targetCount);
		}
		tips += MissionTips.getTipsMissionPriceTitle1()
				+ getMissionRewardTips();
		// for (MissionItemRewardTemplate mrTemplate : this.missionReward
		// .getAllMissionItemRewardTemplate()) {
		// tips += "  【" + mrTemplate.getItemTemplate().ItemName + "】x"
		// + mrTemplate.getRewardCount() + "  ";
		// }

		return tips;
	}

	public String getMissionRewardTips() {
		String tips = "";
		for (AttValueStruct att : this.missionReward.attList) {
			tips += " 【" + att.roleAttType.getName() + "】x" + att.addValue;
		}
		for (KCurrencyCountStruct money : this.missionReward.moneyList) {
			tips += " 【" + money.currencyType.name + "】x" + money.currencyCount;
		}
		return tips;
	}

	public boolean isMissionCanAccept(KRole role) {
		if (this.missionTriggerCondition.roleLevelLimit > role.getLevel()) {
			return false;
		} else {
			return true;
		}
	}

	public String getMissionTypeNameText(KGameMissionTemplateTypeEnum type) {
		return type.extTypeName;
	}

	/**
	 * 根据任务状态获取任务名称
	 * 
	 * @param status
	 * @return
	 */
	public String getMissionNameByStatusType(KGameMissionStatusEnum status,
			KRole role) {

		String str = getMissionTypeNameText(missionType) + missionExtName
				+ getMissionStatusNameText(role, status);
		// System.err.println("getMissionNameByStatusType::::::::::::::::"+str);
		return str;
	}

	/**
	 * 根据任务状态获取任务名称
	 * 
	 * @param status
	 * @return
	 */
	public String getMissionNameBySearchRoadButton(KRole role) {

		String str = missionExtName
				+ "\n"
				+ MissionTips
						.getTipsMissionAcceptStatus(missionTriggerCondition.roleLevelLimit);
		// System.err.println("getMissionNameByStatusType::::::::::::::::"+str);
		return str;
	}

	public String getMissionStatusNameText(KRole role,
			KGameMissionStatusEnum status) {
		String name = "";
		switch (status) {
		case MISSION_STATUS_TRYRECEIVE:
			if (this.isMissionCanAccept(role)) {
				name = HyperTextTool.extColor(status.statusName, 7);
			} else {
				// ht = new HyperText(" 【"
				// + this.missionTriggerCondition.roleLevelLimit + "级可接】");
				// ht.addTag(new HyperTextTag(TagType.c, 1));
				name = MissionTips
						.getTipsMissionAcceptStatus(this.missionTriggerCondition.roleLevelLimit);
			}
			break;
		case MISSION_STATUS_TRYFINISH:
			name = HyperTextTool.extColor(status.statusName, 9);
			break;
		case MISSION_STATUS_TRYSUBMIT:
			name = HyperTextTool.extColor(status.statusName, 6);
			break;
		}
		return name;
	}
}
