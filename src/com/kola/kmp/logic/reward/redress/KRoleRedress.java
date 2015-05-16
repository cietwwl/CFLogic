package com.kola.kmp.logic.reward.redress;

import javax.management.timer.Timer;

import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kmp.logic.reward.KRewardSonModuleAbs.KRewardSonModuleType;
import com.kola.kmp.logic.reward.KRoleReward;
import com.kola.kmp.logic.reward.KRoleRewardSonAbs;

/**
 * <pre>
 * 角色改版补偿
 * 
 * @author CamusHuang
 * @creation 2013-12-28 上午10:38:13
 * </pre>
 */
public class KRoleRedress extends KRoleRewardSonAbs {

	/** 是否已经执行过V1改版补偿 */
	private boolean isRunVer1Redress;
	/** 是否已经执行过V2改版补偿 */
	private boolean isRunVer2Redress;
	/** 最后一次合服补偿执行的时间点 CombinedService*/
	private long lastCSRedress;

	// ////////////////////////////////
	static final String JSON_ISV1RUN = "1";//第一次补偿
	static final String JSON_ISV2RUN = "3";//1.0.5补偿
	static final String JSON_LastCSRedress = "Y";//最后一次合服补偿执行的时间点

	public KRoleRedress(KRoleReward owner, KRewardSonModuleType type, boolean isFirstNew) {
		super(owner, type);
	}

	@Override
	public void decode(JSONObject json, int ver) throws JSONException {
		// 由底层调用,解释出逻辑层数据
		try {
			// CEND 暂时只有版本0
			switch (ver) {
			case 0:
				isRunVer1Redress = json.optBoolean(JSON_ISV1RUN);
				isRunVer2Redress = json.optBoolean(JSON_ISV2RUN);
				lastCSRedress = json.optLong(JSON_LastCSRedress)*Timer.ONE_MINUTE;
				break;
			}

		} catch (Exception ex) {
			_LOGGER.error("decode数据时发生错误 roleId=" + owner.getRoleId() + " ----丢失数据，存在运行隐患！", ex);
		}
	}

	@Override
	public JSONObject encode() throws JSONException {
		JSONObject json = new JSONObject();
		// 构造一个数据对象给底层
		try {
			// CEND 暂时只有版本0
			json.put(JSON_ISV1RUN, isRunVer1Redress);
			json.put(JSON_ISV2RUN, isRunVer2Redress);
			json.put(JSON_LastCSRedress, lastCSRedress/Timer.ONE_MINUTE);

		} catch (Exception ex) {
			_LOGGER.error("encode数据时发生错误 roleId=" + owner.getRoleId() + " ----丢失数据！", ex);
		}
		return json;
	}
	

	boolean isRunVer1Redress() {
		return isRunVer1Redress;
	}

	void setRunVer1Redress(boolean isRunVer1Redress) {
		this.isRunVer1Redress = isRunVer1Redress;
		notifyDB();
	}

	boolean isRunVer2Redress() {
		return isRunVer2Redress;
	}
	
	void setRunVer2Redress(boolean isRunVer2Redress) {
		this.isRunVer2Redress = isRunVer2Redress;
		notifyDB();
	}
	
	long getLastCSRedress() {
		return lastCSRedress;
	}
	
	void setLastCSRedress(long lastCSRedress) {
		this.lastCSRedress = lastCSRedress;
		notifyDB();
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 
	 * @param newDayStartTime
	 * @author CamusHuang
	 * @creation 2014-4-10 上午10:25:35
	 * </pre>
	 */
	protected void notifyForDayChange(long nowTime) {
	}

}
