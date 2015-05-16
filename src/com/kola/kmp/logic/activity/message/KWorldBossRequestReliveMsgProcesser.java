package com.kola.kmp.logic.activity.message;

import static com.kola.kmp.protocol.activity.KActivityProtocol.CM_SYNC_REQUEST_ALIVE;
import static com.kola.kmp.protocol.activity.KActivityProtocol.SM_SYNC_REQUEST_ALIVE;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.activity.worldboss.KWorldBossManager;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;

/**
 * 
 * @author PERRY CHAN
 */
public class KWorldBossRequestReliveMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KWorldBossRequestReliveMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_SYNC_REQUEST_ALIVE;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(msgEvent.getPlayerSession());
		if (role != null) {
			CommonResult result = KWorldBossManager.getWorldBossActivity().requestRelive(role);
			KGameMessage msg = KGame.newLogicMessage(SM_SYNC_REQUEST_ALIVE);
			msg.writeBoolean(result.isSucess);
			if(!result.isSucess) {
				msg.writeUtf8String(result.tips);
			}
			msgEvent.getPlayerSession().send(msg);
		}
	}

}
