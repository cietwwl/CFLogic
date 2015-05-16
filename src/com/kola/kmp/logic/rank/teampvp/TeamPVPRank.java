package com.kola.kmp.logic.rank.teampvp;

import java.util.Map;
import java.util.Set;

import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.db.dataobject.DBRank;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.other.KRankTypeEnum;
import com.kola.kmp.logic.rank.abs.RankAbs;
import com.kola.kmp.logic.role.KRole;

/**
 * <pre>
 * 
 * 
 * @param <E>
 * @author CamusHuang
 * @creation 2014-9-3 下午12:06:45
 * </pre>
 */
public class TeamPVPRank<E extends TeamPVPRankElement> extends RankAbs<E, KTeamPVPRankTypeEnum> {

	TeamPVPRank(KTeamPVPRankTypeEnum rankType) {
		super(rankType);
	}

	public CacheData<E, KTeamPVPRankTypeEnum> getTempCacheData() {
		return tempCacheData;
	}

	public PublishData<E, KTeamPVPRankTypeEnum> getPublishData() {
		return publishCacheData;
	}

	public KTeamPVPRankTypeEnum getType() {
		return rankType;
	}

	/**
	 * <pre>
	 * 单纯队伍队长、队员变更时通知
	 * 只更新现存于排行榜中的队伍的信息，不新增入榜
	 * 
	 * @param tempId
	 * @param leaderRoleId
	 * @param leaderRoleName
	 * @param leaderRoleVip
	 * @param memRoleId
	 * @param memRoleName
	 * @param memRoleVip
	 * @author CamusHuang
	 * @creation 2014-9-3 下午3:56:11
	 * </pre>
	 */
	void resetTeamMemChange(long tempId, long leaderRoleId, String leaderRoleName, int leaderRoleVip, long memRoleId, String memRoleName, int memRoleVip) {

		_LOGGER.debug("排行榜：成员通知 ");

		rwlock.readLock().lock();
		try {
			E e = tempCacheData.getElement(tempId);
			if (e == null) {
				// 之前未入榜则无视
				return;
			}
		} finally {
			rwlock.readLock().unlock();
		}

		rwlock.writeLock().lock();
		try {
			E e = tempCacheData.getElement(tempId);
			if (e == null) {
				// 之前未入榜则无视
				return;
			} else {
				// 之前已入榜，重设数据
				e.resetRoles(leaderRoleId, leaderRoleName, leaderRoleVip, memRoleId, memRoleName, memRoleVip);
			}
		} finally {
			rwlock.writeLock().unlock();
		}
	}

	/**
	 * <pre>
	 * 单纯队伍战力变化时通知
	 * 只更新现存于排行榜中的队伍的信息，不新增入榜
	 * 
	 * @param tempId
	 * @param battlePower
	 * @author CamusHuang
	 * @creation 2014-9-3 下午3:56:02
	 * </pre>
	 */
	void resetTeamBattlePower(long tempId, int battlePower) {
		_LOGGER.debug("排行榜：战力通知 battlePower={}", battlePower);

		rwlock.readLock().lock();
		try {
			E e = tempCacheData.getElement(tempId);
			if (e == null) {
				// 之前未入榜则无视
				return;
			}
		} finally {
			rwlock.readLock().unlock();
		}

		rwlock.writeLock().lock();
		try {
			E e = tempCacheData.getElement(tempId);
			if (e == null) {
				// 之前未入榜则无视
				return;
			} else {
				// 之前已入榜，重设数据
				e.setBattlePower(battlePower);
			}
		} finally {
			rwlock.writeLock().unlock();
		}
	}

	/**
	 * <pre>
	 * 队伍所属段位、段级、胜点变化时通知
	 * 会根据数据新增入榜
	 * 
	 * @param tempId
	 * @param tempName
	 * @param lv 段级
	 * @param exp 胜点
	 * @param battlePow 队伍战力
	 * @author CamusHuang
	 * @creation 2014-9-3 下午3:31:00
	 * </pre>
	 */
	void notifyTempChange(long tempId, String tempName, int lv, int exp, int battlePow, long leaderRoleId, String leaderRoleName, int leaderRoleVip, long memRoleId, String memRoleName, int memRoleVip) {

		_LOGGER.debug("排行榜：天梯数据变化通知 lv={}, exp = {}", lv, exp);

		rwlock.readLock().lock();
		try {
			E e = tempCacheData.getElement(tempId);
			if (e == null) {
				// 之前未入榜
				if (tempCacheData.isFull()) {
					// 临时榜已满员，如果冲榜者未达到临时榜门槛则无视
					if (lv < tempCacheData.getLastE().getElementLv()) {
						return;
					}
					if (lv == tempCacheData.getLastE().getElementLv() && exp < tempCacheData.getLastE().getExp()) {
						return;
					}
				}
			}
		} finally {
			rwlock.readLock().unlock();
		}

		rwlock.writeLock().lock();
		try {
			E e = (E) tempCacheData.getElement(tempId);
			if (e == null) {
				// 之前未入榜
				if (tempCacheData.isFull()) {
					// 临时榜已满员，如果冲榜者未达到临时榜门槛则无视
					if (lv < tempCacheData.getLastE().getElementLv()) {
						return;
					}
					if (lv == tempCacheData.getLastE().getElementLv() && exp < tempCacheData.getLastE().getExp()) {
						return;
					}
				}

				e = (E) new TeamPVPRankElement(tempId, tempName, lv, exp, battlePow);
				e.resetRoles(leaderRoleId, leaderRoleName, leaderRoleVip, memRoleId, memRoleName, memRoleVip);

				// 通知缓存榜插入新元素
				tempCacheData.insert(e);
			} else {
				// 之前已入榜，刷新数据
				e.resetInfo(lv, exp, battlePow);
				e.resetRoles(leaderRoleId, leaderRoleName, leaderRoleVip, memRoleId, memRoleName, memRoleVip);

				// 通知缓存榜刷新门槛
				tempCacheData.updateLast(e);
			}
		} finally {
			rwlock.writeLock().unlock();
		}
	}

	protected E newRankElement(DBRank e) {

		JSONObject jsonCA = null;
		try {
			jsonCA = new JSONObject(e.getAttribute());
		} catch (JSONException e1) {
			throw new RuntimeException(e1.getMessage()+" dbId="+e.getDBId());
		}

		switch (rankType) {
		case 最强王者:
		case 钻石:
		case 白金:
		case 黄金:
		case 白银:
		case 青铜:
		default:
			return (E) new TeamPVPRankElement(e, jsonCA);
		}
	}

	@Override
	protected void actionBeforePublish(Map<Long, E> elementMap) {
		// CTODO 设置每个元素的冗余属性
		// for (E t : elementMap.values()) {
		// String gangName =
		// KSupportFactory.getGangSupport().getGangNameByRoleId(t.elementId);
		// t.setGangName(gangName);
		// }
	}

	@Override
	protected void actionAfterResort(Set<Long> changedElementSet) {
		if (!changedElementSet.isEmpty()) {
			// 提交时效任务异步通知其它模块
			TeamPVPRankTaskManager.RankChangeNotifyTask.instance.notifyData(rankType, changedElementSet);
		}
	}
}
