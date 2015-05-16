package com.kola.kmp.logic.activity.message;

import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.activity.worldboss.KWorldBossManager;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

import static com.kola.kmp.protocol.activity.KActivityProtocol.CM_REQUEST_LEAVE_WORLD_BOSS_ACTIVITY;

/**
 * 
 * @author PERRY CHAN
 */
public class KWorldBossLeaveActivityMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KWorldBossLeaveActivityMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_REQUEST_LEAVE_WORLD_BOSS_ACTIVITY;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(msgEvent.getPlayerSession());
		if (role != null) {
			KWorldBossManager.getWorldBossActivity().leaveActivity(role);
		}
	}

}
