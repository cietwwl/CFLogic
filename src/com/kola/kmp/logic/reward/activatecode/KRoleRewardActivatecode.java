package com.kola.kmp.logic.reward.activatecode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.koala.thirdpart.json.JSONArray;
import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kmp.logic.reward.KRewardSonModuleAbs.KRewardSonModuleType;
import com.kola.kmp.logic.reward.KRoleReward;
import com.kola.kmp.logic.reward.KRoleRewardSonAbs;
import com.kola.kmp.logic.reward.daylucky.KDayluckyDataManager;
import com.kola.kmp.logic.reward.daylucky.KDayluckyDataManager.KDayluckyRateDataManager.DayluckyRateData;

/**
 * <pre>
 * 角色奖励数据的子数据
 * 
 * @author CamusHuang
 * @creation 2013-12-28 上午10:38:13
 * </pre>
 */
public class KRoleRewardActivatecode extends KRoleRewardSonAbs {

	/** 已使用的激活码类型<大类型,<小类型>> */
	private Map<String, Set<Integer>> typeData = new HashMap<String, Set<Integer>>();

	// ////////////////////////////////
	// ///////////////
	static final String JSON_TYPE_DATA = "1";
	static final String JSON_TYPE_DATA_BIGTYPE = "1";
	static final String JSON_TYPE_DATA_SONTYPE = "2";

	public KRoleRewardActivatecode(KRoleReward owner, KRewardSonModuleType type, boolean isFirstNew) {
		super(owner, type);
		if (isFirstNew) {
			// 如果第一张刮刮卡是0活跃度要求，则默认生成刮刮卡
			DayluckyRateData data = KDayluckyDataManager.mDayluckyRateDataManager.getData(1);
			if (data != null) {
				if (data.needvitality < 1) {
					// hidedNums = KDayluckyCenter.randomNum(data);
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
				decodeBaseInfo(json.getJSONArray(JSON_TYPE_DATA));
				break;
			}

		} catch (Exception ex) {
			_LOGGER.error("decode数据时发生错误 roleId=" + owner.getRoleId() + " ----丢失数据，存在运行隐患！", ex);
		}
	}

	public void decodeBaseInfo(JSONArray data) throws JSONException {
		typeData.clear();
		
		int len = data.length();
		for (int i=0;i<len;i++) {
			JSONObject temp = data.getJSONObject(i);
			//
			String type = temp.getString(JSON_TYPE_DATA_BIGTYPE);
			Set<Integer> set=new HashSet<Integer>();
			typeData.put(type, set);
			
			JSONArray array = temp.getJSONArray(JSON_TYPE_DATA_SONTYPE);
			{
				int len2 = array.length();
				for (int k=0;k<len2;k++) {
					set.add(array.getInt(k));
				}
			}
		}
	}

	@Override
	public JSONObject encode() throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put(JSON_TYPE_DATA, encodeBaseInfo());// 基础信息
		return obj;
	}

	public JSONArray encodeBaseInfo() throws JSONException {
		JSONArray data = new JSONArray();
		{
			for (Entry<String, Set<Integer>> e : typeData.entrySet()) {
				JSONObject temp = new JSONObject();
				data.put(temp);
				//
				temp.put(JSON_TYPE_DATA_BIGTYPE, e.getKey());
				JSONArray array = new JSONArray();
				temp.put(JSON_TYPE_DATA_SONTYPE, array);
				{
					for (int st : e.getValue()) {
						array.put(st);
					}
				}
			}
		}

		return data;
	}

	@Override
	protected void notifyForDayChange(long nowTime) {
		// 忽略
	}

	boolean isUsedSonType(String type, int sonType) {
		rwLock.lock();
		try {

			Set<Integer> set = typeData.get(type);
			if (set == null) {
				return false;
			}
			return set.contains(sonType);
		} finally {
			rwLock.unlock();
		}
	}

	int getUsedSonTypeCount(String type) {
		rwLock.lock();
		try {
			Set<Integer> set = typeData.get(type);
			if (set == null) {
				return 0;
			}
			return set.size();
		} finally {
			rwLock.unlock();
		}
	}

	boolean recordUsedCode(String type, int sonType) {
		rwLock.lock();
		try {
			Set<Integer> set = typeData.get(type);
			if (set == null) {
				set = new HashSet<Integer>();
				typeData.put(type, set);
			}
			boolean isSuccess = set.add(sonType);
			if(isSuccess){
				notifyDB();
			}
			return isSuccess;
		} finally {
			rwLock.unlock();
		}
	}

}
