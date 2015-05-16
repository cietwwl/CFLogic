package com.kola.kmp.logic.gang.reswar;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

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
import com.kola.kgame.db.dataobject.DBGameExtCA;
import com.kola.kgame.db.dataobject.DBRank;
import com.kola.kgame.db.dataobject.impl.DBGameExtCAImpl;
import com.kola.kmp.logic.competition.KCompetitor;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.other.KGameExtDataDBTypeEnum;
import com.kola.kmp.logic.util.KXmlWriter;

/**
 * <pre>
 * 竞价榜
 * 
 * @author CamusHuang
 * @creation 2014-5-8 下午12:05:38
 * </pre>
 */
public class ResWarCityBidRank {

	public static final Logger _LOGGER = KGameLogger.getLogger(ResWarCityBidRank.class);

	// 读写锁
	private final ReentrantLock rwLock = new ReentrantLock();
	//
	public final int cityId;
	//
	private List<BPElement> elementList = new ArrayList<BPElement>();
	private List<BPElement> copyElementList = new ArrayList<BPElement>(elementList);
	private Map<Long, BPElement> elementMap = new HashMap<Long, BPElement>();
	private AtomicBoolean isDirty = new AtomicBoolean();

	ResWarCityBidRank(int cityId) {
		this.cityId = cityId;
	}

	void notifyBid(KGang gang, int price) {
		rwLock.lock();
		try {
			BPElement element = elementMap.get(gang.getId());
			if (element == null) {
				element = new BPElement(gang.getId(), gang.getName(), price);
				elementMap.put(element.elementId, element);
				elementList.add(element);
			} else {
				element.price += price;
			}

			// 重排
			resort();

			isDirty.set(true);
		} finally {
			rwLock.unlock();
		}
	}

	private void resort() {
		// 重排
		Collections.sort(elementList, RComparator.instance);

		// copy一份，并重新设置名次
		copyElementList = new ArrayList<BPElement>(elementList);
		int rank = 1;
		for (BPElement e : copyElementList) {
			e.rank = rank;
			rank++;
		}
	}

	BPElement getElement(long gangId) {
		return elementMap.get(gangId);
	}

	List<BPElement> getCopyElementList() {
		return copyElementList;
	}

	void clear() {
		rwLock.lock();
		try {
			elementList.clear();
			copyElementList.clear();
			elementMap.clear();
			isDirty.set(true);
		} finally {
			rwLock.unlock();
		}
	}

	void loadFromDB() {
		try {
			// <角色ID，DBElementImpl>
			Map<Long, DBElementImpl> nowDBMap = loadDB();
			{
				for (DBElementImpl dbRank : nowDBMap.values()) {
					BPElement element = new BPElement(dbRank, dbRank.getExtJSon());
					elementMap.put(element.elementId, element);
					elementList.add(element);
				}

				// 重排
				resort();
			}
		} catch (KGameDBException e) {
			_LOGGER.error("读取排行榜出错！城市ID={}", cityId);
			_LOGGER.error(e.getMessage(), e);
		}
	}

	void onTimeSignalForSave() {
		rwLock.lock();
		try {
			if (isDirty.compareAndSet(true, false)) {
				save(true, false, false);
			}
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 无条件保存排行榜
	 * 
	 * @param isAddDate 是否在文件名称后面添加日期戳（即每次保存不同的文件名）
	 * @author CamusHuang
	 * @creation 2014-2-21 上午10:00:11
	 * </pre>
	 */
	void save(boolean isSaveToDB, boolean isSaveToFile, boolean isFileNameAddDate) {
		List<BPElement> list = copyElementList;
		//
		if (isSaveToDB) {
			saveToDB(list);
		}
		//
		if (isSaveToFile) {
			String addStr = isFileNameAddDate ? UtilTool.DATE_FORMAT7.format(new Date()) : "";
			saveToXML(list, KResWarConfig.saveDirPath, KResWarConfig.saveFileName + cityId + addStr, KResWarConfig.saveFileNameSuffix);
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
	private void saveToXML(List<BPElement> list, String saveDirPath, String saveFileName, String saveFileNameSuffix) {

		String url = saveDirPath + saveFileName + saveFileNameSuffix;
		KXmlWriter writer = null;
		try {
			File file = new File(saveDirPath);
			file.mkdirs();

			writer = new KXmlWriter(url, true);

			Element root = writer.getRoot();
			root.setAttribute("城市ID", cityId + "");
			root.setAttribute("保存时间", UtilTool.DATE_FORMAT.format(new Date()));
			Element element;
			for (BPElement t : list) {
				element = new Element("rank");
				t.saveToXML(element);
				writer.addElement(element);
			}
			writer.output();

			_LOGGER.error("保存排行榜成功！城市ID={}，路径={}", cityId, url);

		} catch (Exception e) {
			_LOGGER.error("保存排行榜出错！城市ID={}，路径={}", cityId, url);
			_LOGGER.error(e.getMessage(), e);
			print(list);
			return;
		}
	}

	private Map<Long, DBElementImpl> loadDB() throws KGameDBException {
		// <角色ID，DBElementImpl>
		Map<Long, DBElementImpl> nowDBMap = new HashMap<Long, DBElementImpl>();
		{
			List<DBGameExtCA> nowDBList = DataAccesserFactory.getGameExtCADataAccesser().getDBGameExtCA(KGameExtDataDBTypeEnum.军团资源竞价榜.dbType, cityId);
			for (DBGameExtCA db : nowDBList) {
				DBElementImpl dbImpl = new DBElementImpl(db);
				nowDBMap.put(dbImpl.getElementId(), dbImpl);
			}
		}
		return nowDBMap;
	}

	private void saveToDB(List<BPElement> eList) {
		isDirty.set(false);
		try {
			// <角色ID，DBElementImpl>
			Map<Long, DBElementImpl> nowDBMap = loadDB();

			// ////////////////////////
			List<DBGameExtCA> insertList = new ArrayList<DBGameExtCA>();
			List<DBGameExtCA> updateList = new ArrayList<DBGameExtCA>();
			List<Long> deleteList = new ArrayList<Long>();
			// 步骤1：按排行榜找出元素对应的DBElementImpl，刷新数据；
			// 步骤2：按排行榜剩余元素，利用现有的DBElementImpl，刷新数据；
			// 步骤3：如果有多余的DBElementImpl，则删除
			// 步骤4：如果有排行榜有剩余元素，则新建
			List<BPElement> tempEList = new ArrayList<BPElement>(eList);
			{
				// 步骤1：按排行榜找出元素对应的DBElementImpl，刷新数据；
				for (Iterator<BPElement> it = tempEList.iterator(); it.hasNext();) {
					BPElement e = it.next();
					DBElementImpl db = nowDBMap.remove(e.elementId);
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
						//
						if (oldRank != db.getRank() || !newJaonCA.toString().equals(db.getExtJSon().toString())) {
							db.setExtJSon(newJaonCA);
							// 需要更新
							updateList.add(db.getDBGameExtCA());
						}
					}
				}

				// 步骤2：按排行榜剩余元素，利用现有的DBElementImpl，刷新数据；
				Iterator<DBElementImpl> dbIt = nowDBMap.values().iterator();
				for (Iterator<BPElement> it = tempEList.iterator(); it.hasNext() && dbIt.hasNext();) {
					BPElement e = it.next();
					DBElementImpl db = dbIt.next();
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
						db.setExtJSon(newJaonCA);
						// 需要更新
						updateList.add(db.getDBGameExtCA());
					}
				}

				// 步骤3：如果有多余的DBElementImpl，则删除
				for (DBElementImpl db : nowDBMap.values()) {
					deleteList.add(db.getDBId());
				}

				// 步骤4：如果有排行榜有剩余元素，则新建
				for (Iterator<BPElement> it = tempEList.iterator(); it.hasNext();) {
					BPElement e = it.next();
					DBElementImpl db = new DBElementImpl(cityId, DataIdGeneratorFactory.getGameExtDataIdGenerator().nextId());
					{
						JSONObject newJaonCA = new JSONObject();
						try {
							e.saveToDB(db, newJaonCA);
						} catch (JSONException e1) {
							_LOGGER.error(e1.getMessage());
						}
						db.setExtJSon(newJaonCA);
						// 需要新增
						insertList.add(db.getDBGameExtCA());
					}
				}
			}
			//
			DataAccesserFactory.getGameExtCADataAccesser().addDBGameExtCAs(insertList);
			DataAccesserFactory.getGameExtCADataAccesser().updateDBGameExtCAs(updateList);
			DataAccesserFactory.getGameExtCADataAccesser().deleteDBGameExtCAs(deleteList);
			_LOGGER.error("城市ID={}，插入数量={}，更新数量={}，删除数量={}", cityId, insertList.size(), updateList.size(), deleteList.size());
		} catch (KGameDBException e) {
			_LOGGER.error("保存排行榜出错！城市ID={}", cityId);
			_LOGGER.error(e.getMessage(), e);
			return;
		}
	}

	private void print(List<BPElement> list) {
		_LOGGER.warn("城市ID={} 保存时间={}", cityId, UtilTool.DATE_FORMAT.format(new Date()));
		for (BPElement e : list) {
			e.print(_LOGGER);
		}
	}

	class BPElement {

		private final static String Str_elementId = "eId";
		private final static String Str_elementName = "eName";
		private final static String Str_price = "price";
		private final static String Str_rank = "rank";

		int rank;
		long elementId;// 军团ID
		String elementName;// 军团名称
		int price;

		private BPElement(long gangId, String gangName, int price) {
			this.elementId = gangId;
			this.elementName = gangName;
			this.price = price;
		}

		private BPElement(DBElementImpl db, JSONObject jsonCA) {
			rank = db.getRank();
			elementId = db.getElementId();
			try {
				elementName = jsonCA.getString(Str_elementName);
				price = jsonCA.getInt(Str_price);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		void saveToXML(Element element) {
			element.setAttribute(Str_rank, String.valueOf(rank));
			element.setAttribute(Str_elementId, String.valueOf(elementId));
			element.setAttribute(Str_elementName, elementName);
			element.setAttribute(Str_price, String.valueOf(price));
		}

		void saveToDB(DBElementImpl db, JSONObject jsonCA) throws JSONException {
			db.setRank(rank);
			db.setElementId(elementId);
			jsonCA.put(Str_elementName, elementName);
			jsonCA.put(Str_price, price);
		}

		void print(Logger _LOGGER) {
			_LOGGER.warn("rank={},elementId={},elementName={},price={},", rank, elementId, elementName, price);
		}
	}

	public static class DBElementImpl {
		private DBGameExtCA _dbCa;
		// ----以下是真实CA---
		private long _eid;
		private int _rank;
		private JSONObject _attribute;
		//
		private final static String Str_elementId = "eId";
		private final static String Str_rank = "rank";
		private final static String Str_ca = "ca";

		DBElementImpl(DBGameExtCA dbCa) {
			_dbCa = dbCa;
			try {
				JSONObject json = new JSONObject(dbCa.getAttribute());
				_eid = json.getLong(Str_elementId);
				_rank = json.getInt(Str_rank);
				_attribute = json.getJSONObject(Str_ca);
			} catch (JSONException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}

		DBElementImpl(int rankType, long dbId) {
			_dbCa = new DBGameExtCAImpl();
			_dbCa.setDBId(dbId);
			_dbCa.setDBType(KGameExtDataDBTypeEnum.军团资源竞价榜.dbType);
			_dbCa.setCAType(rankType);
		}

		long getDBId() {
			return _dbCa.getDBId();
		}

		public long getElementId() {
			return _eid;
		}

		public void setElementId(long eid) {
			this._eid = eid;
		}

		public int getRank() {
			return _rank;
		}

		public void setRank(int rank) {
			this._rank = rank;
		}

		public JSONObject getExtJSon() {
			return _attribute;
		}

		public void setExtJSon(JSONObject attribute) {
			_attribute = attribute;
		}

		public DBGameExtCA getDBGameExtCA() {
			try {
				JSONObject json = new JSONObject();
				json.put(Str_elementId, _eid);
				json.put(Str_rank, _rank);
				json.put(Str_ca, _attribute);
				_dbCa.setAttribute(json.toString());
			} catch (JSONException e) {
				_LOGGER.error(e.getMessage(), e);
			}
			return _dbCa;
		}
	}

	static class RComparator implements Comparator<BPElement> {
		static final RComparator instance = new RComparator();

		@Override
		public int compare(BPElement o1, BPElement o2) {
			if (o1.price > o2.price) {
				return -1;
			}
			if (o1.price < o2.price) {
				return 1;
			}
			return 0;
		}

	}
}
