package com.kola.kmp.logic.mount;

import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.item.message.KPushItemsMsg;
import com.kola.kmp.logic.mount.message.KPushMountMsg;
import com.kola.kmp.logic.role.IRoleEventListener;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KMountEventListener implements IRoleEventListener {

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {
		
		// 发送消息给客户端
		KPushMountMsg.SM_PUSH_MOUNTDATA(role);
		KPushMountMsg.SM_PUSH_MOUNT_CONSTANCE(role);
	}

	@Override
	public void notifyRoleLeavedGame(/*KGamePlayerSession session,*/KRole role) {
		// 忽略
	}

	@Override
	public void notifyRoleCreated(KGamePlayerSession session, KRole role) {
		KMountLogic.presentMountForLv(role);
	}

	@Override
	public void notifyRoleDeleted(long roleId) {
		// 忽略
	}

	@Override
	public void notifyRoleLevelUp(KRole role, int preLv) {
		KMountLogic.presentMountForLv(role);
	}
	
	@Override
	public void notifyRoleDataPutToCache(KRole role) {
		
	}
}
