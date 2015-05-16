package com.kola.kmp.logic;

import java.util.List;

import com.kola.kgame.cache.role.Role;

public interface INotificationTemplate {

	/**
	 * 按照注册时间推送 
	 */
	byte TYPE_PUSH_BY_REGISTER_TIME = 1;
	
	/**
	 * 按照等级推送
	 */
	byte TYPE_PUSH_BY_LEVEL = 2;
	
	/**
	 * 重复类型：不重复
	 */
	byte REPEAT_TYPE_NONE = 0;
	/**
	 * 重复类型：按分钟
	 */
	byte REPEAT_TYPE_MINUTE = 1;
	/**
	 * 重复类型：按小时
	 */
	byte REPEAT_TYPE_HOUR = 2;
	/**
	 * 重复类型：按天
	 */
	byte REPEAT_TYPE_DAY = 3;
	/**
	 * 重复类型：按周
	 */
	byte REPEAT_TYPE_WEEK = 4;
	
	/**
	 * 
	 * @param role
	 * @return
	 */
	List<INotification> getNotifications(Role role);
	
	/**
	 * 
	 * @return
	 */
	long getStartTime();
	
	/**
	 * 
	 * @return
	 */
	long getEndTime();
	
	/**
	 * 
	 * @return
	 */
	String getContent();
}
