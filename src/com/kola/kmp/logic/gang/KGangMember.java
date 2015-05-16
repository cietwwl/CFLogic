package com.kola.kmp.logic.gang;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.gang.impl.KAGang;
import com.kola.kgame.cache.gang.impl.KAGangMember;
import com.kola.kgame.db.dataobject.DBGangMemberData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.text.HyperTextTool;

public class KGangMember extends KAGangMember {

	/* 今日贡献值 */
	private long todayContribution;
	/* 历史贡献值 */
	private long totalContribution;
	// 角色离开军团时，捐献次数从{@link KGangMember}迁移到{@link KRoleGangData}
	// 角色进入军团时，捐献次数从{@link KRoleGangData}迁移到{@link KGangMember}
	private Map<Integer, Integer> contributionData = new HashMap<Integer, Integer>();// 捐献次数
	/* 客户端最近同步捐献数据的时间:不用保存到DB */
	private long lastSynContributionDataTime;

	// 勋章
	private int medalRank;
	private boolean hasShowedMedalRank;
	// /////////////////////////////////

	private static final String JSON_VER = "0";// 版本
	//
	private static final String JSON_BASEINFO = "A";// 基础信息
	private static final String JSON_BASEINFO_TODAY_CONTRIBUTION = "1";
	private static final String JSON_BASEINFO_TOTAL_CONTRIBUTION = "2";
	private static final String JSON_BASEINFO_MEDAL_RANK = "3";// 勋章名次
	private static final String JSON_BASEINFO_MEDAL_SHOW = "4";// 勋章是否已经显示过
	private static final String JSON_CONTRIBUTION_DATA = "B";// 捐献次数
	
	

	KGangMember(KGang owner, DBGangMemberData dbdata) {
		super(owner, dbdata.getId(), dbdata.getOwnerId(), dbdata.getType(), dbdata.getCreateTimeMillis());
		// 以防父类【在子类的全局属性还没有执行初始化时】回调子类decodeCA方法导致NullPointerException
		this.decodeCA(dbdata.getCustomizeAttribute());
	}

	KGangMember(KGang owner, long _roleId, KGangPositionEnum positionEnum) {
		super(owner, _roleId, positionEnum.sign);
	}

	private void decodeCA(String jsonStr) {
		try {
			JSONObject json = new JSONObject(jsonStr);
			// 由底层调用,解释出逻辑层数据
			int ver = json.getInt(JSON_VER);// 默认版本
			switch (ver) {
			case 0:
				decodeBaseinfo(json.getJSONObject(JSON_BASEINFO));
				decodeContributionData(json.getJSONObject(JSON_CONTRIBUTION_DATA));
				break;
			}
		} catch (Exception ex) {
			_LOGGER.error("decode数据时发生错误 gangId=" + owner.getId() + " ----丢失数据！", ex);
		}
	}

	private void decodeBaseinfo(JSONObject json) throws Exception {
		this.todayContribution = json.getInt(JSON_BASEINFO_TODAY_CONTRIBUTION);
		this.totalContribution = json.getInt(JSON_BASEINFO_TOTAL_CONTRIBUTION);
		this.medalRank = json.optInt(JSON_BASEINFO_MEDAL_RANK);
		this.hasShowedMedalRank = json.optBoolean(JSON_BASEINFO_MEDAL_SHOW);
	}

	private void decodeContributionData(JSONObject obj) throws JSONException {
		for (Iterator<String> it = obj.keys(); it.hasNext();) {
			String key = it.next();
			int value = obj.getInt(key);
			contributionData.put(Integer.parseInt(key), value);
		}
	}

	@Override
	protected String encodeCA() {
		owner.rwLock.lock();
		try {
			JSONObject obj = new JSONObject();
			obj.put(JSON_VER, 0);// 默认版本
			//
			obj.put(JSON_BASEINFO, encodeBaseinfo());
			obj.put(JSON_CONTRIBUTION_DATA, encodeContributionData());
			return obj.toString();
		} catch (Exception ex) {
			_LOGGER.error("encode数据时发生错误 gangId=" + owner.getId() + " ----丢失数据！", ex);
			return null;
		} finally {
			owner.rwLock.unlock();
		}
	}

	private JSONObject encodeBaseinfo() throws Exception {

		JSONObject json = new JSONObject();
		json.put(JSON_BASEINFO_TODAY_CONTRIBUTION, todayContribution);
		json.put(JSON_BASEINFO_TOTAL_CONTRIBUTION, totalContribution);
		json.put(JSON_BASEINFO_MEDAL_RANK, medalRank);
		json.put(JSON_BASEINFO_MEDAL_SHOW, hasShowedMedalRank);
		return json;
	}

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
	 * @deprecated 由所属军团统一调用
	 * @param newDayStartTime
	 * @author CamusHuang
	 * @creation 2014-4-10 上午10:25:35
	 * </pre>
	 */
	boolean notifyForDayChange() {

		boolean result = clearContributionTime();

		if (todayContribution > 0) {
			todayContribution = 0;
			super.notifyDB();
			return true;
		}
		return result;
	}

	void changePosition(KGangPositionEnum newPosition) {
		if (newPosition == null) {
			return;
		}
		setType(newPosition.sign);
	}

	long getTodayContribution() {
		return todayContribution;
	}

	long getTotalContribution() {
		return totalContribution;
	}

	void addContribution(long addValue) {
		if (addValue == 0) {
			return;
		}
		owner.rwLock.lock();
		try {
			todayContribution += addValue;
			totalContribution += addValue;
			notifyDB();
		} finally {
			owner.rwLock.unlock();
		}
	}

	private boolean clearContributionTime() {
		rwLock.lock();
		try {
			if (!contributionData.isEmpty()) {
				contributionData.clear();
				notifyDB();
				return true;
			}
			return false;
		} finally {
			rwLock.unlock();
		}
	}

	void setContributionTime(int type, int contributionTime) {
		rwLock.lock();
		try {
			contributionData.put(type, contributionTime);
			notifyDB();
		} finally {
			rwLock.unlock();
		}
	}

	int getContributionTime(int type) {
		rwLock.lock();
		try {
			Integer oldValue = contributionData.get(type);
			if (oldValue != null) {
				return oldValue;
			}
			return 0;
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 直接获取缓存，谨慎使用
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-14 下午4:17:06
	 * </pre>
	 */
	Map<Integer, Integer> getContributionDataCache() {
		return contributionData;
	}

	void setContributionData(Map<Integer, Integer> contributionData) {
		rwLock.lock();
		try {
			this.contributionData.clear();
			this.contributionData.putAll(contributionData);
			notifyDB();
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 是否团长或副团长
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2013-8-27 下午3:26:52
	 * </pre>
	 */
	public boolean isSirs() {
		if (getType() == KGangPositionEnum.军团长.sign || getType() == KGangPositionEnum.副团长.sign) {
			return true;
		}
		return false;
	}

	/**
	 * <pre>
	 * CTODO 关于成员的基础信息如何优化？
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-10 下午1:58:19
	 * </pre>
	 */
	public String getRoleName() {
		return KSupportFactory.getRoleModuleSupport().getRoleName(_roleId);
	}
	
	public String getExtRoleName() {
		return KSupportFactory.getRoleModuleSupport().getRoleExtName(_roleId);
	}

	public long getLastLoginTime() {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(_roleId);
		if (role == null) {
			return 0;
		}
		if (role.isOnline()) {
			return role.getLastJoinGameTime();
		}
		return role.getLastLeaveGameTime();
	}

	public long getLastSynContributionDataTime() {
		return lastSynContributionDataTime;
	}

	public void setLastSynContributionDataTime(long lastSynContributionDataTime) {
		this.lastSynContributionDataTime = lastSynContributionDataTime;
	}

	public KGangPositionEnum getPositionEnum() {
		return KGangPositionEnum.getEnum(getType());
	}

	void setMedal(int rank, boolean hasShowedMedalRank) {
		rwLock.lock();
		try {
			this.medalRank = rank;
			this.hasShowedMedalRank = hasShowedMedalRank;
			notifyDB();
		} finally {
			rwLock.unlock();
		}
	}

	public int getMedal() {
		return medalRank;
	}

	boolean hasShowedMedal() {
		return hasShowedMedalRank;
	}

	void clearMedal() {
		rwLock.lock();
		try {
			this.medalRank = 0;
			this.hasShowedMedalRank = false;
			notifyDB();
		} finally {
			rwLock.unlock();
		}
	}
}
