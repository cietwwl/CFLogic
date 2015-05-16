package com.kola.kmp.logic.combat.resulthandler;

import com.kola.kmp.logic.other.KGameBattlefieldTypeEnum;

/**
 * 
 * 战斗中，关于关卡的一些信息
 * 
 * @author PERRY CHAN
 */
public interface ICombatGameLevelInfo {

	/**
	 * 
	 * @return
	 */
	public int getLastBattleFieldId();
	
	/**
	 * 
	 * @return
	 */
	public KGameBattlefieldTypeEnum getLastBattleFieldType();
}
