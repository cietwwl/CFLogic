package com.kola.kmp.logic.gm.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.gm.KGMLogic;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.protocol.gm.ProtocolGs;

public class KGMPushMsg implements ProtocolGs {

	public static void pushRoleLeaveToGM(KRole role) {
		KGamePlayerSession session = KGMLogic.getGMSession();
		if (session == null) {
			return;
		}

		KGameMessage msg = KGame.newLogicMessage(GS_GMS_PLAYER_ROLE_RELEAVED);
		msg.writeLong(role.getId());
		session.send(msg);
	}

}
