package com.kola.kmp.logic.combat.api;

import java.util.Map;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICombatDropInfoTemplate {
	
	/** 掉落类型：道具 */
	public static final byte DROP_TYPE_ITEM = 1;
	/** 掉落类型：随从 */
	public static final byte DROP_TYPE_PET = 2;
	/** 掉落类型：状态 */
	public static final byte DROP_TYPE_BUFF = 3;
	/** 掉落类型：生命补给 */
	public static final byte DROP_TYPE_HP = 4;
	/** 掉落类型：怒气 */
	public static final byte DROP_TYPE_ENERGY = 5;

	/**
	 * 
	 * @return
	 */
	public int getDropId();
	
	/**
	 * 
	 * @return
	 */
	public int getResId();
	
	/**
	 * 
	 * 获取掉落类型
	 * 
	 * @return
	 */
	public byte getDropType();
	
	/**
	 * 
	 * 根据几率判定，本次获取的时候，是否会掉落
	 * 
	 * @return
	 */
	public boolean willDrop();
	
	/**
	 * 
	 * @return
	 */
	public int getDropGold();
	
	/**
	 * 
	 * @return
	 */
	public Map<String, Integer> getDropItems();
	
	/**
	 * 
	 * @return
	 */
	public Map<Integer, Integer> getDropPets();
	
	/**
	 * 
	 * @return
	 */
	public int getDropStateId();
	
	/**
	 * 
	 * @return
	 */
	public int getDropHp();
	
	/**
	 * 
	 * @return
	 */
	public int getDropEnergy();
}
