package com.kola.kmp.logic.combat.mirror;

import com.kola.kmp.logic.combat.api.ICombatRole;
import com.kola.kmp.logic.combat.api.ICombatSkillSupport;

/**
 * 
 * 角色的战斗镜像数据
 * 
 * @author PERRY CHAN
 */
public class KCombatRoleMirrior extends KCombatMirrorFight implements ICombatRole, ICombatSkillSupport {
	
	private byte _job;
	private int _maxEnergy;
	private int _maxEnergyBean;
	private int _hpRecovery;
	private String _fightAI;
	private int _battlePower;
	private int _skillDmPctInc;
	private int _dmReducePct;
	private int _currentEnergy;
	private int _currentEnergyBean;

	/**
	 * 
	 */
	KCombatRoleMirrior(ICombatRole combatRole) {
		super(combatRole, combatRole, combatRole.getSkillSupport().getUsableSkills(), combatRole.getSkillSupport().getPassiveSkills());
		this._fightAI = combatRole.getAIId();
		this._job = combatRole.getJob();
		this._battlePower = combatRole.getBattlePower();
		this._skillDmPctInc = combatRole.getSkillDmPctInc();
		this._dmReducePct = combatRole.getDmReducePct();
		this._maxEnergy = combatRole.getMaxEnergy();
		this._maxEnergyBean = combatRole.getMaxEnergyBean();
		this._currentEnergy = combatRole.getCurrentEnergy();
		this._currentEnergyBean = combatRole.getEnergyBean();
	}
	
	@Override
	public int getDmReducePct() {
		return _dmReducePct;
	}
	
	@Override
	public int getSkillDmPctInc() {
		return _skillDmPctInc;
	}

	@Override
	public int getCurrentEnergy() {
		return _currentEnergy;
	}

	@Override
	public int getMaxEnergy() {
		return _maxEnergy;
	}

	@Override
	public int getEnergyBean() {
		return _currentEnergyBean;
	}

	@Override
	public int getMaxEnergyBean() {
		return _maxEnergyBean;
	}

	@Override
	public int getHpRecovery() {
		return _hpRecovery;
	}
	
	@Override
	public String getAIId() {
		return _fightAI;
	}
	
	@Override
	public byte getJob() {
		return _job;
	}
	
	@Override
	public int getBattlePower() {
		return _battlePower;
	}

	@Override
	public ICombatSkillSupport getSkillSupport() {
		return this;
	}

}
