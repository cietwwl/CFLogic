package com.kola.kmp.logic.competition;

import static com.kola.kmp.logic.competition.KCompetitionModule._LOGGER;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.koala.game.KGameMessage;
import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.DataStatus;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kgame.db.dataobject.DBRank;
import com.kola.kgame.db.dataobject.impl.DBRankImpl;
import com.kola.kmp.logic.competition.KCompetitionManager.CompetitionRewardShowData;
import com.kola.kmp.logic.level.KGameLevelRecord.PlayerRoleGamelevelData;
import com.kola.kmp.logic.map.KGameMapEntity.RoleEquipShowData;
import com.kola.kmp.logic.other.KEquipmentTypeEnum;
import com.kola.kmp.logic.other.KGameLevelRecordDBTypeEnum;
import com.kola.kmp.logic.other.KItemQualityEnum;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.other.KRankTypeEnum;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;
import com.kola.kmp.logic.util.DefaultRoleMapResInfoManager;
import com.kola.kmp.logic.util.IRoleMapResInfo;
import com.kola.kmp.logic.util.text.HyperTextTool;

public class KCompetitor {

	private final static String KEY_ROLE_NAME = "1";
	private final static String KEY_ROLE_LV = "2";
	private final static String KEY_ROLE_OCC = "3";
	private final static String KEY_ROLE_HEAD_ID = "4";
	private final static String KEY_ROLE_MAP_ID = "5";
	private final static String KEY_ROLE_FIGHT_PWD = "6";
	private final static String KEY_ROLE_TOTAL_BATTLE = "7";
	private final static String KEY_ROLE_WIN_BATTLE = "8";
	private final static String KEY_ROLE_CHALLENGE_COUNT = "9";
	private final static String KEY_ROLE_BUY_COUNT = "10";
	private final static String KEY_ROLE_TODAY_REWARD = "11";
	private final static String KEY_ROLE_LAST_WEEK_REWARD = "12";
	private final static String KEY_ROLE_VISIT_HALL = "13";
	private final static String KEY_ROLE_CHECK_TIME = "14";
	private final static String KEY_ROLE_VIP_LV = "15";
	private final static String KEY_LAST_CD_TIME = "16";
	private final static String KEY_SERIAL_WIN_COUNT = "17";

	private final static String KEY_REWARD_RANK = "1";
	private final static String KEY_REWARD_HAS_GET = "2";

	private long _recordId;
	private long _roleId;
	private String _roleName;
	private String _extRoleName;
	private int _roleLevel;
	private int _ranking;
	private byte _occupation;
	private int _headResId;
	private int _inMapResId; // 显示在排行榜的资源id
	private int _fightPower; // 战斗力
	private int _vipLv;
	private int _totalBattleCount;// 总战斗场数
	private int _winBattleCount;// 胜利战斗场数
	private long _resetTimeMillis;
	private AtomicInteger _canChallengeCount;
	private AtomicInteger _todayBuyCount;
	private Deque<KCompetitionBattleHistory> _history = new LinkedBlockingDeque<KCompetitionBattleHistory>(10);
	private boolean _hadOpen;
	private boolean _onLine;
	private int _lastPickRank;
	private List<Integer> _picks;
	private KCompetitionReward _todayResult;
	// private KCompetitionReward _lastWeekResult;

	public long last_reflash_picks_time = 0;

	public long _lastCdTime = 0;

	private Map<Integer, Boolean> hallOfFrameVisitData = new LinkedHashMap<Integer, Boolean>();

	public DataStatus _dataStatus;

	private KRankTypeEnum rankType = KRankTypeEnum.竞技榜;

	private int _serialWinCount = 0;// 连胜或连负记录

	public KCompetitor(DBRank dbRank) {
		this._recordId = dbRank.getDBId();
		this._roleId = dbRank.getElementId();
		this._ranking = dbRank.getRank();
		decodeDBAttribute(dbRank.getAttribute());
		this._picks = new ArrayList<Integer>();
		this._dataStatus = new DataStatus();
	}

	public KCompetitor(long recordId, long roleId, String roleName,
			int roleLevel, int ranking, byte occupation, int headResId,
			int inMapResId, long resetTimeMillis, int canChallengeCount,
			int fightPower, int vipLv, KCompetitionReward cResult) {
		super();
		this._recordId = recordId;
		this._roleId = roleId;
		this._roleName = roleName;
		this._extRoleName = HyperTextTool.extRoleName(_roleName);
		this._roleLevel = roleLevel;
		this._ranking = ranking;
		this._occupation = occupation;
		this._headResId = headResId;
		this._inMapResId = inMapResId;
		this._fightPower = fightPower;
		this._vipLv = vipLv;
		this._resetTimeMillis = resetTimeMillis;
		this._canChallengeCount = new AtomicInteger(canChallengeCount);
		this._todayBuyCount = new AtomicInteger(0);
		this._todayResult = cResult;
		// this._lastWeekResult = null;
		this._picks = new ArrayList<Integer>();

		for (int i = KHallOfFrameData.HOF_MAX_POSITION; i <= KHallOfFrameData.HOF_MIN_POSITION; i++) {
			hallOfFrameVisitData.put(i, false);
		}

		_dataStatus = new DataStatus();
		_dataStatus.notifyInsert();
	}

	public long getRecordId() {
		return _recordId;
	}

	public void setRecordId(long _recordId) {
		this._recordId = _recordId;
	}

	public long getRoleId() {
		return _roleId;
	}

	public void setRoleId(long _roleId) {
		this._roleId = _roleId;
	}

	public String getRoleName() {
		return _roleName;
	}

	public void setRoleName(String _roleName) {
		this._roleName = _roleName;
	}

	public String getExtRoleName() {
		return _extRoleName;
	}

	public int getRoleLevel() {
		return _roleLevel;
	}

	public void setRoleLevel(int _roleLevel) {
		this._roleLevel = _roleLevel;
	}

	public int getRanking() {
		return _ranking;
	}

	public void setRanking(int _ranking) {
		this._ranking = _ranking;
	}

	public byte getOccupation() {
		return _occupation;
	}

	public void setOccupation(byte _occupation) {
		this._occupation = _occupation;
	}

	public int getHeadResId() {
		return _headResId;
	}

	public void setHeadResId(int _headResId) {
		this._headResId = _headResId;
	}

	public int getInMapResId() {
		return _inMapResId;
	}

	public void setInMapResId(int _inMapResId) {
		this._inMapResId = _inMapResId;
	}

	public long getResetTimeMillis() {
		return _resetTimeMillis;
	}

	public void setResetTimeMillis(long _resetTimeMillis) {
		this._resetTimeMillis = _resetTimeMillis;
	}

	public AtomicInteger getCanChallengeCount() {
		return _canChallengeCount;
	}

	public AtomicInteger getTodayBuyCount() {
		return _todayBuyCount;
	}

	public boolean isHadOpen() {
		return _hadOpen;
	}

	public void setHadOpen(boolean hadOpen) {
		this._hadOpen = hadOpen;
	}

	public boolean isOnLine() {
		return _onLine;
	}

	public void setOnLine(boolean _onLine) {
		this._onLine = _onLine;
	}

	public KCompetitionReward getTodayCompetitionReward() {
		return _todayResult;
	}

	public void setTodayCompetitionReward(KCompetitionReward cResult) {
		this._todayResult = cResult;
		this._dataStatus.notifyUpdate();
	}

	// public KCompetitionReward getLastWeekReward() {
	// return _lastWeekResult;
	// }

	// public void setLastWeekReward(KCompetitionReward lastWeekResult) {
	// this._lastWeekResult = lastWeekResult;
	// this._dataStatus.notifyUpdate();
	// }

	public int getFightPower() {
		return _fightPower;
	}

	public void setFightPower(int fightPower) {
		this._fightPower = fightPower;
	}

	public int getVipLv() {
		return _vipLv;
	}

	public void setVipLv(int vipLv) {
		this._vipLv = vipLv;
	}

	public Map<Integer, Boolean> getHallOfFrameVisitData() {
		return hallOfFrameVisitData;
	}

	public int getSerialWinCount() {
		return _serialWinCount;
	}

	public boolean visitHall(int position) {
		if (hallOfFrameVisitData.containsKey(position)) {
			if (!hallOfFrameVisitData.get(position)) {
				hallOfFrameVisitData.put(position, true);
				this._dataStatus.notifyUpdate();
				return true;
			}
		}
		return false;
	}

	public boolean isVisitHall(int position) {
		if (hallOfFrameVisitData.containsKey(position)) {
			return hallOfFrameVisitData.get(position);
		}
		return false;
	}

	private void decodeDBAttribute(String attribute) {
		try {
			JSONObject obj = new JSONObject(attribute);
			_roleName = obj.getString(KEY_ROLE_NAME);
			_extRoleName = HyperTextTool.extRoleName(_roleName);
			_roleLevel = obj.getInt(KEY_ROLE_LV);
			_occupation = obj.getByte(KEY_ROLE_OCC);
			_headResId = obj.getInt(KEY_ROLE_HEAD_ID);
			_inMapResId = obj.getInt(KEY_ROLE_MAP_ID);
			_fightPower = obj.getInt(KEY_ROLE_FIGHT_PWD);
			_vipLv = obj.optInt(KEY_ROLE_VIP_LV, (byte) 0);
			_totalBattleCount = obj.getInt(KEY_ROLE_TOTAL_BATTLE);
			_winBattleCount = obj.getInt(KEY_ROLE_WIN_BATTLE);
			int cCount = obj.optInt(KEY_ROLE_CHALLENGE_COUNT,
					KCompetitionModuleConfig.getMaxChallengeTimePerDay());
			_canChallengeCount = new AtomicInteger(cCount);
			int buyCount = obj.optInt(KEY_ROLE_BUY_COUNT, 0);
			_todayBuyCount = new AtomicInteger(buyCount);
			this._resetTimeMillis = obj.optLong(KEY_ROLE_CHECK_TIME,
					System.currentTimeMillis());
			this._lastCdTime = obj.optLong(KEY_LAST_CD_TIME, 0);
			if (obj.has(KEY_ROLE_TODAY_REWARD)) {
				decodeRewardData(obj.getJSONObject(KEY_ROLE_TODAY_REWARD));
			} else {
				this._todayResult = new KCompetitionReward( _roleId,
						_roleLevel, _ranking, false);
			}
//			if (obj.has(KEY_ROLE_LAST_WEEK_REWARD)) {
//				decodeRewardData(obj.getJSONObject(KEY_ROLE_LAST_WEEK_REWARD),
//						false);
//			}
			if (obj.has(KEY_ROLE_VISIT_HALL)) {
				decodeVisitHallData(obj.getJSONObject(KEY_ROLE_VISIT_HALL));
			} else {
				for (int i = KHallOfFrameData.HOF_MAX_POSITION; i <= KHallOfFrameData.HOF_MIN_POSITION; i++) {
					hallOfFrameVisitData.put(i, false);
				}
			}
			_serialWinCount = obj.optInt(KEY_SERIAL_WIN_COUNT, 0);
		} catch (JSONException ex) {
			_LOGGER.error("竞技场decodeDBAttribute出现异常！此时json的字符串是：" + attribute,
					ex);
		}
	}

	public void decodeRewardData(JSONObject obj) throws JSONException {
		int rank = obj.getInt(KEY_REWARD_RANK);
		boolean isHasReward = (obj.getInt(KEY_REWARD_HAS_GET) == 1);
		this._todayResult = new KCompetitionReward(_roleId, _roleLevel,
				rank, isHasReward);
	}

	public void decodeVisitHallData(JSONObject obj) throws JSONException {
		for (int i = KHallOfFrameData.HOF_MAX_POSITION; i <= KHallOfFrameData.HOF_MIN_POSITION; i++) {
			boolean isVisit = (obj.getInt("" + i) == 1);
			hallOfFrameVisitData.put(i, isVisit);
		}
	}

	private String encodeAttribute() {
		JSONObject obj = new JSONObject();
		try {
			obj.put(KEY_ROLE_NAME, _roleName);
			obj.put(KEY_ROLE_LV, _roleLevel);
			obj.put(KEY_ROLE_OCC, _occupation);
			obj.put(KEY_ROLE_HEAD_ID, _headResId);
			obj.put(KEY_ROLE_MAP_ID, _inMapResId);
			obj.put(KEY_ROLE_FIGHT_PWD, _fightPower);
			obj.put(KEY_ROLE_VIP_LV, _vipLv);
			obj.put(KEY_ROLE_TOTAL_BATTLE, _totalBattleCount);
			obj.put(KEY_ROLE_WIN_BATTLE, _winBattleCount);
			obj.put(KEY_ROLE_CHALLENGE_COUNT, _canChallengeCount.get());
			obj.put(KEY_ROLE_BUY_COUNT, _todayBuyCount.get());
			if (_todayResult != null) {
				obj.put(KEY_ROLE_TODAY_REWARD, encodeRewardData(_todayResult));
			}
			// if (_lastWeekResult != null) {
			// obj.put(KEY_ROLE_LAST_WEEK_REWARD,
			// encodeRewardData(_lastWeekResult));
			// }
			obj.put(KEY_ROLE_VISIT_HALL, encodeVisitHallData());
			obj.put(KEY_ROLE_CHECK_TIME, _resetTimeMillis);
			obj.put(KEY_LAST_CD_TIME, _lastCdTime);
			obj.put(KEY_SERIAL_WIN_COUNT, _serialWinCount);
		} catch (Exception ex) {
			_LOGGER.error(
					"竞技场encodeAttribute出现异常！此时json的字符串是：" + obj.toString(), ex);
		}
		return obj.toString();
	}

	private JSONObject encodeRewardData(KCompetitionReward reward) {
		JSONObject obj = new JSONObject();
		try {
			obj.put(KEY_REWARD_RANK, reward._rank);
			obj.put(KEY_REWARD_HAS_GET, ((reward._hadReceivedReward) ? 1 : 0));
		} catch (Exception ex) {
			_LOGGER.error("encodeAttribute出现异常！此时json的字符串是：" + obj.toString(),
					ex);
		}
		return obj;
	}

	private JSONObject encodeVisitHallData() {
		JSONObject obj = new JSONObject();
		try {
			for (Integer position : hallOfFrameVisitData.keySet()) {
				int isVisit = (hallOfFrameVisitData.get(position)) ? 1 : 0;
				obj.put(position + "", isVisit);
			}
		} catch (Exception ex) {
			_LOGGER.error("encodeAttribute出现异常！此时json的字符串是：" + obj.toString(),
					ex);
		}
		return obj;
	}

	void setPicks(List<Integer> picks, int pickRank) {
		_picks.clear();
		_picks.addAll(picks);
		_lastPickRank = pickRank;
		last_reflash_picks_time = System.currentTimeMillis();
	}

	boolean need2Repick() {
		return this._picks.isEmpty() || this._lastPickRank != this._ranking;
	}

	List<Integer> getPicks() {
		return this._picks;
	}

	public void notifyBattleResult(boolean isWin) {
		this._totalBattleCount++;
		this._lastCdTime = System.currentTimeMillis();
		if (isWin) {
			this._winBattleCount++;
			if (_serialWinCount <= 0) {
				_serialWinCount = 1;
			} else {
				_serialWinCount++;
			}
		} else {			
			if (_serialWinCount >= 0) {
				_serialWinCount = -1;
			} else {
				_serialWinCount--;
			}
		}
		this._dataStatus.notifyUpdate();
	}

	public boolean isCdTime() {
		return System.currentTimeMillis() - _lastCdTime < KCompetitionManager.challengeCdTimeMillis;
	}

	public int getRestCDTimeSeconds() {
		long nowTime = System.currentTimeMillis();
		long endTime = _lastCdTime + KCompetitionManager.challengeCdTimeMillis;
		if (nowTime >= endTime) {
			return 0;
		} else {
			return (int) ((endTime - nowTime) / 1000);
		}
	}

	void changeRanking(int pRanking) {
		this._ranking = pRanking;
		// this._todayResult.changeRank(_roleLevel, pRanking);
		if (_hadOpen) {
			if (_onLine) {
				KCompetitionServerMsgSender.sendUpdateRank(this, true,
						this._todayResult);
				// if (this.need2Repick()) {
				// List<KCompetitor> tempList =
				// KGameCompetitionModule.getCompetitionManager().pickCompetitor(this);
				// // 重新加载一次名单
				// KCompetitionServerMsgSender.sendUpdateCompetitionList(this._roleId,
				// tempList);
				// }
			}
			// else {
			// KGameCompetitionModule.getCompetitionManager().pickCompetitor(this);
			// // 重新加载一次名单
			// }
		}
		this._dataStatus.notifyUpdate();
	}

	void afterRankingChange(int preRanking) {
		if (_hadOpen && _onLine) {
			List<KCompetitor> tempList = KCompetitionModule
					.getCompetitionManager().pickCompetitor(this, false);
			// 重新加载一次名单
			KCompetitionServerMsgSender.sendUpdateCompetitionList(this,
					tempList, preRanking);
		}
	}

	public int getCanChallengeTimes() {
		return _canChallengeCount.intValue();
	}

	public KCompetitionBattleHistory getRecentlyHistory() {
		return _history.peek();
	}

	void addHistory(boolean isWin,String tips) {
		KCompetitionBattleHistory history = new KCompetitionBattleHistory(isWin, tips);
		synchronized (_history) {
			if (!_history.offerFirst(history)) {
				_history.removeLast();
				_history.addFirst(history);
			}
			// 发送更新
			if (_onLine) {
				KCompetitionServerMsgSender.sendNewHistory(this);
			}
		}
	}

	/**
	 * 增加一次挑战记录
	 */
	void notifyChallengeStart() {
		changeCanChallengeTime(1, false);
	}

	private void changeCanChallengeTime(int delta, boolean add) {
		if (add) {
			this._canChallengeCount.getAndAdd(delta);
		} else {
			this._canChallengeCount.getAndAdd(-delta);
			this._lastCdTime = 0;
		}
		if (this._onLine && this._hadOpen) {
			// 发送更新
			KCompetitionServerMsgSender.sendUpdateCanChallengeTimes(this);
		}
		this._dataStatus.notifyUpdate();
	}

	void increaseCanChallengeTime(int addValue) {
		this.changeCanChallengeTime(addValue, true);
		this._todayBuyCount.addAndGet(addValue);
		this._dataStatus.notifyUpdate();
	}

	void checkChallengeTime() {
		if (UtilTool.isBetweenDay(_resetTimeMillis, System.currentTimeMillis())) {
			// for (int i = KHallOfFrameData.HOF_MAX_POSITION; i <=
			// KHallOfFrameData.HOF_MIN_POSITION; i++) {
			// hallOfFrameVisitData.put(i, false);
			// }
			resetChallengeTime();
		}
	}

	void resetChallengeTime() {
		// VIPLevelData vipData =
		// KSupportFactory.getVipModuleSupport().getVIPLevelData(_roleId);
		int maxTimes = 0;
		// if(vipData != null) {
		// maxTimes = vipData.competitionFreeTime;
		// } else {
		maxTimes = KCompetitionModuleConfig.getMaxChallengeTimePerDay();
		// + KCompetitionManager.vip_canChallengeCount;
		// }
		for (int i = KHallOfFrameData.HOF_MAX_POSITION; i <= KHallOfFrameData.HOF_MIN_POSITION; i++) {
			hallOfFrameVisitData.put(i, false);
		}
		changeCanChallengeTime(maxTimes - this._canChallengeCount.intValue(),
				true);
		this._todayBuyCount.set(0);
		// this._todayResult.resetReceivedReward();
		this._resetTimeMillis = System.currentTimeMillis();
		this._dataStatus.notifyUpdate();
	}

	public void notifyBattlePowerChange(int fightPower) {
		this._fightPower = fightPower;
		this._dataStatus.notifyUpdate();
	}

	public void notifyVipLvChange(int vipLv) {
		this._vipLv = vipLv;
		this._dataStatus.notifyUpdate();
	}

	public KCompetitionBattleHistory[] getHistory() {
		return this._history.toArray(new KCompetitionBattleHistory[0]);
	}

	public void packInfo(KGameMessage msg) {
		IRoleMapResInfo info = null;
		KRole role = null;
		RoleModuleSupport roleSupport = KSupportFactory.getRoleModuleSupport();
		if (roleSupport.isRoleDataInCache(_roleId)) {
			role = roleSupport.getRole(_roleId);
			info = role;
		} else {
			info = DefaultRoleMapResInfoManager
					.getDefaultRoleMapResInfo(KJobTypeEnum.getJob(_occupation));
		}
		msg.writeLong(_roleId);
		msg.writeInt(_ranking);
		msg.writeShort(_roleLevel);
		msg.writeUtf8String(_roleName);
		msg.writeByte(_occupation);
		msg.writeInt(_headResId);
		msg.writeInt(_inMapResId);
		roleSupport.packRoleResToMsg(info, _occupation, msg);
		boolean hasRedEquip = false;
		if(role!=null){
			List<RoleEquipShowData> list = KSupportFactory.getItemModuleSupport().getRoleEquipShowDataList(role.getId());
			if(list!=null){
				for (RoleEquipShowData equipData:list) {
					if(equipData.equipType == KEquipmentTypeEnum.主武器.sign){
						if(equipData.getQuality() == KItemQualityEnum.无敌的){
							hasRedEquip = true;
						}
					}
				}
			}
		}
		msg.writeBoolean(hasRedEquip);
		msg.writeInt(_fightPower);
		msg.writeInt(_winBattleCount);
		msg.writeInt(_totalBattleCount);
		msg.writeByte(_vipLv);
	}

	@Override
	public String toString() {
		StringBuilder strBld = new StringBuilder("KCompetitor [");
		strBld.append("roleId=").append(_roleId).append(", ");
		strBld.append("roleName=").append(_roleName).append(", ");
		strBld.append("roleLevel=").append(_roleLevel).append(", ");
		strBld.append("ranking=").append(_ranking);
		strBld.append("]");
		return strBld.toString();
	}

	public DBRank notifyScan() {
		int original = this._dataStatus.getStatus();

		DBRank dbRank = new DBRankImpl();
		dbRank.setDBId(_recordId);
		dbRank.setElementId(_roleId);
		dbRank.setRankType(rankType.sign);
		dbRank.setRank(_ranking);
		dbRank.setAttribute(encodeAttribute());
		this._dataStatus.changeToNone(original);
		return dbRank;
	}

	public void notifyDB() {
		this._dataStatus.notifyUpdate();
	}

	public static class KCompetitionReward {

		private long _roleId;
		private int _rank;
		private boolean _hadReceivedReward;
		private CompetitionRewardShowData reward;

		public KCompetitionReward(long pRoleId, int roleLv, int pRank,
				boolean isHasReward) {
			this._roleId = pRoleId;
			this._rank = pRank;
			this._hadReceivedReward = isHasReward;
			this.reward = KCompetitionManager.caculateTodayReward("", roleLv,
					pRank);
		}

		public long getRoleId() {
			return _roleId;
		}

		public int getRank() {
			return _rank;
		}

		public boolean isHadReceivedReward() {
			return this._hadReceivedReward;
		}

		void notifyReceivedReward() {
			this._hadReceivedReward = true;
		}

		void resetReceivedReward() {
			this._hadReceivedReward = false;
		}

		public CompetitionRewardShowData getReward() {
			return reward;
		}

		public void changeRank(int roleLv, int rank) {
			this._rank = rank;
			this.reward = KCompetitionManager.caculateTodayReward("", roleLv,
					rank);
		}
	}
	
	public static class KCompetitionBattleHistory{
		public boolean isWin;
		public String tips;
		private KCompetitionBattleHistory(boolean isWin, String tips) {
			super();
			this.isWin = isWin;
			this.tips = tips;
		}		
	}

}
