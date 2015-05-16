package com.kola.kmp.logic.role;

import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.DataStatus;

public interface IRoleGameSettingData {
	
	/**
	 * <pre>
	 * 设置DatatStatus对象，当本对象数据发生改变的时候，
	 * 应该调用{@link DataStatus#notifyUpdate()}
	 * 这样，母体就会知道数据发生改变
	 * </pre>
	 * @param entity
	 */
	public void setDataStatusInstance(DataStatus entity);
	
	public void decode(String attribute) throws Exception;

	public String encode() throws Exception;

	/**
	 * 是否屏蔽接受切磋（在线PVP请求）
	 * 
	 * @return
	 */
	public boolean isBlockOnlinePVP();

	public void setBlockOnlinePVP(boolean isBlockOnlinePVP);

	/**
	 * 是否屏蔽聊天
	 * 
	 * @return
	 */
	public boolean isBlockChat();

	public void setBlockChat(boolean isBlockChat);

	
	/**
	 * 角色地图显示人数的等级
	 * 
	 * @return
	 */
	public byte getMapShowPlayerLevel();

	public void setMapShowPlayerLevel(byte _mapShowPlayerLevel);
	
	/**
	 * 
	 * 是否处于免打扰状态
	 * 
	 * @return
	 */
	public boolean isNotDisturb();
	
	public void setNotDisturb(boolean isNotDisturb);
	
	/**
	 * 是否用指令打开所有关卡
	 * @return
	 */
	public boolean isDebugOpenLevel();
	
	public void setDebugOpenLevel(boolean isOpen);
	
	public void notifyLogin();
	
	public void notifyLogout();
	
	public int notifyUpgradeLv();
	
	/**
	 * 根据客户端状态（内存容量）检测确定角色的地图显示人数的等级
	 * @param session
	 * @return
	 */
	public void checkAndSetMapShowPlayerLevel(KGamePlayerSession session);
	
}
