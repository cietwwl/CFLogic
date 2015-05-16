package com.kola.kmp.logic.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.KGameExtDataDBTypeEnum;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.TimeLimitActivityDataManager.TimeLimieProduceActivity;
import com.kola.kmp.logic.util.CamusGlobalDataAbs;

/**
 * <pre>
 * 物品模块全局数据
 * 目前用于圣诞礼包开启（随机宝箱线下奖励）
 * 
 * @author CamusHuang
 * @creation 2014-12-22 上午12:37:22
 * </pre>
 */
class ItemGlobalDataImpl extends CamusGlobalDataAbs {
	static final ItemGlobalDataImpl instance = new ItemGlobalDataImpl();

	// --------------
	// 圣诞礼包开启（随机宝箱）线下奖励限量
	private Map<String, AtomicInteger> map = new HashMap<String, AtomicInteger>();
	//
	private final static String JSON_MERRY = "A";

	private ItemGlobalDataImpl() {
		super(KGameExtDataDBTypeEnum.物品模块数据);
	}

	protected void decode(JSONObject json) throws JSONException{
		JSONObject temp = json.optJSONObject(JSON_MERRY);
		if (temp != null) {
			for (Iterator<String> it = temp.keys(); it.hasNext();) {
				String key = it.next();
				int count = temp.getInt(key);
				map.put(key, new AtomicInteger(count));
			}
		}
	}

	protected JSONObject encode() throws JSONException {
		JSONObject json = new JSONObject();
		{
			JSONObject temp = new JSONObject();
			json.put(JSON_MERRY, temp);
			for (Entry<String, AtomicInteger> e : map.entrySet()) {
				temp.put(e.getKey(), e.getValue().get());
			}
		}
		return json;
	}

	synchronized int getCount(String name) {
		AtomicInteger count = map.get(name);
		return count == null ? 0 : count.get();
	}

	synchronized void increaseCount(String name) {
		AtomicInteger count = map.get(name);
		if (count == null) {
			count = new AtomicInteger();
			map.put(name, count);
		}
		count.incrementAndGet();

		save();
	}

	/**
	 * 圣诞礼包开启（随机宝箱）线下奖励
	 * 全局已经开出的数量，结合限时活动中的线下奖励限量，随机选定本次的线下奖励
	 * @param dreamActivity
	 * @return null表示未开出线下奖励
	 */
	static String randomOfflineReward(TimeLimieProduceActivity dreamActivity) {
		
		if(dreamActivity.mutiMapReward.isEmpty()){
			return null;
		}
		
		if (UtilTool.random(100) < 10) {
			ArrayList<Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(dreamActivity.mutiMapReward.entrySet());
			Entry<String, Integer> e = list.get(UtilTool.random(list.size()));
			//
			int count = instance.getCount(e.getKey());
			if (count < e.getValue()) {
				instance.increaseCount(e.getKey());
				return e.getKey();
			}
		}
		return null;
	}
}
