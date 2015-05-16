package com.kola.kmp.logic.gm;

import java.util.concurrent.LinkedBlockingQueue;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.chat.ChatDataAbs;
import com.kola.kmp.logic.support.GMSupport;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.gm.ProtocolGs;

/**
 * <pre>
 * </pre>
 */
public class KGMSupportImpl implements GMSupport, ProtocolGs {

	private static LinkedBlockingQueue<GMMail> GMMailCache = new LinkedBlockingQueue<GMMail>(1000);

	static class GMMail {
		long roleId;
		String roleName;
		String title;
		String content;
		long sendTime;
	}

	static void onTimeSignalToClearGMMailCache() {
		KGamePlayerSession session = KGMLogic.getGMSession();
		if (session == null) {
			return;
		}

		GMMail mail = null;
		while ((mail = GMMailCache.poll()) != null) {
			KGameMessage msg = KGame.newLogicMessage(GS_GMS_TCP_NEW_EMAIL);
			msg.writeLong(mail.roleId);
			msg.writeUtf8String(mail.roleName);
			msg.writeUtf8String(mail.title);
			msg.writeUtf8String(mail.content);
			msg.writeLong(mail.sendTime);

			if (!session.send(msg)) {
				GMMailCache.offer(mail);
				break;
			}
		}
	}

	/**
	 * <pre>
	 * GS聊天频道信息
	 * int 频道类型（2;世界频道 3;区域频道 4;私聊频道）
	 * String 区域名(公告频道为"",世界频道为"",区域频道为 "区域名,地图名"，私聊频道为"",公会频道为"公会名")
	 * String 对话发起者(世界频道为说话者,区域频道为说话者，私聊频道为对话发起者)
	 * String 对话接收者（世界频道、区域频道为""，私聊频道为对话接受者）
	 * String 消息内容
	 * public static final int GS_GMS_CHANNEL_MESSAGE
	 * 
	 * @param channel
	 * @param channelName
	 * @param speaker
	 * @param privateReceiver
	 * @param content
	 * @author CamusHuang
	 * @creation 2013-6-1 下午4:20:54
	 * </pre>
	 */
	private void onChat(int channel, String areaName, String speaker, String privateReceiver, String content) {
		KGamePlayerSession session = KGMLogic.getGMSession();
		if (session == null) {
			return;
		}

		KGameMessage msg = KGame.newLogicMessage(GS_GMS_CHANNEL_MESSAGE);
		// msg.writeInt(KGameServer.getInstance().getGSID());
		msg.writeInt(channel);
		msg.writeUtf8String(areaName);
		msg.writeUtf8String(speaker);
		msg.writeUtf8String(privateReceiver);
		msg.writeUtf8String(content);

		session.send(msg);
	}

	@Override
	public void onChat(ChatDataAbs chatData) {
		switch (chatData.channelType) {
		case 私聊:
			String receiverRoleName = KSupportFactory.getRoleModuleSupport().getRoleName(chatData.receiverId);
			onChat(chatData.channelType.sign, "", chatData.senderRoleName, receiverRoleName, chatData.getOrgChatString());
			break;
		case 系统:
			onChat(chatData.channelType.sign, "", chatData.senderRoleName, "", "[" + chatData.getMinRoleLv() + "-" + chatData.getMaxRoleLv() + "]" + chatData.getOrgChatString());
			break;
		default:
			onChat(chatData.channelType.sign, "", chatData.senderRoleName, "", chatData.getOrgChatString());
			break;
		}
	}

	@Override
	public void onChat(ChatDataAbs chatData, String areaName) {
		onChat(chatData.channelType.sign, areaName, chatData.senderRoleName, "", chatData.getOrgChatString());
	}

	/**
	 * <pre>
	 * GS信件信息
	 * long 发送者邮件的角色Id
	 * String 发送邮件的角色昵称
	 * String 信件标题
	 * String 信件内容
	 * </pre>
	 */
	public boolean onMail(long roleId, String roleName, String title, String content) {

		long nowTime = System.currentTimeMillis();
		boolean isSendSuccess = false;
		try {
			KGamePlayerSession session = KGMLogic.getGMSession();
			if (session == null) {
				return isSendSuccess;
			}

			// if (KGMLogic.getGmInMailCount() == 0) {
			// return isSendSuccess;
			// }

			KGameMessage msg = KGame.newLogicMessage(GS_GMS_TCP_NEW_EMAIL);
			msg.writeLong(roleId);
			msg.writeUtf8String(roleName);
			msg.writeUtf8String(title);
			msg.writeUtf8String(content);
			msg.writeLong(nowTime);

			isSendSuccess = session.send(msg);
			return isSendSuccess;
		} finally {
			if (!isSendSuccess) {
				// GMS离线，缓存邮件
				GMMail mail = new GMMail();
				mail.roleId = roleId;
				mail.roleName = roleName;
				mail.title = title;
				mail.content = content;
				mail.sendTime = nowTime;
				GMMailCache.offer(mail);
			}
		}
	}
}
