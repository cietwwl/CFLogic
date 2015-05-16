package com.kola.kmp.logic.combat.api;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICombatObjectFight {

	/**
	 * 
	 * 获取攻击力
	 * 
	 * @return
	 */
	public int getAtk();
	
	/**
	 * 
	 * 获取防御力
	 * 
	 * @return
	 */
	public int getDef();
	
	/**
	 * 
	 * 获取命中等级
	 * 
	 * @return
	 */
	public int getHitRating();
	
	/**
	 * 
	 * 获取闪避等级
	 * 
	 * @return
	 */
	public int getDodgeRating();
	
	/**
	 * 
	 * 获取暴击等级
	 * 
	 * @return
	 */
	public int getCritRating();
	
	/**
	 * 
	 * @return
	 */
	public int getCritMultiple();
	
	/**
	 * 
	 * @return
	 */
	public int getCdReduce();
	
	/**
	 * 
	 * @return
	 */
	public int getHpAbsorb();
	
	/**
	 * 
	 * @return
	 */
	public int getDefIgnore();
	
	/**
	 * 
	 * 获取抗爆等级
	 * 
	 * @return
	 */
	public int getResilienceRating();
	
	/**
	 * 
	 * 获取眩晕抵抗等级
	 * 
	 * @return
	 */
	public int getFaintResistRating();
	
	/**
	 * 
	 * 获取近程攻击速度
	 * 
	 * @return
	 */
	public int getShortRaAtkItr();
	
	/**
	 * 
	 * 获取远程攻击速度
	 * 
	 * @return
	 */
	public int getLongRaAtkItr();
	
	/**
	 * 
	 * 获取近程攻击距离
	 * 
	 * @return
	 */
	public int getShortRaAtkDist();
	
	/**
	 * 
	 * 获取远程攻击距离
	 * 
	 * @return
	 */
	public int getLongRaAtkDist();
	
	/**
	 * 
	 * @return
	 */
	public int[] getNormalAtkAudioResIdArray();
	
	/**
	 * 
	 * @return
	 */
	public int[] getOnHitAudioResIdArray();
	
	/**
	 * 
	 * @return
	 */
	public int[] getInjuryAudioResIdArray();
	
	/**
	 * 
	 * @return
	 */
	public int[] getDeadAudioResIdArray();
}
