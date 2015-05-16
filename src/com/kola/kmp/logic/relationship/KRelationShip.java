package com.kola.kmp.logic.relationship;

import javax.management.timer.Timer;

import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.relationship.impl.KARelationShip;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kgame.db.dataobject.DBRelationShipData;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-12-6 上午11:21:25
 * </pre>
 */
public class KRelationShip extends KARelationShip {

	private int closeness = 1;// 亲密度
	private long lastPMChatTime;//最后被主人家私聊的时刻

	// /////////////////////////////////
	static final String JSON_NULL = "NULL";// null
	static final String JSON_VER = "A";// 版本
	//
	static final String JSON_BASEINFO = "B";// 基础信息
	static final String JSON_BASEINFO_CLOSE = "1";
	static final String JSON_BASEINFO_LASTPMCHATTIME = "2";

	KRelationShip(KRelationShipSet owner, int type, long guestRoleId) {
		super(owner, type, guestRoleId);
	}

	KRelationShip(KRelationShipSet owner, DBRelationShipData dbdata) {
		super(owner, dbdata.getId(), dbdata.getType(), dbdata.getGuestId(), dbdata.getCreateTimeMillis());
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
				break;
			}

		} catch (Exception ex) {
			_LOGGER.error("decode数据时发生错误 roleId=" + owner._roleId + " ----丢失数据，存在运行隐患！", ex);
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
		this.closeness = obj.getInt(JSON_BASEINFO_CLOSE);
		this.lastPMChatTime = obj.getLong(JSON_BASEINFO_LASTPMCHATTIME)*Timer.ONE_MINUTE;
	}

	@Override
	protected String encodeCA() {
		owner.rwLock.lock();
		// 构造一个数据对象给底层
		try {
			JSONObject obj = new JSONObject();
			obj.put(JSON_VER, 0);
			// CEND 暂时只有版本0
			obj.put(JSON_BASEINFO, encodeBaseInfo());// 基础信息
			return obj.toString();
		} catch (Exception ex) {
			_LOGGER.error("encode数据时发生错误 roleId=" + owner._roleId + " ----丢失数据！", ex);
			return "";
		} finally {
			owner.rwLock.unlock();
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
		obj.put(JSON_BASEINFO_CLOSE, closeness);
		obj.put(JSON_BASEINFO_LASTPMCHATTIME, lastPMChatTime/Timer.ONE_MINUTE);
		return obj;
	}

	public int getCloseness() {
		return closeness;
	}

	int addCloseness(int addValue) {
		rwLock.lock();
		try {
			int oldValue = closeness;
			closeness += addValue;
			
			closeness = Math.max(0, closeness);
			closeness = Math.min(KRelationShipConfig.getInstance().ClosenessMaxValue, closeness);
			
			if (closeness != oldValue) {
				super.notifyDB();
			}
			return closeness;
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 今天有没有私聊过？
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-14 上午9:55:20
	 * </pre>
	 */
	boolean isDonePMChatToday() {
		rwLock.lock();
		try {
			long todayStartTime = UtilTool.getTodayStart().getTimeInMillis();
			
			if(lastPMChatTime<todayStartTime){
				lastPMChatTime = System.currentTimeMillis();
				this.notifyDB();
				return false;
			}
			
			if(lastPMChatTime>(todayStartTime+Timer.ONE_DAY)){
				lastPMChatTime = System.currentTimeMillis();
				this.notifyDB();
				return false;
			}
			
			return true;
		} finally {
			rwLock.unlock();
		}
	}
}
