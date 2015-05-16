package com.kola.kmp.logic.combat.operation;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.IOperationResult;

/**
 * 
 * @author PERRY CHAN
 */
public class KSwitchSecondWeaponOperation extends KOperationBaseImpl {

	private short _shadowId;
	private boolean _swichToSecond; // 是否切换到副武器
	
	public KSwitchSecondWeaponOperation(long opTime, short pShadowId, boolean pSwitchToSecond) {
		super(opTime);
		this._shadowId = pShadowId;
		this._swichToSecond = pSwitchToSecond;
	}
	@Override
	public IOperationResult executeOperation(ICombat combat) {
		ICombatMember member = combat.getCombatMember(_shadowId);
		if(member != null) {
			member.switchWeapon(_swichToSecond);
		}
		return null;
	}

	@Override
	public int getPriority() {
		return PRIORITY_URGENT;
	}
}
