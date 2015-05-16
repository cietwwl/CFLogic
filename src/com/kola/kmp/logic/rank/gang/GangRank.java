package com.kola.kmp.logic.rank.gang;

import java.util.Map;
import java.util.Set;

import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.db.dataobject.DBRank;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.rank.abs.ElementAbs;
import com.kola.kmp.logic.rank.abs.RankAbs;
import com.kola.kmp.logic.support.GangModuleSupport;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * <pre>
 * 排行榜
 * 
 * 工作原理：
 * 1.不断监听角色变化数据并加入到监听缓存
 * 2.定时对监听缓存进行排序并保存到发布缓存
 * 
 * @author CamusHuang
 * @creation 2013-6-10 下午6:37:02
 * </pre>
 */
public class GangRank<E extends GangRankElementAbs> extends RankAbs<E, KGangRankTypeEnum> {

	GangRank(KGangRankTypeEnum rankType) {
		super(rankType);
	}

	public CacheData<E, KGangRankTypeEnum> getTempCacheData() {
		return tempCacheData;
	}

	public PublishData<E, KGangRankTypeEnum> getPublishData() {
		return publishCacheData;
	}

	public KGangRankTypeEnum getType() {
		return rankType;
	}

	/**
	 * <pre>
	 * 通知所有排行榜更新
	 * 本方法只负责更新不以等级为排名条件的榜，即除了军团榜以外的其它榜
	 * 只改变临时缓存中已有元素的数据，不会新建元素
	 * 
	 * @param gang
	 * @param lv
	 * @return
	 * @author CamusHuang
	 * @creation 2013-7-7 上午11:43:58
	 * </pre>
	 */
	void resetGangLevelForAllRank(KGang gang, int lv) {
		if (rankType == KGangRankTypeEnum.全部军团 || rankType == KGangRankTypeEnum.军团战力) {
			return;
		}

		_LOGGER.debug("排行榜：军团升级通知 lv={}", lv);

		rwlock.readLock().lock();
		try {
			E e = tempCacheData.getElement(gang.getId());
			if (e == null) {
				// 之前未入榜则无视
				return;
			}
		} finally {
			rwlock.readLock().unlock();
		}

		rwlock.writeLock().lock();
		try {
			E e = tempCacheData.getElement(gang.getId());
			if (e == null) {
				// 之前未入榜则无视
				return;
			} else {
				// 之前已入榜，重设数据
				e.setElementLv(lv);
			}
		} finally {
			rwlock.writeLock().unlock();
		}
	}

	/**
	 * <pre>
	 * 军团战积分变化时调用 
	 * 本方法只负责更新以军团战积分为排名条件的榜，即军团战积分榜
	 * 如果临时缓存中已有元素，则重设其数据；否则新建元素
	 * 
	 * @param role
	 * @param warScore
	 * @author CamusHuang
	 * @creation 2013-7-7 上午11:43:58
	 * </pre>
	 */
	void notifyGangWarScore(KGang gang, int warScore) {
		if (warScore < 1 || rankType != KGangRankTypeEnum.军团战积分 || gang.getLevel() < KGangRankTypeEnum.军团战积分.getJoinMinLv()) {
			// 未达到门槛则无视
			return;
		}

		_LOGGER.debug("排行榜：军团战积分通知 warScore={}", warScore);

		rwlock.readLock().lock();
		try {
			E e = tempCacheData.getElement(gang.getId());
			if (e == null) {
				// 之前未入榜
				if (tempCacheData.isFull()) {
					// 临时榜已满员，如果冲榜者未达到临时榜门槛则无视
					if (warScore < ((GangRankElementWar) tempCacheData.getLastE()).getWarScore()) {
						return;
					}
				}
			}
		} finally {
			rwlock.readLock().unlock();
		}

		rwlock.writeLock().lock();
		try {
			E e = (E) tempCacheData.getElement(gang.getId());
			if (e == null) {
				// 之前未入榜
				if (tempCacheData.isFull()) {
					// 临时榜已满员，如果冲榜者未达到临时榜门槛则无视
					if (warScore < ((GangRankElementWar) tempCacheData.getLastE()).getWarScore()) {
						return;
					}
				}

				e = (E) new GangRankElementWar(gang.getId(), gang.getName(), gang.getLevel(), warScore);

				// 通知缓存榜插入新元素
				tempCacheData.insert(e);
			} else {
				// 之前已入榜，刷新数据
				((GangRankElementWar) e).setWarScore(warScore);

				// 通知缓存榜刷新门槛
				tempCacheData.updateLast(e);
			}
		} finally {
			rwlock.writeLock().unlock();
		}
	}

	/**
	 * <pre>
	 * 军团战报名时调用 
	 * 本方法只负责更新以军团繁荣度为排名条件的榜，即军团战报名榜
	 * 如果临时缓存中已有元素，则重设其数据；否则新增
	 * 
	 * @param role
	 * @param flourish
	 * @author CamusHuang
	 * @creation 2013-7-7 上午11:43:58
	 * </pre>
	 */
	void notifyGangWarSignup(KGang gang, int flourish) {
		if (flourish < 0 || rankType != KGangRankTypeEnum.军团战报名 || gang.getLevel() < KGangRankTypeEnum.军团战报名.getJoinMinLv()) {
			// 未达到门槛则无视
			return;
		}

		_LOGGER.debug("排行榜：军团战报名通知 flourish={}", flourish);

		rwlock.readLock().lock();
		try {
			E e = tempCacheData.getElement(gang.getId());
			if (e == null) {
				// 之前未入榜
				if (tempCacheData.isFull()) {
					// 临时榜已满员，如果冲榜者未达到临时榜门槛则无视
					if (flourish < ((GangRankElementWarSignUp) tempCacheData.getLastE()).getFlourish()) {
						return;
					}
				}
			}
		} finally {
			rwlock.readLock().unlock();
		}

		rwlock.writeLock().lock();
		try {
			E e = (E) tempCacheData.getElement(gang.getId());
			if (e == null) {
				// 之前未入榜
				if (tempCacheData.isFull()) {
					// 临时榜已满员，如果冲榜者未达到临时榜门槛则无视
					if (flourish < ((GangRankElementWarSignUp) tempCacheData.getLastE()).getFlourish()) {
						return;
					}
				}

				e = (E) new GangRankElementWarSignUp(gang.getId(), gang.getName(), gang.getLevel(), flourish);

				// 通知缓存榜插入新元素
				tempCacheData.insert(e);
			} else {
				// 之前已入榜，刷新数据
				((GangRankElementWarSignUp) e).setFlourish(flourish);

				// 通知缓存榜刷新门槛
				tempCacheData.updateLast(e);
			}
		} finally {
			rwlock.writeLock().unlock();
		}
	}

	/**
	 * <pre>
	 * 军团繁荣度变化时通知
	 * 本方法只负责更新以军团繁荣度为排名条件的榜，即军团战报名榜
	 * 只改变临时缓存中已有元素的数据，不会新建元素
	 * 
	 * @param gang
	 * @param flourish
	 * @return
	 * @author CamusHuang
	 * @creation 2013-7-7 上午11:43:58
	 * </pre>
	 */
	void notifyGangFlourish(KGang gang, int flourish) {
		if (rankType != KGangRankTypeEnum.军团战报名) {
			return;
		}

		_LOGGER.debug("排行榜：军团繁荣度通知 flourish={}", flourish);

		rwlock.readLock().lock();
		try {
			E e = tempCacheData.getElement(gang.getId());
			if (e == null) {
				// 之前未入榜则无视
				return;
			}
		} finally {
			rwlock.readLock().unlock();
		}

		rwlock.writeLock().lock();
		try {
			E e = tempCacheData.getElement(gang.getId());
			if (e == null) {
				// 之前未入榜则无视
				return;
			} else {
				// 之前已入榜，刷新数据
				((GangRankElementWarSignUp) e).setFlourish(flourish);

				// 通知缓存榜刷新门槛
				tempCacheData.updateLast(e);
			}
		} finally {
			rwlock.writeLock().unlock();
		}
	}

	/**
	 * <pre>
	 * 军团总战力变化时通知
	 * 本方法只负责更新以军团总战力为排名条件的榜，即军团战力榜
	 * 如果临时缓存中已有元素，则重设其数据；否则新建元素
	 * 
	 * @param gang
	 * @param battlePow
	 * @return
	 * @author CamusHuang
	 * @creation 2013-7-7 上午11:43:58
	 * </pre>
	 */
	void notifyGangBattlePower(KGang gang, int battlePow) {
		if (rankType != KGangRankTypeEnum.军团战力) {
			return;
		}

		_LOGGER.debug("排行榜：军团总战力通知 battlePow={}", battlePow);

		rwlock.readLock().lock();
		try {
			E e = tempCacheData.getElement(gang.getId());
			if (e == null) {
				// 之前未入榜
				if (tempCacheData.isFull()) {
					// 临时榜已满员，如果冲榜者未达到临时榜门槛则无视
					if (battlePow < ((GangRankElementPower) tempCacheData.getLastE()).getBattlePow()) {
						return;
					}
				}
			}
		} finally {
			rwlock.readLock().unlock();
		}

		rwlock.writeLock().lock();
		try {
			E e = (E) tempCacheData.getElement(gang.getId());
			if (e == null) {
				// 之前未入榜
				if (tempCacheData.isFull()) {
					// 临时榜已满员，如果冲榜者未达到临时榜门槛则无视
					if (battlePow < ((GangRankElementPower) tempCacheData.getLastE()).getBattlePow()) {
						return;
					}
				}

				e = (E) new GangRankElementPower(gang.getId(), gang.getName(), gang.getLevel(), battlePow);

				// 通知缓存榜插入新元素
				tempCacheData.insert(e);
			} else {
				// 之前已入榜，刷新数据
				((GangRankElementPower) e).setBattlePow(battlePow);

				// 通知缓存榜刷新门槛
				tempCacheData.updateLast(e);
			}
		} finally {
			rwlock.writeLock().unlock();
		}
	}

	/**
	 * <pre>
	 * 军团升级时调用 
	 * 本方法只负责更新以军团等级为排名条件的榜，即军团榜
	 * 如果临时缓存中已有元素，则重设其数据；否则新建元素
	 * 
	 * @param role
	 * @param lv
	 * @return 有可能冲击排行榜
	 * @author CamusHuang
	 * @creation 2013-7-7 上午11:43:58
	 * </pre>
	 */
	void notifyLevelUp(KGang gang, int lv, int exp) {
		if (rankType != KGangRankTypeEnum.全部军团 || lv < KGangRankTypeEnum.全部军团.getJoinMinLv()) {
			// 未达到门槛则无视
			return;
		}

		_LOGGER.debug("排行榜：军团战升级通知 lv={}, exp = {}", lv, exp);

		rwlock.readLock().lock();
		try {
			E e = tempCacheData.getElement(gang.getId());
			if (e == null) {
				// 之前未入榜
				if (tempCacheData.isFull()) {
					// 临时榜已满员，如果冲榜者未达到临时榜门槛则无视
					if (lv < tempCacheData.getLastE().getElementLv()) {
						return;
					}
				}
			}
		} finally {
			rwlock.readLock().unlock();
		}

		rwlock.writeLock().lock();
		try {
			E e = (E) tempCacheData.getElement(gang.getId());
			if (e == null) {
				// 之前未入榜
				if (tempCacheData.isFull()) {
					// 临时榜已满员，如果冲榜者未达到临时榜门槛则无视
					if (lv < tempCacheData.getLastE().getElementLv()) {
						return;
					}
				}

				e = (E) new GangRankElement(gang.getId(), gang.getName(), lv, exp);

				// 通知缓存榜插入新元素
				tempCacheData.insert(e);
			} else {
				// 之前已入榜，刷新数据
				((GangRankElement) e).setElementLv(lv);
				((GangRankElement) e).setExp(exp);

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
			throw new RuntimeException(e1.getMessage());
		}

		switch (rankType) {
		case 全部军团:
			return (E) new GangRankElement(e, jsonCA);
		case 军团战积分:
			return (E) new GangRankElementWar(e, jsonCA);
		case 军团战报名:
			return (E) new GangRankElementWarSignUp(e, jsonCA);
		case 军团战力:
			return (E) new GangRankElementPower(e, jsonCA);
		}
		return null;
	}

	@Override
	protected void actionBeforePublish(Map<Long, E> elementMap) {
		// CTODO 设置每个元素的冗余属性

		if (rankType == KGangRankTypeEnum.军团战力) {
			GangModuleSupport gangSupport = KSupportFactory.getGangSupport();
			PublishData<E, KGangRankTypeEnum> pdata = getPublishData();
			for (E e : elementMap.values()) {
				GangRankElementPower ee = (GangRankElementPower) e;
				if (ee.getBattlePow() < 1) {
					KGang gang = gangSupport.getGang(ee.elementId);
					if (gang == null) {
						ElementAbs pe = pdata.getElement(ee.elementId);
						if (pe == null) {
							ee.setBattlePow(0);
						} else {
							ee.setBattlePow(((GangRankElementPower) pe).getBattlePow());
						}
					} else {
						ee.setBattlePow(gang.countGangBattlePower());
					}
				}
			}
		}
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
			GangRankTaskManager.RankChangeNotifyTask.instance.notifyData(rankType, changedElementSet);
		}
	}
}
