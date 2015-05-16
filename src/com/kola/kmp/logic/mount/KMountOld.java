package com.kola.kmp.logic.mount;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import com.koala.thirdpart.json.JSONArray;
import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.db.dataobject.DBMount;
import com.kola.kmp.logic.other.KGameAttrType;

/**
 * <pre>
 * 
 * 
 * @deprecated 旧版本（1.0.4）的机甲数据
 * @author CamusHuang
 * @creation 2012-12-6 上午11:21:25
 * </pre>
 */
public class KMountOld extends KMount {

	private boolean isUseInMap;
	private int upLvExp;
	// 当天培养次数
	private int trainTime;
	// 各属性的培养次数
	private Map<KGameAttrType, AtomicInteger> attTrainTimesMap = new HashMap<KGameAttrType, AtomicInteger>();
	// 培养属性
	private Map<KGameAttrType, AtomicInteger> trainAttsMap = new HashMap<KGameAttrType, AtomicInteger>();
	// /////////////////////////////////
	static final String JSON_NULL = "NULL";// null
	static final String JSON_VER = "A";// 版本
	//
	static final String JSON_BASEINFO = "B";// 基础信息
	static final String JSON_BASEINFO_UPLVEXP = "1";
	static final String JSON_BASEINFO_ISUSEINMAP = "2";
	static final String JSON_BASEINFO_TRAINTIME = "3";
	static final String JSON_TRAINATTS = "C";

	KMountOld(KMountSet owner, DBMount dbdata) {
		super(owner, dbdata);
		// 以防父类【在子类的全局属性还没有执行初始化时】回调子类decodeCA方法导致NullPointerException
		decodeCA(dbdata.getCustomizeAttribute());
	}

	private void decodeCA(String jsonCA) {
		// 由底层调用,解释出逻辑层数据
		try {
			JSONObject obj = new JSONObject(jsonCA);
			int ver = obj.getInt(JSON_VER);// 默认版本
			// CEND 暂时只有版本0
			switch (ver) {
			case 0:
				decodeBaseInfo(obj.getJSONObject(JSON_BASEINFO));
				decodeTrainAtts(obj.getJSONObject(JSON_TRAINATTS));
				break;
			}

		} catch (Exception ex) {
			_LOGGER.error("decode数据时发生错误 roleId=" + _ownerId + " ----丢失数据，存在运行隐患！", ex);
		}
	}

	/**
	 * <pre>
	 * 基础信息解码
	 * 
	 * @throws JSONException
	 * @author CamusHuang
	 * @creation 2013-1-12 下午3:30:24
	 * </pre>
	 */
	private void decodeBaseInfo(JSONObject obj) throws JSONException {
		this.upLvExp = obj.getInt(JSON_BASEINFO_UPLVEXP);
		this.isUseInMap = obj.getBoolean(JSON_BASEINFO_ISUSEINMAP);
		this.trainTime = obj.getInt(JSON_BASEINFO_TRAINTIME);
	}

	private void decodeTrainAtts(JSONObject obj) throws JSONException {
		for (Iterator<String> it = obj.keys(); it.hasNext();) {
			String attTypeStr = it.next();
			KGameAttrType type = KGameAttrType.getAttrTypeEnum(Integer.parseInt(attTypeStr));

			JSONArray array = obj.getJSONArray(attTypeStr);
			trainAttsMap.put(type, new AtomicInteger(array.getInt(0)));
			attTrainTimesMap.put(type, new AtomicInteger(array.getInt(1)));
		}
	}

	@Override
	protected String encodeCA() {
		rwLock.lock();
		// 构造一个数据对象给底层
		try {
			JSONObject obj = new JSONObject();
			obj.put(JSON_VER, 0);
			// CEND 暂时只有版本0
			obj.put(JSON_BASEINFO, encodeBaseInfo());// 基础信息
			obj.put(JSON_TRAINATTS, encodeTrainAtts());
			return obj.toString();
		} catch (Exception ex) {
			_LOGGER.error("encode数据时发生错误 roleId=" + _ownerId + " ----丢失数据！", ex);
			return "";
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 基础信息打包
	 * 
	 * @return
	 * @throws JSONException
	 * @author CamusHuang
	 * @creation 2013-1-11 下午12:29:08
	 * </pre>
	 */
	private JSONObject encodeBaseInfo() throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put(JSON_BASEINFO_UPLVEXP, upLvExp);
		obj.put(JSON_BASEINFO_ISUSEINMAP, isUseInMap);
		obj.put(JSON_BASEINFO_TRAINTIME, trainTime);
		return obj;
	}

	private JSONObject encodeTrainAtts() throws JSONException {
		JSONObject obj = new JSONObject();
		for (Entry<KGameAttrType, AtomicInteger> entry : trainAttsMap.entrySet()) {

			JSONArray array = new JSONArray();
			array.put(0, entry.getValue().get());
			AtomicInteger temp = attTrainTimesMap.get(entry.getKey());
			array.put(1, temp == null ? 0 : temp.get());
			//
			obj.put(entry.getKey().sign + "", array);
		}
		return obj;
	}

	public int getExp() {
		return upLvExp;
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 获取培养属性缓存，请谨慎使用
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-28 下午4:02:04
	 * </pre>
	 */
	public Map<KGameAttrType, AtomicInteger> getTrainAttsMapCache() {
		return trainAttsMap;
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 获取各属性的培养次数缓存，请谨慎使用
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-28 下午4:02:04
	 * </pre>
	 */
	Map<KGameAttrType, AtomicInteger> getAttTrainTimesMapCache() {
		return attTrainTimesMap;
	}

	int getTrainTime() {
		return trainTime;
	}

	/**
	 * <pre>
	 * 是否骑乘中
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-29 下午2:44:07
	 * </pre>
	 */
	boolean isUseInMap() {
		return isUseInMap;
	}
}
