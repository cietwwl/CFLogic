package com.kola.kmp.logic.reward;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.role.RoleExtCABaseImpl;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.reward.KRewardSonModuleAbs.KRewardSonModuleType;

/**
 * <pre>
 * 角色的奖励数据
 * 
 * @author CamusHuang
 * @creation 2014-2-13 下午4:53:22
 * </pre>
 */
public final class KRoleReward extends RoleExtCABaseImpl {
	// 0：已领取，1：可领取，2：未可领取
	public static final int REWARD_STATUS_已领取 = 0;
	public static final int REWARD_STATUS_可领取 = 1;
	public static final int REWARD_STATUS_未可领取 = 2;

	public static final Logger _LOGGER = KGameLogger.getLogger(KRoleReward.class);

	// 读写锁
	public final ReentrantLock rwLock = new ReentrantLock();
	/** 今日的日期时间 */
	private long dataTime;

	// --------------最后发送每日邮件的时间
	private long lastSendDayMailTime = 0;
	
	// --------------每日体力领取
	private long lastCollectPhyPowerTime = 0;

	// --------------维护补偿数据
	private long lastSendShutdownRewardTime = 0;
	
	// --------------卡机补偿数据
	private long lastSendKAJIRewardTime = 0;	

	// -------------------------缓存层规定的数据项
	private Map<KRewardSonModuleType, KRoleRewardSonAbs> sonMap = new HashMap<KRewardSonModuleType, KRoleRewardSonAbs>();

	// ////////////////////////////////
	static final String JSON_VER = "0";// 版本
	static final String JSON_DAY = "Z";// 当前数据对应的日期
	//
	static final String JSON_SONDATA = "A";// 所有子模块的数据
	static final String JSON_SONDATA_TYPE = "1";// 子模块的类型
	static final String JSON_SONDATA_DATA = "2";// 子模块的数据

	//
	static final String JSON_BASE_INFO = "B";// 基础数据
	static final String JSON_BASE_INFO_SEND_DAYMAIL_TIME = "1";// 每日邮件
	static final String JSON_BASE_INFO_SEND_SHUTDOWNREWARD_TIME = "2";// 维护奖励
	static final String JSON_BASE_INFO_SEND_PHYPOWER_TIME = "3";// 体力奖励
	static final String JSON_BASE_INFO_SEND_KAJI_TIME = "4";// 卡机补偿

	/**
	 * <pre>
	 * 逻辑新建对象时使用
	 * 
	 * @param dbdata
	 * @author CamusHuang
	 * @creation 2014-2-13 下午5:04:25
	 * </pre>
	 */
	protected KRoleReward(long _roleId, int _type, boolean isFirstNew) {
		super(_roleId, _type);
		if (isFirstNew) {
			dataTime = System.currentTimeMillis();
		}
		//
		for (KRewardSonModuleType type : KRewardSonModuleType.values()) {
			if (type.isSonOfReward) {
				sonMap.put(type, KRewardModuleExtension.rewardSonImplMap.get(type).newRewardSon(this, isFirstNew));
			}
		}
	}

	@Override
	protected void decode(String jsonCA) {
		// 由底层调用,解释出逻辑层数据
		try {
			JSONObject json = new JSONObject(jsonCA);
			int ver = json.getInt(JSON_VER);// 默认版本
			dataTime = json.optLong(JSON_DAY) * Timer.ONE_HOUR;
			// CEND 暂时只有版本0
			switch (ver) {
			case 0: {
				{
					JSONObject obj = json.getJSONObject(JSON_SONDATA);
					for (Entry<KRewardSonModuleType, KRoleRewardSonAbs> entry : sonMap.entrySet()) {
						JSONObject temp = obj.optJSONObject(entry.getKey().sign + "");
						if (temp != null) {
							entry.getValue().decode(temp, ver);
						}
					}
				}
				{
					JSONObject obj = json.optJSONObject(JSON_BASE_INFO);
					if (obj != null) {
						lastSendDayMailTime = obj.optLong(JSON_BASE_INFO_SEND_DAYMAIL_TIME, 0);
						if (lastSendDayMailTime > 0) {
							lastSendDayMailTime *= Timer.ONE_HOUR;
						}

						lastSendShutdownRewardTime = obj.optLong(JSON_BASE_INFO_SEND_SHUTDOWNREWARD_TIME, 0);
						if (lastSendShutdownRewardTime > 0) {
							lastSendShutdownRewardTime *= Timer.ONE_HOUR;
						}
						
						lastCollectPhyPowerTime = obj.optLong(JSON_BASE_INFO_SEND_PHYPOWER_TIME, 0);
						if (lastCollectPhyPowerTime > 0) {
							lastCollectPhyPowerTime *= Timer.ONE_MINUTE;
						}
						
						lastSendKAJIRewardTime = obj.optLong(JSON_BASE_INFO_SEND_KAJI_TIME, 0);
						if (lastSendKAJIRewardTime > 0) {
							lastSendKAJIRewardTime *= Timer.ONE_HOUR;
						}
					}
				}
			}
				break;
			}

		} catch (Exception ex) {
			_LOGGER.error("decode数据时发生错误 roleId=" + getRoleId() + " ----丢失数据，存在运行隐患！", ex);
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
		try {
			JSONObject json = new JSONObject();
			json.put(JSON_VER, 0);
			json.put(JSON_DAY, dataTime / Timer.ONE_HOUR);
			{
				{
					JSONObject obj = new JSONObject();
					json.put(JSON_SONDATA, obj);

					for (Entry<KRewardSonModuleType, KRoleRewardSonAbs> entry : sonMap.entrySet()) {
						JSONObject temp = entry.getValue().encode();
						obj.put(entry.getKey().sign + "", temp);
					}
				}
				{
					JSONObject obj = new JSONObject();
					json.put(JSON_BASE_INFO, obj);

					if (lastSendDayMailTime > 0) {
						obj.put(JSON_BASE_INFO_SEND_DAYMAIL_TIME, lastSendDayMailTime / Timer.ONE_HOUR);
					}

					obj.put(JSON_BASE_INFO_SEND_SHUTDOWNREWARD_TIME, lastSendShutdownRewardTime / Timer.ONE_HOUR);
					
					obj.put(JSON_BASE_INFO_SEND_PHYPOWER_TIME, lastCollectPhyPowerTime / Timer.ONE_MINUTE);
					
					obj.put(JSON_BASE_INFO_SEND_KAJI_TIME, lastSendKAJIRewardTime / Timer.ONE_HOUR);
				}
			}
			return json.toString();

		} catch (Exception ex) {
			_LOGGER.error("decode数据时发生错误 roleId=" + getRoleId() + " ----丢失数据，存在运行隐患！", ex);
			return null;
		} finally {
			rwLock.unlock();
		}
	}

	public void notifyUpdate() {
		super.notifyUpdate();
	}

	KRoleRewardSonAbs getSon(KRewardSonModuleType type) {
		return sonMap.get(type);
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
	boolean notifyForLogin() {
		rwLock.lock();
		try {
			long nowTime = System.currentTimeMillis();
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
	void notifyForDayChange() {
		rwLock.lock();
		try {
			long nowTime = System.currentTimeMillis();
			notifyForDayChange(nowTime);
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
	private void notifyForDayChange(long nowTime) {
		rwLock.lock();
		try {
			{
				Calendar calLast = Calendar.getInstance();
				calLast.setTime(new Date(dataTime));
				
				Calendar calNow = Calendar.getInstance();
				calNow.setTime(new Date(nowTime));
				
				if(calLast.get(Calendar.MONTH)!=calNow.get(Calendar.MONTH)){
					for (KRoleRewardSonAbs data : sonMap.values()) {
						data.notifyForMonthChange(nowTime);
					}
				}
			}
			
			dataTime = nowTime;
			for (KRoleRewardSonAbs data : sonMap.values()) {
				data.notifyForDayChange(nowTime);
			}
		} finally {
			rwLock.unlock();
		}
	}

	boolean hasSendedDayMail() {
		rwLock.lock();
		try {
			if (lastSendDayMailTime < 1) {
				// 今天未发送
				return false;
			}

			long todayStartTime = UtilTool.getTodayStart().getTimeInMillis();
			if (UtilTool.isBetweenDay(lastSendDayMailTime, todayStartTime)) {
				// 不是同一天，今天未发送
				return false;
			}
			// 今天已发送
			return true;
		} finally {
			rwLock.unlock();
		}
	}

	void notifySendedDayMail() {
		lastSendDayMailTime = System.currentTimeMillis();
		this.notifyUpdate();
	}

	long getSendedShutdownRewardTime() {
		return lastSendShutdownRewardTime;
	}

	void notifySendedShutdownReward(long nowTime) {
		this.lastSendShutdownRewardTime = nowTime;
		this.notifyUpdate();
	}
	
	long getSendedKAJIRewardTime() {
		return lastSendKAJIRewardTime;
	}

	void notifySendedKAJIReward(long nowTime) {
		this.lastSendKAJIRewardTime = nowTime;
		this.notifyUpdate();
	}	

	void setLastCollectPhyPowerTime(long nowTime) {
		this.lastCollectPhyPowerTime = nowTime;
		this.notifyUpdate();
	}

	long getLastCollectPhyPowerTime() {
		return lastCollectPhyPowerTime;
	}
}
