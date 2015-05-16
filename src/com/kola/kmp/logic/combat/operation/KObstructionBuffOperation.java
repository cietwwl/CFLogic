package com.kola.kmp.logic.combat.operation;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.IOperationResult;
import com.kola.kmp.logic.combat.impl.KCombatManager;

/**
 * 
 * 障碍物buff指令
 * 
 * @author PERRY CHAN
 */
public class KObstructionBuffOperation extends KOperationBaseImpl {

	private int _buffId;
	private short _obstructionId;
	private short[] _targetIds;
	
	
	KObstructionBuffOperation(long pOpTime, int pBuffId, short pObstructionId, short[] pTargetIds) {
		super(pOpTime);
		this._buffId = pBuffId;
		this._obstructionId = pObstructionId;
		this._targetIds = pTargetIds;
	}
	
	@Override
	public IOperationResult executeOperation(ICombat combat) {
		if (_targetIds.length> 0) {
			LOGGER.info("障碍物buff指令，障碍物id={}，buffId={}，targetIds={}", _obstructionId, _buffId, _targetIds);
			ICombatMember member = combat.getCombatMember(_obstructionId);
			int checkStateId;
			if (member.isAlive()) {
				checkStateId = KCombatManager.getObstStateByAttack(member.getSrcObjTemplateId());
			} else {
				checkStateId = KCombatManager.getObstStateAfterDestroyed(member.getSrcObjTemplateId());
			}
			if (checkStateId == _buffId) {
				ICombatMember temp;
				for (int i = 0; i < _targetIds.length; i++) {
					temp = combat.getCombatMember(_targetIds[i]);
					if (temp != null && temp.isAlive()) {
						temp.getSkillActor().addState(member, _buffId, opTime);
					}
//					else {
//						LOGGER.info("障碍物buff指令，目标id={}，不存在此目标或目标已死亡！", _targetIds[i]);
//					}
				}
			}
		}
		return null;
	}
	
	@Override
	public int getPriority() {
		return PRIORITY_URGENT;
	}

}
