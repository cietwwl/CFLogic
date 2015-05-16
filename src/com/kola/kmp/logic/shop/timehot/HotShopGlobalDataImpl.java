package com.kola.kmp.logic.shop.timehot;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kmp.logic.other.KGameExtDataDBTypeEnum;
import com.kola.kmp.logic.util.CamusGlobalDataAbs;

/**
 * <pre>
 * 限时热购全局数据
 * 
 * @author CamusHuang
 * @creation 2014-12-31 上午11:21:07
 * </pre>
 */
class HotShopGlobalDataImpl extends CamusGlobalDataAbs{
	static final HotShopGlobalDataImpl instance = new HotShopGlobalDataImpl();

	// -----------
	// 商品全局限量
	private Map<Integer, AtomicInteger> map = new HashMap<Integer, AtomicInteger>();
	//
	private final static String JSON_COUNT = "A";

	private HotShopGlobalDataImpl() {
		super(KGameExtDataDBTypeEnum.限时热购数据);
	}

	protected void decode(JSONObject json) throws JSONException{
		JSONObject temp = json.optJSONObject(JSON_COUNT);
		if (temp != null) {
			for (Iterator<String> it = temp.keys(); it.hasNext();) {
				String key = it.next();
				int count = temp.getInt(key);
				map.put(Integer.parseInt(key), new AtomicInteger(count));
			}
		}
	}

	protected JSONObject encode() throws JSONException {
		JSONObject json = new JSONObject();
		{
			JSONObject temp = new JSONObject();
			json.put(JSON_COUNT, temp);
			for (Entry<Integer, AtomicInteger> e : map.entrySet()) {
				temp.put(e.getKey()+"", e.getValue().get());
			}
		}
		return json;
	}

	synchronized int getCount(int goodsId) {
		AtomicInteger count = map.get(goodsId);
		return count == null ? 0 : count.get();
	}
	
	synchronized void increaseCount(int goodsId) {
		AtomicInteger count = map.get(goodsId);
		if (count == null) {
			count = new AtomicInteger();
			map.put(goodsId, count);
		}
		count.incrementAndGet();
		//
		save();
	}
}	
