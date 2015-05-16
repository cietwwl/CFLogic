package com.kola.kmp.logic.level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.koala.thirdpart.json.JSONArray;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.level.impl.KAGameLevel;
import com.kola.kgame.cache.level.impl.KAGameLevelSet;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kgame.db.dataobject.DBLevelRecord;
import com.kola.kmp.logic.level.copys.KCopyManager;
import com.kola.kmp.logic.level.copys.KFriendCopyManager;
import com.kola.kmp.logic.level.copys.KPetChallengeCopyManager;
import com.kola.kmp.logic.level.copys.KSeniorPetChallengeCopyManager;
import com.kola.kmp.logic.level.copys.KTowerCopyManager;
import com.kola.kmp.logic.level.petcopy.KPetCopyManager;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KGameLevelRecordDBTypeEnum;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.support.KSupportFactory;

public class KGameLevelRecord extends KAGameLevel {

	private static final int VERSION_FIRST = 20121023;
	private static final int CURRENT_VERSION = VERSION_FIRST;

	private static final String KEY_VERSION = "K0";
	private static final String KEY_UPDATE_TIMEMILLIS = "K1";
	private static final String KEY_FRIEND_COPY_DATA = "K2";
	private static final String KEY_PET_COPY_DATA = "K3";
	private static final String KEY_ELITE_COPY_DATA = "K4";
	private static final String KEY_TOWER_COPY_DATA = "K5";
	private static final String KEY_PET_CHALLENGE_COPY_DATA = "K6";
	private static final String KEY_SENIOR_PET_CHALLENGE_COPY_DATA = "K7";
	private static final String KEY_FRIEND_COPY_BUY_COUNT = "L1";
	private static final String KEY_FRIEND_COPY_REMAIN_COUNT = "L2";
	private static final String KEY_FRIEND_COPY_CHALLENGE_COUNT = "L3";
	private static final String KEY_FRIEND_COPY_BE_INVITE_COUNT = "L4";
	private static final String KEY_PET_COPY_BUY_COUNT = "P1";
	private static final String KEY_PET_COPY_REMAIN_COUNT = "P2";
	private static final String KEY_PET_COPY_CHALLENGE_COUNT = "P3";
	private static final String KEY_ELITE_COPY_BUY_COUNT = "E1";
	private static final String KEY_ELITE_COPY_REMAIN_COUNT = "E2";
	private static final String KEY_ELITE_COPY_CHALLENGE_COUNT = "E3";
	private static final String KEY_TOWER_COPY_IS_GET_TODAY_REWARD = "T1";
	private static final String KEY_TOWER_COPY_REMAIN_COUNT = "T2";
	private static final String KEY_TOWER_COPY_CHALLENGE_COUNT = "T3";
	private static final String KEY_TOWER_COPY_NOW_LEVEL_ID = "T4";
	// --------------------------------------------
	private static final String KEY_PET_CHALLENGE_COPY_CHALLENGE_COUNT = "C1";
	private static final String KEY_PET_CHALLENGE_COPY_NOW_LEVEL_ID = "C2";
	private static final String KEY_PET_CHALLENGE_COPY_REST_HP = "C3";
	private static final String KEY_PET_CHALLENGE_COPY_REST_PET_HP = "C4";
	private static final String KEY_PET_CHALLENGE_COPY_REWARD = "C5";
	private static final String KEY_PET_CHALLENGE_COPY_REWARD_ATTR = "1";
	private static final String KEY_PET_CHALLENGE_COPY_REWARD_MONEY = "2";
	private static final String KEY_PET_CHALLENGE_COPY_REWARD_ITEM = "3";
	private static final String KEY_PET_CHALLENGE_IS_COMPLETE_LAST = "C6";
	private static final String KEY_PET_CHALLENGE_COPY_IS_PASS_COPY = "C7";
	private static final String KEY_PET_CHALLENGE_COPY_SAODANG_COUNT = "C8";
	// ---------------------------------------------
	private static final String KEY_SENIOR_PET_CHALLENGE_COPY_CHALLENGE_COUNT = "C1";
	private static final String KEY_SENIOR_PET_CHALLENGE_COPY_NOW_LEVEL_ID = "C2";
	private static final String KEY_SENIOR_PET_CHALLENGE_COPY_REST_HP = "C3";
	private static final String KEY_SENIOR_PET_CHALLENGE_COPY_REST_PET_HP = "C4";
	private static final String KEY_SENIOR_PET_CHALLENGE_COPY_REWARD = "C5";
	private static final String KEY_SENIOR_PET_CHALLENGE_COPY_REWARD_ATTR = "1";
	private static final String KEY_SENIOR_PET_CHALLENGE_COPY_REWARD_MONEY = "2";
	private static final String KEY_SENIOR_PET_CHALLENGE_COPY_REWARD_ITEM = "3";
	private static final String KEY_SENIOR_PET_CHALLENGE_COPY_IS_COMPLETE_LAST = "C6";
	private static final String KEY_SENIOR_PET_CHALLENGE_COPY_IS_PASS_COPY = "C7";
	private static final String KEY_SENIOR_PET_CHALLENGE_COPY_SAODANG_COUNT = "C8";
	private static final String KEY_SENIOR_PET_CHALLENGE_COPY_MONSTER_INFO = "C9";

	/**
	 * 关卡记录的最大关卡数据保存数量
	 */
	public final static int LEVEL_RECORD_MAX_COUNT = 50;

	public long attribute_update_timeMillis;

	public KGameLevelRecordDBTypeEnum dbTypeEnum;

	public FriendCopyData friendCopyData;

	public PetCopyData petCopyData;

	public EliteCopyData eliteCopyData;

	public TowerCopyData towerCopyData;

	public PetChallengeCopyData petChallengeCopyData;

	public SeniorPetChallengeCopyData seniorPetChallengeCopyData;

	// 关卡进行情况数据集合
	public Map<Integer, PlayerRoleGamelevelData> levelDataMap;

	protected KGameLevelRecord(KAGameLevelSet owner, DBLevelRecord dbdata) {
		super(owner, dbdata);
		this.dbTypeEnum = KGameLevelRecordDBTypeEnum.getEnum(_dataType);
		if (levelDataMap == null) {
			levelDataMap = new HashMap<Integer, PlayerRoleGamelevelData>();
		}
	}

	public KGameLevelRecord(KAGameLevelSet owner, int type) {
		super(owner, type);
		this.dbTypeEnum = KGameLevelRecordDBTypeEnum.getEnum(_dataType);
		if (levelDataMap == null) {
			levelDataMap = new HashMap<Integer, PlayerRoleGamelevelData>();
		}
		if (this.dbTypeEnum == KGameLevelRecordDBTypeEnum.好友副本关卡数据记录) {
			friendCopyData = new FriendCopyData();
			friendCopyData.remainChallengeCount = KFriendCopyManager.free_challenge_count;
			friendCopyData.todayBuyCount = 0;
		}
		if (this.dbTypeEnum == KGameLevelRecordDBTypeEnum.随从副本关卡数据记录) {
			petCopyData = new PetCopyData();
			petCopyData.remainChallengeCount = KPetCopyManager.free_challenge_count;
			petCopyData.todayBuyCount = 0;
		}
		if (this.dbTypeEnum == KGameLevelRecordDBTypeEnum.精英副本关卡数据记录) {
			eliteCopyData = new EliteCopyData();
			eliteCopyData.remainChallengeCount = KCopyManager.free_challenge_count;
			eliteCopyData.todayBuyCount = 0;
		}
		if (this.dbTypeEnum == KGameLevelRecordDBTypeEnum.爬塔副本关卡数据记录) {
			towerCopyData = new TowerCopyData();
			// towerCopyData.nowLevelId =
			// KTowerCopyManager.firstLevel.getLevelId();
		}
		if (this.dbTypeEnum == KGameLevelRecordDBTypeEnum.随从挑战副本关卡数据记录) {
			petChallengeCopyData = new PetChallengeCopyData();
			petChallengeCopyData.nowLevelId = KPetChallengeCopyManager.firstLevel.getLevelId();
		}
		if (this.dbTypeEnum == KGameLevelRecordDBTypeEnum.高级随从挑战副本关卡数据记录) {
			seniorPetChallengeCopyData = new SeniorPetChallengeCopyData(this);
			seniorPetChallengeCopyData.nowLevelId = KSeniorPetChallengeCopyManager.firstLevel.getLevelId();
		}
	}

	@Override
	protected void decodeCA(String attribute) {
		if (levelDataMap == null) {
			levelDataMap = new HashMap<Integer, PlayerRoleGamelevelData>();
		}
		if (attribute == null || attribute.equals("")) {
			return;
		}

		try {
			JSONObject obj = new JSONObject(attribute);
			int version = obj.optInt(KEY_VERSION);
			obj.remove(KEY_VERSION);
			switch (version) {
			case VERSION_FIRST:
				if (this._dataType == KGameLevelRecordDBTypeEnum.普通关卡数据记录.getLevelRecordDbType()) {
					this.decodeNormalTypeV1(obj, KGameLevelTypeEnum.普通关卡);
				} else if (this._dataType == KGameLevelRecordDBTypeEnum.精英副本关卡数据记录.getLevelRecordDbType()) {
					this.decodeEliteCopyTypeV1(obj, KGameLevelTypeEnum.精英副本关卡);
				} else if (this._dataType == KGameLevelRecordDBTypeEnum.技术副本关卡数据记录.getLevelRecordDbType()) {
					this.decodeNormalTypeV1(obj, KGameLevelTypeEnum.技术副本关卡);
				} else if (this._dataType == KGameLevelRecordDBTypeEnum.好友副本关卡数据记录.getLevelRecordDbType()) {
					this.decodeFriendCopyTypeV1(obj, KGameLevelTypeEnum.好友副本关卡);
				} else if (this._dataType == KGameLevelRecordDBTypeEnum.随从副本关卡数据记录.getLevelRecordDbType()) {
					this.decodePetCopyTypeV1(obj, KGameLevelTypeEnum.随从副本关卡);
				} else if (this._dataType == KGameLevelRecordDBTypeEnum.爬塔副本关卡数据记录.getLevelRecordDbType()) {
					decodeTowerCopyTypeV1(obj, KGameLevelTypeEnum.爬塔副本关卡);
				} else if (this._dataType == KGameLevelRecordDBTypeEnum.随从挑战副本关卡数据记录.getLevelRecordDbType()) {
					decodePetChallengeCopyTypeV1(obj, KGameLevelTypeEnum.随从挑战副本关卡);
				} else if (this._dataType == KGameLevelRecordDBTypeEnum.高级随从挑战副本关卡数据记录.getLevelRecordDbType()) {
					decodeSeniorPetChallengeCopyTypeV1(obj, KGameLevelTypeEnum.高级随从挑战副本关卡);
				}
				break;
			}
		} catch (Exception e) {
			// TODO 解析角色剧本关卡记录扩展属性的处理
			_LOGGER.error("解析角色剧本关卡记录扩展属性的时候出现异常！！", e);
		}
	}

	@Override
	protected String encodeCA() {
		String ca = "";
		switch (this.dbTypeEnum) {
		case 普通关卡数据记录:
			ca = encodeNormalTypeAttribute();
			break;
		case 精英副本关卡数据记录:
			ca = encodeNormalTypeAttribute();
			break;
		case 技术副本关卡数据记录:
			ca = encodeNormalTypeAttribute();
			break;
		case 好友副本关卡数据记录:
			ca = encodeNormalTypeAttribute();
			break;
		case 随从副本关卡数据记录:
			ca = encodeNormalTypeAttribute();
			break;
		case 爬塔副本关卡数据记录:
			ca = encodeNormalTypeAttribute();
			break;
		case 随从挑战副本关卡数据记录:
			ca = encodeNormalTypeAttribute();
			break;
		case 高级随从挑战副本关卡数据记录:
			ca = encodeNormalTypeAttribute();
			break;
		default:
			break;
		}
		return ca;
	}

	private void decodeNormalTypeV1(JSONObject obj, KGameLevelTypeEnum levelType) throws Exception {
		try {
			this.attribute_update_timeMillis = obj.getLong(KEY_UPDATE_TIMEMILLIS);
			obj.remove(KEY_UPDATE_TIMEMILLIS);
			JSONArray array = obj.names();
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					String levelIdKey = array.getString(i);
					PlayerRoleGamelevelData data = new PlayerRoleGamelevelData(levelType);
					data.decodeAttribute(levelIdKey, obj.getString(levelIdKey));
					levelDataMap.put(data.levelId, data);
				}
			}
		} catch (Exception e) {
			throw e;
		}
	}

	private void decodeFriendCopyTypeV1(JSONObject obj, KGameLevelTypeEnum levelType) throws Exception {
		try {
			this.attribute_update_timeMillis = obj.getLong(KEY_UPDATE_TIMEMILLIS);
			obj.remove(KEY_UPDATE_TIMEMILLIS);
			if (obj.has(KEY_FRIEND_COPY_DATA)) {
				String friendDataStr = obj.optString(KEY_FRIEND_COPY_DATA, null);

				if (friendDataStr != null) {
					JSONObject friendObj = new JSONObject(friendDataStr);
					friendCopyData = new FriendCopyData();
					friendCopyData.decode(friendObj);
				} else {
					friendCopyData = new FriendCopyData();
				}
				obj.remove(KEY_FRIEND_COPY_DATA);
			}

			JSONArray array = obj.names();
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					String levelIdKey = array.getString(i);
					PlayerRoleGamelevelData data = new PlayerRoleGamelevelData(levelType);
					data.decodeAttribute(levelIdKey, obj.getString(levelIdKey));
					levelDataMap.put(data.levelId, data);
				}
			}
		} catch (Exception e) {
			throw e;
		}
	}

	private void decodePetCopyTypeV1(JSONObject obj, KGameLevelTypeEnum levelType) throws Exception {
		try {
			this.attribute_update_timeMillis = obj.getLong(KEY_UPDATE_TIMEMILLIS);
			obj.remove(KEY_UPDATE_TIMEMILLIS);
			if (obj.has(KEY_PET_COPY_DATA)) {
				String petDataStr = obj.optString(KEY_PET_COPY_DATA, null);

				if (petDataStr != null) {
					JSONObject petObj = new JSONObject(petDataStr);
					petCopyData = new PetCopyData();
					petCopyData.decode(petObj);
				} else {
					petCopyData = new PetCopyData();
				}
				obj.remove(KEY_PET_COPY_DATA);
			}

			JSONArray array = obj.names();
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					String levelIdKey = array.getString(i);
					PlayerRoleGamelevelData data = new PlayerRoleGamelevelData(levelType);
					data.decodeAttribute(levelIdKey, obj.getString(levelIdKey));
					levelDataMap.put(data.levelId, data);
				}
			}
		} catch (Exception e) {
			throw e;
		}
	}

	private void decodeTowerCopyTypeV1(JSONObject obj, KGameLevelTypeEnum levelType) throws Exception {
		try {
			this.attribute_update_timeMillis = obj.getLong(KEY_UPDATE_TIMEMILLIS);
			obj.remove(KEY_UPDATE_TIMEMILLIS);
			if (obj.has(KEY_TOWER_COPY_DATA)) {
				String petDataStr = obj.optString(KEY_TOWER_COPY_DATA, null);

				if (petDataStr != null) {
					JSONObject petObj = new JSONObject(petDataStr);
					towerCopyData = new TowerCopyData();
					towerCopyData.decode(petObj);
				} else {
					towerCopyData = new TowerCopyData();
					// towerCopyData.nowLevelId =
					// KTowerCopyManager.firstLevel.getLevelId();
				}
				obj.remove(KEY_PET_COPY_DATA);
			}
		} catch (Exception e) {
			throw e;
		}
	}

	private void decodeEliteCopyTypeV1(JSONObject obj, KGameLevelTypeEnum levelType) throws Exception {
		try {
			this.attribute_update_timeMillis = obj.getLong(KEY_UPDATE_TIMEMILLIS);
			obj.remove(KEY_UPDATE_TIMEMILLIS);
			if (obj.has(KEY_ELITE_COPY_DATA)) {
				String petDataStr = obj.optString(KEY_ELITE_COPY_DATA, null);

				if (petDataStr != null) {
					JSONObject petObj = new JSONObject(petDataStr);
					eliteCopyData = new EliteCopyData();
					eliteCopyData.decode(petObj);
				} else {
					eliteCopyData = new EliteCopyData();
					eliteCopyData.remainChallengeCount = KCopyManager.free_challenge_count;
					eliteCopyData.todayBuyCount = 0;
				}
				obj.remove(KEY_ELITE_COPY_DATA);
			}

			JSONArray array = obj.names();
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					String levelIdKey = array.getString(i);
					PlayerRoleGamelevelData data = new PlayerRoleGamelevelData(levelType);
					data.decodeAttribute(levelIdKey, obj.getString(levelIdKey));
					levelDataMap.put(data.levelId, data);
				}
			}
		} catch (Exception e) {
			throw e;
		}
	}

	private void decodeSeniorPetChallengeCopyTypeV1(JSONObject obj, KGameLevelTypeEnum levelType) throws Exception {
		try {
			this.attribute_update_timeMillis = obj.getLong(KEY_UPDATE_TIMEMILLIS);
			obj.remove(KEY_UPDATE_TIMEMILLIS);
			if (obj.has(KEY_SENIOR_PET_CHALLENGE_COPY_DATA)) {
				String petDataStr = obj.optString(KEY_SENIOR_PET_CHALLENGE_COPY_DATA, null);

				if (petDataStr != null) {
					JSONObject petObj = new JSONObject(petDataStr);
					seniorPetChallengeCopyData = new SeniorPetChallengeCopyData(this);
					seniorPetChallengeCopyData.decode(petObj);
				} else {
					seniorPetChallengeCopyData = new SeniorPetChallengeCopyData(this);
					seniorPetChallengeCopyData.nowLevelId = KSeniorPetChallengeCopyManager.firstLevel.getLevelId();
				}
				obj.remove(KEY_SENIOR_PET_CHALLENGE_COPY_DATA);
			}
		} catch (Exception e) {
			throw e;
		}
	}

	private void decodePetChallengeCopyTypeV1(JSONObject obj, KGameLevelTypeEnum levelType) throws Exception {
		try {
			this.attribute_update_timeMillis = obj.getLong(KEY_UPDATE_TIMEMILLIS);
			obj.remove(KEY_UPDATE_TIMEMILLIS);
			if (obj.has(KEY_PET_CHALLENGE_COPY_DATA)) {
				String petDataStr = obj.optString(KEY_PET_CHALLENGE_COPY_DATA, null);

				if (petDataStr != null) {
					JSONObject petObj = new JSONObject(petDataStr);
					petChallengeCopyData = new PetChallengeCopyData();
					petChallengeCopyData.decode(petObj);
				} else {
					petChallengeCopyData = new PetChallengeCopyData();
					petChallengeCopyData.nowLevelId = KPetChallengeCopyManager.firstLevel.getLevelId();
				}
				obj.remove(KEY_PET_CHALLENGE_COPY_DATA);
			}
		} catch (Exception e) {
			throw e;
		}
	}

	protected String encodeNormalTypeAttribute() {
		JSONObject obj = new JSONObject();
		try {
			obj.put(KEY_VERSION, CURRENT_VERSION);
			obj.put(KEY_UPDATE_TIMEMILLIS, attribute_update_timeMillis);
			if (this.friendCopyData != null && this.dbTypeEnum == KGameLevelRecordDBTypeEnum.好友副本关卡数据记录) {
				obj.put(KEY_FRIEND_COPY_DATA, this.friendCopyData.encode());
			}
			if (this.petCopyData != null && this.dbTypeEnum == KGameLevelRecordDBTypeEnum.随从副本关卡数据记录) {
				obj.put(KEY_PET_COPY_DATA, this.petCopyData.encode());
			}
			if (this.eliteCopyData != null && this.dbTypeEnum == KGameLevelRecordDBTypeEnum.精英副本关卡数据记录) {
				obj.put(KEY_ELITE_COPY_DATA, this.eliteCopyData.encode());
			}
			if (this.towerCopyData != null && this.dbTypeEnum == KGameLevelRecordDBTypeEnum.爬塔副本关卡数据记录) {
				obj.put(KEY_TOWER_COPY_DATA, this.towerCopyData.encode());
			}
			if (this.petChallengeCopyData != null && this.dbTypeEnum == KGameLevelRecordDBTypeEnum.随从挑战副本关卡数据记录) {
				obj.put(KEY_PET_CHALLENGE_COPY_DATA, this.petChallengeCopyData.encode());
			}
			if (this.seniorPetChallengeCopyData != null && this.dbTypeEnum == KGameLevelRecordDBTypeEnum.高级随从挑战副本关卡数据记录) {
				obj.put(KEY_SENIOR_PET_CHALLENGE_COPY_DATA, this.seniorPetChallengeCopyData.encode());
			}
			for (PlayerRoleGamelevelData data : levelDataMap.values()) {
				obj.put(data.getLevelId() + "", data.encodeAttribute());
			}
		} catch (Exception ex) {
			_LOGGER.error("encodeAttribute出现异常！此时json的字符串是：" + obj.toString(), ex);
		}
		return obj.toString();
	}

	public Map<Integer, PlayerRoleGamelevelData> getLevelDataMap() {
		return levelDataMap;
	}

	public int getRecordLevelDataSize() {
		return levelDataMap.size();
	}

	/**
	 * 角色对于每一个关卡进行情况的数据
	 * 
	 * @author zhaizl
	 * 
	 */
	public static class PlayerRoleGamelevelData {
		private KGameLevelTypeEnum levelType;
		// 关卡ID
		private int levelId;
		// 当天剩余的进入次数
		private int remainJoinLevelCount;
		// 完成关卡后对该关卡的评价
		private byte levelEvaluate;
		// 关卡是否已完成
		private boolean isCompleted;
		// 是否已获取首次完成礼包(levelType为精英副本和技术副本时有效)
		private boolean isGetFirstDropPrice;

		// 今天重置副本次数(levelType为精英副本和技术副本时有效)
		private int todayRestCount;
		// 当前完成副本最大波数(levelType为好友副本时有效)
		private int maxWave;

		public PlayerRoleGamelevelData(KGameLevelTypeEnum levelType) {
			this.levelType = levelType;
		}

		public PlayerRoleGamelevelData(int levelId, KGameLevelTypeEnum levelType, int remainJoinLevelCount, byte levelEvaluate, boolean isCompleted) {
			this.levelId = levelId;
			this.levelType = levelType;
			this.remainJoinLevelCount = remainJoinLevelCount;
			this.levelEvaluate = levelEvaluate;
			this.isCompleted = isCompleted;
			this.isGetFirstDropPrice = true;
			this.todayRestCount = 0;
		}

		public PlayerRoleGamelevelData(int levelId, KGameLevelTypeEnum levelType, int remainJoinLevelCount, byte levelEvaluate, boolean isCompleted, boolean isGetFirstDropPrice, int todayRestCount) {
			this.levelId = levelId;
			this.levelType = levelType;
			this.remainJoinLevelCount = remainJoinLevelCount;
			this.levelEvaluate = levelEvaluate;
			this.isCompleted = isCompleted;
			this.isGetFirstDropPrice = isGetFirstDropPrice;
			this.todayRestCount = todayRestCount;
		}

		public PlayerRoleGamelevelData(int levelId, KGameLevelTypeEnum levelType, int remainJoinLevelCount, int maxWave) {
			this.levelId = levelId;
			this.levelType = levelType;
			this.remainJoinLevelCount = remainJoinLevelCount;
			this.levelEvaluate = levelEvaluate;
			this.isCompleted = isCompleted;
			this.isGetFirstDropPrice = true;
			this.todayRestCount = 0;
			this.maxWave = maxWave;
		}

		/**
		 * 获取关卡ID
		 * 
		 * @return
		 */
		public int getLevelId() {
			return levelId;
		}

		/**
		 * 设置关卡ID
		 * 
		 * @param levelId
		 */
		public void setLevelId(int levelId) {
			this.levelId = levelId;
		}

		/**
		 * 获取关卡类型
		 * 
		 * @return
		 */
		public KGameLevelTypeEnum getLevelType() {
			return levelType;
		}

		/**
		 * 获取当天剩余的进入次数
		 * 
		 * @return
		 */
		public int getRemainJoinLevelCount() {
			return remainJoinLevelCount;
		}

		/**
		 * 设置当天剩余的进入次数
		 * 
		 * @param levelId
		 */
		public void setRemainJoinLevelCount(int remainJoinLevelCount) {
			this.remainJoinLevelCount = remainJoinLevelCount;
		}

		/**
		 * 获取完成关卡后对该关卡的评价
		 * 
		 * @return
		 */
		public byte getLevelEvaluate() {
			return levelEvaluate;
		}

		/**
		 * 设置完成关卡后对该关卡的评价
		 * 
		 * @param levelId
		 */
		public void setLevelEvaluate(byte levelEvaluate) {
			this.levelEvaluate = levelEvaluate;
		}

		/**
		 * 该关卡是否已完成
		 * 
		 * @return
		 */
		public boolean isCompleted() {
			return isCompleted;
		}

		/**
		 * 设置该关卡是否已完成
		 * 
		 * @param isCompleted
		 */
		public void setCompleted(boolean isCompleted) {
			this.isCompleted = isCompleted;
		}

		/**
		 * 是否已获取首次完成礼包
		 * 
		 * @return
		 */
		public boolean isGetFirstDropPrice() {
			return isGetFirstDropPrice;
		}

		public void setGetFirstDropPrice(boolean isGetFirstDropPrice) {
			this.isGetFirstDropPrice = isGetFirstDropPrice;
		}

		/**
		 * 今天重置副本次数
		 * 
		 * @return
		 */
		public int getTodayRestCount() {
			return todayRestCount;
		}

		public void setTodayRestCount(int todayRestCount) {
			this.todayRestCount = todayRestCount;
		}

		public int getMaxWave() {
			return maxWave;
		}

		public void setMaxWave(int maxWave) {
			this.maxWave = maxWave;
		}

		/**
		 * encode 所有数据变成DB字符串
		 * 
		 * @return
		 */
		public String encodeAttribute() {
			String encodeString = "";
			encodeString += this.remainJoinLevelCount + ":";
			encodeString += this.levelEvaluate + ":";
			encodeString += (this.isCompleted ? 1 : 0);
			if (levelType == KGameLevelTypeEnum.精英副本关卡 || levelType == KGameLevelTypeEnum.技术副本关卡) {
				encodeString += (":" + (this.isGetFirstDropPrice ? 1 : 0) + ":");
				encodeString += this.todayRestCount;
			} else if (levelType == KGameLevelTypeEnum.好友副本关卡) {
				encodeString += (":" + this.maxWave);
			}
			return encodeString;
		}

		/**
		 * 解释DB字符串，初始化所有属性
		 * 
		 * @param levelIdString
		 * @param jsonString
		 */
		public void decodeAttribute(String levelIdString, String jsonString) {
			if (jsonString != null) {
				String[] attributes = jsonString.split(":");
				this.levelId = Integer.parseInt(levelIdString);
				this.remainJoinLevelCount = Integer.parseInt(attributes[0]);
				this.levelEvaluate = Byte.parseByte(attributes[1]);
				this.isCompleted = (attributes[2].equals("1")) ? true : false;
				if (levelType == KGameLevelTypeEnum.精英副本关卡 || levelType == KGameLevelTypeEnum.技术副本关卡) {
					this.isGetFirstDropPrice = (attributes[3].equals("1")) ? true : false;
					if (attributes.length >= 5) {
						this.todayRestCount = Integer.parseInt(attributes[4]);
					} else {
						this.todayRestCount = 0;
					}
					this.maxWave = 0;
				} else if (levelType == KGameLevelTypeEnum.好友副本关卡) {
					if (attributes.length >= 4) {
						this.maxWave = Integer.parseInt(attributes[3]);
					} else {
						this.maxWave = 0;
					}
					this.isGetFirstDropPrice = true;
					this.todayRestCount = 0;
				} else {
					this.maxWave = 0;
					this.isGetFirstDropPrice = true;
					this.todayRestCount = 0;
				}
			}
		}
	}

	/**
	 * 好友副本数据记录
	 * 
	 * @author Administrator
	 * 
	 */
	public static class FriendCopyData {
		// 好友副本的好友冷却时间
		public Map<Long, Long> friendCoolingTimeMap = new HashMap<Long, Long>();
		// 今日购买挑战次数
		public int todayBuyCount;
		// 当前剩余挑战次数
		public int remainChallengeCount;
		// 当前挑战次数
		public int challengeCount;
		// 陌生人冷却时间
		public Map<Long, Long> strangerCoolingTimeMap = new HashMap<Long, Long>();
		// 陌生人列表
		public Map<Long, StrangerData> strangers = new HashMap<Long, StrangerData>();
		// 陌生人上次刷新时间
		public long strangerReflashTime;
		// 被好友邀请次数
		public int beInviteCount;

		public void decode(JSONObject obj) throws Exception {
			if (obj.has(KEY_FRIEND_COPY_BUY_COUNT)) {
				this.todayBuyCount = obj.optInt(KEY_FRIEND_COPY_BUY_COUNT, 0);
				obj.remove(KEY_FRIEND_COPY_BUY_COUNT);
			}
			if (obj.has(KEY_FRIEND_COPY_REMAIN_COUNT)) {
				this.remainChallengeCount = obj.optInt(KEY_FRIEND_COPY_REMAIN_COUNT, 0);
				obj.remove(KEY_FRIEND_COPY_REMAIN_COUNT);
			}
			if (obj.has(KEY_FRIEND_COPY_CHALLENGE_COUNT)) {
				this.challengeCount = obj.optInt(KEY_FRIEND_COPY_CHALLENGE_COUNT, 0);
				obj.remove(KEY_FRIEND_COPY_CHALLENGE_COUNT);
			}
			if (obj.has(KEY_FRIEND_COPY_BE_INVITE_COUNT)) {
				this.beInviteCount = obj.optInt(KEY_FRIEND_COPY_BE_INVITE_COUNT, 0);
				obj.remove(KEY_FRIEND_COPY_BE_INVITE_COUNT);
			}
			JSONArray array = obj.names();
			long nowTime = System.currentTimeMillis();
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					String roleIdKey = array.getString(i);
					long roleId = Long.parseLong(roleIdKey);
					long endTime = obj.getLong(roleIdKey);
					if (endTime > nowTime) {
						friendCoolingTimeMap.put(roleId, endTime);
					}
				}
			}
		}

		public JSONObject encode() throws Exception {
			JSONObject obj = new JSONObject();
			obj.put(KEY_FRIEND_COPY_BUY_COUNT, this.todayBuyCount);
			obj.put(KEY_FRIEND_COPY_REMAIN_COUNT, this.remainChallengeCount);
			obj.put(KEY_FRIEND_COPY_CHALLENGE_COUNT, this.challengeCount);
			obj.put(KEY_FRIEND_COPY_BE_INVITE_COUNT, this.beInviteCount);
			long nowTime = System.currentTimeMillis();
			for (Long roleId : friendCoolingTimeMap.keySet()) {
				long endTime = friendCoolingTimeMap.get(roleId);
				if (endTime < nowTime) {
					obj.put(roleId + "", endTime);
				}
			}
			return obj;
		}
	}

	/**
	 * 好友副本陌生人数据
	 * 
	 * @author zhaizl
	 * 
	 */
	public static class StrangerData {
		public long roleId;
		public String roleName;
		public byte job;
		public int lv;
		public int fightPower;

		public StrangerData(long roleId, String roleName, byte job, int lv, int fightPower) {
			this.roleId = roleId;
			this.roleName = roleName;
			this.job = job;
			this.lv = lv;
			this.fightPower = fightPower;
		}

	}

	/**
	 * 随从副本数据记录
	 * 
	 * @author Administrator
	 * 
	 */
	public static class PetCopyData {
		// 今日购买挑战次数
		public int todayBuyCount;
		// 当前剩余挑战次数
		public int remainChallengeCount;
		// 当前挑战次数
		public int challengeCount;

		public void decode(JSONObject obj) throws Exception {
			if (obj.has(KEY_PET_COPY_BUY_COUNT)) {
				this.todayBuyCount = obj.optInt(KEY_PET_COPY_BUY_COUNT, 0);
				obj.remove(KEY_PET_COPY_BUY_COUNT);
			}
			if (obj.has(KEY_PET_COPY_REMAIN_COUNT)) {
				this.remainChallengeCount = obj.optInt(KEY_PET_COPY_REMAIN_COUNT, 0);
				obj.remove(KEY_PET_COPY_REMAIN_COUNT);
			}
			if (obj.has(KEY_PET_COPY_CHALLENGE_COUNT)) {
				this.challengeCount = obj.optInt(KEY_PET_COPY_CHALLENGE_COUNT, 0);
				obj.remove(KEY_PET_COPY_CHALLENGE_COUNT);
			}
		}

		public JSONObject encode() throws Exception {
			JSONObject obj = new JSONObject();
			obj.put(KEY_PET_COPY_BUY_COUNT, this.todayBuyCount);
			obj.put(KEY_PET_COPY_REMAIN_COUNT, this.remainChallengeCount);
			obj.put(KEY_PET_COPY_CHALLENGE_COUNT, this.challengeCount);
			return obj;
		}
	}

	/**
	 * 精英副本关卡记录数据
	 * 
	 * @author Administrator
	 * 
	 */
	public static class EliteCopyData {
		// 今日购买挑战次数
		public int todayBuyCount;
		// 当前剩余挑战次数
		public int remainChallengeCount;
		// 当前挑战次数
		public int challengeCount;

		public void decode(JSONObject obj) throws Exception {
			if (obj.has(KEY_ELITE_COPY_BUY_COUNT)) {
				this.todayBuyCount = obj.optInt(KEY_ELITE_COPY_BUY_COUNT, 0);
				obj.remove(KEY_ELITE_COPY_BUY_COUNT);
			}
			if (obj.has(KEY_ELITE_COPY_REMAIN_COUNT)) {
				this.remainChallengeCount = obj.optInt(KEY_ELITE_COPY_REMAIN_COUNT, 0);
				obj.remove(KEY_ELITE_COPY_REMAIN_COUNT);
			}
			if (obj.has(KEY_ELITE_COPY_CHALLENGE_COUNT)) {
				this.challengeCount = obj.optInt(KEY_ELITE_COPY_CHALLENGE_COUNT, 0);
				obj.remove(KEY_ELITE_COPY_CHALLENGE_COUNT);
			}
		}

		public JSONObject encode() throws Exception {
			JSONObject obj = new JSONObject();
			obj.put(KEY_ELITE_COPY_BUY_COUNT, this.todayBuyCount);
			obj.put(KEY_ELITE_COPY_REMAIN_COUNT, this.remainChallengeCount);
			obj.put(KEY_ELITE_COPY_CHALLENGE_COUNT, this.challengeCount);
			return obj;
		}
	}

	/**
	 * 精英副本关卡记录数据
	 * 
	 * @author Administrator
	 * 
	 */
	public static class TowerCopyData {
		// 是否领取今日奖励
		public boolean todayGetReward;
		// 当前挑战次数
		public int challengeCount;
		// 当前关卡ID
		public int nowLevelId;

		public void decode(JSONObject obj) throws Exception {
			if (obj.has(KEY_TOWER_COPY_IS_GET_TODAY_REWARD)) {
				this.todayGetReward = (obj.optInt(KEY_TOWER_COPY_IS_GET_TODAY_REWARD, 0) == 1);
				obj.remove(KEY_TOWER_COPY_IS_GET_TODAY_REWARD);
			}
			if (obj.has(KEY_TOWER_COPY_CHALLENGE_COUNT)) {
				this.challengeCount = obj.optInt(KEY_TOWER_COPY_CHALLENGE_COUNT, 0);
				obj.remove(KEY_TOWER_COPY_CHALLENGE_COUNT);
			}
			if (obj.has(KEY_TOWER_COPY_NOW_LEVEL_ID)) {
				this.nowLevelId = obj.optInt(KEY_TOWER_COPY_NOW_LEVEL_ID, 0);
				obj.remove(KEY_TOWER_COPY_NOW_LEVEL_ID);
			}
		}

		public JSONObject encode() throws Exception {
			JSONObject obj = new JSONObject();
			obj.put(KEY_TOWER_COPY_IS_GET_TODAY_REWARD, this.todayGetReward ? 1 : 0);
			obj.put(KEY_TOWER_COPY_CHALLENGE_COUNT, this.challengeCount);
			obj.put(KEY_TOWER_COPY_NOW_LEVEL_ID, this.nowLevelId);
			return obj;
		}
	}

	/**
	 * 随从试炼副本关卡记录数据
	 * 
	 * @author Administrator
	 * 
	 */
	public static class PetChallengeCopyData {
		// 当前挑战次数
		public int challengeCount;
		// 当前关卡ID
		public int nowLevelId;
		// 剩余血量
		public long restHp = -1;
		// 剩余宠物血量
		public long restPetHp = -1;
		// 是否已经完成最后一关
		public boolean isCompleteLastLevel = false;
		// 是否已经手动通关所有副本
		public boolean isPassCopy = false;
		// 当天扫荡次数
		public int saodangCount = 0;
		// 奖励记录
		public BaseRewardData completeLevelReward = new BaseRewardData(null, null, null, null, null);

		public void decode(JSONObject obj) throws Exception {

			if (obj.has(KEY_PET_CHALLENGE_COPY_CHALLENGE_COUNT)) {
				this.challengeCount = obj.optInt(KEY_PET_CHALLENGE_COPY_CHALLENGE_COUNT, 0);
				obj.remove(KEY_PET_CHALLENGE_COPY_CHALLENGE_COUNT);
			}
			if (obj.has(KEY_PET_CHALLENGE_COPY_NOW_LEVEL_ID)) {
				this.nowLevelId = obj.optInt(KEY_PET_CHALLENGE_COPY_NOW_LEVEL_ID, 0);
				obj.remove(KEY_PET_CHALLENGE_COPY_NOW_LEVEL_ID);
			}
			if (obj.has(KEY_PET_CHALLENGE_COPY_REST_HP)) {
				this.restHp = obj.optLong(KEY_PET_CHALLENGE_COPY_REST_HP, -1l);
				obj.remove(KEY_PET_CHALLENGE_COPY_REST_HP);
			}
			if (obj.has(KEY_PET_CHALLENGE_COPY_REST_PET_HP)) {
				this.restPetHp = obj.optLong(KEY_PET_CHALLENGE_COPY_REST_PET_HP, -1l);
				obj.remove(KEY_PET_CHALLENGE_COPY_REST_PET_HP);
			}
			if (obj.has(KEY_PET_CHALLENGE_IS_COMPLETE_LAST)) {
				this.isCompleteLastLevel = (obj.optInt(KEY_PET_CHALLENGE_IS_COMPLETE_LAST, 0) == 1);
				obj.remove(KEY_PET_CHALLENGE_IS_COMPLETE_LAST);
			}
			if (obj.has(KEY_PET_CHALLENGE_COPY_REWARD)) {
				decodeReward(obj.optString(KEY_PET_CHALLENGE_COPY_REWARD, null));
				obj.remove(KEY_PET_CHALLENGE_COPY_REWARD);
			}
			if (obj.has(KEY_PET_CHALLENGE_COPY_IS_PASS_COPY)) {
				this.isPassCopy = (obj.optInt(KEY_PET_CHALLENGE_COPY_IS_PASS_COPY, 0) == 1);
				obj.remove(KEY_PET_CHALLENGE_IS_COMPLETE_LAST);
			}
			if (obj.has(KEY_PET_CHALLENGE_COPY_SAODANG_COUNT)) {
				this.saodangCount = obj.optInt(KEY_PET_CHALLENGE_COPY_SAODANG_COUNT, 0);
				obj.remove(KEY_PET_CHALLENGE_COPY_SAODANG_COUNT);
			}
		}

		public void decodeReward(String rewardStr) throws Exception {
			if (rewardStr == null) {
				this.completeLevelReward = new BaseRewardData(null, null, null, null, null);
				return;
			}
			JSONObject obj = new JSONObject(rewardStr);

			List<AttValueStruct> attrList = new ArrayList<AttValueStruct>();
			List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();
			List<ItemCountStruct> itemList = new ArrayList<ItemCountStruct>();
			if (obj.has(KEY_PET_CHALLENGE_COPY_REWARD_ATTR)) {
				String attStr = obj.optString(KEY_PET_CHALLENGE_COPY_REWARD_ATTR, null);
				if (attStr != null) {
					JSONObject attrObj = new JSONObject(attStr);
					JSONArray array = attrObj.names();
					if (array != null) {
						for (int i = 0; i < array.length(); i++) {
							String attrTypeKey = array.getString(i);
							KGameAttrType attrType = KGameAttrType.getAttrTypeEnum(Integer.parseInt(attrTypeKey));
							if (attrType != null) {
								int addValue = attrObj.getInt(attrTypeKey);
								if (addValue > 0) {
									attrList.add(new AttValueStruct(attrType, addValue));
								}
							}
						}
					}
				}
			}
			if (obj.has(KEY_PET_CHALLENGE_COPY_REWARD_MONEY)) {
				String moneyStr = obj.optString(KEY_PET_CHALLENGE_COPY_REWARD_MONEY, null);
				if (moneyStr != null) {
					JSONObject attrObj = new JSONObject(moneyStr);
					JSONArray array = attrObj.names();
					if (array != null) {
						for (int i = 0; i < array.length(); i++) {
							String curTypeKey = array.getString(i);
							KCurrencyTypeEnum curType = KCurrencyTypeEnum.getEnum(Byte.parseByte(curTypeKey));
							if (curType != null) {
								int addValue = attrObj.getInt(curTypeKey);
								if (addValue > 0) {
									moneyList.add(new KCurrencyCountStruct(curType, addValue));
								}
							}
						}
					}
				}
			}
			if (obj.has(KEY_PET_CHALLENGE_COPY_REWARD_ITEM)) {
				String itemStr = obj.optString(KEY_PET_CHALLENGE_COPY_REWARD_ITEM, null);
				if (itemStr != null) {
					JSONObject itemObj = new JSONObject(itemStr);
					JSONArray array = itemObj.names();
					if (array != null) {
						for (int i = 0; i < array.length(); i++) {
							String itemCodeKey = array.getString(i);
							if (KSupportFactory.getItemModuleSupport().getItemTemplate(itemCodeKey) != null) {
								int addValue = itemObj.getInt(itemCodeKey);
								if (addValue > 0) {
									itemList.add(new ItemCountStruct(itemCodeKey, addValue));
								}
							}
						}
					}
				}
			}
			this.completeLevelReward = new BaseRewardData(attrList, moneyList, itemList, null, null);
		}

		public JSONObject encode() throws Exception {
			JSONObject obj = new JSONObject();
			obj.put(KEY_PET_CHALLENGE_COPY_CHALLENGE_COUNT, this.challengeCount);
			obj.put(KEY_PET_CHALLENGE_COPY_NOW_LEVEL_ID, this.nowLevelId);
			obj.put(KEY_PET_CHALLENGE_COPY_REST_HP, this.restHp);
			obj.put(KEY_PET_CHALLENGE_COPY_REST_PET_HP, this.restPetHp);
			obj.put(KEY_PET_CHALLENGE_IS_COMPLETE_LAST, this.isCompleteLastLevel ? 1 : 0);
			obj.put(KEY_PET_CHALLENGE_COPY_REWARD, encodeReward().toString());
			return obj;
		}

		public JSONObject encodeReward() throws Exception {
			JSONObject obj = new JSONObject();
			if (completeLevelReward != null) {
				if (completeLevelReward.attList != null && completeLevelReward.attList.size() > 0) {
					JSONObject attrObj = new JSONObject();
					for (AttValueStruct attr : completeLevelReward.attList) {
						attrObj.put(attr.roleAttType.sign + "", attr.addValue);
					}
					obj.put(KEY_PET_CHALLENGE_COPY_REWARD_ATTR, attrObj.toString());
				}
				if (completeLevelReward.moneyList != null && completeLevelReward.moneyList.size() > 0) {
					JSONObject moneyObj = new JSONObject();
					for (KCurrencyCountStruct st : completeLevelReward.moneyList) {
						moneyObj.put(st.currencyType.sign + "", st.currencyCount);
					}
					obj.put(KEY_PET_CHALLENGE_COPY_REWARD_MONEY, moneyObj.toString());
				}
				if (completeLevelReward.itemStructs != null && completeLevelReward.itemStructs.size() > 0) {
					JSONObject itemObj = new JSONObject();
					for (ItemCountStruct st : completeLevelReward.itemStructs) {
						itemObj.put(st.itemCode, st.itemCount);
					}
					obj.put(KEY_PET_CHALLENGE_COPY_REWARD_ITEM, itemObj.toString());
				}
			}
			return obj;
		}
	}

	/**
	 * 随从试炼副本关卡记录数据
	 * 
	 * @author Administrator
	 * 
	 */
	public static class SeniorPetChallengeCopyData {
		// 当前挑战次数
		public int challengeCount;
		// 当前关卡ID
		public int nowLevelId;
		// 剩余血量
		public long restHp = -1;
		// 剩余宠物血量
		public long restPetHp = -1;
		// 是否已经完成最后一关
		public boolean isCompleteLastLevel = false;
		// 是否已经手动通关所有副本
		public boolean isPassCopy = false;
		// 当天扫荡次数
		public int saodangCount = 0;
		// 关卡出现怪物记录
		public Map<Integer, Map<Integer, Integer>> monsterMap = new HashMap<Integer, Map<Integer, Integer>>();
		// 奖励记录
		public BaseRewardData completeLevelReward = new BaseRewardData(null, null, null, null, null);

		public final KGameLevelRecord record;

		public SeniorPetChallengeCopyData(KGameLevelRecord record) {
			this.record = record;
		}

		public void decode(JSONObject obj) throws Exception {
			if (obj.has(KEY_SENIOR_PET_CHALLENGE_COPY_CHALLENGE_COUNT)) {
				this.challengeCount = obj.optInt(KEY_SENIOR_PET_CHALLENGE_COPY_CHALLENGE_COUNT, 0);
				obj.remove(KEY_SENIOR_PET_CHALLENGE_COPY_CHALLENGE_COUNT);
			}
			if (obj.has(KEY_SENIOR_PET_CHALLENGE_COPY_NOW_LEVEL_ID)) {
				this.nowLevelId = obj.optInt(KEY_SENIOR_PET_CHALLENGE_COPY_NOW_LEVEL_ID, 0);
				obj.remove(KEY_SENIOR_PET_CHALLENGE_COPY_NOW_LEVEL_ID);
			}
			if (obj.has(KEY_SENIOR_PET_CHALLENGE_COPY_REST_HP)) {
				this.restHp = obj.optLong(KEY_SENIOR_PET_CHALLENGE_COPY_REST_HP, -1l);
				obj.remove(KEY_SENIOR_PET_CHALLENGE_COPY_REST_HP);
			}
			if (obj.has(KEY_SENIOR_PET_CHALLENGE_COPY_REST_PET_HP)) {
				this.restPetHp = obj.optLong(KEY_SENIOR_PET_CHALLENGE_COPY_REST_PET_HP, -1l);
				obj.remove(KEY_SENIOR_PET_CHALLENGE_COPY_REST_PET_HP);
			}
			if (obj.has(KEY_SENIOR_PET_CHALLENGE_COPY_IS_COMPLETE_LAST)) {
				this.isCompleteLastLevel = (obj.optInt(KEY_SENIOR_PET_CHALLENGE_COPY_IS_COMPLETE_LAST, 0) == 1);
				obj.remove(KEY_SENIOR_PET_CHALLENGE_COPY_IS_COMPLETE_LAST);
			}
			if (obj.has(KEY_SENIOR_PET_CHALLENGE_COPY_REWARD)) {
				decodeReward(obj.optString(KEY_SENIOR_PET_CHALLENGE_COPY_REWARD, null));
				obj.remove(KEY_SENIOR_PET_CHALLENGE_COPY_REWARD);
			}
			if (obj.has(KEY_SENIOR_PET_CHALLENGE_COPY_IS_PASS_COPY)) {
				this.isPassCopy = (obj.optInt(KEY_SENIOR_PET_CHALLENGE_COPY_IS_PASS_COPY, 0) == 1);
				obj.remove(KEY_SENIOR_PET_CHALLENGE_COPY_IS_PASS_COPY);
			}
			if (obj.has(KEY_SENIOR_PET_CHALLENGE_COPY_SAODANG_COUNT)) {
				this.saodangCount = obj.optInt(KEY_SENIOR_PET_CHALLENGE_COPY_SAODANG_COUNT, 0);
				obj.remove(KEY_SENIOR_PET_CHALLENGE_COPY_SAODANG_COUNT);
			}
			if (obj.has(KEY_SENIOR_PET_CHALLENGE_COPY_MONSTER_INFO)) {
				decodeMonster(obj.optString(KEY_SENIOR_PET_CHALLENGE_COPY_MONSTER_INFO, null));
				obj.remove(KEY_SENIOR_PET_CHALLENGE_COPY_MONSTER_INFO);
			}
		}

		public void decodeMonster(String monStr) throws Exception {
			if (monStr == null) {
				return;
			}
			JSONObject obj = new JSONObject(monStr);
			JSONArray levelArray = obj.names();
			if (levelArray != null) {
				for (int i = 0; i < levelArray.length(); i++) {
					String levelIdKey = levelArray.getString(i);
					int levelId = Integer.parseInt(levelIdKey);
					String monInfo = obj.optString(levelIdKey, null);
					if (monInfo != null) {
						JSONObject monObj = new JSONObject(monInfo);
						JSONArray monArray = monObj.names();
						if (monObj != null && monArray != null) {
							monsterMap.put(levelId, new HashMap<Integer, Integer>());
							for (int j = 0; j < monArray.length(); j++) {
								String monIdKey = monArray.getString(j);
								int monId = Integer.parseInt(monIdKey);
								int count = monObj.optInt(monIdKey, 1);
								monsterMap.get(levelId).put(monId, count);
							}
						}
					}
				}
			}
		}

		public void decodeReward(String rewardStr) throws Exception {
			if (rewardStr == null) {
				this.completeLevelReward = new BaseRewardData(null, null, null, null, null);
				return;
			}
			JSONObject obj = new JSONObject(rewardStr);

			List<AttValueStruct> attrList = new ArrayList<AttValueStruct>();
			List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();
			List<ItemCountStruct> itemList = new ArrayList<ItemCountStruct>();
			if (obj.has(KEY_SENIOR_PET_CHALLENGE_COPY_REWARD_ATTR)) {
				String attStr = obj.optString(KEY_SENIOR_PET_CHALLENGE_COPY_REWARD_ATTR, null);
				if (attStr != null) {
					JSONObject attrObj = new JSONObject(attStr);
					JSONArray array = attrObj.names();
					if (array != null) {
						for (int i = 0; i < array.length(); i++) {
							String attrTypeKey = array.getString(i);
							KGameAttrType attrType = KGameAttrType.getAttrTypeEnum(Integer.parseInt(attrTypeKey));
							if (attrType != null) {
								int addValue = attrObj.getInt(attrTypeKey);
								if (addValue > 0) {
									attrList.add(new AttValueStruct(attrType, addValue));
								}
							}
						}
					}
				}
			}
			if (obj.has(KEY_SENIOR_PET_CHALLENGE_COPY_REWARD_MONEY)) {
				String moneyStr = obj.optString(KEY_SENIOR_PET_CHALLENGE_COPY_REWARD_MONEY, null);
				if (moneyStr != null) {
					JSONObject attrObj = new JSONObject(moneyStr);
					JSONArray array = attrObj.names();
					if (array != null) {
						for (int i = 0; i < array.length(); i++) {
							String curTypeKey = array.getString(i);
							KCurrencyTypeEnum curType = KCurrencyTypeEnum.getEnum(Byte.parseByte(curTypeKey));
							if (curType != null) {
								int addValue = attrObj.getInt(curTypeKey);
								if (addValue > 0) {
									moneyList.add(new KCurrencyCountStruct(curType, addValue));
								}
							}
						}
					}
				}
			}
			if (obj.has(KEY_SENIOR_PET_CHALLENGE_COPY_REWARD_ITEM)) {
				String itemStr = obj.optString(KEY_SENIOR_PET_CHALLENGE_COPY_REWARD_ITEM, null);
				if (itemStr != null) {
					JSONObject itemObj = new JSONObject(itemStr);
					JSONArray array = itemObj.names();
					if (array != null) {
						for (int i = 0; i < array.length(); i++) {
							String itemCodeKey = array.getString(i);
							if (KSupportFactory.getItemModuleSupport().getItemTemplate(itemCodeKey) != null) {
								int addValue = itemObj.getInt(itemCodeKey);
								if (addValue > 0) {
									itemList.add(new ItemCountStruct(itemCodeKey, addValue));
								}
							}
						}
					}
				}
			}
			this.completeLevelReward = new BaseRewardData(attrList, moneyList, itemList, null, null);
		}

		public JSONObject encode() throws Exception {
			JSONObject obj = new JSONObject();
			obj.put(KEY_SENIOR_PET_CHALLENGE_COPY_CHALLENGE_COUNT, this.challengeCount);
			obj.put(KEY_SENIOR_PET_CHALLENGE_COPY_NOW_LEVEL_ID, this.nowLevelId);
			obj.put(KEY_SENIOR_PET_CHALLENGE_COPY_REST_HP, this.restHp);
			obj.put(KEY_SENIOR_PET_CHALLENGE_COPY_REST_PET_HP, this.restPetHp);
			obj.put(KEY_SENIOR_PET_CHALLENGE_COPY_IS_COMPLETE_LAST, this.isCompleteLastLevel ? 1 : 0);
			obj.put(KEY_SENIOR_PET_CHALLENGE_COPY_REWARD, encodeReward().toString());
			obj.put(KEY_SENIOR_PET_CHALLENGE_COPY_IS_PASS_COPY, this.isPassCopy ? 1 : 0);
			obj.put(KEY_SENIOR_PET_CHALLENGE_COPY_SAODANG_COUNT, this.saodangCount);
			obj.put(KEY_SENIOR_PET_CHALLENGE_COPY_MONSTER_INFO, encodeMonster().toString());
			return obj;
		}

		public JSONObject encodeMonster() throws Exception {
			JSONObject obj = new JSONObject();
			for (Integer levelId : monsterMap.keySet()) {
				Map<Integer, Integer> tempMap = monsterMap.get(levelId);
				JSONObject monObj = new JSONObject();
				for (Integer monId : tempMap.keySet()) {
					monObj.put(monId + "", tempMap.get(monId));
				}
				obj.put(levelId + "", monObj);
			}
			return obj;
		}

		public JSONObject encodeReward() throws Exception {
			JSONObject obj = new JSONObject();
			if (completeLevelReward != null) {
				if (completeLevelReward.attList != null && completeLevelReward.attList.size() > 0) {
					JSONObject attrObj = new JSONObject();
					for (AttValueStruct attr : completeLevelReward.attList) {
						attrObj.put(attr.roleAttType.sign + "", attr.addValue);
					}
					obj.put(KEY_SENIOR_PET_CHALLENGE_COPY_REWARD_ATTR, attrObj.toString());
				}
				if (completeLevelReward.moneyList != null && completeLevelReward.moneyList.size() > 0) {
					JSONObject moneyObj = new JSONObject();
					for (KCurrencyCountStruct st : completeLevelReward.moneyList) {
						moneyObj.put(st.currencyType.sign + "", st.currencyCount);
					}
					obj.put(KEY_SENIOR_PET_CHALLENGE_COPY_REWARD_MONEY, moneyObj.toString());
				}
				if (completeLevelReward.itemStructs != null && completeLevelReward.itemStructs.size() > 0) {
					JSONObject itemObj = new JSONObject();
					for (ItemCountStruct st : completeLevelReward.itemStructs) {
						itemObj.put(st.itemCode, st.itemCount);
					}
					obj.put(KEY_SENIOR_PET_CHALLENGE_COPY_REWARD_ITEM, itemObj.toString());
				}
			}
			return obj;
		}

		public Map<Integer, Integer> getLevelMonsterInfo(int levelId) {
			return this.monsterMap.get(levelId);
		}

		public void resetAllLevelMonsterInfo(Map<Integer, Map<Integer, Integer>> monMap) {
			this.monsterMap.clear();
			this.monsterMap = monMap;
			this.record.notifyDB();
		}

		public void setLevelMonsterInfo(int levelId, Map<Integer, Integer> monMap) {
			this.monsterMap.put(levelId, monMap);
			this.record.notifyDB();
		}

		public void checkAndResetSeniorPetChallengeCopyData(boolean isNeedCheck) {
			long nowTime = System.currentTimeMillis();
			boolean isDataChanged = false;
			boolean isResetData = false;
			if (!isNeedCheck || (isNeedCheck && UtilTool.checkNowTimeIsArriveTomorrow(record.attribute_update_timeMillis))) {
				this.challengeCount = 0;
				this.saodangCount = 0;
				record.attribute_update_timeMillis = System.currentTimeMillis();
				isDataChanged = true;
				isResetData = true;
			}

			if (isDataChanged) {
				record.notifyDB();
			}
		}

		/**
		 * 记录挑战爬塔副本
		 */
		public void recordChallengeSeniorPetChallengeCopy() {
			this.challengeCount++;
			record.notifyDB();
		}

		public void recordCompleteSeniorPetChallengeCopy(int nextLevelId, long restHp, long restPetHp) {
			this.nowLevelId = nextLevelId;
			this.restHp = restHp;
			this.restPetHp = restPetHp;
			record.notifyDB();
		}

		public void recordCompleteSeniorPetChallengeCopyLastLevel(int nowLevelId) {
			this.isCompleteLastLevel = true;
			this.nowLevelId = nowLevelId;
			this.isPassCopy = true;
			record.notifyDB();
		}

		public void recordUpdateSeniorPetChallengeCopyReward(BaseRewardData data) {
			this.completeLevelReward = data;
			record.notifyDB();
		}

		public void recordResetSeniorPetChallengeCopy() {
			this.nowLevelId = KSeniorPetChallengeCopyManager.firstLevel.getLevelId();
			this.restHp = -1;
			this.restPetHp = -1;
			this.isCompleteLastLevel = false;
			this.completeLevelReward = new BaseRewardData(null, null, null, null, null);
			record.notifyDB();
		}

		public void recordSaodangPetChallengeCopy() {
			this.nowLevelId = KSeniorPetChallengeCopyManager.firstLevel.getLevelId();
			this.restHp = -1;
			this.restPetHp = -1;
			this.isCompleteLastLevel = false;
			this.saodangCount++;
			this.completeLevelReward = new BaseRewardData(null, null, null, null, null);
			record.notifyDB();

		}
	}

}
