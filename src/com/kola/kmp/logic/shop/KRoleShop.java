package com.kola.kmp.logic.shop;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.role.RoleExtCABaseImpl;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.shop.random.KRoleRandomData;
import com.kola.kmp.logic.shop.timehot.TimeHotShopData;

/**
 * <pre>
 * 角色的Shop数据
 * 
 * @author CamusHuang
 * @creation 2014-2-13 下午4:53:22
 * </pre>
 */
public final class KRoleShop extends RoleExtCABaseImpl {

	public static final Logger _LOGGER = KGameLogger.getLogger(KRoleShop.class);

	// 读写锁
	public final ReentrantLock rwLock = new ReentrantLock();

	private long dataTime = System.currentTimeMillis();// 当前数据对应的日期
	// -------------------------缓存层规定的数据项
	// 随机商店数据
	private KRoleRandomData mRandomData = new KRoleRandomData(this);
	// 限时热购商店数据
	private TimeHotShopData mTimeHotShopData = new TimeHotShopData(this);

	// 当天体力购买次数
	private int phyPowerBuyTime;
	// ////////////////////////////////
	static final String JSON_VER = "0";// 版本
	static final String JSON_DAY = "Z";// 当前数据对应的日期
	//
	static final String JSON_RANDOM_TIME = "1";// 当天随机刷新次数
	static final String JSON_PHYPOW_TIME = "2";// 当天体力购买次数
	static final String JSON_LIMITIME_SHOPDATA = "3";// 限时商店数据

	/**
	 * <pre>
	 * 逻辑新建对象时使用
	 * 
	 * @param dbdata
	 * @author CamusHuang
	 * @creation 2014-2-13 下午5:04:25
	 * </pre>
	 */
	protected KRoleShop(long _roleId, int _type) {
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
				mRandomData.setRandomTime(json.getInt(JSON_RANDOM_TIME));
				phyPowerBuyTime = json.optInt(JSON_PHYPOW_TIME);
				mTimeHotShopData.decode(json.optJSONObject(JSON_LIMITIME_SHOPDATA));
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
			json.put(JSON_RANDOM_TIME, mRandomData.getRandomTime());
			json.put(JSON_PHYPOW_TIME, phyPowerBuyTime);
			json.put(JSON_LIMITIME_SHOPDATA, mTimeHotShopData.encode());
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
	boolean notifyForLogin(int roleLv) {
		rwLock.lock();
		try {
			long nowTime = System.currentTimeMillis();
			if (UtilTool.isBetweenDay(dataTime, nowTime)) {
				notifyForDayChange(nowTime, roleLv);
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
	void notifyForDayChange(long nowTime, int roleLv) {
		rwLock.lock();
		try {
			dataTime = nowTime;
			//
			mRandomData.notifyForDayChange(dataTime);
			//
			if (phyPowerBuyTime > 0) {
				phyPowerBuyTime = 0;
				notifyUpdate();
			}
		} finally {
			rwLock.unlock();
		}
	}

	public KRoleRandomData getRandomData() {
		return mRandomData;
	}
	
	public TimeHotShopData getTimeHotShopData() {
		return mTimeHotShopData;
	}

	public int getPhyPowerBuyTime() {
		return phyPowerBuyTime;
	}

	void setPhyPowerBuyTime(int phyPowerBuyTime) {
		rwLock.lock();
		try {
			if (this.phyPowerBuyTime != phyPowerBuyTime) {
				this.phyPowerBuyTime = phyPowerBuyTime;
				notifyUpdate();
			}
		} finally {
			rwLock.unlock();
		}
	}
	
	public void notifyUpdate() {
		super.notifyUpdate();
	}

}
