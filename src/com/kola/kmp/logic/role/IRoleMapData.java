package com.kola.kmp.logic.role;

import com.kola.kgame.cache.DataStatus;

/**
 * 
 * @author PERRY CHAN
 */
public interface IRoleMapData {

	/**
	 * <pre>
	 * 设置DatatStatus对象，当本对象数据发生改变的时候，
	 * 应该调用{@link DataStatus#notifyUpdate()}
	 * 这样，母体就会知道数据发生改变
	 * </pre>
	 * @param entity
	 */
	public void setDataStatusInstance(DataStatus entity);
	
	/**
	 * 
	 * 获取角色当前的地图id
	 * 
	 * @return
	 */
	public int getCurrentMapId();
	
	public void setCurrentMapId(int mapId);
	
	/**
	 * 
	 * 获取角色上一次的地图id
	 * 
	 * @return
	 */
	public int getLastMapId();
	
	public void setLastMapId(int mapId);
	
	/**
	 * 
	 * 获取角色的x坐标
	 * 
	 * @return
	 */
	public float getCorX();
	
	public void setCorX(float corX);
	
	/**
	 * 
	 * 获取角色的y坐标
	 * 
	 * @return
	 */
	public float getCorY();
	
	public void setCorY(float corY);	
	
	
	/**
	 * 
	 * 解码保存的属性
	 * 
	 * @param attribute
	 * @return
	 */
	public void decode(String attribute) throws Exception;
	
	/**
	 * 
	 * 编码保存的属性
	 * 
	 * @return
	 */
	public String encode() throws Exception;
	/**
	 * 是否在副本地图
	 * @return
	 */
	public boolean isInDuplicateMap();

	public void setInDuplicateMap(boolean isInDuplicateMap);
	
	/**
	 * 获取当前副本地图Id
	 * @return
	 */
	public int getCurrentDuplicateMapId();

	public void setCurrentDuplicateMapId(int currentDuplicateMapId);
	
	public float getCorDuplicateX();

	public void setCorDuplicateX(float corDuplicateX);

	public float getCorDuplicateY();

	public void setCorDuplicateY(float corDuplicateY);
}
