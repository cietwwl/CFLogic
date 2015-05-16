package com.kola.kmp.logic.chat;

import com.koala.game.KGameMessage;
import com.kola.kmp.logic.chat.message.KChatPushMsg;
import com.kola.kmp.logic.other.KChatChannelTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * <pre>
 * 私聊聊天频道
 * 
 * @author CamusHuang
 * @creation 2014-7-22 上午10:13:51
 * </pre>
 */
public class ChatChannelPrivate extends ChatChannelAbs {

	ChatChannelPrivate(KChatChannelTypeEnum channelType) {
		super(channelType);
	}

	int broadcastByChannel(ChatDataAbs chatData, KGameMessage msg) {
		if (!KSupportFactory.getRoleModuleSupport().isRoleDataInCache(chatData.receiverId)) {
			return 0;
		}
		KRole receiverRole = KSupportFactory.getRoleModuleSupport().getRole(chatData.receiverId);
		if (receiverRole == null) {
			return 0;
		}

		{
			receiverRole.sendMsg(msg);

			// 如果是普通角色发出的私聊，则发送反馈
			if (chatData instanceof ChatDataFromRole) {
				ChatDataFromRole chatData2 = (ChatDataFromRole) chatData;
				KChatPushMsg.pushChatDataWithNOSpeaker(chatData.senderRoleId, KChatChannelTypeEnum.私聊, chatData2.getSelfChatString(), false, true);
			}

			// 通知GM
			KSupportFactory.getGMSupport().onChat(chatData);
			return 1;
		}
	}
}
