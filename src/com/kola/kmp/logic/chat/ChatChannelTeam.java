package com.kola.kmp.logic.chat;

import com.koala.game.KGameMessage;
import com.kola.kmp.logic.other.KChatChannelTypeEnum;

/**
 * <pre>
 * 组队聊天频道
 * 
 * @author CamusHuang
 * @creation 2014-7-22 上午10:13:51
 * </pre>
 */
public class ChatChannelTeam extends ChatChannelAbs {

	ChatChannelTeam(KChatChannelTypeEnum channelType) {
		super(channelType);
	}

	int broadcastByChannel(ChatDataAbs chatData, KGameMessage msg) {
		// CTODO 根据发送者角色ID chatData.receiverId 找到相应组队，遍历组队成员发送聊天内容

		return 0;
		// KRole role;
		// RoleModuleSupport roleSupport =
		// KSupportFactory.getRoleModuleSupport();
		// //
		// int count = 0;
		// for (long roleId : roleSupport.getAllOnLineRoleIds()) {
		// role = roleSupport.getRole(roleId);
		// if (role == null) {
		// continue;
		// }
		// role.sendMsg(msg.duplicate());
		// count++;
		// }
		//
		// // 通知GM
		// KSupportFactory.getGMSupport().onChat(chatData);
		//
		// return count;
	}
}
