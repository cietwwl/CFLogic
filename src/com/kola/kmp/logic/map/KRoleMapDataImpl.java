package com.kola.kmp.logic.map;

import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.DataStatus;
import com.kola.kmp.logic.role.IRoleMapData;

public class KRoleMapDataImpl implements IRoleMapData {
	private int _currentMapId;
	private int _lastMapId;
	private float _corX;
	private float _corY;
	
	private boolean isInDuplicateMap;
	private int _currentDuplicateMapId;
	private float _corDuplicateX;
	private float _corDuplicateY;
	
	private DataStatus _dataStatus;

	static final String JSON_VER = "0";// 版本

	static final String JSON_CURRENT_MAP_ID = "1";// 当前地图ID的JSON KEY
	static final String JSON_LAST_MAP_ID = "2";// 上次地图ID的JSON KEY
	static final String JSON_COR_X = "3";// 地图X坐标的JSON KEY
	static final String JSON_COR_Y = "4";// 地图Y坐标的JSON KEY

	@Override
	public int getCurrentMapId() {
		return _currentMapId;
	}

	@Override
	public int getLastMapId() {
		return _lastMapId;
	}

	@Override
	public float getCorX() {
		return _corX;
	}

	@Override
	public float getCorY() {
		return _corY;
	}
	
	
	
	
	public float getCorDuplicateX() {
		return _corDuplicateX;
	}

	public void setCorDuplicateX(float _corDuplicateX) {
		this._corDuplicateX = _corDuplicateX;
	}

	public float getCorDuplicateY() {
		return _corDuplicateY;
	}

	public void setCorDuplicateY(float _corDuplicateY) {
		this._corDuplicateY = _corDuplicateY;
	}

	public boolean isInDuplicateMap() {
		return isInDuplicateMap;
	}

	public void setInDuplicateMap(boolean isInDuplicateMap) {
		this.isInDuplicateMap = isInDuplicateMap;
	}	

	public int getCurrentDuplicateMapId() {
		return _currentDuplicateMapId;
	}

	public void setCurrentDuplicateMapId(int currentDuplicateMapId) {
		this._currentDuplicateMapId = currentDuplicateMapId;
	}

	@Override
	public void decode(String attribute) throws Exception {
		JSONObject json = new JSONObject(attribute);

		if (json == null) {
			return;
		}
		int ver = json.getInt(JSON_VER);// 默认版本
		// CEND 暂时只有版本0
		switch (ver) {
		case 0:
			_currentMapId = json.optInt(JSON_CURRENT_MAP_ID, 0);
			_lastMapId = json.optInt(JSON_LAST_MAP_ID, 0);
			_corX = Float.parseFloat(json.optString(JSON_COR_X, "0"));
			_corY = Float.parseFloat(json.optString(JSON_COR_Y, "0"));
			break;
		}
	}

	@Override
	public String encode() throws Exception {
		JSONObject json = new JSONObject();
		json.put(JSON_VER, 0);
		json.put(JSON_CURRENT_MAP_ID, _currentMapId);
		json.put(JSON_LAST_MAP_ID, _lastMapId);
		json.put(JSON_COR_X, Float.toString(_corX));
		json.put(JSON_COR_Y, Float.toString(_corY));
		return json.toString();
	}


	@Override
	public void setCurrentMapId(int mapId) {
		this._currentMapId = mapId;
		_dataStatus.notifyUpdate();
	}

	@Override
	public void setLastMapId(int mapId) {
		this._lastMapId = mapId;
		_dataStatus.notifyUpdate();
	}

	@Override
	public void setCorX(float corX) {
		this._corX = corX;
		_dataStatus.notifyUpdate();
	}

	@Override
	public void setCorY(float corY) {
		this._corY = corY;
		_dataStatus.notifyUpdate();
	}

	@Override
	public void setDataStatusInstance(DataStatus entity) {
		_dataStatus = entity;
	}

}
