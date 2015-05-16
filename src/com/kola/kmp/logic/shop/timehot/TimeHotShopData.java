package com.kola.kmp.logic.shop.timehot;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.koala.thirdpart.json.JSONArray;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.shop.KRoleShop;

/**
 * <pre>
 * 限时热购商店相关数据
 * 
 * @author CamusHuang
 * @creation 2014-11-10 下午4:30:56
 * </pre>
 */
public class TimeHotShopData {
	public final KRoleShop owner;
	// 读写锁
	public final ReentrantLock rwLock;

	private long dataTime = System.currentTimeMillis();// 当前数据对应的日期

	// <商店类型,<商品ID, 已购买次数>>
	private Map<KHotShopTypeEnum, Map<Integer, AtomicInteger>> goodsBuyTimesMap = new HashMap<KHotShopTypeEnum, Map<Integer, AtomicInteger>>();
	//
	static final String JSON_DATE = "Z";// 数据对应的时间
	// static final String JSON_LIMITIME_BUYTIMES = "1";// 购买次数
	// static final String JSON_LIMITIME_ACTIVITY_NAME = "2";// 活动名称
	// static final String JSON_LIMITIME_GOODSLIST = "3";// 商品列表
	static final String JSON_LIMITIME_BUYTIMES_V2 = "4";// 购买次数

	public TimeHotShopData(KRoleShop owner) {
		this.owner = owner;
		this.rwLock = owner.rwLock;
	}

	public void decode(JSONObject json) throws Exception {
		if (json == null) {
			return;
		}

		dataTime = json.optLong(JSON_DATE, dataTime);
		//
		{
			JSONObject tempJson = json.optJSONObject(JSON_LIMITIME_BUYTIMES_V2);
			if (tempJson != null) {
				for (KHotShopTypeEnum type : KHotShopTypeEnum.values()) {
					JSONObject timeJson = tempJson.optJSONObject(type.sign + "");
					if (timeJson != null) {
						Map<Integer, AtomicInteger> map = new HashMap<Integer, AtomicInteger>();
						goodsBuyTimesMap.put(type, map);
						for (Iterator<String> it = timeJson.keys(); it.hasNext();) {
							String key = it.next();
							int time = timeJson.getInt(key);
							map.put(Integer.parseInt(key), new AtomicInteger(time));
						}
					}
				}
			}
		}
	}

	public JSONObject encode() throws Exception {
		JSONObject json = new JSONObject();
		//
		json.put(JSON_DATE, dataTime);
		//
		{
			JSONObject tempJson = new JSONObject();
			json.put(JSON_LIMITIME_BUYTIMES_V2, tempJson);
			for (Entry<KHotShopTypeEnum, Map<Integer, AtomicInteger>> e : goodsBuyTimesMap.entrySet()) {
				JSONObject timeJson = new JSONObject();
				tempJson.put(e.getKey().sign + "", timeJson);
				//
				for (Entry<Integer, AtomicInteger> timeE : e.getValue().entrySet()) {
					timeJson.put(timeE.getKey() + "", timeE.getValue().get());
				}
			}
		}
		return json;
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 直接获取缓存，谨慎使用
	 * @return <商品ID, 当天已购买次数>
	 * @author CamusHuang
	 * @creation 2014-4-17 上午11:55:12
	 * </pre>
	 */
	Map<KHotShopTypeEnum, Map<Integer, AtomicInteger>> getGoodsBuyTimesCache() {
		return goodsBuyTimesMap;
	}

	Map<Integer, AtomicInteger> getGoodsBuyTimesCache(KHotShopTypeEnum type) {
		Map<Integer, AtomicInteger> result = goodsBuyTimesMap.get(type);
		if (result == null) {
			return Collections.emptyMap();
		}
		return result;
	}

	int getGoodsBuyedTime(KHotShopTypeEnum type, int goodsID) {
		owner.rwLock.lock();
		try {
			AtomicInteger count = getGoodsBuyTimesCache(type).get(goodsID);
			if (count == null) {
				return 0;
			}
			return count.get();
		} finally {
			owner.rwLock.unlock();
		}
	}

	void notifyBuyedGoods(KHotShopTypeEnum type, int goodsID) {
		owner.rwLock.lock();
		try {
			Map<Integer, AtomicInteger> result = goodsBuyTimesMap.get(type);
			if (result == null) {
				result = new HashMap<Integer, AtomicInteger>();
				goodsBuyTimesMap.put(type, result);
			}
			
			AtomicInteger count = result.get(goodsID);
			if (count == null) {
				count = new AtomicInteger();
				result.put(goodsID, count);
			}
			count.incrementAndGet();
			owner.notifyUpdate();
		} finally {
			owner.rwLock.unlock();
		}
	}

	boolean clearData() {
		// 清理次数
		if (!goodsBuyTimesMap.isEmpty()) {
			goodsBuyTimesMap.clear();
			owner.notifyUpdate();
			return true;
		}
		return false;
	}

	/**
	 * <pre>
	 * 是否跨天
	 * 
	 * @deprecated 
	 * @param newDayStartTime
	 * @author CamusHuang
	 * @creation 2014-4-10 上午10:25:35
	 * </pre>
	 */
	boolean isAnotherDay() {
		rwLock.lock();
		try {
			long nowTime = System.currentTimeMillis();
			if (UtilTool.isBetweenDay(dataTime, nowTime)) {
				dataTime = nowTime;
				owner.notifyUpdate();
				return true;
			}
			return false;
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
	void notifyForDayChange(long nowTime) {
		rwLock.lock();
		try {
			dataTime = nowTime;
			owner.notifyUpdate();
		} finally {
			rwLock.unlock();
		}
	}
}
