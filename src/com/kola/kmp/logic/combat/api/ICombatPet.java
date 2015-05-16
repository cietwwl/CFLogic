package com.kola.kmp.logic.combat.api;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICombatPet extends ICombatObjectBase, ICombatObjectFight {
	
	/**
	 * 
	 * @return
	 */
	public long getOwnerId();
	
	/**
	 * 
	 * @return
	 */
	public String getAIId();
	
	/**
	 * 
	 * @return
	 */
	public ICombatSkillSupport getCombatSkillSupport();
	
	/**
	 * 
	 * 
	 * @return
	 */
	public int getFullImmunityDuration();
	
	/**
	 * 
	 * 
	 * @return
	 */
	public int getFullImmunityIterval();
	
	/**
	 * 
	 * @return
	 */
	public int getAtkCountPerTime();
}
