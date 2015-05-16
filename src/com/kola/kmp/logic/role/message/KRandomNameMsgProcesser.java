package com.kola.kmp.logic.role.message;

import static com.kola.kmp.protocol.role.KRoleProtocol.CM_SYNC_GET_RANDOM_NAME;
import static com.kola.kmp.protocol.role.KRoleProtocol.SM_SYNC_GET_RANDOM_NAME;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.other.KGameGender;
import com.kola.kmp.logic.role.KRandomNameManager;
import com.kola.kmp.logic.role.KRoleModuleManager;
import com.kola.kmp.logic.role.KRoleTemplate;

/**
 *
 * @author PERRY CHAN
 */
public class KRandomNameMsgProcesser implements GameMessageProcesser {
	
	@Override
	public GameMessageProcesser newInstance() {
		return new KRandomNameMsgProcesser();
	}
	
	@Override
	public int getMsgIdHandled() {
		return CM_SYNC_GET_RANDOM_NAME;
	}
	
	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		int templateId = msgEvent.getMessage().readInt();
		KRoleTemplate template = KRoleModuleManager.getRoleTemplate(templateId);
		KGamePlayerSession session = msgEvent.getPlayerSession();
		String name = KRandomNameManager.getRandomName(session.getBoundPlayer().getID(), (template == null ? KGameGender.MALE.sign : template.gender.sign));
		KGameMessage msg = KGame.newLogicMessage(SM_SYNC_GET_RANDOM_NAME);
		msg.writeUtf8String(name);
		session.send(msg);
	}

	
}
