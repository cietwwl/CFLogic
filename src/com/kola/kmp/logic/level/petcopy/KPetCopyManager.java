package com.kola.kmp.logic.level.petcopy;

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
import com.kola.kmp.logic.flow.KRoleAttrModifyType;
import com.kola.kmp.logic.item.KItemMsgPackCenter;
import com.kola.kmp.logic.level.FightEvaluateData;
import com.kola.kmp.logic.level.FightEventListener;
import com.kola.kmp.logic.level.FightResult;
import com.kola.kmp.logic.level.KEnterLevelCondition;
import com.kola.kmp.logic.level.KGameLevelManager;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.level.KGameLevelRecord;
import com.kola.kmp.logic.level.KGameLevelSet;
import com.kola.kmp.logic.level.KLevelModuleDialogProcesser;
import com.kola.kmp.logic.level.KLevelReward;
import com.kola.kmp.logic.level.KLevelTemplate;
import com.kola.kmp.logic.level.TowerFightResult;
import com.kola.kmp.logic.level.KGameLevelManager.CompleteGameLevelTempRecord;
import com.kola.kmp.logic.level.KGameLevelManager.LevelRewardResultData;
import com.kola.kmp.logic.level.KGameLevelRecord.PlayerRoleGamelevelData;
import com.kola.kmp.logic.level.KGameLevelRecord.StrangerData;
import com.kola.kmp.logic.level.KLevelReward.NormalItemRewardTemplate;
//import com.kola.kmp.logic.level.copys.KCopyManager.CopyActivityConfig;
import com.kola.kmp.logic.level.gamestory.Animation;
import com.kola.kmp.logic.level.petcopy.KPetCopyBattlefield.KPetCopyBattlefieldDropData;
import com.kola.kmp.logic.level.tower.KTowerBattlefield;
import com.kola.kmp.logic.level.tower.KTowerLevelTemplate;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.other.KGangTecTypeEnum;
import com.kola.kmp.logic.other.KLimitTimeProduceActivityTypeEnum;
import com.kola.kmp.logic.other.KPetCopyDropTypeEnum;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.TimeLimitActivityDataManager.TimeLimieProduceActivity;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.ItemModuleSupport;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.LevelTips;
import com.kola.kmp.logic.util.tips.TimeLimitProducActivityTips;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;
import com.kola.kmp.protocol.level.KLevelProtocol;

public class KPetCopyManager {
	private static final KGameLogger _LOGGER = KGameLogger
			.getLogger(KPetCopyManager.class);

	// 副本免费挑战次数
	public static int free_challenge_count = 1;

	// 随从副本的所有关卡MAP
	private Map<Integer, KPetCopyLevelTemplate> petCopyLevelMap = new LinkedHashMap<Integer, KPetCopyLevelTemplate>();

	// 所有随从副本战场模版
	public Map<Integer, KPetCopyBattlefield> allKPetCopyBattlefieldTemplate = new LinkedHashMap<Integer, KPetCopyBattlefield>();

	public void init(String xlsPath) throws KGameServerException {
		KGameExcelFile xlsFile = null;
		try {
			xlsFile = new KGameExcelFile(xlsPath);
		} catch (BiffException e) {
			throw new KGameServerException("读取关卡剧本excel表头发生错误！", e);
		} catch (IOException e) {
			throw new KGameServerException("读取关卡剧本excel表头发生错误！", e);
		}

		if (xlsFile != null) {
			loadExcelData(xlsFile, KGameLevelTypeEnum.好友副本关卡);
		}
	}

	private void loadExcelData(KGameExcelFile xlsFile,
			KGameLevelTypeEnum levelType) throws KGameServerException {
		// 关卡数据
		String tableName = "随从副本";
		int levelDataRowIndex = 5;
		KGameExcelTable levelDataTable = xlsFile.getTable(tableName,
				levelDataRowIndex);
		KGameExcelRow[] allLevelDataRows = levelDataTable.getAllDataRows();

		if (allLevelDataRows != null) {
			for (int i = 0; i < allLevelDataRows.length; i++) {
				KPetCopyLevelTemplate level = new KPetCopyLevelTemplate();
				level.init(tableName, allLevelDataRows[i],
						KGameLevelTypeEnum.随从副本关卡);
				level.levelNum = (byte) (i + 1);
				petCopyLevelMap.put(level.getLevelId(), level);
				allKPetCopyBattlefieldTemplate.put(
						level.getBattlefieldTemplate().battlefieldId,
						level.getBattlefieldTemplate());

				petCopyLevelMap.put(level.getLevelId(), level);
				if (level.getEnterCondition().getFrontLevelId() > 0) {
					if (petCopyLevelMap.containsKey(level.getEnterCondition()
							.getFrontLevelId())) {
						(petCopyLevelMap.get(level.getEnterCondition()
								.getFrontLevelId()).getHinderGameLevelList())
								.add(level);
					}
				}
			}
		}

		tableName = "随从副本掉落方案";
		levelDataTable = xlsFile.getTable(tableName, levelDataRowIndex);
		allLevelDataRows = levelDataTable.getAllDataRows();

		if (allLevelDataRows != null) {
			for (int i = 0; i < allLevelDataRows.length; i++) {
				KPetCopyBattlefieldDropTemplate dropTemplate = new KPetCopyBattlefieldDropTemplate();
				dropTemplate.init(tableName, allLevelDataRows[i]);
				if (!petCopyLevelMap.containsKey(dropTemplate.levelId)) {
					throw new KGameServerException("初始化表<" + tableName
							+ ">错误，找不到关卡ID=" + dropTemplate.levelId
							+ "，excel行数："
							+ allLevelDataRows[i].getIndexInFile());
				}
				petCopyLevelMap.get(dropTemplate.levelId)
						.addKPetCopyBattlefieldDropTemplate(dropTemplate);
			}
		}

		tableName = "随从副本奖励结算";
		levelDataTable = xlsFile.getTable(tableName, levelDataRowIndex);
		allLevelDataRows = levelDataTable.getAllDataRows();

		if (allLevelDataRows != null) {
			for (int i = 0; i < allLevelDataRows.length; i++) {
				KPetCopyReward reward = new KPetCopyReward();
				reward.init(tableName, allLevelDataRows[i]);
				if (!petCopyLevelMap.containsKey(reward.levelId)) {
					throw new KGameServerException("初始化表<" + tableName
							+ ">错误，找不到关卡ID=" + reward.levelId + "，excel行数："
							+ allLevelDataRows[i].getIndexInFile());
				}
				petCopyLevelMap.get(reward.levelId).getRewardMap()
						.put(reward.roleLv, reward);
			}
		}
	}

	public void checkInit() throws KGameServerException {
		for (KPetCopyLevelTemplate level : petCopyLevelMap.values()) {
			if (!level.getPetBattlefieldTemplate().isInitOK) {
				throw new KGameServerException(
						"#########  加载levelConfig.xls表<随从副本>的战场xml数据错误，该战场初始化失败，xml文件名="
								+ level.getPetBattlefieldTemplate()
										.getBattlePathName() + "，关卡ID="
								+ level.getLevelId());
			}
			if (level.getRewardMap().size() == 0) {
				throw new KGameServerException(
						"#########  加载levelConfig.xls表<随从副本奖励结算>的错误，该关卡没有配置结算奖励数据，关卡ID="
								+ level.getLevelId());
			}
			if (level.getDropTemplateMap().size() == 0) {
				throw new KGameServerException(
						"#########  加载levelConfig.xls表<随从副本掉落方案>的错误，该关卡没有配置掉落方案数据，关卡ID="
								+ level.getLevelId());
			}
		}
	}

	public Map<Integer, KPetCopyLevelTemplate> getPetCopyLevelMap() {
		return petCopyLevelMap;
	}

	/**
	 * 处理角色进入关卡
	 * 
	 * @param role
	 * @param levelId
	 * @param isNeedCheck
	 *            是否需要检测进入条件
	 */
	public KActionResult playerRoleJoinGameLevel(KRole role, int levelId,
			boolean isNeedCheck, boolean isSendDialog) {
		if (role == null) {
			_LOGGER.error("角色进入好友副本关卡失败，角色为null，关卡ID：" + levelId + "，关卡类型:"
					+ KGameLevelTypeEnum.好友副本关卡);
			KDialogService.sendUprisingDialog(role.getId(),
					LevelTips.getTipsJoinLevelFailed());
			return new KActionResult(false, LevelTips.getTipsJoinLevelFailed());
		}

		KPetCopyLevelTemplate level = this.petCopyLevelMap.get(levelId);
		if (level == null) {
			_LOGGER.error("角色进入副本关卡失败，找不到对应的关卡。角色id:" + role.getId() + "，关卡ID："
					+ levelId + "，关卡类型" + level.getLevelType());
			KDialogService.sendUprisingDialog(role.getId(),
					LevelTips.getTipsJoinLevelFailed());
			return new KActionResult(false, LevelTips.getTipsJoinLevelFailed());
		}

		// 获取关卡开启条件
		KEnterLevelCondition condition = level.getEnterCondition();

		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance()
				.getGameLevelSet(role.getId());

		PlayerRoleGamelevelData levelData = null;
		KGameLevelRecord pcRecord = levelSet
				.getCopyRecord(level.getLevelType());
		if (pcRecord != null) {
			levelData = pcRecord.getLevelDataMap().get(level.getLevelId());
		}

		if (isNeedCheck) {
			// 判断关卡是否开放
			byte levelOpenType = judgeGameLevelOpenState(role, levelSet, level);
			if (levelOpenType != KLevelTemplate.GAME_LEVEL_STATE_OPEN) {
				KGameLevelModuleExtension.getManager()
						.sendJoinNormalGameLevelTipsMessage(role, (short) -1,
								LevelTips.getTipsLevelNotOpen(), false, null);
				return new KActionResult(false, LevelTips.getTipsLevelNotOpen());
			}

			if (pcRecord != null && pcRecord.petCopyData != null) {
				int remainChallengeCount = pcRecord.petCopyData.remainChallengeCount;
				int challengeCount = pcRecord.petCopyData.challengeCount;
				int buyCount = pcRecord.petCopyData.todayBuyCount;
				if (remainChallengeCount <= 0) {
					VIPLevelData vipData = KSupportFactory
							.getVIPModuleSupport()
							.getVIPLevelData(role.getId());
					int vip_buy_count = vipData.rescueHostages.length;
					if (buyCount < vip_buy_count) {
						int usePoint = vipData.rescueHostages[buyCount];
						KGameLevelModuleExtension
								.getManager()
								.sendLevelTipsMessage(
										role,
										KLevelModuleDialogProcesser.KEY_BUY_PET_COPY,
										LevelTips
												.getTipsJoinFriendCopyFailedUsePoint(usePoint),
										true, "");
						return new KActionResult(
								false,
								LevelTips
										.getTipsJoinFriendCopyFailedUsePoint(usePoint));

					} else if (buyCount >= vip_buy_count) {
						int vipLv = KSupportFactory.getVIPModuleSupport()
								.getVipLv(role.getId());
						KGameLevelModuleExtension
								.getManager()
								.sendJoinNormalGameLevelTipsMessage(
										role,
										(short) -1,
										LevelTips
												.getTipsJoinFriendCopyFailedCountNotEnough(),
										false, null);
						return new KActionResult(
								false,
								LevelTips
										.getTipsJoinFriendCopyFailedCountNotEnough());
					}
				}
			} else {
				KGameLevelModuleExtension.getManager()
						.sendJoinNormalGameLevelTipsMessage(role, (short) -1,
								LevelTips.getTipsLevelNotOpen(), false, null);
				return new KActionResult(false, LevelTips.getTipsLevelNotOpen());
			}
		}

		levelSet.recordChallengePetCopy();

		sendUpdatePetCopyInfoMsg(role, null, pcRecord, null);

		// 检测是否有限时产出活动
		boolean isCopyActivityPrice = false;
		float expRate = 1, goldRate = 1, potentialRate = 1;
		int itemMultiple = 1;
		if (level.getLevelType() == KGameLevelTypeEnum.随从副本关卡) {
			TimeLimieProduceActivity activity = KSupportFactory
					.getExcitingRewardSupport().getTimeLimieProduceActivity(
							KLimitTimeProduceActivityTypeEnum.随从副本活动);

			if (activity != null && activity.isActivityTakeEffectNow()) {
				isCopyActivityPrice = true;
				expRate = activity.expRate;
				goldRate = activity.goldRate;
				potentialRate = activity.potentialRate;
				itemMultiple = activity.itemMultiple;
				KSupportFactory.getMailModuleSupport().sendSimpleMailBySystem(
						role.getId(), activity.mailTitle, activity.mailContent);
			}
		}

		// 通知战斗模块，角色进入战场
		for (FightEventListener listener : KGameLevelModuleExtension
				.getManager().getFightEventListenerList()) {
			listener.notifyPetCopyBattle(role,
					level.createPetCopyBattlefield(itemMultiple));
		}

		KPetCopyActivity.getInstance().sendUpdateActivity(role);

		return new KActionResult(true, "");
	}

	/**
	 * 判断剧本关卡开启状态
	 * 
	 * @param levelAttr
	 * @param level
	 * @return
	 */
	public byte judgeGameLevelOpenState(KRole role, KGameLevelSet levelSet,
			KPetCopyLevelTemplate level) {
		if (role.getRoleGameSettingData() != null
				&& role.getRoleGameSettingData().isDebugOpenLevel()) {
			return KLevelTemplate.GAME_LEVEL_STATE_OPEN;
		}

		if (level.getEnterCondition() != null) {
			// TODO 处理任务控制关卡开放
			if (level.getEnterCondition().getOpenRoleLevel() > role.getLevel()) {
				return KLevelTemplate.GAME_LEVEL_STATE_NOT_OPEN;
			}

			if (level.getEnterCondition().getFrontMissionTemplateId() > 0) {
				boolean missionIsOpen = KSupportFactory.getMissionSupport()
						.checkMissionIsAcceptedOrCompleted(
								role,
								level.getEnterCondition()
										.getFrontMissionTemplateId());
				if (!missionIsOpen) {
					return KLevelTemplate.GAME_LEVEL_STATE_NOT_OPEN;
				}
			}

			if (level.getEnterCondition().getFrontLevelId() > 0) {
				PlayerRoleGamelevelData frontLevelData = levelSet
						.getCopyLevelData(level.getEnterCondition()
								.getFrontLevelId(), level.getLevelType());

				if (frontLevelData == null) {
					return KLevelTemplate.GAME_LEVEL_STATE_NOT_OPEN;
				}

				if (!frontLevelData.isCompleted()) {
					return KLevelTemplate.GAME_LEVEL_STATE_NOT_OPEN;
				}
			}

		}

		return KLevelTemplate.GAME_LEVEL_STATE_OPEN;
	}

	/**
	 * 发送关卡更新数据
	 * 
	 * @param role
	 * @param scenarioId
	 * @param levelData
	 */
	public void sendUpdatePetCopyInfoMsg(KRole role,
			KPetCopyLevelTemplate level, KGameLevelRecord record,
			KGameLevelSet levelSet) {
		boolean isHasLevelData = false;

		if (level != null) {
			isHasLevelData = true;
		}
		KGameMessage sendMsg = KGame
				.newLogicMessage(KLevelProtocol.SM_UPDATE_PET_COPY_DATA);
		sendMsg.writeInt(record.petCopyData.remainChallengeCount);
		sendMsg.writeBoolean(isHasLevelData);
		if (isHasLevelData) {
			sendMsg.writeInt(level.getLevelId());
			byte levelViewType = judgeGameLevelOpenState(role, levelSet, level);

			sendMsg.writeByte(levelViewType);
			PlayerRoleGamelevelData levelData = levelSet.getCopyLevelData(
					level.getLevelId(), level.getLevelType());
			if (levelData != null) {
				byte levelCompleteType = (byte) (levelData.isCompleted() ? 1
						: 0);
				sendMsg.writeByte(levelCompleteType);
				byte levelEvaluate = levelData.getLevelEvaluate(); // 表示关卡的评价（5星，未完成为0）
				sendMsg.writeByte(levelEvaluate);
			} else {
				sendMsg.writeByte(0);
				sendMsg.writeByte(0);
			}
		}
		role.sendMsg(sendMsg);
	}

	public void processPlayerRoleBuyPetCopyCount(KRole role, boolean isNeedCheck) {
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance()
				.getGameLevelSet(role.getId());
		KGameLevelRecord pcRecord = levelSet
				.getCopyRecord(KGameLevelTypeEnum.随从副本关卡);
		if (isNeedCheck) {
			if (pcRecord != null && pcRecord.petCopyData != null) {
				VIPLevelData vipData = KSupportFactory.getVIPModuleSupport()
						.getVIPLevelData(role.getId());
				int vip_buy_count = vipData.rescueHostages.length;
				int buyCount = pcRecord.petCopyData.todayBuyCount;
				if (buyCount < vip_buy_count) {
					int usePoint = vipData.rescueHostages[buyCount];
					KGameLevelModuleExtension
							.getManager()
							.sendLevelTipsMessage(
									role,
									KLevelModuleDialogProcesser.KEY_BUY_PET_COPY,
									LevelTips.getTipsBuyFriendCopyCount(
											usePoint, vipData.lvl,
											(vip_buy_count - buyCount)), true,
									"");
					return;

				} else if (buyCount >= vip_buy_count) {
					int vipLv = KSupportFactory.getVIPModuleSupport().getVipLv(
							role.getId());
					KGameLevelModuleExtension
							.getManager()
							.sendJoinNormalGameLevelTipsMessage(
									role,
									(short) -1,
									LevelTips
											.getTipsJoinFriendCopyFailedCountNotEnough(),
									false, null);
					return;
				}
			}

		}

		levelSet.recordBuyPetCopyCount();

		sendUpdatePetCopyInfoMsg(role, null, pcRecord, null);
	}

	public void sendPetCopyData(KRole role) {
		checkAndResetPetCopyDatas(role, true);

		// 获取关卡记录
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance()
				.getGameLevelSet(role.getId());

		KGameLevelRecord record = levelSet
				.getCopyRecord(KGameLevelTypeEnum.随从副本关卡);

		KGameMessage sendMsg = KGame
				.newLogicMessage(KLevelProtocol.SM_GET_PET_COPY_DATA);

		int remainCount = free_challenge_count;
		if (record != null) {
			remainCount = record.petCopyData.remainChallengeCount;
		}
		sendMsg.writeInt(remainCount);
		sendMsg.writeInt(this.petCopyLevelMap.size());
		for (KPetCopyLevelTemplate level : this.petCopyLevelMap.values()) {
			PlayerRoleGamelevelData levelData = levelSet.getCopyLevelData(
					level.getLevelId(), KGameLevelTypeEnum.随从副本关卡);

			sendMsg.writeInt(level.getLevelId());
			sendMsg.writeByte(level.getLevelType().levelType);
			sendMsg.writeUtf8String(level.getLevelName());
			sendMsg.writeUtf8String(level.getDesc());
			sendMsg.writeByte(level.levelNum);
			sendMsg.writeInt(level.getIconResId());
			sendMsg.writeInt(level.getBossIconResId());
			sendMsg.writeShort(level.getEnterCondition().getOpenRoleLevel());

			byte levelViewType = judgeGameLevelOpenState(role, levelSet, level);

			sendMsg.writeByte(levelViewType);
			sendMsg.writeByte(level.getEnterCondition().getUseStamina());

			if (levelData != null) {
				byte levelCompleteType = (byte) (levelData.isCompleted() ? 1
						: 0);
				sendMsg.writeByte(levelCompleteType);
				sendMsg.writeInt(level.getFightPower());
				byte levelEvaluate = levelData.getLevelEvaluate(); // 表示关卡的评价（5星，未完成为0）
				sendMsg.writeByte(levelEvaluate);
			} else {
				sendMsg.writeByte(0);
				sendMsg.writeInt(level.getFightPower());
				sendMsg.writeByte(0);
			}

			sendMsg.writeByte(level.itemRewardShowList.size());
			for (int i = 0; i < level.itemRewardShowList.size(); i++) {
				ItemCountStruct struct = level.itemRewardShowList.get(i);
				KItemMsgPackCenter.packItem(sendMsg, struct.getItemTemplate(),
						struct.itemCount);
				sendMsg.writeByte(level.itemRewardShowDropRateList.get(i));
			}
			List<ItemCountStruct> sItemList = level.getRewardMap().get(
					role.getLevel()).sItemList;
			sendMsg.writeByte(sItemList.size());
			for (ItemCountStruct struct : sItemList) {
				KItemMsgPackCenter.packItem(sendMsg, struct.getItemTemplate(),
						struct.itemCount);
			}
		}

		role.sendMsg(sendMsg);
	}

	public boolean checkAndResetPetCopyDatas(KRole role, boolean isNeedCheck) {
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance()
				.getGameLevelSet(role.getId());

		return levelSet.checkAndResetPetCopyData(isNeedCheck);
	}

	/**
	 * 处理角色完成关卡，处理关卡结算相关流程
	 * 
	 * @param role
	 * @param level
	 */
	public void processPlayerRoleCompleteCopyLevel(KRole role,
			KPetCopyLevelTemplate level, FightResult result) {
		if (result.getEndType() == TowerFightResult.FIGHT_END_TYPE_ESCAPE) {
			KDialogService.sendNullDialog(role);
		}

		// 关卡开启条件
		KEnterLevelCondition condition = level.getEnterCondition();

		// 获取关卡记录
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance()
				.getGameLevelSet(role.getId());

		KGameLevelRecord record = levelSet.getCopyRecord(level.getLevelType());

		PlayerRoleGamelevelData levelData = null;
		if (record != null) {
			levelData = levelSet.getCopyRecord(level.getLevelType()).levelDataMap
					.get(level.getLevelId());
		}

		KPetCopyReward reward = level.getRewardMap().get(role.getLevel());

		// 计算关卡战斗等级
		byte fightLv = caculateLevelFightEvaluate(role, result, level);

		// 计算关卡所有奖励
		LevelRewardResultData rewardData = caculateLevelReward(role, level,
				result, fightLv);

		// 发送完成关卡消息
		KGameMessage sendMsg = KGame
				.newLogicMessage(KLevelProtocol.SM_SHOW_COMPLETE_GAME_LEVEL_RESULT);
		sendMsg.writeInt(level.getLevelId());
		sendMsg.writeByte(level.getLevelType().levelType);
		sendMsg.writeBoolean(result.isWin());
		sendMsg.writeByte(fightLv);
		sendMsg.writeInt((int) (result.getBattleTime() / 1000));
		sendMsg.writeShort(result.getMaxBeHitCount());
		sendMsg.writeInt(result.getTotalDamage());
		String tips = "";
		if (KSupportFactory.getItemModuleSupport().checkEmptyVolumeInBag(
				role.getId()) < rewardData.totalItemSize) {
			tips = LevelTips.getTipsBagCapacityNotEnough();// "您的背包剩余空间不足，请整理背包。";
		}
		sendMsg.writeUtf8String(tips);
		sendMsg.writeUtf8String("");
		sendMsg.writeUtf8String("");

		sendMsg.writeInt(KGameLevelManager.getNextRoleLvUpgradeExp(role));

		sendMsg.writeByte(rewardData.expAddRate);

		rewardData.baseReward.packMsg(sendMsg);
		sendMsg.writeBoolean(rewardData.isDropItemDouble);
		
		if (fightLv == FightEvaluateData.MAX_FIGHT_LEVEL
				&& rewardData.sLevelReward != null) {
			sendMsg.writeBoolean(true);
			rewardData.sLevelReward.packMsg(sendMsg);
		} else {
			sendMsg.writeBoolean(false);
		}

		// 经验、货币结算显示
		for (int i = 0; i < rewardData.attrAndCurrencyShowData.length; i++) {
			sendMsg.writeBoolean(rewardData.isAttrDouble[i]);
			for (int j = 0; j < rewardData.attrAndCurrencyShowData[i].length; j++) {
				sendMsg.writeInt(rewardData.attrAndCurrencyShowData[i][j]);
			}
		}

		// 处理发送抽奖信息
		if (reward.isHasLotteryReward() && rewardData.lotteryRewardList != null
				&& rewardData.lotteryRewardUsePointList != null
				&& rewardData.lotteryRewardList.size() > 0) {
			sendMsg.writeBoolean(true);
			sendMsg.writeByte(rewardData.lotteryRewardList.size());
			sendMsg.writeInt(rewardData.lotteryRewardList.size());
			for (int i = 0; i < rewardData.lotteryRewardList.size(); i++) {
				ItemCountStruct lotteryReward = rewardData.lotteryRewardList
						.get(i);
				sendMsg.writeByte(i);
				sendMsg.writeByte(2);
				KItemMsgPackCenter.packItem(sendMsg,
						lotteryReward.getItemTemplate(),
						lotteryReward.itemCount);
			}
			for (int i = 0; i < rewardData.lotteryRewardList.size(); i++) {
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
			levelSet.addOrModifyCopyGameLevelData(level.getLevelId(), level
					.getLevelType(), (level.getEnterCondition()
					.getLevelLimitJoinCount() - 1), fightLv, true, false, 0);

			isLevelDataChange = true;
			if (level.getHinderGameLevelList().size() > 0) {
				isCompletedAndTriggerOpenHinderLevel = true;
			}
			isSendUpdateLevelEvaluateInfo = true;
		} else {
			if (levelData.getLevelEvaluate() < fightLv) {
				isSendUpdateLevelEvaluateInfo = true;
			}

			if (!levelData.isCompleted()) {
				if (level.getHinderGameLevelList().size() > 0) {
					isCompletedAndTriggerOpenHinderLevel = true;
				}
			}

			levelSet.addOrModifyCopyGameLevelData(level.getLevelId(), level
					.getLevelType(), 0,
					((levelData.getLevelEvaluate() < fightLv) ? fightLv
							: levelData.getLevelEvaluate()), true, levelData
							.isGetFirstDropPrice(), levelData
							.getTodayRestCount());
			isLevelDataChange = true;
		}

		// 发消息通知用户该关卡记录有发生改变
		if (isLevelDataChange) {
			sendUpdatePetCopyInfoMsg(role, level, record, levelSet);
		}

		// 处理后置关卡开放状态
		if (isCompletedAndTriggerOpenHinderLevel) {
			for (KPetCopyLevelTemplate hinderLevel : level
					.getHinderGameLevelList()) {
				if (level.getEnterCondition().getOpenRoleLevel() <= role
						.getLevel()) {

					sendUpdatePetCopyInfoMsg(role, hinderLevel, record, null);
				}
			}
		}

		// 通知活跃度模块
		KSupportFactory.getRewardModuleSupport().recordFun(role,
				KVitalityTypeEnum.随从营救);
	}

	public byte caculateLevelFightEvaluate(KRole role, FightResult result,
			KPetCopyLevelTemplate level) {
		byte value = -1;
		boolean isComplete = true;
		if (result.getPetCopyResultMap().size() <= 0) {
			value = 1;
			return value;
		}
		for (KPetCopyBattlefieldDropData data : result.getPetCopyResultMap()
				.keySet()) {
			if (!result.getPetCopyResultMap().get(data)) {
				isComplete = false;
			}
		}
		if (!isComplete) {
			value = 1;
			return value;
		}

		// byte temp = -1;
		int battleTime = (int) (result.getBattleTime() / 1000);
		int hitCount = result.getMaxDoubleHitCount();
		int beHitCount = result.getMaxBeHitCount();
		for (FightEvaluateData data : level.getFightEvaluateDataMap(
				role.getJob()).values()) {
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

	public LevelRewardResultData caculateLevelReward(KRole role,
			KPetCopyLevelTemplate level, FightResult fightResult, int fightLv) {
		KPetCopyReward reward = level.getRewardMap().get(role.getLevel());
		LevelRewardResultData resultData = new LevelRewardResultData(
				CompleteGameLevelTempRecord.LOTTERY_TYPE_ITEM);

		// 检测是否有限时产出活动
		boolean isCopyActivityPrice = false;
		float expRate = 1, goldRate = 1, potentialRate = 1;
		int itemMultiple = 1;
		if (level.getLevelType() == KGameLevelTypeEnum.随从副本关卡) {
			TimeLimieProduceActivity activity = KSupportFactory
					.getExcitingRewardSupport().getTimeLimieProduceActivity(
							KLimitTimeProduceActivityTypeEnum.随从副本活动);

			if (activity != null && activity.isActivityTakeEffectNow()) {
				isCopyActivityPrice = true;
				expRate = activity.expRate;
				goldRate = activity.goldRate;
				potentialRate = activity.potentialRate;
				if (activity.isDropItemDouble) {
					int mRate = UtilTool.random(0,
							UtilTool.TEN_THOUSAND_RATIO_UNIT);
					if (mRate >= (UtilTool.TEN_THOUSAND_RATIO_UNIT - activity.itemMultipleRate)) {
						itemMultiple = activity.itemMultiple;
						resultData.isDropItemDouble = true;
					}
				}
				KSupportFactory.getMailModuleSupport().sendSimpleMailBySystem(
						role.getId(), activity.mailTitle, activity.mailContent);
				resultData.isAttrDouble[0] = activity.isExpDouble;
				resultData.isAttrDouble[1] = activity.isGoldDouble;
				resultData.isAttrDouble[2] = activity.isPotentialDouble;
			}
		}

		// 计算关卡数值奖励
		List<AttValueStruct> attList = new ArrayList<AttValueStruct>();
		List<AttValueStruct> attShowList = new ArrayList<AttValueStruct>();

		int baseExp = reward.baseExp;
		int exp = (int) (reward.baseExp * expRate);
		for (KPetCopyBattlefieldDropData data : fightResult
				.getPetCopyResultMap().keySet()) {
			if (data.dropType == KPetCopyDropTypeEnum.MONSTER
					&& fightResult.getPetCopyResultMap().get(data)) {
				float rate = level.getDropTemplateMap().get(data.dropId).expAdditionRate;
				int addExp = (int) (baseExp * rate);
				exp += addExp;
			}

		}
		resultData.attrAndCurrencyShowData[LevelRewardResultData.EXP_LINE][LevelRewardResultData.NORMAL_ROW] = exp;

		attList.add(new AttValueStruct(KGameAttrType.EXPERIENCE, exp));
		attShowList.add(new AttValueStruct(KGameAttrType.EXPERIENCE, exp));

		// 处理关卡货币奖励
		List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();
		List<KCurrencyCountStruct> moneyShowList = new ArrayList<KCurrencyCountStruct>();
		// 金币
		int goldCount = (int) (reward.baseGold * goldRate);
		moneyList.add(new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD,
				goldCount));
		moneyShowList.add(new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD,
				goldCount));
		resultData.attrAndCurrencyShowData[LevelRewardResultData.GOLD_LINE][LevelRewardResultData.NORMAL_ROW] = goldCount;
		processAttributeReward(role, KCurrencyTypeEnum.GOLD, goldCount);
		// 潜能
		int potentialCount = (int) (reward.basePotential * potentialRate);
		moneyList.add(new KCurrencyCountStruct(KCurrencyTypeEnum.POTENTIAL,
				potentialCount));
		moneyShowList.add(new KCurrencyCountStruct(KCurrencyTypeEnum.POTENTIAL,
				potentialCount));
		resultData.attrAndCurrencyShowData[LevelRewardResultData.POTENTIAL_LINE][LevelRewardResultData.NORMAL_ROW] = potentialCount;
		processAttributeReward(role, KCurrencyTypeEnum.GOLD, potentialCount);

		ItemModuleSupport itemSupport = KSupportFactory.getItemModuleSupport();

		// 计算获得道具列表
		List<ItemCountStruct> itemRewardList = new ArrayList<ItemCountStruct>();
		List<ItemCountStruct> itemRewardShowList = new ArrayList<ItemCountStruct>();
		// 再处理关卡常规掉落道具
		List<ItemCountStruct> itemList = reward
				.caculateNormalItemReward(itemMultiple);
		itemRewardList.addAll(itemList);
		itemRewardShowList.addAll(itemList);

		// 计算关卡战斗的掉落
		if (fightResult.getBattleReward() != null) {
			if (!fightResult.getBattleReward().getAdditionalCurrencyReward()
					.isEmpty()) {
				for (KCurrencyTypeEnum currType : fightResult.getBattleReward()
						.getAdditionalCurrencyReward().keySet()) {
					int value = fightResult.getBattleReward()
							.getAdditionalCurrencyReward().get(currType);
					moneyShowList
							.add(new KCurrencyCountStruct(currType, value));
					if (currType == KCurrencyTypeEnum.GOLD) {
						resultData.attrAndCurrencyShowData[LevelRewardResultData.GOLD_LINE][LevelRewardResultData.NORMAL_ROW] += value;
					} else if (currType == KCurrencyTypeEnum.POTENTIAL) {
						resultData.attrAndCurrencyShowData[LevelRewardResultData.POTENTIAL_LINE][LevelRewardResultData.NORMAL_ROW] += value;
					}
				}
				moneyShowList = KCurrencyCountStruct
						.mergeCurrencyCountStructs(moneyShowList);
			}
			// 道具
			if (fightResult.getBattleReward().getAdditionalItemReward().size() > 0) {
				for (String itemCode : fightResult.getBattleReward()
						.getAdditionalItemReward().keySet()) {
					int count = fightResult.getBattleReward()
							.getAdditionalItemReward().get(itemCode);
					ItemCountStruct itemData = new ItemCountStruct(itemCode,
							count);
					if (itemData != null) {
						itemRewardShowList.add(itemData);
					}
				}
			}
		}
		itemRewardShowList = ItemCountStruct
				.mergeItemCountStructs(itemRewardShowList);

		// 处理S级别奖励
		BaseRewardData sLevelReward = null;
		List<ItemCountStruct> sLv_itemRewardList = new ArrayList<ItemCountStruct>();
		if (fightLv == FightEvaluateData.MAX_FIGHT_LEVEL) {
			sLv_itemRewardList.addAll(reward.caculateSReward(1));
			sLevelReward = new BaseRewardData(null, null, sLv_itemRewardList,
					Collections.<Integer> emptyList(),
					Collections.<Integer> emptyList());

			// 金币
			processAttributeReward(role, KCurrencyTypeEnum.GOLD, reward.sGold);
			resultData.attrAndCurrencyShowData[LevelRewardResultData.GOLD_LINE][LevelRewardResultData.S_ROW] = reward.sGold;

			// 潜能
			processAttributeReward(role, KCurrencyTypeEnum.POTENTIAL,
					reward.sPotential);
			resultData.attrAndCurrencyShowData[LevelRewardResultData.POTENTIAL_LINE][LevelRewardResultData.S_ROW] = reward.sPotential;
		}

		// 处理数值和货币奖励
		BaseRewardData baseReward = new BaseRewardData(attShowList,
				moneyShowList, itemRewardShowList,
				Collections.<Integer> emptyList(),
				Collections.<Integer> emptyList());
		KSupportFactory.getRoleModuleSupport().addAttsFromBaseReward(role,
				attList, KRoleAttrModifyType.随从副本奖励, level.getLevelId());

		for (KCurrencyCountStruct attReward : moneyList) {
			processAttributeReward(role, attReward.currencyType,
					attReward.currencyCount);
		}
		// 得出所有的道具奖励，生成等待确认奖励的临时记录
		CompleteGameLevelTempRecord temprecord = new CompleteGameLevelTempRecord(
				CompleteGameLevelTempRecord.LOTTERY_TYPE_ITEM);
		temprecord.setRoleId(role.getId());
		temprecord.setLevelId(level.getLevelId());
		temprecord.setLevelType(level.getLevelType().levelType);
		List<ItemCountStruct> temprecordItemList = new ArrayList<ItemCountStruct>();
		temprecordItemList.addAll(itemRewardList);
		temprecordItemList.addAll(sLv_itemRewardList);
		temprecordItemList = ItemCountStruct
				.mergeItemCountStructs(temprecordItemList);
		temprecord.setItemRewardResultDataList(temprecordItemList);
		List<ItemCountStruct> lotteryRewardList = null;
		List<Integer> lotteryRewardUsePointList = null;
		if (reward.isHasLotteryReward()) {
			temprecord.setHasLotteryReward(true);
			// lotteryRewardList =
			// reward.getLotteryReward().getLotteryGroup()
			// .caculateLotteryRewards(role.getLevel());
			List<NormalItemRewardTemplate> lotteryList = reward.lotteryGroup
					.getCaculateItemRewardList();

			lotteryRewardList = reward.lotteryGroup
					.getLotteryRewardShowItemList(lotteryList);
			lotteryRewardUsePointList = reward.lotteryGroup.lotteryGroupUsePointList;
			temprecord.setLotteryInfo(lotteryList, lotteryRewardUsePointList);
		}

		KGameLevelModuleExtension.getManager().allCompleteGameLevelTempRecord
				.put(temprecord.getRoleId(), temprecord);

		resultData.baseReward = baseReward;
		resultData.sLevelReward = sLevelReward;
		resultData.lotteryRewardList = lotteryRewardList;
		resultData.lotteryRewardUsePointList = lotteryRewardUsePointList;
		resultData.totalItemSize = itemRewardList.size()
				+ sLv_itemRewardList.size();

		return resultData;
	}

	/**
	 * 处理数值奖励
	 * 
	 * @param role
	 * @param type
	 * @param value
	 */
	public boolean processAttributeReward(KRole role, KCurrencyTypeEnum type,
			long value) {
		// 游戏币奖励
		PresentPointTypeEnum presentType = PresentPointTypeEnum.随从副本奖励; // 2014-06-25
																		// 15:54
																		// 所有货币奖励都要注明来源
		// if (type == KCurrencyTypeEnum.DIAMOND) {
		// presentType = PresentPointTypeEnum.关卡奖励;
		// }
		long result = KSupportFactory.getCurrencySupport().increaseMoney(
				role.getId(), type, value, presentType, false);
		if (result != -1) {
			return true;
		} else {
			return false;
		}

	}
}
