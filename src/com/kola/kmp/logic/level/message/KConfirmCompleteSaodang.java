package com.kola.kmp.logic.level.message;

import static com.kola.kmp.protocol.level.KLevelProtocol.CM_CONFIRM_COMPLETE_SAODANG;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KConfirmCompleteSaodang implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KConfirmCompleteSaodang();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_CONFIRM_COMPLETE_SAODANG;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		KDialogService.sendNullDialog(session);
		KGameLevelModuleExtension.getManager().confirmPlayerRoleCompleteSaodang(role);
	}

}
