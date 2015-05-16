package com.kola.kmp.logic.reward.exciting;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.game.util.StringUtil;
import com.koala.thirdpart.json.JSONArray;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.role.RoleExtCABaseImpl;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.KDataStructs.EquiSetStruct;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.ExcitingDataManager.ExcitionActivity;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.ExcitingRuleManager.RewardRule;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.tips.RewardTips;

/**
 * <pre>
 * 主角精彩活动数据
 * 
 * @author camus
 * @creation 2012-12-31 上午11:41:09
 * </pre>
 */
public class KRoleExciting extends RoleExtCABaseImpl {

	private static Logger _LOGGER = KGameLogger.getLogger(KRoleExciting.class);
	// 读写锁
	public final ReentrantLock rwLock = new ReentrantLock();
	/** 今日的日期时间 */
	private long dataTime;
	// ////////////////////////////////////
	/**
	 * @deprecated 尽量使用{@link #getActivityData(int activityId)}方法调用
	 */
	private Map<Integer, RoleActivityData> roleActivityDatas = new HashMap<Integer, RoleActivityData>();

	// //////////////////
	static final String JSON_VER = "0";// 版本
	static final String JSON_DAY = "Z";// 当前数据对应的日期
	//
	static final String JSON_ACTIVITY = "1";// 活动数据
	static final String JSON_ACTIVITY_ID = "1";// 活动ID
	static final String JSON_ACTIVITY_CHARGE = "2";// 期间累计充值钻石
	static final String JSON_ACTIVITY_ONLINE = "3";// 期间累计在线时长
	static final String JSON_ACTIVITY_LOGINDAY = "4";// 期间连续登录天数
	static final String JSON_ACTIVITY_GETREWARDRULE = "5";// 已领取的奖励规则
	static final String JSON_ACTIVITY_PHYPOW = "6";// 期间累计消耗体力
	static final String JSON_ACTIVITY_PAYINGOT = "11";// 期间累计消费钻石
	static final String JSON_ACTIVITY_ISPAYED = "12";// 是否已为本活动付费
	static final String JSON_ACTIVITY_RULETIMES = "13";// 各规则领奖次数

	/**
	 * <pre>
	 * 逻辑新建对象时使用
	 * 
	 * @param dbdata
	 * @author CamusHuang
	 * @creation 2014-2-13 下午5:04:25
	 * </pre>
	 */
	protected KRoleExciting(long _roleId, int _type, boolean isFirstNew) {
		super(_roleId, _type);
		if (isFirstNew) {
			dataTime = System.currentTimeMillis();
		}
	}

	@Override
	protected void decode(String attribute) {
		if (attribute == null || attribute.length() < 1) {
			return;
		}
		// 由底层调用,解释出逻辑层数据
		try {
			JSONObject obj = new JSONObject(attribute);
			int ver = obj.getInt(JSON_VER);// 默认版本
			dataTime = obj.optLong(JSON_DAY) * Timer.ONE_HOUR;
			// CEND 暂时只有版本0
			switch (ver) {
			case 0:
				JSONArray array = obj.optJSONArray(JSON_ACTIVITY);
				if (array == null) {
					return;
				}

				JSONObject obj2;
				JSONObject obj3;
				JSONArray array2;
				RoleActivityData data;
				int len = array.length();
				for (int i = 0; i < len; i++) {
					obj2 = array.getJSONObject(i);
					data = new RoleActivityData(obj2.getInt(JSON_ACTIVITY_ID));
					if (KExcitingDataManager.mExcitingDataManager.getData(data.activityId) == null) {
						// 活动不存在（无效或过期）
						continue;
					}
					data.totalCharge = obj2.optInt(JSON_ACTIVITY_CHARGE);
					data.totalOnlineInMills = obj2.optInt(JSON_ACTIVITY_ONLINE) * Timer.ONE_SECOND;
					data.totalLoginDay = obj2.optInt(JSON_ACTIVITY_LOGINDAY);
					data.totalUsePhyPow = obj2.optInt(JSON_ACTIVITY_PHYPOW);
					data.totalPay = obj2.optInt(JSON_ACTIVITY_PAYINGOT);
					data.isPayed = obj2.optBoolean(JSON_ACTIVITY_ISPAYED);

					array2 = obj2.optJSONArray(JSON_ACTIVITY_GETREWARDRULE);
					if (array2 != null) {
						int len2 = array2.length();
						for (int m = 0; m < len2; m++) {
							data.receiveRules.add(array2.getInt(m));
						}
					}
					obj3 = obj2.optJSONObject(JSON_ACTIVITY_RULETIMES);
					if (obj3 != null) {
						for (Iterator<String> it = obj3.keys(); it.hasNext(); ) {
							String key = it.next();
							data.ruleTimeMap.put(Integer.parseInt(key), new AtomicInteger(obj3.getInt(key)));
						}
					}
					
					roleActivityDatas.put(data.activityId, data);
				}
				break;
			}
		} catch (Exception ex) {
			_LOGGER.error("decode数据时发生错误 roleId=" + getRoleId() + " ----丢失数据，存在运行隐患！", ex);
		}
	}

	protected String encode() {
		rwLock.lock();
		// 构造一个数据对象给底层
		try {
			if (roleActivityDatas.isEmpty()) {
				return "";
			}
			JSONObject obj = new JSONObject();
			obj.put(JSON_VER, 0);
			obj.put(JSON_DAY, dataTime / Timer.ONE_HOUR);

			JSONArray array = new JSONArray();
			JSONObject obj2;
			JSONObject obj3;
			JSONArray array2;
			for (RoleActivityData data : roleActivityDatas.values()) {
				obj2 = new JSONObject();
				obj2.put(JSON_ACTIVITY_ID, data.activityId);
				if(data.totalCharge>0){
					obj2.put(JSON_ACTIVITY_CHARGE, data.totalCharge);
				}
				if(data.totalOnlineInMills>0){
					obj2.put(JSON_ACTIVITY_ONLINE, data.totalOnlineInMills / Timer.ONE_SECOND);
				}
				if(data.totalLoginDay>0){
					obj2.put(JSON_ACTIVITY_LOGINDAY, data.totalLoginDay);
				}
				if(data.totalUsePhyPow>0){
					obj2.put(JSON_ACTIVITY_PHYPOW, data.totalUsePhyPow);
				}
				if(data.totalPay>0){
					obj2.put(JSON_ACTIVITY_PAYINGOT, data.totalPay);
				}
				if(data.isPayed){
					obj2.put(JSON_ACTIVITY_ISPAYED, data.isPayed);
				}

				if (!data.receiveRules.isEmpty()) {
					array2 = new JSONArray();
					for (Integer ruleId : data.receiveRules) {
						array2.put(ruleId);
					}
					obj2.put(JSON_ACTIVITY_GETREWARDRULE, array2);
				}
				if (!data.ruleTimeMap.isEmpty()) {
					obj3 = new JSONObject();
					for (Entry<Integer,AtomicInteger> e : data.ruleTimeMap.entrySet()) {
						if(e.getValue().get()>0){
							obj3.put(e.getKey().toString(), e.getValue().get());
						}
					}
					if(obj3.length()>0){
						obj2.put(JSON_ACTIVITY_RULETIMES, obj3);
					}
				}

				array.put(obj2);
			}

			obj.put(JSON_ACTIVITY, array);
			return obj.toString();
		} catch (Exception ex) {
			_LOGGER.error("encode数据时发生错误 roleId=" + getRoleId() + " ----丢失数据！", ex);
			return "";
		} finally {
			rwLock.unlock();
		}
	}

	private RoleActivityData getActivityData(int activityId) {
		RoleActivityData data = roleActivityDatas.get(activityId);
		if (data == null) {
			data = new RoleActivityData(activityId);
			roleActivityDatas.put(activityId, data);
			notifyUpdate();
		}
		return data;
	}

	/**
	 * <pre>
	 * 以活动期间的统计数据为基础进行状态判断
	 * 
	 * @param activity
	 * @param rule
	 * @return
	 * @author CamusHuang
	 * @creation 2013-7-6 下午2:50:39
	 * </pre>
	 */
	ExcitiongStatusEnum checkRewardStatus(ExcitionActivity activity, RewardRule rule) {
		rwLock.lock();
		try {

			RoleActivityData data = getActivityData(activity.id);

			if (activity.buyPrice != null) {
				// 需要预先支付
				if (!isPayed(activity)) {
					// 未支付
					return ExcitiongStatusEnum.NOT_FINISHED;
				}
			}

			if (data.receiveRules.contains(rule.ruleId)) {
				return ExcitiongStatusEnum.COLLECTED;
			}

			if (rule.isSenstiveForTotalCharge() && data.totalCharge < rule.totalCharge) {
				return ExcitiongStatusEnum.NOT_FINISHED;
			}
			if (rule.isSenstiveForTotalUsePhyPow() && data.totalUsePhyPow < rule.totalUsePhyPow) {
				return ExcitiongStatusEnum.NOT_FINISHED;
			}
			if (rule.isSenstiveForTotalPay() && data.totalPay < rule.totalPay) {
				return ExcitiongStatusEnum.NOT_FINISHED;
			}
			if (rule.isSenstiveForTotalOnline() && data.totalOnlineInMills < rule.totalOnlineMills) {
				return ExcitiongStatusEnum.NOT_FINISHED;
			}
			if (rule.isSenstiveForLoginDay() && data.totalLoginDay < rule.totalLoginDay) {
				return ExcitiongStatusEnum.NOT_FINISHED;
			}

			return ExcitiongStatusEnum.FINISHED;
		} finally {
			rwLock.unlock();
		}
	}

	boolean hasCollectedAllReward(ExcitionActivity activity) {
		rwLock.lock();
		try {
			RoleActivityData data = getActivityData(activity.id);
			for (int ruleId : activity.ruleMap.keySet()) {
				if (!data.receiveRules.contains(ruleId)) {
					return false;
				}
			}
			return true;
		} finally {
			rwLock.unlock();
		}
	}

	String collectReward(ExcitionActivity activity, RewardRule rule) {
		String failTips=null;
		rwLock.lock();
		try {
			RoleActivityData data = getActivityData(activity.id);
			if (data.receiveRules.contains(rule.ruleId)) {
				failTips = RewardTips.你已领取此礼包;
				return failTips;
			}

			if (rule.isSenstiveForTotalCharge() && data.totalCharge < rule.totalCharge) {
				failTips = StringUtil.format(RewardTips.充值金额未满x钻石, rule.totalCharge);
				return failTips;
			}
			if (rule.isSenstiveForTotalUsePhyPow() && data.totalUsePhyPow < rule.totalUsePhyPow) {
				failTips = StringUtil.format(RewardTips.消耗体力未满x, rule.totalUsePhyPow);
				return failTips;
			}
			if (rule.isSenstiveForTotalPay() && data.totalPay < rule.totalPay) {
				failTips = StringUtil.format(RewardTips.消费金额未满x钻石, rule.totalPay);
				return failTips;
			}
			if (rule.isSenstiveForTotalOnline() && data.totalOnlineInMills < rule.totalOnlineMills) {
				failTips = StringUtil.format(RewardTips.在线时长未满x分钟, rule.getTotalOnlineInMinu());
				return failTips;
			}
			if (rule.isSenstiveForLoginDay() && data.totalLoginDay < rule.totalLoginDay) {
				failTips = StringUtil.format(RewardTips.登陆天数未满x天, rule.totalLoginDay);
				return failTips;
			}

			data.receiveRules.add(rule.ruleId);
			return failTips;
		} finally {
			rwLock.unlock();
			if (failTips == null) {
				notifyUpdate();
			}
		}
	}

	boolean isPayed(ExcitionActivity activity) {
		rwLock.lock();
		try {
			RoleActivityData data = getActivityData(activity.id);
			return data.isPayed;
		} finally {
			rwLock.unlock();
		}
	}
	
	/**
	 * 
	 * @param activity
	 * @return 是否>=最大值
	 */
	boolean increaseCollectRuleTime(ExcitionActivity activity, RewardRule rule) {
		boolean isDataChange = false;
		// 如果是全区限量
		if (rule.maxTimeForWorld > 0) {
			if (ExcitingGlobalDataImpl.instance.increaseCount(activity, rule)) {
				isDataChange = true;
			}
		}

		// 如果个人次数限量
		if (rule.chargeInOneTimeMaxTimeForRole > 0) {
			rwLock.lock();
			try {
				RoleActivityData data = getActivityData(activity.id);
				data.increaseTime(rule.ruleId);
				notifyUpdate();
				isDataChange = true;
			} finally {
				rwLock.unlock();
			}
		}

		return isDataChange;
	}

	/**
	 * <pre>
	 * 
	 * 
	 * @param activity
	 * @param rule
	 * @return 0表示次数用完，-1表示不限次数，>0表示剩余次数
	 * @author CamusHuang
	 * @creation 2015-1-12 上午11:02:05
	 * </pre>
	 */
	int getRoleReleaseTime(ExcitionActivity activity, RewardRule rule) {
		rwLock.lock();
		try {
			// 如果个人次数限量，则判断个人次数
			int roleRleaseTime = -1;
			if (rule.chargeInOneTimeMaxTimeForRole > 0) {
				RoleActivityData data = getActivityData(activity.id);
				int roleUsedTime = data.getTime(rule.ruleId);
				roleRleaseTime = Math.max(0, rule.chargeInOneTimeMaxTimeForRole - roleUsedTime);
			}
			return roleRleaseTime;
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 
	 * 
	 * @param activity
	 * @param rule
	 * @return 0表示次数用完，-1表示不限次数，>0表示剩余次数
	 * @author CamusHuang
	 * @creation 2015-1-6 下午4:13:21
	 * </pre>
	 */
	int getRuleReleaseTime(ExcitionActivity activity, RewardRule rule) {
		rwLock.lock();
		try {
			// 如果是全区限量 ，则判断全服数量
			int worldReleaseTime = -1;
			if (rule.maxTimeForWorld > 0) {
				worldReleaseTime = Math.max(0, rule.maxTimeForWorld - ExcitingGlobalDataImpl.instance.getCount(activity.id, rule.ruleId));
			}

			// 如果个人次数限量，则判断个人次数
			int roleRleaseTime = getRoleReleaseTime(activity, rule);

			if (worldReleaseTime == -1) {
				return roleRleaseTime;
			}

			if (roleRleaseTime == -1) {
				return worldReleaseTime;
			}

			return Math.min(roleRleaseTime, worldReleaseTime);
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
	 * </pre>
	 */
	boolean notifyForLogin(long nowTime) {
		rwLock.lock();
		try {
			if (UtilTool.isBetweenDay(dataTime, nowTime)) {
				return notifyForDayChange(nowTime);
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
	 * </pre>
	 */
	boolean notifyForDayChange(long nowTime) {
		boolean isShouldSave = false;
		rwLock.lock();
		try {
			dataTime = nowTime;
			//
			RoleActivityData data;
			for (ExcitionActivity tempActivity : KExcitingDataManager.mExcitingDataManager.getDatas()) {
				if (!tempActivity.caTime.isInEffectTime(nowTime)) {
					continue;
				}
				
				data = getActivityData(tempActivity.id);
				
				if (tempActivity.isSenstiveForLoginDay()) {
					// 记录登陆天数
					data.addLoginDay();
					isShouldSave = true;
				}

				for (RewardRule rule : tempActivity.ruleMap.values()) {
					if (rule.isChargeInOneTimeMaxTimeForRolePerDay) {
						if (data.clearTime(rule.ruleId)) {
							isShouldSave = true;
						}
					}
					if (rule.isTotalChargePerDay) {
						if (data.clearTime(rule.ruleId)) {
							isShouldSave = true;
						}
						if (data.receiveRules.remove(rule.ruleId)) {
							isShouldSave = true;
						}
						if (data.totalCharge > 0) {
							data.totalCharge = 0;
							isShouldSave = true;
						}
					}
				}
			}
			
			if (isShouldSave) {
				notifyUpdate();
			}
			
			return isShouldSave;
		} finally {
			rwLock.unlock();
		}
	}

	void setPayed(ExcitionActivity activity, boolean isPayed) {
		rwLock.lock();
		try {
			RoleActivityData data = getActivityData(activity.id);
			data.isPayed = isPayed;
		} finally {
			rwLock.unlock();
			notifyUpdate();
		}
	}

	boolean addTotalCharge(int chargeIngot, boolean isFirstCharge) {
		boolean isStatusChange = false;
		boolean isShouldSave = false;
		rwLock.lock();
		try {
			RoleActivityData data;
			long nowTime = System.currentTimeMillis();
			for (ExcitionActivity tempActivity : KExcitingDataManager.mExcitingDataManager.getDatas()) {
				if (!tempActivity.caTime.isInEffectTime(nowTime)) {
					continue;
				}
				if (!tempActivity.isSenstiveForTotalCharge()) {
					continue;
				}

				if (isFirstCharge && tempActivity.isExcuteFirstCharge()) {
					continue;
				}

				{
					data = getActivityData(tempActivity.id);
					if (data.addTotalCharge(tempActivity, chargeIngot)) {
						isStatusChange = true;
					}
					isShouldSave = true;
				}
			}
		} finally {
			rwLock.unlock();
			if (isShouldSave) {
				notifyUpdate();
			}
		}
		return isStatusChange;
	}

	boolean addTotalPay(int payIngot) {
		boolean isStatusChange = false;
		boolean isShouldSave = false;
		rwLock.lock();
		try {
			RoleActivityData data;
			long nowTime = System.currentTimeMillis();
			for (ExcitionActivity tempActivity : KExcitingDataManager.mExcitingDataManager.getDatas()) {
				if (!tempActivity.caTime.isInEffectTime(nowTime)) {
					continue;
				}
				if (!tempActivity.isSenstiveForTotalPay()) {
					continue;
				}

				{
					data = getActivityData(tempActivity.id);
					if (data.addTotalPay(tempActivity, payIngot)) {
						isStatusChange = true;
					}
					isShouldSave = true;
				}
			}
		} finally {
			rwLock.unlock();
			if (isShouldSave) {
				notifyUpdate();
			}
		}
		return isStatusChange;
	}

	boolean addTotalUsePhyPow(int usePhyPow) {
		boolean isStatusChange = false;
		boolean isShouldSave = false;
		rwLock.lock();
		try {
			RoleActivityData data;
			long nowTime = System.currentTimeMillis();
			for (ExcitionActivity tempActivity : KExcitingDataManager.mExcitingDataManager.getDatas()) {
				if (!tempActivity.caTime.isInEffectTime(nowTime)) {
					continue;
				}
				if (!tempActivity.isSenstiveForTotalUsePhyPow()) {
					continue;
				}

				{
					data = getActivityData(tempActivity.id);
					if (data.addTotalUsePhyPow(tempActivity, usePhyPow)) {
						isStatusChange = true;
					}
					isShouldSave = true;
				}
			}
		} finally {
			rwLock.unlock();
			if (isShouldSave) {
				notifyUpdate();
			}
		}
		return isStatusChange;
	}

	void addOnlineTime(long onlineTime) {
		boolean isShouldSave = false;
		rwLock.lock();
		try {
			RoleActivityData data;
			long nowTime = System.currentTimeMillis();
			for (ExcitionActivity tempActivity : KExcitingDataManager.mExcitingDataManager.getDatas()) {
				if (!tempActivity.caTime.isInEffectTime(nowTime)) {
					continue;
				}
				if (!tempActivity.isSenstiveForTotalOnline()) {
					continue;
				}

				{
					data = getActivityData(tempActivity.id);
					data.addTotalOnline(onlineTime);
					isShouldSave = true;
				}
			}
		} finally {
			rwLock.unlock();
			if (isShouldSave) {
				notifyUpdate();
			}
		}
	}

	boolean notifyLevelUp(int preLv, int nowLv) {
		// 升级
		boolean isStatusChange = false;
		rwLock.lock();
		try {
			RoleActivityData data;
			long nowTime = System.currentTimeMillis();
			for (ExcitionActivity tempActivity : KExcitingDataManager.mExcitingDataManager.getDatas()) {
				if (!tempActivity.caTime.isInEffectTime(nowTime)) {
					continue;
				}
				if (!tempActivity.isSenstiveForLv()) {
					continue;
				}

				{
					data = getActivityData(tempActivity.id);
					for (RewardRule rule : tempActivity.ruleMap.values()) {
						if (!data.receiveRules.contains(rule.ruleId)) {
							if (preLv < rule.minLv && rule.minLv <= nowLv) {
								isStatusChange = true;
								break;
							}
						}
					}
					if (isStatusChange) {
						break;
					}
				}
			}
		} finally {
			rwLock.unlock();
		}
		return isStatusChange;
	}

	boolean notifyMountLevelUp(int oldLv, int nowLv) {
		// 机甲升级
		boolean isStatusChange = false;
		rwLock.lock();
		try {
			RoleActivityData data;
			long nowTime = System.currentTimeMillis();
			for (ExcitionActivity tempActivity : KExcitingDataManager.mExcitingDataManager.getDatas()) {
				if (!tempActivity.caTime.isInEffectTime(nowTime)) {
					continue;
				}
				if (!tempActivity.isSenstiveForMountLv()) {
					continue;
				}

				{
					data = getActivityData(tempActivity.id);
					for (RewardRule rule : tempActivity.ruleMap.values()) {
						if (!data.receiveRules.contains(rule.ruleId)) {
							if (oldLv < rule.minMountLv && rule.minMountLv <= nowLv) {
								isStatusChange = true;
								break;
							}
						}
					}
					if (isStatusChange) {
						break;
					}
				}
			}
		} finally {
			rwLock.unlock();
		}
		return isStatusChange;
	}

	boolean notifyBattlePowChange(int battlePow) {
		// 战力变化
		boolean isStatusChange = false;
		rwLock.lock();
		try {
			RoleActivityData data;
			long nowTime = System.currentTimeMillis();
			for (ExcitionActivity tempActivity : KExcitingDataManager.mExcitingDataManager.getDatas()) {
				if (!tempActivity.caTime.isInEffectTime(nowTime)) {
					continue;
				}
				if (!tempActivity.isSenstiveForBattlePow()) {
					continue;
				}

				{
					data = getActivityData(tempActivity.id);
					for (RewardRule rule : tempActivity.ruleMap.values()) {
						if (!data.receiveRules.contains(rule.ruleId)) {
							if (battlePow >= rule.minBattlePow) {
								isStatusChange = true;
								break;
							}
						}
					}
					if (isStatusChange) {
						break;
					}
				}
			}
		} finally {
			rwLock.unlock();
		}
		return isStatusChange;
	}

	/**
	 * <pre>
	 * 
	 * 
	 * @param setlvs
	 * @return
	 * @author CamusHuang
	 * @creation 2014-9-2 下午5:14:25
	 * </pre>
	 */
	boolean notifyEquiSetChange(EquiSetStruct setlvs) {
		// 升级
		boolean isStatusChange = false;
		rwLock.lock();
		try {
			RoleActivityData data;
			long nowTime = System.currentTimeMillis();
			for (ExcitionActivity tempActivity : KExcitingDataManager.mExcitingDataManager.getDatas()) {

				if (!tempActivity.caTime.isInEffectTime(nowTime)) {
					continue;
				}
				{
					int setlv = setlvs.strongSetLv;
					if (tempActivity.isSenstiveForEquiStrongSetLv()) {
						data = getActivityData(tempActivity.id);
						for (RewardRule rule : tempActivity.ruleMap.values()) {
							if (!data.receiveRules.contains(rule.ruleId)) {
								if (setlv >= rule.minEquiStrongSetLv) {
									isStatusChange = true;
									break;
								}
							}
						}
						if (isStatusChange) {
							break;
						}
					}
				}
				{
					int setlv = setlvs.stoneSetLv;
					if (tempActivity.isSenstiveForEquiStoneSetLv()) {
						data = getActivityData(tempActivity.id);
						for (RewardRule rule : tempActivity.ruleMap.values()) {
							if (!data.receiveRules.contains(rule.ruleId)) {
								if (setlv >= rule.minEquiStoneSetLv) {
									isStatusChange = true;
									break;
								}
							}
						}
						if (isStatusChange) {
							break;
						}
					}
				}
				{
					int setlv = setlvs.starSetLv;
					if (tempActivity.isSenstiveForEquiStarSetLv()) {
						data = getActivityData(tempActivity.id);
						for (RewardRule rule : tempActivity.ruleMap.values()) {
							if (!data.receiveRules.contains(rule.ruleId)) {
								if (setlv >= rule.minEquiStarSetLv) {
									isStatusChange = true;
									break;
								}
							}
						}
						if (isStatusChange) {
							break;
						}
					}
				}
			}
		} finally {
			rwLock.unlock();
		}
		return isStatusChange;
	}

	/**
	 * <pre>
	 * 某个活动的统计数据
	 * 只针对需要活动期间统计的数据
	 * 
	 * @author CamusHuang
	 * @creation 2013-7-6 上午10:12:49
	 * </pre>
	 */
	class RoleActivityData {
		final int activityId; // 活动ID
		private boolean isPayed;// 是否已支付
		private int totalCharge; // 期间累计充值钻石
		private int totalPay; // 期间累计消费钻石
		private long totalOnlineInMills; // 期间累计在线时长（毫秒）
		private int totalLoginDay; // 期间连续登录天数
		private int totalUsePhyPow;// 期间消耗体力

		// 已领取的奖励规则
		private Set<Integer> receiveRules = new HashSet<Integer>();
		// 各规则领奖的次数
		private Map<Integer,AtomicInteger> ruleTimeMap = new HashMap<Integer,AtomicInteger>();

		RoleActivityData(int activityId) {
			this.activityId = activityId;
		}
		
		private boolean clearTime(int ruleId) {
			AtomicInteger count = ruleTimeMap.get(ruleId);
			if (count == null) {
				return false;
			}
			if(count.get()>0){
				count.set(0);
				return true;
			}
			return false;
		}

		private int getTime(int ruleId) {
			AtomicInteger count = ruleTimeMap.get(ruleId);
			if (count == null) {
				return 0;
			}

			return count.get();
		}

		private int increaseTime(int ruleId) {
			AtomicInteger count = ruleTimeMap.get(ruleId);
			if (count == null) {
				count = new AtomicInteger();
				ruleTimeMap.put(ruleId, count);
			}

			return count.incrementAndGet();
		}

		/**
		 * <pre>
		 * 
		 * 
		 * @param activity
		 * @param chargeIngot
		 * @return 是否条件满足
		 * @author CamusHuang
		 * @creation 2014-1-11 下午3:34:51
		 * </pre>
		 */
		private boolean addTotalCharge(ExcitionActivity activity, int chargeIngot) {
			int oldValue = totalCharge;
			totalCharge += chargeIngot;
			for (RewardRule rule : activity.ruleMap.values()) {
				if (!receiveRules.contains(rule.ruleId)) {
					if (oldValue < rule.totalCharge && rule.totalCharge <= totalCharge) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * <pre>
		 * 
		 * 
		 * @param activity
		 * @param payIngot
		 * @return 是否条件满足
		 * @author CamusHuang
		 * @creation 2014-1-11 下午3:34:51
		 * </pre>
		 */
		private boolean addTotalPay(ExcitionActivity activity, int payIngot) {
			int oldValue = totalPay;
			totalPay += payIngot;
			for (RewardRule rule : activity.ruleMap.values()) {
				if (!receiveRules.contains(rule.ruleId)) {
					if (oldValue < rule.totalPay && rule.totalPay <= totalPay) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * <pre>
		 * 
		 * 
		 * @param activity
		 * @param usePhyPow
		 * @return 是否条件满足
		 * @author CamusHuang
		 * @creation 2014-1-11 下午3:34:51
		 * </pre>
		 */
		private boolean addTotalUsePhyPow(ExcitionActivity activity, int usePhyPow) {
			int oldValue = totalUsePhyPow;
			totalUsePhyPow += usePhyPow;
			for (RewardRule rule : activity.ruleMap.values()) {
				if (!receiveRules.contains(rule.ruleId)) {
					if (oldValue < rule.totalUsePhyPow && rule.totalUsePhyPow <= totalUsePhyPow) {
						return true;
					}
				}
			}
			return false;
		}

		private void addTotalOnline(long onlineTime) {
			totalOnlineInMills += onlineTime;
		}

		private void addLoginDay() {
			totalLoginDay++;
		}
	}
}
