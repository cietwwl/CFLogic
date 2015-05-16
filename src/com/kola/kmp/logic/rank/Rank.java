package com.kola.kmp.logic.rank;

import java.util.Map;
import java.util.Set;

import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kgame.db.dataobject.DBRank;
import com.kola.kmp.logic.other.KRankTypeEnum;
import com.kola.kmp.logic.rank.abs.RankAbs;
import com.kola.kmp.logic.role.KRole;
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
public class Rank<E extends RankElementAbs> extends RankAbs<E, KRankTypeEnum> {

	Rank(KRankTypeEnum rankType) {
		super(rankType);
	}

	public KRankTypeEnum getType() {
		return rankType;
	}

	public CacheData<E, KRankTypeEnum> getTempCacheData() {
		return tempCacheData;
	}

	public PublishData<E, KRankTypeEnum> getPublishData() {
		return publishCacheData;
	}	
	
	/**
	 * <pre>
	 * 通知所有排行榜更新
	 * 本方法只负责更新不以等级为排名条件的榜，即除了总榜以外的其它榜
	 * 只改变临时缓存中已有元素的数据，不会新建元素
	 * 
	 * @param role
	 * @param lv
	 * @return
	 * @author CamusHuang
	 * @creation 2013-7-7 上午11:43:58
	 * </pre>
	 */
	void resetRoleLevelForAllRank(KRole role, int lv) {
		if (rankType == KRankTypeEnum.等级榜) {
			return;
		}

		_LOGGER.debug("排行榜：角色升级通知 lv={}", lv);

		rwlock.readLock().lock();
		try {
			E e = tempCacheData.getElement(role.getId());
			if (e == null) {
				// 之前未入榜则无视
				return;
			}
		} finally {
			rwlock.readLock().unlock();
		}

		rwlock.writeLock().lock();
		try {
			E e = tempCacheData.getElement(role.getId());
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
	 * 通知所有排行榜更新
	 * 本方法只负责更新不以战力为排名条件的榜，即除了战略榜以外的其它榜
	 * 只改变临时缓存中已有元素的数据，不会新建元素
	 * 
	 * @param role
	 * @param lv
	 * @return
	 * @author CamusHuang
	 * @creation 2013-7-7 上午11:43:58
	 * </pre>
	 */
	void resetRoleBattlePowerForAllRank(KRole role, int battlePower) {
		if (rankType == KRankTypeEnum.战力榜) {
			return;
		}

		_LOGGER.debug("排行榜：角色战力通知 battlePower={}", battlePower);

		rwlock.readLock().lock();
		try {
			E e = tempCacheData.getElement(role.getId());
			if (e == null) {
				// 之前未入榜则无视
				return;
			}
		} finally {
			rwlock.readLock().unlock();
		}

		rwlock.writeLock().lock();
		try {
			E e = tempCacheData.getElement(role.getId());
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
	 * 通知等级榜更新
	 * 本方法只负责更新以等级为排名条件的榜，即等级榜
	 * 如果临时缓存中已有元素，则重设其数据；否则新建元素
	 * 
	 * @param role
	 * @param lv
	 * @return
	 * @author CamusHuang
	 * @creation 2013-7-7 上午11:43:58
	 * </pre>
	 */
	void notifyRoleLevelUp(KRole role, int lv, int exp) {
		if (rankType != KRankTypeEnum.等级榜 || lv < KRankTypeEnum.等级榜.getJoinMinLv()) {
			// 未达到门槛则无视
			return;
		}

		_LOGGER.debug("排行榜：角色升级通知 lv={}, exp={}", lv, exp);

		rwlock.readLock().lock();
		try {
			E e = tempCacheData.getElement(role.getId());
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
			E e = tempCacheData.getElement(role.getId());
			if (e == null) {
				// 之前未入榜
				if (tempCacheData.isFull()) {
					// 临时榜已满员，如果冲榜者未达到临时榜门槛则无视
					if (lv < tempCacheData.getLastE().getElementLv()) {
						return;
					}
				}

				e = (E) new RankElementLevel(role.getId(), role.getName(), lv, exp, role.getJob(), role.getBattlePower(), UtilTool.getNotNullString(null));

				// 通知缓存榜插入新元素
				tempCacheData.insert(e);
			} else {
				// 之前已入榜，刷新数据
				e.setElementLv(lv);
				((RankElementLevel) e).setExp(exp);

				// 通知缓存榜刷新门槛
				tempCacheData.updateLast(e);
			}
		} finally {
			rwlock.writeLock().unlock();
		}
	}

	/**
	 * <pre>
	 * 通知战力榜更新
	 * 本方法只负责更新以战力为排名条件的榜，即战力榜
	 * 如果临时缓存中已有元素，则重设其数据；否则新建元素
	 * 
	 * @param role
	 * @param lv
	 * @return
	 * @author CamusHuang
	 * @creation 2013-7-7 上午11:43:58
	 * </pre>
	 */
	void notifyBattlePowerChange(KRole role, int battlePower) {
		if (rankType != KRankTypeEnum.战力榜 || role.getLevel() < KRankTypeEnum.战力榜.getJoinMinLv()) {
			// 未达到门槛则无视
			return;
		}

		_LOGGER.debug("排行榜：战力变化通知");

		rwlock.readLock().lock();
		try {
			E e = tempCacheData.getElement(role.getId());
			if (e == null) {
				// 之前未入榜
				if (tempCacheData.isFull()) {
					// 临时榜已满员，如果冲榜者未达到临时榜门槛则无视
					if (battlePower < tempCacheData.getLastE().getBattlePower()) {
						return;
					}
				}
			}
		} finally {
			rwlock.readLock().unlock();
		}

		rwlock.writeLock().lock();
		try {
			E e = tempCacheData.getElement(role.getId());
			if (e == null) {
				// 之前未入榜
				if (tempCacheData.isFull()) {
					// 临时榜已满员，如果冲榜者未达到临时榜门槛则无视
					if (battlePower < tempCacheData.getLastE().getBattlePower()) {
						return;
					}
				}

				e = (E) new RankElementPower(role.getId(), role.getName(), role.getLevel(), role.getJob(), role.getBattlePower(), UtilTool.getNotNullString(null));

				// 通知缓存榜插入新元素
				tempCacheData.insert(e);
			} else {
				// 之前已入榜，刷新数据
				e.setElementLv(role.getLevel());
				e.setBattlePower(battlePower);

				// 通知缓存榜刷新门槛
				tempCacheData.updateLast(e);
			}
		} finally {
			rwlock.writeLock().unlock();
		}
	}
	
	/**
	 * <pre>
	 * 通知随从榜更新
	 * 本方法只负责更新随从榜
	 * 如果临时缓存中已有元素，则重设其数据；否则新建元素
	 * 
	 * @param role
	 * @param lv
	 * @return
	 * @author CamusHuang
	 * @creation 2013-7-7 上午11:43:58
	 * </pre>
	 */
	void notifyPetInfoChange(KRole role, String petName, int petLv, int petPow) {
		if (rankType != KRankTypeEnum.随从榜 || role.getLevel() < KRankTypeEnum.随从榜.getJoinMinLv()) {
			// 未达到门槛则无视
			return;
		}

		_LOGGER.debug("排行榜：随从变化通知");

		rwlock.readLock().lock();
		try {
			E e = tempCacheData.getElement(role.getId());
			if (e == null) {
				// 之前未入榜
				if (tempCacheData.isFull()) {
					// 临时榜已满员，如果冲榜者未达到临时榜门槛则无视
					if (petPow < ((RankElementPet)tempCacheData.getLastE()).getPetPow()) {
						return;
					}
				}
			}
		} finally {
			rwlock.readLock().unlock();
		}

		rwlock.writeLock().lock();
		try {
			RankElementPet e = (RankElementPet)tempCacheData.getElement(role.getId());
			if (e == null) {
				// 之前未入榜
				if (tempCacheData.isFull()) {
					// 临时榜已满员，如果冲榜者未达到临时榜门槛则无视
					if (petPow < ((RankElementPet)tempCacheData.getLastE()).getPetPow()) {
						return;
					}
				}

				e = new RankElementPet(role.getId(), role.getName(), role.getLevel(), role.getJob(), role.getBattlePower(), UtilTool.getNotNullString(null));
				e.setPetName(petName).setPetPow(petPow).setPetLv(petLv);

				// 通知缓存榜插入新元素
				tempCacheData.insert((E)e);
			} else {
				// 之前已入榜，刷新数据
				e.setElementLv(role.getLevel());
				e.setPetName(petName).setPetPow(petPow).setPetLv(petLv);

				// 通知缓存榜刷新门槛
				tempCacheData.updateLast((E)e);
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
		case 战力榜:
			return (E) new RankElementPower(e, jsonCA);
		case 等级榜:
			return (E) new RankElementLevel(e, jsonCA);
		case 随从榜:
			return (E) new RankElementPet(e, jsonCA);
		case 竞技榜:
			return null;
		}
		return null;
	}

	@Override
	protected void actionBeforePublish(Map<Long, E> elementMap) {
		// 设置每个元素的所属军团
		for (E t : elementMap.values()) {
			String gangName = KSupportFactory.getGangSupport().getGangNameByRoleId(t.elementId);
			t.setGangName(gangName);
		}
	}

	@Override
	protected void actionAfterResort(Set<Long> changedElementSet) {
		if (!changedElementSet.isEmpty()) {
			// 提交时效任务异步通知其它模块
			RankTaskManager.RankChangeNotifyTask.instance.notifyData(rankType, changedElementSet);
		}
	}
}
