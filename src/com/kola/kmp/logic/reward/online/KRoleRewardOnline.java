package com.kola.kmp.logic.reward.online;

import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kmp.logic.reward.KRewardSonModuleAbs.KRewardSonModuleType;
import com.kola.kmp.logic.reward.KRoleReward;
import com.kola.kmp.logic.reward.KRoleRewardSonAbs;

/**
 * <pre>
 * 角色奖励数据的子数据
 * 
 * @author CamusHuang
 * @creation 2013-12-28 上午10:38:13
 * </pre>
 */
public class KRoleRewardOnline extends KRoleRewardSonAbs {

//	/** 今日的日期时间 */
//	private long dataTime;
	/** 今日最后一次领取的奖励ID,0表示今天未领取过 */
	private int lastRewardId;
	/** 本次在线时长起始时间 */
	private long startTime;
	/** 本次在线时长积累(S) */
	private int totalTime;

	// ////////////////////////////////
//	static final String JSON_DAY = "Z";// 当前数据对应的日期

	static final String JSON_LAST_REWARD_ID = "A";
	static final String JSON_START_TIME = "B";
	static final String JSON_TOTAL_TIME = "C";

	public KRoleRewardOnline(KRoleReward owner, KRewardSonModuleType type, boolean isFirstNew) {
		super(owner, type);
		if (isFirstNew) {
//			dataTime = System.currentTimeMillis();
		}
	}

	@Override
	public void decode(JSONObject json, int ver) throws JSONException {
		// 由底层调用,解释出逻辑层数据
		try {
			// CEND 暂时只有版本0
			switch (ver) {
			case 0:
//				dataTime = json.getLong(JSON_DAY) * Timer.ONE_DAY;
				lastRewardId = json.getInt(JSON_LAST_REWARD_ID);
				startTime = json.getLong(JSON_START_TIME);
				totalTime = json.getInt(JSON_TOTAL_TIME);
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
//			json.put(JSON_DAY, dataTime / Timer.ONE_DAY);
			json.put(JSON_LAST_REWARD_ID, lastRewardId);
			json.put(JSON_START_TIME, startTime);
			json.put(JSON_TOTAL_TIME, totalTime);
		} catch (Exception ex) {
			_LOGGER.error("encode数据时发生错误 roleId=" + owner.getRoleId() + " ----丢失数据！", ex);
		}
		return json;
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 本方法必须设置计时起始时间 
	 * @param newDayStartTime
	 * @author CamusHuang
	 * @creation 2014-4-10 上午10:25:35
	 * </pre>
	 */
	void notifyForLogin(long nowTime) {
		rwLock.lock();
		try {
//			if (UtilTool.isBetweenDay(dataTime, nowTime)) {
//				//已在KRoleReward的登陆中处理跨天数据重置
//				// notifyForDayChange(nowTime); 已由KRoleReward统一调用，本处可忽略
//			}
			startTime = nowTime;
			notifyDB();
		} finally {
			rwLock.unlock();
		}
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
		rwLock.lock();
		try {
//			dataTime = nowTime;
			lastRewardId = 0;
			startTime = nowTime;
			totalTime = 0;
			notifyDB();
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 在线时长结算
	 * 
	 * @deprecated 只能在玩家在线时或下线时调用
	 * @author CamusHuang
	 * @creation 2014-5-6 下午9:38:14
	 * </pre>
	 */
	int countOnlineTime(long nowTime) {
		rwLock.lock();
		try {
			if (startTime > 0) {
				totalTime += (nowTime - startTime);
				startTime = nowTime;
				notifyDB();
			}
			return totalTime;
		} finally {
			rwLock.unlock();
		}
	}

	void setLastRewardId(int lastRewardId) {
		rwLock.lock();
		try {
			this.lastRewardId = lastRewardId;
			totalTime = 0;
			startTime = System.currentTimeMillis();
			notifyDB();
		} finally {
			rwLock.unlock();
		}
	}

	int getLastRewardId() {
		return lastRewardId;
	}

}
