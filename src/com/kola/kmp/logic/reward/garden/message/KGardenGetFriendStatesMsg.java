package com.kola.kmp.logic.reward.garden.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.reward.garden.KGardenCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.reward.KRewardProtocol;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-12-12 上午10:11:40
 * </pre>
 */
public class KGardenGetFriendStatesMsg implements GameMessageProcesser, KRewardProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KGardenGetFriendStatesMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GARDEN_GET_FRIENDSTATE;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		KGameMessage backmsg = KGame.newLogicMessage(SM_GARDEN_SYN_FRIENDSTATE);
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			backmsg.writeShort(0);
		} else {
			KGardenCenter.packFriendSates(role, backmsg);
		}
		// -------------
		// -------------
		session.send(backmsg);
	}
}
