package com.kola.kmp.logic.combat;

import com.koala.game.KGameMessage;
import com.kola.kmp.logic.combat.operation.IOperation;

/**
 * 
 * @author PERRY CHAN
 */
public interface IOperationResult {

	/**
	 * 操作类型：普通攻击
	 */
	public static final byte OPERATION_TYPE_NORMAL_ATTACK = IOperation.OPERATION_TYPE_NORMAL_ATTACK;
	/**
	 * 操作类型：技能攻击
	 */
	public static final byte OPERATION_TYPE_SKILL_ATTACK = IOperation.OPERATION_TYPE_SKILL_ATTACK;
	/**
	 * 操作类型：buff
	 */
	public static final byte OPERATION_TYPE_BUFF = IOperation.OPERATION_TYPE_OBSTRUCTION_BUFF;
	/**
	 * 操作类型：回血
	 */
	public static final byte OPERATION_TYPE_SYNC_HP = IOperation.OPERATION_TYPE_HP_RECOVERY;
	/**
	 * 操作类型：怒气恢复
	 */
	public static final byte OPERATION_TYPE_ENERGY_RECOVERY = IOperation.OPERATION_TYPE_ENERGY_RECOVERY;
	/**
	 * 
	 * @return
	 */
	public byte getOperationType();
	
	/**
	 * 
	 * 填充数据到消息当中
	 * 
	 * @param msg
	 */
	public void fillMsg(KGameMessage msg);
}
