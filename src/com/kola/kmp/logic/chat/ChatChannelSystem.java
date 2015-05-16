package com.kola.kmp.logic.chat;

import com.koala.game.KGameMessage;
import com.kola.kmp.logic.other.KChatChannelTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;

/**
 * <pre>
 * 系统聊天频道
 * 
 * @author CamusHuang
 * @creation 2014-7-22 上午10:13:51
 * </pre>
 */
public class ChatChannelSystem extends ChatChannelAbs {

	ChatChannelSystem(KChatChannelTypeEnum channelType) {
		super(channelType);
	}

	/**
	 * <pre>
	 * CTODO 系统频道有可能是全世界广播、有可能是针对单个角色、有可能针对队伍……
	 * 
	 * @param chatData
	 * @param msg
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-4 下午7:55:25
	 * </pre>
	 */
	int broadcastByChannel(ChatDataAbs chatData, KGameMessage msg) {
		KRole role;
		RoleModuleSupport roleSupport = KSupportFactory.getRoleModuleSupport();
		//
		KGameMessage dupMsg = msg.duplicate();
		int count = 0;
		for (long roleId : roleSupport.getAllOnLineRoleIds()) {
			role = roleSupport.getRole(roleId);
			if (role == null) {
				continue;
			}
			// 等级过滤
			if (chatData.isShouldSend(role.getLevel())) {
				if (role.sendMsg(dupMsg)) {
					dupMsg = msg.duplicate();
					count++;
				}
			}
		}
		// 通知GM
		KSupportFactory.getGMSupport().onChat(chatData);
		return count;
	}
}
