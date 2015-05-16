package com.kola.kmp.logic.combat.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.kola.kmp.logic.combat.ICombatForce;
import com.kola.kmp.logic.combat.ICombatMember;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatForceImpl implements ICombatForce {

	private List<ICombatMember> _allMembers = new ArrayList<ICombatMember>();
	private List<ICombatMember> _allMembersReadOnly = Collections.unmodifiableList(_allMembers);
	
	
	public KCombatForceImpl() {
	}
	
	/**
	 * 
	 * @param pMemberListToAdd 需要添加到成员列表的成员
	 */
	KCombatForceImpl(List<ICombatMember> pMemberListToAdd) {
		this._allMembers.addAll(pMemberListToAdd);
	}
	
	@Override
	public List<ICombatMember> getAllMembers() {
		return _allMembersReadOnly;
	}
	
	@Override
	public void addMemberToForce(ICombatMember member) {
		this._allMembers.add(member);
	}
	
	@Override
	public void addAllMembersToForce(List<ICombatMember> members) {
		this._allMembers.addAll(members);
	}
	
	@Override
	public void dispose() {
		this._allMembers.clear();
	}

}
