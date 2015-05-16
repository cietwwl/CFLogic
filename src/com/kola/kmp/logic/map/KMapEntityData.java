package com.kola.kmp.logic.map;

import java.util.List;

import com.kola.kmp.logic.other.KMapEntityTypeEnum;

public interface KMapEntityData<T> {
	
	/**
	 * 取得该物体的类型枚举
	 * 
	 * @return 类型
	 */
	public KMapEntityTypeEnum getEntityType();

	/**
	 * 判断是否角色类型的Entity
	 * 
	 * @return
	 */
	public boolean isPlayerRoleType();

	/**
	 * 取得该物体的对象实例ID，例如playerId、itemId等等
	 * 
	 * @return
	 */
	public long getSourceObjectID();

	/**
	 * 取得“源对象”。<br>
	 * 例如：PlayerRole可以是一个GameMapEntity，则此方法返回的就是PlayerRole对象实例。
	 * 
	 * @return
	 */
	public T getSourceObject();

	/**
	 * 取得本地图实体的地图坐标<br>
	 * 
	 * @return KCoordinate 地图坐标（X、Y）
	 */
	public KCoordinate getCoordinate();

	/**
	 * 设置本地图实体的地图坐标<br>
	 * 
	 * @return KCoordinate 地图坐标（X、Y）
	 */
	public void setCoordinate(KCoordinate coor);
	
	/**
	 * 获取地图持有我角色实例的角色对象Id（即地图上显示我的角色的其他角色）
	 * @return
	 */
	public List<Long> getMyHolderRoleIds();
	
	/**
	 * 获取地图上我能够显示的其他角色ID列表
	 * @return
	 */
	public List<Long> getMyShowRoleIds();

}
