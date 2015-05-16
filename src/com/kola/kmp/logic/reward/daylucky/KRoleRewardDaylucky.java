package com.kola.kmp.logic.reward.daylucky;

import java.util.ArrayList;
import java.util.List;

import javax.management.timer.Timer;

import com.koala.thirdpart.json.JSONArray;
import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kmp.logic.reward.KRewardSonModuleAbs.KRewardSonModuleType;
import com.kola.kmp.logic.reward.daylucky.KDayluckyDataManager.KDayluckyRateDataManager.DayluckyRateData;
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
public class KRoleRewardDaylucky extends KRoleRewardSonAbs {

	/** 今日已刮开的幸运号码 */
	final List<int[]> todayLuckNums = new ArrayList<int[]>();
	/** 今日未刮开的号码 */
	private int[] hidedNums;

	// ////////////////////////////////
	static final String JSON_HIDED = "A";
	static final String JSON_LUCKNUMS = "B";

	public KRoleRewardDaylucky(KRoleReward owner, KRewardSonModuleType type, boolean isFirstNew) {
		super(owner, type);
		if (isFirstNew) {
			// 如果第一张刮刮卡是0活跃度要求，则默认生成刮刮卡
			DayluckyRateData data = KDayluckyDataManager.mDayluckyRateDataManager.getData(1);
			if (data != null) {
				if (data.needvitality < 1) {
					hidedNums = KDayluckyCenter.randomNum(data);
				}
			}
		}
	}

	@Override
	public void decode(JSONObject json, int ver) throws JSONException {
		// 由底层调用,解释出逻辑层数据
		try {
			// CEND 暂时只有版本0
			switch (ver) {
			case 0:
				hidedNums = decodeInts(json.optJSONArray(JSON_HIDED));
				//
				JSONArray array = json.optJSONArray(JSON_LUCKNUMS);
				if (array != null) {
					int len = array.length();
					for (int i = 0; i < len; i++) {
						int[] temp = decodeInts(array.getJSONArray(i));
						if (temp != null) {
							todayLuckNums.add(temp);
						}
					}
				}
				break;
			}

		} catch (Exception ex) {
			_LOGGER.error("decode数据时发生错误 roleId=" + owner.getRoleId() + " ----丢失数据，存在运行隐患！", ex);
		}
	}

	private int[] decodeInts(JSONArray array) throws JSONException {
		if (array == null) {
			return null;
		}
		int[] result = new int[array.length()];
		for (int i = 0; i < result.length; i++) {
			result[i] = array.getInt(i);
		}
		return result;
	}

	@Override
	public JSONObject encode() throws JSONException {
		JSONObject json = new JSONObject();
		// 构造一个数据对象给底层
		try {
			// CEND 暂时只有版本0
			JSONArray temp = encodeInts(hidedNums);
			if (temp != null) {
				json.put(JSON_HIDED, temp);
			}
			//
			if (!todayLuckNums.isEmpty()) {
				JSONArray array = new JSONArray();
				json.put(JSON_LUCKNUMS, array);

				for (int[] nums : todayLuckNums) {
					temp = encodeInts(nums);
					array.put(temp);
				}
			}

		} catch (Exception ex) {
			_LOGGER.error("encode数据时发生错误 roleId=" + owner.getRoleId() + " ----丢失数据！", ex);
		}
		return json;
	}

	private JSONArray encodeInts(int[] nums) throws JSONException {
		if (nums == null) {
			return null;
		}
		JSONArray array = new JSONArray();
		for (int i = 0; i < nums.length; i++) {
			array.put(nums[i]);
		}
		return array;
	}

	int[] getHidedNums() {
		return hidedNums;
	}

	void setHidedNums(int[] hidedNums) {
		this.hidedNums = hidedNums;
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
		rwLock.lock();
		try {
			// 清理昨天已刮的刮刮卡
			if (!todayLuckNums.isEmpty()) {
				todayLuckNums.clear();
			}
			// 清空昨天未刮的刮刮卡
			hidedNums = null;
			// 如果第一张刮刮卡是0活跃度要求，则默认生成刮刮卡
			DayluckyRateData data = KDayluckyDataManager.mDayluckyRateDataManager.getData(1);
			if (data != null) {
				if (data.needvitality < 1) {
					hidedNums = KDayluckyCenter.randomNum(data);
				}
			}
			//
			notifyDB();
		} finally {
			rwLock.unlock();
		}
	}

}
