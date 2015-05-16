package com.kola.kmp.logic.activity.message;

import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.activity.KActivityRoleExtCaCreator;
import com.kola.kmp.logic.activity.worldboss.KWorldBossRoleData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

import static com.kola.kmp.protocol.activity.KActivityProtocol.CM_SHUT_SHOW_INTRODUCE;

/**
 * 
 * @author PERRY CHAN
 */
public class KWorldBossShutIntroduceMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KWorldBossShutIntroduceMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_SHUT_SHOW_INTRODUCE;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(msgEvent.getPlayerSession());
		if (role != null) {
			KWorldBossRoleData worldBossRoleData = KActivityRoleExtCaCreator.getWorldBossRoleData(role.getId());
			worldBossRoleData.setShowIntroduce(false);
		}
	}

}
