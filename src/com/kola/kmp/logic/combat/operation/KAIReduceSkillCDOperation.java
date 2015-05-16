package com.kola.kmp.logic.combat.operation;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.IOperationResult;

/**
 * 
 * @author PERRY CHAN
 */
public class KAIReduceSkillCDOperation extends KOperationBaseImpl {

	private short _shadowId;
	private int _skillId;
	private int _cdTime;
	
	public KAIReduceSkillCDOperation(short pShadowId, long happenTime, int pSkillId, int pCdTime) {
		super(happenTime);
		this._shadowId = pShadowId;
		this._skillId = pSkillId;
		this._cdTime = pCdTime;
	}
	
	@Override
	public IOperationResult executeOperation(ICombat combat) {
		ICombatMember member = combat.getCombatMember(_shadowId);
		if(member != null) {
			member.getSkillActor().reduceSkillCoolDown(_skillId, _cdTime);
		}
		return null;
	}
	
	@Override
	public int getPriority() {
		return PRIORITY_URGENT;
	}

}
