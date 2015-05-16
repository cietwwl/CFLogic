package com.kola.kmp.logic.currency;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.management.timer.Timer;

import com.koala.thirdpart.json.JSONArray;
import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.CustomizeAttribute;
import com.kola.kgame.cache.DataStatus;
import com.kola.kgame.cache.currency.impl.KACurrencyAccountSet;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;

/**
 * <pre>
 * 某个角色的帐户集
 * 方便安全控制&按类型获取帐户
 * 
 * Set及其内部所有Account共用一把锁
 * 
 * @author CamusHuang
 * @creation 2012-12-5 下午8:14:02
 * </pre>
 */
public class KCurrencyAccountSet extends KACurrencyAccountSet<KCurrencyAccount, KCurrencyCountStruct> {

	private KCurrencyAccountSetCA ca;

	KCurrencyAccountSet(long roleId, boolean isFirstNew) {
		super(roleId, isFirstNew);
		// 必须在roleId初始化之后才能NEW CA
		ca = new KCurrencyAccountSetCA(this, isFirstNew);
	}

	@Override
	protected Map<Byte, KCurrencyAccount> initAccounts(boolean isFirstNew) {
		Map<Byte, KCurrencyAccount> accountMap = new HashMap<Byte, KCurrencyAccount>();
		{
			for (KCurrencyTypeEnum type : KCurrencyTypeEnum.values()) {
				KCurrencyAccount account = new KCurrencyAccount(this, type, isFirstNew);
				accountMap.put(account._type, account);
			}
		}
		return accountMap;
	}

	@Override
	protected void decodeCA(String jsonStr) {
		ca.decodeAttribute(jsonStr);
	}

	@Override
	protected String encodeCA() {
		return ca.encodeAttribute();
	}

	public KCurrencyAccount getAccountByEnum(KCurrencyTypeEnum type) {
		return getAccount(type.sign);
	}

	/**
	 * <pre>
	 * 是否首次消耗点数，是则设为false并返回true
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-8-15 下午3:44:42
	 * </pre>
	 */
	boolean getAndClearFirstDecresePoint() {
		if (ca.isFirstDecresePoint) {
			ca.isFirstDecresePoint = false;
			notifyDB();
			return true;
		}
		return false;
	}

	/**
	 * <pre>
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-8-15 下午3:44:42
	 * </pre>
	 */
	boolean hasNotCatchFirstChargeGiftReward() {
		return ca.hasNotCatchFirstChargeGiftReward;
	}
	
	public boolean isCatchFirstChargeReturn(int priceIngot) {
		return ca.catchFirstChargeReturn.contains(priceIngot);
	}
	
	/**
	 * <pre>
	 * 是否未领取首充礼包，是则设为已领取首充礼包
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2015-1-21 上午10:24:18
	 * </pre>
	 */
	boolean tryToRecordCatchFirstChargeGiftReward(){
		if (ca.hasNotCatchFirstChargeGiftReward) {
			ca.hasNotCatchFirstChargeGiftReward = false;
			notifyDB();
			return true;
		}
		return false;
	}

	/**
	 * <pre>
	 * 是否未领取档位首充双倍，是则设为已领取档位首充双倍
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2015-1-21 上午10:26:24
	 * </pre>
	 */
	boolean tryToRecordCatchFirstChargeReturn(int priceIngot) {
		if (!ca.catchFirstChargeReturn.contains(priceIngot)) {
			ca.catchFirstChargeReturn.add(priceIngot);
			notifyDB();
			return true;
		}
		return false;
	}

	/**
	 * <pre>
	 * 是否已经首次充值，是则设为未首充
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-11-19 下午9:43:32
	 * </pre>
	 */
	public boolean resetAndGetFirstCharge() {
		boolean isShouldSave = false;
		
		if (!ca.catchFirstChargeReturn.isEmpty()) {
			ca.catchFirstChargeReturn.clear();
			isShouldSave = true;
		}
		if (!ca.hasNotCatchFirstChargeGiftReward) {
			ca.hasNotCatchFirstChargeGiftReward = true;
			isShouldSave = true;
		}
		
		if(isShouldSave){
			notifyDB();
		}
		return isShouldSave;
	}

	long getMonthCardEndTime() {
		return ca.monthCardEndTime;
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 用于复制数据
	 * @param monthCardEndTime
	 * @author CamusHuang
	 * @creation 2014-10-15 下午5:48:47
	 * </pre>
	 */
	void setMonthCardEndTime(long monthCardEndTime) {
		ca.monthCardEndTime = monthCardEndTime;
		notifyDB();
	}

	String getMonthCardEndTimeStr() {
		return UtilTool.DATE_FORMAT.format(new Date(ca.monthCardEndTime));
	}

	boolean isMonthCard(long nowTime) {
		rwLock.lock();
		try {
			if (nowTime < ca.monthCardEndTime) {
				return true;
			}
			return false;
		} finally {
			rwLock.unlock();
		}
	}

	boolean isCollectMonthCardReward(long nowTime) {
		return !UtilTool.isBetweenDay(ca.monthCardRewardCatchTime, nowTime);
	}

	void collectedMonthCardDayReward(long nowTime) {
		rwLock.lock();
		try {
			ca.monthCardRewardCatchTime = nowTime;
			notifyDB();
		} finally {
			rwLock.unlock();
		}
	}

	public int getMontyCardReleaseDay() {
		long nowTime = System.currentTimeMillis();
		rwLock.lock();
		try {
			if (nowTime >= ca.monthCardEndTime) {
				return 0;
			}

			long releaseTime = ca.monthCardEndTime - nowTime;
			int day = (int) (releaseTime / Timer.ONE_DAY);
			if (releaseTime % Timer.ONE_DAY > Timer.ONE_HOUR) {
				day++;
			}
			return day;

		} finally {
			rwLock.unlock();
		}
	}

	void addMonthCardTime(long addTime) {
		long startTime = System.currentTimeMillis();
		rwLock.lock();
		try {
			if (startTime < ca.monthCardEndTime) {
				ca.monthCardEndTime += addTime;
			} else {
				ca.monthCardEndTime = startTime + addTime;
			}
			notifyDB();
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 返回旧的值
	 * 
	 * @param diamond
	 * @return
	 * @author CamusHuang
	 * @creation 2014-10-28 下午8:30:44
	 * </pre>
	 */
	public long recordExchangeDiamond(int ver, long diamond) {
		long nowTime = System.currentTimeMillis();
		rwLock.lock();
		try {
			if (ver != ca.exchangeActivityVer || UtilTool.isBetweenDay(nowTime, ca.recordExchangeDiamondDay)) {
				// 换版本 或 跨天
				ca.recordExchangeDiamond = 0;
				ca.recordExchangeDiamondDay = nowTime;
			}

			ca.exchangeActivityVer = ver;
			long old = ca.recordExchangeDiamond;
			ca.recordExchangeDiamond += diamond;
			notifyDB();

			return old;
		} finally {
			rwLock.unlock();
		}
	}

	void startTimeLimitPresentActivity(int ver, long startTime) {
		rwLock.lock();
		try {
			if (ver != ca.timeLimitPresentActivityVer) {
				// 换版本
				ca.timeLimitPresentActivityVer = ver;
				ca.timeLimitPresentActivityStartTime = startTime;
				//
				notifyDB();
			}
		} finally {
			rwLock.unlock();
		}
	}

	// boolean isTimeLimitPresentActivityEffect() {
	// rwLock.lock();
	// try {
	// long nowTime = System.currentTimeMillis();
	// if (nowTime < ca.timeLimitPresentActivityEndTime) {
	// return true;
	// }
	// return false;
	// } finally {
	// rwLock.unlock();
	// }
	// }

	long getTimeLimitPresentActivityStartTime() {
		rwLock.lock();
		try {
			return ca.timeLimitPresentActivityStartTime;
		} finally {
			rwLock.unlock();
		}
	}

	public static class KCurrencyAccountSetCA implements CustomizeAttribute {

		final KCurrencyAccountSet owner;

		// ////////////////
		private boolean isFirstDecresePoint = true;// 是否首次消耗点数
		private boolean hasNotCatchFirstChargeGiftReward = true;// true表示未领取首充礼包，false表示已领取首充礼包
		private Set<Integer> catchFirstChargeReturn=new HashSet<Integer>();// 各档位首充返点是否已经领取

		private long monthCardEndTime;// 月卡结束时间
		private long monthCardRewardCatchTime;// 月卡每日发钻石

		private int exchangeActivityVer;// 兑换活动的版本号
		private long recordExchangeDiamond;// 每天兑换的钻石数量
		private long recordExchangeDiamondDay;// 兑换数量记录时间

		private int timeLimitPresentActivityVer;// 限时返现活动版本号
		private long timeLimitPresentActivityStartTime;// 限时返现开始时间

		// ///////////////
		private static final String JSON_NULL = "NULL";// null
		private static final String JSON_VER = "0";// 版本
		//
		private static final String JSON_BASEINFO = "1";// 基础信息
		private static final String JSON_BASEINFO_FIRSTDECRESEPOINT = "1";// 是否首次消耗点数
		private static final String JSON_BASEINFO_FIRSTCHARGEGIFT = "2";// 是否领取首充礼包

		private static final String JSON_BASEINFO_MONTHCARD = "3";// 月卡结束时间
		private static final String JSON_BASEINFO_MONTHREWARD = "4";//

		private static final String JSON_BASEINFO_REEXVERSION = "7";// 每天兑换的钻石数量
		private static final String JSON_BASEINFO_REEXDIAMOND = "5";// 每天兑换的钻石数量
		private static final String JSON_BASEINFO_REEXDIAMONDDAY = "6";// 兑换数量记录时间
		
		private static final String JSON_BASEINFO_FIRSTCHARGERETURN = "8";// 已经领取的首充返点

		private static final String JSON_BASEINFO_TIMELIMITPRESENT_VER = "A";// 限时返现活动版本号
		private static final String JSON_BASEINFO_TIMELIMITPRESENT_STARTTIME = "B";// 限时返现结束时间

		KCurrencyAccountSetCA(KCurrencyAccountSet owner, boolean isFirstNew) {
			this.owner = owner;
		}

		@Override
		public void decodeAttribute(String attribute) {
			owner.rwLock.lock();
			try {
				JSONObject obj = new JSONObject(attribute);
				int ver = obj.getInt(JSON_VER);// 默认版本
				// CEND 货币--暂时只有版本0
				switch (ver) {
				case 0:
					decodeBaseInfo(obj.getJSONObject(JSON_BASEINFO));
					break;
				}
			} catch (Exception ex) {
				_LOGGER.error("decode数据时发生错误 roleId=" + owner._ownerId + " ----丢失数据！", ex);
			} finally {
				owner.rwLock.unlock();
			}
		}

		/**
		 * <pre>
		 * 
		 * @param obj
		 * @throws JSONException
		 * @author CamusHuang
		 * @creation 2012-11-20 下午12:01:05
		 * </pre>
		 */
		private void decodeBaseInfo(JSONObject obj) throws JSONException {

			isFirstDecresePoint = obj.optBoolean(JSON_BASEINFO_FIRSTDECRESEPOINT, true);
			hasNotCatchFirstChargeGiftReward = obj.optBoolean(JSON_BASEINFO_FIRSTCHARGEGIFT, true);
			{
				JSONArray array = obj.optJSONArray(JSON_BASEINFO_FIRSTCHARGERETURN);
				if(array!=null){
					for(int i = array.length()-1;i>=0;i--){
						catchFirstChargeReturn.add(array.getInt(i));
					}
				}
			}

			monthCardEndTime = obj.optLong(JSON_BASEINFO_MONTHCARD) * Timer.ONE_HOUR;
			monthCardRewardCatchTime = obj.optLong(JSON_BASEINFO_MONTHREWARD) * Timer.ONE_HOUR;

			exchangeActivityVer = obj.optInt(JSON_BASEINFO_REEXVERSION, 1);
			recordExchangeDiamond = obj.optLong(JSON_BASEINFO_REEXDIAMOND);
			recordExchangeDiamondDay = obj.optLong(JSON_BASEINFO_REEXDIAMONDDAY) * Timer.ONE_HOUR;

			timeLimitPresentActivityVer = obj.optInt(JSON_BASEINFO_TIMELIMITPRESENT_VER, -1);
			timeLimitPresentActivityStartTime = obj.optInt(JSON_BASEINFO_TIMELIMITPRESENT_STARTTIME, 0) * Timer.ONE_MINUTE;
		}

		@Override
		public String encodeAttribute() {
			owner.rwLock.lock();
			try {
				JSONObject obj = new JSONObject();
				obj.put(JSON_VER, 0);// 默认版本
				// CEND 货币--暂时只有版本0
				obj.put(JSON_BASEINFO, encodeBaseInfo());// 基础信息
				return obj.toString();
			} catch (Exception ex) {
				_LOGGER.error("encode数据时发生错误roleId=" + owner._ownerId + " ----丢失数据！", ex);
				return "";
			} finally {
				owner.rwLock.unlock();
			}
		}

		private JSONObject encodeBaseInfo() throws JSONException {
			JSONObject obj = new JSONObject();

			obj.put(JSON_BASEINFO_FIRSTDECRESEPOINT, isFirstDecresePoint);
			obj.put(JSON_BASEINFO_FIRSTCHARGEGIFT, hasNotCatchFirstChargeGiftReward);
			{
				
				if(!catchFirstChargeReturn.isEmpty()){
					JSONArray array = new JSONArray();
					obj.put(JSON_BASEINFO_FIRSTCHARGERETURN, array);
					for(int priceIngot:catchFirstChargeReturn){
						array.put(priceIngot);
					}
				}
			}

			obj.put(JSON_BASEINFO_MONTHCARD, monthCardEndTime / Timer.ONE_HOUR);
			obj.put(JSON_BASEINFO_MONTHREWARD, monthCardRewardCatchTime / Timer.ONE_HOUR);

			obj.put(JSON_BASEINFO_REEXVERSION, exchangeActivityVer);
			obj.put(JSON_BASEINFO_REEXDIAMOND, recordExchangeDiamond);
			obj.put(JSON_BASEINFO_REEXDIAMONDDAY, recordExchangeDiamondDay / Timer.ONE_HOUR);
			
			obj.put(JSON_BASEINFO_TIMELIMITPRESENT_VER, timeLimitPresentActivityVer);
			obj.put(JSON_BASEINFO_TIMELIMITPRESENT_STARTTIME, timeLimitPresentActivityStartTime/Timer.ONE_MINUTE);

			return obj;
		}

		/**
		 * @deprecated 空实现
		 */
		public DataStatus getDataStatus() {
			return null;
		}
	}
}
