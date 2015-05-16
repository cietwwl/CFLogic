package com.kola.kmp.logic.map.duplicatemap;

import com.kola.kmp.logic.other.KBattleObjectTypeEnum;

public class KDuplicateMapBornPoint implements
		Comparable<KDuplicateMapBornPoint> {

	/**
	 * 地编中的实例ID
	 */
	public int _objInstanceId;

	/**
	 * 所属出口Id
	 */
	public int _exitId;

	/**
	 * x坐标
	 */
	public float _corX;

	/**
	 * y坐标
	 */
	public float _corY;

	public KDuplicateMapBornPoint(int instanceId, int exitId, float x, float y) {
		_objInstanceId = instanceId;
		_exitId = exitId;
		_corX = x;
		_corY = y;
	}

	@Override
	public int compareTo(KDuplicateMapBornPoint o) {
		if (this._corX < o._corX) {
			return 1;
		} else if (this._corX > o._corX) {
			return -1;
		} else {
			if (this._corY < o._corY) {
				return 1;
			} else if (this._corY > o._corY) {
				return -1;
			} else {
				return 0;
			}
		}
	}
}
