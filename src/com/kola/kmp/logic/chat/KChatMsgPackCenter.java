package com.kola.kmp.logic.chat;

import com.koala.game.KGameMessage;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.gang.KGangMember;
import com.kola.kmp.logic.gang.KGangPositionEnum;
import com.kola.kmp.logic.mail.KMailConfig;
import com.kola.kmp.logic.other.KChatChannelTypeEnum;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.chat.KChatProtocol;

public class KChatMsgPackCenter implements KChatProtocol {

	public static void packChatInitMsg(KGameMessage rmsg) {
		rmsg.writeByte(ChatChannelAbs.chatchannels.size());
		for (ChatChannelAbs cc : ChatChannelAbs.chatchannels.values()) {
			rmsg.writeByte(cc.getTypeEnum().sign);
			rmsg.writeBoolean(cc.isOn());
			rmsg.writeLong(cc.getCD());
		}
		// 发送者链接菜单
		rmsg.writeUtf8String(KChatConfig.getInstance().popupmenus);
		// 客户端消息缓存数量
		rmsg.writeInt(KChatConfig.getInstance().cachedChatDataCapacity_clientside);
		rmsg.writeInt(KChatConfig.getInstance().maxcharpermsg);
		rmsg.writeInt(KChatConfig.getInstance().maxemopermsg);
		// 图片和声音限制
//		rmsg.writeInt(KChatConfig.getInstance().picColdTime);
//		rmsg.writeInt(KChatConfig.getInstance().soundColdTime);
//		rmsg.writeShort(KChatConfig.getInstance().picOpenLevel);
//		rmsg.writeShort(KChatConfig.getInstance().soundOpenLevel);
//		rmsg.writeByte(KChatConfig.getInstance().picVipLimite);
//		rmsg.writeByte(KChatConfig.getInstance().soundVipLimite);
//		rmsg.writeShort(KChatConfig.getInstance().picTimesLimite);
//		rmsg.writeShort(KChatConfig.getInstance().soundTimesLimite);
//		rmsg.writeBoolean(KChatConfig.getInstance().isOpenPic);// 图片开启
//		rmsg.writeBoolean(KChatConfig.getInstance().isOpenSound);// 声音开启

		rmsg.writeShort(KChatConfig.getInstance().worldChatOpenLevel);
	}

	public static void packChatData(KGameMessage msg, ChatDataAbs chatData) {
		msg.writeByte(chatData.channelType.sign);
		msg.writeUtf8String(chatData.getOutChatString());
		msg.writeUtf8String(chatData.senderRoleName);
		msg.writeLong(chatData.senderRoleId);
		msg.writeByte(chatData.vipLv);
		msg.writeBoolean(chatData.isShowTop());
		msg.writeBoolean(chatData.isShowInChannel());
		
		// 军团名称，职位
		{
			KGang gang = KSupportFactory.getGangSupport().getGangByRoleId(chatData.senderRoleId);
			KGangMember mem = null;
			if(gang!=null){
				mem=gang.getMember(chatData.senderRoleId);
			}
			
			if(mem==null || mem.getPositionEnum() == KGangPositionEnum.成员){
				msg.writeBoolean(false);
			} else {
				msg.writeBoolean(true);
				msg.writeUtf8String(gang.getName());
				msg.writeByte(mem.getPositionEnum().sign);
			}
		}
	}

	public static void packChatDataNoSpeaker(KGameMessage msg, KChatChannelTypeEnum channelType, String string, boolean isShowTop, boolean isShowInChannel) {
		msg.writeByte(channelType.sign);
		msg.writeUtf8String(string);
		msg.writeUtf8String(UtilTool.getNotNullString(null));
		msg.writeLong(KMailConfig.SYS_MAIL_SENDER_ID);
		msg.writeByte(0);//VIP
		msg.writeBoolean(isShowTop);
		msg.writeBoolean(isShowInChannel);
		msg.writeBoolean(false);
	}

}
