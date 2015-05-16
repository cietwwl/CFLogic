package com.kola.kmp.logic.chat.message;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayer;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.chat.KChatLogic;
import com.kola.kmp.logic.other.KChatChannelTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.chat.KChatProtocol;

public class KSendChatMsg implements GameMessageProcesser, KChatProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KSendChatMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_CHATMSG_SEND;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		byte channelTypeId = msg.readByte();
		KChatChannelTypeEnum channelType = KChatChannelTypeEnum.getEnum(channelTypeId);
		String chatStr = msg.readUtf8String();
		long receiverRoleId= msg.readLong();
		byte attCount =  msg.readByte();
		byte[][] attDatas=null;
		if(attCount>0){
			attDatas=new byte[attCount][];
			for(int i=0;i<attCount;i++){
				int datalen = msg.readInt();
				attDatas[i] = new byte[datalen];
				msg.readBytes(attDatas[i]);
			}
		}
		
		// -------------
		KGamePlayer player = session.getBoundPlayer();
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (player == null || role == null) {
			return;
		}
		
		KChatLogic.dealMsg_sendChat(session, player, role, receiverRoleId, channelType, chatStr, attDatas);
	}
}
