package com.kola.kmp.logic.mount;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.timer.Timer;

import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.mount.impl.KAMountSet;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kgame.db.dataobject.DBMount;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountTemplate;
import com.kola.kmp.logic.other.KJobTypeEnum;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-12-5 下午8:14:02
 * </pre>
 */
public class KMountSet extends KAMountSet<KMount> {

	public final KJobTypeEnum job;

	private long dataTime = System.currentTimeMillis();// 当前数据对应的日期
	//
	private int usedModelId = -1;// 正在骑乘的机甲型号

	// <机甲型号,机甲对象>双缓存，保持与引擎层同步
	private Map<Integer, KMount> dataMapByModel = new HashMap<Integer, KMount>();
	
	//<机甲DBID,机甲对象>用于缓存旧版（1.0.4）的机甲数据，这些数据不加入到引擎缓存和dataMapByModel中，仅用于改版补偿
	private Map<Long, KMountOld> oldMountMap = new HashMap<Long, KMountOld>();

	// /////////////////////////////////
	static final String JSON_NULL = "NULL";// null
	static final String JSON_VER = "A";// 版本
	static final String JSON_DAY = "Z";// 当前数据对应的日期
	//
	static final String JSON_BASEINFO = "B";// 基础信息
	static final String JSON_BASEINFO_USED_MODEL = "1";

	KMountSet(long roleId, byte job, boolean isFirstNew) {
		super(roleId, isFirstNew);
		this.job = KJobTypeEnum.getJob(job);
	}

	protected Map<Long, KMount> initDBMounts(List<DBMount> dbdatas) {
		Map<Long, KMount> result = new HashMap<Long, KMount>();
		for (DBMount dbdata : dbdatas) {
			KMountTemplate temp = KMountDataManager.mMountTemplateManager.getTemplate(dbdata.getTemplateId());
			if (temp == null) {
				// 需要缓存，解析，补偿
				_LOGGER.error("找不到机甲模板 模板id={} roleId={} ca={}", dbdata.getTemplateId(), _roleId, dbdata.getCustomizeAttribute());
				KMountOld data = new KMountOld(this, dbdata);
				oldMountMap.put(data._id, data);
				continue;
			}
			KMount data = new KMount(this, temp, dbdata);
			result.put(data._id, data);
			//
			dataMapByModel.put(data.getTemplate().Model, data);
		}
		return result;
	}

	@Override
	protected void decodeCA(String jsonCA) {
		// 由底层调用,解释出逻辑层数据
		try {
			JSONObject obj = new JSONObject(jsonCA);
			int ver = obj.getInt(JSON_VER);// 默认版本
			dataTime = obj.optLong(JSON_DAY) * Timer.ONE_HOUR;
			// CEND 暂时只有版本0
			switch (ver) {
			case 0:
				JSONObject baseobj = obj.optJSONObject(JSON_BASEINFO);
				if(baseobj!=null){
					usedModelId = baseobj.optInt(JSON_BASEINFO_USED_MODEL);
					KMount mount = dataMapByModel.get(usedModelId);
					if (mount == null) {
						usedModelId = -1;
					} else {
						mount.setUsed(true);
					}
				}
				break;
			}

		} catch (Exception ex) {
			_LOGGER.error("decode数据时发生错误 roleId=" + _roleId + " ----丢失数据，存在运行隐患！", ex);
		}
	}

	@Override
	protected String encodeCA() {
		rwLock.lock();
		// 构造一个数据对象给底层
		try {
			JSONObject obj = new JSONObject();
			obj.put(JSON_VER, 0);
			obj.put(JSON_DAY, dataTime / Timer.ONE_HOUR);
			// CEND 暂时只有版本0
			JSONObject baseobj = new JSONObject();
			obj.put(JSON_BASEINFO, baseobj);
			{
				baseobj.put(JSON_BASEINFO_USED_MODEL, usedModelId);
			}
			return obj.toString();
		} catch (Exception ex) {
			_LOGGER.error("encode数据时发生错误 roleId=" + _roleId + " ----丢失数据！", ex);
			return "";
		} finally {
			rwLock.unlock();
		}
	}

	public boolean addMount(KMount mount) {
		rwLock.lock();
		try {
			boolean isSuccess = super.addMount(mount);
			if (isSuccess) {
				dataMapByModel.put(mount.getTemplate().Model, mount);
			}
			return isSuccess;
		} finally {
			rwLock.unlock();
		}
	}

	public KMount getMountByModel(int model) {
		rwLock.lock();
		try {
			return dataMapByModel.get(model);
		} finally {
			rwLock.unlock();
		}
	}

	public int getUsedModelId() {
		return usedModelId;
	}

	void setUsedModelId(int usedModelId) {
		rwLock.lock();
		try {
			if (this.usedModelId != usedModelId) {
				if (this.usedModelId > 0) {
					KMount mount = getMountByModel(this.usedModelId);
					mount.setUsed(false);
				}
				this.usedModelId = usedModelId;
				if (usedModelId > 0) {
					KMount mount = getMountByModel(usedModelId);
					mount.setUsed(true);
				}
				notifyDB();
			}
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 返回正在骑乘的机甲
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-25 下午12:04:09
	 * </pre>
	 */
	public KMount getMountInUsed() {
		return getMountByModel(usedModelId);
	}
	
	/**
	 * <pre>
	 * 
	 * @deprecated 获取旧版（1.0.4）的机甲数据
	 * @return
	 * @author CamusHuang
	 * @creation 2015-1-31 上午11:38:02
	 * </pre>
	 */
	public Map<Long, KMountOld> getOldMountMap(){
		return oldMountMap;
	}

	public KMount notifyElementDelete(long deletedObjId) {
		rwLock.lock();
		try {
			KMount mount = super.notifyElementDelete(deletedObjId);
			if (mount == null){
				// 不存在于引擎缓存，则尝试从旧版（1.0.4）数据中寻找
				mount = oldMountMap.get(deletedObjId);
				if (mount != null) {
					// 将旧版（1.0.4）数据从DB删除
					super.notifyElementDelete(mount);
				}
			} else {
				if (mount.getTemplate().Model == usedModelId) {
					setUsedModelId(-1);
				}
			}
			//
			for (Iterator<Entry<Integer, KMount>> it = dataMapByModel.entrySet().iterator(); it.hasNext();) {
				Entry<Integer, KMount> e = it.next();
				if (e.getValue().getId() == deletedObjId) {
					it.remove();
				}
			}
			return mount;
		} finally {
			rwLock.unlock();
		}
	}
}
