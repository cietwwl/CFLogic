package com.kola.kmp.logic.combat.skill;

import java.util.List;

import com.koala.game.logging.KGameLogger;
import org.slf4j.Logger;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.support.IOperationRecorder;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICombatSkillExecution {
	
	Logger LOGGER = KGameLogger.getLogger("combatLogger");
	/**
	 * 
	 * @param combat
	 * @param operator
	 * @param target
	 */
	public List<IOperationRecorder> execute(ICombat combat, ICombatMember operator, List<ICombatMember> targets, String useCode, long happenTime);
}
