package com.kola.kmp.logic;

import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.gamble.wish2.KWish2Manager;
import com.kola.kmp.logic.role.IRoleEventListener;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.protocol.role.KRoleProtocol;

public class KGlobalModuleRoleEventListener implements IRoleEventListener {

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {
		List<INotification> list = KNotificationCenter.checkNotificationList(role);
		KGameMessage msg = KGame.newLogicMessage(KRoleProtocol.SM_NOTIFICATION_LIST);
		msg.writeByte(list.size());
		if (list.size() > 0) {
			INotification temp;
			for (int i = 0; i < list.size(); i++) {
				temp = list.get(i);
				msg.writeInt(temp.getYear());
				msg.writeInt(temp.getMonth());
				msg.writeInt(temp.getDay());
				msg.writeInt(temp.getHour());
				msg.writeInt(temp.getMinute());
				msg.writeByte(INotificationTemplate.REPEAT_TYPE_NONE);
				msg.writeUtf8String(temp.getContent());
			}
		}
		session.send(msg);
	}

	@Override
	public void notifyRoleLeavedGame(KRole role) {
		
	}

	@Override
	public void notifyRoleCreated(KGamePlayerSession session, KRole role) {
		
	}

	@Override
	public void notifyRoleDeleted(long roleId) {
		
	}

	@Override
	public void notifyRoleLevelUp(KRole role, int preLv) {
		
	}
	
	@Override
	public void notifyRoleDataPutToCache(KRole role) {
		KWish2Manager.notifyRoleDataInitComplete(role);
	}
	
}
