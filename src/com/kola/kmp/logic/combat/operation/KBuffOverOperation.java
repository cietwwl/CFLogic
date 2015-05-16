package com.kola.kmp.logic.combat.operation;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.IOperationResult;

/**
 * 
 * @author PERRY CHAN
 */
public class KBuffOverOperation extends KOperationBaseImpl {

	private short _shadowId;
	
	public KBuffOverOperation(long pOpTime, short pShadowId) {
		super(pOpTime);
		this._shadowId = pShadowId;
	}

	@Override
	public IOperationResult executeOperation(ICombat combat) {
		if (combat.getCombatMember(_shadowId) != null) {
			LOGGER.info("buff over operation, time={}, shadowId={}, combatId={}", opTime, _shadowId, combat.getSerialId());
			combat.addSyncHpShadowId(_shadowId);
		} else {
			LOGGER.error("buff over operation, time={}, cannot find object:{} in combat:{}", opTime, _shadowId, combat.getSerialId());
		}
		return null;
	}
	
	@Override
	public int getPriority() {
		return PRIORITY_URGENT;
	}
}
