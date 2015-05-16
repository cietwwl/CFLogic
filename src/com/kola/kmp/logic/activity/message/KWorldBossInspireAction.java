package com.kola.kmp.logic.activity.message;

import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.activity.worldboss.KWorldBossManager;
import com.kola.kmp.logic.activity.worldboss.KWorldBossMessageHandler;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.tips.GlobalTips;

import static com.kola.kmp.protocol.activity.KActivityProtocol.CM_REQUEST_INSPIRE;

/**
 * 
 * @author PERRY CHAN
 */
public class KWorldBossInspireAction implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KWorldBossInspireAction();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_REQUEST_INSPIRE;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(msgEvent.getPlayerSession());
		if(role != null) {
			CommonResult result = KWorldBossManager.getWorldBossActivity().processInspire(role);
			KDialogService.sendUprisingDialog(msgEvent.getPlayerSession(), result.tips);
		} else {
			KDialogService.sendUprisingDialog(msgEvent.getPlayerSession(), GlobalTips.getTipsServerBusy());
		}
	}
	
}
