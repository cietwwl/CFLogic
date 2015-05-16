package com.kola.kmp.logic.vip;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.thirdpart.json.JSONArray;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.role.RoleExtCABaseImpl;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.mount.KMount;

/**
 * <pre>
 * 角色的VIP数据
 * 
 * @author CamusHuang
 * @creation 2014-2-13 下午4:53:22
 * </pre>
 */
public final class KRoleVIP extends RoleExtCABaseImpl {

	public static final Logger _LOGGER = KGameLogger.getLogger(KRoleVIP.class);

	// 读写锁
	public final ReentrantLock rwLock = new ReentrantLock();

	private long dataTime = System.currentTimeMillis();// 当前数据对应的日期
	// -------------------------缓存层规定的数据项
	private int lv = KVIPDataManager.mVIPLevelDataManager.getZeroLevel().lvl;
	private int exp;
	private int totalExp;// 充值总量
	// vip等级礼包领取数据<已领取了VIP礼包的VIP等级>
	private Set<Integer> lvRewardCollectedSet = new HashSet<Integer>();
	// vip每日礼包领取
	private long dayRewardCollectedTime;

	// ////////////////////////////////
	static final String JSON_VER = "0";// 版本
	static final String JSON_DAY = "Z";// 当前数据对应的日期
	//
	static final String JSON_LV = "1";// VIP LV
	static final String JSON_EXP = "2";// VIP EXP
	static final String JSON_TOTALEXP = "3";// VIP TOTALEXP
	static final String JSON_LVREWARD = "4";// vip等级礼包领取数据
	static final String JSON_DAYREWARD = "5";// vip每日礼包领取数据

	/**
	 * <pre>
	 * 逻辑新建对象时使用
	 * 
	 * @param dbdata
	 * @author CamusHuang
	 * @creation 2014-2-13 下午5:04:25
	 * </pre>
	 */
	protected KRoleVIP(long _roleId, int _type) {
		super(_roleId, _type);
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
			case 0:
				lv = json.getByte(JSON_LV);
				exp = json.getInt(JSON_EXP);
				totalExp = json.getInt(JSON_TOTALEXP);
				dayRewardCollectedTime = json.getLong(JSON_DAYREWARD) * Timer.ONE_HOUR;
				JSONArray rewardJson = json.optJSONArray(JSON_LVREWARD);
				if (rewardJson != null) {
					for (int i = 0; i < rewardJson.length(); i++) {
						lvRewardCollectedSet.add(rewardJson.getInt(i));
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
		// 构造一个数据对象给底层
		try {
			JSONObject json = new JSONObject();
			json.put(JSON_VER, 0);
			json.put(JSON_DAY, dataTime / Timer.ONE_HOUR);
			// CEND 暂时只有版本0
			json.put(JSON_LV, lv);
			json.put(JSON_EXP, exp);
			json.put(JSON_TOTALEXP, totalExp);
			json.put(JSON_DAYREWARD, dayRewardCollectedTime / Timer.ONE_HOUR);
			if (!lvRewardCollectedSet.isEmpty()) {
				JSONArray rewardJson = new JSONArray();
				json.put(JSON_LVREWARD, rewardJson);
				for (Integer value : lvRewardCollectedSet) {
					rewardJson.put(value);
				}
			}
			return json.toString();
		} catch (Exception ex) {
			_LOGGER.error("encode数据时发生错误 roleId=" + getRoleId() + " ----丢失数据！", ex);
			return "";
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
	void notifyForDayChange(long nowTime) {
		rwLock.lock();
		try {
			dataTime = nowTime;
			notifyUpdate();
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 获取VIP等级
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2013-7-3 下午5:27:04
	 * </pre>
	 */
	public int getLv() {
		return lv;
	}

	public void setLv(int lv) {
		rwLock.lock();
		try {
			this.lv = lv;
			notifyUpdate();
		} finally {
			rwLock.unlock();
		}
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		rwLock.lock();
		try {
			this.exp = exp;
			notifyUpdate();
		} finally {
			rwLock.unlock();
		}
	}

	int getTotalExp() {
		return totalExp;
	}

	void setTotalExp(int totalExp) {
		rwLock.lock();
		try {
			this.totalExp = totalExp;
			notifyUpdate();
		} finally {
			rwLock.unlock();
		}
	}

	public boolean isCollectedLvReward(int lv) {
		rwLock.lock();
		try {
			return lvRewardCollectedSet.contains(lv);
		} finally {
			rwLock.unlock();
		}
	}

	void collectedLvReward(int lv) {
		rwLock.lock();
		try {
			lvRewardCollectedSet.add(lv);
			notifyUpdate();
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 用于复制数据
	 * @author CamusHuang
	 * @creation 2014-10-15 下午5:57:52
	 * </pre>
	 */
	void clearLvReward() {
		rwLock.lock();
		try {
			lvRewardCollectedSet.clear();
			notifyUpdate();
		} finally {
			rwLock.unlock();
		}
	}

	boolean isCollectedDayReward(long nowTime) {
		return !UtilTool.isBetweenDay(dayRewardCollectedTime, nowTime);
	}

	void collectedDayReward(long nowTime) {
		rwLock.lock();
		try {
			dayRewardCollectedTime = nowTime;
			notifyUpdate();
		} finally {
			rwLock.unlock();
		}
	}
}
