package com.kola.kmp.logic.combat.state;

import java.util.List;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.api.ICombatSkillActor;
import com.kola.kmp.logic.combat.operation.IOperation;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICombatState {
	
	public static final Logger LOGGER = KGameLogger.getLogger("combatLogger");
	
	/**
	 * 
	 * 是否周期性buff
	 * 
	 * @return
	 */
	public boolean isCycState();
	
	/**
	 * 
	 * @return
	 */
	public long getStartTimeMillis();
	
	/**
	 * 
	 * 获取状态的模板id
	 * 
	 * @return
	 */
	public int getStateTemplateId();
	
	/**
	 * 
	 * @return
	 */
	public int getIconResId();
	
	/**
	 * 
	 * 获取状态的状态组id
	 * 
	 * @return
	 */
	public int getGroupId();
	
	/**
	 * 
	 * 获取状态的等级
	 * 
	 * @return
	 */
	public int getLevel();
	
	/**
	 * 
	 * 获取状态结束的时间
	 * 
	 * @return
	 */
	public long getEndTime();
	
	/**
	 * 
	 * @param combat
	 * @param actor
	 */
	public void notifyAdded(ICombat combat, ICombatMember target, long happenTime);

	/**
	 * 
	 * 通知扩展
	 * 
	 * @param happenTime
	 */
	public void notifyExtend(ICombat combat, ICombatMember target, long happenTime);
	
	/**
	 * 
	 * 周期性影响通知
	 * 
	 * @param combat
	 * @param target
	 */
	public void durationEffect(ICombat combat, ICombatSkillActor target, long happenTime);

	/**
	 * 
	 * 移除通知
	 * 
	 * @param combat
	 * @param actor
	 */
	public void notifyRemoved(ICombat combat, ICombatSkillActor actor);
	
	/**
	 * 
	 * 相对于传入的时间参数，状态是否会时效
	 * 
	 * @param time
	 * @return
	 */
	public boolean isTimeOut(long time);
	
	/**
	 * 
	 * 是否有效
	 * 
	 * @return
	 */
	public boolean isEffective();
	
	/**
	 * 
	 * 获取周期buff的某段时间内产生的指令
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public List<IOperation> getCycStateOperation(ICombat combat, ICombatMember target, long start, long end);
}
