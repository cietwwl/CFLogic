package com.kola.kmp.logic.chat;

import com.koala.game.KGameMessage;
import com.kola.kmp.logic.other.KChatChannelTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;

/**
 * <pre>
 * 世界聊天频道
 * 
 * @author CamusHuang
 * @creation 2014-7-22 上午10:13:51
 * </pre>
 */
public class ChatChannelWorld extends ChatChannelAbs {

	ChatChannelWorld(KChatChannelTypeEnum channelType) {
		super(channelType);
	}

	int broadcastByChannel(ChatDataAbs chatData, KGameMessage msg) {
		RoleModuleSupport roleSupport = KSupportFactory.getRoleModuleSupport();
		//
		KGameMessage dupMsg = msg.duplicate();
		int count = 0;
		KRole role;
		for (long roleId : roleSupport.getAllOnLineRoleIds()) {
			role = roleSupport.getRole(roleId);
			if (role == null) {
				continue;
			}

			// 角色屏蔽聊天设置
			// IRoleGameSettingData set = role.getRoleGameSettingData();
			// if (set.isBlockChat()) {
			// continue;
			// }

			// 等级过滤
			if (!chatData.isShouldSend(role.getLevel())) {
				continue;
			}

			if(role.sendMsg(dupMsg)){
				dupMsg = msg.duplicate();
				count++;
			}
		}

		// 通知GM
		KSupportFactory.getGMSupport().onChat(chatData);

		return count;
	}
}
