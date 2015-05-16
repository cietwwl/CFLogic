package com.kola.kmp.logic.map.message;


import static com.kola.kmp.protocol.map.KMapProtocol.CM_CHANGE_ROLE_SETTING;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.map.KMapModule;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KChangeRoleSettingMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KChangeRoleSettingMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_CHANGE_ROLE_SETTING;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		boolean isBlockPVP = msg.readBoolean();
		byte showLevel = msg.readByte();
		boolean isBlockChat = msg.readBoolean();
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		
		KMapModule.getGameMapManager().changeRoleSetting(role, isBlockPVP, showLevel, isBlockChat);
		
	}

}
