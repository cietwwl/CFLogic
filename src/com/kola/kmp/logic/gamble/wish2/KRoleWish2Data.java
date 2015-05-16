package com.kola.kmp.logic.gamble.wish2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.thirdpart.json.JSONArray;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.competition.KCompetitionServerMsgSender;
import com.kola.kmp.logic.competition.KCompetitor.KCompetitionBattleHistory;
import com.kola.kmp.logic.gamble.KGambleRoleExtData;
import com.kola.kmp.logic.gamble.wish2.KWish2ItemPool.KWish2DropItem;
import com.kola.kmp.logic.gamble.wish2.KWish2ItemPool.PoolInfoData;
import com.kola.kmp.logic.level.KGameLevelRecord.PlayerRoleGamelevelData;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KRoleWish2Data {
	private static final Logger _LOGGER = KGameLogger.getLogger(KRoleWish2Data.class);
	private final static String JSON_KEY_WISH2DATA_SYS_CHECK_TIME = "L0";
	private final static String JSON_KEY_WISH2DATA_IS_GUIDE_WISH = "L1";
	private final static String JSON_KEY_WISH2DATA_POOL_DATA = "L2";

	// private final static long oneDayTimeMillis = 24 * 60 * 60 *1000;

	// 系统跨天检测时间
	public long systemCheckTime;
	// 是否首次引导的许愿
	public boolean isGuideWish;

	public Deque<String> _history = new LinkedBlockingDeque<String>(10);

	public Map<Byte, RolePoolData> _rolePoolDataMap = new LinkedHashMap<Byte, RolePoolData>();

	KGambleRoleExtData extData;

	public KRoleWish2Data(KGambleRoleExtData extData, boolean isFirstInit) {
		this.extData = extData;
		if (isFirstInit) {
			this.systemCheckTime = System.currentTimeMillis();
			this.isGuideWish = true;
			extData.notifyDB();
		}
	}

	public String encode() throws Exception {
		try {
			JSONObject obj = new JSONObject();
			obj.put(JSON_KEY_WISH2DATA_SYS_CHECK_TIME, systemCheckTime);
			obj.put(JSON_KEY_WISH2DATA_IS_GUIDE_WISH, isGuideWish ? 1 : 0);
			JSONObject poolDataObj = new JSONObject();
			for (Byte poolType : _rolePoolDataMap.keySet()) {
				poolDataObj.put(poolType + "", _rolePoolDataMap.get(poolType).encode());
			}
			obj.put(JSON_KEY_WISH2DATA_POOL_DATA, poolDataObj);
			return obj.toString();
		} catch (Exception e) {
			_LOGGER.error("保存许愿2系统的json数据出错！角色ID=" + extData.getRoleId(), e);
			throw e;
		}
	}

	public void decode(String data) throws Exception {
		try {
			JSONObject obj = new JSONObject(data);
			this.systemCheckTime = obj.optLong(JSON_KEY_WISH2DATA_SYS_CHECK_TIME, 0);
			this.isGuideWish = (obj.optInt(JSON_KEY_WISH2DATA_IS_GUIDE_WISH, 0) == 1);
			String poolDataMapStr = obj.optString(JSON_KEY_WISH2DATA_POOL_DATA);
			if (poolDataMapStr != null) {
				JSONObject poolDataObj = new JSONObject(poolDataMapStr);
				JSONArray array = poolDataObj.names();
				if (array != null) {
					for (int i = 0; i < array.length(); i++) {
						String poolTypeKey = array.getString(i);
						byte poolType = Byte.parseByte(poolTypeKey);
						if (poolType >= 1 && poolType <= 4) {
							// String poolDataStr =
							// poolDataObj.getString(poolTypeKey);
							JSONObject poolDataJSON = poolDataObj.getJSONObject(poolTypeKey);
							RolePoolData poolData = new RolePoolData();
							poolData.poolType = poolType;
							// poolData.decode(poolDataStr);
							poolData.decode(poolDataJSON);
							_rolePoolDataMap.put(poolType, poolData);
						}
					}
				}
			}
		} catch (Exception e) {
			_LOGGER.error("解析许愿2系统的json数据出错！角色ID=" + extData.getRoleId(), e);
			throw e;
		}
		// checkPoolDatas();
	}

	public void checkPoolDatas() {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(extData.getRoleId());
		if (role != null) {
			boolean isNeedReflash = false;
			for (byte poolType : KWish2ItemPool.poolType) {
				if (_rolePoolDataMap.containsKey(poolType)) {
					RolePoolData poolData = _rolePoolDataMap.get(poolType);
					if (poolData._roleGirdDataList.isEmpty()) {
						reflashPoolNative(role, poolType, false);
						isNeedReflash = true;
					} else {
						for (RoleGirdData girdData : poolData._roleGirdDataList) {
							if (!KWish2ItemPool._alldropItemMap.containsKey(girdData.dropId)) {
								isNeedReflash = true;
								break;
							}
						}
						if (isNeedReflash) {
							reflashPoolNative(role, poolType, false);
						}
					}

				} else {
					reflashPoolNative(role, poolType, true);
					isNeedReflash = true;
				}
			}
			if (isNeedReflash) {
				extData.notifyDB();
			}
		}
	}

	public boolean checkAndResetWishData(KRole role) {
		if (checkArriveAutoReflashTime() || _rolePoolDataMap.isEmpty()) {
			for (int i = 0; i < KWish2ItemPool.poolType.length; i++) {
				reflashPoolNative(role, KWish2ItemPool.poolType[i], true);
			}
			this.systemCheckTime = System.currentTimeMillis();
			extData.notifyDB();
			return true;
		}

		return false;
	}

	public boolean checkArriveAutoReflashTime() {
		long nowTime = System.currentTimeMillis();
		if (nowTime >= UtilTool.getTommorowStart(systemCheckTime).getTimeInMillis() + KWish2Manager.dayReflashTimeDelay) {
			return true;
		} else {
			return false;
		}
	}

	public void reflashPool(KRole role, byte poolType) {
		reflashPoolNative(role, poolType, false);
		extData.notifyDB();
	}

	void addHistory(String tips) {
		synchronized (_history) {
			if (!_history.offerFirst(tips)) {
				_history.removeLast();
				_history.addFirst(tips);
			}
		}
	}

	private void reflashPoolNative(KRole role, byte poolType, boolean isInit) {
		List<KWish2DropItem> list = null;
		if (poolType == KWish2ItemPool.GOLD_POOL && isGuideWish) {
			list = KWish2ItemPool.caculateGuideDropableItemList();
		} else {
			list = KWish2ItemPool.caculateDropableItemList(role, poolType);
		}

		RolePoolData poolData = _rolePoolDataMap.get(poolType);
		if (poolData != null) {
			poolData.clearData(isInit);
			poolData.reflashCount++;
			poolData.restLotteryCount = 10;
		} else {
			poolData = new RolePoolData();
			poolData.poolType = poolType;
			poolData.canUse10Count = true;
			poolData.reflashCount = 0;
			poolData.restLotteryCount = 10;
			_rolePoolDataMap.put(poolType, poolData);
		}
		for (int i = 0; i < 10 && i < list.size(); i++) {
			KWish2DropItem item = list.get(i);
			poolData._roleGirdDataList.add(new RoleGirdData(i, item.dropId, item.lotteryWeight, false));
		}
		poolData.nowIndex = UtilTool.random(poolData._roleGirdDataList.size());
	}

	public void guideWish(KRole role) {
		this.isGuideWish = false;
		reflashPool(role, KWish2ItemPool.GOLD_POOL);
	}

	public boolean wish(KRole role, byte poolType, int index) {
		if (this._rolePoolDataMap.containsKey(poolType)) {
			this.isGuideWish = false;
			RolePoolData poolData = this._rolePoolDataMap.get(poolType);
			if (index > -1) {
				poolData.nowIndex = index;
				if (index < poolData._roleGirdDataList.size()) {
					poolData._roleGirdDataList.get(index).isUse = true;
					poolData.restLotteryCount--;
				}
			}
			boolean isNeedReflash = true;
			for (int i = 0; i < poolData._roleGirdDataList.size(); i++) {
				if (!poolData._roleGirdDataList.get(i).isUse) {
					isNeedReflash = false;
					break;
				}
			}
			if (isNeedReflash) {
				reflashPoolNative(role, poolType, false);
			}
			extData.notifyDB();
			return isNeedReflash;
		}
		return false;
	}

	public void wish10Count(KRole role, byte poolType) {
		this.isGuideWish = false;
		reflashPool(role, poolType);
	}

	public static class RolePoolData {
		private final static String JSON_KEY_REFLASH_COUNT = "1";
		private final static String JSON_KEY_CAN_10_COUNT = "2";
		private final static String JSON_KEY_NOW_INDEX = "3";
		private final static String JSON_KEY_GIRD_DATA_LIST = "4";
		private final static String JSON_KEY_REST_COUNT = "5";

		public byte poolType;
		public int reflashCount;
		public boolean canUse10Count;
		public int nowIndex;
		public int restLotteryCount;
		public List<RoleGirdData> _roleGirdDataList = new ArrayList<RoleGirdData>();

		// public void decode(String data) throws Exception {
		public void decode(JSONObject obj) throws Exception {
			// JSONObject obj = new JSONObject(data);
			this.reflashCount = obj.optInt(JSON_KEY_REFLASH_COUNT, 0);
			this.canUse10Count = (obj.optInt(JSON_KEY_CAN_10_COUNT, 0) == 1);
			this.nowIndex = obj.optInt(JSON_KEY_NOW_INDEX, 0);
			this.restLotteryCount = obj.optInt(JSON_KEY_REST_COUNT, 10);
			String girdDataListStr = obj.optString(JSON_KEY_GIRD_DATA_LIST, null);
			if (girdDataListStr != null) {
				JSONObject girdDataObj = new JSONObject(girdDataListStr);
				JSONArray array = girdDataObj.names();
				if (array != null) {
					for (int i = 0; i < array.length(); i++) {
						String girdIdKey = array.getString(i);
						int girdIndex = Integer.parseInt(girdIdKey);
						RoleGirdData girdData = new RoleGirdData(girdIndex);
						// girdData.decode(girdDataObj.getString(girdIdKey));
						girdData.decode(girdDataObj.getJSONObject(girdIdKey));
						if (KWish2ItemPool._alldropItemMap.containsKey(girdData.dropId)) {
							girdData.dropWeight = KWish2ItemPool._alldropItemMap.get(girdData.dropId).lotteryWeight;
						}
						_roleGirdDataList.add(girdData);
					}
					Collections.sort(_roleGirdDataList);
				}
			}
		}

		public JSONObject encode() throws Exception {
			try {
				JSONObject obj = new JSONObject();
				obj.put(JSON_KEY_REFLASH_COUNT, reflashCount);
				obj.put(JSON_KEY_CAN_10_COUNT, canUse10Count ? 1 : 0);
				obj.put(JSON_KEY_NOW_INDEX, nowIndex);
				obj.put(JSON_KEY_REST_COUNT, restLotteryCount);
				JSONObject girdDataObj = new JSONObject();
				for (int i = 0; i < _roleGirdDataList.size(); i++) {
					RoleGirdData data = _roleGirdDataList.get(i);
					girdDataObj.put(data.girdIndex + "", data.encode());
				}
				obj.put(JSON_KEY_GIRD_DATA_LIST, girdDataObj);
				return obj;
			} catch (Exception e) {
				throw e;
			}

		}

		public void clearData(boolean isInit) {
			if (isInit) {
				reflashCount = 0;
			}
			canUse10Count = true;
			nowIndex = 0;
			_roleGirdDataList.clear();
		}

		public int getRestAllLotteryUseCurrCount() {
			if (restLotteryCount > 0) {
				PoolInfoData poolInfo = KWish2ItemPool._poolInfoMap.get(poolType);
				if(restLotteryCount == 1){
					return poolInfo.wishUseCount;
				}else{
				   return (poolInfo.wishUseCount * restLotteryCount * poolInfo.wish10DiscountRate) / 100;
				}
			}
			return -1;
		}
	}

	public static class RoleGirdData implements Comparable<RoleGirdData> {
		private final static String JSON_KEY_DROP_ID = "1";
		private final static String JSON_KEY_IS_USE = "2";

		public int girdIndex;
		public int dropId;
		public int dropWeight;
		public boolean isUse;

		public RoleGirdData(int girdIndex) {
			super();
			this.girdIndex = girdIndex;
		}

		public RoleGirdData(int girdIndex, int dropId, int dropWeight, boolean isUse) {
			this.girdIndex = girdIndex;
			this.dropId = dropId;
			this.dropWeight = dropWeight;
			this.isUse = isUse;
		}

		// public void decode(String data) throws Exception {
		public void decode(JSONObject obj) throws Exception {
			// JSONObject obj = new JSONObject(data);
			this.dropId = obj.getInt(JSON_KEY_DROP_ID);
			this.isUse = (obj.optInt(JSON_KEY_IS_USE, 1) == 1);
		}

		public JSONObject encode() throws Exception {
			JSONObject obj = new JSONObject();
			obj.put(JSON_KEY_DROP_ID, dropId);
			obj.put(JSON_KEY_IS_USE, isUse ? 1 : 0);
			return obj;
		}

		@Override
		public int compareTo(RoleGirdData o) {
			if (o.girdIndex > this.girdIndex) {
				return 1;
			} else if (o.girdIndex < this.girdIndex) {
				return -1;
			} else {
				return 0;
			}
		}
	}
}
