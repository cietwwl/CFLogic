package com.kola.kmp.logic.reward;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kmp.logic.reward.KRewardSonModuleAbs.KRewardSonModuleType;
import com.kola.kmp.logic.shop.KRoleShop;

/**
 * <pre>
 * 角色奖励数据的子数据
 * 
 * @author CamusHuang
 * @creation 2013-12-28 上午10:38:13
 * </pre>
 */
public abstract class KRoleRewardSonAbs {
	
	public static final Logger _LOGGER = KGameLogger.getLogger(KRoleRewardSonAbs.class);

	public final KRoleReward owner;
	public final ReentrantLock rwLock;// 读写锁
	public final KRewardSonModuleType type;

	public KRoleRewardSonAbs(KRoleReward owner, KRewardSonModuleType type) {
		this.owner = owner;
		this.rwLock = owner.rwLock;
		this.type = type;
	}

	public abstract void decode(JSONObject ca, int ver) throws JSONException;

	public abstract JSONObject encode() throws JSONException;

	public final void notifyDB() {
		owner.notifyUpdate();
	}
	
	/**
	 * <pre>
	 * 时效任务跨天通知
	 * 
	 * @param nowTime
	 * @author CamusHuang
	 * @creation 2014-4-24 下午3:12:26
	 * </pre>
	 */
	protected abstract void notifyForDayChange(long nowTime);
	
	
	public void notifyForMonthChange(long nowTime) {
		
	}
}
