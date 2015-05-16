package com.kola.kmp.logic.fashion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.role.RoleExtCABaseImpl;
import com.kola.kgame.cache.util.UUIDGenerator;
import com.kola.kmp.logic.fashion.KFashionDataStructs.KFashionTemplate;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * <pre>
 * 角色的时装数据
 * 
 * @author CamusHuang
 * @creation 2014-2-13 下午4:53:22
 * </pre>
 */
public final class KRoleFashion extends RoleExtCABaseImpl {

	public static final Logger _LOGGER = KGameLogger.getLogger(KRoleFashion.class);

	// 读写锁
	public final ReentrantLock rwLock = new ReentrantLock();

	// -------------------------逻辑层数据项
	private HashMap<Integer, FashionData> allFashions = new HashMap<Integer, KRoleFashion.FashionData>();
	private int selectedFashionId;

	// /////////////////////////////////
	static final String JSON_NULL = "NULL";// null
	static final String JSON_VER = "A";// 版本
	//
	static final String JSON_BASEINFO = "B";// 基础信息
	static final String JSON_BASEINFO_SELECT = "1";
	static final String JSON_FASHION = "C";
	static final String JSON_FASHION_ENDTIME = "1";
	static final String JSON_FASHION_UUID = "2";

	/**
	 * <pre>
	 * 逻辑新建对象时使用
	 * 
	 * @param dbdata
	 * @author CamusHuang
	 * @creation 2014-2-13 下午5:04:25
	 * </pre>
	 */
	protected KRoleFashion(long _roleId, int _type) {
		super(_roleId, _type);
	}

	@Override
	protected void decode(String jsonCA) {
		// 由底层调用,解释出逻辑层数据
		try {
			JSONObject obj = new JSONObject(jsonCA);
			int ver = obj.getInt(JSON_VER);// 默认版本
			// CEND 暂时只有版本0
			switch (ver) {
			case 0:
				decodeBaseInfo(obj.getJSONObject(JSON_BASEINFO));
				decodeFashions(obj.getJSONObject(JSON_FASHION));
				break;
			}

		} catch (Exception ex) {
			_LOGGER.error("decode数据时发生错误 roleId=" + getRoleId() + " ----丢失数据，存在运行隐患！", ex);
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
		this.selectedFashionId = obj.getInt(JSON_BASEINFO_SELECT);
	}

	private void decodeFashions(JSONObject obj) throws JSONException {
		for (KFashionTemplate temp : KFashionDataManager.mFashionTemplateManager.getFashionTemplateList()) {
			JSONObject sun = obj.optJSONObject(temp.id + "");
			if (sun != null) {
				FashionData data = new FashionData(this, temp.id);
				data.decode(sun);
				this.allFashions.put(data.tempId, data); // Perry 2014-05-27
															// bug修复
			}
		}
	}

	@Override
	protected String encode() {
		boolean isLock = false;
		try {
			isLock = rwLock.tryLock(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			_LOGGER.error(e.getMessage(), e);
		}
		if (!isLock) {
			return null;
		}
		// 构造一个数据对象给底层
		try {
			JSONObject obj = new JSONObject();
			obj.put(JSON_VER, 0);
			// CEND 暂时只有版本0
			obj.put(JSON_BASEINFO, encodeBaseInfo());// 基础信息
			obj.put(JSON_FASHION, encodeFashions());
			return obj.toString();
		} catch (Exception ex) {
			_LOGGER.error("encode数据时发生错误 roleId=" + getRoleId() + " ----丢失数据！", ex);
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
		obj.put(JSON_BASEINFO_SELECT, selectedFashionId);
		return obj;
	}

	private JSONObject encodeFashions() throws JSONException {
		JSONObject obj = new JSONObject();
		for (FashionData data : allFashions.values()) {
			obj.put(data.tempId + "", data.encode());
		}
		return obj;
	}

	int getSelectedFashionId() {
		return selectedFashionId;
	}

	void setSelectedFashionId(int newFashionId) {
		rwLock.lock();
		try {
			int oldValue = selectedFashionId;
			//
			if (!allFashions.containsKey(newFashionId)) {
				selectedFashionId = 0;
			} else {
				selectedFashionId = newFashionId;
			}
			//
			if (selectedFashionId != oldValue) {
				notifyUpdate();
			}
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 直接获取缓存，请谨慎使用
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-15 下午3:54:14
	 * </pre>
	 */
	HashMap<Integer, FashionData> getAllFashionsCacha() {
		return allFashions;
	}

	int searchMaxPowerTempId() {
		rwLock.lock();
		try {
			if (allFashions.isEmpty()) {
				return 0;
			}

			int maxPower = 0;
			FashionData result = null;
			for (FashionData data : allFashions.values()) {
				int power = data.countPower();
				if (power > maxPower) {
					maxPower = power;
					result = data;
				}
			}
			
			return result==null?0:result.tempId;
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 从指定的时装中，找出比当前正在穿戴的时装战力要高且最高的一件
	 * 
	 * @param newTempIds
	 * @return
	 * @author CamusHuang
	 * @creation 2014-10-28 上午10:16:42
	 * </pre>
	 */
	int searchTopPowerTempId(Set<Integer> newTempIds) {

		rwLock.lock();
		try {
			if (allFashions.isEmpty()) {
				return 0;
			}

			FashionData nowData = allFashions.get(selectedFashionId);
			int nowPower = nowData == null ? 0 : nowData.countPower();

			int maxPower = 0;
			FashionData result = null;
			for (int tempId : newTempIds) {
				FashionData tempData = allFashions.get(tempId);
				if (tempData == null) {
					continue;
				}

				int power = tempData.countPower();
				if (power > nowPower) {
					if (power > maxPower) {
						maxPower = power;
						result = tempData;
					}
				}
			}
			return result == null ? 0 : result.tempId;
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 从指定的时装中，剔除比当前正在穿戴的时装战力要低的
	 * 
	 * @param fashionTempIdSet
	 * @return
	 * @author CamusHuang
	 * @creation 2014-10-28 下午12:27:06
	 * </pre>
	 */
	void removeLowPowerTempIds(Set<Integer> fashionTempIdSet) {
		rwLock.lock();
		try {
			if (allFashions.isEmpty()) {
				return;
			}
			//
			FashionData nowData = allFashions.get(selectedFashionId);
			if(nowData == null){
				return;
			}
			int nowPower = nowData.countPower();
			//
			for (Iterator<Integer> it = fashionTempIdSet.iterator(); it.hasNext();) {
				int tempId = it.next();
				//
				int power = 0;
				FashionData fashionData = allFashions.get(tempId);
				if (fashionData != null) {
					power = fashionData.countPower();
				} else {
					KFashionTemplate temp = KFashionDataManager.mFashionTemplateManager.getFashionTemplate(tempId);
					power = FashionData.countPower(temp, getRoleId());
				}

				if (power <= nowPower) {
					it.remove();
				}
			}
		} finally {
			rwLock.unlock();
		}
	}

	FashionData getFashionData(int fashionId) {
		rwLock.lock();
		try {
			return allFashions.get(fashionId);
		} finally {
			rwLock.unlock();
		}
	}

	FashionData addFashion(KFashionTemplate temp) {
		rwLock.lock();
		try {
			FashionData data = allFashions.get(temp.id);
			if (data != null) {
				// 本来已有时装
				if (temp.effectTime <= 0) {
					// 永久
					long oldValue = data.endTime;
					data.endTime = temp.effectTime;
					if (data.endTime != oldValue) {
						notifyUpdate();
					}
				} else {
					// 非永久
					long nowTime = System.currentTimeMillis();
					if (data.endTime < nowTime) {
						data.endTime = nowTime + temp.effectTime;
					} else {
						data.endTime += temp.effectTime;
					}
					notifyUpdate();
				}
			} else {
				// 本来没有时装
				data = new FashionData(this, UUIDGenerator.generate(), temp.id);
				if (temp.effectTime <= 0) {
					// 永久
					data.endTime = temp.effectTime;
				} else {
					// 非永久
					long nowTime = System.currentTimeMillis();
					data.endTime = nowTime + temp.effectTime;
				}
				allFashions.put(data.tempId, data);
				notifyUpdate();
			}

			return data;
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 清理超时失效的时装
	 * 
	 * @return 被清理的时装
	 * @author CamusHuang
	 * @creation 2014-7-1 下午2:59:40
	 * </pre>
	 */
	List<FashionData> clearTimeOutFashion() {
		List<FashionData> clearFashions = new ArrayList<FashionData>();
		rwLock.lock();
		try {
			for (Iterator<FashionData> it = allFashions.values().iterator(); it.hasNext();) {
				FashionData data = it.next();
				// 本来已有时装
				if (data.endTime <= 0) {
					// 永久
					continue;
				} else {
					// 非永久
					long nowTime = System.currentTimeMillis();
					if (data.endTime <= nowTime) {
						it.remove();
						clearFashions.add(data);
						if (selectedFashionId == data.tempId) {
							// Perry 2014-05-27 如果这件时装是穿在身上，需要去掉
							setSelectedFashionId(0);
						}
					}
				}
			}
			if (!clearFashions.isEmpty()) {
				notifyUpdate();
			}

			return clearFashions;
		} finally {
			rwLock.unlock();
		}
	}

	void notifyDB() {
		this.notifyUpdate();
	}

	/**
	 * <pre>
	 * 角色拥有的一件时装数据
	 * 
	 * @author CamusHuang
	 * @creation 2014-3-15 上午11:55:18
	 * </pre>
	 */
	static class FashionData {
		private KRoleFashion owner;
		//
		private String uuid;
		public final int tempId;
		private long endTime;// 时效结束时间（毫秒，<=0表示永久）

		private FashionData(KRoleFashion owner, int tempId) {
			this.owner = owner;
			this.tempId = tempId;
		}

		private FashionData(KRoleFashion owner, String uuid, int tempId) {
			this.owner = owner;
			this.uuid = uuid;
			this.tempId = tempId;
		}

		/**
		 * <pre>
		 * （毫秒，<=0表示永久）
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2014-6-17 上午9:53:56
		 * </pre>
		 */
		long getEndTime() {
			return endTime;
		}

		/**
		 * <pre>
		 * 
		 * @deprecated 用于复制数据，不保存，必须在外部保存
		 * @param endTime
		 * @author CamusHuang
		 * @creation 2014-10-15 下午12:09:28
		 * </pre>
		 */
		void setEndTime(long endTime) {
			this.endTime = endTime;
		}

		String getUUID() {
			return uuid;
		}

		int countPower() {
			KFashionTemplate temp = KFashionDataManager.mFashionTemplateManager.getFashionTemplate(tempId);
			return countPower(temp, owner.getRoleId());
		}

		static int countPower(KFashionTemplate temp, long roleId) {
			if (temp == null) {
				return 0;
			}
			return KSupportFactory.getRoleModuleSupport().calculateBattlePower(temp.allEffects, roleId);
		}

		void decode(JSONObject obj) throws JSONException {
			endTime = obj.getLong(JSON_FASHION_ENDTIME);
			uuid = obj.optString(JSON_FASHION_UUID, null);
			if (uuid == null) {
				uuid = UUIDGenerator.generate();
			}
		}

		JSONObject encode() throws JSONException {
			JSONObject obj = new JSONObject();
			obj.put(JSON_FASHION_ENDTIME, endTime);
			obj.put(JSON_FASHION_UUID, uuid);
			return obj;
		}
	}
}
