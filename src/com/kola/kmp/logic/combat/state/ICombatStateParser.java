package com.kola.kmp.logic.combat.state;

import com.kola.kmp.logic.combat.ICombatMember;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICombatStateParser {

	/**
	 * 
	 * 根据模板的数据，返回一个新的战场buff状态
	 * 
	 * @param template
	 * @return
	 */
	public ICombatState newInstance(ICombatMember operator, ICombatStateTemplate template);
}
