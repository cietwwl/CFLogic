package com.kola.kmp.logic.level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.level.impl.KAGameLevelSet;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kgame.db.dataobject.DBLevelRecord;
import com.kola.kmp.logic.level.KGameLevelRecord.EliteCopyData;
import com.kola.kmp.logic.level.KGameLevelRecord.FriendCopyData;
import com.kola.kmp.logic.level.KGameLevelRecord.PetChallengeCopyData;
import com.kola.kmp.logic.level.KGameLevelRecord.PetCopyData;
import com.kola.kmp.logic.level.KGameLevelRecord.PlayerRoleGamelevelData;
import com.kola.kmp.logic.level.KGameLevelRecord.TowerCopyData;
import com.kola.kmp.logic.level.copys.KCopyManager;
import com.kola.kmp.logic.level.copys.KFriendCopyManager;
import com.kola.kmp.logic.level.copys.KPetChallengeCopyManager;
import com.kola.kmp.logic.level.copys.KTowerCopyManager;
import com.kola.kmp.logic.level.petcopy.KPetCopyManager;
import com.kola.kmp.logic.other.KGameLevelRecordDBTypeEnum;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;

public class KGameLevelSet extends KAGameLevelSet<KGameLevelRecord> {

	private static final int VERSION_FIRST = 20140625;
	private static final int CURRENT_VERSION = VERSION_FIRST;
	private static final String JSON_KEY_VERSION = "0";
	public static final String JSON_KEY_SCENARIO_PASS_ITEM = "1";
	public static final String JSON_KEY_SCENARIO_S_ITEM = "2";
	public static final String JSON_KEY_OPEN_BATTLE_POWER_SLOT = "3";

	// 普通关卡数据记录
	public ConcurrentHashMap<Integer, PlayerRoleGamelevelData> normalLevelDataMap = new ConcurrentHashMap<Integer, KGameLevelRecord.PlayerRoleGamelevelData>();
	// 普通关卡ID与DB记录ID 的mapping，key：关卡ID，value：DB记录Id
	HashMap<Integer, Long> levelDataRecordMapping = new HashMap<Integer, Long>();
	// 当前普通关卡数据正在使用的DB记录
	public KGameLevelRecord currentInUseNormalLevelRecord;

	public int maxCompleteNormalLevelId = KGameLevelModuleExtension.getManager().firstNormalGameLevel.getLevelId();
	/**
	 * 副本关卡记录
	 */
	public Map<KGameLevelTypeEnum, KGameLevelRecord> copyLevelRecordMap = new HashMap<KGameLevelTypeEnum, KGameLevelRecord>();
	// 普通关卡剧本全通奖励记录
	public HashSet<Integer> scenarioPriceRecordSet = new HashSet<Integer>();

	// 普通关卡剧本全S奖励记录
	public HashSet<Integer> scenarioSLevelPriceRecordSet = new HashSet<Integer>();

	// //是否开放战斗怒气槽
	private boolean isOpenBattlePowerSlot;

	protected KGameLevelSet(long roleId, boolean isFirstNew) {
		super(roleId, isFirstNew);
	}

	@Override
	protected Map<Long, KGameLevelRecord> initGameLevels(List<DBLevelRecord> dbdatas) {
		Map<Long, KGameLevelRecord> result = new HashMap<Long, KGameLevelRecord>();
		for (DBLevelRecord dbdata : dbdatas) {
			KGameLevelRecord record = new KGameLevelRecord(this, dbdata);
			result.put(record.getId(), record);
			if (record.dbTypeEnum == KGameLevelRecordDBTypeEnum.普通关卡数据记录) {
				for (PlayerRoleGamelevelData data : record.levelDataMap.values()) {
					normalLevelDataMap.put(data.getLevelId(), data);
					levelDataRecordMapping.put(data.getLevelId(), record.getId());
					if (data.getLevelId() > maxCompleteNormalLevelId) {
						maxCompleteNormalLevelId = data.getLevelId();
					}
				}
				if (record.levelDataMap.size() < KGameLevelRecord.LEVEL_RECORD_MAX_COUNT) {
					currentInUseNormalLevelRecord = record;
				}
			} else if (record.dbTypeEnum == KGameLevelRecordDBTypeEnum.精英副本关卡数据记录) {
				this.copyLevelRecordMap.put(KGameLevelTypeEnum.精英副本关卡, record);
			} else if (record.dbTypeEnum == KGameLevelRecordDBTypeEnum.技术副本关卡数据记录) {
				this.copyLevelRecordMap.put(KGameLevelTypeEnum.技术副本关卡, record);
			} else if (record.dbTypeEnum == KGameLevelRecordDBTypeEnum.好友副本关卡数据记录) {
				this.copyLevelRecordMap.put(KGameLevelTypeEnum.好友副本关卡, record);
			} else if (record.dbTypeEnum == KGameLevelRecordDBTypeEnum.随从副本关卡数据记录) {
				this.copyLevelRecordMap.put(KGameLevelTypeEnum.随从副本关卡, record);
			} else if (record.dbTypeEnum == KGameLevelRecordDBTypeEnum.爬塔副本关卡数据记录) {
				this.copyLevelRecordMap.put(KGameLevelTypeEnum.爬塔副本关卡, record);
			} else if (record.dbTypeEnum == KGameLevelRecordDBTypeEnum.随从挑战副本关卡数据记录) {
				this.copyLevelRecordMap.put(KGameLevelTypeEnum.随从挑战副本关卡, record);
			} else if (record.dbTypeEnum == KGameLevelRecordDBTypeEnum.高级随从挑战副本关卡数据记录) {
				this.copyLevelRecordMap.put(KGameLevelTypeEnum.高级随从挑战副本关卡, record);
			}
		}
		return result;
	}

	@Override
	protected void decodeCA(String ca) {
		if (ca == null || ca.trim().length() == 0) {
			return;
		}
		try {
			JSONObject obj = new JSONObject(ca);
			int version = obj.optInt(JSON_KEY_VERSION);
			obj.remove(JSON_KEY_VERSION);
			switch (version) {
			case VERSION_FIRST:
				this.decodeV1(obj);
				break;
			}
		} catch (Exception e) {
			// TODO 解析角色剧本关卡记录扩展属性的处理
			_LOGGER.error("解析角色剧本关卡记录扩展属性的时候出现异常！！arrt=" + ca, e);
		}
	}

	private void decodeV1(JSONObject obj) throws Exception {
		try {
			if (obj.has(JSON_KEY_SCENARIO_PASS_ITEM)) {
				String infoStr = obj.optString(JSON_KEY_SCENARIO_PASS_ITEM, null);
				if (infoStr != null && !infoStr.equals("")) {
					String[] ids = infoStr.split(",");
					for (int i = 0; i < ids.length; i++) {
						this.scenarioPriceRecordSet.add(Integer.parseInt(ids[i]));
					}
				}
			}
			if (obj.has(JSON_KEY_SCENARIO_S_ITEM)) {
				String infoStr = obj.optString(JSON_KEY_SCENARIO_S_ITEM, null);
				if (infoStr != null && !infoStr.equals("")) {
					String[] ids = infoStr.split(",");
					for (int i = 0; i < ids.length; i++) {
						this.scenarioSLevelPriceRecordSet.add(Integer.parseInt(ids[i]));
					}
				}
			}
			this.isOpenBattlePowerSlot = (obj.optInt(JSON_KEY_OPEN_BATTLE_POWER_SLOT, 0) == 1);
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	protected String encodeCA() {
		// TODO 处理角色的关卡数据记录扩展属性的encode方法
		JSONObject obj = new JSONObject();
		try {
			obj.put(JSON_KEY_VERSION, CURRENT_VERSION);
			String infoStr = "";
			if (scenarioPriceRecordSet.size() > 0) {
				int index = 0;
				for (Integer scenarioId : scenarioPriceRecordSet) {
					infoStr += scenarioId;
					if (index < scenarioPriceRecordSet.size() - 1) {
						infoStr += ",";
					}
					index++;
				}
			}
			obj.put(JSON_KEY_SCENARIO_PASS_ITEM, infoStr);
			infoStr = "";
			if (scenarioSLevelPriceRecordSet.size() > 0) {
				int index = 0;
				for (Integer scenarioId : scenarioSLevelPriceRecordSet) {
					infoStr += scenarioId;
					if (index < scenarioSLevelPriceRecordSet.size() - 1) {
						infoStr += ",";
					}
					index++;
				}
			}
			obj.put(JSON_KEY_SCENARIO_S_ITEM, infoStr);
			obj.put(JSON_KEY_OPEN_BATTLE_POWER_SLOT, isOpenBattlePowerSlot ? 1 : 0);
		} catch (Exception ex) {
			_LOGGER.error("encodeAttribute出现异常！此时json的字符串是：" + obj.toString(), ex);
		}
		return obj.toString();
	}

	/**
	 * 增加或者修改一个关卡进行情况数据
	 * 
	 * @param levelId
	 *            关卡ID
	 * @param remainJoinLevelCount
	 *            当天剩余进入次数
	 * @param levelEvaluate
	 *            关卡完成评价
	 * @param isCompleted
	 *            是否已经完成
	 */
	public void addOrModifyNormalGameLevelData(int levelId, int remainJoinLevelCount, byte levelEvaluate, boolean isCompleted) {

		if (normalLevelDataMap.containsKey(levelId)) {
			PlayerRoleGamelevelData data = normalLevelDataMap.get(levelId);

			data.setLevelEvaluate(levelEvaluate);
			data.setCompleted(isCompleted);
			data.setRemainJoinLevelCount(remainJoinLevelCount);
			if (levelDataRecordMapping.containsKey(levelId)) {
				KGameLevelRecord record = this.getLevel(levelDataRecordMapping.get(levelId));
				record.getLevelDataMap().put(levelId, data);
				record.notifyDB();
			}
		} else {
			addNormalGameLevelData(levelId, remainJoinLevelCount, levelEvaluate, isCompleted);
		}
		if (levelId > maxCompleteNormalLevelId) {
			maxCompleteNormalLevelId = levelId;
		}
	}

	private void addNormalGameLevelData(int levelId, int remainJoinLevelCount, byte levelEvaluate, boolean isCompleted) {
		if (currentInUseNormalLevelRecord == null || currentInUseNormalLevelRecord.getRecordLevelDataSize() >= KGameLevelRecord.LEVEL_RECORD_MAX_COUNT) {
			KGameLevelRecord newRecord = new KGameLevelRecord(this, KGameLevelRecordDBTypeEnum.普通关卡数据记录.getLevelRecordDbType());
			this.currentInUseNormalLevelRecord = newRecord;
			this.addLevel(newRecord);
		}

		PlayerRoleGamelevelData data = new PlayerRoleGamelevelData(KGameLevelTypeEnum.普通关卡);
		data.setLevelId(levelId);
		data.setRemainJoinLevelCount(remainJoinLevelCount);
		data.setLevelEvaluate(levelEvaluate);
		data.setCompleted(isCompleted);
		normalLevelDataMap.put(levelId, data);
		this.currentInUseNormalLevelRecord.levelDataMap.put(levelId, data);
		levelDataRecordMapping.put(levelId, this.currentInUseNormalLevelRecord.getId());
		this.currentInUseNormalLevelRecord.notifyDB();
	}

	/**
	 * 获取当前完成的最大普通关卡模版ID
	 * 
	 * @return
	 */
	public int getMaxCompleteNormalLevelId() {
		int targetLevelId = 0;
		if (currentInUseNormalLevelRecord != null && currentInUseNormalLevelRecord.getLevelDataMap() != null) {
			for (PlayerRoleGamelevelData data : currentInUseNormalLevelRecord.getLevelDataMap().values()) {
				if (data.isCompleted() && data.getLevelId() > targetLevelId) {
					targetLevelId = data.getLevelId();
				}
			}
		}
		if (targetLevelId > 0) {
			return targetLevelId;
		} else {
			if (normalLevelDataMap != null && normalLevelDataMap.size() > 0) {
				for (PlayerRoleGamelevelData data : normalLevelDataMap.values()) {
					if (data.isCompleted() && data.getLevelId() > targetLevelId) {
						targetLevelId = data.getLevelId();
					}
				}
			}
		}

		if (targetLevelId > 0) {
			return targetLevelId;
		} else {
			return KGameLevelModuleExtension.getManager().firstNormalGameLevel.getLevelId();
		}
	}

	/**
	 * 获取某个普通关卡进行情况数据
	 * 
	 * @param levelId
	 *            关卡ID
	 * @return
	 */
	public PlayerRoleGamelevelData getPlayerRoleNormalGamelevelData(int levelId) {
		return normalLevelDataMap.get(levelId);
	}

	/**
	 * 检测关卡是否完成
	 * 
	 * @param levelId
	 * @return
	 */
	public boolean checkGameLevelIsCompleted(int levelId) {
		if (normalLevelDataMap.containsKey(levelId)) {
			if (normalLevelDataMap.get(levelId).isCompleted()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param levelId
	 * @param copyType
	 * @param remainJoinLevelCount
	 * @param levelEvaluate
	 * @param isCompleted
	 */
	public void addOrModifyCopyGameLevelData(int levelId, KGameLevelTypeEnum levelType, int remainJoinLevelCount, byte levelEvaluate, boolean isCompleted, boolean isGetFirstDropPrice,
			int todayRestCount) {
		KGameLevelRecord record;
		if (!this.copyLevelRecordMap.containsKey(levelType)) {
			record = new KGameLevelRecord(this, getKGameLevelRecordDBTypeEnumByLevelType(levelType).getLevelRecordDbType());
			this.addLevel(record);
		} else {
			record = this.copyLevelRecordMap.get(levelType);
		}
		if (record.levelDataMap.containsKey(levelId)) {
			PlayerRoleGamelevelData data = record.levelDataMap.get(levelId);

			data.setLevelEvaluate(levelEvaluate);
			data.setCompleted(isCompleted);
			data.setRemainJoinLevelCount(remainJoinLevelCount);
			data.setGetFirstDropPrice(isGetFirstDropPrice);
			data.setTodayRestCount(todayRestCount);
			record.notifyDB();
		} else {
			PlayerRoleGamelevelData data = new PlayerRoleGamelevelData(levelType);
			data.setLevelId(levelId);
			data.setRemainJoinLevelCount(remainJoinLevelCount);
			data.setLevelEvaluate(levelEvaluate);
			data.setCompleted(isCompleted);
			data.setGetFirstDropPrice(isGetFirstDropPrice);
			data.setTodayRestCount(todayRestCount);
			record.getLevelDataMap().put(levelId, data);
			record.notifyDB();
		}
	}

	/**
	 * 
	 * @param levelId
	 * @param copyType
	 * @param remainJoinLevelCount
	 * @param levelEvaluate
	 * @param isCompleted
	 */
	public void addOrModifyFriendCopyData(int levelId, KGameLevelTypeEnum levelType, int remainJoinLevelCount, byte levelEvaluate, boolean isCompleted, int waveCount) {
		KGameLevelRecord record;
		if (!this.copyLevelRecordMap.containsKey(levelType)) {
			record = new KGameLevelRecord(this, getKGameLevelRecordDBTypeEnumByLevelType(levelType).getLevelRecordDbType());
			this.addLevel(record);
		} else {
			record = this.copyLevelRecordMap.get(levelType);
		}
		if (record.levelDataMap.containsKey(levelId)) {
			PlayerRoleGamelevelData data = record.levelDataMap.get(levelId);

			data.setLevelEvaluate(levelEvaluate);
			data.setCompleted(isCompleted);
			data.setRemainJoinLevelCount(remainJoinLevelCount);
			data.setMaxWave(waveCount);
			record.notifyDB();
		} else {
			PlayerRoleGamelevelData data = new PlayerRoleGamelevelData(levelType);
			data.setLevelId(levelId);
			data.setRemainJoinLevelCount(remainJoinLevelCount);
			data.setLevelEvaluate(levelEvaluate);
			data.setCompleted(isCompleted);
			data.setMaxWave(waveCount);
			record.getLevelDataMap().put(levelId, data);
			record.notifyDB();
		}
	}

	private KGameLevelRecordDBTypeEnum getKGameLevelRecordDBTypeEnumByLevelType(KGameLevelTypeEnum levelType) {
		switch (levelType) {
		case 精英副本关卡:
			return KGameLevelRecordDBTypeEnum.精英副本关卡数据记录;
		case 技术副本关卡:
			return KGameLevelRecordDBTypeEnum.技术副本关卡数据记录;
		case 好友副本关卡:
			return KGameLevelRecordDBTypeEnum.好友副本关卡数据记录;
		case 随从副本关卡:
			return KGameLevelRecordDBTypeEnum.随从副本关卡数据记录;
		case 爬塔副本关卡:
			return KGameLevelRecordDBTypeEnum.爬塔副本关卡数据记录;
		case 随从挑战副本关卡:
			return KGameLevelRecordDBTypeEnum.随从挑战副本关卡数据记录;
		case 高级随从挑战副本关卡:
			return KGameLevelRecordDBTypeEnum.高级随从挑战副本关卡数据记录;
		default:
			return null;
		}
	}

	public KGameLevelRecord getCopyRecord(KGameLevelTypeEnum levelType) {
		KGameLevelRecord record;
		if (!copyLevelRecordMap.containsKey(levelType)) {
			record = new KGameLevelRecord(this, getKGameLevelRecordDBTypeEnumByLevelType(levelType).getLevelRecordDbType());
			this.addLevel(record);
			copyLevelRecordMap.put(levelType, record);
		} else {
			record = copyLevelRecordMap.get(levelType);
		}
		return record;
	}

	public PlayerRoleGamelevelData getCopyLevelData(int levelId, KGameLevelTypeEnum levelType) {
		PlayerRoleGamelevelData data = null;
		KGameLevelRecord record;
		if (!copyLevelRecordMap.containsKey(levelType)) {
			record = new KGameLevelRecord(this, getKGameLevelRecordDBTypeEnumByLevelType(levelType).getLevelRecordDbType());
			this.addLevel(record);
			copyLevelRecordMap.put(levelType, record);
		} else {
			record = copyLevelRecordMap.get(levelType);
		}

		if (record.levelDataMap.containsKey(levelId)) {
			data = record.levelDataMap.get(levelId);
		}

		return data;
	}

	/**
	 * 记录副本关卡重置一次
	 * 
	 * @param levelId
	 * @param levelType
	 */
	public void recordResetCopylevelData(KLevelTemplate level, KGameLevelTypeEnum levelType) {
		KGameLevelRecord record = getCopyRecord(levelType);
		if (record != null) {
			PlayerRoleGamelevelData data = record.levelDataMap.get(level.getLevelId());
			if (data != null) {
				int resetCount = data.getTodayRestCount() + 1;
				data.setTodayRestCount(resetCount);
				data.setRemainJoinLevelCount(level.getEnterCondition().getLevelLimitJoinCount());
				record.notifyDB();
			}
		}
	}

	/**
	 * 记录副本关卡重置一次
	 * 
	 * @param levelId
	 * @param levelType
	 */
	public void recordSaodangCopylevelData(KLevelTemplate level, KGameLevelTypeEnum levelType, int saodangCount) {
		KGameLevelRecord record = getCopyRecord(levelType);
		if (record != null) {
			PlayerRoleGamelevelData data = record.levelDataMap.get(level.getLevelId());
			if (data != null) {
				int resetCount = data.getTodayRestCount();
				int remainCount = data.getRemainJoinLevelCount();

				if (remainCount >= saodangCount) {
					data.setRemainJoinLevelCount(remainCount - saodangCount);
				} else {
					data.setRemainJoinLevelCount(0);
					data.setTodayRestCount(resetCount + (saodangCount - remainCount));
				}
				record.notifyDB();
			}
		}
	}

	/**
	 * 记录副本关卡已领取首次通关奖励
	 * 
	 * @param levelId
	 * @param levelType
	 */
	public void recordGetCopylevelFirstDropPrice(int levelId, KGameLevelTypeEnum levelType) {
		KGameLevelRecord record = getCopyRecord(levelType);
		if (record != null) {
			PlayerRoleGamelevelData data = record.levelDataMap.get(levelId);
			if (data != null) {
				data.setGetFirstDropPrice(true);
				record.notifyDB();
			}
		}
	}

	/**
	 * 记录购买好友副本挑战次数
	 */
	public void recordBuyFriendCopyCount() {
		KGameLevelRecord record = getCopyRecord(KGameLevelTypeEnum.好友副本关卡);
		if (record != null && record.friendCopyData != null) {
			record.friendCopyData.todayBuyCount++;
			record.friendCopyData.remainChallengeCount++;
			record.notifyDB();
		}
	}

	/**
	 * 记录购买好友副本挑战次数
	 */
	public void recordChallengeFriendCopy(long friendRoleId, long coolEndTime) {

		KGameLevelRecord record = getCopyRecord(KGameLevelTypeEnum.好友副本关卡);
		if (record != null && record.friendCopyData != null) {
			if (record.friendCopyData.remainChallengeCount > 0) {
				record.friendCopyData.remainChallengeCount--;
				record.friendCopyData.challengeCount++;
				if (friendRoleId > 0) {
					if (record.friendCopyData.strangers.containsKey(friendRoleId)) {
						record.friendCopyData.strangerCoolingTimeMap.put(friendRoleId, coolEndTime);
					} else {
						record.friendCopyData.friendCoolingTimeMap.put(friendRoleId, coolEndTime);
					}
				}
				record.notifyDB();
			}
		}
	}

	/**
	 * 记录好友副本被邀请挑战次数
	 */
	public void recordFriendCopyBeInvite() {
		KGameLevelRecord record = getCopyRecord(KGameLevelTypeEnum.好友副本关卡);
		if (record != null && record.friendCopyData != null) {
			record.friendCopyData.beInviteCount++;
			record.notifyDB();
		}
	}

	/**
	 * 检测并重置好友副本的相关数据。并检测好友冷却时间是否到时。
	 */
	public boolean checkAndResetFriendCopyData(boolean isNeedCheck) {
		KGameLevelRecord record = getCopyRecord(KGameLevelTypeEnum.好友副本关卡);
		long nowTime = System.currentTimeMillis();
		boolean isDataChanged = false;
		boolean isResetData = false;
		if (!isNeedCheck || (isNeedCheck && UtilTool.checkNowTimeIsArriveTomorrow(record.attribute_update_timeMillis))) {
			if (record.friendCopyData == null) {
				record.friendCopyData = new FriendCopyData();
			}
			record.friendCopyData.todayBuyCount = 0;
			record.friendCopyData.challengeCount = 0;
			record.friendCopyData.remainChallengeCount = KFriendCopyManager.free_challenge_count;
			record.attribute_update_timeMillis = System.currentTimeMillis();
			record.friendCopyData.beInviteCount = 0;
			isDataChanged = true;
			isResetData = true;
		}

		if (record != null && record.friendCopyData != null) {
			List<Long> removeIds = new ArrayList<Long>();
			long endTime;
			for (Long roleId : record.friendCopyData.friendCoolingTimeMap.keySet()) {
				endTime = record.friendCopyData.friendCoolingTimeMap.get(roleId);
				if (nowTime > endTime) {
					removeIds.add(roleId);
				}
			}
			if (!removeIds.isEmpty()) {
				for (Long roleId : removeIds) {
					record.friendCopyData.friendCoolingTimeMap.remove(roleId);
				}
				isDataChanged = true;
			}
			removeIds.clear();
			for (Long roleId : record.friendCopyData.strangerCoolingTimeMap.keySet()) {
				endTime = record.friendCopyData.strangerCoolingTimeMap.get(roleId);
				if (nowTime > endTime) {
					removeIds.add(roleId);
				}
			}
			if (!removeIds.isEmpty()) {
				for (Long roleId : removeIds) {
					record.friendCopyData.strangerCoolingTimeMap.remove(roleId);
				}
				isDataChanged = true;
			}
		}

		if (isDataChanged) {
			record.notifyDB();
		}

		return isResetData;
	}

	/**
	 * 记录挑战随从副本
	 */
	public void recordChallengePetCopy() {

		KGameLevelRecord record = getCopyRecord(KGameLevelTypeEnum.随从副本关卡);
		if (record != null && record.petCopyData != null) {
			if (record.petCopyData.remainChallengeCount > 0) {
				record.petCopyData.remainChallengeCount--;
				record.petCopyData.challengeCount++;
				record.notifyDB();
			}
		}
	}

	/**
	 * 记录购买随从副本挑战次数
	 */
	public void recordBuyPetCopyCount() {
		KGameLevelRecord record = getCopyRecord(KGameLevelTypeEnum.随从副本关卡);
		if (record != null && record.petCopyData != null) {
			record.petCopyData.todayBuyCount++;
			record.petCopyData.remainChallengeCount++;
			record.notifyDB();
		}
	}

	/**
	 * 检测并重置随从副本的相关数据。
	 */
	public boolean checkAndResetPetCopyData(boolean isNeedCheck) {
		KGameLevelRecord record = getCopyRecord(KGameLevelTypeEnum.随从副本关卡);
		long nowTime = System.currentTimeMillis();
		boolean isDataChanged = false;
		boolean isResetData = false;
		if (!isNeedCheck || (isNeedCheck && UtilTool.checkNowTimeIsArriveTomorrow(record.attribute_update_timeMillis))) {
			if (record.petCopyData == null) {
				record.petCopyData = new PetCopyData();
			}
			record.petCopyData.todayBuyCount = 0;
			record.petCopyData.challengeCount = 0;
			record.petCopyData.remainChallengeCount = KPetCopyManager.free_challenge_count;
			record.attribute_update_timeMillis = System.currentTimeMillis();
			isDataChanged = true;
			isResetData = true;
		}

		if (isDataChanged) {
			record.notifyDB();
		}

		return isResetData;
	}

	/**
	 * 记录挑战精英副本
	 */
	public void recordChallengeEliteCopy() {

		KGameLevelRecord record = getCopyRecord(KGameLevelTypeEnum.精英副本关卡);
		if (record != null && record.eliteCopyData != null) {
			if (record.eliteCopyData.remainChallengeCount > 0) {
				record.eliteCopyData.remainChallengeCount--;
			}
			record.eliteCopyData.challengeCount++;
			record.notifyDB();
		}
	}

	/**
	 * 记录购买精英副本挑战次数
	 */
	public void recordBuyEliteCopyCount() {
		KGameLevelRecord record = getCopyRecord(KGameLevelTypeEnum.精英副本关卡);
		if (record != null && record.eliteCopyData != null) {
			record.eliteCopyData.todayBuyCount++;
			record.eliteCopyData.remainChallengeCount++;
			record.notifyDB();
		}
	}

	/**
	 * 检测并重置精英副本的相关数据。
	 */
	public boolean checkAndResetEliteCopyData(boolean isNeedCheck) {
		KGameLevelRecord record = getCopyRecord(KGameLevelTypeEnum.精英副本关卡);
		if (record != null) {
			if (record.eliteCopyData == null) {
				record.eliteCopyData = new EliteCopyData();
				record.eliteCopyData.remainChallengeCount = KCopyManager.free_challenge_count;
				record.eliteCopyData.todayBuyCount = 0;
			}
		}
		long nowTime = System.currentTimeMillis();
		boolean isDataChanged = false;
		boolean isResetData = false;
		if (!isNeedCheck || (isNeedCheck && UtilTool.checkNowTimeIsArriveTomorrow(record.attribute_update_timeMillis))) {
			if (record.eliteCopyData == null) {
				record.eliteCopyData = new EliteCopyData();
			}
			record.eliteCopyData.todayBuyCount = 0;
			record.eliteCopyData.challengeCount = 0;
			record.eliteCopyData.remainChallengeCount = KCopyManager.free_challenge_count;
			record.attribute_update_timeMillis = System.currentTimeMillis();
			isDataChanged = true;
			isResetData = true;
		}

		if (isDataChanged) {
			record.notifyDB();
		}

		return isResetData;
	}

	/**
	 * 记录挑战爬塔副本
	 */
	public void recordChallengeTowerCopy() {

		KGameLevelRecord record = getCopyRecord(KGameLevelTypeEnum.爬塔副本关卡);
		if (record != null && record.towerCopyData != null) {
			record.towerCopyData.challengeCount++;
			record.notifyDB();
		}
	}

	public void recordCompleteTowerCopy(int completeLevelId) {
		KGameLevelRecord record = getCopyRecord(KGameLevelTypeEnum.爬塔副本关卡);
		if (record != null && record.towerCopyData != null) {
			record.towerCopyData.nowLevelId = completeLevelId;
			record.notifyDB();
		}
	}

	/**
	 * 记录获取爬塔副本每日奖励
	 */
	public void recordGetTowerReward() {
		KGameLevelRecord record = getCopyRecord(KGameLevelTypeEnum.爬塔副本关卡);
		if (record != null && record.towerCopyData != null) {
			record.towerCopyData.todayGetReward = true;
			record.notifyDB();
		}
	}

	/**
	 * 检测并重置爬塔副本的相关数据。
	 */
	public boolean checkAndResetTowerCopyData(boolean isNeedCheck) {
		KGameLevelRecord record = getCopyRecord(KGameLevelTypeEnum.爬塔副本关卡);
		if (record != null) {
			if (record.towerCopyData == null) {
				record.towerCopyData = new TowerCopyData();
				// record.towerCopyData.nowLevelId =
				// KTowerCopyManager.firstLevel.getLevelId();
			}
		}
		long nowTime = System.currentTimeMillis();
		boolean isDataChanged = false;
		boolean isResetData = false;
		if (!isNeedCheck || (isNeedCheck && UtilTool.checkNowTimeIsArriveTomorrow(record.attribute_update_timeMillis))) {
			record.towerCopyData.todayGetReward = false;
			record.towerCopyData.challengeCount = 0;
			record.attribute_update_timeMillis = System.currentTimeMillis();
			isDataChanged = true;
			isResetData = true;
		}

		if (isDataChanged) {
			record.notifyDB();
		}

		return isResetData;
	}

	/**
	 * 记录挑战爬塔副本
	 */
	public void recordChallengePetChallengeCopy() {

		KGameLevelRecord record = getCopyRecord(KGameLevelTypeEnum.随从挑战副本关卡);
		if (record != null && record.petChallengeCopyData != null) {
			record.petChallengeCopyData.challengeCount++;
			record.notifyDB();
		}
	}

	public void recordCompletePetChallengeCopy(int nextLevelId, long restHp, long restPetHp) {
		KGameLevelRecord record = getCopyRecord(KGameLevelTypeEnum.随从挑战副本关卡);
		if (record != null && record.petChallengeCopyData != null) {
			record.petChallengeCopyData.nowLevelId = nextLevelId;
			record.petChallengeCopyData.restHp = restHp;
			record.petChallengeCopyData.restPetHp = restPetHp;
			record.notifyDB();
		}
	}
	
	public void recordCompletePetChallengeCopyLastLevel() {
		KGameLevelRecord record = getCopyRecord(KGameLevelTypeEnum.随从挑战副本关卡);
		if (record != null && record.petChallengeCopyData != null) {
			record.petChallengeCopyData.isCompleteLastLevel = true;
			record.petChallengeCopyData.isPassCopy = true;
			record.notifyDB();
		}
	}
	
	public void recordUpdatePetChallengeCopyReward(BaseRewardData data){
		KGameLevelRecord record = getCopyRecord(KGameLevelTypeEnum.随从挑战副本关卡);
		if (record != null && record.petChallengeCopyData != null) {
			record.petChallengeCopyData.completeLevelReward = data;
			record.notifyDB();
		}
	}

	public void recordResetPetChallengeCopy() {
		KGameLevelRecord record = getCopyRecord(KGameLevelTypeEnum.随从挑战副本关卡);
		if (record != null && record.petChallengeCopyData != null) {
			record.petChallengeCopyData.nowLevelId = KPetChallengeCopyManager.firstLevel.getLevelId();
			record.petChallengeCopyData.restHp = -1;
			record.petChallengeCopyData.restPetHp = -1;
			record.petChallengeCopyData.isCompleteLastLevel = false;
			record.petChallengeCopyData.completeLevelReward = new BaseRewardData(null, null, null, null, null);
			record.notifyDB();
		}
	}
	
	public void recordSaodangPetChallengeCopy(){
		KGameLevelRecord record = getCopyRecord(KGameLevelTypeEnum.随从挑战副本关卡);
		if (record != null && record.petChallengeCopyData != null) {
			record.petChallengeCopyData.nowLevelId = KPetChallengeCopyManager.firstLevel.getLevelId();
			record.petChallengeCopyData.restHp = -1;
			record.petChallengeCopyData.restPetHp = -1;
			record.petChallengeCopyData.isCompleteLastLevel = false;
			record.petChallengeCopyData.saodangCount++;
			record.petChallengeCopyData.completeLevelReward = new BaseRewardData(null, null, null, null, null);
			record.notifyDB();
		}
	}

	/**
	 * 检测并重置随从挑战副本的相关数据。
	 */
	public boolean checkAndResetPetChallengeCopyData(boolean isNeedCheck) {
		KGameLevelRecord record = getCopyRecord(KGameLevelTypeEnum.随从挑战副本关卡);
		if (record != null) {
			if (record.petChallengeCopyData == null) {
				record.petChallengeCopyData = new PetChallengeCopyData();
				record.petChallengeCopyData.nowLevelId = KPetChallengeCopyManager.firstLevel.getLevelId();
				record.petChallengeCopyData.challengeCount = 0;
				record.petChallengeCopyData.isCompleteLastLevel = false;
				record.notifyDB();
			}
		}
		long nowTime = System.currentTimeMillis();
		boolean isDataChanged = false;
		boolean isResetData = false;
		if (!isNeedCheck || (isNeedCheck && UtilTool.checkNowTimeIsArriveTomorrow(record.attribute_update_timeMillis))) {
			record.petChallengeCopyData.challengeCount = 0;
			record.petChallengeCopyData.saodangCount = 0;
			record.attribute_update_timeMillis = System.currentTimeMillis();			
			isDataChanged = true;
			isResetData = true;
		}

		if (isDataChanged) {
			record.notifyDB();
		}

		return false;
	}

	/**
	 * 
	 * @param scenarioId
	 * @return
	 */
	public boolean checkHasGetScenarioPrice(int scenarioId) {
		return scenarioPriceRecordSet.contains(scenarioId);
	}

	/**
	 * 
	 * @param scenarioId
	 */
	public void recordGetScenarioPrice(int scenarioId) {
		scenarioPriceRecordSet.add(scenarioId);
		this.notifyDB();
	}

	/**
	 * 
	 * @param scenarioId
	 * @return
	 */
	public boolean checkHasGetScenarioSLevelPrice(int scenarioId) {
		return scenarioSLevelPriceRecordSet.contains(scenarioId);
	}

	/**
	 * 
	 * @param scenarioId
	 */
	public void recordGetScenarioSLevelPrice(int scenarioId) {
		scenarioSLevelPriceRecordSet.add(scenarioId);
		this.notifyDB();
	}

	public boolean isOpenBattlePowerSlot() {
		return isOpenBattlePowerSlot;
	}

	public void notifyOpenBattlePowerSlot() {
		this.isOpenBattlePowerSlot = true;
		this.notifyDB();
	}

	public void notifyUpdateLevelSet() {
		super.notifyDB();
	}

}
