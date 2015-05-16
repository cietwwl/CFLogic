package com.kola.kmp.logic.combat.cmd;

import java.util.ArrayList;
import java.util.List;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatForce;
import com.kola.kmp.logic.combat.ICombatMember;

public class KForceFinishCombatCommand implements ICombatCommand {

	private ICombat _combat;
	private boolean _roleWin;
	
	public KForceFinishCombatCommand(ICombat combat, boolean isRoleWin) {
		this._combat = combat;
		this._roleWin = isRoleWin;
	}
	
	@Override
	public void execute() {
		List<ICombatMember> list;
		if(_roleWin) {
			list = new ArrayList<ICombatMember>();
			List<ICombatForce> allForces = _combat.getAllEnermyForces();
			for(int i = 0; i < allForces.size(); i++) {
				list.addAll(allForces.get(i).getAllMembers());
			}
		} else {
			list = _combat.getForce(ICombatForce.FORCE_TYPE_ROLE_SIDE).getAllMembers();
		}
		for(int i = 0; i < list.size(); i++) {
			list.get(i).sentenceToDead(System.currentTimeMillis());
		}
	}

}
