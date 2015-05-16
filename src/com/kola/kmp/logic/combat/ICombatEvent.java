package com.kola.kmp.logic.combat;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICombatEvent {

	/** 攻击后 */
	public static final int EVENT_AFTER_ATTACK = 1;
	/** 治疗后 */
	public static final int EVENT_AFTER_CURE = 2;
	/** 自身死亡 */
	public static final int EVENT_SELF_DEAD = 3;
	/** 受击后 */
	public static final int EVENT_UNDER_ATTACK = 4;
}
