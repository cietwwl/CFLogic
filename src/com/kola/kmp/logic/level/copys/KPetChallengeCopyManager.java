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
import com.kola.kmp.logic.combat.ICombatRoleSideHpUpdater;
import com.kola.kmp.logic.combat.KCombatType;
import com.kola.kmp.logic.level.FightEventListener;
import com.kola.kmp.logic.level.FightResult;
import com.kola.kmp.logic.level.KGameBattlefield;
import com.kola.kmp.logic.level.KGameLevelManager;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.level.KGameLevelRecord;
import com.kola.kmp.logic.level.KGameLevelRecord.PetChallengeCopyData;
import com.kola.kmp.logic.level.KGameLevelSet;
import com.kola.kmp.logic.level.KLevelReward;
import com.kola.kmp.logic.level.KLevelTemplate;
import com.kola.kmp.logic.level.KGameLevelManager.LevelRewardResultData;
import com.kola.kmp.logic.level.gamestory.Animation;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.other.KLimitTimeProduceActivityTypeEnum;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.pet.KPet;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.TimeLimitActivityDataManager.TimeLimieProduceActivity;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.ItemModuleSupport;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.LevelTips;
import com.kola.kmp.logic.util.tips.RewardTips;
import com.kola.kmp.logic.vip.KVIPDataManager;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;
import com.kola.kmp.protocol.level.KLevelProtocol;

/**
 * 随从挑战副本管理器
 * 
 * @author Administrator
 * 
 */
public class KPetChallengeCopyManager {
	private static final KGameLogger _LOGGER = KGameLogger.getLogger(KTowerCopyManager.class);

	// 副本的所有关卡MAP
	public static Map<Integer, KLevelTemplate> petChallengeCopyLevelMap = new LinkedHashMap<Integer, KLevelTemplate>();

	// 副本中的所有战场
	public static Map<Integer, KGameBattlefield> allKGameBattlefield = new LinkedHashMap<Integer, KGameBattlefield>();

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
			throw new KGameServerException("读取关卡剧本excel《随从试炼》表头发生错误！", e);
		} catch (IOException e) {
			throw new KGameServerException("读取关卡剧本excel《随从试炼》表头发生错误！", e);
		}

		if (xlsFile != null) {
			loadExcelData(xlsFile);
		}

	}

	private void loadExcelData(KGameExcelFile xlsFile) throws KGameServerException {
		String tableName = "随从试炼";
		int levelDataRowIndex = 5;
		KGameExcelTable levelDataTable = xlsFile.getTable(tableName, levelDataRowIndex);
		KGameExcelRow[] allLevelDataRows = levelDataTable.getAllDataRows();

		if (allLevelDataRows != null) {
			int groupId = 1;
			for (int i = 0; i < allLevelDataRows.length; i++) {
				KLevelTemplate level = new KLevelTemplate();
				level.init(tableName, allLevelDataRows[i], KGameLevelTypeEnum.随从挑战副本关卡);
				level.setLevelNumber((i + 1));
				petChallengeCopyLevelMap.put(level.getLevelId(), level);
				if (level.getEnterCondition().getFrontLevelId() > 0) {
					if (petChallengeCopyLevelMap.containsKey(level.getEnterCondition().getFrontLevelId())) {
						(petChallengeCopyLevelMap.get(level.getEnterCondition().getFrontLevelId()).getHinderGameLevelList()).add(level);
					}
				}

				if (i == 0) {
					firstLevel = level;
				} else if (i == allLevelDataRows.length - 1) {
					endLevel = level;
				}
			}
		}

		tableName = "随从试炼参数";
		levelDataTable = xlsFile.getTable(tableName, levelDataRowIndex);
		allLevelDataRows = levelDataTable.getAllDataRows();
		for (int i = 0; i < allLevelDataRows.length; i++) {
			free_challenge_count = allLevelDataRows[i].getInt("limit_join");
		}
	}

	public void checkInit() throws KGameServerException {
		boolean checkBattlefied = true;
		for (KGameBattlefield battle : allKGameBattlefield.values()) {

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

		KSupportFactory.getCombatModuleSupport().registerCombatHpUpdater(new PetChallengeCopyCombatRoleSideHpUpdater());
	}

	public void sendCopyData(KRole role) {
		// checkAndResetCopyDatas(role, true);
		checkAndResetPetChallengeCopyDatas(role, true);
		// 获取关卡记录
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
		KGameLevelTypeEnum levelTypeEnum = KGameLevelTypeEnum.getEnum(KGameLevelTypeEnum.随从挑战副本关卡.levelType);

		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_GET_PET_CHALLENGE_COPY_DATA);
		KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.随从挑战副本关卡);
		int nowLevelId = firstLevel.getLevelId();
		if (record != null && record.petChallengeCopyData != null) {
			if (record.petChallengeCopyData.nowLevelId == 0) {
				nowLevelId = firstLevel.getLevelId();
			} else {
				nowLevelId = record.petChallengeCopyData.nowLevelId;
			}
		}
		KLevelTemplate nowLevelTemplate = petChallengeCopyLevelMap.get(nowLevelId);
		sendMsg.writeInt(nowLevelId);
		sendMsg.writeByte(KGameLevelTypeEnum.随从挑战副本关卡.levelType);
		sendMsg.writeInt(nowLevelTemplate.getEnterCondition().getUseStamina());
		sendMsg.writeByte(petChallengeCopyLevelMap.size());
		for (KLevelTemplate levelTemplate : petChallengeCopyLevelMap.values()) {
			sendMsg.writeInt(levelTemplate.getLevelId());
			sendMsg.writeUtf8String(levelTemplate.getLevelName());
			sendMsg.writeUtf8String(levelTemplate.getDesc());
			sendMsg.writeInt(levelTemplate.getLevelNumber());
			sendMsg.writeInt(levelTemplate.getBossIconResId());
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

	public boolean checkAndResetPetChallengeCopyDatas(KRole role, boolean isNeedCheck) {
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());

		return levelSet.checkAndResetPetChallengeCopyData(isNeedCheck);
	}

	public void completeOrUpdateCopyInfo(KRole role, int nextLevelId) {
		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_UPDATE_PET_CHALLENGE_COPY_DATA);
		sendMsg.writeInt(nextLevelId);
		sendMsg.writeInt(getRestChallengeCount(role));
		sendMsg.writeInt(free_challenge_count);
		sendMsg.writeInt(getRestVIPSaodangCount(role));
		role.sendMsg(sendMsg);
	}

	public void updateCopyInfo(KRole role) {
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
		KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.随从挑战副本关卡);
		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_UPDATE_PET_CHALLENGE_COPY_DATA);
		sendMsg.writeInt(record.petChallengeCopyData.nowLevelId);
		sendMsg.writeInt(getRestChallengeCount(role));
		sendMsg.writeInt(free_challenge_count);
		sendMsg.writeInt(getRestVIPSaodangCount(role));
		role.sendMsg(sendMsg);
	}

	public int getRestChallengeCount(KRole role) {
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
		KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.随从挑战副本关卡);
		int restCount = free_challenge_count;
		if (record != null && record.petChallengeCopyData != null) {
			restCount = restCount - record.petChallengeCopyData.challengeCount;
			if (restCount < 0) {
				restCount = 0;
			}
		}
		return restCount;
	}
	
	public int getRestVIPSaodangCount(KRole role) {
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
		KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.随从挑战副本关卡);
		VIPLevelData vipData = KSupportFactory.getVIPModuleSupport().getVIPLevelData(role.getId());
		int restCount = vipData.petTestSweepCount;
		if (record != null && record.petChallengeCopyData != null) {
			restCount = restCount - record.petChallengeCopyData.saodangCount;
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
			_LOGGER.error("角色进入随从试炼副本关卡失败，角色为null，关卡ID：" + levelId);
			// KDialogService.sendUprisingDialog(role.getId(),
			// LevelTips.getTipsJoinLevelFailed());
			return new KActionResult(false, LevelTips.getTipsJoinLevelFailed());
		}
		KLevelTemplate level = petChallengeCopyLevelMap.get(levelId);
		if (level == null) {
			_LOGGER.error("角色进入随从挑战副本关卡失败，找不到对应的关卡。角色id:" + role.getId() + "，关卡ID：" + levelId);
			KDialogService.sendUprisingDialog(role.getId(), LevelTips.getTipsJoinLevelFailed());
			return new KActionResult(false, LevelTips.getTipsJoinLevelFailed());
		}
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
		KGameLevelRecord ptRecord = levelSet.getCopyRecord(KGameLevelTypeEnum.随从挑战副本关卡);

		KActionResult joinResult = checkPlayerRoleCanJoinGameLevel(role, levelSet, level, true, true);
		if (joinResult.success) {
			// 当前进入为普通战场模式，取得关卡第一层战场的数据，并通知战斗模块
			if (level.getAllNormalBattlefields().isEmpty()) {
				_LOGGER.error("角色进入关卡失败，找不到对应的第一层战场。角色id:" + role.getId() + "，关卡ID：" + levelId);
				KGameLevelModuleExtension.getManager().sendJoinNormalGameLevelTipsMessage(role, (short) -1, LevelTips.getTipsJoinLevelFailed(), false, null);
				return new KActionResult(false, LevelTips.getTipsJoinLevelFailed());
			}

			if (level.getLevelId() == firstLevel.getLevelId()) {
				levelSet.recordResetPetChallengeCopy();
			}

			// 通知战斗模块，角色进入战场
			List<Animation> animation = new ArrayList<Animation>();
			for (FightEventListener listener : KGameLevelModuleExtension.getManager().getFightEventListenerList()) {
				listener.notifyBattle(role, level.getAllNormalBattlefields(), animation);
			}
			// 活跃度
			KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.随从试炼);
		}

		return joinResult;
	}

	private KActionResult checkPlayerRoleCanJoinGameLevel(KRole role, KGameLevelSet levelSet, KLevelTemplate level, boolean isNeedCheckCondition, boolean isSendDialog) {
		KActionResult result = new KActionResult();
		String tips = "";
		KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.随从挑战副本关卡);
		if (isNeedCheckCondition) {
			// 获取角色当前体力值

			if (level.getLevelId() == endLevel.getLevelId() && record.petChallengeCopyData.isCompleteLastLevel) {
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
			if (record != null && record.petChallengeCopyData != null) {
				if (record.petChallengeCopyData.nowLevelId == 0) {
					nowLevelId = firstLevel.getLevelId();
				} else {
					nowLevelId = record.petChallengeCopyData.nowLevelId;
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
				levelSet.recordChallengePetChallengeCopy();
				// 修改角色体力,减少值为该关卡的消耗体力值
				int useStamina = level.getEnterCondition().getUseStamina();
				String reason = "随从挑战关卡扣除体力";
				KSupportFactory.getRoleModuleSupport().decreasePhyPower(role, useStamina, reason);
			}
		} else {
			if (level.getLevelId() > firstLevel.getLevelId() && level.getEnterCondition().getFrontLevelId() > 0) {
				KLevelTemplate caculateLevel = petChallengeCopyLevelMap.get(level.getEnterCondition().getFrontLevelId());
				rewardData = record.petChallengeCopyData.completeLevelReward;
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
				levelSet.recordCompletePetChallengeCopy(nextLevel.getLevelId(), restHp, restPetHp);
				completeOrUpdateCopyInfo(role, nextLevel.getLevelId());
			} else if (level.getLevelId() == endLevel.getLevelId()) {
				levelSet.recordCompletePetChallengeCopyLastLevel();
			}
		}

		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_SHOW_COMPLETE_PET_CHALLENGE_COPY_BATTLE_RESULT);
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

	public void confirmCompleteAndExitLevel(KRole role, int levelId) {
		// 获取关卡记录
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());

		KLevelTemplate level = petChallengeCopyLevelMap.get(levelId);

		KGameLevelRecord record = levelSet.getCopyRecord(level.getLevelType());
		int nowLevelId = 0;
		if (record != null && record.petChallengeCopyData != null) {
			nowLevelId = record.petChallengeCopyData.nowLevelId;
		}
		if (nowLevelId <= firstLevel.getLevelId()) {
			return;
		} else {
			KLevelTemplate caculateLevel;
			if (nowLevelId > levelId) {
				caculateLevel = level;
			} else {
				caculateLevel = petChallengeCopyLevelMap.get(level.getEnterCondition().getFrontLevelId());
			}
			BaseRewardData rewardData = record.petChallengeCopyData.completeLevelReward;
			if (!rewardData.sendReward(role, PresentPointTypeEnum.随从挑战副本奖励)) {
				BaseMailContent mainContent = new BaseMailContent(LevelTips.getTipsSendMailItemForBagFullTitle(), LevelTips.getTipsSendMailItemForBagFull(), null, null);
				KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), mainContent, rewardData, PresentPointTypeEnum.随从挑战副本奖励);

				KDialogService.sendDataUprisingDialog(role, RewardTips.背包已满奖励通过邮件发送);
			}
		}

		levelSet.recordResetPetChallengeCopy();

		completeOrUpdateCopyInfo(role, firstLevel.getLevelId());
	}

	public void resetCopyAndGetReward(KRole role) {
		// 获取关卡记录
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
		KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.随从挑战副本关卡);
		int nowLevelId = 0;
		if (record != null && record.petChallengeCopyData != null) {
			nowLevelId = record.petChallengeCopyData.nowLevelId;
		}
		if (nowLevelId <= firstLevel.getLevelId()) {
			KDialogService.sendDataUprisingDialog(role, LevelTips.getTipsRestCopyLevelFailedWhileNotFinished());
			return;
		}
		KLevelTemplate level = petChallengeCopyLevelMap.get((petChallengeCopyLevelMap.get(nowLevelId).getEnterCondition().getFrontLevelId()));

		BaseRewardData rewardData = record.petChallengeCopyData.completeLevelReward;
		if (!rewardData.sendReward(role, PresentPointTypeEnum.随从挑战副本奖励)) {
			BaseMailContent mainContent = new BaseMailContent(LevelTips.getTipsSendMailItemForBagFullTitle(), LevelTips.getTipsSendMailItemForBagFull(), null, null);
			KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), mainContent, rewardData, PresentPointTypeEnum.爬塔副本奖励);

			KDialogService.sendUprisingDialog(role, RewardTips.背包已满奖励通过邮件发送);
		} else {
			KDialogService.sendDataUprisingDialog(role, rewardData.dataUprisingTips);
		}

		levelSet.recordResetPetChallengeCopy();

		completeOrUpdateCopyInfo(role, firstLevel.getLevelId());

		KDialogService.sendNullDialog(role);
	}

	public BaseRewardData caculateLevelReward(KRole role, KLevelTemplate level, KGameLevelSet levelSet) {
		KGameLevelRecord record = levelSet.getCopyRecord(level.getLevelType());

		if (record.petChallengeCopyData == null) {
			record.petChallengeCopyData = new PetChallengeCopyData();
			record.notifyDB();
		}

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

		attList.addAll(record.petChallengeCopyData.completeLevelReward.attList);
		moneyList.addAll(record.petChallengeCopyData.completeLevelReward.moneyList);
		itemRewardList.addAll(record.petChallengeCopyData.completeLevelReward.itemStructs);

		attList = AttValueStruct.mergeCountStructs(attList);
		moneyList = KCurrencyCountStruct.mergeCurrencyCountStructs(moneyList);
		itemRewardList = ItemCountStruct.mergeItemCountStructs(itemRewardList);

		baseReward = new BaseRewardData(attList, moneyList, itemRewardList, null, null);

		levelSet.recordUpdatePetChallengeCopyReward(baseReward);

		return baseReward;
	}

	public BaseRewardData caculateSaodangLevelReward(KRole role, KGameLevelSet levelSet) {
		KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.随从挑战副本关卡);

		BaseRewardData baseReward;
		List<AttValueStruct> attList = new ArrayList<AttValueStruct>();
		List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();
		List<ItemCountStruct> itemRewardList = new ArrayList<ItemCountStruct>();
		if (record.petChallengeCopyData.nowLevelId > firstLevel.getLevelId() && record.petChallengeCopyData.completeLevelReward != null) {
			attList.addAll(record.petChallengeCopyData.completeLevelReward.attList);
			moneyList.addAll(record.petChallengeCopyData.completeLevelReward.moneyList);
			itemRewardList.addAll(record.petChallengeCopyData.completeLevelReward.itemStructs);
		}

		// 检测是否有限时产出活动
		boolean isCopyActivityPrice = false;
		float expRate = 1, goldRate = 1, potentialRate = 1;
		int itemMultiple = 1;

		for (int levelId = record.petChallengeCopyData.nowLevelId; levelId <= endLevel.getLevelId(); levelId++) {
			KLevelReward reward = petChallengeCopyLevelMap.get(levelId).getReward();

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

			attList.addAll(record.petChallengeCopyData.completeLevelReward.attList);
			moneyList.addAll(record.petChallengeCopyData.completeLevelReward.moneyList);
			itemRewardList.addAll(record.petChallengeCopyData.completeLevelReward.itemStructs);
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
		if (frontLevelId > 0 && petChallengeCopyLevelMap.containsKey(frontLevelId)) {
			KLevelTemplate frontLevel = petChallengeCopyLevelMap.get(frontLevelId);
			getFrontLevelList(frontLevel, caculateLevelList);
		} else {
			return;
		}
	}

	public void processSaodangCopy(KRole role) {
		if (role == null) {
			_LOGGER.error("角色扫荡随从试炼副本关卡失败，角色为null。");
			sendSaodangFailedMsg(role);
			KDialogService.sendUprisingDialog(role.getId(), GlobalTips.服务器繁忙请稍候再试);
			return;
		}

		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
		KGameLevelRecord ptRecord = levelSet.getCopyRecord(KGameLevelTypeEnum.随从挑战副本关卡);

		if (!ptRecord.petChallengeCopyData.isPassCopy) {
			sendSaodangFailedMsg(role);
			KDialogService.sendUprisingDialog(role.getId(), LevelTips.getTipsCannotSaodang1());
			return;
		}

		if (ptRecord.petChallengeCopyData.nowLevelId == endLevel.getLevelId() && ptRecord.petChallengeCopyData.isCompleteLastLevel) {
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
		if (ptRecord.petChallengeCopyData.saodangCount >= vipData.petTestSweepCount) {
			sendSaodangFailedMsg(role);
			KDialogService.sendUprisingDialog(role, LevelTips.getTipsSaodangMaxCount(vipData.petTestSweepCount));
			return;
		}
		// 检测体力
		if (ptRecord.petChallengeCopyData.nowLevelId == firstLevel.getLevelId()) {
			// 获取角色当前体力值
			int roleStamina = role.getPhyPower();
			int useStamina = firstLevel.getEnterCondition().getUseStamina();

			if (useStamina > 0 && roleStamina < useStamina) {
				sendSaodangFailedMsg(role);
				KGameLevelModuleExtension.getManager().checkAndSendPhyPowerNotEnoughDialog(role);
				return;
			}
			KSupportFactory.getRoleModuleSupport().decreasePhyPower(role, useStamina, "随从挑战关卡扣除体力");
		}
		
		BaseRewardData rewardData = caculateSaodangLevelReward(role, levelSet);
		
		if (!rewardData.sendReward(role, PresentPointTypeEnum.随从挑战副本奖励)) {
			BaseMailContent mainContent = new BaseMailContent(LevelTips.getTipsSendMailItemForBagFullTitle(), LevelTips.getTipsSendMailItemForBagFull(), null, null);
			KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), mainContent, rewardData, PresentPointTypeEnum.随从挑战副本奖励);

			KDialogService.sendDataUprisingDialog(role, RewardTips.背包已满奖励通过邮件发送);
		}
		
		levelSet.recordSaodangPetChallengeCopy();
		
		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_VIP_SAODANG_PET_CHALLENGE_COPY);
		sendMsg.writeBoolean(true);
		rewardData.packMsg(sendMsg);
		role.sendMsg(sendMsg);
		
		completeOrUpdateCopyInfo(role, firstLevel.getLevelId());

		KDialogService.sendNullDialog(role);
	}

	public static void sendSaodangFailedMsg(KRole role) {
		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_VIP_SAODANG_PET_CHALLENGE_COPY);
		sendMsg.writeBoolean(false);
		role.sendMsg(sendMsg);
	}

	public static class PetChallengeCopyCombatRoleSideHpUpdater implements ICombatRoleSideHpUpdater {

		@Override
		public KCombatType getCombatTypeResponse() {
			return KCombatType.PET_CHALLENGE_COPY;
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
			KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.随从挑战副本关卡);
			if (record != null && record.petChallengeCopyData != null) {
				if (record.petChallengeCopyData.restHp > 0) {
					roleHp = record.petChallengeCopyData.restHp;
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
			KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.随从挑战副本关卡);
			if (record != null && record.petChallengeCopyData != null) {
				if (record.petChallengeCopyData.restPetHp > 0) {
					petHp = record.petChallengeCopyData.restPetHp;
				}
			}
			return petHp;
		}
	}

}
