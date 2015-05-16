package com.kola.kmp.logic.combat.api;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICombatSkillData {

	/**
	 * 
	 * @return
	 */
	public int getSkillTemplateId();
	
	/**
	 * 
	 * @return
	 */
	public int getLv();
	
	/**
	 * <pre>
	 * 是否超级技能
	 * </pre>
	 * @return
	 */
	public boolean isSuperSkill();
	
	/**
	 * 
	 * @return
	 */
	public boolean onlyEffectInPVP();
}
