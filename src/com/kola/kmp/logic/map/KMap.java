package com.kola.kmp.logic.map;

import java.util.List;
import java.util.Map;

import com.kola.kmp.logic.other.KMapEntityTypeEnum;
import com.kola.kmp.logic.other.KMapTypeEnum;

public interface KMap<E extends KMapEntityData> {

	/**
	 * 地图Id
	 * @return
	 */
	public int getMapId();

	/**
	 * 地图类型
	 * @return
	 */
	public KMapTypeEnum getMapType();

	/**
	 * 获取地图所有实体的Map
	 * @return
	 */
	public Map<GameMapEntityCacheKey, E> getEntitysMap();
	
	/**
	 * 把一个地图实体（如角色、NPC、障碍物等等）放入地图。不一定都是成功的。<br>
	 * @param sourceId 实体源ID
	 * @param sourceObject 实体源对象
	 * @param entityType 地图实体类型
	 * @param x 放入的X坐标
	 * @param y 放入的X坐标
	 * @return 成功放入地图后的实体，失败则null。<br>
	 * @throws KGameMapModuleException
	 */
	public <T> E putEntity(long sourceId, T sourceObject,
			KMapEntityTypeEnum entityType, float x, float y)
			throws KGameMapModuleException;

	/**
	 * 把一个地图实体（如NPC、角色、怪物、物品等等）放入地图某个坐标。<br>
	 * 
	 * @param entity
	 *            实体<br>
	 * @param x
	 *            目标坐标X
	 * @param y
	 *            目标坐标Y
	 * @return 成功放入地图后的实体，失败则null。<br>
	 * @throws GameMapServiceModuleException
	 *             ;
	 */
	public E putEntityTo(E entity, float x, float y)
			throws KGameMapModuleException;

	/**
	 * 移除一个地图对象实体。<br>
	 * 
	 * @param entity
	 *            将被移除的实体。<br>
	 * @return 刚被移除的对象<br>
	 */
	public E removeEntity(E entity);

	/**
	 * 移除一个地图对象实体。<br>
	 * 
	 * @param entityType
	 *            类型<br>
	 * @param entityObjectID
	 *            对象ID<br>
	 * @return 刚被移除的对象<br>
	 */
	public E removeEntity(KMapEntityTypeEnum entityType, long entityObjectID);

	/**
	 * 移除地图上某种类型的所有实体。<br>
	 * 
	 * @param entityType
	 *            类型<br>
	 */
	public void removeEntities(KMapEntityTypeEnum entityType);

	/**
	 * 移除地图上的所有实体。<br>
	 */
	public void removeAllEntities();

	/**
	 * 根据类型和ID取得某个地图实体。<br>
	 * 
	 * @param entityType
	 *            类型<br>
	 * @param entityObjectID
	 *            对象ID<br>
	 * @return 地图实体<br>
	 */
	public E getEntity(KMapEntityTypeEnum entityType, long entityObjectID);

	/**
	 * 取得某类型的所有地图实体。<br>
	 * 
	 * @param entityType
	 *            类型<br>
	 * @return 实体集合<br>
	 */
	public List<E> getEntities(KMapEntityTypeEnum entityType);

	/**
	 * 取得某类型的所有地图实体的对象实例ID集合。<br>
	 * 
	 * @param entityType
	 *            类型<br>
	 * @return 实体集合<br>
	 */
	public List<Long> getEntitieIds(KMapEntityTypeEnum entityType);

}
