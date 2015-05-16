package com.kola.kmp.logic.support;

import com.koala.game.KGameMessage;
import com.kola.kmp.logic.chat.ChatDataAbs;


/**
 * <pre>
 * 
 * @author camus
 * @creation 2012-12-30 下午2:48:14
 * </pre>
 */
public interface GMSupport {
	/**
	 * <pre>
	 * 世界、军团以外的聊天
	 * 
	 * @param chatData
	 * @author CamusHuang
	 * @creation 2013-6-1 下午4:57:30
	 * </pre>
	 */
	public void onChat(ChatDataAbs chatData);
	
	/**
	 * <pre>
	 * 世界、军团聊天
	 * 
	 * @param chatData
	 * @param areaName
	 * @author CamusHuang
	 * @creation 2013-6-1 下午4:57:47
	 * </pre>
	 */
	public void onChat(ChatDataAbs chatData, String areaName);

	/**
	 * <pre>
	 * 发送邮件给GM
	 * 如果GMS断连，则缓存
	 * 缓存1000条，FIFO机制
	 * 
	 * @param roleId
	 * @param roleName
	 * @param title
	 * @param content
	 * @return
	 * @author camus
	 * @creation 2013-6-15 下午9:52:21
	 * </pre>
	 */
	public boolean onMail(long roleId,String roleName,String title,String content);
}
