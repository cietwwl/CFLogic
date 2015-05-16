package com.kola.kmp.logic.combat.operation;

import java.util.List;

import org.slf4j.Logger;

import com.koala.game.KGameMessage;
import com.koala.game.logging.KGameLogger;
import com.kola.kmp.logic.combat.ICombat;

/**
 * 
 * @author PERRY CHAN
 */
public interface IOperationMsgHandler {
	
	Logger LOGGER = KGameLogger.getLogger("combatLogger");
	
	public List<IOperation> handleOperationMsg(long roleId, ICombat combat, KGameMessage msg);
}
