package com.kola.kmp.logic.activity.worldboss;

import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.role.RoleExtCABaseImpl.IExtCADataStatusProxy;

/**
 * 
 * @author PERRY CHAN
 */
public class KWorldBossRoleData {

	
	private static final String KEY_SHOW_INTRODUCE = "1";
	private static final String KEY_AUTO_JOIN = "2";
	private static final String KEY_CURRENT_AUTO_JOIN_TIMES = "3";
	
//	private long _roleId;
	private IExtCADataStatusProxy _proxy;
	
	private boolean _showIntroduce = true; // 是否显示简介
	private boolean _autoJoin = false;
	private int _currentAutoJoinTimes = 0;
	
	public KWorldBossRoleData(long roleId, IExtCADataStatusProxy pProxy) {
//		this._roleId = roleId;
		this._proxy = pProxy;
	}
	
	boolean isShowIntroduce() {
		return _showIntroduce;
	}
	
	boolean isAutoJoin() {
		return _autoJoin;
	}
	
	int getCurrentAutoJoinTime() {
		return _currentAutoJoinTimes;
	}
	
	public void setShowIntroduce(boolean flag) {
		this._showIntroduce = flag;
		this._proxy.notifyUpdate();
	}
	
	public void setAutoJoinStatus(boolean flag) {
		this._autoJoin = flag;
		if (!flag) {
			this._currentAutoJoinTimes = 0;
		}
		this._proxy.notifyUpdate();
	}
	
	public void notifyAutoJoin() {
		this._currentAutoJoinTimes++;
		this._proxy.notifyUpdate();
	}
	
	public void parseData(String dbData) throws Exception {
		if (dbData != null && dbData.length() > 0 && dbData.startsWith("{")) {
			JSONObject obj = new JSONObject(dbData);
			this._showIntroduce = obj.optBoolean(KEY_SHOW_INTRODUCE, true);
			this._autoJoin = obj.optBoolean(KEY_AUTO_JOIN, false);
			this._currentAutoJoinTimes = obj.optInt(KEY_CURRENT_AUTO_JOIN_TIMES, _currentAutoJoinTimes);
		}
	}
	
	public String saveData() throws Exception {
		JSONObject obj = new JSONObject();
		obj.put(KEY_SHOW_INTRODUCE, _showIntroduce);
		obj.put(KEY_AUTO_JOIN, _autoJoin);
		obj.put(KEY_CURRENT_AUTO_JOIN_TIMES, _currentAutoJoinTimes);
		return obj.toString();
	}
}
