package com.kola.kmp.logic.map.message;


import static com.kola.kmp.protocol.map.KMapProtocol.CM_JUMP_MAP;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.map.KMapModule;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KJumpMapMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KJumpMapMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_JUMP_MAP;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		int exitId = msg.readInt();
		int srcMapId = msg.readInt();
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		
		boolean result = KMapModule.getGameMapManager().processPlayerRoleJumpMap(role, srcMapId, exitId);
		if(!result){
			KDialogService.sendUprisingDialog(role, "跳转地图错误。");
		}
	}

}
