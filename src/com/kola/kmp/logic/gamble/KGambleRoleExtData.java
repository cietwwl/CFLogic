package com.kola.kmp.logic.gamble;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.role.RoleExtCABaseImpl;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.gamble.wish.KRoleWishData;
import com.kola.kmp.logic.gamble.wish.KWishSystemManager;
import com.kola.kmp.logic.gamble.wish2.KRoleWish2Data;

public class KGambleRoleExtData extends RoleExtCABaseImpl {

	private static final Logger _LOGGER = KGameLogger.getLogger(KGambleRoleExtData.class);

	private final static String JSON_KEY_OBJECT_WISHDATA = "K1";
	private final static String JSON_KEY_OBJECT_WISH2DATA = "K2";

	private KRoleWishData wishData;
	public KRoleWish2Data wish2Data;

	/**
	 * <pre>
	 * 为缓存生成对象时使用
	 * 
	 * @param dbdata
	 * @author zhaizl
	 * @creation 2014-2-13 下午5:04:42
	 * </pre>
	 */
	protected KGambleRoleExtData() {
	}

	/**
	 * <pre>
	 * 逻辑新建对象时使用
	 * 
	 * @param dbdata
	 * @author zhaizl
	 * @creation 2014-2-13 下午5:04:25
	 * </pre>
	 */
	protected KGambleRoleExtData(long _roleId, int _type) {
		super(_roleId, _type);
	}

	public void notifyDB() {
		this.notifyUpdate();
	}

	@Override
	protected void decode(String attribute) {
		try {
			JSONObject obj = new JSONObject(attribute);
			if (obj.has(JSON_KEY_OBJECT_WISHDATA)) {
				this.wishData = new KRoleWishData(this);
				this.wishData.decode(obj.getString(JSON_KEY_OBJECT_WISHDATA));
			}
			if(obj.has(JSON_KEY_OBJECT_WISH2DATA)) {
				this.wish2Data = new KRoleWish2Data(this, false);
				this.wish2Data.decode(obj.getString(JSON_KEY_OBJECT_WISH2DATA));
			}
		} catch (Exception e) {
			_LOGGER.error("解析许愿数据出错！", e);
		}
	}

	@Override
	protected String encode() {
		JSONObject obj = new JSONObject();
		try {
			if (wishData != null) {
				obj.put(JSON_KEY_OBJECT_WISHDATA, wishData.encode());
			}
			if (wish2Data != null) {
				obj.put(JSON_KEY_OBJECT_WISH2DATA, wish2Data.encode());
			}
		} catch (Exception e) {
			_LOGGER.error("保存博彩系统自定义数据出错！角色ID：" + this.getRoleId(), e);
		}
		return obj.toString();
	}

	public KRoleWishData getWishData() {
		if (wishData == null) {
			long nowTime = System.currentTimeMillis();
			wishData = new KRoleWishData(this, 0, 0, 0, 0, KWishSystemManager.FREE_WISH_STATUS_CLOSE, UtilTool.getNextWeekDayStart(nowTime, 0).getTimeInMillis(), 0,
					KWishSystemManager.MAX_LUCKY_PRICE_PASS_DAY);
		}
		return wishData;
	}
	
	public KRoleWish2Data getWish2Data() {
		if (wish2Data == null) {
			wish2Data = new KRoleWish2Data(this, true);
		}
		return wish2Data;
	}

	public void addWishDiceCount(int count, boolean isPoor, boolean is10Count) {
		if (wishData != null) {
			wishData.diceTimes.addAndGet(count);
			if (isPoor && is10Count) {
				wishData.poorWishCount++;
			} else if (!isPoor && is10Count) {
				wishData.richWishCount++;
			}
			this.notifyUpdate();
		}
	}

	// public void useWishDiceCount(int count) {
	// if (wishData != null) {
	//
	// this.notifyUpdate();
	// }
	// }

	public void completeGuideWish() {
		if (wishData != null) {
			wishData.isGuideWish = false;
			this.notifyUpdate();
		}
	}

	public void freeWish() {
		if (wishData != null) {
			wishData.freeWishStatus = KWishSystemManager.FREE_WISH_STATUS_COMPLETE;
			// wishData.freeCheckTime = System.currentTimeMillis()
			// + KWishSystemManager._freeWishTimeSecond * 1000;
			this.notifyUpdate();
		}
	}

	public boolean checkIsFreeWish() {
		boolean result = false;
		if (wishData != null) {
			// if (!wishData.isFreeWish) {
			// long nowTime = System.currentTimeMillis();
			// if (nowTime >= wishData.freeCheckTime) {
			// wishData.freeCheckTime = 0;
			// wishData.isFreeWish = true;
			// return true;
			// }
			// }else{
			// return true;
			// }
			long nowTime = System.currentTimeMillis();
			if (wishData.freeWishStatus == KWishSystemManager.FREE_WISH_STATUS_CLOSE) {
				if (UtilTool.checkNowTimeIsArriveTomorrow(wishData.freeCheckTime)) {
					wishData.freeCheckTime = nowTime;
					this.notifyUpdate();
				} else if (nowTime >= wishData.freeCheckTime + KWishSystemManager._freeWishTimeSecond * 1000) {
					wishData.freeWishStatus = KWishSystemManager.FREE_WISH_STATUS_OPEN;
					this.notifyUpdate();
					result = true;
				}
			} else if (wishData.freeWishStatus == KWishSystemManager.FREE_WISH_STATUS_OPEN) {
				result = true;
			}
		}
		return result;
	}

	public boolean updateDiceIndex(int nowIndex, boolean isLucky) {
		if (wishData != null) {
			int preIndex = wishData.nowGirlIndex.get();
			wishData.nowGirlIndex.set(nowIndex);
			wishData.decreaseDiceTime(1);
			if (isLucky) {
				wishData.runDiceCount = 0;
				wishData.luckyPricePassDay = KWishSystemManager.MIN_LUCKY_PRICE_PASS_DAY;
			} else {
				wishData.runDiceCount++;
			}
			if (preIndex > nowIndex) {
				wishData.circle.incrementAndGet();
				this.notifyUpdate();
				return true;
			}
			this.notifyUpdate();
		}
		return false;
	}

	public boolean checkAndRestWishData() {
		if (wishData != null) {
			long nowTime = System.currentTimeMillis();
			boolean isDataChanged = false;
			// if (!wishData.isFreeWish) {
			// if (nowTime >= wishData.freeCheckTime) {
			// wishData.freeCheckTime = 0;
			// wishData.isFreeWish = true;
			// isDataChanged = true;
			// }
			// }
			if (UtilTool.checkNowTimeIsArriveTomorrow(wishData.freeCheckTime)) {
				wishData.freeCheckTime = nowTime;
				wishData.freeWishStatus = KWishSystemManager.FREE_WISH_STATUS_CLOSE;
				isDataChanged = true;
			}

			if (UtilTool.checkNowTimeIsArriveTomorrow(wishData.todayCheckTime)) {
				wishData.todayCheckTime = nowTime;
				wishData.richWishCount = 0;
				wishData.poorWishCount = 0;
				if (wishData.luckyPricePassDay < KWishSystemManager.MAX_LUCKY_PRICE_PASS_DAY) {
					wishData.luckyPricePassDay++;
				}
				isDataChanged = true;
			}

			if (nowTime > wishData.circleCheckTime) {
				wishData.circleCheckTime = UtilTool.getNextWeekDayStart(nowTime, 0).getTimeInMillis();
				wishData.circle.set(0);
				isDataChanged = true;
			}

			if (isDataChanged) {
				this.notifyUpdate();
			}

			return isDataChanged;
		}
		return false;
	}
}
