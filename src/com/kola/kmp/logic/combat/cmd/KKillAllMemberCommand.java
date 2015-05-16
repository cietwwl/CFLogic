package com.kola.kmp.logic.combat.cmd;

import java.util.List;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatForce;
import com.kola.kmp.logic.combat.ICombatMember;

/**
 * 
 * @author PERRY CHAN
 */
public class KKillAllMemberCommand implements ICombatCommand {

	private ICombat _combat;
	private boolean _killAllMonsters;
	
	public KKillAllMemberCommand(ICombat combat, boolean killAllMonsters) {
		this._combat = combat;
		this._killAllMonsters = killAllMonsters;
	}
	
	@Override
	public void execute() {
		if(_combat.isTerminal()) {
			return;
		}
		long currentTime = System.currentTimeMillis();
		List<ICombatMember> allMembers;
		ICombatMember currentMember;
		if (_killAllMonsters) {
			ICombatMember roleMember = _combat.getCombatMemberByType(ICombatMember.MEMBER_TYPE_ROLE).get(0);
			List<ICombatForce> allForces = _combat.getAllEnermyForces();
			long currentHp;
			for (int i = 0; i < allForces.size(); i++) {
				allMembers = allForces.get(i).getAllMembers();
				for (int k = 0; k < allMembers.size(); k++) {
					currentMember = allMembers.get(k);
					if (currentMember.isGeneralMonster() && currentMember.isAlive()) {
						currentHp = currentMember.getCurrentHp();
						currentMember.decreaseHp(currentHp, currentTime);
						roleMember.getCombatRecorder().recordDm(currentHp);
						roleMember.getCombatRecorder().recordKillMember(currentMember);
						ICombat.LOGGER.info("战场id：{}，强制杀死怪物：{},{}", _combat.getSerialId(), currentMember.getName(), currentMember.getShadowId());
					}
				}
			}
		} else {
			ICombatForce force = _combat.getForce(ICombatForce.FORCE_TYPE_ROLE_SIDE);
			allMembers = force.getAllMembers();
			for(int i = 0; i < allMembers.size(); i++) {
				currentMember = allMembers.get(i);
				if(currentMember.isAlive()) {
					currentMember.decreaseHp(currentMember.getCurrentHp(), currentTime);
					ICombat.LOGGER.info("战场id：{}，强制杀死角色单位：{},{}", _combat.getSerialId(), currentMember.getName(), currentMember.getShadowId());
				}
			}
		}
	}

}
