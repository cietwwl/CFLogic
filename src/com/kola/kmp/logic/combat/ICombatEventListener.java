package com.kola.kmp.logic.combat;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICombatEventListener {

	/**
	 * 
	 * 获取监听的事件id
	 * 
	 * @return
	 */
	public int getEventId();
	
	/**
	 * 
	 * 执行事件
	 * 
	 * @return
	 */
	public void run(ICombat combat, ICombatMember operator, long happenTime);
	
	/**
	 * 
	 * 是否生效
	 * 
	 * @return
	 */
	public boolean isEffective(long happenTime);
}
