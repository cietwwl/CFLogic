package com.kola.kmp.logic.support;

import java.util.List;

import com.kola.kmp.logic.other.KRelationShipTypeEnum;
import com.kola.kmp.logic.role.KRole;


public interface RelationShipModuleSupport {

	/**
	 * <pre>
	 * 私聊通知
	 * 
	 * @param roleId
	 * @param oppRoleId
	 * @author CamusHuang
	 * @creation 2014-3-13 下午4:53:20
	 * </pre>
	 */
	public void notifyPMChat(long roleId, long oppRoleId);

	/**
	 * <pre>
	 * 亲密度行为通知
	 * 好友副本结束时调用
	 * 
	 * @param roleId 发起邀请者
	 * @param oppRoleId 发起方的好友
	 * @param intimacy  亲密度
	 * @author CamusHuang
	 * @creation 2014-3-14 上午9:29:46
	 * </pre>
	 */
	public void notifyCloseAction_WAR(long roleId, long oppRoleId, boolean isWin);
	/**
	 * <pre>
	 * 亲密度行为通知
	 * 发起私聊时时调用
	 * 
	 * @param roleId 发起方（发出私聊者。。。）
	 * @param oppRoleId 发起方的好友
	 * @param type
	 * @author CamusHuang
	 * @creation 2014-3-14 上午9:29:46
	 * </pre>
	 */
	public void notifyCloseAction_PMChat(long roleId, long oppRoleId);

	/**
	 * <pre>
	 * 对方是否在我的黑名单中
	 * 
	 * @param roleId 我方
	 * @param oppRoleId 对方
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-13 下午8:14:14
	 * </pre>
	 */
	public boolean isInBlackList(long roleId, long oppRoleId);

	/**
	 * <pre>
	 * 获取对方对自己的亲密度
	 * 如果不是好友则返回0
	 * 
	 * @param roleId
	 * @param oppRoleId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-14 上午9:26:47
	 * </pre>
	 */
	public int getCloseness(long roleId, long oppRoleId);
	
	/**
	 * <pre>
	 * 对方是否在我的好友列表中
	 * 
	 * @param roleId 我方
	 * @param oppRoleId 对方
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-13 下午8:14:14
	 * </pre>
	 */
	public boolean isInFriendList(long roleId, long oppRoleId);
	
	/**
	 * <pre>
	 * 获取角色的所有好友
	 * 
	 * @param roleId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-7-2 下午9:03:51
	 * </pre>
	 */
	public List<Long> getAllFriends(long roleId);
	
	/**
	 * <pre>
	 * 检查角色指定关系类型数量是否已满
	 * PS：好友关系极限容量会跟随VIP等级变化
	 * 
	 * @param roleId
	 * @param type
	 * @return
	 * @author CamusHuang
	 * @creation 2014-8-27 上午10:00:59
	 * </pre>
	 */
	public boolean isRelationShipFull(long roleId, KRelationShipTypeEnum type);
	
	/**
	 * <pre>
	 * 自动加好友
	 * 
	 * @param role 角色
	 * @param oppRole AI角色
	 * @author CamusHuang
	 * @creation 2014-8-29 下午6:21:15
	 * </pre>
	 */
	public void autoAppFriend(KRole role, KRole oppRole);
}
