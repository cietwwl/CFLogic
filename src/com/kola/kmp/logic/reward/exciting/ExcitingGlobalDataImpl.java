package com.kola.kmp.logic.reward.exciting;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kmp.logic.other.KGameExtDataDBTypeEnum;
import com.kola.kmp.logic.reward.exciting.ExcitingTaskManager.SynAllOnlineRoleStatusTaskForExciting;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.ExcitingDataManager.ExcitionActivity;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.ExcitingRuleManager.RewardRule;
import com.kola.kmp.logic.util.CamusGlobalDataAbs;

/**
 * <pre>
 * 精彩活动全局数据
 * 
 * @author CamusHuang
 * @creation 2014-12-31 上午11:21:07
 * </pre>
 */
class ExcitingGlobalDataImpl extends CamusGlobalDataAbs {
	static final ExcitingGlobalDataImpl instance = new ExcitingGlobalDataImpl();

	// -----------
	// 活动规则全服限量领取次数 <活动ID,<规则ID,次数>>
	private Map<Integer, Map<Integer, AtomicInteger>> ruleCountMap = new HashMap<Integer, Map<Integer, AtomicInteger>>();
	//
	private final static String JSON_COUNT = "A";

	private ExcitingGlobalDataImpl() {
		super(KGameExtDataDBTypeEnum.精彩活动数据);
	}

	protected void decode(JSONObject json) throws JSONException {
		JSONObject temp = json.optJSONObject(JSON_COUNT);
		if (temp != null) {
			for (Iterator<String> it = temp.keys(); it.hasNext();) {
				String key = it.next();
				JSONObject tempA = temp.getJSONObject(key);
				Map<Integer, AtomicInteger> map = new HashMap<Integer, AtomicInteger>();
				ruleCountMap.put(Integer.parseInt(key), map);
				//

				for (Iterator<String> itA = tempA.keys(); itA.hasNext();) {
					String keyA = itA.next();
					int count = tempA.getInt(keyA);
					map.put(Integer.parseInt(keyA), new AtomicInteger(count));
				}
			}
		}
	}

	protected JSONObject encode() throws JSONException {
		JSONObject json = new JSONObject();
		{
			JSONObject temp = new JSONObject();
			json.put(JSON_COUNT, temp);
			for (Entry<Integer, Map<Integer, AtomicInteger>> e : ruleCountMap
					.entrySet()) {
				JSONObject tempA = new JSONObject();
				temp.put(e.getKey() + "", tempA);
				//
				for (Entry<Integer, AtomicInteger> eA : e.getValue().entrySet()) {
					tempA.put(eA.getKey() + "", eA.getValue().get());
				}
			}
		}
		return json;
	}

	synchronized int getCount(int activityId, int ruleId) {
		Map<Integer, AtomicInteger> map = ruleCountMap.get(activityId);
		if (map == null) {
			return 0;
		}

		AtomicInteger count = map.get(ruleId);
		if (count == null) {
			return 0;
		}

		return count.get();
	}

	synchronized boolean increaseCount(ExcitionActivity activity,
			RewardRule rule) {

		Map<Integer, AtomicInteger> map = ruleCountMap.get(activity.id);
		if (map == null) {
			map = new HashMap<Integer, AtomicInteger>();
			ruleCountMap.put(activity.id, map);
		}

		AtomicInteger count = map.get(rule.ruleId);
		if (count == null) {
			count = new AtomicInteger();
			map.put(rule.ruleId, count);
		}

		if (count.get() < rule.maxTimeForWorld) {
			count.incrementAndGet();
			save();
			SynAllOnlineRoleStatusTaskForExciting.instance.isDirty.set(true);
			return true;
		}
		return false;
	}
}
