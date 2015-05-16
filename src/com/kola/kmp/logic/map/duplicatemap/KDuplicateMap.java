package com.kola.kmp.logic.map.duplicatemap;

import java.util.List;

import com.kola.kmp.logic.map.KMap;
import com.kola.kmp.logic.map.KMapEntityData;
import com.kola.kmp.logic.other.KMapDuplicateTypeEnum;

public interface KDuplicateMap<T extends KMapEntityData> extends KMap<T>,
		Cloneable {

	/**
	 * 获取副本类型
	 * 
	 * @return
	 */
	public KMapDuplicateTypeEnum getDuplicateTypeEnum();

	/**
	 * 获取副本ID
	 * 
	 * @return
	 */
	public int getDuplicateId();

	/**
	 * 获取副本地图组Id
	 */
	public int getMapGroupId();

	/**
	 * 获取所有出生点数据
	 * 
	 * @return
	 */
	public List<KDuplicateMapBornPoint> getAllBornPointEntity();

	/**
	 * 根据地图编辑器实例Id，获取副本地图上某个可发生碰撞检测的物体实例对象
	 * 
	 * @param mapInstanceId
	 * @return
	 */
	public CollisionEventObjectData getCollisionEventObject(int mapInstanceId);

	/**
	 * 获取副本地图上所有可发生碰撞检测的物体实例对象
	 * 
	 * @return
	 */
	public List<CollisionEventObjectData> getAllCollisionEventObject();

	/**
	 * 通知删除某个可发生碰撞检测的物体实例对象
	 * 
	 * @param mapInstanceId
	 *            ,地图编辑器实例Id
	 * @return
	 */
	public CollisionEventObjectData removeCollisionEventObjectData(
			int mapInstanceId);

	/**
	 * 通知重新放入某个可发生碰撞检测的物体实例对象，如果该实例已存在，则不做任何操作
	 * 
	 * @param mapInstanceId
	 *            ,地图编辑器实例Id
	 * @param attachment
	 *            ,事件自定义数据对象
	 * @return
	 */
	public CollisionEventObjectData putCollisionEventObjectData(
			int mapInstanceId, Object attachment);

	/**
	 * 设置碰撞检测监听接口
	 * 
	 * @param listener
	 */
	public void setCollisionEventListener(CollisionEventListener listener);
}
