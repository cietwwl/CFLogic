package com.kola.kmp.logic.currency;

import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.CustomizeAttribute;
import com.kola.kgame.cache.DataStatus;
import com.kola.kgame.cache.currency.impl.KACurrencyAccount;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;

/**
 * <pre>
 * 一个角色帐户
 * 元宝属于角色，不属于玩家
 * 
 * @author CamusHuang
 * @creation 2012-12-6 上午11:21:25
 * </pre>
 */
public class KCurrencyAccount extends KACurrencyAccount {

	private KCurrencyAccountCA ca;
	public final KCurrencyTypeEnum typeEnum;

	/**
	 * <pre>
	 * 逻辑新建对象时使用
	 * 新建后，是一个初生帐户的默认参数
	 * ID由缓存赋值
	 * 
	 * @param _type
	 * @param _balance
	 * @author CamusHuang
	 * @creation 2012-12-6 上午11:21:21
	 * </pre>
	 */
	KCurrencyAccount(KCurrencyAccountSet owner, KCurrencyTypeEnum typeEnum, boolean isFirstNew) {
		super(owner, typeEnum.sign, isFirstNew);
		this.typeEnum = typeEnum;
		//
		ca = new KCurrencyAccountCA(this, isFirstNew);
	}

	@Override
	protected String encodeCA() {
		return ca.encodeAttribute();
	}

	@Override
	protected void decodeCA(String jsonStr) {
		ca.decodeAttribute(jsonStr);
	}



	/**
	 * <pre>
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-5 上午10:10:24
	 * </pre>
	 */
	public static class KCurrencyAccountCA implements CustomizeAttribute {

		private final KCurrencyAccount owner;

		// //
		/** 总数额中的绑定部分，即_balance包含了_bindBalance */
		private long _bindBalance;

		// /////////////////////////////////
		static final String JSON_NULL = "NULL";// null
		static final String JSON_VER = "A";// 版本
		//
		static final String JSON_BASEINFO = "B";// 基础信息
		static final String JSON_BASEINFO_BIND = "1";// 绑定金额

		// /////////////////////////////////
		/**
		 * <pre>
		 * 
		 * 
		 * @param owner
		 * @author CamusHuang
		 * @creation 2014-1-21 下午4:31:38
		 * </pre>
		 */
		KCurrencyAccountCA(KCurrencyAccount owner, boolean isFirstNew) {
			this.owner = owner;
		}

		@Override
		public void decodeAttribute(String attribute) {
			// 由底层调用,解释出逻辑层数据
			try {
				JSONObject obj = new JSONObject(attribute);
				int ver = obj.getInt(JSON_VER);// 默认版本
				// CEND 暂时只有版本0
				switch (ver) {
				case 0:
					decodeBaseInfo(obj.getJSONObject(JSON_BASEINFO));
					break;
				}

			} catch (Exception ex) {
				_LOGGER.error("decode数据时发生错误 roleId=" + owner.getRoleId() + " ----丢失数据，存在运行隐患！", ex);
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
			this._bindBalance = obj.getLong(JSON_BASEINFO_BIND);
		}

		@Override
		public String encodeAttribute() {
			owner.owner.rwLock.lock();
			// 构造一个数据对象给底层
			try {
				JSONObject obj = new JSONObject();
				obj.put(JSON_VER, 0);
				// CEND 暂时只有版本0
				obj.put(JSON_BASEINFO, encodeBaseInfo());// 基础信息
				return obj.toString();
			} catch (Exception ex) {
				_LOGGER.error("encode数据时发生错误 roleId=" + owner.getRoleId() + " ----丢失数据！", ex);
				return "";
			} finally {
				owner.owner.rwLock.unlock();
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
			obj.put(JSON_BASEINFO_BIND, _bindBalance);
			return obj;
		}

		/**
		 * @deprecated 空实现
		 */
		public DataStatus getDataStatus() {
			return null;
		}
	}
}
