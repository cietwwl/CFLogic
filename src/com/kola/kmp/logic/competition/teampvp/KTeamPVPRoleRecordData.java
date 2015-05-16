package com.kola.kmp.logic.competition.teampvp;

import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.role.RoleExtCABaseImpl.IExtCADataStatusProxy;

/**
 * 
 * @author PERRY CHAN
 */
public class KTeamPVPRoleRecordData {

	private static final String KEY_CURRENT_CHALLENGE_TIME = "1";
	private static final String KEY_CURRENT_BUY_TIME = "2";
	private static final String KEY_LAST_GET_REWARD_TIME = "3";
	private static final String KEY_MY_HIGHEST_STAGE = "4";
	
	private IExtCADataStatusProxy _proxy;
	
	private int _currentChallengeTime;
	private int _currentBuyTime;
	private long _lastGetDailyRewardTime;
	private int _myHighestStage;
	
	public KTeamPVPRoleRecordData(IExtCADataStatusProxy proxy) {
		_proxy = proxy;
	}
	
	public String encode() throws Exception {
		JSONObject obj = new JSONObject();
		obj.put(KEY_CURRENT_CHALLENGE_TIME, _currentChallengeTime);
		obj.put(KEY_CURRENT_BUY_TIME, _currentBuyTime);
		obj.put(KEY_LAST_GET_REWARD_TIME, _lastGetDailyRewardTime);
		obj.put(KEY_MY_HIGHEST_STAGE, _myHighestStage);
		return obj.toString();
	}
	
	public void decode(String attribute) throws Exception {
		if (attribute != null && attribute.length() > 0) {
			JSONObject obj = new JSONObject(attribute);
			this._currentChallengeTime = obj.getInt(KEY_CURRENT_CHALLENGE_TIME);
			this._currentBuyTime = obj.getInt(KEY_CURRENT_BUY_TIME);
			this._lastGetDailyRewardTime = obj.optLong(KEY_LAST_GET_REWARD_TIME, 0);
			this._myHighestStage = obj.optInt(KEY_MY_HIGHEST_STAGE, KTeamPVPConfig.getFirstDanRankId());
		}
	}
	
	public void setCurrentChallengeTime(int value) {
		this._currentChallengeTime = value;
		this._proxy.notifyUpdate();
	}
	
	public int getCurrentChallengeTime() {
		return _currentChallengeTime;
	}
	
	public void setCurrentBuyTime(int value) {
		this._currentBuyTime = value;
		this._proxy.notifyUpdate();
	}

	public int getCurrentBuyTime() {
		return _currentBuyTime;
	}

	public long getLastGetDailyRewardTime() {
		return _lastGetDailyRewardTime;
	}

	public void setLastGetDailyRewardTime(long pLastGetDailyRewardTime) {
		this._lastGetDailyRewardTime = pLastGetDailyRewardTime;
		_proxy.notifyUpdate();
	}

	public int getMyHighestStage() {
		return _myHighestStage;
	}

	public void setMyHighestStage(int pMyHighestStage) {
		this._myHighestStage = pMyHighestStage;
		_proxy.notifyUpdate();
	}
}
