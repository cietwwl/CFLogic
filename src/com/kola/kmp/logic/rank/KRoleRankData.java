package com.kola.kmp.logic.rank;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.role.RoleExtCABaseImpl;
import com.kola.kgame.cache.util.UtilTool;

/**
 * <pre>
 * 角色的排行榜数据
 * 
 * 
 * @author CamusHuang
 * @creation 2014-2-13 下午4:53:22
 * </pre>
 */
public final class KRoleRankData extends RoleExtCABaseImpl {

	public static final Logger _LOGGER = KGameLogger.getLogger(KRoleRankData.class);

	// 读写锁
	public final ReentrantLock rwLock = new ReentrantLock();

	// -------------------------逻辑层数据项
	/** 今日的日期时间 */
	private long dataTime;

	private int good;// 点赞数量

	// 给别人点赞的次数 <类型,次数>
	private Map<Integer, Integer> doGoodTimes = new HashMap<Integer, Integer>(2);

	// /////////////////////////////////
	static final String JSON_NULL = "NULL";// null
	static final String JSON_VER = "A";// 版本
	static final String JSON_DAY = "Z";// 当前数据对应的日期
	//
	static final String JSON_BASEINFO = "B";// 基础信息
	static final String JSON_BASEINFO_GOOD = "1";
	static final String JSON_BASEINFO_GOODTIMD = "3";

	/**
	 * <pre>
	 * 逻辑新建对象时使用
	 * 
	 * @param dbdata
	 * @author CamusHuang
	 * @creation 2014-2-13 下午5:04:25
	 * </pre>
	 */
	protected KRoleRankData(long _roleId, int _type, boolean isFirstNew) {
		super(_roleId, _type);
		if (isFirstNew) {
			dataTime = System.currentTimeMillis();
		}
	}

	@Override
	protected void decode(String jsonCA) {
		// 由底层调用,解释出逻辑层数据
		try {
			JSONObject obj = new JSONObject(jsonCA);
			int ver = obj.getInt(JSON_VER);// 默认版本
			dataTime = obj.optLong(JSON_DAY) * Timer.ONE_HOUR;
			// CEND 暂时只有版本0
			switch (ver) {
			case 0:
				decodeBaseInfo(obj.getJSONObject(JSON_BASEINFO));
				break;
			}

		} catch (Exception ex) {
			_LOGGER.error("decode数据时发生错误 roleId=" + getRoleId() + " ----丢失数据，存在运行隐患！", ex);
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
		this.good = obj.getInt(JSON_BASEINFO_GOOD);
		{
			JSONObject temp = obj.optJSONObject(JSON_BASEINFO_GOODTIMD);
			if (temp != null) {
				for (Iterator<String> it = temp.keys(); it.hasNext();) {
					String key = it.next();
					this.doGoodTimes.put(Integer.parseInt(key), temp.getInt(key));
				}
			}
		}
	}

	@Override
	protected String encode() {
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
			obj.put(JSON_DAY, dataTime / Timer.ONE_HOUR);
			// CEND 暂时只有版本0
			obj.put(JSON_BASEINFO, encodeBaseInfo());// 基础信息
			return obj.toString();
		} catch (Exception ex) {
			_LOGGER.error("encode数据时发生错误 roleId=" + getRoleId() + " ----丢失数据！", ex);
			return "";
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 基础信息打包
	 * 
	 * @return
	 * @throws JSONException
	 * @author CamusHuang
	 * @creation 2013-1-11 下午12:29:08
	 * </pre>
	 */
	private JSONObject encodeBaseInfo() throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put(JSON_BASEINFO_GOOD, good);
		{
			if (!doGoodTimes.isEmpty()) {
				JSONObject temp = new JSONObject();
				obj.put(JSON_BASEINFO_GOODTIMD, temp);
				for (Entry<Integer, Integer> entry : doGoodTimes.entrySet()) {
					temp.put(entry.getKey() + "", entry.getValue());
				}
			}
		}
		return obj;
	}

	public int getGood() {
		return good;
	}

	void setGood(int good) {
		this.good = good;
		notifyUpdate();
	}

	public int getGoodDoTime(int goodType) {
		Integer time = doGoodTimes.get(goodType);
		return time == null ? 0 : time;
	}

	public void recordGoodDoTime(int goodType) {
		doGoodTimes.put(goodType, getGoodDoTime(goodType) + 1);
		notifyUpdate();
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
	boolean notifyForLogin(long nowTime) {
		rwLock.lock();
		try {
			if (UtilTool.isBetweenDay(dataTime, nowTime)) {
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
	 * @param newDayStartTime
	 * @author CamusHuang
	 * @creation 2014-4-10 上午10:25:35
	 * </pre>
	 */
	void notifyForDayChange(long nowTime) {
		rwLock.lock();
		try {
			dataTime = nowTime;
			doGoodTimes.clear();
			notifyUpdate();
		} finally {
			rwLock.unlock();
		}
	}
}
