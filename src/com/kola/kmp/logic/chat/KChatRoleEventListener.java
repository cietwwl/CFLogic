package com.kola.kmp.logic.chat;

import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.chat.message.KChatPushMsg;
import com.kola.kmp.logic.role.IRoleEventListener;
import com.kola.kmp.logic.role.KRole;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2013-1-29 下午5:50:54
 * </pre>
 */
public class KChatRoleEventListener implements IRoleEventListener {

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {
		// 主动推送聊天模块初始信息
		KChatPushMsg.pushChatInitMsg(session);

		// 推送缓存的私聊
		KChatLogic.notifyForSendCachePrivateChat(session, role);
	}

	@Override
	public void notifyRoleLeavedGame(/*KGamePlayerSession session,*/KRole role) {
		// 忽略
	}

	@Override
	public void notifyRoleCreated(KGamePlayerSession session, KRole role) {
		// 忽略
	}

	@Override
	public void notifyRoleDeleted(long roleId) {
		ChatCache.pollPrivateChats(roleId);
	}

	@Override
	public void notifyRoleLevelUp(KRole role, int preLv) {
		// 忽略
	}

	@Override
	public void notifyRoleDataPutToCache(KRole role) {
		
	}

}
