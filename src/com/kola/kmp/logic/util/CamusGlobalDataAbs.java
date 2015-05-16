package com.kola.kmp.logic.util;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;

import com.koala.game.dataaccess.KGameDBException;
import com.koala.game.logging.KGameLogger;
import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.db.DataIdGeneratorFactory;
import com.kola.kgame.db.dataaccess.DataAccesserFactory;
import com.kola.kgame.db.dataobject.DBGameExtCA;
import com.kola.kgame.db.dataobject.impl.DBGameExtCAImpl;
import com.kola.kmp.logic.other.KGameExtDataDBTypeEnum;

/**
 * 如果一种全局数据类型只存在一个全局唯一实例，则可能使用本类作为基类
 * 例如：精彩活动，限时热购，
 * 
 * 警告：使用本类实例前，请务必调用load方法加载DB数据
 * 建议：在服务器关闭时，调用save方法再保存一次数据
 * 
 * @author Administrator
 *
 */
public abstract class CamusGlobalDataAbs {
	
	public static final Logger _LOGGER = KGameLogger.getLogger(CamusGlobalDataAbs.class);
	//

	private boolean isNew = true;
	private KGameExtDataDBTypeEnum dbTypeEnum;
	private DBGameExtCA _dbCa;

	public CamusGlobalDataAbs(KGameExtDataDBTypeEnum dbTypeEnum) {
		long dbId = 0;
		int caType = 1;
		//
		this.dbTypeEnum = dbTypeEnum;
		_dbCa = new DBGameExtCAImpl();
		_dbCa.setDBId(dbId);
		_dbCa.setDBType(dbTypeEnum.dbType);
		_dbCa.setCAType(caType);
	}

	private void init(DBGameExtCA dbCa) {
		isNew = false;
		_dbCa = dbCa;
		
		try {
			String jsonStr = dbCa.getAttribute();
			if(jsonStr!=null && !jsonStr.isEmpty()){
				decode(new JSONObject(jsonStr));
			}
		} catch (JSONException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	private synchronized DBGameExtCA getDBGameExtCA() {
		try {
			_dbCa.setAttribute(encode().toString());
		} catch (JSONException e) {
			_LOGGER.error(e.getMessage(), e);
		}
		return _dbCa;
	}
	
	protected abstract void decode(JSONObject json) throws JSONException;
	
	protected abstract JSONObject encode() throws JSONException;

	public synchronized void save() {
		try {
			if (isNew) {
				_dbCa.setDBId(DataIdGeneratorFactory.getGameExtDataIdGenerator().nextId());
				DataAccesserFactory.getGameExtCADataAccesser().addDBGameExtCAs(Arrays.asList(getDBGameExtCA()));
				isNew = false;
			} else {
				DataAccesserFactory.getGameExtCADataAccesser().updateDBGameExtCAs(Arrays.asList(getDBGameExtCA()));
			}
		} catch (KGameDBException e) {
			_LOGGER.error(e.getMessage(), e);
		} catch (Exception e) {
			_LOGGER.error(e.getMessage(), e);
		}
	}

	/**
	 * 警告：使用本类实例前，请务必调用load方法
	 * 本方法只需要在服务器启动完成时调用一次
	 */
	public synchronized void load() {
		try {
			List<DBGameExtCA> nowDBList = DataAccesserFactory.getGameExtCADataAccesser().getDBGameExtCA(dbTypeEnum.dbType);
			if (!nowDBList.isEmpty()) {
				init(nowDBList.get(0));
			}
		} catch (KGameDBException e) {
			_LOGGER.error(e.getMessage(), e);
		} catch (Exception e) {
			_LOGGER.error(e.getMessage(), e);
		}
	}
}
