package com.kola.kmp.logic.combat;

import com.koala.game.KGameMessage;
import com.kola.kmp.logic.combat.api.ICombatObjectBase;
import com.kola.kmp.logic.combat.api.ICombatObjectFight;
import com.kola.kmp.logic.combat.api.ICombatSkillSupport;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICombatMinion extends ICombatObjectBase, ICombatObjectFight {
	
	/**
	 * 
	 * @param msg
	 */
	public void packDataToMsg(KGameMessage msg);
	
	/**
	 * 
	 * @return
	 */
	public byte getForceType();
	
	/**
	 * 
	 * @return
	 */
	public ICombatSkillSupport getSKillSupport();
	
	/**
	 * 
	 * @return
	 */
	public int getDuration();
	
	/**
	 * 
	 * @return
	 */
	public boolean isFullImmunity();
	
	/**
	 * 
	 * @return
	 */
	public int getFullImmunityDuration();
	
	/**
	 * 
	 * @return
	 */
	public int getFullImmunityIteration();
}
