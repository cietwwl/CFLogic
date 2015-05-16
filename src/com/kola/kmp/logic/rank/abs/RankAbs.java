package com.kola.kmp.logic.rank.abs;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.dataaccess.KGameDBException;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kgame.db.DataIdGeneratorFactory;
import com.kola.kgame.db.dataaccess.DataAccesserFactory;
import com.kola.kgame.db.dataobject.DBRank;
import com.kola.kgame.db.dataobject.impl.DBRankImpl;
import com.kola.kmp.logic.competition.KCompetitor;
import com.kola.kmp.logic.util.KXmlWriter;

/**
 * <pre>
 * 排行榜
 * 
 * 工作原理：
 * 1.不断监听元素变化数据并加入到监听缓存
 * 2.定时对监听缓存进行排序并保存到发布缓存
 * 
 * @author CamusHuang
 * @creation 2013-6-10 下午6:37:02
 * </pre>
 */
public abstract class RankAbs<E extends ElementAbs, T extends RankTypeInterface> {
	public final static Logger _LOGGER = KGameLogger.getLogger(RankAbs.class);

	public final ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();

	protected final T rankType;

	protected CacheData<E, T> tempCacheData = new CacheData<E, T>(this);// 临时缓存
	protected PublishData<E, T> publishCacheData = new PublishData<E, T>(this);// 发布缓存

	public static class CacheData<E extends ElementAbs, T extends RankTypeInterface> implements Cloneable {
		private RankAbs<E, T> owner;
		/** KEY=ID */
		private Map<Long, E> elementMap = new LinkedHashMap<Long, E>();
		private E lastT;// 最差的一名，用作冲榜门槛，此对象不允许修改

		private CacheData(RankAbs<E, T> rank) {
			this.owner = rank;
		}

		private void reset(Map<Long, E> elementMap, E lastT) {
			this.elementMap.clear();
			for (E t : elementMap.values()) {
				try {
					this.elementMap.put(t.elementId, (E) t.clone());
				} catch (CloneNotSupportedException e) {
					_LOGGER.error(e.getMessage(), e);
				}
			}
			try {
				this.lastT = lastT == null ? null : (E) lastT.clone();
			} catch (CloneNotSupportedException e) {
				_LOGGER.error(e.getMessage(), e);
			}
		}

		public boolean isFull() {
			owner.rwlock.readLock().lock();
			try {
				return elementMap.size() >= owner.rankType.getMaxLen();
			} finally {
				owner.rwlock.readLock().unlock();
			}
		}

		public void insert(E t) {
			owner.rwlock.writeLock().lock();
			try {
				// 加入缓存
				elementMap.put(t.elementId, t);
				// 刷新门槛
				updateLast(t);
			} finally {
				owner.rwlock.writeLock().unlock();
			}
		}

		public E getLastE() {
			owner.rwlock.readLock().lock();
			try {
				return lastT;
			} finally {
				owner.rwlock.readLock().unlock();
			}
		}

		public E getElement(long elementId) {
			owner.rwlock.readLock().lock();
			try {
				return elementMap.get(elementId);
			} finally {
				owner.rwlock.readLock().unlock();
			}
		}

		/**
		 * <pre>
		 * 从临时缓存清除
		 * 
		 * @param elementId
		 * @author CamusHuang
		 * @creation 2014-4-13 上午11:16:51
		 * </pre>
		 */
		public void removeElement(long elementId) {
			owner.rwlock.writeLock().lock();
			try {
				elementMap.remove(elementId);
			} finally {
				owner.rwlock.writeLock().unlock();
			}
		}

		/**
		 * <pre>
		 * 刷新门槛
		 * 
		 * @param t
		 * @author CamusHuang
		 * @creation 2014-2-20 下午7:02:13
		 * </pre>
		 */
		public void updateLast(E t) {
			owner.rwlock.writeLock().lock();
			try {
				if (lastT == null) {
					lastT = t;
				} else if (lastT.compareTo(t) > 0) {// -1,0,1
					lastT = t;
				}
			} finally {
				owner.rwlock.writeLock().unlock();
			}
		}

		public int size() {
			return elementMap.size();
		}

	}

	public static class PublishData<E extends ElementAbs, T extends RankTypeInterface> implements Cloneable {
		private RankAbs<E, T> owner;
		/** KEY=ID */
		private Map<Long, E> unmodifiableElementMap = Collections.emptyMap();
		private List<E> unmodifiableElementList = Collections.emptyList();;

		private PublishData(RankAbs<E, T> rank) {
			this.owner = rank;
		}

		private void reset(Map<Long, E> elementMap) {
			LinkedList<E> tempList = new LinkedList<E>();
			tempList.addAll(elementMap.values());
			// 重新排序
			resort(tempList);
			// 转为不可修改的List
			unmodifiableElementList = Collections.unmodifiableList(new ArrayList<E>(tempList));
			//
			Map<Long, E> tempMap = new HashMap<Long, E>();
			for (E t : unmodifiableElementList) {
				tempMap.put(t.elementId, t);
			}
			// 转为不可修改的Map
			unmodifiableElementMap = Collections.unmodifiableMap(tempMap);
		}

		/**
		 * <pre>
		 * 从发布缓存查找
		 * 
		 * @param elementId
		 * @return
		 * @author CamusHuang
		 * @creation 2014-4-13 上午11:17:25
		 * </pre>
		 */
		public E getElement(long elementId) {
			owner.rwlock.readLock().lock();
			try {
				return unmodifiableElementMap.get(elementId);
			} finally {
				owner.rwlock.readLock().unlock();
			}
		}

		/**
		 * <pre>
		 * 从发布缓存查找
		 * 
		 * @param rank
		 * @return
		 * @author CamusHuang
		 * @creation 2014-4-13 上午11:17:10
		 * </pre>
		 */
		public E getElementByRank(int rank) {
			owner.rwlock.readLock().lock();
			try {
				if (unmodifiableElementList.isEmpty()) {
					return null;
				}
				if (unmodifiableElementList.size() < rank) {
					return null;
				}
				return unmodifiableElementList.get(rank - 1);
			} finally {
				owner.rwlock.readLock().unlock();
			}
		}

		private E getLastE() {
			int size = unmodifiableElementList.size();
			E lastT = size == 0 ? null : unmodifiableElementList.get(size - 1);
			return lastT;
		}

		public List<E> getUnmodifiableElementList() {
			owner.rwlock.readLock().lock();
			try {
				return unmodifiableElementList;
			} finally {
				owner.rwlock.readLock().unlock();
			}
		}

		/**
		 * <pre>
		 * 重排，删除多余元素
		 * 
		 * @author camus
		 * @creation 2013-6-10 下午11:58:45
		 * </pre>
		 */
		private void resort(LinkedList<E> elementList) {
			// 排序
			Collections.sort(elementList);

			// 记录排名变化的ID，提交时效任务异步通知其它模块
			Set<Long> changedElementSet = new HashSet<Long>();

			// 删除溢出的元素
			int maxLen = owner.rankType.getMaxLen();
			while (elementList.size() > maxLen) {
				E removeT = elementList.removeLast();
				if (removeT.setRank(0)) {
					// 原榜上玩家被挤出榜
					changedElementSet.add(removeT.elementId);
				}
			}

			// 重新设置元素的排名
			int rank = 0;
			for (E t : elementList) {
				rank++;
				if (t.setRank(rank)) {
					// 新玩家进榜或者原榜上玩家排名变化
					changedElementSet.add(t.elementId);
				}
			}

			// 提交时效任务异步通知其它模块
			owner.actionAfterResort(changedElementSet);
		}
	}

	protected RankAbs(T rankType) {
		this.rankType = rankType;
	}

	public CacheData<E, T> getTempCacheData() {
		return tempCacheData;
	}

	public PublishData<E, T> getPublishData() {
		return publishCacheData;
	}

	/**
	 * <pre>
	 * 时效通知：发布排行榜
	 * 
	 * @deprecated 本方法负责发布并保存排行榜
	 * @author CamusHuang
	 * @creation 2014-2-20 下午7:12:53
	 * </pre>
	 */
	public void onTimeSignalForPublish(boolean isSaveToDB, boolean isSaveToFile, boolean isFileNameAddDate) {
		rwlock.writeLock().lock();
		try {
			actionBeforePublish(tempCacheData.elementMap);
			// 用临时缓存去发布
			publishCacheData.reset(tempCacheData.elementMap);
			// 将发布结果重置入临时缓存
			tempCacheData.reset(publishCacheData.unmodifiableElementMap, publishCacheData.getLastE());
		} finally {
			rwlock.writeLock().unlock();
		}

		// boolean isSaveToDB, boolean isSaveToFile, boolean isFileNameAddDate
		save(isSaveToDB, isSaveToFile, isFileNameAddDate);
	}

	/**
	 * <pre>
	 * 发布前的操作
	 * 例如将所有角色元素的军团名称赋值
	 * 
	 * @param elementMap
	 * @author CamusHuang
	 * @creation 2014-4-13 上午10:38:52
	 * </pre>
	 */
	protected abstract void actionBeforePublish(Map<Long, E> elementMap);

	/**
	 * <pre>
	 * 发布过程中元素重新排序后的操作
	 * 
	 * 
	 * @param elementMap
	 * @author CamusHuang
	 * @creation 2014-4-13 上午10:38:52
	 * </pre>
	 */
	protected abstract void actionAfterResort(Set<Long> changedElementSet);

	protected abstract E newRankElement(DBRank e);

	/**
	 * <pre>
	 * 
	 * @deprecated 彻底清空报排行榜，谨慎使用
	 * @author CamusHuang
	 * @creation 2014-5-20 上午11:44:29
	 * </pre>
	 */
	public void clear() {
		rwlock.writeLock().lock();
		try {
			save(false, true, true);
			//
			// 清空发布缓存
			publishCacheData.reset(Collections.<Long, E> emptyMap());
			// 清空临时缓存
			tempCacheData.reset(Collections.<Long, E> emptyMap(), null);
			//
			save(true, false, false);
		} finally {
			rwlock.writeLock().unlock();
		}
	}

	/**
	 * <pre>
	 * 从DB加载
	 * 
	 * @author CamusHuang
	 * @creation 2013-6-11 下午12:42:29
	 * </pre>
	 */
	public void load() throws Exception {
		rwlock.writeLock().lock();
		try {
			// 将数据加载到临时缓存
			tempCacheData.elementMap.clear();

			List<DBRank> list = null;
			try {
				list = DataAccesserFactory.getRankDataAccesser().getRankDataByType(rankType.getSign());
			} catch (KGameDBException ex) {
				throw new KGameServerException("加载" + rankType.name() + "数据时出现异常！", ex);
			}
			if (list != null) {

				for (DBRank dbRank : list) {
					E t = newRankElement(dbRank);
					tempCacheData.elementMap.put(t.elementId, t);
				}

				// 发布一次排行榜
				// boolean isSaveToDB, boolean isSaveToFile, boolean isFileNameAddDate
				onTimeSignalForPublish(false, false, false);
			}
		} finally {
			rwlock.writeLock().unlock();
		}
	}

	/**
	 * <pre>
	 * 保存排行榜
	 * 
	 * @param isAddDate 是否在文件名称后面添加日期戳（即每次保存不同的文件名）
	 * @author CamusHuang
	 * @creation 2014-2-21 上午10:00:11
	 * </pre>
	 */
	public void save(boolean isSaveToDB, boolean isSaveToFile, boolean isFileNameAddDate) {
		List<E> list = getPublishData().getUnmodifiableElementList();
		//
		if (isSaveToDB) {
			saveToDB(list);
		}
		//
		if (isSaveToFile) {
			String addStr = isFileNameAddDate ? UtilTool.DATE_FORMAT7.format(new Date()) : "";
			saveToXML(list, rankType.getSaveDirPath(), rankType.getSaveFileName() + addStr, rankType.getSaveFileNameSuffix());
		}
	}

	/**
	 * <pre>
	 * 保存排行榜
	 * 指定保存路径
	 * 
	 * @param saveDirPath
	 * @param saveFileName
	 * @author CamusHuang
	 * @creation 2013-7-22 上午12:37:21
	 * </pre>
	 */
	private void saveToXML(List<E> list, String saveDirPath, String saveFileName, String saveFileNameSuffix) {

		String url = saveDirPath + saveFileName + saveFileNameSuffix;
		KXmlWriter writer = null;
		try {
			File file = new File(saveDirPath);
			file.mkdirs();

			writer = new KXmlWriter(url, true);

			Element root = writer.getRoot();
			root.setAttribute("排行榜类型", rankType.name());
			root.setAttribute("保存时间", UtilTool.DATE_FORMAT.format(new Date()));
			Element element;
			for (E t : list) {
				element = new Element("rank");
				t.saveToXML(element);
				writer.addElement(element);
			}
			writer.output();

			_LOGGER.warn("保存排行榜成功！排行榜类型={}，路径={}", rankType.name(), url);

		} catch (Exception e) {
			_LOGGER.error("保存排行榜出错！排行榜类型={}，路径={}", rankType.name(), url);
			_LOGGER.error(e.getMessage(), e);
			print(list);
			return;
		}
	}

	private void saveToDB(List<E> eList) {

		try {
			// <元素ID，DBRank>
			Map<Long, DBRank> nowDBMap = new HashMap<Long, DBRank>();
			{
				List<DBRank> nowDBList = DataAccesserFactory.getRankDataAccesser().getRankDataByType(rankType.getSign());
				for (DBRank db : nowDBList) {
					nowDBMap.put(db.getElementId(), db);
				}
			}

			// ////////////////////////
			List<DBRank> insertList = new ArrayList<DBRank>();
			List<DBRank> updateList = new ArrayList<DBRank>();
			List<Long> deleteList = new ArrayList<Long>();
			// 步骤1：按排行榜找出元素对应的DBRank，刷新数据；
			// 步骤2：按排行榜剩余元素，利用现有的DBRank，刷新数据；
			// 步骤3：如果有多余的DBRank，则删除
			// 步骤4：如果有排行榜有剩余元素，则新建
			List<E> tempEList = new ArrayList<E>(eList);
			{
				// 步骤1：按排行榜找出元素对应的DBRank，刷新数据；
				for (Iterator<E> it = tempEList.iterator(); it.hasNext();) {
					E e = it.next();
					DBRank db = nowDBMap.remove(e.elementId);
					if (db != null) {
						it.remove();
						//
						int oldRank = db.getRank();
						JSONObject newJaonCA = new JSONObject();
						try {
							e.saveToDB(db, newJaonCA);
						} catch (JSONException e1) {
							_LOGGER.error(e1.getMessage());
						}
						String newCA = newJaonCA.toString();
						//
						if (oldRank != db.getRank() || !newCA.equals(db.getAttribute())) {
							db.setAttribute(newCA);
							// 需要更新
							updateList.add(db);
						}
					}
				}

				// 步骤2：按排行榜剩余元素，利用现有的DBRank，刷新数据；
				Iterator<DBRank> dbIt = nowDBMap.values().iterator();
				for (Iterator<E> it = tempEList.iterator(); it.hasNext() && dbIt.hasNext();) {
					E e = it.next();
					DBRank db = dbIt.next();
					{
						it.remove();
						dbIt.remove();
						//
						JSONObject newJaonCA = new JSONObject();
						try {
							e.saveToDB(db, newJaonCA);
						} catch (JSONException e1) {
							_LOGGER.error(e1.getMessage());
						}
						String newCA = newJaonCA.toString();
						db.setAttribute(newCA);
						// 需要更新
						updateList.add(db);
					}
				}

				// 步骤3：如果有多余的DBRank，则删除
				for (DBRank db : nowDBMap.values()) {
					deleteList.add(db.getDBId());
				}

				// 步骤4：如果有排行榜有剩余元素，则新建
				for (Iterator<E> it = tempEList.iterator(); it.hasNext();) {
					E e = it.next();
					DBRank db = new DBRankImpl();
					db.setDBId(DataIdGeneratorFactory.getRankIdGenerator().nextId());
					db.setRankType(this.rankType.getSign());
					{
						JSONObject newJaonCA = new JSONObject();
						try {
							e.saveToDB(db, newJaonCA);
						} catch (JSONException e1) {
							_LOGGER.error(e1.getMessage());
						}
						String newCA = newJaonCA.toString();
						db.setAttribute(newCA);
						// 需要新增
						insertList.add(db);
					}
				}
			}
			//
			DataAccesserFactory.getRankDataAccesser().addRanks(insertList);
			DataAccesserFactory.getRankDataAccesser().updateRanks(updateList);
			DataAccesserFactory.getRankDataAccesser().deleteRanks(deleteList);
			_LOGGER.debug("排行榜类型={}，插入数量={}，更新数量={}，删除数量={}", rankType.name(), insertList.size(), updateList.size(), deleteList.size());
		} catch (KGameDBException e) {
			_LOGGER.error("保存排行榜出错！排行榜类型={}", rankType.name());
			_LOGGER.error(e.getMessage(), e);
			return;
		}
	}

	private void print(List<E> list) {
		_LOGGER.warn("排行榜类型={} 保存时间={}", rankType.name(), UtilTool.DATE_FORMAT.format(new Date()));
		for (E t : list) {
			t.print(_LOGGER);
		}
	}
}
