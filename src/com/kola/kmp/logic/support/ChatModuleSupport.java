package com.kola.kmp.logic.support;

import java.util.List;

import com.koala.game.KGameMessage;
import com.kola.kmp.logic.chat.ChatDataAbs;
import com.kola.kmp.logic.chat.ChatDataFromSystem;
import com.kola.kmp.logic.chat.KWordBroadcastType;
import com.kola.kmp.logic.other.KChatChannelTypeEnum;

public interface ChatModuleSupport {

	/**
	 * <pre>
	 * 以【系统】身份【私聊】指定角色
	 * 
	 * @param chatString
	 * @param receiverRoleId
	 * @return 接收者数量
	 * @author CamusHuang
	 * @creation 2013-7-23 下午8:06:01
	 * </pre>
	 */
	public int sendChatToRole(String chatString, long receiverRoleId);

	/**
	 * <pre>
	 * 以【系统】身份【群发】【世界播报】或【系统频道】
	 * 1.默认发送给所有等级的在线角色
	 * 2.可指定是否显示于【世界播报】和【系统频道】
	 * 3.isShowTop||isShowInSystemChannel 必须等于true
	 * 
	 * @param chatString
	 * @param isShowTop 是否将本消息显示于【世界播报】
	 * @param isShowInSystemChannel 是否将本消息显示于【系统频道】
	 * @return 接收者数量
	 * @author CamusHuang
	 * @creation 2013-5-15 下午3:11:26
	 * </pre>
	 */
	public int sendSystemChat(String chatString, boolean isShowTop, boolean isShowInSystemChannel);
	
	/**
	 * <pre>
	 * 以【系统】身份【群发】【世界播报】或【系统频道】
	 * 1.默认发送给所有等级的在线角色
	 * 
	 * @param chatString
	 * @param type
	 * @return 接收者数量
	 * @author CamusHuang
	 * @creation 2013-5-15 下午3:11:26
	 * </pre>
	 */
	public int sendSystemChat(String chatString, KWordBroadcastType type);

	/**
	 * <pre>
	 * 以【系统】身份【群发】【世界播报】或【系统频道】
	 * 1.由参数指定在线角色等级上限和下限
	 * 2.可指定是否显示于【世界播报】和【系统频道】
	 * 3.isShowTop||isShowInSystemChannel 必须等于true
	 * 
	 * @param chatString
	 * @param isShowTop 是否将本消息显示于【世界播报】
	 * @param isShowInSystemChannel 是否将本消息显示于【系统频道】
	 * @param minRoleLv 接收者等级下限（包含）
	 * @param maxRoleLv 接收者等级上限（包含）
	 * @return 接收者数量
	 * @author CamusHuang
	 * @creation 2013-5-15 下午3:11:26
	 * </pre>
	 */
	public int sendSystemChat(String chatString, boolean isShowTop, boolean isShowInSystemChannel, int minRoleLv, int maxRoleLv);

	/**
	 * <pre>
	 * 以【系统】身份向【指定频道】发送聊天
	 * PS：只显示于【指定频道】，不显示于【世界播报】
	 * 
	 * receiverKey 接收者定义
	 * 【世界】：receiverKey 无效（全服发送）
	 * 【军团】：receiverKey 表示【军团ID】 
	 * 【组队】：receiverKey 表示【接收者角色ID】（组队中任一角色的ID均可） 
	 * 【附近】：receiverKey 表示【地图ID】
	 * 【私聊】：receiverKey 表示【接收者角色ID】
	 * 【系统】：receiverKey 同【世界】定义
	 * 
	 * 以下是不支持的行为:
	 * 1.使用世界（或军团、组队、附近）频道向指定的某一个角色单独发送消息
	 * 
	 * @param channelType 频道类型
	 * @param chatString 聊天内容
	 * @param receiverKey 接收者
	 * @return 发送结果，如果为-1表示频道不存在，其它值代表广播出的数量
	 * @author CamusHuang
	 * @creation 2014-3-6 上午10:09:04
	 * </pre>
	 */
	public int sendChatToAnyChannel(KChatChannelTypeEnum channelType, String chatString, long receiverKey);
	
	/**
	 * <pre>
	 * 以【玩家角色】身份向【世界频道】发送聊天
	 * PS：只显示于【世界频道】，不显示于【世界播报】
	 * 主要用于支持GM直接友好聊天
	 * 
	 * @param senderRole 发送者角色名，可以不存在实际角色
	 * @param chatString 聊天内容
	 * @return 发送结果，如果为-1表示频道不存在，其它值代表广播出的数量
	 * @author CamusHuang
	 * @creation 2014-3-6 上午10:09:04
	 * </pre>
	 */
	public int sendChatToWorldChannel(String senderRole, String chatString);
	
	/**
	 * <pre>
	 * 以【系统】身份使用【指定频道】向【指定玩家】发送聊天
	 * PS：只显示于【指定频道】，不显示于【世界播报】
	 * PS：只支持以下频道：【世界】、【附近】、【系统】（同【世界】）
	 * PS：不支持以下频道：【军团】、【组队】、【私聊】
	 * 
	 * @param channelType 频道类型
	 * @param chatString 聊天内容
	 * @param receiverKey 接收者
	 * @return 发送结果，如果为-1表示频道不存在，其它值代表广播出的数量
	 * @author CamusHuang
	 * @creation 2014-3-6 上午10:09:04
	 * </pre>
	 */
	public int sendChatToAnyOne(KChatChannelTypeEnum channelType, String chatString, List<Long> receiverRoleIds);	

	/**
	 * <pre>
	 * 以【系统】身份生成【世界播报】或【系统频道】消息，由调用者决定发送给谁
	 * 调用者可以将此消息群发给任意玩家，例如群发给军团全体成员，又或者群发给其它特定人员
	 * 
	 * PS：调用者在发送后需手工通知GM:
	 * ChatData chatData=genChatDataForGM(String chatString);
	 * KSupportFactory.getGMSupport().onChat(chatData);
	 * 
	 * @deprecated 调用者在发送后需要再通知GM
	 * @param chatString
	 * @param isShowTop
	 * @param isShowInChannel
	 * @return
	 * @author CamusHuang
	 * @creation 2013-8-29 上午10:49:33
	 * </pre>
	 */
	public KGameMessage genSystemChatMsg(String chatString, boolean isShowTop, boolean isShowInChannel);
	
	/**
	 * <pre>
	 * 以【系统】身份生成【世界播报】或【系统频道】消息，由调用者决定发送给谁
	 * 调用者可以将此消息群发给任意玩家，例如群发给军团全体成员，又或者群发给其它特定人员
	 * 
	 * PS：调用者在发送后需手工通知GM:
	 * ChatData chatData=genChatDataForGM(String chatString);
	 * KSupportFactory.getGMSupport().onChat(chatData);
	 * 
	 * @deprecated 调用者在发送后需要再通知GM
	 * @param chatString
	 * @param type
	 * @return
	 * @author CamusHuang
	 * @creation 2014-6-20 上午11:08:13
	 * </pre>
	 */
	public KGameMessage genSystemChatMsg(String chatString, KWordBroadcastType type);

	/**
	 * <pre>
	 * 生成一个聊天数据对象，由调用者通知GM
	 * 参考:KSupportFactory.getGMSupport().onChat(chatData);
	 * 
	 * @deprecated 目前仅由军团战模块使用
	 * @param chatString
	 * @param minRoleLv
	 * @param maxRoleLv
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-6 上午11:44:20
	 * </pre>
	 */
	public ChatDataAbs genSystemChatDataForGM(String chatString, int minRoleLv, int maxRoleLv);
	
	/**
	 * <pre>
	 * 生成一个聊天数据对象，由调用者通知GM
	 * 参考:KSupportFactory.getGMSupport().onChat(chatData);
	 * 
	 * @deprecated 目前仅由军团战模块使用
	 * @param chatString
	 * @param type
	 * @return
	 * @author CamusHuang
	 * @creation 2014-6-20 上午11:11:12
	 * </pre>
	 */
	public ChatDataFromSystem genSystemChatDataForGM(String chatString, KWordBroadcastType type);	
}
