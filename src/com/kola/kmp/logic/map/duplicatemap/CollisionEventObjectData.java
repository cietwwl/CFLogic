package com.kola.kmp.logic.map.duplicatemap;

public interface CollisionEventObjectData extends Comparable<CollisionEventObjectData>{
	
	/**
	 * 地图编辑器实例Id
	 * @return
	 */
	public int getMapInstanceId();
	
	/**
	 * 事件自定义数据对象
	 * @return
	 */
	public Object getAttachment();
	
	/**
	 * 设置事件自定义数据对象
	 * @return
	 */
	public void setAttachment(Object obj);
	
	/**
	 * 事件所在地图X坐标
	 * @return
	 */
	public float getX();
	
	/**
	 * 事件所在地图Y坐标
	 * @return
	 */
	public float getY();

}
