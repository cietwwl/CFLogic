package com.kola.kmp.logic.activity.message;

import static com.kola.kmp.protocol.activity.KActivityProtocol.CM_CONFIRM_WORLD_BOSS_COMBAT_RESULT;

import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.activity.worldboss.KWorldBossActivityMain;
import com.kola.kmp.logic.activity.worldboss.KWorldBossManager;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 
 * @author PERRY CHAN
 */
public class KWorldBossConfirmCombatResultMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KWorldBossConfirmCombatResultMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_CONFIRM_WORLD_BOSS_COMBAT_RESULT;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(msgEvent.getPlayerSession());
		if (role != null) {
			KWorldBossManager.getWorldBossActivity().confirmCombatResult(role);
		}
	}

}
