package com.kola.kmp.logic.gamble.wish;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kmp.logic.gamble.KGambleRoleExtData;
import com.kola.kmp.logic.talent.KTalentTree;

public class KRoleWishData {
	
	

	private static final Logger _LOGGER = KGameLogger.getLogger(KRoleWishData.class);

	private final static String JSON_KEY_WISHDATA_DICE_COUNT = "1";
	private final static String JSON_KEY_WISHDATA_CIRCLE = "2";
	private final static String JSON_KEY_WISHDATA_FREE_CHECK_TIME = "3";
	private final static String JSON_KEY_WISHDATA_FREE_WISH_STATUS = "7";
	private final static String JSON_KEY_WISHDATA_GIRD_IDX = "4";
	private final static String JSON_KEY_WISHDATA_CIRCLE_CHECK_TIME = "5";
	private final static String JSON_KEY_WISHDATA_RUN_DICE_COUNT = "6";
	private final static String JSON_KEY_WISHDATA_POOR_WISH_COUNT = "7";
	private final static String JSON_KEY_WISHDATA_RICH_WISH_COUNT = "8";
	private final static String JSON_KEY_WISHDATA_TODAY_CHECK_TIME = "9";
	private final static String JSON_KEY_WISHDATA_LUCKY_PRICE_PASS_DAY = "10";
	private final static String JSON_KEY_WISHDATA_IS_GUIDE_WISH = "11";

	// 择骰子次数
	public AtomicInteger diceTimes;
	// // 圈数奖励记录
	// public Map<Integer,Boolean> circlePriceData = new LinkedHashMap<Integer,
	// Boolean>();
	// 当前圈数
	public AtomicInteger circle;
	// 当前格子数
	public AtomicInteger nowGirlIndex;
	// 免费屌丝许愿检测时间
	public long freeCheckTime;
	// 是否免费屌丝许愿
	public byte freeWishStatus;
	// 每周圈数清零检测时间
	public long circleCheckTime;
	// 已经掷点次数
	public int runDiceCount;
	// 时间广播消息队列当前地址
	public long broadcastIndex;

	// 当天屌丝许愿10连抽使用次数
	public int poorWishCount;
	// 当天高富帅许愿10连抽使用次数
	public int richWishCount;
	// 跨天检测时间
	public long todayCheckTime;
	//距离上次欢乐送中奖的间隔天数
	public int luckyPricePassDay;
//	//是否首次引导的许愿
	public boolean isGuideWish;

	KGambleRoleExtData extData;

	public KRoleWishData(KGambleRoleExtData extData) {
		this.extData = extData;
		this.isGuideWish = false;
	}

	public KRoleWishData(KGambleRoleExtData extData, int diceTimes, int circle, int nowGirlIndex, long freeCheckTime, byte freeWishStatus, long circleCheckTime, int runDiceCount, int luckyPricePassDay) {
		super();
		this.extData = extData;
		this.diceTimes = new AtomicInteger(diceTimes);
		this.circle = new AtomicInteger(circle);
		this.nowGirlIndex = new AtomicInteger(nowGirlIndex);
		this.freeCheckTime = freeCheckTime;
		this.freeWishStatus = freeWishStatus;
		this.circleCheckTime = circleCheckTime;
		this.runDiceCount = runDiceCount;
		this.luckyPricePassDay = luckyPricePassDay;
		this.isGuideWish = true;
		extData.notifyDB();
		// for (int i = 0; i < KWishSystemManager.circleRewardList.size(); i++)
		// {
		// circlePriceData.put(i, false);
		// }
	}

	private void changeDiceTime(int delta, boolean add) {
		if (add) {
			this.diceTimes.getAndAdd(delta);
		} else {
			this.diceTimes.getAndAdd(-delta);
		}
		extData.notifyDB();
	}

	public void increaseDiceTime(int count) {
		changeDiceTime(count, true);
		extData.notifyDB();
	}

	public boolean decreaseDiceTime(int count) {
		if (diceTimes.get() < count) {
			return false;
		}
		changeDiceTime(count, false);
		extData.notifyDB();
		return true;
	}

	public void setBroadcastIndex(long index) {
		this.broadcastIndex = index;
	}

	public String encode() {
		JSONObject obj = new JSONObject();
		try {
			obj.put(JSON_KEY_WISHDATA_DICE_COUNT, diceTimes.get());
			obj.put(JSON_KEY_WISHDATA_CIRCLE, circle.get());
			obj.put(JSON_KEY_WISHDATA_GIRD_IDX, nowGirlIndex.get());
			obj.put(JSON_KEY_WISHDATA_FREE_CHECK_TIME, freeCheckTime);
			obj.put(JSON_KEY_WISHDATA_FREE_WISH_STATUS, freeWishStatus);
			obj.put(JSON_KEY_WISHDATA_CIRCLE_CHECK_TIME, circleCheckTime);
			obj.put(JSON_KEY_WISHDATA_RUN_DICE_COUNT, runDiceCount);
			obj.put(JSON_KEY_WISHDATA_POOR_WISH_COUNT, poorWishCount);
			obj.put(JSON_KEY_WISHDATA_RICH_WISH_COUNT, richWishCount);
			obj.put(JSON_KEY_WISHDATA_TODAY_CHECK_TIME, todayCheckTime);
			obj.put(JSON_KEY_WISHDATA_LUCKY_PRICE_PASS_DAY, luckyPricePassDay);
			obj.put(JSON_KEY_WISHDATA_IS_GUIDE_WISH, isGuideWish ? 1 : 0);
		} catch (Exception e) {
			_LOGGER.error("保存许愿数据出错！", e);
		}
		return obj.toString();
	}

	public void decode(String attribute) {
		try {
			JSONObject obj = new JSONObject(attribute);
			this.diceTimes = new AtomicInteger(obj.optInt(JSON_KEY_WISHDATA_DICE_COUNT, 0));
			this.circle = new AtomicInteger(obj.optInt(JSON_KEY_WISHDATA_CIRCLE, 0));
			this.nowGirlIndex = new AtomicInteger(obj.optInt(JSON_KEY_WISHDATA_GIRD_IDX, 0));
			this.freeCheckTime = obj.optLong(JSON_KEY_WISHDATA_FREE_CHECK_TIME, System.currentTimeMillis());
			this.freeWishStatus = obj.optByte(JSON_KEY_WISHDATA_FREE_WISH_STATUS, KWishSystemManager.FREE_WISH_STATUS_CLOSE);
			this.circleCheckTime = obj.optLong(JSON_KEY_WISHDATA_CIRCLE_CHECK_TIME, System.currentTimeMillis());
			this.runDiceCount = obj.optInt(JSON_KEY_WISHDATA_RUN_DICE_COUNT, 0);
			this.poorWishCount = obj.optInt(JSON_KEY_WISHDATA_POOR_WISH_COUNT, 0);
			this.richWishCount = obj.optInt(JSON_KEY_WISHDATA_RICH_WISH_COUNT, 0);
			this.todayCheckTime = obj.optLong(JSON_KEY_WISHDATA_TODAY_CHECK_TIME, System.currentTimeMillis());
			this.luckyPricePassDay = obj.optInt(JSON_KEY_WISHDATA_LUCKY_PRICE_PASS_DAY, KWishSystemManager.MAX_LUCKY_PRICE_PASS_DAY);
			this.isGuideWish = (obj.optInt(JSON_KEY_WISHDATA_IS_GUIDE_WISH, 0) == 1);
		} catch (Exception e) {
			_LOGGER.error("解析许愿数据出错！", e);
		}
	}
}
