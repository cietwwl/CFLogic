package com.kola.kmp.logic.activity.mineral;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.game.util.StringUtil;
import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.role.RoleExtCABaseImpl.IExtCADataStatusProxy;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.item.KItem;
import com.kola.kmp.logic.item.KItemDataManager;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMapBornPoint;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.util.tips.ActivityTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.ShopTips;

public class KRoleDiggerData {

	public static final Logger _LOGGER = KGameLogger.getLogger(KRoleDiggerData.class);

	public final long _roleId;
	private IExtCADataStatusProxy _proxy;

	public final ReentrantLock rwLock = new ReentrantLock();

	// 个性签名
	private String declare = "";
	//
	private int mineId = -1;// 当前正在作业的矿区ID：-1表示停止作业
	// 需要向谁复仇:-1表示不需要复仇
	private RevengeData revengeData = new RevengeData();

	public final AtomicBoolean isInMap = new AtomicBoolean();
	public final AtomicReference<KDuplicateMapBornPoint> digPoint = new AtomicReference<KDuplicateMapBornPoint>();
	public final AtomicBoolean isInWar = new AtomicBoolean();
	public long nextProduceTime;// 下次产出的时间点

	// 日志
	private LinkedList<String> dialys = new LinkedList<String>();
	// 离线数据统计，上线邮件，并清空
	private AtomicLong offline_gold = new AtomicLong();
	private Map<String, AtomicLong> offline_item = new HashMap<String, AtomicLong>();
	private AtomicInteger offlineCount = new AtomicInteger();// 离线执行的次数
	// 当天的奖励统计，跨天清空
	private long todayTime = System.currentTimeMillis();
	private AtomicLong today_gold = new AtomicLong();
	private Map<String, AtomicLong> today_item = new HashMap<String, AtomicLong>();
	private AtomicInteger todayCount = new AtomicInteger();// 今天执行的次数

	// /////////////////////////////////
	static final String JSON_VER = "A";// 版本
	//
	static final String JSON_BASEINFO = "B";// 基础信息
	// static final String JSON_BASEINFO_MINEID = "1";
	static final String JSON_BASEINFO_REVENGEROLE = "2";
	static final String JSON_BASEINFO_REVENGEMINE = "3";
	static final String JSON_BASEINFO_DECLARE = "3";
	static final String JSON_OFFLINE = "C";
	static final String JSON_OFFLINE_COUNT = "1";
	static final String JSON_OFFLINE_GOLD = "2";
	static final String JSON_OFFLINE_ITEM = "3";
	static final String JSON_TODAY = "D";
	static final String JSON_TODAY_COUNT = "1";
	static final String JSON_TODAY_GOLD = "2";
	static final String JSON_TODAY_ITEM = "3";
	static final String JSON_TODAY_TIME = "4";

	boolean hashTool() {
		KItem item = KItemLogic.searchItemFromBag(_roleId, KDigMineralDataManager.铁镐ItemCode);
		return item != null && item.getCount() > 0;
	}

	public KRoleDiggerData(long roleId, IExtCADataStatusProxy proxy) {
		this._roleId = roleId;
		this._proxy = proxy;
	}

	public String save() {
		boolean isLock = false;
		try {
			isLock = rwLock.tryLock(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			_LOGGER.error(e.getMessage(), e);
		}
		if (!isLock) {
			return null;
		}
		// 构造一个数据对象给底层
		try {
			JSONObject obj = new JSONObject();
			obj.put(JSON_VER, 0);
			// CEND 暂时只有版本0
			obj.put(JSON_BASEINFO, encodeBaseInfo());// 基础信息
			obj.put(JSON_OFFLINE, encodeOffline());
			obj.put(JSON_TODAY, encodeToday());

			return obj.toString();
		} catch (Exception ex) {
			_LOGGER.error("encode数据时发生错误 ----丢失数据！", ex);
			return "";
		} finally {
			rwLock.unlock();
		}
	}

	public void parse(String data) {
		// 由底层调用,解释出逻辑层数据
		try {
			JSONObject obj = new JSONObject(data);
			int ver = obj.getInt(JSON_VER);// 默认版本
			// CEND 暂时只有版本0
			switch (ver) {
			case 0:
				decodeBaseInfo(obj.getJSONObject(JSON_BASEINFO));
				decodeOffline(obj.getJSONObject(JSON_OFFLINE));
				decodeToday(obj.getJSONObject(JSON_TODAY));
				break;
			}

		} catch (Exception ex) {
			_LOGGER.error("decode数据时发生错误  ----丢失数据，存在运行隐患！", ex);
		}
	}

	/**
	 * <pre>
	 * 基础信息解码
	 * 
	 * @throws JSONException
	 * @author CamusHuang
	 * @creation 2013-1-12 下午3:30:24
	 * </pre>
	 */
	private void decodeBaseInfo(JSONObject obj) throws JSONException {
		// mineId = obj.optInt(JSON_BASEINFO_MINEID,-1);
		revengeData.revengeRoleId = obj.optInt(JSON_BASEINFO_REVENGEROLE, -1);
		revengeData.revengeMineId = obj.optInt(JSON_BASEINFO_REVENGEMINE, -1);
		declare = obj.optString(JSON_BASEINFO_DECLARE, "");
	}

	private void decodeOffline(JSONObject obj) throws JSONException {

		offlineCount.set(obj.optInt(JSON_OFFLINE_COUNT, 0));
		offline_gold.set(obj.optLong(JSON_OFFLINE_GOLD, 0));

		obj = obj.optJSONObject(JSON_OFFLINE_ITEM);
		if (obj != null) {
			for (Iterator<String> it = obj.keys(); it.hasNext();) {
				String itemCode = it.next();
				long count = obj.getLong(itemCode);
				offline_item.put(itemCode, new AtomicLong(count));
			}
		}
	}

	private void decodeToday(JSONObject obj) throws JSONException {
		todayTime = obj.optLong(JSON_TODAY_TIME) * Timer.ONE_MINUTE;
		todayCount.set(obj.optInt(JSON_TODAY_COUNT, 0));
		today_gold.set(obj.optLong(JSON_TODAY_GOLD, 0));

		obj = obj.optJSONObject(JSON_TODAY_ITEM);
		if (obj != null) {
			for (Iterator<String> it = obj.keys(); it.hasNext();) {
				String itemCode = it.next();
				long count = obj.getLong(itemCode);
				today_item.put(itemCode, new AtomicLong(count));
			}
		}
	}

	/**
	 * <pre>
	 * 基础信息解码
	 * 
	 * @throws JSONException
	 * @author CamusHuang
	 * @creation 2013-1-12 下午3:30:24
	 * </pre>
	 */
	private JSONObject encodeBaseInfo() throws JSONException {
		JSONObject obj = new JSONObject();
		// mineId = obj.optInt(JSON_BASEINFO_MINEID,-1);
		obj.put(JSON_BASEINFO_REVENGEROLE, revengeData.revengeRoleId);
		obj.put(JSON_BASEINFO_REVENGEMINE, revengeData.revengeMineId);
		obj.put(JSON_BASEINFO_DECLARE, declare);
		return obj;
	}

	private JSONObject encodeOffline() throws JSONException {
		JSONObject obj = new JSONObject();

		obj.put(JSON_OFFLINE_COUNT, offlineCount.get());
		obj.put(JSON_OFFLINE_GOLD, offline_gold.get());

		JSONObject obj2 = new JSONObject();
		obj.put(JSON_OFFLINE_ITEM, obj2);
		if (!offline_item.isEmpty()) {
			for (Entry<String, AtomicLong> e : offline_item.entrySet()) {
				obj2.put(e.getKey(), e.getValue().get());
			}
		}
		return obj;
	}

	private JSONObject encodeToday() throws JSONException {
		JSONObject obj = new JSONObject();

		obj.put(JSON_TODAY_TIME, todayTime / Timer.ONE_MINUTE);
		obj.put(JSON_TODAY_COUNT, todayCount.get());
		obj.put(JSON_TODAY_GOLD, today_gold.get());

		JSONObject obj2 = new JSONObject();
		obj.put(JSON_TODAY_ITEM, obj2);
		if (!today_item.isEmpty()) {
			for (Entry<String, AtomicLong> e : today_item.entrySet()) {
				obj2.put(e.getKey(), e.getValue().get());
			}
		}
		return obj;
	}

	public void setDeclare(String declare) {
		this.declare = declare;
		_proxy.notifyUpdate();// 保存
	}

	public String getDeclare() {
		return this.declare;
	}

	public void setMineData(int mineId) {
		if (this.mineId != mineId) {
			this.mineId = mineId;
			_proxy.notifyUpdate();// 保存

			if (this.mineId > 0) { // 角色在副本中的坐标
				nextProduceTime = System.currentTimeMillis() + KDigMineralDataManager.产出周期;
				KDuplicateMapBornPoint orgPoint = KDigMineralActivityManager.getDigPoint(_roleId);
				if (orgPoint != null) {
					digPoint.set(orgPoint);
				}
			}
		}
	}

	public KDuplicateMapBornPoint getDigPoint() {
		KDuplicateMapBornPoint orgPoint = digPoint.get();
		if (orgPoint == null) {
			orgPoint = KDigMineralDataManager.mineBornPoint;
		}
		return orgPoint;
	}

	void addDialy(String dialy) {
		dialys.add(dialy);
		if (dialys.size() > KDigMineralDataManager.奖励日志最大数量) {
			dialys.removeFirst();
		}
	}

	/**
	 * <pre>
	 * 
	 * @deprecated
	 * @return
	 * @author CamusHuang
	 * @creation 2014-12-8 下午8:39:15
	 * </pre>
	 */
	public List<String> getDialyCache() {
		return dialys;
	}

	public int getMineId() {
		return mineId;
	}

	public void setRevengeRoleId(long revengeRoleId, int mineId) {
		if (revengeData.revengeMineId != mineId) {
			revengeData.revengeMineId = mineId;
			_proxy.notifyUpdate();// 保存
		}
		if (revengeData.revengeRoleId != revengeRoleId) {
			revengeData.revengeRoleId = revengeRoleId;
			_proxy.notifyUpdate();// 保存
		}
	}

	public long getRevengeRoleId() {
		return revengeData.revengeRoleId;
	}
	
	public int getRevengeMineId(){
		return revengeData.revengeMineId;
	}

	public void setBanishRoleId(long banishRoleId) {
		revengeData.banishRoleId = banishRoleId;
	}

	public long getBanishRoleId() {
		return revengeData.banishRoleId;
	}

	void recordForReward(boolean isOnline, long gold, List<ItemCountStruct> items) {
		// 今天的统计
		todayCount.incrementAndGet();
		today_gold.addAndGet(gold);
		for (ItemCountStruct item : items) {
			AtomicLong count = today_item.get(item.itemCode);
			if (count == null) {
				count = new AtomicLong();
				today_item.put(item.itemCode, count);
			}
			count.addAndGet(item.itemCount);
		}
		// 离线的统计
		if (!isOnline) {
			offlineCount.incrementAndGet();
			offline_gold.addAndGet(gold);
			for (ItemCountStruct item : items) {
				AtomicLong count = offline_item.get(item.itemCode);
				if (count == null) {
					count = new AtomicLong();
					offline_item.put(item.itemCode, count);
				}
				count.addAndGet(item.itemCount);
			}
		}

		_proxy.notifyUpdate();// 保存
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-12-9 上午9:56:33
	 * </pre>
	 */
	boolean notifyForLogin() {
		rwLock.lock();
		try {
			long nowTime = System.currentTimeMillis();
			if (UtilTool.isBetweenDay(todayTime, nowTime)) {
				notifyForDayChange(nowTime);
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
	 * @param nowTime
	 * @return 是否清理了数据，需要同步客户端
	 * @author CamusHuang
	 * @creation 2014-12-9 上午10:03:14
	 * </pre>
	 */
	boolean notifyForDayChange(long nowTime) {
		rwLock.lock();
		try {

			todayTime = nowTime;
			{
				today_gold.set(0);
				today_item.clear();
				if (todayCount.get() > 0) {
					todayCount.set(0);
					_proxy.notifyUpdate();// 保存
					return true;
				}
				return false;
			}
		} finally {
			rwLock.unlock();
		}
	}

	public String genStringForTodayReward() {
		rwLock.lock();
		try {
			int useTool = todayCount.get();
			long time = useTool * KDigMineralDataManager.产出周期;
			String timeStr = UtilTool.genReleaseCDTimeString(time);
			//
			StringBuffer sbf = new StringBuffer();
			{
				if (!today_item.isEmpty()) {
					for (Entry<String, AtomicLong> e : today_item.entrySet()) {
						long count = e.getValue().get();
						if (count > 0) {
							KItemTempAbs template = KItemDataManager.mItemTemplateManager.getItemTemplate(e.getKey());
							String temp = StringUtil.format(ShopTips.xxx, template.extItemName, count);
							sbf.append(temp).append('\n');
						}
					}
				}
				if (today_gold.get() > 0) {
					String temp = StringUtil.format(ShopTips.xxx, KCurrencyTypeEnum.GOLD.extName, today_gold.get());
					sbf.append(temp).append('\n');
				}
			}

			return StringUtil.format(ActivityTips.今天已挖矿x时间获得奖励x, timeStr, sbf.toString());
		} finally {
			rwLock.unlock();
		}
	}

	String genAndClearOfflineRewardMail() {
		rwLock.lock();
		try {
			// 挖矿时间,消耗铁镐数量,获得道具
			try {
				int useTool = offlineCount.get();
				if (useTool < 1) {
					return null;
				}

				long time = useTool * KDigMineralDataManager.产出周期;
				String timeStr = UtilTool.genReleaseCDTimeString(time);
				//
				StringBuffer sbf = new StringBuffer();
				if (!offline_item.isEmpty()) {
					for (Entry<String, AtomicLong> e : offline_item.entrySet()) {
						long count = e.getValue().get();
						if (count > 0) {
							KItemTempAbs template = KItemDataManager.mItemTemplateManager.getItemTemplate(e.getKey());
							String temp = StringUtil.format(ShopTips.xxx, template.extItemName, count);
							sbf.append(temp).append(GlobalTips.顿号);
						}
					}
				}
				if (offline_gold.get() > 0) {
					String temp = StringUtil.format(ShopTips.xxx, KCurrencyTypeEnum.GOLD.extName, offline_gold.get());
					sbf.append(temp).append(GlobalTips.顿号);
				}

				KItemTempAbs template = KItemDataManager.mItemTemplateManager.getItemTemplate(KDigMineralDataManager.铁镐ItemCode);
				String toolStr = StringUtil.format(ShopTips.xxx, template.extItemName, useTool);

				if (sbf.length() < 1) {
					return null;
				}

				// 挖矿时间,消耗铁镐数量,获得道具
				sbf.deleteCharAt(sbf.length() - 1);
				return StringUtil.format(ActivityTips.挖矿x时间消耗工具x获得了x, timeStr, toolStr, sbf.toString());
			} finally {
				// 清空
				offlineCount.set(0);
				offline_item.clear();
				offline_gold.set(0);
			}
		} finally {
			rwLock.unlock();
		}

	}

	static class RevengeData {
		long banishRoleId;// 本人上次驱赶的角色
		long revengeRoleId;// 本人需要复仇的角色
		int revengeMineId;// 本人需要复仇的角色
	}
}
