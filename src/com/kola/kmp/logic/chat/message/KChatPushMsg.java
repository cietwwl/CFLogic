package com.kola.kmp.logic.chat.message;

import ch.qos.logback.classic.pattern.Util;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.chat.ChatDataAbs;
import com.kola.kmp.logic.chat.KChatMsgPackCenter;
import com.kola.kmp.logic.mail.KMailConfig;
import com.kola.kmp.logic.other.KChatChannelTypeEnum;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.chat.KChatProtocol;

public class KChatPushMsg implements KChatProtocol {

	public static void pushChatInitMsg(KGamePlayerSession session) {
		KGameMessage rmsg = KGame.newLogicMessage(SM_PUSH_CHAT_INIT);
		KChatMsgPackCenter.packChatInitMsg(rmsg);
		session.send(rmsg);
	}

	public static void pushChatInitMsgToAllOnlineRoles() {
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_CHAT_INIT);
		KChatMsgPackCenter.packChatInitMsg(msg);

		KGameMessage dupMsg = msg.duplicate();
		for (Long roleId : KSupportFactory.getRoleModuleSupport().getAllOnLineRoleIds()) {
			if (KSupportFactory.getRoleModuleSupport().sendMsg(roleId, dupMsg)) {
				dupMsg = msg.duplicate();
			}
		}
	}

	public static void pushAttachmentConfirm(KGamePlayerSession session) {
		KGameMessage msg = KGame.newLogicMessage(SM_ATTACHMENT_CONFIRM);
		session.send(msg);
	}

	public static void pushChatData(ChatDataAbs chatData) {
		KGameMessage msg = KGame.newLogicMessage(SM_CHATMSG_BROADCAST);
		KChatMsgPackCenter.packChatData(msg, chatData);
		
	}
	
	public static KGameMessage genChatData(ChatDataAbs chatData) {
		KGameMessage msg = KGame.newLogicMessage(SM_CHATMSG_BROADCAST);
		KChatMsgPackCenter.packChatData(msg, chatData);
		return msg;
	}
	
	public static KGameMessage genSystemChatMsg(String chatString, boolean isShowTop, boolean isShowInChannel) {
		KGameMessage msg = KGame.newLogicMessage(KChatProtocol.SM_CHATMSG_BROADCAST);
		msg.writeByte(KChatChannelTypeEnum.系统.sign);
		msg.writeUtf8String(chatString);
		msg.writeUtf8String(UtilTool.getNotNullString(null));
		msg.writeLong(KMailConfig.SYS_MAIL_SENDER_ID);
		msg.writeByte(0);//VIP
		msg.writeBoolean(isShowTop);
		msg.writeBoolean(isShowInChannel);
		msg.writeBoolean(false);
		return msg;
	}

	/**
	 * <pre>
	 * 
	 * 
	 * @param session
	 * @param chatChannelType
	 * @param chatStr
	 * @param sysMailSenderId
	 * @param isShowTop
	 * @param isShowInChannel
	 * @author CamusHuang
	 * @creation 2014-3-5 下午4:37:10
	 * </pre>
	 */
	public static void pushChatDataWithNOSpeaker(KGamePlayerSession session, KChatChannelTypeEnum channelType, String chatStr, boolean isShowTop, boolean isShowInChannel) {
		KGameMessage msg = KGame.newLogicMessage(SM_CHATMSG_BROADCAST);
		KChatMsgPackCenter.packChatDataNoSpeaker(msg, channelType, chatStr, isShowTop, isShowInChannel);
		//
		session.send(msg);
	}
	
	public static void pushChatDataWithNOSpeaker(long roleId, KChatChannelTypeEnum channelType, String chatStr, boolean isShowTop, boolean isShowInChannel) {
		KGameMessage msg = KGame.newLogicMessage(SM_CHATMSG_BROADCAST);
		KChatMsgPackCenter.packChatDataNoSpeaker(msg, channelType, chatStr, isShowTop, isShowInChannel);
		//
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}
}
