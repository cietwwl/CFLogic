package com.kola.kmp.logic.map;

/**
 *  地图实体对象缓存KEY
 */
public class GameMapEntityCacheKey {

	public final int type;
	public final long objId;

	public GameMapEntityCacheKey(int type, long objId) {
		this.type = type;
		this.objId = objId;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && (obj instanceof GameMapEntityCacheKey)) {
			GameMapEntityCacheKey o = (GameMapEntityCacheKey) obj;
			return o.objId == objId && o.type == type;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 89 * hash + this.type;
		hash = 89 * hash + (int) (this.objId ^ (this.objId >>> 32));
		return hash;
	}
}
