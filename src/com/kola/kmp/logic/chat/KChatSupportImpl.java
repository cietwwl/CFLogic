package com.kola.kmp.logic.chat;

import java.util.List;

import com.koala.game.KGameMessage;
import com.kola.kmp.logic.chat.message.KChatPushMsg;
import com.kola.kmp.logic.other.KChatChannelTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.ChatModuleSupport;
import com.kola.kmp.logic.support.KSupportFactory;

public class KChatSupportImpl implements ChatModuleSupport {

	public int sendChatToRole(String chatString, long receiverRoleId) {
		return sendChatToAnyChannel(KChatChannelTypeEnum.私聊, chatString, receiverRoleId);
	}

	@Override
	public int sendSystemChat(String chatString, boolean isShowTop, boolean isShowInSystemChannel) {
		return sendSystemChat(chatString, isShowTop, isShowInSystemChannel, 0, Short.MAX_VALUE);
	}
	
	public int sendSystemChat(String chatString, KWordBroadcastType type){
		return sendSystemChat(chatString, type.isShowTop(), type.isShowInChannel(), type.getMinRoleLv(), type.getMaxRoleLv());
	}

	public int sendSystemChat(String chatString, boolean isShowTop, boolean isShowInSystemChannel, int minRoleLv, int maxRoleLv) {
		if (chatString == null || chatString.isEmpty()) {
			return 0;
		}

		if (isShowTop || isShowInSystemChannel) {
			ChatDataFromSystem chatData = new ChatDataFromSystem(chatString, isShowTop, isShowInSystemChannel, minRoleLv, maxRoleLv);
			return KChatLogic.sendChatFinally(chatData);
		}
		return 0;
	}

	@Override
	public int sendChatToAnyChannel(KChatChannelTypeEnum channelType, String chatString, long receiverKey) {
		if (chatString == null || chatString.isEmpty()) {
			return 0;
		}

		ChatDataFromSystem chatData = null;
		if (channelType == KChatChannelTypeEnum.私聊) {
			KRole receiverRole = KSupportFactory.getRoleModuleSupport().getRole(receiverKey);
			if (receiverRole == null) {
				return 0;
			}
			chatData = new ChatDataFromSystem(channelType, chatString, receiverKey);
		} else {
			chatData = new ChatDataFromSystem(channelType, chatString, receiverKey);
		}
		return KChatLogic.sendChatFinally(chatData);
	}

	@Override
	public int sendChatToWorldChannel(String senderRole, String chatString) {
		if (chatString == null || chatString.isEmpty()) {
			return 0;
		}

		ChatDataFromRole chatData = new ChatDataFromRole(KChatChannelTypeEnum.世界, chatString, 0, senderRole, (byte)0, 0, null);
		return KChatLogic.sendChatFinally(chatData);
	}

	public int sendChatToAnyOne(KChatChannelTypeEnum channelType, String chatString, List<Long> receiverRoleIds) {
		if (chatString == null || chatString.isEmpty()) {
			return 0;
		}

		// * PS：只支持以下频道：【世界】、【附近】、【系统】（同【世界】）
		// * PS：不支持以下频道：【军团】、【组队】、【私聊】
		switch (channelType) {
		case 军团:
		case 组队:
		case 私聊:
			return 0;
		default:
			break;
		}
		ChatDataFromSystem chatData = new ChatDataFromSystem(channelType, chatString, receiverRoleIds);

		return KChatLogic.sendChatFinally(chatData);
	}

	public KGameMessage genSystemChatMsg(String chatString, boolean isShowTop, boolean isShowInChannel) {
		return KChatPushMsg.genSystemChatMsg(chatString, isShowTop, isShowInChannel);
	}
	
	public KGameMessage genSystemChatMsg(String chatString, KWordBroadcastType type){
		return KChatPushMsg.genSystemChatMsg(chatString, type.isShowTop(), type.isShowInChannel());
	}

	public ChatDataFromSystem genSystemChatDataForGM(String chatString, int minRoleLv, int maxRoleLv) {
		ChatDataFromSystem chatData = new ChatDataFromSystem(chatString, true, true, minRoleLv, maxRoleLv);
		return chatData;
	}
	
	public ChatDataFromSystem genSystemChatDataForGM(String chatString, KWordBroadcastType type) {
		ChatDataFromSystem chatData = new ChatDataFromSystem(chatString, true, true, type.getMinRoleLv(), type.getMaxRoleLv());
		return chatData;
	}
}
