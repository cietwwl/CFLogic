package com.kola.kmp.logic.combat.operation;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.IOperationResult;

/**
 * 
 * @author PERRY CHAN
 */
public interface IOperation {
	
	public static final Logger LOGGER = KGameLogger.getLogger("combatLogger");

	/**
	 * 操作类型：普通攻击
	 */
	public static final byte OPERATION_TYPE_NORMAL_ATTACK = 1;
	/**
	 * 操作类型：技能攻击
	 */
	public static final byte OPERATION_TYPE_SKILL_ATTACK = 2;
	/**
	 * 操作类型：buff
	 */
	public static final byte OPERATION_TYPE_OBSTRUCTION_BUFF = 3;
	/**
	 * 操作类型：拾取
	 */
	public static final byte OPERATION_TYPE_PICK = 4;
	/**
	 * 操作类型：上下载具
	 */
	public static final byte OPERATION_TYPE_MOUNT = 5;
	/**
	 * 操作类型：回血
	 */
	public static final byte OPERATION_TYPE_HP_RECOVERY = 6;
	/**
	 * 操作类型：怒气恢复
	 */
	public static final byte OPERATION_TYPE_ENERGY_RECOVERY = 7;
	/**
	 * 操作类型：切换场景
	 */
	public static final byte OPERATION_TYPE_SWITCH_SENCE = 8;
	/**
	 * 操作类型：切换副武器
	 */
	public static final byte OPERATION_TYPE_SWITCH_WEAPON = 9;
	/**
	 * 操作类型：聚力
	 */
	public static final byte OPERATION_TYPE_COHESION = 10;
	/**
	 * 操作类型：格挡切换
	 */
	public static final byte OPERATION_TYPE_BLOCK_SWITCH = 11;
	/**
	 * 操作类型：buff执行结束
	 */
	public static final byte OPERATION_TYPE_BUFF_OVER = 12;
	/**
	 * 操作类型：客户端宣布死亡
	 */
	public static final byte OPERATION_TYPE_CLIENT_CLAIM_DEAD = 13;
	/**
	 * 操作类型：自爆
	 */
	public static final byte OPERATION_TYPE_SELF_EXPLOSE = 14;
	/**
	 * 操作类型：AI指令
	 */
	public static final byte OPERATION_TYPE_AI_CMD = 15;
	
	/**
	 * AI指令类型：加buff
	 */
	public static final byte AI_CMD_BUFF = 1;
	/**
	 * AI指令类型：修改属性
	 */
	public static final byte AI_CMD_ATTR_MODIFY = 2;
	/**
	 * AI指令：加特殊状态（定身、霸体。。。）
	 */
	public static final byte AI_CMD_STATE = 3;
	/**
	 * AI指令：
	 */
	public static final byte AI_CMD_CD_REDUCE = 4;
	
	/**
	 * 优先级：普通
	 */
	public static final int PRIORITY_NORMAL = 1;
	
	/**
	 * 优先级：紧急
	 */
	public static final int PRIORITY_URGENT = 2;
	
	/**
	 * 
	 * 执行操作
	 * 
	 * @param combat
	 */
	public IOperationResult executeOperation(ICombat combat);
	
	/**
	 * 
	 * 本次operation发生的时间
	 * 
	 * @return
	 */
	public long getOperationTime();
	
	/**
	 * 
	 * @return
	 */
	public int getPriority();
	
	/**
	 * 
	 * @param master
	 * @param mount
	 */
	public void notifyMountAdded(ICombatMember master, ICombatMember mount);
	
	/**
	 * 
	 * @param master
	 * @param mount
	 */
	public void notifyMountReleased(ICombatMember master, ICombatMember mount);
}
