package com.kola.kmp.logic.chat;

import com.koala.game.KGameMessage;
import com.kola.kmp.logic.other.KChatChannelTypeEnum;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * <pre>
 * 军团聊天频道
 * 
 * @author CamusHuang
 * @creation 2014-7-22 上午10:13:51
 * </pre>
 */
public class ChatChannelGang extends ChatChannelAbs {

	ChatChannelGang(KChatChannelTypeEnum channelType) {
		super(channelType);
	}

	/**
	 * <pre>
	 * 根据发送者角色ID chatData.receiverId 找到相应军团，遍历军团成员发送聊天内容
	 * 
	 * @param chatData
	 * @param msg
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-12 上午11:05:28
	 * </pre>
	 */
	int broadcastByChannel(ChatDataAbs chatData, KGameMessage msg) {

		int count = KSupportFactory.getGangSupport().broadcastChatToGang(chatData, msg);
		return count;
	}
}
