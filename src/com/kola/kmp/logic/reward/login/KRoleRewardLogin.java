package com.kola.kmp.logic.reward.login;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.koala.thirdpart.json.JSONArray;
import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.reward.KRewardSonModuleAbs.KRewardSonModuleType;
import com.kola.kmp.logic.reward.KRoleReward;
import com.kola.kmp.logic.reward.KRoleRewardSonAbs;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * <pre>
 * 角色奖励数据的子数据
 * 
 * @author CamusHuang
 * @creation 2013-12-28 上午10:38:13
 * </pre>
 */
public class KRoleRewardLogin extends KRoleRewardSonAbs {

	/** 累计登陆天数 */
	private int totalLoginDays = 1;

	/** 七天奖励领取到哪天 <天> */
	private int lastSevenCollectDay;

	/** 签到奖励领取到哪天 */
	private int lastChuckUpRewardCollectDay;
	/** 签到数据 */
	private Set<Integer> checkUpData = new HashSet<Integer>();

	// ////////////////////////////////
	static final String JSON_TOTALDAY = "A";
	static final String JSON_SEVENDAY_COLLECTED = "E";
	static final String JSON_CHECKUPDAY_COLLECTED = "F";
	static final String JSON_CHECKUPDAY_CHECKUPDATA = "G";

	public KRoleRewardLogin(KRoleReward owner, KRewardSonModuleType type, boolean isFirstNew) {
		super(owner, type);
	}

	@Override
	public void decode(JSONObject json, int ver) throws JSONException {
		// 由底层调用,解释出逻辑层数据
		try {
			// CEND 暂时只有版本0
			switch (ver) {
			case 0:
				totalLoginDays = json.getInt(JSON_TOTALDAY);
				lastSevenCollectDay = json.optInt(JSON_SEVENDAY_COLLECTED, totalLoginDays - 1);
				lastChuckUpRewardCollectDay = json.optInt(JSON_CHECKUPDAY_COLLECTED, totalLoginDays - 1);
				{
					JSONArray jarray = json.optJSONArray(JSON_CHECKUPDAY_CHECKUPDATA);
					if (jarray != null) {
						for (int i = 0; i < jarray.length(); i++) {
							checkUpData.add(jarray.getInt(i));
						}
					}
				}
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
			json.put(JSON_TOTALDAY, totalLoginDays);
			json.put(JSON_SEVENDAY_COLLECTED, lastSevenCollectDay);
			json.put(JSON_CHECKUPDAY_COLLECTED, lastChuckUpRewardCollectDay);
			{
				JSONArray jarray = new JSONArray();
				json.put(JSON_CHECKUPDAY_CHECKUPDATA, jarray);
				for (int day : checkUpData) {
					jarray.put(day);
				}
			}
		} catch (Exception ex) {
			_LOGGER.error("encode数据时发生错误 roleId=" + owner.getRoleId() + " ----丢失数据！", ex);
		}
		return json;
	}

	int getTotalLoginDays() {
		return totalLoginDays;
	}

	int getLastSevenCollectDay() {
		return lastSevenCollectDay;
	}

	void setLastSevenCollectDay(int lastCollectDay) {
		this.lastSevenCollectDay = lastCollectDay;
		notifyDB();
	}

	void checkUp(int TODAY) {
		checkUpData.add(TODAY);
		notifyDB();
	}

	Set<Integer> getCheckUpData() {
		return checkUpData;
	}

	int getLastCheckUpRewardCollectDay() {
		return lastChuckUpRewardCollectDay;
	}

	void setLastChuckUpRewardCollectDay(int lastCollectDay) {
		this.lastChuckUpRewardCollectDay = lastCollectDay;
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
		totalLoginDays++;
		notifyDB();
	}

	public void notifyForMonthChange(long nowTime) {
		checkUpData.clear();
		lastChuckUpRewardCollectDay = 0;
		notifyDB();
	}

}
