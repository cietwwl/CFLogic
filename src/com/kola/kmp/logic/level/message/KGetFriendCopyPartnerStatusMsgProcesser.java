package com.kola.kmp.logic.level.message;
import static com.kola.kmp.protocol.level.KLevelProtocol.CM_GET_FRIEND_COPY_PARTNER_STATUS;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KGetFriendCopyPartnerStatusMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KGetFriendCopyPartnerStatusMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GET_FRIEND_COPY_PARTNER_STATUS;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		KGameLevelModuleExtension.getManager().getFriendCopyManager().processGetFriendCoolingTime(role);
	}

}
