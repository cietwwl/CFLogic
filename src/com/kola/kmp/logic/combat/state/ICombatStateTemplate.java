package com.kola.kmp.logic.combat.state;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICombatStateTemplate {

	/** 状态类型：扣减属性 */
	public static final int STATE_TYPE_DECREASE_ATTR = 1;
	/** 状态类型：增加属性 */
	public static final int STATE_TYPE_INCREASE_ATTR = 2;
	/** 状态类型：定身 */
	public static final int STATE_TYPE_FREEZE = 3;
	/** 状态类型：眩晕 */
	public static final int STATE_TYPE_FAINT = 4;
	/** 状态类型：霸体 */
	public static final int STATE_TYPE_IMMUNITY_CONTROL = 5;
	/** 状态类型：周期扣减固定属性 */
	public static final int STATE_TYPE_DEC_FIXED_ATTR_CYC = 6;
	/** 状态类型：周期扣减百分比属性 */
	public static final int STATE_TYPE_DEC_PCT_ATTR_CYC = 7;
	/** 状态类型：攻击时有几率给自己加属性 */
	public static final int STATE_TYPE_ADD_STATE_TO_OWN = 8;
	/** 状态类型：攻击时有几率给敌方加属性 */
	public static final int STATE_TYPE_ADD_STATE_TO_TARGET = 9;
	/** 状态类型：吸收HP */
	public static final int STATE_TYPE_ABSORB_HP = 10;
	/**
	 * 
	 * @return
	 */
	public int getStateTemplateId();
	
	/**
	 * 
	 * @return
	 */
	public String getStateName();
	
	/**
	 * 
	 * @return
	 */
	public String getStateDesc();
	
	/**
	 * 
	 * @return
	 */
	public int getStateType();
	
	/**
	 * 
	 * @return
	 */
	public int[] getParas();
	
	/**
	 * 
	 * @return
	 */
	public int getStateIcon();
	
	/**
	 * 
	 * @return
	 */
	public int getResId();
	
	/**
	 * 
	 * @return
	 */
	public int getGroupId();
	
	/**
	 * 
	 * @return
	 */
	public int getLevel();
}
