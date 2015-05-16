package com.kola.kmp.logic.gang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.management.timer.Timer;

import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.gang.impl.KAGang;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kgame.db.dataobject.DBGangMemberData;
import com.kola.kmp.logic.other.KColorFunEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;
import com.kola.kmp.logic.util.text.HyperTextTool;
import com.kola.kmp.logic.util.tips.GangTips;

public class KGang extends KAGang<KGangMember> {

	private String extGangName;// 染色的军团名称
	private int exp = 0;// 经验
	private int warScore = 0;// 军团战积分
	private int flourish = 0;// 繁荣度（军团战报名期间可修改、报名结束时清0）
	private int level = KGangDataManager.mGangLevelDataManager.getDefaultLevel().LegionLv;// 等级
	private long resource = KGangConfig.getInstance().initResource;// 军团资金
	private int contributionTime;// 当天捐献次数
	private String notice = GangTips.暂无公告;// 公告
	// ---------------- 以下数据不需要永久保存

	// <角色ID,邀请CD结束时间>
	private Map<Long, Long> inviteRoleDatas = new HashMap<Long, Long>();
	// 世界邀请CD结束时间
	private long inviteForWorldCDEndTime;

	// /////////////////////////////////

	private static final String JSON_VER = "0";// 版本
	//
	private static final String JSON_BASEINFO = "A";// 基础信息
	private static final String JSON_BASEINFO_LV = "1";
	private static final String JSON_BASEINFO_EXP = "2";
	private static final String JSON_BASEINFO_RESOURCE = "3";
	private static final String JSON_BASEINFO_NOTICE = "4";
	private static final String JSON_BASEINFO_CONTRIBUTION_TIME = "5";
	private static final String JSON_BASEINFO_WARSOCRE = "6";
	private static final String JSON_BASEINFO_FLOURISH = "7";

	KGang() {
		super();
	}

	KGang(String gangName, long _createRoleId) {
		super(gangName, _createRoleId, 1);

		extGangName = HyperTextTool.extColor(gangName, KColorFunEnum.军团名称);
	}

	@Override
	protected Map<Long, KGangMember> initMembers(List<DBGangMemberData> dbdatas) {
		Map<Long, KGangMember> result = new HashMap<Long, KGangMember>();
		for (DBGangMemberData dbdata : dbdatas) {
			KGangMember data = new KGangMember(this, dbdata);
			result.put(data._roleId, data);
		}
		return result;
	}

	protected void notifyInitFinished() {
		extGangName = HyperTextTool.extColor(_gangName, KColorFunEnum.军团名称);
		//
		// 添加角色ID与军团ID映射
		for (Long roleId : getAllElementRoleIds()) {
			KGangLogic.mGangMappingDataManager.putRoleIdToGangId(roleId, _id);
		}
	}

	public String getExtName() {
		return extGangName;
	}

	@Override
	protected void decodeCA(String attribute) {
		try {
			JSONObject json = new JSONObject(attribute);
			// 由底层调用,解释出逻辑层数据
			int ver = json.getInt(JSON_VER);// 默认版本
			switch (ver) {
			case 0:
				decodeBaseinfo(json.getJSONObject(JSON_BASEINFO));
				break;
			}
		} catch (Exception ex) {
			_LOGGER.error("decode数据时发生错误 gangId=" + _id + " ----丢失数据！", ex);
		}
	}

	private void decodeBaseinfo(JSONObject json) throws Exception {
		this.exp = json.getInt(JSON_BASEINFO_EXP);
		this.level = json.getInt(JSON_BASEINFO_LV);
		this.resource = json.getInt(JSON_BASEINFO_RESOURCE);
		this.notice = json.getString(JSON_BASEINFO_NOTICE);
		this.contributionTime = json.getInt(JSON_BASEINFO_CONTRIBUTION_TIME);
		this.warScore = json.optInt(JSON_BASEINFO_WARSOCRE);
		this.flourish = json.optInt(JSON_BASEINFO_FLOURISH);
	}

	protected String encodeCA() {
		rwLock.lock();
		try {
			JSONObject obj = new JSONObject();
			obj.put(JSON_VER, 0);// 默认版本
			//
			obj.put(JSON_BASEINFO, encodeBaseinfo());
			return obj.toString();
		} catch (Exception ex) {
			_LOGGER.error("encode数据时发生错误 gangId=" + _id + " ----丢失数据！", ex);
			return null;
		} finally {
			rwLock.unlock();
		}
	}

	private JSONObject encodeBaseinfo() throws Exception {
		JSONObject json = new JSONObject();
		json.put(JSON_BASEINFO_EXP, exp);
		json.put(JSON_BASEINFO_LV, level);
		json.put(JSON_BASEINFO_RESOURCE, resource);
		json.put(JSON_BASEINFO_NOTICE, notice);
		json.put(JSON_BASEINFO_CONTRIBUTION_TIME, contributionTime);
		json.put(JSON_BASEINFO_WARSOCRE, warScore);
		json.put(JSON_BASEINFO_FLOURISH, flourish);
		return json;
	}

	public final void addMember(KGangMember member) {
		rwLock.lock();
		try {
			super.addMember(member);

			// 添加角色ID与军团ID映射
			KGangLogic.mGangMappingDataManager.putRoleIdToGangId(member._roleId, _id);

			// 恢复其捐献数据
			{
				KRoleGangData roleData = KGangRoleExtCACreator.getData(member._roleId, false);
				if (roleData != null) {
					long time = roleData.getContributionTime();
					Map<Integer, Integer> data = roleData.getContributionDataCache();
					if (!data.isEmpty() && !UtilTool.isBetweenDay(System.currentTimeMillis(), time)) {
						// 同一天
						member.setContributionData(data);
					}
				}
			}
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 成员需要从DB删除时请调用此方法
	 * 注意：本方法会从缓存中删除，然后交给垃圾回收器
	 * 
	 * @param deletedItem
	 * @author CamusHuang
	 * @creation 2014-2-12 下午7:26:02
	 * </pre>
	 */
	public final KGangMember notifyElementDelete(long roleId) {
		rwLock.lock();
		try {
			KGangMember mem = super.notifyElementDelete(roleId);
			if (mem != null) {
				// 取消角色ID与军团ID映射
				KGangLogic.mGangMappingDataManager.removeRoleIdToGangId(roleId);
				// 备份其捐献数据
				Map<Integer, Integer> data = mem.getContributionDataCache();
				if (!data.isEmpty()) {
					KRoleGangData roleData = KGangRoleExtCACreator.getData(roleId, true);
					if (roleData != null) {
						roleData.setContributionData(mem.getContributionDataCache());
					}
				}
			}
			return mem;
		} finally {
			rwLock.unlock();
		}
	}

	public int getExp() {
		return exp;
	}

	void setExp(int exp) {
		rwLock.lock();
		try {
			if (this.exp != exp) {
				this.exp = exp;
				notifyDB();
			}
		} finally {
			rwLock.unlock();
		}
	}

	public int getLevel() {
		return level;
	}

	void setLevel(int level) {
		rwLock.lock();
		try {
			if (this.level != level) {
				this.level = level;
				notifyDB();
			}
		} finally {
			rwLock.unlock();
		}
	}

	int getContributionTime() {
		return contributionTime;
	}

	void setContributionTime(int contributionTime) {
		rwLock.lock();
		try {
			if (this.contributionTime != contributionTime) {
				this.contributionTime = contributionTime;
				notifyDB();
			}
		} finally {
			rwLock.unlock();
		}
	}

	long getResource() {
		return resource;
	}

	public void changeResource(long changeValue) {
		rwLock.lock();
		try {
			this.resource += changeValue;
			if (this.resource < 0) {
				this.resource = 0;
			}
			notifyDB();
		} finally {
			rwLock.unlock();
		}
	}

	String getNotice() {
		return notice;
	}

	void setNotice(String notice) {
		this.notice = notice;
		notifyDB();
	}

	/**
	 * <pre>
	 * 军团战积分
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-13 下午3:22:01
	 * </pre>
	 */
	public int getWarSocre() {
		return warScore;
	}

	public void addWarSocre(int addValue) {
		rwLock.lock();
		try {
			warScore += addValue;
			notifyDB();
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 繁荣度
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-5-7 下午3:33:56
	 * </pre>
	 */
	public int getFlourish() {
		return flourish;
	}

	/**
	 * <pre>
	 * 军团战报名期间可修改、报名结束时清0
	 * 
	 * @param newValue
	 * @author CamusHuang
	 * @creation 2014-5-18 上午11:53:01
	 * </pre>
	 */
	public void setFlourish(int newValue) {
		rwLock.lock();
		try {
			if (flourish != newValue) {
				flourish = newValue;
				notifyDB();
			}
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 计算军团成员总战力
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-5-19 下午5:16:48
	 * </pre>
	 */
	public int countGangBattlePower() {
		int result = 0;
		KRole role = null;
		RoleModuleSupport support = KSupportFactory.getRoleModuleSupport();
		for (Long roleId : getAllElementRoleIds()) {
			role = support.getRole(roleId);
			if (role != null) {
				result += role.getBattlePower();
			}
		}
		return result;
	}

	int countPosition(KGangPositionEnum position) {
		rwLock.lock();
		try {
			int type = position.sign;
			int count = 0;
			Map<Long, KGangMember> map = super.getAllElementsCache();
			for (KGangMember mem : map.values()) {
				if (mem.getType() == type) {
					count++;
				}
			}
			return count;
		} finally {
			rwLock.unlock();
		}
	}

	public KGangMember searchPosition(KGangPositionEnum position) {
		rwLock.lock();
		try {
			int type = position.sign;
			Map<Long, KGangMember> map = super.getAllElementsCache();
			for (KGangMember mem : map.values()) {
				if (mem.getType() == type) {
					return mem;
				}
			}
			return null;
		} finally {
			rwLock.unlock();
		}
	}

	public boolean isPosition(long roleId, KGangPositionEnum position) {
		rwLock.lock();
		try {
			KGangMember mem = getMember(roleId);
			if (mem == null) {
				return false;
			}
			return mem.getType() == position.sign;
		} finally {
			rwLock.unlock();
		}
	}

	List<KGangMember> searchPositions(KGangPositionEnum position) {
		rwLock.lock();
		try {
			List<KGangMember> result = new ArrayList<KGangMember>();
			int type = position.sign;
			Map<Long, KGangMember> map = super.getAllElementsCache();
			for (KGangMember mem : map.values()) {
				if (mem.getType() == type) {
					result.add(mem);
				}
			}
			return result;
		} finally {
			rwLock.unlock();
		}
	}
	
	/**
	 * <pre>
	 * 返回指定军团指定职位的成员角色ID
	 * 
	 * @param positions
	 * @return
	 * @author CamusHuang
	 * @creation 2014-11-30 下午9:37:45
	 * </pre>
	 */
	List<Long> searchPositions(Set<KGangPositionEnum> positions) {
		rwLock.lock();
		try {
			List<Long> result = new ArrayList<Long>();
			for (KGangMember mem : super.getAllElementsCache().values()) {
				if (positions.contains(mem.getPositionEnum())) {
					result.add(mem._roleId);
				}
			}
			return result;
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 是否能邀请角色
	 * 受CD限制
	 * 
	 * @param id
	 * @return
	 * @author CamusHuang
	 * @creation 2014-10-24 上午11:26:21
	 * </pre>
	 */
	boolean addInvite(long roleId) {
		rwLock.lock();
		try {
			long nowTime = System.currentTimeMillis();
			clearOldInvite(nowTime);

			Long endTimeL = inviteRoleDatas.get(roleId);
			if (endTimeL != null && endTimeL > nowTime) {
				return false;
			}
			inviteRoleDatas.put(roleId, nowTime + KGangConfig.getInstance().InviteCDTimeForRole);
			return true;
		} finally {
			rwLock.unlock();
		}
	}

	private void clearOldInvite(long nowTime) {
		// 清理已经失效的邀请
		for (Iterator<Entry<Long, Long>> it = inviteRoleDatas.entrySet().iterator(); it.hasNext();) {
			Entry<Long, Long> entry = it.next();
			if (entry.getValue() <= nowTime) {
				it.remove();
			}
		}
	}

	boolean isInviteEffect(long roleId) {
		rwLock.lock();
		try {
			long nowTime = System.currentTimeMillis();
			clearOldInvite(nowTime);

			Long endTimeL = inviteRoleDatas.get(roleId);
			if (endTimeL != null) {
				return true;
			}
			return false;
		} finally {
			rwLock.unlock();
		}
	}

	long checkInviteForWoldCDTime() {
		rwLock.lock();
		try {
			long nowTime = System.currentTimeMillis();
			if (inviteForWorldCDEndTime > nowTime) {
				return inviteForWorldCDEndTime - nowTime;
			}
			inviteForWorldCDEndTime = nowTime + KGangConfig.getInstance().InviteCDTimeForWorld;
			return 0;
		} finally {
			rwLock.unlock();
		}
	}
}
