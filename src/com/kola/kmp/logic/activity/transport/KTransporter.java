package com.kola.kmp.logic.activity.transport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kgame.db.DataIdGeneratorFactory;
import com.kola.kgame.db.dataobject.DBGameExtCA;
import com.kola.kgame.db.dataobject.impl.DBGameExtCAImpl;
import com.kola.kmp.logic.activity.KActivityRoleExtCaCreator;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KGameExtDataDBTypeEnum;
import com.kola.kmp.logic.other.KLimitTimeProduceActivityTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.TimeLimitActivityDataManager.TimeLimieProduceActivity;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.TimeLimitProducActivityTips;
import com.kola.kmp.logic.util.tips.TransportTips;

public class KTransporter implements KGameTimerTask {

	private static final Logger _LOGGER = KGameLogger
			.getLogger(KTransporter.class);

	private final static String JSON_KEY_ROLE_ID = "0";
	private final static String JSON_KEY_ROLE_NANE = "1";
	private final static String JSON_KEY_JOB = "2";
	private final static String JSON_KEY_START_TIME = "3";
	private final static String JSON_KEY_CARRIER = "4";
	private final static String JSON_KEY_INTERCEPT_COUNT = "5";
	private final static String JSON_KEY_HISTORY = "6";
	private final static String JSON_KEY_REST_EXP = "7";
	private final static String JSON_KEY_REST_POTENTIAL = "8";
	private final static String JSON_KEY_FIGHT_POWER = "9";
	private final static String JSON_KEY_EXP_ACTIVITY_RATE = "10";
	private final static String JSON_KEY_POTENTIAL_ACTIVITY_RATE = "11";
	private final static String JSON_KEY_IS_PRODUCE_ACTIVITY = "12";

	private long roleId;
	private int roleLv;
	private String roleName;
	private byte jobType;
	private long startTime;
	private long endTime;
	private AtomicInteger interceptCount;
	private CarrierData carrier;
	private List<InterceptHistory> interceptHistory = new ArrayList<InterceptHistory>();
	private int baseExp;
	private int basePotential;
	private int restExp;
	private int restPotential;
	private int fightPower;
	private float expActivityRate;
	private float potentialActivityRate;
	private boolean isTimeLimitProduceActivity = false;

	// private ReentrantLock lock = new ReentrantLock();

	public KTransporter(long roleId, String roleName, byte jobType, int roleLv,
			int fightPower, CarrierData carrier, float expActivityRate,
			float potentialActivityRate, boolean isTimeLimitProduceActivity) {
		this.roleId = roleId;
		this.roleLv = roleLv;
		this.roleName = roleName;
		this.jobType = jobType;
		this.startTime = System.currentTimeMillis();
		interceptCount = new AtomicInteger(0);
		this.carrier = carrier;
		this.endTime = startTime + carrier.getTranTime();
		this.restExp = KTransportManager.getCarrierBaseExp(
				carrier.getCarrierId(), roleLv);
		this.restPotential = KTransportManager.getCarrierBasePotential(
				carrier.getCarrierId(), roleLv);
		this.baseExp = restExp;
		this.basePotential = restPotential;
		this.fightPower = fightPower;
		this.expActivityRate = expActivityRate;
		this.potentialActivityRate = potentialActivityRate;
		this.isTimeLimitProduceActivity = isTimeLimitProduceActivity;
	}

	public KTransporter() {
	}

	@Override
	public String getName() {
		return KTransporter.class.getName();
	}

	@Override
	public Object onTimeSignal(KGameTimeSignal timeSignal)
			throws KGameServerException {
		if (!KTransportManager.isShutdown) {
			completeTransport();
		}
		return null;
	}

	@Override
	public void done(KGameTimeSignal timeSignal) {

	}

	@Override
	public void rejected(RejectedExecutionException e) {

	}

	public boolean decode(DBGameExtCA dbData) {
		this.roleLv = dbData.getCAType();
		boolean result = true;
		try {
			JSONObject obj = new JSONObject(dbData.getAttribute());
			this.roleId = obj.optLong(JSON_KEY_ROLE_ID, 0);
			this.roleName = obj.optString(JSON_KEY_ROLE_NANE);
			this.jobType = obj.optByte(JSON_KEY_JOB, (byte) 0);
			this.startTime = obj.optLong(JSON_KEY_START_TIME);
			int carrierId = obj.optInt(JSON_KEY_CARRIER, 0);
			if (KTransportManager.getCarrierDataMap().containsKey(carrierId)) {
				this.carrier = KTransportManager.getCarrierData(carrierId);
				this.endTime = startTime + carrier.getTranTime();
			}
			this.interceptCount = new AtomicInteger(obj.optInt(
					JSON_KEY_INTERCEPT_COUNT, 0));
			this.restExp = obj.optInt(JSON_KEY_REST_EXP, 0);
			this.restPotential = obj.optInt(JSON_KEY_REST_POTENTIAL, 0);
			this.fightPower = obj.optInt(JSON_KEY_FIGHT_POWER, 0);
			this.expActivityRate = obj.optFloat(JSON_KEY_EXP_ACTIVITY_RATE, 1);
			this.potentialActivityRate = obj.optFloat(
					JSON_KEY_POTENTIAL_ACTIVITY_RATE, 1);
			this.isTimeLimitProduceActivity = (obj.optInt(
					JSON_KEY_IS_PRODUCE_ACTIVITY, 0) == 1);
			if (this.roleId <= 0 || this.jobType <= 0 || this.carrier == null
					|| this.startTime <= 0) {
				result = false;
			}
		} catch (Exception e) {
			_LOGGER.error("解析物资运输者数据出错！", e);
		}
		return result;
	}

	private void decodeHistory(String data) {
		if (data != null) {
			String[] datas = data.split(",");
			for (int i = 0; i < datas.length; i++) {
				String[] historyData = datas[i].split(":");
				InterceptHistory history = new InterceptHistory(
						Long.parseLong(historyData[0]),
						Integer.parseInt(historyData[1]));
				this.interceptHistory.add(history);
			}
		}
	}

	public DBGameExtCA encode() {
		DBGameExtCA data = new DBGameExtCAImpl();
		data.setDBId(DataIdGeneratorFactory.getGameExtDataIdGenerator()
				.nextId());
		data.setDBType(KGameExtDataDBTypeEnum.物资运送数据.dbType);
		data.setCAType(roleLv);

		JSONObject obj = new JSONObject();
		try {
			obj.put(JSON_KEY_ROLE_ID, this.roleId);
			obj.put(JSON_KEY_ROLE_NANE, this.roleName);
			obj.put(JSON_KEY_JOB, this.jobType);
			obj.put(JSON_KEY_START_TIME, this.startTime);
			obj.put(JSON_KEY_CARRIER, carrier.getCarrierId());
			obj.put(JSON_KEY_INTERCEPT_COUNT, interceptCount.get());
			obj.put(JSON_KEY_HISTORY, encodeHistory());
			obj.put(JSON_KEY_REST_EXP, restExp);
			obj.put(JSON_KEY_REST_POTENTIAL, restPotential);
			obj.put(JSON_KEY_FIGHT_POWER, fightPower);
			obj.put(JSON_KEY_EXP_ACTIVITY_RATE, expActivityRate);
			obj.put(JSON_KEY_POTENTIAL_ACTIVITY_RATE, potentialActivityRate);
			obj.put(JSON_KEY_IS_PRODUCE_ACTIVITY,
					(isTimeLimitProduceActivity ? 1 : 0));
		} catch (Exception e) {
			_LOGGER.error("保存博彩系统自定义数据出错！角色ID：" + this.getRoleId(), e);
		}
		data.setAttribute(obj.toString());
		return data;
	}

	private String encodeHistory() {
		String data = "";
		InterceptHistory history;
		for (int i = 0; i < interceptHistory.size(); i++) {
			history = interceptHistory.get(i);
			data += history.roleId + ":" + history.roleLv;
			if (i < (interceptHistory.size() - 1)) {
				data += ",";
			}
		}
		return data;
	}

	public long getRoleId() {
		return roleId;
	}

	public int getRoleLv() {
		return roleLv;
	}

	public String getRoleName() {
		return roleName;
	}

	public byte getJobType() {
		return jobType;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public int getRestExp() {
		return restExp;
	}

	public int getRestPotential() {
		return restPotential;
	}

	public int getRestTimeSeconds() {
		long nowTime = System.currentTimeMillis();
		if (nowTime >= endTime) {
			return 0;
		} else {
			return (int) ((endTime - nowTime) / 1000);
		}
	}

	public int getInterceptCount() {
		return interceptCount.get();
	}

	public CarrierData getCarrier() {
		return carrier;
	}

	public List<InterceptHistory> getInterceptHistory() {
		return interceptHistory;
	}

	public void intercept() {
		interceptCount.incrementAndGet();
	}

	/**
	 * 记录拦截结果
	 * 
	 * @param intercepter
	 *            ，拦截者角色
	 * @param isWin
	 *            ，拦截者是否胜利
	 */
	public void interceptResult(KRole intercepter, boolean isWin) {
		InterceptHistory history = new InterceptHistory(intercepter.getId(),
				intercepter.getName(), intercepter.getJob(),
				intercepter.getLevel(), isWin);
		this.interceptHistory.add(history);
		if (isWin) {
			int reduceExpCount = KTransportManager
					.caculateInterceptLoseResource(roleLv, history.roleLv,
							baseExp);
			int reducePotentialCount = KTransportManager
					.caculateInterceptLoseResource(roleLv, history.roleLv,
							basePotential);
			restExp -= reduceExpCount;
			restPotential -= reducePotentialCount;

			KRole myRole = KSupportFactory.getRoleModuleSupport().getRole(
					this.roleId);
			if (myRole != null) {

				KTransportData data = KActivityRoleExtCaCreator
						.getActivityRoleExtData(this.roleId).getTransportData();
				if (data != null) {

					String carrierName = ((this.carrier == null) ? ""
							: this.carrier.getExtName());
					InterceptHistory info = new InterceptHistory(false,
							TransportTips.getTipsBeInterceptFaildInfo(
									UtilTool.DATE_FORMAT.format(System
											.currentTimeMillis()), intercepter
											.getExName(), carrierName,
									reduceExpCount, reducePotentialCount));
					data.addInterceptInfoQueue(info);
					if (myRole.isOnline()) {
						KTransportManager
								.pushInterceptHistoryData(myRole, info);
					}
				}

			}
		} else {
			KRole myRole = KSupportFactory.getRoleModuleSupport().getRole(
					this.roleId);
			if (myRole != null) {

				KTransportData data = KActivityRoleExtCaCreator
						.getActivityRoleExtData(this.roleId).getTransportData();
				if (data != null) {
					String carrierName = ((this.carrier == null) ? ""
							: this.carrier.getExtName());
					InterceptHistory info = new InterceptHistory(true,
							TransportTips.getTipsBeInterceptSuccessInfo(
									UtilTool.DATE_FORMAT.format(System
											.currentTimeMillis()), intercepter
											.getExName(), carrierName));
					data.addInterceptInfoQueue(info);
					if (myRole.isOnline()) {
						KTransportManager
								.pushInterceptHistoryData(myRole, info);
					}
				}
			}
		}
	}

	/**
	 * 启动运输
	 * 
	 * @return 返回是否运输结束
	 */
	public boolean startTranspot() {
		int time = getRestTimeSeconds();
		if (time > 0) {
			KGame.newTimeSignal(this, time, TimeUnit.SECONDS);
			return false;
		} else {
			completeTransport();
			return true;
		}
	}

	public void completeTransport() {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		if (role != null) {
			KTransportData data = KActivityRoleExtCaCreator
					.getActivityRoleExtData(this.roleId).getTransportData();
			endTransport();
			if (data != null) {
				data.finishTransport();
			}
			if (KSupportFactory.getRoleModuleSupport()
					.isRoleOnline(this.roleId)) {

				KDialogService.sendUprisingDialog(role,
						TransportTips.getTipsFinishTransport());
				KTransportManager.sendTransportData(role, false, false);
			}
		}
	}

	public void endTransport() {
		KTransportManager.removeTransporter(roleId);

		int exp = (int) (restExp * expActivityRate);
		int potential = (int) (restPotential * potentialActivityRate);
		List<AttValueStruct> attList = new ArrayList<AttValueStruct>();
		attList.add(new AttValueStruct(KGameAttrType.EXPERIENCE, exp));
		List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();
		moneyList.add(new KCurrencyCountStruct(KCurrencyTypeEnum.POTENTIAL,
				potential));
		BaseRewardData priceData = new BaseRewardData(attList, moneyList,
				Collections.<ItemCountStruct> emptyList(),
				Collections.<Integer> emptyList(),
				Collections.<Integer> emptyList());
		BaseMailContent mailContent = new BaseMailContent(
				TransportTips.getTipsTransportMailTitle(),
				TransportTips.getTipsTransportMailContent(carrier.getExtName(),
						getInterceptCount()), null, null);

		BaseMailRewardData mailReward = new BaseMailRewardData(1, mailContent,
				priceData);

		KSupportFactory.getMailModuleSupport().sendAttMailBySystem(this.roleId,
				mailReward, null);

		if (isTimeLimitProduceActivity) {
			TimeLimieProduceActivity activity = KSupportFactory
					.getExcitingRewardSupport().getTimeLimieProduceActivity(
							KLimitTimeProduceActivityTypeEnum.赚金币活动);
			if (activity != null) {
				KSupportFactory
						.getMailModuleSupport()
						.sendSimpleMailBySystem(
								roleId,
								activity.mailTitle,
								activity.mailContent);
			}
		}

		dispose();
	}

	// private int caculateSource(int baseSource) {
	// int reduceCount = 0;
	// for (InterceptHistory history : interceptHistory) {
	// if (history.isWin) {
	// reduceCount += KTransportManager.caculateInterceptResource(
	// roleLv, history.roleLv, baseSource);
	// }
	// }
	// return baseSource - reduceCount;
	// }

	private void dispose() {
		this.carrier = null;
		this.interceptHistory.clear();
		this.interceptHistory = null;
	}

	public void packMyTransporterMsg(KGameMessage sendMsg) {
		sendMsg.writeInt(carrier.getCarrierId());
		sendMsg.writeInt(getRestTimeSeconds());
		sendMsg.writeShort(getInterceptCount());
		sendMsg.writeInt(getRestExp());
		sendMsg.writeInt(getRestPotential());
	}

	public void packOtherTransporterMsg(KGameMessage sendMsg, int otherRoleLv) {
		/**
		 * <pre>
		 * long roleId 拥有载具的角色Id 
		 * String roleName 角色名称 
		 * byte jobType 角色职业类型 
		 * int roleLv 角色等级 
		 * int carrierId 当前载具Id
		 *       int    fightPower               角色战斗力
		 *       int    exp                      拦截获得经验
		 *       int    potential                拦截获得潜能
		 *       int    beInterceptCount         被拦截次数
		 *       int    restTimeSecond           剩余运输时间
		 * </pre>
		 */
		sendMsg.writeLong(roleId);
		sendMsg.writeUtf8String(roleName);
		sendMsg.writeByte(jobType);
		sendMsg.writeInt(roleLv);
		sendMsg.writeInt(carrier.getCarrierId());
		sendMsg.writeInt(fightPower);
		sendMsg.writeInt(KTransportManager.caculateInterceptGetResource(roleLv,
				otherRoleLv, baseExp));
		sendMsg.writeInt(KTransportManager.caculateInterceptGetResource(roleLv,
				otherRoleLv, basePotential));
		sendMsg.writeInt(getInterceptCount());
		sendMsg.writeInt(getRestTimeSeconds());
	}

	public static class InterceptHistory {
		public long roleId;
		public String roleName;
		public byte jobtype;
		public int roleLv;
		public boolean isWin;
		public String info = "";

		public InterceptHistory(long roleId, int roleLv) {
			this.roleId = roleId;
			this.roleLv = roleLv;
		}

		public InterceptHistory(long roleId, String roleName, byte jobtype,
				int roleLv, boolean isWin) {
			this.roleId = roleId;
			this.roleName = roleName;
			this.jobtype = jobtype;
			this.roleLv = roleLv;
			this.isWin = isWin;
		}

		public InterceptHistory(boolean isWin, String info) {
			this.isWin = isWin;
			this.info = info;
		}

	}

}
