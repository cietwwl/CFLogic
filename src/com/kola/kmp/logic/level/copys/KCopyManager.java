package com.kola.kmp.logic.level.copys;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import com.kola.kmp.logic.activity.KActivityTimeStruct.TimeIntervalStruct;
import com.kola.kmp.logic.item.KItemMsgPackCenter;
import com.kola.kmp.logic.level.FightEvaluateData;
import com.kola.kmp.logic.level.FightEventListener;
import com.kola.kmp.logic.level.FightResult;
import com.kola.kmp.logic.level.KCompleteLevelCondition;
import com.kola.kmp.logic.level.KEnterLevelCondition;
import com.kola.kmp.logic.level.KGameBattlefield;
import com.kola.kmp.logic.level.KGameLevelManager;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.level.KGameLevelRecord;
import com.kola.kmp.logic.level.KGameLevelSet;
import com.kola.kmp.logic.level.KGameScenario;
import com.kola.kmp.logic.level.KLevelModuleDialogProcesser;
import com.kola.kmp.logic.level.KLevelReward;
import com.kola.kmp.logic.level.KLevelTemplate;
import com.kola.kmp.logic.level.KGameLevelManager.LevelRewardResultData;
import com.kola.kmp.logic.level.KGameLevelRecord.PlayerRoleGamelevelData;
import com.kola.kmp.logic.level.gamestory.Animation;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KGameBattlefieldTypeEnum;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.other.KGangTecTypeEnum;
import com.kola.kmp.logic.other.KLimitTimeProduceActivityTypeEnum;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.TimeLimitActivityDataManager.TimeLimieProduceActivity;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_AddItem;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.LevelTips;
import com.kola.kmp.logic.util.tips.RewardTips;
import com.kola.kmp.logic.vip.KVIPDataManager;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;
import com.kola.kmp.protocol.level.KLevelProtocol;

public class KCopyManager {
	// 表示进入关卡的操作按钮
	public static final byte COPY_BUTTON_TYPE_ENTER = 1;
	// 表示扫荡关卡的操作按钮
	public static final byte COPY_BUTTON_TYPE_SAODANG = 2;
	// 表示重置关卡的操作按钮
	public static final byte COPY_BUTTON_TYPE_RESET = 3;

	// 表示首通奖励可领取
	public static final byte FIRST_DROP_PRICE_TYPE_CAN_TAKE = 1;
	// 表示首通奖励不能领取
	public static final byte FIRST_DROP_PRICE_TYPE_CANNOT_TAKE = 0;
	// 表示首通奖励已经领取
	public static final byte FIRST_DROP_PRICE_TYPE_ALREADY_TAKE = -1;

	public static int saodangVipLevel = 1;

	private static final KGameLogger _LOGGER = KGameLogger.getLogger(KCopyManager.class);

	// 精英副本的所有关卡MAP
	private Map<Integer, KLevelTemplate> eliteCopyLevelMap = new LinkedHashMap<Integer, KLevelTemplate>();
	private Map<Byte, Map<Integer, KLevelTemplate>> eliteCopyLevelMapByDifficulty = new LinkedHashMap<Byte, Map<Integer, KLevelTemplate>>();
	// 技术副本的所有关卡MAP
	private Map<Integer, KLevelTemplate> techCopyLevelMap = new LinkedHashMap<Integer, KLevelTemplate>();
	// 副本中的所有战场
	public Map<Integer, KGameBattlefield> allKGameBattlefield = new LinkedHashMap<Integer, KGameBattlefield>();

	// 副本免费挑战次数
	public static int free_challenge_count = 1;
	// 精英副本次数消耗体力表，key：次数，value：体力值
	public static Map<Integer, Integer> buyCountUsePhyPwdMap = new LinkedHashMap<Integer, Integer>();
	// 每天购买精英副本最大次数，-1表示无限次
	public static int maxCanBuyCount = -1;
	// 使用钻石购买的最大默认次数，超过此值钻石消耗不会上升（buyCountUsePointMap里面Key的最大值）
	public static int maxDefaultBuyCount = 0;

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
			for (KGameLevelTypeEnum levelType : KGameLevelTypeEnum.values()) {
				if (levelType == KGameLevelTypeEnum.精英副本关卡) {
					// || levelType == KGameLevelTypeEnum.技术副本关卡) {
					loadExcelData(xlsFile, levelType);
				}
			}
		}
	}

	private void loadExcelData(KGameExcelFile xlsFile, KGameLevelTypeEnum levelType) throws KGameServerException {

		String tableName = null;
		switch (levelType) {
		case 精英副本关卡:
			tableName = "精英副本";
			break;
		case 技术副本关卡:
			tableName = "技术副本";
			break;
		default:
			break;
		}
		_LOGGER.error("### 正在加载<" + tableName + ">表数据...");

		if (tableName != null) {
			int levelDataRowIndex = 5;
			KGameExcelTable levelDataTable = xlsFile.getTable(tableName, levelDataRowIndex);
			KGameExcelRow[] allLevelDataRows = levelDataTable.getAllDataRows();

			if (allLevelDataRows != null) {
				for (int i = 0; i < allLevelDataRows.length; i++) {
					KLevelTemplate level = new KLevelTemplate();
					level.init(tableName, allLevelDataRows[i], levelType);

					switch (levelType) {

					case 精英副本关卡:
						this.eliteCopyLevelMap.put(level.getLevelId(), level);
						if (level.getEnterCondition().getFrontLevelId() > 0) {
							if (eliteCopyLevelMap.containsKey(level.getEnterCondition().getFrontLevelId())) {
								(eliteCopyLevelMap.get(level.getEnterCondition().getFrontLevelId()).getHinderGameLevelList()).add(level);
							}
						}
						if (!this.eliteCopyLevelMapByDifficulty.containsKey(level.difficulty)) {
							this.eliteCopyLevelMapByDifficulty.put(level.difficulty, new LinkedHashMap<Integer, KLevelTemplate>());
						}
						this.eliteCopyLevelMapByDifficulty.get(level.difficulty).put(level.getLevelId(), level);
						break;
					case 技术副本关卡:
						this.techCopyLevelMap.put(level.getLevelId(), level);
						if (level.getEnterCondition().getFrontLevelId() > 0) {
							if (techCopyLevelMap.containsKey(level.getEnterCondition().getFrontLevelId())) {
								(techCopyLevelMap.get(level.getEnterCondition().getFrontLevelId()).getHinderGameLevelList()).add(level);
							}
						}
						break;
					default:
						break;
					}
				}

				// 读取<精英副本重置消耗>数据 ///////////////////
				tableName = "精英副本重置消耗";
				levelDataRowIndex = 5;
				levelDataTable = xlsFile.getTable(tableName, levelDataRowIndex);
				allLevelDataRows = levelDataTable.getAllDataRows();

				if (allLevelDataRows != null) {
					int maxCount = 0;
					boolean isUnLimit = false;
					for (int i = 0; i < allLevelDataRows.length; i++) {
						int count = allLevelDataRows[i].getInt("sceneId");
						int phyPow = allLevelDataRows[i].getInt("reset_Physical");
						if (count > maxCount) {
							maxCount = count;
						}
						if (count != 0) {
							buyCountUsePhyPwdMap.put(count, phyPow);
						} else {
							isUnLimit = true;
						}
					}
					if (!isUnLimit) {
						maxCanBuyCount = maxCount;
					} else {
						maxDefaultBuyCount = maxCount;
						if (!buyCountUsePhyPwdMap.containsKey(maxDefaultBuyCount)) {
							throw new KGameServerException("初始化表<精英副本重置消耗>的错误!");
						}
					}
				}

				// 读取<精英副本翻牌>数据 ///////////////////
				tableName = "精英副本翻牌";
				levelDataRowIndex = 5;
				levelDataTable = xlsFile.getTable(tableName, levelDataRowIndex);
				allLevelDataRows = levelDataTable.getAllDataRows();

				if (allLevelDataRows != null) {
					for (int i = 0; i < allLevelDataRows.length; i++) {
						int levelId = allLevelDataRows[i].getInt("id");

						KLevelTemplate level = eliteCopyLevelMap.get(levelId);
						if (level == null) {
							throw new KGameServerException("初始化表<精英副本翻牌>的字段<id>错误，找不到对应的关卡：，excel行数：" + allLevelDataRows[i].getIndexInFile() + ",关卡ID：" + levelId);
						}
						level.getReward().initLotteryReward("精英副本翻牌", allLevelDataRows[i]);
					}
				}
			}
		}
	}

	public void checkInit() throws KGameServerException {
		for (int vipLv = 0; vipLv <= KVIPDataManager.mVIPLevelDataManager.getMaxLevel().lvl; vipLv++) {
			VIPLevelData data = KSupportFactory.getVIPModuleSupport().getVIPLevelData(vipLv);
			if (data != null) {
				if (data.levelClear == 1) {
					saodangVipLevel = vipLv;
					break;
				}
			}
		}

		boolean checkBattlefied = true;
		for (KGameBattlefield battle : allKGameBattlefield.values()) {

			if (battle.getBornPoint() == null) {
				checkBattlefied = false;
				_LOGGER.error("#########  加载levelConfig.xls表精英或技术副本的战场xml数据错误，该战场没有设置出生点，xml文件名=" + battle.getBattlePathName());
			}
			if (!battle.isInitOK) {
				_LOGGER.error("#########  加载levelConfig.xls表精英或技术副本的战场xml数据错误，该战场初始化失败，xml文件名={}，关卡ID={}", battle.getBattlePathName(), battle.getLevelId());
				checkBattlefied = false;
			}
		}
		if (!checkBattlefied) {
			throw new KGameServerException("#########  加载levelConfig.xls表精英或技术副本的战场xml数据错误。");
		}

	}

	public Map<Integer, KLevelTemplate> getEliteCopyLevelMap() {
		return eliteCopyLevelMap;
	}

	public Map<Integer, KLevelTemplate> getTechCopyLevelMap() {
		return techCopyLevelMap;
	}

	public Map<Integer, KGameBattlefield> getAllKGameBattlefieldMap() {
		return allKGameBattlefield;
	}

	public KLevelTemplate getCopyLevelTemplate(int levelId, KGameLevelTypeEnum levelType) {
		KLevelTemplate level = null;
		switch (levelType) {
		case 精英副本关卡:
			level = this.eliteCopyLevelMap.get(levelId);
			break;
		case 技术副本关卡:
			level = this.techCopyLevelMap.get(levelId);
			break;
		default:
			break;
		}
		return level;
	}

	/**
	 * 向客户端发送剧本关卡数据
	 * 
	 * @param role
	 *            对应的玩家角色
	 * @param scenarioId
	 *            地图出口对应的剧本ID
	 */
	public void sendCopyData(KRole role, byte levelType) {
		// checkAndResetCopyDatas(role, true);
		checkAndResetEliteCopyDatas(role, true);
		// 获取关卡记录
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
		KGameLevelTypeEnum levelTypeEnum = KGameLevelTypeEnum.getEnum(levelType);

		KGameMessage sendMsg;
		switch (levelTypeEnum) {
		case 精英副本关卡:
			sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_GET_ELITE_COPY_DATA);
			break;
		case 技术副本关卡:
			sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_GET_ELITE_COPY_DATA);
			break;
		default:
			// sendMsg = KGame
			// .newLogicMessage(KLevelProtocol.SM_GET_ELITE_COPY_DATA);
			// break;
			return;
		}

		KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.精英副本关卡);

		int remainCount = free_challenge_count;
		int challengeCount = 0;
		if (record != null && record.eliteCopyData != null) {
			remainCount = record.eliteCopyData.remainChallengeCount;
			challengeCount = record.eliteCopyData.challengeCount;
		}
		sendMsg.writeInt(remainCount);
		sendMsg.writeByte(getBuyChallenCountUsePhyPwd(challengeCount));

		// 发送场景基本信息
		for (Byte difficulty : eliteCopyLevelMapByDifficulty.keySet()) {

			Map<Integer, KLevelTemplate> levelMap = eliteCopyLevelMapByDifficulty.get(difficulty);
			int levelSize = levelMap.size();
			sendMsg.writeInt(levelSize);
			for (KLevelTemplate level : levelMap.values()) {
				PlayerRoleGamelevelData levelData = levelSet.getCopyLevelData(level.getLevelId(), levelTypeEnum);

				sendMsg.writeInt(level.getLevelId());
				sendMsg.writeByte(level.getLevelType().levelType);
				sendMsg.writeUtf8String(level.getLevelName());
				sendMsg.writeUtf8String(level.getDesc());
				sendMsg.writeByte(level.getLevelNumber());
				sendMsg.writeInt(level.getIconResId());
				sendMsg.writeInt(level.getBossIconResId());
				sendMsg.writeShort(level.getEnterCondition().getOpenRoleLevel());

				byte levelViewType = KGameLevelModuleExtension.getManager().judgeGameLevelOpenState(role, levelSet, level);

				sendMsg.writeByte(levelViewType);
				// sendMsg.writeByte(level.getEnterCondition().getUseStamina());

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
					if (remainJoinCount <= 0) {
						sendMsg.writeBoolean(true);
					} else {
						sendMsg.writeBoolean(false);
					}
				} else {
					sendMsg.writeByte(0);
					sendMsg.writeInt(level.getFightPower());
					sendMsg.writeByte(0);
					sendMsg.writeInt(level.getEnterCondition().getLevelLimitJoinCount());
					sendMsg.writeInt(level.getEnterCondition().getLevelLimitJoinCount());
					sendMsg.writeBoolean(false);
				}

				// 奖励信息
				level.getReward().getShowRewardData(role.getJob()).packMsg(sendMsg);
				// S级别奖励
				level.getReward().s_probableReward.packMsg(sendMsg);

				// 首次通关奖励礼包
				boolean isHasFirstPassItem = false;
				byte isCanGetFirstPrice = FIRST_DROP_PRICE_TYPE_CANNOT_TAKE;
				if (level.getReward().isHasFirstDropItem()) {
					isHasFirstPassItem = true;
					if (levelData != null) {
						if (levelData.isCompleted() && !levelData.isGetFirstDropPrice()) {
							isCanGetFirstPrice = FIRST_DROP_PRICE_TYPE_CAN_TAKE;
						} else if (levelData.isCompleted() && levelData.isGetFirstDropPrice()) {
							isCanGetFirstPrice = FIRST_DROP_PRICE_TYPE_ALREADY_TAKE;
						}
					}
				}
				sendMsg.writeBoolean(isHasFirstPassItem);
				if (isHasFirstPassItem) {
					sendMsg.writeByte(isCanGetFirstPrice);
					ItemCountStruct struct = level.getReward().getFirstDropItem();
					KItemMsgPackCenter.packItem(sendMsg, struct.getItemTemplate(), struct.itemCount);
				}

				// 操作按钮信息
				List<Byte> buttons = getCopyLevelButtons(role, level, KGameLevelModuleExtension.getManager().judgeGameLevelOpenState(role, levelSet, level), levelData);

				sendMsg.writeByte(buttons.size());
				for (Byte btType : buttons) {
					sendMsg.writeByte(btType);
					if (btType == COPY_BUTTON_TYPE_SAODANG) {
						sendMsg.writeShort(getMaxCanSaodangCount(role, level, levelData));
						// sendMsg.writeByte(level.getEnterCondition().getUseStamina());
						sendMsg.writeByte(KGameLevelModuleExtension.getManager().checkSaodangSuccessRate(role, level));
						// List<Integer> saodangUsePointList =
						// getSaodangUsePointList(
						// role, level, record);
						// sendMsg.writeByte(saodangUsePointList.size());
						// for (Integer point : saodangUsePointList) {
						// sendMsg.writeInt(point);
						// }
						/***** 精英副本扫荡界面显示需要钻石改为0 ****/
						sendMsg.writeByte(1);
						sendMsg.writeInt(0);
					}
					/****** 20141129注释，精英副本不需要消耗钻石 ********/
					// else if (btType == COPY_BUTTON_TYPE_RESET) {
					// int point = getRestCopyLevelUsePoint(role, level,
					// levelData);
					// sendMsg.writeInt(point);
					// }
				}

				List<Byte> itemDropRateList = level.getReward().getShowRewardDataDropRate(role.getJob());
				if (itemDropRateList != null) {
					sendMsg.writeByte(itemDropRateList.size());
					for (byte rate : itemDropRateList) {
						sendMsg.writeByte(rate);
					}
				} else {
					sendMsg.writeByte(0);
				}
			}
		}

		role.sendMsg(sendMsg);
		KDialogService.sendNullDialog(role);
	}

	private int getMaxCanSaodangCount(KRole role, KLevelTemplate level, PlayerRoleGamelevelData levelData) {
		/****** 20141129注释，精英副本消耗体力方案变化 ********/
		// int count = role.getPhyPower() /
		// level.getEnterCondition().getUseStamina();
		// if (count > levelData.getRemainJoinLevelCount()) {
		// count = levelData.getRemainJoinLevelCount();
		// }
		// return count;
		return 0;
	}

	private KLevelTemplate getAllCopyLevelTemplate(int levelId, KGameLevelTypeEnum levelType) {
		switch (levelType) {
		case 精英副本关卡:
			return this.eliteCopyLevelMap.get(levelId);
		case 技术副本关卡:
			return this.techCopyLevelMap.get(levelId);
		}
		return null;
	}

	private List<KLevelTemplate> getAllCopyLevels(KGameLevelTypeEnum levelType) {
		List<KLevelTemplate> list = new ArrayList<KLevelTemplate>();
		switch (levelType) {
		case 精英副本关卡:
			list.addAll(this.eliteCopyLevelMap.values());
			break;
		case 技术副本关卡:
			list.addAll(this.techCopyLevelMap.values());
			break;
		default:
			break;
		}
		return list;
	}

	/**
	 * 处理角色进入关卡
	 * 
	 * @param role
	 * @param levelId
	 * @param isNeedCheck
	 *            是否需要检测进入条件
	 */
	public KActionResult playerRoleJoinGameLevel(KRole role, int levelId, KGameLevelTypeEnum levelType, boolean isNeedCheck, boolean isSendDialog) {
		if (role == null) {
			_LOGGER.error("角色进入副本关卡失败，角色为null，关卡ID：" + levelId + "，关卡类型" + levelType.levelType);
			// KDialogService.sendUprisingDialog(role.getId(),
			// LevelTips.getTipsJoinLevelFailed());
			return new KActionResult(false, LevelTips.getTipsJoinLevelFailed());
		}
		KLevelTemplate level = getAllCopyLevelTemplate(levelId, levelType);
		if (level == null) {
			_LOGGER.error("角色进入副本关卡失败，找不到对应的关卡。角色id:" + role.getId() + "，关卡ID：" + levelId + "，关卡类型" + levelType.levelType);
			KDialogService.sendUprisingDialog(role.getId(), LevelTips.getTipsJoinLevelFailed());
			return new KActionResult(false, LevelTips.getTipsJoinLevelFailed());
		}

		// 获取关卡开启条件
		KEnterLevelCondition condition = level.getEnterCondition();

		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());

		PlayerRoleGamelevelData levelData = null;
		KGameLevelRecord jyRecord = levelSet.getCopyRecord(levelType);
		if (jyRecord != null) {
			levelData = jyRecord.getLevelDataMap().get(level.getLevelId());
		}
		// 判断角色是否能进入关卡，参考方法checkPlayerRoleCanJoinGameLevel()
		KActionResult joinLevelState = checkPlayerRoleCanJoinGameLevel(role, levelSet, level, isNeedCheck, isSendDialog);
		// 如果可以进入
		if (joinLevelState.success) {
			// 当前进入为普通战场模式，取得关卡第一层战场的数据，并通知战斗模块
			if (level.getAllNormalBattlefields().isEmpty()) {
				_LOGGER.error("角色进入关卡失败，找不到对应的第一层战场。角色id:" + role.getId() + "，关卡ID：" + levelId);
				KGameLevelModuleExtension.getManager().sendJoinNormalGameLevelTipsMessage(role, (short) -1, LevelTips.getTipsJoinLevelFailed(), false, null);
				return new KActionResult(false, LevelTips.getTipsJoinLevelFailed());
			}

			// // 记录进入关卡，扣除次数 /***** 20141124 改变为胜利才扣次数，注释 *****/
			// levelSet.recordChallengeEliteCopy();

			sendUpdateCopyGameLevelInfoMsg(role, null, levelSet);

			// 通知战斗模块，角色进入战场
			List<Animation> animation = new ArrayList<Animation>();
			for (FightEventListener listener : KGameLevelModuleExtension.getManager().getFightEventListenerList()) {
				listener.notifyBattle(role, level.getAllNormalBattlefields(), animation);
			}

			// 角色行为统计
			if (level.getLevelType() == KGameLevelTypeEnum.精英副本关卡) {
				KActionRecorder.recordAction(role.getId(), KActionType.ACTION_TYPE_SENIOR_FB, 1);
			} else if (level.getLevelType() == KGameLevelTypeEnum.技术副本关卡) {
				KActionRecorder.recordAction(role.getId(), KActionType.ACTION_TYPE_BOSS_FB, 1);
			}
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
	private KActionResult checkPlayerRoleCanJoinGameLevel(KRole role, KGameLevelSet levelSet, KLevelTemplate level, boolean isNeedCheckCondition, boolean isSendDialog) {
		KActionResult result = new KActionResult();
		String tips = "";
		if (isNeedCheckCondition) {
			// 获取角色当前体力值
			int roleStamina = role.getPhyPower();

			// 判断关卡是否开放
			byte levelOpenType = KGameLevelModuleExtension.getManager().judgeGameLevelOpenState(role, levelSet, level);
			if (levelOpenType != KLevelTemplate.GAME_LEVEL_STATE_OPEN) {
				// sendJoinGameLevelTipsMessage(role,
				// KGameLevel.PLAYERROLE_JOIN_GAMELEVEL_STATE_NOT_OPEN,
				// "该关卡未开启");
				tips = LevelTips.getTipsLevelNotOpen();
				if (isSendDialog) {
					KGameLevelModuleExtension.getManager().sendJoinNormalGameLevelTipsMessage(role, (short) -1, tips, false, null);
				}
				result.success = false;
				result.tips = tips;
				return result;
			}
			// 检测体力
			int useStamina = 0;
			KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.精英副本关卡);
			if (record != null && record.eliteCopyData != null) {
				useStamina = getBuyChallenCountUsePhyPwd(record.eliteCopyData.challengeCount);
			}
			if (useStamina > 0 && roleStamina < useStamina) {
				tips = LevelTips.getTipsJoinLevelFailedWhileStaminaNotEnough();
				if (isSendDialog) {
					// KGameLevelModuleExtension.getManager()
					// .sendJoinNormalGameLevelTipsMessage(role,
					// (short) -1, tips, false, null);
					KGameLevelModuleExtension.getManager().checkAndSendPhyPowerNotEnoughDialog(role);
				}
				result.success = false;
				result.tips = tips;
				return result;
			}

			// // 判断是否限制进入次数关卡
			// PlayerRoleGamelevelData levelData = levelSet
			// .getPlayerRoleNormalGamelevelData(level.getLevelId());
			// if (level.getEnterCondition().isLimitJoinCountLevel()) {
			// // 如果是限制次数，则判断其剩余进入次数
			// if (levelData != null
			// && levelData.getRemainJoinLevelCount() <= 0) {
			// // 剩余次数小于等于0，则发送错误提示
			// if (level.getLevelType() == KGameLevelTypeEnum.精英副本关卡) {
			// tips = LevelTips
			// .getTipsJoinCopyLevelFailedWhileMaxCount();
			// if (isSendDialog) {
			// KGameLevelModuleExtension.getManager()
			// .sendJoinNormalGameLevelTipsMessage(role,
			// (short) -1, tips, false, null);
			// }
			// } else if (level.getLevelType() == KGameLevelTypeEnum.技术副本关卡) {
			// tips = LevelTips
			// .getTipsJoinCopyLevelFailedWhileMaxCount();
			// if (isSendDialog) {
			// KGameLevelModuleExtension.getManager()
			// .sendJoinNormalGameLevelTipsMessage(role,
			// (short) -1, tips, false, null);
			// }
			// }
			//
			// result.success = false;
			// result.tips = tips;
			// return result;
			// }
			// }

			/****** 以下内容 20141129注释，精英副本不需要消耗钻石 ********/
			// KGameLevelRecord pcRecord = levelSet.getCopyRecord(level
			// .getLevelType());
			// PlayerRoleGamelevelData levelData = null;
			// if (pcRecord != null) {
			// levelData = pcRecord.getLevelDataMap().get(level.getLevelId());
			// }
			// if (pcRecord != null && pcRecord.eliteCopyData != null) {
			// int remainChallengeCount =
			// pcRecord.eliteCopyData.remainChallengeCount;
			// int challengeCount = pcRecord.eliteCopyData.challengeCount;
			// int buyCount = pcRecord.eliteCopyData.todayBuyCount;
			// if (remainChallengeCount <= 0) {
			// if (buyCount < maxCanBuyCount || maxCanBuyCount == -1) {
			// int usePoint = getBuyChallenCountUsePoint(buyCount);
			// KGameLevelModuleExtension
			// .getManager()
			// .sendLevelTipsMessage(
			// role,
			// KLevelModuleDialogProcesser.KEY_BUY_ELITE_COPY,
			// LevelTips
			// .getTipsJoinFriendCopyFailedUsePoint(usePoint),
			// true, "");
			// return new KActionResult(
			// false,
			// LevelTips
			// .getTipsJoinFriendCopyFailedUsePoint(usePoint));
			// } else if (buyCount >= maxCanBuyCount) {
			// int vipLv = KSupportFactory.getVIPModuleSupport()
			// .getVipLv(role.getId());
			// KGameLevelModuleExtension
			// .getManager()
			// .sendJoinNormalGameLevelTipsMessage(
			// role,
			// (short) -1,
			// LevelTips
			// .getTipsJoinFriendCopyFailedCountNotEnough(),
			// false, null);
			// return new KActionResult(
			// false,
			// LevelTips
			// .getTipsJoinFriendCopyFailedCountNotEnough());
			// }
			// }
			// }
		}
		/****** 以上内容 20141129注释，精英副本不需要消耗钻石 ********/
		result.success = true;
		result.tips = tips;
		return result;
	}

	public int getBuyChallenCountUsePhyPwd(int alreadyBuyCount) {
		if (maxDefaultBuyCount == 0) {
			return 0;
		}

		int phyPwd = 0;
		
		if (alreadyBuyCount >= maxDefaultBuyCount) {
			phyPwd =  buyCountUsePhyPwdMap.get(maxDefaultBuyCount);
		} else {
			int count = alreadyBuyCount + 1;
			if (buyCountUsePhyPwdMap.containsKey(count)) {
				phyPwd = buyCountUsePhyPwdMap.get(count);
			} else {
				phyPwd = buyCountUsePhyPwdMap.get(maxDefaultBuyCount);
			}
		}
		
		int discount = 100;
		TimeLimieProduceActivity activity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.精英副本消耗体力减半);
		if(activity!=null && activity.isActivityTakeEffectNow()){
			discount = activity.discount;
		}
		
		return phyPwd * discount / 100;
	}

	/**
	 * 处理角色完成关卡，处理关卡结算相关流程
	 * 
	 * @param role
	 * @param level
	 */
	public void processPlayerRoleCompleteCopyLevel(KRole role, KLevelTemplate level, FightResult result) {
		// 关卡开启条件
		KEnterLevelCondition condition = level.getEnterCondition();

		// 获取关卡记录
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());

		KGameLevelRecord record = levelSet.getCopyRecord(level.getLevelType());

		PlayerRoleGamelevelData levelData = null;
		if (record != null) {
			levelData = levelSet.getCopyRecord(level.getLevelType()).levelDataMap.get(level.getLevelId());
		}

		int preCount = record.eliteCopyData.challengeCount;
		// 记录进入关卡，扣除次数 /***** 20141124 改变为胜利才扣次数 *****/
		levelSet.recordChallengeEliteCopy();

		int useStamina = 0;
		if (preCount > 0) {
			useStamina = KGameLevelModuleExtension.getManager().getCopyManager().getBuyChallenCountUsePhyPwd(preCount);
		}
		// 修改角色体力,减少值为该关卡的消耗体力值
		// if (condition.getUseStamina() > 0 && role.getPhyPower() >=
		// condition.getUseStamina()) {
		if (useStamina > 0 && role.getPhyPower() >= useStamina) {
			String reason = "";
			if (level.getLevelType() == KGameLevelTypeEnum.精英副本关卡) {
				reason = "精英副本关卡扣除体力";
			} else {
				reason = "技术副本关卡扣除体力";
			}
			// KSupportFactory.getRoleModuleSupport().decreasePhyPower(role,
			// condition.getUseStamina(), reason);
			KSupportFactory.getRoleModuleSupport().decreasePhyPower(role, useStamina, reason);
			// KSupportFactory.getRoleModuleSupport().decreasePhyPower(role,
			// condition.getUseStamina());
		}

		KLevelReward reward = level.getReward();

		// 计算关卡战斗等级
		byte fightLv = KGameLevelModuleExtension.getManager().caculateLevelFightEvaluate(role, result, level);

		// 计算关卡所有奖励
		PresentPointTypeEnum presentType;
		if (level.getLevelType() == KGameLevelTypeEnum.精英副本关卡) {
			presentType = PresentPointTypeEnum.精英副本奖励;
		} else {
			presentType = PresentPointTypeEnum.技术副本奖励;
		}
		LevelRewardResultData rewardData = KGameLevelModuleExtension.getManager().caculateLevelReward(role, level, result, fightLv, presentType, true);

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

		sendMsg.writeInt(KGameLevelManager.getNextRoleLvUpgradeExp(role));

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
		for (int i = 0; i < rewardData.attrAndCurrencyShowData.length; i++) {
			sendMsg.writeBoolean(rewardData.isAttrDouble[i]);
			for (int j = 0; j < rewardData.attrAndCurrencyShowData[i].length; j++) {
				sendMsg.writeInt(rewardData.attrAndCurrencyShowData[i][j]);
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
			levelSet.addOrModifyCopyGameLevelData(level.getLevelId(), level.getLevelType(), (level.getEnterCondition().getLevelLimitJoinCount() - 1), fightLv, true, false, 0);

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

			levelSet.addOrModifyCopyGameLevelData(level.getLevelId(), level.getLevelType(), 0, ((levelData.getLevelEvaluate() < fightLv) ? fightLv : levelData.getLevelEvaluate()), true,
					levelData.isGetFirstDropPrice(), levelData.getTodayRestCount());
			isLevelDataChange = true;
		}

		// 发消息通知用户该关卡记录有发生改变
		// if (isLevelDataChange) {
		sendUpdateCopyGameLevelInfoMsg(role, level, levelSet);
		// }

		// 通知战场监听器通知关卡完成
		for (FightEventListener listener : KGameLevelModuleExtension.getManager().getFightEventListenerList()) {
			listener.notifyGameLevelCompleted(role, level, result);
		}

		// 处理后置关卡开放状态
		if (isCompletedAndTriggerOpenHinderLevel) {
			for (KLevelTemplate hinderLevel : level.getHinderGameLevelList()) {
				if (level.getEnterCondition().getOpenRoleLevel() <= role.getLevel()) {

					sendUpdateCopyGameLevelInfoMsg(role, hinderLevel, levelSet);
				}
			}
		}

		// 通知活跃度模块
		if (level.getLevelType() == KGameLevelTypeEnum.精英副本关卡) {
			if (level.difficulty == 0) {
				KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.通关普通精英副本);
			} else {
				KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.通关困难精英副本);
			}
		} else if (level.getLevelType() == KGameLevelTypeEnum.技术副本关卡) {
			// KSupportFactory.getRewardModuleSupport().recordFun(role,
			// KVitalityTypeEnum.通关技术副本);
		}
	}

	/**
	 * 发送关卡更新数据
	 * 
	 * @param role
	 * @param scenarioId
	 * @param levelData
	 */
	public void sendUpdateCopyGameLevelInfoMsg(KRole role, KLevelTemplate level, KGameLevelSet levelSet) {
		// VIPLevelData vipData = KSupportFactory.getVIPModuleSupport()
		// .getVIPLevelData(role.getId());
		KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_UPDATE_ELITE_COPY_DATA);

		KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.精英副本关卡);

		int remainCount = free_challenge_count;
		int challengeCount = 0;
		if (record != null && record.eliteCopyData != null) {
			remainCount = record.eliteCopyData.remainChallengeCount;
			challengeCount = record.eliteCopyData.challengeCount;
		}

		sendMsg.writeInt(remainCount);
		sendMsg.writeByte(getBuyChallenCountUsePhyPwd(challengeCount));
		if (level != null && levelSet != null) {
			sendMsg.writeBoolean(true);
			sendMsg.writeByte(level.difficulty);
			sendMsg.writeInt(level.getLevelId());

			sendMsg.writeByte(KGameLevelModuleExtension.getManager().judgeGameLevelOpenState(role, levelSet, level));
			// 完成状态
			byte completeType = 0;
			PlayerRoleGamelevelData levelData = levelSet.getCopyLevelData(level.getLevelId(), level.getLevelType());
			if (levelData != null && levelData.isCompleted()) {
				completeType = 1;
			}
			sendMsg.writeByte(completeType);
			// 关卡评价
			byte levelEvaluate = 0;
			if (levelData != null && levelData.isCompleted()) {
				levelEvaluate = levelData.getLevelEvaluate();
			}
			sendMsg.writeByte(levelEvaluate);
			// 表示关卡的当天剩余进入次数
			int count = 0;
			if (level.getEnterCondition().isLimitJoinCountLevel()) {
				if (levelData == null) {
					count = level.getEnterCondition().getLevelLimitJoinCount();
				} else {
					count = levelData.getRemainJoinLevelCount();
				}
			}
			sendMsg.writeInt(count);
			if (count <= 0) {
				sendMsg.writeBoolean(true);
			} else {
				sendMsg.writeBoolean(false);
			}

			// 首次通关奖励礼包
			boolean isHasFirstPassItem = false;
			byte isCanGetFirstPrice = FIRST_DROP_PRICE_TYPE_CANNOT_TAKE;
			if (level.getReward().isHasFirstDropItem()) {
				isHasFirstPassItem = true;
				if (levelData != null) {
					if (levelData.isCompleted() && !levelData.isGetFirstDropPrice()) {
						isCanGetFirstPrice = FIRST_DROP_PRICE_TYPE_CAN_TAKE;
					} else if (levelData.isCompleted() && levelData.isGetFirstDropPrice()) {
						isCanGetFirstPrice = FIRST_DROP_PRICE_TYPE_ALREADY_TAKE;
					}
				}
			}
			sendMsg.writeBoolean(isHasFirstPassItem);
			if (isHasFirstPassItem) {
				sendMsg.writeByte(isCanGetFirstPrice);
			}

			// 更新按钮信息
			List<Byte> buttons = getCopyLevelButtons(role, level, KGameLevelModuleExtension.getManager().judgeGameLevelOpenState(role, levelSet, level), levelData);

			sendMsg.writeByte(buttons.size());
			for (Byte btType : buttons) {
				sendMsg.writeByte(btType);
				if (btType == COPY_BUTTON_TYPE_SAODANG) {
					sendMsg.writeShort(getMaxCanSaodangCount(role, level, levelData));
					// sendMsg.writeByte(level.getEnterCondition().getUseStamina());
					// sendMsg.writeByte(getBuyChallenCountUsePhyPwd(challengeCount));
					sendMsg.writeByte(KGameLevelModuleExtension.getManager().checkSaodangSuccessRate(role, level));
					// List<Integer> saodangUsePointList =
					// getSaodangUsePointList(
					// role, level, record);
					// sendMsg.writeByte(saodangUsePointList.size());
					// for (Integer point : saodangUsePointList) {
					// sendMsg.writeInt(point);
					// }
					/***** 精英副本扫荡界面显示需要钻石改为0 ****/
					sendMsg.writeByte(1);
					sendMsg.writeInt(0);
				}
				/****** 20141129注释，精英副本不需要消耗钻石 ********/
				// else if (btType == COPY_BUTTON_TYPE_RESET) {
				// int point = getRestCopyLevelUsePoint(role, level, levelData);
				// sendMsg.writeInt(point);
				// }
			}
		} else {
			sendMsg.writeBoolean(false);
		}

		role.sendMsg(sendMsg);
	}

	public List<Byte> getCopyLevelButtons(KRole role, KLevelTemplate level, int levelViewType, PlayerRoleGamelevelData levelData) {
		List<Byte> buttons = new ArrayList<Byte>();
		// if (levelViewType != KLevelTemplate.GAME_LEVEL_STATE_OPEN) {
		// return buttons;
		// }
		if (levelData == null) {
			buttons.add(COPY_BUTTON_TYPE_ENTER);
			// return buttons;
		} else {
			if (levelData.isCompleted()) {
				buttons.add(COPY_BUTTON_TYPE_ENTER);
				buttons.add(COPY_BUTTON_TYPE_SAODANG);
			} else {
				buttons.add(COPY_BUTTON_TYPE_ENTER);
			}
		}
		// if (levelData.getRemainJoinLevelCount() > 0) {
		// buttons.add(COPY_BUTTON_TYPE_ENTER);
		// } else {
		// // if (level.getLevelType() == KGameLevelTypeEnum.技术副本关卡) {
		// // if (level.getHinderGameLevelList().size() > 0) {
		// // KLevelTemplate hinderLevel = level.getHinderGameLevelList()
		// // .get(0);
		// // if (hinderLevel != null
		// // && checkCopyLevelIsComplete(role, hinderLevel)) {
		// // return buttons;
		// // }
		// // }
		// // }
		// buttons.add(COPY_BUTTON_TYPE_RESET);
		// }
		return buttons;
	}

	/**
	 * 检测副本关卡是否通过
	 * 
	 * @param role
	 * @param level
	 * @return
	 */
	private boolean checkCopyLevelIsComplete(KRole role, KLevelTemplate level) {
		// 获取关卡记录
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());

		PlayerRoleGamelevelData levelData = levelSet.getCopyLevelData(level.getLevelId(), level.getLevelType());
		if (levelData == null) {
			return false;
		} else {
			if (levelData.isCompleted()) {
				return true;
			} else {
				return false;
			}
		}
	}

	public void processResetCopyGameLevel(KRole role, int levelId, KGameLevelTypeEnum levelType, boolean isNeedCheck) {

		KLevelTemplate level = getCopyLevelTemplate(levelId, levelType);
		if (level == null) {
			_LOGGER.error("角色重置副本关卡失败，角色为null，关卡ID：" + levelId + "，关卡类型" + levelType.levelType);
			KDialogService.sendUprisingDialog(role.getId(), LevelTips.getTipsJoinLevelFailed());
			return;
		}
		// 获取关卡记录
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
		PlayerRoleGamelevelData levelData = levelSet.getCopyLevelData(level.getLevelId(), level.getLevelType());
		if (isNeedCheck) {
			if (levelData == null || !levelData.isCompleted()) {
				KDialogService.sendUprisingDialog(role.getId(), LevelTips.getTipsRestCopyLevelFailedWhileNotFinished());
				return;
			}
			// if (levelType == KGameLevelTypeEnum.技术副本关卡) {
			// if (level.getHinderGameLevelList().size() > 0) {
			// KLevelTemplate hinderLevel = level.getHinderGameLevelList()
			// .get(0);
			// if (hinderLevel != null
			// && checkCopyLevelIsComplete(role, hinderLevel)) {
			// KDialogService
			// .sendUprisingDialog(
			// role.getId(),
			// LevelTips
			// .getTipsRestCopyLevelFailedWhileLvHigher());
			// return;
			// }
			// }
			// }
			int point = getRestCopyLevelUsePoint(role, level, levelData);
			KGameLevelModuleExtension.getManager().sendLevelTipsMessage(role, KLevelModuleDialogProcesser.KEY_RESET_COPY, LevelTips.getTipsRestCopyLevel(point), true,
					levelId + "," + levelType.levelType);
			return;
		}

		levelSet.recordResetCopylevelData(level, levelType);

		sendUpdateCopyGameLevelInfoMsg(role, level, levelSet);
	}

	/**
	 * 获取重置副本需要使用钻石数
	 * 
	 * @param role
	 * @param level
	 * @param levelData
	 * @return
	 */
	public int getRestCopyLevelUsePoint(KRole role, KLevelTemplate level, PlayerRoleGamelevelData levelData) {
		int point = 0;
		int restCount = levelData.getTodayRestCount();
		if (level.getEnterCondition().resetUsePointList.size() > 0 && restCount >= 0) {
			int maxUsePointIndex = level.getEnterCondition().resetUsePointList.size() - 1;
			if (restCount >= maxUsePointIndex) {
				point = level.getEnterCondition().resetUsePointList.get(maxUsePointIndex);
			} else {
				point = level.getEnterCondition().resetUsePointList.get(restCount);
			}
		}
		return point;
	}

	public List<Integer> getSaodangUsePointList(KRole role, KLevelTemplate level, PlayerRoleGamelevelData levelData) {
		List<Integer> list = new ArrayList<Integer>();
		if (levelData != null) {
			int restCount = levelData.getTodayRestCount();
			int remainCount = levelData.getRemainJoinLevelCount();
			if (remainCount > 0) {
				for (int i = 0; i < remainCount; i++) {
					list.add(0);
				}
			}
			List<Integer> resetUsePointList = level.getEnterCondition().resetUsePointList;
			if (resetUsePointList.size() > 0 && restCount >= 0) {
				int maxUsePointIndex = level.getEnterCondition().resetUsePointList.size() - 1;
				int point;
				if (restCount >= maxUsePointIndex) {
					point = level.getEnterCondition().resetUsePointList.get(maxUsePointIndex);
					list.add(point);
				} else {
					for (int i = restCount; i <= maxUsePointIndex; i++) {
						point = level.getEnterCondition().resetUsePointList.get(i);
						list.add(point);
					}
				}
			}
			if (list.isEmpty()) {
				list.add(0);
			}
		}
		return list;
	}

	public List<Integer> getSaodangUsePhyPwdList(KRole role, KLevelTemplate level, KGameLevelRecord record) {
		List<Integer> list = new ArrayList<Integer>();
		if (record != null && record.eliteCopyData != null) {
			int buyCount = record.eliteCopyData.todayBuyCount;
			int remainCount = record.eliteCopyData.challengeCount;
			if (remainCount > 0) {
				list.add(0);
			} else if (buyCount < maxCanBuyCount || maxCanBuyCount == -1) {
				list.add(getBuyChallenCountUsePhyPwd(buyCount));
			} else {
				list.add(getBuyChallenCountUsePhyPwd(maxDefaultBuyCount));
			}
		}
		return list;
	}

	public void processGetCopyFirstDropItem(KRole role, int levelId, KGameLevelTypeEnum levelType) {
		KLevelTemplate level = getCopyLevelTemplate(levelId, levelType);
		if (level == null) {
			_LOGGER.error("角色重置副本关卡失败，角色为null，关卡ID：" + levelId + "，关卡类型" + levelType.levelType);
			KDialogService.sendUprisingDialog(role.getId(), LevelTips.getTipsJoinLevelFailed());
			return;
		}

		// 首次通关奖励礼包
		ItemCountStruct item = level.getReward().getFirstDropItem();
		if (!level.getReward().isHasFirstDropItem() || item == null) {
			KDialogService.sendUprisingDialog(role.getId(), GlobalTips.getTipsServerBusy());
			return;
		}

		// 获取关卡记录
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
		PlayerRoleGamelevelData levelData = levelSet.getCopyLevelData(level.getLevelId(), level.getLevelType());
		if (levelData == null || !levelData.isCompleted()) {
			KDialogService.sendUprisingDialog(role.getId(), LevelTips.getTipsCarnnotGetFirstDropPrice());
			return;
		} else if (levelData.isGetFirstDropPrice()) {
			KDialogService.sendUprisingDialog(role.getId(), LevelTips.getTipsAlreadyGetFirstDropPrice());
			return;
		} else {
			ItemResult_AddItem result = KSupportFactory.getItemModuleSupport().addItemToBag(role, item, this.getClass().getSimpleName());
			if (!result.isSucess) {
				// 如果背包满
				KDialogService.sendUprisingDialog(role, LevelTips.getTipsBagCapacityNotEnough());
			} else {
				KDialogService.sendDataUprisingDialog(role, item.getItemTemplate().extItemName + "x" + item.itemCount + " ");
				levelSet.addOrModifyCopyGameLevelData(levelId, levelType, levelData.getRemainJoinLevelCount(), levelData.getLevelEvaluate(), levelData.isCompleted(), true,
						levelData.getTodayRestCount());
				sendUpdateCopyGameLevelInfoMsg(role, level, levelSet);
			}
		}
	}

	/**
	 * 处理副本扫荡
	 * 
	 * @param role
	 * @param levelId
	 * @param saodangCount
	 */
	public void processCopyLevelSaodang(KRole role, int levelId, KGameLevelTypeEnum levelType, byte saodangCount, boolean needCheck) {
		// TODO 检测体力
		KLevelTemplate level = getCopyLevelTemplate(levelId, levelType);

		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
		KGameLevelRecord record = levelSet.getCopyRecord(KGameLevelTypeEnum.精英副本关卡);
		PlayerRoleGamelevelData levelData = levelSet.getCopyLevelData(level.getLevelId(), levelType);

		if (level == null || levelData == null) {
			KGameLevelManager.sendSaodangFailedMsg(role);
			KDialogService.sendUprisingDialog(role, LevelTips.getTipsProcessDataError());
			return;
		}
		if (saodangCount < 1) {
			KGameLevelManager.sendSaodangFailedMsg(role);
			KDialogService.sendUprisingDialog(role, GlobalTips.getTipsServerBusy());
			return;
		}
		if (record != null && record.eliteCopyData != null) {
			// int usePhyPower = level.getEnterCondition().getUseStamina();
			int usePhyPower = 0;
			for (int i = 0; i < saodangCount; i++) {
				usePhyPower += getBuyChallenCountUsePhyPwd(record.eliteCopyData.challengeCount + i);
			}

			if (needCheck) {
				VIPLevelData vipData = KSupportFactory.getVIPModuleSupport().getVIPLevelData(role.getId());
				if (vipData.levelClear == 0) {
					// VIP等级不足
					KGameLevelManager.sendSaodangFailedMsg(role);
					KDialogService.sendUprisingDialog(role, LevelTips.getTipsSaodangVipLvNotEnough(saodangVipLevel));
					return;
				}

				if (role.getPhyPower() < usePhyPower) {
					// TODO 发送购买体力提示
					// KDialogService.sendUprisingDialog(role,
					// LevelTips.getTipsSaodangStaminaNotEnough());
					KGameLevelManager.sendSaodangFailedMsg(role);
					KGameLevelModuleExtension.getManager().checkAndSendPhyPowerNotEnoughDialog(role);
					return;
				}
				/****** 以下内容 20141129注释，精英副本不需要消耗钻石 ********/
				// if (record != null && record.eliteCopyData != null) {
				// int remainChallengeCount =
				// record.eliteCopyData.remainChallengeCount;
				// int challengeCount = record.eliteCopyData.challengeCount;
				// int buyCount = record.eliteCopyData.todayBuyCount;
				// if (remainChallengeCount <= 0) {
				// if (buyCount < maxCanBuyCount || maxCanBuyCount == -1) {
				// int usePoint = getBuyChallenCountUsePoint(buyCount);
				// KGameLevelModuleExtension
				// .getManager()
				// .sendLevelTipsMessage(
				// role,
				// KLevelModuleDialogProcesser.KEY_BUY_ELITE_COPY,
				// LevelTips
				// .getTipsJoinFriendCopyFailedUsePoint(usePoint),
				// true, "");
				// // return new KActionResult(
				// // false,
				// // LevelTips
				// // .getTipsJoinFriendCopyFailedUsePoint(usePoint));
				// return;
				// } else if (buyCount >= maxCanBuyCount) {
				// int vipLv = KSupportFactory.getVIPModuleSupport()
				// .getVipLv(role.getId());
				// KGameLevelModuleExtension
				// .getManager()
				// .sendJoinNormalGameLevelTipsMessage(
				// role,
				// (short) -1,
				// LevelTips
				// .getTipsJoinFriendCopyFailedCountNotEnough(),
				// false, null);
				// // return new KActionResult(
				// // false,
				// // LevelTips
				// // .getTipsJoinFriendCopyFailedCountNotEnough());
				// }
				// }
				// }
				/****** 以上内容 20141129注释，精英副本不需要消耗钻石 ********/

				// List<Integer> usePointList = getSaodangUsePointList(role,
				// level,
				// record);
				// int totalPoint = 0;
				// if (usePointList.size() > 0) {
				// totalPoint = usePointList.get(0);
				// }
				// if (totalPoint > 0) {
				// KGameLevelModuleExtension.getManager().sendLevelTipsMessage(
				// role,
				// KLevelModuleDialogProcesser.KEY_COPY_LEVEL_SAODANG,
				// LevelTips.getTipsSaodangUsePoint(1, totalPoint), true,
				// levelId + "," + levelType.levelType);
				// return;
				// }
			}
			// 扣除体力
			// KSupportFactory.getRoleModuleSupport().decreasePhyPower(role,
			// usePhyPower);
			String reason = "";
			PresentPointTypeEnum presentType;
			if (level.getLevelType() == KGameLevelTypeEnum.精英副本关卡) {
				reason = "精英副本关卡扣除体力";
				presentType = PresentPointTypeEnum.精英副本奖励;
			} else {
				reason = "技术副本关卡扣除体力";
				presentType = PresentPointTypeEnum.技术副本奖励;
			}
			KSupportFactory.getRoleModuleSupport().decreasePhyPower(role, usePhyPower, reason);
			// 记录关卡数据
			levelSet.recordChallengeEliteCopy();

			// 计算扫荡成功率
			// int successRate = (role.getBattlePower() * 100)
			// / (level.getFightPower());
			// if (successRate > 100) {
			// successRate = 100;
			// }
			// List<KActionResult> resultList = new ArrayList<KActionResult>();
			// for (int i = 0; i < saodangCount; i++) {
			// boolean saodangSuccess = true;//successRate >=
			// (UtilTool.random(0,
			// 100));
			// if (saodangSuccess) {
			// String tips = KGameLevelModuleExtension.getManager()
			// .caculateSaodangReward(role, level, presentType);
			// resultList.add(new KActionResult(true, tips));
			// } else {
			// resultList.add(new KActionResult(false, LevelTips
			// .getTipsSaodangNothing()));
			// }
			// }

			if (saodangCount == 1) {
				LevelRewardResultData rewardData = KGameLevelModuleExtension.getManager().caculateSaodangReward(role, level, presentType);

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

				sendUpdateCopyGameLevelInfoMsg(role, level, levelSet);

				// 通知活跃度模块
				if (level.getLevelType() == KGameLevelTypeEnum.精英副本关卡) {
					if (level.difficulty == 0) {
						KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.通关普通精英副本);
					} else {
						KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.通关困难精英副本);
					}
				} else if (level.getLevelType() == KGameLevelTypeEnum.技术副本关卡) {
					// KSupportFactory.getRewardModuleSupport().recordFun(role,
					// KVitalityTypeEnum.通关技术副本);
				}
			} else {
				LevelRewardResultData rewardData = KGameLevelModuleExtension.getManager().caculateSaodangRewardMoreCounts(role, level, presentType, saodangCount);
			    
				KGameMessage sendMsg = KGame.newLogicMessage(KLevelProtocol.SM_RESPONSE_SAODANG_DATA1);
				sendMsg.writeBoolean(true);
				rewardData.baseReward.packMsg(sendMsg);
				rewardData.saodangLotteryReward.packMsg(sendMsg);
				role.sendMsg(sendMsg);
				KDialogService.sendNullDialog(role);
				
				sendUpdateCopyGameLevelInfoMsg(role, level, levelSet);

				// 通知活跃度模块
				if (level.getLevelType() == KGameLevelTypeEnum.精英副本关卡) {
					if (level.difficulty == 0) {
						KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.通关普通精英副本);
					} else {
						KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.通关困难精英副本);
					}
				} else if (level.getLevelType() == KGameLevelTypeEnum.技术副本关卡) {
					// KSupportFactory.getRewardModuleSupport().recordFun(role,
					// KVitalityTypeEnum.通关技术副本);
				}
			}
		}
	}

	public boolean checkAndResetCopyDatas(KRole role, boolean isNeedCheck) {
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());
		boolean isDataChange = false;
		for (KGameLevelTypeEnum levelType : KGameLevelTypeEnum.values()) {
			switch (levelType) {
			case 精英副本关卡:
				KGameLevelRecord jyRecord = levelSet.getCopyRecord(levelType);
				if ((jyRecord != null && UtilTool.checkNowTimeIsArriveTomorrow(jyRecord.attribute_update_timeMillis)) || !isNeedCheck) {
					jyRecord.attribute_update_timeMillis = System.currentTimeMillis();
					for (PlayerRoleGamelevelData data : jyRecord.levelDataMap.values()) {
						KLevelTemplate level = this.eliteCopyLevelMap.get(data.getLevelId());
						data.setRemainJoinLevelCount(level.getEnterCondition().getLevelLimitJoinCount());
						data.setTodayRestCount(0);
					}
					jyRecord.notifyDB();
					isDataChange = true;
				}
				break;
			case 技术副本关卡:

				// KGameLevelRecord jsRecord =
				// levelSet.getCopyRecord(levelType);
				// if ((jsRecord != null && UtilTool
				// .checkNowTimeIsArriveTomorrow(jsRecord.attribute_update_timeMillis))
				// || !isNeedCheck) {
				// jsRecord.attribute_update_timeMillis = System
				// .currentTimeMillis();
				// for (PlayerRoleGamelevelData data : jsRecord.levelDataMap
				// .values()) {
				// KLevelTemplate level = this.techCopyLevelMap.get(data
				// .getLevelId());
				// data.setRemainJoinLevelCount(level.getEnterCondition()
				// .getLevelLimitJoinCount());
				// data.setTodayRestCount(0);
				// }
				// jsRecord.notifyDB();
				// isDataChange = true;
				// }

				break;
			}
		}
		return isDataChange;
	}

	public void checkAndUpdateGameLevelOpenState(KRole role) {
		KGameLevelSet set = KGameLevelModuleExtension.getGameLevelSet(role.getId());
		for (KLevelTemplate template : eliteCopyLevelMap.values()) {
			if (template.getEnterCondition().getOpenRoleLevel() == role.getLevel()) {
				sendUpdateCopyGameLevelInfoMsg(role, template, set);

			}
		}

		for (KLevelTemplate template : techCopyLevelMap.values()) {
			if (template.getEnterCondition().getOpenRoleLevel() == role.getLevel()) {
				sendUpdateCopyGameLevelInfoMsg(role, template, set);

			}
		}

	}

	public boolean checkAndResetEliteCopyDatas(KRole role, boolean isNeedCheck) {
		KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance().getGameLevelSet(role.getId());

		return levelSet.checkAndResetEliteCopyData(isNeedCheck);
	}

	// public void processPlayerRoleBuyEliteCopyCount(KRole role,
	// boolean isNeedCheck) {
	// KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance()
	// .getGameLevelSet(role.getId());
	// KGameLevelRecord pcRecord = levelSet
	// .getCopyRecord(KGameLevelTypeEnum.精英副本关卡);
	// if (isNeedCheck) {
	// if (pcRecord != null && pcRecord.eliteCopyData != null) {
	// VIPLevelData vipData = KSupportFactory.getVIPModuleSupport()
	// .getVIPLevelData(role.getId());
	//
	// int buyCount = pcRecord.eliteCopyData.todayBuyCount;
	// if (buyCount < maxCanBuyCount || maxCanBuyCount == -1) {
	// int usePoint = getBuyChallenCountUsePoint(buyCount);
	// KGameLevelModuleExtension
	// .getManager()
	// .sendLevelTipsMessage(
	// role,
	// KLevelModuleDialogProcesser.KEY_BUY_ELITE_COPY,
	// LevelTips
	// .getTipsBuyEliteCopyCount(usePoint),
	// true, "");
	// return;
	//
	// } else if (buyCount >= maxCanBuyCount) {
	// int vipLv = KSupportFactory.getVIPModuleSupport().getVipLv(
	// role.getId());
	// KGameLevelModuleExtension
	// .getManager()
	// .sendJoinNormalGameLevelTipsMessage(
	// role,
	// (short) -1,
	// LevelTips
	// .getTipsJoinEliteCopyFailedCountNotEnough(),
	// false, null);
	// return;
	// }
	// }
	//
	// }
	//
	// levelSet.recordBuyEliteCopyCount();
	//
	// sendUpdateCopyGameLevelInfoMsg(role, null, levelSet);
	// }

	// public static class CopyActivityConfig {
	// /**
	// * 指定的当天时间段列表，根据timeIntervalStr转化后得来
	// */
	// public static List<TimeIntervalStruct> timeIntervalList = new
	// ArrayList<TimeIntervalStruct>();
	//
	// public static int priceMultiple;
	//
	// public static boolean isOpen;
	//
	// public static void init(String timeIntervalStr, int multiple,
	// boolean is_open) throws KGameServerException {
	// priceMultiple = multiple;
	// isOpen = is_open;
	// if (timeIntervalStr != null) {
	// String[] timeStr = timeIntervalStr.split(",");
	// if (timeStr != null) {
	// timeIntervalList = new ArrayList<TimeIntervalStruct>();
	// for (int i = 0; i < timeStr.length; i++) {
	// String[] temp = (timeStr[i].substring(
	// timeStr[i].indexOf("(") + 1,
	// timeStr[i].indexOf(")"))).split("-");
	// if (temp == null || temp.length != 2) {
	// throw new KGameServerException(
	// "初始化副本活动时间参数格式错误，解释指定当天时间段字符串参数出错，str="
	// + timeIntervalStr);
	// }
	// String[] beginTimeStr = temp[0].split(":");
	// String[] endTimeStr = temp[1].split(":");
	// long beginTime;
	// long endTime;
	// try {
	// System.out.println(temp[0] + "----" + temp[1]);
	// beginTime = UtilTool.parseHHmmToMillis(temp[0]);
	// endTime = UtilTool.parseHHmmToMillis(temp[1]);
	// System.out.println(beginTime + "----" + endTime);
	// } catch (Exception e) {
	// throw new KGameServerException(
	// "初始化副本活动时间参数格式错误，解释指定当天时间段字符串参数出错，str="
	// + timeIntervalStr);
	// }
	// TimeIntervalStruct struct = new TimeIntervalStruct(
	// temp[0], temp[1], beginTime, endTime);
	// timeIntervalList.add(struct);
	// }
	//
	// if (!timeIntervalList.isEmpty()) {
	// Collections.sort(timeIntervalList);
	// }
	// }
	// }
	// }
	//
	// public static boolean isActivityPrice() {
	// if (isOpen) {
	// long nowTime = System.currentTimeMillis();
	// long todayTime = UtilTool.getNowDayInMilliseconds();
	// for (TimeIntervalStruct time : timeIntervalList) {
	// if ((todayTime + time.getBeginTime()) <= nowTime
	// && (todayTime + time.getEndTime()) >= nowTime) {
	// return true;
	// }
	// }
	// }
	// return false;
	// }
	// }
}
