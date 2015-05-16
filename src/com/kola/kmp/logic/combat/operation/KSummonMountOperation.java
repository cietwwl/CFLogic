package com.kola.kmp.logic.combat.operation;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.IOperationResult;
import com.kola.kmp.logic.combat.impl.KCombatConfig;

/**
 * 
 * @author PERRY CHAN
 */
public class KSummonMountOperation extends KOperationBaseImpl {

	private short _memberShadowId; // 操作者的id
	private boolean _up; // 是否召唤
	
	/**
	 * 
	 */
	KSummonMountOperation(short pMemberShadowId, long pHappenTime, boolean pUp) {
		super(pHappenTime);
		this._memberShadowId = pMemberShadowId;
		this._up = pUp;
	}
	
	@Override
	public IOperationResult executeOperation(ICombat combat) {
		ICombatMember member = combat.getCombatMember(_memberShadowId);
		if (member != null) {
			if (_up) {
				member.summonMount(opTime);
			} else {
				combat.releaseMount(member, opTime);
			}
		}
		return null;
	}
	
	@Override
	public int getPriority() {
		return PRIORITY_URGENT;
	}

}
