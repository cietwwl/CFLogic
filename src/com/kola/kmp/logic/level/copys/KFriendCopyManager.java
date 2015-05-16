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
import com.kola.kmp.logic.competition.KCompetitionModule;
import com.kola.kmp.logic.competition.KCompetitor;
import com.kola.kmp.logic.item.KItemMsgPackCenter;
import com.kola.kmp.logic.level.FightEvaluateData;
import com.kola.kmp.logic.level.FightEventListener;
import com.kola.kmp.logic.level.FightResult;
import com.kola.kmp.logic.level.KEnterLevelCondition;
import com.kola.kmp.logic.level.KGameBattlefield;
import com.kola.kmp.logic.level.KGameLevelManager;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.level.KGameLevelRecord;
import com.kola.kmp.logic.level.KGameLevelSet;
import com.kola.kmp.logic.level.KLevelModuleDialogProcesser;
import com.kola.kmp.logic.level.KLevelReward;
import com.kola.kmp.logic.level.KLevelTemplate;
import com.kola.kmp.logic.level.KGameLevelManager.CompleteGameLevelTempRecord;
import com.kola.kmp.logic.level.KGameLevelManager.LevelRewardResultData;
import com.kola.kmp.logic.level.KGameLevelRecord.PlayerRoleGamelevelData;
import com.kola.kmp.logic.level.KGameLevelRecord.StrangerData;
import com.kola.kmp.logic.level.TowerFightResult;
import com.kola.kmp.logic.level.gamestory.Animation;
import com.kola.kmp.logic.level.tower.KTowerBattlefield;
import com.kola.kmp.logic.level.tower.KTowerData;
import com.kola.kmp.logic.level.tower.KTowerDataManager;
import com.kola.kmp.logic.level.tower.KTowerLevelTemplate;
import com.kola.kmp.logic.level.tower.KTowerReward;
import com.kola.kmp.logic.map.KGameMapEntity;
import com.kola.kmp.logic.map.KGameNormalMap;
import com.kola.kmp.logic.map.KMapModule;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.other.KGangTecTypeEnum;
import com.kola.kmp.logic.other.KLimitTimeProduceActivityTypeEnum;
import com.kola.kmp.logic.other.KMapEntityTypeEnum;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.TimeLimitActivityDataManager.TimeLimieProduceActivity;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.ItemModuleSupport;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.GambleTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.LevelTips;
import com.kola.kmp.logic.util.tips.RewardTips;
import com.kola.kmp.logic.util.tips.TimeLimitProducActivityTips;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;
import com.kola.kmp.protocol.level.KLevelProtocol;

public class KFriendCopyManager {
	private static final KGameLogger _LOGGER = KGameLogger.getLogger(KFriendCopyManager.class);
	// 副本免费挑战次数
	public static int free_challenge_count = 3;

	// 被邀请的好友当天最大的发奖次数
	public static int max_be_invite_price_count = 5;
	// 好友副本的所有关卡MAP
	private Map<Integer, KTowerLevelTemplate> friendCopyLevelMap = new LinkedHashMap<Integer, KTowerLevelTemplate>();

	public Map<Integer, KTowerBattlefield> allKTowerBattlefield = new LinkedHashMap<Integer, KTowerBattlefield>();

	// // 购买挑战次数使用钻石表
	// public static Map<Integer, Integer> buyChallengeCountUsePointMap = new
	// LinkedHashMap<Integer, Integer>();

	// 角色等级系数(万分比)
	private Map<Integer, Integer> roleLvRatio = new LinkedHashMap<Integer, Integer>();
	private Map<Integer, Integer> friendLvRatio = new LinkedHashMap<Integer, Integer>();

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

	private void loadExcelData(KGameExcelFile xlsFile, KGameLevelTypeEnum levelType) throws KGameServerException {
		// 关卡数据
		String tableName = "好友副本";
		int levelDataRowIndex = 5;
		KGameExcelTable levelDataTable = xlsFile.getTable(tableName, levelDataRowIndex);
		KGameExcelRow[] allLevelDataRows = levelDataTable.getAllDataRows();

		if (allLevelDataRows != null) {
			for (int i = 0; i < allLevelDataRows.length; i++) {
				KTowerLevelTemplate level = new KTowerLevelTemplate();
				level.init(tableName, allLevelDataRows[i], levelType);
				level.setLevelNum((byte) (i + 1));

				friendCopyLevelMap.put(level.getLevelId(), level);
				if (level.getEnterCondition().getFrontLevelId() > 0) {
					if (friendCopyLevelMap.containsKey(level.getEnterCondition().getFrontLevelId())) {
						(friendCopyLevelMap.get(level.getEnterCondition().getFrontLevelId()).getHinderGameLevelList()).add(level);
					}
				}

				KTowerBattlefield battle = new KTowerBattlefield();
				battle.initBattlefield(level.getLevelId(), tableName, allLevelDataRows[i]);

				level.setTowerBattlefield(battle);
				this.allKTowerBattlefield.put(battle.getBattlefieldId(), battle);

				for (int j = 1; j <= battle.getTotalWave() && j <= 20; j++) {
					if (j % 5 == 0) {
						KTowerData data = battle.getTowerDataMapByWaveNum().get(j);
						KTowerReward reward = KTowerDataManager.getTowerReward(data.getTowerId());
						List<ItemCountStruct> list = reward.getReward().itemStructs;
						ItemCountStruct struct = list.get(list.size() - 1);
						level.getWavePriceBoxMap().put(j, struct);
					}
				}
			}
		}

		// tableName = "好友副本购买挑战次数消耗钻石";
		// levelDataTable = xlsFile.getTable(tableName, levelDataRowIndex);
		// allLevelDataRows = levelDataTable.getAllDataRows();
		//
		// if (allLevelDataRows != null) {
		// for (int i = 0; i < allLevelDataRows.length; i++) {
		// int count = allLevelDataRows[i].getInt("buyCount");
		// int point = allLevelDataRows[i].getInt("point");
		// buyChallengeCountUsePointMap.put(count, point);
		// }
		// }

		tableName = "好友副本奖励角色等级系数";
		levelDataTable = xlsFile.getTable(tableName, levelDataRowIndex);
		allLevelDataRows = levelDataTable.getAllDataRows();

		if (allLevelDataRows != null) {
			for (int i = 0; i < allLevelDataRows.length; i++) {
				int lv = allLevelDataRows[i].getInt("roleLv");
				int rate = allLevelDataRows[i].getInt("rate");
				int fRate = allLevelDataRows[i].getInt("levelrate");
				roleLvRatio.put(lv, rate);
				friendLvRatio.put(lv, fRate);
			}
		}
	}

	public void sendFriendCopyData(KRole role) {
		checkAndResetFriendCopyDatas(role, true);
		checkAndReflashStrangerData(role);

		// 获取关卡记录
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());

		KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.好友副本关卡);

		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_GET_FRIEND_COPY_DATA);

		int remainCount = free_challenge_count;
		if (record != null) {
			remainCount = record.friendCopyData.remainChallengeCount;
		}
		sendMsg.writeInt(remainCount);
		sendMsg.writeInt(this.friendCopyLevelMap.size());
		for (KTowerLevelTemplate level : this.friendCopyLevelMap.values()) {
			PlayerRoleGamelevelData levelData = levelSet.getCopyLevelData(level.getLevelId(), KGameLevelTypeEnum.好友副本关卡);

			sendMsg.writeInt(level.getLevelId());
			sendMsg.writeByte(level.getLevelType().levelType);
			sendMsg.writeUtf8String(level.getLevelName());
			sendMsg.writeUtf8String(level.getDesc());
			sendMsg.writeByte(level.getLevelNum());
			sendMsg.writeInt(level.getIconResId());
			sendMsg.writeInt(level.getBossIconResId());
			sendMsg.writeShort(level.getEnterCondition().getOpenRoleLevel());

			byte levelViewType = judgeGameLevelOpenState(role, levelSet, level);

			sendMsg.writeByte(levelViewType);
			sendMsg.writeByte(level.getEnterCondition().getUseStamina());

			if (levelData != null) {
				byte levelCompleteType = (byte) (levelData.isCompleted() ? 1 : 0);
				sendMsg.writeByte(levelCompleteType);
				sendMsg.writeInt(level.getFightPower());
				byte levelEvaluate = levelData.getLevelEvaluate(); // 表示关卡的评价（5星，未完成为0）
				sendMsg.writeByte(levelEvaluate);
				sendMsg.writeInt(level.getTowerBattlefield().getTotalWave());
				sendMsg.writeInt(levelData.getMaxWave());
			} else {
				sendMsg.writeByte(0);
				sendMsg.writeInt(level.getFightPower());
				sendMsg.writeByte(0);
				sendMsg.writeInt(level.getTowerBattlefield().getTotalWave());
				sendMsg.writeInt(0);
			}

			// sendMsg.writeByte(level.getWavePriceBoxMap().size());
			// for (Integer waveId : level.getWavePriceBoxMap().keySet()) {
			// ItemCountStruct struct = level.getWavePriceBoxMap().get(waveId);
			// sendMsg.writeByte(waveId);
			// KItemMsgPackCenter.packItem(sendMsg, struct.getItemTemplate(),
			// struct.itemCount);
			// }

			BaseRewardData reward = KTowerDataManager.getTowerReward(level.getTowerBattlefield().getEndTowerId()).getReward();
			reward.packMsg(sendMsg);
		}

		/**
		 * 陌生人数据
		 */
		if (record != null && record.friendCopyData != null && !record.friendCopyData.strangers.isEmpty()) {
			sendMsg.writeByte(record.friendCopyData.strangers.size());
			for (StrangerData data : record.friendCopyData.strangers.values()) {
				sendMsg.writeLong(data.roleId);
				sendMsg.writeUtf8String(data.roleName);
				sendMsg.writeByte(data.job);
				sendMsg.writeShort(data.lv);
				sendMsg.writeInt(data.fightPower);
			}
		} else {
			sendMsg.writeByte(0);
		}

		role.sendMsg(sendMsg);
	}

	/**
	 * 处理角色进入关卡
	 * 
	 * @param role
	 * @param friendId
	 *            好友角色ID，当角色ID=-1时，表示不需要好友辅助
	 * @param levelId
	 * @param isNeedCheck
	 *            是否需要检测进入条件
	 */
	public KActionResult playerRoleJoinGameLevel(KRole role, long friendId, int levelId, boolean isNeedCheck) {
		if (role == null) {
			_LOGGER.error("角色进入好友副本关卡失败，角色为null，关卡ID：" + levelId + "，关卡类型:" + KGameLevelTypeEnum.好友副本关卡);
			KDialogService.sendUprisingDialog(role.getId(), LevelTips.getTipsJoinLevelFailed());
			return new KActionResult(false, LevelTips.getTipsJoinLevelFailed());
		}
		KRole friend = null;
		if (friendId != -1) {
			friend = KSupportFactory.getRoleModuleSupport().getRole(friendId);
			if (friend == null) {
				_LOGGER.error("角色进入好友副本关卡失败，好友角色为null，好友角色ID：" + friendId + "，关卡ID：" + levelId + "，关卡类型:" + KGameLevelTypeEnum.好友副本关卡);
				KDialogService.sendUprisingDialog(role.getId(), LevelTips.getTipsJoinFriendCopyFailedFriendNotFound());
				return new KActionResult(false, LevelTips.getTipsJoinFriendCopyFailedFriendNotFound());
			}
		}

		KTowerLevelTemplate level = this.friendCopyLevelMap.get(levelId);
		if (level == null) {
			_LOGGER.error("角色进入副本关卡失败，找不到对应的关卡。角色id:" + role.getId() + "，关卡ID：" + levelId + "，关卡类型:" + KGameLevelTypeEnum.好友副本关卡);
			KDialogService.sendUprisingDialog(role.getId(), LevelTips.getTipsJoinLevelFailed());
			return new KActionResult(false, LevelTips.getTipsJoinLevelFailed());
		}

		KEnterLevelCondition condition = level.getEnterCondition();

		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());

		PlayerRoleGamelevelData levelData = null;
		KGameLevelRecord fRecord = levelSet.getCopyRecord(KGameLevelTypeEnum.好友副本关卡);
		if (fRecord != null) {
			levelData = fRecord.getLevelDataMap().get(level.getLevelId());
		}
		if (isNeedCheck) {
			// 判断关卡是否开放
			byte levelOpenType = judgeGameLevelOpenState(role, levelSet, level);
			if (levelOpenType != KLevelTemplate.GAME_LEVEL_STATE_OPEN) {
				KGameLevelModuleExtension.getManager().sendJoinNormalGameLevelTipsMessage(role, (short) -1, LevelTips.getTipsLevelNotOpen(), false, null);
				return new KActionResult(false, LevelTips.getTipsLevelNotOpen());
			}

			if (fRecord != null && fRecord.friendCopyData != null) {
				int remainChallengeCount = fRecord.friendCopyData.remainChallengeCount;
				int challengeCount = fRecord.friendCopyData.challengeCount;
				int buyCount = fRecord.friendCopyData.todayBuyCount;
				if (remainChallengeCount <= 0) {
					VIPLevelData vipData = KSupportFactory.getVIPModuleSupport().getVIPLevelData(role.getId());
					int vip_buy_count = vipData.friendbuyrmb.length;
					if (buyCount < vip_buy_count) {
						int usePoint = vipData.friendbuyrmb[buyCount];
						KGameLevelModuleExtension.getManager().sendLevelTipsMessage(role, KLevelModuleDialogProcesser.KEY_BUY_FRIEND_COPY, LevelTips.getTipsJoinFriendCopyFailedUsePoint(usePoint),
								true, "");
						return new KActionResult(false, LevelTips.getTipsJoinFriendCopyFailedUsePoint(usePoint));

					} else if (buyCount >= vip_buy_count) {
						int vipLv = KSupportFactory.getVIPModuleSupport().getVipLv(role.getId());
						KGameLevelModuleExtension.getManager().sendJoinNormalGameLevelTipsMessage(role, (short) -1, LevelTips.getTipsJoinFriendCopyFailedCountNotEnough(), false, null);
						return new KActionResult(false, LevelTips.getTipsJoinFriendCopyFailedCountNotEnough());
					}
				}

				if (fRecord.friendCopyData.friendCoolingTimeMap.containsKey(friendId) || fRecord.friendCopyData.strangerCoolingTimeMap.containsKey(friendId)) {
					KDialogService.sendUprisingDialog(role.getId(), LevelTips.getTipsJoinFriendCopyFailedFriendIsCooling());
					return new KActionResult(false, LevelTips.getTipsJoinFriendCopyFailedFriendIsCooling());
				}
			} else {
				KGameLevelModuleExtension.getManager().sendJoinNormalGameLevelTipsMessage(role, (short) -1, LevelTips.getTipsLevelNotOpen(), false, null);
				return new KActionResult(false, LevelTips.getTipsLevelNotOpen());
			}
		}
		// 技术好友冷却时间
		if (friendId > 0 && friend != null) {
			long coolEndTime = getFriendCoolingTime(role.getLevel(), friend.getLevel());
			levelSet.recordChallengeFriendCopy(friendId, coolEndTime);
		} else {
			levelSet.recordChallengeFriendCopy(-1, 0);
		}

		sendUpdateFriendCopyInfoMsg(role, null, fRecord, null);

		// 通知战斗模块，角色进入战场
		for (FightEventListener listener : KGameLevelModuleExtension.getManager().getFightEventListenerList()) {
			listener.notifyFriendTowerBattle(role, friendId, level.getTowerBattlefield());
		}

		KFriendCopyActivity.getInstance().sendUpdateActivity(role);

		return new KActionResult(true, "");
	}

	/**
	 * 计算好友冷却时间
	 * 
	 * @param roleLv
	 * @param friendLv
	 * @return
	 */
	private long getFriendCoolingTime(int roleLv, int friendLv) {
		long coolTime = System.currentTimeMillis();

		int lv_d_value = Math.abs(roleLv - friendLv);

		if (lv_d_value >= 0 && lv_d_value <= 3) {
			coolTime += 10 * 60 * 1000;
		} else if (lv_d_value >= 4 && lv_d_value <= 8) {
			coolTime += 60 * 60 * 1000;
		} else {
			coolTime += 3 * 60 * 60 * 1000;
		}
		return coolTime;
	}

	/**
	 * 获取冷却中的好友与陌生人的数据，并发送给客户端
	 * 
	 * @param role
	 */
	public void processGetFriendCoolingTime(KRole role) {
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
		KGameLevelRecord fRecord = levelSet.getCopyRecord(KGameLevelTypeEnum.好友副本关卡);
		if (fRecord != null) {
			levelSet.checkAndResetFriendCopyData(true);

			long nowTime = System.currentTimeMillis();

			KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_GET_FRIEND_COPY_PARTNER_STATUS);
			// 好友
			sendMsg.writeInt(fRecord.friendCopyData.friendCoolingTimeMap.size());
			for (Long roleId : fRecord.friendCopyData.friendCoolingTimeMap.keySet()) {
				long restTime = fRecord.friendCopyData.friendCoolingTimeMap.get(roleId) - nowTime;
				int time = 0;
				if (restTime > 0) {
					time = (int) (restTime / 1000);
				}
				sendMsg.writeLong(roleId);
				sendMsg.writeInt(time);
			}
			// 陌生人
			sendMsg.writeInt(fRecord.friendCopyData.strangerCoolingTimeMap.size());
			for (Long roleId : fRecord.friendCopyData.strangerCoolingTimeMap.keySet()) {
				long restTime = fRecord.friendCopyData.strangerCoolingTimeMap.get(roleId) - nowTime;
				int time = 0;
				if (restTime > 0) {
					time = (int) (restTime / 1000);
				}
				sendMsg.writeLong(roleId);
				sendMsg.writeInt(time);
			}
			role.sendMsg(sendMsg);
		} else {
			KDialogService.sendUprisingDialog(role, GlobalTips.getTipsServerBusy());
		}
	}

	/**
	 * 处理角色完成关卡，处理关卡结算相关流程
	 * 
	 * @param role
	 * @param lv
	 */
	public boolean processPlayerRoleCompleteCopyLevel(KRole role, TowerFightResult result) {

		if (result.getEndType() == TowerFightResult.FIGHT_END_TYPE_ESCAPE) {
			KDialogService.sendNullDialog(role);
		}

		int battlefieldId = result.getBattlefieldId();
		KTowerLevelTemplate level = null;
		if (this.allKTowerBattlefield.containsKey(battlefieldId)) {
			KTowerBattlefield battlefield = this.allKTowerBattlefield.get(battlefieldId);
			level = this.friendCopyLevelMap.get(battlefield.getLevelId());
		} else {
			// playerRoleCompleteBattlefield方法处理找不到战场数据情况
			// sendBattleFaildResultMessage(role, result.getBattlefieldId(),
			// result.getBattlefieldType().battlefieldType);
			_LOGGER.error("### Exctpeion----战斗结束关卡剧本模块找不到对应的战场实例，" + "战场ID:" + result.getBattlefieldId() + "，战场类型：" + result.getBattlefieldType().battlefieldType);
			return false;

		}

		// 关卡开启条件
		KEnterLevelCondition condition = level.getEnterCondition();

		// 获取关卡记录
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());

		KGameLevelRecord record = levelSet.getCopyRecord(level.getLevelType());

		PlayerRoleGamelevelData levelData = null;
		if (record != null) {
			levelData = levelSet.getCopyRecord(level.getLevelType()).levelDataMap.get(level.getLevelId());
		}

		// 计算关卡战斗等级
		byte fightLv = caculateLevelFightEvaluate(result, level);

		LevelRewardResultData rewardData = caculateLevelReward(role, level, result, fightLv);

		// 发送完成关卡消息
		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_SHOW_COMPLETE_GAME_LEVEL_RESULT);
		sendMsg.writeInt(level.getLevelId());
		sendMsg.writeByte(level.getLevelType().levelType);
		sendMsg.writeBoolean(true);
		sendMsg.writeByte(fightLv);
		sendMsg.writeInt((int) (result.getBattleTime() / 1000));
		sendMsg.writeShort(result.getMaxBeHitCount());
		sendMsg.writeInt(result.getTotalDamage());

		// 当前波数
		sendMsg.writeInt(result.getFinishWave());
		int friendDoubleHit = 0;// 好友连击数
		int friendDamage = 0; // 好友总伤害
		byte friendJobType = -1; // 好友职业类型

		if (result.getFriendId() > 0) {
			KRole friend = KSupportFactory.getRoleModuleSupport().getRole(result.getFriendId());
			if (friend != null) {
				friendJobType = friend.getJob();
			}
			friendDoubleHit = result.getMaxFriendDoubleHitCount();
			friendDamage = result.getFriendTotalDamage();
		}
		sendMsg.writeInt(friendDoubleHit);
		sendMsg.writeInt(friendDamage);
		sendMsg.writeByte(friendJobType);

		String tips = "";
		if (KSupportFactory.getItemModuleSupport().checkEmptyVolumeInBag(role.getId()) < rewardData.totalItemSize) {
			tips = LevelTips.getTipsBagCapacityNotEnough();// "您的背包剩余空间不足，请整理背包。";
		}
		sendMsg.writeUtf8String(tips);
		sendMsg.writeUtf8String("");
		sendMsg.writeUtf8String("");

		sendMsg.writeInt(KGameLevelManager.getNextRoleLvUpgradeExp(role));

		// 经验加成
		sendMsg.writeByte(rewardData.expAddRate);

		rewardData.baseReward.packMsg(sendMsg);
		sendMsg.writeBoolean(rewardData.isDropItemDouble);

		// 没S级别奖励
		sendMsg.writeBoolean(false);
		// 经验、货币结算显示
		for (int i = 0; i < rewardData.attrAndCurrencyShowData.length; i++) {
			sendMsg.writeBoolean(rewardData.isAttrDouble[i]);
			for (int j = 0; j < rewardData.attrAndCurrencyShowData[i].length; j++) {
				sendMsg.writeInt(rewardData.attrAndCurrencyShowData[i][j]);
			}
		}

		// 没有抽奖信息
		sendMsg.writeBoolean(false);

		role.sendMsg(sendMsg);

		// 处理个人关卡记录，计算关卡评价
		boolean isLevelDataChange = false;
		boolean isCompletedAndTriggerOpenHinderLevel = false;
		// 是否检测星级评定并发送更新信息
		boolean isSendUpdateLevelEvaluateInfo = false;

		if (levelData == null) {
			// 找不到关卡记录，则添加记录并将状态改为完成状态，设入关卡评价
			levelSet.addOrModifyFriendCopyData(level.getLevelId(), level.getLevelType(), 0, fightLv, true, result.getFinishWave());

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

			int nowWave = result.getFinishWave();
			int lastWave = levelData.getMaxWave();
			if (nowWave > lastWave || levelData.getLevelEvaluate() < fightLv) {
				levelSet.addOrModifyFriendCopyData(level.getLevelId(), level.getLevelType(), 0, ((levelData.getLevelEvaluate() < fightLv) ? fightLv : levelData.getLevelEvaluate()), true,
						((nowWave > lastWave) ? nowWave : lastWave));
				isLevelDataChange = true;
			}
		}

		// 发消息通知用户该关卡记录有发生改变
		if (isLevelDataChange) {
			sendUpdateFriendCopyInfoMsg(role, level, record, levelSet);
		}

		// 处理后置关卡开放状态
		if (isCompletedAndTriggerOpenHinderLevel) {
			for (KTowerLevelTemplate hinderLevel : level.getHinderGameLevelList()) {
				if (level.getEnterCondition().getOpenRoleLevel() <= role.getLevel()) {

					sendUpdateFriendCopyInfoMsg(role, hinderLevel, record, null);
				}
			}
		}
		// 通知好友模块增加亲密度，计算好友奖励
		if (result.getFriendId() > 0) {
			KRole friendRole = KSupportFactory.getRoleModuleSupport().getRole(result.getFriendId());
			if (friendRole != null) {
				if (KSupportFactory.getRelationShipModuleSupport().isInFriendList(role.getId(), result.getFriendId())) {
					KSupportFactory.getRelationShipModuleSupport().notifyCloseAction_WAR(role.getId(), result.getFriendId(), result.isWin());

					KGameLevelSet fLevelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(friendRole.getId());
					KGameLevelRecord fRecord = fLevelSet.getCopyRecord(level.getLevelType());

					boolean isSendFriendPrice = true;
					if (fRecord != null && fRecord.friendCopyData != null) {
						if (fRecord.friendCopyData.beInviteCount >= max_be_invite_price_count) {
//							KSupportFactory.getMailModuleSupport().sendSimpleMailBySystem(friendRole.getId(), LevelTips.getTipsFriendCopyMailTitle(),
//									LevelTips.getTipsFriendCopyMailContentFullCount(role.getExName(), level.getLevelName(), result.getFinishWave(), max_be_invite_price_count));
							isSendFriendPrice = false;
						} else {
							fLevelSet.recordFriendCopyBeInvite();
						}
					}

					if (isSendFriendPrice) {
						int intimacy = KSupportFactory.getRelationShipModuleSupport().getCloseness(role.getId(), result.getFriendId());
						// 计算好友亲密度加成
						float intimacyRate = caculateIntimacyRate(intimacy);
						List<AttValueStruct> friendAttList = new ArrayList<AttValueStruct>();
						List<KCurrencyCountStruct> friendMoneyList = new ArrayList<KCurrencyCountStruct>();
						for (AttValueStruct att : rewardData.baseReward.attList) {
							int count = (int) (att.addValue * intimacyRate);
							friendAttList.add(new AttValueStruct(att.roleAttType, count));
						}
						for (KCurrencyCountStruct money : rewardData.baseReward.moneyList) {
							int count = (int) (money.currencyCount * intimacyRate);
							friendMoneyList.add(new KCurrencyCountStruct(money.currencyType, count));
						}

						BaseRewardData priceData = new BaseRewardData(friendAttList, friendMoneyList, Collections.<ItemCountStruct> emptyList(), Collections.<Integer> emptyList(),
								Collections.<Integer> emptyList());
						BaseMailContent mailContent = new BaseMailContent(LevelTips.getTipsFriendCopyMailTitle(), LevelTips.getTipsFriendCopyMailContent(role.getExName(), level.getLevelName(),
								result.getFinishWave()), null, null);
						BaseMailRewardData mailData = new BaseMailRewardData(1, mailContent, priceData);

						mailData.sendReward(friendRole, PresentPointTypeEnum.好友副本奖励, false);
					}
				}

				// 通知好友模块是否自动添加好友
				KSupportFactory.getRelationShipModuleSupport().autoAppFriend(role, friendRole);
			}
		}

		// 通知活跃度模块
		KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.好友地下城);
		// 通知封测活动奖励
		KSupportFactory.getRewardModuleSupport().notifyForFengceFriendFubenReward(role, result.getFinishWave());

		return true;
	}

	public byte caculateLevelFightEvaluate(TowerFightResult result, KTowerLevelTemplate level) {
		byte value = 1;
		for (Byte fightLv : level.getFightEvaluateDataMap().keySet()) {
			int completeWave = level.getFightEvaluateDataMap().get(fightLv);
			if (result.getFinishWave() >= completeWave) {
				value = fightLv;
				break;
			}
		}
		return value;
	}

	/**
	 * 计算好友亲密度公式
	 * 
	 * @param intimacy
	 * @return
	 */
	public float caculateIntimacyRate(int intimacy) {
		return 0.5f + ((float) (intimacy)) / 20000;
	}

	public LevelRewardResultData caculateLevelReward(KRole role, KTowerLevelTemplate level, TowerFightResult fightResult, int fightLv) {
		LevelRewardResultData resultData = new LevelRewardResultData(CompleteGameLevelTempRecord.LOTTERY_TYPE_ITEM);
		KTowerReward reward = KTowerDataManager.getTowerReward(fightResult.getLastTowerId());
		if (reward != null) {

			// 检测是否有限时产出活动
			boolean isCopyActivityPrice = false;
			float expRate = 1, goldRate = 1, potentialRate = 1;
			int itemMultiple = 1;
			if (level.getLevelType() == KGameLevelTypeEnum.好友副本关卡) {
				TimeLimieProduceActivity activity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.好友地下城活动);

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

			int ratio = this.roleLvRatio.get(role.getLevel());
			List<AttValueStruct> attList = new ArrayList<AttValueStruct>();
			for (AttValueStruct struct : reward.getReward().attList) {
				int count = (struct.addValue * ratio / 10000);
				attList.add(new AttValueStruct(struct.roleAttType, count));
				if (struct.roleAttType == KGameAttrType.EXPERIENCE) {
					count = (int) (count * expRate);
					resultData.attrAndCurrencyShowData[LevelRewardResultData.EXP_LINE][LevelRewardResultData.NORMAL_ROW] = count;
				}
			}
			List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();
			for (KCurrencyCountStruct struct : reward.getReward().moneyList) {
				long baseCount = struct.currencyCount * ratio / 10000;
				long count = baseCount;
				// 军团科技潜能加成
				if (struct.currencyType == KCurrencyTypeEnum.POTENTIAL) {
					count = (long) (count * potentialRate);
					resultData.attrAndCurrencyShowData[LevelRewardResultData.POTENTIAL_LINE][LevelRewardResultData.NORMAL_ROW] = (int) count;
					int addRate = KSupportFactory.getGangSupport().getGangEffect(KGangTecTypeEnum.好友副本潜能产出, role.getId());
					if (addRate > 0) {
						int addCount = (int) ((baseCount * addRate) / UtilTool.TEN_THOUSAND_RATIO_UNIT);
						count += addCount;
						resultData.attrAndCurrencyShowData[LevelRewardResultData.POTENTIAL_LINE][LevelRewardResultData.GANG_ROW] = addCount;
					}
				} else if (struct.currencyType == KCurrencyTypeEnum.GOLD) {
					count = (long) (count * goldRate);
					resultData.attrAndCurrencyShowData[LevelRewardResultData.GOLD_LINE][LevelRewardResultData.NORMAL_ROW] = (int) count;
				}
				moneyList.add(new KCurrencyCountStruct(struct.currencyType, count));
			}

			List<ItemCountStruct> itemRewardList = new ArrayList<ItemCountStruct>();
			for (ItemCountStruct itemStruct : reward.getReward().itemStructs) {
				itemRewardList.add(new ItemCountStruct(itemStruct.itemCode, itemStruct.itemCount * itemMultiple));
			}

			BaseRewardData baseReward = new BaseRewardData(attList, moneyList, itemRewardList, Collections.<Integer> emptyList(), Collections.<Integer> emptyList());

			// 得出所有的道具奖励，生成等待确认奖励的临时记录
			CompleteGameLevelTempRecord temprecord = new CompleteGameLevelTempRecord(CompleteGameLevelTempRecord.LOTTERY_TYPE_ITEM);
			temprecord.setRoleId(role.getId());
			temprecord.setLevelId(level.getLevelId());
			temprecord.setLevelType(level.getLevelType().levelType);
			temprecord.setItemRewardResultDataList(itemRewardList);

			KGameLevelModuleExtension.getManager().allCompleteGameLevelTempRecord.put(temprecord.getRoleId(), temprecord);
			// 添加奖励
			if (!baseReward.sendReward(role, PresentPointTypeEnum.好友副本奖励)) {
				BaseMailContent mainContent = new BaseMailContent(LevelTips.getTipsSendMailItemForBagFullTitle(), LevelTips.getTipsSendMailItemForBagFull(), null, null);
				KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), mainContent, baseReward, PresentPointTypeEnum.好友副本奖励);
				KDialogService.sendUprisingDialog(role, RewardTips.背包已满奖励通过邮件发送);
			}

			resultData.baseReward = baseReward;
			resultData.sLevelReward = null;
			resultData.lotteryRewardList = null;
			resultData.totalItemSize = 0;
			return resultData;
		}

		if (resultData.baseReward == null) {
			resultData.baseReward = new BaseRewardData(Collections.<AttValueStruct> emptyList(), Collections.<KCurrencyCountStruct> emptyList(), Collections.<ItemCountStruct> emptyList(),
					Collections.<Integer> emptyList(), Collections.<Integer> emptyList());
		}
		return resultData;
	}

	/**
	 * 发送关卡更新数据
	 * 
	 * @param role
	 * @param scenarioId
	 * @param levelData
	 */
	public void sendUpdateFriendCopyInfoMsg(KRole role, KTowerLevelTemplate level, KGameLevelRecord record, KGameLevelSet levelSet) {
		boolean isHasLevelData = false;

		if (level != null) {
			isHasLevelData = true;
		}
		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_UPDATE_FRIEND_COPY_DATA);
		sendMsg.writeInt(record.friendCopyData.remainChallengeCount);
		sendMsg.writeBoolean(isHasLevelData);
		if (isHasLevelData) {
			sendMsg.writeInt(level.getLevelId());
			byte levelViewType = judgeGameLevelOpenState(role, levelSet, level);

			sendMsg.writeByte(levelViewType);
			PlayerRoleGamelevelData levelData = levelSet.getCopyLevelData(level.getLevelId(), level.getLevelType());
			if (levelData != null) {
				byte levelCompleteType = (byte) (levelData.isCompleted() ? 1 : 0);
				sendMsg.writeByte(levelCompleteType);
				byte levelEvaluate = levelData.getLevelEvaluate(); // 表示关卡的评价（5星，未完成为0）
				sendMsg.writeByte(levelEvaluate);
				sendMsg.writeInt(levelData.getMaxWave());
			} else {
				sendMsg.writeByte(0);
				sendMsg.writeByte(0);
				sendMsg.writeInt(0);
			}
		}
		role.sendMsg(sendMsg);
	}

	public void processPlayerRoleBuyFriendCopyCount(KRole role, boolean isNeedCheck) {
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
		KGameLevelRecord fRecord = levelSet.getCopyRecord(KGameLevelTypeEnum.好友副本关卡);
		if (isNeedCheck) {
			if (fRecord != null && fRecord.friendCopyData != null) {
				VIPLevelData vipData = KSupportFactory.getVIPModuleSupport().getVIPLevelData(role.getId());
				int vip_buy_count = vipData.friendbuyrmb.length;
				int buyCount = fRecord.friendCopyData.todayBuyCount;
				if (buyCount < vip_buy_count) {
					int usePoint = vipData.friendbuyrmb[buyCount];
					KGameLevelModuleExtension.getManager().sendLevelTipsMessage(role, KLevelModuleDialogProcesser.KEY_BUY_FRIEND_COPY,
							LevelTips.getTipsBuyFriendCopyCount(usePoint, vipData.lvl, (vip_buy_count - buyCount)), true, "");
					return;

				} else if (buyCount >= vip_buy_count) {
					int vipLv = KSupportFactory.getVIPModuleSupport().getVipLv(role.getId());
					KGameLevelModuleExtension.getManager().sendJoinNormalGameLevelTipsMessage(role, (short) -1, LevelTips.getTipsJoinFriendCopyFailedCountNotEnough(), false, null);
					return;
				}
			}

		}

		levelSet.recordBuyFriendCopyCount();

		sendUpdateFriendCopyInfoMsg(role, null, fRecord, null);
	}

	/**
	 * 判断剧本关卡开启状态
	 * 
	 * @param levelAttr
	 * @param level
	 * @return
	 */
	public byte judgeGameLevelOpenState(KRole role, KGameLevelSet levelSet, KTowerLevelTemplate level) {
		if (role.getRoleGameSettingData() != null && role.getRoleGameSettingData().isDebugOpenLevel()) {
			return KLevelTemplate.GAME_LEVEL_STATE_OPEN;
		}

		if (level.getEnterCondition() != null) {
			// TODO 处理任务控制关卡开放
			if (level.getEnterCondition().getOpenRoleLevel() > role.getLevel()) {
				return KLevelTemplate.GAME_LEVEL_STATE_NOT_OPEN;
			}

			if (level.getEnterCondition().getFrontMissionTemplateId() > 0) {
				boolean missionIsOpen = KSupportFactory.getMissionSupport().checkMissionIsAcceptedOrCompleted(role, level.getEnterCondition().getFrontMissionTemplateId());
				if (!missionIsOpen) {
					return KLevelTemplate.GAME_LEVEL_STATE_NOT_OPEN;
				}
			}

			if (level.getEnterCondition().getFrontLevelId() > 0) {
				PlayerRoleGamelevelData levelData = null;
				if (level.getLevelType() == KGameLevelTypeEnum.好友副本关卡) {
					levelData = levelSet.getCopyLevelData(level.getEnterCondition().getFrontLevelId(), level.getLevelType());
				}

				if (levelData == null) {
					return KLevelTemplate.GAME_LEVEL_STATE_NOT_OPEN;
				}

				if (!levelData.isCompleted()) {
					return KLevelTemplate.GAME_LEVEL_STATE_NOT_OPEN;
				}
			}

		}

		return KLevelTemplate.GAME_LEVEL_STATE_OPEN;
	}

	public void checkAndReflashStrangerData(KRole role) {
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());

		KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.好友副本关卡);
		if (record.friendCopyData != null) {
			boolean isStrangerDelete = false;
			for (Long strangerRoleId:record.friendCopyData.strangers.keySet()) {
				if(KSupportFactory.getRoleModuleSupport().getRole(strangerRoleId) == null){
					isStrangerDelete = true;
					break;
				}
			}
			
			if (isStrangerDelete || record.friendCopyData.strangers.size() < 3 || UtilTool.checkNowTimeIsArriveTomorrow(record.friendCopyData.strangerReflashTime)) {
				record.friendCopyData.strangers.clear();
				record.friendCopyData.strangerCoolingTimeMap.clear();
				record.friendCopyData.strangerReflashTime = System.currentTimeMillis();

				if (KCompetitionModule.getCompetitionManager().getCurrentLastRank() >= 10) {
					int myRank;
					int lastRank = KCompetitionModule.getCompetitionManager().getCurrentLastRank();
					KCompetitor myCompetitor = KCompetitionModule.getCompetitionManager().getCompetitorByRoleId(role.getId());
					if (myCompetitor != null) {
						myRank = myCompetitor.getRanking();
						int maxRank = (lastRank - myRank > 5) ? (myRank + 5) : lastRank;
						int minRank = (myRank > 5) ? (myRank - 5) : 1;
						for (int i = 0, size = 0; i < 10 && size < 3; i++) {
							int nowRank = UtilTool.random(minRank, maxRank);
							if (nowRank != myRank) {
								KCompetitor otherC = KCompetitionModule.getCompetitionManager().getCompetitorByRanking(nowRank);
								if (otherC != null && otherC.getRoleId() != role.getId()) {
									if (!KSupportFactory.getRelationShipModuleSupport().isInFriendList(role.getId(), otherC.getRoleId())) {

										StrangerData data = new StrangerData(otherC.getRoleId(), otherC.getRoleName(), otherC.getOccupation(), otherC.getRoleLevel(), otherC.getFightPower());
										record.friendCopyData.strangers.put(data.roleId, data);
										size++;
									}
								}
							}
						}
					} else {
						myRank = lastRank;
						int maxRank = myRank + 10;
						for (int i = 0, size = 0; i < 10 && size < 3; i++) {
							int nowRank = UtilTool.random(myRank, maxRank);
							if (nowRank != myRank) {
								KCompetitor otherC = KCompetitionModule.getCompetitionManager().getCompetitorByRanking(nowRank);
								if (otherC != null && otherC.getRoleId() != role.getId()) {
									if (!KSupportFactory.getRelationShipModuleSupport().isInFriendList(role.getId(), otherC.getRoleId())) {
										StrangerData data = new StrangerData(otherC.getRoleId(), otherC.getRoleName(), otherC.getOccupation(), otherC.getRoleLevel(), otherC.getFightPower());
										record.friendCopyData.strangers.put(data.roleId, data);
										size++;
									}
								}
							}
						}
					}
				} else {
					KGameNormalMap map = KMapModule.getGameMapManager().getGameMap(role.getRoleMapData().getCurrentMapId());
					if (map != null) {
						map = KMapModule.getGameMapManager().firstMap;
					}

					List<KGameMapEntity> entityList = map.getEntities(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE);
					if (entityList != null && !entityList.isEmpty()) {
						for (int i = 0, size = 0; i < entityList.size() && size < 3; i++) {
							KGameMapEntity entity = entityList.get(i);
							if (entity.getSourceObjectID() != role.getId()) {
								KRole otherR = KSupportFactory.getRoleModuleSupport().getRole(entity.getSourceObjectID());
								if (otherR != null && otherR.getId() != role.getId()) {
									if (!KSupportFactory.getRelationShipModuleSupport().isInFriendList(role.getId(), otherR.getId())) {
										StrangerData data = new StrangerData(otherR.getId(), otherR.getName(), otherR.getJob(), otherR.getLevel(), otherR.getBattlePower());
										record.friendCopyData.strangers.put(data.roleId, data);
										size++;
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public boolean checkAndResetFriendCopyDatas(KRole role, boolean isNeedCheck) {
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());

		return levelSet.checkAndResetFriendCopyData(isNeedCheck);
	}
}
