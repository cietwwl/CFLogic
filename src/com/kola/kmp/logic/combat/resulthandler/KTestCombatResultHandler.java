package com.kola.kmp.logic.combat.resulthandler;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.level.KLevelProtocol;

/**
 * 
 * @author PERRY CHAN
 */
public class KTestCombatResultHandler implements ICombatResultHandler {

	@Override
	public void processCombatResultToRole(long roleId, ICombat combat, ICombatResult combatResult) {
		KGameMessage sendMsg = KGame
				.newLogicMessage(KLevelProtocol.SM_SHOW_COMPLETE_GAME_LEVEL_RESULT);
		sendMsg.writeInt(1);
		sendMsg.writeByte(1);
		sendMsg.writeBoolean(false);
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, sendMsg);
	}

	@Override
	public void processCombatFinish(ICombat combat, ICombatResult combatResult) {
		
	}

	@Override
	public void processRoleExitCombatFinish(long roleId, ICombatResult result) {
		
	}

}
