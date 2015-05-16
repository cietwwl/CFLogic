package com.kola.kmp.logic.combat.mirror;

import java.util.Map;

import com.kola.kmp.logic.combat.api.ICombatMount;
import com.kola.kmp.logic.other.KGameAttrType;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatMountMirror extends KCombatMirrorBase implements ICombatMount {

//	private int _duration;
//	private float _reduceDuration;
	private Map<Integer, Integer> _beanTime;
	private float _speedUpTimes;
	private int _atkCountPerTime;
	private int _fullImmunityDuration;
	private int _fullImmunityIterval;
	private String _ai;
	private Map<KGameAttrType, Integer> _basicAttrs;
	private Map<KGameAttrType, Integer> _equipmentAttrs;
	
	/**
	 * @param base
	 * @param pActivateSkills
	 * @param pPassiveSkills
	 */
	protected KCombatMountMirror(ICombatMount mount) {
		super(mount, mount.getUsableSkills(), mount.getPassiveSkills());
//		this._duration = mount.getDuration();
//		this._reduceDuration = mount.getReduceDuration();
//		this._beanTime = mount.getBeanTime();
		this._beanTime = mount.getBeanTime();
		this._speedUpTimes = mount.getSpeedUpTimes();
		this._atkCountPerTime = mount.getAtkCountPerTime();
		this._fullImmunityDuration = mount.getFullImmunityDuration();
		this._fullImmunityIterval = mount.getFullImmunityIteration();
		this._ai = mount.getAI();
		this._basicAttrs = mount.getBasicAttrs();
		this._equipmentAttrs = mount.getEquipmentAttrs();
	}

//	@Override
//	public int getDuration() {
//		return _duration;
//	}
//
//	@Override
//	public float getReduceDuration() {
//		return _reduceDuration;
//	}

	@Override
	public float getSpeedUpTimes() {
		return _speedUpTimes;
	}
	
	@Override
	public int getAtkCountPerTime() {
		return _atkCountPerTime;
	}
	
	@Override
	public int getFullImmunityDuration() {
		return _fullImmunityDuration;
	}
	
	@Override
	public int getFullImmunityIteration() {
		return _fullImmunityIterval;
	}

	@Override
	public String getAI() {
		return _ai;
	}

	@Override
	public Map<KGameAttrType, Integer> getBasicAttrs() {
		return _basicAttrs;
	}

	@Override
	public Map<KGameAttrType, Integer> getEquipmentAttrs() {
		return _equipmentAttrs;
	}

	@Override
	public Map<Integer, Integer> getBeanTime() {
		return _beanTime;
	}

}
