package com.kola.kmp.logic.activity.goldact;

import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.role.RoleExtCABaseImpl.IExtCADataStatusProxy;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.mission.daily.KDailyMissionManager;

public class GoldActivityRoleRecordData {
	
	private final static String JSON_KEY_REST_COUNT = "1";
	private final static String JSON_KEY_CHECK_TIME = "2";

	public int restChallengeCount;
	
	public long checkTime;
	
	private IExtCADataStatusProxy proxy;

	public GoldActivityRoleRecordData(IExtCADataStatusProxy proxy) {
		super();
		this.proxy = proxy;
		this.restChallengeCount = KGoldActivityManager.max_can_challenge_count;
		this.checkTime = System.currentTimeMillis();
	}

	public void decode(String dbData) throws Exception{
		if (dbData != null && dbData.length() > 0) {
			JSONObject obj = new JSONObject(dbData);
			this.restChallengeCount = obj.optInt(JSON_KEY_REST_COUNT, 0);
			this.checkTime = obj.optLong(JSON_KEY_CHECK_TIME,System.currentTimeMillis());
		}
	}
	
	public String encode() throws Exception{
		JSONObject obj = new JSONObject();
		obj.put(JSON_KEY_REST_COUNT, restChallengeCount);
		obj.put(JSON_KEY_CHECK_TIME, checkTime);
		return obj.toString();
	}
	
	public void challenge(){
		restChallengeCount--;
		proxy.notifyUpdate();
	}
	
	public boolean checkAndRestData(boolean isNeedCheck){
		boolean isDataChanged = false;
		if (!isNeedCheck
				|| (isNeedCheck && UtilTool
						.checkNowTimeIsArriveTomorrow(this.checkTime))) {
			this.checkTime = System
					.currentTimeMillis();
			this.restChallengeCount = KGoldActivityManager.max_can_challenge_count;
			isDataChanged = true;
		}

		if (isDataChanged) {
			proxy.notifyUpdate();
		}
		
		return isDataChanged;
	}
}
