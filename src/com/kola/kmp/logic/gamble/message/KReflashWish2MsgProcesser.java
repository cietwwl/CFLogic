package com.kola.kmp.logic.gamble.message;
import static com.kola.kmp.protocol.gamble.KGambleProtocol.CM_REFLASH_WISH2_DATA;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gamble.KGambleModule;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KReflashWish2MsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KReflashWish2MsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_REFLASH_WISH2_DATA;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		
		byte poolType = msg.readByte();

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		KGambleModule.getWish2Manager().reflashWishData(role, poolType,true);
		KDialogService.sendNullDialog(role);
	}

}
