package com.kola.kmp.logic.reward.vitality;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.koala.thirdpart.json.JSONArray;
import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
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
public class KRoleRewardVitality extends KRoleRewardSonAbs {

	// 累计活跃度值
	private int vitalityValue;
	// 各类型功能完成的次数
	private Map<KVitalityTypeEnum, AtomicInteger> funTimeMap = new HashMap<KVitalityTypeEnum, AtomicInteger>();
	// 今天已经领到的奖励
	private Set<Integer> collectedReward = new HashSet<Integer>();

	// ////////////////////////////////
	private static final String JSON_VATALITY_VALUE = "B";
	private static final String JSON_FUN_TIME = "C";
	private static final String JSON_COLLECTED_REWARD = "D";

	public KRoleRewardVitality(KRoleReward owner, KRewardSonModuleType type, boolean isFirstNew) {
		super(owner, type);
	}

	@Override
	public void decode(JSONObject json, int ver) throws JSONException {
		// 由底层调用,解释出逻辑层数据
		try {
			// CEND 暂时只有版本0
			switch (ver) {
			case 0:
				vitalityValue = json.getInt(JSON_VATALITY_VALUE);
				{
					JSONObject temp = json.getJSONObject(JSON_FUN_TIME);
					for (KVitalityTypeEnum type : KVitalityTypeEnum.values()) {
						int time = temp.optInt(type.sign + "");
						if (time > 0) {
							AtomicInteger atime = funTimeMap.get(type);
							if (atime == null) {
								atime = new AtomicInteger();
								funTimeMap.put(type, atime);
							}
							atime.set(time);
						}
					}
				}
				{
					JSONArray temp = json.optJSONArray(JSON_COLLECTED_REWARD);
					if (temp != null) {
						for (int i = temp.length() - 1; i >= 0; i--) {
							collectedReward.add(temp.getInt(i));
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
			json.put(JSON_VATALITY_VALUE, vitalityValue);
			{
				JSONObject temp = new JSONObject();
				json.put(JSON_FUN_TIME, temp);
				for (Entry<KVitalityTypeEnum, AtomicInteger> entry : funTimeMap.entrySet()) {
					int time = entry.getValue().get();
					if (time > 0) {
						temp.put(entry.getKey().sign + "", time);
					}
				}
			}
			{
				JSONArray temp = new JSONArray();
				json.put(JSON_COLLECTED_REWARD, temp);
				for (int score : collectedReward) {
					temp.put(score);
				}
			}
		} catch (Exception ex) {
			_LOGGER.error("encode数据时发生错误 roleId=" + owner.getRoleId() + " ----丢失数据！", ex);
		}
		return json;
	}

	void addVitality(int addValue) {
		rwLock.lock();
		try {
			vitalityValue += addValue;
			notifyDB();
		} finally {
			rwLock.unlock();
		}
	}

	int getVitalityValue() {
		return vitalityValue;
	}

	int getFunTime(KVitalityTypeEnum funtype) {
		rwLock.lock();
		try {
			AtomicInteger time = funTimeMap.get(funtype);
			if (time == null) {
				return 0;
			}
			return time.get();
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 
	 * 
	 * @param funtype
	 * @param addTime
	 * @return
	 * @author CamusHuang
	 * @creation 2014-6-2 下午6:09:39
	 * </pre>
	 */
	int recordFuns(KVitalityTypeEnum funtype, int addTime) {
		rwLock.lock();
		try {
			AtomicInteger time = funTimeMap.get(funtype);
			if (time == null) {
				time = new AtomicInteger();
				funTimeMap.put(funtype, time);
			}
			time.addAndGet(addTime);
			notifyDB();
			return time.get();
		} finally {
			rwLock.unlock();
		}
	}

	boolean isCollectedReward(int score) {
		return collectedReward.contains(score);
	}

	void setCollectedReward(int score) {
		rwLock.lock();
		try {
			if (collectedReward.add(score)) {
				notifyDB();
			}
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
			if (!collectedReward.isEmpty() || vitalityValue != 0 || !funTimeMap.isEmpty()) {
				collectedReward.clear();
				vitalityValue = 0;
				funTimeMap.clear();
				notifyDB();
			}
		} finally {
			rwLock.unlock();
		}
	}

}
