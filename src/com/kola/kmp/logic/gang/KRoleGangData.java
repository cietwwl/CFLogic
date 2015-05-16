package com.kola.kmp.logic.gang;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.google.code.hs4j.network.util.ConcurrentHashSet;
import com.koala.game.logging.KGameLogger;
import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.role.RoleExtCABaseImpl;
import com.kola.kgame.cache.util.UtilTool;

/**
 * <pre>
 * 角色的军团数据
 * 本类对象的含义是：管理角色独立于军团之外的数据
 * 此对象区别于{@link KGangMember}
 * 
 * 
 * @author CamusHuang
 * @creation 2014-2-13 下午4:53:22
 * </pre>
 */
public final class KRoleGangData extends RoleExtCABaseImpl {

	public static final Logger _LOGGER = KGameLogger.getLogger(KRoleGangData.class);

	// 读写锁
	public final ReentrantLock rwLock = new ReentrantLock();

	// -------------------------逻辑层数据项
	// 角色离开军团时，捐献次数从{@link KGangMember}迁移到{@link KRoleGangData}
	// 角色进入军团时，捐献次数从{@link KRoleGangData}迁移到{@link KGangMember}
	private long contributionTime;// 备份捐献数据对应的日期
	private Map<Integer, Integer> contributionData = new HashMap<Integer, Integer>();// 备份捐献次数

	// 城市征收数据对应的时间
	private long levyCityTime;
	private Set<Integer> levyCityData = new ConcurrentHashSet<Integer>();// 今天征收过的城市

	// /////////////////////////////////
	static final String JSON_NULL = "NULL";// null
	static final String JSON_VER = "A";// 版本
	//
	static final String JSON_CONTRIBUTION_TIME = "B";// 捐献次数记录时间
	static final String JSON_CONTRIBUTION_DATA = "C";// 捐献次数
	static final String JSON_LAST_LEVEY_CITY_TIME = "D";// 角色上次领取征收奖励时间

	/**
	 * <pre>
	 * 逻辑新建对象时使用
	 * 
	 * @param dbdata
	 * @author CamusHuang
	 * @creation 2014-2-13 下午5:04:25
	 * </pre>
	 */
	protected KRoleGangData(long _roleId, int _type) {
		super(_roleId, _type);
	}

	@Override
	protected void decode(String jsonCA) {
		// 由底层调用,解释出逻辑层数据
		try {
			JSONObject obj = new JSONObject(jsonCA);
			int ver = obj.getInt(JSON_VER);// 默认版本
			// CEND 暂时只有版本0
			switch (ver) {
			case 0:
				contributionTime = obj.getLong(JSON_CONTRIBUTION_TIME) * Timer.ONE_HOUR;
				decodeContributionData(obj.getJSONObject(JSON_CONTRIBUTION_DATA));
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
	private void decodeContributionData(JSONObject obj) throws JSONException {
		for (Iterator<String> it = obj.keys(); it.hasNext();) {
			String key = it.next();
			int value = obj.getInt(key);
			contributionData.put(Integer.parseInt(key), value);
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
			// CEND 暂时只有版本0
			obj.put(JSON_CONTRIBUTION_TIME, contributionTime / Timer.ONE_HOUR);
			obj.put(JSON_CONTRIBUTION_DATA, encodeContributionData());
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
	private JSONObject encodeContributionData() throws JSONException {
		JSONObject obj = new JSONObject();
		for (Entry<Integer, Integer> entry : contributionData.entrySet()) {
			obj.put(entry.getKey() + "", entry.getValue());
		}
		return obj;
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
			if (UtilTool.isBetweenDay(levyCityTime, nowTime)) {
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
			levyCityTime = nowTime;
			if (!levyCityData.isEmpty()) {
				levyCityData.clear();
				notifyUpdate();
			}
		} finally {
			rwLock.unlock();
		}
	}

	void setContributionData(Map<Integer, Integer> contributionData) {
		rwLock.lock();
		try {
			this.contributionTime = System.currentTimeMillis();
			this.contributionData.clear();
			this.contributionData.putAll(contributionData);
			notifyUpdate();
		} finally {
			rwLock.unlock();
		}
	}
	
	/**
	 * <pre>
	 * 剩余多长毫秒可以加入军团
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-10-23 上午10:47:54
	 * </pre>
	 */
	long getReleaseJoinGangTime(){
		long nowTime = System.currentTimeMillis();
		long nextJoinGangTime = contributionTime + KGangConfig.getInstance().JoinGangCDHour * Timer.ONE_HOUR;
		if(nextJoinGangTime <= nowTime){
			return  0 ;
		}
		return nextJoinGangTime - nowTime;
	}

	Map<Integer, Integer> getContributionDataCache() {
		rwLock.lock();
		try {
			return contributionData;
		} finally {
			rwLock.unlock();
		}
	}

	long getContributionTime() {
		rwLock.lock();
		try {
			return contributionTime;
		} finally {
			rwLock.unlock();
		}
	}

	public boolean isLevyedCity(int cityId) {
		return levyCityData.contains(cityId);
	}

	public void recordLevyCity(int cityId) {
		rwLock.lock();
		try {
			levyCityData.add(cityId);
			notifyUpdate();
		} finally {
			rwLock.unlock();
		}
	}

}
