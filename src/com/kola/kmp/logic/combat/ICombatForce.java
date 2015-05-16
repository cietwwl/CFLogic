package com.kola.kmp.logic.combat;

import java.util.List;

/**
 * 
 * 战场“势力”封装
 * 
 * @author PERRY CHAN
 */
public interface ICombatForce {

	/**
	 * 战场“势力”：角色势力
	 */
	public static final byte FORCE_TYPE_ROLE_SIDE= 1;
	/**
	 * 战场“势力”：怪物势力
	 */
	public static final byte FORCE_TYPE_MONSTER_SIDE = 2;
	/**
	 * 战场“势力”：中立势力
	 */
	public static final byte FORCE_TYPE_NEUTRAL = 3;
	
	/**
	 * 
	 * 获取所有的成员
	 * 
	 * @return
	 */
	public List<ICombatMember> getAllMembers();
	
	/**
	 * 
	 */
	public void addMemberToForce(ICombatMember member);
	
	/**
	 * 
	 * @param members
	 */
	public void addAllMembersToForce(List<ICombatMember> members);
	
	/**
	 * 
	 */
	public void dispose();
}
