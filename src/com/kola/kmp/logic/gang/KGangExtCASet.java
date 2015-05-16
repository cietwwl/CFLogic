package com.kola.kmp.logic.gang;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.timer.Timer;

import com.koala.thirdpart.json.JSONArray;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.gang.impl.KAGangExtCA;
import com.kola.kgame.cache.gang.impl.KAGangExtCASet;
import com.kola.kgame.db.dataobject.DBGangExtCAData;
import com.kola.kmp.logic.gang.KGangExtCASet.GangDialyCache.GangDialy;

public class KGangExtCASet extends KAGangExtCASet<KAGangExtCA> {

	private final GangDialyCache dialyCache = new GangDialyCache(this);// 日志数据

	private ApplicationCache appCache;// 申请书数据

	private GangTechCache techCache;// 科技数据

	/* 捐献数据最近变化时间:不用保存到DB */
	private long contributionLastChangeTime;

	// ////////////////////////////////////

	KGangExtCASet() {
		super();
	}

	KGangExtCASet(KGang gang) {
		super(gang.getId(), gang.rwLock);
	}

	@Override
	protected Map<Integer, Map<Long, KAGangExtCA>> initDBMembers(List<DBGangExtCAData> dbdatas) {

		Map<Integer, Map<Long, KAGangExtCA>> result = new HashMap<Integer, Map<Long, KAGangExtCA>>();

		for (DBGangExtCAData dbdata : dbdatas) {
			Map<Long, KAGangExtCA> temp = result.get(dbdata.getType());
			if (temp == null) {
				temp = new HashMap<Long, KAGangExtCA>();
				result.put(dbdata.getType(), temp);
			}

			KGangExtCATypeEnum type = KGangExtCATypeEnum.getEnum(dbdata.getType());
			switch (type) {
			case 日志:
				GangDialy data = new GangDialy(this, dbdata);
				temp.put(data._id, data);
				break;
			case 申请书:
				appCache = new ApplicationCache(this, dbdata);
				temp.put(appCache._id, appCache);
				break;
			case 科技:
				techCache = new GangTechCache(this, dbdata);
				temp.put(techCache._id, techCache);
				break;
			}
		}

		// 初始化日志缓存
		Map<Long, KAGangExtCA> temp = result.get(KGangExtCATypeEnum.日志.sign);
		dialyCache.initData(temp);

		return result;
	}

	@Override
	protected void notifyInitFinished() {
		// CTODO
	}

	public long getContributionLastChangeTime() {
		return contributionLastChangeTime;
	}

	public void setContributionLastChangeTime(long contributionLastChangeTime) {
		this.contributionLastChangeTime = contributionLastChangeTime;
	}

	/**
	 * <pre>
	 * 获取申请书缓存
	 * 
	 * @deprecated 直接获取缓存，请谨慎使用
	 * @return
	 * @author CamusHuang
	 * @creation 2013-1-30 下午4:08:54
	 * </pre>
	 */
	public ApplicationCache getAppCache() {
		rwLock.lock();
		try {
			if (appCache == null) {
				appCache = new ApplicationCache(this);
				addMember(appCache);
			}
			return appCache;
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 获取科技缓存
	 * 
	 * @deprecated 直接获取缓存，请谨慎使用
	 * @return
	 * @author CamusHuang
	 * @creation 2013-1-30 下午4:08:54
	 * </pre>
	 */
	public GangTechCache getTechCache() {
		rwLock.lock();
		try {
			if (techCache == null) {
				techCache = new GangTechCache(this);
				addMember(techCache);
			}
			return techCache;
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 获取日志缓存
	 * 
	 * @deprecated 直接获取缓存，请谨慎使用
	 * @return
	 * @author CamusHuang
	 * @creation 2013-1-30 下午4:08:54
	 * </pre>
	 */
	public GangDialyCache getDialyCache() {
		return dialyCache;
	}

	/**
	 * <pre>
	 * 类型枚举
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-22 下午10:43:16
	 * </pre>
	 */
	public enum KGangExtCATypeEnum {
		日志(1), //
		申请书(2), //
		科技(3), //
		;

		public final int sign;

		private KGangExtCATypeEnum(int sign) {
			this.sign = sign;
		}

		// 所有枚举
		private static final Map<Integer, KGangExtCATypeEnum> typeMap = new HashMap<Integer, KGangExtCATypeEnum>();
		static {
			KGangExtCATypeEnum[] enums = KGangExtCATypeEnum.values();
			KGangExtCATypeEnum type;
			for (int i = 0; i < enums.length; i++) {
				type = enums[i];
				typeMap.put(type.sign, type);
			}
		}

		// //////////////////
		/**
		 * <pre>
		 * 通过标识数值获取枚举对象
		 * 
		 * @param sign
		 * @return
		 * @author CamusHuang
		 * @creation 2012-11-5 上午10:53:13
		 * </pre>
		 */
		public static KGangExtCATypeEnum getEnum(int sign) {
			return typeMap.get(sign);
		}
	}

	/**
	 * <pre>
	 * 军团日志缓存
	 * 每条日志作为一个扩展CA记录
	 * 
	 * @author CamusHuang
	 * @creation 2013-1-29 上午10:50:07
	 * </pre>
	 */
	public static class GangDialyCache {
		private final KGangExtCASet owner;

		/**
		 * 保存于扩展CA
		 */
		private final LinkedList<GangDialy> dataList = new LinkedList<GangDialy>();

		private GangDialyCache(KGangExtCASet owner) {
			this.owner = owner;
		}

		private void initData(Map<Long, KAGangExtCA> temp) {
			if (temp == null) {
				return;
			}

			for (KAGangExtCA obj : temp.values()) {
				dataList.add((GangDialy) obj);
			}
			// 按时间排序
			Collections.sort(dataList, new Comparator<GangDialy>() {

				@Override
				public int compare(GangDialy o1, GangDialy o2) {
					if (o1.getTime() < o2.getTime()) {
						return -1;
					}
					if (o1.getTime() > o2.getTime()) {
						return 1;
					}
					return 0;
				}
			});
		}

		/**
		 * <pre>
		 * 添加日志
		 * 
		 * @deprecated 仅限{@link KGangLogic#addDialy(KGang, String, boolean, boolean, boolean)}调用
		 * @param content
		 * @return
		 * @author CamusHuang
		 * @creation 2013-10-18 下午7:07:54
		 * </pre>
		 */
		GangDialy addDialy(String content) {
			GangDialy dialy = null;
			owner.rwLock.lock();
			try {
				if (dataList.size() >= KGangConfig.getInstance().DAILY_MAXLEN) {
					// 重用旧的
					dialy = dataList.removeFirst();
				} else {
					// 生成新的
					dialy = new GangDialy(owner);
					owner.addMember(dialy);
				}
				dialy.resetData(System.currentTimeMillis(), content);
				dataList.addLast(dialy);
				return dialy;
			} finally {
				owner.rwLock.unlock();
			}
		}

		/**
		 * <pre>
		 * 获取日志
		 * 
		 * @deprecated 直接获取缓存，请谨慎使用
		 * @return
		 * @author CamusHuang
		 * @creation 2013-1-30 下午4:08:54
		 * </pre>
		 */
		public List<GangDialy> getDataCache() {
			return dataList;
		}

		/**
		 * <pre>
		 * 军团日志
		 * @author CamusHuang
		 * @creation 2013-1-24 上午11:47:03
		 * </pre>
		 */
		public static class GangDialy extends KAGangExtCA {
			/** 时间 */
			private long time;
			/** 日期格式 */
			private String dateTime;
			/** 日志内容 */
			private String content;

			// /////////////////////////////////
			private static final String JSON_VER = "0";// 版本
			//
			private static final String JSON_TIME = "A";// 时间（分钟）
			private static final String JSON_CONTENT = "B";// 内容

			private GangDialy(KGangExtCASet owner) {
				super(owner, KGangExtCATypeEnum.日志.sign);
			}

			private GangDialy(KGangExtCASet owner, DBGangExtCAData dbdata) {
				super(owner, dbdata.getId(), dbdata.getType());
				// 以防父类【在子类的全局属性还没有执行初始化时】回调子类decodeCA方法导致NullPointerException
				this.decodeCA(dbdata.getAttribute());
			}

			private void resetData(long time, String content) {
				this.time = time;
				this.content = content;
				this.dateTime = KGangConfig.getInstance().DAILY_DATEFORMATE.format(new Date(time));
				super.notifyDB();
			}

			private void decodeCA(String jsonCA) {
				// 由底层调用,解释出逻辑层数据
				try {
					JSONObject obj = new JSONObject(jsonCA);
					int ver = obj.getInt(JSON_VER);// 默认版本
					// CEND 暂时只有版本0
					switch (ver) {
					case 0:
						time = obj.getLong(JSON_TIME) * Timer.ONE_MINUTE;
						dateTime = KGangConfig.getInstance().DAILY_DATEFORMATE.format(new Date(time));
						content = obj.getString(JSON_CONTENT);
						break;
					}

				} catch (Exception ex) {
					_LOGGER.error("decode数据时发生错误 gang=" + owner.getGangId() + " extCAId=" + _id + " ----丢失数据，存在运行隐患！", ex);
				}
			}

			@Override
			protected String encodeCA() {
				owner.rwLock.lock();
				// 构造一个数据对象给底层
				try {
					JSONObject obj = new JSONObject();
					obj.put(JSON_VER, 0);
					// CEND 暂时只有版本0
					obj.put(JSON_TIME, time / Timer.ONE_MINUTE);
					obj.put(JSON_CONTENT, content);
					return obj.toString();
				} catch (Exception ex) {
					_LOGGER.error("encode数据时发生错误 gang=" + owner.getGangId() + " extCAId=" + _id + " ----丢失数据！", ex);
					return "";
				} finally {
					owner.rwLock.unlock();
				}
			}

			long getTime() {
				return time;
			}

			String getDateTime() {
				return dateTime;
			}

			String getContent() {
				return content;
			}
		}
	}

	/**
	 * <pre>
	 * 申请书缓存
	 * 全体申请书作为一条扩展CA记录
	 * 
	 * @author CamusHuang
	 * @creation 2013-1-11 下午3:20:53
	 * </pre>
	 */
	public static class ApplicationCache extends KAGangExtCA {
		// <角色ID,申请时间>
		private LinkedHashMap<Long, Long> dataMap = new LinkedHashMap<Long, Long>();

		// //////////////////////////////
		private static final String JSON_VER = "0";// 版本
		//
		private static final String JSON_APP_DATA = "A";// 申请书数据

		private ApplicationCache(KGangExtCASet owner) {
			super(owner, KGangExtCATypeEnum.申请书.sign);
		}

		private ApplicationCache(KGangExtCASet owner, DBGangExtCAData dbdata) {
			super(owner, dbdata.getId(), dbdata.getType());
			// 以防父类【在子类的全局属性还没有执行初始化时】回调子类decodeCA方法导致NullPointerException
			this.decodeCA(dbdata.getAttribute());
		}

		private void decodeCA(String jsonCA) {
			// 由底层调用,解释出逻辑层数据
			try {
				JSONObject obj = new JSONObject(jsonCA);
				int ver = obj.getInt(JSON_VER);// 默认版本
				// CEND 暂时只有版本0
				switch (ver) {
				case 0:
					decodeAppData(obj.getJSONArray(JSON_APP_DATA));
					break;
				}

			} catch (Exception ex) {
				_LOGGER.error("decode数据时发生错误 gang=" + owner.getGangId() + " extCAId=" + _id + " ----丢失数据，存在运行隐患！", ex);
			}
		}

		private void decodeAppData(JSONArray obj) throws Exception {
			int len = obj.length();
			for (int i = 0; i < len; i++) {
				JSONArray temp = obj.getJSONArray(i);
				dataMap.put(temp.getLong(0), temp.getLong(1) * Timer.ONE_MINUTE);
			}
		}

		@Override
		protected String encodeCA() {
			owner.rwLock.lock();
			// 构造一个数据对象给底层
			try {
				JSONObject obj = new JSONObject();
				obj.put(JSON_VER, 0);
				// CEND 暂时只有版本0
				obj.put(JSON_APP_DATA, encodeAppData());
				return obj.toString();
			} catch (Exception ex) {
				_LOGGER.error("encode数据时发生错误 gang=" + owner.getGangId() + " extCAId=" + _id + " ----丢失数据！", ex);
				return "";
			} finally {
				owner.rwLock.unlock();
			}
		}

		private JSONArray encodeAppData() throws Exception {
			JSONArray obj = new JSONArray();
			for (Entry<Long, Long> entry : dataMap.entrySet()) {
				JSONArray temp = new JSONArray();
				temp.put(entry.getKey());
				temp.put(entry.getValue() / Timer.ONE_MINUTE);
				obj.put(temp);
			}
			return obj;
		}

		/**
		 * <pre>
		 * 添加申请
		 * 
		 * @deprecated 仅限{@link KGangLogic#addDialy(KGang, String, boolean, boolean, boolean)}调用
		 * @param content
		 * @return
		 * @author CamusHuang
		 * @creation 2013-10-18 下午7:07:54
		 * </pre>
		 */
		public void addApp(long roleId) {
			owner.rwLock.lock();
			try {
				if(dataMap.containsKey(roleId)){
					return;
				}
				
				if (dataMap.size() >= KGangConfig.getInstance().APP_MAXLEN) {
					// 删除旧的
					// 由于客户端是打开申请书界面才获取申请列表，所以不需要同步给客户端
					Iterator<Entry<Long, Long>> it = dataMap.entrySet().iterator();
					it.next();
					it.remove();
				}

				// 添加新的
				dataMap.put(roleId, System.currentTimeMillis());
				notifyDB();
			} finally {
				owner.rwLock.unlock();
			}
		}

		public boolean deleteApp(long roleId) {
			owner.rwLock.lock();
			try {
				Long result = dataMap.remove(roleId);
				if (result == null) {
					return false;
				} else {
					notifyDB();
					return true;
				}
			} finally {
				owner.rwLock.unlock();
			}
		}

		public boolean containApp(long roleId) {
			owner.rwLock.lock();
			try {
				return dataMap.containsKey(roleId);
			} finally {
				owner.rwLock.unlock();
			}
		}

		/**
		 * <pre>
		 * 
		 * @deprecated 直接获取缓存，请谨慎使用
		 * @return
		 * @author CamusHuang
		 * @creation 2013-1-30 下午4:08:54
		 * </pre>
		 */
		public Map<Long, Long> getDataCache() {
			return dataMap;
		}
	}

	/**
	 * <pre>
	 * 军团科技
	 * 
	 * @author CamusHuang
	 * @creation 2013-1-29 上午10:50:07
	 * </pre>
	 */
	public static class GangTechCache extends KAGangExtCA {
		/**
		 * <pre>
		 * <科技ID,科技等级>
		 * </pre>
		 */
		private LinkedHashMap<Integer, Integer> dataMap = new LinkedHashMap<Integer, Integer>();

		// //////////////////////////////
		private static final String JSON_VER = "0";// 版本
		//
		private static final String JSON_TECH_DATA = "A";// 科技数据

		protected GangTechCache(KGangExtCASet owner) {
			super(owner, KGangExtCATypeEnum.科技.sign);
		}

		protected GangTechCache(KGangExtCASet owner, DBGangExtCAData dbdata) {
			super(owner, dbdata.getId(), dbdata.getType());
			// 以防父类【在子类的全局属性还没有执行初始化时】回调子类decodeCA方法导致NullPointerException
			this.decodeCA(dbdata.getAttribute());
		}

		private void decodeCA(String jsonCA) {
			// 由底层调用,解释出逻辑层数据
			try {
				JSONObject obj = new JSONObject(jsonCA);
				int ver = obj.getInt(JSON_VER);// 默认版本
				// CEND 暂时只有版本0
				switch (ver) {
				case 0:
					decodeTechData(obj.getJSONArray(JSON_TECH_DATA));
					break;
				}

			} catch (Exception ex) {
				_LOGGER.error("decode数据时发生错误 gang=" + owner.getGangId() + " extCAId=" + _id + " ----丢失数据，存在运行隐患！", ex);
			}
		}

		private void decodeTechData(JSONArray obj) throws Exception {
			int len = obj.length();
			for (int i = 0; i < len; i++) {
				JSONArray temp = obj.getJSONArray(i);
				dataMap.put(temp.getInt(0), temp.getInt(1));
			}
		}

		@Override
		protected String encodeCA() {
			owner.rwLock.lock();
			// 构造一个数据对象给底层
			try {
				JSONObject obj = new JSONObject();
				obj.put(JSON_VER, 0);
				// CEND 暂时只有版本0
				obj.put(JSON_TECH_DATA, encodeTechData());
				return obj.toString();
			} catch (Exception ex) {
				_LOGGER.error("encode数据时发生错误 gang=" + owner.getGangId() + " extCAId=" + _id + " ----丢失数据！", ex);
				return "";
			} finally {
				owner.rwLock.unlock();
			}
		}

		private JSONArray encodeTechData() throws Exception {
			JSONArray obj = new JSONArray();
			for (Entry<Integer, Integer> entry : dataMap.entrySet()) {
				JSONArray temp = new JSONArray();
				temp.put(entry.getKey());
				temp.put(entry.getValue());
				obj.put(temp);
			}
			return obj;
		}

		/**
		 * <pre>
		 * 获取科技数据
		 * 
		 * @deprecated 直接获取缓存，请谨慎使用
		 * @return
		 * @author CamusHuang
		 * @creation 2013-1-30 下午4:08:54
		 * </pre>
		 */
		public Map<Integer, Integer> getDataCache() {
			return dataMap;
		}

	}

}
