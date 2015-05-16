package com.kola.kmp.logic.combat.mirror;

import com.kola.kmp.logic.combat.api.ICombatPet;
import com.kola.kmp.logic.combat.api.ICombatSkillSupport;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatPetMirror extends KCombatMirrorFight implements ICombatPet, ICombatSkillSupport {
	
	private long _ownerId;
	private String _aiid;
	private int _atkCountPerTime;
	private int _fullImmunityDuration;
	private int _fullImmunityItervation;
	
	public KCombatPetMirror(ICombatPet combatPet) {
		super(combatPet, combatPet, combatPet.getCombatSkillSupport().getUsableSkills(), combatPet.getCombatSkillSupport().getPassiveSkills());
		this._ownerId = combatPet.getOwnerId();
		this._aiid = combatPet.getAIId();
		this._atkCountPerTime = combatPet.getAtkCountPerTime();
		this._fullImmunityDuration = combatPet.getFullImmunityDuration();
		this._fullImmunityItervation = combatPet.getFullImmunityIterval();
	}
	
	@Override
	public long getOwnerId() {
		return _ownerId;
	}
	
	@Override
	public String getAIId() {
		return _aiid;
	}

	@Override
	public ICombatSkillSupport getCombatSkillSupport() {
		return this;
	}
	
	@Override
	public int getFullImmunityDuration() {
		return _fullImmunityDuration;
	}
	
	@Override
	public int getFullImmunityIterval() {
		return _fullImmunityItervation;
	}

	@Override
	public int getAtkCountPerTime() {
		return _atkCountPerTime;
	}
	

}
