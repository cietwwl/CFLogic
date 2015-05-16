package com.kola.kmp.logic.activity.transport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.koala.game.exception.KGameServerException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.role.RoleExtCABaseImpl.IExtCADataStatusProxy;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.activity.goldact.KGoldActivityManager;
import com.kola.kmp.logic.activity.transport.KTransporter.InterceptHistory;
import com.kola.kmp.logic.level.KLevelReward.NormalItemRewardTemplate;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.support.KSupportFactory;

public class KTransportData {

	private final static String JSON_KEY_TRANSPORT_COUNT = "0";
	private final static String JSON_KEY_INTERCEPT_COUNT = "1";
	private final static String JSON_KEY_IS_TRANSPORT = "2";
	private final static String JSON_KEY_CHECK_TIME = "3";
	private final static String JSON_KEY_LAST_INTERCEPT_TIME = "4";
	private final static String JSON_KEY_REFLASH_COUNT = "5";
	private final static String JSON_KEY_NOW_CARRIER_ID = "6";

	private long roleId;
	// 已经运输次数
	private AtomicInteger transportCount;
	// 已拦截次数
	private AtomicInteger interceptCount;
	// 检测时间
	public long checkTime;
	// 上次拦截时间
	public long lastInterceptTime;

	// private KActivityRoleExtData extData;
	private IExtCADataStatusProxy proxy;
	// 载具刷新权重表
	private Map<Integer, Integer> reflashCarrierWeightMap = new HashMap<Integer, Integer>();
	// 载具刷新次数
	private int reflashCount = 0;
	// 载具刷新总权重值
	private int totalWeight = 0;
	// 当前角色载具ID
	private int nowCarrierId;
	// 拦截历史
	private Queue<InterceptHistory> interceptInfoQueue = new ConcurrentLinkedQueue<InterceptHistory>();

	// public List<Long> landTransportorList;
	// public List<Long> waterTransportorList;
	// public long listCheckTime;

	public KTransportData(long pRoleId, IExtCADataStatusProxy proxy) {
		this.transportCount = new AtomicInteger(0);
		this.interceptCount = new AtomicInteger(0);
		this.checkTime = System.currentTimeMillis();
		this.proxy = proxy;
		this.roleId = pRoleId;
		initReflashCarrierWeightMap();
		if (KTransportManager.isRoleTransporting(roleId)) {
			if (KTransportManager.getTransporter(roleId).getCarrier() != null) {
				nowCarrierId = KTransportManager.getTransporter(roleId)
						.getCarrier().getCarrierId();
			}
		}
	}

	public void decode(String dbData) throws Exception {
		if (dbData != null && dbData.length() > 0) {
			JSONObject obj = new JSONObject(dbData);
			this.transportCount = new AtomicInteger(obj.optInt(
					JSON_KEY_TRANSPORT_COUNT, 0));
			this.interceptCount = new AtomicInteger(obj.optInt(
					JSON_KEY_INTERCEPT_COUNT, 0));
			this.lastInterceptTime = obj.optLong(JSON_KEY_LAST_INTERCEPT_TIME,
					0);
			this.checkTime = obj.optLong(JSON_KEY_CHECK_TIME,
					System.currentTimeMillis());
			this.reflashCount = obj.optInt(JSON_KEY_REFLASH_COUNT, 0);
			this.nowCarrierId = obj.optInt(JSON_KEY_NOW_CARRIER_ID, 0);
		}
	}

	public String encode() throws Exception {
		JSONObject obj = new JSONObject();
		obj.put(JSON_KEY_TRANSPORT_COUNT, transportCount.get());
		obj.put(JSON_KEY_INTERCEPT_COUNT, interceptCount.get());
		obj.put(JSON_KEY_LAST_INTERCEPT_TIME, lastInterceptTime);
		obj.put(JSON_KEY_CHECK_TIME, checkTime);
		obj.put(JSON_KEY_REFLASH_COUNT, reflashCount);
		obj.put(JSON_KEY_NOW_CARRIER_ID, nowCarrierId);
		return obj.toString();
	}

	public int getTransportCount() {
		return transportCount.get();
	}

	/**
	 * 获取剩余可运输次数
	 * 
	 * @return
	 */
	public int getCanTransportRestCount() {
		int count = KTransportManager.max_can_transport_count
				- transportCount.get();
		if (count < 0) {
			count = 0;
		}
		return count;
	}

	public int getInterceptCount() {
		return interceptCount.get();
	}

	/**
	 * 获取剩余可拦截次数
	 * 
	 * @return
	 */
	public int getCanInterceptRestCount() {
		int count = KTransportManager.max_can_challenge_count
				- interceptCount.get();
		if (count < 0) {
			count = 0;
		}
		return count;
	}

	public long getCheckTime() {
		return checkTime;
	}

	/**
	 * 记录拦截信息
	 */
	public void intercept() {
		this.lastInterceptTime = System.currentTimeMillis();
		interceptCount.incrementAndGet();
		// extData.notifyDB();
		proxy.notifyUpdate();
	}

	/**
	 * 清除冷却时间
	 */
	public void clearInterceptCoolTime() {
		this.lastInterceptTime = 0;
		proxy.notifyUpdate();
	}

	/**
	 * 获取拦截冷却时间
	 * 
	 * @return
	 */
	public int getInterceptCoolTimeSeconds() {
		long nowTime = System.currentTimeMillis();
		if (nowTime >= this.lastInterceptTime
				+ KTransportManager.intercept_cool_time_seconds * 1000) {
			return 0;
		} else {
			return (int) (((this.lastInterceptTime + KTransportManager.intercept_cool_time_seconds * 1000) - nowTime) / 1000);
		}
	}

	public int getReflashCount() {
		return reflashCount;
	}

	public int getNowCarrierId() {
		return nowCarrierId;
	}

	public Map<Integer, Integer> getReflashCarrierWeightMap() {
		return reflashCarrierWeightMap;
	}

	/**
	 * 初始化载具权重表
	 */
	private void initReflashCarrierWeightMap() {
		for (Integer carrierId : KTransportManager.getCarrierDataMap().keySet()) {
			CarrierData carrier = KTransportManager.getCarrierDataMap().get(
					carrierId);
			reflashCarrierWeightMap.put(carrierId, carrier.getBaseWeight());
			totalWeight += carrier.getBaseWeight();
		}
		if (this.nowCarrierId == 0) {
			this.nowCarrierId = KTransportManager.defaultCarrierData
					.getCarrierId();
		}
	}

	/**
	 * 记录运输信息
	 * 
	 * @param carrierId
	 */
	public void transport(int carrierId) {
		this.transportCount.incrementAndGet();
		this.nowCarrierId = carrierId;
		initReflashCarrierWeightMap();
		// extData.notifyDB();
		proxy.notifyUpdate();
	}

	/**
	 * 完成运输，重新刷新载具
	 */
	public void finishTransport() {
		reflashCarrierWeightMap.clear();
		this.totalWeight = 0;
		for (CarrierData carrier : KTransportManager.getCarrierDataMap()
				.values()) {
			reflashCarrierWeightMap.put(carrier.getCarrierId(),
					carrier.getBaseWeight());
			this.totalWeight += carrier.getBaseWeight();
		}

		reflashCarrier(false);
		// extData.notifyDB();
		proxy.notifyUpdate();
	}

	/**
	 * 刷新载具
	 * 
	 * @param isAddWeight
	 *            表示是否增加权重
	 * @return
	 */
	public CarrierData reflashCarrier(boolean isAddWeight) {
		if (isAddWeight) {
			this.reflashCount++;
			this.totalWeight = 0;
			int nowCarrierId = this.nowCarrierId;
			for (Integer carrierId : KTransportManager.getCarrierDataMap()
					.keySet()) {
				CarrierData carrier = KTransportManager.getCarrierDataMap()
						.get(carrierId);
				int weight;
				if (carrierId >= nowCarrierId) {
					if ((reflashCarrierWeightMap.containsKey(carrierId))) {
						weight = reflashCarrierWeightMap.get(carrierId)
								+ carrier.getReflashUpWieghtCount();
						if (weight > carrier.getMaxWeight()) {
							weight = carrier.getMaxWeight();
						}
					} else {
						weight = carrier.getBaseWeight()
								+ carrier.getReflashUpWieghtCount();
					}
				} else {
					weight = 0;
				}
				reflashCarrierWeightMap.put(carrierId, weight);
				this.totalWeight += weight;
			}
		}

		// if (reflashCarrierWeightMap.size() == 0) {
		// this.totalWeight = 0;
		// for (CarrierData carrier : KTransportManager.getCarrierDataMap()
		// .values()) {
		// reflashCarrierWeightMap.put(carrier.getCarrierId(),
		// carrier.getBaseWeight());
		// this.totalWeight += carrier.getBaseWeight();
		// }
		// if (this.nowCarrierId == 0) {
		// this.nowCarrierId = KTransportManager.defaultCarrierData
		// .getCarrierId();
		// }
		// return KTransportManager.getCarrierDataMap().get(this.nowCarrierId);
		// }

		int weight = UtilTool.random(0, this.totalWeight);
		this.nowCarrierId = 0;
		int tempRate = 0;
		for (Integer carrierId : reflashCarrierWeightMap.keySet()) {
			int carrierWeight = reflashCarrierWeightMap.get(carrierId);
			if (tempRate < weight && weight <= (tempRate + carrierWeight)) {
				this.nowCarrierId = carrierId;
				break;
			} else {
				tempRate += carrierWeight;
			}
		}
		if (this.nowCarrierId == 0) {
			this.nowCarrierId = KTransportManager.defaultCarrierData
					.getCarrierId();
		}
		proxy.notifyUpdate();
		return KTransportManager.getCarrierDataMap().get(this.nowCarrierId);
	}

	public boolean checkAndRestData(boolean isNeedCheck) {
		boolean isDataChanged = false;
		if (!isNeedCheck
				|| (isNeedCheck && UtilTool
						.checkNowTimeIsArriveTomorrow(this.checkTime))) {
			this.checkTime = System.currentTimeMillis();
			this.reflashCount = 0;
			this.transportCount.set(0);
			this.interceptCount.set(0);
			this.lastInterceptTime = 0;
			isDataChanged = true;
		}

		if (isDataChanged) {
			proxy.notifyUpdate();
		}

		return isDataChanged;
	}

	public void addInterceptInfoQueue(InterceptHistory info) {
		while (this.interceptInfoQueue.size() > 10) {
			this.interceptInfoQueue.poll();
		}
		this.interceptInfoQueue.add(info);
	}

	public List<InterceptHistory> getInterceptInfoList() {
		List<InterceptHistory> list = new ArrayList<InterceptHistory>();
		list.addAll(this.interceptInfoQueue);
		return list;
	}
}
