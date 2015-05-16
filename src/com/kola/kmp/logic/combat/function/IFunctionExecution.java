package com.kola.kmp.logic.combat.function;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;

/**
 * 
 * @author PERRY CHAN
 */
public interface IFunctionExecution {
	
	/** 10000 */
	public static final int TEN_THOUSAND = UtilTool.TEN_THOUSAND_RATIO_UNIT;
	
	/** 功能类型：添加状态 */
	public static final int FUNCTION_TYPE_ADD_STATE = 1;
	
	/** 功能类型：添加怒气 */
	public static final int FUNCTION_TYPE_ADD_ENERGY = 2;
	
	/** 功能类型：吸血*/
	public static final int FUNCTION_TYPE_ABSORB_HP = 3;
	
	public static final Logger LOGGER = KGameLogger.getLogger("combatLogger");

	/**
	 * 
	 * 执行特殊效果
	 * 
	 * @param actor
	 */
	public void execute(ICombat combat, ICombatMember operator, long happenTime);
}
